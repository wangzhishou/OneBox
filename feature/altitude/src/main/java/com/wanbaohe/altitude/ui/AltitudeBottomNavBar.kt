package com.wanbaohe.altitude.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavItem
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavigationBar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSpeedTest
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInsights

/**
 * 底部导航栏 — INSTRUMENT / HISTORY
 */
@Composable
internal fun AltitudeBottomNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    data class NavItem(val label: Int, val icon: ImageVector)

    val items = listOf(
        NavItem(CoreR.string.altitude_tab_instrument, com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSpeedTest),
        NavItem(CoreR.string.altitude_tab_history, com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInsights)
    )

    val tabs = items.mapIndexed { index, item ->
        BottomNavItem(
            id = index.toString(),
            label = stringResource(item.label),
            icon = item.icon,
            contentDescription = stringResource(item.label),
        )
    }

    BottomNavigationBar(
        items = tabs,
        selectedItemId = selectedTab.toString(),
        onItemClick = { clicked ->
            clicked.id.toIntOrNull()?.let(onTabSelected)
        },
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
    )
}
