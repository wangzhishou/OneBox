package com.wanbaohe.textcard.presentation.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.button.ConfirmButton
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.LoadingDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.presentation.editor.panels.BackgroundPanel
import com.wanbaohe.textcard.presentation.editor.panels.FontPanel
import com.wanbaohe.textcard.presentation.editor.panels.LayersPanel
import com.wanbaohe.textcard.presentation.editor.panels.TextStylePanel
import com.wanbaohe.textcard.presentation.screenLogic.EditorPanel
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.CropSquare
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Tune

/**
 * 编辑首页(设计稿 01):画布预览 + 底部常驻栏(4 个面板 Tab + 保存按钮)。
 * Tab 点击后以 [EnhancedModalBottomSheet] 弹出对应面板(标题栏 + 关闭按钮,
 * 模式参考 DemoScreen);点画布文字弹出 [TextEditSheet],文字块/自定义背景图
 * 支持画布内拖动(手势在 [CardCanvasPreview] 内)。
 */
@Composable
fun TextCardEditorScreen(
    component: TextCardComponent,
) {
    var showTextEditSheet by rememberSaveable { mutableStateOf(false) }
    var showDecorationSheet by rememberSaveable { mutableStateOf(false) }
    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    // 编辑页返回(顶栏 + 系统返回键/手势):有未保存变更先弹退出确认,选择页不拦截
    val onBack = {
        if (component.haveChanges) {
            showExitDialog = true
        } else {
            component.backToSelection()
        }
    }

    BaseScreen(
        title = stringResource(R.string.textcard_title),
        onGoBack = onBack,
        showNavigationBarsPadding = false,
        supportGlassEffect = false,
        content = {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                component.renderState()?.let { state ->
                    CardCanvasPreview(
                        state = state,
                        onTextClick = { blockId ->
                            component.selectTextBlock(blockId)
                            showTextEditSheet = true
                        },
                        onTextDrag = component::updateTextBlockOffset,
                        onBackgroundDrag = component::updateBackgroundImageOffset,
                        onDecorationDrag = component::updateDecorationOffset,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            EditorBottomBar(
                component = component,
                onSaveClick = {
                    component.saveCard(component::parseSaveResult)
                }
            )
        }
    )

    EditorPanelSheet(
        component = component,
        onEditDecoration = { showDecorationSheet = true }
    )

    TextEditSheet(
        visible = showTextEditSheet,
        component = component,
        onDismiss = { showTextEditSheet = false }
    )

    DecorationPickerSheet(
        visible = showDecorationSheet,
        component = component,
        onDismiss = { showDecorationSheet = false }
    )

    ExitWithoutSavingDialog(
        onExit = { component.backToSelection() },
        onDismiss = { showExitDialog = false },
        visible = showExitDialog
    )

    LoadingDialog(
        visible = component.isSaving,
        onCancelLoading = component::cancelSaving,
        canCancel = true
    )
}

/** 面板底部弹层:标题栏(居中标题 + 关闭按钮) + 对应面板内容 */
@Composable
private fun EditorPanelSheet(
    component: TextCardComponent,
    onEditDecoration: () -> Unit,
) {
    val activePanel = component.activePanel
    EnhancedModalBottomSheet(
        visible = activePanel != null,
        dragHandle = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                actions = {
                    IconButton(onClick = { component.setActivePanel(null) }) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = activePanel?.let { stringResource(it.labelRes()) }.orEmpty(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            )
        },
        onDismiss = { component.setActivePanel(null) },
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                when (activePanel) {
                    EditorPanel.Background -> BackgroundPanel(component)
                    EditorPanel.Font -> FontPanel(component)
                    EditorPanel.TextStyle -> TextStylePanel(component)
                    EditorPanel.Layers -> LayersPanel(
                        component = component,
                        onEditDecoration = onEditDecoration
                    )

                    null -> Unit
                }
            }
        }
    )
}

/** 底部常驻栏(设计稿 01):4 个面板 Tab + 保存按钮 */
@Composable
private fun EditorBottomBar(
    component: TextCardComponent,
    onSaveClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        EditorPanel.entries.forEach { panel ->
            BottomTab(
                panel = panel,
                active = component.activePanel == panel,
                onClick = { component.togglePanel(panel) },
                modifier = Modifier.weight(1f)
            )
        }
        ConfirmButton(
            text = stringResource(R.string.textcard_save),
            onClick = onSaveClick,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun BottomTab(
    panel: EditorPanel,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = if (active) {
        MaterialTheme.colorScheme.primary
    } else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = panel.icon(),
            contentDescription = stringResource(panel.labelRes()),
            tint = color
        )
        Text(
            text = stringResource(panel.labelRes()),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

private fun EditorPanel.icon(): ImageVector = when (this) {
    EditorPanel.Background -> MaterialIcons.Outlined.CropSquare
    EditorPanel.Font -> MaterialIcons.Outlined.TextFields
    EditorPanel.TextStyle -> MaterialIcons.Outlined.Tune
    EditorPanel.Layers -> MaterialIcons.Outlined.Layers
}

private fun EditorPanel.labelRes(): Int = when (this) {
    EditorPanel.Background -> R.string.textcard_tab_background
    EditorPanel.Font -> R.string.textcard_tab_font
    EditorPanel.TextStyle -> R.string.textcard_tab_text_style
    EditorPanel.Layers -> R.string.textcard_tab_layers
}
