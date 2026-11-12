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

package com.wanbaohe.app.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.wanbaohe.base.ui.modifier.onGloballyPositionedDebounced
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.robot.RobotController
import com.wanbaohe.core.ui.utils.navigation.Screen

@Composable
fun MainLayout(
    appComponent: AppComponent,
    scrollBehavior: TopAppBarScrollBehavior,
    topAppBarState: TopAppBarState,
    content: @Composable (ColumnScope.() -> Unit),
) {
    val localDensity = LocalDensity.current
    val statusBarTopPx = with(localDensity) {
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding().toPx()
    }

    Column(
        Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
    ) {
        AppLargeTopAppBar(
            scrollBehavior = scrollBehavior,
            appComponent = appComponent,
            modifier = Modifier.onGloballyPositionedDebounced { coordinates ->
                RobotController.updateRobotPosition(
                    coordinates = coordinates,
                    localDensity = localDensity,
                    collapsedFraction = topAppBarState.collapsedFraction,
                    topOffsetPx = statusBarTopPx,
                    appComponent = appComponent,
                    screenName = Screen.NewApp().simpleName,
                    currentScreen = Screen.NewApp().simpleName
                )
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

