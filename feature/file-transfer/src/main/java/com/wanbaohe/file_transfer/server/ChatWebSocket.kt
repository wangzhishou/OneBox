package com.wanbaohe.file_transfer.server

import android.util.Log
import com.google.gson.Gson
import com.shifenmiao.model.transfer.ChatMessage
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoWSD
import java.io.IOException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * WebSocket聊天处理器
 * 支持实时聊天和文件传输
 */
class ChatWebSocket(handshake: NanoHTTPD.IHTTPSession) : NanoWSD.WebSocket(handshake) {

    private val gson = Gson()

    /**
     * 当前连接所属频道。一个浏览器（或一个标签页）对应一个频道，从而实现互不串频道。
     */
    private val channelId: String = resolveOrCreateChannelId(handshake)

    /**
     * 浏览器上报的设备名（可选），用于手机端 session 列表展示。
     */
    private val deviceName: String? = resolveDeviceName(handshake)

    companion object {
        private const val TAG = "ChatWebSocket"

        private const val CHANNEL_ID_QUERY = "channelId"
        private const val CHANNEL_ID_COOKIE = "chat_channel_id"
        private const val DEVICE_NAME_QUERY = "deviceName"

        // channelId -> sockets
        private val channelConnections: ConcurrentHashMap<String, CopyOnWriteArraySet<ChatWebSocket>> =
            ConcurrentHashMap()

        // 单线程发送队列：避免并发 write 导致底层 socket 状态问题，同时确保不在主线程发送
        private val sendExecutor: ExecutorService = Executors.newSingleThreadExecutor { r ->
            Thread(r, "ChatWebSocket-Send").apply { isDaemon = true }
        }

        private fun register(socket: ChatWebSocket) {
            channelConnections.getOrPut(socket.channelId) { CopyOnWriteArraySet() }.add(socket)
        }

        private fun unregister(socket: ChatWebSocket) {
            channelConnections[socket.channelId]?.remove(socket)
            // 清理空集合，避免 map 泄漏
            channelConnections.entries.removeIf { it.value.isEmpty() }
        }

        /**
         * 广播消息到指定频道
         */
        fun broadcastToChannel(channelId: String, message: ChatMessage) {
            val jsonMessage = Gson().toJson(message)
            val sockets = channelConnections[channelId]
            Log.d(TAG, "Broadcasting message to channel=$channelId clients=${sockets?.size ?: 0}: $jsonMessage")

            sendExecutor.execute {
                sockets?.forEach { socket ->
                    try {
                        socket.send(jsonMessage)
                    } catch (e: IOException) {
                        Log.e(TAG, "Failed to send message to client", e)
                    } catch (e: Exception) {
                        Log.e(TAG, "Unexpected error when sending message", e)
                    }
                }
            }
        }

        /**
         * 获取当前连接数（全部频道总和）
         */
        fun getConnectionCount(): Int = channelConnections.values.sumOf { it.size }

        /**
         * 获取当前频道列表（仅包含当前有连接的频道）
         */
        fun getActiveChannelIds(): Set<String> = channelConnections.keys

        /**
         * 关闭所有连接
         */
        fun closeAll() {
            val snapshot = channelConnections.values.flatMap { it.toList() }
            snapshot.forEach { socket ->
                try {
                    socket.close(
                        NanoWSD.WebSocketFrame.CloseCode.NormalClosure,
                        "Server shutting down",
                        false
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error closing socket", e)
                }
            }
            channelConnections.clear()
        }

        /**
         * 全局消息监听器：用于把任意 WebSocket 连接收到的消息转发到 Android 侧（本机 UI）。
         */
        private val globalMessageListeners = CopyOnWriteArraySet<(String, ChatMessage) -> Unit>()

        fun addGlobalMessageListener(listener: (channelId: String, message: ChatMessage) -> Unit) {
            globalMessageListeners.add(listener)
        }

        fun removeGlobalMessageListener(listener: (String, ChatMessage) -> Unit) {
            globalMessageListeners.remove(listener)
        }

        private fun notifyGlobalMessageListeners(channelId: String, message: ChatMessage) {
            globalMessageListeners.forEach { l ->
                try {
                    l(channelId, message)
                } catch (e: Exception) {
                    Log.e(TAG, "Global message listener error", e)
                }
            }
        }

        private fun resolveOrCreateChannelId(handshake: NanoHTTPD.IHTTPSession): String {
            // 1) Query param (explicit, supports no-cookie contexts)
            val queryChannelId = runCatching {
                handshake.parameters[CHANNEL_ID_QUERY]?.firstOrNull()?.trim()
            }.getOrNull()
            if (!queryChannelId.isNullOrBlank()) return queryChannelId

            // 2) Cookie (stable across reload)
            val cookieChannelId = runCatching {
                handshake.cookies.read(CHANNEL_ID_COOKIE)?.trim()
            }.getOrNull()
            if (!cookieChannelId.isNullOrBlank()) return cookieChannelId

            // 3) Create new
            return UUID.randomUUID().toString()
        }

        private fun buildSetCookieHeader(channelId: String): String {
            // 注意：NanoHTTPD WebSocket 握手需要通过 handshake response 的 header 才能下发 cookie。
            // 这里只构造值，最终在 FileTransferServer.openWebSocket() 的握手响应里设置。
            return "$CHANNEL_ID_COOKIE=$channelId; Path=/; SameSite=Lax"
        }

        /**
         * 给外部（FileTransferServer）使用：构造 chat_channel_id 的 Set-Cookie header。
         */
        fun cookieHeaderForChannel(channelId: String): String = buildSetCookieHeader(channelId)

        /**
         * 从 URL (handshake.uri) 中解析 query 参数的兜底实现（某些 NanoHTTPD 版本 parameters 为空）。
         */
        fun parseQueryParam(uri: String, name: String): String? {
            val idx = uri.indexOf('?')
            if (idx < 0 || idx == uri.length - 1) return null
            val query = uri.substring(idx + 1)
            return query.split('&')
                .mapNotNull { part ->
                    val kv = part.split('=', limit = 2)
                    if (kv.size != 2) return@mapNotNull null
                    val k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8.name())
                    if (k != name) return@mapNotNull null
                    URLDecoder.decode(kv[1], StandardCharsets.UTF_8.name())
                }
                .firstOrNull()
        }

        private fun resolveDeviceName(handshake: NanoHTTPD.IHTTPSession): String? {
            // Prefer parsed parameters; fallback to uri parsing for older NanoHTTPD.
            val v = runCatching {
                handshake.parameters[DEVICE_NAME_QUERY]?.firstOrNull()?.trim()
            }.getOrNull()
            if (!v.isNullOrBlank()) return v

            val fromUri = runCatching {
                parseQueryParam(handshake.uri, DEVICE_NAME_QUERY)?.trim()
            }.getOrNull()
            return fromUri?.takeIf { it.isNotBlank() }
        }
    }

    /**
     * 消息接收回调（用于转发给 Android 端 UI）
     */
    var onMessageReceived: ((ChatMessage) -> Unit)? = null

    override fun onOpen() {
        register(this)
        Log.d(TAG, "WebSocket opened. channelId=$channelId totalConnections=${getConnectionCount()}")

        // 发送连接成功消息（包含 channelId，方便前端保存/重连）
        try {
            send(
                gson.toJson(
                    mapOf(
                        "type" to "system",
                        "event" to "connected",
                        "channelId" to channelId,
                        "timestamp" to System.currentTimeMillis()
                    )
                )
            )
        } catch (e: IOException) {
            Log.e(TAG, "Failed to send connection message", e)
        }
    }

    override fun onClose(
        code: NanoWSD.WebSocketFrame.CloseCode?,
        reason: String?,
        initiatedByRemote: Boolean
    ) {
        unregister(this)
        Log.d(TAG, "WebSocket closed. channelId=$channelId reason=$reason totalConnections=${getConnectionCount()}")
    }

    override fun onMessage(message: NanoWSD.WebSocketFrame) {
        val text = message.textPayload
        Log.d(TAG, "Received message (channelId=$channelId): $text")

        try {
            // 先尝试解析为通用 Map，用于识别 system/error 等非 ChatMessage 包
            val raw = try {
                gson.fromJson(text, Map::class.java) as Map<*, *>
            } catch (_: Exception) {
                null
            }

            val type = raw?.get("type") as? String

            // 忽略系统/错误类消息
            if (type == "system" || type == "error") {
                Log.d(TAG, "Ignoring non-chat message of type=$type")
                return
            }

            // 解析为 ChatMessage（频道由服务端绑定，不能信任前端）
            val chatMessage = gson.fromJson(text, ChatMessage::class.java)

            if (chatMessage.id.isBlank()) {
                Log.w(TAG, "Ignoring invalid chat message (blank id): $text")
                return
            }

            // 通知 Android 侧（本机 UI）
            notifyGlobalMessageListeners(channelId, chatMessage)
            onMessageReceived?.invoke(chatMessage)

            // 只在本频道广播（避免串频道）
            broadcastToChannel(channelId, chatMessage)

        } catch (e: Exception) {
            Log.e(TAG, "Error processing message", e)
            try {
                send(
                    gson.toJson(
                        mapOf(
                            "type" to "error",
                            "message" to "消息处理失败: ${e.message}",
                            "timestamp" to System.currentTimeMillis()
                        )
                    )
                )
            } catch (sendError: IOException) {
                Log.e(TAG, "Failed to send error message", sendError)
            }
        }
    }

    override fun onPong(pong: NanoWSD.WebSocketFrame) {
        Log.d(TAG, "Received pong")
    }

    override fun onException(exception: IOException) {
        Log.e(TAG, "WebSocket exception", exception)
        unregister(this)
    }

    fun getChannelId(): String = channelId

    fun getDeviceName(): String? = deviceName
}
