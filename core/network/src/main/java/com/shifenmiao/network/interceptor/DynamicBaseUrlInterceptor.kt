package com.shifenmiao.network.interceptor

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class DynamicBaseUrlInterceptor(private val baseUrlProvider: () -> String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()
        val originalHttpUrl: HttpUrl = originalRequest.url

        // Get the new base URL from the provider
        val newBaseUrl = baseUrlProvider.invoke()
        val newHttpUrl = newBaseUrl.toHttpUrlOrNull()

        // If newHttpUrl is null, use the original URL
        val finalHttpUrl = if (newHttpUrl != null) {
            originalHttpUrl.newBuilder()
                .scheme(newHttpUrl.scheme)
                .host(newHttpUrl.host)
                .port(newHttpUrl.port)
                .build()
        } else {
            originalHttpUrl
        }

        // Create a new request with the final URL
        val newRequest: Request = originalRequest.newBuilder()
            .url(finalHttpUrl)
            .build()

        return chain.proceed(newRequest)
    }
}