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

package com.wanbaohe.base.ui.button

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.wanbaohe.base.ui.utils.Animation


@Composable
fun ExtendedFloatingActionButton(
    text: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    shape: Shape = FloatingActionButtonDefaults.extendedFabShape,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    interactionSource: MutableInteractionSource? = null,
) {
    val startPadding = if (expanded) 8.dp else 0.dp
    val endPadding = if (expanded) 8.dp else 0.dp
    Row(
        modifier = modifier
            .clip(shape)
            .defaultMinSize(
                minWidth = 32.dp,
                minHeight = 32.dp,
            )
            .background(
                color = containerColor,
                shape = shape
            )
            .clickable {
                onClick.invoke()
            }
            .padding(start = startPadding, end = endPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (expanded) Arrangement.Start else Arrangement.Center
    ) {
        icon()
        AnimatedVisibility(
            visible = expanded,
            enter = Animation.extendedFabExpandAnimation(),
            exit = Animation.extendedFabCollapseAnimation(),
        ) {
            Row(Modifier.clearAndSetSemantics {}) {
                Spacer(Modifier.width(4.dp))
                text()
            }
        }
    }
}

@Composable
fun ExtendedFloatingActionVerticalTextButton(
    text: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    shape: Shape = FloatingActionButtonDefaults.extendedFabShape,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    interactionSource: MutableInteractionSource? = null,
) {
    val startPadding = if (expanded) 12.dp else 0.dp
    val endPadding = if (expanded) 12.dp else 0.dp
    Column(
        modifier = modifier
            .clip(shape)
            .defaultMinSize(
                minWidth = 32.dp,
                minHeight = 32.dp,
            )
            .background(
                color = containerColor,
                shape = shape
            )
            .clickable {
                onClick.invoke()
            }
            .padding(top = startPadding, bottom = endPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon()
        AnimatedVisibility(
            visible = expanded,
            enter = Animation.extendedFabExpandExpandVerticallyAnimation(),
            exit = Animation.extendedFabCollapseExpandVerticallyAnimation(),
        ) {
            Column(Modifier.clearAndSetSemantics {}) {
                Spacer(Modifier.height(4.dp))
                text()
            }
        }
    }
}