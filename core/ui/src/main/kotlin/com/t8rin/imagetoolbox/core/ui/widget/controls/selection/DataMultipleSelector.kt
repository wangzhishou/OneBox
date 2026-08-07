package com.t8rin.imagetoolbox.core.ui.widget.controls.selection


import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedChip
import com.t8rin.imagetoolbox.core.ui.widget.modifier.fadingEdges
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.system.OnePrimaryButton
import com.t8rin.imagetoolbox.core.ui.widget.system.OneSecondaryButton
import kotlinx.coroutines.delay
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.line.LineKeyboardArrowDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineNewLabel
import com.t8rin.imagetoolbox.core.resources.icons.line.LineNote

@Composable
fun <T : Any> DataMultipleSelector(
    value: List<T>,
    onValueChange: (List<T>) -> Unit,
    entries: List<T>,
    title: String,
    titleIcon: ImageVector?,
    itemContentText: @Composable (T) -> String,
    spanCount: Int = 2,
    modifier: Modifier = Modifier,
    badgeContent: (@Composable RowScope.() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    color: Color = MaterialTheme.colorScheme.surface,
    selectedItemColor: Color = MaterialTheme.colorScheme.tertiary,
    initialExpanded: Boolean = false,
    onNewItemChange: (String) -> Unit,
    onDeleteChange: (T) -> Unit = {},
    onEditItemChange: (T, String) -> Unit,
    showEdit: Boolean = true
) {
    val newSpanCount = spanCount.coerceAtLeast(1)
    var showNewItemDialog by rememberSaveable { mutableStateOf(false) }
    var showManageIcon by rememberSaveable { mutableStateOf(false) }
    val newItemText = remember { mutableStateOf("") }

    var showEditItemDialog by rememberSaveable { mutableStateOf(false) }
    val editItem = remember { mutableStateOf<T?>(value.firstOrNull()) }
    Column {
        var expanded by rememberSaveable(initialExpanded, newSpanCount) {
            mutableStateOf(
                initialExpanded && newSpanCount > 1
            )
        }
        val rotation by animateFloatAsState(if (expanded) 180f else 0f)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (newSpanCount > 1) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineKeyboardArrowDown,
                        contentDescription = "Expand",
                        modifier = Modifier
                            .size(14.dp)
                            .rotate(rotation)
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            if (showEdit) {
                Row(
                    modifier = Modifier
                        .wrapContentSize()
                        .clickable {
                            newItemText.value = ""
                            showNewItemDialog = true
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(12.dp),
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineNewLabel,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(com.t8rin.imagetoolbox.core.resources.R.string.create_new),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    modifier = Modifier
                        .wrapContentSize()
                        .clickable {
                            showManageIcon = !showManageIcon
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(12.dp),
                        imageVector = if (showManageIcon) {
                            com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close
                        } else {
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineNote
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (showManageIcon) {
                            stringResource(com.t8rin.imagetoolbox.core.resources.R.string.cancel)
                        } else {
                            stringResource(com.shifenmiao.core.R.string.manage)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        val state = rememberLazyStaggeredGridState()
        LaunchedEffect(value, entries) {
            delay(300)
            val targetIndex = entries.indexOfFirst { it in value }.takeIf { it >= 0 } ?: 0
            if (state.layoutInfo.visibleItemsInfo.all { it.index != targetIndex }) {
                state.scrollToItem(targetIndex)
            }
        }

        LazyHorizontalStaggeredGrid(
            verticalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.CenterVertically
            ),
            state = state,
            horizontalItemSpacing = 8.dp,
            rows = StaggeredGridCells.Adaptive(30.dp),
            modifier = Modifier
                .heightIn(
                    max = animateDpAsState(
                        if (expanded) {
                            54.dp * newSpanCount
                        } else 32.dp
                    ).value
                )
                .fadingEdges(
                    scrollableState = state,
                    isVertical = false,
                    spanCount = newSpanCount
                ),
        ) {
            items(entries) { entry ->
                val selected by remember(entry, value) {
                    derivedStateOf {
                        entry in value
                    }
                }
                val currentText = itemContentText(entry)
                FilterChip(
                    modifier = Modifier.height(32.dp),
                    selected = selected,
                    border = null,
                    onClick = {
                        val newValue = if (selected) {
                            value.toMutableList().apply { remove(entry) }
                        } else {
                            value.toMutableList().apply { add(entry) }
                        }
                        onValueChange(newValue)
                    },
                    label = {
                        Text(
                            text = currentText,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    leadingIcon = if (selected) {
                        {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(14.dp)
                            )
                        }
                    } else null,
                    trailingIcon = if (showManageIcon) {
                        {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable {
                                            editItem.value = entry
                                            showEditItemDialog = true
                                        }
                                )
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable {
                                            onDeleteChange(entry)
                                        }
                                )
                            }
                        }
                    } else null,

                    shape = shape,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTrailingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }

    if (showNewItemDialog) {
        newItemText.value = ""
        NewItemDialog(
            newItemText = newItemText,
            onDismiss = { showNewItemDialog = false },
            onConfirm = { newValue ->
                onNewItemChange(newValue)
                showNewItemDialog = false
            }
        )
    }
    if (showEditItemDialog && editItem.value != null) {
        newItemText.value = itemContentText(editItem.value!!)
        NewItemDialog(
            newItemText = newItemText,
            onDismiss = {
                showEditItemDialog = false
            },
            onConfirm = { newValue ->
                onEditItemChange(editItem.value!!, newValue)
                showEditItemDialog = false
            },
            isEdit = true
        )
    }
}

@Composable
fun NewItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    newItemText: MutableState<String>,
    isEdit: Boolean = false
) {
    EnhancedAlertDialog(
        visible = true,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isEdit) {
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineNote
                    } else {
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineNewLabel
                    },
                    contentDescription = null
                )
                Text(
                    text = if (isEdit) {
                        stringResource(com.t8rin.imagetoolbox.core.resources.R.string.edit)
                    } else {
                        stringResource(com.t8rin.imagetoolbox.core.resources.R.string.create_new)
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            OneBoxOutlinedTextField(
                value = newItemText.value,
                onValueChange = {
                    newItemText.value = it
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            OnePrimaryButton(
                text = stringResource(com.shifenmiao.core.R.string.button_confirm),
                onClick = {
                    onConfirm(newItemText.value)
                },
                enabled = newItemText.value.isNotBlank()
            )
        },
        dismissButton = {
            OneSecondaryButton(
                text = stringResource(com.shifenmiao.core.R.string.button_cancel),
                onClick = onDismiss
            )
        }
    )
}
