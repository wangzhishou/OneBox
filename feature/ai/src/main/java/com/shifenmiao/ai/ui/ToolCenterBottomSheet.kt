package com.shifenmiao.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.ai.component.ToolCenterUiState
import com.shifenmiao.core.R
import com.shifenmiao.model.ai.tool.ToolCatalogItem
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet

@Composable
fun ToolCenterBottomSheet(
    visible: Boolean,
    uiState: ToolCenterUiState,
    onDismiss: () -> Unit,
    onToggleTool: (String, Boolean) -> Unit,
    onToggleTools: (List<String>, Boolean) -> Unit
) {
    if (!visible) return

    var searchQuery by remember(uiState.allTools) { mutableStateOf("") }
    var selectedCategory by remember(uiState.allTools) { mutableStateOf<ToolCategory?>(null) }
    var pendingSystemToolDisable by remember { mutableStateOf<ToolCatalogItem?>(null) }

    val enabledSet = uiState.enabledToolNames.toSet()
    val availableCategories =
        remember(uiState.allTools) { uiState.allTools.map { it.category }.distinct() }
    val filteredTools = uiState.allTools.filter { tool ->
        val matchCategory = selectedCategory == null || tool.category == selectedCategory
        val matchQuery = searchQuery.isBlank() ||
                tool.title.contains(searchQuery, ignoreCase = true) ||
                tool.summary.contains(searchQuery, ignoreCase = true) ||
                tool.keywords.any { it.contains(searchQuery, ignoreCase = true) }
        matchCategory && matchQuery
    }
    val sortedTools = filteredTools.sortedWith(
        compareByDescending<ToolCatalogItem> { it.name in enabledSet }
            .thenBy { if (it.name in uiState.systemToolNames) 0 else 1 }
            .thenBy { it.sortOrder }
            .thenBy { it.title }
    )
    val filteredToolNames = sortedTools.map { it.name }
    val deselectableToolNames = sortedTools
        .map { it.name }
        .filterNot { it in uiState.systemToolNames }
    val hasSystemToolsInCurrentResult = sortedTools.any { it.name in uiState.systemToolNames }
    val canEnableAll = filteredToolNames.any { it !in enabledSet }
    val canDisableAll = deselectableToolNames.any { it in enabledSet }
    val headerTitle = when {
        searchQuery.isNotBlank() -> stringResource(R.string.ai_tools_filtered)
        selectedCategory != null -> selectedCategory!!.displayName()
        else -> stringResource(R.string.ai_tools_all)
    }
    val selectAllLabel = when {
        searchQuery.isNotBlank() -> stringResource(R.string.ai_tools_select_filtered)
        selectedCategory != null -> stringResource(R.string.ai_tools_select_group)
        else -> stringResource(R.string.ai_tools_select_all)
    }
    val deselectAllLabel = when {
        hasSystemToolsInCurrentResult && searchQuery.isNotBlank() -> stringResource(R.string.ai_tools_deselect_all_regular)
        hasSystemToolsInCurrentResult && selectedCategory != null -> stringResource(R.string.ai_tools_deselect_all_regular)
        hasSystemToolsInCurrentResult -> stringResource(R.string.ai_tools_deselect_all_regular)
        searchQuery.isNotBlank() -> stringResource(R.string.ai_tools_deselect_filtered)
        selectedCategory != null -> stringResource(R.string.ai_tools_deselect_group)
        else -> stringResource(R.string.ai_tools_deselect_all)
    }

    fun handleToggle(tool: ToolCatalogItem, enabled: Boolean) {
        if (!enabled && tool.name in uiState.systemToolNames) {
            pendingSystemToolDisable = tool
        } else {
            onToggleTool(tool.name, enabled)
        }
    }

    fun handleBulkToggle(toolNames: List<String>, enable: Boolean) {
        if (toolNames.isEmpty()) return
        onToggleTools(toolNames, enable)
    }

    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = { onDismiss() },
        dragHandle = {
            AiBottomSheetHeader(
                title = stringResource(R.string.ai_tools_center_title),
                onClose = onDismiss
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(
                    min = AiBottomSheetDefaults.ContentMinHeight,
                    max = AiBottomSheetDefaults.ContentMaxHeight
                )
                .navigationBarsPadding()
                .padding(horizontal = AiBottomSheetDefaults.HorizontalPadding)
        ) {
            if (uiState.disabledSystemToolTitles.isNotEmpty()) {
                Text(
                    text = stringResource(
                        R.string.ai_tools_system_tools_warning,
                        uiState.disabledSystemToolTitles.joinToString("、")
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.enabledToolNames.isNotEmpty()) {
                        stringResource(R.string.ai_tools_enabled_count, uiState.enabledToolNames.size)
                    } else {
                        stringResource(R.string.ai_tools_none_enabled)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            AiBottomSheetSearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = stringResource(R.string.ai_tools_search_placeholder)
            )

            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item(key = "all_category") {
                    CategoryChip(
                        text = stringResource(R.string.ai_tools_category_all),
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null }
                    )
                }
                items(availableCategories, key = { it.name }) { category ->
                    CategoryChip(
                        text = category.displayName(),
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (sortedTools.isNotEmpty()) {
                    item(key = "all_header") {
                        SectionHeader(
                            title = headerTitle,
                            selectAllLabel = selectAllLabel,
                            deselectAllLabel = deselectAllLabel,
                            canEnableAll = canEnableAll,
                            canDisableAll = canDisableAll,
                            onSelectAll = { handleBulkToggle(filteredToolNames, true) },
                            onDeselectAll = { handleBulkToggle(deselectableToolNames, false) }
                        )
                    }
                }

                items(sortedTools, key = { it.name }) { tool ->
                    ToolRow(
                        tool = tool,
                        enabled = tool.name in enabledSet,
                        isSystemTool = tool.name in uiState.systemToolNames,
                        onToggle = ::handleToggle
                    )
                }

                if (sortedTools.isEmpty()) {
                    item(key = "empty_result") {
                        Text(
                            text = stringResource(R.string.ai_tools_no_results),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    pendingSystemToolDisable?.let { tool ->
        AlertDialog(
            onDismissRequest = { pendingSystemToolDisable = null },
            title = {
                Text(text = stringResource(R.string.ai_tools_system_disable_confirm_title))
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.ai_tools_system_disable_confirm_message,
                        tool.title
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onToggleTool(tool.name, false)
                        pendingSystemToolDisable = null
                    }
                ) {
                    Text(text = stringResource(R.string.button_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingSystemToolDisable = null
                    }
                ) {
                    Text(text = stringResource(R.string.button_cancel))
                }
            }
        )
    }

}

@Composable
private fun CategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    AiBottomSheetFilterChip(
        text = text,
        selected = selected,
        onClick = onClick
    )
}

@Composable
private fun SectionHeader(
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
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
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
private fun ToolRow(
    tool: ToolCatalogItem,
    enabled: Boolean,
    isSystemTool: Boolean,
    onToggle: (ToolCatalogItem, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .toggleable(
                value = enabled,
                role = Role.Switch,
                onValueChange = { onToggle(tool, it) }
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tool.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tool.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = { onToggle(tool, it) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSystemTool) {
                Text(
                    text = stringResource(R.string.ai_tool_system_required),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            RiskBadge(riskLevel = tool.riskLevel)
            if (tool.isInteractive) {
                Text(
                    text = stringResource(R.string.ai_tool_interactive),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
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

@Composable
private fun RiskBadge(riskLevel: ToolRiskLevel) {
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

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = content
        )
    }
}
