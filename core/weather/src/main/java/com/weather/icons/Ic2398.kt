package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2398: ImageVector
    get() {
        val current = _ic2398
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2398",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8 2.875 14.3 13.8 H1.7 L8 2.875Z m.455 -1.614 a.526 .526 0 0 0 -.91 0 L.07 14.224 c-.2 .346 .052 .776 .454 .776 h14.952 c.402 0 .654 -.43 .454 -.776 L8.455 1.26Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8 2.875
                moveTo(x = 8.0f, y = 2.875f)
                // L 14.3 13.8
                lineTo(x = 14.3f, y = 13.8f)
                // H 1.7
                horizontalLineTo(x = 1.7f)
                // L 8 2.875z
                lineTo(x = 8.0f, y = 2.875f)
                close()
                // m 0.455 -1.614
                moveToRelative(dx = 0.455f, dy = -1.614f)
                // a 0.526 0.526 0 0 0 -0.91 0
                arcToRelative(
                    a = 0.526f,
                    b = 0.526f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.91f,
                    dy1 = 0.0f,
                )
                // L 0.07 14.224
                lineTo(x = 0.07f, y = 14.224f)
                // c -0.2 0.346 0.052 0.776 0.454 0.776
                curveToRelative(
                    dx1 = -0.2f,
                    dy1 = 0.346f,
                    dx2 = 0.052f,
                    dy2 = 0.776f,
                    dx3 = 0.454f,
                    dy3 = 0.776f,
                )
                // h 14.952
                horizontalLineToRelative(dx = 14.952f)
                // c 0.402 0 0.654 -0.43 0.454 -0.776
                curveToRelative(
                    dx1 = 0.402f,
                    dy1 = 0.0f,
                    dx2 = 0.654f,
                    dy2 = -0.43f,
                    dx3 = 0.454f,
                    dy3 = -0.776f,
                )
                // L 8.455 1.26z
                lineTo(x = 8.455f, y = 1.26f)
                close()
            }
            // M8.188 7.188 a.187 .187 0 1 0 -.376 0 v.484 L7.57 7.43 a.187 .187 0 1 0 -.265 .265 l.508 .508 v.687 a1.124 1.124 0 0 0 -.68 .393 .192 .192 0 0 0 -.014 -.008 l-.582 -.336 -.186 -.693 a.188 .188 0 0 0 -.362 .097 l.089 .33 -.42 -.242 a.187 .187 0 1 0 -.187 .325 L5.89 9 l-.332 .088 a.188 .188 0 0 0 .097 .363 l.694 -.186 .582 .336 a.193 .193 0 0 0 .014 .007 1.123 1.123 0 0 0 0 .786 .195 .195 0 0 0 -.014 .007 l-.582 .336 -.694 -.186 a.188 .188 0 0 0 -.097 .363 L5.89 11 l-.42 .243 a.187 .187 0 1 0 .188 .325 l.42 -.243 -.089 .331 a.187 .187 0 1 0 .362 .097 l.186 -.693 .582 -.336 a.161 .161 0 0 0 .014 -.008 c.168 .204 .408 .347 .68 .393 v.687 l-.508 .508 a.188 .188 0 0 0 .265 .265 l.242 -.242 v.485 a.187 .187 0 1 0 .375 0 v-.485 l.243 .242 a.188 .188 0 0 0 .265 -.265 l-.508 -.508 v-.687 c.272 -.046 .512 -.19 .68 -.393 a.195 .195 0 0 0 .014 .008 l.582 .336 .186 .693 a.188 .188 0 0 0 .362 -.097 l-.089 -.33 .42 .242 a.188 .188 0 1 0 .187 -.325 l-.42 -.242 .332 -.09 a.188 .188 0 0 0 -.097 -.362 l-.694 .186 -.582 -.336 a.196 .196 0 0 0 -.014 -.007 1.123 1.123 0 0 0 0 -.786 .194 .194 0 0 0 .014 -.007 l.582 -.336 .694 .186 a.187 .187 0 1 0 .097 -.363 L10.11 9 l.42 -.243 a.188 .188 0 0 0 -.188 -.325 l-.42 .243 .089 -.331 a.187 .187 0 1 0 -.362 -.097 l-.186 .693 -.582 .336 a.187 .187 0 0 0 -.014 .008 1.124 1.124 0 0 0 -.68 -.393 v-.687 l.508 -.508 a.187 .187 0 1 0 -.265 -.265 l-.242 .242 v-.484Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.188 7.188
                moveTo(x = 8.188f, y = 7.188f)
                // a 0.187 0.187 0 1 0 -0.376 0
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.376f,
                    dy1 = 0.0f,
                )
                // v 0.484
                verticalLineToRelative(dy = 0.484f)
                // L 7.57 7.43
                lineTo(x = 7.57f, y = 7.43f)
                // a 0.187 0.187 0 1 0 -0.265 0.265
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.265f,
                    dy1 = 0.265f,
                )
                // l 0.508 0.508
                lineToRelative(dx = 0.508f, dy = 0.508f)
                // v 0.687
                verticalLineToRelative(dy = 0.687f)
                // a 1.124 1.124 0 0 0 -0.68 0.393
                arcToRelative(
                    a = 1.124f,
                    b = 1.124f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.68f,
                    dy1 = 0.393f,
                )
                // a 0.192 0.192 0 0 0 -0.014 -0.008
                arcToRelative(
                    a = 0.192f,
                    b = 0.192f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.014f,
                    dy1 = -0.008f,
                )
                // l -0.582 -0.336
                lineToRelative(dx = -0.582f, dy = -0.336f)
                // l -0.186 -0.693
                lineToRelative(dx = -0.186f, dy = -0.693f)
                // a 0.188 0.188 0 0 0 -0.362 0.097
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.362f,
                    dy1 = 0.097f,
                )
                // l 0.089 0.33
                lineToRelative(dx = 0.089f, dy = 0.33f)
                // l -0.42 -0.242
                lineToRelative(dx = -0.42f, dy = -0.242f)
                // a 0.187 0.187 0 1 0 -0.187 0.325
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.187f,
                    dy1 = 0.325f,
                )
                // L 5.89 9
                lineTo(x = 5.89f, y = 9.0f)
                // l -0.332 0.088
                lineToRelative(dx = -0.332f, dy = 0.088f)
                // a 0.188 0.188 0 0 0 0.097 0.363
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.097f,
                    dy1 = 0.363f,
                )
                // l 0.694 -0.186
                lineToRelative(dx = 0.694f, dy = -0.186f)
                // l 0.582 0.336
                lineToRelative(dx = 0.582f, dy = 0.336f)
                // a 0.193 0.193 0 0 0 0.014 0.007
                arcToRelative(
                    a = 0.193f,
                    b = 0.193f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.014f,
                    dy1 = 0.007f,
                )
                // a 1.123 1.123 0 0 0 0 0.786
                arcToRelative(
                    a = 1.123f,
                    b = 1.123f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.786f,
                )
                // a 0.195 0.195 0 0 0 -0.014 0.007
                arcToRelative(
                    a = 0.195f,
                    b = 0.195f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.014f,
                    dy1 = 0.007f,
                )
                // l -0.582 0.336
                lineToRelative(dx = -0.582f, dy = 0.336f)
                // l -0.694 -0.186
                lineToRelative(dx = -0.694f, dy = -0.186f)
                // a 0.188 0.188 0 0 0 -0.097 0.363
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.097f,
                    dy1 = 0.363f,
                )
                // L 5.89 11
                lineTo(x = 5.89f, y = 11.0f)
                // l -0.42 0.243
                lineToRelative(dx = -0.42f, dy = 0.243f)
                // a 0.187 0.187 0 1 0 0.188 0.325
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.188f,
                    dy1 = 0.325f,
                )
                // l 0.42 -0.243
                lineToRelative(dx = 0.42f, dy = -0.243f)
                // l -0.089 0.331
                lineToRelative(dx = -0.089f, dy = 0.331f)
                // a 0.187 0.187 0 1 0 0.362 0.097
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.362f,
                    dy1 = 0.097f,
                )
                // l 0.186 -0.693
                lineToRelative(dx = 0.186f, dy = -0.693f)
                // l 0.582 -0.336
                lineToRelative(dx = 0.582f, dy = -0.336f)
                // a 0.161 0.161 0 0 0 0.014 -0.008
                arcToRelative(
                    a = 0.161f,
                    b = 0.161f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.014f,
                    dy1 = -0.008f,
                )
                // c 0.168 0.204 0.408 0.347 0.68 0.393
                curveToRelative(
                    dx1 = 0.168f,
                    dy1 = 0.204f,
                    dx2 = 0.408f,
                    dy2 = 0.347f,
                    dx3 = 0.68f,
                    dy3 = 0.393f,
                )
                // v 0.687
                verticalLineToRelative(dy = 0.687f)
                // l -0.508 0.508
                lineToRelative(dx = -0.508f, dy = 0.508f)
                // a 0.188 0.188 0 0 0 0.265 0.265
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.265f,
                    dy1 = 0.265f,
                )
                // l 0.242 -0.242
                lineToRelative(dx = 0.242f, dy = -0.242f)
                // v 0.485
                verticalLineToRelative(dy = 0.485f)
                // a 0.187 0.187 0 1 0 0.375 0
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.375f,
                    dy1 = 0.0f,
                )
                // v -0.485
                verticalLineToRelative(dy = -0.485f)
                // l 0.243 0.242
                lineToRelative(dx = 0.243f, dy = 0.242f)
                // a 0.188 0.188 0 0 0 0.265 -0.265
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.265f,
                    dy1 = -0.265f,
                )
                // l -0.508 -0.508
                lineToRelative(dx = -0.508f, dy = -0.508f)
                // v -0.687
                verticalLineToRelative(dy = -0.687f)
                // c 0.272 -0.046 0.512 -0.19 0.68 -0.393
                curveToRelative(
                    dx1 = 0.272f,
                    dy1 = -0.046f,
                    dx2 = 0.512f,
                    dy2 = -0.19f,
                    dx3 = 0.68f,
                    dy3 = -0.393f,
                )
                // a 0.195 0.195 0 0 0 0.014 0.008
                arcToRelative(
                    a = 0.195f,
                    b = 0.195f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.014f,
                    dy1 = 0.008f,
                )
                // l 0.582 0.336
                lineToRelative(dx = 0.582f, dy = 0.336f)
                // l 0.186 0.693
                lineToRelative(dx = 0.186f, dy = 0.693f)
                // a 0.188 0.188 0 0 0 0.362 -0.097
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.362f,
                    dy1 = -0.097f,
                )
                // l -0.089 -0.33
                lineToRelative(dx = -0.089f, dy = -0.33f)
                // l 0.42 0.242
                lineToRelative(dx = 0.42f, dy = 0.242f)
                // a 0.188 0.188 0 1 0 0.187 -0.325
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.187f,
                    dy1 = -0.325f,
                )
                // l -0.42 -0.242
                lineToRelative(dx = -0.42f, dy = -0.242f)
                // l 0.332 -0.09
                lineToRelative(dx = 0.332f, dy = -0.09f)
                // a 0.188 0.188 0 0 0 -0.097 -0.362
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.097f,
                    dy1 = -0.362f,
                )
                // l -0.694 0.186
                lineToRelative(dx = -0.694f, dy = 0.186f)
                // l -0.582 -0.336
                lineToRelative(dx = -0.582f, dy = -0.336f)
                // a 0.196 0.196 0 0 0 -0.014 -0.007
                arcToRelative(
                    a = 0.196f,
                    b = 0.196f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.014f,
                    dy1 = -0.007f,
                )
                // a 1.123 1.123 0 0 0 0 -0.786
                arcToRelative(
                    a = 1.123f,
                    b = 1.123f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.786f,
                )
                // a 0.194 0.194 0 0 0 0.014 -0.007
                arcToRelative(
                    a = 0.194f,
                    b = 0.194f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.014f,
                    dy1 = -0.007f,
                )
                // l 0.582 -0.336
                lineToRelative(dx = 0.582f, dy = -0.336f)
                // l 0.694 0.186
                lineToRelative(dx = 0.694f, dy = 0.186f)
                // a 0.187 0.187 0 1 0 0.097 -0.363
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.097f,
                    dy1 = -0.363f,
                )
                // L 10.11 9
                lineTo(x = 10.11f, y = 9.0f)
                // l 0.42 -0.243
                lineToRelative(dx = 0.42f, dy = -0.243f)
                // a 0.188 0.188 0 0 0 -0.188 -0.325
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.188f,
                    dy1 = -0.325f,
                )
                // l -0.42 0.243
                lineToRelative(dx = -0.42f, dy = 0.243f)
                // l 0.089 -0.331
                lineToRelative(dx = 0.089f, dy = -0.331f)
                // a 0.187 0.187 0 1 0 -0.362 -0.097
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.362f,
                    dy1 = -0.097f,
                )
                // l -0.186 0.693
                lineToRelative(dx = -0.186f, dy = 0.693f)
                // l -0.582 0.336
                lineToRelative(dx = -0.582f, dy = 0.336f)
                // a 0.187 0.187 0 0 0 -0.014 0.008
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.014f,
                    dy1 = 0.008f,
                )
                // a 1.124 1.124 0 0 0 -0.68 -0.393
                arcToRelative(
                    a = 1.124f,
                    b = 1.124f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.68f,
                    dy1 = -0.393f,
                )
                // v -0.687
                verticalLineToRelative(dy = -0.687f)
                // l 0.508 -0.508
                lineToRelative(dx = 0.508f, dy = -0.508f)
                // a 0.187 0.187 0 1 0 -0.265 -0.265
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.265f,
                    dy1 = -0.265f,
                )
                // l -0.242 0.242
                lineToRelative(dx = -0.242f, dy = 0.242f)
                // v -0.484z
                verticalLineToRelative(dy = -0.484f)
                close()
            }
        }.build().also { _ic2398 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2398: ImageVector? = null
