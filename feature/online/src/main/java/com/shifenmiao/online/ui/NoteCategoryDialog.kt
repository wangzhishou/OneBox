package com.shifenmiao.online.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.shifenmiao.database.item.entity.toModel
import com.shifenmiao.model.item.ItemDataUiState
import com.shifenmiao.online.component.CreateNoteComponent
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog

/**
 * 笔记分类浮动弹窗：选择分类 + 跳转分类管理页
 */
@Composable
fun NoteCategoryDialog(
    visible: Boolean,
    createNoteComponent: CreateNoteComponent,
    uiState: ItemDataUiState,
    onDismiss: () -> Unit
) {
    val categories by createNoteComponent.categories.collectAsState()
    val onNavigator = LocalOnNavigate.current
    val selectedCategoryIds = uiState.selectedCategories.map { it.id }.toSet()

    EnhancedAlertDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.note_select_category))
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismiss()
                                onNavigator(Screen.Reorderable(type = createNoteComponent.reorderableType))
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.manage),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    HorizontalDivider()
                }
                if (categories.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.note_category_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }
                items(categories, key = { it.id }) { category ->
                    val isSelected = category.id in selectedCategoryIds
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val updatedCategories = if (isSelected) {
                                    uiState.selectedCategories.filterNot { it.id == category.id }
                                } else {
                                    uiState.selectedCategories + category.toModel()
                                }
                                createNoteComponent.saveCategories(updatedCategories.toList())
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(
                                modifier = Modifier.size(18.dp),
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.done_button))
            }
        }
    )
}
