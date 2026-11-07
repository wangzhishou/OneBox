package com.wanbaohe.file_transfer.screen

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import com.wanbaohe.core.ui.widget.glass.GlassSwitch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.wanbaohe.base.ui.card.AllFilesAccessPermissionCard
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.model.event.PermissionRequest
import com.shifenmiao.model.transfer.ServerState
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.utils.helper.ContextUtils.requestPermissions
import com.wanbaohe.core.ui.utils.helper.isPortraitOrientationAsState
import com.wanbaohe.core.ui.utils.provider.LocalComponentActivity
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.glass.tintedGlassCardColors
import com.wanbaohe.core.ui.widget.navigation.BottomNavItem
import com.wanbaohe.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.file_transfer.R
import com.wanbaohe.file_transfer.model.ReceivedFile
import com.wanbaohe.file_transfer.screen.components.ChatTabContent
import com.wanbaohe.file_transfer.screenLogic.FileTransferComponent
import com.wanbaohe.file_transfer.util.FileUtils
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.PlayCircle
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLock
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolder
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUpload
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWifi
import com.t8rin.imagetoolbox.core.resources.icons.line.LineQrCode
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSendToMobile
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStop
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChatBubble

@Composable
fun FileTransferScreen(
    transferComponent: FileTransferComponent,
    appComponent: AppComponent
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 权限请求: 说明弹窗由全局申请流程负责(见 PermissionRationale)
    val activity = LocalComponentActivity.current

    // needed data from component
    val chatMessages by transferComponent.chatMessages.collectAsState()
    val unreadCount by transferComponent.unreadCount.collectAsState()
    val chatSessions by transferComponent.chatSessions.collectAsState()
    val selectedChannelId by transferComponent.selectedChannelId.collectAsState()
    val webSocketConnections by transferComponent.webSocketConnections.collectAsState()
    val connectedClients by transferComponent.connectedClients.collectAsState()

    // server config / url
    val config by transferComponent.config.collectAsState()
    val accessUrl by transferComponent.accessUrl.collectAsState()
    val serverState by transferComponent.serverState.collectAsState()

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })

    // 自动切换：收到 browser 新消息且当前不在聊天页时，切换到聊天页
    var lastMessageCount by remember { mutableIntStateOf(chatMessages.size) }
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.size > lastMessageCount) {
            val newMessages = chatMessages.drop(lastMessageCount)
            val hasBrowserMessage = newMessages.any { it.sender == "browser" }
            if (hasBrowserMessage && pagerState.currentPage != 1) {
                pagerState.animateScrollToPage(1)
                transferComponent.clearUnreadCount()
            }
        }
        lastMessageCount = chatMessages.size
    }

    val permissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    fun checkPermissions(): Boolean {
        return permissions.all {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, it
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    // 权限申请统一走全局流程: 先弹用途说明(同屏告知目的)再拉起系统权限框,
    // 应用商店隐私检测按"申请前有没有说明"判定, 直接 launch 会被判违规。
    fun requestTransferPermissions() {
        activity.requestPermissions(
            permissions = permissions,
            permissionRequest = PermissionRequest.ALL,
            onSuccess = { transferComponent.startServer() },
            onFailed = { /* 拒绝后停在页面, 用户可再点一次 */ }
        )
    }
    val isPortrait by isPortraitOrientationAsState()
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    BaseScreen(
        title = {
            Text(
                text = stringResource(R.string.file_transfer_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        onGoBack = appComponent.onGoBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            HorizontalPager(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        vertical = 0.dp,
                        horizontal = AppTheme.dimens.paddingNormal
                    ),
                state = pagerState,
            ) { page ->
                when (page) {
                    0 -> {
                        FileTransferContent(
                            component = transferComponent,
                            context = context,
                        ) {
                            if (checkPermissions()) {
                                transferComponent.startServer()
                            } else {
                                requestTransferPermissions()
                            }
                        }
                    }

                    1 -> {
                        ChatTabContent(
                            sessions = chatSessions,
                            selectedChannelId = selectedChannelId,
                            onSelectSession = transferComponent::selectChatSession,
                            messages = chatMessages,
                            onSendMessage = transferComponent::sendTextMessage,
                            onSendFileFromUri = transferComponent::sendFileFromUri,
                            webSocketConnections = webSocketConnections,
                            unreadCount = unreadCount,
                            connectedClients = connectedClients,
                            modifier = Modifier.fillMaxSize(),
                            serverBaseUrl = accessUrl,
                            rootPath = config.rootPath,
                            serverPort = config.port,
                            isServerRunning = serverState is ServerState.Running
                        )
                    }
                }
            }

            if (isPortrait) {
                val navItems = listOf(
                    BottomNavItem(
                        id = "transfer",
                        label = stringResource(R.string.file_transfer_tab_transfer),
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSendToMobile,
                        selectedIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSendToMobile,
                    ),
                    BottomNavItem(
                        id = "chat",
                        label = stringResource(R.string.file_transfer_tab_chat),
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChatBubble,
                        selectedIcon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChatBubble,
                    ),
                )
                BottomNavigationBar(
                    items = navItems,
                    selectedItemId = if (pagerState.currentPage == 0) "transfer" else "chat",
                    onItemClick = { item ->
                        scope.launch {
                            pagerState.animateScrollToPage(
                                if (item.id == "transfer") 0 else 1
                            )
                        }
                    },
                    showBar = !isImeVisible,
                    navigationBarsPadding = true,
                )
            }
        }
    }

    BackHandler {
        if (pagerState.currentPage == 1) {
            // 如果在聊天页，返回键切回传输页
            transferComponent.clearUnreadCount()
            // BackHandler 里不能直接调用 LaunchedEffect；用协程切页
            scope.launch {
                pagerState.scrollToPage(0)
            }
        } else {
            appComponent.onGoBack()
        }
    }
}

@Composable
fun FileTransferContent(
    component: FileTransferComponent,
    context: Context,
    onStartServer: () -> Unit
) {
    val serverState by component.serverState.collectAsState()
    val config by component.config.collectAsState()
    val ipAddress by component.ipAddress.collectAsState()
    val accessUrl by component.accessUrl.collectAsState()
    val qrCodeBitmap by component.qrCodeBitmap.collectAsState()
    val recentReceived by component.recentReceived.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceLarge)
    ) {
        AllFilesAccessPermissionCard()
        NetworkStatusCard(
            ipAddress = ipAddress,
            isWifiConnected = component.isWifiConnected()
        )

        ServerStatusCard(
            serverState = serverState,
            accessUrl = accessUrl,
            onStart = onStartServer,
            onStop = { component.stopServer() },
            onCopyAddress = {
                accessUrl?.let { url ->
                    copyToClipboard(context, url)
                }
            }
        )

        if (serverState is ServerState.Running) {
            QRCodeCard(qrCodeBitmap = qrCodeBitmap)
        }

        RecentReceivedCard(
            items = recentReceived,
            onOpen = { item -> openReceivedFile(context, config.rootPath, item) },
            onClear = { component.clearRecentReceived() }
        )

        SettingsCard(
            port = config.port,
            password = config.password,
            allowUpload = config.allowUpload,
            rootPath = config.rootPath,
            isServerRunning = serverState is ServerState.Running,
            onPortChange = { component.updatePort(it) },
            onPasswordChange = { component.updatePassword(it) },
            onAllowUploadChange = { component.updateAllowUpload(it) }
        )
    }
}

/**
 * 最近接收：电脑上传到这里后，手机上能看到"传了什么、存在哪"。
 */
@Composable
fun RecentReceivedCard(
    items: List<ReceivedFile>,
    onOpen: (ReceivedFile) -> Unit,
    onClear: () -> Unit
) {
    if (items.isEmpty()) return

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimens.paddingNormal)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.recent_received_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onClear) {
                    Text(
                        text = stringResource(R.string.recent_received_clear),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppTheme.dimens.spaceSmall))

            items.take(5).forEach { item ->
                val dirLabel = if (item.dir.isBlank()) {
                    stringResource(R.string.recent_received_root)
                } else {
                    "/${item.dir}"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpen(item) }
                        .padding(
                            horizontal = AppTheme.dimens.spaceSmall,
                            vertical = AppTheme.dimens.spaceSmall
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.fileName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${formatReceivedSize(item.size)} · ${formatReceivedTime(item.timestamp)} · $dirLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private fun formatReceivedSize(bytes: Long): String = when {
    bytes >= 1024L * 1024 * 1024 -> String.format(
        Locale.getDefault(),
        "%.1f GB",
        bytes / (1024.0 * 1024 * 1024)
    )

    bytes >= 1024L * 1024 -> String.format(
        Locale.getDefault(),
        "%.1f MB",
        bytes / (1024.0 * 1024)
    )

    bytes >= 1024L -> String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0)
    else -> "$bytes B"
}

private fun formatReceivedTime(timestamp: Long): String {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { timeInMillis = timestamp }

    val sameDay = now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)

    val pattern = if (sameDay) "HH:mm" else "MM-dd HH:mm"
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
}

/**
 * 打开"最近接收"里的文件；失败时给 toast，不静默。
 */
private fun openReceivedFile(context: Context, rootPath: String, item: ReceivedFile) {
    val opened = runCatching {
        val absolute = FileUtils.validatePath(rootPath, item.relativePath) ?: return@runCatching false
        val file = File(absolute)
        if (!file.exists() || !file.isFile || !file.canRead()) return@runCatching false

        val mime = FileUtils.getMimeType(file) ?: "*/*"
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(intent)
        true
    }.getOrDefault(false)

    if (!opened) {
        Toast.makeText(
            context,
            context.getString(R.string.recent_received_open_failed),
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
fun NetworkStatusCard(
    ipAddress: String?,
    isWifiConnected: Boolean
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        colors = tintedGlassCardColors(
            if (isWifiConnected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimens.paddingNormal),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isWifiConnected) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWifi else Icons.Default.WifiOff,
                contentDescription = null,
                tint = if (isWifiConnected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(AppTheme.dimens.spaceLarge))
            Column {
                Text(
                    text = stringResource(
                        if (isWifiConnected) R.string.file_transfer_wifi_connected
                        else R.string.file_transfer_wifi_disconnected
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isWifiConnected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer
                )
                if (ipAddress != null) {
                    Text(
                        text = stringResource(R.string.file_transfer_ip_prefix, ipAddress),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isWifiConnected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
fun ServerStatusCard(
    serverState: ServerState,
    accessUrl: String?,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onCopyAddress: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when (serverState) {
                            is ServerState.Running -> MaterialTheme.colorScheme.onPrimaryContainer
                            is ServerState.Starting -> MaterialTheme.colorScheme.onTertiaryContainer
                            is ServerState.Error -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.outline
                        }
                    )
            )
            Spacer(modifier = Modifier.width(AppTheme.dimens.spaceSmall))
            Text(
                text = when (serverState) {
                    is ServerState.Running -> stringResource(R.string.server_status_running)
                    is ServerState.Starting -> stringResource(R.string.server_status_starting)
                    is ServerState.Error -> stringResource(R.string.server_status_error)
                    else -> stringResource(R.string.server_status_stopped)
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.dimens.spaceLarge))

        if (serverState is ServerState.Running && accessUrl != null) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                colors = tintedGlassCardColors(MaterialTheme.colorScheme.secondaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppTheme.dimens.paddingSmall),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.access_address),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = accessUrl,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    IconButton(onClick = onCopyAddress) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                            contentDescription = stringResource(R.string.copy_address),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppTheme.dimens.spaceSmall))

            Text(
                text = stringResource(R.string.qr_code_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        if (serverState is ServerState.Error) {
            Spacer(modifier = Modifier.height(AppTheme.dimens.spaceSmall))
            Text(
                text = serverState.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.dimens.spaceLarge))

        Row(
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSmall)
        ) {
            when (serverState) {
                is ServerState.Running -> {
                    Button(
                        onClick = onStop,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStop, contentDescription = null)
                        Spacer(modifier = Modifier.width(AppTheme.dimens.spaceSmall))
                        Text(stringResource(R.string.stop_server))
                    }
                }

                is ServerState.Starting -> {
                    OutlinedButton(
                        onClick = { },
                        enabled = false
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(AppTheme.dimens.spaceSmall))
                        Text(stringResource(R.string.server_status_starting))
                    }
                }

                else -> {
                    Button(
                        onClick = onStart,
                        colors = AppTheme.colors.getPrimaryButtonColors()
                    ) {
                        Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PlayCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(AppTheme.dimens.spaceSmall))
                        Text(stringResource(R.string.start_server))
                    }
                }
            }
        }
    }
}

@Composable
fun QRCodeCard(qrCodeBitmap: Bitmap?) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimens.paddingNormal),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineQrCode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(AppTheme.dimens.spaceSmall))
                Text(
                    text = stringResource(R.string.scan_qr_code),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(AppTheme.dimens.spaceLarge))

            if (qrCodeBitmap != null) {
                Image(
                    bitmap = qrCodeBitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.file_transfer_cd_qr_code),
                    modifier = Modifier
                        .size(AppTheme.dimens.fileTransferQrCodeSize)
                        .clip(RoundedCornerShape(AppTheme.dimens.cornerRadiusNormal))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(AppTheme.dimens.paddingExtraSmall)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(AppTheme.dimens.fileTransferQrCodeSize)
                        .clip(RoundedCornerShape(AppTheme.dimens.cornerRadiusNormal))
                        .background(MaterialTheme.colorScheme.surfaceContainer),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun SettingsCard(
    port: Int,
    password: String?,
    allowUpload: Boolean,
    rootPath: String,
    isServerRunning: Boolean,
    onPortChange: (Int) -> Unit,
    onPasswordChange: (String?) -> Unit,
    onAllowUploadChange: (Boolean) -> Unit
) {
    var portText by remember(port) { mutableStateOf(port.toString()) }
    var passwordText by remember(password) { mutableStateOf(password ?: "") }


    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(AppTheme.dimens.spaceLarge))

        GlassOutlinedTextField(
            shape = MaterialTheme.shapes.medium,
            value = portText,
            onValueChange = {
                portText = it
                it.toIntOrNull()?.let { newPort ->
                    if (newPort in 1024..65535) {
                        onPortChange(newPort)
                    }
                }
            },
            label = { Text(stringResource(R.string.port_setting)) },
            placeholder = { Text(stringResource(R.string.port_hint)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = !isServerRunning,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(AppTheme.dimens.spaceNormal))

        GlassOutlinedTextField(
            shape = MaterialTheme.shapes.medium,
            value = passwordText,
            onValueChange = {
                passwordText = it
                onPasswordChange(it.ifBlank { null })
            },
            label = { Text(stringResource(R.string.password_protection)) },
            placeholder = { Text(stringResource(R.string.password_hint)) },
            leadingIcon = {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLock, contentDescription = null)
            },
            visualTransformation = PasswordVisualTransformation(),
            enabled = !isServerRunning,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(AppTheme.dimens.spaceLarge))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineUpload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(AppTheme.dimens.spaceLarge))
                Column {
                    Text(
                        text = stringResource(R.string.allow_upload),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.allow_upload_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            GlassSwitch(
                checked = allowUpload,
                onCheckedChange = onAllowUploadChange,
                enabled = !isServerRunning,
                colors = AppTheme.colors.switchColors()
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.dimens.spaceNormal))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFolder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(AppTheme.dimens.spaceLarge))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.root_directory),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = rootPath,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("URL", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, R.string.address_copied, Toast.LENGTH_SHORT).show()
}
