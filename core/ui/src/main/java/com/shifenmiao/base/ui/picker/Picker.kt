package com.shifenmiao.base.ui.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.button.CancelButton
import com.shifenmiao.base.ui.button.ConfirmButton
import com.shifenmiao.base.ui.popup.MaskedPopup
import com.shifenmiao.theme.AppTheme

@Composable
fun Picker(
    visible: Boolean,
    ranges: Array<List<String>>,
    values: Array<Int>,
    title: String? = null,
    onCancel: () -> Unit,
    onColumnValueChange: ((column: Int, value: Int, values: Array<Int>) -> Unit)? = null,
    onValuesChange: (Array<Int>) -> Unit
) {
    val localValues = remember(visible) { values.copyOf() }
    MaskedPopup(
        visible = visible,
        onDismissRequest = {
            onCancel()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceContainer,
                    shape = MaterialTheme.shapes.extraLarge.copy(
                        bottomStart = CornerSize(0.0.dp),
                        bottomEnd = CornerSize(0.0.dp)
                    )
                )
                .padding(
                    vertical = AppTheme.dimens.paddingNormal,
                    horizontal = AppTheme.dimens.paddingNormal
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            title?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = AppTheme.dimens.paddingNormal
                        ),
                    contentAlignment = Alignment.TopStart
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .height(280.dp)
                    .drawIndicator(
                        MaterialTheme.colorScheme.surfaceContainerLowest.copy(0.5f)
                    )
            ) {
                // 可选列表
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ranges.forEachIndexed { index, options ->
                        ColumnItem(
                            options = options,
                            index = localValues[index]
                        ) {
                            localValues[index] = it
                            onColumnValueChange?.invoke(index, it, localValues.copyOf())
                        }
                    }
                }
                // 遮罩层
                Mask()
            }
            Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
            // 操作栏
            ActionBar(onCancel) {
                onValuesChange(localValues)
                onCancel()
            }
            Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
        }
    }
}

@Composable
fun RowScope.ColumnItem(
    options: List<String>,
    index: Int,
    onChange: (Int) -> Unit
) {
    val itemHeight = 56.dp
    val verticalPadding = remember { (280.dp - itemHeight) / 2 }
    val listState = rememberLazyListState(index)
    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect {
                onChange(it)
                hapticFeedback.performHapticFeedback(
                    hapticFeedbackType = HapticFeedbackType.LongPress,
                )
            }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(vertical = verticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        flingBehavior = rememberSnapFlingBehavior(listState)
    ) {
        items(options) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
fun Mask() {
    @Composable
    fun ColumnScope.MaskItem(verticalGradientColors: List<Color>) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    Brush.verticalGradient(
                        verticalGradientColors
                    )
                )
        )
    }

    Column {
        MaskItem(
            verticalGradientColors = listOf(
                MaterialTheme.colorScheme.surfaceContainer.copy(0.9f),
                MaterialTheme.colorScheme.surfaceContainer.copy(0.6f)
            )
        )
        Box(modifier = Modifier.height(56.dp))
        MaskItem(
            verticalGradientColors = listOf(
                MaterialTheme.colorScheme.surfaceContainer.copy(0.6f),
                MaterialTheme.colorScheme.surfaceContainer.copy(0.9f)
            )
        )
    }
}

@Composable
fun Modifier.drawIndicator(color: Color) = this.drawBehind {
    drawRoundRect(
        color,
        topLeft = Offset(0f, size.height / 2 - 56.dp.toPx() / 2),
        size = Size(size.width, 56.dp.toPx()),
        cornerRadius = CornerRadius(6.dp.toPx())
    )
}

@Composable
fun ActionBar(
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        CancelButton() {
            onCancel()
        }
        Spacer(modifier = Modifier.width(20.dp))
        ConfirmButton() {
            onConfirm()
        }
    }
}