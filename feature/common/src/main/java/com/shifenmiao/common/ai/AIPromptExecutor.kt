package com.shifenmiao.common.ai

import com.google.gson.Gson
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.ChatCompletionChunk
import com.shifenmiao.model.ai.ChatCompletionRequest
import com.shifenmiao.model.ai.RequestMessage
import com.shifenmiao.model.ai.RoleType
import com.shifenmiao.network.AiRequestUrlResolver
import com.shifenmiao.network.api.OpenAICompatibleService
import com.shifenmiao.network.api.OwnProxyAIService
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.logger.makeLog
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 全局 AI 提示词执行器，输入系统提示词和用户内容即可同步获取当前工作引擎大模型的生成内容。
 *
 * 使用方式：
 * ```
 * @Inject lateinit var aiPromptExecutor: AIPromptExecutor
 *
 * // 在协程中调用（systemPrompt 可选，input 必填）
 * val result = aiPromptExecutor.execute(
 *     systemPrompt = "你是一个翻译助手，只输出翻译结果",
 *     input = "帮我翻译这段话：Hello World"
 * )
 * if (result.isSuccess) {
 *     val content = result.content  // AI 生成的内容
 * } else {
 *     val error = result.errorMessage  // 错误信息
 * }
 * ```
 */
@Singleton
class AIPromptExecutor @Inject constructor(
    private val aiEngineManager: AIEngineManager,
    private val openAICompatibleService: OpenAICompatibleService,
    private val ownProxyAIService: OwnProxyAIService,
    dispatchersHolder: DispatchersHolder,
) : DispatchersHolder by dispatchersHolder {

    private val gson = Gson()

    enum class EngineMode {
        DEFAULT,
        FAST,
        DUEL_A,
        DUEL_B,
    }

    suspend fun execute(
        input: String,
        systemPrompt: String = "",
        engineMode: EngineMode = EngineMode.DEFAULT,
    ): AIPromptResult {
        val engine = resolveEngine(engineMode)

        if (engine.name.isBlank()) {
            return AIPromptResult(
                content = "",
                isSuccess = false,
                errorMessage = "AI engine not configured",
            )
        }

        // Phase 1 保护：本地引擎当前没有走 HTTP 链路的能力，
        // 直接在调用 URL Resolver 之前拦截，避免 error() 抛出崩溃。
        // Phase 2 计划：构造 LlmTurnRequest(stream = false) 走 LlmRequestGateway。
        if (engine.requestProtocol == AiRequestProtocol.LOCAL_ON_DEVICE) {
            return AIPromptResult(
                content = "",
                isSuccess = false,
                errorMessage = "Local on-device engine is not yet supported by AIPromptExecutor (Phase 2)",
                engineName = engine.name,
                modelName = engine.model.name,
            )
        }

        val messages = buildList {
            if (systemPrompt.isNotBlank()) {
                add(
                    RequestMessage.createTextMessage(
                        role = RoleType.SYSTEM.value,
                        text = systemPrompt
                    )
                )
            }
            add(
                RequestMessage.createTextMessage(
                    role = RoleType.USER.value,
                    text = input
                )
            )
        }

        val request = ChatCompletionRequest(
            model = engine.model.name,
            messages = messages,
            stream = false,
        )

        return try {
            val isProxyRoute = AiRequestUrlResolver.shouldUseProxyRequest(engine)
            val response = withContext(ioDispatcher) {
                val url = AiRequestUrlResolver.resolveRequestUrl(engine)
                if (AiRequestUrlResolver.shouldUseDirectRequest(engine)) {
                    val authorization = AiRequestUrlResolver.resolveAuthorizationHeader(engine)
                    openAICompatibleService.chatNoStreaming(
                        url = url,
                        authorization = authorization,
                        chatCompletionRequest = request
                    ).execute()
                } else {
                    ownProxyAIService.chatNoStreaming(
                        url = url,
                        chatCompletionRequest = request
                    ).execute()
                }
            }

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string().orEmpty()
                makeLog { "AIPromptExecutor: HTTP ${response.code()} - $errorBody" }
                return AIPromptResult(
                    content = "",
                    isSuccess = false,
                    errorMessage = "HTTP ${response.code()}: ${errorBody.take(200)}",
                    engineName = engine.name,
                    modelName = engine.model.name,
                )
            }

            val body = response.body()?.string()
            if (body.isNullOrBlank()) {
                return AIPromptResult(
                    content = "",
                    isSuccess = false,
                    errorMessage = "Empty response body",
                    engineName = engine.name,
                    modelName = engine.model.name,
                )
            }

            val chunk = try {
                gson.fromJson(body, ChatCompletionChunk::class.java)
            } catch (e: Exception) {
                makeLog { "AIPromptExecutor: JSON parse failed: ${e.message}" }
                return AIPromptResult(
                    content = "",
                    isSuccess = false,
                    errorMessage = "Failed to parse response: ${e.message}",
                    engineName = engine.name,
                    modelName = engine.model.name,
                )
            }

            if (chunk.errorCode != 0 || chunk.errorMsg.isNotBlank()) {
                return AIPromptResult(
                    content = "",
                    isSuccess = false,
                    errorMessage = chunk.errorMsg.ifBlank { "API error code: ${chunk.errorCode}" },
                    engineName = engine.name,
                    modelName = engine.model.name,
                )
            }

            val content = chunk.choices.firstOrNull()?.message?.content.orEmpty()
            AIPromptResult(
                content = content,
                isSuccess = true,
                engineName = engine.name,
                modelName = engine.model.name,
                totalTokens = chunk.usage?.totalTokens ?: 0,
                isProxyRoute = isProxyRoute,
            )
        } catch (t: Throwable) {
            makeLog { "AIPromptExecutor: Request failed: ${t.message}" }
            AIPromptResult(
                content = "",
                isSuccess = false,
                errorMessage = t.message ?: t.toString(),
                engineName = engine.name,
                modelName = engine.model.name,
            )
        }
    }

    private fun resolveEngine(engineMode: EngineMode): AiEngine {
        return when (engineMode) {
            EngineMode.DEFAULT -> aiEngineManager.getCurrentAiEngine()
            EngineMode.FAST -> aiEngineManager.getFastAiEngine()
            EngineMode.DUEL_A -> aiEngineManager.getDuelEngineA()
            EngineMode.DUEL_B -> aiEngineManager.getDuelEngineB()
        }
    }
}
