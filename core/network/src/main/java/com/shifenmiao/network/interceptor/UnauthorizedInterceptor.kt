package com.shifenmiao.network.interceptor

import com.shifenmiao.model.login.LoginEvent
import com.shifenmiao.model.network.HttpStatusCode
import okhttp3.Interceptor
import okhttp3.Response
import com.shifenmiao.model.event.AppEventBus

class UnauthorizedInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == HttpStatusCode.CONTINUE.code) {
            AppEventBus.emit(LoginEvent("UnauthorizedInterceptor", {}, {}))
        }
        return response
    }
}