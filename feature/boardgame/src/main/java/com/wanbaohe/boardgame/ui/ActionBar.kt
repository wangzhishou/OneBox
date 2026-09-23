package com.wanbaohe.boardgame.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalIconButton
import com.wanbaohe.boardgame.model.BoardGameAction

/**
 * 对局操作栏:悔棋/重做/分析/分享/重开/认输/重命名/全屏等图标按钮。
 * 按钮列表完全由调用方组装(棋种可增减按钮),kit 不内置具体动作。
 */
@Composable
fun ActionBar(
    actions: List<BoardGameAction>,
    modifier: Modifier = Modifier,
) {
    GlassSurface(
        modifier = modifier,
        style = GlassStyle.Medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            actions.forEach { action ->
                GlassTonalIconButton(onClick = action.onClick, enabled = action.enabled) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.contentDescription,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
