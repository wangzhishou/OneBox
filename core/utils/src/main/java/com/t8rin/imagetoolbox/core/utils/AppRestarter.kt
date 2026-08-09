package com.t8rin.imagetoolbox.core.utils

import android.content.Context
import android.content.Intent
import kotlin.system.exitProcess

/**
 * App 冷重启工具：拉起 launcher intent 并杀掉当前进程，
 * 让 DI/Room/MMKV 等进程内单例在下次启动时全部重建。
 */
object AppRestarter {

    fun restartToColdStart(context: Context) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: return
        launchIntent.addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK
        )
        context.startActivity(launchIntent)
        // Ensure process is killed so everything reopens from disk.
        exitProcess(0)
    }
}
