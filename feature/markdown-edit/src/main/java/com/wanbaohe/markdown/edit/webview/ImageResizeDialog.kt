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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wanbaohe.base.ui.button.CancelButton
import com.wanbaohe.base.ui.button.ConfirmButton
import com.shifenmiao.core.R
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.sliders.custom_slider.CustomSlider
import com.wanbaohe.markdown.edit.R as MarkdownR
import kotlin.math.roundToInt

/**
 * 图片缩放调整弹窗
 */
@Composable
fun ImageResizeDialog(
    currentWidth: Int,
    maxWidth: Int,
    naturalWidth: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    onReset: () -> Unit,
    onPreview: ((Int) -> Unit)? = null // 实时预览回调
) {
    val minWidth = 50f
    val effectiveMaxWidth = maxWidth.coerceAtLeast(minWidth.toInt()).toFloat()

    var sliderValue by remember {
        mutableFloatStateOf(currentWidth.toFloat().coerceIn(minWidth, effectiveMaxWidth))
    }

    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = {
            // 取消时恢复原始宽度
            onPreview?.invoke(currentWidth)
            onDismiss()
        },
        title = {
            Text(text = stringResource(MarkdownR.string.resize_image))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 当前宽度显示
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(MarkdownR.string.markdown_image_resize_width),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${sliderValue.roundToInt()} px",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 滑块
                CustomSlider(
                    value = sliderValue,
                    onValueChange = { newValue ->
                        sliderValue = newValue
                        // 实时预览
                        onPreview?.invoke(newValue.roundToInt())
                    },
                    valueRange = minWidth..effectiveMaxWidth,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 快捷按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickSizeButton(
                        text = "25%",
                        onClick = {
                            sliderValue = (effectiveMaxWidth * 0.25f).coerceAtLeast(minWidth)
                            onPreview?.invoke(sliderValue.roundToInt())
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickSizeButton(
                        text = "50%",
                        onClick = {
                            sliderValue = (effectiveMaxWidth * 0.5f).coerceAtLeast(minWidth)
                            onPreview?.invoke(sliderValue.roundToInt())
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickSizeButton(
                        text = "75%",
                        onClick = {
                            sliderValue = (effectiveMaxWidth * 0.75f).coerceAtLeast(minWidth)
                            onPreview?.invoke(sliderValue.roundToInt())
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickSizeButton(
                        text = "100%",
                        onClick = {
                            sliderValue = effectiveMaxWidth
                            onPreview?.invoke(sliderValue.roundToInt())
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 原始大小信息
                if (naturalWidth > 0) {
                    Text(
                        text = stringResource(MarkdownR.string.original_width, naturalWidth),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Row {
                CancelButton(stringResource(R.string.reset_origin) ) {
                    onPreview?.invoke(0) // 预览原始大小
                    onReset.invoke()
                }
                Spacer(modifier = Modifier.width(8.dp))

                ConfirmButton(stringResource(R.string.button_confirm) ) {
                    onConfirm(sliderValue.roundToInt())
                }
            }
        },
        dismissButton = {
            CancelButton(stringResource(R.string.cancel) ) {
                // 取消时恢复原始宽度
                onPreview?.invoke(currentWidth)
                onDismiss.invoke()
            }
        }
    )
}

@Composable
private fun QuickSizeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

