package com.shifenmiao.base.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.Dp

/**
 * 视口底部 [height] 高度内把内容渐隐成透明（真正的 alpha 遮罩，不铺任何颜色）。
 *
 * 用处：列表的视口底边正好压在底栏 / 浮层上沿时，滚动会把下一行横切一刀，露出来的往往只是
 * 卡片顶部那圈内边距，看着像"凭空多出一条空卡片"。把底部渐隐掉之后，内容是从视口边缘淡出
 * 的，卡片被切断的那条硬线不存在了，视口边缘上只剩浮层自己的一条线。
 *
 * 曲线是**下陡上缓**（alpha 按 (1-p)² 走，p 为离底边的比例）：贴边那一小段直接擦干净，
 * 往上再柔和收尾；如果按线性渐变，离底 30dp 左右的内容几乎不掉，看着等于没做。
 *
 * 只能用 `saveLayer` + [BlendMode.DstIn] 做遮罩：直接 `DstOut` 擦画布会把列表背后的页面
 * 背景一起擦穿，露出窗口底色。代价是每帧一次离屏合成，所以 [height] 别开太大（一个行距出头
 * 即可），并且只给需要收边的那一层列表加。
 *
 * 注意：调用方列表的 `contentPadding.bottom` 要大于 [height]，否则滚到底时最后一行会被
 * 这条遮罩吃掉一截。
 */
fun Modifier.fadeBottomEdge(height: Dp): Modifier = drawWithContent {
    val fadePx = height.toPx().coerceIn(0f, size.height)
    if (fadePx <= 0f) {
        drawContent()
        return@drawWithContent
    }
    drawContext.canvas.saveLayer(Rect(0f, 0f, size.width, size.height), Paint())
    drawContent()
    drawRect(
        brush = Brush.verticalGradient(
            *edgeFadeStops,
            startY = size.height - fadePx,
            endY = size.height,
        ),
        blendMode = BlendMode.DstIn,
    )
    drawContext.canvas.restore()
}

/** 离底边越近擦得越干净：alpha = (1 - 离底边比例)²，取 7 段近似这条曲线，避免出现渐变色阶。 */
private val edgeFadeStops: Array<Pair<Float, Color>> = Array(7) { index ->
    val progress = index / 6f
    val remaining = 1f - progress
    progress to Color.Black.copy(alpha = remaining * remaining)
}
