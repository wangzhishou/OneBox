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
import com.shifenmiao.base.ui.button.CancelButton
import com.shifenmiao.base.ui.button.ConfirmButton
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.sliders.custom_slider.CustomSlider
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
                        text = stringResource(MarkdownR.string.width),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${sliderValue.roundToInt()} px",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
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

