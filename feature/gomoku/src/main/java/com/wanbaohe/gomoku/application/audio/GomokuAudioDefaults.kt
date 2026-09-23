package com.wanbaohe.gomoku.application.audio

/**
 * 内置音效的 CDN 地址。
 *
 * 音效不打进 APK（包体考虑），统一放 R2。一期没有五子棋专用音效文件，
 * 直接沿用象棋同款（`audio/xiangqi/`）：落子/终局提示/背景音乐听感与棋类一致。
 *
 * 用户在设置页填了自定义 URL 就用他的，没填就回落到这里的默认音。
 */
object GomokuAudioDefaults {
    private const val BASE = "https://images.oneboxable.com/audio/xiangqi"

    /** 落子 */
    const val MOVE = "$BASE/move.ogg"

    /** 终局（胜/负提示，沿用象棋绝杀音） */
    const val WIN = "$BASE/checkmate.ogg"

    /** 和棋 */
    const val DRAW = "$BASE/draw.ogg"

    /** 背景音乐（24s 无缝循环，五声音阶，很轻，不抢落子音） */
    const val BACKGROUND = "$BASE/bgm.ogg"

    /** 预热用：进页面先把这些下下来，第一次落子才不会有网络延迟 */
    val ALL: List<String> = listOf(MOVE, WIN, DRAW, BACKGROUND)
}
