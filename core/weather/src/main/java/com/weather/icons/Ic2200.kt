package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2200: ImageVector
    get() {
        val current = _ic2200
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2200",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M1.293 2.707 A1 1 0 0 1 1 2 c0 -.5 .555 -1.395 1 -2 .445 .605 1 1.5 1 2 a1 1 0 0 1 -1.707 .707Z M7.268 4 h4.053 c.197 0 .316 .233 .209 .411 L9.299 7.298 a.13 .13 0 0 0 .037 .175 .11 .11 0 0 0 .06 .017 h2.347 c.312 0 .473 .398 .26 .641 L5.064 16 l1.652 -6.062 a.133 .133 0 0 0 -.019 -.109 .119 .119 0 0 0 -.04 -.036 .11 .11 0 0 0 -.053 -.014 H4.206 a.339 .339 0 0 1 -.176 -.05 .368 .368 0 0 1 -.13 -.137 .4 .4 0 0 1 -.005 -.377 l3 -4.982 A.422 .422 0 0 1 7.269 4Z M9 2 a1 1 0 0 0 2 0 c0 -.5 -.555 -1.395 -1 -2 -.445 .605 -1 1.5 -1 2Z m4.293 .707 A1 1 0 0 1 13 2 c0 -.5 .555 -1.395 1 -2 .445 .605 1 1.5 1 2 a1 1 0 0 1 -1.707 .707Z M5 2 a1 1 0 0 0 2 0 c0 -.5 -.555 -1.395 -1 -2 -.445 .605 -1 1.5 -1 2Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 1.293 2.707
                moveTo(x = 1.293f, y = 2.707f)
                // A 1 1 0 0 1 1 2
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 1.0f,
                    y1 = 2.0f,
                )
                // c 0 -0.5 0.555 -1.395 1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                    dx2 = 0.555f,
                    dy2 = -1.395f,
                    dx3 = 1.0f,
                    dy3 = -2.0f,
                )
                // c 0.445 0.605 1 1.5 1 2
                curveToRelative(
                    dx1 = 0.445f,
                    dy1 = 0.605f,
                    dx2 = 1.0f,
                    dy2 = 1.5f,
                    dx3 = 1.0f,
                    dy3 = 2.0f,
                )
                // a 1 1 0 0 1 -1.707 0.707z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.707f,
                    dy1 = 0.707f,
                )
                close()
                // M 7.268 4
                moveTo(x = 7.268f, y = 4.0f)
                // h 4.053
                horizontalLineToRelative(dx = 4.053f)
                // c 0.197 0 0.316 0.233 0.209 0.411
                curveToRelative(
                    dx1 = 0.197f,
                    dy1 = 0.0f,
                    dx2 = 0.316f,
                    dy2 = 0.233f,
                    dx3 = 0.209f,
                    dy3 = 0.411f,
                )
                // L 9.299 7.298
                lineTo(x = 9.299f, y = 7.298f)
                // a 0.13 0.13 0 0 0 0.037 0.175
                arcToRelative(
                    a = 0.13f,
                    b = 0.13f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.037f,
                    dy1 = 0.175f,
                )
                // a 0.11 0.11 0 0 0 0.06 0.017
                arcToRelative(
                    a = 0.11f,
                    b = 0.11f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.06f,
                    dy1 = 0.017f,
                )
                // h 2.347
                horizontalLineToRelative(dx = 2.347f)
                // c 0.312 0 0.473 0.398 0.26 0.641
                curveToRelative(
                    dx1 = 0.312f,
                    dy1 = 0.0f,
                    dx2 = 0.473f,
                    dy2 = 0.398f,
                    dx3 = 0.26f,
                    dy3 = 0.641f,
                )
                // L 5.064 16
                lineTo(x = 5.064f, y = 16.0f)
                // l 1.652 -6.062
                lineToRelative(dx = 1.652f, dy = -6.062f)
                // a 0.133 0.133 0 0 0 -0.019 -0.109
                arcToRelative(
                    a = 0.133f,
                    b = 0.133f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.019f,
                    dy1 = -0.109f,
                )
                // a 0.119 0.119 0 0 0 -0.04 -0.036
                arcToRelative(
                    a = 0.119f,
                    b = 0.119f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.04f,
                    dy1 = -0.036f,
                )
                // a 0.11 0.11 0 0 0 -0.053 -0.014
                arcToRelative(
                    a = 0.11f,
                    b = 0.11f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.053f,
                    dy1 = -0.014f,
                )
                // H 4.206
                horizontalLineTo(x = 4.206f)
                // a 0.339 0.339 0 0 1 -0.176 -0.05
                arcToRelative(
                    a = 0.339f,
                    b = 0.339f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.176f,
                    dy1 = -0.05f,
                )
                // a 0.368 0.368 0 0 1 -0.13 -0.137
                arcToRelative(
                    a = 0.368f,
                    b = 0.368f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.13f,
                    dy1 = -0.137f,
                )
                // a 0.4 0.4 0 0 1 -0.005 -0.377
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.005f,
                    dy1 = -0.377f,
                )
                // l 3 -4.982
                lineToRelative(dx = 3.0f, dy = -4.982f)
                // A 0.422 0.422 0 0 1 7.269 4z
                arcTo(
                    horizontalEllipseRadius = 0.422f,
                    verticalEllipseRadius = 0.422f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.269f,
                    y1 = 4.0f,
                )
                close()
                // M 9 2
                moveTo(x = 9.0f, y = 2.0f)
                // a 1 1 0 0 0 2 0
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.0f,
                    dy1 = 0.0f,
                )
                // c 0 -0.5 -0.555 -1.395 -1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                    dx2 = -0.555f,
                    dy2 = -1.395f,
                    dx3 = -1.0f,
                    dy3 = -2.0f,
                )
                // c -0.445 0.605 -1 1.5 -1 2z
                curveToRelative(
                    dx1 = -0.445f,
                    dy1 = 0.605f,
                    dx2 = -1.0f,
                    dy2 = 1.5f,
                    dx3 = -1.0f,
                    dy3 = 2.0f,
                )
                close()
                // m 4.293 0.707
                moveToRelative(dx = 4.293f, dy = 0.707f)
                // A 1 1 0 0 1 13 2
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 13.0f,
                    y1 = 2.0f,
                )
                // c 0 -0.5 0.555 -1.395 1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                    dx2 = 0.555f,
                    dy2 = -1.395f,
                    dx3 = 1.0f,
                    dy3 = -2.0f,
                )
                // c 0.445 0.605 1 1.5 1 2
                curveToRelative(
                    dx1 = 0.445f,
                    dy1 = 0.605f,
                    dx2 = 1.0f,
                    dy2 = 1.5f,
                    dx3 = 1.0f,
                    dy3 = 2.0f,
                )
                // a 1 1 0 0 1 -1.707 0.707z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.707f,
                    dy1 = 0.707f,
                )
                close()
                // M 5 2
                moveTo(x = 5.0f, y = 2.0f)
                // a 1 1 0 0 0 2 0
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.0f,
                    dy1 = 0.0f,
                )
                // c 0 -0.5 -0.555 -1.395 -1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                    dx2 = -0.555f,
                    dy2 = -1.395f,
                    dx3 = -1.0f,
                    dy3 = -2.0f,
                )
                // c -0.445 0.605 -1 1.5 -1 2z
                curveToRelative(
                    dx1 = -0.445f,
                    dy1 = 0.605f,
                    dx2 = -1.0f,
                    dy2 = 1.5f,
                    dx3 = -1.0f,
                    dy3 = 2.0f,
                )
                close()
            }
        }.build().also { _ic2200 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2200: ImageVector? = null
