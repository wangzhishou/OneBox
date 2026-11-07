package com.wanbaohe.adwatch.component

data class AdWatchUiState(
    /** 单次观看奖励积分(来自远程配置, 默认 15) */
    val rewardPoints: Int = 15,
    /** 今日已观看次数 */
    val watchedToday: Int = 0,
    /** 今日剩余次数 */
    val remainingToday: Int = 10,
    /** 已看完、尚未发到账上的积分(未登录时先攒着, 登录或注册成功再发) */
    val unclaimedPoints: Int = 0,
    /** 是否正在展示广告(禁用按钮防重复点击) */
    val isShowing: Boolean = false,
)
