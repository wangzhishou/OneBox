package com.wanbaohe.textcard.presentation.editor

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent

/**
 * 文字编辑弹窗:再点已选中的文字块弹出,只管编辑内容;
 * 增删统一走「基础」面板与图层面板。
 */
@Composable
fun TextEditSheet(
    visible: Boolean,
    component: TextCardComponent,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    val block = component.selectedTextBlock() ?: return
    var content by androidx.compose.runtime.remember(block.id) {
        mutableStateOf(block.content)
    }

    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.textcard_edit_text_title)) },
        text = {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text(stringResource(R.string.textcard_edit_title_hint)) },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            com.shifenmiao.base.ui.button.ConfirmButton(
                onClick = {
                    component.updateTextBlock(block.id) { it.copy(content = content) }
                    onDismiss()
                }
            )
        },
        dismissButton = {
            com.shifenmiao.base.ui.button.CancelButton(onClick = onDismiss)
        }
    )
}
