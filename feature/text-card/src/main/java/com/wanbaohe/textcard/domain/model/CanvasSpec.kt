package com.wanbaohe.textcard.domain.model

import androidx.annotation.StringRes
import com.wanbaohe.textcard.R
import kotlin.math.roundToInt

/**
 * 画布规格:导出位图尺寸即 width × height。
 * 内置小红书/微信两个平台规格 + 用户自定义尺寸([Custom],持久化记忆上次输入)。
 */
sealed class CanvasSpec(
    val width: Int,
    val height: Int,
    @get:StringRes val titleRes: Int,
    @get:StringRes val descRes: Int,
    val ratioLabel: String,
) {

    data object Xiaohongshu : CanvasSpec(
        width = 1080,
        height = 1350,
        titleRes = R.string.textcard_canvas_xiaohongshu,
        descRes = R.string.textcard_canvas_xiaohongshu_desc,
        ratioLabel = "3:4"
    )

    data object WeChat : CanvasSpec(
        width = 1080,
        height = 1080,
        titleRes = R.string.textcard_canvas_wechat,
        descRes = R.string.textcard_canvas_wechat_desc,
        ratioLabel = "1:1"
    )

    /** 用户自定义宽高(px,输入侧钳制 256..4096) */
    data class Custom(
        val customWidth: Int,
        val customHeight: Int,
    ) : CanvasSpec(
        width = customWidth,
        height = customHeight,
        titleRes = R.string.textcard_canvas_custom,
        descRes = R.string.textcard_canvas_custom_desc,
        ratioLabel = "${customWidth}×${customHeight}"
    )

    val aspectRatio: Float get() = width.toFloat() / height.toFloat()

    /** 导出尺寸:长边超过 [maxSide] 时等比缩小(防 OOM),否则原样返回 */
    fun exportScaled(maxSide: Int = 2048): CanvasSpec {
        val longSide = maxOf(width, height)
        if (longSide <= maxSide) return this
        val scale = maxSide.toFloat() / longSide
        return Custom(
            customWidth = (width * scale).roundToInt().coerceAtLeast(1),
            customHeight = (height * scale).roundToInt().coerceAtLeast(1)
        )
    }

    companion object {
        /**
         * 内置平台规格(自定义规格单独入口)。
         * 必须 lazy:直接初始化会在「首个被触发的子类是 Xiaohongshu」时
         * 因类初始化循环(父类 clinit 读取尚在初始化中的子类静态字段)
         * 读到 null。
         */
        val builtIn: List<CanvasSpec> by lazy { listOf(Xiaohongshu, WeChat) }
    }
}
