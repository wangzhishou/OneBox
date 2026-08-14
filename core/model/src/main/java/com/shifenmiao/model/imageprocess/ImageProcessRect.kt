package com.shifenmiao.model.imageprocess

/**
 * 归一化矩形(0..1,相对所发图片的宽高),图像修复([ImageProcessOp.Inpainting])
 * 的修复区域用;请求时换算为所发图片的像素坐标。
 */
data class ImageProcessRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    companion object {
        val Full = ImageProcessRect(0f, 0f, 1f, 1f)
    }
}
