package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1060: ImageVector
    get() {
        val current = _ic1060
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1060",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8 9.8 a1.8 1.8 0 1 1 0 -3.6 1.8 1.8 0 0 1 0 3.6Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8 9.8
                moveTo(x = 8.0f, y = 9.8f)
                // a 1.8 1.8 0 1 1 0 -3.6
                arcToRelative(
                    a = 1.8f,
                    b = 1.8f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -3.6f,
                )
                // a 1.8 1.8 0 0 1 0 3.6z
                arcToRelative(
                    a = 1.8f,
                    b = 1.8f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 3.6f,
                )
                close()
            }
            // M14 8 c0 1.167 -3.59 3.5 -6 3.5 S2 9.167 2 8 c0 -1.167 3.59 -3.5 6 -3.5 s6 2.333 6 3.5Z m-1.25 -.211 .001 -.003 -.004 .008 a.11 .11 0 0 0 .003 -.005Z m-.259 .1 c-.278 -.286 -.71 -.623 -1.245 -.949 C10.14 6.267 8.875 5.812 8 5.812 s-2.14 .455 -3.246 1.128 c-.536 .326 -.967 .663 -1.245 .948 -.04 .04 -.074 .078 -.104 .112 .03 .034 .064 .071 .104 .112 .278 .285 .71 .622 1.245 .948 C5.86 9.733 7.125 10.187 8 10.187 s2.14 -.454 3.246 -1.127 c.536 -.326 .967 -.663 1.245 -.948 .04 -.04 .075 -.078 .104 -.112 a2.783 2.783 0 0 0 -.104 -.112Z M0 0 v4 h1.5 V1.5 H4 V0 H0Z m16 0 v4 h-1.5 V1.5 H12 V0 h4Z M0 16 v-4 h1.5 v2.5 H4 V16 H0Z m16 0 h-4 v-1.5 h2.5 V12 H16 v4Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 14 8
                moveTo(x = 14.0f, y = 8.0f)
                // c 0 1.167 -3.59 3.5 -6 3.5
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.167f,
                    dx2 = -3.59f,
                    dy2 = 3.5f,
                    dx3 = -6.0f,
                    dy3 = 3.5f,
                )
                // S 2 9.167 2 8
                reflectiveCurveTo(
                    x1 = 2.0f,
                    y1 = 9.167f,
                    x2 = 2.0f,
                    y2 = 8.0f,
                )
                // c 0 -1.167 3.59 -3.5 6 -3.5
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.167f,
                    dx2 = 3.59f,
                    dy2 = -3.5f,
                    dx3 = 6.0f,
                    dy3 = -3.5f,
                )
                // s 6 2.333 6 3.5z
                reflectiveCurveToRelative(
                    dx1 = 6.0f,
                    dy1 = 2.333f,
                    dx2 = 6.0f,
                    dy2 = 3.5f,
                )
                close()
                // m -1.25 -0.211
                moveToRelative(dx = -1.25f, dy = -0.211f)
                // l 0.001 -0.003
                lineToRelative(dx = 0.001f, dy = -0.003f)
                // l -0.004 0.008
                lineToRelative(dx = -0.004f, dy = 0.008f)
                // a 0.11 0.11 0 0 0 0.003 -0.005z
                arcToRelative(
                    a = 0.11f,
                    b = 0.11f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.003f,
                    dy1 = -0.005f,
                )
                close()
                // m -0.259 0.1
                moveToRelative(dx = -0.259f, dy = 0.1f)
                // c -0.278 -0.286 -0.71 -0.623 -1.245 -0.949
                curveToRelative(
                    dx1 = -0.278f,
                    dy1 = -0.286f,
                    dx2 = -0.71f,
                    dy2 = -0.623f,
                    dx3 = -1.245f,
                    dy3 = -0.949f,
                )
                // C 10.14 6.267 8.875 5.812 8 5.812
                curveTo(
                    x1 = 10.14f,
                    y1 = 6.267f,
                    x2 = 8.875f,
                    y2 = 5.812f,
                    x3 = 8.0f,
                    y3 = 5.812f,
                )
                // s -2.14 0.455 -3.246 1.128
                reflectiveCurveToRelative(
                    dx1 = -2.14f,
                    dy1 = 0.455f,
                    dx2 = -3.246f,
                    dy2 = 1.128f,
                )
                // c -0.536 0.326 -0.967 0.663 -1.245 0.948
                curveToRelative(
                    dx1 = -0.536f,
                    dy1 = 0.326f,
                    dx2 = -0.967f,
                    dy2 = 0.663f,
                    dx3 = -1.245f,
                    dy3 = 0.948f,
                )
                // c -0.04 0.04 -0.074 0.078 -0.104 0.112
                curveToRelative(
                    dx1 = -0.04f,
                    dy1 = 0.04f,
                    dx2 = -0.074f,
                    dy2 = 0.078f,
                    dx3 = -0.104f,
                    dy3 = 0.112f,
                )
                // c 0.03 0.034 0.064 0.071 0.104 0.112
                curveToRelative(
                    dx1 = 0.03f,
                    dy1 = 0.034f,
                    dx2 = 0.064f,
                    dy2 = 0.071f,
                    dx3 = 0.104f,
                    dy3 = 0.112f,
                )
                // c 0.278 0.285 0.71 0.622 1.245 0.948
                curveToRelative(
                    dx1 = 0.278f,
                    dy1 = 0.285f,
                    dx2 = 0.71f,
                    dy2 = 0.622f,
                    dx3 = 1.245f,
                    dy3 = 0.948f,
                )
                // C 5.86 9.733 7.125 10.187 8 10.187
                curveTo(
                    x1 = 5.86f,
                    y1 = 9.733f,
                    x2 = 7.125f,
                    y2 = 10.187f,
                    x3 = 8.0f,
                    y3 = 10.187f,
                )
                // s 2.14 -0.454 3.246 -1.127
                reflectiveCurveToRelative(
                    dx1 = 2.14f,
                    dy1 = -0.454f,
                    dx2 = 3.246f,
                    dy2 = -1.127f,
                )
                // c 0.536 -0.326 0.967 -0.663 1.245 -0.948
                curveToRelative(
                    dx1 = 0.536f,
                    dy1 = -0.326f,
                    dx2 = 0.967f,
                    dy2 = -0.663f,
                    dx3 = 1.245f,
                    dy3 = -0.948f,
                )
                // c 0.04 -0.04 0.075 -0.078 0.104 -0.112
                curveToRelative(
                    dx1 = 0.04f,
                    dy1 = -0.04f,
                    dx2 = 0.075f,
                    dy2 = -0.078f,
                    dx3 = 0.104f,
                    dy3 = -0.112f,
                )
                // a 2.783 2.783 0 0 0 -0.104 -0.112z
                arcToRelative(
                    a = 2.783f,
                    b = 2.783f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.104f,
                    dy1 = -0.112f,
                )
                close()
                // M 0 0
                moveTo(x = 0.0f, y = 0.0f)
                // v 4
                verticalLineToRelative(dy = 4.0f)
                // h 1.5
                horizontalLineToRelative(dx = 1.5f)
                // V 1.5
                verticalLineTo(y = 1.5f)
                // H 4
                horizontalLineTo(x = 4.0f)
                // V 0
                verticalLineTo(y = 0.0f)
                // H 0z
                horizontalLineTo(x = 0.0f)
                close()
                // m 16 0
                moveToRelative(dx = 16.0f, dy = 0.0f)
                // v 4
                verticalLineToRelative(dy = 4.0f)
                // h -1.5
                horizontalLineToRelative(dx = -1.5f)
                // V 1.5
                verticalLineTo(y = 1.5f)
                // H 12
                horizontalLineTo(x = 12.0f)
                // V 0
                verticalLineTo(y = 0.0f)
                // h 4z
                horizontalLineToRelative(dx = 4.0f)
                close()
                // M 0 16
                moveTo(x = 0.0f, y = 16.0f)
                // v -4
                verticalLineToRelative(dy = -4.0f)
                // h 1.5
                horizontalLineToRelative(dx = 1.5f)
                // v 2.5
                verticalLineToRelative(dy = 2.5f)
                // H 4
                horizontalLineTo(x = 4.0f)
                // V 16
                verticalLineTo(y = 16.0f)
                // H 0z
                horizontalLineTo(x = 0.0f)
                close()
                // m 16 0
                moveToRelative(dx = 16.0f, dy = 0.0f)
                // h -4
                horizontalLineToRelative(dx = -4.0f)
                // v -1.5
                verticalLineToRelative(dy = -1.5f)
                // h 2.5
                horizontalLineToRelative(dx = 2.5f)
                // V 12
                verticalLineTo(y = 12.0f)
                // H 16
                horizontalLineTo(x = 16.0f)
                // v 4z
                verticalLineToRelative(dy = 4.0f)
                close()
            }
            // m12.773 4.288 -8.485 8.485 -1.061 -1.06 8.485 -8.486 1.06 1.06Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 12.773 4.288
                moveTo(x = 12.773f, y = 4.288f)
                // l -8.485 8.485
                lineToRelative(dx = -8.485f, dy = 8.485f)
                // l -1.061 -1.06
                lineToRelative(dx = -1.061f, dy = -1.06f)
                // l 8.485 -8.486
                lineToRelative(dx = 8.485f, dy = -8.486f)
                // l 1.06 1.06z
                lineToRelative(dx = 1.06f, dy = 1.06f)
                close()
            }
        }.build().also { _ic1060 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1060: ImageVector? = null
