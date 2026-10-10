package com.shifenmiao.common.ai

import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.AnthropicMessage
import com.shifenmiao.model.ai.AnthropicMessagesRequest
import com.shifenmiao.model.ai.AnthropicMessagesResponse
import com.shifenmiao.model.ai.AuthType
import com.shifenmiao.model.ai.ChatCompletionChunk
import com.shifenmiao.model.ai.ChatCompletionRequest
import com.shifenmiao.model.ai.ChunkChoice
import com.shifenmiao.model.ai.Message
import com.shifenmiao.model.ai.RequestMessage
import com.shifenmiao.model.ai.RoleType
import com.shifenmiao.model.ai.Usage
import com.shifenmiao.model.ai.openai.responses.ResponsesApiContentItem
import com.shifenmiao.model.ai.openai.responses.ResponsesApiInputItem
import com.shifenmiao.model.ai.openai.responses.ResponsesApiRequest
import com.shifenmiao.network.AiRequestUrlResolver
import com.shifenmiao.network.api.AnthropicCompatibleService
import com.shifenmiao.network.api.OpenAICompatibleService
import com.shifenmiao.network.api.OpenAIWithApiKeyService
import com.shifenmiao.network.api.OwnProxyAIService
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.awaitResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIPromptClient internal constructor(
    private val openAICompatibleService: OpenAICompatibleService,
    private val ownProxyAIService: OwnProxyAIService,
    private val openAIWithApiKeyService: OpenAIWithApiKeyService,
    private val anthropicCompatibleService: AnthropicCompatibleService,
    private val resolveRequestUrl: (AiEngine) -> String,
) {
    @Inject
    constructor(
        openAICompatibleService: OpenAICompatibleService,
        ownProxyAIService: OwnProxyAIService,
        openAIWithApiKeyService: OpenAIWithApiKeyService,
        anthropicCompatibleService: AnthropicCompatibleService,
    ) : this(
        openAICompatibleService, ownProxyAIService, openAIWithApiKeyService, anthropicCompatibleService,
        AiRequestUrlResolver::resolveRequestUrl,
    )

    suspend fun execute(engine: AiEngine, input: String, systemPrompt: String): Response<ResponseBody> {
        val url = resolveRequestUrl(engine)
        val direct = AiRequestUrlResolver.shouldUseDirectRequest(engine)
        val messages = buildList {
            if (systemPrompt.isNotBlank()) add(RoleType.SYSTEM.value to systemPrompt)
            add(RoleType.USER.value to input)
        }
        val call = when (engine.requestProtocol) {
            AiRequestProtocol.ANTHROPIC_COMPATIBLE -> {
                val request = AnthropicMessagesRequest(
                    model = engine.model.name,
                    system = systemPrompt.takeIf { it.isNotBlank() },
                    messages = listOf(AnthropicMessage(RoleType.USER.value, input)),
                    stream = false,
                )
                if (direct) anthropicCompatibleService.messagesNoStreaming(
                    url = url, apiKey = AiRequestUrlResolver.resolveApiKey(engine).orEmpty(), request = request,
                ) else ownProxyAIService.messagesNoStreaming(url = url, request = request)
            }
            AiRequestProtocol.RESPONSES_COMPATIBLE -> {
                val request = ResponsesApiRequest(
                    model = engine.model.name,
                    input = messages.map { (role, text) ->
                        ResponsesApiInputItem.Message(role = role, content = listOf(ResponsesApiContentItem.InputText(text = text)))
                    },
                    stream = false,
                )
                when {
                    !direct -> ownProxyAIService.responsesNoStreaming(url = url, request = request)
                    engine.authType == AuthType.API_KEY -> openAIWithApiKeyService.responsesNoStreaming(
                        url = url, apiKey = AiRequestUrlResolver.resolveApiKey(engine).orEmpty(), responsesApiRequest = request,
                    )
                    else -> openAICompatibleService.responsesNoStreaming(
                        url = url, authorization = AiRequestUrlResolver.resolveAuthorizationHeader(engine), responsesApiRequest = request,
                    )
                }
            }
            AiRequestProtocol.OPENAI_COMPATIBLE, AiRequestProtocol.OWN_PROXY -> {
                val request = ChatCompletionRequest(
                    model = engine.model.name,
                    messages = messages.map { (role, text) -> RequestMessage.createTextMessage(role = role, text = text) },
                    stream = false,
                )
                when {
                    !direct -> ownProxyAIService.chatNoStreaming(url = url, chatCompletionRequest = request)
                    engine.authType == AuthType.API_KEY -> openAIWithApiKeyService.chatNoStreaming(
                        url = url, apiKey = AiRequestUrlResolver.resolveApiKey(engine).orEmpty(), chatCompletionRequest = request,
                    )
                    else -> openAICompatibleService.chatNoStreaming(
                        url = url, authorization = AiRequestUrlResolver.resolveAuthorizationHeader(engine), chatCompletionRequest = request,
                    )
                }
            }
            AiRequestProtocol.JEV, AiRequestProtocol.PIKAFISH, AiRequestProtocol.LOCAL_ON_DEVICE ->
                error("Unsupported prompt protocol: ${engine.requestProtocol}")
        }
        return call.awaitResponse()
    }

    fun decodeResponse(body: String, protocol: AiRequestProtocol): ChatCompletionChunk {
        val json = AppJson.parseToJsonElement(body).jsonObject
        val error = json["error"]
        if (error != null && error != JsonNull) {
            return ChatCompletionChunk(errorCode = 1, errorMsg = (error as? JsonObject)?.string("message") ?: error.toString())
        }
        // Some app proxies return the normalized chat envelope for native protocols.
        if ("choices" in json || "error_code" in json || "error_msg" in json ||
            protocol == AiRequestProtocol.OPENAI_COMPATIBLE || protocol == AiRequestProtocol.OWN_PROXY
        ) {
            return AppJson.decodeFromJsonElement(json)
        }
        return when (protocol) {
            AiRequestProtocol.ANTHROPIC_COMPATIBLE -> {
                val response = AppJson.decodeFromJsonElement<AnthropicMessagesResponse>(json)
                val text = response.content.filter { it.type == "text" }.mapNotNull { it.text }.joinToString("")
                normalizedChunk(text, response.usage?.let {
                    Usage(promptTokens = it.inputTokens, completionTokens = it.outputTokens, totalTokens = it.inputTokens + it.outputTokens)
                })
            }
            AiRequestProtocol.RESPONSES_COMPATIBLE -> {
                val status = json.string("status")
                if (status != null && status != "completed") {
                    return ChatCompletionChunk(errorCode = 1, errorMsg = "Response $status: ${json["incomplete_details"] ?: ""}")
                }
                val output = json["output"] as? JsonArray ?: throw SerializationException("Missing response output")
                val text = output.filterIsInstance<JsonObject>().filter { it.string("type") == "message" }
                    .flatMap { (it["content"] as? JsonArray)?.filterIsInstance<JsonObject>().orEmpty() }
                    .filter { it.string("type") == "output_text" }.mapNotNull { it.string("text") }.joinToString("")
                val usage = (json["usage"] as? JsonObject)?.let {
                    val inputTokens = it.int("input_tokens")
                    val outputTokens = it.int("output_tokens")
                    Usage(promptTokens = inputTokens, completionTokens = outputTokens,
                        totalTokens = (it["total_tokens"] as? JsonPrimitive)?.intOrNull ?: inputTokens + outputTokens)
                }
                normalizedChunk(text, usage)
            }
            else -> error("Unsupported prompt protocol: $protocol")
        }
    }

    private fun normalizedChunk(text: String, usage: Usage?): ChatCompletionChunk =
        if (text.isBlank()) ChatCompletionChunk(errorCode = 1, errorMsg = "Empty response content")
        else ChatCompletionChunk(choices = listOf(ChunkChoice(message = Message(role = RoleType.ASSISTANT.value, content = text))), usage = usage)

    private fun JsonObject.string(key: String): String? =
        (get(key) as? JsonPrimitive)?.takeIf { it.isString }?.content

    private fun JsonObject.int(key: String): Int = (get(key) as? JsonPrimitive)?.intOrNull ?: 0
}
