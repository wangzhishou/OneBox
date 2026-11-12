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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.widget.enhanced.EnhancedDropdownMenu
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.cloud.storage.R
import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCloudStorage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMore

@Composable
fun ConnectionSwitcherSheet(
    connections: List<CloudStorageConnection>,
    currentConnectionId: String?,
    onDismiss: () -> Unit,
    onSwitch: (String) -> Unit,
    onAdd: () -> Unit,
    onEdit: (CloudStorageConnection) -> Unit,
    onDelete: (CloudStorageConnection) -> Unit,
) {
    EnhancedModalBottomSheet(
        visible = true,
        onDismiss = { if (!it) onDismiss() },
        title = stringResource(R.string.cloud_storage_connection_label),
        endContent = {
            TextButton(onClick = onAdd) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(com.shifenmiao.core.R.string.new_add),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(OneBoxDesignSystem.screenPadding),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing),
        ) {
            if (connections.isEmpty()) {
                GlassSurface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCloudStorage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(32.dp),
                        )
                        Text(
                            text = stringResource(R.string.cloud_storage_no_connection),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    connections.forEach { conn ->
                        val isCurrent = conn.id == currentConnectionId
                        val tint = if (isCurrent) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                        var menuExpanded by remember(conn.id) { mutableStateOf(false) }

                        GlassSurface(
                            onClick = {
                                onSwitch(conn.id)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCurrent) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainer
                            },
                            style = GlassStyle.Regular,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 20.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = conn.displayName.ifBlank {
                                                stringResource(conn.protocol.titleRes)
                                            },
                                            style = MaterialTheme.typography.titleMedium,
                                            color = tint,
                                            maxLines = 1,
                                        )
                                        if (isCurrent) {
                                            Icon(
                                                imageVector = Icons.Outlined.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier
                                                    .padding(start = 6.dp)
                                                    .size(16.dp),
                                            )
                                        }
                                    }
                                    Text(
                                        text = connectionSubtitle(conn),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                    )
                                }

                                // 更多：编辑 / 删除，用全局气泡菜单
                                Box {
                                    IconButton(
                                        onClick = { menuExpanded = true },
                                        modifier = Modifier.size(40.dp),
                                    ) {
                                        Icon(
                                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMore,
                                            contentDescription = stringResource(R.string.cloud_storage_manage_connections),
                                            tint = if (isCurrent) {
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                    EnhancedDropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false },
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.cloud_storage_edit_connection)) },
                                            onClick = {
                                                menuExpanded = false
                                                onEdit(conn)
                                            },
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stringResource(R.string.cloud_storage_delete_connection),
                                                    color = MaterialTheme.colorScheme.error,
                                                )
                                            },
                                            onClick = {
                                                menuExpanded = false
                                                onDelete(conn)
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun connectionSubtitle(c: CloudStorageConnection): String = when (c) {
    is CloudStorageConnection.S3Compat ->
        c.endpoint.removePrefix("https://").removePrefix("http://").substringBefore("/")
    is CloudStorageConnection.WebDav ->
        c.baseUrl.removePrefix("https://").removePrefix("http://").substringBefore("/")
    is CloudStorageConnection.Smb -> "${c.host}:${c.port}"
}
