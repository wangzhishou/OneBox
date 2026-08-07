package com.shifenmiao.lifetime.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.base.ui.picker.ChineseDatePickerDialog
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.lifetime.R
import com.shifenmiao.lifetime.component.LifeTimeSettingsComponent
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.sliders.custom_slider.CustomSlider
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionHeader
import com.t8rin.imagetoolbox.core.ui.widget.system.OnePrimaryButton
import java.time.LocalDate

@Composable
fun LifeTimeSettingsScreen(
    component: LifeTimeSettingsComponent
) {
    val uiState by component.uiState.collectAsState()

    BaseScreen(
        title = stringResource(R.string.lifetime_settings),
        onGoBack = { component.onGoBack() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = OneBoxDesignSystem.screenPadding,
                    vertical = OneBoxDesignSystem.compactSpacing
                ),
            verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.sectionSpacing)
        ) {
            BirthDateSection(
                startDate = uiState.startDate,
                onShowDatePicker = { component.showDatePicker() }
            )

            TargetAgeSection(
                targetAge = uiState.targetAge,
                onTargetAgeChange = { component.saveTargetAge(it) }
            )

            Spacer(modifier = Modifier.weight(1f))

            OnePrimaryButton(
                text = stringResource(R.string.lifetime_update_settings),
                onClick = { component.saveAndGoBack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )
        }
    }

    if (uiState.showDatePicker) {
        ChineseDatePickerDialog(
            initialDate = uiState.startDate,
            maxDate = LocalDate.now(),
            onDateSelected = { date -> component.saveStartDate(date) },
            onDismiss = { component.hideDatePicker() }
        )
    }
}

@Composable
private fun BirthDateSection(
    startDate: LocalDate?,
    onShowDatePicker: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {
        OneBoxSectionHeader(
            title = stringResource(R.string.lifetime_birth_date)
        )

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = OneBoxDesignSystem.sectionCardShape,
            containerAlpha = 0.22f,
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            onClick = onShowDatePicker
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = OneBoxDesignSystem.blockSpacing),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (startDate != null) {
                    Text(
                        text = stringResource(R.string.lifetime_date_ymd, startDate.year, startDate.monthValue, startDate.dayOfMonth),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
                    Text(
                        text = stringResource(R.string.lifetime_tap_to_change),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = stringResource(R.string.lifetime_select_birthday),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun TargetAgeSection(
    targetAge: Int,
    onTargetAgeChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OneBoxSectionHeader(
                title = stringResource(R.string.lifetime_target_age)
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = targetAge.toString(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = " ${stringResource(R.string.lifetime_unit_years)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        OneBoxSectionCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(OneBoxDesignSystem.blockSpacing * 2)
            ) {
                CustomSlider(
                    value = targetAge.toFloat(),
                    onValueChange = { onTargetAgeChange(it.toInt()) },
                    valueRange = 50f..120f,
                    steps = 13,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "50",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = targetAge.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "120",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.lifetime_target_age_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = OneBoxDesignSystem.microSpacing)
        )
    }
}
