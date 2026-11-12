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

package com.wanbaohe.iching.model

/** A line produced by the three-coin method. Values are 6, 7, 8, or 9. */
data class HexagramLine(val value: Int) {
    init {
        require(value in 6..9) { "A hexagram line must be 6, 7, 8, or 9" }
    }

    val isYang: Boolean get() = value == 7 || value == 9
    val isChanging: Boolean get() = value == 6 || value == 9
    val changedIsYang: Boolean get() = if (isChanging) !isYang else isYang
}

/**
 * 卦的纯结构信息:卦号 + 上下卦三爻编码。
 * 卦名/象名等本地化文本由 IChingTextLibrary 按 number/code 解析,不进此模型。
 */
data class HexagramInfo(
    val number: Int,
    val upperTrigramCode: Int,
    val lowerTrigramCode: Int,
)

data class DivinationResult(
    val question: String,
    /** Lines in traditional bottom-to-top order. */
    val lines: List<HexagramLine>,
    val primary: HexagramInfo,
    val changed: HexagramInfo?,
) {
    val changingLineNumbers: List<Int>
        get() = lines.mapIndexedNotNull { index, line -> (index + 1).takeIf { line.isChanging } }

    /** 变卦六爻(动爻阴阳翻转,均为不变爻值 7/8),用于绘制变卦图 */
    val changedLines: List<HexagramLine>
        get() = lines.map { line -> HexagramLine(if (line.changedIsYang) 7 else 8) }
}

