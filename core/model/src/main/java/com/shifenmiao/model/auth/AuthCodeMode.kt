package com.shifenmiao.model.auth

/**
 * 授权码锁屏模式。
 *
 * - [Unlock] 已存在授权码,验证后进入
 * - [Setup] 首次使用,需要设置并二次确认
 */
enum class AuthCodeMode {
    Unlock,
    Setup,
}
