/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.imagegeneration.provider.qwen

import com.shifenmiao.imagegeneration.model.ImageGenerationRequest
import com.shifenmiao.imagegeneration.model.ImageProviderConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QwenImageProviderTest {

    @Test
    fun `token uses direct endpoint and authorization`() = runBlocking {
        val directApi = RecordingApi()
        val proxyApi = RecordingApi()
        val provider = QwenImageProvider(directApi, proxyApi)

        val result = provider.generate(
            config = config(apiToken = " token ", baseUrl = "https://dashscope.example/"),
            request = ImageGenerationRequest(prompt = "cat"),
        )

        assertTrue(result.isSuccess)
        assertEquals("https://dashscope.example/api/v1/services/aigc/multimodal-generation/generation", directApi.url)
        assertEquals("Bearer token", directApi.authorization)
        assertNull(proxyApi.url)
    }

    @Test
    fun `blank token uses proxy endpoint without provider authorization`() = runBlocking {
        val directApi = RecordingApi()
        val proxyApi = RecordingApi()
        val provider = QwenImageProvider(directApi, proxyApi)

        val result = provider.generate(
            config = config(apiToken = " ", proxyUrl = "proxy.example", proxyPath = "/images/generate"),
            request = ImageGenerationRequest(prompt = "cat"),
        )

        assertTrue(result.isSuccess)
        assertNull(directApi.url)
        assertEquals("https://proxy.example/images/generate", proxyApi.url)
        assertNull(proxyApi.authorization)
    }

    @Test
    fun `zero and three input images are accepted`() = runBlocking {
        val api = RecordingApi()
        val provider = QwenImageProvider(api, RecordingApi())

        assertTrue(provider.generate(config(apiToken = "token"), ImageGenerationRequest("cat")).isSuccess)
        assertTrue(
            provider.generate(
                config(apiToken = "token"),
                ImageGenerationRequest("edit", listOf("one", "two", "three")),
            ).isSuccess
        )
        assertEquals(4, api.request?.input?.messages?.single()?.content?.size)
    }

    @Test
    fun `more than three or blank input images are rejected`() = runBlocking {
        val provider = QwenImageProvider(RecordingApi(), RecordingApi())

        val tooMany = provider.generate(
            config(apiToken = "token"),
            ImageGenerationRequest("edit", listOf("one", "two", "three", "four")),
        )
        val blank = provider.generate(
            config(apiToken = "token"),
            ImageGenerationRequest("edit", listOf(" ")),
        )

        assertTrue(tooMany.isFailure)
        assertTrue(blank.isFailure)
    }

    @Test
    fun `cancellation is not converted to failure result`() {
        val provider = QwenImageProvider(
            directApi = RecordingApi(error = CancellationException("cancelled")),
            proxyApi = RecordingApi(),
        )

        assertFailsWith<CancellationException> {
            runBlocking {
                provider.generate(config(apiToken = "token"), ImageGenerationRequest("cat"))
            }
        }
    }

    private fun config(
        apiToken: String = "",
        baseUrl: String = "https://dashscope.example",
        proxyUrl: String = "https://proxy.example",
        proxyPath: String = "/generate",
    ) = ImageProviderConfig(
        id = "test",
        providerId = QwenImageProvider.PROVIDER_ID,
        displayName = "test",
        baseUrl = baseUrl,
        apiToken = apiToken,
        proxyUrl = proxyUrl,
        proxyPath = proxyPath,
        model = QwenImageProvider.MODEL_PRO,
    )

    private class RecordingApi(
        private val error: Exception? = null,
    ) : QwenImageApi {
        var url: String? = null
        var authorization: String? = null
        var request: QwenRequest? = null

        override suspend fun generateOrEdit(
            url: String,
            authorization: String?,
            request: QwenRequest,
        ): Response<QwenResponse> {
            error?.let { throw it }
            this.url = url
            this.authorization = authorization
            this.request = request
            return Response.success(
                QwenResponse(
                    output = QwenOutput(
                        choices = listOf(
                            QwenChoice(
                                message = QwenMessage(content = listOf(QwenContent(image = "https://example/image.png")))
                            )
                        )
                    ),
                    requestId = "request-id",
                )
            )
        }
    }
}
