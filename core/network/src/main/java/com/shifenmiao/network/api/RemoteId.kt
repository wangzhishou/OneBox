package com.shifenmiao.network.api

/**
 * 服务端资源定位符的类型化包装。避免把本地表主键误传到 API。
 *
 * 取值是 Strapi v5 documentId（稳定标识），空时降级为数字 id 的字符串形式
 * （服务端 `:id` 路由两种都兼容）。数字 id 在 draft-and-publish 重发后会漂移，
 * 仅作遗留 fallback。
 *
 * 使用 [of] 安全构造：documentId 空白且 remoteId null/非正数时返回 null，
 * 由调用方决定 fallback 策略。
 */
@JvmInline
value class RemoteId(val value: String) {
    init {
        require(value.isNotBlank()) { "RemoteId must not be blank" }
    }

    companion object {
        /** documentId 优先；为空时降级数字 remoteId。 */
        fun of(documentId: String?, remoteId: Int?): RemoteId? =
            documentId?.takeIf { it.isNotBlank() }?.let(::RemoteId)
                ?: remoteId?.takeIf { it > 0 }?.let { RemoteId(it.toString()) }
    }
}
