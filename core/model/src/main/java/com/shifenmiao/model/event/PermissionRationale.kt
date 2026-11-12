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
import android.content.Context
import androidx.annotation.StringRes
import com.shifenmiao.core.R

/**
 * 运行时权限的"申请前用途说明"文案。
 *
 * 应用商店与国内隐私合规检测要求: 申请运行时权限前必须在同一屏说明用途,
 * 且不得在没有对应功能时提前申请。所以这里按**实际申请的权限**拼文案,
 * 不依赖 [PermissionRequest] 枚举里写死的句子 —— 例如 [PermissionRequest.ALL]
 * 带着任意权限列表, 写一句"请求访问所有权限"本身就属于违规表述。
 *
 * 文案一律用调用方传入的 [Context] 解析: 这个 Context 来自界面(Activity),
 * 才会跟随 App 内切换的语言; 用 AppContext 会拿到进程启动时的语言。
 */
object PermissionRationale {

    private data class Reason(
        @StringRes val titleRes: Int,
        @StringRes val descriptionRes: Int,
    )

    /** 单个权限对应的说明; 未收录的权限返回 null, 由调用方走通用文案。 */
    private fun reasonFor(permission: String): Reason? = when (permission) {
        Manifest.permission.CAMERA ->
            Reason(R.string.privacy_item_camera_title, R.string.privacy_item_camera_description)

        Manifest.permission.RECORD_AUDIO ->
            Reason(R.string.privacy_item_4, R.string.privacy_item_4_description)

        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_MEDIA_LOCATION ->
            Reason(
                R.string.permission_rationale_media_title,
                R.string.permission_rationale_media_description
            )

        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION ->
            Reason(
                R.string.permission_rationale_location_title,
                R.string.permission_rationale_location_description
            )

        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS ->
            Reason(
                R.string.permission_rationale_contacts_title,
                R.string.permission_rationale_contacts_description
            )

        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR ->
            Reason(
                R.string.permission_rationale_calendar_title,
                R.string.permission_rationale_calendar_description
            )

        Manifest.permission.POST_NOTIFICATIONS ->
            Reason(
                R.string.permission_rationale_notification_title,
                R.string.permission_rationale_notification_description
            )

        Manifest.permission.REQUEST_INSTALL_PACKAGES ->
            Reason(
                R.string.permission_rationale_install_title,
                R.string.permission_rationale_install_description
            )

        Manifest.permission.MANAGE_EXTERNAL_STORAGE ->
            Reason(
                R.string.permission_rationale_all_files_title,
                R.string.permission_rationale_all_files_description
            )

        else -> null
    }

    private fun reasonsOf(permissions: List<String>): List<Reason> =
        permissions.mapNotNull(::reasonFor).distinctBy { it.descriptionRes }

    /**
     * 弹窗标题: 枚举自带标题优先; 否则按实际权限取(单一权限用它的标题, 多种权限用通用标题)。
     */
    fun title(
        context: Context,
        permissionRequest: PermissionRequest,
        permissions: List<String>,
    ): String {
        if (permissionRequest.titleRes != 0) {
            return context.getString(permissionRequest.titleRes)
        }
        val reasons = reasonsOf(permissions)
        return if (reasons.size == 1) {
            context.getString(reasons.first().titleRes)
        } else {
            context.getString(R.string.permission_rationale_title)
        }
    }

    /**
     * 弹窗正文: 枚举自带说明优先; 否则把实际权限的用途逐条拼起来。
     */
    fun description(
        context: Context,
        permissionRequest: PermissionRequest,
        permissions: List<String>,
    ): String {
        if (permissionRequest.descriptionRes != 0) {
            return context.getString(permissionRequest.descriptionRes)
        }
        val reasons = reasonsOf(permissions)
        if (reasons.isEmpty()) {
            return context.getString(R.string.permission_rationale_description)
        }
        return reasons.joinToString(separator = "\n") {
            context.getString(it.descriptionRes)
        }
    }
}
