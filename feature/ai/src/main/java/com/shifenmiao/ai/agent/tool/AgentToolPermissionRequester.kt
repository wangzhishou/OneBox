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

package com.shifenmiao.ai.agent.tool

import com.shifenmiao.model.event.PermissionRequest
import com.wanbaohe.core.ui.utils.helper.ContextUtils
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class AgentToolPermissionRequester @Inject constructor() {

    suspend fun requestPermissions(
        permissions: List<String>,
        permissionRequest: PermissionRequest
    ): Boolean {
        val normalizedPermissions = permissions
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
        if (normalizedPermissions.isEmpty()) return true

        return suspendCancellableCoroutine { continuation ->
            ContextUtils.requestPermissionAndExecute(
                permissions = normalizedPermissions.toTypedArray(),
                permissionRequest = permissionRequest,
                onGranted = {
                    if (continuation.isActive) continuation.resume(true)
                },
                onDenied = {
                    if (continuation.isActive) continuation.resume(false)
                }
            )
        }
    }
}

