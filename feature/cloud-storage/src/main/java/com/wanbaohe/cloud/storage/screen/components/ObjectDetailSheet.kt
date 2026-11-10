package com.wanbaohe.cloud.storage.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.wanbaohe.core.ui.widget.system.OneBoxDangerButton
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.system.OneBoxOutlinedTextField
import com.wanbaohe.core.ui.widget.system.OneBoxSectionCard
import com.wanbaohe.core.ui.widget.system.OnePrimaryButton
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton
import com.wanbaohe.cloud.storage.R
import com.wanbaohe.cloud.storage.model.CloudObjectItem
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage

@Composable
fun ObjectDetailSheet(
    item: CloudObjectItem,
    signedUrl: String?,
    previewHeaders: Map<String, String>,
    copyLinkEnabled: Boolean,
    busy: Boolean,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit,
    onMove: (String) -> Unit,
    onDelete: () -> Unit,
    onCopyLink: () -> Unit,
    onDownload: (CloudObjectItem) -> Unit,
) {
    var renameText by remember(item.key) { mutableStateOf(item.displayName) }
    var movePrefix by remember(item.key) { mutableStateOf(item.prefix) }
    val renameChanged = renameText.trim().isNotEmpty() && renameText.trim() != item.displayName
    val context = LocalContext.current
    val previewModel = remember(signedUrl, previewHeaders) {
        cloudPreviewModel(context, signedUrl, previewHeaders)
    }

    EnhancedModalBottomSheet(
        visible = true,
        onDismiss = { if (!it) onDismiss() },
        title = item.displayName,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(OneBoxDesignSystem.screenPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing),
        ) {
            OneBoxSectionCard {
                if (item.isImage && previewModel != null) {
                    var isError by remember(previewModel) { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isError) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(64.dp)
                            )
                        } else {
                            AsyncImage(
                                model = previewModel,
                                contentDescription = item.displayName,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize(),
                                onState = { state ->
                                    isError = state is coil3.compose.AsyncImagePainter.State.Error
                                }
                            )
                        }
                    }
                }
                DetailRow(
                    label = stringResource(R.string.cloud_storage_detail_location),
                    value = item.key,
                )
                item.contentType?.takeIf { it.isNotBlank() }?.let {
                    DetailRow(
                        label = stringResource(R.string.cloud_storage_detail_type),
                        value = it,
                    )
                }
                formatRemoteTimestamp(item.lastModified)?.let {
                    DetailRow(
                        label = stringResource(R.string.cloud_storage_detail_modified),
                        value = it,
                    )
                }
            }
            OneBoxSectionCard {
                OneBoxOutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text(stringResource(R.string.cloud_storage_rename_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OnePrimaryButton(
                    text = stringResource(R.string.cloud_storage_rename_action),
                    onClick = { onRename(renameText) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = renameChanged && !busy,
                )
                OneBoxOutlinedTextField(
                    value = movePrefix,
                    onValueChange = { movePrefix = it },
                    label = { Text(stringResource(R.string.cloud_storage_move_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OnePrimaryButton(
                    text = stringResource(R.string.cloud_storage_move_action),
                    onClick = { onMove(movePrefix) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy && movePrefix.trim() != item.prefix.trim(),
                )
                OneSecondaryButton(
                    text = stringResource(R.string.cloud_storage_copy_download_link),
                    onClick = onCopyLink,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy && copyLinkEnabled,
                )
                OneSecondaryButton(
                    text = stringResource(R.string.cloud_storage_download_local),
                    onClick = { onDownload(item) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy,
                )
                OneBoxDangerButton(
                    text = stringResource(R.string.cloud_storage_delete_action),
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy,
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 12.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}
