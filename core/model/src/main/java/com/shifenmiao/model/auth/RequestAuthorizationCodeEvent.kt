package com.shifenmiao.model.auth

/**
 * 请求授权码解锁 / 设置。
 *
 * 由调用方通过 [com.shifenmiao.model.event.AppEventBus] 发出,被全局
 * [com.shifenmiao.base.auth.AuthorizationCodeStateHolder] 订阅处理。
 *
 * @param source 调用来源,用于日志 / 调试
 * @param onSuccess 解锁或设置成功时回调
 * @param onFailure 用户取消或验证失败时回调,参数为可空的错误信息
 */
class RequestAuthorizationCodeEvent(
    val source: String = "",
    val onSuccess: () -> Unit = {},
    val onFailure: (String?) -> Unit = {},
)
