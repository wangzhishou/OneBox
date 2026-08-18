package com.wanbaohe.compass.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavItem
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.compass.component.CompassComponent
import com.wanbaohe.compass.ui.DialPages
import kotlinx.coroutines.launch
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWarning
import com.t8rin.imagetoolbox.core.resources.icons.Compass

/**
 * 电子罗盘主屏幕（ViewPager 多表盘架构）
 *
 * 布局（由上至下）：
 *   - 校准警告条（仅在精度不可靠时显示）
 *   - HorizontalPager：每页自含表盘与自己的底部面板（见 [DialPages] 注册表，
 *     默认第 0 页罗经盘；新增仪表盘在注册表加一行即可）
 *   - 底部表盘导航（复用 core/ui 的 BottomNavigationBar，与万年历同款：
 *     图标 + 文案，点 tab 切页，左右滑动后选中态跟随）
 *   - 传感器不可用时，整体替换为提示卡
 *
 * 性能要点：高频平滑角度经 [CompassComponent.heading] 直达各表盘绘制层，
 * 本屏幕只消费取整后的低频 [CompassComponent.uiState]，
 * 静止时传感器抖动不会引发重组。
 */
@Composable
fun CompassScreen(component: CompassComponent) {
    val state by component.uiState.collectAsState()
    val heading = component.heading.collectAsState()

    val pagerState = rememberPagerState(pageCount = { DialPages.size })
    val scope = rememberCoroutineScope()

    BaseScreen(
        title = stringResource(CoreR.string.compass),
        onGoBack = component.onGoBack,
        // 底部导航栏自行处理 navigationBars 内边距（与万年历一致）
        showNavigationBarsPadding = false,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── 校准提示横幅 ──────────────────────────────────────────
            AnimatedVisibility(
                visible = state.needsCalibration,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    CalibrationBanner(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (!state.isSensorAvailable) {
                // ── 传感器不可用提示 ──────────────────────────────────
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SensorUnavailableCard(modifier = Modifier.fillMaxWidth())
                }
            } else {
                // ── 表盘 Pager：每页自含表盘 + 底部面板（页面自带内边距） ──
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { page ->
                    DialPages[page].content(heading, state)
                }

                // ── 底部表盘导航（复用 core/ui 的 BottomNavigationBar，万年历同款） ──
                val tabItems = DialPages.mapIndexed { index, page ->
                    BottomNavItem(
                        id = index.toString(),
                        label = stringResource(page.titleRes),
                        icon = page.icon
                    )
                }
                BottomNavigationBar(
                    items = tabItems,
                    selectedItemId = pagerState.currentPage.toString(),
                    onItemClick = { item ->
                        item.id.toIntOrNull()?.let { index ->
                            scope.launch { pagerState.animateScrollToPage(index) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ─── 子组件 ───────────────────────────────────────────────────────────────────

/**
 * 校准提示横幅：显示"传感器精度低，请画 8 字形校准"
 */
@Composable
private fun CalibrationBanner(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWarning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(CoreR.string.compass_calibrate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

/**
 * 传感器不可用提示卡
 */
@Composable
private fun SensorUnavailableCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Compass,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(CoreR.string.compass_unavailable),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
