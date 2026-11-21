/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file has been modified from the original ImageToolbox source.
 *
 * Origin: ImageToolbox derivative, modified
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

package com.wanbaohe.core.ui.widget.other

import androidx.compose.animation.core.Easing
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.utils.animation.AlphaEasing
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

typealias RevealState = AnchoredDraggableState<RevealValue>

@Composable
fun SwipeToReveal(
    modifier: Modifier = Modifier,
    enableSwipe: Boolean = true,
    alphaEasing: Easing = AlphaEasing,
    maxRevealDp: Dp = 75.dp,
    directions: Set<RevealDirection> = setOf(
        RevealDirection.StartToEnd,
    ),
    alphaTransformEnabled: Boolean = false,
    interactionSource: MutableInteractionSource = remember {
        MutableInteractionSource()
    },
    state: RevealState = rememberRevealState(),
    revealedContentEnd: @Composable BoxScope.() -> Unit = {},
    revealedContentStart: @Composable BoxScope.() -> Unit = {},
    swipeableContent: @Composable () -> Unit,
) {
    Box(modifier) {
        val maxRevealPx = with(LocalDensity.current) { maxRevealDp.toPx() }
        val alphaModifier = if (alphaTransformEnabled && maxRevealPx > 0f) {
            Modifier.graphicsLayer {
                val ratio = (state.offset.offsetOrZero().absoluteValue / maxRevealPx).coerceIn(0f, 1f)
                alpha = alphaEasing.transform(ratio)
            }
        } else Modifier

        // non swipable with hidden content
        Box(
            modifier = Modifier.matchParentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .then(alphaModifier),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.CenterStart,
                    content = revealedContentStart
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.CenterEnd,
                    content = revealedContentEnd
                )
            }
        }

        Box(
            modifier = Modifier
                .then(
                    if (enableSwipe) {
                        Modifier
                            .offset {
                                IntOffset(
                                    state.offset.offsetOrZero().roundToInt(),
                                    0
                                )
                            }
                            .revealSwipeable(
                                state = state,
                                maxRevealPx = maxRevealPx,
                                directions = directions,
                                interactionSource = interactionSource
                            )
                    } else Modifier
                )
        ) {
            swipeableContent()
        }
    }
}


private fun Modifier.revealSwipeable(
    maxRevealPx: Float,
    directions: Set<RevealDirection>,
    state: RevealState,
    interactionSource: MutableInteractionSource
) = this.composed {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val anchors = remember(maxRevealPx, directions) {
        DraggableAnchors {
            RevealValue.Default at 0f
            if (maxRevealPx > 0f && RevealDirection.StartToEnd in directions) {
                RevealValue.FullyRevealedEnd at maxRevealPx
            }
            if (maxRevealPx > 0f && RevealDirection.EndToStart in directions) {
                RevealValue.FullyRevealedStart at -maxRevealPx
            }
        }
    }
    SideEffect {
        val target = state.targetValue.takeUnless { anchors.positionOf(it).isNaN() }
            ?: RevealValue.Default
        state.updateAnchors(anchors, target)
    }
    Modifier.anchoredDraggable(
        state = state,
        orientation = Orientation.Horizontal,
        reverseDirection = isRtl,
        flingBehavior = AnchoredDraggableDefaults.flingBehavior(
            state = state,
            positionalThreshold = { it * 0.5f },
        ),
        interactionSource = interactionSource
    )
}

private fun Float.offsetOrZero(): Float = if (isNaN()) 0f else this

enum class RevealDirection {
    /**
     * Can be dismissed by swiping in the reading direction.
     */
    StartToEnd,

    /**
     * Can be dismissed by swiping in the reverse of the reading direction.
     */
    EndToStart
}

/**
 * Possible values of [RevealState].
 */
enum class RevealValue {
    /**
     * Indicates the component has not been revealed yet.
     */
    Default,

    /**
     * Fully revealed to end
     */
    FullyRevealedEnd,

    /**
     * Fully revealed to start
     */
    FullyRevealedStart,
}

/**
 * Create and [remember] a [RevealState] with the default animation clock.
 *
 * @param initialValue The initial value of the state.
 */
@Composable
fun rememberRevealState(
    initialValue: RevealValue = RevealValue.Default,
): RevealState {
    return rememberSaveable(saver = AnchoredDraggableState.Saver()) {
        AnchoredDraggableState(initialValue)
    }
}

/**
 * Reset the component to the default position, with an animation.
 */
suspend fun RevealState.reset() {
    animateTo(
        targetValue = RevealValue.Default,
    )
}
