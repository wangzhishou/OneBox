package com.shifenmiao.base.pullrefresh

data class PullToRefreshUIState (
    val type: PullToRefreshUIStateType = PullToRefreshUIStateType.IDLE,
    val message: String = ""
)

enum class PullToRefreshUIStateType(val value: Int) {
    IDLE(1),
    LOADING(2),
    ERROR(3),
    SUCCESS(4);

    companion object {
        fun fromInt(value: Int): PullToRefreshUIStateType? {
            return entries.firstOrNull { it.value == value }
        }
    }
}