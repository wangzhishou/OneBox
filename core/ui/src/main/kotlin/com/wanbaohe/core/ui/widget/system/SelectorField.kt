package com.wanbaohe.core.ui.widget.system

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.line.LineKeyboardArrowDown
import com.wanbaohe.core.ui.widget.glass.GlassSurface

@Composable
fun <T> SelectorField(
    label: String,
    selected: T,
    options: List<T>,
    optionLabel: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    menuFooter: (@Composable ColumnScope.(dismiss: () -> Unit) -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val canExpand = options.size > 1 || menuFooter != null
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth(),
        ) {
            GlassSurface(
                onClick = { expanded = !expanded },
                enabled = canExpand,
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = false)
                    .fillMaxWidth(),
                style = OneBoxDesignSystem.rowGlassStyle,
                shape = OneBoxDesignSystem.listRowShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                borderWidth = 0.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 52.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = optionLabel(selected),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (canExpand) {
                        Icon(
                            imageVector = Icons.Outlined.LineKeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
            if (canExpand) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    shape = MaterialTheme.shapes.large,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ) {
                    options.forEach { option ->
                        val isSelected = option == selected
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = optionLabel(option),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                )
                            },
                            trailingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                            } else {
                                null
                            },
                            onClick = {
                                expanded = false
                                if (!isSelected) onSelect(option)
                            },
                        )
                    }
                    menuFooter?.invoke(this) { expanded = false }
                }
            }
        }
    }
}
