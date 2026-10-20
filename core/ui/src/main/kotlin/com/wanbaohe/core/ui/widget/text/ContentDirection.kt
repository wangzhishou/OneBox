package com.wanbaohe.core.ui.widget.text

import androidx.compose.ui.unit.LayoutDirection

/**
 * 按第一个有方向的字符决定段落方向。
 * 从右往左的页面里，英文思考、通知正文若跟着页面走，句号会被排到行首。
 */
fun String.firstStrongLayoutDirection(): LayoutDirection? {
    for (ch in this) {
        when (Character.getDirectionality(ch)) {
            Character.DIRECTIONALITY_RIGHT_TO_LEFT,
            Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC,
            Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING,
            Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE -> return LayoutDirection.Rtl

            Character.DIRECTIONALITY_LEFT_TO_RIGHT,
            Character.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING,
            Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE -> return LayoutDirection.Ltr
        }
    }
    return null
}
