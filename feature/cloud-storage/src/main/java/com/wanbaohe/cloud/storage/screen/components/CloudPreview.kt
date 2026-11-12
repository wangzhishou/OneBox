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

package com.wanbaohe.cloud.storage.screen.components

import android.content.Context
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest

/**
 * 远端图片的 Coil model。
 *
 * S3 的签名 URL 自带鉴权，直接把字符串交给 Coil 即可；WebDAV 的地址要靠 Basic 头，
 * 必须把认证信息挂在 ImageRequest 上，否则 Coil 的请求会 401，网格里永远只有占位图标。
 */
internal fun cloudPreviewModel(
    context: Context,
    url: String?,
    headers: Map<String, String>,
): Any? {
    if (url.isNullOrBlank()) return null
    if (headers.isEmpty()) return url
    return ImageRequest.Builder(context)
        .data(url)
        .httpHeaders(
            NetworkHeaders.Builder().apply {
                headers.forEach { (name, value) -> set(name, value) }
            }.build()
        )
        .build()
}
