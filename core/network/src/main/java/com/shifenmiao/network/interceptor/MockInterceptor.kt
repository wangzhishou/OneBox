package com.shifenmiao.network.interceptor


import okhttp3.Interceptor
import okhttp3.Interceptor.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import javax.inject.Inject


//class MockInterceptor @Inject constructor(
//    private val onlineRepository: OnlineRepository
//) : Interceptor {
//    @Throws(IOException::class)
//    override fun intercept(chain: Chain): Response {
//        val responseString = onlineRepository.getMockJson("mock/items.json")
//        return Response.Builder()
//            .code(200)
//            .message(responseString)
//            .request(chain.request())
//            .protocol(Protocol.HTTP_1_0)
//            .body(responseString.toByteArray().toResponseBody("application/json".toMediaType()))
//            .addHeader("content-type", "application/json")
//            .build()
//    }
//}