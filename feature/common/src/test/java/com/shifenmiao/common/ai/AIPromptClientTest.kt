package com.shifenmiao.common.ai

import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.AnthropicMessagesRequest
import com.shifenmiao.model.ai.AuthType
import com.shifenmiao.model.ai.ChatCompletionRequest
import com.shifenmiao.model.ai.openai.responses.ResponsesApiContentItem
import com.shifenmiao.model.ai.openai.responses.ResponsesApiInputItem
import com.shifenmiao.model.ai.openai.responses.ResponsesApiRequest
import com.shifenmiao.network.api.AnthropicCompatibleService
import com.shifenmiao.network.api.OpenAICompatibleService
import com.shifenmiao.network.api.OpenAIWithApiKeyService
import com.shifenmiao.network.api.OwnProxyAIService
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.SerializationException
import okhttp3.Request
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Timeout
import org.junit.Test
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Proxy
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AIPromptClientTest {
    @Test
    fun cancellingAnyPromptProtocolCancelsHttpWithoutWaitingForTheResponse() = runBlocking {
        for (protocol in supportedProtocols) {
            for (direct in listOf(true, false)) {
                val services = RecordingServices()
                val waiting = async(start = CoroutineStart.UNDISPATCHED) {
                    services.client.execute(engine(protocol, direct), "position", "rules")
                }
                assertTrue(services.call.isExecuted())
                withTimeout(1_000) { waiting.cancelAndJoin() }
                assertTrue(services.call.isCanceled())
                services.call.complete("""{"choices":[{"message":{"content":"late"}}]}""")
                assertTrue(waiting.isCancelled)
            }
        }
    }

    @Test
    fun chatRequestsKeepTheSavedModelAndSystemPromptAndDeliverTheResponse() = runBlocking {
        val services = RecordingServices()
        val waiting = async(start = CoroutineStart.UNDISPATCHED) {
            services.client.execute(engine(), "position", "rules")
        }
        assertEquals("openai", services.service)
        assertEquals("chatNoStreaming", services.method)
        assertEquals("Bearer test-key", services.arguments[1])
        val request = assertIs<ChatCompletionRequest>(services.arguments[2])
        assertEquals("saved-model", request.model)
        assertEquals(listOf("system", "user"), request.messages.map { it.role })
        assertFalse(request.stream)
        services.call.complete("""{"choices":[{"message":{"content":"e2e4"}}]}""")
        val body = waiting.await().body()?.use { it.string() }.orEmpty()
        assertEquals("e2e4", services.client.decodeResponse(body, AiRequestProtocol.OPENAI_COMPATIBLE).choices.first().message?.content)
    }

    @Test
    fun apiKeyChatProvidersUseTheApiKeyService() = runBlocking {
        val services = RecordingServices()
        val waiting = async(start = CoroutineStart.UNDISPATCHED) {
            services.client.execute(engine().copy(authType = AuthType.API_KEY), "position", "")
        }
        assertEquals("api-key", services.service)
        assertEquals("test-key", services.arguments[1])
        assertEquals(1, assertIs<ChatCompletionRequest>(services.arguments[2]).messages.size)
        waiting.cancelAndJoin()
    }

    @Test
    fun responsesUsesNativeInputAndTheSelectedAuthenticationType() = runBlocking {
        for (authType in listOf(AuthType.BEARER, AuthType.API_KEY)) {
            val services = RecordingServices()
            val waiting = async(start = CoroutineStart.UNDISPATCHED) {
                services.client.execute(engine(AiRequestProtocol.RESPONSES_COMPATIBLE).copy(authType = authType), "position", "rules")
            }
            assertEquals(if (authType == AuthType.API_KEY) "api-key" else "openai", services.service)
            assertEquals("responsesNoStreaming", services.method)
            val request = assertIs<ResponsesApiRequest>(services.arguments[2])
            assertEquals("saved-model", request.model)
            assertFalse(request.stream)
            val messages = request.input.map { assertIs<ResponsesApiInputItem.Message>(it) }
            assertEquals(listOf("system", "user"), messages.map { it.role })
            assertEquals(listOf("rules", "position"), messages.map { assertIs<ResponsesApiContentItem.InputText>(it.content.single()).text })
            waiting.cancelAndJoin()
        }
    }

    @Test
    fun anthropicUsesNativeMessagesAndApiKeyWithSystemSeparate() = runBlocking {
        val services = RecordingServices()
        val waiting = async(start = CoroutineStart.UNDISPATCHED) {
            services.client.execute(engine(AiRequestProtocol.ANTHROPIC_COMPATIBLE), "position", "rules")
        }
        assertEquals("anthropic", services.service)
        assertEquals("messagesNoStreaming", services.method)
        assertEquals("test-key", services.arguments[1])
        val request = assertIs<AnthropicMessagesRequest>(services.arguments[3])
        assertEquals("saved-model", request.model)
        assertEquals("rules", request.system)
        assertEquals("user", request.messages.single().role)
        assertEquals("position", request.messages.single().content)
        assertTrue(request.maxTokens > 0)
        assertFalse(request.stream)
        waiting.cancelAndJoin()
    }

    @Test
    fun proxiesUseTheAuthenticatedAppServiceAndMatchingProtocolPayload() = runBlocking {
        for (protocol in supportedProtocols) {
            val services = RecordingServices()
            val waiting = async(start = CoroutineStart.UNDISPATCHED) {
                services.client.execute(engine(protocol, false), "position", "rules")
            }
            assertEquals("proxy", services.service)
            assertEquals("https://proxy.invalid/prompt", services.arguments[0])
            when (protocol) {
                AiRequestProtocol.RESPONSES_COMPATIBLE -> assertIs<ResponsesApiRequest>(services.arguments[1])
                AiRequestProtocol.ANTHROPIC_COMPATIBLE -> assertIs<AnthropicMessagesRequest>(services.arguments[1])
                else -> assertIs<ChatCompletionRequest>(services.arguments[1])
            }
            waiting.cancelAndJoin()
        }
    }

    @Test
    fun responsesTextExcludesReasoningAndPreservesTokenUsage() {
        val response = RecordingServices().client.decodeResponse(
            """{"status":"completed","output":[
                {"type":"reasoning","summary":[{"type":"summary_text","text":"private reasoning"}]},
                {"type":"message","content":[{"type":"output_text","text":"e2"},{"type":"output_text","text":"e4"}]}
            ],"usage":{"input_tokens":11,"output_tokens":7,"total_tokens":18}}""",
            AiRequestProtocol.RESPONSES_COMPATIBLE,
        )
        assertEquals("e2e4", response.choices.single().message?.content)
        assertEquals(18, response.usage?.totalTokens)
        assertEquals(11, response.usage?.promptTokens)
    }

    @Test
    fun anthropicTextExcludesThinkingAndPreservesTokenUsage() {
        val response = RecordingServices().client.decodeResponse(
            """{"content":[{"type":"thinking","thinking":"private reasoning"},{"type":"text","text":"e2"},{"type":"text","text":"e4"}],
                "usage":{"input_tokens":11,"output_tokens":7}}""",
            AiRequestProtocol.ANTHROPIC_COMPATIBLE,
        )
        assertEquals("e2e4", response.choices.single().message?.content)
        assertEquals(18, response.usage?.totalTokens)
    }

    @Test
    fun normalizedProxyResponsesAreAcceptedForEitherNativeProtocol() {
        val body = """{"choices":[{"message":{"content":"H8"}}],"usage":{"total_tokens":4}}"""
        for (protocol in supportedProtocols) {
            val response = RecordingServices().client.decodeResponse(body, protocol)
            assertEquals("H8", response.choices.single().message?.content)
            assertEquals(4, response.usage?.totalTokens)
        }
    }

    @Test
    fun apiErrorsAndIncompleteNativeResponsesCannotLookLikeSuccess() {
        val client = RecordingServices().client
        for (protocol in supportedProtocols) {
            val response = client.decodeResponse("""{"error":{"message":"unavailable"}}""", protocol)
            assertNotEquals(0, response.errorCode)
            assertEquals("unavailable", response.errorMsg)
            val proxyError = client.decodeResponse("""{"error_code":5,"error_msg":"unavailable"}""", protocol)
            assertEquals(5, proxyError.errorCode)
            assertEquals("unavailable", proxyError.errorMsg)
        }
        val incomplete = client.decodeResponse(
            """{"status":"incomplete","incomplete_details":{"reason":"max_output_tokens"},"output":[]}""",
            AiRequestProtocol.RESPONSES_COMPATIBLE,
        )
        assertNotEquals(0, incomplete.errorCode)
        assertTrue(incomplete.errorMsg.contains("max_output_tokens"))
        assertNotEquals(0, client.decodeResponse("""{"content":[]}""", AiRequestProtocol.ANTHROPIC_COMPATIBLE).errorCode)
        assertNotEquals(0, client.decodeResponse("""{"status":"completed","output":[]}""", AiRequestProtocol.RESPONSES_COMPATIBLE).errorCode)
        assertFailsWith<SerializationException> { client.decodeResponse("{}", AiRequestProtocol.RESPONSES_COMPATIBLE) }
    }

    private fun engine(protocol: AiRequestProtocol = AiRequestProtocol.OPENAI_COMPATIBLE, direct: Boolean = true): AiEngine = AiEngine(
        name = "test-provider",
        model = AiModel(name = "saved-model", title = "Saved model", updateTime = 1),
        requestProtocol = protocol,
        authType = if (protocol == AiRequestProtocol.ANTHROPIC_COMPATIBLE) AuthType.API_KEY else AuthType.BEARER,
        requestUrl = "https://example.invalid/",
        requestPath = "/prompt",
        proxyUrl = "https://proxy.invalid/",
        proxyPath = "/prompt",
        authorizationCode = if (direct) "test-key" else "",
        isDetestPassed = direct,
    )

    private class RecordingServices {
        var service = ""
        var method = ""
        var arguments: Array<out Any?> = emptyArray()
        val call = ControlledCall()
        val client = AIPromptClient(
            stub(OpenAICompatibleService::class.java, "openai"),
            stub(OwnProxyAIService::class.java, "proxy"),
            stub(OpenAIWithApiKeyService::class.java, "api-key"),
            stub(AnthropicCompatibleService::class.java, "anthropic"),
            resolveRequestUrl = { if (it.isDetestPassed) "https://example.invalid/prompt" else "https://proxy.invalid/prompt" },
        )

        private fun <T> stub(type: Class<T>, name: String): T = type.cast(Proxy.newProxyInstance(
            type.classLoader, arrayOf(type),
        ) { _, invoked, args ->
            service = name
            method = invoked.name
            arguments = args.orEmpty()
            call
        })
    }

    private class ControlledCall : Call<ResponseBody> {
        private lateinit var callback: Callback<ResponseBody>
        private var cancelled = false
        private var executed = false
        fun complete(body: String) = callback.onResponse(this, Response.success(body.toResponseBody()))
        override fun enqueue(callback: Callback<ResponseBody>) { executed = true; this.callback = callback }
        override fun cancel() { cancelled = true }
        override fun isCanceled(): Boolean = cancelled
        override fun isExecuted(): Boolean = executed
        override fun clone(): Call<ResponseBody> = ControlledCall()
        override fun execute(): Response<ResponseBody> = error("Prompt requests must be cancellable")
        override fun request(): Request = Request.Builder().url("https://example.invalid/").build()
        override fun timeout(): Timeout = Timeout()
    }

    private val supportedProtocols = listOf(
        AiRequestProtocol.OPENAI_COMPATIBLE,
        AiRequestProtocol.OWN_PROXY,
        AiRequestProtocol.RESPONSES_COMPATIBLE,
        AiRequestProtocol.ANTHROPIC_COMPATIBLE,
    )
}
