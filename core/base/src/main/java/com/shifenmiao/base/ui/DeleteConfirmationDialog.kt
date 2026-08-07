package com.shifenmiao.base.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.manager.DeleteConfirmationManager
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.resources.icons.Delete

/**
 * 高级删除确认对话框，支持"不再提示"选项
 *
 * @param operationType 操作类型，用于识别不同的删除操作
 * @param showDialog 控制对话框显示状态的MutableState
 * @param onConfirm 确认删除时的回调
 * @param title 对话框标题，默认为"删除"
 * @param message 对话框消息内容，默认为通用删除确认信息
 * @param showDoNotAskAgain 是否显示"不再提示"选项，默认为true
 */
@Composable
fun AdvancedDeleteConfirmDialog(
    operationType: String,
    showDialog: MutableState<Boolean>,
    onConfirm: () -> Unit,
    title: String = stringResource(R.string.ai_chat_delete_title),
    message: String = stringResource(R.string.ai_chat_delete_message),
    showDoNotAskAgain: Boolean = true
) {
    // 检查是否应该显示确认对话框
    if (!DeleteConfirmationManager.shouldConfirmDeletion(operationType)) {
        // 如果用户选择了不再提示，直接执行删除操作
        onConfirm()
        showDialog.value = false
        return
    }
    
    if (showDialog.value) {
        var doNotAskAgain by remember { mutableStateOf(false) }
        
        ConfirmContentDialog(
            title = title,
            confirmButtonText = stringResource(R.string.button_confirm),
            dismissButtonText = stringResource(R.string.button_cancel),
            onConfirm = {
                // 保存用户的"不再提示"选择
                if (doNotAskAgain) {
                    DeleteConfirmationManager.setShouldConfirmDeletion(operationType, false)
                }
                onConfirm()
            },
            onDismiss = {
                // 不执行任何操作，只关闭对话框
            },
            showDialog = showDialog,
            icon = {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Delete,
                    contentDescription = null
                )
            }
        ) {
            Column {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (showDoNotAskAgain) {
                    Spacer(modifier = Modifier.height(16.dp))
                    // "不再提示"选项
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = doNotAskAgain,
                            onCheckedChange = { doNotAskAgain = it }
                        )
                        Text(
                            text = stringResource(R.string.do_not_ask_again),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
