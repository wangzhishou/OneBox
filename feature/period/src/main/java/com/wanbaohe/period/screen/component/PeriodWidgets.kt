package com.wanbaohe.period.screen.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCheckCircleOutline
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCircle
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePeriodAcne
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePeriodAppetite
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePeriodBackPain
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePeriodBreastTender
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePeriodCramps
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePeriodEdema
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePeriodFatigue
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePeriodHeadache
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePeriodInsomnia
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePeriodMoodSwings
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePeriodNausea
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWaterDrop
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.period.R
import com.wanbaohe.period.model.PeriodColors
import com.wanbaohe.period.model.PeriodFlow
import com.wanbaohe.period.model.PeriodMood
import com.wanbaohe.period.model.PeriodStatus
import com.wanbaohe.period.model.PeriodSymptom
import com.wanbaohe.period.model.periodColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

// ── 枚举 → 文案 / 图标 ─────────────────────────────

@Composable
fun symptomLabel(symptom: PeriodSymptom): String = when (symptom) {
    PeriodSymptom.ABDOMINAL_PAIN -> stringResource(R.string.period_symptom_abdominal_pain)
    PeriodSymptom.HEADACHE -> stringResource(R.string.period_symptom_headache)
    PeriodSymptom.BACK_PAIN -> stringResource(R.string.period_symptom_back_pain)
    PeriodSymptom.MOOD_SWINGS -> stringResource(R.string.period_symptom_mood_swings)
    PeriodSymptom.BREAST_TENDERNESS -> stringResource(R.string.period_symptom_breast_tenderness)
    PeriodSymptom.FATIGUE -> stringResource(R.string.period_symptom_fatigue)
    PeriodSymptom.NAUSEA -> stringResource(R.string.period_symptom_nausea)
    PeriodSymptom.INSOMNIA -> stringResource(R.string.period_symptom_insomnia)
    PeriodSymptom.APPETITE_CHANGE -> stringResource(R.string.period_symptom_appetite_change)
    PeriodSymptom.ACNE -> stringResource(R.string.period_symptom_acne)
    PeriodSymptom.EDEMA -> stringResource(R.string.period_symptom_edema)
}

@Composable
fun moodLabel(mood: PeriodMood): String = when (mood) {
    PeriodMood.VERY_HAPPY -> stringResource(R.string.period_mood_very_happy)
    PeriodMood.HAPPY -> stringResource(R.string.period_mood_happy)
    PeriodMood.NORMAL -> stringResource(R.string.period_mood_normal)
    PeriodMood.SAD -> stringResource(R.string.period_mood_sad)
    PeriodMood.VERY_SAD -> stringResource(R.string.period_mood_very_sad)
}

@Composable
fun flowLabel(flow: PeriodFlow): String = when (flow) {
    PeriodFlow.NONE -> stringResource(R.string.period_flow_none)
    PeriodFlow.LIGHT -> stringResource(R.string.period_flow_light)
    PeriodFlow.MEDIUM -> stringResource(R.string.period_flow_medium)
    PeriodFlow.HEAVY -> stringResource(R.string.period_flow_heavy)
}

fun symptomIcon(symptom: PeriodSymptom): ImageVector = when (symptom) {
    PeriodSymptom.ABDOMINAL_PAIN -> Icons.Outlined.LinePeriodCramps
    PeriodSymptom.HEADACHE -> Icons.Outlined.LinePeriodHeadache
    PeriodSymptom.BACK_PAIN -> Icons.Outlined.LinePeriodBackPain
    PeriodSymptom.MOOD_SWINGS -> Icons.Outlined.LinePeriodMoodSwings
    PeriodSymptom.BREAST_TENDERNESS -> Icons.Outlined.LinePeriodBreastTender
    PeriodSymptom.FATIGUE -> Icons.Outlined.LinePeriodFatigue
    PeriodSymptom.NAUSEA -> Icons.Outlined.LinePeriodNausea
    PeriodSymptom.INSOMNIA -> Icons.Outlined.LinePeriodInsomnia
    PeriodSymptom.APPETITE_CHANGE -> Icons.Outlined.LinePeriodAppetite
    PeriodSymptom.ACNE -> Icons.Outlined.LinePeriodAcne
    PeriodSymptom.EDEMA -> Icons.Outlined.LinePeriodEdema
}

// ── 日期格式化 ─────────────────────────────────────

fun LocalDate.periodFullDate(): String =
    format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

@Composable
fun LocalDate.periodShortDate(): String =
    format(DateTimeFormatter.ofPattern(stringResource(R.string.period_short_date_pattern)))

fun LocalDate.periodWeekday(style: TextStyle = TextStyle.SHORT): String =
    dayOfWeek.getDisplayName(style, Locale.getDefault())

@Composable
fun YearMonth.periodMonthTitle(): String =
    format(DateTimeFormatter.ofPattern(stringResource(R.string.period_month_group_pattern)))

// ── 通用小部件 ─────────────────────────────────────

/** 表单区块：玻璃卡片 + 区块标题 */
@Composable
fun PeriodFormSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = periodColors().onSurface,
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

/** 记录状态胶囊：经期中 / 排卵期 / 正常 */
@Composable
fun PeriodStatusBadge(status: PeriodStatus, colors: PeriodColors) {
    val label = when (status) {
        PeriodStatus.PERIOD -> stringResource(R.string.period_status_period)
        PeriodStatus.OVULATION -> stringResource(R.string.period_status_ovulation)
        PeriodStatus.NORMAL -> stringResource(R.string.period_status_normal)
    }
    val isPeriod = status == PeriodStatus.PERIOD
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = if (isPeriod) colors.accent else colors.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isPeriod) colors.accentSoft else colors.accentSoft.copy(alpha = 0.5f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/** 流量水滴指示：5 颗水滴按强度点亮 */
@Composable
fun PeriodFlowDrops(
    level: PeriodFlow,
    colors: PeriodColors,
    modifier: Modifier = Modifier,
) {
    val filled = when (level) {
        PeriodFlow.NONE -> 0
        PeriodFlow.LIGHT -> 2
        PeriodFlow.MEDIUM -> 3
        PeriodFlow.HEAVY -> 5
    }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { index ->
            Icon(
                imageVector = Icons.Outlined.LineWaterDrop,
                contentDescription = null,
                tint = if (index < filled) colors.accent else colors.onSurfaceVariant.copy(alpha = 0.25f),
                modifier = Modifier.size(16.dp),
            )
            if (index < 4) Spacer(modifier = Modifier.width(2.dp))
        }
    }
}

/** 心情脸：Canvas 自绘线条表情（圆圈 + 眼睛 + 嘴），贴合设计稿风格 */
@Composable
fun PeriodMoodFace(
    mood: PeriodMood,
    color: Color,
    modifier: Modifier = Modifier.size(30.dp),
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.8.dp.toPx()
        val radius = size.minDimension / 2f - strokeWidth
        val faceCenter = center
        drawCircle(
            color = color,
            radius = radius,
            center = faceCenter,
            style = Stroke(width = strokeWidth),
        )
        val eyeY = faceCenter.y - radius * 0.2f
        val eyeDx = radius * 0.38f
        val eyeRadius = strokeWidth * 0.85f
        if (mood == PeriodMood.VERY_HAPPY) {
            listOf(-1f, 1f).forEach { side ->
                drawArc(
                    color = color,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(faceCenter.x + side * eyeDx - eyeRadius * 1.6f, eyeY - eyeRadius * 1.4f),
                    size = Size(eyeRadius * 3.2f, eyeRadius * 2.8f),
                    style = Stroke(width = strokeWidth * 0.8f, cap = StrokeCap.Round),
                )
            }
        } else {
            drawCircle(color, eyeRadius, Offset(faceCenter.x - eyeDx, eyeY))
            drawCircle(color, eyeRadius, Offset(faceCenter.x + eyeDx, eyeY))
        }
        val mouthWidth = radius * 0.95f
        val mouthStyle = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        when (mood) {
            PeriodMood.VERY_HAPPY, PeriodMood.HAPPY -> drawArc(
                color = color,
                startAngle = 15f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(faceCenter.x - mouthWidth / 2f, faceCenter.y + radius * 0.02f),
                size = Size(mouthWidth, radius * 0.68f),
                style = mouthStyle,
            )

            PeriodMood.NORMAL -> drawLine(
                color = color,
                start = Offset(faceCenter.x - mouthWidth * 0.38f, faceCenter.y + radius * 0.42f),
                end = Offset(faceCenter.x + mouthWidth * 0.38f, faceCenter.y + radius * 0.42f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )

            PeriodMood.SAD, PeriodMood.VERY_SAD -> drawArc(
                color = color,
                startAngle = 195f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(faceCenter.x - mouthWidth / 2f, faceCenter.y + radius * 0.4f),
                size = Size(mouthWidth, radius * 0.68f),
                style = mouthStyle,
            )
        }
    }
}

/** 症状选择 chip：选中红底带对勾，未选中浅底带空心圆 */
@Composable
fun PeriodSymptomChip(
    symptom: PeriodSymptom,
    selected: Boolean,
    onClick: () -> Unit,
    colors: PeriodColors,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) colors.accent else colors.accentSoft)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
    ) {
        Text(
            text = symptomLabel(symptom),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colors.onAccent else colors.onSurface,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = if (selected) Icons.Outlined.LineCheckCircleOutline else Icons.Outlined.LineCircle,
            contentDescription = null,
            tint = if (selected) colors.onAccent else colors.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
    }
}

/** 统计卡片：图标 + 标题 + 大数值 + 单位 + 副标题 */
@Composable
fun PeriodStatCard(
    icon: ImageVector,
    title: String,
    value: String,
    unit: String,
    subtitle: String,
    colors: PeriodColors,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent,
                )
                if (unit.isNotEmpty()) {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp, start = 2.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

/** 信息行图标：浅红圆底 + 线性图标（详情页各行左侧） */
@Composable
fun PeriodInfoIcon(
    icon: ImageVector,
    colors: PeriodColors,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = colors.accent,
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(50))
            .background(colors.accentSoft)
            .padding(8.dp),
    )
}
