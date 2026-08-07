package com.shifenmiao.network.api

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * OpenAI 兼容协议的 TTS 语音合成 API。
 *
 * POST {baseUrl}/audio/speech
 * Authorization: Bearer {token}
 */
interface TTSSpeechApi {

    @POST
    suspend fun synthesizeSpeech(
        @Url url: String,
        @Header("Authorization") authorization: String,
        @Body request: com.shifenmiao.model.tts.TTSSpeechRequest,
    ): ResponseBody
}
