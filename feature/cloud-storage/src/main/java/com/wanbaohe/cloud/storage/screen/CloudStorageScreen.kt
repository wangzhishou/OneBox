package com.wanbaohe.cloud.storage.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalIconButton
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxBottomActionBar
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionCard
import com.wanbaohe.cloud.storage.R
import com.wanbaohe.cloud.storage.model.CloudBrowserState
import com.wanbaohe.cloud.storage.model.CloudObjectItem
import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import com.wanbaohe.cloud.storage.screen.components.BucketSwitcherSheet
import com.wanbaohe.cloud.storage.screen.components.CloudFileGrid
import com.wanbaohe.cloud.storage.screen.components.ConnectionSheet
import com.wanbaohe.cloud.storage.screen.components.ConnectionSwitcherSheet
import com.wanbaohe.cloud.storage.screen.components.ObjectDetailSheet
import com.wanbaohe.cloud.storage.screen.components.SearchTabContent
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

    var showConnectionSheet by remember { mutableStateOf(false) }
    var showConnectionSwitcher by remember { mutableStateOf(false) }
    var editingConnection by remember { mutableStateOf<CloudStorageConnection?>(null) }
    var showBucketSheet by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var folderName by remember { mutableStateOf("") }
    val selectedItems = remember { mutableStateMapOf<String, CloudObjectItem>() }
    var pendingDeleteItems by remember { mutableStateOf<List<CloudObjectItem>>(emptyList()) }

    val uploadLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let(component::uploadFromUri)
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
                        modifier = Modifier.size(20.dp),
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
            IconButton(onClick = { showConnectionSheet = true }) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                    contentDescription = null,
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
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (currentPrefix.isNotBlank()) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.ArrowUpward else Icons.Outlined.Folder,
                                contentDescription = null,
                                tint = if (currentPrefix.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        if (currentPrefix.isBlank()) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { showBucketSheet = true }
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = bucket,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandMore,
                                    contentDescription = stringResource(R.string.cloud_storage_bucket_sheet_title),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        } else {
                            Text(
                                text = currentPrefix,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = component::toggleViewMode,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isGridMode) Icons.AutoMirrored.Outlined.ViewList else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFeatures,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = component::refresh,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                                contentDescription = stringResource(R.string.cloud_storage_refresh_action),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (currentConnection == null) {
                CloudStatePlaceholder(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCloudStorage,
                    title = stringResource(R.string.cloud_storage_no_connection_title),
                    desc = stringResource(R.string.cloud_storage_no_connection_desc),
                    actionText = stringResource(R.string.cloud_storage_no_connection_action),
                    onActionClick = { showConnectionSheet = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                FilesContent(
                    browserState = browserState,
                    isGridMode = isGridMode,
                    signedUrlProvider = component::signedUrlFor,
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

        if (selectedItems.isNotEmpty()) {
            OneBoxBottomActionBar(
                primaryText = stringResource(R.string.cloud_storage_delete_selected_action, selectedItems.size),
                onPrimaryClick = { pendingDeleteItems = selectedItems.values.toList() },
                secondaryText = stringResource(R.string.cloud_storage_cancel_selection),
                onSecondaryClick = { selectedItems.clear() },
                primaryEnabled = !busy,
                secondaryEnabled = !busy,
                extraActions = {
                    GlassTonalIconButton(
                        onClick = { pendingDeleteItems = selectedItems.values.toList() },
                        enabled = !busy,
                    ) {
                        Icon(imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete, contentDescription = null)
                    }
                },
            )
        } else {
            OneBoxBottomActionBar(
                primaryText = stringResource(R.string.cloud_storage_upload_action),
                onPrimaryClick = { uploadLauncher.launch(arrayOf("*/*")) },
                secondaryText = stringResource(R.string.cloud_storage_create_folder),
                onSecondaryClick = { showCreateFolderDialog = true },
                extraActions = {
                    GlassTonalIconButton(onClick = { showConnectionSheet = true }) {
                        Icon(imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add, contentDescription = null)
                    }
                    GlassTonalIconButton(onClick = { uploadLauncher.launch(arrayOf("*/*")) }) {
                        Icon(imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCloudUpload, contentDescription = null)
                    }
                },
            )
        }
    }


    if (showConnectionSwitcher) {
        ConnectionSwitcherSheet(
            connections = connections,
            currentConnectionId = currentConnection?.id,
            onDismiss = { showConnectionSwitcher = false },
            onSwitch = { component.switchConnection(it) },
            onManage = {
                showConnectionSwitcher = false
                editingConnection = currentConnection
                showConnectionSheet = true
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
            busy = busy,
            onDismiss = component::hideObject,
            onRename = component::renameSelected,
            onMove = component::moveSelected,
            onDelete = { pendingDeleteItems = listOf(selectedObject!!) },
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

    if (showCreateFolderDialog) {
        EnhancedAlertDialog(
            visible = showCreateFolderDialog,
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text(stringResource(R.string.cloud_storage_create_folder)) },
            text = {
                com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxOutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text(stringResource(R.string.cloud_storage_folder_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (folderName.isNotBlank()) {
                        component.createFolder(folderName)
                        folderName = ""
                        showCreateFolderDialog = false
                    }
                }) {
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
                color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
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
    signedUrlProvider: (CloudObjectItem) -> String?,
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
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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

        is CloudBrowserState.Success -> CloudFileGrid(
            items = browserState.items,
            isGridMode = isGridMode,
            signedUrlProvider = signedUrlProvider,
            onItemClick = onItemClick,
            selectedKeys = selectedKeys,
            selectionMode = selectionMode,
            onSelectionToggle = onSelectionToggle,
            modifier = modifier,
        )
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
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
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
                    containerColor = MaterialTheme.colorScheme.primary
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
