package com.wanbaohe.cloud.storage.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxListItem
import com.wanbaohe.cloud.storage.R
import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCloudStorage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolder
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandMore

@Composable
fun CloudStorageTopBar(
    connection: CloudStorageConnection?,
    currentBucket: String?,
    onConnectionClick: () -> Unit,
    onBucketClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
    ) {
        OneBoxListItem(
            modifier = Modifier.weight(1f),
            contained = true,
            headlineContent = {
                Text(
                    text = connection?.displayName ?: stringResource(R.string.cloud_storage_no_connection),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            subtitle = {
                Text(
                    text = if (connection != null) {
                        connectionSubtitle(connection)
                    } else {
                        stringResource(R.string.cloud_storage_connection_hint)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingContent = {
                Icon(imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCloudStorage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            trailingContent = {
                Icon(imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandMore, contentDescription = null)
            },
            onClick = onConnectionClick,
        )
        OneBoxListItem(
            modifier = Modifier.weight(1f),
            contained = true,
            headlineContent = {
                Text(
                    text = currentBucket ?: stringResource(R.string.cloud_storage_no_bucket),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            subtitle = {
                Text(
                    text = if (currentBucket != null) {
                        stringResource(R.string.cloud_storage_bucket_label)
                    } else {
                        stringResource(R.string.cloud_storage_bucket_hint)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingContent = {
                Icon(imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFolder, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            },
            trailingContent = {
                Icon(imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandMore, contentDescription = null)
            },
            onClick = onBucketClick,
        )
    }
}

private fun connectionSubtitle(connection: CloudStorageConnection): String = when (connection) {
    is CloudStorageConnection.S3Compat ->
        connection.endpoint.substringAfter("://").substringBefore("/")
    is CloudStorageConnection.WebDav ->
        connection.baseUrl.substringAfter("://").substringBefore("/")
    is CloudStorageConnection.Smb ->
        "${connection.host}:${connection.port}"
}
