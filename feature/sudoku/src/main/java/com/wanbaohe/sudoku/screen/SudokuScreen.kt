/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.sudoku.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBarChart
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGrid4x4
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings
import com.wanbaohe.core.ui.widget.navigation.BottomNavItem
import com.wanbaohe.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.sudoku.R
import com.wanbaohe.sudoku.component.SudokuComponent
import com.wanbaohe.sudoku.component.SudokuGamePhase
import com.wanbaohe.sudoku.component.SudokuTab

/**
 * 数独外壳: 内容区 + 常驻底部 tab(游戏/统计/设置)。
 *
 * tab 是横向状态切换, 不进返回栈 —— 全部状态躺在 [SudokuComponent] 里,
 * 对局中切去统计/设置再切回来, 棋盘、计时、选中格原样还在。
 */
@Composable
fun SudokuScreen(
    component: SudokuComponent
) {
    val state by component.uiState.collectAsState()

    val bottomInset = AppTheme.dimens.navigationHeight +
        WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()

    Box(modifier = Modifier.fillMaxSize()) {
        when (state.tab) {
            SudokuTab.GAME -> when (state.phase) {
                SudokuGamePhase.HOME -> SudokuHomeContent(
                    component = component,
                    bottomInset = bottomInset
                )

                SudokuGamePhase.PLAYING -> SudokuGameContent(
                    component = component,
                    bottomInset = bottomInset
                )

                SudokuGamePhase.COMPLETED -> SudokuCompleteContent(
                    component = component,
                    bottomInset = bottomInset
                )
            }

            SudokuTab.STATS -> SudokuStatsContent(
                component = component,
                bottomInset = bottomInset
            )

            SudokuTab.SETTINGS -> SudokuSettingsContent(
                component = component,
                bottomInset = bottomInset
            )
        }

        // BottomNavigationBar 内部把内容套在 AnimatedVisibility 里, align 要落在外层包裹 Box 上
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            SudokuBottomBar(
                current = state.tab,
                onSelect = component::switchTab
            )
        }
    }
}

@Composable
private fun SudokuBottomBar(
    current: SudokuTab,
    onSelect: (SudokuTab) -> Unit
) {
    val tabs = listOf(
        SudokuTab.GAME to BottomNavItem(
            id = SudokuTab.GAME.ordinal.toString(),
            label = stringResource(R.string.sudoku_tab_game),
            icon = Icons.Outlined.LineGrid4x4
        ),
        SudokuTab.STATS to BottomNavItem(
            id = SudokuTab.STATS.ordinal.toString(),
            label = stringResource(R.string.sudoku_tab_stats),
            icon = Icons.Outlined.LineBarChart
        ),
        SudokuTab.SETTINGS to BottomNavItem(
            id = SudokuTab.SETTINGS.ordinal.toString(),
            label = stringResource(R.string.sudoku_tab_settings),
            icon = Icons.Outlined.LineSettings
        )
    )

    BottomNavigationBar(
        items = tabs.map { it.second },
        selectedItemId = current.ordinal.toString(),
        onItemClick = { item ->
            val index = item.id.toIntOrNull() ?: return@BottomNavigationBar
            onSelect(tabs.getOrNull(index)?.first ?: return@BottomNavigationBar)
        },
        modifier = Modifier.fillMaxWidth()
    )
}
