package com.shifenmiao.base.ui.picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.R
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import java.time.Year
import java.time.YearMonth
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.resources.icons.line.LineKeyboardArrowLeft

@Composable
fun ChineseYearMonthPickerDialog(
    initialYearMonth: YearMonth,
    onYearMonthSelected: (YearMonth) -> Unit,
    onDismiss: () -> Unit,
    minYear: Int = 1900,
    maxYear: Int = 2100,
) {
    var year by remember(initialYearMonth) { mutableIntStateOf(initialYearMonth.year) }
    var yearInput by remember(initialYearMonth) {
        mutableStateOf(initialYearMonth.year.toString())
    }
    val inputYear = yearInput.toIntOrNull()?.takeIf { it in minYear..maxYear }

    fun updateYear(value: Int) {
        year = value.coerceIn(minYear, maxYear)
        yearInput = year.toString()
    }

    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = onDismiss,
        enableGlass = true,
        containerColor = AppTheme.colors.getContainerSurfaceColor(),
        title = {
            Text(
                text = stringResource(R.string.date_picker_select_year_month),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        confirmButton = {
            FilledTonalButton(
                enabled = inputYear != null,
                onClick = {
                    inputYear?.let {
                        onYearMonthSelected(YearMonth.of(it, initialYearMonth.monthValue))
                        onDismiss()
                    }
                },
            ) {
                Text(stringResource(R.string.date_picker_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.date_picker_cancel))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IconButton(onClick = { updateYear(year - 1) }) {
                        Icon(
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineKeyboardArrowLeft,
                            contentDescription = null,
                        )
                    }
                    GlassOutlinedTextField(
                        value = yearInput,
                        onValueChange = { input ->
                            if (input.length <= 4 && input.all(Char::isDigit)) {
                                yearInput = input
                                input.toIntOrNull()
                                    ?.takeIf { it in minYear..maxYear }
                                    ?.let { year = it }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text(stringResource(R.string.date_picker_year_input)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = AppTheme.colors.getOutlinedTextFieldColors(),
                    )
                    IconButton(onClick = { updateYear(year + 1) }) {
                        Icon(
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronRight,
                            contentDescription = null,
                        )
                    }
                }
                TextButton(onClick = { updateYear(Year.now().value) }) {
                    Text(stringResource(R.string.date_picker_current_year))
                }

                (1..12).chunked(3).forEach { months ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        months.forEach { month ->
                            FilledTonalButton(
                                onClick = {
                                    onYearMonthSelected(YearMonth.of(year, month))
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                colors = if (
                                    year == initialYearMonth.year &&
                                    month == initialYearMonth.monthValue
                                ) {
                                    AppTheme.colors.getSecondaryContainerButtonColors()
                                } else {
                                    AppTheme.colors.getSurfaceContainerButtonColors()
                                },
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.date_picker_month_format,
                                        month,
                                    ),
                                    modifier = Modifier.padding(vertical = 2.dp),
                                )
                            }
                        }
                    }
                }
            }
        },
    )
}
