/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.cloud.storage.screen.components

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.helper.SaveSuccessNotifier
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OneBoxSectionCard
import com.wanbaohe.cloud.storage.R
import com.wanbaohe.cloud.storage.model.CloudObjectItem
import java.text.DecimalFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.CheckCircle
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolder
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDescription
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDownload
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage

private const val DOWNLOAD_POLL_INTERVAL_MS = 500L
private const val DOWNLOAD_WATCH_TIMEOUT_MS = 120_000L

fun downloadCloudFile(
    context: Context,
    url: String,
    fileName: String,
    scope: CoroutineScope,
    headers: Map<String, String> = emptyMap(),
) {
    try {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setDescription("Downloading file...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .apply {
                // WebDAV 等需要认证的地址：DownloadManager 支持自定义请求头
                headers.forEach { (name, value) -> addRequestHeader(name, value) }
            }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)
        Toast.makeText(context, context.getString(R.string.cloud_storage_download_queued), Toast.LENGTH_SHORT).show()
        watchCloudDownload(context, downloadManager, downloadId, fileName, scope)
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.cloud_storage_download_failed, e.localizedMessage), Toast.LENGTH_SHORT).show()
    }
}

/**
 * 轮询 DownloadManager 下载状态：成功后走标准保存成功管线（带「打开」按钮的 toast + 撒花 +
 * ActivityLog），失败弹失败 toast；页面销毁时随传入的协程作用域自然取消。
 */
private fun watchCloudDownload(
    context: Context,
    downloadManager: DownloadManager,
    downloadId: Long,
    fileName: String,
    scope: CoroutineScope
) {
    scope.launch {
        runCatching {
            val deadline = System.currentTimeMillis() + DOWNLOAD_WATCH_TIMEOUT_MS
            while (System.currentTimeMillis() < deadline) {
                delay(DOWNLOAD_POLL_INTERVAL_MS)
                downloadManager.query(
                    DownloadManager.Query().setFilterById(downloadId)
                )?.use { cursor ->
                    if (!cursor.moveToFirst()) return@use
                    when (cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            downloadManager.getUriForDownloadedFile(downloadId)?.toString()?.let { fileUri ->
                                SaveSuccessNotifier.notifyFileSaved(
                                    fileUri = fileUri,
                                    fileName = fileName,
                                    savingPath = Environment.DIRECTORY_DOWNLOADS
                                )
                            }
                            return@launch
                        }

                        DownloadManager.STATUS_FAILED -> {
                            AppToastHost.showFailureToast(
                                com.t8rin.imagetoolbox.core.resources.R.string.something_went_wrong
                            )
                            return@launch
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CloudFileGrid(
    items: List<CloudObjectItem>,
    isGridMode: Boolean,
    previewUrlProvider: (CloudObjectItem) -> String?,
    previewHeaders: Map<String, String> = emptyMap(),
    onItemClick: (CloudObjectItem) -> Unit,
    selectedKeys: Set<String> = emptySet(),
    selectionMode: Boolean = false,
    onSelectionToggle: (CloudObjectItem) -> Unit = {},
    copyLinkEnabled: Boolean = false,
    downloadEnabled: Boolean = false,
    onCopyLink: (CloudObjectItem) -> Unit = {},
    onDownload: (CloudObjectItem) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (isGridMode) {
        LazyVerticalGrid(
            modifier = modifier.fillMaxSize(),
            columns = GridCells.Adaptive(minSize = 140.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
            horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
        ) {
            items(items, key = { it.key }) { item ->
                CloudFileGridCard(
                    item = item,
                    previewUrl = previewUrlFor(item, previewUrlProvider),
                    previewHeaders = previewHeaders,
                    selected = item.key in selectedKeys,
                    selectionMode = selectionMode,
                    onClick = { if (selectionMode) onSelectionToggle(item) else onItemClick(item) },
                    onLongClick = { onSelectionToggle(item) },
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
        ) {
            items(items, key = { it.key }) { item ->
                CloudFileListRow(
                    item = item,
                    previewUrl = previewUrlFor(item, previewUrlProvider),
                    previewHeaders = previewHeaders,
                    selected = item.key in selectedKeys,
                    selectionMode = selectionMode,
                    onClick = { if (selectionMode) onSelectionToggle(item) else onItemClick(item) },
                    onLongClick = { onSelectionToggle(item) },
                    copyLinkEnabled = copyLinkEnabled && !item.isDirectory,
                    downloadEnabled = downloadEnabled && !item.isDirectory,
                    onCopyLink = { onCopyLink(item) },
                    onDownload = { onDownload(item) },
                )
            }
        }
    }
}

/** 只有图片预览需要链接；目录和普通文件不生成签名 URL。 */
private fun previewUrlFor(
    item: CloudObjectItem,
    provider: (CloudObjectItem) -> String?,
): String? = if (item.isImage && !item.isDirectory) provider(item) else null

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CloudFileGridCard(
    item: CloudObjectItem,
    previewUrl: String?,
    previewHeaders: Map<String, String>,
    selected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val context = LocalContext.current
    val previewModel = remember(previewUrl, previewHeaders) {
        cloudPreviewModel(context, previewUrl, previewHeaders)
    }
    OneBoxSectionCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(OneBoxDesignSystem.sectionCardShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            when {
                item.isDirectory -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFolder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
                item.isImage && previewModel != null -> {
                    var isError by remember(previewModel) { mutableStateOf(false) }
                    if (isError) {
                        Surface(
                            modifier = Modifier.size(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    } else {
                        AsyncImage(
                            model = previewModel,
                            contentDescription = item.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            onState = { state ->
                                isError = state is coil3.compose.AsyncImagePainter.State.Error
                            }
                        )
                    }
                }
                item.isImage -> {
                    Surface(
                        modifier = Modifier.size(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                else -> {
                    Surface(
                        modifier = Modifier.size(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDescription,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
            if (selectionMode || selected) {
                SelectionIndicator(
                    selected = selected,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = item.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (item.isDirectory) stringResource(R.string.cloud_storage_prefix_item) else formatSize(item.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CloudFileListRow(
    item: CloudObjectItem,
    previewUrl: String?,
    previewHeaders: Map<String, String>,
    selected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    copyLinkEnabled: Boolean,
    downloadEnabled: Boolean,
    onCopyLink: () -> Unit,
    onDownload: () -> Unit,
) {
    val context = LocalContext.current
    val previewModel = remember(previewUrl, previewHeaders) {
        cloudPreviewModel(context, previewUrl, previewHeaders)
    }
    OneBoxSectionCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(OneBoxDesignSystem.sectionCardShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectionMode || selected) {
                SelectionIndicator(selected = selected)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    item.isDirectory -> {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFolder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    item.isImage && previewModel != null -> {
                        var isError by remember(previewModel) { mutableStateOf(false) }
                        if (isError) {
                            FileTypeTile(
                                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage,
                                container = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                content = MaterialTheme.colorScheme.onSecondaryContainer,
                                iconSize = 22.dp,
                            )
                        } else {
                            AsyncImage(
                                model = previewModel,
                                contentDescription = item.displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                onState = { state ->
                                    isError = state is coil3.compose.AsyncImagePainter.State.Error
                                }
                            )
                        }
                    }

                    item.isImage -> {
                        FileTypeTile(
                            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage,
                            container = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                            content = MaterialTheme.colorScheme.onSecondaryContainer,
                            iconSize = 22.dp,
                        )
                    }

                    else -> {
                        FileTypeTile(
                            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDescription,
                            container = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                            content = MaterialTheme.colorScheme.onTertiaryContainer,
                            iconSize = 22.dp,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (item.isDirectory) stringResource(R.string.cloud_storage_prefix_item) else formatSize(item.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    val modified = formatRemoteTimestamp(item.lastModified)
                    if (modified != null) {
                        Text(
                            text = modified,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 触达区 40dp：原来 28dp/14dp 图标几乎点不中；复制/下载的链接在点击时才生成。
            if (copyLinkEnabled || downloadEnabled) {
                Spacer(modifier = Modifier.width(4.dp))
                if (copyLinkEnabled) {
                    IconButton(
                        onClick = onCopyLink,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                            contentDescription = stringResource(R.string.cloud_storage_copy_link_action),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (downloadEnabled) {
                    IconButton(
                        onClick = onDownload,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDownload,
                            contentDescription = stringResource(R.string.cloud_storage_download_icon_action),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FileTypeTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    container: androidx.compose.ui.graphics.Color,
    content: androidx.compose.ui.graphics.Color,
    iconSize: Dp,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(8.dp),
        color = container
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@Composable
private fun SelectionIndicator(
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.size(22.dp),
        shape = RoundedCornerShape(11.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.72f)
        },
    ) {
        if (selected) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(2.dp),
            )
        }
    }
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceAtMost(units.size - 1)
    return DecimalFormat("#,##0.#").format(bytes / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
}
