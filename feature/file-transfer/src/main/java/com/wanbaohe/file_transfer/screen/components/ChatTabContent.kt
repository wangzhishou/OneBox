package com.wanbaohe.file_transfer.screen.components

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.model.transfer.ChatMessage
import com.shifenmiao.model.transfer.ChatSession
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.FileType
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFilePicker
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.helper.isPortraitOrientationAsState
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalComponentActivity
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.file_transfer.R
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSend
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAttachFile

@Composable
fun ChatTabContent(
    sessions: List<ChatSession>,
    selectedChannelId: String?,
    onSelectSession: (String) -> Unit,
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onSendFileFromUri: (Uri) -> Unit,
    webSocketConnections: Int,
    unreadCount: Int,
    connectedClients: Int,
    modifier: Modifier = Modifier,
    serverBaseUrl: String? = null,
    rootPath: String? = null,
    serverPort: Int? = null,
    isServerRunning: Boolean = false
) {
    val isPortrait by isPortraitOrientationAsState()

    if (isPortrait) {
        // Phone portrait: sessions as a top horizontal row; messages/composer below.
        Column(
            modifier = modifier
                .fillMaxSize()
        ) {
            if(sessions.size > 1) {
                Text(
                    text = stringResource(R.string.file_transfer_tab_chat),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = AppTheme.dimens.spaceSmall)
                )

                ChatSessionRow(
                    sessions = sessions,
                    selectedChannelId = selectedChannelId,
                    onSelect = onSelectSession,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(AppTheme.dimens.spaceSmall))
            }
            ChatMessagesPane(
                sessionsCount = sessions.size,
                selectedChannelId = selectedChannelId,
                webSocketConnections = webSocketConnections,
                connectedClients = connectedClients,
                unreadCount = unreadCount,
                messages = messages,
                onSendMessage = onSendMessage,
                onSendFileFromUri = onSendFileFromUri,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                enabled = selectedChannelId != null || sessions.isNotEmpty(),
                serverBaseUrl = serverBaseUrl,
                rootPath = rootPath,
                serverPort = serverPort,
                isServerRunning = isServerRunning
            )
        }
    } else {
        // Landscape / tablet: keep current split layout.
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(AppTheme.dimens.paddingNormal)
        ) {
            Column(
                modifier = Modifier
                    .width(160.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = stringResource(R.string.file_transfer_tab_chat),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = AppTheme.dimens.spaceSmall)
                )

                ChatSessionList(
                    sessions = sessions,
                    selectedChannelId = selectedChannelId,
                    onSelect = onSelectSession,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(AppTheme.dimens.spaceNormal))

            ChatMessagesPane(
                sessionsCount = sessions.size,
                selectedChannelId = selectedChannelId,
                webSocketConnections = webSocketConnections,
                connectedClients = connectedClients,
                unreadCount = unreadCount,
                messages = messages,
                onSendMessage = onSendMessage,
                onSendFileFromUri = onSendFileFromUri,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                enabled = selectedChannelId != null || sessions.isNotEmpty(),
                serverBaseUrl = serverBaseUrl,
                rootPath = rootPath,
                serverPort = serverPort,
                isServerRunning = isServerRunning
            )
        }
    }
}

@Composable
private fun ChatMessagesPane(
    sessionsCount: Int,
    selectedChannelId: String?,
    webSocketConnections: Int,
    connectedClients: Int,
    unreadCount: Int,
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onSendFileFromUri: (Uri) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    serverBaseUrl: String? = null,
    rootPath: String? = null,
    serverPort: Int? = null,
    isServerRunning: Boolean = false
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = modifier) {
        ChatStatusBar(
            sessionsCount = sessionsCount,
            selectedChannelId = selectedChannelId,
            webSocketConnections = webSocketConnections,
            connectedClients = connectedClients,
            unreadCount = unreadCount,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(AppTheme.dimens.spaceSmall))

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSmall)
        ) {
            if (messages.isEmpty()) {
                item {
                    ChatEmptyState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = AppTheme.dimens.paddingLarge),
                        hasSession = sessionsCount > 0,
                        isServerRunning = isServerRunning
                    )
                }
            } else {
                items(messages) { message ->
                    ChatMessageItem(
                        message = message,
                        serverBaseUrl = serverBaseUrl,
                        rootPath = rootPath,
                        serverPort = serverPort
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.dimens.spaceNormal))

        ChatComposer(
            onSendMessage = onSendMessage,
            onSendFileFromUri = onSendFileFromUri,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )
    }
}

@Composable
private fun ChatStatusBar(
    sessionsCount: Int,
    selectedChannelId: String?,
    webSocketConnections: Int,
    connectedClients: Int,
    unreadCount: Int,
    modifier: Modifier = Modifier
) {
    val text = buildString {
        append("Sessions: ").append(sessionsCount)
        append("  |  HTTP: ").append(connectedClients)
        append("  |  WS: ").append(webSocketConnections)
        append("  |  Unread: ").append(unreadCount)
        if (!selectedChannelId.isNullOrBlank()) {
            append("  |  Channel: ")
            append(selectedChannelId.take(12))
        }
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Composable
private fun ChatEmptyState(
    modifier: Modifier = Modifier,
    hasSession: Boolean = true,
    isServerRunning: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.file_transfer_chat_empty_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(AppTheme.dimens.spaceSmall))

        // First show a concise status tip that depends on server state
        Text(
            text = stringResource(
                if (isServerRunning) {
                    R.string.file_transfer_chat_empty_server_running
                } else {
                    R.string.file_transfer_chat_empty_server_stopped
                }
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(AppTheme.dimens.spaceSmall))

        Text(
            text = stringResource(R.string.file_transfer_chat_empty_guide),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!hasSession) {
            Spacer(modifier = Modifier.height(AppTheme.dimens.spaceSmall))
            Text(
                text = stringResource(R.string.file_transfer_chat_empty_no_session),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChatComposer(
    onSendMessage: (String) -> Unit,
    onSendFileFromUri: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var messageText by remember { mutableStateOf("") }

    val localActivity = LocalComponentActivity.current
    val filePicker = rememberFilePicker(
        type = FileType.Single,
        onFailure = remember( localActivity) {
            {
                AppToastHost.showToast(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                    message = localActivity.getString(R.string.cancel),
                )
            }
        },
        onSuccess = { uris ->
            uris.firstOrNull()?.let(onSendFileFromUri)
        }
    )

    Row(
        modifier = modifier.padding(bottom = 2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        GlassOutlinedTextField(
            value = messageText,
            onValueChange = { messageText = it },
            placeholder = { Text(stringResource(R.string.file_transfer_chat_input_hint)) },
            modifier = Modifier.weight(1f),
            maxLines = 4,
            enabled = enabled,
            leadingIcon = {
                IconButton(
                    onClick = { filePicker.pickFile() },
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAttachFile,
                        contentDescription = stringResource(R.string.file_transfer_cd_send),
                        tint = if (enabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        val text = messageText.trim()
                        if (text.isNotEmpty()) {
                            onSendMessage(text)
                            messageText = ""
                        }
                    },
                    enabled = enabled && messageText.isNotBlank()
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSend,
                        contentDescription = stringResource(R.string.file_transfer_cd_send),
                        tint = if (enabled && messageText.isNotBlank()) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        )
    }
}
