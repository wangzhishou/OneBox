package com.shifenmiao.network.api

/**
 * 服务端 ID 的类型化包装。避免把本地表主键误传到 API。
 *
 * 使用 [of] 安全构造：null/非正数都返回 null，由调用方决定 fallback 策略。
 */
@JvmInline
value class RemoteId(val value: Int) {
    init {
        require(value > 0) { "RemoteId must be positive, got $value" }
    }

    companion object {
        fun of(value: Int?): RemoteId? = value?.takeIf { it > 0 }?.let(::RemoteId)
    }
}