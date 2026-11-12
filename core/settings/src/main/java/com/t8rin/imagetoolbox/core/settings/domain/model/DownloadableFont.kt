/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
    val approxSizeMb: Float,
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
            approxSizeMb = 10f
        ),
        DownloadableFont(
            id = "noto_serif_sc",
            nameRes = R.string.font_noto_serif,
            urls = listOf(
                "https://cdn.jsdelivr.net/npm/@expo-google-fonts/noto-serif-sc@0.4.3/400Regular/NotoSerifSC_400Regular.ttf",
                "https://raw.githubusercontent.com/notofonts/noto-cjk/main/Serif/OTF/SimplifiedChinese/NotoSerifCJKsc-Regular.otf"
            ),
            fileName = "NotoSerifSC-Regular.ttf",
            approxSizeMb = 15f
        ),
        DownloadableFont(
            id = "lxgw_wenkai",
            nameRes = R.string.font_lxgw,
            urls = listOf(
                "https://github.com/lxgw/LxgwWenKai/releases/latest/download/LXGWWenKai-Regular.ttf"
            ),
            fileName = "LXGWWenKai-Regular.ttf",
            approxSizeMb = 19f
        ),
        // 手写字体(2026-10,选自 freefonts.space 可追溯-手写体分类,文件自托管于 R2):
        // 鸿雷行书简体/江西拙楷/寒蝉手拙体源自官方公开发布(作者声明免费商用),仅 R2 单镜像;
        // 辰宇落雁體/悠哉字体 GitHub 有官方 release,海外渠道可走 GitHub 镜像
        DownloadableFont(
            id = "honglei_xingshu",
            nameRes = R.string.font_honglei_xingshu,
            urls = emptyList(),
            fileName = "HongLeiXingShu.ttf",
            approxSizeMb = 9f
        ),
        DownloadableFont(
            id = "jiangxi_zhuokai",
            nameRes = R.string.font_jiangxi_zhuokai,
            urls = emptyList(),
            fileName = "JiangXiZhuoKai.ttf",
            approxSizeMb = 10f
        ),
        DownloadableFont(
            id = "hanchan_shouzhuo",
            nameRes = R.string.font_hanchan_shouzhuo,
            urls = emptyList(),
            fileName = "HanChanShouZhuo.ttf",
            approxSizeMb = 13f
        ),
        DownloadableFont(
            id = "chenyuluoyan",
            nameRes = R.string.font_chenyuluoyan,
            urls = listOf(
                "https://github.com/Chenyu-otf/chenyuluoyan_thin/releases/latest/download/ChenYuluoyan-2.0-Thin.ttf"
            ),
            fileName = "ChenYuluoyan-2.0-Thin.ttf",
            approxSizeMb = 9f
        ),
        DownloadableFont(
            id = "yozai",
            nameRes = R.string.font_yozai,
            urls = listOf(
                "https://github.com/lxgw/yozai-font/releases/latest/download/Yozai-Regular.ttf"
            ),
            fileName = "Yozai-Regular.ttf",
            approxSizeMb = 15f
        ),
        // 多语言字体(2026-10,Google Fonts OFL,补小语种覆盖):
        // Caveat 手写(拉丁+西里尔)、Tajawal(阿拉伯)、Mukta(天城文/印地)、
        // Sarabun(泰)、Noto Sans JP/KR(日/韩)。海外渠道 jsDelivr 优先,国内 R2 兜底
        DownloadableFont(
            id = "caveat",
            nameRes = R.string.font_caveat,
            urls = listOf(
                "https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/caveat/Caveat%5Bwght%5D.ttf"
            ),
            fileName = "Caveat.ttf",
            approxSizeMb = 0.4f
        ),
        DownloadableFont(
            id = "tajawal",
            nameRes = R.string.font_tajawal,
            urls = listOf(
                "https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/tajawal/Tajawal-Regular.ttf"
            ),
            fileName = "Tajawal-Regular.ttf",
            approxSizeMb = 0.1f
        ),
        DownloadableFont(
            id = "mukta",
            nameRes = R.string.font_mukta,
            urls = listOf(
                "https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/mukta/Mukta-Regular.ttf"
            ),
            fileName = "Mukta-Regular.ttf",
            approxSizeMb = 0.4f
        ),
        DownloadableFont(
            id = "sarabun",
            nameRes = R.string.font_sarabun,
            urls = listOf(
                "https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/sarabun/Sarabun-Regular.ttf"
            ),
            fileName = "Sarabun-Regular.ttf",
            approxSizeMb = 0.1f
        ),
        DownloadableFont(
            id = "noto_sans_jp",
            nameRes = R.string.font_noto_sans_jp,
            urls = listOf(
                "https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/notosansjp/NotoSansJP%5Bwght%5D.ttf"
            ),
            fileName = "NotoSansJP.ttf",
            approxSizeMb = 9f
        ),
        DownloadableFont(
            id = "noto_sans_kr",
            nameRes = R.string.font_noto_sans_kr,
            urls = listOf(
                "https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/notosanskr/NotoSansKR%5Bwght%5D.ttf"
            ),
            fileName = "NotoSansKR.ttf",
            approxSizeMb = 10f
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
