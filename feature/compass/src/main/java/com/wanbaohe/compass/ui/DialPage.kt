package com.wanbaohe.compass.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.unit.dp
import com.wanbaohe.compass.component.CompassUiState
import com.wanbaohe.compass.ui.luopan.LuopanPage
import com.shifenmiao.core.R as CoreR

/** 表盘最大直径：在大屏（平板/折叠屏）上避免表盘过大失真 */
internal val DIAL_MAX_SIZE = 340.dp

/**
 * 一个仪表盘页面：tab 标题 + 页面内容。
 *
 * 页面自含表盘与自己的底部面板；heading（高频，只重绘）与
 * uiState（低频，驱动文本）由屏幕层统一注入，各页共享同一份传感器订阅。
 */
class DialPage(
    @StringRes val titleRes: Int,
    val content: @Composable (heading: State<Float>, uiState: CompassUiState) -> Unit
)

/**
 * 表盘注册表：顺序即 tab/页顺序，第 0 项为默认页。
 * 新增仪表盘：实现页面 Composable 并在此加一行即可。
 */
val DialPages: List<DialPage> = listOf(
    DialPage(CoreR.string.compass_tab_luopan) { heading, uiState ->
        LuopanPage(heading = heading, uiState = uiState)
    },
    DialPage(CoreR.string.compass_tab_compass) { heading, uiState ->
        ClassicCompassPage(heading = heading, uiState = uiState)
    }
)
