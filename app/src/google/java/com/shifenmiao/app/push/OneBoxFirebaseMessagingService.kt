package com.shifenmiao.app.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.shifenmiao.app.R

/**
 * FCM 消息接收服务(仅 google 渠道)。
 *
 * 服务端发 data-only 消息, 这里自行构建通知:
 * - data["title"] / data["body"]: 标题与正文(title 缺省用应用名)
 * - data["deeplink"]: 点击跳转, 缺省 onebox://screen/notification(消息中心)
 * - data["type"]: 消息类型(comment_reply/system/feedback_reply), 不认识也照常弹
 *
 * 通知权限未授权时静默跳过(不打扰用户)。
 */
class OneBoxFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FcmTokenUploader.onNewToken(this, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) return

        val data = message.data
        val title = data["title"]?.takeIf { it.isNotBlank() }
            ?: message.notification?.title
            ?: getString(R.string.app_name)
        val body = data["body"]?.takeIf { it.isNotBlank() }
            ?: message.notification?.body
            .orEmpty()
        if (title.isBlank() && body.isBlank()) return

        runCatching { showNotification(title, body, data) }
    }

    private fun showNotification(
        title: String,
        body: String,
        data: Map<String, String>,
    ) {
        ensureChannel()

        val deeplink = data["deeplink"]?.takeIf { it.isNotBlank() }
            ?: DEFAULT_DEEPLINK
        val contentIntent = Intent(Intent.ACTION_VIEW, deeplink.toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val notificationId = data["id"]?.toIntOrNull()
            ?: (System.currentTimeMillis() and 0x7fffffff).toInt()
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.push_channel_comment_reply_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = getString(R.string.push_channel_comment_reply_desc)
            }
            getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "comment_reply"
        private const val DEFAULT_DEEPLINK = "onebox://screen/notification"
    }
}
