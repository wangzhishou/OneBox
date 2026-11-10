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
