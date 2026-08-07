package com.wanbaohe.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.ai.agent.tool.AgentUserQuestionPresentation
import com.shifenmiao.ai.component.GlobalToolUiHost
import com.shifenmiao.base.ui.button.CancelButton
import com.shifenmiao.base.ui.button.ConfirmButton
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.FileType
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFilePicker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFolderPicker
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog

/**
 * 将 LLM 传入的 mimeType 字符串（如 "image/jpg"、多类型逗号分隔）
 * 转换为 picker 使用的 [MimeType] 对象。
 */
private fun String.toPickerMimeType(): MimeType {
    if (isBlank() || this == "*/*") return MimeType.All
    val parts = split(",").map { it.trim() }.filter { it.isNotEmpty() }
    return when {
        parts.size == 1 -> MimeType.Single(parts[0])
        else -> MimeType.Multiple(parts.toSet())
    }
}

@Composable
fun AIGlobalActionOverlay(
    toolUiHost: GlobalToolUiHost,
) {
    val filePickerRequest by toolUiHost.filePickerRequest.collectAsState()
    val folderPickerRequest by toolUiHost.folderPickerRequest.collectAsState()

    // Blocker 1：从当前请求派生 mimeType，变化时 rememberFilePicker 自动重建
    val currentMimeType = remember(filePickerRequest?.mimeType) {
        filePickerRequest?.mimeType?.toPickerMimeType() ?: MimeType.All
    }

    // ── 文件 / 目录 picker 必须无条件注册（rememberLauncherForActivityResult 限制）──
    val singleFilePicker = rememberFilePicker(
        type = FileType.Single,
        mimeType = currentMimeType,
        onFailure = { toolUiHost.cancelFilePicker() },
        onSuccess = { uris ->
            toolUiHost.submitFilePicker(uris.joinToString(",") { it.toString() })
        })
    val multipleFilePicker = rememberFilePicker(
        type = FileType.Multiple,
        mimeType = currentMimeType,
        onFailure = { toolUiHost.cancelFilePicker() },
        onSuccess = { uris ->
            toolUiHost.submitFilePicker(uris.joinToString(",") { it.toString() })
        })
    val folderPicker = rememberFolderPicker(
        onFailure = { toolUiHost.cancelFolderPicker() },
        onSuccess = { uri -> toolUiHost.submitFolderPicker(uri.toString()) })

    // Blocker 2：触发文件选取前先展示 message，让用户明白 AI 为什么要选文件
    LaunchedEffect(filePickerRequest?.toolCallId) {
        filePickerRequest?.let { req ->
            if (req.message.isNotBlank()) {
                AppToastHost.showToast(req.message)
            }
            if (req.multiple) {
                multipleFilePicker.pickFile()
            } else {
                singleFilePicker.pickFile()
            }
        }
    }

    LaunchedEffect(folderPickerRequest?.toolCallId) {
        folderPickerRequest?.let { req ->
            if (req.message.isNotBlank()) {
                AppToastHost.showToast(req.message)
            }
            folderPicker.pickFolder()
        }
    }

    // ── 工具确认对话框 ──────────────────────────────────────────────────────
    val confirmationRequest by toolUiHost.confirmationRequest.collectAsState()
    val questionRequest by toolUiHost.questionRequest.collectAsState()

    confirmationRequest?.let { waiting ->
        val onDismiss = {
            val dismissPayload = waiting.dismissPayload
            if (dismissPayload != null) {
                toolUiHost.submitConfirmation(dismissPayload)
            } else {
                toolUiHost.cancelConfirmation()
            }
        }

        val titleContent: (@Composable () -> Unit)? = if (waiting.dialogTitle.isNotBlank()) {
            { Text(waiting.dialogTitle) }
        } else {
            null
        }

        val submitText =
            waiting.submitButtonText.ifBlank { stringResource(R.string.agent_tool_confirm_approve) }
        val cancelText =
            waiting.cancelButtonText.ifBlank { stringResource(R.string.agent_tool_confirm_reject) }

        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = onDismiss,
            title = titleContent,
            text = {
                if (waiting.dialogMessage.isNotBlank()) {
                    Text(waiting.dialogMessage)
                }
            },
            dismissButton = {
                CancelButton(onClick = onDismiss, text = cancelText)
            },
            confirmButton = {
                ConfirmButton(onClick = {
                    val payload = waiting.confirmPayload
                    if (!payload.isNullOrBlank()) {
                        toolUiHost.submitConfirmation(payload)
                    } else {
                        toolUiHost.submitConfirmation("{}")
                    }
                }, text = submitText)
            },
            placeAboveAll = true
        )
        return
    }

    // ── 结构化问询表单 ──────────────────────────────────────────────────────
    questionRequest?.let { waiting ->
        val formState = remember(waiting.toolCallId) {
            AIQuestionFormState()
        }
        val submitAnswers = {
            toolUiHost.submitUserQuestionAnswers(
                formState.buildAnswers(waiting.questions)
            )
        }
        val cancelQuestion = {
            toolUiHost.cancelUserQuestion()
        }

        when (waiting.presentation) {
            AgentUserQuestionPresentation.dialog -> AIQuestionDialog(
                request = waiting,
                formState = formState,
                onSubmit = submitAnswers,
                onCancel = cancelQuestion
            )

            AgentUserQuestionPresentation.bottom_sheet -> AIQuestionBottomSheet(
                request = waiting,
                formState = formState,
                onSubmit = submitAnswers,
                onCancel = cancelQuestion
            )
        }
        return
    }
}
