package com.shifenmiao.network.api

import com.shifenmiao.model.ai.JevRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface JevService {

    @POST
    fun systemOne(
        @Url url: String,
        @Header("Authorization") authorization: String? = null,
        @Body body: JevRequest,
    ): Call<ResponseBody>
}
