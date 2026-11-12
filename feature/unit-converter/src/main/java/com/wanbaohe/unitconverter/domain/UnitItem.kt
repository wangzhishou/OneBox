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

package com.wanbaohe.unitconverter.domain

import androidx.annotation.StringRes

/**
 * 换算单位条目。
 * [toBase]   : 将该单位换算为基准单位的系数（乘法）。温度类别忽略此字段，由 [toBaseFn] 处理。
 * [fromBase] : 将基准单位换算为该单位的系数（乘法）。温度同上。
 *
 * 对于线性换算：value_in_base = value * toBase
 * 对于温度等非线性换算：使用 [toBaseFn] / [fromBaseFn]
 */
data class UnitItem(
    @StringRes val nameRes: Int,
    val symbol: String,
    val toBase: Double = 1.0,
    val fromBase: Double = if (toBase != 0.0) 1.0 / toBase else 1.0,
    /** 非线性换算（温度）：输入值 → 基准值（摄氏度） */
    val toBaseFn: ((Double) -> Double)? = null,
    /** 非线性换算（温度）：基准值（摄氏度） → 当前单位值 */
    val fromBaseFn: ((Double) -> Double)? = null,
) {
    fun convertToBase(value: Double): Double =
        toBaseFn?.invoke(value) ?: (value * toBase)

    fun convertFromBase(baseValue: Double): Double =
        fromBaseFn?.invoke(baseValue) ?: (baseValue * fromBase)
}
