package com.t8rin.imagetoolbox.core.utils

import java.util.Locale

object LocaleUtils {

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
        return Locale.getDefault().toLanguageTag().ifBlank { "en" }
    }
}
