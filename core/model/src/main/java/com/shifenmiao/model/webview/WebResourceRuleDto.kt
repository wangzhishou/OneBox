package com.shifenmiao.model.webview

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * 远程下发的 WebView 资源拦截规则（DTO）。
 *
 * 由 [com.shifenmiao.model.remote.RemoteConfig.webViewResourceRules] 承载，
 * 消费方是 [com.shifenmiao.webview.resource.WebResourceEngine]。
 *
 * ---
 *
 * ## 字段说明
 *
 * | 字段 | 类型 | 必填 | 枚举值 | 说明 |
 * |------|------|------|--------|------|
 * | `matchKind` | String | ✅ | `host` / `exactUrl` / `urlPrefix` | 匹配策略，见下方「matchKind 取值」 |
 * | `matchValue` | String | ✅ | 任意字符串 | 匹配目标。host 模式填域名，exactUrl 模式填完整 URL，urlPrefix 模式填 URL 前缀 |
 * | `ruleKind` | String | ✅ | `asset` / `remoteUrl` | 规则行为，见下方「ruleKind 取值」 |
 * | `assetPath` | String? | `asset` 时必填 | 相对 `assets/` 的路径 | 例：`js/tailwindcss.js`（不带前导 `/`） |
 * | `realUrl` | String? | `remoteUrl` 时必填 | 完整 URL | WebView 实际请求的"虚拟 URL"，命中后从这里拉真实内容 |
 * | `cacheTtlSeconds` | Long? | ❌ | 任意正整数 | 磁盘缓存 TTL（秒）。不填则尊重服务端返回的 `Cache-Control` |
 *
 * ---
 *
 * ## matchKind 取值
 *
 * - **`host`** — 按请求的 `host` 字段匹配（忽略大小写），path 任意。
 *   适合整域拦截，例如 `cdn.tailwindcss.com` 命中该域下所有路径。
 * - **`exactUrl`** — 完整 URL 等值匹配（包含 query string）。
 *   适合精确替换某个具体资源，例如某个第三方脚本被官方下架后切到自托管。
 * - **`urlPrefix`** — 完整 URL 前缀匹配（包含 query string）。
 *   适合批量拦截同一 CDN 目录下的所有文件，例如 `https://cdn.example.com/lib/`。
 *
 * ## ruleKind 取值
 *
 * - **`asset`** — 命中后用 APK `assets/` 目录下的本地资源响应。
 *   必须在 `assetPath` 中指定路径。**不发网络请求**。
 * - **`remoteUrl`** — 命中后从 `realUrl` 拉取真实资源。
 *   支持 OkHttp 磁盘缓存 + ETag/Last-Modified 自动 304。
 *   可选 `cacheTtlSeconds` 覆盖服务端 Cache-Control。
 *
 * ---
 *
 * ## 完整 JSON 示例
 *
 * ```json
 * {
 *   "webViewResourceRules": [
 *     {
 *       "matchKind": "host",
 *       "matchValue": "cdn.tailwindcss.com",
 *       "ruleKind": "asset",
 *       "assetPath": "js/tailwindcss.js"
 *     },
 *     {
 *       "matchKind": "exactUrl",
 *       "matchValue": "https://example.com/old/lib.js",
 *       "ruleKind": "remoteUrl",
 *       "realUrl": "https://cdn.example.com/new/lib-v2.js",
 *       "cacheTtlSeconds": 604800
 *     },
 *     {
 *       "matchKind": "urlPrefix",
 *       "matchValue": "https://fonts.googleapis.com/",
 *       "ruleKind": "remoteUrl",
 *       "realUrl": "https://fonts.loli.net/",
 *       "cacheTtlSeconds": 2592000
 *     }
 *   ]
 * }
 * ```
 *
 * ---
 *
 * ## 行为说明
 *
 * - **优先级**：单条规则内**无所谓**（按列表顺序求值，第一个匹配胜出）。
 * - **相对代码内置规则**：内置规则（`BuiltinResources`）永远优先于本字段。
 *   下发同名规则**不会**覆盖内置规则——本字段只是**额外补充**。
 * - **坏数据容错**：单条规则缺字段或枚举值非法时，引擎静默丢弃该条，**不影响**其它规则。
 *   服务端上线新字段时无需发版客户端。
 */
@Parcelize
@Serializable
data class WebResourceRuleDto(
    /**
     * 匹配策略。取值见 [MATCH_HOST] / [MATCH_EXACT_URL] / [MATCH_URL_PREFIX]。
     */
    @SerializedName("matchKind")
    val matchKind: String,

    /**
     * 匹配目标字符串。
     * - `host` 模式：域名（如 `cdn.tailwindcss.com`），忽略大小写
     * - `exactUrl` 模式：完整 URL，包含 query string
     * - `urlPrefix` 模式：URL 前缀，包含 query string
     */
    @SerializedName("matchValue")
    val matchValue: String,

    /**
     * 规则行为。取值见 [RULE_ASSET] / [RULE_REMOTE_URL]。
     */
    @SerializedName("ruleKind")
    val ruleKind: String,

    /**
     * 本地资源路径（`ruleKind=asset` 时必填）。
     * 相对 `assets/` 目录，**不**带前导 `/`。例：`js/tailwindcss.js`。
     */
    @SerializedName("assetPath")
    val assetPath: String? = null,

    /**
     * 真实资源 URL（`ruleKind=remoteUrl` 时必填）。
     * WebView 请求命中本规则后，实际从该 URL 拉取内容（带磁盘缓存）。
     */
    @SerializedName("realUrl")
    val realUrl: String? = null,

    /**
     * 磁盘缓存 TTL（秒，`ruleKind=remoteUrl` 时可选）。
     * 设置后会在响应中追加 `Cache-Control: max-age=...`，OkHttp 按此判定过期。
     * 不设置则尊重服务端 `Cache-Control` 头。
     */
    @SerializedName("cacheTtlSeconds")
    val cacheTtlSeconds: Long? = null,
) : Parcelable {

    companion object {
        /** 按 host 匹配，path 任意。例：`cdn.tailwindcss.com` */
        const val MATCH_HOST = "host"

        /** 完整 URL 等值匹配，包含 query。例：`https://x.com/a.js?v=1` */
        const val MATCH_EXACT_URL = "exactUrl"

        /** 完整 URL 前缀匹配，包含 query。例：`https://cdn.x.com/lib/` */
        const val MATCH_URL_PREFIX = "urlPrefix"

        /** 命中后用 APK assets 目录下的本地资源响应。需要 [assetPath]。 */
        const val RULE_ASSET = "asset"

        /** 命中后从 [realUrl] 拉取真实资源（带缓存）。需要 [realUrl]。 */
        const val RULE_REMOTE_URL = "remoteUrl"
    }
}
