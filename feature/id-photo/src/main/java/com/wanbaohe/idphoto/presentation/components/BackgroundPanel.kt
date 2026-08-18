package com.wanbaohe.idphoto.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.wanbaohe.idphoto.R
import com.wanbaohe.idphoto.domain.IdPhotoBackground
import com.wanbaohe.idphoto.util.localizedBackgroundName

/**
 * 背景选择面板(「背景」tab,位于「一键美化」之前):
 * 预置项横向滚动:原图 / 透明 / 各纯色;「透明」与真实颜色都会触发 AI 抠图
 * (积分预检由调用方处理),选中项以对象相等判定(透明与原图同为透明色值,不能按颜色比较)。
 */
@Composable
fun BackgroundPanel(
    currentBackground: IdPhotoBackground,
    onBackgroundSelected: (IdPhotoBackground) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(IdPhotoBackground.PRESETS, key = { it.name }) { background ->
            BackgroundColorItem(
                background = background,
                isSelected = background == currentBackground,
                onClick = { onBackgroundSelected(background) }
            )
        }
    }
}

/**
 * 单个背景选项:「原图」画斜线表示不替换,「透明」画灰白棋盘格,纯色直接填色
 */
@Composable
private fun BackgroundColorItem(
    background: IdPhotoBackground,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    // 「原图」「透明」无实色,用中性色垫底便于辨识
                    if (background.isOriginal || background.isTransparent) {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    } else {
                        background.getColor()
                    },
                    CircleShape
                )
                .then(
                    if (isSelected) {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    } else {
                        Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                    }
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            when {
                background.isOriginal -> {
                    // 「原图」画一条斜线表示"不替换背景"
                    Canvas(modifier = Modifier.size(40.dp)) {
                        drawLine(
                            color = Color.Gray,
                            start = Offset(size.width * 0.22f, size.height * 0.78f),
                            end = Offset(size.width * 0.78f, size.height * 0.22f),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }

                background.isTransparent -> {
                    // 「透明」画灰白棋盘格
                    Canvas(modifier = Modifier.size(40.dp)) {
                        val cell = size.width / 6f
                        val gray = Color.LightGray
                        for (row in 0 until 6) {
                            for (col in 0 until 6) {
                                if ((row + col) % 2 == 0) {
                                    drawRect(
                                        color = gray,
                                        topLeft = Offset(col * cell, row * cell),
                                        size = androidx.compose.ui.geometry.Size(cell, cell)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                    contentDescription = stringResource(R.string.id_photo_selected),
                    tint = when {
                        background.isOriginal || background.isTransparent -> MaterialTheme.colorScheme.onSurfaceVariant
                        background.color == 0xFFFFFFFF -> Color.Black
                        else -> Color.White
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = localizedBackgroundName(background.name),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
