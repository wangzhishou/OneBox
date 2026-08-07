package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1022: ImageVector
    get() {
        val current = _ic1022
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1022",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M2.121 .352 C1.873 .01 1.337 -.103 .924 .104 .51 .31 .377 .756 .624 1.099 l3.09 4.281 a.21 .21 0 0 1 -.017 .27 l-2.63 2.915 C.641 9.038 .541 9.66 .805 10.21 L2.628 14 H1 a1 1 0 1 0 0 2 h14 a1 1 0 1 0 0 -2 h-.492 l-2.082 -4.33 a.209 .209 0 0 1 .037 -.234 l2.63 -2.915 c.496 -.551 .543 -1.297 .117 -1.888 L12.12 .353 c-.248 -.344 -.784 -.456 -1.197 -.25 -.413 .207 -.547 .653 -.3 .996 l3.089 4.281 a.21 .21 0 0 1 -.017 .27 l-2.629 2.915 c-.426 .473 -.526 1.096 -.262 1.645 L12.63 14 H9.51 L7.425 9.67 a.209 .209 0 0 1 .037 -.234 l2.63 -2.915 c.496 -.551 .543 -1.297 .117 -1.888 L7.12 .353 c-.248 -.344 -.784 -.456 -1.197 -.25 -.413 .207 -.547 .653 -.3 .996 L8.714 5.38 a.21 .21 0 0 1 -.017 .27 l-2.63 2.915 c-.426 .473 -.526 1.096 -.262 1.645 L7.628 14 h-3.12 L2.426 9.67 a.209 .209 0 0 1 .037 -.234 l2.63 -2.915 c.497 -.551 .543 -1.297 .117 -1.888 L2.12 .353Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 2.121 0.352
                moveTo(x = 2.121f, y = 0.352f)
                // C 1.873 0.01 1.337 -0.103 0.924 0.104
                curveTo(
                    x1 = 1.873f,
                    y1 = 0.01f,
                    x2 = 1.337f,
                    y2 = -0.103f,
                    x3 = 0.924f,
                    y3 = 0.104f,
                )
                // C 0.51 0.31 0.377 0.756 0.624 1.099
                curveTo(
                    x1 = 0.51f,
                    y1 = 0.31f,
                    x2 = 0.377f,
                    y2 = 0.756f,
                    x3 = 0.624f,
                    y3 = 1.099f,
                )
                // l 3.09 4.281
                lineToRelative(dx = 3.09f, dy = 4.281f)
                // a 0.21 0.21 0 0 1 -0.017 0.27
                arcToRelative(
                    a = 0.21f,
                    b = 0.21f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.017f,
                    dy1 = 0.27f,
                )
                // l -2.63 2.915
                lineToRelative(dx = -2.63f, dy = 2.915f)
                // C 0.641 9.038 0.541 9.66 0.805 10.21
                curveTo(
                    x1 = 0.641f,
                    y1 = 9.038f,
                    x2 = 0.541f,
                    y2 = 9.66f,
                    x3 = 0.805f,
                    y3 = 10.21f,
                )
                // L 2.628 14
                lineTo(x = 2.628f, y = 14.0f)
                // H 1
                horizontalLineTo(x = 1.0f)
                // a 1 1 0 1 0 0 2
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 2.0f,
                )
                // h 14
                horizontalLineToRelative(dx = 14.0f)
                // a 1 1 0 1 0 0 -2
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -2.0f,
                )
                // h -0.492
                horizontalLineToRelative(dx = -0.492f)
                // l -2.082 -4.33
                lineToRelative(dx = -2.082f, dy = -4.33f)
                // a 0.209 0.209 0 0 1 0.037 -0.234
                arcToRelative(
                    a = 0.209f,
                    b = 0.209f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.037f,
                    dy1 = -0.234f,
                )
                // l 2.63 -2.915
                lineToRelative(dx = 2.63f, dy = -2.915f)
                // c 0.496 -0.551 0.543 -1.297 0.117 -1.888
                curveToRelative(
                    dx1 = 0.496f,
                    dy1 = -0.551f,
                    dx2 = 0.543f,
                    dy2 = -1.297f,
                    dx3 = 0.117f,
                    dy3 = -1.888f,
                )
                // L 12.12 0.353
                lineTo(x = 12.12f, y = 0.353f)
                // c -0.248 -0.344 -0.784 -0.456 -1.197 -0.25
                curveToRelative(
                    dx1 = -0.248f,
                    dy1 = -0.344f,
                    dx2 = -0.784f,
                    dy2 = -0.456f,
                    dx3 = -1.197f,
                    dy3 = -0.25f,
                )
                // c -0.413 0.207 -0.547 0.653 -0.3 0.996
                curveToRelative(
                    dx1 = -0.413f,
                    dy1 = 0.207f,
                    dx2 = -0.547f,
                    dy2 = 0.653f,
                    dx3 = -0.3f,
                    dy3 = 0.996f,
                )
                // l 3.089 4.281
                lineToRelative(dx = 3.089f, dy = 4.281f)
                // a 0.21 0.21 0 0 1 -0.017 0.27
                arcToRelative(
                    a = 0.21f,
                    b = 0.21f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.017f,
                    dy1 = 0.27f,
                )
                // l -2.629 2.915
                lineToRelative(dx = -2.629f, dy = 2.915f)
                // c -0.426 0.473 -0.526 1.096 -0.262 1.645
                curveToRelative(
                    dx1 = -0.426f,
                    dy1 = 0.473f,
                    dx2 = -0.526f,
                    dy2 = 1.096f,
                    dx3 = -0.262f,
                    dy3 = 1.645f,
                )
                // L 12.63 14
                lineTo(x = 12.63f, y = 14.0f)
                // H 9.51
                horizontalLineTo(x = 9.51f)
                // L 7.425 9.67
                lineTo(x = 7.425f, y = 9.67f)
                // a 0.209 0.209 0 0 1 0.037 -0.234
                arcToRelative(
                    a = 0.209f,
                    b = 0.209f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.037f,
                    dy1 = -0.234f,
                )
                // l 2.63 -2.915
                lineToRelative(dx = 2.63f, dy = -2.915f)
                // c 0.496 -0.551 0.543 -1.297 0.117 -1.888
                curveToRelative(
                    dx1 = 0.496f,
                    dy1 = -0.551f,
                    dx2 = 0.543f,
                    dy2 = -1.297f,
                    dx3 = 0.117f,
                    dy3 = -1.888f,
                )
                // L 7.12 0.353
                lineTo(x = 7.12f, y = 0.353f)
                // c -0.248 -0.344 -0.784 -0.456 -1.197 -0.25
                curveToRelative(
                    dx1 = -0.248f,
                    dy1 = -0.344f,
                    dx2 = -0.784f,
                    dy2 = -0.456f,
                    dx3 = -1.197f,
                    dy3 = -0.25f,
                )
                // c -0.413 0.207 -0.547 0.653 -0.3 0.996
                curveToRelative(
                    dx1 = -0.413f,
                    dy1 = 0.207f,
                    dx2 = -0.547f,
                    dy2 = 0.653f,
                    dx3 = -0.3f,
                    dy3 = 0.996f,
                )
                // L 8.714 5.38
                lineTo(x = 8.714f, y = 5.38f)
                // a 0.21 0.21 0 0 1 -0.017 0.27
                arcToRelative(
                    a = 0.21f,
                    b = 0.21f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.017f,
                    dy1 = 0.27f,
                )
                // l -2.63 2.915
                lineToRelative(dx = -2.63f, dy = 2.915f)
                // c -0.426 0.473 -0.526 1.096 -0.262 1.645
                curveToRelative(
                    dx1 = -0.426f,
                    dy1 = 0.473f,
                    dx2 = -0.526f,
                    dy2 = 1.096f,
                    dx3 = -0.262f,
                    dy3 = 1.645f,
                )
                // L 7.628 14
                lineTo(x = 7.628f, y = 14.0f)
                // h -3.12
                horizontalLineToRelative(dx = -3.12f)
                // L 2.426 9.67
                lineTo(x = 2.426f, y = 9.67f)
                // a 0.209 0.209 0 0 1 0.037 -0.234
                arcToRelative(
                    a = 0.209f,
                    b = 0.209f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.037f,
                    dy1 = -0.234f,
                )
                // l 2.63 -2.915
                lineToRelative(dx = 2.63f, dy = -2.915f)
                // c 0.497 -0.551 0.543 -1.297 0.117 -1.888
                curveToRelative(
                    dx1 = 0.497f,
                    dy1 = -0.551f,
                    dx2 = 0.543f,
                    dy2 = -1.297f,
                    dx3 = 0.117f,
                    dy3 = -1.888f,
                )
                // L 2.12 0.353z
                lineTo(x = 2.12f, y = 0.353f)
                close()
            }
        }.build().also { _ic1022 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1022: ImageVector? = null
