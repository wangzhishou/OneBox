package com.shifenmiao.network.interceptor

import com.shifenmiao.model.network.HttpStatusCode
import com.t8rin.imagetoolbox.core.utils.makeLog
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class ErrorHandlingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return try {
            chain.proceed(request)
        } catch (exception: Exception) {
            "请求失败: ${request.method} ${request.url}".makeLog(TAG)
            exception.makeLog(TAG)
            val body = buildString {
                append(exception.javaClass.name)
                append(": ")
                append(exception.message ?: "(no message)")
            }
            Response.Builder()
                .request(request)
                .code(HttpStatusCode.BAD_REQUEST.code)
                .message(HttpStatusCode.BAD_REQUEST.message)
                .protocol(Protocol.HTTP_1_1)
                .body(body.toResponseBody("text/plain".toMediaTypeOrNull()))
                .build()
        }
    }

    private companion object {
        const val TAG = "Network"
    }
}