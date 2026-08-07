package com.shifenmiao.base.ui.icon

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.ui.R
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.resources.icons.Search

private enum class IconTab(val label: String) {
    APP("应用"),
    MATERIAL("通用"),
    ALL("全部"),
}

@Composable
fun IconPickerSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onIconSelected: (String) -> Unit,
    selectedIconName: String? = null,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val appKeys = remember { IconRegistry.appKeys }
    val materialKeys = remember { IconRegistry.materialKeys }
    val allKeys = remember { IconRegistry.allKeys }

    val currentKeys by remember(selectedTab) {
        derivedStateOf {
            when (selectedTab) {
                0 -> appKeys
                1 -> materialKeys
                else -> allKeys
            }
        }
    }

    val filteredIcons by remember(currentKeys, searchQuery) {
        derivedStateOf {
            if (searchQuery.isBlank()) currentKeys
            else currentKeys.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = { if (!it) onDismiss() },
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // 键盘弹起时整体让位,配合下方网格 weight 缩小,避免搜索框被遮挡
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                // ── Title ───────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "选择图标",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }

                // ── Search ──────────────────────────────────
                IconSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )

                // ── Tabs + 右侧当前数量(随 tab/搜索结果变化) ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PrimaryScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        modifier = Modifier.weight(1f),
                        edgePadding = 16.dp,
                        divider = {},
                        containerColor = Color.Transparent,
                    ) {
                        IconTab.entries.forEachIndexed { index, tab ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.icon_picker_icon_count, filteredIcons.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(end = 20.dp)
                    )
                }

                // ── Grid(键盘弹起时随可用空间缩小,上限 400.dp) ──
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 72.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 400.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = filteredIcons,
                        key = { it }
                    ) { iconName ->
                        IconGridItem(
                            iconName = iconName,
                            isSelected = iconName == selectedIconName,
                            onClick = {
                                onIconSelected(iconName)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun IconSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        placeholder = {
            Text(
                "搜索图标名称…",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        textStyle = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun IconGridItem(
    iconName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val icon: ImageVector? = remember(iconName) { IconRegistry.resolve(iconName) }
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier
            .size(72.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconName,
                    modifier = Modifier.size(24.dp),
                    tint = contentColor
                )
            } else {
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iconName.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = iconName,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
