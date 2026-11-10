package com.shifenmiao.lifetime.screen

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import com.wanbaohe.core.ui.widget.glass.GlassSwitch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wanbaohe.base.ui.icon.IconRegistry
import com.wanbaohe.base.ui.icon.IconSelector
import com.wanbaohe.base.ui.icon.IconSelectorDisplayMode
import com.wanbaohe.base.ui.picker.ChineseDatePickerDialog
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.BottomSaveCancelBar
import com.shifenmiao.lifetime.R
import com.shifenmiao.lifetime.component.LifeTimeAddCountdownComponent
import com.shifenmiao.lifetime.domain.model.CountdownEvent
import com.wanbaohe.calendar.data.LunarCalendarCalculator
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OneBoxOutlinedTextField
import com.wanbaohe.core.ui.widget.system.OneBoxSectionHeader
import java.time.LocalDate

@Composable
fun LifeTimeAddCountdownScreen(
    component: LifeTimeAddCountdownComponent
) {
    val uiState by component.uiState.collectAsState()

    if (uiState.isLoading) {
        BaseScreen(
            title = stringResource(R.string.lifetime_loading),
            onGoBack = { component.onGoBack() }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.lifetime_loading),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    CountdownForm(
        initial = uiState.countdown,
        onCancel = { component.onGoBack() },
        onSave = component::save,
    )
}

@Composable
private fun CountdownForm(
    initial: CountdownEvent?,
    onCancel: () -> Unit,
    onSave: (CountdownEvent) -> Unit,
) {
    val isEditing = initial != null
    // 农历节日在库里没有 targetDate，用下一次发生日兜底显示
    val initialTargetDate = initial?.targetDate ?: initial?.nextOccurrence()
    var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    var selectedIconKey by remember(initial) { mutableStateOf(initial?.iconKey ?: "Event") }
    var targetDate by remember(initial) { mutableStateOf(initialTargetDate) }
    var isLunarTarget by remember(initial) { mutableStateOf(initial?.isLunarTarget ?: false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var note by remember(initial) { mutableStateOf(initial?.note.orEmpty()) }
    var hasSubmitted by remember { mutableStateOf(false) }

    val trimmedName = name.trim()
    val isNameValid = trimmedName.isNotEmpty()
    val isDateValid = targetDate != null
    val canSave = isNameValid && isDateValid
    val shouldShowNameError = hasSubmitted && !isNameValid
    val shouldShowDateError = hasSubmitted && !isDateValid
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
    val dateCardContainerColor = if (shouldShowDateError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }
    val datePlaceholderColor = if (shouldShowDateError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        placeholderColor
    }

    BaseScreen(
        title = stringResource(
            if (isEditing) R.string.lifetime_edit_countdown else R.string.lifetime_add_countdown
        ),
        onGoBack = onCancel
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(
                    horizontal = OneBoxDesignSystem.screenPadding,
                    vertical = OneBoxDesignSystem.compactSpacing
                ),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.sectionSpacing)
        ) {
            // Name
            Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {
                OneBoxSectionHeader(
                    title = stringResource(R.string.lifetime_countdown_name_label)
                )
                val nameTextStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                )
                val namePlaceholderTextStyle = MaterialTheme.typography.headlineMedium
                OneBoxOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.lifetime_countdown_name_placeholder),
                            style = namePlaceholderTextStyle,
                            color = placeholderColor
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = nameTextStyle,
                    isError = shouldShowNameError,
                    supportingText = {
                        if (shouldShowNameError) {
                            Text(stringResource(R.string.lifetime_validation_milestone_name_required))
                        }
                    }
                )
            }

            // Icon
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = OneBoxDesignSystem.sectionCardShape,
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(OneBoxDesignSystem.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)
                ) {
                    OneBoxSectionHeader(
                        title = stringResource(R.string.lifetime_choose_icon)
                    )
                    IconSelector(
                        selectedIconKey = selectedIconKey,
                        onIconSelected = { selectedIconKey = it },
                        iconKeys = IconRegistry.allKeys,
                        displayMode = IconSelectorDisplayMode.ROW,
                        modifier = Modifier.height(56.dp)
                    )
                }
            }

            // Target date
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = OneBoxDesignSystem.sectionCardShape,
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = dateCardContainerColor
                ),
                onClick = { showDatePicker = true }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(OneBoxDesignSystem.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
                ) {
                    OneBoxSectionHeader(
                        title = stringResource(R.string.lifetime_countdown_target_date)
                    )
                    if (targetDate != null) {
                        val lunar = LunarCalendarCalculator.solarToLunar(
                            targetDate!!.year,
                            targetDate!!.monthValue,
                            targetDate!!.dayOfMonth,
                        )
                        Text(
                            text = stringResource(R.string.lifetime_date_ymd, targetDate!!.year, targetDate!!.monthValue, targetDate!!.dayOfMonth),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.lifetime_lunar_date, lunar.ganZhiYear, lunar.monthName, lunar.dayName),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.lifetime_select_date),
                            style = MaterialTheme.typography.titleMedium,
                            color = datePlaceholderColor
                        )
                    }
                }
            }
            if (shouldShowDateError) {
                Text(
                    text = stringResource(R.string.lifetime_validation_milestone_target_date_required),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(horizontal = OneBoxDesignSystem.compactSpacing)
                )
            }

            // Lunar toggle
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = OneBoxDesignSystem.sectionCardShape,
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(OneBoxDesignSystem.cardPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.lifetime_countdown_lunar_target),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.lifetime_countdown_lunar_target_help),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    GlassSwitch(
                        checked = isLunarTarget,
                        onCheckedChange = { isLunarTarget = it },
                    )
                }
            }

            // Note
            Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {
                OneBoxSectionHeader(
                    title = stringResource(R.string.lifetime_milestone_note)
                )
                OneBoxOutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.lifetime_countdown_note_placeholder),
                            color = placeholderColor
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
        }

        BottomSaveCancelBar(
            modifier = Modifier.imePadding(),
            onCancel = onCancel,
            onSave = {
                hasSubmitted = true
                if (!canSave) return@BottomSaveCancelBar
                val target = targetDate ?: return@BottomSaveCancelBar
                val (lunarMonth, lunarDay) = if (isLunarTarget) {
                    val lunar = LunarCalendarCalculator.solarToLunar(
                        target.year, target.monthValue, target.dayOfMonth
                    )
                    lunar.month to lunar.day
                } else 0 to 0

                // 编辑时保留 id / 排序 / 预置标记等原字段
                val base = initial ?: CountdownEvent(name = trimmedName)
                onSave(
                    base.copy(
                        name = trimmedName,
                        iconKey = selectedIconKey,
                        targetDate = target,
                        isLunarTarget = isLunarTarget,
                        lunarMonth = lunarMonth.takeIf { isLunarTarget },
                        lunarDay = lunarDay.takeIf { isLunarTarget },
                        note = note.takeIf { it.isNotBlank() },
                    )
                )
            },
            saveEnabled = true,
            saveText = stringResource(
                if (isEditing) R.string.lifetime_note_save else R.string.lifetime_add_countdown
            )
        )
    }

    if (showDatePicker) {
        ChineseDatePickerDialog(
            initialDate = targetDate,
            onDateSelected = { date ->
                targetDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}
