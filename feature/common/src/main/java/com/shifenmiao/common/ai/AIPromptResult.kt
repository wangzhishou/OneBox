package com.shifenmiao.common.ai

data class AIPromptResult(
    val content: String,
    val isSuccess: Boolean,
    val errorMessage: String? = null,
    val engineName: String = "",
    val modelName: String = "",
)
