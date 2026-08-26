package com.wanbaohe.textcard.domain

/**
 * 上次自定义画布尺寸的持久化(Component 不直接碰 IO,经接口注入)。
 */
interface CustomCanvasStore {

    /** 上次输入的自定义宽高,null = 从未自定义过 */
    fun lastCustom(): Pair<Int, Int>?

    fun saveLastCustom(width: Int, height: Int)
}
