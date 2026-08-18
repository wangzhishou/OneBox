package com.wanbaohe.compass.ui.luopan

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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.wanbaohe.compass.component.CompassUiState
import java.util.Locale
import com.shifenmiao.core.R as CoreR

/** 宽屏布局时右侧信息面板的宽度 */
private val INFO_PANEL_WIDTH = 220.dp

/**
 * 罗经盘页面：LuopanDial 表盘 + 信息栏（方位/坐向/磁偏角/校准状态）
 *
 * 自适应布局：
 *   - 竖屏（宽 ≤ 高）：表盘权重撑满（吃满宽度），信息栏在底部横排四格
 *   - 宽屏（宽 > 高，横屏/折叠屏展开）：表盘吃满高度，信息栏挪到右侧竖排，
 *     避免圆形表盘两侧大面积留白
 *
 * 表盘消费高频 [heading]（只重绘），信息栏消费低频 [uiState]（取整度数变化才重组）。
 */
@Composable
fun LuopanPage(
    heading: State<Float>,
    uiState: CompassUiState,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        if (maxWidth > maxHeight) {
            // ── 宽屏：表盘居左吃满高度，信息栏右侧竖排 ──────────────────
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
                    LuopanDial(
                        heading = heading,
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxHeight()
                            .aspectRatio(1f)
                    )
                }
                LuopanInfoColumn(
                    uiState = uiState,
                    modifier = Modifier.width(INFO_PANEL_WIDTH)
                )
            }
        } else {
            // ── 竖屏：表盘吃满宽度，信息栏底部横排 ──────────────────────
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
                    LuopanDial(
                        heading = heading,
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxSize()
                            .aspectRatio(1f)
                    )
                }

                LuopanInfoBar(
                    uiState = uiState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ─── 信息栏 ───────────────────────────────────────────────────────────────────

private class InfoItem(
    val label: String,
    val value: String,
    val valueColor: Color
)

/** 由 uiState 组装四格信息（方位/坐向/磁偏角/校准状态），横竖两种布局共用 */
@Composable
private fun buildInfoItems(uiState: CompassUiState): List<InfoItem> {
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

    val mountainIdx = mountainIndex(uiState.degrees)
    val directionValue = buildString {
        append(MOUNTAINS[mountainIdx])
        append('(')
        append(directionNames[mountainDirectionIndex(mountainIdx)])
        append(") ")
        append(uiState.degrees)
        append('°')
    }
    val (sitting, facing) = seatFacing(uiState.degrees)
    val declinationValue = uiState.declination
        ?.let { String.format(Locale.US, "%.1f°", it) }
        ?: "--"

    val normalColor = MaterialTheme.colorScheme.onSurface
    val calibrationColor = if (uiState.needsCalibration) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.primary

    return listOf(
        InfoItem(stringResource(CoreR.string.compass_info_direction), directionValue, normalColor),
        InfoItem(
            stringResource(CoreR.string.compass_info_seat),
            "${sitting}山${facing}向",
            normalColor
        ),
        InfoItem(
            stringResource(CoreR.string.compass_info_declination),
            declinationValue,
            normalColor
        ),
        InfoItem(
            stringResource(CoreR.string.compass_info_calibration),
            stringResource(
                if (uiState.needsCalibration) CoreR.string.compass_not_calibrated
                else CoreR.string.compass_calibrated
            ),
            calibrationColor
        )
    )
}

/** 竖屏底部信息栏：一卡四等分横排 + 竖分割线（玻璃质感） */
@Composable
private fun LuopanInfoBar(uiState: CompassUiState, modifier: Modifier = Modifier) {
    val items = buildInfoItems(uiState)
    GlassCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                if (index > 0) {
                    VerticalDivider(
                        modifier = Modifier.height(32.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                InfoCell(
                    label = item.label,
                    value = item.value,
                    valueColor = item.valueColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** 宽屏右侧信息栏：一卡四行竖排 + 横分割线（玻璃质感） */
@Composable
private fun LuopanInfoColumn(uiState: CompassUiState, modifier: Modifier = Modifier) {
    val items = buildInfoItems(uiState)
    GlassCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                InfoCell(
                    label = item.label,
                    value = item.value,
                    valueColor = item.valueColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun InfoCell(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontFeatureSettings = "tnum"   // 等宽数字，读数变化时字形不跳动
            ),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = valueColor
        )
    }
}
