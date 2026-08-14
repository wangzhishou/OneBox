package com.wanbaohe.markuplayers.presentation.tools.basic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCrop
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFilters
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRotateRight
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent
import com.wanbaohe.markuplayers.presentation.tools.adjust.AdjustPanelContent

/**
 * 「基础工具」底部面板(设计稿 Light 主题主编辑界面的底部展开区):
 * 顶部入口图标行(裁剪/旋转 → 裁剪旋转页;滤镜 → 占位)+ 基础调节滑杆。
 */
@Composable
fun BasicToolsSheet(
    visible: Boolean,
    component: MarkupLayersComponent,
    onOpenCrop: () -> Unit,
    onOpenFilter: () -> Unit,
    onDismiss: () -> Unit,
) {
    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = { onDismiss() },
        sheetContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val openCrop = {
                        onDismiss()
                        onOpenCrop()
                    }
                    BasicToolEntry(
                        icon = Icons.Outlined.LineCrop,
                        label = stringResource(R.string.markup_tool_crop),
                        onClick = openCrop,
                        modifier = Modifier.weight(1f)
                    )
                    BasicToolEntry(
                        icon = Icons.Outlined.LineRotateRight,
                        label = stringResource(R.string.markup_crop_rotate_entry),
                        onClick = openCrop,
                        modifier = Modifier.weight(1f)
                    )
                    BasicToolEntry(
                        icon = Icons.Outlined.LineFilters,
                        label = stringResource(R.string.markup_tool_filter),
                        onClick = {
                            onDismiss()
                            onOpenFilter()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(4.dp))
                AdjustPanelContent(component = component)
            }
        }
    )
}

@Composable
private fun BasicToolEntry(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(ShapeDefaults.default)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}
