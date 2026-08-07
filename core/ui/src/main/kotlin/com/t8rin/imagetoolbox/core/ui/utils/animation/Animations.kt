/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
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

@file:Suppress("NOTHING_TO_INLINE")

package com.t8rin.imagetoolbox.core.ui.utils.animation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import com.arkivanov.decompose.ExperimentalDecomposeApi
import androidx.compose.runtime.remember
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimator
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.androidPredictiveBackAnimatableV1
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.essenty.backhandler.BackHandler
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen

fun fancySlideTransition(
    isForward: Boolean,
    screenWidthPx: Int,
    duration: Int = 600
): ContentTransform = if (isForward) {
    slideInHorizontally(
        animationSpec = tween(duration, easing = FancyTransitionEasing),
        initialOffsetX = { screenWidthPx }) + fadeIn(
        tween(300, 100)
    ) togetherWith slideOutHorizontally(
        animationSpec = tween(duration, easing = FancyTransitionEasing),
        targetOffsetX = { -screenWidthPx }) + fadeOut(
        tween(300, 100)
    )
} else {
    slideInHorizontally(
        animationSpec = tween(600, easing = FancyTransitionEasing),
        initialOffsetX = { -screenWidthPx }) + fadeIn(
        tween(300, 100)
    ) togetherWith slideOutHorizontally(
        animationSpec = tween(600, easing = FancyTransitionEasing),
        targetOffsetX = { screenWidthPx }) + fadeOut(
        tween(300, 100)
    )
}

/**
 * ImageViewer 使用缩放+淡入淡出动画（类似查看图片放大效果），
 * 其他页面使用默认的滑动+淡入淡出+缩放动画。
 */
private val imageViewerAnimator: StackAnimator =
    fade(tween(durationMillis = 250)) +
        scale(
            animationSpec = tween(durationMillis = 350, easing = FancyTransitionEasing),
            frontFactor = 0.6F,
            backFactor = 0.95F
        )

private val defaultAnimator: StackAnimator =
    fade(
        tween(
            durationMillis = 300,
            easing = AlphaEasing
        )
    ) + slide(
        tween(
            durationMillis = 400,
            easing = FancyTransitionEasing
        )
    ) + scale(
        tween(
            durationMillis = 500,
            easing = PointToPointEasing
        )
    )

@OptIn(ExperimentalDecomposeApi::class)
fun <NavigationChild : Any> toolboxPredictiveBackAnimation(
    backHandler: BackHandler,
    onBack: () -> Unit
): StackAnimation<Screen, NavigationChild> = predictiveBackAnimation(
    backHandler = backHandler,
    onBack = onBack,
    fallbackAnimation = stackAnimation { child ->
        if (child.configuration is Screen.ImageViewer) {
            imageViewerAnimator
        } else {
            defaultAnimator
        }
    },
    selector = { backEvent, _, _ -> androidPredictiveBackAnimatableV1(backEvent) },
)

inline fun <T> springySpec() = spring<T>(
    dampingRatio = 0.35f,
    stiffness = Spring.StiffnessLow
)

inline fun <T> lessSpringySpec() = spring<T>(
    dampingRatio = 0.4f,
    stiffness = Spring.StiffnessLow
)

@Composable
fun animateFloatingRangeAsState(
    range: ClosedFloatingPointRange<Float>,
    animationSpec: AnimationSpec<Float> = spring()
): State<ClosedFloatingPointRange<Float>> {
    val start = animateFloatAsState(
        targetValue = range.start,
        animationSpec = animationSpec
    )

    val end = animateFloatAsState(
        targetValue = range.endInclusive,
        animationSpec = animationSpec
    )

    return remember(start, end) {
        derivedStateOf {
            start.value..end.value
        }
    }
}