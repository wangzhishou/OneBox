package com.wanbaohe.markuplayers.presentation.tools.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.glassBackground
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.core.ui.widget.system.OnePrimaryButton
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.domain.model.AiImageOp
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.presentation.components.SheetHeader
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent

/**
 * 「AI 处理」底部面板:点底部 AI Tab 直达,所有能力一层完成。
 * 顶部自然语言输入:选中图片图层 = 图生图编辑该图层,未选中 = 文生图新增图层,
 * 提交走 [onGenerateSubmit](登录/积分预检在外层完成),不再跳二级弹层。
 * 下方 [AiImageOp] 快捷处理卡:作用于选中图层,未选中时作用于背景图;
 * 当前目标不可处理(选中文字/形状等非图片图层)时整组置灰,点击提示原因。
 * 能力卡点击即处理(结果有预览采用环节兜底,无需二次确认),[AiImageOp.needsRect]
 * 的能力(图像修复)先进入框选模式。
 */
@Composable
fun AiToolSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    component: MarkupLayersComponent,
    pointsCost: Int,
    onOpClick: (AiImageOp) -> Unit,
    onGenerateSubmit: (prompt: String, edit: Boolean) -> Unit,
) {
    // 目标即当前选中态:选中的未锁定图片图层 = 处理/编辑对象;未选中 = 背景图(处理)/新增图层(生成)
    val selectedImageLayer = component.selectedLayer?.takeIf {
        it.type is LayerType.Image && !it.transform.locked
    }
    val canProcess = if (component.selectedLayer == null) {
        component.hasImage
    } else {
        selectedImageLayer != null
    }
    var prompt by rememberSaveable(visible) { mutableStateOf("") }

    EnhancedModalBottomSheet(
        visible = visible,
        dragHandle = {},
        onDismiss = { onDismiss() },
        sheetContent = {
            SheetHeader(
                title = stringResource(R.string.markup_ai_sheet_title),
                onClose = onDismiss
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    // 键盘弹出时面板整体上移,输入框不被遮挡
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                GlassOutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    placeholder = {
                        Text(
                            stringResource(
                                if (selectedImageLayer != null) R.string.markup_ai_edit_image_hint
                                else R.string.markup_ai_generate_hint
                            )
                        )
                    },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                OnePrimaryButton(
                    text = stringResource(R.string.markup_ai_generate_action),
                    onClick = {
                        if (prompt.isBlank()) {
                            AppToastHost.showFailureToast(R.string.markup_ai_generate_empty)
                            return@OnePrimaryButton
                        }
                        onGenerateSubmit(prompt.trim(), selectedImageLayer != null)
                        prompt = ""
                    },
                    enabled = !component.isAiProcessing,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(R.string.markup_ai_generate_points_hint, pointsCost),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                AiImageOp.entries.toList().chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        pair.forEach { op ->
                            AiFeatureCard(
                                op = op,
                                enabled = canProcess,
                                onClick = {
                                    if (canProcess) {
                                        onOpClick(op)
                                    } else {
                                        AppToastHost.showFailureToast(
                                            R.string.studio_ai_image_target_required
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    )
}

@Composable
private fun AiFeatureCard(
    op: AiImageOp,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.45f)
            .glassBackground(
                style = GlassStyle.None,
                shape = ShapeDefaults.default,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Icon(
                imageVector = op.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .clip(ShapeDefaults.small)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(8.dp)
                    .size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(op.nameRes),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1
                )
                Text(
                    text = stringResource(op.descRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Outlined.LineChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
