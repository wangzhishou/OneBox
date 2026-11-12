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

package com.wanbaohe.base.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import kotlin.math.roundToInt


@Composable
fun CustomBottomAppBar(
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    containerColor: Color = BottomAppBarDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = BottomAppBarDefaults.ContainerElevation,
    contentPadding: PaddingValues = BottomAppBarDefaults.ContentPadding,
    windowInsets: WindowInsets = BottomAppBarDefaults.windowInsets,
    scrollBehavior: BottomAppBarScrollBehavior? = null,
    content: @Composable RowScope.() -> Unit
) {
    DisableContainer(
        enabled = enabled
    ) {
        // Set up support for resizing the bottom app bar when vertically dragging the bar itself.
        val appBarDragModifier = Modifier

        // Compose a Surface with a Row content.
        // The height of the app bar is determined by subtracting the bar's height offset from the
        // app bar's defined constant height value (i.e. the ContainerHeight token).
        Surface(
            color = containerColor,
            contentColor = contentColor,
            tonalElevation = tonalElevation,
            modifier = modifier
                .layout { measurable, constraints ->

                    val placeable = measurable.measure(constraints)
                    val height = placeable.height + (scrollBehavior?.state?.heightOffset ?: 0f)
                    layout(placeable.width, height.roundToInt()) {
                        placeable.place(0, 0)
                    }
                }
                .then(appBarDragModifier)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(windowInsets)
                    .padding(contentPadding),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}