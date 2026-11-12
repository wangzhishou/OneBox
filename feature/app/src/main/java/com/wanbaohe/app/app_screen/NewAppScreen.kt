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

package com.wanbaohe.app.app_screen

import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.model.HomeTabKey
import com.shifenmiao.online.component.ItemListComponent
import com.shifenmiao.online.component.PlaygroundComponent
import com.shifenmiao.online.screen.HomeContent
import com.t8rin.imagetoolbox.core.domain.performance.StartupTrace
import com.wanbaohe.app.components.MainLayout

@Composable
fun NewAppScreen(
    appComponent: AppComponent,
    itemListComponent: ItemListComponent,
    playgroundComponent: PlaygroundComponent,
    initialTab: HomeTabKey? = null,
) {
    val topAppBarState = rememberTopAppBarState()
    var initializedCollapsed by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        StartupTrace.markOnce("new_app_screen_composed", "NewAppScreen.composed")
    }

    LaunchedEffect(topAppBarState.heightOffsetLimit) {
        if (!initializedCollapsed && topAppBarState.heightOffsetLimit < 0f) {
            topAppBarState.heightOffset = topAppBarState.heightOffsetLimit
            initializedCollapsed = true
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)
    MainLayout(
        appComponent = appComponent,
        scrollBehavior = scrollBehavior,
        topAppBarState = topAppBarState
    ) {
        HomeContent(
            itemListComponent = itemListComponent,
            playgroundComponent = playgroundComponent,
            onAiCreate = appComponent::showAIChat,
            initialTab = initialTab,
        )
    }
}