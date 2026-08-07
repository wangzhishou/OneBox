package com.shifenmiao.ai.ui

import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.button.CancelButton
import com.shifenmiao.base.ui.button.ConfirmButton
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.core.R
import com.shifenmiao.core.constants.Constants.AI_MODEL_DEFAULT_TITLE
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.CheckCircle
import com.t8rin.imagetoolbox.core.resources.icons.Edit

@Composable
fun AiModelTabView(
    aiModels: List<AiModel>,
    selectedAiModel: AiModel,
    onModelChange: (AiModel) -> Unit,
    onModelEdit: (AiModel) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var modelToEdit by remember { mutableStateOf<AiModel?>(null) }

    // 将 aiModels 分组，每组 3 个元素
    val chunkedModels: List<List<AiModel>> = aiModels.chunked(3)

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chunkedModels.forEach { rowModels ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    8.dp,
                    Alignment.CenterHorizontally
                )
            ) {
                rowModels.forEach { aiModel ->
                    val isCustomModel = aiModel.canEdit
                    val isSelected = aiModel.id == selectedAiModel.id
                    val contentColor = if (isSelected) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    }
                    val displayText = if (isCustomModel) {
                        if (aiModel.title.trim()
                                .isNotEmpty()
                        ) aiModel.title else AI_MODEL_DEFAULT_TITLE
                    } else {
                        aiModel.title
                    }

                    // 每个元素占用 1/3 的宽度
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        BadgedBox(
                            badge = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.CheckCircle,
                                        contentDescription = "Icon",
                                        modifier = Modifier
                                            .size(14.dp)
                                            .padding(end = AppTheme.dimens.paddingTooSmall),
                                        tint = contentColor
                                    )
                                }
                            }
                        ) {
                            TextButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    onModelChange(aiModel)
                                },
                                colors = ButtonDefaults.textButtonColors().copy(
                                    contentColor = contentColor,
                                    containerColor = containerColor
                                ),
                                shape = MaterialTheme.shapes.extraSmall
                            ) {
                                Text(
                                    text = displayText,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = if (isSelected) {
                                        Modifier.basicMarquee(
                                            iterations = Int.MAX_VALUE,
                                            spacing = MarqueeSpacing(30.dp),
                                            velocity = 30.dp,
                                            repeatDelayMillis = 1000
                                        )
                                    } else {
                                        Modifier
                                    },
                                    maxLines = 1,
                                    overflow = if (!isSelected) TextOverflow.Ellipsis else TextOverflow.Clip,
                                    softWrap = false
                                )
                                if (isCustomModel && isSelected) {
                                    Spacer(modifier = Modifier.size(4.dp))
                                    Icon(
                                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                                        contentDescription = "Edit model",
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                modelToEdit = aiModel
                                                showEditDialog = true
                                            },
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // 如果这一行不足 3 个元素，填充空白占位
                if (rowModels.size < 3) {
                    repeat(3 - rowModels.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    if (showEditDialog && modelToEdit != null) {
        CustomModelEditDialog(
            model = modelToEdit!!,
            onDismiss = { showEditDialog = false },
            onSave = { updatedModel ->
                onModelEdit(updatedModel)
                onModelChange(updatedModel)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun CustomModelEditDialog(
    model: AiModel,
    onDismiss: () -> Unit,
    onSave: (AiModel) -> Unit
) {
    var nameInput by remember { mutableStateOf(model.name) }
    var titleInput by remember { mutableStateOf(model.title) }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = AppTheme.shapes.getLargeShape(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        title = {
            Text(
                text = stringResource(R.string.edit_custom_model),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = {
                        Text(text = stringResource(R.string.edit_custom_name))
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppTheme.shapes.getTextFieldShape(),
                    colors = AppTheme.colors.getOutlinedTextFieldColors()
                )
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = {
                        Text(text = stringResource(R.string.edit_custom_title))
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppTheme.shapes.getTextFieldShape(),
                    colors = AppTheme.colors.getOutlinedTextFieldColors()
                )
            }
        },
        confirmButton = {
            ConfirmButton {
                if (nameInput.trim().isEmpty() || titleInput.trim().isEmpty()) {
                    ActionUtils.showError(R.string.required_field)
                    return@ConfirmButton
                }
                onSave(model.copy(name = nameInput, title = titleInput))
            }
        },
        dismissButton = {
            CancelButton {
                onDismiss()
            }
        }
    )
}