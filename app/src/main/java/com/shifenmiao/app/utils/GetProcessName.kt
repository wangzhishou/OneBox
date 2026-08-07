/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
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

package com.shifenmiao.app.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context.ACTIVITY_SERVICE
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Process

internal fun Application.isMain(): Boolean =
    getProcessName() == packageName


@SuppressLint("PrivateApi")
private fun Application.getProcessName(): String? {
    if (SDK_INT >= Build.VERSION_CODES.P) {
        return Application.getProcessName()
    }

    val pid = Process.myPid()
    val activityManager = getSystemService(ACTIVITY_SERVICE) as? ActivityManager
    val processName =
        activityManager?.runningAppProcesses?.firstOrNull { it.pid == pid }?.processName
    if (processName != null) {
        return processName
    }

    // Try using ActivityThread to determine the current process name.
    try {
        val activityThread = Class.forName(
            "android.app.ActivityThread",
            false,
            this::class.java.classLoader
        )
        val packageName: Any?
        val currentProcessName = activityThread.getDeclaredMethod("currentProcessName")
        currentProcessName.isAccessible = true
        packageName = currentProcessName.invoke(null)
        if (packageName is String) {
            return packageName
        }
    } catch (_: Throwable) {
        return null
    }
    return null
}