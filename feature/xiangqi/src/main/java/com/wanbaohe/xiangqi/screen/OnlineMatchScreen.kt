package com.wanbaohe.xiangqi.screen

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shifenmiao.common.handle.navigation.AppNavigationRegistry
import com.shifenmiao.common.handle.navigation.AppNavigationTargetType
import com.shifenmiao.storage.TokenStorage
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.ui.utils.capturable.CaptureController
import com.t8rin.imagetoolbox.core.ui.utils.capturable.capturable
import com.t8rin.imagetoolbox.core.ui.utils.capturable.rememberCaptureController
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalImageShareProvider
import com.t8rin.imagetoolbox.core.ui.widget.other.BarcodeType
import com.t8rin.imagetoolbox.core.ui.widget.other.QrCode
import com.t8rin.imagetoolbox.core.ui.widget.other.QrCodeParams
import com.wanbaohe.xiangqi.R
import com.wanbaohe.xiangqi.application.port.outbound.RoomInfo
import com.wanbaohe.xiangqi.application.usecase.OnlinePlayUseCase
import com.wanbaohe.xiangqi.domain.FenCodec
import com.wanbaohe.xiangqi.domain.model.ConnectionState
import com.wanbaohe.xiangqi.domain.model.Side
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVideogame

@Composable
fun OnlineMatchScreen(
    onlinePlay: OnlinePlayUseCase,
    onDismiss: () -> Unit,
    onMatchReady: (roomId: String, mySide: Side, opponentName: String, opponentAvatarUrl: String, initialFen: String) -> Unit,
    initialRoomId: String = "",
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
            ) {
                Header(onDismiss = onDismiss)
                Spacer(modifier = Modifier.height(16.dp))
                MatchContent(
                    onlinePlay = onlinePlay,
                    onMatchReady = onMatchReady,
                    initialRoomId = initialRoomId,
                )
            }
        }
    }
}

@Composable
private fun Header(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.xiangqi_online_match_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        IconButton(onClick = onDismiss) {
            Icon(com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close, contentDescription = null)
        }
    }
}

@Composable
private fun MatchContent(
    onlinePlay: OnlinePlayUseCase,
    onMatchReady: (roomId: String, mySide: Side, opponentName: String, opponentAvatarUrl: String, initialFen: String) -> Unit,
    initialRoomId: String,
) {
    val targetRoomId = initialRoomId.trim()
    var mode by remember(targetRoomId) { mutableStateOf(if (targetRoomId.isBlank()) MatchMode.CREATE else MatchMode.JOIN) }
    val connectionState by onlinePlay.connectionState.collectAsState()
    var currentRoom by remember { mutableStateOf<RoomInfo?>(null) }
    var mySide by remember { mutableStateOf(Side.RED) }
    var rooms by remember { mutableStateOf<List<RoomInfo>>(emptyList()) }
    var initialFen by remember { mutableStateOf(FenCodec.INITIAL_FEN) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var hasAutoJoinedTargetRoom by remember(targetRoomId) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val remotePlayerName = stringResource(R.string.xiangqi_player_remote)
    val playerFallbackName = stringResource(R.string.xiangqi_player_you)
    val createFailedText = stringResource(R.string.xiangqi_create_room_failed)
    val joinFailedText = stringResource(R.string.xiangqi_join_room_failed)
    val fetchFailedText = stringResource(R.string.xiangqi_fetch_rooms_failed)
    val userName = remember {
        TokenStorage.getLoginInfo()?.user?.nickname?.takeIf { it.isNotBlank() }
            ?: TokenStorage.getLoginInfo()?.user?.username?.takeIf { it.isNotBlank() }
            ?: playerFallbackName
    }
    val userAvatarUrl = remember {
        TokenStorage.getLoginInfo()?.user?.avatar.orEmpty()
    }

    fun Throwable.friendlyMessage(fallback: String): String {
        val raw = message.orEmpty()
        return when {
            raw.isBlank() -> fallback
            raw.equals("API error", ignoreCase = true) -> fallback
            raw.contains("timeout", ignoreCase = true) -> fetchFailedText
            raw.contains("Unable to resolve host", ignoreCase = true) -> fetchFailedText
            else -> raw
        }
    }

    fun refreshRooms(showLoading: Boolean = true) {
        if (isLoading) return
        if (showLoading) isLoading = true
        scope.launch {
            val result = onlinePlay.listRooms()
            if (showLoading) isLoading = false
            result.onSuccess { list ->
                rooms = list
                if (errorMessage == fetchFailedText || errorMessage.equals("API error", ignoreCase = true)) {
                    errorMessage = ""
                }
            }.onFailure { e ->
                if (showLoading || rooms.isEmpty()) {
                    errorMessage = e.friendlyMessage(fetchFailedText)
                }
            }
        }
    }

    suspend fun joinRoomById(roomId: String) {
        if (roomId.isBlank() || isLoading) return
        isLoading = true
        val result = onlinePlay.joinRoom(roomId, userName, userAvatarUrl)
        isLoading = false
        result.onSuccess { joinedRoom ->
            errorMessage = ""
            currentRoom = joinedRoom
            mySide = joinedRoom.config.guestSide
            onlinePlay.useRoom(joinedRoom, joinedRoom.config.guestSide, isHost = false)
        }.onFailure { e ->
            errorMessage = e.friendlyMessage(joinFailedText)
        }
    }

    LaunchedEffect(connectionState) {
        if (connectionState == ConnectionState.READY || connectionState == ConnectionState.PLAYING) {
            currentRoom?.let { room ->
                val opponent = if (mySide == room.config.hostSide) room.guestName else room.hostName
                val opponentAvatarUrl = if (mySide == room.config.hostSide) {
                    room.guestAvatarUrl
                } else {
                    room.hostAvatarUrl
                }
                onMatchReady(
                    room.id,
                    mySide,
                    opponent.ifBlank { remotePlayerName },
                    opponentAvatarUrl,
                    room.config.initialFen,
                )
            }
        }
    }

    LaunchedEffect(mode) {
        if (mode != MatchMode.JOIN || targetRoomId.isNotBlank()) return@LaunchedEffect
        refreshRooms(showLoading = rooms.isEmpty())
        while (true) {
            delay(5_000L)
            refreshRooms(showLoading = false)
        }
    }

    LaunchedEffect(targetRoomId) {
        if (targetRoomId.isBlank() || hasAutoJoinedTargetRoom) return@LaunchedEffect
        hasAutoJoinedTargetRoom = true
        joinRoomById(targetRoomId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (targetRoomId.isBlank()) {
            ModeSelector(
                selected = mode,
                onSelect = { mode = it },
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (targetRoomId.isNotBlank()) {
            JoinTargetRoomPanel(
                roomId = targetRoomId,
                isLoading = isLoading || connectionState == ConnectionState.CONNECTING,
                connectionState = connectionState,
                onRetry = { scope.launch { joinRoomById(targetRoomId) } },
            )
        } else when (mode) {
            MatchMode.CREATE -> CreateRoomPanel(
                isLoading = isLoading,
                connectionState = connectionState,
                currentRoom = currentRoom,
                initialFen = initialFen,
                onInitialFenChange = { initialFen = it },
                onCreateRoom = {
                    val normalizedFenResult = runCatching { FenCodec.encode(FenCodec.parse(initialFen)) }
                    normalizedFenResult.onFailure {
                        errorMessage = it.message ?: ""
                    }.onSuccess { normalizedFen ->
                        isLoading = true
                        scope.launch {
                            val result = onlinePlay.createRoom(userName, normalizedFen, userAvatarUrl)
                            isLoading = false
                            result.onSuccess { room ->
                                    errorMessage = ""
                                currentRoom = room
                                mySide = room.config.hostSide
                                onlinePlay.useRoom(room, room.config.hostSide, isHost = true)
                            }.onFailure { e ->
                                    errorMessage = e.friendlyMessage(createFailedText)
                            }
                        }
                    }
                },
                onCancel = {
                    currentRoom?.let { onlinePlay.disconnect() }
                    currentRoom = null
                },
            )

            MatchMode.JOIN -> JoinRoomPanel(
                rooms = rooms,
                isLoading = isLoading,
                onRefresh = { refreshRooms(showLoading = true) },
                onJoinRoom = { room ->
                    scope.launch { joinRoomById(room.id) }
                },
            )
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ModeSelector(
    selected: MatchMode,
    onSelect: (MatchMode) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ModeButton(
            label = stringResource(R.string.xiangqi_create_room),
            isSelected = selected == MatchMode.CREATE,
            onClick = { onSelect(MatchMode.CREATE) },
            modifier = Modifier.weight(1f),
        )
        ModeButton(
            label = stringResource(R.string.xiangqi_join_room),
            isSelected = selected == MatchMode.JOIN,
            onClick = { onSelect(MatchMode.JOIN) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ModeButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CreateRoomPanel(
    isLoading: Boolean,
    connectionState: ConnectionState,
    currentRoom: RoomInfo?,
    initialFen: String,
    onInitialFenChange: (String) -> Unit,
    onCreateRoom: () -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val shareProvider = LocalImageShareProvider.current
    val captureController = rememberCaptureController()
    val scope = rememberCoroutineScope()
    var isSharingImage by remember { mutableStateOf(false) }
    val inviteChooserTitle = stringResource(R.string.xiangqi_online_invite_share_title)

    fun shareInviteImage(roomCode: String) {
        scope.launch {
            isSharingImage = true
            runCatching {
                val bitmap = captureController.bitmap()
                shareProvider.shareImage(
                    imageInfo = ImageInfo(
                        width = bitmap.width,
                        height = bitmap.height,
                        imageFormat = ImageFormat.Png.Lossless,
                        originalUri = "xiangqi_online_invite_$roomCode",
                    ),
                    image = bitmap,
                    onComplete = { isSharingImage = false },
                )
            }.onFailure {
                isSharingImage = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when {
            currentRoom == null -> {
                OutlinedTextField(
                    value = initialFen,
                    onValueChange = onInitialFenChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.xiangqi_online_initial_fen)) },
                    supportingText = { Text(stringResource(R.string.xiangqi_online_initial_fen_hint)) },
                    minLines = 3,
                )
                Button(
                    onClick = onCreateRoom,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(stringResource(R.string.xiangqi_create_room_action))
                    }
                }
            }

            connectionState == ConnectionState.CONNECTING -> {
                CircularProgressIndicator(modifier = Modifier.size(40.dp))
                Text(stringResource(R.string.xiangqi_connecting))
            }

            connectionState == ConnectionState.WAITING_FOR_OPPONENT -> {
                val roomCode = currentRoom.id.takeLast(6)
                val invitationLink = remember(currentRoom.id) { buildXiangqiJoinRoomDeeplink(currentRoom.id) }
                val invitationText = stringResource(
                    R.string.xiangqi_online_invite_share_message,
                    roomCode,
                    invitationLink,
                )
                Text(
                    stringResource(R.string.xiangqi_waiting_opponent),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    stringResource(R.string.xiangqi_room_code, roomCode),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                RoomInviteQrCard(
                    roomCode = roomCode,
                    invitationLink = invitationLink,
                    captureController = captureController,
                    isSharingImage = isSharingImage,
                    onShareImage = { shareInviteImage(roomCode) },
                    onShareLink = {
                        shareXiangqiRoomInvitation(
                            context = context,
                            chooserTitle = inviteChooserTitle,
                            invitationText = invitationText,
                        )
                    },
                )
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.xiangqi_cancel))
                }
            }

            connectionState == ConnectionState.PLAYING -> {
                Text(
                    stringResource(R.string.xiangqi_opponent_ready),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            else -> {
                CircularProgressIndicator(modifier = Modifier.size(40.dp))
                Text(stringResource(R.string.xiangqi_connecting))
            }
        }
    }
}

@Composable
private fun RoomInviteQrCard(
    roomCode: String,
    invitationLink: String,
    captureController: CaptureController,
    isSharingImage: Boolean,
    onShareImage: () -> Unit,
    onShareLink: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .capturable(captureController),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = stringResource(R.string.xiangqi_online_invite_qr_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                    )
                    QrCode(
                        content = invitationLink,
                        modifier = Modifier.size(180.dp),
                        cornerRadius = 12.dp,
                        type = BarcodeType.QR_CODE,
                        qrParams = QrCodeParams(
                            foregroundColor = Color.Black,
                            backgroundColor = Color.White,
                        ),
                    )
                    Text(
                        text = stringResource(R.string.xiangqi_room_code, roomCode),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onShareImage,
                    enabled = !isSharingImage,
                    modifier = Modifier.weight(1f),
                ) {
                    if (isSharingImage) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(stringResource(R.string.xiangqi_online_invite_share_image_action))
                    }
                }
                OutlinedButton(
                    onClick = onShareLink,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.xiangqi_online_invite_share_link_action))
                }
            }
            OutlinedButton(
                onClick = {
                    Clipboard.copy(
                        text = invitationLink,
                        message = R.string.xiangqi_online_invite_link_copied,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.xiangqi_online_invite_copy_link_action))
            }
        }
    }
}

@Composable
private fun JoinTargetRoomPanel(
    roomId: String,
    isLoading: Boolean,
    connectionState: ConnectionState,
    onRetry: () -> Unit,
) {
    val shouldShowProgress = isLoading || connectionState in setOf(
        ConnectionState.CONNECTING,
        ConnectionState.WAITING_FOR_OPPONENT,
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.xiangqi_join_target_room_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.xiangqi_room_code, roomId.takeLast(6)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (shouldShowProgress) {
            CircularProgressIndicator(modifier = Modifier.size(40.dp))
            Text(stringResource(R.string.xiangqi_join_target_room_connecting))
        } else {
            OutlinedButton(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.xiangqi_join_target_room_retry))
            }
        }
    }
}

@Composable
private fun JoinRoomPanel(
    rooms: List<RoomInfo>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onJoinRoom: (RoomInfo) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.xiangqi_room_list),
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(onClick = onRefresh, enabled = !isLoading) {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading && rooms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (rooms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.xiangqi_no_rooms),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(rooms, key = { it.id }) { room ->
                    RoomItem(
                        room = room,
                        onJoin = { onJoinRoom(room) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RoomItem(
    room: RoomInfo,
    onJoin: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onJoin),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVideogame,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    room.hostName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    stringResource(R.string.xiangqi_room_code, room.id.takeLast(6)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(onClick = onJoin) {
                Text(stringResource(R.string.xiangqi_join))
            }
        }
    }
}

private enum class MatchMode { CREATE, JOIN }

private fun buildXiangqiJoinRoomDeeplink(roomId: String): String {
    return AppNavigationRegistry.buildStructuredDeeplink(
        targetType = AppNavigationTargetType.SCREEN,
        routeKey = "xiangqi_router",
        params = mapOf(
            "action" to "join_room",
            "room_id" to roomId,
        ),
    )
}

private fun shareXiangqiRoomInvitation(
    context: Context,
    chooserTitle: String,
    invitationText: String,
) {
    val intent = Intent(Intent.ACTION_SEND)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_TEXT, invitationText)
    context.startActivity(Intent.createChooser(intent, chooserTitle))
}
