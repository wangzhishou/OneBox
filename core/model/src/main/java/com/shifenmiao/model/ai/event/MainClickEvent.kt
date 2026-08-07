package com.shifenmiao.model.ai.event

data class MainClickEvent(
    val from: MainClickEventFrom = MainClickEventFrom.HOME,
    val type:MainShowType = MainShowType.ROBOT,
)

enum class MainShowType {
    ROBOT,
    AI_SETTING,
    QUICK_DRAWER,
    BUY_COFFEE,
}

enum class MainClickEventFrom(val value: String) {
    HOME("HomeRobotClickEvent"),
    TOP_APP_BAR("TopAppBar"),
    AI_ACTION_MENU("AIActionMenu"),
    AI_START_CHAT("AIStartChat"),
    OCR_TASK_LIST("OcrTaskList"),
}