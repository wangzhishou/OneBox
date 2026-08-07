package com.wanbaohe.markdown.edit.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedButton
import com.wanbaohe.markdown.edit.R as MarkdownR

/**
 * 链接插入对话框
 *
 * @param visible 是否显示
 * @param initialText 初始文本（如果有选中的文本）
 * @param onConfirm 确认回调，返回 (文本, URL)
 * @param onDismiss 取消回调
 */
@Composable
fun LinkInsertDialog(
    visible: Boolean,
    initialText: String = "",
    onConfirm: (text: String, url: String) -> Unit,
    onDismiss: () -> Unit
) {
    var linkText by remember(visible) { mutableStateOf(initialText) }
    var linkUrl by remember(visible) { mutableStateOf("https://") }

    EnhancedAlertDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.insert_link)) },
        confirmButton = {
            EnhancedButton(
                onClick = {
                    if (linkText.isNotBlank() && linkUrl.isNotBlank()) {
                        onConfirm(linkText, linkUrl)
                    }
                },
                enabled = linkText.isNotBlank() && linkUrl.isNotBlank()
            ) {
                Text(stringResource(R.string.button_confirm))
            }
        },
        dismissButton = {
            EnhancedButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.button_cancel))
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 链接文本输入
                OutlinedTextField(
                    value = linkText,
                    onValueChange = { linkText = it },
                    label = { Text(stringResource(R.string.link_text_hint)) },
                    placeholder = { Text(stringResource(MarkdownR.string.link_text_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // URL 输入
                OutlinedTextField(
                    value = linkUrl,
                    onValueChange = { linkUrl = it },
                    label = { Text(stringResource(R.string.link_url_hint)) },
                    placeholder = { Text("https://example.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

