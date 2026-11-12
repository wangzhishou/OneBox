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

package com.wanbaohe.file_transfer.server

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.wanbaohe.file_transfer.R
import com.wanbaohe.file_transfer.model.ReceivedFile
import com.wanbaohe.file_transfer.store.RecentReceivedStore
import com.shifenmiao.model.transfer.ServerState
import com.shifenmiao.model.transfer.TransferConfig
import com.wanbaohe.file_transfer.util.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 文件传输前台服务
 * 确保HTTP服务器在后台稳定运行
 */
class FileTransferService : Service() {

    private var server: FileTransferServer? = null
    private val binder = LocalBinder()

    // 同一批上传会在很短时间内连发，避免十几次通知提醒
    @Volatile
    private var lastReceiveNotifyAt: Long = 0L

    private val _serverState = MutableStateFlow<ServerState>(ServerState.Stopped)
    val serverState: StateFlow<ServerState> = _serverState

    var currentConfig = TransferConfig()
        private set

    inner class LocalBinder : Binder() {
        fun getService(): FileTransferService = this@FileTransferService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        createReceiveNotificationChannel()
        RecentReceivedStore.init(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val port = intent.getIntExtra(EXTRA_PORT, 8080)
                val password = intent.getStringExtra(EXTRA_PASSWORD)
                val allowUpload = intent.getBooleanExtra(EXTRA_ALLOW_UPLOAD, true)
                val rootPath = intent.getStringExtra(EXTRA_ROOT_PATH)
                    ?: android.os.Environment.getExternalStorageDirectory().absolutePath

                currentConfig = TransferConfig(
                    port = port,
                    password = password,
                    allowUpload = allowUpload,
                    rootPath = rootPath
                )

                startServer()
            }
            ACTION_STOP -> {
                stopServer()
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        stopServer()
        super.onDestroy()
    }

    /**
     * 启动HTTP服务器
     */
    private fun startServer() {
        if (server?.isAlive == true) {
            return
        }

        _serverState.value = ServerState.Starting

        try {
            val ipAddress = NetworkUtils.getLocalIpAddress(this)
            if (ipAddress == null) {
                _serverState.value = ServerState.Error(getString(R.string.server_ip_address_error))
                return
            }

            server = FileTransferServer(this, currentConfig).apply {
                // P1：浏览器上传成功后，手机端要"有感觉"——系统通知 + 最近接收
                onFileReceived = { received -> handleReceivedFile(received) }
                // 关键：禁用 socket 读超时。注意：NanoHTTPD.SOCKET_READ_TIMEOUT 在不同版本里通常是 5000ms（默认值），
                // 不是“禁用超时”。为了确保 WebSocket 不会因为空闲而被读超时误杀，这里显式传 0。
                start(0, false)
            }

            val accessUrl = NetworkUtils.buildAccessUrl(ipAddress, currentConfig.port)
            _serverState.value = ServerState.Running(currentConfig.port, accessUrl)

            // 启动前台服务
            startForeground(NOTIFICATION_ID, createNotification(accessUrl))

        } catch (e: Exception) {
            _serverState.value = ServerState.Error(e.message ?: getString(R.string.server_start_failed))
            server?.stop()
            server = null
        }
    }

    /**
     * 停止HTTP服务器
     */
    private fun stopServer() {
        server?.stop()
        server = null
        _serverState.value = ServerState.Stopped
        @Suppress("DEPRECATION")
        stopForeground(true)
    }

    /**
     * 更新服务器配置
     */
    fun updateConfig(config: TransferConfig) {
        currentConfig = config
        server?.updateConfig(config)
    }

    /**
     * 获取当前连接数
     */
    fun getConnectedClientsCount(): Int {
        return server?.getConnectedClientsCount() ?: 0
    }

    /**
     * 获取服务器实例（用于聊天功能）
     */
    fun getServer(): FileTransferServer? {
        return server
    }

    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 创建"接收文件"通知渠道
     */
    private fun createReceiveNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                RECEIVE_CHANNEL_ID,
                getString(R.string.receive_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.receive_notification_channel_description)
                setShowBadge(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 收到浏览器上传的文件：写"最近接收" + 发系统通知
     */
    private fun handleReceivedFile(received: ReceivedFile) {
        RecentReceivedStore.add(applicationContext, received)
        notifyFileReceived(received)
    }

    private fun notifyFileReceived(received: ReceivedFile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val dirLabel = if (received.dir.isBlank()) {
            getString(R.string.recent_received_root)
        } else {
            "/${received.dir}"
        }
        val content = getString(R.string.receive_notification_content, received.fileName, dirLabel)

        val contentIntent = Intent(Intent.ACTION_VIEW, Uri.parse("onebox://FileTransfer")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            1,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val now = System.currentTimeMillis()
        val quietUpdate = now - lastReceiveNotifyAt < 1500L
        lastReceiveNotifyAt = now

        val notification = NotificationCompat.Builder(this, RECEIVE_CHANNEL_ID)
            .setContentTitle(getString(R.string.receive_notification_title))
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setOnlyAlertOnce(quietUpdate)
            .setContentIntent(contentPendingIntent)
            .build()

        runCatching {
            NotificationManagerCompat.from(this).notify(RECEIVE_NOTIFICATION_ID, notification)
        }
    }

    /**
     * 创建前台服务通知
     */
    private fun createNotification(accessUrl: String): Notification {
        // 停止服务的PendingIntent
        val stopIntent = Intent(this, FileTransferService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 点击通知跳转的PendingIntent
        val contentIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("onebox://FileTransfer")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_content, accessUrl))
            .setSmallIcon(android.R.drawable.ic_menu_share)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.notification_stop),
                stopPendingIntent
            )
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "file_transfer_channel"
        private const val NOTIFICATION_ID = 1001

        private const val RECEIVE_CHANNEL_ID = "file_transfer_receive"
        private const val RECEIVE_NOTIFICATION_ID = 1002

        const val ACTION_START = "com.wanbaohe.file_transfer.START"
        const val ACTION_STOP = "com.wanbaohe.file_transfer.STOP"

        const val EXTRA_PORT = "port"
        const val EXTRA_PASSWORD = "password"
        const val EXTRA_ALLOW_UPLOAD = "allow_upload"
        const val EXTRA_ROOT_PATH = "root_path"

        /**
         * 启动服务
         */
        fun start(context: Context, config: TransferConfig) {
            val intent = Intent(context, FileTransferService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_PORT, config.port)
                putExtra(EXTRA_PASSWORD, config.password)
                putExtra(EXTRA_ALLOW_UPLOAD, config.allowUpload)
                putExtra(EXTRA_ROOT_PATH, config.rootPath)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /**
         * 停止服务
         */
        fun stop(context: Context) {
            val intent = Intent(context, FileTransferService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
