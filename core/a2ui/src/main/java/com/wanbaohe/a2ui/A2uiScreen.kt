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

package com.wanbaohe.a2ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shifenmiao.core.R
import com.wanbaohe.core.ui.widget.enhanced.EnhancedIconButton
import com.wanbaohe.core.ui.widget.glass.GlassTopAppBar
import com.wanbaohe.core.ui.widget.glass.MeshGradientBackground
import com.wanbaohe.a2ui.ui.A2uiSurfaceView
import com.wanbaohe.a2ui.viewModel.A2uiComponent
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack

@Composable
fun A2uiScreen(
    component: A2uiComponent,
) {
    DisposableEffect(Unit) {
        component.connect()
        onDispose {
            component.disconnect()
        }
    }

    MeshGradientBackground {
        Scaffold(
            topBar = {
                GlassTopAppBar(
                    title = { Text(stringResource(R.string.a2ui_screen)) },
                    navigationIcon = {
                        EnhancedIconButton(onClick = { component.onGoBack() }) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.back_button),
                            )
                        }
                    },
                )
            },
        ) { padding ->
            A2uiSurfaceView(
                surfaceId = component.surfaceId,
                viewerContext = component.viewerContext,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding).imePadding(),
            )
        }
    }
}
