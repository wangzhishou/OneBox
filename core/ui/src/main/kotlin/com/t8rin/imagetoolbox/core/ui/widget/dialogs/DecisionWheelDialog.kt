package com.t8rin.imagetoolbox.core.ui.widget.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.BasicEnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedButton
import com.t8rin.imagetoolbox.core.ui.widget.other.DecisionWheel
import com.t8rin.imagetoolbox.core.ui.widget.other.DecisionWheelItem
import com.wanbaohe.com.color.ColorGenerator
import com.t8rin.imagetoolbox.core.resources.icons.Refresh

/**
 * 决策转盘弹窗。
 *
 * 弹窗内嵌入 [DecisionWheel] 转盘组件，用户点击开始后转盘旋转，
 * 旋转停止后在弹窗内展示选中的结果。
 *
 * @param visible 是否显示弹窗
 * @param items 决策项目文本列表（至少2项）
 * @param title 弹窗标题，默认空
 * @param onDismissRequest 弹窗关闭请求回调
 * @param onItemSelected 旋转停止后回调，参数为选中的项目文本
 * @param spinDurationMillis 旋转动画时长（毫秒），默认 4000
 */
@Composable
fun DecisionWheelDialog(
    visible: Boolean,
    items: List<String>,
    title: String = "",
    onDismissRequest: () -> Unit,
    onItemSelected: (String) -> Unit,
    spinDurationMillis: Int = 4000
) {
    if (items.size < 2) return

    var showResult by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<DecisionWheelItem?>(null) }
    var isSpinning by remember { mutableStateOf(false) }

    // 自动生成颜色
    val baseColor = MaterialTheme.colorScheme.primaryContainer
    val wheelItems = remember(items, baseColor) {
        val colors = ColorGenerator.generateSegmentBackgrounds(baseColor, items.size)
        items.mapIndexed { index, label ->
            DecisionWheelItem(
                label = label,
                color = colors.getOrNull(index) ?: baseColor
            )
        }
    }

    LaunchedEffect(visible) {
        if (visible) {
            showResult = false
            selectedItem = null
            isSpinning = false
        }
    }

    BasicEnhancedAlertDialog(
        visible = visible,
        onDismissRequest = {
            if (!isSpinning) {
                onDismissRequest()
            }
        },
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = !isSpinning,
            dismissOnClickOutside = !isSpinning
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (title.isNotBlank()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 转盘区域
            AnimatedVisibility(
                visible = !showResult,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                DecisionWheel(
                    items = wheelItems,
                    onItemSelected = { item ->
                        selectedItem = item
                        showResult = true
                        onItemSelected(item.label)
                    },
                    onSpinningStateChanged = { spinning ->
                        isSpinning = spinning
                    },
                    spinDurationMillis = spinDurationMillis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 结果展示
            AnimatedVisibility(
                visible = showResult && selectedItem != null,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                selectedItem?.let { item ->
                    val contentColor = remember(item.color) {
                        ColorGenerator.contentColorFor(item.color)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.result_is),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = item.color,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp
                        ) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = contentColor,
                                modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // 按钮区域
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (showResult) {
                    EnhancedButton(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = {
                            showResult = false
                            selectedItem = null
                        }
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(stringResource(R.string.spin_again))
                    }
                }

                EnhancedButton(
                    onClick = onDismissRequest,
                    enabled = !isSpinning
                ) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}
