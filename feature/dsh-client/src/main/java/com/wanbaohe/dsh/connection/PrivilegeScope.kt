package com.wanbaohe.dsh.connection

import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * 特权面可见性(DSH-PROTOCOL §6 信任围栏,对齐 Flutter connect_config.dart)。
 *
 * - [Loopback]:桌面同机(127.0.0.1/localhost/::1),全方法可用
 * - [Lan]:LAN 直连 —— settings.* / credentials.* / host.pickDirectory / openPath /
 *   agentPreset 写面被围栏 403,客户端只做 UI 隐藏(可达性策略,不是认证)
 * - [AuthenticatedRemote]:经 dsh-gateway 鉴权的远程(隧道 + Host 改写后 dsh 视为
 *   loopback,特权方法实际可用);P6 起由持令牌的网关连接进入
 */
enum class PrivilegeScope {
    Loopback,
    Lan,
    AuthenticatedRemote;

    /** 特权方法 UI(settings/credentials/llm 配置面)是否可见 */
    val showPrivilegedPanels: Boolean get() = this != Lan

    companion object {
        /** 从连接目标推断 loopback;[authenticatedRemote] = 网关令牌形态(P6) */
        fun of(baseUri: String, authenticatedRemote: Boolean = false): PrivilegeScope {
            if (authenticatedRemote) return AuthenticatedRemote
            val host = runCatching { baseUri.toHttpUrl().host.lowercase() }.getOrDefault("")
            return when (host) {
                "127.0.0.1", "localhost", "::1", "[::1]" -> Loopback
                else -> Lan
            }
        }
    }
}
