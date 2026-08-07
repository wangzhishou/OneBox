package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2214: ImageVector
    get() {
        val current = _ic2214
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2214",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M2.724 12.362 a.25 .25 0 0 0 -.448 -.224 l-1 2 a.25 .25 0 0 0 .448 .224 l1 -2Z m4 1 a.25 .25 0 0 0 -.447 -.224 l-1 2 a.25 .25 0 0 0 .447 .224 l1 -2Z m3.888 -.336 a.25 .25 0 0 1 .112 .336 l-1 2 a.25 .25 0 0 1 -.447 -.224 l1 -2 a.25 .25 0 0 1 .335 -.112Z m4.112 -.664 a.25 .25 0 0 0 -.447 -.224 l-1 2 a.25 .25 0 1 0 .447 .224 l1 -2Z M7.302 3.7 a.7 .7 0 0 1 1.4 0 v1.088 l.942 -.544 a.7 .7 0 1 1 .7 1.212 L9.402 6 l.942 .544 a.7 .7 0 0 1 -.7 1.212 l-.942 -.544 V8.3 a.7 .7 0 1 1 -1.4 0 V7.212 l-.942 .544 a.7 .7 0 0 1 -.7 -1.212 L6.602 6 l-.942 -.544 a.7 .7 0 0 1 .7 -1.212 l.942 .544 V3.7Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 2.724 12.362
                moveTo(x = 2.724f, y = 12.362f)
                // a 0.25 0.25 0 0 0 -0.448 -0.224
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.448f,
                    dy1 = -0.224f,
                )
                // l -1 2
                lineToRelative(dx = -1.0f, dy = 2.0f)
                // a 0.25 0.25 0 0 0 0.448 0.224
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.448f,
                    dy1 = 0.224f,
                )
                // l 1 -2z
                lineToRelative(dx = 1.0f, dy = -2.0f)
                close()
                // m 4 1
                moveToRelative(dx = 4.0f, dy = 1.0f)
                // a 0.25 0.25 0 0 0 -0.447 -0.224
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.447f,
                    dy1 = -0.224f,
                )
                // l -1 2
                lineToRelative(dx = -1.0f, dy = 2.0f)
                // a 0.25 0.25 0 0 0 0.447 0.224
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.447f,
                    dy1 = 0.224f,
                )
                // l 1 -2z
                lineToRelative(dx = 1.0f, dy = -2.0f)
                close()
                // m 3.888 -0.336
                moveToRelative(dx = 3.888f, dy = -0.336f)
                // a 0.25 0.25 0 0 1 0.112 0.336
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.112f,
                    dy1 = 0.336f,
                )
                // l -1 2
                lineToRelative(dx = -1.0f, dy = 2.0f)
                // a 0.25 0.25 0 0 1 -0.447 -0.224
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.447f,
                    dy1 = -0.224f,
                )
                // l 1 -2
                lineToRelative(dx = 1.0f, dy = -2.0f)
                // a 0.25 0.25 0 0 1 0.335 -0.112z
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.335f,
                    dy1 = -0.112f,
                )
                close()
                // m 4.112 -0.664
                moveToRelative(dx = 4.112f, dy = -0.664f)
                // a 0.25 0.25 0 0 0 -0.447 -0.224
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.447f,
                    dy1 = -0.224f,
                )
                // l -1 2
                lineToRelative(dx = -1.0f, dy = 2.0f)
                // a 0.25 0.25 0 1 0 0.447 0.224
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.447f,
                    dy1 = 0.224f,
                )
                // l 1 -2z
                lineToRelative(dx = 1.0f, dy = -2.0f)
                close()
                // M 7.302 3.7
                moveTo(x = 7.302f, y = 3.7f)
                // a 0.7 0.7 0 0 1 1.4 0
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.4f,
                    dy1 = 0.0f,
                )
                // v 1.088
                verticalLineToRelative(dy = 1.088f)
                // l 0.942 -0.544
                lineToRelative(dx = 0.942f, dy = -0.544f)
                // a 0.7 0.7 0 1 1 0.7 1.212
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.7f,
                    dy1 = 1.212f,
                )
                // L 9.402 6
                lineTo(x = 9.402f, y = 6.0f)
                // l 0.942 0.544
                lineToRelative(dx = 0.942f, dy = 0.544f)
                // a 0.7 0.7 0 0 1 -0.7 1.212
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.7f,
                    dy1 = 1.212f,
                )
                // l -0.942 -0.544
                lineToRelative(dx = -0.942f, dy = -0.544f)
                // V 8.3
                verticalLineTo(y = 8.3f)
                // a 0.7 0.7 0 1 1 -1.4 0
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.4f,
                    dy1 = 0.0f,
                )
                // V 7.212
                verticalLineTo(y = 7.212f)
                // l -0.942 0.544
                lineToRelative(dx = -0.942f, dy = 0.544f)
                // a 0.7 0.7 0 0 1 -0.7 -1.212
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.7f,
                    dy1 = -1.212f,
                )
                // L 6.602 6
                lineTo(x = 6.602f, y = 6.0f)
                // l -0.942 -0.544
                lineToRelative(dx = -0.942f, dy = -0.544f)
                // a 0.7 0.7 0 0 1 0.7 -1.212
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.7f,
                    dy1 = -1.212f,
                )
                // l 0.942 0.544
                lineToRelative(dx = 0.942f, dy = 0.544f)
                // V 3.7z
                verticalLineTo(y = 3.7f)
                close()
            }
            // M12.987 2.807 8.262 .071 a.52 .52 0 0 0 -.525 0 L3.013 2.807 a.529 .529 0 0 0 -.263 .457 v5.472 c0 .188 .1 .362 .263 .457 l4.724 2.736 a.516 .516 0 0 0 .525 0 l4.724 -2.736 a.527 .527 0 0 0 .264 -.457 V3.264 a.529 .529 0 0 0 -.263 -.457Z m-.79 5.625 L8 10.863 3.802 8.432 V3.569 L8 1.137 l4.198 2.432 v4.863Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 12.987 2.807
                moveTo(x = 12.987f, y = 2.807f)
                // L 8.262 0.071
                lineTo(x = 8.262f, y = 0.071f)
                // a 0.52 0.52 0 0 0 -0.525 0
                arcToRelative(
                    a = 0.52f,
                    b = 0.52f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.525f,
                    dy1 = 0.0f,
                )
                // L 3.013 2.807
                lineTo(x = 3.013f, y = 2.807f)
                // a 0.529 0.529 0 0 0 -0.263 0.457
                arcToRelative(
                    a = 0.529f,
                    b = 0.529f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.263f,
                    dy1 = 0.457f,
                )
                // v 5.472
                verticalLineToRelative(dy = 5.472f)
                // c 0 0.188 0.1 0.362 0.263 0.457
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.188f,
                    dx2 = 0.1f,
                    dy2 = 0.362f,
                    dx3 = 0.263f,
                    dy3 = 0.457f,
                )
                // l 4.724 2.736
                lineToRelative(dx = 4.724f, dy = 2.736f)
                // a 0.516 0.516 0 0 0 0.525 0
                arcToRelative(
                    a = 0.516f,
                    b = 0.516f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.525f,
                    dy1 = 0.0f,
                )
                // l 4.724 -2.736
                lineToRelative(dx = 4.724f, dy = -2.736f)
                // a 0.527 0.527 0 0 0 0.264 -0.457
                arcToRelative(
                    a = 0.527f,
                    b = 0.527f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.264f,
                    dy1 = -0.457f,
                )
                // V 3.264
                verticalLineTo(y = 3.264f)
                // a 0.529 0.529 0 0 0 -0.263 -0.457z
                arcToRelative(
                    a = 0.529f,
                    b = 0.529f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.263f,
                    dy1 = -0.457f,
                )
                close()
                // m -0.79 5.625
                moveToRelative(dx = -0.79f, dy = 5.625f)
                // L 8 10.863
                lineTo(x = 8.0f, y = 10.863f)
                // L 3.802 8.432
                lineTo(x = 3.802f, y = 8.432f)
                // V 3.569
                verticalLineTo(y = 3.569f)
                // L 8 1.137
                lineTo(x = 8.0f, y = 1.137f)
                // l 4.198 2.432
                lineToRelative(dx = 4.198f, dy = 2.432f)
                // v 4.863z
                verticalLineToRelative(dy = 4.863f)
                close()
            }
        }.build().also { _ic2214 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2214: ImageVector? = null
