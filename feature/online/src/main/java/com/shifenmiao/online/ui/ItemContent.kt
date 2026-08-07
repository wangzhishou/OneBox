package com.shifenmiao.online.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.shifenmiao.database.item.entity.Category
import com.shifenmiao.database.item.entity.toModel
import com.shifenmiao.model.item.ItemDataUiState
import com.shifenmiao.model.reorderable.ReorderableType
import com.shifenmiao.online.component.CreateHtmlComponent
import com.shifenmiao.online.component.CreateHtmlUiState
import com.shifenmiao.online.component.CreateNoteComponent
import com.shifenmiao.online.component.EditPromptComponent
import com.shifenmiao.online.component.EditPromptUiState
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassFilterChip
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxOutlinedTextField
import kotlinx.coroutines.flow.StateFlow
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings
import com.t8rin.imagetoolbox.core.resources.icons.Check

private val EditorCategoryChipShape = RoundedCornerShape(16.dp)
private val EditorCategoryChipPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)

@Composable
private fun EditorCategoryChipLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium
    )
}

// ===================== CategorySelection for CreateNoteComponent =====================

@Composable
fun NoteCategorySelection(
    modifier: Modifier = Modifier.padding(horizontal = 14.dp, vertical = 0.dp),
    createNoteComponent: CreateNoteComponent,
    uiState: ItemDataUiState
) {
    val categoriesState = createNoteComponent.categories.collectAsState()
    val onNavigator = LocalOnNavigate.current
    val listState = rememberLazyListState()
    val selectedCategoryIds = uiState.selectedCategories.map { it.id }.toSet()

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        state = listState
    ) {
        item {
            GlassFilterChip(
                selected = false,
                onClick = {
                    onNavigator(Screen.Reorderable(type = createNoteComponent.reorderableType))
                },
                leadingIcon = {
                    Icon(
                        modifier = Modifier.size(14.dp),
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettings,
                        contentDescription = null
                    )
                },
                label = {
                    EditorCategoryChipLabel(text = stringResource(R.string.manage))
                },
                shape = EditorCategoryChipShape,
                contentPadding = EditorCategoryChipPadding,
                colors = AppTheme.colors.getFilterChipColors(),
                border = null
            )
        }
        items(categoriesState.value) { category ->
            val isSelected = category.id in selectedCategoryIds
            GlassFilterChip(
                selected = isSelected,
                onClick = {
                    val updatedCategories = if (isSelected) {
                        uiState.selectedCategories.filterNot { it.id == category.id }
                    } else {
                        uiState.selectedCategories + category.toModel()
                    }
                    createNoteComponent.saveCategories(updatedCategories.toList())
                },
                leadingIcon = {
                    if (isSelected) {
                        Icon(
                            modifier = Modifier.size(14.dp),
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                            contentDescription = null
                        )
                    }
                },
                label = {
                    EditorCategoryChipLabel(text = category.name)
                },
                shape = EditorCategoryChipShape,
                contentPadding = EditorCategoryChipPadding,
                colors = AppTheme.colors.getFilterChipColors(),
                border = null
            )
        }


    }
}

@Composable
fun ReadOnlyNoteCategorySelection(
    modifier: Modifier = Modifier,
    uiState: ItemDataUiState
) {
    if (uiState.selectedCategories.isEmpty()) return

    val listState = rememberLazyListState()

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        state = listState
    ) {
        items(uiState.selectedCategories.toList(), key = { it.id }) { category ->
            GlassFilterChip(
                selected = true,
                onClick = {},
                label = {
                    EditorCategoryChipLabel(text = category.name.orEmpty())
                },
                shape = EditorCategoryChipShape,
                contentPadding = EditorCategoryChipPadding,
                colors = AppTheme.colors.getFilterChipColors(),
                border = null
            )
        }
    }
}

// ===================== CategorySelection for CreateHtmlComponent =====================

@Composable
fun HtmlCategorySelection(
    modifier: Modifier = Modifier,
    createHtmlComponent: CreateHtmlComponent,
    uiState: CreateHtmlUiState
) {
    DraftCategorySelection(
        modifier = modifier,
        categories = createHtmlComponent.categories,
        selectedCategories = uiState.selectedCategories,
        reorderableType = createHtmlComponent.reorderableType,
        onSaveCategories = { createHtmlComponent.saveCategories(it) },
    )
}

// ===================== CategorySelection for EditPromptComponent =====================

@Composable
fun PromptCategorySelection(
    modifier: Modifier = Modifier,
    editPromptComponent: EditPromptComponent,
    uiState: EditPromptUiState
) {
    DraftCategorySelection(
        modifier = modifier,
        categories = editPromptComponent.categories,
        selectedCategories = uiState.selectedCategories,
        reorderableType = editPromptComponent.reorderableType,
        onSaveCategories = { editPromptComponent.saveCategories(it) },
    )
}

// ===================== Unified category selection: chips + trailing "manage" chip =====================

@Composable
fun DraftCategorySelection(
    modifier: Modifier = Modifier,
    categories: StateFlow<List<Category>>,
    selectedCategories: Set<Category>,
    reorderableType: ReorderableType,
    onSaveCategories: (List<Category>) -> Unit,
) {
    val categoriesState = categories.collectAsState()
    val onNavigator = LocalOnNavigate.current
    val listState = rememberLazyListState()

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        state = listState,
    ) {
        items(categoriesState.value) { category ->
            val isSelected = selectedCategories.contains(category)
            GlassFilterChip(
                selected = isSelected,
                onClick = {
                    val updatedCategories = if (isSelected) {
                        selectedCategories - category
                    } else {
                        selectedCategories + category
                    }
                    onSaveCategories(updatedCategories.toList())
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            modifier = Modifier.size(14.dp),
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                            contentDescription = null,
                        )
                    }
                } else null,
                label = {
                    EditorCategoryChipLabel(text = category.name)
                },
                shape = EditorCategoryChipShape,
                contentPadding = EditorCategoryChipPadding,
                colors = AppTheme.colors.getFilterChipColors(),
                border = null,
            )
        }

        // Trailing "manage" chip — navigates to category management
        item {
            GlassFilterChip(
                selected = false,
                onClick = {
                    onNavigator(Screen.Reorderable(type = reorderableType))
                },
                leadingIcon = {
                    Icon(
                        modifier = Modifier.size(14.dp),
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettings,
                        contentDescription = null,
                    )
                },
                label = {
                    EditorCategoryChipLabel(text = stringResource(R.string.creation_meta_add_category))
                },
                shape = EditorCategoryChipShape,
                contentPadding = EditorCategoryChipPadding,
                colors = AppTheme.colors.getFilterChipColors(),
                border = null,
            )
        }
    }
}

@Composable
fun ItemTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 10,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
) {
    OneBoxOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = label,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines.coerceAtMost(20),
        trailingIcon = trailingIcon,
        leadingIcon = leadingIcon,
        supportingText = supportingText,
        isError = isError,
        placeholder = placeholder
    )
}
