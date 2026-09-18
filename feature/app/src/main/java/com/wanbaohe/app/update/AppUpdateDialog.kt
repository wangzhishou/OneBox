package com.wanbaohe.app.update

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.shifenmiao.base.ui.ConfirmDialog
import com.shifenmiao.storage.AppSharedStorage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSync
import com.shifenmiao.core.R as CoreR

/**
 * 启动弹窗式更新提醒。
 *
 * 是否弹完全由远程配置决定（见 [shouldShowUpdateDialog]）：
 * - 未下发 / dialogEnabled != true：不弹，只保留角标与设置页副标题；
 * - dialogForVersionBelow：把弹窗收窄到"版本特别老"的那一段装机。
 *
 * 挂在 AppContent 的全局弹窗区（与隐私弹窗同一层），
 * 远程配置要在用户同意隐私后才会拉取，所以不会盖住首次启动的合规弹窗。
 */
@Composable
fun AppUpdateDialogHost() {
    val context = LocalContext.current
    val checker = remember(context) { openSourceReleaseChecker(context) }
    val release by checker.latestRelease.collectAsState()
    val appUpdateConfig = rememberAppUpdateConfig()

    // 本次启动是否已经回答过（点过"立即更新"或"稍后"），避免配置刷新后重复弹
    var answeredThisSession by rememberSaveable { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }

    LaunchedEffect(release, appUpdateConfig, answeredThisSession) {
        val target = release
        if (target == null) {
            showDialog.value = false
            return@LaunchedEffect
        }
        showDialog.value = !answeredThisSession && shouldShowUpdateDialog(
            isNewer = target.isNewer,
            tag = target.tag,
            localVersionCode = currentVersionCode(context),
            config = appUpdateConfig,
            dismissedTag = AppSharedStorage.loadUpdatePromptDismissedTag(),
            dismissedAtMs = AppSharedStorage.loadUpdatePromptDismissedAt(),
            nowMs = System.currentTimeMillis(),
        )
    }

    val target = release
    if (target != null && showDialog.value) {
        ConfirmDialog(
            title = stringResource(CoreR.string.app_update_dialog_title, target.tag),
            message = stringResource(
                CoreR.string.app_update_dialog_message,
                currentVersionName(context),
                target.tag,
            ),
            confirmButtonText = stringResource(CoreR.string.app_update_dialog_confirm),
            dismissButtonText = stringResource(CoreR.string.app_update_dialog_later),
            onConfirm = {
                answeredThisSession = true
                showDialog.value = false
                AppSharedStorage.saveUpdatePromptDismissed(target.tag, System.currentTimeMillis())
                openUpdatePage(context, resolveUpdateUrl(target, appUpdateConfig))
            },
            onDismiss = {
                answeredThisSession = true
                showDialog.value = false
                // 记 tag + 时间：冷却期内不再弹同一个版本，出了更新的 tag 立刻重新弹
                AppSharedStorage.saveUpdatePromptDismissed(target.tag, System.currentTimeMillis())
            },
            showDialog = showDialog,
            icon = {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSync,
                    contentDescription = null,
                )
            },
        )
    }
}
