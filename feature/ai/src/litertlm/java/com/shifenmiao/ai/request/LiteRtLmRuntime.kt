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
 * - cancel 通过 [Conversation.cancelProcess] 打断 native 推理。
 *
 * 并发约定同接口注释:engine / conversations 的可变状态只在 [mutex] 内读写,
 * 流式 collect 本身不持锁,避免 cancel / release 被饿死。
 */
@Singleton
class LiteRtLmRuntime @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocalLlmRuntime {

    private val mutex = Mutex()
    private var engine: Engine? = null
    private var loadedModelId: String? = null

    /** 当前 Engine 是否为 GPU 后端。部分设备(如模拟器)GPU 初始化延迟到首次推理才失败,
     *  generate 捕获到首批 token 之前的失败时据此做一次 CPU 回退重试。 */
    private var currentBackendIsGpu = false
    private val conversations = mutableMapOf<String, Conversation>()
    private val cancelledSessionIds = mutableSetOf<String>()

    override suspend fun prepare(model: LocalLlmModelSpec): LocalLlmPrepareResult = mutex.withLock {
        if (model.modelPath.isBlank() || !File(model.modelPath).isFile) {
            return@withLock LocalLlmPrepareResult.Failure(LocalLlmError.ModelFileMissing)
        }
        engine?.let { current ->
            if (loadedModelId == model.id) {
                return@withLock LocalLlmPrepareResult.Success(loadedModelId = model.id)
            }
            // 切换模型前先释放旧 Engine
            closeEngine(current)
            engine = null
            loadedModelId = null
        }
        val created = createEngine(model, Backend.GPU())
            ?.also { currentBackendIsGpu = true }
            ?: createEngine(model, Backend.CPU())
                ?.also { currentBackendIsGpu = false }
            ?: return@withLock LocalLlmPrepareResult.Failure(LocalLlmError.RuntimeLoadFailed)
        engine = created
        loadedModelId = model.id
        LocalLlmPrepareResult.Success(loadedModelId = model.id)
    }

    override fun generate(request: LocalLlmGenerateRequest): Flow<LocalLlmGenerateEvent> = flow {
        val prepared = mutex.withLock {
            engine?.takeIf { loadedModelId == request.model.id }
        }
        if (prepared == null) {
            emit(LocalLlmGenerateEvent.Failed("Local model is not prepared: ${request.model.id}"))
            return@flow
        }
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
            if (failure == null) {
                emit(LocalLlmGenerateEvent.Completed(finishReason = "stop"))
                return@flow
            }
            val cancelled = mutex.withLock { cancelledSessionIds.remove(request.sessionId) }
            if (cancelled) {
                emit(LocalLlmGenerateEvent.Failed("cancelled"))
                return@flow
            }
            // GPU 初始化在部分设备(如模拟器)上延迟到首次推理才失败:
            // 首批 token 之前的失败,回退 CPU 重建 Engine 重试一次;已产出 token 不重试,避免重复输出
            val canCpuRetry = !emittedAnyToken && !cpuRetried && mutex.withLock { currentBackendIsGpu }
            if (!canCpuRetry) {
                emit(LocalLlmGenerateEvent.Failed(failure.message ?: "Local generation failed"))
                return@flow
            }
            cpuRetried = true
            val cpuEngine = mutex.withLock {
                engine?.let { closeEngine(it) }
                val created = createEngine(request.model, Backend.CPU())
                engine = created
                currentBackendIsGpu = false
                if (created == null) loadedModelId = null
                created
            }
            if (cpuEngine == null) {
                emit(LocalLlmGenerateEvent.Failed("Local generation failed and CPU fallback unavailable"))
                return@flow
            }
            currentEngine = cpuEngine
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
        mutex.withLock { conversations[request.sessionId] = conversation }

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
            cancelledSessionIds.add(sessionId)
            conversations[sessionId]
        }
        conversation?.let { runCatching { it.cancelProcess() } }
    }

    override suspend fun release(modelId: String) = mutex.withLock {
        if (loadedModelId != modelId) return@withLock
        conversations.values.forEach { conversation ->
            runCatching { conversation.cancelProcess() }
            runCatching { conversation.close() }
        }
        conversations.clear()
        cancelledSessionIds.clear()
        engine?.let { closeEngine(it) }
        engine = null
        loadedModelId = null
        currentBackendIsGpu = false
    }

    /** Engine.initialize 为阻塞调用(大模型可达数秒),放到 Default dispatcher。 */
    private suspend fun createEngine(model: LocalLlmModelSpec, backend: Backend): Engine? =
        withContext(Dispatchers.Default) {
            val candidate = Engine(
                EngineConfig(
                    modelPath = model.modelPath,
                    backend = backend,
                    maxNumTokens = model.contextWindowTokens,
                    cacheDir = context.cacheDir.absolutePath,
                )
            )
            try {
                candidate.initialize()
                candidate
            } catch (e: CancellationException) {
                throw e
            } catch (_: Throwable) {
                if (candidate.isInitialized()) runCatching { candidate.close() }
                null
            }
        }

    /** 调用方须持有 [mutex];释放权重耗时,切到 Default 且不可取消。 */
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
    }
}
