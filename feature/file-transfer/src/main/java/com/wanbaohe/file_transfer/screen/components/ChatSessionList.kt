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

package com.wanbaohe.file_transfer.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.shifenmiao.model.transfer.ChatSession
import com.wanbaohe.core.ui.widget.glass.GlassSurface

@Composable
fun ChatSessionList(
    sessions: List<ChatSession>,
    selectedChannelId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(sessions, key = { it.channelId }) { session ->
            ChatSessionCard(
                session = session,
                selected = session.channelId == selectedChannelId,
                onClick = { onSelect(session.channelId) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ChatSessionRow(
    sessions: List<ChatSession>,
    selectedChannelId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state = rememberLazyListState()

    LazyRow(
        state = state,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sessions, key = { it.channelId }) { session ->
            ChatSessionCard(
                session = session,
                selected = session.channelId == selectedChannelId,
                onClick = { onSelect(session.channelId) },
                modifier = Modifier.width(170.dp)
            )
        }
    }
}

@Composable
private fun ChatSessionCard(
    session: ChatSession,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassSurface(
        onClick = onClick,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.dimens.paddingNormal, vertical = 10.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = session.deviceName?.takeIf { it.isNotBlank() } ?: session.channelId.take(8),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = session.messageCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val subtitle = listOfNotNull(session.lastSender, session.lastContent)
                .joinToString(": ")
                .takeIf { it.isNotBlank() }
                ?: ""

            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
