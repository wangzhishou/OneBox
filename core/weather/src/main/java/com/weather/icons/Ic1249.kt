package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1249: ImageVector
    get() {
        val current = _ic1249
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1249",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.531 6.829 16 13.922 5.5 16 0 13.832 l6.318 -6.461 -.999 2.606 a.396 .396 0 0 0 .002 .251 .23 .23 0 0 0 .055 .092 .109 .109 0 0 0 .075 .033 H6.47 c.008 0 .015 .003 .022 .009 a.068 .068 0 0 1 .017 .024 .12 .12 0 0 1 .01 .035 .138 .138 0 0 1 -.002 .038 l-.7 4.041 L8.76 9.254 c.09 -.162 .022 -.427 -.11 -.427 h-.996 a.036 .036 0 0 1 -.025 -.012 .079 .079 0 0 1 -.019 -.031 .13 .13 0 0 1 .003 -.085 l.919 -1.87Z M7.74 6 c.922 0 1.746 -.416 2.296 -1.07 a1.8 1.8 0 1 0 .332 -3.378 3 3 0 0 0 -5.243 -.023 1.8 1.8 0 1 0 .352 3.44 A2.993 2.993 0 0 0 7.74 6Z m-.475 -3.839 c-.034 -.217 .102 -.48 .298 -.598 a.455 .455 0 0 1 .484 .014 l.042 .025 c.18 .11 .277 .31 .245 .512 L8.119 3.48 h-.646 l-.208 -1.319Z M8.16 4.14 a.36 .36 0 1 1 -.72 0 .36 .36 0 0 1 .72 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.531 6.829
                moveTo(x = 8.531f, y = 6.829f)
                // L 16 13.922
                lineTo(x = 16.0f, y = 13.922f)
                // L 5.5 16
                lineTo(x = 5.5f, y = 16.0f)
                // L 0 13.832
                lineTo(x = 0.0f, y = 13.832f)
                // l 6.318 -6.461
                lineToRelative(dx = 6.318f, dy = -6.461f)
                // l -0.999 2.606
                lineToRelative(dx = -0.999f, dy = 2.606f)
                // a 0.396 0.396 0 0 0 0.002 0.251
                arcToRelative(
                    a = 0.396f,
                    b = 0.396f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.002f,
                    dy1 = 0.251f,
                )
                // a 0.23 0.23 0 0 0 0.055 0.092
                arcToRelative(
                    a = 0.23f,
                    b = 0.23f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.055f,
                    dy1 = 0.092f,
                )
                // a 0.109 0.109 0 0 0 0.075 0.033
                arcToRelative(
                    a = 0.109f,
                    b = 0.109f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.075f,
                    dy1 = 0.033f,
                )
                // H 6.47
                horizontalLineTo(x = 6.47f)
                // c 0.008 0 0.015 0.003 0.022 0.009
                curveToRelative(
                    dx1 = 0.008f,
                    dy1 = 0.0f,
                    dx2 = 0.015f,
                    dy2 = 0.003f,
                    dx3 = 0.022f,
                    dy3 = 0.009f,
                )
                // a 0.068 0.068 0 0 1 0.017 0.024
                arcToRelative(
                    a = 0.068f,
                    b = 0.068f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.017f,
                    dy1 = 0.024f,
                )
                // a 0.12 0.12 0 0 1 0.01 0.035
                arcToRelative(
                    a = 0.12f,
                    b = 0.12f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.01f,
                    dy1 = 0.035f,
                )
                // a 0.138 0.138 0 0 1 -0.002 0.038
                arcToRelative(
                    a = 0.138f,
                    b = 0.138f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.002f,
                    dy1 = 0.038f,
                )
                // l -0.7 4.041
                lineToRelative(dx = -0.7f, dy = 4.041f)
                // L 8.76 9.254
                lineTo(x = 8.76f, y = 9.254f)
                // c 0.09 -0.162 0.022 -0.427 -0.11 -0.427
                curveToRelative(
                    dx1 = 0.09f,
                    dy1 = -0.162f,
                    dx2 = 0.022f,
                    dy2 = -0.427f,
                    dx3 = -0.11f,
                    dy3 = -0.427f,
                )
                // h -0.996
                horizontalLineToRelative(dx = -0.996f)
                // a 0.036 0.036 0 0 1 -0.025 -0.012
                arcToRelative(
                    a = 0.036f,
                    b = 0.036f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.025f,
                    dy1 = -0.012f,
                )
                // a 0.079 0.079 0 0 1 -0.019 -0.031
                arcToRelative(
                    a = 0.079f,
                    b = 0.079f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.019f,
                    dy1 = -0.031f,
                )
                // a 0.13 0.13 0 0 1 0.003 -0.085
                arcToRelative(
                    a = 0.13f,
                    b = 0.13f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.003f,
                    dy1 = -0.085f,
                )
                // l 0.919 -1.87z
                lineToRelative(dx = 0.919f, dy = -1.87f)
                close()
                // M 7.74 6
                moveTo(x = 7.74f, y = 6.0f)
                // c 0.922 0 1.746 -0.416 2.296 -1.07
                curveToRelative(
                    dx1 = 0.922f,
                    dy1 = 0.0f,
                    dx2 = 1.746f,
                    dy2 = -0.416f,
                    dx3 = 2.296f,
                    dy3 = -1.07f,
                )
                // a 1.8 1.8 0 1 0 0.332 -3.378
                arcToRelative(
                    a = 1.8f,
                    b = 1.8f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.332f,
                    dy1 = -3.378f,
                )
                // a 3 3 0 0 0 -5.243 -0.023
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -5.243f,
                    dy1 = -0.023f,
                )
                // a 1.8 1.8 0 1 0 0.352 3.44
                arcToRelative(
                    a = 1.8f,
                    b = 1.8f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.352f,
                    dy1 = 3.44f,
                )
                // A 2.993 2.993 0 0 0 7.74 6z
                arcTo(
                    horizontalEllipseRadius = 2.993f,
                    verticalEllipseRadius = 2.993f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.74f,
                    y1 = 6.0f,
                )
                close()
                // m -0.475 -3.839
                moveToRelative(dx = -0.475f, dy = -3.839f)
                // c -0.034 -0.217 0.102 -0.48 0.298 -0.598
                curveToRelative(
                    dx1 = -0.034f,
                    dy1 = -0.217f,
                    dx2 = 0.102f,
                    dy2 = -0.48f,
                    dx3 = 0.298f,
                    dy3 = -0.598f,
                )
                // a 0.455 0.455 0 0 1 0.484 0.014
                arcToRelative(
                    a = 0.455f,
                    b = 0.455f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.484f,
                    dy1 = 0.014f,
                )
                // l 0.042 0.025
                lineToRelative(dx = 0.042f, dy = 0.025f)
                // c 0.18 0.11 0.277 0.31 0.245 0.512
                curveToRelative(
                    dx1 = 0.18f,
                    dy1 = 0.11f,
                    dx2 = 0.277f,
                    dy2 = 0.31f,
                    dx3 = 0.245f,
                    dy3 = 0.512f,
                )
                // L 8.119 3.48
                lineTo(x = 8.119f, y = 3.48f)
                // h -0.646
                horizontalLineToRelative(dx = -0.646f)
                // l -0.208 -1.319z
                lineToRelative(dx = -0.208f, dy = -1.319f)
                close()
                // M 8.16 4.14
                moveTo(x = 8.16f, y = 4.14f)
                // a 0.36 0.36 0 1 1 -0.72 0
                arcToRelative(
                    a = 0.36f,
                    b = 0.36f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.72f,
                    dy1 = 0.0f,
                )
                // a 0.36 0.36 0 0 1 0.72 0z
                arcToRelative(
                    a = 0.36f,
                    b = 0.36f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.72f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic1249 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1249: ImageVector? = null
