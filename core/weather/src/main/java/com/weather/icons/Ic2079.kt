package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2079: ImageVector
    get() {
        val current = _ic2079
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2079",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.9 10 a4.99 4.99 0 0 0 3.827 -1.783 3 3 0 1 0 .553 -5.63 A4.999 4.999 0 0 0 7.9 0 a4.998 4.998 0 0 0 -4.359 2.549 3 3 0 1 0 .586 5.732 A4.988 4.988 0 0 0 7.9 10Z m-.791 -6.398 c-.057 -.362 .17 -.8 .496 -.997 .256 -.153 .551 -.133 .806 .023 l.07 .042 a.846 .846 0 0 1 .409 .853 L8.532 5.8 H7.454 L7.11 3.602Z M8.599 6.9 a.6 .6 0 1 1 -1.2 0 .6 .6 0 0 1 1.2 0Z m-6.064 3.567 a.467 .467 0 1 1 .933 0 v.725 l.628 -.363 a.467 .467 0 1 1 .467 .808 L3.935 12 l.628 .363 a.467 .467 0 0 1 -.467 .808 l-.628 -.363 v.725 a.467 .467 0 1 1 -.933 0 v-.725 l-.628 .363 a.467 .467 0 1 1 -.467 -.808 L2.068 12 l-.628 -.363 a.467 .467 0 0 1 .467 -.808 l.628 .363 v-.725Z m5 2 a.467 .467 0 1 1 .933 0 v.725 l.628 -.363 a.467 .467 0 1 1 .467 .808 L8.935 14 l.628 .363 a.467 .467 0 0 1 -.467 .808 l-.628 -.363 v.725 a.467 .467 0 1 1 -.933 0 v-.725 l-.628 .363 a.467 .467 0 1 1 -.467 -.808 L7.068 14 l-.628 -.363 a.467 .467 0 0 1 .467 -.808 l.628 .363 v-.725Z M13.001 10 a.467 .467 0 0 0 -.466 .467 v.725 l-.628 -.363 a.467 .467 0 1 0 -.467 .808 l.628 .363 -.628 .363 a.467 .467 0 0 0 .467 .808 l.628 -.363 v.725 a.467 .467 0 0 0 .933 0 v-.725 l.628 .363 a.467 .467 0 1 0 .467 -.808 L13.935 12 l.628 -.363 a.467 .467 0 0 0 -.467 -.808 l-.628 .363 v-.725 a.467 .467 0 0 0 -.467 -.467Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.9 10
                moveTo(x = 7.9f, y = 10.0f)
                // a 4.99 4.99 0 0 0 3.827 -1.783
                arcToRelative(
                    a = 4.99f,
                    b = 4.99f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 3.827f,
                    dy1 = -1.783f,
                )
                // a 3 3 0 1 0 0.553 -5.63
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.553f,
                    dy1 = -5.63f,
                )
                // A 4.999 4.999 0 0 0 7.9 0
                arcTo(
                    horizontalEllipseRadius = 4.999f,
                    verticalEllipseRadius = 4.999f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.9f,
                    y1 = 0.0f,
                )
                // a 4.998 4.998 0 0 0 -4.359 2.549
                arcToRelative(
                    a = 4.998f,
                    b = 4.998f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -4.359f,
                    dy1 = 2.549f,
                )
                // a 3 3 0 1 0 0.586 5.732
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.586f,
                    dy1 = 5.732f,
                )
                // A 4.988 4.988 0 0 0 7.9 10z
                arcTo(
                    horizontalEllipseRadius = 4.988f,
                    verticalEllipseRadius = 4.988f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.9f,
                    y1 = 10.0f,
                )
                close()
                // m -0.791 -6.398
                moveToRelative(dx = -0.791f, dy = -6.398f)
                // c -0.057 -0.362 0.17 -0.8 0.496 -0.997
                curveToRelative(
                    dx1 = -0.057f,
                    dy1 = -0.362f,
                    dx2 = 0.17f,
                    dy2 = -0.8f,
                    dx3 = 0.496f,
                    dy3 = -0.997f,
                )
                // c 0.256 -0.153 0.551 -0.133 0.806 0.023
                curveToRelative(
                    dx1 = 0.256f,
                    dy1 = -0.153f,
                    dx2 = 0.551f,
                    dy2 = -0.133f,
                    dx3 = 0.806f,
                    dy3 = 0.023f,
                )
                // l 0.07 0.042
                lineToRelative(dx = 0.07f, dy = 0.042f)
                // a 0.846 0.846 0 0 1 0.409 0.853
                arcToRelative(
                    a = 0.846f,
                    b = 0.846f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.409f,
                    dy1 = 0.853f,
                )
                // L 8.532 5.8
                lineTo(x = 8.532f, y = 5.8f)
                // H 7.454
                horizontalLineTo(x = 7.454f)
                // L 7.11 3.602z
                lineTo(x = 7.11f, y = 3.602f)
                close()
                // M 8.599 6.9
                moveTo(x = 8.599f, y = 6.9f)
                // a 0.6 0.6 0 1 1 -1.2 0
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.2f,
                    dy1 = 0.0f,
                )
                // a 0.6 0.6 0 0 1 1.2 0z
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.2f,
                    dy1 = 0.0f,
                )
                close()
                // m -6.064 3.567
                moveToRelative(dx = -6.064f, dy = 3.567f)
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
                // L 3.935 12
                lineTo(x = 3.935f, y = 12.0f)
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
                // L 2.068 12
                lineTo(x = 2.068f, y = 12.0f)
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
                // m 5 2
                moveToRelative(dx = 5.0f, dy = 2.0f)
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
                // L 8.935 14
                lineTo(x = 8.935f, y = 14.0f)
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
                // L 7.068 14
                lineTo(x = 7.068f, y = 14.0f)
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
                // M 13.001 10
                moveTo(x = 13.001f, y = 10.0f)
                // a 0.467 0.467 0 0 0 -0.466 0.467
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.466f,
                    dy1 = 0.467f,
                )
                // v 0.725
                verticalLineToRelative(dy = 0.725f)
                // l -0.628 -0.363
                lineToRelative(dx = -0.628f, dy = -0.363f)
                // a 0.467 0.467 0 1 0 -0.467 0.808
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.467f,
                    dy1 = 0.808f,
                )
                // l 0.628 0.363
                lineToRelative(dx = 0.628f, dy = 0.363f)
                // l -0.628 0.363
                lineToRelative(dx = -0.628f, dy = 0.363f)
                // a 0.467 0.467 0 0 0 0.467 0.808
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.467f,
                    dy1 = 0.808f,
                )
                // l 0.628 -0.363
                lineToRelative(dx = 0.628f, dy = -0.363f)
                // v 0.725
                verticalLineToRelative(dy = 0.725f)
                // a 0.467 0.467 0 0 0 0.933 0
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.933f,
                    dy1 = 0.0f,
                )
                // v -0.725
                verticalLineToRelative(dy = -0.725f)
                // l 0.628 0.363
                lineToRelative(dx = 0.628f, dy = 0.363f)
                // a 0.467 0.467 0 1 0 0.467 -0.808
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.467f,
                    dy1 = -0.808f,
                )
                // L 13.935 12
                lineTo(x = 13.935f, y = 12.0f)
                // l 0.628 -0.363
                lineToRelative(dx = 0.628f, dy = -0.363f)
                // a 0.467 0.467 0 0 0 -0.467 -0.808
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.467f,
                    dy1 = -0.808f,
                )
                // l -0.628 0.363
                lineToRelative(dx = -0.628f, dy = 0.363f)
                // v -0.725
                verticalLineToRelative(dy = -0.725f)
                // a 0.467 0.467 0 0 0 -0.467 -0.467z
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.467f,
                    dy1 = -0.467f,
                )
                close()
            }
        }.build().also { _ic2079 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2079: ImageVector? = null
