/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.a2ui.catalog.builtin.input

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wanbaohe.base.ui.picker.ChineseDatePickerDialog
import com.wanbaohe.base.ui.picker.ChineseDateRangePickerDialog
import com.wanbaohe.base.ui.picker.ChineseTimePickerDialog
import com.shifenmiao.core.R
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import com.wanbaohe.a2ui.domain.model.DynamicValue
import kotlinx.serialization.json.JsonPrimitive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalendar

class DateInputRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "DateInput"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val mode = context.resolveString(component.properties["mode"]) ?: "date"
        val valueDynamic = component.properties["value"]
        val currentValue = context.resolveString(valueDynamic) ?: ""
        val pointerPath = (valueDynamic as? DynamicValue.Pointer)?.path
        val label = context.resolveString(component.properties["label"])
            ?: stringResource(R.string.a2ui_datetime_label)
        val enabled = context.resolveBoolean(component.properties["enabled"]) ?: true
        val separator = context.resolveString(component.properties["separator"]) ?: " ~ "

        var showDatePicker by remember { mutableStateOf(false) }
        var showDateRangePicker by remember { mutableStateOf(false) }
        var showTimePicker by remember { mutableStateOf(false) }
        var pendingDateMillis by remember { mutableStateOf<Long?>(null) }

        GlassOutlinedTextField(
            value = currentValue,
            onValueChange = { newValue ->
                pointerPath?.let {
                    context.updateDataModel(it, JsonPrimitive(newValue))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            label = label.takeIf { it.isNotBlank() }?.let { { Text(it) } },
            singleLine = true,
            trailingIcon = {
                IconButton(
                    onClick = {
                        when (mode) {
                            "daterange" -> showDateRangePicker = true
                            else -> showDatePicker = true
                        }
                    },
                    enabled = enabled,
                ) {
                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalendar, contentDescription = null)
                }
            },
        )

        val initialDateMillis = remember(currentValue, mode) {
            if (mode == "date" || mode == "datetime") parseDateMillis(currentValue) else null
        }

        if (showDatePicker) {
            ChineseDatePickerDialog(
                initialDateMillis = initialDateMillis ?: pendingDateMillis,
                onDateSelected = { millis ->
                    when (mode) {
                        "datetime" -> {
                            pendingDateMillis = millis
                            showTimePicker = true
                        }

                        else -> {
                            pointerPath?.let {
                                context.updateDataModel(it, JsonPrimitive(formatDate(millis)))
                            }
                        }
                    }
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false },
            )
        }

        if (showDateRangePicker) {
            val rangeDates = remember(currentValue, separator) {
                parseDateRange(currentValue, separator)
            }
            ChineseDateRangePickerDialog(
                initialStartDateMillis = rangeDates?.first,
                initialEndDateMillis = rangeDates?.second,
                onDateRangeSelected = { startMillis, endMillis ->
                    val formatted = "${formatDate(startMillis)}$separator${formatDate(endMillis)}"
                    pointerPath?.let {
                        context.updateDataModel(it, JsonPrimitive(formatted))
                    }
                    showDateRangePicker = false
                },
                onDismiss = { showDateRangePicker = false },
            )
        }

        if (showTimePicker) {
            val timeParts = remember(currentValue) {
                parseTimeParts(currentValue.substringAfter('T', ""))
            }
            ChineseTimePickerDialog(
                initialHour = timeParts?.first ?: 0,
                initialMinute = timeParts?.second ?: 0,
                title = label,
                onTimeSelected = { hour, minute ->
                    val datePart = pendingDateMillis?.let(::formatDate)
                        ?: formatDate(System.currentTimeMillis())
                    val formatted = "${datePart}T${formatTimeWithSeconds(hour, minute)}"
                    pointerPath?.let {
                        context.updateDataModel(it, JsonPrimitive(formatted))
                    }
                    showTimePicker = false
                    pendingDateMillis = null
                },
                onDismiss = {
                    showTimePicker = false
                    pendingDateMillis = null
                },
            )
        }
    }

    private fun formatDate(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))

    private fun formatTimeWithSeconds(hour: Int, minute: Int): String =
        String.format(Locale.US, "%02d:%02d:00", hour, minute)

    private fun parseDateMillis(value: String): Long? {
        if (value.isBlank()) return null
        return runCatching {
            val datePart = value.substringBefore('T')
            SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(datePart)?.time
        }.getOrNull()
    }

    private fun parseDateRange(value: String, separator: String): Pair<Long, Long>? {
        if (value.isBlank()) return null
        val parts = value.split(separator)
        if (parts.size != 2) return null
        val start = parseDateMillis(parts[0]) ?: return null
        val end = parseDateMillis(parts[1]) ?: return null
        return start to end
    }

    private fun parseTimeParts(value: String): Pair<Int, Int>? {
        if (value.isBlank()) return null
        return runCatching {
            val parts = value.split(":")
            parts[0].toInt() to parts[1].toInt()
        }.getOrNull()
    }
}
