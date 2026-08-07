package com.wanbaohe.markdown.edit.webview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.button.CancelButton
import com.shifenmiao.base.ui.button.ConfirmButton
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTextButton

/**
 * 插入链接对话框 —— 弹窗本身不透明,内部输入/按钮用玻璃组件
 */
@Composable
fun InsertLinkDialog(
    initialText: String = "",
    onDismiss: () -> Unit,
    onInsert: (text: String, url: String) -> Unit
) {
    var linkText by remember { mutableStateOf(initialText) }
    var linkUrl by remember { mutableStateOf("") }

    val textFocusRequester = remember { FocusRequester() }
    val urlFocusRequester = remember { FocusRequester() }

    val defaultLinkText = stringResource(R.string.link_default_text)

    // 如果已有链接文字,自动聚焦到链接地址输入框
    LaunchedEffect(Unit) {
        if (initialText.isNotBlank()) {
            urlFocusRequester.requestFocus()
        } else {
            textFocusRequester.requestFocus()
        }
    }

    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = onDismiss,
        enableGlass = false,
        title = {
            Text(text = stringResource(R.string.insert_link))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassOutlinedTextField(
                    value = linkText,
                    onValueChange = { linkText = it },
                    label = { Text(stringResource(R.string.link_text_hint)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(textFocusRequester)
                )
                GlassOutlinedTextField(
                    value = linkUrl,
                    onValueChange = { linkUrl = it },
                    label = { Text(stringResource(R.string.link_url_hint)) },
                    placeholder = { Text("https://...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(urlFocusRequester)
                )
            }
        },
        confirmButton = {
            ConfirmButton(
                text = stringResource(R.string.insert),
                onClick = {
                    val finalText = linkText.ifBlank { defaultLinkText }
                    onInsert(finalText.trim(), linkUrl.trim())
                },
                enabled = linkUrl.isNotBlank(),
            )
        },
        dismissButton = {
            CancelButton(
                onClick = onDismiss
            )
        }
    )
}
