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