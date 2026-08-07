package com.shifenmiao.model.ai

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

// 事件处理类
data class ChatInputEventHandler(
    val sendMessage: () -> Unit,
    val cancelFetch: () -> Unit,
    val toggleExpand: () -> Unit
)