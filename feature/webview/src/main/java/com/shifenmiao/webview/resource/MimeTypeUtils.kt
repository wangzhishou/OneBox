package com.shifenmiao.webview.resource

import java.util.Locale

/**
 * 统一的 MIME Type 推断工具。
 *
 * 消除项目内三处重复实现（[com.shifenmiao.webview.client.CustomWebViewClient]、
 * [com.shifenmiao.webview.resource.cache.WebResourceCache]、
 * [com.wanbaohe.file_transfer.server.FileTransferServer]）。
 *
 * 推断优先级：
 * 1. 调用方显式提供的 Content-Type（如 OkHttp `response.header("Content-Type")`）
 * 2. URL 后缀映射表（覆盖常见 web 资源：html/css/js/图片/字体/数据/音视频）
 * 3. 兜底 [FALLBACK_BINARY]
 */
object MimeTypeUtils {

    const val FALLBACK_BINARY = "application/octet-stream"
    const val DEFAULT_CHARSET = "UTF-8"

    private val EXTENSION_MAP: Map<String, String> = buildMap {
        // 文本
        put("html", "text/html; charset=utf-8")
        put("htm", "text/html; charset=utf-8")
        put("css", "text/css; charset=utf-8")
        put("js", "application/javascript; charset=utf-8")
        put("mjs", "application/javascript; charset=utf-8")
        put("json", "application/json; charset=utf-8")
        put("xml", "application/xml; charset=utf-8")
        put("txt", "text/plain; charset=utf-8")
        put("md", "text/markdown; charset=utf-8")
        put("csv", "text/csv; charset=utf-8")
        put("svg", "image/svg+xml")

        // 图片
        put("png", "image/png")
        put("jpg", "image/jpeg")
        put("jpeg", "image/jpeg")
        put("gif", "image/gif")
        put("webp", "image/webp")
        put("bmp", "image/bmp")
        put("ico", "image/x-icon")
        put("heic", "image/heic")
        put("heif", "image/heif")
        put("avif", "image/avif")

        // 字体
        put("ttf", "font/ttf")
        put("otf", "font/otf")
        put("woff", "font/woff")
        put("woff2", "font/woff2")
        put("eot", "application/vnd.ms-fontobject")

        // 数据
        put("wasm", "application/wasm")
        put("map", "application/json; charset=utf-8")

        // 音视频
        put("mp3", "audio/mpeg")
        put("wav", "audio/wav")
        put("ogg", "audio/ogg")
        put("mp4", "video/mp4")
        put("webm", "video/webm")

        // 其它常见
        put("pdf", "application/pdf")
        put("zip", "application/zip")
    }

    /**
     * 从 URL / 文件路径推断 MIME Type。
     *
     * @param source 用于提取后缀的字符串（完整 URL 或 path 都可）。
     * @return 推断结果；未知后缀返回 [FALLBACK_BINARY]。
     */
    fun guessFromExtension(source: String?): String {
        if (source.isNullOrBlank()) return FALLBACK_BINARY
        val ext = source.substringAfterLast('.', "").lowercase(Locale.US)
        if (ext.isEmpty() || ext.contains('/') || ext.contains('?') || ext.contains('#')) {
            return FALLBACK_BINARY
        }
        return EXTENSION_MAP[ext] ?: FALLBACK_BINARY
    }

    /**
     * 规范化 Content-Type 头。
     *
     * - 空 / "text/plain" 会被当作未设置，走扩展名推断。
     * - 已带 charset 的不再追加。
     * - 已知文本类缺 charset 时补 UTF-8。
     */
    fun normalizeContentType(rawHeader: String?, fallbackSource: String?): String {
        val header = rawHeader?.trim()?.takeIf { it.isNotEmpty() }
        if (header == null || header.equals("text/plain", ignoreCase = true)) {
            return guessFromExtension(fallbackSource)
        }
        // 已经包含 charset
        if (header.contains("charset=", ignoreCase = true)) return header
        // 文本类默认补 utf-8
        val isTextFamily = header.startsWith("text/", ignoreCase = true) ||
                header.startsWith("application/json", ignoreCase = true) ||
                header.startsWith("application/javascript", ignoreCase = true) ||
                header.startsWith("application/xml", ignoreCase = true) ||
                header.startsWith("application/wasm", ignoreCase = true)
        return if (isTextFamily) "$header; charset=$DEFAULT_CHARSET" else header
    }
}
