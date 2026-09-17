package com.shifenmiao.ai.request

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.SamplerConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import com.t8rin.logger.makeLog
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * LiteRT-LM 真实端侧运行时,替换 [StubLocalLlmRuntime]。
 *
 * 行为:
 * - prepare 校验模型文件存在后初始化 Engine,优先 GPU 后端,失败回退 CPU;
 *   Engine 按 modelId 缓存,重复 prepare 同一模型直接复用,切换模型先释放旧 Engine。
 *   注意部分设备(如模拟器)GPU 初始化延迟到首次推理才失败,此类失败由 generate
 *   在首批 token 之前捕获并做一次 CPU 回退重试。
 * - generate 每次新建 [Conversation]:system 消息进 systemInstruction,其余历史进
 *   initialMessages,最后一条作为本轮输入;流式 chunk 映射为 Token 事件。
 *   LiteRT-LM 按模型自带 chat template 渲染,不再走 [LocalChatTemplate.render];
 *   request.stop 的字符串没有原生对应参数,这里做输出后处理截断。
 * - cancel 通过 [Conversation.cancelProcess] 打断 native 推理;取消的轮次一律以
 *   Failed("cancelled") 收场,不会误报 Completed。
 *
 * 并发约定(两把锁,满足接口"native runtime 内部串行化"约束):
 * - [mutex] 只护可变状态(engine / loadedModelId / conversations / cancelledSessionIds /
 *   activeSessionId / currentBackendIsGpu)的读写,临界区短平快;
 *   Engine 创建 / initialize / close 等耗时操作一律在锁外进行。
 * - [inferenceMutex] 串行化整个推理过程(streamConversation + CPU 回退重试循环);
 *   协程取消按结构化取消上抛,锁随之释放。
 * - 新轮次接管:generate 排队进 [inferenceMutex] 之前,若仍有其他 sessionId 在推理,
 *   先 cancel(旧sessionId) 打断其 native 推理,避免新旧两轮互相等死。
 * - releaseAll 由 Application.onTrimMemory(TRIM_MEMORY_MODERATE 及以上)触发,
 *   任何时刻调用都安全(幂等),是内存压力下的保命路径。
 */
@Singleton
class LiteRtLmRuntime @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocalLlmRuntime {

    private val mutex = Mutex()
    private val inferenceMutex = Mutex()
    private var engine: Engine? = null
    private var loadedModelId: String? = null

    /** 当前 Engine 是否为 GPU 后端。部分设备(如模拟器)GPU 初始化延迟到首次推理才失败,
     *  generate 捕获到首批 token 之前的失败时据此做一次 CPU 回退重试。 */
    private var currentBackendIsGpu = false
    private val conversations = mutableMapOf<String, Conversation>()
    private val cancelledSessionIds = mutableSetOf<String>()

    /** 正在推理(持有 inferenceMutex)的 sessionId,供新轮次接管与 cancel 判活。 */
    private var activeSessionId: String? = null

    override suspend fun prepare(model: LocalLlmModelSpec): LocalLlmPrepareResult {
        if (model.modelPath.isBlank() || !File(model.modelPath).isFile) {
            return LocalLlmPrepareResult.Failure(LocalLlmError.ModelFileMissing)
        }
        // 锁内只做缓存命中判断:目标模型已加载直接复用
        val cached = mutex.withLock { engine?.takeIf { loadedModelId == model.id } }
        if (cached != null) return LocalLlmPrepareResult.Success(loadedModelId = model.id)

        // Engine 创建与 initialize(大模型可达数秒)在锁外进行,不堵 cancel / releaseAll
        val created = createEngine(model, Backend.GPU())?.let { it to true }
            ?: createEngine(model, Backend.CPU())?.let { it to false }
            ?: return LocalLlmPrepareResult.Failure(LocalLlmError.RuntimeLoadFailed)
        val (newEngine, isGpu) = created

        // 回锁内存放。若锁外期间别的 prepare 已加载同一模型(race),复用已有的、
        // 关掉自己新建的;否则释放旧 Engine(切换模型或锁外被 releaseAll 清空),安放新 Engine
        var engineToClose: Engine? = null
        mutex.withLock {
            val existing = engine
            if (existing != null && loadedModelId == model.id) {
                engineToClose = newEngine
            } else {
                engineToClose = existing
                engine = newEngine
                loadedModelId = model.id
                currentBackendIsGpu = isGpu
            }
        }
        // 释放权重耗时,同样放在锁外
        engineToClose?.let { closeEngine(it) }
        return LocalLlmPrepareResult.Success(loadedModelId = model.id)
    }

    override fun generate(request: LocalLlmGenerateRequest): Flow<LocalLlmGenerateEvent> = flow {
        // 新轮次接管:仍有其他 session 在推理时先打断它,再排队等推理锁;
        // 旧轮次会在流结束/失败后读到 cancelled 标记,以 Failed("cancelled") 收场
        mutex.withLock { activeSessionId }
            ?.takeIf { it != request.sessionId }
            ?.let { cancel(it) }

        inferenceMutex.withLock {
            val prepared = mutex.withLock {
                engine?.takeIf { loadedModelId == request.model.id }
            }
            if (prepared == null) {
                emit(LocalLlmGenerateEvent.Failed("Local model is not prepared: ${request.model.id}"))
                return@withLock
            }
            mutex.withLock { activeSessionId = request.sessionId }
            try {
                var currentEngine: Engine = prepared
                var emittedAnyToken = false
                var cpuRetried = false
                while (true) {
                    val failure = try {
                        streamConversation(currentEngine, request) { delta ->
                            emittedAnyToken = true
                            emit(LocalLlmGenerateEvent.Token(delta))
                        }
                        null
                    } catch (e: CancellationException) {
                        throw e
                    } catch (t: Throwable) {
                        t
                    }
                    // 成功与失败出口都要查取消标记:cancelProcess() 可能让流正常结束(无异常),
                    // 用户取消必须上报 Failed("cancelled") 而非 Completed
                    val cancelled = mutex.withLock { cancelledSessionIds.contains(request.sessionId) }
                    if (cancelled) {
                        emit(LocalLlmGenerateEvent.Failed("cancelled"))
                        return@withLock
                    }
                    if (failure == null) {
                        emit(LocalLlmGenerateEvent.Completed(finishReason = "stop"))
                        return@withLock
                    }
                    // GPU 初始化在部分设备(如模拟器)上延迟到首次推理才失败:
                    // 首批 token 之前的失败,回退 CPU 重建 Engine 重试一次;已产出 token 不重试,避免重复输出
                    val canCpuRetry = !emittedAnyToken && !cpuRetried && mutex.withLock { currentBackendIsGpu }
                    if (!canCpuRetry) {
                        emit(LocalLlmGenerateEvent.Failed(failure.message ?: "Local generation failed"))
                        return@withLock
                    }
                    cpuRetried = true
                    // CPU 重建同样在状态锁外进行,避免数秒初始化堵住 cancel
                    makeLog { "LiteRtLmRuntime: GPU inference failed before first token, fallback to CPU. cause=${failure.message}" }
                    val cpuEngine = createEngine(request.model, Backend.CPU())
                    if (cpuEngine == null) {
                        val stale = mutex.withLock {
                            val e = engine
                            engine = null
                            loadedModelId = null
                            currentBackendIsGpu = false
                            e
                        }
                        stale?.let { closeEngine(it) }
                        emit(LocalLlmGenerateEvent.Failed("Local generation failed and CPU fallback unavailable"))
                        return@withLock
                    }
                    val replaced = mutex.withLock {
                        val old = engine
                        engine = cpuEngine
                        currentBackendIsGpu = false
                        old
                    }
                    replaced?.let { closeEngine(it) }
                    currentEngine = cpuEngine
                }
            } finally {
                // 取消标记与活跃标记在所有出口统一清理(含 CancellationException 出口),避免无界增长
                withContext(NonCancellable) {
                    mutex.withLock {
                        if (activeSessionId == request.sessionId) activeSessionId = null
                        cancelledSessionIds.remove(request.sessionId)
                    }
                }
            }
        }
    }

    /** 单次会话流式输出;正常结束与命中 stop 串均正常返回,失败抛异常交由调用方处理。 */
    private suspend fun streamConversation(
        currentEngine: Engine,
        request: LocalLlmGenerateRequest,
        onDelta: suspend (String) -> Unit,
    ) {
        val nonSystem = request.messages.filterNot { it.role.equals("system", ignoreCase = true) }
        val outgoing = nonSystem.lastOrNull()?.toLiteRtMessage()
            ?: throw IllegalStateException("No message to send")
        val systemText = request.messages
            .filter { it.role.equals("system", ignoreCase = true) }
            .joinToString("\n") { it.textContent() }
        val maxOutputToken = request.maxTokens.takeIf { it > 0 }
        val config = ConversationConfig(
            systemInstruction = systemText.takeIf { it.isNotBlank() }?.let(Contents::of),
            initialMessages = nonSystem.dropLast(1).map { it.toLiteRtMessage() },
            samplerConfig = SamplerConfig(
                topK = DEFAULT_TOP_K,
                topP = request.topP.coerceIn(0.0, 1.0),
                temperature = request.temperature.coerceAtLeast(0.0),
            ),
            maxOutputToken = maxOutputToken,
        )
        val conversation = withContext(Dispatchers.Default) { currentEngine.createConversation(config) }
        val alreadyCancelled = mutex.withLock {
            conversations[request.sessionId] = conversation
            cancelledSessionIds.contains(request.sessionId)
        }
        // 注册窗口内已被 cancel(如新轮次接管),立即打断,不空转 native 推理
        if (alreadyCancelled) runCatching { conversation.cancelProcess() }

        try {
            val fullText = StringBuilder()
            conversation.sendMessageAsync(outgoing, maxOutputToken = maxOutputToken)
                .collect { chunk ->
                    val delta = chunk.text()
                    if (delta.isEmpty()) return@collect
                    fullText.append(delta)
                    val stopIndex = request.stop
                        .filter { it.isNotEmpty() }
                        .mapNotNull { s -> fullText.indexOf(s).takeIf { it >= 0 } }
                        .minOrNull()
                    if (stopIndex != null) {
                        // 命中 stop 串:只发截断后的增量,随后打断 native 推理
                        val prevLen = fullText.length - delta.length
                        if (stopIndex > prevLen) {
                            onDelta(fullText.substring(prevLen, stopIndex))
                        }
                        conversation.cancelProcess()
                        throw StopSequenceReached()
                    }
                    onDelta(delta)
                }
        } catch (_: StopSequenceReached) {
            // 命中 stop 串,按正常结束处理
        } finally {
            mutex.withLock { conversations.remove(request.sessionId) }
            runCatching { conversation.close() }
        }
    }

    override suspend fun cancel(sessionId: String) {
        val conversation = mutex.withLock {
            val conv = conversations[sessionId]
            // 只对确实活跃的 session 打取消标记,避免对早已结束的 session 滞留标记
            if (conv != null || activeSessionId == sessionId) cancelledSessionIds.add(sessionId)
            conv
        }
        conversation?.let { runCatching { it.cancelProcess() } }
    }

    override suspend fun releaseAll() {
        val (activeConversations, staleEngine) = mutex.withLock {
            val convs = conversations.values.toList()
            conversations.clear()
            cancelledSessionIds.clear()
            val e = engine
            engine = null
            loadedModelId = null
            currentBackendIsGpu = false
            convs to e
        }
        // 打断与关闭放在锁外:进行中的 generate 会因流断裂以 Failed 收场,
        // 其 finally 自行清理残余状态
        activeConversations.forEach { conversation ->
            runCatching { conversation.cancelProcess() }
            runCatching { conversation.close() }
        }
        staleEngine?.let { closeEngine(it) }
    }

    /** Engine.initialize 为阻塞调用(大模型可达数秒),放到 Default dispatcher。 */
    private suspend fun createEngine(model: LocalLlmModelSpec, backend: Backend): Engine? =
        withContext(Dispatchers.Default) {
            val candidate = Engine(
                EngineConfig(
                    modelPath = model.modelPath,
                    backend = backend,
                    maxNumTokens = model.contextWindowTokens,
                    // 推理编译/权重缓存(GPU mldrift + XNNPack,单模型可达 ~1.2GB)必须放 filesDir:
                    // cacheDir 会被 installd 按配额随时清空(实测 S23 Ultra 上写入 45s 后即被 purge),
                    // 一旦缓存丢失,每次模型加载都要全量重建,冷启动后首次推理慢一个数量级
                    cacheDir = inferenceCacheDir().absolutePath,
                )
            )
            try {
                candidate.initialize()
                makeLog { "LiteRtLmRuntime: engine initialized model=${model.id} backend=${if (backend is Backend.GPU) "GPU" else "CPU"}" }
                candidate
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                makeLog { "LiteRtLmRuntime: engine init failed model=${model.id} backend=${if (backend is Backend.GPU) "GPU" else "CPU"}: $t" }
                if (candidate.isInitialized()) runCatching { candidate.close() }
                null
            }
        }

    private fun inferenceCacheDir(): File =
        File(context.filesDir, INFERENCE_CACHE_DIR_NAME).apply { mkdirs() }

    /** 释放权重耗时,切到 Default 且不可取消;调用方不得持有 [mutex]。 */
    private suspend fun closeEngine(target: Engine) {
        withContext(NonCancellable + Dispatchers.Default) {
            if (target.isInitialized()) runCatching { target.close() }
        }
    }

    private fun com.shifenmiao.model.ai.unified.LlmMessage.toLiteRtMessage(): Message =
        when (role.lowercase()) {
            "assistant", "model" -> Message.model(textContent())
            else -> Message.user(textContent())
        }

    private fun Message.text(): String =
        contents.contents.filterIsInstance<Content.Text>().joinToString("") { it.text }

    private class StopSequenceReached : Exception()

    private companion object {
        const val DEFAULT_TOP_K = 40
        const val INFERENCE_CACHE_DIR_NAME = "litertlm_cache"
    }
}
