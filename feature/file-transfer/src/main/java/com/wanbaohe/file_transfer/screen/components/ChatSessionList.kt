package com.wanbaohe.file_transfer.screen.components

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.shifenmiao.model.transfer.ChatSession

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
    Surface(
        tonalElevation = if (selected) 2.dp else 0.dp,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.clickable(onClick = onClick)
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
