package com.shifenmiao.network.interceptor

import com.shifenmiao.storage.RemoteConfigStorage
import com.shifenmiao.storage.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    /**
     * 免登录的鉴权类接口：即使本地存有 token 也不附带。
     * 否则过期/跨环境的旧 token 会被 Strapi 的 JWT 策略拦截，
     * 直接返回 401 "Missing or invalid credentials"，根本走不到账号密码校验。
     */
    private val anonymousPaths = setOf(
        "/api/auth/local",
        "/api/auth/send-code",
        "/api/auth/phone",
        "/api/register",
        "/api/login",
        "/api/forgot-password",
        "/api/reset-password",
        "/api/wechat-login",
        "/wechat-login",
        "/api/google-login"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        if (originalRequest.header("Authorization").isNullOrBlank().not()) {
            return chain.proceed(originalRequest)
        }
        if (originalRequest.url.encodedPath in anonymousPaths) {
            return chain.proceed(originalRequest)
        }

        val bearerToken = TokenStorage.getTokenFromLocalStorage()
            ?.takeIf { it.isNotBlank() }
            ?: RemoteConfigStorage.getRemoteConfig().accessToken?.takeIf { it.isNotBlank() }

        val request = bearerToken?.let { token ->
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } ?: originalRequest

        return chain.proceed(request)
    }
}