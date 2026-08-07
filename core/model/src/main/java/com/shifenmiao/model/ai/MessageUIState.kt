package com.shifenmiao.model.ai

/**
 * 消息UI状态枚举
 */
enum class MessageUIState(val value: Int) {
    LOADING(-1),         // 输入后正在等待后台返回数据
    ERROR(-2),           // 错误消息
    STREAMING(0),        // 流式输出中的消息
    NORMAL(1);           // 正常保存的消息

    companion object {
        fun fromValue(value: Int): MessageUIState {
            return when(value) {
                LOADING.value -> LOADING
                ERROR.value -> ERROR
                STREAMING.value -> STREAMING
                else -> NORMAL
            }
        }
    }
}
