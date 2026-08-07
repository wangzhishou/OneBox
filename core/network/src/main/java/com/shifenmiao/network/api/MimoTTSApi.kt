package com.shifenmiao.network.api

import com.shifenmiao.model.tts.mimo.MimoTTSRequest
import com.shifenmiao.model.tts.mimo.MimoTTSResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface MimoTTSApi {

    @POST
    suspend fun synthesize(
        @Url url: String,
        @Header("api-key") apiKey: String,
        @Body request: MimoTTSRequest,
    ): Response<MimoTTSResponse>

    @POST
    suspend fun synthesizeViaProxy(
        @Url url: String,
        @Body request: MimoTTSRequest,
    ): Response<MimoTTSResponse>
}
