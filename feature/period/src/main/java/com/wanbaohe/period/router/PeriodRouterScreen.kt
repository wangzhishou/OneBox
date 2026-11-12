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

package com.wanbaohe.period.router

import androidx.compose.runtime.Composable
import com.wanbaohe.period.router.screenLogic.PeriodRouterComponent
import com.wanbaohe.period.screen.PeriodDetailScreen
import com.wanbaohe.period.screen.PeriodEditorScreen
import com.wanbaohe.period.screen.PeriodMainScreen

@Composable
fun PeriodRouterScreen(component: PeriodRouterComponent) {
    when (val child = component.child) {
        is PeriodRouterComponent.PeriodChild.Main -> PeriodMainScreen(
            component = child.component,
        )
        is PeriodRouterComponent.PeriodChild.Editor -> PeriodEditorScreen(
            component = child.component,
        )
        is PeriodRouterComponent.PeriodChild.Detail -> PeriodDetailScreen(
            component = child.component,
        )
    }
}
