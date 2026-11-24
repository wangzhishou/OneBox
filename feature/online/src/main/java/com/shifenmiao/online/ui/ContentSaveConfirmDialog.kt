/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shifenmiao.online.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.R as CommonR
import com.shifenmiao.common.components.category.AddCategoryDialog
import com.shifenmiao.core.R as CoreR
import com.shifenmiao.database.item.entity.Category
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRobot
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.glass.GlassAssistChip
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassTextButton
import com.wanbaohe.core.ui.widget.system.OnePrimaryButton
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton

/**
 * 笔记和提示词共用的保存确认表单；AI 生成由调用方按需接入。
 */
@Composable
internal fun ContentSaveConfirmDialog(
    visible: Boolean,
    dialogTitle: String,
    dialogIcon: ImageVector,
    title: String,
    description: String,
    categories: List<Category>,
    selectedCategoryIds: Set<Int>,
    isSaving: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onToggleCategory: (Category) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onCreateCategory: ((String) -> Unit)? = null,
    onManageCategories: (() -> Unit)? = null,
    isGenerating: Boolean = false,
    onGenerate: (() -> Unit)? = null,
) {
    var showAddCategoryDialog by remember(visible) { mutableStateOf(false) }

    EnhancedAlertDialog(
        visible = visible,
        onDismissRequest = {
            if (!isSaving) onDismiss()
        },
        icon = {
            Icon(
                imageVector = dialogIcon,
                contentDescription = null,
            )
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(CommonR.string.note_save_confirm_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                GlassOutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                    label = { Text(stringResource(CoreR.string.create_ai_common_title_label)) },
                    singleLine = true,
                    maxLines = 1,
                    style = GlassStyle.Medium,
                )
                GlassOutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    label = { Text(stringResource(CommonR.string.note_summary_label)) },
                    singleLine = false,
                    minLines = 3,
                    maxLines = 5,
                    style = GlassStyle.Medium,
                )
                if (onGenerate != null) Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    // AI 生成：共用 AssistChip + 机器人图标，和 App 内其他 AI 入口一致。
                    GlassAssistChip(
                        onClick = onGenerate,
                        enabled = !isGenerating && !isSaving,
                        style = GlassStyle.Thin,
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        glassContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        leadingIcon = {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(15.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.LineRobot,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        },
                        label = {
                            Text(
                                text = stringResource(
                                    if (isGenerating) {
                                        CommonR.string.note_ai_generating
                                    } else {
                                        CommonR.string.note_ai_generate
                                    },
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                            )
                        },
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(CoreR.string.note_select_category),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (onManageCategories != null) {
                        // 分类管理从 ⋮ 菜单移到分类标题右侧，和「新增分类」一起都在这个弹窗里。
                        GlassTextButton(
                            onClick = onManageCategories,
                            enabled = !isSaving,
                            style = GlassStyle.Thin,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LineSettings,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(CoreR.string.manage),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
                if (categories.isEmpty() && onCreateCategory == null) {
                    Text(
                        text = stringResource(CommonR.string.note_category_empty_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                FlexBox(
                    config = {
                        wrap(FlexWrap.Wrap)
                        columnGap(8.dp)
                        rowGap(6.dp)
                    },
                ) {
                    if (onCreateCategory != null) {
                        NoteAddCategoryChip(onClick = { if (!isSaving) showAddCategoryDialog = true })
                    }
                    // 最新更新的分类排最前，与展示/编辑页的分类栏顺序一致。
                    categories
                        .sortedWith(compareByDescending<Category> { it.updatedAt }.thenBy { it.name })
                        .forEach { category ->
                            NoteCategoryChip(
                                name = category.name,
                                selected = category.id in selectedCategoryIds,
                                onClick = { if (!isSaving) onToggleCategory(category) },
                            )
                        }
                }
            }
        },
        confirmButton = {
            // 按钮统一走共用胶囊按钮：确认是实心玻璃按钮，取消是次按钮。
            OnePrimaryButton(
                text = stringResource(CommonR.string.common_save),
                leadingIcon = Icons.Outlined.LineSave,
                enabled = !isSaving && title.isNotBlank(),
                onClick = {
                    if (!isSaving) onConfirm()
                },
            )
        },
        dismissButton = {
            OneSecondaryButton(
                text = stringResource(CommonR.string.common_cancel),
                enabled = !isSaving,
                onClick = onDismiss,
            )
        },
    )

    if (showAddCategoryDialog && onCreateCategory != null) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name ->
                onCreateCategory(name)
                showAddCategoryDialog = false
            },
        )
    }
}
