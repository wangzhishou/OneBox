package com.wanbaohe.app.ui

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.shifenmiao.ai.agent.tool.AgentQuestionType
import com.shifenmiao.ai.agent.tool.AgentUserQuestionItem
import com.shifenmiao.base.ui.picker.ChineseDatePickerDialog
import com.shifenmiao.base.ui.picker.ChineseDateRangePickerDialog
import com.shifenmiao.base.ui.picker.ChineseTimeRangePickerDialog
import com.shifenmiao.base.ui.picker.rememberCityPickerState
import com.shifenmiao.model.ui.picker.SelectedCountryData
import com.t8rin.imagetoolbox.core.data.utils.SafUriUtils
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.FileType
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.Picker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFilePicker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFolderPicker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.widget.color_picker.ColorPickerSheet
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedTimePickerDialog
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTextFieldVisualPreset
import androidx.compose.runtime.compositionLocalOf
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTheme
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalendar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolder
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAccessTime
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDateRange
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePlace
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAttachFile

/**
 * AI 提问弹层内的专业类型 picker 共享的 "placeAboveAll" 上下文。
 *
 * 父弹框（[AIQuestionDialog] / [AIQuestionBottomSheet]）用 [placeAboveAll][androidx.compose.runtime.CompositionLocalProvider] = true 渲染时
 * 内嵌的 picker（date / time / date_range / time_range / color / city）需要同步把
 * `placeAboveAll` 也设成 true，否则会被外层 [com.t8rin.imagetoolbox.core.ui.widget.utils.FullscreenPopup]
 * 的 z-index 处理逻辑盖住（详见 [com.t8rin.imagetoolbox.core.ui.widget.utils.PopupLayout.show]）。
 *
 * 默认 false，保持非嵌套场景下与改动前一致。
 */
val LocalAIQuestionPickerPlaceAboveAll = compositionLocalOf { false }

/**
 * 特殊类型问题的输入框 —— `OutlinedTextField` + 右侧 picker 图标。
 *
 * 当 [AgentUserQuestionItem.type] 不是 [AgentQuestionType.text] 时，输入框右侧会渲染一个图标按钮；
 * 点击图标唤起对应的选择器（时间 / 时间段 / 日期 / 日期段 / 颜色 / 城市 / 图片 / 文件 / 目录），
 * 用户确认后选择结果以字符串形式回写到 [onValueChange]。
 *
 * 系统类 picker（image / file / folder）会自动把 SAF 或 MediaStore content URI 转写为 file:// URI，
 * 避免后台执行时遇到 SAF 权限过期的问题（依赖 [SafUriUtils.toFileUri]）。
 *
 * 用户也可以直接在输入框里手动编辑文字，picker 只是一种便捷入口。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISpecializedQuestionField(
    question: AgentUserQuestionItem,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isLastTextQuestion: Boolean,
    onMoveToNextTextQuestion: () -> Unit,
    onDone: () -> Unit,
) {
    val type = question.type
    val context = LocalContext.current
    var pickerVisible by remember(question.name, type) { mutableStateOf(false) }

    val launchSystemPicker = rememberSystemPicker(type = type, context = context, onPicked = onValueChange)

    GlassOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        visualPreset = GlassTextFieldVisualPreset.Expressive,
        placeholder = {
            if (question.placeholder.isNotBlank()) {
                Text(question.placeholder)
            }
        },
        trailingIcon = {
            val icon = pickerIconFor(type)
            if (icon != null) {
                IconButton(
                    onClick = {
                        val launcher = launchSystemPicker
                        if (launcher != null) {
                            launcher()
                        } else {
                            pickerVisible = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = type.name,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = if (isLastTextQuestion) ImeAction.Done else ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { onMoveToNextTextQuestion() },
            onDone = { onDone() }
        ),
    )

    if (pickerVisible) {
        AISpecializedPicker(
            type = type,
            currentValue = value,
            title = question.question,
            onPicked = {
                onValueChange(it)
                pickerVisible = false
            },
            onDismiss = { pickerVisible = false },
        )
    }
}

/**
 * 为 image / file / folder 类型注册系统 picker 的启动器，返回可在点击时调用的 lambda。
 *
 * 对于其他类型返回 null，调用方应回退到内嵌的 composable picker。
 */
@Composable
private fun rememberSystemPicker(
    type: AgentQuestionType,
    context: Context,
    onPicked: (String) -> Unit,
): (() -> Unit)? = when (type) {
    AgentQuestionType.image -> {
        val picker = rememberImagePicker(picker = Picker.Single) { uris ->
            handlePickedUri(context, uris.firstOrNull(), onPicked)
        }
        ({ picker.pickImage() })
    }
    AgentQuestionType.file -> {
        val picker = rememberFilePicker(type = FileType.Single, mimeType = MimeType.All) { uris ->
            handlePickedUri(context, uris.firstOrNull(), onPicked)
        }
        ({ picker.pickFile() })
    }
    AgentQuestionType.folder -> {
        val picker = rememberFolderPicker { uri ->
            handlePickedUri(context, uri, onPicked)
        }
        ({ picker.pickFolder() })
    }
    else -> null
}

private fun handlePickedUri(
    context: Context,
    picked: Uri?,
    onPicked: (String) -> Unit,
) {
    if (picked == null) return
    val fileUri = SafUriUtils.toFileUri(context, picked)
    onPicked(fileUri?.toString() ?: picked.toString())
}

private fun pickerIconFor(type: AgentQuestionType): ImageVector? = when (type) {
    AgentQuestionType.text -> null
    AgentQuestionType.time -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory
    AgentQuestionType.time_range -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAccessTime
    AgentQuestionType.date -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalendar
    AgentQuestionType.date_range -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDateRange
    AgentQuestionType.color -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTheme
    AgentQuestionType.city -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePlace
    AgentQuestionType.image -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage
    AgentQuestionType.file -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAttachFile
    AgentQuestionType.folder -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFolder
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AISpecializedPicker(
    type: AgentQuestionType,
    currentValue: String,
    title: String,
    onPicked: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val placeAboveAll = LocalAIQuestionPickerPlaceAboveAll.current
    when (type) {
        AgentQuestionType.text,
        AgentQuestionType.image,
        AgentQuestionType.file,
        AgentQuestionType.folder -> Unit

        AgentQuestionType.time -> {
            val (initialHour, initialMinute) = remember(currentValue) {
                parseTimeString(currentValue)
            }
            val timeState = rememberTimePickerState(
                initialHour = initialHour,
                initialMinute = initialMinute,
                is24Hour = true,
            )
            EnhancedTimePickerDialog(
                visible = true,
                onDismissRequest = onDismiss,
                state = timeState,
                placeAboveAll = placeAboveAll,
                onTimePicked = { hour, minute ->
                    onPicked(formatTime(hour, minute))
                },
            )
        }

        AgentQuestionType.time_range -> {
            val parts = remember(currentValue) { parseTimeRangeString(currentValue) }
            ChineseTimeRangePickerDialog(
                initialStartHour = parts.first.first,
                initialStartMinute = parts.first.second,
                initialEndHour = parts.second.first,
                initialEndMinute = parts.second.second,
                title = title.ifBlank { null },
                onTimeRangeSelected = { sh, sm, eh, em ->
                    onPicked("${formatTime(sh, sm)} ~ ${formatTime(eh, em)}")
                },
                onDismiss = onDismiss,
                placeAboveAll = placeAboveAll,
            )
        }

        AgentQuestionType.date -> {
            ChineseDatePickerDialog(
                initialDateMillis = parseDateString(currentValue),
                title = title.ifBlank { stringResource(com.t8rin.imagetoolbox.core.ui.R.string.date_picker_title) },
                onDateSelected = { millis ->
                    onPicked(formatDateMillis(millis))
                },
                onDismiss = onDismiss,
                placeAboveAll = placeAboveAll,
            )
        }

        AgentQuestionType.date_range -> {
            val parts = remember(currentValue) { parseDateRangeString(currentValue) }
            ChineseDateRangePickerDialog(
                initialStartDateMillis = parts.first,
                initialEndDateMillis = parts.second,
                onDateRangeSelected = { start, end ->
                    onPicked("${formatDateMillis(start)} ~ ${formatDateMillis(end)}")
                },
                onDismiss = onDismiss,
                placeAboveAll = placeAboveAll,
            )
        }

        AgentQuestionType.color -> {
            val initialColor = remember(currentValue) {
                parseHexColor(currentValue) ?: Color.Black
            }
            ColorPickerSheet(
                visible = true,
                onDismiss = onDismiss,
                color = initialColor,
                allowAlpha = currentColorHasAlpha(currentValue),
                onColorSelected = { picked ->
                    onPicked(picked.toHex(includeAlpha = currentColorHasAlpha(currentValue)))
                },
                placeAboveAll = placeAboveAll,
            )
        }

        AgentQuestionType.city -> {
            val cityPicker = rememberCityPickerState()
            val initialData = remember(currentValue) { parseCityString(currentValue) }
            LaunchedEffect(Unit) {
                cityPicker.show(
                    title = title.ifBlank { null },
                    initData = initialData,
                    initLayer = 3,
                    onCancel = { cityPicker.hide() },
                ) { selected ->
                    cityPicker.hide()
                    onPicked(formatCity(selected))
                }
            }
        }
    }
}

internal fun parseTimeString(raw: String?): Pair<Int, Int> {
    val now = LocalTime.now()
    if (raw.isNullOrBlank()) return now.hour to now.minute
    val pattern = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    return runCatching {
        val t = LocalTime.parse(raw.trim(), pattern)
        t.hour to t.minute
    }.getOrElse { now.hour to now.minute }
}

internal fun parseTimeRangeString(raw: String?): Pair<Pair<Int, Int>, Pair<Int, Int>> {
    val now = LocalTime.now()
    val fallback = (now.hour to now.minute) to (now.plusHours(2).hour to now.plusHours(2).minute)
    if (raw.isNullOrBlank()) return fallback
    val parts = raw.split("~", "～").map { it.trim() }
    if (parts.size != 2) return fallback
    val start = parseTimeString(parts[0])
    val end = parseTimeString(parts[1])
    return start to end
}

internal fun formatTime(hour: Int, minute: Int): String =
    "%02d:%02d".format(hour.coerceIn(0, 23), minute.coerceIn(0, 59))

internal fun parseDateString(raw: String?): Long? {
    if (raw.isNullOrBlank()) return null
    val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
    return runCatching {
        val d = LocalDate.parse(raw.trim(), pattern)
        d.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }.getOrNull()
}

internal fun parseDateRangeString(raw: String?): Pair<Long?, Long?> {
    if (raw.isNullOrBlank()) return null to null
    val parts = raw.split("~", "～").map { it.trim() }
    return if (parts.size == 2) {
        parseDateString(parts[0]) to parseDateString(parts[1])
    } else {
        null to null
    }
}

internal fun formatDateMillis(millis: Long): String {
    val d = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    return d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault()))
}

internal fun parseHexColor(raw: String?): Color? {
    val value = raw?.trim().orEmpty()
    if (value.isEmpty()) return null
    return runCatching {
        val normalized = if (value.startsWith("#")) value else "#$value"
        Color(android.graphics.Color.parseColor(normalized))
    }.getOrNull()
}

internal fun currentColorHasAlpha(raw: String?): Boolean {
    if (raw.isNullOrBlank()) return true
    val trimmed = raw.trim().removePrefix("#")
    return trimmed.length == 8
}

internal fun Color.toHex(includeAlpha: Boolean): String {
    val argb = toArgb()
    return if (includeAlpha) {
        String.format("#%08X", argb)
    } else {
        String.format("#%06X", argb and 0x00FFFFFF)
    }
}

internal fun formatCity(selected: SelectedCountryData): String =
    listOfNotNull(selected.province, selected.city, selected.district)
        .filter { it.isNotBlank() }
        .joinToString(" ")

internal fun parseCityString(raw: String?): SelectedCountryData? {
    if (raw.isNullOrBlank()) return null
    val parts = raw.split(" ").map { it.trim() }.filter { it.isNotEmpty() }
    if (parts.isEmpty()) return null
    return SelectedCountryData(
        province = parts.getOrNull(0),
        city = parts.getOrNull(1),
        district = parts.getOrNull(2),
    )
}