package com.shifenmiao.ai.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.shifenmiao.ai.logic.ChatInputComponent
import com.shifenmiao.core.R
import com.shifenmiao.model.ai.ChatInputEventHandler
import com.shifenmiao.model.state.ChatUIState
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.ArrowUpward
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.DeleteSweep
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStopCircle

/**
 * 全屏写作态：用 ModalBottomSheet 占满屏幕，提供更舒适的长文撰写体验。
 * 内容直接绑定到 ChatInputComponent 的输入文本，与主输入框双向同步；
 * 关闭后保留输入内容，由 [ChatInputComponent.toggleExpand] 控制可见性。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenChatInputSheet(
    chatInputComponent: ChatInputComponent,
    chatUIState: ChatUIState,
    eventHandler: ChatInputEventHandler,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    // Sheet 完全展开后再抢 IME：此时 sheet 的 window 已建立，IME 可无缝 handoff，
    // 避免先隐藏再重新弹出造成的闪动。
    LaunchedEffect(sheetState.currentValue) {
        if (sheetState.currentValue == SheetValue.Expanded) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    // 统一的关闭入口：先让 sheet 播完自身的下滑动画，再从父级移除组合，
    // 这样关闭时能看到明显的滑下 + 内容淡出效果。
    val dismiss: () -> Unit = {
        scope.launch {
            sheetState.hide()
            chatInputComponent.toggleExpand(false)
        }
    }

    ModalBottomSheet(
        onDismissRequest = { chatInputComponent.toggleExpand(false) },
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = null,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        FullScreenContent(
            chatInputComponent = chatInputComponent,
            chatUIState = chatUIState,
            eventHandler = eventHandler,
            focusRequester = focusRequester,
            // 不主动 hide 键盘——让 IME 自然跟随下一个获取焦点的输入框，
            // 保证关闭时键盘不会先闪一下。
            onClose = dismiss,
            onSendAndClose = {
                eventHandler.sendMessage()
                dismiss()
            }
        )
    }
}

@Composable
private fun FullScreenContent(
    chatInputComponent: ChatInputComponent,
    chatUIState: ChatUIState,
    eventHandler: ChatInputEventHandler,
    focusRequester: FocusRequester,
    onClose: () -> Unit,
    onSendAndClose: () -> Unit,
) {
    val inputState = chatInputComponent.chatInputState.collectAsState()
    val text = inputState.value.inputText
    val charCount = text.length
    val isLoading = chatUIState.chatActive

    // 与 inline 输入框共享光标位置：进入时即沿用 component 中保留的 selection；
    // 用户在全屏中移动光标也会回写到 component，下次切回 inline 时位置一致。
    val externalSelection = TextRange(
        inputState.value.cursorStart.coerceIn(0, text.length),
        inputState.value.cursorEnd.coerceIn(0, text.length),
    )
    var fieldValue by remember {
        mutableStateOf(TextFieldValue(text = text, selection = externalSelection))
    }
    LaunchedEffect(text, externalSelection) {
        if (text != fieldValue.text || externalSelection != fieldValue.selection) {
            fieldValue = TextFieldValue(text = text, selection = externalSelection)
        }
    }

    // 内容延迟淡入：让 sheet 先滑到位再显示文字与按钮，
    // 避免「输入框被放大」的视觉错觉
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { contentVisible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
    ) {
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 220, delayMillis = 120))
                    + slideInVertically(
                        animationSpec = tween(durationMillis = 240, delayMillis = 120),
                        initialOffsetY = { it / 18 }
                    ),
            exit = fadeOut(animationSpec = tween(durationMillis = 120))
                    + slideOutVertically(
                        animationSpec = tween(durationMillis = 120),
                        targetOffsetY = { it / 18 }
                    ),
        ) {
            // 顶部栏：仅右上角关闭按钮，更接近"全屏写作"页面的常见交互
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 中部：编辑器占满剩余空间。
        // 注意：这里**不**用 AnimatedVisibility 包裹——否则 BasicTextField 会
        // 延迟挂载，IME 会先失去目标再重新连接，产生闪动。
        val scrollState = rememberScrollState()
        val textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            BasicTextField(
                value = fieldValue,
                onValueChange = { newValue ->
                    fieldValue = newValue
                    chatInputComponent.onInputValueChange(
                        newValue.text,
                        newValue.selection.start,
                        newValue.selection.end,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                textStyle = textStyle,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    if (fieldValue.text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.create_ai_agent_expand),
                            style = textStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    ProvideTextStyle(value = textStyle) { innerTextField() }
                }
            )
        }

        // 底部操作栏：左侧字数统计 + 右侧（清空 / 发送）
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 220, delayMillis = 200))
                    + slideInVertically(
                        animationSpec = tween(durationMillis = 240, delayMillis = 200),
                        initialOffsetY = { it / 4 }
                    ),
            exit = fadeOut(animationSpec = tween(durationMillis = 100)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (charCount > 0) charCount.toString() else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f)
                )
                if (charCount >= MIN_CHARS_FOR_CLEAR && !isLoading) {
                    IconButton(onClick = { chatInputComponent.clearInputText() }) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.DeleteSweep,
                            contentDescription = stringResource(R.string.clear),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                FullScreenSendButton(
                    isLoading = isLoading,
                    textEmpty = text.isEmpty()
                            && inputState.value.attachedMedia.isEmpty(),
                    onSend = onSendAndClose,
                    onCancel = eventHandler.cancelFetch,
                )
            }
        }
    }
}

@Composable
private fun FullScreenSendButton(
    isLoading: Boolean,
    textEmpty: Boolean,
    onSend: () -> Unit,
    onCancel: () -> Unit,
) {
    val size = 44.dp
    val iconSize = 26.dp
    val activeContainerColor = lerp(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.primaryContainer,
        0.26f
    )
    if (isLoading) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .clickable(onClick = onCancel),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStopCircle,
                contentDescription = stringResource(R.string.ai_duel_stop),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(iconSize)
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    if (textEmpty) MaterialTheme.colorScheme.surfaceContainerHighest
                    else activeContainerColor
                )
                .clickable(enabled = !textEmpty, onClick = onSend),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.ArrowUpward,
                contentDescription = stringResource(R.string.ai_chat_send),
                tint = if (textEmpty) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                else MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

internal const val MIN_CHARS_FOR_CLEAR = 20





