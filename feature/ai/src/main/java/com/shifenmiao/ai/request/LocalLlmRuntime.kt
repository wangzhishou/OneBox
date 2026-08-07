package com.shifenmiao.ai.request

import com.shifenmiao.model.ai.unified.LlmMessage
import kotlinx.coroutines.flow.Flow

/**
 * 端侧本地 LLM 推理抽象。
 *
 * 业务侧只依赖该接口，不直接接触 llama.cpp / MediaPipe / ONNX Runtime 等具体实现。
 * 第一阶段：实现可以是 Stub（直接产出 Error 或固定回放），用于打通主链路；
 * 第二阶段：替换为 LlamaCppLocalLlmRuntime 等真实 native 实现。
 *
 * 实现约束（必读）：
 * - prepare / generate / cancel / release 必须在内部串行化（推荐用单线程 dispatcher
 *   + Mutex 包外层调用），避免同一 native 上下文被并发访问导致崩溃或 OOM。
 * - 同一时间只能持有一个 loaded modelId；切换模型前调用 release。
 * - native 推理循环未必响应协程取消，cancel() 必须显式打断 native 线程。
 */
interface LocalLlmRuntime {

    /**
     * 准备 / 加载模型。
     * - 同一时间只允许加载一个本地模型，调用方需保证串行；
     * - 内部应使用单线程 dispatcher，避免与 generate 抢资源。
     * - 失败应返回 [LocalLlmPrepareResult.Failure]，由 Adapter 转成 [LlmStreamEvent.Error]。
     */
    suspend fun prepare(model: LocalLlmModelSpec): LocalLlmPrepareResult

    /**
     * 执行一次推理。返回的 Flow 至少包含：
     * - 0..N 个 [LocalLlmGenerateEvent.Token]
     * - 1 个 [LocalLlmGenerateEvent.Completed] 或 [LocalLlmGenerateEvent.Failed]
     */
    fun generate(request: LocalLlmGenerateRequest): Flow<LocalLlmGenerateEvent>

    /** 取消指定 session 的生成。native 推理循环未必响应协程取消，必须显式调用。 */
    suspend fun cancel(sessionId: String)

    /** 释放指定模型的 native 资源。 */
    suspend fun release(modelId: String)
}

sealed interface LocalLlmPrepareResult {
    data class Success(val loadedModelId: String) : LocalLlmPrepareResult
    data class Failure(val error: LocalLlmError) : LocalLlmPrepareResult
}

/**
 * 端侧模型规格。独立于 [com.shifenmiao.model.ai.AiEngine]，
 * 避免污染云端协议的 URL / Path / API Key 字段。
 */
data class LocalLlmModelSpec(
    val id: String,
    val displayName: String,
    val modelPath: String,
    val tokenizerPath: String? = null,
    val chatTemplate: LocalChatTemplate = LocalChatTemplate.Generic,
    val backend: LocalLlmBackend = LocalLlmBackend.LLAMA_CPP,
    val contextWindowTokens: Int = 4096,
    val maxOutputTokens: Int = 1024,
    val quantization: String = "Q4_K_M",
    val estimatedMemoryMb: Int = 0,
    val supportsToolCalls: Boolean = false,
    val supportsVision: Boolean = false,
)

/**
 * Chat template 抽象。设计要点：把 system / user / assistant 段以及
 * generation prompt、stop tokens 拆开，方便小模型（Qwen / Llama / Gemma）按需覆盖。
 */
sealed interface LocalChatTemplate {
    val systemPrefix: String
    val userPrefix: String
    val assistantPrefix: String
    val generationPrompt: String
    val stopTokens: List<String>

    /**
     * 将 LLM 消息列表渲染为单个 prompt 字符串，供 native runtime 一次性输入。
     *
     * 默认实现按 system / user / assistant 顺序拼接，每段以模板前缀 + 内容 + 段尾分隔符
     * 结束；末尾追加 [generationPrompt] 以提示模型开始生成。
     *
     * 子类覆盖：实现各自的 message 段格式（如 ChatML / Llama-3 / Gemma）。
     */
    fun render(messages: List<LlmMessage>): String {
        if (messages.isEmpty()) return generationPrompt
        val builder = StringBuilder()
        for (message in messages) {
            val text = message.textContent()
            when (message.role.lowercase()) {
                "system" -> builder.append(systemPrefix).append(text).append('\n')
                "user" -> builder.append(userPrefix).append(text).append('\n')
                "assistant" -> builder.append(assistantPrefix).append(text).append('\n')
                // TODO(Phase 4): Phase 1 本地模型 supportToolCalls = false，tool 消息不会到达。
                // Phase 4 启用 Prompt-based / 原生 tool calling 时，需按 ChatML / Llama-3 等模板
                // 包装 tool result（通常作为用户消息的一个 block，附 tool_call_id 与 name）。
                "tool" -> builder.append(userPrefix).append(text).append('\n')
                else -> builder.append(text).append('\n')
            }
        }
        builder.append(generationPrompt)
        return builder.toString()
    }

    data object Generic : LocalChatTemplate {
        override val systemPrefix: String = "<|system|>\n"
        override val userPrefix: String = "<|user|>\n"
        override val assistantPrefix: String = "<|assistant|>\n"
        override val generationPrompt: String = "<|assistant|>\n"
        override val stopTokens: List<String> = listOf("<|end|>", "<|endoftext|>")
    }

    data object Qwen25 : LocalChatTemplate {
        override val systemPrefix: String = "<|im_start|>system\n"
        override val userPrefix: String = "<|im_start|>user\n"
        override val assistantPrefix: String = "<|im_start|>assistant\n"
        override val generationPrompt: String = "<|im_start|>assistant\n"
        override val stopTokens: List<String> = listOf("<|im_end|>")
    }

    data object Llama3 : LocalChatTemplate {
        override val systemPrefix: String = "<|start_header_id|>system<|end_header_id|>\n\n"
        override val userPrefix: String = "<|start_header_id|>user<|end_header_id|>\n\n"
        override val assistantPrefix: String = "<|start_header_id|>assistant<|end_header_id|>\n\n"
        override val generationPrompt: String = "<|start_header_id|>assistant<|end_header_id|>\n\n"
        override val stopTokens: List<String> = listOf("<|eot_id|>")
    }
}

enum class LocalLlmBackend {
    LLAMA_CPP,
    MEDIAPIPE,
    ONNX_RUNTIME,
    MNN,
    EXECUTORCH,
}

data class LocalLlmGenerateRequest(
    val sessionId: String,
    val model: LocalLlmModelSpec,
    val messages: List<LlmMessage>,
    val temperature: Double = 0.7,
    val topP: Double = 0.9,
    val maxTokens: Int = 1024,
    val stop: List<String> = emptyList(),
)

sealed interface LocalLlmGenerateEvent {
    data class Token(val text: String) : LocalLlmGenerateEvent
    data class Completed(val finishReason: String? = null) : LocalLlmGenerateEvent
    data class Failed(val message: String) : LocalLlmGenerateEvent
}

sealed interface LocalLlmError {
    data object ModelFileMissing : LocalLlmError
    data object UnsupportedAbi : LocalLlmError
    data object NotEnoughMemory : LocalLlmError
    data object RuntimeLoadFailed : LocalLlmError
    data object ContextTooLong : LocalLlmError
    data object GenerationCancelled : LocalLlmError
    data class Unknown(val message: String) : LocalLlmError
}
