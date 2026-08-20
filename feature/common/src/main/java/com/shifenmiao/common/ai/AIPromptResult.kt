package com.shifenmiao.common.ai

data class AIPromptResult(
    val content: String,
    val isSuccess: Boolean,
    val errorMessage: String? = null,
    val engineName: String = "",
    val modelName: String = "",
    /** 接口返回的 totalTokens(无则 0),供调用方按量扣积分 */
    val totalTokens: Int = 0,
    /** 是否走自有代理路由(需登录/计积分);false 为 BYOK 直连,不应扣积分 */
    val isProxyRoute: Boolean = false,
)
