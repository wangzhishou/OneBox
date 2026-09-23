package com.wanbaohe.gomoku.domain.model

/** 对局模式:本地双人 / 人机 / AI 对战 / 在线双人 */
enum class GameMode {
    LOCAL_PVP,
    HUMAN_VS_LLM,
    LLM_VS_LLM,
    ONLINE_PVP,
}
