package com.wanbaohe.file_transfer.screen.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.file_transfer.R
import com.shifenmiao.model.transfer.ChatMessage
import com.shifenmiao.model.transfer.MessageType
import androidx.core.content.FileProvider
import com.wanbaohe.file_transfer.util.FileUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolder

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    modifier: Modifier = Modifier,
    serverBaseUrl: String? = null,
    rootPath: String? = null,
    serverPort: Int? = null
) {
    val context = LocalContext.current
    val isFromMobile = message.sender == "mobile"
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    fun openLocalFileIfExists(): Boolean {
        val rp = rootPath ?: return false
        val fp = message.filePath ?: return false

        val safeAbsolute = FileUtils.validatePath(rp, fp) ?: return false
        val file = File(safeAbsolute)
        if (!file.exists() || !file.isFile || !file.canRead()) return false

        val mime = FileUtils.getMimeType(file) ?: "*/*"
        val uri = runCatching {
            FileProvider.getUriForFile(
                context,
                "com.shifenmiao.js.fileprovider",
                file
            )
        }.getOrNull() ?: return false

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        return runCatching {
            context.startActivity(intent)
            true
        }.getOrElse { false }
    }

    fun openViaServerDownload(): Boolean {
        val fp = message.filePath ?: return false

        val base = serverBaseUrl
            ?: serverPort?.let { "http://127.0.0.1:$it" }
            ?: return false

        val url = "$base/api/download?path=" + Uri.encode(fp)

        return runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        }.getOrElse { false }
    }

    val canOpen = message.type == MessageType.FILE && !message.filePath.isNullOrBlank()

    val onFileClick: (() -> Unit)? = if (canOpen) {
        {
            // Prefer opening the local file directly (no extra download over HTTP)
            openLocalFileIfExists() || openViaServerDownload()
        }
    } else {
        null
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppTheme.dimens.spaceExtraSmall),
        horizontalAlignment = if (isFromMobile) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(AppTheme.dimens.cornerRadiusNormal),
            color = if (isFromMobile) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier
                .padding(horizontal = AppTheme.dimens.paddingExtraSmall)
                .then(
                    if (onFileClick != null) {
                        Modifier.clickable { onFileClick() }
                    } else {
                        Modifier
                    }
                )
        ) {
            Column(
                modifier = Modifier.padding(AppTheme.dimens.paddingSmall)
            ) {
                when (message.type) {
                    MessageType.TEXT -> {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    MessageType.FILE -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFolder,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(AppTheme.dimens.spaceSmall))
                            Column {
                                Text(
                                    text = message.fileName
                                        ?: stringResource(R.string.file_transfer_file_default_name),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                message.fileSize?.let { size ->
                                    Text(
                                        text = FileUtils.formatFileSize(size),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Hint (only when filePath is available)
                                if (!message.filePath.isNullOrBlank()) {
                                    Text(
                                        text = stringResource(R.string.file_transfer_tap_to_open),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    MessageType.SYSTEM -> {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppTheme.dimens.spaceExtraSmall))

                Text(
                    text = dateFormat.format(Date(message.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
