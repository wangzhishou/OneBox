package com.shifenmiao.network.api

import com.shifenmiao.model.ai.image.QwenImageRequest
import com.shifenmiao.model.ai.image.QwenImageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface QwenImageService {
    @POST
    suspend fun generateOrEdit(
        @Url url: String,
        @Header("Authorization") authorization: String? = null,
        @Body request: QwenImageRequest,
    ): Response<QwenImageResponse>
}
