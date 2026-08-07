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
