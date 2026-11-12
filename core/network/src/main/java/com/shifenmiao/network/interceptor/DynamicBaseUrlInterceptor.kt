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