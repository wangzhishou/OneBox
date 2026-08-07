package com.wanbaohe.unitconverter.domain

/**
 * 换算单位条目。
 * [toBase]   : 将该单位换算为基准单位的系数（乘法）。温度类别忽略此字段，由 [toBaseFn] 处理。
 * [fromBase] : 将基准单位换算为该单位的系数（乘法）。温度同上。
 *
 * 对于线性换算：value_in_base = value * toBase
 * 对于温度等非线性换算：使用 [toBaseFn] / [fromBaseFn]
 */
data class UnitItem(
    val name: String,
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

