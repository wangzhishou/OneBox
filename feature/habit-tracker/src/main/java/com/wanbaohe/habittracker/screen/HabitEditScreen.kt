package com.wanbaohe.habittracker.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.icon.IconPickerSheet
import com.shifenmiao.base.ui.picker.ChineseTimePickerDialog
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAlarm
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBarChart
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.color_picker.ColorSelectionRow
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedChip
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.ui.widget.preferences.PreferenceItem
import com.t8rin.imagetoolbox.core.ui.widget.preferences.PreferenceRowSwitch
import com.t8rin.imagetoolbox.core.utils.getString
import com.wanbaohe.habittracker.R
import com.wanbaohe.habittracker.component.HabitEditComponent
import com.wanbaohe.habittracker.model.HabitIcons
import com.wanbaohe.habittracker.model.HabitRepeat
import com.wanbaohe.habittracker.model.habitPalette

// ─────────────────────────────────────────────────────────────────────────────
// 新增/编辑习惯页
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HabitEditScreen(component: HabitEditComponent) {
    val uiState by component.uiState.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }

    val nameRequiredMessage = getString(R.string.habit_name_required)
    val savedMessage = getString(R.string.habit_saved)

    val submit: () -> Unit = {
        val accepted = component.save {
            AppToastHost.showConfetti()
            AppToastHost.showToast(savedMessage)
            component.onGoBack()
        }
        if (!accepted) {
            AppToastHost.showToast(nameRequiredMessage)
        }
    }

    BaseScreen(
        title = {
            Text(
                text = stringResource(
                    if (uiState.isEditing) R.string.habit_edit_title
                    else R.string.habit_add_title
                )
            )
        },
        onGoBack = component.onGoBack,
        actions = {
            // 右上角对勾保存,名称为空时禁用
            IconButton(
                onClick = submit,
                enabled = uiState.name.isNotBlank() && !uiState.isSaving,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = stringResource(R.string.habit_save),
                )
            }
        },
        supportGlassEffect = true,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // 键盘弹起时缩小滚动视口,名称/备注输入框不被遮挡
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // ── 习惯名称(右侧圆形图标钮 + 下方"选择图标"小字) ──
                FormSection(title = stringResource(R.string.habit_edit_name_label)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GlassOutlinedTextField(
                            value = uiState.name,
                            onValueChange = { if (it.length <= 30) component.onNameChange(it) },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(stringResource(R.string.habit_edit_name_hint)) },
                            singleLine = true,
                        )
                        IconPickButton(
                            iconKey = uiState.iconKey,
                            onClick = { showIconPicker = true },
                        )
                    }
                }

                // ── 选择颜色(不选 = 自动主题色) ──────────────
                FormSection(
                    title = stringResource(R.string.habit_edit_color_label),
                    subtitle = stringResource(R.string.habit_edit_color_auto_hint),
                ) {
                    ColorSection(
                        selectedArgb = uiState.colorArgb,
                        onSelect = component::onColorSelect,
                    )
                }

                // ── 重复频率 ──────────────────────────────
                FormSection(title = stringResource(R.string.habit_edit_repeat_label)) {
                    RepeatSection(
                        repeatType = uiState.repeatType,
                        repeatTarget = uiState.repeatTarget,
                        weekdaysMask = uiState.weekdaysMask,
                        onRepeatTypeChange = component::onRepeatTypeChange,
                        onRepeatTargetChange = component::onRepeatTargetChange,
                        onWeekdayToggle = component::onWeekdayToggle,
                    )
                }

                // ── 提醒时间(可选) ─────────────────────────
                FormSection(title = stringResource(R.string.habit_edit_reminder_label)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PreferenceItem(
                            title = formatRemindMinutes(uiState.remindMinutes)
                                ?: getString(R.string.habit_edit_reminder_none),
                            subtitle = stringResource(R.string.habit_edit_reminder_subtitle),
                            startIcon = Icons.Outlined.LineAlarm,
                            onClick = { showTimePicker = true },
                        )
                        if (uiState.remindMinutes != null) {
                            EnhancedChip(
                                selected = false,
                                onClick = component::onRemindClear,
                                selectedColor = MaterialTheme.colorScheme.primary,
                            ) {
                                Text(stringResource(R.string.habit_edit_reminder_clear))
                            }
                        }
                    }
                }

                // ── 备注(可选) ────────────────────────────
                FormSection(title = stringResource(R.string.habit_edit_note_label)) {
                    GlassOutlinedTextField(
                        value = uiState.note,
                        onValueChange = { if (it.length <= 200) component.onNoteChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.habit_edit_note_hint)) },
                        singleLine = false,
                        minLines = 2,
                        maxLines = 4,
                    )
                }

                // ── 统计打卡数据开关 ────────────────────────
                PreferenceRowSwitch(
                    title = stringResource(R.string.habit_edit_stats_label),
                    subtitle = stringResource(R.string.habit_edit_stats_subtitle),
                    startIcon = Icons.Outlined.LineBarChart,
                    checked = uiState.statsEnabled,
                    onClick = component::onStatsEnabledChange,
                )
            }
        },
        showNavigationBarsPadding = true,
    )

    if (showTimePicker) {
        val initial = uiState.remindMinutes ?: 9 * 60
        ChineseTimePickerDialog(
            initialHour = initial / 60,
            initialMinute = initial % 60,
            onTimeSelected = { hour, minute ->
                component.onRemindTimeSelect(hour, minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
        )
    }

    // 完整图标库浮动层(底部弹层,应用/通用/全部 tab + 搜索)
    IconPickerSheet(
        visible = showIconPicker,
        onDismiss = { showIconPicker = false },
        onIconSelected = component::onIconSelect,
        selectedIconName = HabitIcons.resolvedRegistryKey(uiState.iconKey),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// 表单小节容器
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FormSection(
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            trailing?.invoke()
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        content()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 圆形图标选择钮(未选显示 "+",已选显示所选图标)+ 下方"选择图标"小字
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun IconPickButton(
    iconKey: String,
    onClick: () -> Unit,
) {
    val hasIcon = iconKey.isNotBlank()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .glassBackground(
                    style = GlassStyle.Thick,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (hasIcon) HabitIcons.iconFor(iconKey) else Icons.Outlined.Add,
                contentDescription = stringResource(R.string.habit_edit_icon_label),
                tint = if (hasIcon) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.habit_edit_icon_label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 颜色区(ColorSelectionRow 主题派生色板;不选任何颜色 = 自动主题色)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ColorSection(
    selectedArgb: Long?,
    onSelect: (Long?) -> Unit,
) {
    // 未选颜色时传 Color.Unspecified:不在色板中 → 无选中勾,正好表达"自动"
    ColorSelectionRow(
        defaultColors = habitPalette(),
        value = selectedArgb?.let { Color(it) } ?: Color.Unspecified,
        onValueChange = { color -> onSelect(color.toArgb().toLong()) },
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// 重复频率区(chip 行 + 次数步进 / 星期多选)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RepeatSection(
    repeatType: String,
    repeatTarget: Int,
    weekdaysMask: Int,
    onRepeatTypeChange: (String) -> Unit,
    onRepeatTargetChange: (Int) -> Unit,
    onWeekdayToggle: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                HabitRepeat.DAILY to R.string.habit_repeat_daily,
                HabitRepeat.WEEKLY_TIMES to R.string.habit_repeat_weekly,
                HabitRepeat.MONTHLY_TIMES to R.string.habit_repeat_monthly,
                HabitRepeat.CUSTOM_WEEKDAYS to R.string.habit_repeat_custom,
            ).forEach { (type, labelRes) ->
                EnhancedChip(
                    selected = repeatType == type,
                    onClick = { onRepeatTypeChange(type) },
                    selectedColor = MaterialTheme.colorScheme.primary,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 14.dp,
                        vertical = 8.dp,
                    ),
                ) {
                    Text(stringResource(labelRes))
                }
            }
        }

        when (repeatType) {
            HabitRepeat.DAILY -> Text(
                text = stringResource(R.string.habit_repeat_daily_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HabitRepeat.WEEKLY_TIMES, HabitRepeat.MONTHLY_TIMES -> {
                // 次数步进(- N +,1..30)
                val labelRes = if (repeatType == HabitRepeat.WEEKLY_TIMES) {
                    R.string.habit_repeat_times_week_label
                } else {
                    R.string.habit_repeat_times_month_label
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    StepperButton(text = "−", onClick = { onRepeatTargetChange(-1) })
                    Text(
                        text = repeatTarget.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    StepperButton(text = "+", onClick = { onRepeatTargetChange(1) })
                    Text(
                        text = stringResource(labelRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HabitRepeat.CUSTOM_WEEKDAYS -> {
                // 7 个星期 chip 多选
                val labels = stringArrayResource(R.array.habit_weekdays_short)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..7).forEach { dayOfWeek ->
                        EnhancedChip(
                            selected = HabitRepeat.isWeekdaySet(weekdaysMask, dayOfWeek),
                            onClick = { onWeekdayToggle(dayOfWeek) },
                            selectedColor = MaterialTheme.colorScheme.primary,
                        ) {
                            Text(labels[dayOfWeek - 1])
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepperButton(
    text: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** 一天内分钟数 → "HH:mm" */
private fun formatRemindMinutes(minutes: Int?): String? {
    if (minutes == null) return null
    return "%02d:%02d".format(minutes / 60, minutes % 60)
}
