package com.wanbaohe.passwordvault.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassFilterChip
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSearchTextField
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxListItem
import com.wanbaohe.passwordvault.R
import com.wanbaohe.passwordvault.list.screenLogic.PasswordVaultListComponent
import com.wanbaohe.passwordvault.model.PasswordVaultListEvent
import com.wanbaohe.passwordvault.model.VaultEntry
import com.wanbaohe.passwordvault.service.PasswordVaultServiceImpl
import com.wanbaohe.passwordvault.util.localizedCategoryName
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.ShieldKey
import com.t8rin.imagetoolbox.core.resources.icons.Language
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSearchOff
import com.t8rin.imagetoolbox.core.resources.icons.line.LineEmail
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWifi
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAccountBalance
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLabel
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShieldKey

@Composable
fun PasswordVaultListScreen(
    component: PasswordVaultListComponent
) {
    val uiState by component.uiState.collectAsState()

    BaseScreen(
        title = stringResource(R.string.password_vault_title),
        onGoBack = component.onGoBack,
        actions = {
            IconButton(
                onClick = { component.handleEvent(PasswordVaultListEvent.CreateEntry) }
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.password_vault_add_entry)
                )
            }
        }
    ) {
        if (uiState.entries.isEmpty() && uiState.query.isEmpty() && uiState.selectedCategoryId == null) {
            EmptyPlaceholder(
                onCreate = { component.handleEvent(PasswordVaultListEvent.CreateEntry) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(OneBoxDesignSystem.screenPadding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = OneBoxDesignSystem.screenPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                GlassSearchTextField(
                    value = uiState.query,
                    onValueChange = { component.handleEvent(PasswordVaultListEvent.QueryChanged(it)) },
                    placeholder = stringResource(R.string.password_vault_search_placeholder),
                )

                if (uiState.categories.isNotEmpty()) {
                    CategoryFilterRow(
                        categories = uiState.categories.map {
                            it.id to localizedCategoryName(it.id, it.name)
                        },
                        selectedCategoryId = uiState.selectedCategoryId,
                        onSelect = { id ->
                            component.handleEvent(PasswordVaultListEvent.SelectCategory(id))
                        },
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (uiState.entries.isEmpty()) {
                        FilteredEmptyPlaceholder(
                            onClear = { component.handleEvent(PasswordVaultListEvent.ClearFilters) },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        EntriesList(
                            entries = uiState.entries,
                            onOpen = { component.handleEvent(PasswordVaultListEvent.OpenEntry(it)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    categories: List<Pair<String, String>>,
    selectedCategoryId: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(vertical = 2.dp),
    ) {
        item {
            GlassFilterChip(
                selected = selectedCategoryId == null,
                onClick = { onSelect(null) },
                label = {
                    Text(text = stringResource(R.string.password_vault_filter_all))
                },
            )
        }
        items(categories, key = { it.first }) { (id, name) ->
            GlassFilterChip(
                selected = id == selectedCategoryId,
                onClick = { onSelect(id) },
                label = { Text(text = name) },
            )
        }
    }
}

@Composable
private fun EntriesList(
    entries: List<VaultEntry>,
    onOpen: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            top = 4.dp,
            bottom = OneBoxDesignSystem.itemSpacing,
        ),
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)
    ) {
        items(entries, key = { it.id }) { entry ->
            PasswordEntryListItem(
                entry = entry,
                onClick = { onOpen(entry.id) }
            )
        }
    }
}

@Composable
private fun PasswordEntryListItem(
    entry: VaultEntry,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        OneBoxListItem(
            headlineContent = {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            subtitle = entry.account?.takeIf { it.isNotBlank() }?.let { account ->
                {
                    Text(
                        text = account,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            supportingContent = {
                Text(
                    text = localizedCategoryName(entry.categoryId, entry.categoryName),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingContent = {
                CategoryAvatar(categoryId = entry.categoryId)
            }
        )
    }
}

/**
 * 分类圆形图标头像:按内置分类 id 映射语义图标,自定义分类用标签图标兜底。
 */
@Composable
private fun CategoryAvatar(
    categoryId: String,
    modifier: Modifier = Modifier
) {
    val icon = when (categoryId) {
        PasswordVaultServiceImpl.DEFAULT_CATEGORY_LOGIN -> com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Language
        PasswordVaultServiceImpl.DEFAULT_CATEGORY_BANK -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAccountBalance
        PasswordVaultServiceImpl.DEFAULT_CATEGORY_EMAIL -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineEmail
        PasswordVaultServiceImpl.DEFAULT_CATEGORY_WIFI -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWifi
        else -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLabel
    }
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun EmptyPlaceholder(
    onCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShieldKey,
                contentDescription = null,
                modifier = Modifier
                    .padding(bottom = OneBoxDesignSystem.blockSpacing)
                    .size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.password_vault_empty_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.password_vault_empty_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            EnhancedButton(
                onClick = onCreate,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(top = OneBoxDesignSystem.blockSpacing)
            ) {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add, contentDescription = null)
                Text(stringResource(R.string.password_vault_add_first_entry))
            }
        }
    }
}

@Composable
private fun FilteredEmptyPlaceholder(
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.password_vault_filter_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        EnhancedButton(
            onClick = onClear,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Text(stringResource(R.string.password_vault_filter_clear))
        }
    }
}
