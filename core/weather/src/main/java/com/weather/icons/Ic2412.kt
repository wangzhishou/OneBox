package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2412: ImageVector
    get() {
        val current = _ic2412
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2412",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8 16 A8 8 0 1 1 8 0 a8 8 0 0 1 0 16Z m0 -1.3 A6.7 6.7 0 1 0 8 1.3 a6.7 6.7 0 0 0 0 13.4Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8 16
                moveTo(x = 8.0f, y = 16.0f)
                // A 8 8 0 1 1 8 0
                arcTo(
                    horizontalEllipseRadius = 8.0f,
                    verticalEllipseRadius = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 8.0f,
                    y1 = 0.0f,
                )
                // a 8 8 0 0 1 0 16z
                arcToRelative(
                    a = 8.0f,
                    b = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 16.0f,
                )
                close()
                // m 0 -1.3
                moveToRelative(dx = 0.0f, dy = -1.3f)
                // A 6.7 6.7 0 1 0 8 1.3
                arcTo(
                    horizontalEllipseRadius = 6.7f,
                    verticalEllipseRadius = 6.7f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    x1 = 8.0f,
                    y1 = 1.3f,
                )
                // a 6.7 6.7 0 0 0 0 13.4z
                arcToRelative(
                    a = 6.7f,
                    b = 6.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 13.4f,
                )
                close()
            }
            // M7.925 11.75 c1.152 0 2.183 -.52 2.87 -1.337 a2.25 2.25 0 1 0 .415 -4.223 3.749 3.749 0 0 0 -6.554 -.029 2.25 2.25 0 1 0 .44 4.3 3.74 3.74 0 0 0 2.829 1.289Z m.371 -4.1 c-.018 .032 .009 .07 .049 .07 h1.101 c.046 0 .07 .048 .042 .08 l-2.234 2.432 c-.039 .042 -.114 .005 -.093 -.047 l.554 -1.458 c.012 -.031 -.014 -.065 -.051 -.065 H6.598 c-.072 0 -.12 -.068 -.088 -.127 l1.048 -1.977 a.119 .119 0 0 1 .043 -.043 .11 .11 0 0 1 .057 -.015 h1.184 c.04 0 .066 .038 .048 .07 l-.594 1.08Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.925 11.75
                moveTo(x = 7.925f, y = 11.75f)
                // c 1.152 0 2.183 -0.52 2.87 -1.337
                curveToRelative(
                    dx1 = 1.152f,
                    dy1 = 0.0f,
                    dx2 = 2.183f,
                    dy2 = -0.52f,
                    dx3 = 2.87f,
                    dy3 = -1.337f,
                )
                // a 2.25 2.25 0 1 0 0.415 -4.223
                arcToRelative(
                    a = 2.25f,
                    b = 2.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.415f,
                    dy1 = -4.223f,
                )
                // a 3.749 3.749 0 0 0 -6.554 -0.029
                arcToRelative(
                    a = 3.749f,
                    b = 3.749f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -6.554f,
                    dy1 = -0.029f,
                )
                // a 2.25 2.25 0 1 0 0.44 4.3
                arcToRelative(
                    a = 2.25f,
                    b = 2.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.44f,
                    dy1 = 4.3f,
                )
                // a 3.74 3.74 0 0 0 2.829 1.289z
                arcToRelative(
                    a = 3.74f,
                    b = 3.74f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.829f,
                    dy1 = 1.289f,
                )
                close()
                // m 0.371 -4.1
                moveToRelative(dx = 0.371f, dy = -4.1f)
                // c -0.018 0.032 0.009 0.07 0.049 0.07
                curveToRelative(
                    dx1 = -0.018f,
                    dy1 = 0.032f,
                    dx2 = 0.009f,
                    dy2 = 0.07f,
                    dx3 = 0.049f,
                    dy3 = 0.07f,
                )
                // h 1.101
                horizontalLineToRelative(dx = 1.101f)
                // c 0.046 0 0.07 0.048 0.042 0.08
                curveToRelative(
                    dx1 = 0.046f,
                    dy1 = 0.0f,
                    dx2 = 0.07f,
                    dy2 = 0.048f,
                    dx3 = 0.042f,
                    dy3 = 0.08f,
                )
                // l -2.234 2.432
                lineToRelative(dx = -2.234f, dy = 2.432f)
                // c -0.039 0.042 -0.114 0.005 -0.093 -0.047
                curveToRelative(
                    dx1 = -0.039f,
                    dy1 = 0.042f,
                    dx2 = -0.114f,
                    dy2 = 0.005f,
                    dx3 = -0.093f,
                    dy3 = -0.047f,
                )
                // l 0.554 -1.458
                lineToRelative(dx = 0.554f, dy = -1.458f)
                // c 0.012 -0.031 -0.014 -0.065 -0.051 -0.065
                curveToRelative(
                    dx1 = 0.012f,
                    dy1 = -0.031f,
                    dx2 = -0.014f,
                    dy2 = -0.065f,
                    dx3 = -0.051f,
                    dy3 = -0.065f,
                )
                // H 6.598
                horizontalLineTo(x = 6.598f)
                // c -0.072 0 -0.12 -0.068 -0.088 -0.127
                curveToRelative(
                    dx1 = -0.072f,
                    dy1 = 0.0f,
                    dx2 = -0.12f,
                    dy2 = -0.068f,
                    dx3 = -0.088f,
                    dy3 = -0.127f,
                )
                // l 1.048 -1.977
                lineToRelative(dx = 1.048f, dy = -1.977f)
                // a 0.119 0.119 0 0 1 0.043 -0.043
                arcToRelative(
                    a = 0.119f,
                    b = 0.119f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.043f,
                    dy1 = -0.043f,
                )
                // a 0.11 0.11 0 0 1 0.057 -0.015
                arcToRelative(
                    a = 0.11f,
                    b = 0.11f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.057f,
                    dy1 = -0.015f,
                )
                // h 1.184
                horizontalLineToRelative(dx = 1.184f)
                // c 0.04 0 0.066 0.038 0.048 0.07
                curveToRelative(
                    dx1 = 0.04f,
                    dy1 = 0.0f,
                    dx2 = 0.066f,
                    dy2 = 0.038f,
                    dx3 = 0.048f,
                    dy3 = 0.07f,
                )
                // l -0.594 1.08z
                lineToRelative(dx = -0.594f, dy = 1.08f)
                close()
            }
        }.build().also { _ic2412 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2412: ImageVector? = null
