package com.t8rin.imagetoolbox.core.settings.domain.model

import androidx.annotation.StringRes
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.settings.BuildConfig

/**
 * 全局可下载字体清单(自 feature/text-card 上移)。urls 为多镜像按序回退,
 * 应对部分环境(如模拟器)对单一 CDN 的证书校验失败。
 * 中文字体体积大,展示 [approxSizeMb] 提示用户;不预打包任何字体文件。
 *
 * 镜像策略(按 [urlsForCurrentFlavor] 排序):
 * - 国内渠道:R2(自有静态资源,bucket onebox-images 的 fonts/ 路径,国内可达)优先,
 *   jsDelivr/GitHub 兜底——GitHub 系国内基本不可达
 * - google 渠道:jsDelivr/GitHub 优先,R2 最后兜底
 *
 * 镜像可达性(2026-08-25 curl 实测,代理/无代理两环境均 GET+Range 验证):
 * - 思源黑体:jsDelivr google/fonts 可变 TTF 206/206;GitHub raw OTF 206/206
 * - 思源宋体:jsDelivr npm @expo-google-fonts/noto-serif-sc(14.1MB,未超 jsDelivr
 *   20MB 限额;google/fonts 的 NotoSerifSC[wght].ttf 超限额 403 不可用)206/206;
 *   GitHub raw OTF 206/206
 * - 霞鹜文楷:npm 上仅 woff2 分包不可用,只留 GitHub releases 206/206
 */
data class DownloadableFont(
    val id: String,
    @param:StringRes val nameRes: Int,
    val urls: List<String>,
    val fileName: String,
    val approxSizeMb: Int,
)

object DownloadableFonts {

    /** R2 静态资源基地址(自有 CDN,字体文件在 bucket 的 fonts/ 路径下,按 fileName 寻址) */
    private const val R2_FONT_BASE = "https://images.oneboxable.com/fonts"

    val all: List<DownloadableFont> = listOf(
        DownloadableFont(
            id = "noto_sans_sc",
            nameRes = R.string.font_noto_sans,
            urls = listOf(
                "https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/notosanssc/NotoSansSC%5Bwght%5D.ttf",
                "https://raw.githubusercontent.com/notofonts/noto-cjk/main/Sans/OTF/SimplifiedChinese/NotoSansCJKsc-Regular.otf"
            ),
            fileName = "NotoSansSC.ttf",
            approxSizeMb = 10
        ),
        DownloadableFont(
            id = "noto_serif_sc",
            nameRes = R.string.font_noto_serif,
            urls = listOf(
                "https://cdn.jsdelivr.net/npm/@expo-google-fonts/noto-serif-sc@0.4.3/400Regular/NotoSerifSC_400Regular.ttf",
                "https://raw.githubusercontent.com/notofonts/noto-cjk/main/Serif/OTF/SimplifiedChinese/NotoSerifCJKsc-Regular.otf"
            ),
            fileName = "NotoSerifSC-Regular.ttf",
            approxSizeMb = 15
        ),
        DownloadableFont(
            id = "lxgw_wenkai",
            nameRes = R.string.font_lxgw,
            urls = listOf(
                "https://github.com/lxgw/LxgwWenKai/releases/latest/download/LXGWWenKai-Regular.ttf"
            ),
            fileName = "LXGWWenKai-Regular.ttf",
            approxSizeMb = 19
        ),
    )

    fun byId(id: String): DownloadableFont? = all.find { it.id == id }

    /**
     * 当前渠道的镜像排序(下载按序回退):国内渠道 R2 优先(GitHub 系国内不可达),
     * 海外渠道(google / foss)保持 jsDelivr/GitHub 优先、R2 最后兜底
     */
    fun DownloadableFont.urlsForCurrentFlavor(): List<String> {
        val r2Url = "$R2_FONT_BASE/$fileName"
        return if (BuildConfig.FLAVOR == "google" || BuildConfig.FLAVOR == "foss") urls + r2Url else listOf(r2Url) + urls
    }
}
