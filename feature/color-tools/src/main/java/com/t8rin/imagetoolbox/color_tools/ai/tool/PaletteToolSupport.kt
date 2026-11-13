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

package com.t8rin.imagetoolbox.color_tools.ai.tool

import android.graphics.Color as AndroidColor
import com.t8rin.imagetoolbox.core.domain.model.ColorPalette

/**
 * 调色板工具共用的小工具：颜色解析/格式化、和谐色生成、调色板 JSON 结构。
 *
 * 这里刻意只做纯计算，不碰 Compose 与存储，方便四个工具复用同一份规则。
 */
internal object PaletteToolSupport {

    const val MIN_GENERATE_COUNT = 2
    const val MAX_GENERATE_COUNT = 16
    const val DEFAULT_GENERATE_COUNT = 5

    /** 支持的和谐规则与色相偏移 */
    private val harmonyOffsets = mapOf(
        "complementary" to listOf(0f, 180f),
        "analogous" to listOf(0f, 30f, -30f),
        "triadic" to listOf(0f, 120f, -120f),
        "split_complementary" to listOf(0f, 150f, -150f),
        "tetradic" to listOf(0f, 30f, 180f, 210f),
        "square" to listOf(0f, 90f, 180f, 270f)
    )

    val harmonyNames: List<String> = harmonyOffsets.keys.toList()

    /**
     * 解析十六进制颜色：#RGB / #RRGGBB / #AARRGGBB，`#` 可省略。
     *
     * @return ARGB int；无法解析时返回 null
     */
    fun parseHex(raw: String?): Int? {
        val text = raw?.trim()?.removePrefix("#")?.uppercase().orEmpty()
        if (text.isEmpty()) return null
        return when (text.length) {
            3 -> {
                val expanded = buildString {
                    text.forEach { c ->
                        append(c)
                        append(c)
                    }
                }
                parseHex("FF$expanded")
            }

            6 -> text.toLongOrNull(16)?.let { (0xFF000000L or it).toInt() }
            8 -> text.toLongOrNull(16)?.toInt()
            else -> null
        }
    }

    fun hexOf(argb: Int): String = String.format("#%06X", argb and 0xFFFFFF)

    fun hexWithAlphaOf(argb: Int): String {
        val alpha = (argb ushr 24) and 0xFF
        return if (alpha == 0xFF) hexOf(argb) else String.format("#%08X", argb)
    }

    /**
     * 从一个种子颜色生成 [count] 个颜色。
     *
     * 先铺和谐色（主色在前），不够时按「提亮 → 压深 → 降饱和」的阶梯循环补，
     * 既保证色相不跑，又能凑出一套有层次、能直接当 UI 配色用的色板。
     *
     * @return null 表示 [harmony] 不认识
     */
    fun generate(
        seed: Int,
        harmony: String,
        count: Int
    ): List<Int>? {
        val offsets = harmonyOffsets[harmony.lowercase()] ?: return null
        val target = count.coerceIn(MIN_GENERATE_COUNT, MAX_GENERATE_COUNT)

        val hsv = FloatArray(3)
        AndroidColor.colorToHSV(seed, hsv)
        val hue = hsv[0]
        val saturation = hsv[1]
        val value = hsv[2]

        val result = LinkedHashSet<Int>()
        offsets.forEach { offset ->
            result += colorOf(hue + offset, saturation, value)
        }

        val ladders = listOf(
            0.30f to 0.20f,
            -0.28f to -0.22f,
            -0.50f to 0.04f,
            0.14f to 0.42f,
            -0.12f to -0.45f
        )

        var round = 0
        while (result.size < target && round < MAX_GENERATE_COUNT) {
            val (sDelta, vDelta) = ladders[round % ladders.size]
            for (offset in offsets) {
                if (result.size >= target) break
                result += colorOf(
                    hue + offset,
                    saturation + sDelta,
                    value + vDelta
                )
            }
            round++
        }
        return result.take(target)
    }

    fun normalizeCount(count: Int?): Int =
        (count ?: DEFAULT_GENERATE_COUNT).coerceIn(MIN_GENERATE_COUNT, MAX_GENERATE_COUNT)

    fun paletteJson(
        palette: ColorPalette,
        includeColors: Boolean
    ): Map<String, Any?> = linkedMapOf<String, Any?>(
        "id" to palette.id,
        "name" to palette.name,
        "color_count" to palette.colors.size,
        "updated_at" to palette.updatedAtMillis
    ).apply {
        if (includeColors) {
            put("colors", palette.colors.map(::hexWithAlphaOf))
        }
    }

    private fun colorOf(
        hue: Float,
        saturation: Float,
        value: Float
    ): Int = AndroidColor.HSVToColor(
        floatArrayOf(
            ((hue % 360f) + 360f) % 360f,
            saturation.coerceIn(0f, 1f),
            value.coerceIn(0f, 1f)
        )
    )
}
