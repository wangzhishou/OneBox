package com.wanbaohe.cloud.storage.screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OnePrimaryButton
import com.t8rin.imagetoolbox.core.ui.widget.system.OneSecondaryButton
import com.wanbaohe.cloud.storage.R
import com.wanbaohe.cloud.storage.model.CloudObjectItem
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage

@Composable
fun ObjectDetailSheet(
    item: CloudObjectItem,
    signedUrl: String?,
    busy: Boolean,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit,
    onMove: (String) -> Unit,
    onDelete: () -> Unit,
    onCopyLink: () -> Unit,
) {
    val context = LocalContext.current
    var renameText by remember(item.key) { mutableStateOf(item.displayName) }
    var movePrefix by remember(item.key) { mutableStateOf(item.prefix) }

    EnhancedModalBottomSheet(
        visible = true,
        onDismiss = { if (!it) onDismiss() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(OneBoxDesignSystem.screenPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing),
        ) {
            Text(
                text = item.displayName,
                style = MaterialTheme.typography.titleLarge,
            )
            OneBoxSectionCard {
                if (item.isImage && !signedUrl.isNullOrBlank()) {
                    var isError by remember(signedUrl) { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
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
                                model = signedUrl,
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
                Text(text = item.key, style = MaterialTheme.typography.bodySmall)
                Text(text = item.contentType ?: "application/octet-stream", style = MaterialTheme.typography.bodySmall)
                Text(text = item.lastModified.orEmpty(), style = MaterialTheme.typography.bodySmall)
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
                    enabled = renameText.isNotBlank() && !busy,
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
                    enabled = !busy,
                )
                OneSecondaryButton(
                    text = stringResource(R.string.cloud_storage_copy_download_link),
                    onClick = onCopyLink,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy,
                )
                OneSecondaryButton(
                    text = stringResource(R.string.cloud_storage_download_local),
                    onClick = {
                        signedUrl?.let { url ->
                            downloadCloudFile(context, url, item.displayName)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy && !signedUrl.isNullOrBlank(),
                )
                OneSecondaryButton(
                    text = stringResource(R.string.cloud_storage_delete_action),
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !busy,
                )
            }
        }
    }
}
