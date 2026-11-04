package com.shifenmiao.common.search

import com.wanbaohe.core.ui.utils.navigation.Screen

sealed interface GlobalSearchDestination {
    data class Navigate(val screen: Screen) : GlobalSearchDestination

    data class OpenContent(val uri: String) : GlobalSearchDestination
}
