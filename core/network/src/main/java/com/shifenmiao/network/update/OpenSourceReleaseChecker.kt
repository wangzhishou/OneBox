package com.shifenmiao.network.update

import com.shifenmiao.model.channel.FlavorType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import javax.xml.parsers.DocumentBuilderFactory

/** 开源仓库(wangzhishou/OneBox)最新 release 信息。 */
data class OpenSourceRelease(
    val tag: String,
    val url: String,
    val isNewer: Boolean,
)

/**
 * 检查开源项目是否有新版本,参考上游 ImageToolbox 的 tryGetUpdate 实现。
 *
 * 按渠道分两个源(见 [fetchLatestOrNull]):
 * - 海外(google / foss):GitHub,优先 releases.atom(无 API 限流),失败回退 api.github.com;
 * - 国内(xiaomi / yyb / oppo / vivo / huawei / onebox):GitCode OpenAPI,
 *   国内直连 GitHub 不稳定,而 GitCode 免 token 且国内可达。
 *
 * 设计约束:
 * - 任一源失败(GitHub 被墙 / GitCode 仓库还没发过 release / 网络抖动)都静默失败,
 *   返回 null 不影响任何其它功能;
 * - 结果内存缓存 [CHECK_INTERVAL_MS],避免频繁请求;
 * - 全部工作在 IO 线程,调用方不阻塞。
 */
@Singleton
class OpenSourceReleaseChecker @Inject constructor() {

    // 自建最小 client,不挂 core/network 的鉴权拦截器(打的是 GitHub 公开接口)
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .callTimeout(10, TimeUnit.SECONDS)
        .build()

    private val mutex = Mutex()
    private var lastCheckAt = 0L

    private val _latestRelease = MutableStateFlow<OpenSourceRelease?>(null)
    val latestRelease: StateFlow<OpenSourceRelease?> = _latestRelease.asStateFlow()

    /** 后台检查,永不抛异常;非 [force] 时距上次检查不足间隔直接跳过。 */
    suspend fun checkLatest(currentVersion: String, force: Boolean = false) {
        if (!force && System.currentTimeMillis() - lastCheckAt < CHECK_INTERVAL_MS) return
        mutex.withLock {
            if (!force && System.currentTimeMillis() - lastCheckAt < CHECK_INTERVAL_MS) return
            lastCheckAt = System.currentTimeMillis()
            withContext(Dispatchers.IO) {
                val release = fetchLatestOrNull()
                if (release != null) {
                    _latestRelease.value = release.copy(
                        isNewer = isNewerVersion(current = currentVersion, latest = release.tag)
                    )
                }
            }
        }
    }

    /** 按渠道选源;两个源都失败时返回 null(静默)。 */
    private fun fetchLatestOrNull(): OpenSourceRelease? =
        if (FlavorType.fromName().isOverseas) {
            runCatching { fetchFromAtom() }.getOrNull()
                ?: runCatching { fetchFromGitHubApi() }.getOrNull()
        } else {
            runCatching { fetchFromGitCodeApi() }.getOrNull()
        }

    /**
     * GitCode OpenAPI(免 token,国内直连)。2026-09 实测的三个坑:
     * - GitCode 没有 releases.atom,网页 HTML 还会被 CloudWAF 拦非浏览器请求,只能走 API;
     * - 仓库一次 release 都没发时返回 HTTP 400「未找到 release」,仓库设成私有时返回 403,
     *   两种都按"没查到"静默处理(所以 GitCode 侧必须先把仓库设为公开、并发过 release);
     * - 下载链接要走稳定的 /releases 入口,不能固化 302 到 file-cdn 的带签名地址。
     */
    private fun fetchFromGitCodeApi(): OpenSourceRelease {
        val json = JSONObject(get(GITCODE_RELEASE_LATEST_API))
        if (json.optString("release_status") == "pre") error("GitCode: latest is a prerelease")
        val tag = json.optString("tag_name")
        if (tag.isBlank()) error("GitCode: blank tag_name")
        return OpenSourceRelease(tag = tag, url = GITCODE_RELEASES_URL, isNewer = false)
    }

    /** releases.atom 无鉴权无限流;entry 按时间倒序,取第一个非 prerelease。 */
    private fun fetchFromAtom(): OpenSourceRelease {
        val body = get(RELEASES_ATOM)
        val document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(ByteArrayInputStream(body.toByteArray()))
        val entries = document.getElementsByTagName("entry")
        for (i in 0 until entries.length) {
            val entry = entries.item(i) as? Element ?: continue
            val link = entry.getElementsByTagName("link").item(0)
                ?.attributes?.getNamedItem("href")?.nodeValue.orEmpty()
            val tag = link.substringAfter("/releases/tag/", "")
                .ifBlank { entry.getElementsByTagName("title").item(0)?.textContent.orEmpty() }
            if (tag.isBlank()) continue
            // tag 含 '-' 是 CI 约定的 prerelease,跳过
            if ('-' in tag) continue
            return OpenSourceRelease(tag = tag, url = link.ifBlank { REPO_URL }, isNewer = false)
        }
        error("No release entry found in atom feed")
    }

    private fun fetchFromGitHubApi(): OpenSourceRelease {
        val json = JSONObject(get(RELEASE_LATEST_API))
        return OpenSourceRelease(
            tag = json.getString("tag_name"),
            url = json.optString("html_url").ifBlank { REPO_URL },
            isNewer = false,
        )
    }

    private fun get(url: String): String {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "OneBox-Android")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("HTTP ${response.code} for $url")
            return response.body.string()
        }
    }

    companion object {
        private const val REPO_SLUG = "wangzhishou/OneBox"
        private const val REPO_URL = "https://github.com/$REPO_SLUG"
        private const val RELEASES_ATOM = "$REPO_URL/releases.atom"
        private const val RELEASE_LATEST_API =
            "https://api.github.com/repos/$REPO_SLUG/releases/latest"

        /** GitCode 开源镜像:国内渠道的更新源(免 token,国内直连)。 */
        private const val GITCODE_RELEASES_URL = "https://gitcode.com/$REPO_SLUG/releases"
        private const val GITCODE_RELEASE_LATEST_API =
            "https://api.gitcode.com/api/v5/repos/$REPO_SLUG/releases/latest"

        private val CHECK_INTERVAL_MS = TimeUnit.HOURS.toMillis(6)
    }
}

/** 纯数字段比较版本号("1.3.6" > "1.3.5"),忽略 v 前缀与 - 后缀。 */
internal fun isNewerVersion(current: String, latest: String): Boolean {
    fun parts(version: String): List<Int> = version
        .trim()
        .removePrefix("v")
        .split('.', '-', '+', ' ')
        .mapNotNull { segment -> segment.takeWhile(Char::isDigit).toIntOrNull() }

    val currentParts = parts(current)
    val latestParts = parts(latest)
    if (latestParts.isEmpty()) return false
    for (i in 0 until maxOf(currentParts.size, latestParts.size)) {
        val diff = latestParts.getOrElse(i) { 0 } - currentParts.getOrElse(i) { 0 }
        if (diff != 0) return diff > 0
    }
    return false
}
