package com.t8rin.imagetoolbox.feature.zip.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedLoadingIndicator
import com.t8rin.imagetoolbox.core.ui.widget.text.AutoSizeText
import com.t8rin.imagetoolbox.feature.zip.domain.ZipProgress
import kotlin.math.roundToInt
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineError
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFileOpen
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolder
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInfo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMore
import com.t8rin.imagetoolbox.core.resources.icons.line.LineZip
import com.t8rin.imagetoolbox.core.resources.R as CoreR
import com.t8rin.imagetoolbox.core.ui.utils.file.FileTypeIconProvider
import com.t8rin.imagetoolbox.core.ui.utils.file.FileVisualCategory
import com.t8rin.imagetoolbox.core.ui.widget.file.FileTypeBadge
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassRegular
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedDropdownMenu
import com.t8rin.imagetoolbox.feature.zip.R
import com.t8rin.imagetoolbox.feature.zip.domain.ZipArchiveEntry
import com.t8rin.imagetoolbox.feature.zip.presentation.formatZipTimestamp
import com.t8rin.imagetoolbox.feature.zip.presentation.rememberFormattedSize
import com.t8rin.imagetoolbox.feature.zip.presentation.screenLogic.ZipBrowserItem

/**
 * 压缩包信息卡：ZIP 图标 + 名称 + 文件数/体积/压缩率。
 */
@Composable
internal fun ZipArchiveHeaderCard(
    name: String,
    fileCount: Int,
    totalSize: Long,
    compressionPercent: Int?,
    modifier: Modifier = Modifier
) {
    val formattedSize = rememberFormattedSize(totalSize)
    val summary = if (compressionPercent != null) {
        pluralStringResource(
            R.plurals.zip_archive_summary_compressed,
            fileCount,
            fileCount,
            formattedSize,
            compressionPercent
        )
    } else {
        pluralStringResource(
            R.plurals.zip_archive_summary,
            fileCount,
            fileCount,
            formattedSize
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassRegular(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FileTypeBadge(
            category = FileVisualCategory.Archive,
            size = 56.dp,
            iconSize = 28.dp,
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun ZipFolderPathRow(
    path: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.LineFolder,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = path,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun ZipFolderRow(
    item: ZipBrowserItem.Folder,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassRegular(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FileTypeBadge(category = FileVisualCategory.Folder)

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = pluralStringResource(
                    R.plurals.zip_file_count,
                    item.fileCount,
                    item.fileCount
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun ZipFileRow(
    entry: ZipArchiveEntry,
    onExtract: () -> Unit,
    onShowDetails: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val formattedSize = rememberFormattedSize(entry.size)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassRegular(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            )
            .clickable(onClick = onShowDetails)
            .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FileTypeBadge(
            category = FileTypeIconProvider.categoryOf(
                isDirectory = false,
                extension = entry.extension,
                mimeType = entry.mimeType
            )
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = entry.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formattedSize,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Outlined.LineMore,
                    contentDescription = stringResource(R.string.zip_more_actions),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            EnhancedDropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.zip_extract_file)) },
                    onClick = {
                        menuExpanded = false
                        onExtract()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.LineFileOpen,
                            contentDescription = null
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.zip_entry_details)) },
                    onClick = {
                        menuExpanded = false
                        onShowDetails()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.LineInfo,
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}

@Composable
internal fun ZipEntryDetailsDialog(
    entry: ZipArchiveEntry,
    onDismiss: () -> Unit
) {
    val originalSize = rememberFormattedSize(entry.size)
    val compressedSize = rememberFormattedSize(entry.compressedSize)
    val modified = formatZipTimestamp(entry.lastModified)

    AlertDialog(
        containerColor = AppTheme.colors.getContainerSurfaceColor(),
        onDismissRequest = onDismiss,
        icon = {
            FileTypeBadge(
                category = FileTypeIconProvider.categoryOf(
                    isDirectory = false,
                    extension = entry.extension,
                    mimeType = entry.mimeType
                )
            )
        },
        title = {
            Text(
                text = entry.name,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ZipDetailRow(
                    label = stringResource(R.string.zip_details_path),
                    value = entry.path
                )
                ZipDetailRow(
                    label = stringResource(R.string.zip_details_size),
                    value = originalSize
                )
                ZipDetailRow(
                    label = stringResource(R.string.zip_details_compressed_size),
                    value = compressedSize
                )
                if (modified.isNotBlank()) {
                    ZipDetailRow(
                        label = stringResource(R.string.zip_details_modified),
                        value = modified
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(CoreR.string.ok))
            }
        }
    )
}

@Composable
private fun ZipDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(104.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
internal fun ZipLoadingState(
    progress: ZipProgress,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (progress.totalBytes > 0L) {
            Box(
                modifier = Modifier.size(108.dp),
                contentAlignment = Alignment.Center
            ) {
                EnhancedLoadingIndicator(
                    progress = progress.fraction,
                    additionalContent = { loaderSize ->
                        AutoSizeText(
                            text = stringResource(
                                R.string.zip_progress_percent,
                                (progress.fraction * 100).roundToInt()
                            ),
                            modifier = Modifier.width(loaderSize * 0.8f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                )
            }
        } else {
            CircularProgressIndicator(modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.zip_loading),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun ZipArchiveErrorState(
    isUnsupportedFormat: Boolean,
    isEncrypted: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.LineError,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.zip_open_failed),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        if (isUnsupportedFormat || isEncrypted) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(
                    if (isEncrypted) R.string.zip_encrypted_open else R.string.zip_only_zip_supported
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onRetry) {
            Text(stringResource(R.string.zip_retry))
        }
    }
}

@Composable
internal fun ZipEmptyArchiveState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.LineZip,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
