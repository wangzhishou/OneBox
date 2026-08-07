package com.wanbaohe.habittracker.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBarChart
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCheckCircleOutline
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavItem
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.habittracker.R
import com.wanbaohe.habittracker.component.HabitTrackerComponent
import com.wanbaohe.habittracker.model.HabitTab
import com.wanbaohe.habittracker.screen.tab.CheckInTab
import com.wanbaohe.habittracker.screen.tab.StatsTab

// ─────────────────────────────────────────────────────────────────────────────
// 习惯打卡主页(打卡 / 数据 双 tab)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HabitMainScreen(component: HabitTrackerComponent) {
    val uiState by component.uiState.collectAsState()

    BaseScreen(
        title = {
            Text(
                text = stringResource(
                    when (uiState.currentTab) {
                        HabitTab.CHECKIN -> R.string.habit_title
                        HabitTab.STATS -> R.string.habit_tab_stats
                    }
                )
            )
        },
        onGoBack = component.onGoBack,
        actions = {
            IconButton(onClick = component::navigateToAddHabit) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.habit_add),
                )
            }
        },
        supportGlassEffect = true,
        content = {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    when (uiState.currentTab) {
                        HabitTab.CHECKIN -> CheckInTab(
                            component = component,
                            modifier = Modifier.fillMaxSize(),
                        )

                        HabitTab.STATS -> StatsTab(
                            component = component,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                BottomTabs(
                    current = uiState.currentTab,
                    onSelect = component::toggleTab,
                )
            }
        },
        showNavigationBarsPadding = false,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// 底部导航栏(打卡 / 数据)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BottomTabs(
    current: HabitTab,
    onSelect: (HabitTab) -> Unit,
) {
    val tabs = listOf(
        HabitTab.CHECKIN to BottomNavItem(
            id = HabitTab.CHECKIN.name,
            label = stringResource(R.string.habit_tab_checkin),
            icon = Icons.Outlined.LineCheckCircleOutline,
            contentDescription = stringResource(R.string.habit_tab_checkin),
        ),
        HabitTab.STATS to BottomNavItem(
            id = HabitTab.STATS.name,
            label = stringResource(R.string.habit_tab_stats),
            icon = Icons.Outlined.LineBarChart,
            contentDescription = stringResource(R.string.habit_tab_stats),
        ),
    )

    BottomNavigationBar(
        items = tabs.map { it.second },
        selectedItemId = current.name,
        onItemClick = { item ->
            tabs.firstOrNull { it.second.id == item.id }?.first?.let(onSelect)
        },
        modifier = Modifier.fillMaxWidth(),
        showBar = true,
    )
}
