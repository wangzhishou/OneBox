package com.shifenmiao.lifetime.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.icon.IconRegistry
import com.shifenmiao.base.ui.icon.IconSelector
import com.shifenmiao.base.ui.icon.IconSelectorDisplayMode
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.BottomSaveCancelBar
import com.shifenmiao.lifetime.R
import com.shifenmiao.lifetime.component.LifeTimeAddEventComponent
import com.shifenmiao.lifetime.domain.model.FrequencyEvent
import com.shifenmiao.lifetime.domain.model.FrequencyType
import com.shifenmiao.lifetime.util.localizedEventUnit
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionHeader
import com.t8rin.imagetoolbox.core.ui.widget.system.OnePrimaryButton
import com.t8rin.imagetoolbox.core.ui.widget.system.OneSecondaryButton
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowDropDown

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun LifeTimeAddEventScreen(
    component: LifeTimeAddEventComponent
) {
    var name by remember { mutableStateOf("") }
    var selectedIconKey by remember { mutableStateOf("Category") }
    var frequencyType by remember { mutableStateOf(FrequencyType.DAILY) }
    var timesPerPeriod by remember { mutableStateOf("1") }
    var selectedUnit by remember { mutableStateOf("次") }
    var unitMenuExpanded by remember { mutableStateOf(false) }
    var hasSubmitted by remember { mutableStateOf(false) }

    val trimmedName = name.trim()
    val parsedTimesPerPeriod = timesPerPeriod.toIntOrNull()
    val isNameValid = trimmedName.isNotEmpty()
    val isTimesPerPeriodValid = (parsedTimesPerPeriod ?: 0) > 0
    val canSave = isNameValid && isTimesPerPeriodValid
    val shouldShowNameError = hasSubmitted && !isNameValid
    val shouldShowTimesPerPeriodError = hasSubmitted && !isTimesPerPeriodValid
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)

    BaseScreen(
        title = stringResource(R.string.lifetime_add_event),
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
            Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {
                OneBoxSectionHeader(
                    title = stringResource(R.string.lifetime_event_name_label)
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
                            text = stringResource(R.string.lifetime_event_name_placeholder),
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
                            Text(stringResource(R.string.lifetime_validation_event_name_required))
                        }
                    }
                )
            }

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
                        title = stringResource(R.string.lifetime_frequency_label)
                    )
                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing),
                        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
                    ) {
                        listOf(FrequencyType.ONE_TIME, FrequencyType.DAILY, FrequencyType.WEEKLY, FrequencyType.MONTHLY, FrequencyType.YEARLY).forEach { type ->
                            val isSelected = frequencyType == type
                            if (isSelected) {
                                OnePrimaryButton(
                                    text = getFrequencyLabel(type),
                                    onClick = { frequencyType = type }
                                )
                            } else {
                                OneSecondaryButton(
                                    text = getFrequencyLabel(type),
                                    onClick = { frequencyType = type }
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.itemSpacing)
            ) {
                OneBoxOutlinedTextField(
                    value = timesPerPeriod,
                    onValueChange = { if (it.all { c -> c.isDigit() }) timesPerPeriod = it },
                    label = { Text(stringResource(R.string.lifetime_times_per_period)) },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.lifetime_default_times_per_period),
                            color = placeholderColor
                        )
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    isError = shouldShowTimesPerPeriodError,
                    supportingText = {
                        if (shouldShowTimesPerPeriodError) {
                            Text(stringResource(R.string.lifetime_validation_times_per_period_required))
                        }
                    }
                )

                Box(modifier = Modifier.weight(1f)) {
                    OneBoxOutlinedTextField(
                        value = localizedEventUnit(selectedUnit),
                        onValueChange = {},
                        label = { Text(stringResource(R.string.lifetime_unit_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        singleLine = true,
                        trailingIcon = {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineArrowDropDown,
                                contentDescription = null
                            )
                        }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { unitMenuExpanded = true }
                    )
                    DropdownMenu(
                        expanded = unitMenuExpanded,
                        onDismissRequest = { unitMenuExpanded = false }
                    ) {
                        UNIT_OPTIONS.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(localizedEventUnit(unit)) },
                                onClick = {
                                    selectedUnit = unit
                                    unitMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
        }

        BottomSaveCancelBar(
            modifier = Modifier.imePadding(),
            onCancel = { component.onGoBack() },
            onSave = {
                hasSubmitted = true
                if (!canSave) return@BottomSaveCancelBar

                component.addEvent(
                    FrequencyEvent(
                        name = trimmedName,
                        iconKey = selectedIconKey,
                        frequencyType = frequencyType,
                        timesPerPeriod = parsedTimesPerPeriod ?: 1,
                        unit = selectedUnit
                    )
                )
            },
            saveEnabled = true,
            saveText = stringResource(R.string.lifetime_add_to_timeline)
        )
    }
}

@Composable
private fun getFrequencyLabel(type: FrequencyType): String {
    return when (type) {
        FrequencyType.ONE_TIME -> stringResource(R.string.lifetime_frequency_one_time)
        FrequencyType.DAILY -> stringResource(R.string.lifetime_frequency_daily)
        FrequencyType.WEEKLY -> stringResource(R.string.lifetime_frequency_weekly)
        FrequencyType.MONTHLY -> stringResource(R.string.lifetime_frequency_monthly)
        FrequencyType.YEARLY -> stringResource(R.string.lifetime_frequency_yearly)
    }
}

private val UNIT_OPTIONS = listOf(
    "次", "顿", "篇", "本", "部", "集",
    "公里", "米", "步", "分钟", "小时",
    "个", "杯", "瓶", "包", "盒"
)
