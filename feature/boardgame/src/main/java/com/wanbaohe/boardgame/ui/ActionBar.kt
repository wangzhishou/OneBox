package com.wanbaohe.boardgame.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.wanbaohe.boardgame.model.BoardGameAction

/**
 * 对局与摆棋共用的图标、文字操作栏。
 * 按钮列表完全由调用方组装(棋种可增减按钮),kit 不内置具体动作。
 */
@Composable
fun ActionBar(
    actions: List<BoardGameAction>,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable RowScope.() -> Unit)? = null,
) {
    GlassSurface(
        modifier = modifier,
        style = GlassStyle.Medium,
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            actions.forEach { action ->
                BoardGameToolButton(
                    icon = action.icon,
                    label = action.contentDescription.orEmpty(),
                    onClick = action.onClick,
                    enabled = action.enabled,
                    selected = action.selected,
                    modifier = Modifier.weight(1f),
                )
            }
            trailingContent?.invoke(this)
        }
    }
}
