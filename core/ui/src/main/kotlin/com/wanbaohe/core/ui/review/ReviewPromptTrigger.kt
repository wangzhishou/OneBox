package com.wanbaohe.core.ui.review

/**
 * 触发应用内评分弹层的「成功时刻」场景。
 *
 * 只用于日志排查(看得出是哪类时刻把用户推到评分层),门控对所有场景一视同仁:
 * 累计次数门槛、冷却时间、每安装上限统一由 InAppReviewPrompt 维护。
 * 新增场景时优先选"用户刚拿到价值、情绪为正"的瞬间,而不是刚被扣积分/刚看到报错的瞬间。
 */
enum class ReviewPromptTrigger {

    /** 文件保存成功(com.wanbaohe.core.ui.utils.helper.AppToastHost.showFileSuccessToast) */
    SAVE_SUCCESS,

    /** 压缩包制作 / 解压成功(AppToastHost.showFileSuccessToast 由压缩包页面传入) */
    ARCHIVE_SUCCESS,

    /** 网络测速跑完并落库(SpeedTestComponent) */
    SPEED_TEST_SUCCESS,

    /** 决策转盘停稳、结果已出(DecisionWheelSpinComponent.onSpinSettled) */
    DECISION_WHEEL_SUCCESS,

    /** AI 一轮问答成功落库(MessagePersistenceWorker.onChatCompletionEnd) */
    AI_ANSWER_SUCCESS,

    /** 小游戏通关:2048 拼出 2048、扫雷清盘、数独完成、30 秒生存通关 */
    GAME_WIN,
}
