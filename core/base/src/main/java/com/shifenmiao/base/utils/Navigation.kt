package com.shifenmiao.base.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import com.t8rin.imagetoolbox.core.ui.utils.helper.isShellPortraitOrientationAsState
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen


object Navigation {

    private val portraitStartEntryOptions: List<Screen>
        get() = Screen.startEntries

    private val portraitTopLevelEntries: List<Screen>
        get() = listOf(
            Screen.NewApp(),
            Screen.AITabChatScreen(),
            Screen.Online(),
            Screen.Profile(),
        )

    private val landscapeTopLevelEntries: List<Screen>
        get() = listOf(
            Screen.Search(),
            Screen.NewApp(),
            Screen.Online(),
            Screen.Profile(),
        )

    private val landscapeStartEntryOptions: List<Screen>
        get() = landscapeTopLevelEntries.filterNot { it.id == Screen.Profile().id }

    fun getTopLevelDestinationIndex(currentScreen: Screen?, tabEntries: List<Screen>): Int {
        return if (currentScreen != null) {
            tabEntries.indexOfFirst {
                it.id == currentScreen.id
            }
        } else {
            0
        }
    }

    fun isShowBottomBar(currentScreen: Screen?, tabEntries: List<Screen>): Boolean {
        return tabEntries.any {
            it.id == currentScreen?.id
        }
    }

    fun topLevelEntries(isPortrait: Boolean): List<Screen> {
        return if (isPortrait) portraitTopLevelEntries else landscapeTopLevelEntries
    }

    fun startEntryOptions(isPortrait: Boolean): List<Screen> {
        return if (isPortrait) portraitStartEntryOptions else landscapeStartEntryOptions
    }

    fun resolveStartEntry(
        preferredScreenId: Int?,
        legacyIndex: Int,
        isPortrait: Boolean,
    ): Screen {
        val entries = startEntryOptions(isPortrait)

        return preferredScreenId
            ?.let { screenId -> entries.firstOrNull { it.id == screenId } }
            ?: entries.getOrElse(legacyIndex) { entries.first() }
    }

    @Composable
    fun rememberTabEntries(): List<Screen> {
        val isPortrait by isShellPortraitOrientationAsState()

        return remember(isPortrait) {
            topLevelEntries(isPortrait)
        }
    }

    @Composable
    fun rememberStartEntryOptions(): List<Screen> {
        val isPortrait by isShellPortraitOrientationAsState()

        return remember(isPortrait) {
            startEntryOptions(isPortrait)
        }
    }


    @Composable
    fun getBottomBarHeight(): Dp {
        return WindowInsets.systemBars
            .asPaddingValues()
            .calculateBottomPadding()
    }

}

