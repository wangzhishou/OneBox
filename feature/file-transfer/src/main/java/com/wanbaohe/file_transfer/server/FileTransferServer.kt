package com.wanbaohe.file_transfer.server

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.shifenmiao.database.FeatureDatabase
import com.shifenmiao.database.transfer.ChatMessageEntity
import com.shifenmiao.database.transfer.ChatSessionEntity
import com.shifenmiao.model.transfer.ChatSession
import com.shifenmiao.model.transfer.ChatMessage
import com.shifenmiao.model.transfer.DeviceInfo
import com.shifenmiao.model.transfer.FileListResponse
import com.shifenmiao.model.transfer.TransferConfig
import com.shifenmiao.model.transfer.UploadResponse
import com.shifenmiao.theme.TailwindHelper
import com.t8rin.logger.Logger
import com.t8rin.logger.makeLog
import com.wanbaohe.file_transfer.util.FileUtils
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoWSD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.atomic.AtomicInteger
import kotlin.text.get
import kotlin.text.toLong

/**
 * 文件传输HTTP服务器
 * 基于NanoWSD实现，支持WebSocket实时聊天
 */
class FileTransferServer(
    private val context: Context,
    private var config: TransferConfig
) : NanoWSD(config.port) {

    private val gson = Gson()
    private val connectedClients = AtomicInteger(0)

    private val db by lazy { FeatureDatabase.getInstanceOrCreate(context) }
    private val chatDao by lazy { db.chatMessageDao() }

    // 已验证的session
    private val authenticatedSessions = mutableSetOf<String>()

    /**
     * 最近活跃的浏览器频道（用于手机端发送消息时选择目标）。
     *
     * 说明：目前手机端 UI 只有一个聊天窗口；在不改 UI 的前提下，默认发送给“最近有浏览器发过消息的频道”。
     * 如果你后续要做多会话列表，可以把这个扩展成“当前选中的 channelId”。
     */
    @Volatile
    private var lastActiveChannelId: String = ChatMessageEntity.DEFAULT_CHANNEL_ID

    // 聊天消息回调（推到手机端 UI）
    var onChatMessageReceived: ((ChatMessage) -> Unit)? = null

    /**
     * 全局 WebSocket 消息监听器（用于把浏览器发来的消息推到手机端 UI）。
     */
    private val globalWsListener: (String, ChatMessage) -> Unit = { channelId, message ->
        lastActiveChannelId = channelId
        persistMessage(channelId, message)
        onChatMessageReceived?.invoke(message)
    }

    init {
        // 注册一次全局监听：任何连接收到消息都能触发到本机 UI
        ChatWebSocket.addGlobalMessageListener(globalWsListener)
    }

    override fun stop() {
        ChatWebSocket.removeGlobalMessageListener(globalWsListener)
        // 注意：db 是进程级共享单例（FeatureDatabase），生命周期由 App 管理，
        // 这里不能 close——否则服务重启后 getInstanceOrCreate 仍返回已关闭的实例，
        // 聊天消息持久化会静默失败。
        super.stop()
    }

    /**
     * 获取当前连接数
     */
    fun getConnectedClientsCount(): Int = connectedClients.get()

    /**
     * 更新配置
     */
    fun updateConfig(newConfig: TransferConfig) {
        config = newConfig
    }

    /**
     * 发送聊天消息到浏览器客户端（默认发送到最近活跃频道）。
     */
    fun sendChatMessage(message: ChatMessage) {
        sendChatMessageToChannel(lastActiveChannelId, message)
    }

    /**
     * 发送聊天消息到指定频道
     */
    fun sendChatMessageToChannel(channelId: String, message: ChatMessage) {
        persistMessage(channelId, message)
        ChatWebSocket.broadcastToChannel(channelId, message)
    }

    private val serverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun persistMessage(channelId: String, message: ChatMessage) {
        serverScope.launch {
            try {
                chatDao.insertMessage(ChatMessageEntity.fromChatMessage(message, channelId))
                // 简单保护：每个频道最多保留 500 条
                chatDao.trimChannelToMaxCount(channelId, 500)
            } catch (e: Exception) {
                makeLog("FileTransferServer", Logger.Level.Error, {
                    e.printStackTrace()
                })
            }
        }
    }

    /**
     * 获取WebSocket连接数
     */
    fun getWebSocketConnectionCount(): Int = ChatWebSocket.getConnectionCount()

    /**
     * 获取聊天历史（默认频道）。
     *
     * 注意：旧签名保留给手机端 UI 使用；它会读取最近活跃频道的历史。
     */
    fun getChatHistory(): List<ChatMessage> = getChatHistoryByChannel(lastActiveChannelId)

    fun getChatHistoryByChannel(channelId: String): List<ChatMessage> {
        return runBlocking(Dispatchers.IO) {
            runCatching {
                chatDao.getMessagesByChannel(channelId).map { it.toChatMessage() }
            }.getOrElse { emptyList() }
        }
    }

    /**
     * 清空聊天历史（默认频道）
     */
    fun clearChatHistory() {
        clearChatHistoryByChannel(lastActiveChannelId)
    }

    fun clearChatHistoryByChannel(channelId: String) {
        serverScope.launch {
            try {
                chatDao.deleteChannelMessages(channelId)
            } catch (e: Exception) {
                makeLog("FileTransferServer", Logger.Level.Error, {
                    e.printStackTrace()
                })
            }
        }
    }

    override fun serve(session: IHTTPSession): Response {
        return super.serve(session)
    }

    /**
     * 重写WebSocket创建方法
     */
    override fun openWebSocket(handshake: IHTTPSession): WebSocket? {
        val uri = handshake.uri

        return when {
            uri.startsWith("/ws/chat") -> {
                // 兜底：某些 NanoHTTPD 版本 handshake.parameters 解析不到 query
                val queryChannelId = ChatWebSocket.parseQueryParam(uri, "channelId")
                val cookieChannelId = handshake.cookies.read("chat_channel_id")
                val resolvedForCookie =
                    (queryChannelId ?: cookieChannelId)?.takeIf { it.isNotBlank() }

                val webSocket = ChatWebSocket(handshake)

                // Persist deviceName mapping for session list display.
                runCatching {
                    val db = FeatureDatabase.getInstanceOrCreate(context)
                    val sessionDao = db.chatSessionDao()
                    val deviceName = webSocket.getDeviceName()?.takeIf { it.isNotBlank() }
                    if (!deviceName.isNullOrBlank()) {
                        serverScope.launch {
                            sessionDao.upsert(
                                ChatSessionEntity(
                                    channelId = webSocket.getChannelId(),
                                    deviceName = deviceName,
                                    updatedAt = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }.onFailure { e ->
                    Log.w("FileTransferServer", "Failed to persist chat session deviceName", e)
                }

                // 如果浏览器没有 cookie，我们为它补一份，保证刷新页面仍在同一频道。
                // 注意：WebSocket 握手 header 由 NanoWSD 内部生成；此处只能尽力设置 cookie。
                // 对于不支持的 NanoWSD 版本，前端会使用 localStorage + query 参数兜底。
                runCatching {
                    val channelId = webSocket.getChannelId()
                    if (resolvedForCookie.isNullOrBlank()) {
                        handshake.headers["set-cookie"] =
                            ChatWebSocket.cookieHeaderForChannel(channelId)
                    }
                }

                webSocket
            }

            else -> null
        }
    }

    override fun serveHttp(session: IHTTPSession): Response {
        connectedClients.incrementAndGet()
        try {
            val response = handleRequest(session)

            // CORS: keep it permissive for LAN use.
            // Important for browsers that send preflight (OPTIONS) or extra headers (Range, X-Requested-With).
            response.addHeader("Access-Control-Allow-Origin", "*")
            response.addHeader("Vary", "Origin")
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            response.addHeader(
                "Access-Control-Allow-Headers",
                "Content-Type, Range, X-Requested-With, Authorization"
            )
            response.addHeader(
                "Access-Control-Expose-Headers",
                "Content-Disposition, Content-Length"
            )

            return response
        } finally {
            connectedClients.decrementAndGet()
        }
    }

    private fun handleRequest(session: IHTTPSession): Response {
        val uri = session.uri
        val method = session.method

        if (method == Method.OPTIONS) {
            return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "")
        }


        if (config.password != null && !isAuthenticated(session)) {
            if (uri.startsWith("/api/") && uri != "/api/auth") {
                return jsonResponse(
                    mapOf(
                        "success" to false,
                        "requiresAuth" to true,
                        "message" to "需要登录"
                    )
                )
            }
        }
        makeLog {
            "FileTransferServer - handleRequest - uri: $uri, method: $method"
        }

        return when {
            uri == "/" || uri == "/index.html" || uri == "/login" -> serveMainPage()
            // 公共
            uri.startsWith("/js/") -> serveAssetsResource(uri)
            uri.startsWith("/css/") -> serveAssetsResource(uri)
            // 自定义静态资源路径
            uri.startsWith("/static/") -> serveStaticResource(uri)

            uri.startsWith("/i18n/") && uri.endsWith(".json") -> serveI18nJson(uri)

            uri == "/api/auth" && method == Method.POST -> handleAuth(session)
            uri == "/api/files" -> handleFileList(session)
            uri == "/api/download" -> handleDownload(session)
            uri == "/api/upload" && method == Method.POST -> handleUpload(session)
            uri == "/api/thumbnail" -> handleThumbnail(session)
            uri == "/api/info" -> handleDeviceInfo()
            uri == "/api/mkdir" && method == Method.POST -> handleMkdir(session)
            uri == "/api/rename" && method == Method.POST -> handleRename(session)
            uri == "/api/delete" && method == Method.POST -> handleDelete(session)
            uri == "/api/chat/history" -> handleChatHistory(session)
            uri == "/api/chat/sessions" -> handleChatSessions()

            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found")
        }
    }

    private fun handleChatSessions(): Response {
        return try {
            val sessions = listChatSessions()
            jsonResponse(
                mapOf(
                    "success" to true,
                    "sessions" to sessions
                )
            )
        } catch (e: Exception) {
            Log.e("FileTransferServer", "handleChatSessions error", e)
            jsonResponse(
                mapOf(
                    "success" to false,
                    "message" to "获取会话列表失败: ${e.message}"
                )
            )
        }
    }

    /**
     * 获取会话摘要列表（用于手机端 UI 展示多浏览器会话）。
     */
    fun listChatSessions(): List<ChatSession> {
        return runBlocking(Dispatchers.IO) {
            runCatching {
                chatDao.listSessionSummaries().map {
                    ChatSession(
                        channelId = it.channelId,
                        deviceName = it.deviceName,
                        lastTimestamp = it.lastTimestamp,
                        lastSender = it.lastSender,
                        lastContent = it.lastContent,
                        messageCount = it.messageCount
                    )
                }
            }.getOrElse { emptyList() }
        }
    }

    /**
     * 检查是否已验证
     */
    private fun isAuthenticated(session: IHTTPSession): Boolean {
        if (config.password == null) return true

        val cookies = session.cookies
        val sessionId = cookies.read("session_id")
        return sessionId != null && authenticatedSessions.contains(sessionId)
    }

    /**
     * 处理密码验证
     */
    private fun handleAuth(session: IHTTPSession): Response {
        val files = mutableMapOf<String, String>()
        session.parseBody(files)

        val postData = files["postData"] ?: ""
        val params = try {
            @Suppress("UNCHECKED_CAST")
            gson.fromJson(postData, Map::class.java) as Map<String, Any>
        } catch (_: Exception) {
            emptyMap()
        }

        val password =
            (params["password"] as? String) ?: session.parameters["password"]?.firstOrNull()

        return if (password == config.password) {
            val sessionId = java.util.UUID.randomUUID().toString()
            authenticatedSessions.add(sessionId)

            val response = newFixedLengthResponse(
                Response.Status.OK,
                "application/json",
                gson.toJson(mapOf("success" to true))
            )
            response.addHeader("Set-Cookie", "session_id=$sessionId; Path=/; HttpOnly")
            response
        } else {
            newFixedLengthResponse(
                Response.Status.UNAUTHORIZED,
                "application/json",
                gson.toJson(mapOf("success" to false, "message" to "密码错误"))
            )
        }
    }

    /**
     * 返回 i18n json。
     *
     * 按 URL 里的语言选择 res/raw 下的字典：
     * - /i18n/en.json -> res/raw/i18n_en.json
     * - /i18n/es.json -> res/raw/i18n_es.json
     * - /i18n/pt-BR.json -> res/raw/i18n_pt_br.json
     * - 其它（含 zh-CN、未知语言、资源缺失）回退 res/raw/i18n.json（中文）
     */
    private fun serveI18nJson(uri: String): Response {
        // 从 "/i18n/<lang>.json" 中取出 lang，并转成合法的 raw 资源名
        val lang = uri.removePrefix("/i18n/").removeSuffix(".json")
        val langResName = "i18n_" + lang.lowercase().replace('-', '_')

        val content = try {
            var resId = context.resources.getIdentifier(langResName, "raw", context.packageName)
            if (resId == 0) {
                // 未知语言或对应字典缺失时回退中文
                resId = context.resources.getIdentifier("i18n", "raw", context.packageName)
            }
            if (resId != 0) {
                context.resources.openRawResource(resId).bufferedReader().use { it.readText() }
            } else {
                "{}"
            }
        } catch (_: Exception) {
            "{}"
        }

        return newFixedLengthResponse(
            Response.Status.OK,
            "application/json; charset=utf-8",
            content
        )
    }


    /**
     * 处理文件列表请求
     */
    private fun handleFileList(session: IHTTPSession): Response {
        val params = session.parameters
        val requestPath = params["path"]?.firstOrNull() ?: ""

        try {
            val rootDir = File(config.rootPath)
            if (!rootDir.exists()) {
                return jsonResponse(
                    FileListResponse(
                        success = false,
                        message = "根目录不存在: ${config.rootPath}"
                    )
                )
            }

            if (!rootDir.canRead()) {
                return jsonResponse(
                    FileListResponse(
                        success = false,
                        message = "根目录无法读取，请检查权限: ${config.rootPath}"
                    )
                )
            }

            val safePath = FileUtils.validatePath(config.rootPath, requestPath)
            if (safePath == null) {
                makeLog("FileTransferServer", "Invalid path: $requestPath")
                return jsonResponse(
                    FileListResponse(
                        success = false,
                        message = "无效的路径"
                    )
                )
            }

            val directory = File(safePath)
            if (!directory.exists()) {
                return jsonResponse(FileListResponse(success = false, message = "目录不存在"))
            }
            if (!directory.isDirectory) {
                return jsonResponse(FileListResponse(success = false, message = "不是目录"))
            }
            if (!directory.canRead()) {
                return jsonResponse(FileListResponse(success = false, message = "目录无法读取"))
            }

            val files = FileUtils.listFiles(directory, config.showHiddenFiles)
            val canGoUp = FileUtils.canGoUp(config.rootPath, safePath)
            val parentPath = FileUtils.getParentPath(config.rootPath, safePath)

            val response = FileListResponse(
                success = true,
                files = files,
                currentPath = FileUtils.getRelativePath(config.rootPath, safePath),
                canGoUp = canGoUp,
                parentPath = parentPath?.let { FileUtils.getRelativePath(config.rootPath, it) }
            )

            // 使用流式方式返回 JSON，避免大字符串内存问题
            val jsonString = gson.toJson(response)
            val jsonBytes = jsonString.toByteArray(Charsets.UTF_8)
            val inputStream = ByteArrayInputStream(jsonBytes)

            return newFixedLengthResponse(
                Response.Status.OK,
                "application/json; charset=utf-8",
                inputStream,
                jsonBytes.size.toLong()
            )
        } catch (e: Exception) {
            Log.e("FileTransferServer", "handleFileList error", e)
            return jsonResponse(
                FileListResponse(
                    success = false,
                    message = "获取文件列表失败: ${e.message}"
                )
            )
        }
    }


    /**
     * 处理文件下载请求
     */
    private fun handleDownload(session: IHTTPSession): Response {
        val params = session.parameters
        val requestPath = params["path"]?.firstOrNull() ?: return errorResponse("缺少path参数")

        val safePath = FileUtils.validatePath(config.rootPath, requestPath)
            ?: return errorResponse("无效的路径")

        val file = File(safePath)
        if (!file.exists() || !file.isFile) {
            return errorResponse("文件不存在")
        }

        if (!file.canRead()) {
            return errorResponse("无法读取文件")
        }

        val mimeType = FileUtils.getMimeType(file) ?: "application/octet-stream"
        val fileInputStream = FileInputStream(file)

        val response = newFixedLengthResponse(
            Response.Status.OK,
            mimeType,
            fileInputStream,
            file.length()
        )

        // 设置下载文件名
        val encodedFileName = java.net.URLEncoder.encode(file.name, "UTF-8")
        response.addHeader("Content-Disposition", "attachment; filename*=UTF-8''$encodedFileName")
        response.addHeader("Content-Length", file.length().toString())

        return response
    }

    /**
     * 处理文件上传请求
     */
    private fun handleUpload(session: IHTTPSession): Response {
        if (!config.allowUpload) {
            return jsonResponse(
                UploadResponse(
                    success = false,
                    message = "上传功能已禁用"
                )
            )
        }

        val params = session.parameters
        val targetPath = params["path"]?.firstOrNull() ?: ""

        val safePath = FileUtils.validatePath(config.rootPath, targetPath)
            ?: return jsonResponse(
                UploadResponse(
                    success = false,
                    message = "无效的路径"
                )
            )

        val targetDir = File(safePath)
        if (!targetDir.exists() || !targetDir.isDirectory) {
            return jsonResponse(
                UploadResponse(
                    success = false,
                    message = "目标目录不存在"
                )
            )
        }

        if (!targetDir.canWrite()) {
            return jsonResponse(
                UploadResponse(
                    success = false,
                    message = "目标目录不可写"
                )
            )
        }

        try {
            val files = mutableMapOf<String, String>()
            session.parseBody(files)

            // 获取上传的文件
            val tempFilePath = files["file"]
            if (tempFilePath == null) {
                return jsonResponse(
                    UploadResponse(
                        success = false,
                        message = "没有找到上传的文件"
                    )
                )
            }

            // 获取原始文件名
            val fileParams = session.parameters["file"]
            val originalFileName = fileParams?.firstOrNull() ?: "uploaded_file"

            // 生成唯一文件名
            val uniqueFileName = FileUtils.generateUniqueFileName(targetDir, originalFileName)
            val targetFile = File(targetDir, uniqueFileName)

            // 移动临时文件到目标位置
            val tempFile = File(tempFilePath)
            tempFile.copyTo(targetFile, overwrite = true)
            tempFile.delete()

            return jsonResponse(
                UploadResponse(
                    success = true,
                    fileName = uniqueFileName,
                    filePath = FileUtils.getRelativePath(config.rootPath, targetFile.absolutePath)
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return jsonResponse(
                UploadResponse(
                    success = false,
                    message = "上传失败: ${e.message}"
                )
            )
        }
    }

    /**
     * 处理缩略图请求
     */
    private fun handleThumbnail(session: IHTTPSession): Response {
        val params = session.parameters
        val requestPath = params["path"]?.firstOrNull() ?: return errorResponse("缺少path参数")

        val safePath = FileUtils.validatePath(config.rootPath, requestPath)
            ?: return errorResponse("无效的路径")

        val file = File(safePath)
        if (!file.exists() || !file.isFile) {
            return errorResponse("文件不存在")
        }

        val mimeType = FileUtils.getMimeType(file)
        if (!FileUtils.isImage(mimeType)) {
            return errorResponse("不是图片文件")
        }

        // 生成缩略图
        val thumbnail = FileUtils.generateThumbnail(file, 300, 300)
        if (thumbnail != null) {
            try {
                // 将Bitmap转换为JPEG字节流
                val outputStream = ByteArrayOutputStream()
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                val thumbnailBytes = outputStream.toByteArray()
                thumbnail.recycle()

                val inputStream = ByteArrayInputStream(thumbnailBytes)
                return newFixedLengthResponse(
                    Response.Status.OK,
                    "image/jpeg",
                    inputStream,
                    thumbnailBytes.size.toLong()
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 如果生成缩略图失败，返回原图
        val fileInputStream = FileInputStream(file)
        return newFixedLengthResponse(
            Response.Status.OK,
            mimeType,
            fileInputStream,
            file.length()
        )
    }

    /**
     * 处理设备信息请求
     */
    private fun handleDeviceInfo(): Response {
        return try {
            val rootDir = File(config.rootPath)
            val deviceInfo = DeviceInfo(
                deviceName = Build.MODEL,
                totalSpace = rootDir.totalSpace,
                freeSpace = rootDir.freeSpace,
                allowUpload = config.allowUpload
            )
            jsonResponse(deviceInfo)
        } catch (e: Exception) {
            Log.e("FileTransferServer", "handleDeviceInfo error", e)
            jsonResponse(
                mapOf(
                    "success" to false,
                    "message" to "获取设备信息失败: ${e.message}"
                )
            )
        }
    }

    /**
     * 处理创建文件夹请求
     */
    private fun handleMkdir(session: IHTTPSession): Response {
        if (!config.allowUpload) {
            return jsonResponse(
                mapOf(
                    "success" to false,
                    "message" to "上传功能已禁用，无法创建文件夹"
                )
            )
        }

        try {
            val files = mutableMapOf<String, String>()
            session.parseBody(files)

            val postData = files["postData"] ?: ""
            val params = try {
                @Suppress("UNCHECKED_CAST")
                gson.fromJson(postData, Map::class.java) as Map<String, Any>
            } catch (_: Exception) {
                return jsonResponse(
                    mapOf(
                        "success" to false,
                        "message" to "无效的请求数据"
                    )
                )
            }

            val requestPath = params["path"] as? String ?: ""
            val folderName = params["name"] as? String ?: return jsonResponse(
                mapOf(
                    "success" to false,
                    "message" to "缺少文件夹名称"
                )
            )

            if (folderName.isBlank() || folderName.contains("/") || folderName.contains("\\")) {
                return jsonResponse(
                    mapOf(
                        "success" to false,
                        "message" to "无效的文件夹名称"
                    )
                )
            }

            val safePath = FileUtils.validatePath(config.rootPath, requestPath)
                ?: return jsonResponse(
                    mapOf(
                        "success" to false,
                        "message" to "无效的路径"
                    )
                )

            val parentDir = File(safePath)
            if (!parentDir.exists() || !parentDir.isDirectory) {
                return jsonResponse(
                    mapOf(
                        "success" to false,
                        "message" to "目标目录不存在"
                    )
                )
            }

            if (!parentDir.canWrite()) {
                return jsonResponse(
                    mapOf(
                        "success" to false,
                        "message" to "目标目录不可写"
                    )
                )
            }

            val newFolder = File(parentDir, folderName)
            if (newFolder.exists()) {
                return jsonResponse(
                    mapOf(
                        "success" to false,
                        "message" to "文件夹已存在"
                    )
                )
            }

            val created = newFolder.mkdir()
            if (created) {
                return jsonResponse(
                    mapOf(
                        "success" to true,
                        "message" to "创建成功"
                    )
                )
            } else {
                return jsonResponse(
                    mapOf(
                        "success" to false,
                        "message" to "创建失败"
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return jsonResponse(
                mapOf(
                    "success" to false,
                    "message" to "创建失败: ${e.message}"
                )
            )
        }
    }

    /**
     * 处理重命名请求
     */
    private fun handleRename(session: IHTTPSession): Response {
        if (!config.allowUpload) {
            return jsonResponse(
                mapOf(
                    "success" to false,
                    "message" to "上传功能已禁用，无法重命名"
                )
            )
        }

        try {
            val files = mutableMapOf<String, String>()
            session.parseBody(files)

            val postData = files["postData"] ?: ""
            val params = try {
                @Suppress("UNCHECKED_CAST")
                gson.fromJson(postData, Map::class.java) as Map<String, Any>
            } catch (_: Exception) {
                return jsonResponse(
                    mapOf(
                        "success" to false,
                        "message" to "无效的请求数据"
                    )
                )
            }

            val requestPath = params["path"] as? String ?: return jsonResponse(
                mapOf(
                    "success" to false,
                    "message" to "缺少文件路径"
                )
            )
            val newName = params["newName"] as? String ?: return jsonResponse(
                mapOf(
                    "success" to false,
                    "message" to "缺少新名称"
                )
            )

            if (newName.isBlank() || newName.contains("/") || newName.contains("\\")) {
                return jsonResponse(
                    mapOf(
                        "success" to false,
                        "message" to "无效的文件名"
                    )
                )
            }

            val safePath = FileUtils.validatePath(config.rootPath, requestPath)
                ?: return jsonResponse(
                    mapOf(
                        "success" to false,
                        "message" to "无效的路径"
                    )
                )

            val sourceFile = File(safePath)
            if (!sourceFile.exists()) {
                return jsonResponse(
                    mapOf(
                        "success" to false,
                        "message" to "文件不存在"
                    )
                )
            }

            val parentDir = sourceFile.parentFile
            if (parentDir == null || !parentDir.canWrite()) {
                return jsonResponse(
                    mapOf(
                        "success" to false,
                        "message" to "目标目录不可写"
                    )
                )
            }

            val targetFile = File(parentDir, newName)
            if (targetFile.exists()) {
                return jsonResponse(
                    mapOf(
                        "success" to false,
                        "message" to "目标文件已存在"
                    )
                )
            }

            val renamed = sourceFile.renameTo(targetFile)
            if (renamed) {
                return jsonResponse(
                    mapOf(
                        "success" to true,
                        "message" to "重命名成功"
                    )
                )
            } else {
                return jsonResponse(
                    mapOf(
                        "success" to false,
                        "message" to "重命名失败"
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return jsonResponse(
                mapOf(
                    "success" to false,
                    "message" to "重命名失败: ${e.message}"
                )
            )
        }
    }

    /**
     * 处理删除请求
     */
    private fun handleDelete(session: IHTTPSession): Response {
        if (!config.allowUpload) {
            return jsonResponse(
                mapOf(
                    "success" to false,
                    "message" to "上传功能已禁用，无法删除文件"
                )
            )
        }

        try {
            val files = mutableMapOf<String, String>()
            session.parseBody(files)

            val postData = files["postData"] ?: ""
            val params = try {
                @Suppress("UNCHECKED_CAST")
                gson.fromJson(postData, Map::class.java) as Map<String, Any>
            } catch (_: Exception) {
                return jsonResponse(
                    mapOf(
                        "success" to false,
                        "message" to "无效的请求数据"
                    )
                )
            }

            val paths = params["paths"] as? List<*> ?: return jsonResponse(
                mapOf(
                    "success" to false,
                    "message" to "缺少文件路径列表"
                )
            )

            if (paths.isEmpty()) {
                return jsonResponse(
                    mapOf(
                        "success" to false,
                        "message" to "未选择任何文件"
                    )
                )
            }

            val failedFiles = mutableListOf<String>()
            var successCount = 0

            for (path in paths) {
                val requestPath = path as? String ?: continue

                val safePath = FileUtils.validatePath(config.rootPath, requestPath)
                if (safePath == null) {
                    failedFiles.add(requestPath)
                    continue
                }

                val file = File(safePath)
                if (!file.exists()) {
                    failedFiles.add(requestPath)
                    continue
                }

                val deleted = if (file.isDirectory) {
                    file.deleteRecursively()
                } else {
                    file.delete()
                }

                if (deleted) {
                    successCount++
                } else {
                    failedFiles.add(file.name)
                }
            }

            return if (failedFiles.isEmpty()) {
                jsonResponse(
                    mapOf(
                        "success" to true,
                        "message" to "成功删除 $successCount 个文件"
                    )
                )
            } else {
                jsonResponse(
                    mapOf(
                        "success" to (successCount > 0),
                        "message" to "删除完成，成功: $successCount，失败: ${failedFiles.size}",
                        "failedFiles" to failedFiles
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return jsonResponse(
                mapOf(
                    "success" to false,
                    "message" to "删除失败: ${e.message}"
                )
            )
        }
    }

    /**
     * 返回主页面
     */
    private fun serveMainPage(): Response {
        val html = getWebResource("index.html")
        return newFixedLengthResponse(Response.Status.OK, "text/html", html)
    }


    /**
     * 返回静态资源
     */
    private fun serveStaticResource(uri: String): Response {
        val resourceName = uri.removePrefix("/static/")

        val content = getWebResource(resourceName)

        val mimeType = when {
            resourceName.endsWith(".css") -> "text/css"
            resourceName.endsWith(".js") -> "application/javascript"
            resourceName.endsWith(".png") -> "image/png"
            resourceName.endsWith(".svg") -> "image/svg+xml"
            else -> "application/octet-stream"
        }

        return newFixedLengthResponse(Response.Status.OK, mimeType, content)
    }

    /**
     * 返回静态资源
     */
    /**
     * 返回静态资源
     */
    private fun serveAssetsResource(uri: String): Response {
        // 1. 移除 URI 开头的 '/'，因为 AssetManager.open() 不接受绝对路径风格
        val assetPath = if (uri.startsWith("/")) uri.substring(1) else uri

        return try {
            // 2. 尝试从 assets 目录打开文件
            // 确保你的文件位于: src/main/assets/js/tailwindcss.js
            val content = getAssetResource(assetPath)

            // 3. 设置正确的 MIME 类型 (浏览器对 JS 文件的 MIME 类型很敏感)
            val mimeType = when {
                assetPath.endsWith(".js") -> "application/javascript"
                assetPath.endsWith(".css") -> "text/css"
                assetPath.endsWith(".html") -> "text/html"
                assetPath.endsWith(".png") -> "image/png"
                assetPath.endsWith(".jpg") -> "image/jpeg"
                else -> "application/octet-stream"
            }
            // 4. 返回流式响应
            return newFixedLengthResponse(Response.Status.OK, mimeType, content)
        } catch (e: Exception) {
            // 5. 调试日志：打印出试图访问的路径，方便排查
            Log.e("FileTransferServer", "Asset not found: $assetPath", e)
            newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "File not found")
        }
    }


    /**
     * 从assets直接获取资源
     */
    private fun getAssetResource(path: String): String {
        return try {
            val content = context.assets.open(path).bufferedReader().use { it.readText() }
            if (path.endsWith("tailwindcss.js")) {
                content + "\n" + TailwindHelper.getTailwindConfig()
            } else {
                content
            }
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * 从assets获取Web资源
     */
    private fun getWebResource(name: String): String {
        return try {
            context.assets.open("file_transfer/$name").bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * 流式 JSON 响应，避免大字符串内存问题
     */
    private fun <T> jsonResponse(data: T): Response {
        return try {
            val jsonString = gson.toJson(data)
            val jsonBytes = jsonString.toByteArray(Charsets.UTF_8)
            val inputStream = ByteArrayInputStream(jsonBytes)
            newFixedLengthResponse(
                Response.Status.OK,
                "application/json; charset=utf-8",
                inputStream,
                jsonBytes.size.toLong()
            )
        } catch (e: Exception) {
            Log.e("FileTransferServer", "jsonResponse error", e)
            val errorJson = """{"success":false,"message":"序列化失败: ${e.message?.replace("\"", "'")}"}"""
            val errorBytes = errorJson.toByteArray(Charsets.UTF_8)
            val errorStream = ByteArrayInputStream(errorBytes)
            newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                "application/json; charset=utf-8",
                errorStream,
                errorBytes.size.toLong()
            )
        }
    }

    private fun errorResponse(message: String): Response {
        return try {
            val jsonString = gson.toJson(mapOf("success" to false, "message" to message))
            val jsonBytes = jsonString.toByteArray(Charsets.UTF_8)
            val inputStream = ByteArrayInputStream(jsonBytes)
            newFixedLengthResponse(
                Response.Status.BAD_REQUEST,
                "application/json; charset=utf-8",
                inputStream,
                jsonBytes.size.toLong()
            )
        } catch (e: Exception) {
            Log.e("FileTransferServer", "errorResponse error", e)
            val errorJson = """{"success":false,"message":"${message.replace("\"", "'")}"}"""
            val errorBytes = errorJson.toByteArray(Charsets.UTF_8)
            val errorStream = ByteArrayInputStream(errorBytes)
            newFixedLengthResponse(
                Response.Status.BAD_REQUEST,
                "application/json; charset=utf-8",
                errorStream,
                errorBytes.size.toLong()
            )
        }
    }

    /**
     * 处理聊天历史请求（按 channelId 隔离）。
     *
     * 获取顺序：query channelId > cookie chat_channel_id > default
     */
    private fun handleChatHistory(session: IHTTPSession): Response {
        return try {
            val channelId = session.parameters["channelId"]?.firstOrNull()?.takeIf { it.isNotBlank() }
                ?: session.cookies.read("chat_channel_id")?.takeIf { it.isNotBlank() }
                ?: ChatMessageEntity.DEFAULT_CHANNEL_ID

            // 手机端默认发送目标：跟随最近一次查看/活跃的会话
            lastActiveChannelId = channelId

            val messages = getChatHistoryByChannel(channelId)

            val resp = jsonResponse(
                mapOf(
                    "success" to true,
                    "channelId" to channelId,
                    "messages" to messages
                )
            )

            // 首次访问时，尽量补一个 cookie，方便浏览器 refresh 后保持会话。
            if (session.cookies.read("chat_channel_id").isNullOrBlank()) {
                resp.addHeader("Set-Cookie", ChatWebSocket.cookieHeaderForChannel(channelId))
            }

            resp
        } catch (e: Exception) {
            Log.e("FileTransferServer", "handleChatHistory error", e)
            jsonResponse(
                mapOf(
                    "success" to false,
                    "message" to "获取聊天历史失败: ${e.message}"
                )
            )
        }
    }

    companion object {
    }
}
