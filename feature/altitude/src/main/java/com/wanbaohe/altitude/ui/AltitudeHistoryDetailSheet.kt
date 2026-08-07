package com.wanbaohe.altitude.ui

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.ui.utils.capturable.capturable
import com.t8rin.imagetoolbox.core.ui.utils.capturable.rememberCaptureController
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.altitude.component.AltitudeUiState
import com.wanbaohe.altitude.domain.AltitudeRecord
import com.wanbaohe.altitude.domain.AltitudeUnit
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare
import com.t8rin.imagetoolbox.core.resources.icons.line.LineNote

private val detailDateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

/**
 * 海拔历史记录详情底部弹窗 — 复用仪表盘卡片组件
 */
@Composable
fun AltitudeHistoryDetailSheet(
    record: AltitudeRecord?,
    unit: AltitudeUnit,
    onDismiss: () -> Unit,
    onShareBitmap: (Bitmap) -> Unit = {}
) {
    EnhancedModalBottomSheet(
        visible = record != null,
        onDismiss = { onDismiss() },
        dragHandle = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .align(Alignment.CenterHorizontally)
                        .size(width = 32.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
                Text(
                    text = stringResource(CoreR.string.altitude_detail_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            }
        },
        sheetContent = {
            record ?: return@EnhancedModalBottomSheet

            val captureController = rememberCaptureController()
            val scope = rememberCoroutineScope()

            // 从历史记录构造 AltitudeUiState，供 ElevationGaugeSection 复用
            val gaugeState = remember(record, unit) {
                AltitudeUiState(
                    currentAltitudeMeters = record.altitudeMeters,
                    accuracyMeters = record.accuracyMeters,
                    unit = unit,
                    altitudeSource = record.source,
                    isAcquiring = false
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── 可截图区域 ────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .capturable(captureController),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ── 1. 海拔仪表盘（复用 ElevationGaugeSection）──────
                    ElevationGaugeSection(state = gaugeState)

                    // ── 3. 位置信息卡片（复用 LocationInfoCard）─────────
                    record.citySnapshot?.let { snapshot ->
                        val cityInfo = snapshot.toCityInfo(
                            lat = record.latitude ?: 0.0,
                            lon = record.longitude ?: 0.0
                        )
                        LocationInfoCard(cityInfo = cityInfo)
                    }

                    // ── 4. 天气卡片（复用 WeatherCard + WeatherDetailsCard + SmallDetailCards）
                    record.weatherSnapshot?.let { snapshot ->
                        if (snapshot.temp.isNotBlank() || snapshot.text.isNotBlank()) {
                            val weatherInfo = snapshot.toWeatherInfo()
                            WeatherCard(weather = weatherInfo)
                            WeatherDetailsCard(weather = weatherInfo)
                            SmallDetailCards(weather = weatherInfo)

                            // ── 5. 观测时间（复用 ObservationTimeBar）────
                            ObservationTimeBar(weather = weatherInfo)
                        }
                    }

                    // ── 6. 备注 ──────────────────────────────────────────
                    if (record.note.isNotBlank()) {
                        DetailItem(
                            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineNote,
                            label = stringResource(CoreR.string.altitude_detail_note),
                            value = record.note
                        )
                    }
                }

                // ── 分享按钮（不参与截图）────────────────────────────────
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                onShareBitmap(captureController.bitmap())
                            }
                        },
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShare,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(CoreR.string.altitude_share),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    )
}

// ─── 私有子组件 ─────────────────────────────────────────────────────────

/** 单行详情条目（数据来源/精度/备注/时间等） */
@Composable
private fun DetailItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                letterSpacing = 0.3.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
