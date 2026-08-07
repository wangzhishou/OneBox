package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2001: ImageVector
    get() {
        val current = _ic2001
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2001",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.348 10.759 c1.4 0 2.535 1.173 2.535 2.62 S9.747 15.999 8.348 16 c-1.4 -.002 -2.533 -1.175 -2.536 -2.621 0 -.21 .165 -.381 .369 -.381 .203 0 .368 .17 .368 .38 0 1.028 .805 1.86 1.799 1.86 .993 0 1.797 -.832 1.797 -1.86 0 -1.026 -.804 -1.857 -1.797 -1.857 h-6.98 A.375 .375 0 0 1 1 11.14 c0 -.21 .165 -.381 .368 -.381 h6.98Z m3.262 -8.827 c1.835 -.022 3.346 1.418 3.389 3.23 .043 1.812 -1.398 3.32 -3.232 3.384 a.591 .591 0 0 1 -.136 .004 H1.378 A.376 .376 0 0 1 1 8.177 c0 -.206 .17 -.373 .378 -.373 h10.32 c1.427 -.027 2.564 -1.186 2.546 -2.595 -.018 -1.408 -1.184 -2.539 -2.611 -2.53 -1.427 .009 -2.579 1.153 -2.58 2.562 a.375 .375 0 0 1 -.377 .373 .375 .375 0 0 1 -.377 -.373 c-.001 -1.812 1.475 -3.288 3.31 -3.31Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.348 10.759
                moveTo(x = 8.348f, y = 10.759f)
                // c 1.4 0 2.535 1.173 2.535 2.62
                curveToRelative(
                    dx1 = 1.4f,
                    dy1 = 0.0f,
                    dx2 = 2.535f,
                    dy2 = 1.173f,
                    dx3 = 2.535f,
                    dy3 = 2.62f,
                )
                // S 9.747 15.999 8.348 16
                reflectiveCurveTo(
                    x1 = 9.747f,
                    y1 = 15.999f,
                    x2 = 8.348f,
                    y2 = 16.0f,
                )
                // c -1.4 -0.002 -2.533 -1.175 -2.536 -2.621
                curveToRelative(
                    dx1 = -1.4f,
                    dy1 = -0.002f,
                    dx2 = -2.533f,
                    dy2 = -1.175f,
                    dx3 = -2.536f,
                    dy3 = -2.621f,
                )
                // c 0 -0.21 0.165 -0.381 0.369 -0.381
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.21f,
                    dx2 = 0.165f,
                    dy2 = -0.381f,
                    dx3 = 0.369f,
                    dy3 = -0.381f,
                )
                // c 0.203 0 0.368 0.17 0.368 0.38
                curveToRelative(
                    dx1 = 0.203f,
                    dy1 = 0.0f,
                    dx2 = 0.368f,
                    dy2 = 0.17f,
                    dx3 = 0.368f,
                    dy3 = 0.38f,
                )
                // c 0 1.028 0.805 1.86 1.799 1.86
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.028f,
                    dx2 = 0.805f,
                    dy2 = 1.86f,
                    dx3 = 1.799f,
                    dy3 = 1.86f,
                )
                // c 0.993 0 1.797 -0.832 1.797 -1.86
                curveToRelative(
                    dx1 = 0.993f,
                    dy1 = 0.0f,
                    dx2 = 1.797f,
                    dy2 = -0.832f,
                    dx3 = 1.797f,
                    dy3 = -1.86f,
                )
                // c 0 -1.026 -0.804 -1.857 -1.797 -1.857
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.026f,
                    dx2 = -0.804f,
                    dy2 = -1.857f,
                    dx3 = -1.797f,
                    dy3 = -1.857f,
                )
                // h -6.98
                horizontalLineToRelative(dx = -6.98f)
                // A 0.375 0.375 0 0 1 1 11.14
                arcTo(
                    horizontalEllipseRadius = 0.375f,
                    verticalEllipseRadius = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 1.0f,
                    y1 = 11.14f,
                )
                // c 0 -0.21 0.165 -0.381 0.368 -0.381
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.21f,
                    dx2 = 0.165f,
                    dy2 = -0.381f,
                    dx3 = 0.368f,
                    dy3 = -0.381f,
                )
                // h 6.98z
                horizontalLineToRelative(dx = 6.98f)
                close()
                // m 3.262 -8.827
                moveToRelative(dx = 3.262f, dy = -8.827f)
                // c 1.835 -0.022 3.346 1.418 3.389 3.23
                curveToRelative(
                    dx1 = 1.835f,
                    dy1 = -0.022f,
                    dx2 = 3.346f,
                    dy2 = 1.418f,
                    dx3 = 3.389f,
                    dy3 = 3.23f,
                )
                // c 0.043 1.812 -1.398 3.32 -3.232 3.384
                curveToRelative(
                    dx1 = 0.043f,
                    dy1 = 1.812f,
                    dx2 = -1.398f,
                    dy2 = 3.32f,
                    dx3 = -3.232f,
                    dy3 = 3.384f,
                )
                // a 0.591 0.591 0 0 1 -0.136 0.004
                arcToRelative(
                    a = 0.591f,
                    b = 0.591f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.136f,
                    dy1 = 0.004f,
                )
                // H 1.378
                horizontalLineTo(x = 1.378f)
                // A 0.376 0.376 0 0 1 1 8.177
                arcTo(
                    horizontalEllipseRadius = 0.376f,
                    verticalEllipseRadius = 0.376f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 1.0f,
                    y1 = 8.177f,
                )
                // c 0 -0.206 0.17 -0.373 0.378 -0.373
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.206f,
                    dx2 = 0.17f,
                    dy2 = -0.373f,
                    dx3 = 0.378f,
                    dy3 = -0.373f,
                )
                // h 10.32
                horizontalLineToRelative(dx = 10.32f)
                // c 1.427 -0.027 2.564 -1.186 2.546 -2.595
                curveToRelative(
                    dx1 = 1.427f,
                    dy1 = -0.027f,
                    dx2 = 2.564f,
                    dy2 = -1.186f,
                    dx3 = 2.546f,
                    dy3 = -2.595f,
                )
                // c -0.018 -1.408 -1.184 -2.539 -2.611 -2.53
                curveToRelative(
                    dx1 = -0.018f,
                    dy1 = -1.408f,
                    dx2 = -1.184f,
                    dy2 = -2.539f,
                    dx3 = -2.611f,
                    dy3 = -2.53f,
                )
                // c -1.427 0.009 -2.579 1.153 -2.58 2.562
                curveToRelative(
                    dx1 = -1.427f,
                    dy1 = 0.009f,
                    dx2 = -2.579f,
                    dy2 = 1.153f,
                    dx3 = -2.58f,
                    dy3 = 2.562f,
                )
                // a 0.375 0.375 0 0 1 -0.377 0.373
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.377f,
                    dy1 = 0.373f,
                )
                // a 0.375 0.375 0 0 1 -0.377 -0.373
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.377f,
                    dy1 = -0.373f,
                )
                // c -0.001 -1.812 1.475 -3.288 3.31 -3.31z
                curveToRelative(
                    dx1 = -0.001f,
                    dy1 = -1.812f,
                    dx2 = 1.475f,
                    dy2 = -3.288f,
                    dx3 = 3.31f,
                    dy3 = -3.31f,
                )
                close()
            }
            // M4.056 0 c1.496 0 2.709 1.172 2.709 2.617 s-1.213 2.616 -2.71 2.616 l-.075 .008 H1.394 A.387 .387 0 0 1 1 4.861 c0 -.21 .176 -.38 .394 -.38 h2.662 c1.06 0 1.92 -.83 1.922 -1.854 C5.978 1.603 5.12 .77 4.06 .769 3 .766 2.138 1.593 2.134 2.617 c0 .21 -.176 .38 -.394 .38 a.387 .387 0 0 1 -.393 -.38 C1.347 1.172 2.56 0 4.056 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.056 0
                moveTo(x = 4.056f, y = 0.0f)
                // c 1.496 0 2.709 1.172 2.709 2.617
                curveToRelative(
                    dx1 = 1.496f,
                    dy1 = 0.0f,
                    dx2 = 2.709f,
                    dy2 = 1.172f,
                    dx3 = 2.709f,
                    dy3 = 2.617f,
                )
                // s -1.213 2.616 -2.71 2.616
                reflectiveCurveToRelative(
                    dx1 = -1.213f,
                    dy1 = 2.616f,
                    dx2 = -2.71f,
                    dy2 = 2.616f,
                )
                // l -0.075 0.008
                lineToRelative(dx = -0.075f, dy = 0.008f)
                // H 1.394
                horizontalLineTo(x = 1.394f)
                // A 0.387 0.387 0 0 1 1 4.861
                arcTo(
                    horizontalEllipseRadius = 0.387f,
                    verticalEllipseRadius = 0.387f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 1.0f,
                    y1 = 4.861f,
                )
                // c 0 -0.21 0.176 -0.38 0.394 -0.38
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.21f,
                    dx2 = 0.176f,
                    dy2 = -0.38f,
                    dx3 = 0.394f,
                    dy3 = -0.38f,
                )
                // h 2.662
                horizontalLineToRelative(dx = 2.662f)
                // c 1.06 0 1.92 -0.83 1.922 -1.854
                curveToRelative(
                    dx1 = 1.06f,
                    dy1 = 0.0f,
                    dx2 = 1.92f,
                    dy2 = -0.83f,
                    dx3 = 1.922f,
                    dy3 = -1.854f,
                )
                // C 5.978 1.603 5.12 0.77 4.06 0.769
                curveTo(
                    x1 = 5.978f,
                    y1 = 1.603f,
                    x2 = 5.12f,
                    y2 = 0.77f,
                    x3 = 4.06f,
                    y3 = 0.769f,
                )
                // C 3 0.766 2.138 1.593 2.134 2.617
                curveTo(
                    x1 = 3.0f,
                    y1 = 0.766f,
                    x2 = 2.138f,
                    y2 = 1.593f,
                    x3 = 2.134f,
                    y3 = 2.617f,
                )
                // c 0 0.21 -0.176 0.38 -0.394 0.38
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.21f,
                    dx2 = -0.176f,
                    dy2 = 0.38f,
                    dx3 = -0.394f,
                    dy3 = 0.38f,
                )
                // a 0.387 0.387 0 0 1 -0.393 -0.38
                arcToRelative(
                    a = 0.387f,
                    b = 0.387f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.393f,
                    dy1 = -0.38f,
                )
                // C 1.347 1.172 2.56 0 4.056 0z
                curveTo(
                    x1 = 1.347f,
                    y1 = 1.172f,
                    x2 = 2.56f,
                    y2 = 0.0f,
                    x3 = 4.056f,
                    y3 = 0.0f,
                )
                close()
            }
        }.build().also { _ic2001 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2001: ImageVector? = null
