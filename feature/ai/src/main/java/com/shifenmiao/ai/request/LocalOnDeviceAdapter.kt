package com.shifenmiao.ai.request

import com.shifenmiao.model.ai.AiProvider
import com.shifenmiao.model.ai.Conversation
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Stub 实现：用于 Phase 1 主链路打通，不依赖 native 库。
 *
 * 行为：
 * - prepare 始终成功（只要 spec 不为空）；
 * - generate 逐 token 输出 "stub: ..." 文本，便于 UI/事件层联调；
 * - 真实 llama.cpp / MediaPipe / ONNX 实现后续替换 [LocalLlmRuntime] 绑定即可，
 *   Gateway 与 LocalOnDeviceAdapter 不需要任何改动。
 *
 * 替换方式：在 Hilt 中把 [LocalLlmRuntime] 绑定到真实实现（如 LlamaCppLocalLlmRuntime），
 * 当前 Stub 自然不再被使用。
 */
@Singleton
class StubLocalLlmRuntime @Inject constructor() : LocalLlmRuntime {

    // 实现规范：native runtime 必须在内部串行化。
    // Stub 用 Mutex 演示这一约束；真实 llama.cpp / MediaPipe 实现同样需要。
    private val mutex = Mutex()
    private var currentSessionId: String? = null

    override suspend fun prepare(model: LocalLlmModelSpec): LocalLlmPrepareResult = mutex.withLock {
        if (model.modelPath.isBlank()) {
            return@withLock LocalLlmPrepareResult.Failure(LocalLlmError.ModelFileMissing)
        }
        LocalLlmPrepareResult.Success(loadedModelId = model.id)
    }

    override fun generate(request: LocalLlmGenerateRequest): Flow<LocalLlmGenerateEvent> = flow {
        // currentSessionId 的读写必须在 mutex 内，避免两次并发的 generate 互相覆盖取消标记。
        mutex.withLock { currentSessionId = request.sessionId }
        // 用 ChatTemplate 把 messages 渲染成 prompt 字符串（与真实 runtime 一致），
        // 便于联调时确认模板正确性。
        val prompt = request.model.chatTemplate.render(request.messages)
        val reply = buildString {
            append("[local-stub model=")
            append(request.model.displayName)
            append("] received: ")
            append(prompt.take(120).replace("\n", " "))
            append(if (prompt.length > 120) "..." else "")
        }
        for (ch in reply.chunked(4)) {
            delay(20)
            val cancelled = mutex.withLock { currentSessionId != request.sessionId }
            if (cancelled) {
                emit(LocalLlmGenerateEvent.Failed("cancelled"))
                return@flow
            }
            emit(LocalLlmGenerateEvent.Token(ch))
        }
        emit(LocalLlmGenerateEvent.Completed(finishReason = "stop"))
    }

    override suspend fun cancel(sessionId: String) {
        currentSessionId = null
    }

    override suspend fun release(modelId: String) = mutex.withLock {
        // Stub 不持有 native 资源，no-op
    }
}

/**
 * 端侧 Adapter。
 *
 * 当前职责：
 * 1. 从 Conversation 解析 LocalLlmModelSpec（Phase 1 由 [LocalLlmModelRegistry] 提供桩实现，
 *    Phase 2 改为 LocalLlmModelEntity 查询）；
 * 2. 调用 runtime.prepare()，失败转 Error；
 * 3. 调 runtime.generate()，把 LocalLlmGenerateEvent 翻译成 LlmStreamEvent。
 *
 * 不负责：文件导入、下载、hash 校验、UI、Agent orchestration。
 */
class LocalOnDeviceAdapter @Inject constructor(
    private val runtime: LocalLlmRuntime,
    private val modelRegistry: LocalLlmModelRegistry,
) : LlmProviderAdapter {

    override val protocol: com.shifenmiao.model.ai.AiRequestProtocol =
        com.shifenmiao.model.ai.AiRequestProtocol.LOCAL_ON_DEVICE

    override fun streamTurn(
        conversation: Conversation,
        request: com.shifenmiao.model.ai.unified.LlmTurnRequest
    ): Flow<com.shifenmiao.model.ai.unified.LlmStreamEvent> = flow {
        val modelName = request.model
        val spec = modelRegistry.resolve(modelName)
        if (spec == null) {
            emit(
                com.shifenmiao.model.ai.unified.LlmStreamEvent.Error(
                    errorMessage = "Local model not registered: $modelName"
                )
            )
            return@flow
        }

        val prepared = runtime.prepare(spec)
        if (prepared is LocalLlmPrepareResult.Failure) {
            emit(
                com.shifenmiao.model.ai.unified.LlmStreamEvent.Error(
                    errorMessage = prepared.error.toMessage()
                )
            )
            return@flow
        }

        emit(
            com.shifenmiao.model.ai.unified.LlmStreamEvent.ResponseStarted(
                responseId = "local-${UUID.randomUUID()}",
                model = modelName,
            )
        )

        val sessionId = "local-session-${UUID.randomUUID()}"
        val model = conversation.engine.model
        // maxTokens 取模型配置与 spec 上限的较小值，避免 spec 配置错误时一次输出撑爆上下文。
        val maxTokens = minOf(spec.maxOutputTokens, model.maxTokens)
            .takeIf { it > 0 }
            ?: spec.maxOutputTokens
        val generateRequest = LocalLlmGenerateRequest(
            sessionId = sessionId,
            model = spec,
            messages = request.messages,
            temperature = model.temperature,
            topP = model.topP,
            maxTokens = maxTokens,
            stop = spec.chatTemplate.stopTokens,
        )

        runtime.generate(generateRequest).collect { event ->
            when (event) {
                is LocalLlmGenerateEvent.Token ->
                    emit(com.shifenmiao.model.ai.unified.LlmStreamEvent.TextDelta(event.text))
                is LocalLlmGenerateEvent.Completed ->
                    emit(
                        com.shifenmiao.model.ai.unified.LlmStreamEvent.Completed(
                            finishReason = event.finishReason
                        )
                    )
                is LocalLlmGenerateEvent.Failed ->
                    emit(
                        com.shifenmiao.model.ai.unified.LlmStreamEvent.Error(
                            errorMessage = event.message
                        )
                    )
            }
        }
    }
}

private fun LocalLlmError.toMessage(): String = when (this) {
    LocalLlmError.ModelFileMissing -> "Local model file is missing"
    LocalLlmError.UnsupportedAbi -> "Device ABI not supported by local runtime"
    LocalLlmError.NotEnoughMemory -> "Not enough memory to load local model"
    LocalLlmError.RuntimeLoadFailed -> "Local runtime failed to load model"
    LocalLlmError.ContextTooLong -> "Local model context window exceeded"
    LocalLlmError.GenerationCancelled -> "Local generation cancelled"
    is LocalLlmError.Unknown -> message
}
