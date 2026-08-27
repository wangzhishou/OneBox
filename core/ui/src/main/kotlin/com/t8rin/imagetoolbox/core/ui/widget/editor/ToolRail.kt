package com.t8rin.imagetoolbox.core.ui.widget.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassDense
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import kotlin.math.roundToInt

/**
 * 编辑器浮动工具竖栏共享件(自 markup-layers EditorScaffold 提取,供图片创作/图文卡片共用):
 * 玻璃竖条 + 可垂直拖动([VerticalDraggable]) + 工具项(22dp 图标 + labelSmall,
 * active = primaryContainer 底)。
 */

/** 垂直可拖动的浮动卡片容器:本地 offset,钳制在画布区域内,会话级 remember */
@Composable
fun VerticalDraggable(
    containerHeightPx: Float,
    modifier: Modifier = Modifier,
    consumeTap: Boolean = false,
    content: @Composable () -> Unit,
) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    var contentHeightPx by remember { mutableIntStateOf(0) }
    Box(
        modifier = modifier
            .onSizeChanged { contentHeightPx = it.height }
            .offset { IntOffset(x = 0, y = offsetY.roundToInt()) }
            // consumeTap:吞掉落在卡片空白处的点按,避免穿透成「点空白取消选择」
            .pointerInput(consumeTap) {
                if (consumeTap) detectTapGestures { }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val limit = ((containerHeightPx - contentHeightPx) / 2f).coerceAtLeast(0f)
                    offsetY = (offsetY + dragAmount.y).coerceIn(-limit, limit)
                }
            }
    ) {
        content()
    }
}

/** 工具竖栏项 */
data class EditorRailTool(
    val id: String,
    val icon: ImageVector,
    val label: String,
    val enabled: Boolean = true,
)

/** 浮动玻璃工具竖栏(同 markup-layers EditorSideBar 样式:0.92 实色打底 + 玻璃层) */
@Composable
fun EditorToolRail(
    tools: List<EditorRailTool>,
    activeId: String?,
    onToolClick: (EditorRailTool) -> Unit,
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
        tools.forEach { tool ->
            EditorToolRailItem(
                tool = tool,
                isActive = activeId == tool.id,
                onClick = { if (tool.enabled) onToolClick(tool) }
            )
        }
    }
}

/** 竖栏单项(同 markup-layers EditorToolItem:22dp 图标 + labelSmall,active=primaryContainer 底) */
@Composable
private fun EditorToolRailItem(
    tool: EditorRailTool,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = if (!tool.enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    } else if (isActive) {
        MaterialTheme.colorScheme.primary
    } else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(ShapeDefaults.default)
            .background(
                if (isActive) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                } else Color.Transparent
            )
            .clickable(enabled = tool.enabled, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = tool.icon,
            contentDescription = tool.label,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = tool.label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            maxLines = 1
        )
    }
}
