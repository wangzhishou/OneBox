package com.shifenmiao.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.shifenmiao.database.item.entity.Category
import com.shifenmiao.model.ai.tool.ToolCatalogItem
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.controls.selection.NewItemDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSegmentedButtonRow
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Check

private enum class CreationConfigTab {
    Categories,
    Tools,
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreationMetaConfigSheet(
    title: String,
    categories: List<Category>,
    selectedCategoryIds: Set<Int>,
    availableTools: List<ToolCatalogItem>,
    selectedToolNames: Set<String>,
    systemToolNames: Set<String> = emptySet(),
    categoryHint: String,
    toolTitle: String,
    toolEmptyHint: String,
    toolSelectedHint: (Int) -> String,
    confirmText: String,
    onDismiss: () -> Unit,
    showCategorySection: Boolean = true,
    onAddCategory: (String) -> Unit,
    onConfirm: (Set<Int>, Set<String>) -> Unit,
) {
    var localSelectedCategoryIds by remember(selectedCategoryIds) {
        mutableStateOf(selectedCategoryIds)
    }
    var localSelectedToolNames by remember(selectedToolNames) {
        mutableStateOf(selectedToolNames)
    }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var toolSearchQuery by rememberSaveable { mutableStateOf("") }
    var selectedToolCategory by rememberSaveable { mutableStateOf<ToolCategory?>(null) }
    var selectedTabName by rememberSaveable(showCategorySection) {
        mutableStateOf(
            if (showCategorySection) {
                CreationConfigTab.Categories.name
            } else {
                CreationConfigTab.Tools.name
            }
        )
    }
    val newCategoryName = remember { mutableStateOf("") }
    val selectedTab = remember(selectedTabName, showCategorySection) {
        runCatching { CreationConfigTab.valueOf(selectedTabName) }
            .getOrElse { if (showCategorySection) CreationConfigTab.Categories else CreationConfigTab.Tools }
    }
    val toolCategories = remember(availableTools) { availableTools.map { it.category }.distinct() }
    val selectedCategories = remember(categories, localSelectedCategoryIds) {
        categories.filter { it.id in localSelectedCategoryIds }
    }
    val selectedTools = remember(availableTools, localSelectedToolNames) {
        availableTools
            .filter { it.name in localSelectedToolNames }
            .sortedWith(compareBy<ToolCatalogItem> { it.sortOrder }.thenBy { it.title })
    }
    val filteredTools = remember(availableTools, toolSearchQuery, selectedToolCategory, localSelectedToolNames) {
        availableTools.filter { tool ->
            val matchesCategory = selectedToolCategory == null || tool.category == selectedToolCategory
            val matchesQuery = toolSearchQuery.isBlank() ||
                tool.title.contains(toolSearchQuery, ignoreCase = true) ||
                tool.summary.contains(toolSearchQuery, ignoreCase = true) ||
                tool.name.contains(toolSearchQuery, ignoreCase = true) ||
                tool.keywords.any { it.contains(toolSearchQuery, ignoreCase = true) }
            matchesCategory && matchesQuery
        }.sortedWith(
            compareByDescending<ToolCatalogItem> { it.name in localSelectedToolNames }
                .thenBy { if (it.name in systemToolNames) 0 else 1 }
                .thenBy { it.sortOrder }
                .thenBy { it.title }
        )
    }
    val filteredToolNames = remember(filteredTools) { filteredTools.map { it.name } }
    val deselectableToolNames = remember(filteredTools, systemToolNames) {
        filteredTools.map { it.name }.filterNot { it in systemToolNames }
    }
    val hasSystemToolsInCurrentResult = remember(filteredTools, systemToolNames) {
        filteredTools.any { it.name in systemToolNames }
    }
    val canEnableAll = filteredToolNames.any { it !in localSelectedToolNames }
    val canDisableAll = deselectableToolNames.any { it in localSelectedToolNames }
    val sectionTitle = when {
        toolSearchQuery.isNotBlank() -> stringResource(R.string.ai_tools_filtered)
        selectedToolCategory != null -> selectedToolCategory!!.displayName()
        else -> stringResource(R.string.ai_tools_all)
    }
    val selectAllLabel = when {
        toolSearchQuery.isNotBlank() -> stringResource(R.string.ai_tools_select_filtered)
        selectedToolCategory != null -> stringResource(R.string.ai_tools_select_group)
        else -> stringResource(R.string.ai_tools_select_all)
    }
    val deselectAllLabel = when {
        hasSystemToolsInCurrentResult && toolSearchQuery.isNotBlank() -> stringResource(R.string.ai_tools_deselect_all_regular)
        hasSystemToolsInCurrentResult && selectedToolCategory != null -> stringResource(R.string.ai_tools_deselect_all_regular)
        hasSystemToolsInCurrentResult -> stringResource(R.string.ai_tools_deselect_all_regular)
        toolSearchQuery.isNotBlank() -> stringResource(R.string.ai_tools_deselect_filtered)
        selectedToolCategory != null -> stringResource(R.string.ai_tools_deselect_group)
        else -> stringResource(R.string.ai_tools_deselect_all)
    }
    val systemTools = remember(availableTools, systemToolNames) {
        availableTools.filter { it.name in systemToolNames }
            .sortedWith(compareBy<ToolCatalogItem> { it.sortOrder }.thenBy { it.title })
    }
    val filteredSystemTools = remember(filteredTools, systemToolNames) {
        filteredTools.filter { it.name in systemToolNames }
    }
    val filteredNormalTools = remember(filteredTools, systemToolNames) {
        filteredTools.filterNot { it.name in systemToolNames }
    }
    val disabledSystemToolTitles = remember(localSelectedToolNames, systemTools) {
        systemTools.filterNot { it.name in localSelectedToolNames }.map { it.title }
    }
    val selectedFilteredTools = remember(filteredTools, localSelectedToolNames) {
        filteredTools.filter { it.name in localSelectedToolNames }
    }
    val unselectedFilteredTools = remember(filteredTools, localSelectedToolNames) {
        filteredTools.filterNot { it.name in localSelectedToolNames }
    }

    fun applyToolToggle(toolNames: List<String>, enable: Boolean) {
        if (toolNames.isEmpty()) return
        localSelectedToolNames = if (enable) {
            localSelectedToolNames + toolNames
        } else {
            localSelectedToolNames - toolNames.toSet()
        }
    }

    fun handleToggle(tool: ToolCatalogItem, enable: Boolean) {
        applyToolToggle(listOf(tool.name), enable)
    }

    fun handleBulkToggle(toolNames: List<String>, enable: Boolean) {
        if (toolNames.isEmpty()) return
        applyToolToggle(toolNames, enable)
    }

    EnhancedModalBottomSheet(
        nestedScrollEnabled = false,
        visible = true,
        onDismiss = { if (!it) onDismiss() },
        endConfirmButtonPadding = 0.dp,
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    onConfirm(localSelectedCategoryIds, localSelectedToolNames)
                },
                colors = AppTheme.colors.filledTonalButtonColors(),
            ) {
                Icon(
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(confirmText)
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
            ) {
                ConfigSelectionOverviewCard(
                    showCategorySection = showCategorySection,
                    selectedCategories = selectedCategories,
                    selectedTools = selectedTools,
                    toolEmptyHint = toolEmptyHint,
                    onOpenCategories = {
                        selectedTabName = CreationConfigTab.Categories.name
                    },
                    onOpenTools = {
                        selectedTabName = CreationConfigTab.Tools.name
                    },
                )

                if (showCategorySection) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CreationConfigTabRow(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTabName = it.name },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (showCategorySection) {
                    if (selectedTab == CreationConfigTab.Categories) {
                        CreationCategorySelectionSection(
                            categoryHint = categoryHint,
                            categories = categories,
                            selectedCategoryIds = localSelectedCategoryIds,
                            onToggleCategory = { categoryId, isSelected ->
                                localSelectedCategoryIds = if (isSelected) {
                                    localSelectedCategoryIds - categoryId
                                } else {
                                    localSelectedCategoryIds + categoryId
                                }
                            },
                            onAddCategoryClick = { showAddDialog = true },
                        )
                    } else {
                        CreationToolsPanel(
                            toolTitle = toolTitle,
                            toolEmptyHint = toolEmptyHint,
                            toolSelectedHint = toolSelectedHint,
                            selectedToolNames = localSelectedToolNames,
                            searchQuery = toolSearchQuery,
                            onSearchQueryChange = { toolSearchQuery = it },
                            selectedCategory = selectedToolCategory,
                            categories = toolCategories,
                            onCategorySelected = { selectedToolCategory = it },
                            sectionTitle = sectionTitle,
                            selectAllLabel = selectAllLabel,
                            deselectAllLabel = deselectAllLabel,
                            canEnableAll = canEnableAll,
                            canDisableAll = canDisableAll,
                            filteredTools = filteredTools,
                            selectedFilteredTools = selectedFilteredTools,
                            unselectedFilteredTools = unselectedFilteredTools,
                            systemToolNames = systemToolNames,
                            disabledSystemToolTitles = disabledSystemToolTitles,
                            filteredSystemTools = filteredSystemTools,
                            filteredNormalTools = filteredNormalTools,
                            onSelectAll = { handleBulkToggle(filteredToolNames, true) },
                            onDeselectAll = { handleBulkToggle(deselectableToolNames, false) },
                            onToggleTool = { tool, selected -> handleToggle(tool, selected) }
                        )
                    }
                } else {
                    CreationToolsPanel(
                        toolTitle = toolTitle,
                        toolEmptyHint = toolEmptyHint,
                        toolSelectedHint = toolSelectedHint,
                        selectedToolNames = localSelectedToolNames,
                        searchQuery = toolSearchQuery,
                        onSearchQueryChange = { toolSearchQuery = it },
                        selectedCategory = selectedToolCategory,
                        categories = toolCategories,
                        onCategorySelected = { selectedToolCategory = it },
                        sectionTitle = sectionTitle,
                        selectAllLabel = selectAllLabel,
                        deselectAllLabel = deselectAllLabel,
                        canEnableAll = canEnableAll,
                        canDisableAll = canDisableAll,
                        filteredTools = filteredTools,
                        selectedFilteredTools = selectedFilteredTools,
                        unselectedFilteredTools = unselectedFilteredTools,
                        systemToolNames = systemToolNames,
                        disabledSystemToolTitles = disabledSystemToolTitles,
                        filteredSystemTools = filteredSystemTools,
                        filteredNormalTools = filteredNormalTools,
                        onSelectAll = { handleBulkToggle(filteredToolNames, true) },
                        onDeselectAll = { handleBulkToggle(deselectableToolNames, false) },
                        onToggleTool = { tool, selected -> handleToggle(tool, selected) }
                    )
                }
            }
        },
    )

    if (showAddDialog) {
        newCategoryName.value = ""
        NewItemDialog(
            newItemText = newCategoryName,
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                onAddCategory(name)
                showAddDialog = false
            },
        )
    }

}

@Composable
private fun CreationConfigTabRow(
    selectedTab: CreationConfigTab,
    onTabSelected: (CreationConfigTab) -> Unit,
) {
    val options = listOf(CreationConfigTab.Categories, CreationConfigTab.Tools)
    GlassSegmentedButtonRow(
        options = options,
        selectedOption = selectedTab,
        onOptionSelected = onTabSelected,
        modifier = Modifier.fillMaxWidth(),
        label = { tab ->
            Text(
                text = when (tab) {
                    CreationConfigTab.Categories -> stringResource(R.string.creation_meta_tab_categories)
                    CreationConfigTab.Tools -> stringResource(R.string.creation_meta_tab_tools)
                },
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
            )
        },
        rowStyle = GlassStyle.None,
        rowColor = MaterialTheme.colorScheme.surfaceContainer,
        selectedColor = MaterialTheme.colorScheme.primaryContainer,
        selectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConfigSelectionOverviewCard(
    showCategorySection: Boolean,
    selectedCategories: List<Category>,
    selectedTools: List<ToolCatalogItem>,
    toolEmptyHint: String,
    onOpenCategories: () -> Unit,
    onOpenTools: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.14f),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = stringResource(R.string.creation_meta_summary_title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(10.dp))
            if (showCategorySection) {
                SelectionPreviewSection(
                    title = stringResource(R.string.creation_meta_tab_categories),
                    count = stringResource(
                        R.string.creation_meta_selected_category_count,
                        selectedCategories.size
                    ),
                    onClick = onOpenCategories,
                ) {
                    if (selectedCategories.isEmpty()) {
                        Text(
                            text = stringResource(R.string.creation_meta_categories_empty),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            selectedCategories.take(6).forEach { category ->
                                CategorySelectionTag(text = category.name)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            SelectionPreviewSection(
                title = stringResource(R.string.creation_meta_tab_tools),
                count = stringResource(
                    R.string.creation_meta_selected_tool_count,
                    selectedTools.size
                ),
                onClick = onOpenTools,
            ) {
                SelectedToolSummary(
                    tools = selectedTools,
                    emptyText = toolEmptyHint,
                    maxPreviewCount = 3,
                )
            }
        }
    }
}

@Composable
private fun SelectionPreviewSection(
    title: String,
    count: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.34f),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = count,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun CategorySelectionTag(text: String) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.75f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreationCategorySelectionSection(
    categoryHint: String,
    categories: List<Category>,
    selectedCategoryIds: Set<Int>,
    onToggleCategory: (Int, Boolean) -> Unit,
    onAddCategoryClick: () -> Unit,
) {
    Column {
        if (categoryHint.isNotBlank()) {
            Text(
                text = categoryHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            categories.forEach { category ->
                val isSelected = category.id in selectedCategoryIds
                FilterChip(
                    selected = isSelected,
                    onClick = { onToggleCategory(category.id, isSelected) },
                    leadingIcon = {
                        if (isSelected) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    },
                    label = {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    colors = AppTheme.colors.getFilterChipColors(),
                    border = null,
                )
            }

            AssistChip(
                onClick = onAddCategoryClick,
                leadingIcon = {
                    Icon(
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.creation_meta_add_category),
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
            )
        }
    }
}

@Composable
private fun CreationToolsPanel(
    toolTitle: String,
    toolEmptyHint: String,
    toolSelectedHint: (Int) -> String,
    selectedToolNames: Set<String>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: ToolCategory?,
    categories: List<ToolCategory>,
    onCategorySelected: (ToolCategory?) -> Unit,
    sectionTitle: String,
    selectAllLabel: String,
    deselectAllLabel: String,
    canEnableAll: Boolean,
    canDisableAll: Boolean,
    filteredTools: List<ToolCatalogItem>,
    selectedFilteredTools: List<ToolCatalogItem>,
    unselectedFilteredTools: List<ToolCatalogItem>,
    systemToolNames: Set<String>,
    disabledSystemToolTitles: List<String>,
    filteredSystemTools: List<ToolCatalogItem>,
    filteredNormalTools: List<ToolCatalogItem>,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onToggleTool: (ToolCatalogItem, Boolean) -> Unit,
) {
    Column {
        Text(
            text = toolTitle,
            style = MaterialTheme.typography.titleMedium,
        )
        val helperText = if (selectedToolNames.isEmpty()) {
            toolEmptyHint
        } else {
            toolSelectedHint(selectedToolNames.size)
        }
        if (helperText.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }
        CreationToolSelectionSection(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            selectedCategory = selectedCategory,
            categories = categories,
            onCategorySelected = onCategorySelected,
            sectionTitle = sectionTitle,
            selectAllLabel = selectAllLabel,
            deselectAllLabel = deselectAllLabel,
            canEnableAll = canEnableAll,
            canDisableAll = canDisableAll,
            filteredTools = filteredTools,
            selectedFilteredTools = selectedFilteredTools,
            unselectedFilteredTools = unselectedFilteredTools,
            selectedToolNames = selectedToolNames,
            systemToolNames = systemToolNames,
            disabledSystemToolTitles = disabledSystemToolTitles,
            filteredSystemTools = filteredSystemTools,
            filteredNormalTools = filteredNormalTools,
            onSelectAll = onSelectAll,
            onDeselectAll = onDeselectAll,
            onToggleTool = onToggleTool,
        )
    }
}

@Composable
private fun CreationToolSelectionSection(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: ToolCategory?,
    categories: List<ToolCategory>,
    onCategorySelected: (ToolCategory?) -> Unit,
    sectionTitle: String,
    selectAllLabel: String,
    deselectAllLabel: String,
    canEnableAll: Boolean,
    canDisableAll: Boolean,
    filteredTools: List<ToolCatalogItem>,
    selectedFilteredTools: List<ToolCatalogItem>,
    unselectedFilteredTools: List<ToolCatalogItem>,
    selectedToolNames: Set<String>,
    systemToolNames: Set<String>,
    disabledSystemToolTitles: List<String>,
    filteredSystemTools: List<ToolCatalogItem>,
    filteredNormalTools: List<ToolCatalogItem>,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onToggleTool: (ToolCatalogItem, Boolean) -> Unit,
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = {
                Text(text = stringResource(R.string.ai_tools_search_placeholder))
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "all_tool_category") {
                ToolCategoryChip(
                    text = stringResource(R.string.ai_tools_category_all),
                    selected = selectedCategory == null,
                    onClick = { onCategorySelected(null) }
                )
            }
            items(categories, key = { it.name }) { category ->
                ToolCategoryChip(
                    text = category.displayName(),
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ToolSectionHeader(
            title = sectionTitle,
            selectAllLabel = selectAllLabel,
            deselectAllLabel = deselectAllLabel,
            canEnableAll = canEnableAll,
            canDisableAll = canDisableAll,
            onSelectAll = onSelectAll,
            onDeselectAll = onDeselectAll
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (disabledSystemToolTitles.isNotEmpty()) {
            Text(
                text = stringResource(
                    R.string.ai_tools_system_tools_warning,
                    disabledSystemToolTitles.joinToString("、")
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (filteredTools.isEmpty()) {
            Text(
                text = stringResource(R.string.ai_tools_no_results),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedFilteredTools.isNotEmpty()) {
                    item(key = "selected_tools_title") {
                        ToolSubsectionHeader(
                            title = stringResource(
                                R.string.ai_tools_selected_section_title,
                                selectedFilteredTools.size
                            ),
                            supporting = if (filteredSystemTools.any { it.name in selectedToolNames }) {
                                stringResource(R.string.ai_tools_system_section_hint)
                            } else {
                                null
                            },
                            accentColor = MaterialTheme.colorScheme.primary,
                        )
                    }
                    items(selectedFilteredTools, key = { "selected_${it.name}" }) { tool ->
                        val isSelected = tool.name in selectedToolNames
                        ToolSelectionOptionItem(
                            tool = tool,
                            selected = isSelected,
                            isSystemTool = tool.name in systemToolNames,
                            onClick = { onToggleTool(tool, !isSelected) }
                        )
                    }
                } else {
                    item(key = "selected_tools_empty") {
                        ToolEmptyStateHint(
                            text = stringResource(R.string.ai_tools_selected_section_empty)
                        )
                    }
                }

                item(key = "unselected_tools_title") {
                    ToolSubsectionHeader(
                        title = stringResource(
                            R.string.ai_tools_unselected_section_title,
                            unselectedFilteredTools.size
                        ),
                        supporting = when {
                            unselectedFilteredTools.isEmpty() -> stringResource(R.string.ai_tools_unselected_section_empty)
                            filteredNormalTools.isEmpty() && filteredSystemTools.isNotEmpty() -> stringResource(R.string.ai_tools_system_section_title)
                            else -> null
                        },
                        accentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(unselectedFilteredTools, key = { "unselected_${it.name}" }) { tool ->
                    val isSelected = tool.name in selectedToolNames
                    ToolSelectionOptionItem(
                        tool = tool,
                        selected = isSelected,
                        isSystemTool = tool.name in systemToolNames,
                        onClick = { onToggleTool(tool, !isSelected) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolSubsectionHeader(
    title: String,
    supporting: String?,
    accentColor: androidx.compose.ui.graphics.Color,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = accentColor,
        )
        if (!supporting.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ToolEmptyStateHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectedToolSummary(
    tools: List<ToolCatalogItem>,
    modifier: Modifier = Modifier,
    emptyText: String,
    maxPreviewCount: Int = 4,
) {
    if (tools.isEmpty()) {
        Text(
            text = emptyText,
            modifier = modifier,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val previewTools = tools.take(maxPreviewCount)
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        previewTools.forEach { tool ->
            val backgroundColor = if (tool.riskLevel == ToolRiskLevel.DANGEROUS) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
            val contentColor = if (tool.riskLevel == ToolRiskLevel.DANGEROUS) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            }
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = backgroundColor
            ) {
                Text(
                    text = tool.title,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor
                )
            }
        }
        val remainCount = tools.size - previewTools.size
        if (remainCount > 0) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = stringResource(R.string.agent_bound_tools_more_count, remainCount),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ToolSelectionOptionItem(
    tool: ToolCatalogItem,
    selected: Boolean,
    isSystemTool: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Switch(
                checked = selected,
                onCheckedChange = { onClick() },
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tool.title,
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tool.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (isSystemTool) {
                        ToolSelectionTag(text = stringResource(R.string.ai_tool_system_required))
                    }
                    ToolSelectionTag(text = tool.category.displayName())
                    ToolSelectionRiskTag(riskLevel = tool.riskLevel)
                    if (tool.isInteractive) {
                        ToolSelectionTag(text = stringResource(R.string.ai_tool_interactive))
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolCategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (selected) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ToolSectionHeader(
    title: String,
    selectAllLabel: String,
    deselectAllLabel: String,
    canEnableAll: Boolean,
    canDisableAll: Boolean,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = onSelectAll, enabled = canEnableAll) {
                Text(text = selectAllLabel)
            }
            TextButton(onClick = onDeselectAll, enabled = canDisableAll) {
                Text(text = deselectAllLabel)
            }
        }
    }
}

@Composable
private fun ToolSelectionTag(text: String) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ToolSelectionRiskTag(riskLevel: ToolRiskLevel) {
    val text = when (riskLevel) {
        ToolRiskLevel.SAFE -> stringResource(R.string.ai_tool_risk_safe)
        ToolRiskLevel.SENSITIVE -> stringResource(R.string.ai_tool_risk_sensitive)
        ToolRiskLevel.DANGEROUS -> stringResource(R.string.ai_tool_risk_dangerous)
    }
    val background = when (riskLevel) {
        ToolRiskLevel.SAFE -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ToolRiskLevel.SENSITIVE -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)
        ToolRiskLevel.DANGEROUS -> MaterialTheme.colorScheme.error.copy(alpha = 0.14f)
    }
    val content = when (riskLevel) {
        ToolRiskLevel.SAFE -> MaterialTheme.colorScheme.primary
        ToolRiskLevel.SENSITIVE -> MaterialTheme.colorScheme.tertiary
        ToolRiskLevel.DANGEROUS -> MaterialTheme.colorScheme.error
    }
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = background
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = content
        )
    }
}

@Composable
private fun ToolCategory.displayName(): String {
    return when (this) {
        ToolCategory.SYSTEM -> stringResource(R.string.ai_tools_category_system)
        ToolCategory.DEVICE -> stringResource(R.string.ai_tools_category_device)
        ToolCategory.FORM -> stringResource(R.string.ai_tools_category_form)
        ToolCategory.FILE -> stringResource(R.string.ai_tools_category_file)
        ToolCategory.KNOWLEDGE -> stringResource(R.string.ai_tools_category_knowledge)
        ToolCategory.NETWORK -> stringResource(R.string.ai_tools_category_network)
        ToolCategory.BUSINESS -> stringResource(R.string.ai_tools_category_business)
        ToolCategory.IMAGE -> stringResource(R.string.ai_tools_category_image)
        ToolCategory.MEDIA -> stringResource(R.string.ai_tools_category_media)
        ToolCategory.PROMPT -> stringResource(R.string.ai_tools_category_prompt)
    }
}
