/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
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

package com.t8rin.imagetoolbox.core.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 用户自己调的一组颜色(调色板)。
 *
 * [colors] 按「最近添加的排最前」保存:刚加的颜色在第一位,打开选色面板立刻能看到,
 * 不用滚到末尾确认;批量加入时组内顺序保持传入顺序。同一个颜色在同一组里只留一个,
 * 重复添加会被忽略,避免色块里出现两块一样的颜色。
 *
 * 存在 settings 的 DataStore 里,序列化成 JSON 字符串,所以改动字段要保证向后兼容
 * (新增字段必须给默认值,[Json] 已经开了 ignoreUnknownKeys)。
 */
@Serializable
data class ColorPalette(
    val id: String,
    val name: String,
    val colors: List<Int> = emptyList(),
    val updatedAtMillis: Long = 0L
) {

    val isEmpty: Boolean get() = colors.isEmpty()

    fun contains(argb: Int): Boolean = argb in colors

    /**
     * 插入一个颜色到最前面;已存在或已满则原样返回。
     *
     * 用户的预期是「刚加的颜色要马上看见」:调色板色块列表可能很长,
     * 排到最后需要手动往下翻才能确认加进去了,所以统一置顶。
     */
    fun prepending(
        argb: Int,
        now: Long = System.currentTimeMillis()
    ): ColorPalette = if (contains(argb) || colors.size >= MAX_COLORS) {
        this
    } else {
        copy(
            colors = listOf(argb) + colors,
            updatedAtMillis = now
        )
    }

    /**
     * 一次插入一组颜色到最前面,组内保持传入顺序。
     *
     * 「从收藏创建」「一次把图片配色整组存进来」都会走这里;逐色 [prepending]
     * 会把整组顺序倒过来,所以批量插入单独实现。
     */
    fun prependingAll(
        argbs: List<Int>,
        now: Long = System.currentTimeMillis()
    ): ColorPalette {
        val room = MAX_COLORS - colors.size
        if (room <= 0) return this
        val newColors = argbs
            .distinct()
            .filterNot { contains(it) }
            .take(room)
        if (newColors.isEmpty()) return this
        return copy(
            colors = newColors + colors,
            updatedAtMillis = now
        )
    }

    fun removing(
        argb: Int,
        now: Long = System.currentTimeMillis()
    ): ColorPalette = copy(
        colors = colors.filterNot { it == argb },
        updatedAtMillis = now
    )

    companion object {
        const val MAX_COLORS = 24
        const val MAX_PALETTES = 20

        /** 完全透明色(#00000000)。预置色板第一位,色块用棋盘格展示。 */
        const val TRANSPARENT = 0x00000000

        /** 预置「默认调色板」的固定 id：首启播种、重复播种检测都靠它 */
        const val DEFAULT_PALETTE_ID = "builtin_default"

        /**
         * 预置默认调色板的颜色，和色彩工具色板行的预置色一致。
         *
         * 第一位是透明色:文字/水印/背景这类颜色经常要「无填充」,
         * 没有透明色只能去取色器里把不透明度拉到 0,门槛太高。
         *
         * 首次启动会把它播种成一组调色板；之后用户改名/增删都不影响这份常量，
         * 删掉后也不会再补回来（靠播种标记控制）。
         */
        val DEFAULT_COLORS: List<Int> = listOf(
            TRANSPARENT,
            0xFF6B8E9F.toInt(),
            0xFF22B8CF.toInt(),
            0xFF52C41A.toInt(),
            0xFF9CCC65.toInt(),
            0xFFFFE066.toInt(),
            0xFFFF9A00.toInt(),
            0xFFFF6B4A.toInt(),
            0xFFF8130D.toInt(),
            0xFFFC50A6.toInt(),
            0xFF7B2BEC.toInt(),
            0xFF005FFF.toInt(),
            0xFF59CBF0.toInt(),
            0xFF07DDC3.toInt(),
            0xFFFFFFFF.toInt(),
            0xFF333333.toInt(),
            0xFF000000.toInt()
        )
        /** 名称放宽到 40：展示侧用下拉选择器，长名字只在选中行省略，不会撑坏布局 */
        const val MAX_NAME_LENGTH = 40

        private val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        fun encode(palettes: List<ColorPalette>): String = runCatching {
            json.encodeToString(palettes)
        }.getOrDefault("")

        fun decode(raw: String?): List<ColorPalette> {
            if (raw.isNullOrBlank()) return emptyList()
            return runCatching {
                json.decodeFromString<List<ColorPalette>>(raw)
            }.getOrDefault(emptyList())
        }
    }
}
