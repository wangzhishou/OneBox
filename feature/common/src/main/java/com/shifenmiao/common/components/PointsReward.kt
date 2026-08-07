package com.shifenmiao.common.components

import com.shifenmiao.common.utils.BaseUtils

fun interface PointsRewarder {
    fun reward(points: Int)
}

internal object DefaultPointsRewarder : PointsRewarder {
    override fun reward(points: Int) {
        BaseUtils.rewardPoints(
            points = points,
            desc = "赚积分小游戏奖励",
            source = "robot_game_survival_100s",
            bizId = "",
            showToast = true
        )
    }
}

fun rewardPoints(points: Int, rewarder: PointsRewarder = DefaultPointsRewarder) {
    rewarder.reward(points)
}
