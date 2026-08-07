package com.shifenmiao.ai.agent.callback

/**
 * 回调结果包装类
 *
 * 统一的工具回调结果类型，支持成功和失败两种状态。
 *
 * @param data 成功时返回的数据，可以是任意类型
 * @param error 失败时的错误信息，为 null 表示成功
 */
data class CallbackResult(
    val data: Any? = null,
    val error: String? = null
) {
    /** 是否成功 */
    val isSuccess: Boolean get() = error == null

    /** 是否失败 */
    val isError: Boolean get() = error != null

    companion object {
        /** 创建成功结果 */
        fun success(data: Any?) = CallbackResult(data = data)

        /** 创建失败结果 */
        fun error(message: String) = CallbackResult(error = message)
    }
}
