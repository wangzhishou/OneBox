package com.wanbaohe.teleprompter.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave
import com.t8rin.imagetoolbox.core.resources.icons.PlayCircle
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.utils.getString
import com.wanbaohe.teleprompter.R
import com.wanbaohe.teleprompter.component.TeleprompterComponent
import com.wanbaohe.teleprompter.util.ScriptCountUnit
import com.wanbaohe.teleprompter.util.countScriptWords
import com.wanbaohe.teleprompter.util.scriptCountUnit
import com.wanbaohe.teleprompter.util.estimateReadingSeconds

/** 文稿正文的上下留白：比单行输入框更宽，长文贴着圆角会显得挤。 */
private val BodyHorizontalPadding = 20.dp
private val BodyVerticalPadding = 18.dp

/** 标题输入框的留白，左右与正文对齐。 */
private val TitleHorizontalPadding = 20.dp
private val TitleVerticalPadding = 16.dp

/**
 * 脚本编辑页 (Script Editor)
 *
 * 版式：标题输入 + 实时字数/时长 + 正文输入，三段之间留出呼吸空间。
 * 正文按可朗读的口径给足行高与内边距，写长稿时不会贴边。
 */
@Composable
fun TeleprompterEditorScreen(
    component: TeleprompterComponent,
) {
    val state by component.editorState.collectAsState()
    val focusManager = LocalFocusManager.current

    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    val wordCount = remember(state.content) { countScriptWords(state.content) }
    val countUnit = remember(state.content) { scriptCountUnit(state.content) }
    val readingSeconds = remember(state.content) { estimateReadingSeconds(state.content) }

    val onBack: () -> Unit = {
        if (state.isDirty) {
            showExitDialog = true
        } else {
            component.onEditorBack()
        }
    }

    BaseScreen(
        title = if (state.isNew)
            stringResource(R.string.teleprompter_new_script)
        else
            stringResource(R.string.teleprompter_edit_script),
        onGoBack = onBack,
        actions = {
            // 保存按钮
            IconButton(
                onClick = {
                    if (component.onSaveScript()) {
                        AppToastHost.showToast(
                            message = getString(R.string.teleprompter_saved),
                        )
                    } else {
                        AppToastHost.showToast(
                            message = getString(R.string.teleprompter_title_required),
                        )
                    }
                },
                colors = AppTheme.colors.iconButtonColors()
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave,
                    contentDescription = stringResource(R.string.teleprompter_save),
                )
            }

            Spacer(Modifier.width(4.dp))

            // 播放按钮
            IconButton(
                onClick = { component.onPlayFromEditor() },
                colors = AppTheme.colors.iconButtonColors()
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PlayCircle,
                    contentDescription = stringResource(R.string.teleprompter_play),
                )
            }

            Spacer(Modifier.width(8.dp))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            // 标题输入
            GlassOutlinedTextField(
                value = state.title,
                onValueChange = { component.onTitleChange(it) },
                placeholder = {
                    Text(
                        text = stringResource(R.string.teleprompter_title_hint),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                contentPadding = PaddingValues(
                    horizontal = TitleHorizontalPadding,
                    vertical = TitleVerticalPadding,
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            EditorStats(
                wordCount = wordCount,
                countUnit = countUnit,
                readingSeconds = readingSeconds,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
            )

            Spacer(Modifier.height(8.dp))

            // 正文输入
            GlassOutlinedTextField(
                value = state.content,
                onValueChange = { component.onContentChange(it) },
                placeholder = {
                    Text(
                        text = stringResource(R.string.teleprompter_content_hint),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge,
                contentPadding = PaddingValues(
                    horizontal = BodyHorizontalPadding,
                    vertical = BodyVerticalPadding,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(Modifier.height(12.dp))
        }
    }

    // 退出未保存提示
    ExitWithoutSavingDialog(
        onExit = { component.onEditorBack() },
        onDismiss = { showExitDialog = false },
        visible = showExitDialog,
    )
}

/**
 * 标题下方的实时统计：字数 + 按口语速度估算的朗读时长。
 * 放在标题和正文之间，既是信息也是两段之间的留白。
 */
@Composable
private fun EditorStats(
    wordCount: Int,
    countUnit: ScriptCountUnit,
    readingSeconds: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            // 英文稿按"词"、中文稿按"字"，单位跟着文字走
            text = pluralStringResource(
                if (countUnit == ScriptCountUnit.WORDS) {
                    R.plurals.teleprompter_word_count_latin
                } else {
                    R.plurals.teleprompter_word_count
                },
                wordCount,
                wordCount,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (readingSeconds > 0) {
            Spacer(Modifier.width(10.dp))
            Text(
                text = readingTimeLabel(readingSeconds),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun readingTimeLabel(seconds: Int): String {
    val minutes = seconds / 60
    val rest = seconds % 60
    return when {
        minutes == 0 -> stringResource(R.string.teleprompter_reading_time_seconds, seconds)
        rest == 0 -> stringResource(R.string.teleprompter_reading_time_minutes_exact, minutes)
        else -> stringResource(R.string.teleprompter_reading_time_minutes, minutes, rest)
    }
}
