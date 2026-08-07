package com.wanbaohe.bookkeeping.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavItem
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.bookkeeping.R
import com.wanbaohe.bookkeeping.component.BookkeepingComponent
import com.wanbaohe.bookkeeping.model.BookkeepingTab
import com.wanbaohe.bookkeeping.screen.tab.DetailTab
import com.wanbaohe.bookkeeping.screen.tab.SettingsTab
import com.wanbaohe.bookkeeping.screen.tab.StatsTab
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePieChart
import com.t8rin.imagetoolbox.core.resources.icons.line.LineViewList

// ─────────────────────────────────────────────────────────────────────────────
// 记账本屏幕入口
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BookkeepingScreen(
    component: BookkeepingComponent,
    onGoBack: () -> Unit,
) {
    val uiState by component.uiState.collectAsState()

    BaseScreen(
        title = {
            Text(
                text = when (uiState.currentTab) {
                    BookkeepingTab.DETAIL -> stringResource(R.string.bookkeeping_title)
                    BookkeepingTab.STATS -> stringResource(R.string.bookkeeping_tab_stats)
                    BookkeepingTab.SETTINGS -> stringResource(R.string.bookkeeping_tab_settings)
                }
            )
        },
        onGoBack = onGoBack,
        actions = {
            IconButton(onClick = component::navigateToAddRecord) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.bookkeeping_add_record),
                )
            }
        },
        supportGlassEffect = true,
        content = {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    when (uiState.currentTab) {
                        BookkeepingTab.DETAIL -> DetailTab(
                            component = component,
                            modifier = Modifier.fillMaxSize()
                        )

                        BookkeepingTab.STATS -> StatsTab(
                            component = component,
                            modifier = Modifier.fillMaxSize()
                        )

                        BookkeepingTab.SETTINGS -> SettingsTab(
                            component = component,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                BottomTabs(current = uiState.currentTab, onSelect = component::switchTab)
            }
        },
        showNavigationBarsPadding = false,
    )
}


// ─────────────────────────────────────────────────────────────────────────────
// 底部导航栏
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BottomTabs(
    current: BookkeepingTab,
    onSelect: (BookkeepingTab) -> Unit,
) {
    val tabs = listOf(
        BookkeepingTab.DETAIL to BottomNavItem(
            id = BookkeepingTab.DETAIL.name,
            label = stringResource(R.string.bookkeeping_tab_detail),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineViewList,
            contentDescription = stringResource(R.string.bookkeeping_tab_detail),
        ),
        BookkeepingTab.STATS to BottomNavItem(
            id = BookkeepingTab.STATS.name,
            label = stringResource(R.string.bookkeeping_tab_stats),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePieChart,
            contentDescription = stringResource(R.string.bookkeeping_tab_stats),
        ),
        BookkeepingTab.SETTINGS to BottomNavItem(
            id = BookkeepingTab.SETTINGS.name,
            label = stringResource(R.string.bookkeeping_tab_settings),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettings,
            contentDescription = stringResource(R.string.bookkeeping_tab_settings),
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
