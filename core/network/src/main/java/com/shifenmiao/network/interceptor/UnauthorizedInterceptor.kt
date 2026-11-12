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

import com.shifenmiao.model.event.AppEventBus
import com.shifenmiao.model.login.LoginEvent
import com.shifenmiao.model.network.HttpStatusCode
import com.shifenmiao.storage.TokenStorage
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * 登录态失效兜底:收到 401 时清掉本地 token 并弹全局登录页。
 *
 * 两个容易踩的点:
 * 1. 只处理 [HttpStatusCode.UNAUTHORIZED](401)。历史上这里写成了 CONTINUE(100),
 *    条件永远不成立 —— token 过期后请求一路 401,却只被上层渲染成通用错误;
 * 2. 只有"真的带着用户 JWT 发出去"的请求才算登录态失效。游客 token(RemoteConfig
 *    accessToken)或 App 级 API token 被拒时不能弹登录页,否则未登录用户会被反复打扰。
 */
class UnauthorizedInterceptor : Interceptor {

    /** 并发请求可能同时 401,这里做时间窗去重,避免连续弹多次登录页 */
    @Volatile
    private var lastNotifyAt = 0L

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.code == HttpStatusCode.UNAUTHORIZED.code && carriedUserToken(request)) {
            notifyLoginExpired()
        }
        return response
    }

    /** 请求头里的 Bearer 是否就是本地保存的用户 JWT(AuthInterceptor 已先挂上鉴权头) */
    private fun carriedUserToken(request: Request): Boolean {
        val bearer = request.header("Authorization")
            ?.removePrefix("Bearer")
            ?.trim()
            .orEmpty()
        if (bearer.isBlank()) return false
        val userToken = TokenStorage.getTokenFromLocalStorage()?.trim().orEmpty()
        return userToken.isNotBlank() && bearer == userToken
    }

    private fun notifyLoginExpired() {
        val now = System.currentTimeMillis()
        if (now - lastNotifyAt < NOTIFY_INTERVAL_MS) return
        synchronized(this) {
            if (now - lastNotifyAt < NOTIFY_INTERVAL_MS) return
            lastNotifyAt = now
        }
        TokenStorage.clearLoginInfo()
        AppEventBus.emit(LoginEvent(SOURCE, {}, {}))
    }

    private companion object {
        const val SOURCE = "UnauthorizedInterceptor"
        const val NOTIFY_INTERVAL_MS = 10_000L
    }
}
