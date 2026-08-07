package com.wanbaohe.app.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
        title = titleContent,
        text = {
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
