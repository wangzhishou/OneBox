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
 * 图片插入对话框
 *
 * @param visible 是否显示
 * @param onConfirm 确认回调，返回 (描述, URL)
 * @param onSelectImage 选择本地图片回调
 * @param onDismiss 取消回调
 */
@Composable
fun ImageInsertDialog(
    visible: Boolean,
    onConfirm: (description: String, url: String) -> Unit,
    onSelectImage: () -> Unit,
    onDismiss: () -> Unit
) {
    var imageDescription by remember(visible) { mutableStateOf("") }
    var imageUrl by remember(visible) { mutableStateOf("https://") }

    EnhancedAlertDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.insert_image)) },
        confirmButton = {
            EnhancedButton(
                onClick = {
                    if (imageDescription.isNotBlank() && imageUrl.isNotBlank()) {
                        onConfirm(imageDescription, imageUrl)
                    }
                },
                enabled = imageDescription.isNotBlank() && imageUrl.isNotBlank()
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

                // 选择本地图片按钮
                EnhancedButton(
                    onClick = {
                        onDismiss()
                        onSelectImage()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.pick_from_gallery))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(stringResource(MarkdownR.string.or_enter_image_url))

                Spacer(modifier = Modifier.height(8.dp))

                // 图片描述输入
                OutlinedTextField(
                    value = imageDescription,
                    onValueChange = { imageDescription = it },
                    label = { Text(stringResource(MarkdownR.string.image_description_label)) },
                    placeholder = { Text(stringResource(MarkdownR.string.image_description_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 图片 URL 输入
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text(stringResource(MarkdownR.string.image_url_label)) },
                    placeholder = { Text("https://example.com/image.jpg") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

