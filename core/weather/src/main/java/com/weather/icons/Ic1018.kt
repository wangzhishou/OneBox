package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1018: ImageVector
    get() {
        val current = _ic1018
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1018",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.86 12.447 a2.807 2.807 0 1 0 0 -5.614 2.807 2.807 0 0 0 0 5.614Z m0 -6.771 c.973 0 1.636 .475 1.833 .628 .013 -.169 .026 -.382 .027 -.628 .015 -2.353 -.956 -5.15 -1.856 -5.15 -.678 0 -1.849 2.306 -1.849 5.15 0 .211 .016 .412 .027 .617 a3.018 3.018 0 0 1 1.818 -.617Z m-3.332 6.1 a3.022 3.022 0 0 1 -.372 -1.901 8.77 8.77 0 0 0 -.558 .29 C1.553 11.328 -.384 13.567 .066 14.347 c.339 .587 2.921 .449 5.384 -.973 .183 -.106 .349 -.22 .52 -.332 a3.016 3.016 0 0 1 -1.442 -1.266Z m7.871 -1.762 a9.015 9.015 0 0 0 -.547 -.285 3.021 3.021 0 0 1 -.375 1.883 3.013 3.013 0 0 1 -1.46 1.273 c.175 .114 .344 .23 .53 .338 2.463 1.422 5.046 1.559 5.388 .967 .45 -.779 -1.492 -3.015 -3.536 -4.176Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.86 12.447
                moveTo(x = 7.86f, y = 12.447f)
                // a 2.807 2.807 0 1 0 0 -5.614
                arcToRelative(
                    a = 2.807f,
                    b = 2.807f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -5.614f,
                )
                // a 2.807 2.807 0 0 0 0 5.614z
                arcToRelative(
                    a = 2.807f,
                    b = 2.807f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 5.614f,
                )
                close()
                // m 0 -6.771
                moveToRelative(dx = 0.0f, dy = -6.771f)
                // c 0.973 0 1.636 0.475 1.833 0.628
                curveToRelative(
                    dx1 = 0.973f,
                    dy1 = 0.0f,
                    dx2 = 1.636f,
                    dy2 = 0.475f,
                    dx3 = 1.833f,
                    dy3 = 0.628f,
                )
                // c 0.013 -0.169 0.026 -0.382 0.027 -0.628
                curveToRelative(
                    dx1 = 0.013f,
                    dy1 = -0.169f,
                    dx2 = 0.026f,
                    dy2 = -0.382f,
                    dx3 = 0.027f,
                    dy3 = -0.628f,
                )
                // c 0.015 -2.353 -0.956 -5.15 -1.856 -5.15
                curveToRelative(
                    dx1 = 0.015f,
                    dy1 = -2.353f,
                    dx2 = -0.956f,
                    dy2 = -5.15f,
                    dx3 = -1.856f,
                    dy3 = -5.15f,
                )
                // c -0.678 0 -1.849 2.306 -1.849 5.15
                curveToRelative(
                    dx1 = -0.678f,
                    dy1 = 0.0f,
                    dx2 = -1.849f,
                    dy2 = 2.306f,
                    dx3 = -1.849f,
                    dy3 = 5.15f,
                )
                // c 0 0.211 0.016 0.412 0.027 0.617
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.211f,
                    dx2 = 0.016f,
                    dy2 = 0.412f,
                    dx3 = 0.027f,
                    dy3 = 0.617f,
                )
                // a 3.018 3.018 0 0 1 1.818 -0.617z
                arcToRelative(
                    a = 3.018f,
                    b = 3.018f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.818f,
                    dy1 = -0.617f,
                )
                close()
                // m -3.332 6.1
                moveToRelative(dx = -3.332f, dy = 6.1f)
                // a 3.022 3.022 0 0 1 -0.372 -1.901
                arcToRelative(
                    a = 3.022f,
                    b = 3.022f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.372f,
                    dy1 = -1.901f,
                )
                // a 8.77 8.77 0 0 0 -0.558 0.29
                arcToRelative(
                    a = 8.77f,
                    b = 8.77f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.558f,
                    dy1 = 0.29f,
                )
                // C 1.553 11.328 -0.384 13.567 0.066 14.347
                curveTo(
                    x1 = 1.553f,
                    y1 = 11.328f,
                    x2 = -0.384f,
                    y2 = 13.567f,
                    x3 = 0.066f,
                    y3 = 14.347f,
                )
                // c 0.339 0.587 2.921 0.449 5.384 -0.973
                curveToRelative(
                    dx1 = 0.339f,
                    dy1 = 0.587f,
                    dx2 = 2.921f,
                    dy2 = 0.449f,
                    dx3 = 5.384f,
                    dy3 = -0.973f,
                )
                // c 0.183 -0.106 0.349 -0.22 0.52 -0.332
                curveToRelative(
                    dx1 = 0.183f,
                    dy1 = -0.106f,
                    dx2 = 0.349f,
                    dy2 = -0.22f,
                    dx3 = 0.52f,
                    dy3 = -0.332f,
                )
                // a 3.016 3.016 0 0 1 -1.442 -1.266z
                arcToRelative(
                    a = 3.016f,
                    b = 3.016f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.442f,
                    dy1 = -1.266f,
                )
                close()
                // m 7.871 -1.762
                moveToRelative(dx = 7.871f, dy = -1.762f)
                // a 9.015 9.015 0 0 0 -0.547 -0.285
                arcToRelative(
                    a = 9.015f,
                    b = 9.015f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.547f,
                    dy1 = -0.285f,
                )
                // a 3.021 3.021 0 0 1 -0.375 1.883
                arcToRelative(
                    a = 3.021f,
                    b = 3.021f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.375f,
                    dy1 = 1.883f,
                )
                // a 3.013 3.013 0 0 1 -1.46 1.273
                arcToRelative(
                    a = 3.013f,
                    b = 3.013f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.46f,
                    dy1 = 1.273f,
                )
                // c 0.175 0.114 0.344 0.23 0.53 0.338
                curveToRelative(
                    dx1 = 0.175f,
                    dy1 = 0.114f,
                    dx2 = 0.344f,
                    dy2 = 0.23f,
                    dx3 = 0.53f,
                    dy3 = 0.338f,
                )
                // c 2.463 1.422 5.046 1.559 5.388 0.967
                curveToRelative(
                    dx1 = 2.463f,
                    dy1 = 1.422f,
                    dx2 = 5.046f,
                    dy2 = 1.559f,
                    dx3 = 5.388f,
                    dy3 = 0.967f,
                )
                // c 0.45 -0.779 -1.492 -3.015 -3.536 -4.176z
                curveToRelative(
                    dx1 = 0.45f,
                    dy1 = -0.779f,
                    dx2 = -1.492f,
                    dy2 = -3.015f,
                    dx3 = -3.536f,
                    dy3 = -4.176f,
                )
                close()
            }
        }.build().also { _ic1018 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1018: ImageVector? = null
