package com.shifenmiao.network.api

import com.shifenmiao.model.ai.ChatCompletionRequest
import com.shifenmiao.model.ai.openai.responses.ResponsesApiRequest
import com.shifenmiao.model.ai.openai.OpenAIModelsResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url

interface OpenAICompatibleService {

    @Streaming
    @POST
    fun chatWithStreaming(
        @Url url: String,
        @Header("Authorization") authorization: String? = null,
        @Body chatCompletionRequest: ChatCompletionRequest
    ): Call<ResponseBody>

    @POST
    fun chatNoStreaming(
        @Url url: String,
        @Header("Authorization") authorization: String? = null,
        @Body chatCompletionRequest: ChatCompletionRequest
    ): Call<ResponseBody>

    @Streaming
    @POST
    fun responsesWithStreaming(
        @Url url: String,
        @Header("Authorization") authorization: String? = null,
        @Body responsesApiRequest: ResponsesApiRequest
    ): Call<ResponseBody>

    @POST
    fun responsesNoStreaming(
        @Url url: String,
        @Header("Authorization") authorization: String? = null,
        @Body responsesApiRequest: ResponsesApiRequest
    ): Call<ResponseBody>

    @GET
    suspend fun listModels(
        @Url url: String,
        @Header("Authorization") authorization: String? = null,
    ): Response<OpenAIModelsResponse>
}

