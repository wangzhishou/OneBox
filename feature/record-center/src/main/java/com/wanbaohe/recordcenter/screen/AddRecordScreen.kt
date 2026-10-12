package com.wanbaohe.recordcenter.screen

import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.picker.ChineseDatePickerDialog
import com.shifenmiao.base.ui.picker.ChineseTimePickerDialog
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalendar
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.wanbaohe.recordcenter.R
import com.wanbaohe.recordcenter.component.RecordEditorComponent
import com.wanbaohe.recordcenter.component.RecordFieldError
import com.wanbaohe.recordcenter.model.RecordFieldsCodec
import com.wanbaohe.recordcenter.screen.util.formatRecordDateTime
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * 记录录入/编辑页 — 按类型定义动态生成数值表单,附记录时间与备注。
 */
@Composable
fun AddRecordScreen(component: RecordEditorComponent) {
    val definition = component.definition
    val inputs by component.inputs.collectAsState()
    val fieldErrors by component.fieldErrors.collectAsState()
    val happenedAt by component.happenedAt.collectAsState()
    val note by component.note.collectAsState()
    val saving by component.saving.collectAsState()
    val saveError by component.saveError.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // service 层校验失败:toast 本地化错误信息
    LaunchedEffect(saveError) {
        saveError?.let {
            AppToastHost.showToast(it)
            component.consumeSaveError()
        }
    }

    BaseScreen(
        title = {
            Text(
                text = stringResource(
                    if (component.isEditing) R.string.record_center_edit_record
                    else R.string.record_center_add_record
                ) + definition?.let { " · ${stringResource(it.titleRes)}" }.orEmpty(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        onGoBack = component.onGoBack,
        actions = {
            if (definition != null) {
                Text(
                    text = stringResource(R.string.record_center_action_save),
                    color = if (saving) MaterialTheme.colorScheme.onSurfaceVariant
                    else AppTheme.colors.getPrimaryColor(),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(enabled = !saving, onClick = component::submit)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        },
        supportGlassEffect = true,
        content = {
            if (definition == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.record_center_error_unknown_type, component.recordTypeKey),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        // 键盘弹起时缩小滚动视口,输入框不被遮挡
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // 动态数值字段
                    definition.fields.forEach { field ->
                        val error = fieldErrors[field.key]
                        GlassOutlinedTextField(
                            value = inputs[field.key].orEmpty(),
                            onValueChange = { component.onFieldInputChange(field.key, it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text(
                                    text = stringResource(field.labelRes) +
                                        if (field.required) "" else stringResource(R.string.agent_tool_record_center_field_optional)
                                )
                            },
                            suffix = {
                                if (field.unit.isNotEmpty()) Text(text = field.unit)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = error != null,
                            supportingText = {
                                if (error != null) {
                                    Text(text = fieldErrorText(error))
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                        )
                    }

                    // 参考范围
                    definition.referenceRangeRes?.let { rangeRes ->
                        Text(
                            text = stringResource(rangeRes),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }

                    // 记录时间
                    GlassCard(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        borderWidth = 0.dp,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LineCalendar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.record_center_label_time),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = formatRecordDateTime(happenedAt),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }

                    // 备注
                    GlassOutlinedTextField(
                        value = note,
                        onValueChange = component::onNoteChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = stringResource(R.string.record_center_label_note)) },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
    )

    if (showDatePicker) {
        ChineseDatePickerDialog(
            initialDate = Instant.ofEpochMilli(happenedAt).atZone(ZoneId.systemDefault()).toLocalDate(),
            maxDate = LocalDate.now(),
            onDateSelected = { date ->
                component.onDateChange(
                    date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                )
                showDatePicker = false
                // 选完日期接着选时间
                showTimePicker = true
            },
            onDismiss = { showDatePicker = false },
        )
    }

    if (showTimePicker) {
        val time = Instant.ofEpochMilli(happenedAt).atZone(ZoneId.systemDefault()).toLocalTime()
        ChineseTimePickerDialog(
            initialHour = time.hour,
            initialMinute = time.minute,
            onTimeSelected = { hour, minute ->
                component.onTimeChange(hour, minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
        )
    }
}

@Composable
private fun fieldErrorText(error: RecordFieldError): String = when (error) {
    RecordFieldError.Required -> stringResource(R.string.record_center_error_required)
    RecordFieldError.InvalidNumber -> stringResource(R.string.record_center_error_invalid_number)
    is RecordFieldError.OutOfRange -> stringResource(
        R.string.record_center_error_out_of_range_short,
        error.min?.let(RecordFieldsCodec::formatValue) ?: "-∞",
        error.max?.let(RecordFieldsCodec::formatValue) ?: "+∞",
    )
}
