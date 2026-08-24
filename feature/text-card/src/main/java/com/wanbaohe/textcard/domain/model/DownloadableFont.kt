package com.wanbaohe.textcard.domain.model

import androidx.annotation.StringRes
import com.wanbaohe.textcard.R

/**
 * 内置可下载字体清单(开源字体 CDN 地址,入库前逐个验证可达)。
 * 中文字体体积大,展示 [approxSizeMb] 提示用户;不预打包任何字体文件。
 */
data class DownloadableFont(
    val id: String,
    @param:StringRes val nameRes: Int,
    val url: String,
    val fileName: String,
    val approxSizeMb: Int,
)

object DownloadableFonts {

    val all: List<DownloadableFont> = listOf(
        DownloadableFont(
            id = "noto_sans_sc",
            nameRes = R.string.textcard_font_noto_sans,
            url = "https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/notosanssc/NotoSansSC%5Bwght%5D.ttf",
            fileName = "NotoSansSC.ttf",
            approxSizeMb = 10
        ),
        DownloadableFont(
            id = "noto_serif_sc",
            nameRes = R.string.textcard_font_noto_serif,
            url = "https://raw.githubusercontent.com/notofonts/noto-cjk/main/Serif/OTF/SimplifiedChinese/NotoSerifCJKsc-Regular.otf",
            fileName = "NotoSerifCJKsc-Regular.otf",
            approxSizeMb = 23
        ),
        DownloadableFont(
            id = "lxgw_wenkai",
            nameRes = R.string.textcard_font_lxgw,
            url = "https://github.com/lxgw/LxgwWenKai/releases/latest/download/LXGWWenKai-Regular.ttf",
            fileName = "LXGWWenKai-Regular.ttf",
            approxSizeMb = 19
        ),
    )

    fun byId(id: String): DownloadableFont? = all.find { it.id == id }
}
