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
import com.shifenmiao.base.ui.picker.ChineseTimePickerDialog
import com.shifenmiao.base.ui.picker.ChineseTimeRangePickerDialog
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.a2ui.catalog.A2uiComponentRenderer
import com.wanbaohe.a2ui.catalog.A2uiRenderContext
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import com.wanbaohe.a2ui.domain.model.DynamicValue
import kotlinx.serialization.json.JsonPrimitive
import java.util.Locale
import javax.inject.Inject
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory

class TimeInputRenderer @Inject constructor() : A2uiComponentRenderer {

    override val componentType = "TimeInput"

    @Composable
    override fun Render(
        component: A2uiComponent,
        context: A2uiRenderContext,
        children: @Composable () -> Unit,
    ) {
        val mode = context.resolveString(component.properties["mode"]) ?: "time"
        val valueDynamic = component.properties["value"]
        val currentValue = context.resolveString(valueDynamic) ?: ""
        val pointerPath = (valueDynamic as? DynamicValue.Pointer)?.path
        val label = context.resolveString(component.properties["label"])
            ?: stringResource(R.string.a2ui_datetime_label)
        val enabled = context.resolveBoolean(component.properties["enabled"]) ?: true
        val separator = context.resolveString(component.properties["separator"]) ?: " ~ "

        var showTimePicker by remember { mutableStateOf(false) }
        var showTimeRangePicker by remember { mutableStateOf(false) }

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
                            "timerange" -> showTimeRangePicker = true
                            else -> showTimePicker = true
                        }
                    },
                    enabled = enabled,
                ) {
                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory, contentDescription = null)
                }
            },
        )

        if (showTimePicker) {
            val timeParts = remember(currentValue) {
                parseTimeParts(currentValue)
            }
            ChineseTimePickerDialog(
                initialHour = timeParts?.first ?: 0,
                initialMinute = timeParts?.second ?: 0,
                title = label,
                onTimeSelected = { hour, minute ->
                    pointerPath?.let {
                        context.updateDataModel(it, JsonPrimitive(formatTimeShort(hour, minute)))
                    }
                    showTimePicker = false
                },
                onDismiss = { showTimePicker = false },
            )
        }

        if (showTimeRangePicker) {
            val rangeParts = remember(currentValue, separator) {
                parseTimeRange(currentValue, separator)
            }
            ChineseTimeRangePickerDialog(
                initialStartHour = rangeParts?.first?.first ?: 9,
                initialStartMinute = rangeParts?.first?.second ?: 0,
                initialEndHour = rangeParts?.second?.first ?: 18,
                initialEndMinute = rangeParts?.second?.second ?: 0,
                title = label,
                onTimeRangeSelected = { startHour, startMinute, endHour, endMinute ->
                    val formatted =
                        "${formatTimeShort(startHour, startMinute)}$separator${formatTimeShort(endHour, endMinute)}"
                    pointerPath?.let {
                        context.updateDataModel(it, JsonPrimitive(formatted))
                    }
                    showTimeRangePicker = false
                },
                onDismiss = { showTimeRangePicker = false },
            )
        }
    }

    private fun formatTimeShort(hour: Int, minute: Int): String =
        String.format(Locale.US, "%02d:%02d", hour, minute)

    private fun parseTimeParts(value: String): Pair<Int, Int>? {
        if (value.isBlank()) return null
        return runCatching {
            val parts = value.split(":")
            parts[0].toInt() to parts[1].toInt()
        }.getOrNull()
    }

    private fun parseTimeRange(
        value: String,
        separator: String,
    ): Pair<Pair<Int, Int>, Pair<Int, Int>>? {
        if (value.isBlank()) return null
        val parts = value.split(separator)
        if (parts.size == 2) {
            val start = parseTimeParts(parts[0]) ?: return null
            val end = parseTimeParts(parts[1]) ?: return null
            return start to end
        }
        val regex = """(\d{1,2}):(\d{2}).*?(\d{1,2}):(\d{2})""".toRegex()
        val match = regex.find(value) ?: return null
        val (startHour, startMinute, endHour, endMinute) = match.destructured
        return (startHour.toInt() to startMinute.toInt()) to (endHour.toInt() to endMinute.toInt())
    }
}
