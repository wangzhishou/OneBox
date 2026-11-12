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

package com.shifenmiao.webview.resource.interceptor

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import androidx.webkit.WebViewAssetLoader
import com.shifenmiao.webview.resource.MimeTypeUtils
import com.shifenmiao.webview.resource.WebResourceEngine
import com.t8rin.logger.makeLog
import java.io.File
import java.io.FileInputStream
import java.net.URLDecoder

/**
 * 本地文件拦截器：仅处理 `https://appassets.androidplatform.net/local-file/<encoded-path>`。
 *
 * HTML 中引用本地图片的格式：
 * ```
 * <img src="https://appassets.androidplatform.net/local-file/<URL编码后的文件路径>">
 * ```
 *
 * **安全约束**：仅允许解析到应用私有目录（filesDir / cacheDir / externalFilesDir / externalCacheDir），
 * 防止 `../../../etc/passwd` 之类的目录遍历攻击。
 */
class LocalFileInterceptor : WebResourceInterceptor {

    override fun intercept(
        request: WebResourceRequest,
        engine: WebResourceEngine,
    ): WebResourceResponse? {
        val host = request.url.host ?: return null
        if (host != WebViewAssetLoader.DEFAULT_DOMAIN) return null
        val path = request.url.path ?: return null
        if (!path.startsWith(LOCAL_FILE_PREFIX)) return null

        val encoded = path.removePrefix(LOCAL_FILE_PREFIX)
        return runCatching {
            val decoded = URLDecoder.decode(encoded, "UTF-8")
            val file = File(decoded)
            if (!isAllowedFilePath(engine, file)) {
                makeLog { "LocalFileInterceptor: 拒绝访问路径 $decoded" }
                return null
            }
            if (!file.exists() || !file.canRead()) {
                makeLog { "LocalFileInterceptor: 文件不存在或不可读: $decoded" }
                return null
            }
            val mime = MimeTypeUtils.guessFromExtension(file.name)
            WebResourceResponse(mime, MimeTypeUtils.DEFAULT_CHARSET, FileInputStream(file))
        }.getOrElse { e ->
            makeLog { "LocalFileInterceptor: 处理失败 ${e.message}" }
            null
        }
    }

    private fun isAllowedFilePath(engine: WebResourceEngine, file: File): Boolean = runCatching {
        val canonical = file.canonicalPath
        val ctx = engine.context
        val allowed = listOfNotNull(
            ctx.filesDir?.canonicalPath,
            ctx.cacheDir?.canonicalPath,
            ctx.getExternalFilesDir(null)?.canonicalPath,
            ctx.externalCacheDir?.canonicalPath,
        )
        allowed.any { canonical.startsWith(it) }
    }.getOrDefault(false)

    companion object {
        private const val LOCAL_FILE_PREFIX = "/local-file/"
    }
}
