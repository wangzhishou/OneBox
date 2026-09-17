package com.shifenmiao.common.ai

import com.google.gson.Gson
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.core.R
import com.shifenmiao.storage.TokenStorage
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
import com.t8rin.imagetoolbox.core.utils.getString
import com.t8rin.logger.makeLog
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
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

    /**
     * 计费策略(见 onebox-doc「AI 功能登录与积分规范」):
     *
     * - [MANAGED](默认):由本执行器统一执行「登录门槛 + 积分预估闸门 + 成功后按 token 扣减」,
     *   调用方不必自己再写一遍,也就不会再漏掉;
     * - [EXTERNAL]:调用方自行处理登录/积分。用于两类场景:① 已经在自己 Service 里扣费的
     *   (诗词赏析/拼音/翻译、易经解读、记录解读);② 有意免费或自动触发的
     *   (象棋 AI 走棋、里程碑文案、会话标题摘要)。
     */
    enum class PromptBilling { MANAGED, EXTERNAL }

    suspend fun execute(
        input: String,
        systemPrompt: String = "",
        engineMode: EngineMode = EngineMode.DEFAULT,
        billing: PromptBilling = PromptBilling.MANAGED,
        billingDesc: String = "",
    ): AIPromptResult {
        val engine = resolveEngine(engineMode) ?: return onDeviceEngineError()

        if (engine.name.isBlank()) {
            return AIPromptResult(
                content = "",
                isSuccess = false,
                errorMessage = "AI engine not configured",
            )
        }

        billingGate(engine, input, billing)?.let { return it }

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
            ).also { billingCharge(engine, billing, input, it, billingDesc) }
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

    /**
     * 流式变体：与 [execute] 参数一致，SSE 逐 chunk 回调 [onDelta]（参数为累计全文快照，
     * UI 直接整体替换即可），流结束后仍返回完整结果（成功时 content 为全文）。
     * [onReasoningDelta] 为深度思考内容( reasoning_content )的累计快照回调,仅作展示,不计入 content。
     */
    suspend fun executeStreaming(
        input: String,
        systemPrompt: String = "",
        engineMode: EngineMode = EngineMode.DEFAULT,
        onDelta: (String) -> Unit = {},
        onReasoningDelta: (String) -> Unit = {},
        billing: PromptBilling = PromptBilling.MANAGED,
        billingDesc: String = "",
    ): AIPromptResult {
        val engine = resolveEngine(engineMode) ?: return onDeviceEngineError()

        if (engine.name.isBlank()) {
            return AIPromptResult(
                content = "",
                isSuccess = false,
                errorMessage = "AI engine not configured",
            )
        }

        billingGate(engine, input, billing)?.let { return it }

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
            stream = true,
        )

        return try {
            val isProxyRoute = AiRequestUrlResolver.shouldUseProxyRequest(engine)
            withContext(ioDispatcher) {
                val url = AiRequestUrlResolver.resolveRequestUrl(engine)
                val call = if (AiRequestUrlResolver.shouldUseDirectRequest(engine)) {
                    val authorization = AiRequestUrlResolver.resolveAuthorizationHeader(engine)
                    openAICompatibleService.chatWithStreaming(
                        url = url,
                        authorization = authorization,
                        chatCompletionRequest = request
                    )
                } else {
                    ownProxyAIService.chatWithStreaming(
                        url = url,
                        chatCompletionRequest = request
                    )
                }

                val content = StringBuilder()
                val reasoning = StringBuilder()
                var totalTokens = 0
                var errorMessage: String? = null
                try {
                    val response = call.execute()
                    if (!response.isSuccessful) {
                        val errorBody = response.errorBody()?.string().orEmpty()
                        makeLog { "AIPromptExecutor: stream HTTP ${response.code()} - $errorBody" }
                        errorMessage = "HTTP ${response.code()}: ${errorBody.take(200)}"
                    } else {
                        val body = response.body()
                        if (body == null) {
                            errorMessage = "Empty response body"
                        } else {
                            body.byteStream().bufferedReader().use { reader ->
                                while (true) {
                                    currentCoroutineContext().ensureActive()
                                    val line = reader.readLine() ?: break
                                    val trimmed = line.trim().trimStart('\uFEFF')
                                    if (!trimmed.startsWith("data:")) continue
                                    val payload = trimmed.substring("data:".length).trim()
                                    if (payload.isEmpty()) continue
                                    if (payload.equals("[DONE]", ignoreCase = true)) break
                                    val chunk = try {
                                        gson.fromJson(payload, ChatCompletionChunk::class.java)
                                    } catch (e: Exception) {
                                        // 单行解析失败属厂商兼容问题,丢弃该行即可
                                        makeLog { "AIPromptExecutor: drop malformed SSE line: ${e.message}" }
                                        continue
                                    }
                                    if (chunk.errorCode != 0 || chunk.errorMsg.isNotBlank()) {
                                        errorMessage =
                                            chunk.errorMsg.ifBlank { "API error code: ${chunk.errorCode}" }
                                        break
                                    }
                                    chunk.usage?.takeIf { it.totalTokens > 0 }
                                        ?.let { totalTokens = it.totalTokens }
                                    val choice = chunk.choices.firstOrNull()
                                    val reasoningDelta = choice?.delta?.reasoningContent?.takeIf { it.isNotEmpty() }
                                        ?: choice?.message?.reasoningContent
                                            ?.takeIf { choice.delta == null && it.isNotEmpty() }
                                    if (!reasoningDelta.isNullOrEmpty()) {
                                        reasoning.append(reasoningDelta)
                                        onReasoningDelta(reasoning.toString())
                                    }
                                    val delta = choice?.delta?.content?.takeIf { it.isNotEmpty() }
                                        ?: choice?.message?.content
                                            ?.takeIf { choice.delta == null && it.isNotEmpty() }
                                    if (!delta.isNullOrEmpty()) {
                                        content.append(delta)
                                        onDelta(content.toString())
                                    }
                                }
                            }
                        }
                    }
                } finally {
                    runCatching { call.cancel() }
                }

                if (errorMessage != null) {
                    AIPromptResult(
                        content = "",
                        isSuccess = false,
                        errorMessage = errorMessage,
                        engineName = engine.name,
                        modelName = engine.model.name,
                    )
                } else {
                    AIPromptResult(
                        content = content.toString(),
                        isSuccess = true,
                        engineName = engine.name,
                        modelName = engine.model.name,
                        totalTokens = totalTokens,
                        isProxyRoute = isProxyRoute,
                    ).also { billingCharge(engine, billing, input, it, billingDesc) }
                }
            }
        } catch (t: Throwable) {
            makeLog { "AIPromptExecutor: Stream request failed: ${t.message}" }
            AIPromptResult(
                content = "",
                isSuccess = false,
                errorMessage = t.message ?: t.toString(),
                engineName = engine.name,
                modelName = engine.model.name,
            )
        }
    }

    /**
     * 请求前的登录/积分预检:仅自有代理路由要求「已登录 + 积分够」,
     * BYOK 直连与端侧本地引擎免费放行。返回非 null 即已被拦截,调用方直接把它当结果返回。
     */
    private fun billingGate(engine: AiEngine, input: String, billing: PromptBilling): AIPromptResult? {
        if (billing == PromptBilling.EXTERNAL) return null
        if (!AiRequestUrlResolver.shouldUseProxyRequest(engine)) return null
        val message = when {
            !TokenStorage.isLogin() -> getString(R.string.login_first)
            !BaseUtils.canConsumePoints(input) -> getString(R.string.no_points)
            else -> return null
        }
        return AIPromptResult(
            content = "",
            isSuccess = false,
            isBlocked = true,
            errorMessage = message,
            engineName = engine.name,
            modelName = engine.model.name,
        )
    }

    /**
     * 成功后的按量扣积分:仅代理路由扣;优先用 usage 的 totalTokens,接口没带 usage 时按输入+输出文本估算。
     * 失败不扣(本方法只在成功结果上调用),扣减失败静默(与聊天一致)。
     */
    private fun billingCharge(
        engine: AiEngine,
        billing: PromptBilling,
        input: String,
        result: AIPromptResult,
        desc: String,
    ) {
        if (billing == PromptBilling.EXTERNAL) return
        if (!result.isSuccess) return
        if (!AiRequestUrlResolver.shouldUseProxyRequest(engine)) return
        val tokens = result.totalTokens.takeIf { it > 0 }
            ?: (StringUtils.calculateTokens(input) + StringUtils.calculateTokens(result.content))
        if (tokens <= 0) return
        runCatching {
            BaseUtils.consumePoints(
                degree = BaseUtils.tokenToPoints(tokens, engine.model.basePoints),
                desc = desc.ifBlank { engine.model.name },
                source = engine.model.name,
            )
        }
    }

    /**
     * 解析本次执行要用的引擎。
     *
     * 端侧引擎(LOCAL_ON_DEVICE)的定位是「会话内由用户显式选择的聊天引擎」,
     * 单轮提示词执行(标题生成、诗词等后台任务)一律走云端:请求槽位解析到端侧引擎时,
     * 依次回退 FAST → DEFAULT → DUEL_A → DUEL_B 中第一个可用的云端引擎;
     * 所有槽位都是端侧引擎时返回 null,由调用方给出用户可理解的错误文案。
     */
    private fun resolveEngine(engineMode: EngineMode): AiEngine? {
        val requested = when (engineMode) {
            EngineMode.DEFAULT -> aiEngineManager.getCurrentAiEngine()
            EngineMode.FAST -> aiEngineManager.getFastAiEngine()
            EngineMode.DUEL_A -> aiEngineManager.getDuelEngineA()
            EngineMode.DUEL_B -> aiEngineManager.getDuelEngineB()
        }
        if (requested.requestProtocol != AiRequestProtocol.LOCAL_ON_DEVICE) return requested

        makeLog { "AIPromptExecutor: engine '${requested.name}' is on-device, fall back to a cloud engine" }
        return listOf(
            aiEngineManager.getFastAiEngine(),
            aiEngineManager.getCurrentAiEngine(),
            aiEngineManager.getDuelEngineA(),
            aiEngineManager.getDuelEngineB(),
        ).firstOrNull { it.name.isNotBlank() && it.requestProtocol != AiRequestProtocol.LOCAL_ON_DEVICE }
    }

    private fun onDeviceEngineError(): AIPromptResult {
        return AIPromptResult(
            content = "",
            isSuccess = false,
            errorMessage = getString(R.string.on_device_model_not_supported),
        )
    }
}
