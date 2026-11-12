/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.markdown.edit.webview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wanbaohe.base.ui.button.CancelButton
import com.wanbaohe.base.ui.button.ConfirmButton
import com.shifenmiao.core.R
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.glass.GlassButton
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.glass.GlassTextButton
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage

/**
 * 插入图片对话框 —— 弹窗本身不透明,内部输入/按钮用玻璃组件
 */
@Composable
fun InsertImageDialog(
    onDismiss: () -> Unit,
    onPickFromGallery: () -> Unit,
    onInsertUrl: (url: String, alt: String) -> Unit
) {
    var imageUrl by remember { mutableStateOf("") }
    var imageAlt by remember { mutableStateOf("") }

    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = onDismiss,
        enableGlass = false,
        title = {
            Text(text = stringResource(R.string.insert_image))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 从相册选择 - 用一个 Row 包裹按钮让图标和文字水平排列
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GlassButton(
                        onClick = onPickFromGallery,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage,
                            contentDescription = null,
                            modifier = Modifier.height(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.pick_from_gallery))
                    }
                }

                GlassOutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text(stringResource(R.string.image_url_hint)) },
                    placeholder = { Text("https://...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                GlassOutlinedTextField(
                    value = imageAlt,
                    onValueChange = { imageAlt = it },
                    label = { Text(stringResource(R.string.image_alt_hint)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            ConfirmButton(
                text = stringResource(R.string.insert),
                onClick = {
                    onInsertUrl(imageUrl.trim(), imageAlt.trim())
                },
                enabled = imageUrl.isNotBlank(),
            )
        },
        dismissButton = {
            CancelButton(
                onClick = onDismiss
            )
        }
    )
}
