package com.wanbaohe.markuplayers.domain.model

/**
 * 归一化矩形(0..1,相对某一参考图的宽高),裁剪框用。
 * 参考系由使用方约定(裁剪会话中 = 旋转/翻转后的底图包围盒)。
 */
data class NormalizedRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top

    companion object {
        val Full = NormalizedRect(0f, 0f, 1f, 1f)
    }
}
