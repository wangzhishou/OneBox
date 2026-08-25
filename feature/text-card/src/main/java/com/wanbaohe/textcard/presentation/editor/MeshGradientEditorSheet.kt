package com.wanbaohe.textcard.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.button.ConfirmButton
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.ui.widget.color_picker.ColorPickerSheet
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.modifier.meshGradient
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.BackgroundSpec
import com.wanbaohe.textcard.domain.model.CanvasSpec
import com.wanbaohe.textcard.domain.model.GradientPresets
import com.wanbaohe.textcard.domain.model.MeshPoint
import com.wanbaohe.textcard.domain.render.MESH_RESOLUTION
import com.wanbaohe.textcard.domain.render.toPointPairs
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent
import kotlin.math.roundToInt

/**
 * Mesh 渐变编辑弹层(对标官方 MeshGradient Explorer):
 * 画布比例预览上叠加 3×3 控制点(带白边的彩色圆点,拖动改位置/点按改色),
 * 底部预设色板行(点预设 = 以它为起点继续调),「完成」一次性回填组件背景。
 * 控制点只在编辑界面显示,画布主页不显示。密度固定 3×3(2×2 patches)。
 *
 * [seed] 为进入时的初始网格:背景面板点任意预设色卡都会以该预设为初值进编辑器;
 * null 时取当前背景渐变,再兜底默认预设。
 */
@Composable
fun MeshGradientEditorSheet(
    visible: Boolean,
    component: TextCardComponent,
    seed: BackgroundSpec.Gradient? = null,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    // 编辑工作副本:seed > 当前背景渐变 > 默认预设
    var points by remember(seed) {
        mutableStateOf(
            seed?.points
                ?: (component.background as? BackgroundSpec.Gradient)?.points
                ?: GradientPresets.default.points
        )
    }
    var selectedPoint by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var showColorPicker by remember { mutableStateOf(false) }
    val canvas = component.canvas ?: CanvasSpec.Xiaohongshu

    EnhancedModalBottomSheet(
        visible = true,
        dragHandle = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                actions = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.textcard_mesh_edit_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            )
        },
        onDismiss = { onDismiss() },
        sheetContent = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = stringResource(R.string.textcard_mesh_edit_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                MeshEditCanvas(
                    points = points,
                    aspectRatio = canvas.aspectRatio,
                    selectedPoint = selectedPoint,
                    onPointSelect = { row, col ->
                        selectedPoint = row to col
                        showColorPicker = true
                    },
                    onPointMove = { row, col, dx, dy ->
                        points = points.mapIndexed { r, rowPoints ->
                            rowPoints.mapIndexed { c, point ->
                                if (r == row && c == col) {
                                    point.copy(
                                        offsetX = (point.offsetX + dx).coerceIn(0f, 1f),
                                        offsetY = (point.offsetY + dy).coerceIn(0f, 1f)
                                    )
                                } else point
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                // 预设色板行:点预设 = 以它为起点继续调
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    items(GradientPresets.all.size) { index ->
                        val preset = GradientPresets.all[index]
                        val isActive = points == preset.points
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .meshGradient(
                                    points = preset.toPointPairs(),
                                    resolutionX = MESH_RESOLUTION,
                                    resolutionY = MESH_RESOLUTION
                                )
                                .border(
                                    width = if (isActive) 2.dp else 1.dp,
                                    color = if (isActive) {
                                        MaterialTheme.colorScheme.primary
                                    } else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { points = preset.points }
                        )
                    }
                }
                // 操作 Bar 钉在最底部
                Row(modifier = Modifier.padding(bottom = 12.dp)) {
                    ConfirmButton(
                        onClick = {
                            component.updateBackground(BackgroundSpec.Gradient(points))
                            onDismiss()
                        }
                    )
                }
            }
        }
    )

    val editing = selectedPoint
    if (editing != null) {
        ColorPickerSheet(
            visible = showColorPicker,
            onDismiss = { showColorPicker = false },
            color = Color(points[editing.first][editing.second].argb),
            onColorSelected = { color ->
                points = points.mapIndexed { r, rowPoints ->
                    rowPoints.mapIndexed { c, point ->
                        if (r == editing.first && c == editing.second) {
                            point.copy(argb = color.toArgb().toLong() and 0xFFFF_FFFFL)
                        } else point
                    }
                }
                showColorPicker = false
            },
            allowAlpha = false
        )
    }
}

/** mesh 预览 + 控制点覆盖层:拖动改变归一化位置,点按进入取色 */
@Composable
private fun MeshEditCanvas(
    points: List<List<MeshPoint>>,
    aspectRatio: Float,
    selectedPoint: Pair<Int, Int>?,
    onPointSelect: (Int, Int) -> Unit,
    onPointMove: (Int, Int, Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .clip(RoundedCornerShape(16.dp))
            .meshGradient(
                points = BackgroundSpec.Gradient(points).toPointPairs(),
                resolutionX = MESH_RESOLUTION,
                resolutionY = MESH_RESOLUTION
            )
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val handleSize = 28.dp
        val handleRadiusPx = with(androidx.compose.ui.platform.LocalDensity.current) {
            (handleSize / 2).toPx()
        }

        points.forEachIndexed { row, rowPoints ->
            rowPoints.forEachIndexed { col, point ->
                val isSelected = selectedPoint == (row to col)
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (point.offsetX * widthPx - handleRadiusPx).roundToInt(),
                                y = (point.offsetY * heightPx - handleRadiusPx).roundToInt()
                            )
                        }
                        .size(handleSize)
                        .clip(CircleShape)
                        .background(Color(point.argb))
                        .border(
                            width = if (isSelected) 3.dp else 2.dp,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else Color.White,
                            shape = CircleShape
                        )
                        .pointerInput(row, col, widthPx, heightPx) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                onPointMove(
                                    row, col,
                                    dragAmount.x / widthPx,
                                    dragAmount.y / heightPx
                                )
                            }
                        }
                        .clickable { onPointSelect(row, col) }
                )
            }
        }
    }
}
