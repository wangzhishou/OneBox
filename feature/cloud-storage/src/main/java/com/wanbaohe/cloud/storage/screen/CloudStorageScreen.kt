package com.wanbaohe.cloud.storage.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wanbaohe.base.ui.empty.EmptyStateGuide
import com.wanbaohe.base.ui.empty.EmptyStateGuideAction
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedDropdownMenu
import com.wanbaohe.core.ui.widget.glass.GlassTonalIconButton
import com.wanbaohe.core.ui.widget.system.OneBoxBottomActionBar
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OneBoxSectionCard
import com.wanbaohe.cloud.storage.R
import com.wanbaohe.cloud.storage.data.protocol.ObjectStoragePathResolver
import com.wanbaohe.cloud.storage.model.CloudBrowserState
import com.wanbaohe.cloud.storage.model.CloudObjectItem
import com.wanbaohe.cloud.storage.model.CloudSortMode
import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import com.wanbaohe.cloud.storage.model.RemoteProtocol
import com.wanbaohe.cloud.storage.screen.components.BucketSwitcherSheet
import com.wanbaohe.cloud.storage.screen.components.CloudFileGrid
import com.wanbaohe.cloud.storage.screen.components.ConnectionSheet
import com.wanbaohe.cloud.storage.screen.components.ConnectionSwitcherSheet
import com.wanbaohe.cloud.storage.screen.components.ObjectDetailSheet
import com.wanbaohe.cloud.storage.screen.components.SearchTabContent
import com.wanbaohe.cloud.storage.screen.components.downloadCloudFile
import com.wanbaohe.cloud.storage.screen.components.SyncPlaceholderTab
import com.wanbaohe.cloud.storage.screenLogic.CloudStorageComponent
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.ArrowUpward
import com.t8rin.imagetoolbox.core.resources.icons.Delete
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCloudStorage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFeatures
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolder
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandMore
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWarning
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCloudUpload
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowDownward
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCreateNewFolder
import com.t8rin.imagetoolbox.core.resources.icons.line.LineViewList
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutoFix

@Composable
fun CloudStorageScreen(
    component: CloudStorageComponent,
    onGoBack: () -> Unit,
) {
    val context = LocalContext.current
    val currentConnection by component.currentConnection.collectAsState()
    val connections by component.connections.collectAsState()
    val buckets by component.buckets.collectAsState()
    val currentBucket by component.currentBucket.collectAsState()
    val currentPrefix by component.currentPrefix.collectAsState()
    val browserState by component.browserState.collectAsState()
    val selectedObject by component.selectedObject.collectAsState()
    val busy by component.busy.collectAsState()
    val uploadProgress by component.uploadProgress.collectAsState()
    val message by component.message.collectAsState()
    val isGridMode by component.isGridMode.collectAsState()
    val sort by component.sort.collectAsState()

    var showConnectionSheet by remember { mutableStateOf(false) }
    var showConnectionSwitcher by remember { mutableStateOf(false) }
    var editingConnection by remember { mutableStateOf<CloudStorageConnection?>(null) }
    var showBucketSheet by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var folderName by remember { mutableStateOf("") }
    val selectedItems = remember { mutableStateMapOf<String, CloudObjectItem>() }
    var pendingDeleteItems by remember { mutableStateOf<List<CloudObjectItem>>(emptyList()) }
    var pendingDeleteConnection by remember { mutableStateOf<CloudStorageConnection?>(null) }
    var searchActive by remember { mutableStateOf(false) }
    var filterQuery by remember { mutableStateOf("") }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    val downloadScope = rememberCoroutineScope()

    val uploadLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            if (uris.isNotEmpty()) component.uploadFromUris(uris)
        }

    BackHandler(enabled = selectedItems.isNotEmpty() || currentPrefix.isNotBlank()) {
        if (selectedItems.isNotEmpty()) {
            selectedItems.clear()
        } else {
            component.navigateUp()
        }
    }

    LaunchedEffect(currentConnection?.id, currentBucket, currentPrefix) {
        selectedItems.clear()
        filterQuery = ""
    }

    BaseScreen(
        title = {
            val conn = currentConnection
            if (conn != null) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showConnectionSwitcher = true }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = conn.displayName.ifBlank {
                            stringResource(conn.protocol.titleRes)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(26.dp),
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.cloud_storage_screen_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        },
        onGoBack = onGoBack,
        isBackHandler = false,
        actions = {
            if (currentConnection != null) {
                IconButton(
                    onClick = {
                        searchActive = !searchActive
                        if (!searchActive) filterQuery = ""
                    }
                ) {
                    Icon(
                        imageVector = if (searchActive) {
                            Icons.Outlined.Close
                        } else {
                            Icons.Outlined.Search
                        },
                        contentDescription = stringResource(
                            if (searchActive) {
                                R.string.cloud_storage_search_clear
                            } else {
                                R.string.cloud_storage_search_action
                            }
                        ),
                    )
                }
            }
            IconButton(onClick = { showConnectionSheet = true }) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.cloud_storage_add_connection),
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val bucket = currentBucket
            if (currentConnection != null && bucket != null) {
                OneBoxSectionCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (currentPrefix.isNotBlank()) component.navigateUp()
                            },
                            enabled = currentPrefix.isNotBlank(),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.ArrowUpward,
                                contentDescription = stringResource(R.string.cloud_storage_up_action),
                                tint = if (currentPrefix.isNotBlank()) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                },
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        val crumbs = ObjectStoragePathResolver
                            .splitBreadcrumbs(currentPrefix)
                            .drop(1)
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // 根节点：在根目录点它切 bucket，在子目录点它回到根。
                            // 只有一个根（WebDAV）时，根目录上不再显示误导性的展开箭头。
                            val canSwitchBucket = buckets.size > 1
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(
                                        enabled = currentPrefix.isNotBlank() || canSwitchBucket,
                                    ) {
                                        if (currentPrefix.isBlank()) {
                                            showBucketSheet = true
                                        } else {
                                            component.openPrefix("")
                                        }
                                    }
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = bucket,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = if (currentPrefix.isBlank()) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (currentPrefix.isBlank() && canSwitchBucket) {
                                    Icon(
                                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandMore,
                                        contentDescription = stringResource(R.string.cloud_storage_bucket_sheet_title),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                            crumbs.forEachIndexed { index, crumb ->
                                Text(
                                    text = "/",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(horizontal = 1.dp),
                                )
                                val isLast = index == crumbs.lastIndex
                                Text(
                                    text = crumb.removeSuffix("/").substringAfterLast('/'),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isLast) FontWeight.Medium else FontWeight.Normal,
                                    color = if (isLast) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    maxLines = 1,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable(enabled = !isLast) { component.openPrefix(crumb) }
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // 排序：与文件浏览器一致的「图标 + 名称」按钮 + 全局气泡菜单
                        Box {
                            TextButton(onClick = { sortMenuExpanded = true }) {
                                Icon(
                                    imageVector = if (sort.ascending) {
                                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.ArrowUpward
                                    } else {
                                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowDownward
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = when (sort.mode) {
                                        CloudSortMode.NAME -> stringResource(R.string.cloud_storage_sort_name)
                                        CloudSortMode.DATE -> stringResource(R.string.cloud_storage_sort_date)
                                        CloudSortMode.SIZE -> stringResource(R.string.cloud_storage_sort_size)
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                            EnhancedDropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false }
                            ) {
                                CloudSortMode.entries.forEach { mode ->
                                    val isSelected = sort.mode == mode
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = when (mode) {
                                                    CloudSortMode.NAME -> stringResource(R.string.cloud_storage_sort_name)
                                                    CloudSortMode.DATE -> stringResource(R.string.cloud_storage_sort_date)
                                                    CloudSortMode.SIZE -> stringResource(R.string.cloud_storage_sort_size)
                                                },
                                                color = if (isSelected) {
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                        },
                                        onClick = {
                                            component.setSort(
                                                if (isSelected) {
                                                    sort.copy(ascending = !sort.ascending)
                                                } else {
                                                    sort.copy(mode = mode, ascending = true)
                                                }
                                            )
                                            sortMenuExpanded = false
                                        },
                                        trailingIcon = if (isSelected) {
                                            {
                                                Icon(
                                                    imageVector = if (sort.ascending) {
                                                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.ArrowUpward
                                                    } else {
                                                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowDownward
                                                    },
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        } else null
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = component::toggleViewMode,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isGridMode) {
                                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineViewList
                                } else {
                                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFeatures
                                },
                                contentDescription = stringResource(
                                    if (isGridMode) {
                                        R.string.cloud_storage_switch_to_list
                                    } else {
                                        R.string.cloud_storage_switch_to_grid
                                    }
                                ),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = component::refresh,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                                contentDescription = stringResource(R.string.cloud_storage_refresh_action),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            if (currentConnection != null && searchActive) {
                com.wanbaohe.core.ui.widget.system.OneBoxOutlinedTextField(
                    value = filterQuery,
                    onValueChange = { filterQuery = it },
                    placeholder = { Text(stringResource(R.string.cloud_storage_search_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (currentConnection == null) {
                CloudNoConnectionGuide(
                    onAddManually = { showConnectionSheet = true },
                    modifier = Modifier.weight(1f),
                )
            } else {
                FilesContent(
                    browserState = browserState,
                    isGridMode = isGridMode,
                    previewUrlProvider = component::signedUrlFor,
                    previewHeaders = component.authHeaders(),
                    copyLinkEnabled = currentConnection?.protocol != RemoteProtocol.SMB,
                    downloadEnabled = true,
                    filterQuery = filterQuery,
                    onClearFilter = { filterQuery = "" },
                    onCopyLink = { item ->
                        val url = component.signedUrlFor(item)
                        if (url != null) {
                            copyToClipboard(context, url)
                        } else {
                            notifyLinkUnavailable(context)
                        }
                    },
                    onDownload = { item ->
                        val url = component.signedUrlFor(item)
                        if (url != null) {
                            downloadCloudFile(
                                context = context,
                                url = url,
                                fileName = item.displayName,
                                scope = downloadScope,
                                headers = component.authHeaders(),
                            )
                        } else {
                            // SMB 没有可交给 DownloadManager 的 URL，应用内读流落盘
                            component.saveObjectToDevice(item)
                        }
                    },
                    onItemClick = { item ->
                        if (item.isDirectory) component.openPrefix(item.key) else component.showObject(item)
                    },
                    selectedKeys = selectedItems.keys,
                    selectionMode = selectedItems.isNotEmpty(),
                    onSelectionToggle = { item ->
                        if (selectedItems.containsKey(item.key)) {
                            selectedItems.remove(item.key)
                        } else {
                            selectedItems[item.key] = item
                        }
                    },
                    onRefresh = component::refresh,
                    onUploadClick = { uploadLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // 未连接云存储时不展示底部操作栏: 上传/新建目录都无处可去, 引导用户走空态里的添加入口
        val hasConnection = currentConnection != null
        if (hasConnection && selectedItems.isNotEmpty()) {
            OneBoxBottomActionBar(
                primaryText = stringResource(R.string.cloud_storage_delete_selected_action, selectedItems.size),
                onPrimaryClick = { pendingDeleteItems = selectedItems.values.toList() },
                secondaryText = stringResource(R.string.cloud_storage_cancel_selection),
                onSecondaryClick = { selectedItems.clear() },
                primaryEnabled = !busy,
                secondaryEnabled = !busy,
                primaryIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
            )
        } else if (hasConnection) {
            OneBoxBottomActionBar(
                primaryText = stringResource(R.string.cloud_storage_upload_action),
                onPrimaryClick = { uploadLauncher.launch(arrayOf("*/*")) },
                secondaryText = stringResource(R.string.cloud_storage_create_folder),
                onSecondaryClick = { showCreateFolderDialog = true },
                primaryIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCloudUpload,
                secondaryIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCreateNewFolder,
            )
        }
    }


    if (showConnectionSwitcher) {
        ConnectionSwitcherSheet(
            connections = connections,
            currentConnectionId = currentConnection?.id,
            onDismiss = { showConnectionSwitcher = false },
            onSwitch = { component.switchConnection(it) },
            onAdd = {
                showConnectionSwitcher = false
                editingConnection = null
                showConnectionSheet = true
            },
            onEdit = { conn ->
                showConnectionSwitcher = false
                editingConnection = conn
                showConnectionSheet = true
            },
            onDelete = { conn ->
                showConnectionSwitcher = false
                pendingDeleteConnection = conn
            },
        )
    }

    if (showConnectionSheet) {
        ConnectionSheet(
            savedConnections = connections,
            initial = editingConnection,
            onDismiss = {
                showConnectionSheet = false
                editingConnection = null
            },
            onSave = {
                component.updateConnection(it)
                showConnectionSheet = false
                editingConnection = null
            },
            onTestConnection = component::testConnection,
        )
    }

    if (showBucketSheet) {
        BucketSwitcherSheet(
            buckets = buckets,
            currentBucket = currentBucket,
            onDismiss = { showBucketSheet = false },
            onSelect = component::switchBucket,
        )
    }

    if (selectedObject != null) {
        ObjectDetailSheet(
            item = selectedObject!!,
            signedUrl = component.signedUrlFor(selectedObject!!),
            previewHeaders = component.authHeaders(),
            copyLinkEnabled = currentConnection?.protocol != RemoteProtocol.SMB,
            busy = busy,
            onDismiss = component::hideObject,
            onRename = component::renameSelected,
            onMove = component::moveSelected,
            onDelete = { pendingDeleteItems = listOf(selectedObject!!) },
            onDownload = { item ->
                val url = component.signedUrlFor(item)
                if (url != null) {
                    downloadCloudFile(
                        context = context,
                        url = url,
                        fileName = item.displayName,
                        scope = downloadScope,
                        headers = component.authHeaders(),
                    )
                } else {
                    component.saveObjectToDevice(item)
                }
            },
            onCopyLink = {
                component.signedUrlFor(selectedObject!!)?.let { copyToClipboard(context, it) }
            },
        )
    }

    if (pendingDeleteItems.isNotEmpty()) {
        val deleteTargets = pendingDeleteItems
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { pendingDeleteItems = emptyList() },
            title = { Text(stringResource(R.string.cloud_storage_delete_confirm_title)) },
            text = {
                Text(
                    text = if (deleteTargets.size == 1) {
                        stringResource(R.string.cloud_storage_delete_confirm_message_single, deleteTargets.first().displayName)
                    } else {
                        stringResource(R.string.cloud_storage_delete_confirm_message_multi, deleteTargets.size)
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        component.deleteObjects(deleteTargets)
                        selectedItems.clear()
                        pendingDeleteItems = emptyList()
                    },
                    enabled = !busy,
                ) {
                    Text(stringResource(R.string.cloud_storage_delete_confirm_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteItems = emptyList() }) {
                    Text(stringResource(R.string.cloud_storage_cancel))
                }
            },
        )
    }

    if (pendingDeleteConnection != null) {
        val target = pendingDeleteConnection!!
        val targetName = target.displayName.ifBlank { stringResource(target.protocol.titleRes) }
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { pendingDeleteConnection = null },
            title = { Text(stringResource(R.string.cloud_storage_connection_delete_title)) },
            text = { Text(stringResource(R.string.cloud_storage_connection_delete_message, targetName)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        component.removeConnection(target.id)
                        pendingDeleteConnection = null
                    }
                ) {
                    Text(stringResource(R.string.cloud_storage_connection_delete_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteConnection = null }) {
                    Text(stringResource(R.string.cloud_storage_cancel))
                }
            },
        )
    }

    if (showCreateFolderDialog) {
        EnhancedAlertDialog(
            visible = showCreateFolderDialog,
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text(stringResource(R.string.cloud_storage_create_folder)) },
            text = {
                com.wanbaohe.core.ui.widget.system.OneBoxOutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text(stringResource(R.string.cloud_storage_folder_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        component.createFolder(folderName)
                        folderName = ""
                        showCreateFolderDialog = false
                    },
                    enabled = folderName.isNotBlank() && !busy,
                ) {
                    Text(stringResource(R.string.cloud_storage_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) {
                    Text(stringResource(R.string.cloud_storage_cancel))
                }
            }
        )
    }

    if (message != null) {
        EnhancedAlertDialog(
            visible = message != null,
            onDismissRequest = component::dismissMessage,
            title = { Text(stringResource(R.string.cloud_storage_error_title)) },
            text = { Text(message!!) },
            confirmButton = {
                TextButton(onClick = component::dismissMessage) {
                    Text(stringResource(R.string.cloud_storage_confirm))
                }
            }
        )
    }

    if (busy) {
        androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
            androidx.compose.material3.Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                    val progressVal = uploadProgress
                    if (progressVal != null) {
                        Text(
                            text = stringResource(R.string.cloud_storage_upload_progress, (progressVal * 100).toInt()),
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.cloud_storage_loading),
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilesContent(
    browserState: CloudBrowserState,
    isGridMode: Boolean,
    previewUrlProvider: (CloudObjectItem) -> String?,
    previewHeaders: Map<String, String>,
    copyLinkEnabled: Boolean,
    downloadEnabled: Boolean,
    filterQuery: String,
    onClearFilter: () -> Unit,
    onCopyLink: (CloudObjectItem) -> Unit,
    onDownload: (CloudObjectItem) -> Unit,
    onItemClick: (CloudObjectItem) -> Unit,
    selectedKeys: Set<String>,
    selectionMode: Boolean,
    onSelectionToggle: (CloudObjectItem) -> Unit,
    onRefresh: () -> Unit,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (browserState) {
        CloudBrowserState.Idle,
        CloudBrowserState.Loading -> Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimaryContainer)
        }

        is CloudBrowserState.Empty -> CloudStatePlaceholder(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFolder,
            title = stringResource(R.string.cloud_storage_empty_folder_title),
            desc = stringResource(R.string.cloud_storage_empty_folder_desc),
            actionText = stringResource(R.string.cloud_storage_upload_action),
            onActionClick = onUploadClick,
            modifier = modifier
        )

        is CloudBrowserState.Error -> CloudStatePlaceholder(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWarning,
            title = stringResource(R.string.cloud_storage_state_error_title),
            desc = browserState.message,
            actionText = stringResource(R.string.cloud_storage_refresh_list_action),
            onActionClick = onRefresh,
            modifier = modifier
        )

        is CloudBrowserState.Success -> {
            val visibleItems = if (filterQuery.isBlank()) {
                browserState.items
            } else {
                browserState.items.filter {
                    it.displayName.contains(filterQuery.trim(), ignoreCase = true)
                }
            }
            if (visibleItems.isEmpty()) {
                CloudStatePlaceholder(
                    icon = Icons.Outlined.Search,
                    title = stringResource(R.string.cloud_storage_search_empty),
                    desc = "",
                    actionText = stringResource(R.string.cloud_storage_search_clear),
                    onActionClick = onClearFilter,
                    modifier = modifier,
                )
            } else {
                CloudFileGrid(
                    items = visibleItems,
            isGridMode = isGridMode,
            previewUrlProvider = previewUrlProvider,
            previewHeaders = previewHeaders,
            onItemClick = onItemClick,
            selectedKeys = selectedKeys,
            selectionMode = selectionMode,
            onSelectionToggle = onSelectionToggle,
            copyLinkEnabled = copyLinkEnabled,
            downloadEnabled = downloadEnabled,
                    onCopyLink = onCopyLink,
                    onDownload = onDownload,
                    modifier = modifier,
                )
            }
        }
    }
}

@Composable
private fun CloudStatePlaceholder(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = desc,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(text = actionText)
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("cloud-storage-url", text))
}

private fun notifyLinkUnavailable(context: Context) {
    Toast.makeText(
        context,
        context.getString(R.string.cloud_storage_link_unavailable),
        Toast.LENGTH_SHORT,
    ).show()
}

@Composable
private fun CloudNoConnectionGuide(
    onAddManually: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val onNavigate = com.wanbaohe.core.ui.utils.navigation.LocalOnNavigate.current
    EmptyStateGuide(
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCloudStorage,
        title = stringResource(R.string.cloud_storage_no_connection_title),
        description = stringResource(R.string.cloud_storage_no_connection_desc),
        actions = listOf(
            EmptyStateGuideAction(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                title = stringResource(R.string.cloud_storage_no_connection_action),
                onClick = onAddManually,
            ),
            EmptyStateGuideAction(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutoFix,
                title = stringResource(R.string.cloud_storage_ai_assist_title),
                onClick = {
                    onNavigate(
                        com.wanbaohe.core.ui.utils.navigation.Screen.AITabChatScreen(
                            com.shifenmiao.model.ai.Conversation(
                                entryType = com.shifenmiao.model.ai.AIConversationEntryType.ASSISTANT,
                                title = AppContext.getString(R.string.cloud_storage_ai_assist_title),
                                template = AppContext.getString(R.string.cloud_storage_ai_assist_fill_in),
                            )
                        )
                    )
                },
            ),
        ),
        modifier = modifier,
    )
}
