package com.shifenmiao.network.api

import com.shifenmiao.model.ai.AnthropicMessagesRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * Anthropic 兼容协议 Service 接口
 *
 * 用于支持 Anthropic Messages API 格式的大模型（如小米 MiMo）。
 * 主要特点：
 * 1. 请求路径: /v1/messages
 * 2. 认证方式: x-api-key header
 * 3. 请求格式: Anthropic Messages API 格式
 */
interface AnthropicCompatibleService {

    /**
     * 流式请求（SSE）
     */
    @Streaming
    @POST
    fun messagesWithStreaming(
        @Url url: String,
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Body request: AnthropicMessagesRequest
    ): Call<ResponseBody>

    /**
     * 非流式请求
     */
    @POST
    fun messagesNoStreaming(
        @Url url: String,
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Body request: AnthropicMessagesRequest
    ): Call<ResponseBody>
}
