package com.shifenmiao.core.constants

/**
 * Different type of navigation supported by app depending on device size and state.
 * @see https://developer.android.com/codelabs/add-adaptive-layouts?hl=zh-cn#6
 */
enum class NavigationType {
    NAVIGATION_BOTTOM, NAVIGATION_RAIL, NAVIGATION_DRAWER
}

/**
 * Different position of navigation content inside Navigation Rail, Navigation Drawer depending on device size and state.
 */
enum class NavigationPosition {
    TOP, CENTER
}