package com.shifenmiao.model.points

import com.shifenmiao.model.user.Login
import retrofit2.Response

class RewardPointsEvent(
    var onSuccess: (str: Response<Login>) -> Unit = { _ -> },
    var onFailure: (str: String) -> Unit = { _ -> },
    var rewardPoints: RewardPoints = RewardPoints()
) {
}

