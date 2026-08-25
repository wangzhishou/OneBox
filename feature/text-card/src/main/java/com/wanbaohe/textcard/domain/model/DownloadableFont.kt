package com.wanbaohe.textcard.domain.model

import androidx.annotation.StringRes
import com.wanbaohe.textcard.R

/**
 * 内置可下载字体清单。urls 为多镜像按序回退(jsDelivr 优先,GitHub 系兜底),
 * 应对部分环境(如模拟器)对单一 CDN 的证书校验失败。
 * 中文字体体积大,展示 [approxSizeMb] 提示用户;不预打包任何字体文件。
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

    val all: List<DownloadableFont> = listOf(
        DownloadableFont(
            id = "noto_sans_sc",
            nameRes = R.string.textcard_font_noto_sans,
            urls = listOf(
                "https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/notosanssc/NotoSansSC%5Bwght%5D.ttf",
                "https://raw.githubusercontent.com/notofonts/noto-cjk/main/Sans/OTF/SimplifiedChinese/NotoSansCJKsc-Regular.otf"
            ),
            fileName = "NotoSansSC.ttf",
            approxSizeMb = 10
        ),
        DownloadableFont(
            id = "noto_serif_sc",
            nameRes = R.string.textcard_font_noto_serif,
            urls = listOf(
                "https://cdn.jsdelivr.net/npm/@expo-google-fonts/noto-serif-sc@0.4.3/400Regular/NotoSerifSC_400Regular.ttf",
                "https://raw.githubusercontent.com/notofonts/noto-cjk/main/Serif/OTF/SimplifiedChinese/NotoSerifCJKsc-Regular.otf"
            ),
            fileName = "NotoSerifSC-Regular.ttf",
            approxSizeMb = 15
        ),
        DownloadableFont(
            id = "lxgw_wenkai",
            nameRes = R.string.textcard_font_lxgw,
            urls = listOf(
                "https://github.com/lxgw/LxgwWenKai/releases/latest/download/LXGWWenKai-Regular.ttf"
            ),
            fileName = "LXGWWenKai-Regular.ttf",
            approxSizeMb = 19
        ),
    )

    fun byId(id: String): DownloadableFont? = all.find { it.id == id }
}
