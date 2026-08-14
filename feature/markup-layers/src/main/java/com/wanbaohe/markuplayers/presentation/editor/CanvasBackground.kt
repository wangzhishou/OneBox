package com.wanbaohe.markuplayers.presentation.editor

/**
 * 操作台(画布)背景:默认(跟随主题 surface 背景色)或纯色。
 * 仅影响编辑器画布显示,不参与导出;会话级状态,不持久化。
 * 底图自身的透明区域仍由图片下方的透明棋盘格标识,与本背景无关。
 */
sealed interface CanvasBackground {

    data object Default : CanvasBackground

    data class Solid(val color: Int) : CanvasBackground

}
