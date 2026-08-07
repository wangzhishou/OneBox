package com.shifenmiao.ai.agent.tool

import com.shifenmiao.model.event.PermissionRequest
import com.t8rin.imagetoolbox.core.ui.utils.helper.ContextUtils
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

