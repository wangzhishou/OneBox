package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2151: ImageVector
    get() {
        val current = _ic2151
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2151",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M11.313 .313 a.312 .312 0 1 0 -.626 0 v.808 l-.403 -.404 a.312 .312 0 1 0 -.442 .441 l.845 .846 v1.121 c0 .009 0 .017 .002 .026 a1.873 1.873 0 0 0 -1.135 .655 .32 .32 0 0 0 -.022 -.014 l-.97 -.56 -.31 -1.156 a.312 .312 0 1 0 -.603 .162 l.148 .552 -.7 -.404 a.313 .313 0 0 0 -.313 .54 l.7 .405 -.552 .148 a.313 .313 0 0 0 .162 .603 l1.156 -.31 .97 .561 a.319 .319 0 0 0 .023 .012 1.871 1.871 0 0 0 0 1.31 .32 .32 0 0 0 -.023 .012 l-.97 .56 -1.156 -.31 a.312 .312 0 1 0 -.162 .604 l.552 .148 -.7 .404 a.312 .312 0 1 0 .313 .541 l.7 -.404 -.148 .552 a.312 .312 0 0 0 .603 .162 l.31 -1.156 .97 -.56 a.316 .316 0 0 0 .022 -.014 c.282 .34 .68 .58 1.135 .655 a.313 .313 0 0 0 -.002 .026 v1.12 l-.845 .847 a.312 .312 0 0 0 .441 .441 l.405 -.404 v.809 a.313 .313 0 0 0 .624 0 v-.809 l.405 .404 a.312 .312 0 1 0 .441 -.441 l-.845 -.846 V6.875 c0 -.009 0 -.017 -.002 -.026 a1.873 1.873 0 0 0 1.135 -.655 .32 .32 0 0 0 .022 .014 l.97 .56 .31 1.156 a.312 .312 0 1 0 .603 -.162 l-.148 -.552 .7 .404 a.313 .313 0 0 0 .313 -.54 l-.7 -.405 .552 -.148 a.313 .313 0 0 0 -.162 -.603 l-1.155 .31 -.97 -.561 a.315 .315 0 0 0 -.024 -.012 1.872 1.872 0 0 0 0 -1.31 .311 .311 0 0 0 .023 -.012 l.97 -.56 1.156 .31 a.312 .312 0 1 0 .162 -.604 l-.552 -.148 .7 -.404 a.312 .312 0 1 0 -.313 -.541 l-.7 .404 .148 -.552 a.312 .312 0 0 0 -.603 -.162 l-.31 1.156 -.97 .56 a.324 .324 0 0 0 -.022 .014 1.87 1.87 0 0 0 -1.134 -.655 V2.005 l.847 -.847 a.312 .312 0 1 0 -.442 -.441 l-.405 .404 V.313Z M3.535 10.467 a.467 .467 0 1 1 .933 0 v.725 l.628 -.363 a.467 .467 0 1 1 .467 .808 L4.935 12 l.628 .363 a.467 .467 0 0 1 -.467 .808 l-.628 -.363 v.725 a.467 .467 0 1 1 -.933 0 v-.725 l-.628 .363 a.467 .467 0 1 1 -.467 -.808 L3.068 12 l-.628 -.363 a.467 .467 0 0 1 .467 -.808 l.628 .363 v-.725Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.313 0.313
                moveTo(x = 11.313f, y = 0.313f)
                // a 0.312 0.312 0 1 0 -0.626 0
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.626f,
                    dy1 = 0.0f,
                )
                // v 0.808
                verticalLineToRelative(dy = 0.808f)
                // l -0.403 -0.404
                lineToRelative(dx = -0.403f, dy = -0.404f)
                // a 0.312 0.312 0 1 0 -0.442 0.441
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.442f,
                    dy1 = 0.441f,
                )
                // l 0.845 0.846
                lineToRelative(dx = 0.845f, dy = 0.846f)
                // v 1.121
                verticalLineToRelative(dy = 1.121f)
                // c 0 0.009 0 0.017 0.002 0.026
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.009f,
                    dx2 = 0.0f,
                    dy2 = 0.017f,
                    dx3 = 0.002f,
                    dy3 = 0.026f,
                )
                // a 1.873 1.873 0 0 0 -1.135 0.655
                arcToRelative(
                    a = 1.873f,
                    b = 1.873f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.135f,
                    dy1 = 0.655f,
                )
                // a 0.32 0.32 0 0 0 -0.022 -0.014
                arcToRelative(
                    a = 0.32f,
                    b = 0.32f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.022f,
                    dy1 = -0.014f,
                )
                // l -0.97 -0.56
                lineToRelative(dx = -0.97f, dy = -0.56f)
                // l -0.31 -1.156
                lineToRelative(dx = -0.31f, dy = -1.156f)
                // a 0.312 0.312 0 1 0 -0.603 0.162
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.603f,
                    dy1 = 0.162f,
                )
                // l 0.148 0.552
                lineToRelative(dx = 0.148f, dy = 0.552f)
                // l -0.7 -0.404
                lineToRelative(dx = -0.7f, dy = -0.404f)
                // a 0.313 0.313 0 0 0 -0.313 0.54
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.313f,
                    dy1 = 0.54f,
                )
                // l 0.7 0.405
                lineToRelative(dx = 0.7f, dy = 0.405f)
                // l -0.552 0.148
                lineToRelative(dx = -0.552f, dy = 0.148f)
                // a 0.313 0.313 0 0 0 0.162 0.603
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.162f,
                    dy1 = 0.603f,
                )
                // l 1.156 -0.31
                lineToRelative(dx = 1.156f, dy = -0.31f)
                // l 0.97 0.561
                lineToRelative(dx = 0.97f, dy = 0.561f)
                // a 0.319 0.319 0 0 0 0.023 0.012
                arcToRelative(
                    a = 0.319f,
                    b = 0.319f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.023f,
                    dy1 = 0.012f,
                )
                // a 1.871 1.871 0 0 0 0 1.31
                arcToRelative(
                    a = 1.871f,
                    b = 1.871f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.31f,
                )
                // a 0.32 0.32 0 0 0 -0.023 0.012
                arcToRelative(
                    a = 0.32f,
                    b = 0.32f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.023f,
                    dy1 = 0.012f,
                )
                // l -0.97 0.56
                lineToRelative(dx = -0.97f, dy = 0.56f)
                // l -1.156 -0.31
                lineToRelative(dx = -1.156f, dy = -0.31f)
                // a 0.312 0.312 0 1 0 -0.162 0.604
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.162f,
                    dy1 = 0.604f,
                )
                // l 0.552 0.148
                lineToRelative(dx = 0.552f, dy = 0.148f)
                // l -0.7 0.404
                lineToRelative(dx = -0.7f, dy = 0.404f)
                // a 0.312 0.312 0 1 0 0.313 0.541
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.313f,
                    dy1 = 0.541f,
                )
                // l 0.7 -0.404
                lineToRelative(dx = 0.7f, dy = -0.404f)
                // l -0.148 0.552
                lineToRelative(dx = -0.148f, dy = 0.552f)
                // a 0.312 0.312 0 0 0 0.603 0.162
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.603f,
                    dy1 = 0.162f,
                )
                // l 0.31 -1.156
                lineToRelative(dx = 0.31f, dy = -1.156f)
                // l 0.97 -0.56
                lineToRelative(dx = 0.97f, dy = -0.56f)
                // a 0.316 0.316 0 0 0 0.022 -0.014
                arcToRelative(
                    a = 0.316f,
                    b = 0.316f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.022f,
                    dy1 = -0.014f,
                )
                // c 0.282 0.34 0.68 0.58 1.135 0.655
                curveToRelative(
                    dx1 = 0.282f,
                    dy1 = 0.34f,
                    dx2 = 0.68f,
                    dy2 = 0.58f,
                    dx3 = 1.135f,
                    dy3 = 0.655f,
                )
                // a 0.313 0.313 0 0 0 -0.002 0.026
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.002f,
                    dy1 = 0.026f,
                )
                // v 1.12
                verticalLineToRelative(dy = 1.12f)
                // l -0.845 0.847
                lineToRelative(dx = -0.845f, dy = 0.847f)
                // a 0.312 0.312 0 0 0 0.441 0.441
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.441f,
                    dy1 = 0.441f,
                )
                // l 0.405 -0.404
                lineToRelative(dx = 0.405f, dy = -0.404f)
                // v 0.809
                verticalLineToRelative(dy = 0.809f)
                // a 0.313 0.313 0 0 0 0.624 0
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.624f,
                    dy1 = 0.0f,
                )
                // v -0.809
                verticalLineToRelative(dy = -0.809f)
                // l 0.405 0.404
                lineToRelative(dx = 0.405f, dy = 0.404f)
                // a 0.312 0.312 0 1 0 0.441 -0.441
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.441f,
                    dy1 = -0.441f,
                )
                // l -0.845 -0.846
                lineToRelative(dx = -0.845f, dy = -0.846f)
                // V 6.875
                verticalLineTo(y = 6.875f)
                // c 0 -0.009 0 -0.017 -0.002 -0.026
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.009f,
                    dx2 = 0.0f,
                    dy2 = -0.017f,
                    dx3 = -0.002f,
                    dy3 = -0.026f,
                )
                // a 1.873 1.873 0 0 0 1.135 -0.655
                arcToRelative(
                    a = 1.873f,
                    b = 1.873f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.135f,
                    dy1 = -0.655f,
                )
                // a 0.32 0.32 0 0 0 0.022 0.014
                arcToRelative(
                    a = 0.32f,
                    b = 0.32f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.022f,
                    dy1 = 0.014f,
                )
                // l 0.97 0.56
                lineToRelative(dx = 0.97f, dy = 0.56f)
                // l 0.31 1.156
                lineToRelative(dx = 0.31f, dy = 1.156f)
                // a 0.312 0.312 0 1 0 0.603 -0.162
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.603f,
                    dy1 = -0.162f,
                )
                // l -0.148 -0.552
                lineToRelative(dx = -0.148f, dy = -0.552f)
                // l 0.7 0.404
                lineToRelative(dx = 0.7f, dy = 0.404f)
                // a 0.313 0.313 0 0 0 0.313 -0.54
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.313f,
                    dy1 = -0.54f,
                )
                // l -0.7 -0.405
                lineToRelative(dx = -0.7f, dy = -0.405f)
                // l 0.552 -0.148
                lineToRelative(dx = 0.552f, dy = -0.148f)
                // a 0.313 0.313 0 0 0 -0.162 -0.603
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.162f,
                    dy1 = -0.603f,
                )
                // l -1.155 0.31
                lineToRelative(dx = -1.155f, dy = 0.31f)
                // l -0.97 -0.561
                lineToRelative(dx = -0.97f, dy = -0.561f)
                // a 0.315 0.315 0 0 0 -0.024 -0.012
                arcToRelative(
                    a = 0.315f,
                    b = 0.315f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.024f,
                    dy1 = -0.012f,
                )
                // a 1.872 1.872 0 0 0 0 -1.31
                arcToRelative(
                    a = 1.872f,
                    b = 1.872f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.31f,
                )
                // a 0.311 0.311 0 0 0 0.023 -0.012
                arcToRelative(
                    a = 0.311f,
                    b = 0.311f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.023f,
                    dy1 = -0.012f,
                )
                // l 0.97 -0.56
                lineToRelative(dx = 0.97f, dy = -0.56f)
                // l 1.156 0.31
                lineToRelative(dx = 1.156f, dy = 0.31f)
                // a 0.312 0.312 0 1 0 0.162 -0.604
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.162f,
                    dy1 = -0.604f,
                )
                // l -0.552 -0.148
                lineToRelative(dx = -0.552f, dy = -0.148f)
                // l 0.7 -0.404
                lineToRelative(dx = 0.7f, dy = -0.404f)
                // a 0.312 0.312 0 1 0 -0.313 -0.541
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.313f,
                    dy1 = -0.541f,
                )
                // l -0.7 0.404
                lineToRelative(dx = -0.7f, dy = 0.404f)
                // l 0.148 -0.552
                lineToRelative(dx = 0.148f, dy = -0.552f)
                // a 0.312 0.312 0 0 0 -0.603 -0.162
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.603f,
                    dy1 = -0.162f,
                )
                // l -0.31 1.156
                lineToRelative(dx = -0.31f, dy = 1.156f)
                // l -0.97 0.56
                lineToRelative(dx = -0.97f, dy = 0.56f)
                // a 0.324 0.324 0 0 0 -0.022 0.014
                arcToRelative(
                    a = 0.324f,
                    b = 0.324f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.022f,
                    dy1 = 0.014f,
                )
                // a 1.87 1.87 0 0 0 -1.134 -0.655
                arcToRelative(
                    a = 1.87f,
                    b = 1.87f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.134f,
                    dy1 = -0.655f,
                )
                // V 2.005
                verticalLineTo(y = 2.005f)
                // l 0.847 -0.847
                lineToRelative(dx = 0.847f, dy = -0.847f)
                // a 0.312 0.312 0 1 0 -0.442 -0.441
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.442f,
                    dy1 = -0.441f,
                )
                // l -0.405 0.404
                lineToRelative(dx = -0.405f, dy = 0.404f)
                // V 0.313z
                verticalLineTo(y = 0.313f)
                close()
                // M 3.535 10.467
                moveTo(x = 3.535f, y = 10.467f)
                // a 0.467 0.467 0 1 1 0.933 0
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.933f,
                    dy1 = 0.0f,
                )
                // v 0.725
                verticalLineToRelative(dy = 0.725f)
                // l 0.628 -0.363
                lineToRelative(dx = 0.628f, dy = -0.363f)
                // a 0.467 0.467 0 1 1 0.467 0.808
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.467f,
                    dy1 = 0.808f,
                )
                // L 4.935 12
                lineTo(x = 4.935f, y = 12.0f)
                // l 0.628 0.363
                lineToRelative(dx = 0.628f, dy = 0.363f)
                // a 0.467 0.467 0 0 1 -0.467 0.808
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.467f,
                    dy1 = 0.808f,
                )
                // l -0.628 -0.363
                lineToRelative(dx = -0.628f, dy = -0.363f)
                // v 0.725
                verticalLineToRelative(dy = 0.725f)
                // a 0.467 0.467 0 1 1 -0.933 0
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.933f,
                    dy1 = 0.0f,
                )
                // v -0.725
                verticalLineToRelative(dy = -0.725f)
                // l -0.628 0.363
                lineToRelative(dx = -0.628f, dy = 0.363f)
                // a 0.467 0.467 0 1 1 -0.467 -0.808
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.467f,
                    dy1 = -0.808f,
                )
                // L 3.068 12
                lineTo(x = 3.068f, y = 12.0f)
                // l -0.628 -0.363
                lineToRelative(dx = -0.628f, dy = -0.363f)
                // a 0.467 0.467 0 0 1 0.467 -0.808
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.467f,
                    dy1 = -0.808f,
                )
                // l 0.628 0.363
                lineToRelative(dx = 0.628f, dy = 0.363f)
                // v -0.725z
                verticalLineToRelative(dy = -0.725f)
                close()
            }
            // M7.324 9.872 4.175 8.048 a.347 .347 0 0 0 -.35 0 L.675 9.872 a.352 .352 0 0 0 -.175 .304 v3.648 c0 .126 .067 .242 .175 .305 l3.15 1.824 a.344 .344 0 0 0 .35 0 l3.149 -1.824 a.351 .351 0 0 0 .176 -.305 v-3.648 a.352 .352 0 0 0 -.176 -.304Z m-.526 3.75 L4 15.241 l-2.798 -1.62 V10.38 L4 8.758 l2.798 1.621 v3.242Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.324 9.872
                moveTo(x = 7.324f, y = 9.872f)
                // L 4.175 8.048
                lineTo(x = 4.175f, y = 8.048f)
                // a 0.347 0.347 0 0 0 -0.35 0
                arcToRelative(
                    a = 0.347f,
                    b = 0.347f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = 0.0f,
                )
                // L 0.675 9.872
                lineTo(x = 0.675f, y = 9.872f)
                // a 0.352 0.352 0 0 0 -0.175 0.304
                arcToRelative(
                    a = 0.352f,
                    b = 0.352f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.175f,
                    dy1 = 0.304f,
                )
                // v 3.648
                verticalLineToRelative(dy = 3.648f)
                // c 0 0.126 0.067 0.242 0.175 0.305
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.126f,
                    dx2 = 0.067f,
                    dy2 = 0.242f,
                    dx3 = 0.175f,
                    dy3 = 0.305f,
                )
                // l 3.15 1.824
                lineToRelative(dx = 3.15f, dy = 1.824f)
                // a 0.344 0.344 0 0 0 0.35 0
                arcToRelative(
                    a = 0.344f,
                    b = 0.344f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.35f,
                    dy1 = 0.0f,
                )
                // l 3.149 -1.824
                lineToRelative(dx = 3.149f, dy = -1.824f)
                // a 0.351 0.351 0 0 0 0.176 -0.305
                arcToRelative(
                    a = 0.351f,
                    b = 0.351f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.176f,
                    dy1 = -0.305f,
                )
                // v -3.648
                verticalLineToRelative(dy = -3.648f)
                // a 0.352 0.352 0 0 0 -0.176 -0.304z
                arcToRelative(
                    a = 0.352f,
                    b = 0.352f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.176f,
                    dy1 = -0.304f,
                )
                close()
                // m -0.526 3.75
                moveToRelative(dx = -0.526f, dy = 3.75f)
                // L 4 15.241
                lineTo(x = 4.0f, y = 15.241f)
                // l -2.798 -1.62
                lineToRelative(dx = -2.798f, dy = -1.62f)
                // V 10.38
                verticalLineTo(y = 10.38f)
                // L 4 8.758
                lineTo(x = 4.0f, y = 8.758f)
                // l 2.798 1.621
                lineToRelative(dx = 2.798f, dy = 1.621f)
                // v 3.242z
                verticalLineToRelative(dy = 3.242f)
                close()
            }
        }.build().also { _ic2151 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2151: ImageVector? = null
