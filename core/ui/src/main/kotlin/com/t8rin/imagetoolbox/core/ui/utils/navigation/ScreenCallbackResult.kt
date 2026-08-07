package com.t8rin.imagetoolbox.core.ui.utils.navigation

/**
 * Screen 回调结果 - 通用的数据载体
 *
 * 所有 Screen 的回调都使用这个通用对象，
 * 包含常用的字段，按需使用。
 */
data class ScreenCallbackResult(
    val status: ScreenCallbackStatus = ScreenCallbackStatus.OPENED,
    val id: Int? = null,
    val idLong: Long? = null,
    val url: String? = null,
    val data: String? = null,
    val message: String? = null,
    val extra: Map<String, Any>? = null,
    val lifecycleTrace: List<ScreenLifecycleEvent> = emptyList()
) {
    val isTerminal: Boolean
        get() = status.isTerminal

    companion object {
        /** 创建只有 ID 的成功保存结果 */
        fun withId(id: Int) = saved(id = id)

        /** 创建只有 Long ID 的成功保存结果 */
        fun withId(id: Long) = saved(idLong = id)

        /** 页面已成功打开 */
        fun opened(
            id: Int? = null,
            idLong: Long? = null,
            url: String? = null,
            data: String? = null,
            message: String? = null,
            extra: Map<String, Any>? = null
        ) = ScreenCallbackResult(
            status = ScreenCallbackStatus.OPENED,
            id = id,
            idLong = idLong,
            url = url,
            data = data,
            message = message,
            extra = extra
        )

        /** 页面进入等待用户操作阶段 */
        fun pendingUserAction(
            id: Int? = null,
            idLong: Long? = null,
            message: String? = null,
            extra: Map<String, Any>? = null
        ) = ScreenCallbackResult(
            status = ScreenCallbackStatus.PENDING_USER_ACTION,
            id = id,
            idLong = idLong,
            message = message,
            extra = extra
        )

        /** 页面已成功保存 */
        fun saved(
            id: Int? = null,
            idLong: Long? = null,
            url: String? = null,
            data: String? = null,
            message: String? = null,
            extra: Map<String, Any>? = null
        ) = ScreenCallbackResult(
            status = ScreenCallbackStatus.SAVED,
            id = id,
            idLong = idLong,
            url = url,
            data = data,
            message = message,
            extra = extra
        )

        /** 页面失败 */
        fun failed(
            message: String,
            id: Int? = null,
            idLong: Long? = null,
            extra: Map<String, Any>? = null
        ) = ScreenCallbackResult(
            status = ScreenCallbackStatus.FAILED,
            id = id,
            idLong = idLong,
            message = message,
            extra = extra
        )

        /** 用户取消或关闭页面 */
        fun cancelled(
            message: String = "用户取消了操作",
            id: Int? = null,
            idLong: Long? = null,
            extra: Map<String, Any>? = null
        ) = ScreenCallbackResult(
            status = ScreenCallbackStatus.CANCELLED,
            id = id,
            idLong = idLong,
            message = message,
            extra = extra
        )

        /** 创建只有 URL 的打开结果 */
        fun withUrl(url: String) = opened(url = url)

        /** 创建只有数据的打开结果 */
        fun withData(data: String) = opened(data = data)

        /** 空结果（用户取消） */
        val EMPTY = cancelled()
    }
}

enum class ScreenCallbackStatus(val isTerminal: Boolean) {
    OPENED(true),
    SAVED(true),
    CANCELLED(true),
    FAILED(true),
    PENDING_USER_ACTION(false)
}

data class ScreenLifecycleEvent(
    val status: ScreenCallbackStatus,
    val timestamp: Long = System.currentTimeMillis(),
    val message: String? = null
)

/**
 * Screen 回调类型定义
 */
typealias ScreenCallback = (ScreenCallbackResult) -> Unit
