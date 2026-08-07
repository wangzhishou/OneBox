package com.shifenmiao.ai.utils

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.core.R
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.database.image.dao.ImageDao
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.ai.agent.AgentLoopExecutor
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.AnthropicEventType
import com.shifenmiao.model.ai.AnthropicStreamEvent
import com.shifenmiao.model.ai.ChatCompletionChunk
import com.shifenmiao.model.ai.ChunkChoice
import com.shifenmiao.model.ai.ContentItem
import com.shifenmiao.model.ai.ContentType
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.Delta
import com.shifenmiao.model.ai.FinishReason
import com.shifenmiao.model.ai.FunctionCall
import com.shifenmiao.model.ai.FunctionCallDelta
import com.shifenmiao.model.ai.ImageUrl
import com.shifenmiao.model.ai.RequestMessage
import com.shifenmiao.model.ai.RoleType
import com.shifenmiao.model.ai.ToolCall
import com.shifenmiao.model.ai.ToolCallDelta
import com.shifenmiao.model.ai.Usage
import com.shifenmiao.model.ai.unified.LlmBuiltinTool
import com.shifenmiao.model.ai.unified.LlmMessage
import com.shifenmiao.model.ai.unified.LlmTurnRequest
import com.shifenmiao.model.ai.unified.toLlmMessage
import com.shifenmiao.ai.context.ContextWindowManager
import com.shifenmiao.storage.RemoteConfigStorage
import com.shifenmiao.storage.TokenStorage
import com.t8rin.imagetoolbox.core.utils.LocaleUtils
import com.t8rin.logger.makeLog
import java.util.Date
import java.util.Locale

object AiUtils {

    fun isAssistant(conversation: Conversation): Boolean {
        return conversation.entryType == AIConversationEntryType.ASSISTANT
    }

    fun isAgent(conversation: Conversation) = conversation.entryType == AIConversationEntryType.AGENT

    fun isPrompt(conversation: Conversation) = conversation.entryType == AIConversationEntryType.PROMPT

    /**
     * 不需要代理的情况 和 付费模型
     */
    fun isNotFree(conversation: Conversation): Boolean {
        return canProxy(conversation) || conversation.engine.model.free
    }

    /**
     * 不需要代理的情况, 就可以聊天
     */
    fun canChat(
        conversation: Conversation,
        questionMessageEntityList: List<MessageEntity>
    ): Boolean {
        return !canProxy(conversation) || (canProxy(conversation) && checkTokenIsNotOverload(
            questionMessageEntityList
        ))
    }

    private fun checkTokenIsNotOverload(questionMessageEntityList: List<MessageEntity>): Boolean {
        val inputText = concatenateQuestionsAndAnswers(questionMessageEntityList)
        TokenStorage.getLoginInfo()?.let { login ->
            StringUtils.calculateTokens(inputText).let { tokens ->
                return (login.user.points ?: 0) >= BaseUtils.tokenToPoints(tokens) * 2
            }
        }
        return false
    }

    fun newQuestionMessageEntity(conversation: Conversation): MessageEntity {
        return MessageEntity(
            completionId = Date().time.toString(),
            conversationId = conversation.id,
            role = RoleType.USER.value,
            question = "",
            reasoningContent = "",
            engine = conversation.engine.name,
            model = conversation.engine.model.name,
            requestProtocol = conversation.engine.requestProtocol.name
        )
    }

    fun newAnswerMessageEntity(conversation: Conversation): MessageEntity {
        return MessageEntity(
            completionId = Date().time.toString(),
            conversationId = conversation.id,
            role = RoleType.ASSISTANT.value,
            answer = "",
            reasoningContent = "",
            engine = conversation.engine.name,
            model = conversation.engine.model.name,
            requestProtocol = conversation.engine.requestProtocol.name
        )
    }

    fun canProxy(conversation: Conversation): Boolean {
        val engine = conversation.engine
        return !engine.isDetestPassed
    }

    fun concatenateQuestionsAndAnswers(questionMessageEntityList: List<MessageEntity>): String {
        val stringBuilder = StringBuilder()
        for (messageEntity in questionMessageEntityList) {
            stringBuilder.append(messageEntity.question)
            stringBuilder.append(messageEntity.answer)
        }
        return stringBuilder.toString()
    }

    /**
     * 将消息实体列表转换为 LLM 请求格式。
     *
     * @param contentReader 可选的内容读取器，用于从缓存文件恢复附件的 base64 内容。
     *        历史消息的 attachmentsJson 仅含 localPath（不含 localContent 以避免 DB 膨胀），
     *        当 localContent 为空时，通过此 lambda 从 localPath 对应的缓存文件读取。
     *        传 null 时跳过文件读取（向后兼容）。
     * @param imageDao 可选的 ImageDao，用于从 DB 恢复历史图片（当缓存文件丢失时的降级方案）。
     */
    suspend fun buildLlmMessages(
        conversation: Conversation,
        messageEntityList: List<MessageEntity>,
        contentReader: (suspend (String) -> String?)? = null,
        imageDao: ImageDao? = null,
    ): List<LlmMessage> {
        val listMessage = mutableListOf<LlmMessage>()
        for (messageEntity in messageEntityList) {
            // 如果 assistant 消息包含 toolCalls JSON，需要重建完整的工具调用链：
            // assistant(tool_calls) → tool(result) × N，否则 LLM 会因缺少 tool result 而报错。
            if (messageEntity.role == RoleType.ASSISTANT.value && messageEntity.toolCalls.isNotBlank()) {
                // 将 assistant 的文本内容和 tool_calls 合并到同一条消息中（符合 OpenAI API 规范）
                val cleanedAnswer = cleanContentForLLMContext(messageEntity.answer)
                val cleanedReasoning = cleanContentForLLMContext(messageEntity.reasoningContent)
                val toolMessages = rebuildToolCallMessages(
                    toolCallsJson = messageEntity.toolCalls,
                    content = cleanedAnswer,
                    reasoningContent = cleanedReasoning
                )
                listMessage.addAll(toolMessages.map { it.toLlmMessage() })
            } else {
                listMessage.add(
                    convertToMessage(messageEntity, contentReader, imageDao, conversation.id).toLlmMessage()
                )
            }
        }
        if (conversation.prompt.isNotEmpty()) {
            listMessage.add(
                0,
                LlmMessage.createTextMessage(
                    role = RoleType.SYSTEM.value,
                    text = conversation.prompt +
                        RemoteConfigStorage.getRemoteConfig().aiPromptSuffix +
                        buildSystemLanguageDirective()
                )
            )
        }
        // 上下文窗口裁剪：确保消息总量不超过模型上下文窗口
        val contextWindow = conversation.engine.model.effectiveContextWindow()
        if (contextWindow > 0 && listMessage.size > 2) {
            return ContextWindowManager.fitToContextWindow(
                messages = listMessage,
                contextWindowTokens = contextWindow,
                maxOutputTokens = conversation.engine.model.maxTokens
            )
        }
        return listMessage
    }

    /**
     * 在系统提示词尾部告知模型用户的系统语言环境, 使其默认按用户语言回复。
     * 发送时拼装、不落库, 对历史会话同样生效。
     */
    private fun buildSystemLanguageDirective(): String {
        val tag = LocaleUtils.getCurrentLocaleTag()
        val languageName = runCatching {
            Locale.forLanguageTag(tag).getDisplayLanguage(Locale.ENGLISH)
        }.getOrNull().takeUnless { it.isNullOrBlank() } ?: tag
        return "\n\n[User context] The user's system language is $languageName ($tag). " +
            "Respond in that language unless the user explicitly asks for another language."
    }

    /**
     * 向后兼容：旧代码仍可拿到 Chat Completions 风格消息列表，
     * 但新链路统一先构建协议无关的 [LlmMessage]。
     */
    suspend fun convertToMessageList(
        conversation: Conversation,
        messageEntityList: List<MessageEntity>,
        contentReader: (suspend (String) -> String?)? = null,
        imageDao: ImageDao? = null,
    ): List<RequestMessage> {
        return buildLlmMessages(
            conversation = conversation,
            messageEntityList = messageEntityList,
            contentReader = contentReader,
            imageDao = imageDao
        ).map { it.toRequestMessage() }
    }

    suspend fun buildTurnRequest(
        conversation: Conversation,
        messageEntityList: List<MessageEntity>,
        contentReader: (suspend (String) -> String?)? = null,
        imageDao: ImageDao? = null,
        tools: List<com.shifenmiao.model.ai.ToolDefinition>? = null,
        enableWebSearch: Boolean = false,
        enableReasoning: Boolean = false,
        previousResponseId: String? = null,
        overrideMessages: List<LlmMessage>? = null,
    ): LlmTurnRequest {
        val messages = overrideMessages ?: buildLlmMessages(
            conversation = conversation,
            messageEntityList = messageEntityList,
            contentReader = contentReader,
            imageDao = imageDao
        )
        return LlmTurnRequest(
            model = conversation.engine.model.name,
            stream = conversation.engine.stream,
            messages = messages,
            tools = tools,
            builtinTools = buildSet {
                if (enableWebSearch) add(LlmBuiltinTool.WEB_SEARCH)
            },
            reasoningEnabled = enableReasoning,
            previousResponseId = previousResponseId
        )
    }

    /**
     * 从 toolCalls JSON 重建 assistant tool_calls + tool result 消息对。
     * JSON 格式由 [AgentLoopExecutor.serializeToolCallsChain] 生成。
     * 从 toolCalls JSON 重建 assistant tool_calls + tool result 消息对。
     * JSON 格式由 [AgentLoopExecutor.serializeToolCallsChain] 生成。
     *
     * @param content LLM 同时输出的文本内容（如 "我来帮你搜索一下"），合并到 assistant 消息中
     */
    private fun rebuildToolCallMessages(
        toolCallsJson: String,
        content: String? = null,
        reasoningContent: String? = null
    ): List<RequestMessage> {
        if (toolCallsJson.isBlank()) return emptyList()
        return try {
            val records = gson.fromJson(
                toolCallsJson,
                com.google.gson.reflect.TypeToken.getParameterized(
                    List::class.java, AgentToolCallRecord::class.java
                ).type
            ) as? List<AgentToolCallRecord> ?: return emptyList()
            if (records.isEmpty()) return emptyList()

            val messages = mutableListOf<RequestMessage>()
            // 1. assistant tool_calls 消息（同时包含 LLM 的文本输出）
            val toolCalls = records.map { record ->
                ToolCall(
                    id = record.id,
                    type = "function",
                    function = FunctionCall(name = record.name, arguments = record.arguments)
                )
            }
            messages.add(
                RequestMessage.createAssistantToolCallMessage(
                    toolCalls = toolCalls,
                    content = content,
                    reasoningContent = reasoningContent
                )
            )
            // 2. 每个工具的执行结果
            for (record in records) {
                messages.add(RequestMessage.createToolResultMessage(
                    toolCallId = record.id,
                    toolName = record.name,
                    content = record.result
                ))
            }
            messages
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * 工具调用记录的反序列化模型，与 [com.shifenmiao.ai.agent.ToolCallRecord] 结构一致。
     * 独立定义避免 feature/ai → core/utils 的循环依赖。
     */
    private data class AgentToolCallRecord(
        val id: String = "",
        val name: String = "",
        val arguments: String = "",
        val result: String = "",
        val isError: Boolean = false
    )


    private suspend fun convertToMessage(
        messageEntity: MessageEntity,
        contentReader: (suspend (String) -> String?)? = null,
        imageDao: ImageDao? = null,
        conversationId: String = "",
    ): RequestMessage {
        return if (messageEntity.role == RoleType.USER.value) {
            buildUserRequestMessage(messageEntity, contentReader, imageDao, conversationId)
        } else {
            // 清理可能残留在历史 DB 数据中的 UI 装饰文本和 AIGC 零宽标记
            val content = cleanContentForLLMContext(messageEntity.answer)
            RequestMessage.createTextMessage(
                role = messageEntity.role,
                text = content
            )
        }
    }

    /**
     * 构建用户请求消息（支持多模态附件）。
     *
     * 当 localContent 为空但 localPath 存在时（历史消息从 DB 加载），
     * 通过 contentReader 从缓存文件恢复 base64 内容。
     *
     * 图片去重逻辑：仅在单条消息内部去重，避免同一消息里重复挂同一张图。
     * 不再跨历史消息去重，否则用户在下一轮重新附图时会被静默跳过，模型就会认为“没收到图片”。
     *
     * DB 降级：当缓存文件丢失时，尝试从 ImageEntity 表恢复 base64 数据。
     */
    private suspend fun buildUserRequestMessage(
        messageEntity: MessageEntity,
        contentReader: (suspend (String) -> String?)? = null,
        imageDao: ImageDao? = null,
        conversationId: String = "",
    ): RequestMessage {
        val hasAttachments = messageEntity.attachmentsJson.isNotBlank()
                || messageEntity.contentType != ContentType.TEXT.value

        return if (!hasAttachments) {
            // 纯文本：保持现有逻辑
            RequestMessage.createTextMessage(
                role = messageEntity.role,
                text = messageEntity.question
            )
        } else {
            // 多模态：构建 text + image 混合内容
            val contentItems = mutableListOf<ContentItem>()
            val seenImageUrisInCurrentMessage = mutableSetOf<String>()

            // 文字部分
            if (messageEntity.question.isNotBlank()) {
                contentItems.add(ContentItem.TextContent(text = messageEntity.question))
            }

            // 解析附件
            val attachmentsJson = messageEntity.attachmentsJson
            if (attachmentsJson.isNotBlank()) {
                try {
                    val attachments = AttachmentPayloadUtils.deserialize(
                        json = attachmentsJson,
                        gson = gson
                    )
                    attachments.forEach { attachment ->
                        // 判断是否为图片：mimeType 或 localContent(data:image) 或 url 判断
                        val isImage = attachment.mimeType.startsWith("image/")
                                || attachment.localContent?.startsWith("data:image/") == true
                                || (attachment.url != null && attachment.mimeType.startsWith("image/"))
                                || attachment.isImage
                        val mimeType = attachment.mimeType.takeIf { it.isNotBlank() } ?: "image/jpeg"
                        val name = attachment.name.ifBlank { "file" }
                        val localPath = attachment.localPath
                        val parseError = attachment.parseError
                        val attachmentUri = attachment.uri.takeIf { it.isNotBlank() } ?: localPath ?: ""

                        // 单条消息内去重：避免重复选择同一张图两次。
                        val isDuplicateImage = isImage
                                && attachmentUri.isNotBlank()
                                && !seenImageUrisInCurrentMessage.add(attachmentUri)

                        if (isDuplicateImage) {
                            return@forEach
                        }


                        // 恢复 localContent：优先内存态 → 缓存文件 → ImageEntity DB
                        val localContent = attachment.localContent
                            ?: localPath?.let { path -> contentReader?.invoke(path) }
                            ?: resolveImageFromDb(imageDao, conversationId, attachmentUri)

                        when {
                            // 图片 Base64 编码（含从缓存恢复的情况）
                            isImage && !localContent.isNullOrBlank() -> {
                                contentItems.add(
                                    ContentItem.ImageContent(
                                        imageUrl = ImageUrl(
                                            url = AttachmentPayloadUtils.resolveImageContentUrl(
                                                mimeType = mimeType,
                                                localContent = localContent
                                            )
                                        )
                                    )
                                )
                            }
                            // 图片 URL（云存储模式）
                            isImage && attachment.url != null -> {
                                contentItems.add(
                                    ContentItem.ImageContent(
                                        imageUrl = ImageUrl(url = attachment.url!!)
                                    )
                                )
                            }
                            // 图片路径（缓存也丢失，降级为文本描述）
                            isImage && !localPath.isNullOrBlank() -> {
                                val reason = parseError ?: "图片缓存已清理"
                                contentItems.add(
                                    ContentItem.TextContent(
                                        text = "[图片: $name, 路径: $localPath, $reason]"
                                    )
                                )
                            }
                            // 非图片文件：文本内容（含从缓存恢复的情况）
                            !localContent.isNullOrBlank() -> {
                                contentItems.add(
                                    ContentItem.TextContent(
                                        text = "【文件: $name】\n$localContent"
                                    )
                                )
                            }
                            // 非图片文件：路径（缓存也丢失）
                            !localPath.isNullOrBlank() -> {
                                val reason = parseError ?: "文件缓存已清理"
                                contentItems.add(
                                    ContentItem.TextContent(
                                        text = "[文件: $name, 路径: $localPath, 类型: $mimeType, $reason]"
                                    )
                                )
                            }
                            // 非图片文件：URL
                            attachment.url != null -> {
                                contentItems.add(
                                    ContentItem.TextContent(text = "[附件: $name](${attachment.url!!})")
                                )
                            }
                            // 兜底
                            else -> {
                                contentItems.add(
                                    ContentItem.TextContent(text = "[附件: $name]")
                                )
                            }
                        }
                    }
                } catch (_: Exception) {
                    // 解析失败时降级为纯文本
                }
            }

            // 兜底：如果 contentItems 为空（解析失败等），至少发文本
            if (contentItems.isEmpty() && messageEntity.question.isNotBlank()) {
                return RequestMessage.createTextMessage(
                    role = messageEntity.role,
                    text = messageEntity.question
                )
            }

            RequestMessage.createMultiContentMessage(
                role = messageEntity.role,
                contentItems = contentItems
            )
        }
    }

    /**
     * 从 ImageEntity DB 恢复图片 base64 数据。
     * 当缓存文件丢失时的降级方案，优先使用 thumbnail（体积小，足够 LLM 理解）。
     */
    private suspend fun resolveImageFromDb(
        imageDao: ImageDao?,
        conversationId: String,
        uri: String
    ): String? {
        if (imageDao == null || conversationId.isBlank() || uri.isBlank()) return null
        return runCatching {
            val entity = imageDao.getImageByConversationAndUri(conversationId, uri)
            // 优先返回缩略图（~5-10KB），避免发送完整原图（~130-700KB）浪费 token
            entity?.thumbnailBase64 ?: entity?.base64Data
        }.getOrNull()
    }

    /**
     * 清理内容中的 UI 装饰文本（agent 工具状态 emoji 行），确保发给 LLM 的上下文是干净的。
     *
     * 这是一个向后兼容的安全网：新版本不再往 answer 中写入这些内容，
     * 但旧版本已持久化的数据可能仍包含它们。
     */
    private fun cleanContentForLLMContext(content: String): String {
        var cleaned = content
        // 移除 agent 工具调用状态行：> 🔧 ..., > ✅ ..., > 📝 ..., ⚠️ ...
        cleaned = cleaned.replace(Regex("""(?m)^>\s*[🔧✅📝]\s.*$\n?"""), "")
        cleaned = cleaned.replace(Regex("""(?m)^⚠️\s.*$\n?"""), "")
        return cleaned.trimEnd()
    }

    /**
     * 将 MessageEntity 中持久化的 toolCalls JSON 还原为完整的请求消息序列。
     * 用于上下文回放：assistant tool_calls → tool results → ... → final assistant answer。
     *
     * @param messageEntity 包含 toolCalls JSON 的 answer 消息
     * @param agentLoopExecutor 用于反序列化 toolCalls 链
     * @return 需要追加到消息列表的额外消息（可能为空）
     */
    fun reconstructToolMessages(
        messageEntity: MessageEntity,
        agentLoopExecutor: AgentLoopExecutor
    ): List<RequestMessage> {
        if (messageEntity.toolCalls.isBlank()) return emptyList()
        val records = agentLoopExecutor.deserializeToolCallsChain(messageEntity.toolCalls)
        if (records.isEmpty()) return emptyList()

        val extraMessages = mutableListOf<RequestMessage>()

        // 将记录中的工具调用转换为 assistant + tool result 消息对
        val toolCalls = records.map { record ->
            ToolCall(
                id = record.id,
                type = "function",
                function = FunctionCall(
                    name = record.name,
                    arguments = record.arguments
                )
            )
        }
        extraMessages.add(
            RequestMessage.createAssistantToolCallMessage(
                toolCalls = toolCalls,
                reasoningContent = cleanContentForLLMContext(messageEntity.reasoningContent)
                    .takeIf { it.isNotBlank() }
            )
        )

        for (record in records) {
            extraMessages.add(
                RequestMessage.createToolResultMessage(
                    toolCallId = record.id,
                    toolName = record.name,
                    content = record.result
                )
            )
        }
        return extraMessages
    }

    fun transformChatCompletion(chatCompletionChunk: ChatCompletionChunk): ChatCompletionChunk {
        chatCompletionChunk.choices.let { choice ->
            if (choice.isNotEmpty()) {
                choice[0].finishReason?.let {
                    when (choice[0].finishReason) {
                        FinishReason.STOP.value,
                        FinishReason.TOOL_CALLS.value,
                        FinishReason.LENGTH.value -> {
                            return chatCompletionChunk
                        }

                        FinishReason.SENSITIVE.value,
                        FinishReason.CONTENT_FILTER.value -> {
                            chatCompletionChunk.errorCode = 1
                            chatCompletionChunk.errorMsg = if (chatCompletionChunk.choices.isNotEmpty()) {
                                chatCompletionChunk.choices[0].let {
                                    if (it.delta?.content?.isNotEmpty() == true) {
                                        it.delta?.content
                                            ?: AppContext.getString(R.string.ai_error_sensitive)
                                    } else {
                                        it.message?.content
                                            ?: AppContext.getString(R.string.ai_error_sensitive)
                                    }
                                }

                            } else {
                                AppContext.getString(R.string.ai_error_sensitive)
                            }
                        }

                        FinishReason.INSUFFICIENT_SYSTEM_RESOURCE.value -> {
                            chatCompletionChunk.errorCode = 1
                            chatCompletionChunk.errorMsg =
                                AppContext.getString(R.string.ai_error_insufficient_system_resource)
                        }


                        else -> {
                            // 未知的 finish_reason（如 "end_turn"、"max_tokens" 等来自不同 provider），
                            // 不视为错误，按正常结束处理
                            return chatCompletionChunk
                        }
                    }
                }
            }
        }
        return chatCompletionChunk
    }


    fun getAiModelTips(it: AiModel): String {
        return it.description
    }

    /**
     * 从 OkHttp 失败响应中提取精简错误信息。
     *
     * 只保留状态码、请求 URL/Method 和从响应体中解析出的错误描述，
     * 不再 dump 完整的请求头和响应头（避免信息泄露和 UI 上过于冗长）。
     */
    fun extractDetailedErrorInfo(response: retrofit2.Response<*>): ChatCompletionChunk {
        try {
            val statusCode = response.code()
            val statusMessage = response.message()
            val requestUrl = response.raw().request.url
            val requestMethod = response.raw().request.method
            val errorBodyString = response.errorBody()?.string().orEmpty()

            // 尝试从 JSON 响应体中解析可读错误描述
            val errorDetail = parseErrorBody(errorBodyString)

            val conciseError = buildString {
                append("HTTP $statusCode")
                if (statusMessage.isNotBlank()) append(" $statusMessage")
                append('\n')
                append("URL: $requestMethod $requestUrl")
                append('\n')
                if (errorDetail.isNotBlank()) {
                    append("Error: $errorDetail")
                }
            }

            return ChatCompletionChunk(
                errorCode = statusCode,
                errorMsg = conciseError
            )
        } catch (e: Throwable) {
            return ChatCompletionChunk(
                errorCode = -1,
                errorMsg = "Failed to extract error details: ${e.message}"
            )
        }
    }

    /**
     * 从 HTTP error body 中解析可读的错误描述。
     * 兼容 JSON（含 error.message / message / code 等常见结构）和纯文本两种格式，
     * 超长内容截断到 500 字符。
     */
    private fun parseErrorBody(errorBody: String): String {
        if (errorBody.isBlank()) return ""
        return try {
            val jsonObject = JsonParser.parseString(errorBody).asJsonObject
            // 优先取 error.message（OpenAI 风格）
            val errorObj = jsonObject.getAsJsonObject("error")
            val message = when {
                errorObj != null && errorObj.has("message") ->
                    errorObj.get("message").asString
                errorObj != null && errorObj.has("code") ->
                    "${errorObj.get("code").asString}: ${errorObj.get("message")?.asString ?: "unknown"}"
                jsonObject.has("message") ->
                    jsonObject.get("message").asString
                jsonObject.has("code") && jsonObject.has("message") ->
                    "${jsonObject.get("code").asString}: ${jsonObject.get("message").asString}"
                jsonObject.has("code") ->
                    "code=${jsonObject.get("code").asString}"
                else -> errorBody.take(MAX_ERROR_BODY_LENGTH)
            }
            message.take(MAX_ERROR_BODY_LENGTH)
        } catch (_: Exception) {
            // 非 JSON 格式（如 HTML 错误页），截断展示
            errorBody.take(MAX_ERROR_BODY_LENGTH)
        }
    }

    private const val MAX_ERROR_BODY_LENGTH = 500

    // 提取Gson为单例对象
    private val gson by lazy { Gson() }

    fun processStreamLine(line: String): ChatCompletionChunk? {
        // 先做基础清洗：去掉 BOM / 末尾 \r 等不可见字符。SSE 规范 (W3C EventSource) 允许
        // 空行 / 注释行 / event:/id:/retry: 等多种行类型，必须做兼容，否则非 OpenAI 但
        // 兼容协议的 provider 容易被误判成"无内容"或"解析失败"。
        val trimmed = line.trim().trimStart('\uFEFF')
        return when {
            trimmed.isEmpty() -> null
            // SSE 注释行（以 ":" 开头），常见于 keep-alive 心跳
            trimmed.startsWith(":") -> null
            // 非 data 字段：event/id/retry —— 当前业务用不到，直接忽略
            trimmed.startsWith("event:", ignoreCase = true) -> null
            trimmed.startsWith("id:", ignoreCase = true) -> null
            trimmed.startsWith("retry:", ignoreCase = true) -> null
            trimmed.startsWith("data:", ignoreCase = true) -> {
                val payload = trimmed.substring("data:".length).trim()
                // 兼容 "[DONE]" / "[done]" / "data:[DONE]" 多种厂商写法
                if (payload.equals("[DONE]", ignoreCase = true)) {
                    return ChatCompletionChunk(isEnd = true)
                }
                if (payload.isEmpty()) return null
                try {
                    val chunk = gson.fromJson(payload, ChatCompletionChunk::class.java)
                    val transformed = transformChatCompletion(chunk)
                    // 某些 provider 在最后一帧只发 usage 或仅带 finish_reason，不再单独
                    // 发 [DONE]。这里识别这两种"隐式结束"信号，主动置 isEnd=true，避免上层
                    // 流被自然 close 但 onChatCompletionEnd 始终不被触发。
                    val hasFinishReason = transformed.choices.any {
                        val fr = it.finishReason
                        !fr.isNullOrBlank() && fr != "null"
                    }
                    val hasUsageOnly = transformed.choices.isEmpty()
                            && transformed.usage != null
                            && (transformed.usage?.totalTokens ?: 0) > 0
                    if (hasFinishReason || hasUsageOnly) {
                        transformed.isEnd = true
                    }
                    transformed
                } catch (e: Exception) {
                    // 单行 JSON 解析失败属于厂商兼容性问题，丢弃即可，不要伪造一个
                    // errorCode=1 的 chunk —— 否则上层会把整次会话当成失败终止。
                    "Drop malformed SSE line: ${e.message}, raw=$payload".makeLog("AiUtils")
                    null
                }
            }

            trimmed.startsWith("{") -> {
                try {
                    val chunk = gson.fromJson(trimmed, ChatCompletionChunk::class.java)
                    val transformed = transformChatCompletion(chunk)
                    val hasFinishReason = transformed.choices.any {
                        val fr = it.finishReason
                        !fr.isNullOrBlank() && fr != "null"
                    }
                    val hasMessageSnapshot = transformed.choices.any { it.message != null }
                    if (transformed.errorCode != 0 || hasFinishReason || hasMessageSnapshot) {
                        transformed.isEnd = true
                    }
                    transformed
                } catch (e: Exception) {
                    "Drop malformed JSON stream line: ${e.message}, raw=$trimmed".makeLog("AiUtils")
                    null
                }
            }

            else -> null // Ignore unexpected line format
        }
    }

    fun handleStreamingError(
        error: Throwable,
        message: String
    ): ChatCompletionChunk {
        return ChatCompletionChunk(
            errorCode = 1,
            errorMsg = "$message: ${error.message ?: error.toString()}"
        )
    }

    // ==================== Anthropic SSE 解析 ====================

    /**
     * Anthropic SSE 状态追踪
     * 用于将多个 Anthropic 事件合并为单个 ChatCompletionChunk
     */
    data class AnthropicStreamState(
        var messageId: String = "",
        var model: String = "",
        var role: String = "assistant",
        var content: StringBuilder = StringBuilder(),
        var inputTokens: Int = 0,
        var outputTokens: Int = 0,
        var stopReason: String? = null,
        var currentEvent: String? = null,
        /** 当前正在累积的 tool_use block 索引 */
        var currentToolUseIndex: Int = -1,
        /** 当前 tool_use 的 id */
        var currentToolUseId: String = "",
        /** 当前 tool_use 的函数名 */
        var currentToolUseName: String = "",
        /** 当前 tool_use 的参数 JSON 碎片累积 */
        var currentToolUseArgs: StringBuilder = StringBuilder(),
        /** 已完成的 tool_use 索引计数，用于映射到 OpenAI 的 tool_call index */
        var toolUseCount: Int = 0
    )

    /**
     * 处理 Anthropic 格式的 SSE 行
     *
     * Anthropic SSE 格式：
     * ```
     * event: message_start
     * data: {"type":"message_start","message":{"id":"msg_...","role":"assistant","content":[]}}
     *
     * event: content_block_start
     * data: {"type":"content_block_start","index":0,"content_block":{"type":"text","text":""}}
     *
     * event: content_block_delta
     * data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":"Hello"}}
     *
     * event: content_block_stop
     * data: {"type":"content_block_stop","index":0}
     *
     * event: message_delta
     * data: {"type":"message_delta","delta":{"stop_reason":"end_turn"},"usage":{"output_tokens":15}}
     *
     * event: message_stop
     * data: {"type":"message_stop"}
     * ```
     */
    fun processAnthropicStreamLine(
        line: String,
        state: AnthropicStreamState
    ): ChatCompletionChunk? {
        val trimmed = line.trim().trimStart('\uFEFF')

        return when {
            trimmed.isEmpty() -> null
            trimmed.startsWith(":") -> null  // 注释行

            trimmed.startsWith("event:", ignoreCase = true) -> {
                val eventType = trimmed.substring("event:".length).trim()
                state.currentEvent = eventType
                null
            }

            trimmed.startsWith("data:", ignoreCase = true) -> {
                val payload = trimmed.substring("data:".length).trim()
                if (payload.isEmpty()) return null

                try {
                    val event = gson.fromJson(payload, AnthropicStreamEvent::class.java)
                    processAnthropicEvent(event, state)
                } catch (e: Exception) {
                    "Drop malformed Anthropic SSE line: ${e.message}, raw=$payload".makeLog("AiUtils")
                    null
                }
            }

            else -> null
        }
    }

    /**
     * 处理解析后的 Anthropic 事件
     */
    private fun processAnthropicEvent(
        event: AnthropicStreamEvent,
        state: AnthropicStreamState
    ): ChatCompletionChunk? {
        return when (event.type) {
            AnthropicEventType.MESSAGE_START.value -> {
                event.message?.let { msg ->
                    state.messageId = msg.id
                    state.model = msg.model
                    state.role = msg.role
                    msg.usage?.let { usage ->
                        state.inputTokens = usage.inputTokens
                    }
                }
                null  // 不产生 chunk，等待内容
            }

            AnthropicEventType.CONTENT_BLOCK_START.value -> {
                val block = event.contentBlock
                if (block?.type == "tool_use") {
                    // 开始新的 tool_use block
                    state.currentToolUseIndex = event.index ?: 0
                    state.currentToolUseId = block.id ?: ""
                    state.currentToolUseName = block.name ?: ""
                    state.currentToolUseArgs = StringBuilder()
                }
                null  // 不产生 chunk，等待内容
            }

            AnthropicEventType.CONTENT_BLOCK_DELTA.value -> {
                event.delta?.let { delta ->
                    when (delta.type) {
                        "text_delta" -> {
                            if (delta.text != null) {
                                state.content.append(delta.text)
                                ChatCompletionChunk(
                                    id = state.messageId,
                                    model = state.model,
                                    choices = listOf(
                                        ChunkChoice(
                                            index = 0,
                                            delta = Delta(
                                                role = state.role,
                                                content = delta.text
                                            )
                                        )
                                    )
                                )
                            } else {
                                null
                            }
                        }
                        "input_json_delta" -> {
                            // 工具调用参数增量
                            delta.partialJson?.let { state.currentToolUseArgs.append(it) }
                            null  // 不单独发射，等 content_block_stop 时一起发射
                        }
                        else -> null
                    }
                }
            }

            AnthropicEventType.CONTENT_BLOCK_STOP.value -> {
                // 如果当前是 tool_use block 结束，发射一个包含 tool_calls 的 chunk
                if (state.currentToolUseIndex >= 0) {
                    val toolCallIndex = state.toolUseCount
                    state.toolUseCount++

                    val chunk = ChatCompletionChunk(
                        id = state.messageId,
                        model = state.model,
                        choices = listOf(
                            ChunkChoice(
                                index = 0,
                                delta = Delta(
                                    role = state.role,
                                    toolCalls = listOf(
                                        ToolCallDelta(
                                            index = toolCallIndex,
                                            id = state.currentToolUseId,
                                            type = "function",
                                            function = FunctionCallDelta(
                                                name = state.currentToolUseName,
                                                arguments = state.currentToolUseArgs.toString()
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )

                    // 重置 tool_use 状态
                    state.currentToolUseIndex = -1
                    state.currentToolUseId = ""
                    state.currentToolUseName = ""
                    state.currentToolUseArgs = StringBuilder()

                    chunk
                } else {
                    null  // text block 结束，不产生 chunk
                }
            }

            AnthropicEventType.MESSAGE_DELTA.value -> {
                event.delta?.let { delta ->
                    state.stopReason = delta.stopReason
                }
                event.usage?.let { usage ->
                    state.outputTokens = usage.outputTokens
                }
                // 构建结束 chunk
                ChatCompletionChunk(
                    id = state.messageId,
                    model = state.model,
                    choices = listOf(
                        ChunkChoice(
                            index = 0,
                            finishReason = mapAnthropicStopReason(state.stopReason)
                        )
                    ),
                    usage = Usage(
                        promptTokens = state.inputTokens,
                        completionTokens = state.outputTokens,
                        totalTokens = state.inputTokens + state.outputTokens
                    ),
                    isEnd = true
                )
            }

            AnthropicEventType.MESSAGE_STOP.value -> {
                // 确保结束
                ChatCompletionChunk(isEnd = true)
            }

            AnthropicEventType.PING.value -> {
                null  // 心跳，忽略
            }

            AnthropicEventType.ERROR.value -> {
                val errorMsg = event.error?.message ?: "Unknown Anthropic error"
                ChatCompletionChunk(
                    errorCode = 1,
                    errorMsg = errorMsg
                )
            }

            else -> null
        }
    }

    /**
     * 将 Anthropic stop_reason 映射为 OpenAI finish_reason
     */
    private fun mapAnthropicStopReason(stopReason: String?): String? {
        return when (stopReason) {
            "end_turn" -> FinishReason.STOP.value
            "max_tokens" -> FinishReason.LENGTH.value
            "stop_sequence" -> FinishReason.STOP.value
            "tool_use" -> FinishReason.TOOL_CALLS.value
            else -> stopReason
        }
    }

    /**
     * 创建新的 Anthropic 流状态
     */
    fun createAnthropicStreamState(): AnthropicStreamState {
        return AnthropicStreamState()
    }

    /**
     * 根据协议类型处理 SSE 行
     */
    fun processStreamLineByProtocol(
        line: String,
        protocol: AiRequestProtocol,
        anthropicState: AnthropicStreamState? = null
    ): ChatCompletionChunk? {
        return when (protocol) {
            AiRequestProtocol.ANTHROPIC_COMPATIBLE -> {
                processAnthropicStreamLine(line, anthropicState ?: createAnthropicStreamState())
            }
            else -> {
                processStreamLine(line)
            }
        }
    }
}