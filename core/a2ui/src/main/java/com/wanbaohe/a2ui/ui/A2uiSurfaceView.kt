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

package com.wanbaohe.a2ui.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.wanbaohe.core.ui.widget.glass.GlassCircularProgressIndicator

private val scrollableRootTypes = setOf("Column", "Card", "Row")

@Composable
fun A2uiSurfaceView(
    surfaceId: String,
    viewerContext: A2uiViewerContext,
    modifier: Modifier = Modifier,
) {
    val surfaceState = viewerContext.surfaceHolder.getOrCreate(surfaceId)
    val rootId = surfaceState.rootComponentId

    if (rootId == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            GlassCircularProgressIndicator()
        }
        return
    }

    val rootType = surfaceState.components[rootId]?.type
    val isScrollableRoot = rootType in scrollableRootTypes

    Box(
        modifier = if (isScrollableRoot) {
            modifier.verticalScroll(rememberScrollState())
        } else {
            modifier
        },
    ) {
        A2uiComponentTree(
            componentId = rootId,
            surfaceState = surfaceState,
            actionBus = viewerContext.actionBus,
            themeMapper = viewerContext.themeMapper,
            registry = viewerContext.registry,
        )
    }
}
