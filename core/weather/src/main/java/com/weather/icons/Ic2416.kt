package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2416: ImageVector
    get() {
        val current = _ic2416
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2416",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.406 16 c1.44 0 2.728 -.623 3.588 -1.604 a2.9 2.9 0 0 0 1.194 .254 c1.553 0 2.812 -1.209 2.812 -2.7 s-1.26 -2.7 -2.813 -2.7 a2.93 2.93 0 0 0 -.674 .078 C11.714 7.94 10.175 7 8.406 7 c-1.754 0 -3.282 .925 -4.086 2.294 a2.943 2.943 0 0 0 -.508 -.044 C2.26 9.25 1 10.46 1 11.95 s1.26 2.7 2.813 2.7 c.373 0 .73 -.07 1.056 -.197 A4.76 4.76 0 0 0 8.406 16Z m-.742 -5.758 c-.053 -.326 .16 -.72 .466 -.897 a.737 .737 0 0 1 .755 .02 l.066 .038 a.751 .751 0 0 1 .383 .768 L9 12.22 H7.99 l-.325 -1.98Z m1.398 2.968 c0 .298 -.251 .54 -.562 .54 a.552 .552 0 0 1 -.563 -.54 c0 -.298 .252 -.54 .563 -.54 .31 0 .562 .242 .562 .54Z M4.745 1.777 a.516 .516 0 1 0 1.007 -.224 L5.496 .403 A.516 .516 0 0 0 4.49 .627 l.255 1.15Z M1.023 3.535 l.994 .633 a.516 .516 0 0 0 .554 -.87 l-.994 -.633 a.516 .516 0 0 0 -.554 .87Z M.628 8.043 l1.15 -.256 a.516 .516 0 1 0 -.223 -1.008 l-1.15 .256 a.516 .516 0 1 0 .223 1.008Z m10.238 -2.28 a.534 .534 0 0 0 .112 -.012 l1.15 -.256 a.516 .516 0 1 0 -.224 -1.008 l-1.15 .256 a.516 .516 0 0 0 .112 1.02Z M8.522 2.728 a.516 .516 0 0 0 .712 -.158 l.633 -.994 a.516 .516 0 0 0 -.87 -.554 l-.633 .994 a.516 .516 0 0 0 .158 .712Z M2.819 7.032 c.071 .303 .182 .596 .331 .87 a3.13 3.13 0 0 0 .908 -.486 2.453 2.453 0 0 1 -.232 -.608 A2.504 2.504 0 0 1 8.714 5.72 l.004 .038 a5.42 5.42 0 0 1 1.064 .25 3.51 3.51 0 0 0 -.061 -.512 3.535 3.535 0 0 0 -6.902 1.536Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.406 16
                moveTo(x = 8.406f, y = 16.0f)
                // c 1.44 0 2.728 -0.623 3.588 -1.604
                curveToRelative(
                    dx1 = 1.44f,
                    dy1 = 0.0f,
                    dx2 = 2.728f,
                    dy2 = -0.623f,
                    dx3 = 3.588f,
                    dy3 = -1.604f,
                )
                // a 2.9 2.9 0 0 0 1.194 0.254
                arcToRelative(
                    a = 2.9f,
                    b = 2.9f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.194f,
                    dy1 = 0.254f,
                )
                // c 1.553 0 2.812 -1.209 2.812 -2.7
                curveToRelative(
                    dx1 = 1.553f,
                    dy1 = 0.0f,
                    dx2 = 2.812f,
                    dy2 = -1.209f,
                    dx3 = 2.812f,
                    dy3 = -2.7f,
                )
                // s -1.26 -2.7 -2.813 -2.7
                reflectiveCurveToRelative(
                    dx1 = -1.26f,
                    dy1 = -2.7f,
                    dx2 = -2.813f,
                    dy2 = -2.7f,
                )
                // a 2.93 2.93 0 0 0 -0.674 0.078
                arcToRelative(
                    a = 2.93f,
                    b = 2.93f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.674f,
                    dy1 = 0.078f,
                )
                // C 11.714 7.94 10.175 7 8.406 7
                curveTo(
                    x1 = 11.714f,
                    y1 = 7.94f,
                    x2 = 10.175f,
                    y2 = 7.0f,
                    x3 = 8.406f,
                    y3 = 7.0f,
                )
                // c -1.754 0 -3.282 0.925 -4.086 2.294
                curveToRelative(
                    dx1 = -1.754f,
                    dy1 = 0.0f,
                    dx2 = -3.282f,
                    dy2 = 0.925f,
                    dx3 = -4.086f,
                    dy3 = 2.294f,
                )
                // a 2.943 2.943 0 0 0 -0.508 -0.044
                arcToRelative(
                    a = 2.943f,
                    b = 2.943f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.508f,
                    dy1 = -0.044f,
                )
                // C 2.26 9.25 1 10.46 1 11.95
                curveTo(
                    x1 = 2.26f,
                    y1 = 9.25f,
                    x2 = 1.0f,
                    y2 = 10.46f,
                    x3 = 1.0f,
                    y3 = 11.95f,
                )
                // s 1.26 2.7 2.813 2.7
                reflectiveCurveToRelative(
                    dx1 = 1.26f,
                    dy1 = 2.7f,
                    dx2 = 2.813f,
                    dy2 = 2.7f,
                )
                // c 0.373 0 0.73 -0.07 1.056 -0.197
                curveToRelative(
                    dx1 = 0.373f,
                    dy1 = 0.0f,
                    dx2 = 0.73f,
                    dy2 = -0.07f,
                    dx3 = 1.056f,
                    dy3 = -0.197f,
                )
                // A 4.76 4.76 0 0 0 8.406 16z
                arcTo(
                    horizontalEllipseRadius = 4.76f,
                    verticalEllipseRadius = 4.76f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 8.406f,
                    y1 = 16.0f,
                )
                close()
                // m -0.742 -5.758
                moveToRelative(dx = -0.742f, dy = -5.758f)
                // c -0.053 -0.326 0.16 -0.72 0.466 -0.897
                curveToRelative(
                    dx1 = -0.053f,
                    dy1 = -0.326f,
                    dx2 = 0.16f,
                    dy2 = -0.72f,
                    dx3 = 0.466f,
                    dy3 = -0.897f,
                )
                // a 0.737 0.737 0 0 1 0.755 0.02
                arcToRelative(
                    a = 0.737f,
                    b = 0.737f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.755f,
                    dy1 = 0.02f,
                )
                // l 0.066 0.038
                lineToRelative(dx = 0.066f, dy = 0.038f)
                // a 0.751 0.751 0 0 1 0.383 0.768
                arcToRelative(
                    a = 0.751f,
                    b = 0.751f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.383f,
                    dy1 = 0.768f,
                )
                // L 9 12.22
                lineTo(x = 9.0f, y = 12.22f)
                // H 7.99
                horizontalLineTo(x = 7.99f)
                // l -0.325 -1.98z
                lineToRelative(dx = -0.325f, dy = -1.98f)
                close()
                // m 1.398 2.968
                moveToRelative(dx = 1.398f, dy = 2.968f)
                // c 0 0.298 -0.251 0.54 -0.562 0.54
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.298f,
                    dx2 = -0.251f,
                    dy2 = 0.54f,
                    dx3 = -0.562f,
                    dy3 = 0.54f,
                )
                // a 0.552 0.552 0 0 1 -0.563 -0.54
                arcToRelative(
                    a = 0.552f,
                    b = 0.552f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.563f,
                    dy1 = -0.54f,
                )
                // c 0 -0.298 0.252 -0.54 0.563 -0.54
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.298f,
                    dx2 = 0.252f,
                    dy2 = -0.54f,
                    dx3 = 0.563f,
                    dy3 = -0.54f,
                )
                // c 0.31 0 0.562 0.242 0.562 0.54z
                curveToRelative(
                    dx1 = 0.31f,
                    dy1 = 0.0f,
                    dx2 = 0.562f,
                    dy2 = 0.242f,
                    dx3 = 0.562f,
                    dy3 = 0.54f,
                )
                close()
                // M 4.745 1.777
                moveTo(x = 4.745f, y = 1.777f)
                // a 0.516 0.516 0 1 0 1.007 -0.224
                arcToRelative(
                    a = 0.516f,
                    b = 0.516f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 1.007f,
                    dy1 = -0.224f,
                )
                // L 5.496 0.403
                lineTo(x = 5.496f, y = 0.403f)
                // A 0.516 0.516 0 0 0 4.49 0.627
                arcTo(
                    horizontalEllipseRadius = 0.516f,
                    verticalEllipseRadius = 0.516f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 4.49f,
                    y1 = 0.627f,
                )
                // l 0.255 1.15z
                lineToRelative(dx = 0.255f, dy = 1.15f)
                close()
                // M 1.023 3.535
                moveTo(x = 1.023f, y = 3.535f)
                // l 0.994 0.633
                lineToRelative(dx = 0.994f, dy = 0.633f)
                // a 0.516 0.516 0 0 0 0.554 -0.87
                arcToRelative(
                    a = 0.516f,
                    b = 0.516f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.554f,
                    dy1 = -0.87f,
                )
                // l -0.994 -0.633
                lineToRelative(dx = -0.994f, dy = -0.633f)
                // a 0.516 0.516 0 0 0 -0.554 0.87z
                arcToRelative(
                    a = 0.516f,
                    b = 0.516f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.554f,
                    dy1 = 0.87f,
                )
                close()
                // M 0.628 8.043
                moveTo(x = 0.628f, y = 8.043f)
                // l 1.15 -0.256
                lineToRelative(dx = 1.15f, dy = -0.256f)
                // a 0.516 0.516 0 1 0 -0.223 -1.008
                arcToRelative(
                    a = 0.516f,
                    b = 0.516f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.223f,
                    dy1 = -1.008f,
                )
                // l -1.15 0.256
                lineToRelative(dx = -1.15f, dy = 0.256f)
                // a 0.516 0.516 0 1 0 0.223 1.008z
                arcToRelative(
                    a = 0.516f,
                    b = 0.516f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.223f,
                    dy1 = 1.008f,
                )
                close()
                // m 10.238 -2.28
                moveToRelative(dx = 10.238f, dy = -2.28f)
                // a 0.534 0.534 0 0 0 0.112 -0.012
                arcToRelative(
                    a = 0.534f,
                    b = 0.534f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.112f,
                    dy1 = -0.012f,
                )
                // l 1.15 -0.256
                lineToRelative(dx = 1.15f, dy = -0.256f)
                // a 0.516 0.516 0 1 0 -0.224 -1.008
                arcToRelative(
                    a = 0.516f,
                    b = 0.516f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.224f,
                    dy1 = -1.008f,
                )
                // l -1.15 0.256
                lineToRelative(dx = -1.15f, dy = 0.256f)
                // a 0.516 0.516 0 0 0 0.112 1.02z
                arcToRelative(
                    a = 0.516f,
                    b = 0.516f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.112f,
                    dy1 = 1.02f,
                )
                close()
                // M 8.522 2.728
                moveTo(x = 8.522f, y = 2.728f)
                // a 0.516 0.516 0 0 0 0.712 -0.158
                arcToRelative(
                    a = 0.516f,
                    b = 0.516f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.712f,
                    dy1 = -0.158f,
                )
                // l 0.633 -0.994
                lineToRelative(dx = 0.633f, dy = -0.994f)
                // a 0.516 0.516 0 0 0 -0.87 -0.554
                arcToRelative(
                    a = 0.516f,
                    b = 0.516f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.87f,
                    dy1 = -0.554f,
                )
                // l -0.633 0.994
                lineToRelative(dx = -0.633f, dy = 0.994f)
                // a 0.516 0.516 0 0 0 0.158 0.712z
                arcToRelative(
                    a = 0.516f,
                    b = 0.516f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.158f,
                    dy1 = 0.712f,
                )
                close()
                // M 2.819 7.032
                moveTo(x = 2.819f, y = 7.032f)
                // c 0.071 0.303 0.182 0.596 0.331 0.87
                curveToRelative(
                    dx1 = 0.071f,
                    dy1 = 0.303f,
                    dx2 = 0.182f,
                    dy2 = 0.596f,
                    dx3 = 0.331f,
                    dy3 = 0.87f,
                )
                // a 3.13 3.13 0 0 0 0.908 -0.486
                arcToRelative(
                    a = 3.13f,
                    b = 3.13f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.908f,
                    dy1 = -0.486f,
                )
                // a 2.453 2.453 0 0 1 -0.232 -0.608
                arcToRelative(
                    a = 2.453f,
                    b = 2.453f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.232f,
                    dy1 = -0.608f,
                )
                // A 2.504 2.504 0 0 1 8.714 5.72
                arcTo(
                    horizontalEllipseRadius = 2.504f,
                    verticalEllipseRadius = 2.504f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 8.714f,
                    y1 = 5.72f,
                )
                // l 0.004 0.038
                lineToRelative(dx = 0.004f, dy = 0.038f)
                // a 5.42 5.42 0 0 1 1.064 0.25
                arcToRelative(
                    a = 5.42f,
                    b = 5.42f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.064f,
                    dy1 = 0.25f,
                )
                // a 3.51 3.51 0 0 0 -0.061 -0.512
                arcToRelative(
                    a = 3.51f,
                    b = 3.51f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.061f,
                    dy1 = -0.512f,
                )
                // a 3.535 3.535 0 0 0 -6.902 1.536z
                arcToRelative(
                    a = 3.535f,
                    b = 3.535f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -6.902f,
                    dy1 = 1.536f,
                )
                close()
            }
        }.build().also { _ic2416 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2416: ImageVector? = null
