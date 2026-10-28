package com.shifenmiao.marktodo.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.marktodo.R
import com.shifenmiao.marktodo.model.TodoCategory
import com.shifenmiao.marktodo.model.TodoTag
import com.shifenmiao.marktodo.model.TodoTask
import com.shifenmiao.marktodo.theme.categoryAccentColor
import com.shifenmiao.marktodo.theme.priorityColor
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAccessTime
import com.t8rin.imagetoolbox.core.resources.icons.line.LineClear
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFlag
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolder
import com.wanbaohe.core.ui.widget.enhanced.EnhancedChip
import com.wanbaohe.core.ui.widget.glass.GlassFilterChip
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OneBoxOutlinedTextField
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PrioritySelector(
    priority: Int,
    onSelect: (Int) -> Unit,
) {
    val options = listOf(
        TodoTask.PRIORITY_HIGH to R.string.priority_high,
        TodoTask.PRIORITY_MEDIUM to R.string.priority_medium,
        TodoTask.PRIORITY_LOW to R.string.priority_low,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)) {
        options.forEach { (value, labelRes) ->
            val color = priorityColor(value)
            val selected = priority == value
            EnhancedChip(
                selected = selected,
                onClick = { onSelect(value) },
                selectedColor = MaterialTheme.colorScheme.primaryContainer,
                selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f),
                label = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LineFlag,
                            contentDescription = null,
                            tint = if (selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                color
                            },
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = stringResource(labelRes),
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSelector(
    tags: List<TodoTag>,
    selectedTagNames: Set<String>,
    onToggle: (String) -> Unit,
    onAddTag: (() -> Unit)? = null,
) {
    val poolNames = tags.map { it.name }.toSet()
    val extraNames = selectedTagNames.filter { it !in poolNames }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
    ) {
        tags.forEach { tag ->
            val selected = tag.name in selectedTagNames
            val tagColor = tag.colorArgb?.let { Color(it) }
            GlassFilterChip(
                selected = selected,
                onClick = { onToggle(tag.name) },
                label = {
                    Text(
                        text = tag.name,
                        color = when {
                            selected -> MaterialTheme.colorScheme.onPrimaryContainer
                            tagColor != null -> tagColor
                            else -> Color.Unspecified
                        },
                    )
                },
            )
        }
        extraNames.forEach { name ->
            GlassFilterChip(
                selected = true,
                onClick = { onToggle(name) },
                label = { Text(text = name) },
            )
        }
        if (onAddTag != null) {
            EnhancedChip(
                selected = false,
                onClick = onAddTag,
                selectedColor = Color.Unspecified,
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = stringResource(R.string.action_add_tag),
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = stringResource(R.string.action_add_tag),
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategorySelector(
    categories: List<TodoCategory>,
    selectedCategory: TodoCategory?,
    onSelect: (TodoCategory) -> Unit,
    onAddCategory: (() -> Unit)? = null,
    onSelectInbox: (() -> Unit)? = null,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
    ) {
        if (onSelectInbox != null) {
            val selected = selectedCategory == null
            EnhancedChip(
                selected = selected,
                onClick = onSelectInbox,
                selectedColor = MaterialTheme.colorScheme.primaryContainer,
                selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LineFolder,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = stringResource(R.string.quick_add_inbox),
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                },
            )
        }
        categories.forEach { category ->
            val selected = category.id == selectedCategory?.id
            val accent = categoryAccentColor(category)
            EnhancedChip(
                selected = selected,
                onClick = { onSelect(category) },
                selectedColor = MaterialTheme.colorScheme.primaryContainer,
                selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else accent,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = category.title,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                },
            )
        }
        if (onAddCategory != null) {
            EnhancedChip(
                selected = false,
                onClick = onAddCategory,
                selectedColor = Color.Unspecified,
                label = {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.action_add_category),
                        modifier = Modifier.size(16.dp),
                    )
                },
            )
        }
    }
}

@Composable
fun TaskTimeField(
    dueDateMillis: Long?,
    hasDueTime: Boolean,
    onSelectTime: () -> Unit,
    onClearTime: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val time = if (hasDueTime && dueDateMillis != null) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(dueDateMillis)
    } else {
        ""
    }
    OneBoxOutlinedTextField(
        value = time,
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        placeholder = { Text(stringResource(R.string.select_exact_time_hint)) },
        trailingIcon = {
            Row {
                IconButton(modifier = Modifier.size(48.dp), onClick = onSelectTime) {
                    Icon(
                        imageVector = Icons.Outlined.LineAccessTime,
                        contentDescription = stringResource(R.string.action_select_time),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                if (hasDueTime) {
                    IconButton(modifier = Modifier.size(48.dp), onClick = onClearTime) {
                        Icon(
                            imageVector = Icons.Outlined.LineClear,
                            contentDescription = stringResource(R.string.action_clear_time),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        modifier = modifier,
    )
}
