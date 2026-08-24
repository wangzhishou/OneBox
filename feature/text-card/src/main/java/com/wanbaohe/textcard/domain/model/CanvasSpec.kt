package com.wanbaohe.textcard.domain.model

import androidx.annotation.StringRes
import com.wanbaohe.textcard.R

/**
 * 画布规格:导出位图尺寸即 width × height(1080 宽 PNG)。
 */
enum class CanvasSpec(
    val width: Int,
    val height: Int,
    @param:StringRes val titleRes: Int,
    @param:StringRes val descRes: Int,
    val ratioLabel: String,
) {
    Xiaohongshu(
        width = 1080,
        height = 1350,
        titleRes = R.string.textcard_canvas_xiaohongshu,
        descRes = R.string.textcard_canvas_xiaohongshu_desc,
        ratioLabel = "3:4"
    ),
    WeChat(
        width = 1080,
        height = 1080,
        titleRes = R.string.textcard_canvas_wechat,
        descRes = R.string.textcard_canvas_wechat_desc,
        ratioLabel = "1:1"
    );

    val aspectRatio: Float get() = width.toFloat() / height.toFloat()
}
