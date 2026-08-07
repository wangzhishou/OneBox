package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2129: ImageVector
    get() {
        val current = _ic2129
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2129",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.313 .313 a.312 .312 0 1 0 -.625 0 v.808 L7.284 .717 a.312 .312 0 1 0 -.442 .441 l.846 .846 v1.147 a1.873 1.873 0 0 0 -1.134 .655 .32 .32 0 0 0 -.022 -.014 l-.97 -.56 -.31 -1.156 a.312 .312 0 1 0 -.603 .162 l.148 .552 -.7 -.404 a.313 .313 0 0 0 -.313 .54 l.7 .405 -.552 .148 a.313 .313 0 0 0 .162 .603 l1.156 -.31 .97 .561 a.319 .319 0 0 0 .023 .012 1.871 1.871 0 0 0 0 1.31 .32 .32 0 0 0 -.023 .012 l-.97 .56 -1.156 -.31 a.312 .312 0 1 0 -.162 .604 l.552 .148 -.7 .404 a.312 .312 0 1 0 .313 .541 l.7 -.404 -.148 .552 a.312 .312 0 0 0 .603 .162 l.31 -1.156 .97 -.56 a.316 .316 0 0 0 .022 -.014 c.282 .34 .68 .58 1.135 .655 a.318 .318 0 0 0 -.001 .026 v1.12 l-.846 .847 a.312 .312 0 1 0 .442 .441 l.404 -.404 v.809 a.312 .312 0 1 0 .625 0 v-.809 l.404 .404 a.312 .312 0 1 0 .442 -.441 l-.846 -.846 V6.875 c0 -.009 0 -.017 -.002 -.026 a1.873 1.873 0 0 0 1.135 -.655 l.022 .014 .97 .56 .31 1.156 a.312 .312 0 1 0 .603 -.162 l-.148 -.552 .7 .404 a.313 .313 0 0 0 .313 -.54 l-.7 -.405 .552 -.148 a.313 .313 0 0 0 -.162 -.603 l-1.155 .31 -.97 -.561 a.317 .317 0 0 0 -.024 -.012 1.871 1.871 0 0 0 0 -1.31 .313 .313 0 0 0 .023 -.012 l.97 -.56 1.156 .31 a.312 .312 0 1 0 .162 -.604 l-.552 -.148 .7 -.404 a.312 .312 0 1 0 -.313 -.541 l-.7 .404 .148 -.552 a.312 .312 0 0 0 -.603 -.162 l-.31 1.156 -.97 .56 a.324 .324 0 0 0 -.022 .014 1.87 1.87 0 0 0 -1.134 -.655 V2.005 l.847 -.847 a.312 .312 0 1 0 -.442 -.441 l-.404 .404 V.313Z m-5.63 10.692 -.832 -.555 L.69 12.191 l1 2 -.839 1.259 .832 .555 1.161 -1.742 -1 -2 .839 -1.258Z m4.154 0 -.832 -.555 -1.161 1.741 1 2 -.84 1.259 .833 .555 1.16 -1.742 -1 -2 .84 -1.258Z m3.322 -.555 .832 .555 -.84 1.258 1 2 -1.16 1.742 -.832 -.555 .839 -1.259 -1 -2 1.16 -1.741Z m4.986 .555 -.832 -.555 -1.161 1.741 1 2 -.84 1.259 .833 .555 1.16 -1.742 -1 -2 .84 -1.258Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.313 0.313
                moveTo(x = 8.313f, y = 0.313f)
                // a 0.312 0.312 0 1 0 -0.625 0
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.625f,
                    dy1 = 0.0f,
                )
                // v 0.808
                verticalLineToRelative(dy = 0.808f)
                // L 7.284 0.717
                lineTo(x = 7.284f, y = 0.717f)
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
                // l 0.846 0.846
                lineToRelative(dx = 0.846f, dy = 0.846f)
                // v 1.147
                verticalLineToRelative(dy = 1.147f)
                // a 1.873 1.873 0 0 0 -1.134 0.655
                arcToRelative(
                    a = 1.873f,
                    b = 1.873f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.134f,
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
                // a 0.318 0.318 0 0 0 -0.001 0.026
                arcToRelative(
                    a = 0.318f,
                    b = 0.318f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.001f,
                    dy1 = 0.026f,
                )
                // v 1.12
                verticalLineToRelative(dy = 1.12f)
                // l -0.846 0.847
                lineToRelative(dx = -0.846f, dy = 0.847f)
                // a 0.312 0.312 0 1 0 0.442 0.441
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.442f,
                    dy1 = 0.441f,
                )
                // l 0.404 -0.404
                lineToRelative(dx = 0.404f, dy = -0.404f)
                // v 0.809
                verticalLineToRelative(dy = 0.809f)
                // a 0.312 0.312 0 1 0 0.625 0
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.625f,
                    dy1 = 0.0f,
                )
                // v -0.809
                verticalLineToRelative(dy = -0.809f)
                // l 0.404 0.404
                lineToRelative(dx = 0.404f, dy = 0.404f)
                // a 0.312 0.312 0 1 0 0.442 -0.441
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.442f,
                    dy1 = -0.441f,
                )
                // l -0.846 -0.846
                lineToRelative(dx = -0.846f, dy = -0.846f)
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
                // l 0.022 0.014
                lineToRelative(dx = 0.022f, dy = 0.014f)
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
                // a 0.317 0.317 0 0 0 -0.024 -0.012
                arcToRelative(
                    a = 0.317f,
                    b = 0.317f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.024f,
                    dy1 = -0.012f,
                )
                // a 1.871 1.871 0 0 0 0 -1.31
                arcToRelative(
                    a = 1.871f,
                    b = 1.871f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.31f,
                )
                // a 0.313 0.313 0 0 0 0.023 -0.012
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
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
                // l -0.404 0.404
                lineToRelative(dx = -0.404f, dy = 0.404f)
                // V 0.313z
                verticalLineTo(y = 0.313f)
                close()
                // m -5.63 10.692
                moveToRelative(dx = -5.63f, dy = 10.692f)
                // l -0.832 -0.555
                lineToRelative(dx = -0.832f, dy = -0.555f)
                // L 0.69 12.191
                lineTo(x = 0.69f, y = 12.191f)
                // l 1 2
                lineToRelative(dx = 1.0f, dy = 2.0f)
                // l -0.839 1.259
                lineToRelative(dx = -0.839f, dy = 1.259f)
                // l 0.832 0.555
                lineToRelative(dx = 0.832f, dy = 0.555f)
                // l 1.161 -1.742
                lineToRelative(dx = 1.161f, dy = -1.742f)
                // l -1 -2
                lineToRelative(dx = -1.0f, dy = -2.0f)
                // l 0.839 -1.258z
                lineToRelative(dx = 0.839f, dy = -1.258f)
                close()
                // m 4.154 0
                moveToRelative(dx = 4.154f, dy = 0.0f)
                // l -0.832 -0.555
                lineToRelative(dx = -0.832f, dy = -0.555f)
                // l -1.161 1.741
                lineToRelative(dx = -1.161f, dy = 1.741f)
                // l 1 2
                lineToRelative(dx = 1.0f, dy = 2.0f)
                // l -0.84 1.259
                lineToRelative(dx = -0.84f, dy = 1.259f)
                // l 0.833 0.555
                lineToRelative(dx = 0.833f, dy = 0.555f)
                // l 1.16 -1.742
                lineToRelative(dx = 1.16f, dy = -1.742f)
                // l -1 -2
                lineToRelative(dx = -1.0f, dy = -2.0f)
                // l 0.84 -1.258z
                lineToRelative(dx = 0.84f, dy = -1.258f)
                close()
                // m 3.322 -0.555
                moveToRelative(dx = 3.322f, dy = -0.555f)
                // l 0.832 0.555
                lineToRelative(dx = 0.832f, dy = 0.555f)
                // l -0.84 1.258
                lineToRelative(dx = -0.84f, dy = 1.258f)
                // l 1 2
                lineToRelative(dx = 1.0f, dy = 2.0f)
                // l -1.16 1.742
                lineToRelative(dx = -1.16f, dy = 1.742f)
                // l -0.832 -0.555
                lineToRelative(dx = -0.832f, dy = -0.555f)
                // l 0.839 -1.259
                lineToRelative(dx = 0.839f, dy = -1.259f)
                // l -1 -2
                lineToRelative(dx = -1.0f, dy = -2.0f)
                // l 1.16 -1.741z
                lineToRelative(dx = 1.16f, dy = -1.741f)
                close()
                // m 4.986 0.555
                moveToRelative(dx = 4.986f, dy = 0.555f)
                // l -0.832 -0.555
                lineToRelative(dx = -0.832f, dy = -0.555f)
                // l -1.161 1.741
                lineToRelative(dx = -1.161f, dy = 1.741f)
                // l 1 2
                lineToRelative(dx = 1.0f, dy = 2.0f)
                // l -0.84 1.259
                lineToRelative(dx = -0.84f, dy = 1.259f)
                // l 0.833 0.555
                lineToRelative(dx = 0.833f, dy = 0.555f)
                // l 1.16 -1.742
                lineToRelative(dx = 1.16f, dy = -1.742f)
                // l -1 -2
                lineToRelative(dx = -1.0f, dy = -2.0f)
                // l 0.84 -1.258z
                lineToRelative(dx = 0.84f, dy = -1.258f)
                close()
            }
        }.build().also { _ic2129 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2129: ImageVector? = null
