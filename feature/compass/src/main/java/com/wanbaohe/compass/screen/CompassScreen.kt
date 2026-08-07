package com.wanbaohe.compass.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.wanbaohe.compass.component.CompassComponent
import com.wanbaohe.compass.ui.CompassDial
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWarning
import com.t8rin.imagetoolbox.core.resources.icons.Compass

/** 表盘最大直径：在大屏（平板/折叠屏）上避免表盘过大失真 */
private val DIAL_MAX_SIZE = 340.dp

/**
 * 指南针主屏幕
 *
 * 布局（由上至下）：
 *   - 校准警告条（仅在精度不可靠时显示）
 *   - CompassDial 表盘（权重撑满剩余空间，自适应方形）
 *   - 底部朝向读数（大号整数度数 + 本地化方位，与表盘上下对称）
 *   - 传感器不可用时，整体替换为提示卡
 *
 * 性能要点：高频平滑角度经 [CompassComponent.heading] 直达表盘绘制层，
 * 本屏幕只消费取整后的低频 [CompassComponent.uiState]，
 * 静止时传感器抖动不会引发重组。
 */
@Composable
fun CompassScreen(component: CompassComponent) {
    val state by component.uiState.collectAsState()
    val heading = component.heading.collectAsState()

    BaseScreen(
        title = stringResource(CoreR.string.compass),
        onGoBack = component.onGoBack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── 校准提示横幅 ──────────────────────────────────────────
            AnimatedVisibility(
                visible = state.needsCalibration,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    CalibrationBanner(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (!state.isSensorAvailable) {
                // ── 传感器不可用提示 ──────────────────────────────────
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    SensorUnavailableCard(modifier = Modifier.fillMaxWidth())
                }
            } else {
                // ── 表盘（权重撑满剩余空间，方形自适应） ──────────────
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CompassDial(
                        heading = heading,
                        modifier = Modifier
                            .padding(20.dp)
                            .sizeIn(maxWidth = DIAL_MAX_SIZE, maxHeight = DIAL_MAX_SIZE)
                            .fillMaxSize()
                            .aspectRatio(1f)
                    )
                }

                // ── 底部朝向读数（与表盘上下对称） ────────────────────
                HeadingReadout(
                    degrees = state.degrees,
                    directionIndex = state.directionIndex
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ─── 子组件 ───────────────────────────────────────────────────────────────────

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

/**
 * 校准提示横幅：显示"传感器精度低，请画 8 字形校准"
 */
@Composable
private fun CalibrationBanner(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWarning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(CoreR.string.compass_calibrate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

/**
 * 传感器不可用提示卡
 */
@Composable
private fun SensorUnavailableCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Compass,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(CoreR.string.compass_unavailable),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
