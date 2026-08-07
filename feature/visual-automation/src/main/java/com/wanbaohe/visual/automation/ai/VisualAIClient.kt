package com.wanbaohe.visual.automation.ai

import android.content.Context
import com.google.gson.Gson
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.model.ai.ChatCompletionChunk
import com.shifenmiao.model.ai.ChatCompletionRequest
import com.shifenmiao.model.ai.ContentItem
import com.shifenmiao.model.ai.RequestMessage
import com.shifenmiao.model.ai.RoleType
import com.shifenmiao.model.automation.AIAction
import com.shifenmiao.network.AiRequestUrlResolver
import com.shifenmiao.network.api.OpenAICompatibleService
import com.shifenmiao.network.api.OwnProxyAIService
import com.t8rin.logger.makeLog
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 视觉自动化 AI 客户端。
 * 将截图和任务描述发送给 AI，解析返回的自动化动作。
 * 支持多模态输入（图片 + 文字）。
 */
@Singleton
class VisualAIClient @Inject constructor(
    @ApplicationContext private val context: Context,
    private val aiEngineManager: AIEngineManager,
    private val openAICompatibleService: OpenAICompatibleService,
    private val ownProxyAIService: OwnProxyAIService,
) {

    private val gson = Gson()

    companion object {
        /** 系统提示词 raw 资源 ID,首次访问时一次性读入内存。 */
        private const val SYSTEM_PROMPT_RAW_RES =
            com.wanbaohe.visual.automation.R.raw.visual_automation_system_prompt
    }

    private val systemPrompt: String by lazy {
        context.resources.openRawResource(SYSTEM_PROMPT_RAW_RES)
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
            .trim()
    }

    /**
     * 发送截图和任务描述给 AI，获取下一步操作。
     * @param imageDataUri 截图的 Data URI (data:image/jpeg;base64,...)
     * @param taskDescription 用户任务描述，如"帮我搜索天气预报"
     * @param history 可选的历史操作记录，用于上下文
     */
    suspend fun requestAction(
        imageDataUri: String,
        taskDescription: String,
        history: List<String> = emptyList()
    ): AIAction {
        val engine = aiEngineManager.getCurrentAiEngine()

        if (engine.name.isBlank()) {
            return AIAction.Error("AI engine not configured")
        }

        val promptText = buildString {
            appendLine("任务目标：$taskDescription")
            if (history.isNotEmpty()) {
                appendLine("\n已执行的操作：")
                history.forEachIndexed { index, action ->
                    appendLine("${index + 1}. $action")
                }
            }
            appendLine("\n请分析截图，返回下一步操作的 JSON。")
        }

        val contentItems = listOf(
            ContentItem.TextContent(text = promptText),
            ContentItem.ImageContent(
                imageUrl = com.shifenmiao.model.ai.ImageUrl(url = imageDataUri)
            )
        )

        val messages = buildList {
            add(
                RequestMessage.createTextMessage(
                    role = RoleType.SYSTEM.value,
                    text = systemPrompt
                )
            )
            add(
                RequestMessage.createMultiContentMessage(
                    role = RoleType.USER.value,
                    contentItems = contentItems
                )
            )
        }

        val request = ChatCompletionRequest(
            model = engine.model.name,
            messages = messages,
            stream = false,
        )

        return try {
            val url = AiRequestUrlResolver.resolveRequestUrl(engine)
            val response = if (AiRequestUrlResolver.shouldUseDirectRequest(engine)) {
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

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string().orEmpty()
                makeLog { "VisualAIClient: HTTP ${response.code()} - $errorBody" }
                return AIAction.Error("HTTP ${response.code()}: ${errorBody.take(200)}")
            }

            val body = response.body()?.string()
            if (body.isNullOrBlank()) {
                return AIAction.Error("Empty response body")
            }

            val chunk = try {
                gson.fromJson(body, ChatCompletionChunk::class.java)
            } catch (e: Exception) {
                makeLog { "VisualAIClient: JSON parse failed: ${e.message}" }
                return AIAction.Error("Failed to parse response: ${e.message}")
            }

            if (chunk.errorCode != 0 || chunk.errorMsg.isNotBlank()) {
                return AIAction.Error(chunk.errorMsg.ifBlank { "API error code: ${chunk.errorCode}" })
            }

            val content = chunk.choices.firstOrNull()?.message?.content.orEmpty()
            AIAction.parse(content)
        } catch (t: Throwable) {
            makeLog { "VisualAIClient: Request failed: ${t.message}" }
            AIAction.Error("Request failed: ${t.message}")
        }
    }

    /**
     * 发送截图和任务描述（使用纯文本模式，如果当前模型不支持图片）。
     */
    suspend fun requestActionTextOnly(
        taskDescription: String,
        history: List<String> = emptyList()
    ): AIAction {
        val engine = aiEngineManager.getCurrentAiEngine()

        if (engine.name.isBlank()) {
            return AIAction.Error("AI engine not configured")
        }

        val promptText = buildString {
            appendLine("任务目标：$taskDescription")
            if (history.isNotEmpty()) {
                appendLine("\n已执行的操作：")
                history.forEachIndexed { index, action ->
                    appendLine("${index + 1}. $action")
                }
            }
            appendLine("\n请返回下一步操作的 JSON。")
        }

        val messages = buildList {
            add(
                RequestMessage.createTextMessage(
                    role = RoleType.SYSTEM.value,
                    text = systemPrompt
                )
            )
            add(
                RequestMessage.createTextMessage(
                    role = RoleType.USER.value,
                    text = promptText
                )
            )
        }

        val request = ChatCompletionRequest(
            model = engine.model.name,
            messages = messages,
            stream = false,
        )

        return try {
            val url = AiRequestUrlResolver.resolveRequestUrl(engine)
            val response = if (AiRequestUrlResolver.shouldUseDirectRequest(engine)) {
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

            if (!response.isSuccessful) {
                return AIAction.Error("HTTP ${response.code()}")
            }

            val body = response.body()?.string().orEmpty()
            val chunk = gson.fromJson(body, ChatCompletionChunk::class.java)
            val content = chunk.choices.firstOrNull()?.message?.content.orEmpty()
            AIAction.parse(content)
        } catch (t: Throwable) {
            AIAction.Error("Request failed: ${t.message}")
        }
    }
}
