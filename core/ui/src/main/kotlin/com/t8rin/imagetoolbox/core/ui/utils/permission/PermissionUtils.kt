/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.core.ui.utils.permission

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat

object PermissionUtils {

    fun Context.checkPermissions(
        permissions: List<String>
    ): PermissionResult {

        val permissionPreference = PermissionPreference(this)

        val permissionResult = PermissionResult()

        val permissionStatus: HashMap<String, PermissionStatus> = hashMapOf()

        permissions.forEach { permission ->
            permissionPreference.setPermissionRequested(permission)
            if (hasPermissionAllowed(permission)) {
                permissionPreference.setPermissionAllowed(permission)
                permissionStatus[permission] = PermissionStatus.ALLOWED
            } else {
                val permissionRequestCount =
                    permissionPreference.permissionRequestCount(permission)
                when {
                    permissionRequestCount > 2 -> {
                        permissionStatus[permission] = PermissionStatus.DENIED_PERMANENTLY
                    }

                    else -> {
                        permissionStatus[permission] = PermissionStatus.NOT_GIVEN
                    }
                }
            }
        }

        permissionResult.permissionStatus = permissionStatus

        val isAnyPermissionDeniedPermanently =
            permissionStatus.values.any { it == PermissionStatus.DENIED_PERMANENTLY }

        if (isAnyPermissionDeniedPermanently) {
            permissionResult.finalStatus = PermissionStatus.DENIED_PERMANENTLY
            return permissionResult
        }

        val isAnyPermissionNotGiven =
            permissionStatus.values.any { it == PermissionStatus.NOT_GIVEN }

        if (isAnyPermissionNotGiven) {
            permissionResult.finalStatus = PermissionStatus.NOT_GIVEN
            return permissionResult
        }

        permissionResult.finalStatus = PermissionStatus.ALLOWED
        return permissionResult
    }


    fun Context.askUserToRequestPermissionExplicitly() {
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    fun Context.hasPermissionAllowed(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun Context.setPermissionsAllowed(permissions: List<String>) {
        permissions.forEach { permission ->
            PermissionPreference(this).setPermissionAllowed(permission)
        }
    }

}


// 使用 SharedPreferences 替代 DataStore+runBlocking，权限计数数据量极小，无需异步
private class PermissionPreference(private val context: Context) {

    private val prefs by lazy {
        context.getSharedPreferences("permissionPreference", Context.MODE_PRIVATE)
    }

    fun permissionRequestCount(permission: String): Int {
        return prefs.getInt(permission, 0)
    }

    fun setPermissionRequested(permission: String) {
        val current = prefs.getInt(permission, 0)
        prefs.edit().putInt(permission, current + 1).apply()
    }

    fun setPermissionAllowed(permission: String) {
        prefs.edit().putInt(permission, 0).apply()
    }

}