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

package com.shifenmiao.model.event

import android.Manifest
import android.annotation.SuppressLint
import androidx.annotation.StringRes
import com.shifenmiao.core.R

data class RequestPermissionEvent(
    val permissions: List<String> = emptyList<String>(),
    val permissionRequest: PermissionRequest = PermissionRequest.ALL,
    val onSuccess: () -> Unit = {},
    val onFailed: () -> Unit = {},
    val onRequest: () -> Unit = {}
)


/**
 * 运行时权限申请类型。
 *
 * [titleRes]/[descriptionRes] 是申请前用途说明的资源 id, 0 表示"按实际申请的权限推导"
 * (见 [PermissionRationale])。这里只存 id 不存字符串: 文案必须按用户当前语言解析,
 * 在枚举初始化阶段用 AppContext 取字符串会锁死成进程启动时的语言。
 */
enum class PermissionRequest(
    val code: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val permissions: Array<String>,
    val requestCode: Int
) {
    // 说明文案留空: 由 PermissionRationale 按实际申请的权限推导,
    // 避免出现"Request access to all permissions"这种违规表述。
    ALL(9, 0, 0, arrayOf(), 1000),
    CAMERA(
        1,
        R.string.privacy_item_camera_title,
        R.string.privacy_item_camera_description,
        arrayOf(Manifest.permission.CAMERA),
        1001
    ),
    LOCATION(
        2,
        0,
        0,
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
        1002
    ),
    STORAGE(
        3,
        R.string.privacy_item_1,
        R.string.privacy_item_storage_description,
        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
        1003
    ),
    CONTACTS(
        4,
        0,
        0,
        arrayOf(Manifest.permission.READ_CONTACTS),
        1004
    ),
    MICROPHONE(
        5,
        R.string.privacy_item_4,
        R.string.privacy_item_4_description,
        arrayOf(Manifest.permission.RECORD_AUDIO),
        1005
    ),

    @SuppressLint("InlinedApi")
    APK_INSTALL(
        6,
        0,
        0,
        arrayOf(Manifest.permission.REQUEST_INSTALL_PACKAGES),
        1006
    ),
    NEW_PERMISSION(
        7,
        0,
        0,
        arrayOf("your.new.permission.HERE"),
        1007
    ) // New permission added
}
