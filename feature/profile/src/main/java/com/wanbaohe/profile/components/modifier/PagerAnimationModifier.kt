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

package com.wanbaohe.profile.components.modifier

import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue

/**
 * Create custom modifiers:
 * https://developer.android.com/jetpack/compose/custom-modifiers
 */
fun Modifier.pagerAnimation(
    pagerState: PagerState,
    thisPageIndex: Int,
): Modifier {
    val pageOffset =
        (pagerState.currentPage - thisPageIndex) + pagerState.currentPageOffsetFraction

    return this then Modifier.graphicsLayer {
        alpha =
            lerp(
                start = 0.4f,
                stop = 1f,
                fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f),
            )

        cameraDistance = 2 * density
        rotationY =
            lerp(
                start = 0f,
                stop = 40f,
                fraction = pageOffset.coerceIn(-1f, 1f),
            )

        lerp(
            start = 0.5f,
            stop = 1f,
            fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f),
        ).also { scale ->
            scaleX = if (pagerState.currentPage == thisPageIndex) {
                scale * 1.2f
            } else {
                scale
            }
            scaleY = if (pagerState.currentPage == thisPageIndex) {
                scale * 1.2f
            } else {
                scale
            }
        }
    }
}
