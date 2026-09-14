package com.wanbaohe.app.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.ai.agent.tool.AgentUserQuestionRequest
import com.shifenmiao.ai.agent.tool.ui.AskUserA2uiForm
import com.shifenmiao.ai.agent.tool.ui.AskUserA2uiFormState
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.a2ui.catalog.A2uiRenderProvider
import com.wanbaohe.a2ui.catalog.LocalA2uiPlaceAboveAll

@Composable
fun AIQuestionDialog(
    request: AgentUserQuestionRequest,
    formState: AskUserA2uiFormState,
    renderProvider: A2uiRenderProvider,
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
            CompositionLocalProvider(LocalA2uiPlaceAboveAll provides true) {
                AskUserA2uiForm(
                    formState = formState,
                    renderProvider = renderProvider,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                        .imePadding(),
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
                enabled = formState.isValid
            ) {
                Text(request.confirmText.ifBlank { stringResource(R.string.agent_tool_confirm_approve) })
            }
        },
        placeAboveAll = true
    )
}
