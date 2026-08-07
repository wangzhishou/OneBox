package com.wanbaohe.xiangqi.router

import androidx.compose.runtime.compositionLocalOf
import com.shifenmiao.common.ui.ImmersiveModeState

/**
 * 提供给象棋对局子屏幕的沉浸式模式状态。
 * Router 顶层创建,Game/Analysis Screen 可通过该 CompositionLocal toggle。
 */
val LocalXiangqiImmersiveModeState = compositionLocalOf<ImmersiveModeState?> { null }
