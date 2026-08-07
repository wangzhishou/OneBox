package com.shifenmiao.webview.resource

import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import androidx.core.net.toUri
import androidx.webkit.WebViewAssetLoader
import com.shifenmiao.storage.RemoteConfigStorage
import com.shifenmiao.webview.resource.cache.WebResourceCache
import com.shifenmiao.webview.resource.interceptor.BuiltinAssetInterceptor
import com.shifenmiao.webview.resource.interceptor.LocalFileInterceptor
import com.shifenmiao.webview.resource.interceptor.RemoteRuleInterceptor
import com.shifenmiao.webview.resource.interceptor.WebResourceInterceptor
import com.shifenmiao.webview.resource.interceptor.WebViewAssetLoaderInterceptor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 统一的 WebView 资源拦截引擎。
 *
 * ## 职责
 * 1. 维护一份**有序的拦截器链**（`builtin → remote → localFile → assetLoader`），
 *    第一个返回非 null 的拦截器胜出。
 * 2. 同步 [RemoteConfigStorage] 中 [com.shifenmiao.model.remote.RemoteConfig.webViewResourceRules] 的变更，
 *    实时刷新内部 [_remoteRules]，无需重启 WebView。
 * 3. 对外暴露 [intercept] 给 [com.shifenmiao.webview.client.CustomWebViewClient] / Mermaid
 *    等 WebView 容器。
 *
 * ## 线程模型
 * - [refresh] / [intercept] 都不做协程切换；拦截跑在 WebView Binder 线程上。
 * - 远程规则变更的订阅跑在内部 [scope] 的 IO 调度器上，与拦截链解耦。
 *
 * ## 单例
 * 由 Hilt `@Singleton` 提供；不要直接 new。`WebViewComponent` 注入它并转发给
 * `CustomWebViewClient` 构造。
 */
@Singleton
class WebResourceEngine @Inject constructor(
    @ApplicationContext val context: Context,
    val cache: WebResourceCache,
    val assetLoader: WebViewAssetLoader,
) {

    /** 代码内硬编码的规则；构造时锁定，永不变化。 */
    val builtinRules: List<WebResourceRule> = BuiltinResources.all()

    private val _remoteRules: MutableStateFlow<List<WebResourceRule>> = MutableStateFlow(emptyList())

    /** 当前生效的远程规则（来自 RemoteConfig），按时间顺序追加。 */
    val remoteRules: StateFlow<List<WebResourceRule>> = _remoteRules.asStateFlow()

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val chain: List<WebResourceInterceptor> = listOf(
        BuiltinAssetInterceptor(),
        RemoteRuleInterceptor(),
        LocalFileInterceptor(),
        WebViewAssetLoaderInterceptor(),
    )

    init {
        // 构造时先拉一次（RemoteConfigStorage 已经是单例 in-memory 缓存）
        refresh()
        // 订阅后续变更
        scope.launch {
            RemoteConfigStorage.rulesChanged.collectLatest { refresh() }
        }
    }

    /**
     * 从 [RemoteConfigStorage] 重新拉取远程规则并刷新 [_remoteRules]。
     * 通常由 [RemoteConfigStorage.rulesChanged] 触发，也可手动调用。
     */
    fun refresh() {
        val config = RemoteConfigStorage.getRemoteConfig()
        val remote = WebResourceRuleMapper.toDomainList(config.webViewResourceRules)
        _remoteRules.value = remote
    }

    /**
     * 主入口。遍历拦截器链，返回第一个非 null 的响应；全部 miss 时返回 null（放行 WebView 默认网络）。
     */
    fun intercept(request: WebResourceRequest): WebResourceResponse? =
        chain.firstNotNullOfOrNull { it.intercept(request, this) }

    /**
     * 把 `assets/...` 路径通过 [assetLoader] 解析为响应。
     * 供 [BuiltinAssetInterceptor] / [RemoteRuleInterceptor] 共用。
     */
    fun serveAssetFromLoader(assetPath: String): WebResourceResponse? {
        val normalized = assetPath.trimStart('/')
        val rewritten = "https://${WebViewAssetLoader.DEFAULT_DOMAIN}/assets/$normalized"
        return assetLoader.shouldInterceptRequest(rewritten.toUri())
    }
}
