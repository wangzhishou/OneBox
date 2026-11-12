/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.line.LineNote
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePinEnd
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSingleEdit
import com.wanbaohe.core.ui.widget.preferences.PreferenceRowSwitch
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OneBoxOutlinedTextField
import com.wanbaohe.core.ui.widget.system.OneBoxSectionCard
import com.wanbaohe.core.ui.widget.system.OnePrimaryButton

/**
 * 新增/编辑分组页：名称 / 描述 / 图标 / 是否置顶 + 底部创建按钮。
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
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(OneBoxDesignSystem.smallRadius))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { showIconPicker = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconFromKey(uiState.iconKey),
                                contentDescription = stringResource(R.string.dialog_add_category_icon),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
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

            // 是否置顶
            PreferenceRowSwitch(
                title = stringResource(R.string.category_pin_top),
                subtitle = stringResource(R.string.category_pin_top_hint),
                checked = uiState.isPinned,
                onClick = { component.handleEvent(AddCategoryUiEvent.UpdatePinned(it)) },
                startIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePinEnd
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                OnePrimaryButton(
                    text = stringResource(
                        if (isEditMode) R.string.action_save else R.string.action_create_category
                    ),
                    onClick = { component.handleEvent(AddCategoryUiEvent.Submit) },
                )
            }

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

/** 表单分组：浅色玻璃卡片 + 图标标题行（与新增待办页一致） */
@Composable
private fun CategoryFormSection(
    icon: ImageVector,
    label: String,
    content: @Composable ColumnScope.() -> Unit
) {
    OneBoxSectionCard(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
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
        content()
    }
}
