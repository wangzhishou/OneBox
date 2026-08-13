package com.wanbaohe.markuplayers.presentation.tools.adjust

import android.graphics.ColorMatrix

/**
 * 基础调节参数(亮度/对比度/饱和度),各通道 -100..+100,0 为原图。
 * 不进图层 undo 历史;预览经 colorFilter,导出时烘焙进位图。
 */
data class BaseAdjustments(
    val brightness: Int = 0,
    val contrast: Int = 0,
    val saturation: Int = 0,
) {
    val isNeutral: Boolean get() = brightness == 0 && contrast == 0 && saturation == 0
}

/**
 * 组装 20 位颜色矩阵(与 androidx.compose.ui.graphics.ColorMatrix 同为 4×5 行主序,
 * 预览/导出共用):饱和度 → 亮度(整幅平移) → 对比度(绕中点 127.5 缩放)。
 * 饱和度/对比度按 (x+100)/100 映射到 0..2 倍率,1 为恒等;亮度 ±100 ≈ ±255 平移。
 */
fun BaseAdjustments.toColorMatrixValues(): FloatArray {
    val matrix = ColorMatrix()
    matrix.setSaturation((saturation + 100) / 100f)

    val brightnessOffset = brightness * 2.55f
    matrix.postConcat(
        ColorMatrix(
            floatArrayOf(
                1f, 0f, 0f, 0f, brightnessOffset,
                0f, 1f, 0f, 0f, brightnessOffset,
                0f, 0f, 1f, 0f, brightnessOffset,
                0f, 0f, 0f, 1f, 0f
            )
        )
    )

    val contrastScale = (contrast + 100) / 100f
    val contrastOffset = (1f - contrastScale) * 127.5f
    matrix.postConcat(
        ColorMatrix(
            floatArrayOf(
                contrastScale, 0f, 0f, 0f, contrastOffset,
                0f, contrastScale, 0f, 0f, contrastOffset,
                0f, 0f, contrastScale, 0f, contrastOffset,
                0f, 0f, 0f, 1f, 0f
            )
        )
    )
    return matrix.array
}
