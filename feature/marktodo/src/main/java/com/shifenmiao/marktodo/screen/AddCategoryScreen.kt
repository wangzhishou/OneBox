package com.shifenmiao.marktodo.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.wanbaohe.base.ui.icon.IconPickerSheet
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.data.iconFromKey
import com.shifenmiao.marktodo.screenLogic.AddCategoryComponent
import com.shifenmiao.marktodo.screenLogic.AddCategoryUiEvent
import com.shifenmiao.marktodo.theme.categoryPaletteWithThemeColors
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.line.LineNote
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePaletteTools
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePinEnd
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSingleEdit
import com.wanbaohe.core.ui.widget.controls.selection.ColorRowSelector
import com.wanbaohe.core.ui.widget.preferences.PreferenceRowSwitch
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OneBoxOutlinedTextField
import com.wanbaohe.core.ui.widget.system.OneBoxSectionCard
import com.wanbaohe.core.ui.widget.system.OnePrimaryButton

/**
 * 新增/编辑主题页 —— 原型设计稿：
 * 主题名称 / 主题描述 / 图标 / 颜色 / 是否置顶 + 底部创建按钮。
 */
@Composable
fun AddCategoryScreen(
    component: AddCategoryComponent,
    onGoBack: () -> Unit
) {
    val uiState by component.uiState.collectAsState()
    val isEditMode = component.editingCategoryId != null
    var showIconPicker by remember { mutableStateOf(false) }

    BaseScreen(
        title = {
            Text(
                text = stringResource(
                    if (isEditMode) R.string.dialog_edit_category_title
                    else R.string.dialog_add_category_title
                )
            )
        },
        actions = {
            IconButton(onClick = { component.handleEvent(AddCategoryUiEvent.Submit) }) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                    contentDescription = stringResource(R.string.action_save)
                )
            }
        },
        onGoBack = onGoBack,
        isShowDefaultActions = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = OneBoxDesignSystem.screenPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing)
        ) {
            // 主题名称（左侧 leadingIcon 为当前图标，点击弹出图标选择器）
            CategoryFormSection(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSingleEdit,
                label = stringResource(R.string.category_name)
            ) {
                OneBoxOutlinedTextField(
                    value = uiState.title,
                    onValueChange = { component.handleEvent(AddCategoryUiEvent.UpdateTitle(it)) },
                    singleLine = true,
                    placeholder = { Text(text = stringResource(R.string.dialog_add_category_hint)) },
                    leadingIcon = {
                        val themeColor = Color(uiState.colorArgb)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(OneBoxDesignSystem.smallRadius))
                                .background(themeColor.copy(alpha = 0.15f))
                                .clickable { showIconPicker = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconFromKey(uiState.iconKey),
                                contentDescription = stringResource(R.string.dialog_add_category_icon),
                                tint = themeColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.hasTitleError,
                    supportingText = if (uiState.hasTitleError) {
                        {
                            Text(
                                text = stringResource(R.string.validation_category_title_required),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else null,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Next)
                )
            }

            // 主题描述（可选）
            CategoryFormSection(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineNote,
                label = stringResource(R.string.category_description)
            ) {
                OneBoxOutlinedTextField(
                    value = uiState.description,
                    onValueChange = { component.handleEvent(AddCategoryUiEvent.UpdateDescription(it)) },
                    placeholder = { Text(text = stringResource(R.string.dialog_add_category_desc_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Done)
                )
            }

            // 颜色（统一用 ColorRowSelector：色板 + 自定义取色，禁透明度避免脏数据）
            CategoryFormSection(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePaletteTools,
                label = stringResource(R.string.dialog_add_category_color),
                showLabel = false
            ) {
                ColorRowSelector(
                    value = Color(uiState.colorArgb),
                    onValueChange = { component.handleEvent(AddCategoryUiEvent.UpdateColor(it.toArgb())) },
                    title = stringResource(R.string.dialog_add_category_color),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePaletteTools,
                    allowAlpha = false,
                    defaultColors = categoryPaletteWithThemeColors(),
                    contentHorizontalPadding = 0.dp
                )
            }

            // 是否置顶
            PreferenceRowSwitch(
                title = stringResource(R.string.category_pin_top),
                subtitle = stringResource(R.string.category_pin_top_hint),
                checked = uiState.isPinned,
                onClick = { component.handleEvent(AddCategoryUiEvent.UpdatePinned(it)) },
                startIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePinEnd
            )

            // 底部创建/保存按钮
            OnePrimaryButton(
                text = stringResource(
                    if (isEditMode) R.string.action_save else R.string.action_create_category
                ),
                onClick = { component.handleEvent(AddCategoryUiEvent.Submit) },
                modifier = Modifier.fillMaxWidth()
            )

            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.padding(bottom = OneBoxDesignSystem.blockSpacing)
            )
        }
    }

    // 图标选择器（名称字段左侧图标弹出）
    if (showIconPicker) {
        IconPickerSheet(
            visible = true,
            onDismiss = { showIconPicker = false },
            onIconSelected = { iconKey ->
                component.handleEvent(AddCategoryUiEvent.UpdateIconKey(iconKey))
            },
            selectedIconName = uiState.iconKey
        )
    }

    BackHandler {
        onGoBack()
    }
}

/**
 * 表单分组：浅色玻璃卡片 + 图标标题行（与新增待办页一致）
 *
 * @param showLabel 分组内组件自带标题时（如 ColorRowSelector）传 false
 */
@Composable
private fun CategoryFormSection(
    icon: ImageVector,
    label: String,
    showLabel: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    OneBoxSectionCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest) {
        if (showLabel) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        content()
    }
}
