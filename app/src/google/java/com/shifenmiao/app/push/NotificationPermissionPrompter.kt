package com.shifenmiao.app.push

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.t8rin.imagetoolbox.core.ui.utils.helper.ContextUtils
import com.tencent.mmkv.MMKV

/**
 * 通知权限申请(仅 google 渠道, TIRAMISU+)。
 *
 * 登录成功后由 main 源集反射调用一次: MMKV 标记只主动弹一次,
 * 拒绝则静默不打扰(此后用户只能在系统设置里开启)。
 * 拿不到前台 Activity 时直接跳过, 等下次登录机会。
 */
object NotificationPermissionPrompter {

    private const val MMKV_ID = "fcm_push"
    private const val KEY_PERMISSION_ASKED = "notification_permission_asked"
    private const val REGISTRY_KEY = "onebox_fcm_post_notifications"

    fun maybeRequest() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val mmkv = MMKV.mmkvWithID(MMKV_ID)
        if (mmkv.decodeBool(KEY_PERMISSION_ASKED, false)) return
        val activity = ContextUtils.currentActivity() as? ComponentActivity ?: return
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mmkv.encode(KEY_PERMISSION_ASKED, true)
            return
        }
        // 先落标记再弹: 无论允许/拒绝都只主动打扰这一次
        mmkv.encode(KEY_PERMISSION_ASKED, true)
        runCatching {
            val launcher = activity.activityResultRegistry.register(
                REGISTRY_KEY,
                ActivityResultContracts.RequestPermission(),
            ) { /* 结果静默处理, 拒绝不再打扰 */ }
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
