package com.wanbaohe.markuplayers.presentation.draw

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Eraser
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFrontHand
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTune
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassDense
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.markuplayers.R

/**
 * 绘制模式画布右侧的精简浮动竖条(与左侧工具栏同款玻璃容器):
 * 画笔设置(开 [BrushSettingsDialog])/橡皮擦快捷开关/笔画撤销/笔画重做/浏览模式切换。
 */
@Composable
internal fun DrawFloatingBar(
    session: DrawSessionState,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
            // 浮动容器近不透明:0.92 实色打底,玻璃层只保留边框/高光与一丝通透
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f),
                shape = ShapeDefaults.extraLarge
            )
            .glassDense(
                shape = ShapeDefaults.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainer
            )
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        DrawBarButton(
            icon = Icons.Outlined.LineTune,
            labelRes = R.string.markup_brush_settings_title,
            onClick = onOpenSettings
        )
        DrawBarButton(
            icon = Icons.Rounded.Eraser,
            labelRes = R.string.markup_draw_eraser_mode,
            active = session.isEraser,
            onClick = {
                if (session.isEraser) session.enableBrush() else session.enableEraser()
            }
        )
        DrawBarButton(
            icon = Icons.Outlined.LineUndo,
            labelRes = R.string.markup_undo,
            enabled = session.canUndo,
            onClick = session::undo
        )
        DrawBarButton(
            icon = Icons.Outlined.LineRedo,
            labelRes = R.string.markup_redo,
            enabled = session.canRedo,
            onClick = session::redo
        )
        DrawBarButton(
            icon = Icons.Outlined.LineFrontHand,
            labelRes = R.string.markup_draw_pan_mode,
            active = session.isPanMode,
            onClick = session::togglePanMode
        )
    }
}

@Composable
private fun DrawBarButton(
    icon: ImageVector,
    @StringRes labelRes: Int,
    onClick: () -> Unit,
    active: Boolean = false,
    enabled: Boolean = true,
) {
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        active -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(ShapeDefaults.default)
            .background(
                if (active) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                } else Color.Transparent
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(labelRes),
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
    }
}
