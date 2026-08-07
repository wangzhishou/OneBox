package com.shifenmiao.webview.browser

import kotlinx.serialization.Serializable
import java.net.URL

@Serializable
data class BrowserTab(
    val id: String,
    val url: String = "",
    val title: String = "",
    val favicon: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isActive: Boolean = false
)

@Serializable
data class BookmarkFolder(
    val id: String,
    val name: String,
    val order: Int = 0
)

@Serializable
data class BookmarkItem(
    val id: String,
    val url: String,
    val title: String,
    val folderId: String = "",
    val favicon: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class HistoryItem(
    val id: String,
    val url: String,
    val title: String,
    val favicon: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class SearchEngine(
    val name: String,
    val searchUrl: String,
    val homeUrl: String
) {
    companion object {
        val GOOGLE = SearchEngine("Google", "https://www.google.com/search?q=", "https://www.google.com")
        val BAIDU = SearchEngine("Baidu", "https://www.baidu.com/s?wd=", "https://www.baidu.com")
        val BING = SearchEngine("Bing", "https://www.bing.com/search?q=", "https://www.bing.com")
        val DUCKDUCKGO = SearchEngine("DuckDuckGo", "https://duckduckgo.com/?q=", "https://duckduckgo.com")

        val all = listOf(GOOGLE, BAIDU, BING, DUCKDUCKGO)

        fun byName(name: String): SearchEngine = all.find { it.name == name } ?: GOOGLE
    }
}

@Serializable
data class UserAgentPreset(
    val id: String,
    val displayName: String,
    val userAgent: String
) {
    companion object {
        val DEFAULT = UserAgentPreset("default", "默认", "")

        val all = listOf(
            DEFAULT,
            UserAgentPreset(
                "iphone_safari",
                "iPhone Safari",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1"
            ),
            UserAgentPreset(
                "ipad_safari",
                "iPad Safari",
                "Mozilla/5.0 (iPad; CPU OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1"
            ),
            UserAgentPreset(
                "android_chrome",
                "Android Chrome",
                "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.6422.113 Mobile Safari/537.36"
            ),
            UserAgentPreset(
                "pc_chrome",
                "PC Chrome",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36"
            ),
            UserAgentPreset(
                "pc_firefox",
                "PC Firefox",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:126.0) Gecko/20100101 Firefox/126.0"
            ),
            UserAgentPreset(
                "pc_safari",
                "PC Safari",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_5) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Safari/605.1.15"
            ),
            UserAgentPreset(
                "pc_edge",
                "PC Edge",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36 Edg/125.0.2535.92"
            ),
            UserAgentPreset(
                "wechat_android",
                "微信 Android",
                "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro Build/UQ1A.240205.002) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/125.0.6422.113 Mobile Safari/537.36 MicroMessenger/8.0.49.2602(0x28003139) NetType/WIFI Language/zh_CN"
            ),
            UserAgentPreset(
                "wechat_ios",
                "微信 iOS",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 MicroMessenger/8.0.49(0x18003130) NetType/WIFI Language/zh_CN"
            ),
        )

        fun byId(id: String): UserAgentPreset = all.find { it.id == id } ?: DEFAULT
    }
}

@Serializable
data class BrowserSettings(
    val searchEngineName: String = SearchEngine.GOOGLE.name,
    val homeUrl: String = "",
    val enablePrivacyMode: Boolean = false,
    val blockAds: Boolean = false,
    val clearCacheOnExit: Boolean = false,
    val enableJavaScript: Boolean = true,
    val textSize: Int = 100,
    val userAgentPresetId: String = UserAgentPreset.DEFAULT.id,
    val customUserAgent: String = ""
) {
    val currentSearchEngine: SearchEngine get() = SearchEngine.byName(searchEngineName)

    val effectiveUserAgent: String?
        get() {
            val preset = UserAgentPreset.byId(userAgentPresetId)
            return when {
                preset.id == "default" -> null
                preset.id == "custom" -> customUserAgent.takeIf { it.isNotBlank() }
                else -> preset.userAgent
            }
        }

    companion object {
        const val CUSTOM_PRESET_ID = "custom"
    }
}

fun String.toFaviconUrl(): String {
    return try {
        val url = URL(this)
        "${url.protocol}://${url.host}/favicon.ico"
    } catch (_: Exception) {
        ""
    }
}

fun normalizeUrl(input: String, searchEngine: SearchEngine = SearchEngine.GOOGLE): String {
    val trimmed = input.trim()
    return when {
        trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
        trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
        else -> searchEngine.searchUrl + java.net.URLEncoder.encode(trimmed, "UTF-8")
    }
}
