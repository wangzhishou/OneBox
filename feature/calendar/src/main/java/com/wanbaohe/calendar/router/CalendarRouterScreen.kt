package com.wanbaohe.calendar.router

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavItem
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.calendar.R
import com.wanbaohe.calendar.bazi.screenLogic.BaZiScreen
import com.wanbaohe.calendar.calendar_view.screenLogic.CalendarViewScreen
import com.wanbaohe.calendar.auspicious.screenLogic.AuspiciousScreen
import com.wanbaohe.calendar.convert.screenLogic.ConvertScreen
import com.wanbaohe.calendar.router.screenLogic.CalendarRouterComponent
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCalendar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMagic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineEventAvailable
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCompareArrows

@Composable
fun CalendarRouterScreen(component: CalendarRouterComponent) {
    val currentType by component.currentType.collectAsState()

    val titleText = when (currentType) {
        is Screen.Calendar.Type.CalendarView -> stringResource(R.string.calendar_tab)
        is Screen.Calendar.Type.BaZi -> stringResource(R.string.bazi_tab)
        is Screen.Calendar.Type.Auspicious -> stringResource(R.string.auspicious_day_tab)
        is Screen.Calendar.Type.Convert -> stringResource(R.string.convert_tab)
    }

    BaseScreen(
        title = {
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleLarge
            )
        },
        onGoBack = component.onGoBack,
        isShowDefaultActions = true,
        showNavigationBarsPadding = false,
        supportGlassEffect = true,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = currentType,
                    transitionSpec = {
                        val direction = if (targetState.ordinal() > initialState.ordinal()) 1 else -1
                        (fadeIn(animationSpec = tween(250)) +
                            slideInHorizontally(animationSpec = tween(300)) { it / 4 * direction })
                            .togetherWith(
                                fadeOut(animationSpec = tween(200)) +
                                    slideOutHorizontally(animationSpec = tween(300)) { -it / 4 * direction }
                            )
                    },
                    label = "calendar_tab_switch"
                ) { type ->
                    when (type) {
                        is Screen.Calendar.Type.CalendarView -> CalendarViewScreen(
                            component.calendarViewComponent
                        )
                        is Screen.Calendar.Type.BaZi -> BaZiScreen(component.baZiComponent)
                        is Screen.Calendar.Type.Auspicious -> AuspiciousScreen(
                            component.auspiciousComponent
                        )
                        is Screen.Calendar.Type.Convert -> ConvertScreen(component.convertComponent)
                    }
                }
            }

            CalendarBottomBar(
                currentType = currentType,
                onSwitch = component::switchTo
            )
        }
    }
}

@Composable
private fun CalendarBottomBar(
    currentType: Screen.Calendar.Type,
    onSwitch: (Screen.Calendar.Type) -> Unit,
) {
    val tabs = listOf(
        TabInfo(
            label = stringResource(R.string.calendar_tab),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCalendar,
            typeFactory = { Screen.Calendar.Type.CalendarView() }
        ),
        TabInfo(
            label = stringResource(R.string.bazi_tab),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMagic,
            typeFactory = { Screen.Calendar.Type.BaZi() }
        ),
        TabInfo(
            label = stringResource(R.string.auspicious_day_tab),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineEventAvailable,
            typeFactory = { Screen.Calendar.Type.Auspicious() }
        ),
        TabInfo(
            label = stringResource(R.string.convert_tab),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCompareArrows,
            typeFactory = { Screen.Calendar.Type.Convert() }
        ),
    )

    val selectedId = currentType.ordinal().toString()

    val items = tabs.mapIndexed { index, tab ->
        BottomNavItem(
            id = index.toString(),
            label = tab.label,
            icon = tab.icon,
            contentDescription = tab.label,
        )
    }

    BottomNavigationBar(
        items = items,
        selectedItemId = selectedId,
        onItemClick = { clicked ->
            val index = clicked.id.toIntOrNull() ?: return@BottomNavigationBar
            tabs.getOrNull(index)?.let { onSwitch(it.typeFactory()) }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

private data class TabInfo(
    val label: String,
    val icon: ImageVector,
    val typeFactory: () -> Screen.Calendar.Type,
)

/** 用于 AnimatedContent 判断滑动方向的顺序 */
private fun Screen.Calendar.Type.ordinal(): Int = when (this) {
    is Screen.Calendar.Type.CalendarView -> 0
    is Screen.Calendar.Type.BaZi -> 1
    is Screen.Calendar.Type.Auspicious -> 2
    is Screen.Calendar.Type.Convert -> 3
}
