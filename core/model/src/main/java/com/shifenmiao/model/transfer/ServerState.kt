package com.shifenmiao.model.transfer

/**
 * 服务器运行状态
 */
sealed class ServerState {
    /** 服务已停止 */
    data object Stopped : ServerState()

    /** 服务正在启动 */
    data object Starting : ServerState()

    /** 服务运行中 */
    data class Running(
        val port: Int,
        val address: String
    ) : ServerState()

    /** 服务出错 */
    data class Error(val message: String) : ServerState()
}

