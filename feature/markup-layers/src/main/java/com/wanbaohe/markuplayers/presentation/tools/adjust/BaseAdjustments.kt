package com.wanbaohe.markuplayers.presentation.tools.adjust

import android.graphics.ColorMatrix
import kotlin.math.pow

/**
 * 线性矩阵调节,各通道 -100..+100,0 为原图。
 * 作用目标 = 当前选中图层(存 [com.wanbaohe.markuplayers.domain.model.MarkupLayer.adjustments],
 * 随图层进编辑历史);未选中图层时作用于背景图,历史由编辑器统一管理。
 * 预览经 colorFilter,导出时按目标烘焙进位图。
 */
data class BaseAdjustments(
    val brightness: Int = 0,
    val contrast: Int = 0,
    val saturation: Int = 0,
    val exposure: Int = 0,
    val temperature: Int = 0,
    val tint: Int = 0,
) {
    val isNeutral: Boolean
        get() = brightness == 0 && contrast == 0 && saturation == 0 &&
            exposure == 0 && temperature == 0 && tint == 0
}

/**
 * 组装 20 位颜色矩阵(与 androidx.compose.ui.graphics.ColorMatrix 同为 4×5 行主序,
 * 预览/导出共用):饱和度 → 亮度(整幅平移) → 对比度(绕中点 127.5 缩放)。
 * 饱和度/对比度按 (x+100)/100 映射到 0..2 倍率,1 为恒等;亮度 ±100 ≈ ±255 平移。
 * 后追加曝光(-2..+2 EV 的 RGB 倍率)、色温(红/蓝增益)与色调(绿/洋红增益)。
 * 新通道为 0 时保持旧三通道矩阵完全不变,不伪装曲线/高光等非线性调节。
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
    if (exposure != 0 || temperature != 0 || tint != 0) {
        val exposureGain = 2f.pow(exposure.coerceIn(-100, 100) / 50f)
        val warmth = temperature.coerceIn(-100, 100) / 100f
        val magenta = tint.coerceIn(-100, 100) / 100f
        val redGain = exposureGain * (1f + warmth * 0.2f) * (1f + magenta * 0.1f)
        val greenGain = exposureGain * (1f - magenta * 0.2f)
        val blueGain = exposureGain * (1f - warmth * 0.2f) * (1f + magenta * 0.1f)
        matrix.postConcat(
            ColorMatrix(
                floatArrayOf(
                    redGain, 0f, 0f, 0f, 0f,
                    0f, greenGain, 0f, 0f, 0f,
                    0f, 0f, blueGain, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        )
    }
    return matrix.array
}
