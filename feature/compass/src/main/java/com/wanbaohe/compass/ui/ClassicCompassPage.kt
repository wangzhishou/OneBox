package com.wanbaohe.compass.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wanbaohe.compass.component.CompassUiState
import com.shifenmiao.core.R as CoreR

/**
 * 经典指南针页面：CompassDial 表盘 + 底部朝向读数
 *
 * 自适应布局：竖屏（宽 ≤ 高）表盘吃满宽度、读数在底部；
 * 宽屏（宽 > 高）表盘吃满高度居左、读数右侧居中，避免圆形表盘两侧大面积留白。
 *
 * 表盘消费高频 [heading]（只重绘），读数消费低频 [uiState]（取整度数变化才重组）。
 */
@Composable
fun ClassicCompassPage(
    heading: State<Float>,
    uiState: CompassUiState,
    modifier: Modifier = Modifier
) {
    // 指针配色跟随主题：北针用 error 呼应罗经盘，南针用 outlineVariant
    val northColor = MaterialTheme.colorScheme.error
    val southColor = MaterialTheme.colorScheme.outlineVariant

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        if (maxWidth > maxHeight) {
            // ── 宽屏：表盘居左吃满高度，读数右侧居中 ──────────────────
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    CompassDial(
                        heading = heading,
                        northColor = northColor,
                        southColor = southColor,
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxHeight()
                            .aspectRatio(1f)
                    )
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    HeadingReadout(
                        degrees = uiState.degrees,
                        directionIndex = uiState.directionIndex
                    )
                }
            }
        } else {
            // ── 竖屏：表盘吃满宽度，读数底部 ──────────────────────────
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CompassDial(
                        heading = heading,
                        northColor = northColor,
                        southColor = southColor,
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxSize()
                            .aspectRatio(1f)
                    )
                }

                HeadingReadout(
                    degrees = uiState.degrees,
                    directionIndex = uiState.directionIndex
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * 底部朝向读数：大号整数度数（等宽数字，避免位数变化抖动）+ 本地化方位
 */
@Composable
private fun HeadingReadout(
    degrees: Int,
    directionIndex: Int,
    modifier: Modifier = Modifier
) {
    val directionNames = listOf(
        stringResource(CoreR.string.compass_dir_n),
        stringResource(CoreR.string.compass_dir_ne),
        stringResource(CoreR.string.compass_dir_e),
        stringResource(CoreR.string.compass_dir_se),
        stringResource(CoreR.string.compass_dir_s),
        stringResource(CoreR.string.compass_dir_sw),
        stringResource(CoreR.string.compass_dir_w),
        stringResource(CoreR.string.compass_dir_nw)
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$degrees°",
            style = MaterialTheme.typography.displayMedium.copy(
                fontFeatureSettings = "tnum"   // 等宽数字，读数变化时字形不跳动
            ),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = directionNames[directionIndex.coerceIn(directionNames.indices)],
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
