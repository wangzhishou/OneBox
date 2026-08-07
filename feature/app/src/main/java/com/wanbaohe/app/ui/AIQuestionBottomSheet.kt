package com.wanbaohe.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.ai.agent.tool.AgentUserQuestionRequest
import com.shifenmiao.base.ui.button.CancelButton
import com.shifenmiao.base.ui.button.ConfirmButton
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet

@Composable
fun AIQuestionBottomSheet(
    request: AgentUserQuestionRequest,
    formState: AIQuestionFormState,
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    EnhancedModalBottomSheet(
        visible = true,
        onDismiss = { onCancel() },
        enableBottomContentWeight = false,
        title = {},
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                CancelButton(
                    onClick = onCancel,
                    text = request.cancelText.ifBlank { stringResource(R.string.button_cancel) }
                )
                ConfirmButton(
                    onClick = onSubmit,
                    enabled = formState.isValid(request.questions),
                    text = request.confirmText.ifBlank { stringResource(R.string.agent_tool_confirm_approve) }
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            request.title.takeIf { it.isNotBlank() }?.let { title ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            CompositionLocalProvider(LocalAIQuestionPickerPlaceAboveAll provides true) {
                AIQuestionContent(
                    request = request,
                    formState = formState,
                    modifier = Modifier.fillMaxWidth(),
                    useLazyColumn = true
                )
            }
        }

    }
}
