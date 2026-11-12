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
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OneBoxListItem
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
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
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
                Icon(imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCloudStorage, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
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
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
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
                Icon(imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFolder, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
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
