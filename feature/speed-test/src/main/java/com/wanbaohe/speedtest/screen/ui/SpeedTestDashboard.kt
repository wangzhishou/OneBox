package com.wanbaohe.speedtest.screen.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSpeedTest
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWifi
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassThin
import com.wanbaohe.speedtest.R
import com.wanbaohe.speedtest.component.SpeedTestStatus
import com.wanbaohe.speedtest.component.SpeedTestUiState
import java.text.DateFormat
import java.util.Date

/** Keep the gauge and metrics mounted; only the controls and gauge centre change state. */
@Composable
internal fun SpeedTestDashboard(
    uiState: SpeedTestUiState,
    onStart: () -> Unit,
    onCancel: () -> Unit,
    onOpenWifi: () -> Unit,
    onShowHistory: () -> Unit
) {
    val primary = AppTheme.colors.getPrimaryColor()
    val subtitle = AppTheme.colors.getOnInactiveContainerColor()
    val config = if (uiState.status == SpeedTestStatus.IDLE) uiState.config else uiState.testConfig
    // Historical records do not contain a server configuration snapshot.
    val historyRecord = uiState.result?.takeIf {
        uiState.status == SpeedTestStatus.DONE && uiState.testConfig == null
    }
    val sourceLabel = config?.name ?: historyRecord?.let {
        val time = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
            .format(Date(it.recordedAt))
        "${stringResource(R.string.speed_test_history_title)} · $time"
    }
    val latency = uiState.latencyMs.takeIf { it >= 0 }?.toString() ?: "--"
    val download = when {
        uiState.status == SpeedTestStatus.IDLE || uiState.measuringLatency -> "--"
        else -> "%.1f".format(uiState.liveMbps)
    }
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val minimumHeight = maxHeight
        Column(
            Modifier.widthIn(max = 560.dp).fillMaxWidth()
                .verticalScroll(rememberScrollState()).heightIn(min = minimumHeight)
                .padding(top = 8.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            SpeedMetricsRow(latencyDisplay = latency, downloadDisplay = download)
            Box(
                Modifier.fillMaxWidth().padding(vertical = 8.dp).heightIn(min = 330.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.matchParentSize()) {
                    val spacing = 24.dp.toPx()
                    for (x in 0..(size.width / spacing).toInt()) {
                        for (y in 0..(size.height / spacing).toInt()) {
                            drawCircle(primary.copy(alpha = 0.055f), 1.dp.toPx(), Offset(x * spacing, y * spacing))
                        }
                    }
                    listOf(160.dp, 190.dp, 220.dp).forEachIndexed { index, radius ->
                        drawCircle(
                            primary.copy(alpha = 0.08f - index * 0.02f), radius.toPx(),
                            style = Stroke(1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 6.dp.toPx())))
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SpeedGauge(
                        status = uiState.status,
                        liveMbps = uiState.liveMbps,
                        progress = uiState.progress,
                        estimatedMb = config?.estimatedDataMb ?: 0,
                        measuringLatency = uiState.measuringLatency,
                        onClick = onStart
                    )
                    if (sourceLabel != null) {
                        Row(
                            Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                                .glassThin(color = AppTheme.colors.getContainerSurfaceColor(), shape = CircleShape, borderWidth = 0.dp)
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                if (historyRecord != null) Icons.Outlined.LineHistory else Icons.Outlined.LineSpeedTest,
                                null, Modifier.size(14.dp), tint = primary
                            )
                            Text(sourceLabel, fontSize = 12.sp, color = subtitle, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
            AnimatedContent(
                targetState = uiState.status,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
                label = "speed_controls",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            ) { status ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (status) {
                        SpeedTestStatus.IDLE -> {
                            NetworkCard(uiState.networkType)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                FilledTonalButton(onOpenWifi, Modifier.weight(1f).heightIn(min = 48.dp)) {
                                    Icon(Icons.Outlined.LineWifi, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(stringResource(R.string.speed_test_switch_wifi), textAlign = TextAlign.Center)
                                }
                                FilledTonalButton(onShowHistory, Modifier.weight(1f).heightIn(min = 48.dp)) {
                                    Icon(Icons.Outlined.LineHistory, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(stringResource(R.string.speed_test_history_link), textAlign = TextAlign.Center)
                                }
                            }
                            Text(stringResource(R.string.speed_test_disclaimer), fontSize = 11.sp, color = subtitle, textAlign = TextAlign.Center)
                        }
                        SpeedTestStatus.MEASURING -> {
                            FilledIconButton(
                                onClick = onCancel,
                                modifier = Modifier.size(64.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            ) {
                                Icon(Icons.Rounded.Close, stringResource(R.string.speed_test_stop), Modifier.size(26.dp))
                            }
                            Text(stringResource(R.string.speed_test_stop_hint), fontSize = 12.sp, color = subtitle)
                            HistoryButton(onShowHistory)
                        }
                        SpeedTestStatus.DONE -> {
                            SpeedReferenceBar(uiState.result?.downloadMbps ?: uiState.liveMbps)
                            NetworkCard(uiState.result?.networkType ?: uiState.networkType, isResult = true)
                            Button(
                                onClick = onStart,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        ),
                                        CircleShape
                                    ),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(
                                    stringResource(R.string.speed_test_restart),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            HistoryButton(onShowHistory)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryButton(onClick: () -> Unit) {
    TextButton(onClick) { Text(stringResource(R.string.speed_test_history_link)) }
}

@Composable
private fun NetworkCard(networkType: String, isResult: Boolean = false) {
    Row(
        Modifier.fillMaxWidth()
            .glassThin(color = AppTheme.colors.getContainerSurfaceColor(), shape = RoundedCornerShape(14.dp), borderWidth = 0.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Outlined.LineWifi, null, Modifier.size(24.dp), tint = AppTheme.colors.getPrimaryColor())
        if (isResult) {
            Text(
                stringResource(R.string.speed_test_network_type_label),
                modifier = Modifier.weight(1f),
                fontSize = 13.sp,
                color = AppTheme.colors.getOnInactiveContainerColor()
            )
        }
        Text(
            if (isResult) networkType else stringResource(R.string.speed_test_current_network, networkType),
            fontSize = 13.sp,
            color = AppTheme.colors.getPrimaryTextColor()
        )
    }
}

@Composable
private fun SpeedReferenceBar(mbps: Float) {
    val primary = AppTheme.colors.getPrimaryColor()
    val secondary = MaterialTheme.colorScheme.tertiary
    val progress = remember { Animatable(0f) }
    LaunchedEffect(mbps) {
        progress.animateTo((mbps / 200f).coerceIn(0f, 1f), tween(900))
    }
    Column(
        Modifier.fillMaxWidth()
            .glassThin(color = AppTheme.colors.getContainerSurfaceColor(), shape = RoundedCornerShape(16.dp), borderWidth = 0.dp)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(R.string.speed_test_reference_scale), fontSize = 12.sp, color = AppTheme.colors.getOnInactiveContainerColor())
        Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(primary.copy(alpha = 0.08f))) {
            Box(Modifier.fillMaxWidth(progress.value).fillMaxHeight().clip(CircleShape).background(Brush.horizontalGradient(listOf(primary, secondary))))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("0", fontSize = 10.sp, color = AppTheme.colors.getOnInactiveContainerColor())
            Text("200+ Mbps", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = primary)
        }
    }
}
