package com.shifenmiao.common.utils

import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.core.R
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.event.AppEventBus
import com.shifenmiao.model.points.ConsumePoints
import com.shifenmiao.model.points.ConsumePointsEvent
import com.shifenmiao.model.points.RewardPoints
import com.shifenmiao.model.points.RewardPointsEvent
import com.shifenmiao.storage.AppSharedStorage
import com.shifenmiao.storage.TokenStorage
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.utils.getString
import kotlin.math.roundToInt

object BaseUtils {

    fun getNameByType(type: Int): String {
        return when (type) {
            ListItemType.PROMPT.id -> ListItemType.PROMPT.title
            ListItemType.HTML.id -> ListItemType.HTML.title
            ListItemType.WECHAT.id -> ListItemType.WECHAT.title
            ListItemType.VIDEO.id -> ListItemType.VIDEO.title
            ListItemType.AGENT.id -> ListItemType.AGENT.title
            ListItemType.BLOG.id -> ListItemType.BLOG.title
            ListItemType.NOTE.id -> ListItemType.NOTE.title
            else -> ListItemType.NORMAL.title
        }
    }


    fun getDisplayName(nickname: String, username: String): String {
        return nickname.ifEmpty {
            username
        }
    }

    fun canConsumePoints(inputText: String = ""): Boolean {
        TokenStorage.getLoginInfo()?.let { login ->
            StringUtils.calculateTokens(inputText).let { tokens ->
                return (login.user.points ?: 0) >= tokenToPoints(tokens) * 3
            }
        }
        return false
    }

    /**
     * 1000个tokens 价值0.2元钱，0.5元钱500个积分，大概1个tokens多少积分？
     * 1个token大概值0.2个积分。
     */
    fun tokenToPoints(token: Int): Int {
        return (token * AppSharedStorage.loadBasePoints()).roundToInt()
    }

    fun tokenToPoints(token: Int, degree: Float): Int {
        return (token * AppSharedStorage.loadBasePoints() * degree).roundToInt()
    }

    private fun showPointsToast(consumePoints: ConsumePoints) {
        if (!AppSharedStorage.loadIsShowPointsTips()) {
            return
        }
        AppToastHost.showToast(
            getString(R.string.consume_points, consumePoints.points)
        )
    }

    private fun showRewardToast(rewardPoints: RewardPoints) {

        AppToastHost.showToast(
            AppContext.getContext()
                .getString(R.string.reward_points, rewardPoints.points)
        )
    }

    fun consumePoints(
        degree: Int,
        desc: String = "",
        source: String = "",
        showToast: Boolean = false
    ) {
        val consumePoints = ConsumePoints().apply {
            this.points = degree
            this.desc = desc
            this.source = source
        }
        AppEventBus.emit(
            ConsumePointsEvent(consumePoints = consumePoints, onSuccess = {
                if (showToast) {
                    showPointsToast(consumePoints)
                }
            })
        )
    }

    fun consumePointsByToken(
        token: Int,
        conversation: Conversation,
        desc: String = "",
    ) {
        val consumePoints = ConsumePoints().apply {
            this.points = tokenToPoints(token, conversation.engine.model.basePoints)
            this.desc = desc
            this.source = conversation.engine.model.name
        }
        AppEventBus.emit(ConsumePointsEvent(consumePoints = consumePoints, onSuccess = {
            showPointsToast(consumePoints)
        }))
    }

    fun rewardPoints(
        points: Int,
        desc: String = "",
        source: String = "",
        bizId: String = "",
        showToast: Boolean = true
    ) {
        val rewardPoints = RewardPoints().apply {
            this.points = points
            this.desc = desc
            this.source = source
            this.bizId = bizId
        }
        AppEventBus.emit(
            RewardPointsEvent(
                rewardPoints = rewardPoints,
                onSuccess = {
                    if (showToast) {
                        showRewardToast(rewardPoints)
                    }
                },
                onFailure = {
                    AppToastHost.showFailureToast(it)
                }
            )
        )
    }

    fun isShowById(miniProgramId: Int): Boolean = true

    fun isHiddenId(id: Int): Boolean = false

    fun isShowByIdString(miniProgramId: String?): Boolean = true

}
