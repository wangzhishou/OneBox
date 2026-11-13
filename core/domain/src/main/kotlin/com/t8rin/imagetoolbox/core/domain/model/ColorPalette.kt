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
 * [colors] 按用户添加顺序保存(主色 → 辅色 → 点缀),不是取色历史那种倒序;
 * 同一个颜色在同一组里只留一个,重复添加会被忽略,避免色块里出现两块一样的颜色。
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

    /** 追加一个颜色;已存在则原样返回(顺序即用户的搭配顺序,不做「最近使用」置顶)。 */
    fun appending(
        argb: Int,
        now: Long = System.currentTimeMillis()
    ): ColorPalette = if (contains(argb) || colors.size >= MAX_COLORS) {
        this
    } else {
        copy(
            colors = colors + argb,
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
