package com.shifenmiao.base.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import com.wanbaohe.core.ui.utils.helper.isShellPortraitOrientationAsState
import com.wanbaohe.core.ui.utils.navigation.Screen


object Navigation {

    /**
     * 快捷启动入口(显示在设置项分段行里);其余可选启动页进「更多」设置页。
     */
    private val quickStartEntryOptions: List<Screen>
        get() = listOf(
            Screen.NewApp(),
            Screen.AITabChatScreen(),
        )

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

    fun startEntryOptions(): List<Screen> {
        return quickStartEntryOptions
    }

    /**
     * 全部可设为启动页的 Screen:主入口 tab + 全部工具页(均为无参/默认参构造)。
     */
    fun startEntryCandidates(): List<Screen> {
        return (portraitTopLevelEntries + Screen.entries).distinctBy { it.id }
    }

    /**
     * 「更多」设置页的分组数据源:第一组主入口,后续复用工具分组及其标题资源。
     * typedEntries 里相邻组可能共用同一标题(如两个「编辑」组),按标题合并避免重复组头;
     * 已在主入口组出现的 Screen(如助手)从工具组里剔除,避免列表重复。
     */
    fun startEntryCandidateGroups(): List<Pair<Int, List<Screen>>> {
        val mainEntries = portraitTopLevelEntries
        val mainIds = mainEntries.mapTo(HashSet()) { it.id }
        val toolGroups = Screen.typedEntries
            .groupBy { it.title }
            .map { (title, groups) ->
                title to groups.flatMap { it.entries }.filterNot { it.id in mainIds }
            }
            .filter { it.second.isNotEmpty() }
        return listOf(com.shifenmiao.core.R.string.start_entry_main_entries to mainEntries) + toolGroups
    }

    /**
     * 按持久化的 Screen.id 解析启动页,找不到/未设置时回退首页。
     */
    fun resolveStartEntry(preferredScreenId: Int?): Screen {
        return preferredScreenId
            ?.let { screenId -> startEntryCandidates().firstOrNull { it.id == screenId } }
            ?: Screen.NewApp()
    }

    /**
     * 启动栈:主入口 tab 单页栈;自定义工具页在栈底垫首页,
     * 保证从启动页返回时回到首页而不是直接退出 App。
     */
    fun startEntryStack(preferredScreenId: Int?): List<Screen> {
        val entry = resolveStartEntry(preferredScreenId)
        val isTopLevel = (portraitTopLevelEntries + landscapeTopLevelEntries).any { it.id == entry.id }
        return if (isTopLevel) listOf(entry) else listOf(Screen.NewApp(), entry)
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
        return remember {
            startEntryOptions()
        }
    }


    @Composable
    fun getBottomBarHeight(): Dp {
        return WindowInsets.systemBars
            .asPaddingValues()
            .calculateBottomPadding()
    }

}

