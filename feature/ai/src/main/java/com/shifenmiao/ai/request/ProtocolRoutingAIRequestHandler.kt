package com.shifenmiao.ai.request

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.shifenmiao.ai.BuildConfig
import com.shifenmiao.ai.upload.AttachmentContentResolver
import com.shifenmiao.ai.utils.AiUtils
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.database.image.dao.ImageDao
import com.shifenmiao.model.ai.AnthropicMessage
import com.shifenmiao.model.ai.AnthropicMessagesRequest
import com.shifenmiao.model.ai.AnthropicTool
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiProvider
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.AuthType
import com.shifenmiao.model.ai.ChatCompletionChunk
import com.shifenmiao.model.ai.ChatCompletionRequest
import com.shifenmiao.model.ai.ContentBlock
import com.shifenmiao.model.ai.ContentItem
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.FunctionCallDelta
import com.shifenmiao.model.ai.ImageSource
import com.shifenmiao.model.ai.ListOrStringContent
import com.shifenmiao.model.ai.RequestMessage
import com.shifenmiao.model.ai.ReasoningOptions
import com.shifenmiao.model.ai.RoleType
import com.shifenmiao.model.ai.ToolCallDelta
import com.shifenmiao.model.ai.ToolDefinition
import com.shifenmiao.model.ai.openai.responses.ResponsesApiContentItem
import com.shifenmiao.model.ai.openai.responses.ResponsesApiInputItem
import com.shifenmiao.model.ai.openai.responses.ResponsesApiRequest
import com.shifenmiao.model.ai.openai.responses.ResponsesWebSearchTool
import com.shifenmiao.model.ai.unified.LlmContentPart
import com.shifenmiao.model.ai.unified.LlmMessage
import com.shifenmiao.model.ai.unified.LlmStreamEvent
import com.shifenmiao.model.ai.unified.LlmTurnRequest
import com.shifenmiao.network.AiRequestUrlResolver
import com.shifenmiao.network.api.AnthropicCompatibleService
import com.shifenmiao.network.api.OpenAICompatibleService
import com.shifenmiao.network.api.OpenAIWithApiKeyService
import com.shifenmiao.network.api.OwnProxyAIService
import com.shifenmiao.core.constants.Constants
import com.t8rin.logger.makeLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import java.io.IOException
import javax.inject.Inject

class ProtocolRoutingAIRequestHandler @Inject constructor(
    private val openAICompatibleService: OpenAICompatibleService,
    private val ownProxyAIService: OwnProxyAIService,
    private val openAIWithApiKeyService: OpenAIWithApiKeyService,
    private val anthropicCompatibleService: AnthropicCompatibleService,
    private val attachmentContentResolver: AttachmentContentResolver,
    private val imageDao: ImageDao,
    private val gson: Gson
) : AIRequestHandler {

    override fun startChatWithStreaming(
        conversation: Conversation,
        messageEntityList: List<MessageEntity>,
        enableWebSearch: Boolean,
        enableReasoning: Boolean,
        tools: List<ToolDefinition>?
    ): Flow<LlmStreamEvent> = flow {
        val request = AiUtils.buildTurnRequest(
            conversation = conversation,
            messageEntityList = messageEntityList,
            contentReader = { path -> attachmentContentResolver.readContentFromPath(path) },
            imageDao = imageDao,
            tools = tools,
            enableWebSearch = enableWebSearch,
            enableReasoning = enableReasoning
        )
        startChatWithDirectRequest(conversation, request).collect { emit(it) }
    }

    override fun startChatWithDirectRequest(
        conversation: Conversation,
        chatCompletionRequest: LlmTurnRequest
    ): Flow<LlmStreamEvent> {
        return when (conversation.engine.requestProtocol) {
            AiRequestProtocol.RESPONSES_COMPATIBLE -> {
                if (chatCompletionRequest.stream) {
                    handleResponsesStreamOperation(conversation, chatCompletionRequest).buffer(Channel.BUFFERED)
                } else {
                    handleResponsesNonStreamOperation(conversation, chatCompletionRequest).buffer(Channel.BUFFERED)
                }
            }

            else -> {
                val mappedRequest = mapToChatCompletionRequest(
                    request = chatCompletionRequest,
                    engine = conversation.engine
                )
                if (mappedRequest.stream) {
                    handleChatStreamOperation(conversation, mappedRequest).buffer(Channel.BUFFERED)
                } else {
                    handleChatNonStreamOperation(conversation, mappedRequest).buffer(Channel.BUFFERED)
                }
            }
        }
    }


    /**
     * Chat Completions / Anthropic 兼容协议继续走现有 chunk 解析，
     * 但在出口统一转换为 [LlmStreamEvent]，避免 UI/Agent Loop 再感知 provider 结构。
     */
    private fun handleChatStreamOperation(
        conversation: Conversation,
        chatCompletionRequest: ChatCompletionRequest
    ): Flow<LlmStreamEvent> = callbackFlow {
        val engine = conversation.engine
        val call = createChatCall(
            conversation = conversation,
            chatCompletionRequest = chatCompletionRequest,
            streaming = true
        )
        var responseBody: ResponseBody? = null
        var sawEnd = false
        var chunkCount = 0
        var emittedResponseId: String? = null

        val anthropicState = if (engine.requestProtocol == AiRequestProtocol.ANTHROPIC_COMPATIBLE) {
            AiUtils.createAnthropicStreamState()
        } else {
            null
        }

        try {
            val response = call.execute()
            if (!response.isSuccessful) {
                trySend(AiUtils.extractDetailedErrorInfo(response).toErrorEvent())
                close()
                return@callbackFlow
            }
            responseBody = response.body()
            if (responseBody == null) {
                trySend(LlmStreamEvent.Error(errorMessage = "Empty response body"))
                close()
                return@callbackFlow
            }
            try {
                responseBody.byteStream().bufferedReader().use { reader ->
                    while (isActive) {
                        val line = try {
                            reader.readLine() ?: break
                        } catch (e: IOException) {
                            if (!isActive) break
                            trySend(AiUtils.handleStreamingError(e, "IO error while reading stream").toErrorEvent())
                            break
                        }
                        val chunk = AiUtils.processStreamLineByProtocol(
                            line = line,
                            protocol = engine.requestProtocol,
                            anthropicState = anthropicState
                        ) ?: continue
                        chunkCount++
                        if (chunk.isEnd) sawEnd = true
                        var closed = false
                        for (event in mapChatChunkToEvents(chunk, emittedResponseId)) {
                            if (event is LlmStreamEvent.ResponseStarted) emittedResponseId = event.responseId
                            val sendResult = trySend(event)
                            if (sendResult.isClosed) {
                                closed = true
                                break
                            }
                        }
                        if (closed || chunk.errorCode != 0) break
                        if (sawEnd) {
                            if (BuildConfig.DEBUG) {
                                "Stream end marker received, breaking read loop (chunkCount=$chunkCount)"
                                    .makeLog("ProtocolRoutingAIRequestHandler")
                            }
                            break
                        }
                        delay(Constants.CALLBACK_TRY_SEND_DELAY)
                    }
                }
                if (BuildConfig.DEBUG) {
                    "Stream read loop exited: chunkCount=$chunkCount sawEnd=$sawEnd isActive=$isActive"
                        .makeLog("ProtocolRoutingAIRequestHandler")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                if (isActive) {
                    trySend(AiUtils.handleStreamingError(e, "IO error while reading stream").toErrorEvent())
                }
            } catch (e: Exception) {
                if (isActive) {
                    trySend(AiUtils.handleStreamingError(e, "Error processing stream data").toErrorEvent())
                }
            }

            if (isActive && !sawEnd) {
                trySend(LlmStreamEvent.Completed(responseId = emittedResponseId))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            trySend(AiUtils.handleStreamingError(t, "Error executing request").toErrorEvent())
        }
        close()
        awaitClose {
            runCatching { call.cancel() }
            runCatching { responseBody?.close() }
        }
    }.buffer(STREAM_BUFFER_CAPACITY).flowOn(Dispatchers.IO)

    private fun handleChatNonStreamOperation(
        conversation: Conversation,
        chatCompletionRequest: ChatCompletionRequest
    ): Flow<LlmStreamEvent> = flow {
        val chunk = withContext(Dispatchers.IO) {
            val call = createChatCall(
                conversation = conversation,
                chatCompletionRequest = chatCompletionRequest,
                streaming = false
            )
            val response = call.execute()
            if (response.isSuccessful) {
                try {
                    val json = response.body()?.string()
                    gson.fromJson(json, ChatCompletionChunk::class.java)
                } catch (_: Exception) {
                    AiUtils.extractDetailedErrorInfo(response)
                }
            } else {
                AiUtils.extractDetailedErrorInfo(response)
            }
        }
        mapChatChunkToEvents(chunk).forEach { emit(it) }
    }

    /**
     * Responses API 单独建模：
     * - input / previous_response_id 与 Chat 不同
     * - SSE 事件是 event-first，不再是统一 chunk 结构
     */
    private fun handleResponsesStreamOperation(
        conversation: Conversation,
        request: LlmTurnRequest
    ): Flow<LlmStreamEvent> = callbackFlow {
        val call = createResponsesCall(conversation, request, streaming = true)
        var responseBody: ResponseBody? = null
        val toolStates = mutableMapOf<Int, ResponseToolState>()
        val contentStates = mutableMapOf<ResponseContentKey, StringBuilder>()

        try {
            val response = call.execute()
            if (!response.isSuccessful) {
                trySend(LlmStreamEvent.Error(errorMessage = AiUtils.extractDetailedErrorInfo(response).errorMsg))
                close()
                return@callbackFlow
            }
            responseBody = response.body()
            if (responseBody == null) {
                trySend(LlmStreamEvent.Error(errorMessage = "Empty response body"))
                close()
                return@callbackFlow
            }

            var currentEvent: String? = null
            val dataBuffer = StringBuilder()
            fun flushEvent() {
                if (dataBuffer.isEmpty()) return
                val payload = dataBuffer.toString().trim()
                dataBuffer.clear()
                parseResponsesSsePayload(payload, currentEvent, toolStates, contentStates).forEach { trySend(it) }
                currentEvent = null
            }

            responseBody.byteStream().bufferedReader().use { reader ->
                while (isActive) {
                    val line = reader.readLine() ?: break
                    when {
                        line.startsWith("event:") -> currentEvent = line.removePrefix("event:").trim()
                        line.startsWith("data:") -> dataBuffer.appendLine(line.removePrefix("data:").trim())
                        line.isBlank() -> flushEvent()
                    }
                }
                flushEvent()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            trySend(LlmStreamEvent.Error(errorMessage = t.message ?: "Responses stream failed"))
        }
        close()
        awaitClose {
            runCatching { call.cancel() }
            runCatching { responseBody?.close() }
        }
    }.buffer(STREAM_BUFFER_CAPACITY).flowOn(Dispatchers.IO)

    private fun handleResponsesNonStreamOperation(
        conversation: Conversation,
        request: LlmTurnRequest
    ): Flow<LlmStreamEvent> = flow {
        val body = withContext(Dispatchers.IO) {
            val call = createResponsesCall(conversation, request, streaming = false)
            val response = call.execute()
            if (!response.isSuccessful) {
                throw IOException(AiUtils.extractDetailedErrorInfo(response).errorMsg)
            }
            response.body()?.string().orEmpty()
        }
        parseResponsesResponse(body).forEach { emit(it) }
    }

    private fun createChatCall(
        conversation: Conversation,
        chatCompletionRequest: ChatCompletionRequest,
        streaming: Boolean
    ): Call<ResponseBody> {
        val engine = conversation.engine
        val url = AiRequestUrlResolver.resolveRequestUrl(engine)

        return when (engine.requestProtocol) {
            AiRequestProtocol.ANTHROPIC_COMPATIBLE -> {
                val anthropicRequest = convertToAnthropicRequest(chatCompletionRequest)
                val apiKey = AiRequestUrlResolver.resolveApiKey(engine) ?: ""
                if (streaming) {
                    anthropicCompatibleService.messagesWithStreaming(url = url, apiKey = apiKey, request = anthropicRequest)
                } else {
                    anthropicCompatibleService.messagesNoStreaming(url = url, apiKey = apiKey, request = anthropicRequest)
                }
            }
            AiRequestProtocol.OPENAI_COMPATIBLE -> {
                when (engine.authType) {
                    AuthType.API_KEY -> {
                        val apiKey = AiRequestUrlResolver.resolveApiKey(engine) ?: ""
                        if (streaming) {
                            openAIWithApiKeyService.chatWithStreaming(url = url, apiKey = apiKey, chatCompletionRequest = chatCompletionRequest)
                        } else {
                            openAIWithApiKeyService.chatNoStreaming(url = url, apiKey = apiKey, chatCompletionRequest = chatCompletionRequest)
                        }
                    }
                    else -> {
                        if (AiRequestUrlResolver.shouldUseDirectRequest(engine)) {
                            val authorization = AiRequestUrlResolver.resolveAuthorizationHeader(engine)
                            if (streaming) {
                                openAICompatibleService.chatWithStreaming(url = url, authorization = authorization, chatCompletionRequest = chatCompletionRequest)
                            } else {
                                openAICompatibleService.chatNoStreaming(url = url, authorization = authorization, chatCompletionRequest = chatCompletionRequest)
                            }
                        } else {
                            if (streaming) {
                                ownProxyAIService.chatWithStreaming(url = url, chatCompletionRequest = chatCompletionRequest)
                            } else {
                                ownProxyAIService.chatNoStreaming(url = url, chatCompletionRequest = chatCompletionRequest)
                            }
                        }
                    }
                }
            }
            AiRequestProtocol.OWN_PROXY -> {
                if (streaming) {
                    ownProxyAIService.chatWithStreaming(url = url, chatCompletionRequest = chatCompletionRequest)
                } else {
                    ownProxyAIService.chatNoStreaming(url = url, chatCompletionRequest = chatCompletionRequest)
                }
            }
            AiRequestProtocol.RESPONSES_COMPATIBLE -> error("Responses API should be routed via createResponsesCall")
            // 端侧本地推理不走 HTTP；Gateway 已在更上层按 protocol 分流到 LocalOnDeviceAdapter，
            // 理论上不会到达此处。error() 是为了在出现回归 bug 时立刻暴露。
            AiRequestProtocol.LOCAL_ON_DEVICE -> error("Local on-device engine should be routed via LocalOnDeviceAdapter, not ProtocolRoutingAIRequestHandler")
        }
    }

    private fun createResponsesCall(
        conversation: Conversation,
        request: LlmTurnRequest,
        streaming: Boolean
    ): Call<ResponseBody> {
        val engine = conversation.engine
        val url = AiRequestUrlResolver.resolveRequestUrl(engine)
        val mappedRequest = mapToResponsesRequest(request)
        return when (engine.authType) {
            AuthType.API_KEY -> {
                val apiKey = AiRequestUrlResolver.resolveApiKey(engine) ?: ""
                if (streaming) {
                    openAIWithApiKeyService.responsesWithStreaming(url = url, apiKey = apiKey, responsesApiRequest = mappedRequest)
                } else {
                    openAIWithApiKeyService.responsesNoStreaming(url = url, apiKey = apiKey, responsesApiRequest = mappedRequest)
                }
            }
            else -> {
                val authorization = AiRequestUrlResolver.resolveAuthorizationHeader(engine)
                if (streaming) {
                    openAICompatibleService.responsesWithStreaming(url = url, authorization = authorization, responsesApiRequest = mappedRequest)
                } else {
                    openAICompatibleService.responsesNoStreaming(url = url, authorization = authorization, responsesApiRequest = mappedRequest)
                }
            }
        }
    }

    private fun mapToChatCompletionRequest(
        request: LlmTurnRequest,
        engine: AiEngine
    ): ChatCompletionRequest {
        return ChatCompletionRequest(
            model = request.model,
            stream = request.stream,
            messages = request.messages.map { message ->
                message.toRequestMessage().adjustReasoningContentForChatProvider(engine)
            },
            tools = request.tools,
            enableWebSearch = request.webSearchEnabled,
            reasoning = if (request.reasoningEnabled) {
                ReasoningOptions(enabled = true)
            } else {
                null
            }
        )
    }


    private fun RequestMessage.adjustReasoningContentForChatProvider(engine: AiEngine): RequestMessage {
        val providerName = engine.name.trim().lowercase()
        val isAssistantToolCallMessage = role == RoleType.ASSISTANT.value && !toolCalls.isNullOrEmpty()

        if (!isAssistantToolCallMessage) {
            return copy(reasoningContent = null)
        }

        if (providerName == AiProvider.OpenAi.value) {
            return copy(reasoningContent = null)
        }

        val needsExplicitReasoningContent = providerName == AiProvider.Kimi.value
        return when {
            !reasoningContent.isNullOrBlank() -> this
            needsExplicitReasoningContent -> copy(reasoningContent = "")
            else -> this
        }
    }

    private fun mapToResponsesRequest(request: LlmTurnRequest): ResponsesApiRequest {
        val tools = buildList<JsonObject> {
            request.tools?.forEach { tool ->
                add(tool.toResponsesToolJson())
            }
            if (request.webSearchEnabled) {
                add(gson.toJsonTree(ResponsesWebSearchTool()).asJsonObject)
            }
        }.takeIf { it.isNotEmpty() }
        return ResponsesApiRequest(
            model = request.model,
            input = request.messages.flatMap { it.toResponsesInputItems() },
            stream = request.stream,
            tools = tools,
            reasoning = if (request.reasoningEnabled) {
                ReasoningOptions()
            } else {
                null
            },
            previousResponseId = request.previousResponseId?.takeIf { it.isNotBlank() }
        )
    }

    private fun ToolDefinition.toResponsesToolJson(): JsonObject {
        return JsonObject().apply {
            addProperty("type", type)
            addProperty("name", function.name)
            addProperty("description", function.description)
            function.parameters?.let { add("parameters", gson.toJsonTree(it)) }
        }
    }

    private fun LlmMessage.toResponsesInputItems(): List<ResponsesApiInputItem> {
        if (role == RoleType.TOOL.value) {
            return listOf(
                ResponsesApiInputItem.FunctionCallOutput(
                    callId = toolCallId ?: "",
                    output = textContent()
                )
            )
        }

        val items = mutableListOf<ResponsesApiInputItem>()
        if (parts.isNotEmpty()) {
            items += ResponsesApiInputItem.Message(
                role = role,
                content = parts.map { part ->
                    when (part) {
                        is LlmContentPart.Text -> ResponsesApiContentItem.InputText(text = part.text)
                        is LlmContentPart.ImageUrlPart -> ResponsesApiContentItem.InputImage(imageUrl = part.url)
                    }
                }
            )
        }
        if (toolCalls.isNotEmpty()) {
            items += toolCalls.map {
                ResponsesApiInputItem.FunctionCall(
                    callId = it.id,
                    name = it.function.name,
                    arguments = it.function.arguments
                )
            }
        }
        return items
    }

    private fun mapChatChunkToEvents(
        chunk: ChatCompletionChunk,
        emittedResponseId: String? = null
    ): List<LlmStreamEvent> {
        if (chunk.errorCode != 0) return listOf(chunk.toErrorEvent())
        val events = mutableListOf<LlmStreamEvent>()
        val responseId = chunk.id?.takeIf { it.isNotBlank() }
        if (responseId != null && responseId != emittedResponseId) {
            events += LlmStreamEvent.ResponseStarted(responseId = responseId, model = chunk.model)
        }
        chunk.usage?.takeIf { it.totalTokens > 0 }?.let { events += LlmStreamEvent.UsageUpdated(it) }
        if (!chunk.searchResults.isNullOrEmpty() || chunk.searchInfo?.searchResults?.isNotEmpty() == true) {
            events += LlmStreamEvent.SearchResultsEvent(chunk.searchResults, chunk.searchInfo)
        }
        val choice = chunk.choices.firstOrNull()
        choice?.delta?.toolCalls?.takeIf { it.isNotEmpty() }?.let {
            events += LlmStreamEvent.ToolCallDeltaEvent(it)
        }
        if (choice?.delta?.toolCalls == null) {
            choice?.message?.toolCalls?.takeIf { it.isNotEmpty() }?.let { toolCalls ->
                events += LlmStreamEvent.ToolCallDeltaEvent(toolCalls.mapIndexed { index, toolCall ->
                    ToolCallDelta(
                        index = index,
                        id = toolCall.id,
                        type = toolCall.type,
                        function = FunctionCallDelta(
                            name = toolCall.function.name,
                            arguments = toolCall.function.arguments
                        )
                    )
                })
            }
        }
        choice?.delta?.content?.takeIf { it.isNotEmpty() }?.let {
            events += LlmStreamEvent.TextDelta(it)
        } ?: choice?.message?.content?.takeIf { choice.delta == null && !it.isNullOrEmpty() }?.let {
            events += LlmStreamEvent.TextDelta(it)
        }
        choice?.delta?.reasoningContent?.takeIf { it.isNotEmpty() }?.let {
            events += LlmStreamEvent.ReasoningDelta(it)
        } ?: choice?.message?.reasoningContent?.takeIf { choice.delta == null && !it.isNullOrEmpty() }?.let {
            events += LlmStreamEvent.ReasoningDelta(it)
        }
        val isEnd = chunk.isEnd || choice?.message != null || (choice?.finishReason != null && choice.finishReason != "null")
        if (isEnd) {
            events += LlmStreamEvent.Completed(
                responseId = responseId,
                finishReason = choice?.finishReason
            )
        }
        return events
    }

    private fun parseResponsesSsePayload(
        payload: String,
        eventName: String?,
        toolStates: MutableMap<Int, ResponseToolState>,
        contentStates: MutableMap<ResponseContentKey, StringBuilder>
    ): List<LlmStreamEvent> {
        if (payload.isBlank() || payload == "[DONE]") return emptyList()
        val json = runCatching { JsonParser.parseString(payload).asJsonObject }.getOrNull()
            ?: return listOf(LlmStreamEvent.Error(errorMessage = payload))
        val type = json.string("type") ?: eventName.orEmpty()
        return when (type) {
            "response.created", "response.in_progress" -> {
                val response = json.objectValue("response") ?: json
                val responseId = response.string("id").orEmpty()
                if (responseId.isBlank()) emptyList()
                else listOf(LlmStreamEvent.ResponseStarted(responseId = responseId, model = response.string("model")))
            }
            "response.output_text.delta" -> listOfNotNull(
                emitResponsesContentDelta(
                    outputIndex = json.intValue("output_index") ?: 0,
                    contentIndex = json.intValue("content_index") ?: 0,
                    contentType = type,
                    delta = json.string("delta").orEmpty(),
                    contentStates = contentStates
                )
            )
            "response.reasoning_text.delta", "response.reasoning_summary_text.delta" -> {
                listOfNotNull(
                    emitResponsesContentDelta(
                        outputIndex = json.intValue("output_index") ?: 0,
                        contentIndex = json.intValue("content_index") ?: 0,
                        contentType = type,
                        delta = json.string("delta").orEmpty(),
                        contentStates = contentStates
                    )
                )
            }
            "response.output_text.done", "response.reasoning_text.done", "response.reasoning_summary_text.done" -> {
                listOfNotNull(
                    emitResponsesContentSnapshot(
                        outputIndex = json.intValue("output_index") ?: 0,
                        contentIndex = json.intValue("content_index") ?: 0,
                        contentType = type,
                        text = json.string("text").orEmpty(),
                        contentStates = contentStates,
                        deduplicateAgainstState = true
                    )
                )
            }
            "response.content_part.added", "response.content_part.done" -> {
                val part = json.objectValue("part") ?: return emptyList()
                listOfNotNull(
                    emitResponsesContentSnapshot(
                        outputIndex = json.intValue("output_index") ?: 0,
                        contentIndex = json.intValue("content_index") ?: 0,
                        contentType = part.string("type"),
                        text = part.string("text").orEmpty(),
                        contentStates = contentStates,
                        deduplicateAgainstState = true
                    )
                )
            }
            "response.output_item.added", "response.output_item.done" -> {
                val item = json.objectValue("item") ?: return emptyList()
                val index = json.intValue("output_index") ?: 0
                parseResponsesOutputItem(
                    item = item,
                    index = index,
                    toolStates = toolStates,
                    contentStates = contentStates,
                    deduplicateAgainstState = true
                )
            }
            "response.function_call_arguments.delta" -> {
                val index = json.intValue("output_index") ?: 0
                val state = toolStates.getOrPut(index) { ResponseToolState(index = index) }
                val delta = json.string("delta").orEmpty()
                if (delta.isNotEmpty()) state.arguments.append(delta)
                listOf(
                    LlmStreamEvent.ToolCallDeltaEvent(
                        listOf(
                            ToolCallDelta(
                                index = index,
                                id = state.callId.takeIf { it.isNotBlank() },
                                type = "function",
                                function = FunctionCallDelta(
                                    name = state.name.takeIf { it.isNotBlank() },
                                    arguments = delta
                                )
                            )
                        )
                    )
                )
            }
            "response.function_call_arguments.done" -> {
                val index = json.intValue("output_index") ?: 0
                val state = toolStates.getOrPut(index) { ResponseToolState(index = index) }
                val fullArguments = json.stringOrJson("arguments").orEmpty()
                val delta = when {
                    fullArguments.isBlank() -> ""
                    fullArguments.startsWith(state.arguments.toString()) -> {
                        fullArguments.removePrefix(state.arguments.toString())
                    }
                    state.arguments.isEmpty() -> fullArguments
                    else -> ""
                }
                if (delta.isEmpty()) {
                    emptyList()
                } else {
                    state.arguments.append(delta)
                    listOf(
                        LlmStreamEvent.ToolCallDeltaEvent(
                            listOf(
                                ToolCallDelta(
                                    index = index,
                                    id = state.callId.takeIf { it.isNotBlank() },
                                    type = "function",
                                    function = FunctionCallDelta(
                                        name = null,
                                        arguments = delta
                                    )
                                )
                            )
                        )
                    )
                }
            }
            "response.completed" -> {
                val response = json.objectValue("response") ?: json
                buildList {
                    response.string("id")?.takeIf { it.isNotBlank() }?.let {
                        add(LlmStreamEvent.ResponseStarted(responseId = it, model = response.string("model")))
                    }
                    response.objectValue("usage")?.toUsageEvent()?.let { add(it) }
                    response.arrayValue("output")?.forEachIndexed { index, element ->
                        if (element is JsonObject) {
                            addAll(
                                parseResponsesOutputItem(
                                    item = element,
                                    index = index,
                                    toolStates = toolStates,
                                    contentStates = contentStates,
                                    deduplicateAgainstState = true
                                )
                            )
                        }
                    }
                    add(
                        LlmStreamEvent.Completed(
                            responseId = response.string("id"),
                            finishReason = response.string("status") ?: "completed",
                            outputItemsJson = response.arrayValue("output")?.toString()
                        )
                    )
                }
            }
            "error", "response.failed", "response.incomplete" -> {
                listOf(LlmStreamEvent.Error(errorMessage = json.string("message") ?: payload))
            }
            else -> emptyList()
        }
    }

    private fun parseResponsesResponse(body: String): List<LlmStreamEvent> {
        if (body.isBlank()) return listOf(LlmStreamEvent.Error(errorMessage = "Empty response body"))
        val json = runCatching { JsonParser.parseString(body).asJsonObject }.getOrNull()
            ?: return listOf(LlmStreamEvent.Error(errorMessage = body))
        val events = mutableListOf<LlmStreamEvent>()
        val responseId = json.string("id")
        responseId?.takeIf { it.isNotBlank() }?.let {
            events += LlmStreamEvent.ResponseStarted(responseId = it, model = json.string("model"))
        }
        json.objectValue("usage")?.toUsageEvent()?.let { events += it }
        val toolStates = mutableMapOf<Int, ResponseToolState>()
        val contentStates = mutableMapOf<ResponseContentKey, StringBuilder>()
        json.arrayValue("output")?.forEachIndexed { index, element ->
            if (element is JsonObject) {
                events += parseResponsesOutputItem(
                    item = element,
                    index = index,
                    toolStates = toolStates,
                    contentStates = contentStates,
                    deduplicateAgainstState = false
                )
            }
        }
        events += LlmStreamEvent.Completed(
            responseId = responseId,
            finishReason = json.string("status") ?: "completed",
            outputItemsJson = json.arrayValue("output")?.toString()
        )
        return events
    }

    private fun parseResponsesOutputItem(
        item: JsonObject,
        index: Int,
        toolStates: MutableMap<Int, ResponseToolState>,
        contentStates: MutableMap<ResponseContentKey, StringBuilder>,
        deduplicateAgainstState: Boolean
    ): List<LlmStreamEvent> {
        return when (item.string("type")) {
            "message" -> {
                val content = item.arrayValue("content") ?: return emptyList()
                content.mapIndexedNotNull { contentIndex, child ->
                    val childObj = child as? JsonObject ?: return@mapIndexedNotNull null
                    emitResponsesContentSnapshot(
                        outputIndex = index,
                        contentIndex = contentIndex,
                        contentType = childObj.string("type"),
                        text = childObj.string("text").orEmpty(),
                        contentStates = contentStates,
                        deduplicateAgainstState = deduplicateAgainstState
                    )
                }
            }
            "function_call", "tool_call" -> {
                val state = toolStates.getOrPut(index) { ResponseToolState(index = index) }
                val callId = item.responseToolCallId().orEmpty()
                val callIdDelta = if (callId.isNotBlank() && state.callId.isBlank()) {
                    state.callId = callId
                    callId
                } else {
                    null
                }

                val fullName = item.string("name").orEmpty()
                val nameDelta = when {
                    fullName.isBlank() -> null
                    state.name.isBlank() -> {
                        state.name = fullName
                        fullName
                    }
                    fullName.startsWith(state.name) && fullName.length > state.name.length -> {
                        fullName.removePrefix(state.name).also { state.name = fullName }
                    }
                    else -> null
                }

                val fullArguments = item.stringOrJson("arguments").orEmpty()
                val argumentsDelta = when {
                    fullArguments.isBlank() -> null
                    fullArguments.startsWith(state.arguments.toString()) -> {
                        fullArguments.removePrefix(state.arguments.toString()).ifEmpty { null }
                    }
                    state.arguments.isEmpty() -> fullArguments
                    else -> null
                }

                argumentsDelta?.let { state.arguments.append(it) }

                if (callIdDelta == null && nameDelta == null && argumentsDelta == null) {
                    emptyList()
                } else {
                    listOf(
                        LlmStreamEvent.ToolCallDeltaEvent(
                            listOf(
                                ToolCallDelta(
                                    index = index,
                                    id = state.callId.takeIf { it.isNotBlank() },
                                    type = "function",
                                    function = FunctionCallDelta(
                                        name = nameDelta,
                                        arguments = argumentsDelta
                                    )
                                )
                            )
                        )
                    )
                }
            }
            "reasoning" -> {
                val summary = item.arrayValue("summary") ?: return emptyList()
                summary.mapIndexedNotNull { contentIndex, child ->
                    val obj = child as? JsonObject ?: return@mapIndexedNotNull null
                    emitResponsesContentSnapshot(
                        outputIndex = index,
                        contentIndex = contentIndex,
                        contentType = obj.string("type") ?: "reasoning_text",
                        text = obj.string("text").orEmpty(),
                        contentStates = contentStates,
                        deduplicateAgainstState = deduplicateAgainstState
                    )
                }
            }
            else -> emptyList()
        }
    }

    private fun emitResponsesContentDelta(
        outputIndex: Int,
        contentIndex: Int,
        contentType: String?,
        delta: String,
        contentStates: MutableMap<ResponseContentKey, StringBuilder>
    ): LlmStreamEvent? {
        val kind = contentType.toResponseContentKind() ?: return null
        if (delta.isBlank()) return null
        val state = contentStates.getOrPut(ResponseContentKey(outputIndex, contentIndex, kind)) { StringBuilder() }
        state.append(delta)
        return kind.toEvent(delta)
    }

    private fun emitResponsesContentSnapshot(
        outputIndex: Int,
        contentIndex: Int,
        contentType: String?,
        text: String,
        contentStates: MutableMap<ResponseContentKey, StringBuilder>,
        deduplicateAgainstState: Boolean
    ): LlmStreamEvent? {
        val kind = contentType.toResponseContentKind() ?: return null
        if (text.isBlank()) return null
        val state = contentStates.getOrPut(ResponseContentKey(outputIndex, contentIndex, kind)) { StringBuilder() }
        val delta = if (!deduplicateAgainstState) {
            text
        } else {
            when {
                state.isEmpty() -> text
                text.startsWith(state.toString()) -> text.removePrefix(state.toString())
                state.toString().startsWith(text) -> ""
                else -> ""
            }
        }
        if (delta.isBlank()) return null
        state.append(delta)
        return kind.toEvent(delta)
    }

    private fun String?.toResponseContentKind(): ResponseContentKind? {
        return when (this) {
            "output_text", "input_text", "text", "response.output_text.delta", "response.output_text.done" -> ResponseContentKind.TEXT
            "reasoning_text", "reasoning_summary_text", "response.reasoning_text.delta", "response.reasoning_text.done", "response.reasoning_summary_text.delta", "response.reasoning_summary_text.done" -> ResponseContentKind.REASONING
            else -> null
        }
    }

    private fun ResponseContentKind.toEvent(text: String): LlmStreamEvent {
        return when (this) {
            ResponseContentKind.TEXT -> LlmStreamEvent.TextDelta(text)
            ResponseContentKind.REASONING -> LlmStreamEvent.ReasoningDelta(text)
        }
    }

    private fun JsonObject.toUsageEvent(): LlmStreamEvent.UsageUpdated? {
        return runCatching {
            gson.fromJson(this, com.shifenmiao.model.ai.Usage::class.java)
        }.getOrNull()?.takeIf { it.totalTokens > 0 }?.let { LlmStreamEvent.UsageUpdated(it) }
    }

    private fun ChatCompletionChunk.toErrorEvent(): LlmStreamEvent.Error {
        return LlmStreamEvent.Error(
            errorCode = errorCode,
            errorMessage = errorMsg.ifBlank { "Unknown error" }
        )
    }

    private fun JsonObject.string(name: String): String? =
        get(name)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.asString

    private fun JsonObject.stringOrJson(name: String): String? =
        get(name)?.takeIf { !it.isJsonNull }?.let { element ->
            if (element.isJsonPrimitive) element.asString else element.toString()
        }

    private fun JsonObject.intValue(name: String): Int? =
        get(name)?.takeIf { !it.isJsonNull }?.asInt

    private fun JsonObject.responseToolCallId(): String? =
        string("call_id")
            ?: string("tool_call_id")
            ?: string("id")

    private fun JsonObject.objectValue(name: String): JsonObject? =
        getAsJsonObject(name)

    private fun JsonObject.arrayValue(name: String): JsonArray? =
        getAsJsonArray(name)

    private data class ResponseToolState(
        val index: Int,
        var callId: String = "",
        var name: String = "",
        val arguments: StringBuilder = StringBuilder()
    )

    private data class ResponseContentKey(
        val outputIndex: Int,
        val contentIndex: Int,
        val kind: ResponseContentKind
    )

    private enum class ResponseContentKind {
        TEXT,
        REASONING
    }

    /**
     * 将 OpenAI ChatCompletionRequest 转换为 Anthropic MessagesRequest。
     *
     * 这里保留独立转换函数，而不是把 Anthropic 分支塞进统一模型本身，
     * 目的是让统一事件层保持 provider-agnostic，协议差异集中在 adapter 内部。
     */
    private fun convertToAnthropicRequest(
        request: ChatCompletionRequest
    ): AnthropicMessagesRequest {
        val systemMessage = request.messages
            .firstOrNull { it.role == RoleType.SYSTEM.value }
            ?.let { msg ->
                when (val content = msg.content) {
                    is ListOrStringContent.StringContent -> content.content
                    is ListOrStringContent.ListContent -> {
                        content.items.filterIsInstance<ContentItem.TextContent>()
                            .joinToString("\n") { it.text }
                    }
                    null -> null
                }
            }

        val anthropicTools = request.tools?.map { toolDef ->
            AnthropicTool(
                name = toolDef.function.name,
                description = toolDef.function.description,
                inputSchema = toolDef.function.parameters ?: emptyMap<String, Any>()
            )
        }?.takeIf { it.isNotEmpty() }

        val otherMessages = request.messages
            .filter { it.role != RoleType.SYSTEM.value }
            .flatMap { msg -> convertAnthropicMessage(msg) }

        return AnthropicMessagesRequest(
            model = request.model,
            maxTokens = request.maxTokens ?: 4096,
            system = systemMessage,
            messages = otherMessages,
            stream = request.stream,
            temperature = request.temperature,
            topP = request.topP,
            tools = anthropicTools
        )
    }

    /**
     * 将单条 OpenAI RequestMessage 转换为 Anthropic 消息列表。
     *
     * 特殊处理：
     * - assistant + tool_calls → assistant 消息，content 包含 text + tool_use block
     * - tool 角色 → user 消息，content 为 tool_result block
     */
    private fun convertAnthropicMessage(msg: RequestMessage): List<AnthropicMessage> {
        val result = mutableListOf<AnthropicMessage>()

        when (msg.role) {
            RoleType.ASSISTANT.value -> {
                if (!msg.toolCalls.isNullOrEmpty()) {
                    val blocks = mutableListOf<ContentBlock>()
                    val textContent = extractTextContent(msg.content)
                    if (textContent.isNotBlank()) {
                        blocks.add(ContentBlock(type = "text", text = textContent))
                    }
                    for (tc in msg.toolCalls) {
                        val inputMap = try {
                            @Suppress("UNCHECKED_CAST")
                            Gson().fromJson(tc.function.arguments, Map::class.java) as? Map<String, Any?>
                                ?: emptyMap()
                        } catch (_: Exception) {
                            emptyMap<String, Any?>()
                        }
                        blocks.add(
                            ContentBlock(
                                type = "tool_use",
                                id = tc.id,
                                name = tc.function.name,
                                input = inputMap
                            )
                        )
                    }
                    result.add(
                        AnthropicMessage(
                            role = RoleType.ASSISTANT.value,
                            content = blocks
                        )
                    )
                } else {
                    result.add(
                        AnthropicMessage(
                            role = RoleType.ASSISTANT.value,
                            content = convertContent(msg.content)
                        )
                    )
                }
            }

            RoleType.TOOL.value -> {
                val toolResultBlock = ContentBlock(
                    type = "tool_result",
                    toolUseId = msg.toolCallId ?: "",
                    toolContent = extractTextContent(msg.content)
                )
                result.add(
                    AnthropicMessage(
                        role = RoleType.USER.value,
                        content = listOf(toolResultBlock)
                    )
                )
            }

            "tool_calls" -> Unit

            else -> {
                result.add(
                    AnthropicMessage(
                        role = msg.role,
                        content = convertContent(msg.content)
                    )
                )
            }
        }

        return result
    }

    private fun extractTextContent(content: ListOrStringContent?): String {
        return when (content) {
            is ListOrStringContent.StringContent -> content.content
            is ListOrStringContent.ListContent -> {
                content.items.filterIsInstance<ContentItem.TextContent>()
                    .joinToString("\n") { it.text }
            }
            null -> ""
        }
    }

    private fun convertContent(content: ListOrStringContent?): Any {
        return when (content) {
            is ListOrStringContent.StringContent -> content.content
            is ListOrStringContent.ListContent -> {
                val blocks = mutableListOf<ContentBlock>()
                for (item in content.items) {
                    when (item) {
                        is ContentItem.TextContent -> {
                            blocks.add(ContentBlock(type = "text", text = item.text))
                        }

                        is ContentItem.ImageContent -> {
                            val imageUrl = item.imageUrl.url
                            val imageSource = if (imageUrl.startsWith("data:")) {
                                val commaIndex = imageUrl.indexOf(',')
                                if (commaIndex > 0) {
                                    val header = imageUrl.substring(5, commaIndex)
                                    val mediaType = header.removeSuffix(";base64")
                                    val base64Data = imageUrl.substring(commaIndex + 1)
                                    ImageSource(type = "base64", mediaType = mediaType, data = base64Data)
                                } else {
                                    ImageSource(type = "url", url = imageUrl)
                                }
                            } else {
                                ImageSource(type = "url", url = imageUrl)
                            }
                            blocks.add(ContentBlock(type = "image", source = imageSource))
                        }
                    }
                }
                blocks
            }

            null -> ""
        }
    }

    companion object {
        /**
         * LLM 流式响应 Channel 缓冲容量。
         *
         * 使用有限缓冲避免生产者过快时内存无限增长。
         * 64 个元素对 LLM 流式输出足够（消费者通常能即时处理 UI 更新）。
         */
        const val STREAM_BUFFER_CAPACITY = 64
    }
}

