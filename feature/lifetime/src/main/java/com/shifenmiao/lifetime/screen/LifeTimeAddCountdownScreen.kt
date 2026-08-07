package com.shifenmiao.lifetime.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.icon.IconRegistry
import com.shifenmiao.base.ui.icon.IconSelector
import com.shifenmiao.base.ui.icon.IconSelectorDisplayMode
import com.shifenmiao.base.ui.picker.ChineseDatePickerDialog
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.BottomSaveCancelBar
import com.shifenmiao.lifetime.R
import com.shifenmiao.lifetime.component.LifeTimeAddCountdownComponent
import com.shifenmiao.lifetime.domain.model.CountdownEvent
import com.wanbaohe.calendar.data.LunarCalendarCalculator
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionHeader
import java.time.LocalDate

@Composable
fun LifeTimeAddCountdownScreen(
    component: LifeTimeAddCountdownComponent
) {
    var name by remember { mutableStateOf("") }
    var selectedIconKey by remember { mutableStateOf("Event") }
    var targetDate by remember { mutableStateOf<LocalDate?>(null) }
    var isLunarTarget by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
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
        MaterialTheme.colorScheme.surfaceContainer
    }
    val datePlaceholderColor = if (shouldShowDateError) {
        MaterialTheme.colorScheme.error
    } else {
        placeholderColor
    }

    BaseScreen(
        title = stringResource(R.string.lifetime_add_countdown),
        onGoBack = { component.onGoBack() }
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
                containerAlpha = 0.22f,
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
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
                containerAlpha = 0.22f,
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
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = OneBoxDesignSystem.compactSpacing)
                )
            }

            // Lunar toggle
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = OneBoxDesignSystem.sectionCardShape,
                containerAlpha = 0.22f,
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
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
                    Switch(
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
            onCancel = { component.onGoBack() },
            onSave = {
                hasSubmitted = true
                if (!canSave) return@BottomSaveCancelBar
                val target = targetDate
                if (target == null) return@BottomSaveCancelBar
                val (lunarMonth, lunarDay) = if (isLunarTarget) {
                    val lunar = LunarCalendarCalculator.solarToLunar(
                        target.year, target.monthValue, target.dayOfMonth
                    )
                    lunar.month to lunar.day
                } else 0 to 0
                component.addCountdown(
                    CountdownEvent(
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
            saveText = stringResource(R.string.lifetime_add_countdown)
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
