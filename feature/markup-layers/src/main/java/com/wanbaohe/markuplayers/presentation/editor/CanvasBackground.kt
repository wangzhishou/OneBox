package com.wanbaohe.markuplayers.presentation.editor

/**
 * 操作台(画布)背景:棋盘格(默认)或纯色。
 * 仅影响编辑器画布显示,不参与导出;会话级状态,不持久化。
 */
sealed interface CanvasBackground {

    data object Checkerboard : CanvasBackground

    data class Solid(val color: Int) : CanvasBackground

}
