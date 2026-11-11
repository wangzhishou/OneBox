package com.wanbaohe.base.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.DeleteSweep

/**
 * 「一键清空历史」的公共确认弹窗。
 *
 * 清空是不可逆操作，所有清空入口（操作记录、转盘历史、诗词历史等）共用这一道确认，
 * 不要在页面里另拼一套 EnhancedAlertDialog；容器与按钮沿用 [ConfirmContentDialog]。
 * 各模块可传入自己的标题与说明（例如「已收藏的诗词会保留」），按钮文案同样可覆盖。
 *
 * @param showDialog 显示状态，确认 / 取消 / 点弹窗外 / 返回键都会自动置为 false
 * @param onConfirm 用户确认后执行真正的清空，组件不代管业务逻辑
 */
@Composable
fun ClearHistoryConfirmDialog(
    showDialog: MutableState<Boolean>,
    onConfirm: () -> Unit,
    title: String = stringResource(R.string.clear_history),
    message: String = stringResource(R.string.clear_history_confirm_message),
    confirmButtonText: String = stringResource(R.string.button_confirm),
    dismissButtonText: String = stringResource(R.string.button_cancel),
) {
    ConfirmContentDialog(
        title = title,
        confirmButtonText = confirmButtonText,
        dismissButtonText = dismissButtonText,
        onConfirm = onConfirm,
        onDismiss = {},
        showDialog = showDialog,
        icon = {
            Icon(
                imageVector = Icons.Outlined.DeleteSweep,
                contentDescription = null
            )
        }
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
