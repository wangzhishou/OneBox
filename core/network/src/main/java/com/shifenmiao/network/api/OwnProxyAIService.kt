package com.shifenmiao.network.api

import com.shifenmiao.model.ai.AnthropicMessagesRequest
import com.shifenmiao.model.ai.ChatCompletionRequest
import com.shifenmiao.model.ai.openai.responses.ResponsesApiRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url

interface OwnProxyAIService {

    @Streaming
    @POST
    fun chatWithStreaming(
        @Url url: String,
        @Body chatCompletionRequest: ChatCompletionRequest
    ): Call<ResponseBody>

    @POST
    fun chatNoStreaming(
        @Url url: String,
        @Body chatCompletionRequest: ChatCompletionRequest
    ): Call<ResponseBody>

    @POST
    fun responsesNoStreaming(
        @Url url: String,
        @Body request: ResponsesApiRequest
    ): Call<ResponseBody>

    @POST
    fun messagesNoStreaming(
        @Url url: String,
        @Body request: AnthropicMessagesRequest
    ): Call<ResponseBody>
}
