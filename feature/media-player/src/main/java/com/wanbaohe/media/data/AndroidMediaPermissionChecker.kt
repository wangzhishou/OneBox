package com.wanbaohe.media.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.wanbaohe.media.domain.MediaKind
import com.wanbaohe.media.domain.MediaPermissionChecker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** 读本机媒体所需的运行时权限（Android 13 起按音频/视频分开申请）。 */
@Singleton
class AndroidMediaPermissionChecker @Inject constructor(
    @ApplicationContext private val context: Context,
) : MediaPermissionChecker {

    override fun requiredPermissions(kind: MediaKind): List<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                when (kind) {
                    MediaKind.VIDEO -> Manifest.permission.READ_MEDIA_VIDEO
                    MediaKind.AUDIO -> Manifest.permission.READ_MEDIA_AUDIO
                }
            )
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    override fun hasAccess(kind: MediaKind): Boolean {
        val granted = requiredPermissions(kind).all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        if (granted) return true
        // Android 14 的"仅部分照片/视频"也算可用：此时库只包含用户选中的内容
        return kind == MediaKind.VIDEO &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
            ) == PackageManager.PERMISSION_GRANTED
    }

    override fun hasNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    override fun notificationPermission(): String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS
        } else {
            null
        }
}
