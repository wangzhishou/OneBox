package com.t8rin.imagetoolbox.core.ui.widget.glass

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 分段卡片中的片段位置 —— 用于由多个独立 Composable 拼接而成的卡片场景。
 *
 * 典型应用：聊天消息卡片由 Header、多个 Content 和 Footer 组合而成，
 * 每个部分需要独立的圆角形状，但整体呈现一张完整玻璃卡片的视觉效果。
 *
 * ```
 * ╭──────────────╮  ← Top     (上方圆角)
 * │  Header      │
 * ├──────────────┤  ← Middle  (无圆角)
 * │  Content 1   │
 * ├──────────────┤  ← Middle  (无圆角)
 * │  Content 2   │
 * ├──────────────┤  ← Bottom  (下方圆角)
 * │  Footer      │
 * ╰──────────────╯
 * ```
 *
 * @see glassCardSegment
 */
@Immutable
enum class GlassCardSegment {
    /** 卡片顶部 — 仅上方有圆角 */
    Top,

    /** 卡片中间 — 无圆角（矩形） */
    Middle,

    /** 卡片底部 — 仅下方有圆角 */
    Bottom,

    /** 独立单段 — 四边都有圆角 */
    Solo;

    /**
     * 根据片段位置和圆角半径生成对应的 [Shape]。
     *
     * 用户消息 (右对齐) 和机器人消息 (左对齐) 的圆角布局不同：
     * - 用户消息：Header 左上圆角，Footer 左下+右下圆角
     * - 机器人消息：Header 右上圆角，Footer 左下+右下圆角
     *
     * 使用 [topStart]、[topEnd]、[bottomStart]、[bottomEnd] 参数精细控制。
     *
     * @param topStart    左上圆角半径
     * @param topEnd      右上圆角半径
     * @param bottomStart 左下圆角半径
     * @param bottomEnd   右下圆角半径
     */
    fun toShape(
        topStart: Dp = 0.dp,
        topEnd: Dp = 0.dp,
        bottomStart: Dp = 0.dp,
        bottomEnd: Dp = 0.dp,
    ): Shape = when (this) {
        Top -> RoundedCornerShape(
            topStart = topStart,
            topEnd = topEnd,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )

        Middle -> RoundedCornerShape(0.dp)

        Bottom -> RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp,
            bottomStart = bottomStart,
            bottomEnd = bottomEnd
        )

        Solo -> RoundedCornerShape(
            topStart = topStart,
            topEnd = topEnd,
            bottomStart = bottomStart,
            bottomEnd = bottomEnd
        )
    }

    /**
     * 简便方法 — 对称圆角（四角等半径）。
     *
     * [Top] 仅上方圆角，[Bottom] 仅下方圆角，[Middle] 无圆角，[Solo] 四角都有。
     */
    fun toShape(cornerRadius: Dp = 16.dp): Shape = when (this) {
        Top -> RoundedCornerShape(
            topStart = cornerRadius,
            topEnd = cornerRadius,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )

        Middle -> RoundedCornerShape(0.dp)

        Bottom -> RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp,
            bottomStart = cornerRadius,
            bottomEnd = cornerRadius
        )

        Solo -> RoundedCornerShape(cornerRadius)
    }
}

