/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
        "/api/register",
        "/api/login",
        "/api/forgot-password",
        "/api/reset-password",
        "/api/wechat-login",
        "/wechat-login",
        "/api/google-login"
    )

    /**
     * 由 go-proxy 处理、要求 App 级 API token 的免登录接口：
     * 只附带 RemoteConfig.accessToken，不附带用户 JWT
     * （APIAuthMiddleware 只认 API token，用户 JWT 会判 "Invalid or expired token"）。
     */
    private val apiTokenOnlyPaths = setOf(
        "/api/auth/send-code",
        "/api/auth/phone"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        if (originalRequest.header("Authorization").isNullOrBlank().not()) {
            return chain.proceed(originalRequest)
        }
        if (originalRequest.url.encodedPath in anonymousPaths) {
            return chain.proceed(originalRequest)
        }
        if (originalRequest.url.encodedPath in apiTokenOnlyPaths) {
            val apiToken = RemoteConfigStorage.getRemoteConfig().accessToken
                ?.takeIf { it.isNotBlank() }
            val request = apiToken?.let { token ->
                originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } ?: originalRequest
            return chain.proceed(request)
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