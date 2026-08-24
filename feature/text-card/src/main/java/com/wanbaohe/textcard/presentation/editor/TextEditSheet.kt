package com.wanbaohe.textcard.presentation.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.TextBlockId
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent

/**
 * 文字编辑弹窗:点画布标题/正文弹出,编辑两个文本块内容,确认后落到画布。
 */
@Composable
fun TextEditSheet(
    visible: Boolean,
    component: TextCardComponent,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    var title by rememberSaveable(visible) { mutableStateOf(component.title.content) }
    var body by rememberSaveable(visible) { mutableStateOf(component.body.content) }

    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.textcard_edit_text_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.textcard_edit_title_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text(stringResource(R.string.textcard_edit_body_hint)) },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }
        },
        confirmButton = {
            com.shifenmiao.base.ui.button.ConfirmButton(
                onClick = {
                    component.updateTextBlock(TextBlockId.Title) { it.copy(content = title) }
                    component.updateTextBlock(TextBlockId.Body) { it.copy(content = body) }
                    onDismiss()
                }
            )
        },
        dismissButton = {
            com.shifenmiao.base.ui.button.CancelButton(onClick = onDismiss)
        }
    )
}
