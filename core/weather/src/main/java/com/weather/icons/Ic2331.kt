package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2331: ImageVector
    get() {
        val current = _ic2331
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2331",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M1 0 H0 v16 h1 v-2.936 c.6 .627 1.6 .851 3.762 .323 C6.782 12.894 9 14 9 14 V8.608 c-2.305 -.87 -3.427 -.607 -5.143 -.203 l-.243 .056 C1.756 8.895 1 8 1 8 V6.064 c.6 .627 1.6 .851 3.762 .323 C6.782 5.894 9 7 9 7 V1.608 c-2.305 -.87 -3.427 -.607 -5.143 -.203 l-.243 .056 C1.756 1.894 1 1 1 1 V0Z m5.6 2.125 v3 L3.4 5.5 v-3 l3.2 -.375Z m0 7 v3 l-3.2 .375 v-3 l3.2 -.375Z M13.17 .473 a.197 .197 0 0 0 -.34 0 l-2.804 4.86 c-.075 .13 .02 .292 .17 .292 h5.607 a.194 .194 0 0 0 .17 -.291 L13.17 .473Z m-.637 1.608 c-.024 -.212 .192 -.393 .467 -.393 s.491 .181 .467 .393 l-.211 1.857 h-.512 l-.21 -1.857Z m.845 2.607 a.375 .375 0 1 1 -.75 0 .375 .375 0 0 1 .75 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 1 0
                moveTo(x = 1.0f, y = 0.0f)
                // H 0
                horizontalLineTo(x = 0.0f)
                // v 16
                verticalLineToRelative(dy = 16.0f)
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // v -2.936
                verticalLineToRelative(dy = -2.936f)
                // c 0.6 0.627 1.6 0.851 3.762 0.323
                curveToRelative(
                    dx1 = 0.6f,
                    dy1 = 0.627f,
                    dx2 = 1.6f,
                    dy2 = 0.851f,
                    dx3 = 3.762f,
                    dy3 = 0.323f,
                )
                // C 6.782 12.894 9 14 9 14
                curveTo(
                    x1 = 6.782f,
                    y1 = 12.894f,
                    x2 = 9.0f,
                    y2 = 14.0f,
                    x3 = 9.0f,
                    y3 = 14.0f,
                )
                // V 8.608
                verticalLineTo(y = 8.608f)
                // c -2.305 -0.87 -3.427 -0.607 -5.143 -0.203
                curveToRelative(
                    dx1 = -2.305f,
                    dy1 = -0.87f,
                    dx2 = -3.427f,
                    dy2 = -0.607f,
                    dx3 = -5.143f,
                    dy3 = -0.203f,
                )
                // l -0.243 0.056
                lineToRelative(dx = -0.243f, dy = 0.056f)
                // C 1.756 8.895 1 8 1 8
                curveTo(
                    x1 = 1.756f,
                    y1 = 8.895f,
                    x2 = 1.0f,
                    y2 = 8.0f,
                    x3 = 1.0f,
                    y3 = 8.0f,
                )
                // V 6.064
                verticalLineTo(y = 6.064f)
                // c 0.6 0.627 1.6 0.851 3.762 0.323
                curveToRelative(
                    dx1 = 0.6f,
                    dy1 = 0.627f,
                    dx2 = 1.6f,
                    dy2 = 0.851f,
                    dx3 = 3.762f,
                    dy3 = 0.323f,
                )
                // C 6.782 5.894 9 7 9 7
                curveTo(
                    x1 = 6.782f,
                    y1 = 5.894f,
                    x2 = 9.0f,
                    y2 = 7.0f,
                    x3 = 9.0f,
                    y3 = 7.0f,
                )
                // V 1.608
                verticalLineTo(y = 1.608f)
                // c -2.305 -0.87 -3.427 -0.607 -5.143 -0.203
                curveToRelative(
                    dx1 = -2.305f,
                    dy1 = -0.87f,
                    dx2 = -3.427f,
                    dy2 = -0.607f,
                    dx3 = -5.143f,
                    dy3 = -0.203f,
                )
                // l -0.243 0.056
                lineToRelative(dx = -0.243f, dy = 0.056f)
                // C 1.756 1.894 1 1 1 1
                curveTo(
                    x1 = 1.756f,
                    y1 = 1.894f,
                    x2 = 1.0f,
                    y2 = 1.0f,
                    x3 = 1.0f,
                    y3 = 1.0f,
                )
                // V 0z
                verticalLineTo(y = 0.0f)
                close()
                // m 5.6 2.125
                moveToRelative(dx = 5.6f, dy = 2.125f)
                // v 3
                verticalLineToRelative(dy = 3.0f)
                // L 3.4 5.5
                lineTo(x = 3.4f, y = 5.5f)
                // v -3
                verticalLineToRelative(dy = -3.0f)
                // l 3.2 -0.375z
                lineToRelative(dx = 3.2f, dy = -0.375f)
                close()
                // m 0 7
                moveToRelative(dx = 0.0f, dy = 7.0f)
                // v 3
                verticalLineToRelative(dy = 3.0f)
                // l -3.2 0.375
                lineToRelative(dx = -3.2f, dy = 0.375f)
                // v -3
                verticalLineToRelative(dy = -3.0f)
                // l 3.2 -0.375z
                lineToRelative(dx = 3.2f, dy = -0.375f)
                close()
                // M 13.17 0.473
                moveTo(x = 13.17f, y = 0.473f)
                // a 0.197 0.197 0 0 0 -0.34 0
                arcToRelative(
                    a = 0.197f,
                    b = 0.197f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.34f,
                    dy1 = 0.0f,
                )
                // l -2.804 4.86
                lineToRelative(dx = -2.804f, dy = 4.86f)
                // c -0.075 0.13 0.02 0.292 0.17 0.292
                curveToRelative(
                    dx1 = -0.075f,
                    dy1 = 0.13f,
                    dx2 = 0.02f,
                    dy2 = 0.292f,
                    dx3 = 0.17f,
                    dy3 = 0.292f,
                )
                // h 5.607
                horizontalLineToRelative(dx = 5.607f)
                // a 0.194 0.194 0 0 0 0.17 -0.291
                arcToRelative(
                    a = 0.194f,
                    b = 0.194f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.17f,
                    dy1 = -0.291f,
                )
                // L 13.17 0.473z
                lineTo(x = 13.17f, y = 0.473f)
                close()
                // m -0.637 1.608
                moveToRelative(dx = -0.637f, dy = 1.608f)
                // c -0.024 -0.212 0.192 -0.393 0.467 -0.393
                curveToRelative(
                    dx1 = -0.024f,
                    dy1 = -0.212f,
                    dx2 = 0.192f,
                    dy2 = -0.393f,
                    dx3 = 0.467f,
                    dy3 = -0.393f,
                )
                // s 0.491 0.181 0.467 0.393
                reflectiveCurveToRelative(
                    dx1 = 0.491f,
                    dy1 = 0.181f,
                    dx2 = 0.467f,
                    dy2 = 0.393f,
                )
                // l -0.211 1.857
                lineToRelative(dx = -0.211f, dy = 1.857f)
                // h -0.512
                horizontalLineToRelative(dx = -0.512f)
                // l -0.21 -1.857z
                lineToRelative(dx = -0.21f, dy = -1.857f)
                close()
                // m 0.845 2.607
                moveToRelative(dx = 0.845f, dy = 2.607f)
                // a 0.375 0.375 0 1 1 -0.75 0
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.75f,
                    dy1 = 0.0f,
                )
                // a 0.375 0.375 0 0 1 0.75 0z
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.75f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic2331 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2331: ImageVector? = null
