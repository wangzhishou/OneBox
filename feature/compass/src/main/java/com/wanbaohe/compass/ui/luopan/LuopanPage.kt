package com.wanbaohe.compass.ui.luopan

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.wanbaohe.compass.component.CompassUiState
import com.wanbaohe.compass.ui.DIAL_MAX_SIZE
import java.util.Locale
import com.shifenmiao.core.R as CoreR

/**
 * 罗经盘页面：LuopanDial 表盘（权重撑满）+ 底部四格信息栏（方位/坐向/磁偏角/校准状态）
 *
 * 表盘消费高频 [heading]（只重绘），信息栏消费低频 [uiState]（取整度数变化才重组）。
 */
@Composable
fun LuopanPage(
    heading: State<Float>,
    uiState: CompassUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
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
                    .padding(20.dp)
                    .sizeIn(maxWidth = DIAL_MAX_SIZE, maxHeight = DIAL_MAX_SIZE)
                    .fillMaxSize()
                    .aspectRatio(1f)
            )
        }

        LuopanInfoBar(uiState = uiState, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ─── 底部信息栏 ───────────────────────────────────────────────────────────────

/** 四格信息栏：方位 / 坐向 / 磁偏角 / 校准状态（按原型稿，一卡四等分 + 竖分割线） */
@Composable
private fun LuopanInfoBar(uiState: CompassUiState, modifier: Modifier = Modifier) {
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

    Card(
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
            InfoCell(
                label = stringResource(CoreR.string.compass_info_direction),
                value = directionValue,
                modifier = Modifier.weight(1f)
            )
            CellDivider()
            InfoCell(
                label = stringResource(CoreR.string.compass_info_seat),
                value = "${sitting}山${facing}向",
                modifier = Modifier.weight(1f)
            )
            CellDivider()
            InfoCell(
                label = stringResource(CoreR.string.compass_info_declination),
                value = declinationValue,
                modifier = Modifier.weight(1f)
            )
            CellDivider()
            InfoCell(
                label = stringResource(CoreR.string.compass_info_calibration),
                value = stringResource(
                    if (uiState.needsCalibration) CoreR.string.compass_not_calibrated
                    else CoreR.string.compass_calibrated
                ),
                valueColor = if (uiState.needsCalibration) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun InfoCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
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

@Composable
private fun CellDivider() {
    VerticalDivider(
        modifier = Modifier.height(32.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}
