package com.wanbaohe.file_transfer.screenLogic

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.net.Uri
import android.os.IBinder
import android.provider.OpenableColumns
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.FeatureDatabase
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.shifenmiao.model.transfer.ChatMessage
import com.shifenmiao.model.transfer.ChatSession
import com.shifenmiao.model.transfer.MessageType
import com.shifenmiao.model.transfer.ServerState
import com.shifenmiao.model.transfer.TransferConfig
import com.wanbaohe.file_transfer.server.FileTransferService
import com.wanbaohe.file_transfer.util.NetworkUtils
import com.wanbaohe.file_transfer.util.QRCodeGenerator
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.UUID

/**
 * 文件传输功能组件
 * 管理HTTP服务器的启动/停止和配置
 */
class FileTransferComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder,
    featureDatabase: FeatureDatabase,
) : BaseComponent(dispatchersHolder, componentContext) {

    // 服务器状态
    private val _serverState = MutableStateFlow<ServerState>(ServerState.Stopped)
    val serverState: StateFlow<ServerState> = _serverState.asStateFlow()

    // 配置
    private val _config = MutableStateFlow(TransferConfig())
    val config: StateFlow<TransferConfig> = _config.asStateFlow()

    // IP地址
    private val _ipAddress = MutableStateFlow<String?>(null)
    val ipAddress: StateFlow<String?> = _ipAddress.asStateFlow()

    // 访问URL
    private val _accessUrl = MutableStateFlow<String?>(null)
    val accessUrl: StateFlow<String?> = _accessUrl.asStateFlow()

    // 二维码Bitmap
    private val _qrCodeBitmap = MutableStateFlow<Bitmap?>(null)
    val qrCodeBitmap: StateFlow<Bitmap?> = _qrCodeBitmap.asStateFlow()

    // 已连接客户端数量
    private val _connectedClients = MutableStateFlow(0)
    val connectedClients: StateFlow<Int> = _connectedClients.asStateFlow()

    // WebSocket 连接数（用于 UI 展示）
    private val _webSocketConnections = MutableStateFlow(0)
    val webSocketConnections: StateFlow<Int> = _webSocketConnections.asStateFlow()

    // 聊天消息列表
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // 未读消息数量
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    // 会话列表（一个浏览器连接对应一个会话）
    private val _chatSessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val chatSessions: StateFlow<List<ChatSession>> = _chatSessions.asStateFlow()

    // 当前选中的会话/频道
    private val _selectedChannelId = MutableStateFlow<String?>(null)
    val selectedChannelId: StateFlow<String?> = _selectedChannelId.asStateFlow()

    // 服务绑定
    private var service: FileTransferService? = null
    private var isBound = false

    // Chat database (read-only for UI rendering)
    private val chatDao by lazy { featureDatabase.chatMessageDao() }

    private var sessionsCollectionJob: Job? = null
    private var messagesCollectionJob: Job? = null
    private var connectionsPollingJob: Job? = null

    private fun startChatSessionsCollection() {
        if (sessionsCollectionJob != null) return
        sessionsCollectionJob = componentScope.launch {
            chatDao.listSessionSummariesFlow()
                .map { summaries ->
                    summaries.map {
                        ChatSession(
                            channelId = it.channelId,
                            deviceName = it.deviceName,
                            lastTimestamp = it.lastTimestamp,
                            lastSender = it.lastSender,
                            lastContent = it.lastContent,
                            messageCount = it.messageCount
                        )
                    }
                }
                .collectLatest { sessions ->
                    withContext(Dispatchers.Main.immediate) {
                        _chatSessions.value = sessions

                        // Ensure selection always points to a valid session.
                        val current = _selectedChannelId.value
                        if (sessions.isNotEmpty()) {
                            if (current == null || sessions.none { it.channelId == current }) {
                                _selectedChannelId.value = sessions.first().channelId
                            }
                        } else {
                            _selectedChannelId.value = null
                        }
                    }
                }
        }
    }

    private fun startChatMessagesCollection() {
        if (messagesCollectionJob != null) return
        messagesCollectionJob = componentScope.launch {
            selectedChannelId
                .filterNotNull()
                .distinctUntilChanged()
                .flatMapLatest { channelId ->
                    chatDao.getMessagesByChannelFlow(channelId)
                        .map { entities -> entities.map { it.toChatMessage() } }
                }
                .collectLatest { messages ->
                    withContext(Dispatchers.Main.immediate) {
                        _chatMessages.value = messages
                    }
                }
        }
    }

    private fun startConnectionsPolling() {
        if (connectionsPollingJob != null) return
        connectionsPollingJob = componentScope.launch(ioDispatcher) {
            while (true) {
                val svc = service
                if (svc == null) {
                    delay(1000)
                    continue
                }

                val state = svc.serverState.value
                if (state is ServerState.Running) {
                    val httpConnections = svc.getConnectedClientsCount()
                    val wsConnections = svc.getServer()?.getWebSocketConnectionCount() ?: 0

                    withContext(Dispatchers.Main.immediate) {
                        _connectedClients.value = httpConnections
                        _webSocketConnections.value = wsConnections
                    }
                    delay(800)
                } else {
                    withContext(Dispatchers.Main.immediate) {
                        _connectedClients.value = 0
                        _webSocketConnections.value = 0
                    }
                    delay(1000)
                }
            }
        }
    }

    private fun stopChatCollections() {
        sessionsCollectionJob?.cancel()
        sessionsCollectionJob = null
        messagesCollectionJob?.cancel()
        messagesCollectionJob = null
    }

    private fun stopConnectionsPolling() {
        connectionsPollingJob?.cancel()
        connectionsPollingJob = null
        _connectedClients.value = 0
        _webSocketConnections.value = 0
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as? FileTransferService.LocalBinder
            service = localBinder?.getService()
            isBound = true

            // 监听服务状态
            service?.let { svc ->
                // 同步服务端的配置
                if (svc.serverState.value is ServerState.Running) {
                    _config.value = svc.currentConfig
                }

                componentScope.launch {
                    svc.serverState.collect { state ->
                        _serverState.value = state

                        // 更新访问URL和二维码
                        if (state is ServerState.Running) {
                            _accessUrl.value = state.address
                            generateQRCode(state.address)
                        } else {
                            _accessUrl.value = null
                            _qrCodeBitmap.value = null
                        }
                    }
                }

                // Chat rendering is DB-driven now.
                // Keep the callback only for lightweight side-effects (unread count).
                svc.getServer()?.onChatMessageReceived = { message ->
                    if (message.sender == "browser") {
                        componentScope.launch {
                            withContext(Dispatchers.Main.immediate) {
                                _unreadCount.value += 1
                            }
                        }
                    }
                }

                // Subscribe DB flows (idempotent).
                startChatSessionsCollection()
                startChatMessagesCollection()

                // Keep connection stats updated for UI.
                startConnectionsPolling()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            isBound = false
            // On disconnect we can stop collectors to avoid duplicate subscriptions after reconnect.
            stopChatCollections()
            stopConnectionsPolling()
        }
    }

    init {
        // 初始化时获取IP地址
        refreshIpAddress()

        // 绑定服务
        bindService()
    }

    /**
     * 刷新IP地址
     */
    fun refreshIpAddress() {
        componentScope.launch(ioDispatcher) {
            _ipAddress.value = NetworkUtils.getLocalIpAddress(context)
        }
    }

    /**
     * 检查网络是否可用
     */
    fun isNetworkAvailable(): Boolean {
        return NetworkUtils.isNetworkAvailable(context)
    }

    /**
     * 检查是否连接WiFi
     */
    fun isWifiConnected(): Boolean {
        return NetworkUtils.isWifiConnected(context)
    }

    /**
     * 启动服务器
     */
    fun startServer() {
        if (_serverState.value is ServerState.Running) {
            return
        }

        // 检查网络
        if (!isNetworkAvailable()) {
            _serverState.value = ServerState.Error("网络不可用")
            return
        }

        _serverState.value = ServerState.Starting

        // 启动前台服务
        FileTransferService.start(context, _config.value)

        // 绑定服务以获取状态更新
        if (!isBound) {
            bindService()
        }
    }

    /**
     * 停止服务器
     */
    fun stopServer() {
        FileTransferService.stop(context)
        _serverState.value = ServerState.Stopped
        _accessUrl.value = null
        _qrCodeBitmap.value = null
    }

    /**
     * 更新配置
     */
    fun updateConfig(newConfig: TransferConfig) {
        _config.value = newConfig
        service?.updateConfig(newConfig)
    }

    /**
     * 更新端口
     */
    fun updatePort(port: Int) {
        updateConfig(_config.value.copy(port = port))
    }

    /**
     * 更新密码
     */
    fun updatePassword(password: String?) {
        updateConfig(_config.value.copy(password = password?.takeIf { it.isNotBlank() }))
    }

    /**
     * 更新是否允许上传
     */
    fun updateAllowUpload(allowUpload: Boolean) {
        updateConfig(_config.value.copy(allowUpload = allowUpload))
    }

    /**
     * 更新根目录
     */
    fun updateRootPath(rootPath: String) {
        updateConfig(_config.value.copy(rootPath = rootPath))
    }

    /**
     * 绑定服务
     */
    private fun bindService() {
        val intent = Intent(context, FileTransferService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    /**
     * 解绑服务
     */
    private fun unbindService() {
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
        }
    }

    /**
     * 生成二维码
     */
    private fun generateQRCode(url: String) {
        componentScope.launch(defaultDispatcher) {
            val bitmap = QRCodeGenerator.generateQRCode(url, 512)
            _qrCodeBitmap.value = bitmap
        }
    }

    /**
     * 获取访问地址用于复制
     */
    fun getAccessUrlForCopy(): String? {
        return _accessUrl.value
    }

    /**
     * 发送文本消息
     */
    fun sendTextMessage(text: String) {
        if (text.isBlank()) return

        val channelId = _selectedChannelId.value ?: _chatSessions.value.firstOrNull()?.channelId

        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            type = MessageType.TEXT,
            content = text,
            sender = "mobile",
            timestamp = System.currentTimeMillis()
        )

        // Don't manually append to _chatMessages; Room Flow will push it after persist.
        componentScope.launch(ioDispatcher) {
            val server = service?.getServer() ?: return@launch
            if (channelId.isNullOrBlank()) {
                server.sendChatMessage(message)
            } else {
                server.sendChatMessageToChannel(channelId, message)
            }
        }
    }

    /**
     * 发送文件消息
     */
    fun sendFileMessage(fileName: String, fileSize: Long, filePath: String) {
        val channelId = _selectedChannelId.value ?: _chatSessions.value.firstOrNull()?.channelId

        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            type = MessageType.FILE,
            content = "发送了文件",
            sender = "mobile",
            timestamp = System.currentTimeMillis(),
            fileName = fileName,
            fileSize = fileSize,
            filePath = filePath
        )

        // Don't manually append to _chatMessages; Room Flow will push it after persist.
        componentScope.launch(ioDispatcher) {
            val server = service?.getServer() ?: return@launch
            if (channelId.isNullOrBlank()) {
                server.sendChatMessage(message)
            } else {
                server.sendChatMessageToChannel(channelId, message)
            }
        }
    }

    /**
     * 从系统选择器拿到 Uri 后，复制到当前服务器根目录（config.rootPath）并作为 FILE 消消息发送。
     *
     * Contract:
     * - input: content:// Uri
     * - output: 在 rootPath 下落地一个文件（尽量保持原文件名，冲突则自动改名）
     * - then: sendFileMessage(fileName, fileSize, relativePath)
     */
    fun sendFileFromUri(uri: Uri) {
        componentScope.launch(ioDispatcher) {
            val rootDir = File(_config.value.rootPath)
            if (!rootDir.exists() || !rootDir.isDirectory || !rootDir.canWrite()) return@launch

            val resolver = context.contentResolver

            val displayName = runCatching {
                resolver.query(uri, null, null, null, null)?.use { c ->
                    val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0 && c.moveToFirst()) c.getString(idx) else null
                }
            }.getOrNull()?.takeIf { it.isNotBlank() } ?: "file-${System.currentTimeMillis()}"

            val sizeFromMeta = runCatching {
                resolver.query(uri, null, null, null, null)?.use { c ->
                    val idx = c.getColumnIndex(OpenableColumns.SIZE)
                    if (idx >= 0 && c.moveToFirst()) c.getLong(idx) else -1L
                }
            }.getOrNull()?.takeIf { it > 0 }

            // Avoid overwrite: generate unique name if already exists
            var target = File(rootDir, displayName)
            if (target.exists()) {
                val dot = displayName.lastIndexOf('.')
                val base = if (dot > 0) displayName.substring(0, dot) else displayName
                val ext = if (dot > 0) displayName.substring(dot) else ""
                var i = 1
                while (target.exists()) {
                    target = File(rootDir, "$base ($i)$ext")
                    i++
                }
            }

            val copied = runCatching {
                resolver.openInputStream(uri)?.use { input ->
                    target.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: return@runCatching false
                true
            }.getOrElse { false }

            if (!copied) return@launch

            val fileSize = sizeFromMeta ?: target.length()

            // filePath should be relative to rootPath for /api/download?path=
            sendFileMessage(
                fileName = target.name,
                fileSize = fileSize,
                filePath = target.name
            )
        }
    }

    /**
     * 清除未读消息计数
     */
    fun clearUnreadCount() {
        _unreadCount.value = 0
    }

    /**
     * 清空聊天记录
     */
    fun clearChatHistory() {
        service?.getServer()?.clearChatHistory()
        _chatMessages.value = emptyList()
        _unreadCount.value = 0
    }

    /**
     * 切换选中的会话
     */
    fun selectChatSession(channelId: String) {
        if (channelId == _selectedChannelId.value) return
        _selectedChannelId.value = channelId
        clearUnreadCount()
    }

    override fun resetState() {
        // 停止服务器并重置状态
        stopServer()
        unbindService()
        stopChatCollections()
        stopConnectionsPolling()
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext
        ): FileTransferComponent
    }
}
