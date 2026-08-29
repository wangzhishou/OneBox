package com.wanbaohe.file.browser.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.ConfirmContentDialog
import com.shifenmiao.base.ui.card.AllFilesAccessPermissionCard
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.helper.isPortraitOrientationAsState
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavItem
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.file.browser.R
import com.wanbaohe.file.browser.model.FileBrowserState
import com.wanbaohe.file.browser.model.FileBrowserViewMode
import com.wanbaohe.file.browser.model.SortConfig
import com.wanbaohe.file.browser.model.SortOrder
import com.wanbaohe.file.browser.model.SortType
import com.wanbaohe.file.browser.screenLogic.FileBrowserComponent
import com.wanbaohe.file.browser.ui.EmptyFolderState
import com.wanbaohe.file.browser.ui.ErrorState
import com.wanbaohe.file.browser.ui.FileList
import com.wanbaohe.file.browser.ui.LoadingState
import com.wanbaohe.file.browser.ui.NoPermissionState
import com.wanbaohe.file.browser.ui.RecentAccessList
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.Search
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.ArrowUpward
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.OpenInNew
import com.t8rin.imagetoolbox.core.resources.icons.line.LineApp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFeatures
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolder
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandLess
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandMore
import com.t8rin.imagetoolbox.core.resources.icons.line.LineViewList
import com.t8rin.imagetoolbox.core.resources.icons.SelectAll
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowDownward
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFileTransfer
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSchedule

/**
 * File browser main screen
 *
 * @param fileBrowserComponent The file browser component managing the state
 * @param onGoBack Callback to go back from the screen
 */
@Composable
fun FileBrowserScreen(
    fileBrowserComponent: FileBrowserComponent,
    onGoBack: () -> Unit
) {
    val state by fileBrowserComponent.state.collectAsState()
    val sortConfig by fileBrowserComponent.sortConfig.collectAsState()
    val isPortrait by isPortraitOrientationAsState()
    val navDestination by fileBrowserComponent.navDestination.collectAsState()
    val currentUri by fileBrowserComponent.currentUri.collectAsState()
    val viewMode by fileBrowserComponent.viewMode.collectAsState()
    val selectionMode by fileBrowserComponent.selectionMode.collectAsState()
    val selectedUris by fileBrowserComponent.selectedUris.collectAsState()

    val isSearchMode = remember { mutableStateOf(false) }
    val searchQuery = remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val showDeleteDialog = remember { mutableStateOf(false) }
    val showClearRecentsDialog = remember { mutableStateOf(false) }
    val showRenameDialog = remember { mutableStateOf(false) }
    val renameInput = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    // Reset renameInput when selection changes (single selection rename default).
    LaunchedEffect(selectedUris) {
        if (selectedUris.size == 1) {
            val single =
                (state as? FileBrowserState.Success)?.files?.firstOrNull { it.uri in selectedUris }
            renameInput.value = single?.name.orEmpty()
        } else {
            renameInput.value = ""
        }
    }

    val deleteNamesText = remember(state, selectedUris) {
        val names = (state as? FileBrowserState.Success)
            ?.files
            ?.filter { it.uri in selectedUris }
            ?.map { it.name }
            .orEmpty()
        names
    }

    BaseScreen(
        title = {
            if (selectionMode != FileBrowserComponent.SelectionMode.NONE) {
                Text(
                    text = stringResource(R.string.selected_count, selectedUris.size),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
            } else {
                Text(
                    text = stringResource(R.string.file_browser_title),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    if (selectionMode != FileBrowserComponent.SelectionMode.NONE) {
                        fileBrowserComponent.exitSelectionMode()
                    } else {
                        onGoBack()
                    }
                }
            ) {
                Icon(
                    com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                    contentDescription = stringResource(
                        if (selectionMode != FileBrowserComponent.SelectionMode.NONE) R.string.cd_exit_selection
                        else R.string.cd_back_button
                    )
                )
            }
        },
        actions = {
            // Selection mode: show select all and exit buttons in top bar
            if (selectionMode != FileBrowserComponent.SelectionMode.NONE) {
                IconButton(onClick = fileBrowserComponent::selectAll) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.SelectAll,
                        contentDescription = stringResource(R.string.cd_select_all)
                    )
                }
                IconButton(onClick = fileBrowserComponent::exitSelectionMode) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.cd_exit_selection)
                    )
                }
                return@BaseScreen null
            }

            if (navDestination == FileBrowserComponent.NavDestination.RECENTS) {
                IconButton(onClick = { showClearRecentsDialog.value = true }) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.cd_clear_recents),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = {
                    if (isSearchMode.value) {
                        isSearchMode.value = false
                        searchQuery.value = ""
                    } else {
                        isSearchMode.value = true
                    }
                }
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Search,
                    contentDescription = stringResource(
                        if (isSearchMode.value) R.string.cd_close_search else R.string.cd_open_search
                    )
                )
            }
        },
        onGoBack = {
            if (selectionMode != FileBrowserComponent.SelectionMode.NONE) {
                fileBrowserComponent.exitSelectionMode()
            } else {
                onGoBack()
            }
        },
        isBackHandler = false,
        showNavigationBarsPadding = false
    ) {
        // Navigation toolbar with breadcrumb, view mode toggle, and sort options
        if (selectionMode == FileBrowserComponent.SelectionMode.NONE) {
            NavigationToolbar(
                breadcrumbs = fileBrowserComponent.getBreadcrumbs(),
                onBreadcrumbClick = fileBrowserComponent::navigateToUri,
                canNavigateUp = currentUri != null,
                onNavigateUp = fileBrowserComponent::navigateUp,
                viewMode = viewMode,
                onToggleViewMode = fileBrowserComponent::toggleViewMode,
                sortConfig = sortConfig,
                onSortTypeChange = fileBrowserComponent::changeSortType
            )
            AllFilesAccessPermissionCard(
                modifier = Modifier.padding(
                    horizontal = AppTheme.dimens.paddingNormal,
                    vertical = AppTheme.dimens.spaceSmall
                )
            )
        }

        // Search input
        AnimatedVisibility(
            visible = isSearchMode.value,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            OutlinedTextField(
                value = searchQuery.value,
                onValueChange = { value -> searchQuery.value = value },
                singleLine = true,
                placeholder = { Text(stringResource(R.string.search_hint)) },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                colors = AppTheme.colors.getOutlinedTextFieldColors(),
                shape = MaterialTheme.shapes.medium,
                trailingIcon = {
                    if (searchQuery.value.isNotEmpty()) {
                        IconButton(onClick = { searchQuery.value = "" }) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.cd_clear_search)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.dimens.paddingNormal)
            )
        }

        // Content + bottom bars.
        if (isPortrait) {
            FileBrowserContent(
                state = state,
                onItemClick = fileBrowserComponent::onItemClick,
                onItemLongClick = fileBrowserComponent::onItemLongClick,
                onRetry = fileBrowserComponent::refresh,
                isPortrait = true,
                onRecentClick = fileBrowserComponent::openRecent,
                viewMode = viewMode,
                searchQuery = if (isSearchMode.value) searchQuery.value else ""
            )

            if (selectionMode != FileBrowserComponent.SelectionMode.NONE) {
                val canOpenWith = selectedUris.size == 1 &&
                        (state as? FileBrowserState.Success)?.files?.firstOrNull { it.uri in selectedUris }?.isDirectory == false
                SelectionActionBar(
                    selectedCount = selectedUris.size,
                    canRename = selectedUris.size == 1,
                    canOpenWith = canOpenWith,
                    onClearSelection = fileBrowserComponent::exitSelectionMode,
                    onDelete = { showDeleteDialog.value = true },
                    onRename = { showRenameDialog.value = true },
                    onCopy = {
                        val dest = fileBrowserComponent.getWorkspaceRootUriOrNull()
                        if (dest != null) {
                            coroutineScope.launch {
                                fileBrowserComponent.copySelectedTo(dest).onFailure {
                                    errorMessage.value = it.message
                                }
                            }
                        }
                    },
                    onMove = {
                        val dest = fileBrowserComponent.getWorkspaceRootUriOrNull()
                        if (dest != null) {
                            coroutineScope.launch {
                                fileBrowserComponent.moveSelectedTo(dest).onFailure {
                                    errorMessage.value = it.message
                                }
                            }
                        }
                    },
                    onOpenWith = fileBrowserComponent::openSelectedWithSystemChooser
                )
            } else {
                FileBrowserBottomBar(
                    current = navDestination,
                    onHome = fileBrowserComponent::openHome,
                    onRecents = fileBrowserComponent::openRecents,
                    onWorkspace = fileBrowserComponent::openWorkspace
                )
            }
        } else {
            // Landscape keeps rail, but still show fixed selection action bar at bottom of the right pane.
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                FileBrowserRail(
                    current = navDestination,
                    onHome = fileBrowserComponent::openHome,
                    onRecents = fileBrowserComponent::openRecents,
                    onWorkspace = fileBrowserComponent::openWorkspace
                )
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    FileBrowserContent(
                        state = state,
                        onItemClick = fileBrowserComponent::onItemClick,
                        onItemLongClick = fileBrowserComponent::onItemLongClick,
                        onRetry = fileBrowserComponent::refresh,
                        isPortrait = false,
                        onRecentClick = fileBrowserComponent::openRecent,
                        viewMode = viewMode,
                        searchQuery = if (isSearchMode.value) searchQuery.value else ""
                    )

                    if (selectionMode != FileBrowserComponent.SelectionMode.NONE) {
                        val canOpenWith = selectedUris.size == 1 &&
                                (state as? FileBrowserState.Success)?.files?.firstOrNull { it.uri in selectedUris }?.isDirectory == false
                        SelectionActionBar(
                            selectedCount = selectedUris.size,
                            canRename = selectedUris.size == 1,
                            canOpenWith = canOpenWith,
                            onClearSelection = fileBrowserComponent::exitSelectionMode,
                            onDelete = { showDeleteDialog.value = true },
                            onRename = { showRenameDialog.value = true },
                            onCopy = {
                                val dest = fileBrowserComponent.getWorkspaceRootUriOrNull()
                                if (dest != null) {
                                    coroutineScope.launch {
                                        fileBrowserComponent.copySelectedTo(dest).onFailure {
                                            errorMessage.value = it.message
                                        }
                                    }
                                }
                            },
                            onMove = {
                                val dest = fileBrowserComponent.getWorkspaceRootUriOrNull()
                                if (dest != null) {
                                    coroutineScope.launch {
                                        fileBrowserComponent.moveSelectedTo(dest).onFailure {
                                            errorMessage.value = it.message
                                        }
                                    }
                                }
                            },
                            onOpenWith = fileBrowserComponent::openSelectedWithSystemChooser
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog.value) {
        val expanded = remember { mutableStateOf(false) }

        // Preview + full list text
        val previewNames = deleteNamesText.take(8)
        val showExpandToggle = deleteNamesText.size > previewNames.size

        ConfirmContentDialog(
            title = stringResource(com.shifenmiao.core.R.string.ai_chat_delete_title),
            confirmButtonText = stringResource(com.shifenmiao.core.R.string.button_confirm),
            dismissButtonText = stringResource(com.shifenmiao.core.R.string.button_cancel),
            icon = {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                    contentDescription = null
                )
            },
            onConfirm = {
                coroutineScope.launch {
                    fileBrowserComponent.deleteSelected().onFailure {
                        errorMessage.value = it.message
                    }
                }
                showDeleteDialog.value = false
            },
            onDismiss = {
                showDeleteDialog.value = false
            },
            showDialog = showDeleteDialog.value
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.dialog_delete_message, selectedUris.size),
                    textAlign = TextAlign.Start
                )

                if (deleteNamesText.isNotEmpty()) {
                    val namesToShow = if (expanded.value) deleteNamesText else previewNames

                    // Scrollable name list when expanded; otherwise show compact list.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (expanded.value) {
                                    Modifier
                                        .defaultMinSize(minHeight = 64.dp)
                                        .heightIn(max = 240.dp)
                                } else {
                                    Modifier.wrapContentHeight()
                                }
                            )
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(namesToShow) { name ->
                                Text(
                                    text = "• $name",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (!expanded.value && showExpandToggle) {
                                item {
                                    Text(
                                        text = "• …",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    if (showExpandToggle) {
                        TextButton(
                            onClick = { expanded.value = !expanded.value }
                        ) {
                            Icon(
                                imageVector = if (expanded.value) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandLess else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandMore,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (expanded.value) {
                                    stringResource(R.string.collapse)
                                } else {
                                    stringResource(R.string.expand)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRenameDialog.value) {
        AlertDialog(
            onDismissRequest = { showRenameDialog.value = false },
            title = { Text(stringResource(R.string.dialog_rename_title)) },
            text = {
                OutlinedTextField(
                    value = renameInput.value,
                    onValueChange = { renameInput.value = it },
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.dialog_rename_hint)) },
                    colors = AppTheme.colors.getOutlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    enabled = renameInput.value.isNotBlank(),
                    onClick = {
                        showRenameDialog.value = false
                        coroutineScope.launch {
                            fileBrowserComponent.renameSingleSelected(renameInput.value)
                                .onFailure { errorMessage.value = it.message }
                        }
                    }
                ) {
                    Text(stringResource(R.string.dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog.value = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

    if (showClearRecentsDialog.value) {
        AlertDialog(
            containerColor = AppTheme.colors.getContainerSurfaceColor(),
            onDismissRequest = { showClearRecentsDialog.value = false },
            title = { Text(stringResource(R.string.dialog_clear_recents_title)) },
            text = { Text(stringResource(R.string.dialog_clear_recents_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearRecentsDialog.value = false
                        fileBrowserComponent.clearRecents()
                    }
                ) {
                    Text(stringResource(R.string.dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearRecentsDialog.value = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

    if (errorMessage.value != null) {
        AlertDialog(
            containerColor = AppTheme.colors.getContainerSurfaceColor(),
            onDismissRequest = { errorMessage.value = null },
            title = { Text(stringResource(R.string.error_loading_title)) },
            text = { Text(stringResource(R.string.operation_failed, errorMessage.value ?: "")) },
            confirmButton = {
                TextButton(onClick = { errorMessage.value = null }) {
                    Text(stringResource(R.string.dialog_confirm))
                }
            }
        )
    }

    BackHandler {
        if (selectionMode != FileBrowserComponent.SelectionMode.NONE) {
            fileBrowserComponent.exitSelectionMode()
            return@BackHandler
        }
        // 返回键触发时，如果在子目录中，先返回上一级目录
        if (currentUri != null) {
            fileBrowserComponent.navigateUp()
            return@BackHandler
        }
        onGoBack()
    }
}

/**
 * Navigation toolbar showing breadcrumb, view mode toggle, and sort options.
 * Placed below the title bar for a flat, modern UI experience.
 */
@Composable
private fun NavigationToolbar(
    breadcrumbs: List<Pair<String, android.net.Uri?>>,
    onBreadcrumbClick: (android.net.Uri?) -> Unit,
    canNavigateUp: Boolean,
    onNavigateUp: () -> Unit,
    viewMode: FileBrowserViewMode,
    onToggleViewMode: () -> Unit,
    sortConfig: SortConfig,
    onSortTypeChange: (SortType) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var sortMenuExpanded by remember { mutableStateOf(false) }
    val iconSize = 16.dp

    // 面包屑保持单行横向滚动;路径超长时自动滚到末尾,保证当前目录始终可见
    LaunchedEffect(breadcrumbs) {
        withFrameNanos { }
        scrollState.scrollTo(scrollState.maxValue)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 7.dp, end = 12.dp, top = 0.dp, bottom = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Up navigation button - only show when can navigate up
        if (canNavigateUp) {
            IconButton(
                onClick = onNavigateUp
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.ArrowUpward,
                    contentDescription = stringResource(R.string.cd_navigate_up),
                    modifier = Modifier.size(iconSize),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            IconButton(
                onClick = { },
                enabled = false
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineApp,
                    contentDescription = stringResource(R.string.cd_navigate_up),
                    modifier = Modifier.size(iconSize),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Breadcrumb path (takes remaining space)
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {
            breadcrumbs.forEachIndexed { index, (name, uri) ->
                val isLast = index == breadcrumbs.size - 1

                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    color = if (isLast) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier
                        .clickable(enabled = !isLast) { onBreadcrumbClick(uri) }
                        .padding(horizontal = 2.dp, vertical = 4.dp)
                )

                if (!isLast) {
                    Text(
                        text = "/",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
        }

        // View mode toggle
        IconButton(
            onClick = onToggleViewMode,
        ) {
            Icon(
                imageVector = if (viewMode == FileBrowserViewMode.LIST) {
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFeatures
                } else {
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineViewList
                },
                contentDescription = stringResource(
                    if (viewMode == FileBrowserViewMode.LIST) R.string.cd_switch_to_grid
                    else R.string.cd_switch_to_list
                ),
                modifier = Modifier.size(iconSize),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Sort dropdown
        Box {
            // Sort button with unified style
            TextButton(
                onClick = { sortMenuExpanded = true },
            ) {
                Icon(
                    imageVector = if (sortConfig.order == SortOrder.ASCENDING) {
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.ArrowUpward
                    } else {
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowDownward
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = when (sortConfig.type) {
                        SortType.NAME -> stringResource(R.string.sort_name)
                        SortType.DATE -> stringResource(R.string.sort_date)
                        SortType.SIZE -> stringResource(R.string.sort_size)
                        SortType.TYPE -> stringResource(R.string.sort_type)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            // Dropdown menu
            DropdownMenu(
                expanded = sortMenuExpanded,
                onDismissRequest = { sortMenuExpanded = false }
            ) {
                SortType.entries.forEach { sortType ->
                    val isSelected = sortConfig.type == sortType
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = when (sortType) {
                                    SortType.NAME -> stringResource(R.string.sort_name)
                                    SortType.DATE -> stringResource(R.string.sort_date)
                                    SortType.SIZE -> stringResource(R.string.sort_size)
                                    SortType.TYPE -> stringResource(R.string.sort_type)
                                },
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        },
                        onClick = {
                            onSortTypeChange(sortType)
                            sortMenuExpanded = false
                        },
                        trailingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = if (sortConfig.order == SortOrder.ASCENDING) {
                                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.ArrowUpward
                                    } else {
                                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowDownward
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null
                    )
                }
            }
        }

    }
}


/**
 * File browser content area
 */
@Composable
private fun ColumnScope.FileBrowserContent(
    state: FileBrowserState,
    onItemClick: (com.wanbaohe.file.browser.model.FileItem) -> Unit,
    onItemLongClick: ((com.wanbaohe.file.browser.model.FileItem) -> Unit)? = null,
    onRetry: () -> Unit,
    isPortrait: Boolean,
    onRecentClick: ((android.net.Uri) -> Unit)? = null,
    viewMode: FileBrowserViewMode = FileBrowserViewMode.LIST,
    searchQuery: String = ""
) {
    val horizontalPadding = if (isPortrait) 16.dp else 8.dp
    val contentPadding = PaddingValues(
        start = horizontalPadding,
        end = horizontalPadding,
        top = 0.dp,
        bottom = 8.dp
    )

    when (state) {
        is FileBrowserState.Idle -> {
            LoadingState()
        }

        is FileBrowserState.Loading -> {
            LoadingState()
        }

        is FileBrowserState.Success -> {
            val filteredFiles = if (searchQuery.isBlank()) {
                state.files
            } else {
                val q = searchQuery.trim()
                state.files.filter { it.name.contains(q, ignoreCase = true) }
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                FileList(
                    files = filteredFiles,
                    onItemClick = onItemClick,
                    onItemLongClick = onItemLongClick,
                    contentPadding = contentPadding,
                    viewMode = viewMode
                )
            }
        }

        is FileBrowserState.Empty -> {
            EmptyFolderState(
                modifier = Modifier.weight(1f)
            )
        }

        is FileBrowserState.Error -> {
            ErrorState(
                message = state.message,
                onRetry = onRetry,
                modifier = Modifier.weight(1f)
            )
        }

        is FileBrowserState.NoPermission -> {
            NoPermissionState(
                modifier = Modifier.weight(1f)
            )
        }

        is FileBrowserState.Recents -> {
            RecentAccessList(
                items = state.items,
                onItemClick = { uri ->
                    onRecentClick?.invoke(uri)
                },
                viewMode = viewMode,
                contentPadding = contentPadding,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            )
        }

        is FileBrowserState.RecentsEmpty -> {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.nav_recents),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.recents_empty_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FileBrowserBottomBar(
    current: FileBrowserComponent.NavDestination,
    onHome: () -> Unit,
    onRecents: () -> Unit,
    onWorkspace: () -> Unit
) {
    val items = listOf(
        BottomNavItem(
            id = FileBrowserComponent.NavDestination.HOME.name,
            label = stringResource(R.string.nav_home),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineApp,
            contentDescription = stringResource(R.string.cd_nav_home),
        ),
        BottomNavItem(
            id = FileBrowserComponent.NavDestination.RECENTS.name,
            label = stringResource(R.string.nav_recents),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
            contentDescription = stringResource(R.string.cd_nav_recents),
        ),
        BottomNavItem(
            id = FileBrowserComponent.NavDestination.WORKSPACE.name,
            label = stringResource(R.string.nav_workspace),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFolder,
            contentDescription = stringResource(R.string.cd_nav_workspace),
        )
    )

    BottomNavigationBar(
        items = items,
        selectedItemId = current.name,
        onItemClick = { clicked ->
            when (clicked.id) {
                FileBrowserComponent.NavDestination.HOME.name -> onHome()
                FileBrowserComponent.NavDestination.RECENTS.name -> onRecents()
                FileBrowserComponent.NavDestination.WORKSPACE.name -> onWorkspace()
            }
        }
    )
}

@Composable
private fun FileBrowserRail(
    current: FileBrowserComponent.NavDestination,
    onHome: () -> Unit,
    onRecents: () -> Unit,
    onWorkspace: () -> Unit
) {
    NavigationRail(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        modifier = Modifier.glassBackground(
            GlassStyle.Medium,
            MaterialTheme.shapes.extraSmall,
            borderWidth = 0.dp
        )
    ) {
        NavigationRailItem(
            selected = current == FileBrowserComponent.NavDestination.HOME,
            onClick = onHome,
            icon = {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineApp,
                    contentDescription = stringResource(R.string.cd_nav_home)
                )
            },
            label = { Text(stringResource(R.string.nav_home)) }
        )

        NavigationRailItem(
            selected = current == FileBrowserComponent.NavDestination.RECENTS,
            onClick = onRecents,
            icon = {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                    contentDescription = stringResource(R.string.cd_nav_recents)
                )
            },
            label = { Text(stringResource(R.string.nav_recents)) }
        )

        NavigationRailItem(
            selected = current == FileBrowserComponent.NavDestination.WORKSPACE,
            onClick = onWorkspace,
            icon = {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFolder,
                    contentDescription = stringResource(R.string.cd_nav_workspace)
                )
            },
            label = { Text(stringResource(R.string.nav_workspace)) }
        )
    }
}

@Composable
private fun SelectionActionBar(
    selectedCount: Int,
    canRename: Boolean,
    canOpenWith: Boolean,
    onClearSelection: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    onCopy: () -> Unit,
    onMove: () -> Unit,
    onOpenWith: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassSurface(
        color = MaterialTheme.colorScheme.surface,
        borderWidth = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                ActionChip(
                    label = stringResource(R.string.cd_clear_selection),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSchedule,
                    enabled = true,
                    onClick = onClearSelection
                )
            }
            item {
                ActionChip(
                    label = stringResource(R.string.open_with),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.OpenInNew,
                    enabled = canOpenWith,
                    onClick = onOpenWith
                )
            }
            item {
                ActionChip(
                    label = stringResource(R.string.action_move),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFileTransfer,
                    enabled = selectedCount > 0,
                    onClick = onMove
                )
            }
            item {
                ActionChip(
                    label = stringResource(R.string.action_delete),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                    enabled = selectedCount > 0,
                    onClick = onDelete
                )
            }
            item {
                ActionChip(
                    label = stringResource(R.string.action_rename),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                    enabled = canRename,
                    onClick = onRename
                )
            }
            item {
                ActionChip(
                    label = stringResource(R.string.action_copy),
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                    enabled = selectedCount > 0,
                    onClick = onCopy
                )
            }
        }
    }
}

@Composable
private fun ActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        enabled = enabled,
        label = {
            Text(
                text = label,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
        },
        elevation = null,
        border = null,
        shape = AppTheme.shapes.getMediumShape(),
        colors = AppTheme.colors.getSuggestionChipColors()
    )
}
