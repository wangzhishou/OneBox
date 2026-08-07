package com.wanbaohe.altitude.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanbaohe.altitude.domain.AltitudeRecord
import com.wanbaohe.altitude.domain.AltitudeUnit
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.resources.icons.ArrowUpward
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowDownward
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGpsFixed

private val dateFormatter = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

/**
 * 历史记录列表节（注入 LazyListScope，共享父 LazyColumn，避免嵌套滚动）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
fun LazyListScope.altitudeHistoryItems(
    history: List<AltitudeRecord>,
    unit: AltitudeUnit,
    onDelete: (Long) -> Unit,
    onClick: (AltitudeRecord) -> Unit = {}
) {
    if (history.isEmpty()) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineGpsFixed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(CoreR.string.altitude_no_history),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }
        return
    }

    itemsIndexed(
        items = history,
        key = { _, record -> record.id }
    ) { index, record ->
        // 与相邻记录计算差值（列表已按时间倒序）
        val prevRecord = history.getOrNull(index + 1)
        val deltaMeters = prevRecord?.let { record.altitudeMeters - it.altitudeMeters }

        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                if (value == SwipeToDismissBoxValue.EndToStart) { onDelete(record.id); true }
                else false
            }
        )

        val bgColor by animateColorAsState(
            targetValue = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                MaterialTheme.colorScheme.errorContainer
            else Color.Transparent,
            animationSpec = tween(200),
            label = "dismiss_bg"
        )

        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(bgColor)
                        .padding(end = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {}
            },
            enableDismissFromStartToEnd = false
        ) {
            AltitudeHistoryCard(
                record = record,
                unit = unit,
                deltaMeters = deltaMeters,
                onClick = { onClick(record) }
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun AltitudeHistoryCard(
    record: AltitudeRecord,
    unit: AltitudeUnit,
    deltaMeters: Float?,
    onClick: () -> Unit = {}
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassBackground(
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── 左侧高度徽章 ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = record.formattedAltitude(unit),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1
            )
        }

        Spacer(Modifier.width(12.dp))

        // ── 中间：单位 + 备注 ─────────────────────────────────────────
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.resolvedTitle,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${record.formattedAltitude(unit)} ${unit.suffix}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                // delta 徽章
                deltaMeters?.let { delta ->
                    Spacer(Modifier.width(6.dp))
                    DeltaBadge(delta = delta, unit = unit)
                }
            }

            record.weatherSnapshot?.brief
                ?.takeIf { it.isNotBlank() }
                ?.let { weatherBrief ->
                    Text(
                        text = weatherBrief,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

            if (record.note.isNotBlank() && record.note != record.resolvedTitle) {
                Text(
                    text = record.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // ── 右侧时间 ──────────────────────────────────────────────────
        Text(
            text = dateFormatter.format(Date(record.recordedAt)),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

/** 相对前一条的高度变化徽章 */
@Composable
private fun DeltaBadge(delta: Float, unit: AltitudeUnit) {
    val absDelta = kotlin.math.abs(delta)
    if (absDelta < 0.5f) return
    val risingColor = MaterialTheme.colorScheme.tertiary
    val fallingColor = MaterialTheme.colorScheme.error
    val (icon, color) = if (delta > 0f)
        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.ArrowUpward to risingColor
    else
        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowDownward to fallingColor
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 5.dp, vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(10.dp))
        Text(
            text = "${"%.1f".format(kotlin.math.abs(unit.fromMeters(delta)))}${unit.suffix}",
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}
