package com.shifenmiao.ai.chat

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.shifenmiao.model.ai.ChatInputState
import com.shifenmiao.model.ai.Conversation
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground

/**
 * 聊天输入框：单行时使用 Row 横排（leading + 文本 + trailing send）；
 * 当文本实际折行 ≥ 2 时切换为 Column 布局：文本占满整行宽度，
 * 操作按钮统一收纳到底部 action bar，避免左右图标和文本混排错位。
 */
@Composable
fun NewTextInputField(
    conversation: Conversation,
    inputState: State<ChatInputState>,
    onValueChange: (text: String, selectionStart: Int, selectionEnd: Int) -> Unit = { _, _, _ -> },
    leadingIcon: @Composable (() -> Unit)? = null,
    /** 单行模式下显示在右侧的图标（一般是发送按钮） */
    singleLineTrailing: @Composable (() -> Unit)? = null,
    /** 多行模式下显示在底部的操作栏（leading 已位于左下角，这里只放右侧 actions） */
    multilineBottomActions: (@Composable () -> Unit)? = null,
) {
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val isInternalFocused by interactionSource.collectIsFocusedAsState()

    var lineCount by remember { mutableStateOf(1) }
    // 多行模式带迟滞（hysteresis）：一旦进入多行就保持，直到文本清空才回到单行。
    // 否则单/多行布局下文本可用宽度不同，会在临界处反复切换，产生抖动动画。
    var stickyMultiline by remember { mutableStateOf(false) }
    val isMultiline = stickyMultiline || lineCount >= 2

    LaunchedEffect(lineCount) {
        if (lineCount >= 2) stickyMultiline = true
    }
    // 文本被外部清空（清空按钮 / 发送）时，复位回单行
    LaunchedEffect(inputState.value.inputText.isEmpty()) {
        if (inputState.value.inputText.isEmpty()) stickyMultiline = false
    }

    // 用 TextFieldValue 同步 [ChatInputState] 中的文本与光标位置，
    // 与全屏写作态共享同一份选区，避免切换时光标位置错乱。
    val externalText = inputState.value.inputText
    val externalSelection = TextRange(
        inputState.value.cursorStart.coerceIn(0, externalText.length),
        inputState.value.cursorEnd.coerceIn(0, externalText.length),
    )
    var fieldValue by remember {
        mutableStateOf(TextFieldValue(text = externalText, selection = externalSelection))
    }
    LaunchedEffect(externalText, externalSelection) {
        if (externalText != fieldValue.text || externalSelection != fieldValue.selection) {
            fieldValue = TextFieldValue(text = externalText, selection = externalSelection)
        }
    }

    val shape = RoundedCornerShape(if (isMultiline) 24.dp else 28.dp)
    val backgroundColor = if (isInternalFocused) {
        MaterialTheme.colorScheme.surfaceContainerHighest
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val placeholderColor = lerp(
        MaterialTheme.colorScheme.onSurfaceVariant,
        MaterialTheme.colorScheme.primary,
        0.08f
    ).copy(alpha = 0.58f)
    val inputTextStyle = MaterialTheme.typography.bodyLarge.copy(
        color = MaterialTheme.colorScheme.onSurface
    )
    BasicTextField(
        value = fieldValue,
        onValueChange = { newValue ->
            fieldValue = newValue
            onValueChange(newValue.text, newValue.selection.start, newValue.selection.end)
        },
        modifier = Modifier
            .focusRequester(focusRequester)
            .fillMaxWidth(),
        textStyle = inputTextStyle,
        minLines = 1,
        maxLines = 6,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        interactionSource = interactionSource,
        onTextLayout = { result ->
            val newLineCount = result.lineCount.coerceAtLeast(1)
            if (newLineCount != lineCount) lineCount = newLineCount
        },
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassBackground(color = backgroundColor, shape = shape)
                    .animateContentSize()
                    .then(
                        if (isMultiline) Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        else Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    )
            ) {
                val showPlaceholder = fieldValue.text.isEmpty()
                        && conversation.placeholder.isNotEmpty()
                        && !isInternalFocused
                if (isMultiline) {
                    MultilineLayout(
                        innerTextField = innerTextField,
                        inputTextStyle = inputTextStyle,
                        leadingIcon = leadingIcon,
                        bottomActions = multilineBottomActions,
                        showPlaceholder = showPlaceholder,
                        placeholder = conversation.placeholder,
                        placeholderColor = placeholderColor
                    )
                } else {
                    SingleLineLayout(
                        innerTextField = innerTextField,
                        inputTextStyle = inputTextStyle,
                        leadingIcon = leadingIcon,
                        trailingIcon = singleLineTrailing,
                        showPlaceholder = showPlaceholder,
                        placeholder = conversation.placeholder,
                        placeholderColor = placeholderColor
                    )
                }
            }
        }
    )
}

@Composable
private fun SingleLineLayout(
    innerTextField: @Composable () -> Unit,
    inputTextStyle: TextStyle,
    leadingIcon: (@Composable () -> Unit)?,
    trailingIcon: (@Composable () -> Unit)?,
    showPlaceholder: Boolean,
    placeholder: String,
    placeholderColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingIcon?.let {
            Box(modifier = Modifier.padding(end = 4.dp)) { it() }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (showPlaceholder) {
                Text(text = placeholder, style = inputTextStyle, color = placeholderColor)
            }
            ProvideTextStyle(value = inputTextStyle) { innerTextField() }
        }
        trailingIcon?.let {
            Box(modifier = Modifier.padding(start = 4.dp)) { it() }
        }
    }
}

@Composable
private fun MultilineLayout(
    innerTextField: @Composable () -> Unit,
    inputTextStyle: TextStyle,
    leadingIcon: (@Composable () -> Unit)?,
    bottomActions: (@Composable () -> Unit)?,
    showPlaceholder: Boolean,
    placeholder: String,
    placeholderColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp, vertical = 8.dp),
            contentAlignment = Alignment.TopStart
        ) {
            if (showPlaceholder) {
                Text(text = placeholder, style = inputTextStyle, color = placeholderColor)
            }
            ProvideTextStyle(value = inputTextStyle) { innerTextField() }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box { leadingIcon?.invoke() ?: Spacer(modifier = Modifier) }
            Box { bottomActions?.invoke() }
        }
    }
}