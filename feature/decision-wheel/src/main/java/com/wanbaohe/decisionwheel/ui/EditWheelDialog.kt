package com.wanbaohe.decisionwheel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.com.color.ColorGenerator
import com.wanbaohe.decisionwheel.R
import com.wanbaohe.decisionwheel.component.WheelOption
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave

/**
 * 编辑转盘选项对话框。
 *
 * 注意：配色方案入口在主界面（编辑按钮旁）统一管理；编辑弹窗仅负责标题/选项名称的编辑。
 */
@Composable
fun EditWheelDialog(
    title: String,
    options: List<WheelOption>,
    onDismiss: () -> Unit,
    onSave: (String, List<WheelOption>) -> Unit,
    onAddOption: (String, Color) -> Unit = { _, _ -> },
    onDeleteOption: (String) -> Unit = {},
    visible: Boolean = false
) {
    var editedTitle by remember { mutableStateOf(title) }
    var editedOptions by remember { mutableStateOf(options) }
    var showAddDialog by remember { mutableStateOf(false) }
    var pendingDeleteOption by remember { mutableStateOf<WheelOption?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val deleteMinTwoTip = stringResource(R.string.delete_option_min_two_tip)

    // Reset state when visible becomes true with new data
    LaunchedEffect(visible, title, options) {
        if (visible) {
            editedTitle = title
            editedOptions = options
        }
    }

    // Capture a composable-safe fallback color once, so helper functions can remain non-composable.
    val fallbackBaseColor = MaterialTheme.colorScheme.primary

    /**
     * Keep existing colors stable.
     *
     * Strategy:
     * - Preserve every option that already has a concrete color.
     * - Only fill colors for options whose color is [Color.Unspecified] (typically newly added).
     * - The fill colors are generated from the current base→complement mix palette.
     */
    fun assignColorsForNewOptionsOnly() {
        val current = editedOptions
        if (current.isEmpty()) return

        // Determine base color.
        // Prefer the first existing concrete color; fallback to the original wheel's first color.
        val baseColor = current.firstOrNull { it.color != Color.Unspecified }?.color
            ?: options.firstOrNull()?.color?.takeIf { it != Color.Unspecified }
            ?: fallbackBaseColor

        // Generate palette for ALL options
        val palette = ColorGenerator
            .generateSegmentBackgrounds(baseColor = baseColor, count = current.size)

        // Collect already used colors
        val usedColors = current
            .mapNotNull { it.color.takeIf { c -> c != Color.Unspecified } }
            .toSet()

        // Create a queue of available colors (not yet used)
        val availableColors = palette.filterNot { color ->
            usedColors.any { used ->
                // Consider colors "equal" if they're very close
                kotlin.math.abs(color.red - used.red) < 0.01f &&
                        kotlin.math.abs(color.green - used.green) < 0.01f &&
                        kotlin.math.abs(color.blue - used.blue) < 0.01f
            }
        }.toMutableList()

        editedOptions = current.map { option ->
            if (option.color != Color.Unspecified) {
                // Keep existing color
                option
            } else {
                // Assign a new color from available colors
                val newColor = if (availableColors.isNotEmpty()) {
                    availableColors.removeAt(0)
                } else {
                    // Fallback: use base color if no colors available
                    baseColor
                }
                option.copy(color = newColor)
            }
        }
    }

    

    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceNormal)
        ) {
            // 标题栏 + 保存按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = AppTheme.dimens.paddingNormal,
                        top = AppTheme.dimens.paddingSmall,
                        end = AppTheme.dimens.paddingNormal
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.edit_wheel),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )


            }

            Column(
                modifier = Modifier
                    .padding(horizontal = AppTheme.dimens.paddingNormal)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceNormal)
            ) {
                // 转盘标题输入
                OutlinedTextField(
                    value = editedTitle,
                    onValueChange = { editedTitle = it },
                    label = { Text(stringResource(R.string.wheel_title)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = AppTheme.shapes.getMediumShape(),
                    colors = AppTheme.colors.getOutlinedTextFieldColors()
                )

                // Options header
                Text(
                    text = stringResource(R.string.options_list_count, editedOptions.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Options list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(editedOptions) { _, option ->
                        val canDelete = editedOptions.size > 2
                        OptionEditItem(
                            option = option,
                            previewColor = null,
                            canDelete = canDelete,
                            onDelete = {
                                if (canDelete) {
                                    pendingDeleteOption = option
                                } else {
                                    coroutineScope.launch {
                                        AppToastHost.showToast(
                                            message = deleteMinTwoTip
                                        )
                                    }
                                }
                            },
                            onEdit = { updatedOption ->
                                editedOptions = editedOptions.map {
                                    if (it.id == option.id) updatedOption else it
                                }
                            }
                        )
                    }
                }

                // Add button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    FilledTonalButton(
                        onClick = { showAddDialog = true },
                        colors = AppTheme.colors.getSecondaryContainerButtonColors(),
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                            contentDescription = stringResource(R.string.add_option)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.add_option))
                    }

                    FilledTonalButton(
                        onClick = {
                            if (editedTitle.isNotBlank() && editedOptions.size >= 2) {
                                assignColorsForNewOptionsOnly()
                                onSave(editedTitle, editedOptions)
                            }
                        },
                        enabled = editedTitle.isNotBlank() && editedOptions.size >= 2,
                        colors = AppTheme.colors.getPrimaryButtonColors(),
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave,
                            contentDescription = stringResource(R.string.save)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.save))
                    }
                }

                Spacer(modifier = Modifier.height(AppTheme.dimens.paddingSmall))
            }
        }
    }

    // Delete confirm dialog
    pendingDeleteOption?.let { option ->
        AlertDialog(
            containerColor = AppTheme.colors.getContainerSurfaceColor(),
            onDismissRequest = { pendingDeleteOption = null },
            title = {
                Text(
                    text = stringResource(R.string.delete_option_confirm),
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(option.name) },
            confirmButton = {
                GlassTonalButton(
                    onClick = {
                        // Update local UI immediately and persist via callback.
                        editedOptions = editedOptions.filter { it.id != option.id }
                        onDeleteOption(option.id)
                        pendingDeleteOption = null
                    },
                    colors = AppTheme.colors.getSecondaryContainerButtonColors(),
                    modifier = Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                FilledTonalButton(
                    onClick = { pendingDeleteOption = null },
                    colors = AppTheme.colors.getSurfaceContainerButtonColors(),
                    modifier = Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Add option dialog (no direct color selection)
    if (showAddDialog) {
        AddOptionDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name ->
                // Create new option with unspecified color
                val newOption = WheelOption(
                    name = name,
                    color = Color.Unspecified
                )

                // Add to local state
                editedOptions = editedOptions + newOption

                // Assign color for the new option
                assignColorsForNewOptionsOnly()

                // Get the newly assigned color and sync to database immediately
                val addedOption = editedOptions.lastOrNull()
                if (addedOption != null && addedOption.name == name) {
                    onAddOption(addedOption.name, addedOption.color)
                }

                showAddDialog = false
            }
        )
    }
}

/**
 * 选项编辑项
 */
@Composable
fun OptionEditItem(
    option: WheelOption,
    previewColor: Color?,
    canDelete: Boolean,
    onDelete: () -> Unit,
    onEdit: (WheelOption) -> Unit = {}
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showEditDialog = true },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                val dotColor = previewColor
                    ?: option.color.takeIf { it != Color.Unspecified }
                    ?: MaterialTheme.colorScheme.primary

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )

                Text(
                    text = option.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            // Keep it tappable even when "disabled" so we can show a reason message.
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = if (canDelete) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showEditDialog) {
        EditOptionDialog(
            option = option,
            onDismiss = { showEditDialog = false },
            onSave = { updatedOption ->
                onEdit(updatedOption)
                showEditDialog = false
            }
        )
    }
}

/**
 * 添加选项对话框（仅编辑名称，不再单独选色）。
 */
@Composable
fun AddOptionDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var optionName by remember { mutableStateOf("") }

    AlertDialog(
        containerColor = AppTheme.colors.getContainerSurfaceColor(),
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.add_new_option),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = optionName,
                onValueChange = { optionName = it },
                label = { Text(stringResource(R.string.option_name)) },
                placeholder = { Text(stringResource(R.string.option_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = AppTheme.shapes.getMediumShape(),
                colors = AppTheme.colors.getOutlinedTextFieldColors()
            )
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    if (optionName.isNotBlank()) {
                        onAdd(optionName)
                    }
                },
                enabled = optionName.isNotBlank(),
                colors = AppTheme.colors.getPrimaryButtonColors(),
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            FilledTonalButton(
                onClick = onDismiss,
                colors = AppTheme.colors.getSurfaceContainerButtonColors(),
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

/**
 * 编辑选项对话框（仅编辑名称，不再单独选色）。
 */
@Composable
fun EditOptionDialog(
    option: WheelOption,
    onDismiss: () -> Unit,
    onSave: (WheelOption) -> Unit
) {
    var optionName by remember { mutableStateOf(option.name) }

    AlertDialog(
        containerColor = AppTheme.colors.getContainerSurfaceColor(),
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.edit_option),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = optionName,
                onValueChange = { optionName = it },
                label = { Text(stringResource(R.string.option_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = AppTheme.shapes.getMediumShape(),
                colors = AppTheme.colors.getOutlinedTextFieldColors()
            )
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    if (optionName.isNotBlank()) {
                        onSave(option.copy(name = optionName))
                    }
                },
                enabled = optionName.isNotBlank(),
                colors = AppTheme.colors.getPrimaryButtonColors(),
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            FilledTonalButton(
                onClick = onDismiss,
                colors = AppTheme.colors.getSurfaceContainerButtonColors(),
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
