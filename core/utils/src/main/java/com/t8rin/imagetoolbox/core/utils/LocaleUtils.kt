package com.t8rin.imagetoolbox.core.utils

import java.util.Locale

object LocaleUtils {

    /**
     * 进程级缓存：语言切换会触发进程冷重启（见 LocaleSwitchWatcher），
     * 进程存活期间唯一的变化窗口由 watcher 经 [refreshCurrentLocaleTag] 先行刷新，
     * 因此缓存是安全的，热路径（如每个 API 请求的 locale 参数）不必重复计算。
     */
    @Volatile
    private var cachedTag: String? = null

    /**
     * 返回当前设备 locale tag（BCP-47 格式）。
     *
     * 注意：这里不做 zh-* -> zh-CN 之类的强制归一化，而是把原始 tag
     * 透传给后端。这样以后新增某个 zh-XX 语言时，只需要改后端/Strapi，
     * 不需要发新版客户端。
     *
     * 后端 go-proxy 会负责把不支持的 zh-XX 变体 fallback 到 zh-CN。
     */
    fun getCurrentLocaleTag(): String {
        return cachedTag ?: computeLocaleTag().also { cachedTag = it }
    }

    /**
     * 重新计算并刷新进程级缓存，返回最新 tag。
     * 仅供 LocaleSwitchWatcher 在 onConfigurationChanged 时调用——必须先刷新再比较，
     * 否则缓存会让"语言已变化"这件事检测不到。
     */
    fun refreshCurrentLocaleTag(): String {
        return computeLocaleTag().also { cachedTag = it }
    }

    /**
     * 启动期代码主动改写语言后（如首启英文兜底），直接把缓存对齐到新语言，
     * 避免后台初始化在配置回调到达前读到旧语言。
     */
    internal fun overrideCachedTagAtStartup(tag: String) {
        cachedTag = tag
    }

    private fun computeLocaleTag(): String {
        return Locale.getDefault().toLanguageTag().ifBlank { "en" }
    }
}
