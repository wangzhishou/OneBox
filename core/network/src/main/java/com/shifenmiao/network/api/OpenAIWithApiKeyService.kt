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

package com.shifenmiao.network.api

import com.shifenmiao.model.ai.ChatCompletionRequest
import com.shifenmiao.model.ai.openai.OpenAIModelsResponse
import com.shifenmiao.model.ai.openai.responses.ResponsesApiRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * 使用 api-key header 认证的 OpenAI 兼容协议 Service 接口
 *
 * 用于支持 OpenAI 兼容格式但使用 api-key header 认证的大模型（如小米 MiMo、Azure 等）。
 * 与标准 OpenAI 兼容协议的区别：
 * - 认证方式: 使用 api-key header 而非 Authorization: Bearer
 * - 请求格式: 与 OpenAI Chat Completion 格式兼容
 */
interface OpenAIWithApiKeyService {

    /**
     * 流式请求（SSE）
     */
    @Streaming
    @POST
    fun chatWithStreaming(
        @Url url: String,
        @Header("api-key") apiKey: String,
        @Body chatCompletionRequest: ChatCompletionRequest
    ): Call<ResponseBody>

    /**
     * 非流式请求
     */
    @POST
    fun chatNoStreaming(
        @Url url: String,
        @Header("api-key") apiKey: String,
        @Body chatCompletionRequest: ChatCompletionRequest
    ): Call<ResponseBody>

    @Streaming
    @POST
    fun responsesWithStreaming(
        @Url url: String,
        @Header("api-key") apiKey: String,
        @Body responsesApiRequest: ResponsesApiRequest
    ): Call<ResponseBody>

    @POST
    fun responsesNoStreaming(
        @Url url: String,
        @Header("api-key") apiKey: String,
        @Body responsesApiRequest: ResponsesApiRequest
    ): Call<ResponseBody>

    @GET
    suspend fun listModels(
        @Url url: String,
        @Header("api-key") apiKey: String,
    ): Response<OpenAIModelsResponse>
}
