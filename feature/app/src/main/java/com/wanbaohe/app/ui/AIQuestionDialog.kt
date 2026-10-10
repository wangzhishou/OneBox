package com.wanbaohe.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.shifenmiao.ai.agent.tool.AgentUserQuestionRequest
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog

@Composable
fun AIQuestionDialog(
    request: AgentUserQuestionRequest,
    formState: AIQuestionFormState,
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    val titleContent: (@Composable () -> Unit)? = request.title
        .takeIf { it.isNotBlank() }
        ?.let { title ->
            { Text(title) }
        }

    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = onCancel,
        // Agent 正在挂起等待表单结果: 点遮罩/返回键不能取消,
        // 否则用户一次误触就让 Agent 收到 cancelled 并瞎编答案继续跑;
        // 只能点「取消」按钮主动取消。
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        title = titleContent,
        text = {
            // dismissOnBackPress=false 时 FullscreenPopup 不再注册返回处理,
            // 返回键会穿透到 App 层(触发导航/退出提示); 在这里消费掉, 表单期间返回键不做事。
            BackHandler(enabled = true) { /* no-op */ }
            CompositionLocalProvider(LocalAIQuestionPickerPlaceAboveAll provides true) {
                AIQuestionContent(
                    request = request,
                    formState = formState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding(),
                    useLazyColumn = false
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(request.cancelText.ifBlank { stringResource(R.string.button_cancel) })
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSubmit,
                enabled = formState.isValid(request.questions)
            ) {
                Text(request.confirmText.ifBlank { stringResource(R.string.agent_tool_confirm_approve) })
            }
        },
        placeAboveAll = true
    )
}
