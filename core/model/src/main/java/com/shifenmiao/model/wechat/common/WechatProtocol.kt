package com.shifenmiao.model.wechat.common

/**
 * 微信 SDK 协议常量, 取值与 com.tencent.mm.opensdk.constants.ConstantsAPI /
 * com.tencent.mm.opensdk.modelbase.BaseResp.ErrCode 中的稳定协议值一致
 * (这些值由微信客户端协议定义, 不会随 SDK 版本变化)。
 *
 * 抽到 SDK-free 的 core/model, 是为了让 foss (F-Droid) 构建在不含微信 SDK 时
 * 也能编译 WechatEvent 的消费方 (LoginComponent / AppComponent / PayComponent)。
 */
object WechatProtocol {

    /** ConstantsAPI.COMMAND_SENDAUTH */
    const val COMMAND_SENDAUTH = 1

    /** ConstantsAPI.COMMAND_PAY_BY_WX */
    const val COMMAND_PAY_BY_WX = 5

    /** ConstantsAPI.COMMAND_LAUNCH_WX_MINIPROGRAM */
    const val COMMAND_LAUNCH_WX_MINIPROGRAM = 19

    /** BaseResp.ErrCode.ERR_OK (用户同意) */
    const val ERR_OK = 0

    /** BaseResp.ErrCode.ERR_USER_CANCEL (用户取消) */
    const val ERR_USER_CANCEL = -2

    /** BaseResp.ErrCode.ERR_AUTH_DENIED (用户拒绝授权) */
    const val ERR_AUTH_DENIED = -4
}
