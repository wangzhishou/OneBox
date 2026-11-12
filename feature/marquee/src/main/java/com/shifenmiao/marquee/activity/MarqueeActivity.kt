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

package com.shifenmiao.marquee.activity

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.retainedComponent
import com.shifenmiao.marquee.screen.FullScreenSubtitles
import com.shifenmiao.marquee.screenLogic.MarqueeComponent
import com.t8rin.imagetoolbox.core.settings.presentation.model.toUiState
import com.wanbaohe.core.ui.utils.ComposeActivity
import com.wanbaohe.core.ui.utils.provider.ImageToolboxCompositionLocals
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MarqueeActivity : ComposeActivity() {

    @Inject
    lateinit var componentFactory: MarqueeComponent.Factory

    private val component: MarqueeComponent by lazy {
        retainedComponent { componentContext ->
            componentFactory(componentContext)
        }
    }

    @Composable
    override fun Content() {
        ImageToolboxCompositionLocals(
            settingsState = component.settingsState.toUiState(),
            settingsManager = component.settingsManager
        ) {
            FullScreenSubtitles(
                onGoBack = {
                    finish()
                },
                component = component
            )
        }

    }
}
