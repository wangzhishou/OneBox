package com.shifenmiao.network.api

import com.shifenmiao.model.ai.XiangqiEngineRequest
import com.shifenmiao.model.ai.XiangqiEngineResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface XiangqiEngineService {

    @POST
    fun bestMove(
        @Url url: String,
        @Body body: XiangqiEngineRequest,
    ): Call<XiangqiEngineResponse>
}
