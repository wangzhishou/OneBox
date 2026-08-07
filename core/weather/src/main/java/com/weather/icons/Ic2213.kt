package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2213: ImageVector
    get() {
        val current = _ic2213
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2213",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M3.188 .188 a.187 .187 0 1 0 -.376 0 v.484 L2.57 .43 a.188 .188 0 0 0 -.265 .265 l.507 .508 v.672 l.001 .015 a1.124 1.124 0 0 0 -.68 .393 .192 .192 0 0 0 -.014 -.008 l-.582 -.336 -.186 -.693 a.188 .188 0 0 0 -.362 .097 l.089 .33 -.42 -.242 a.187 .187 0 1 0 -.187 .325 L.89 2 l-.332 .088 a.187 .187 0 1 0 .097 .362 l.694 -.185 .582 .336 a.193 .193 0 0 0 .014 .007 1.123 1.123 0 0 0 0 .786 .195 .195 0 0 0 -.014 .007 l-.582 .336 -.694 -.186 a.188 .188 0 0 0 -.097 .363 L.89 4 l-.42 .243 a.188 .188 0 0 0 .188 .325 l.42 -.243 -.089 .331 a.188 .188 0 0 0 .362 .097 l.186 -.693 .582 -.336 a.186 .186 0 0 0 .014 -.008 c.168 .204 .408 .347 .68 .393 v.687 l-.508 .508 a.187 .187 0 1 0 .265 .265 l.243 -.242 v.484 a.188 .188 0 0 0 .374 0 v-.484 l.243 .242 a.187 .187 0 1 0 .265 -.265 l-.507 -.508 v-.672 l-.001 -.015 c.272 -.046 .512 -.19 .68 -.393 a.187 .187 0 0 0 .014 .008 l.582 .336 .186 .693 a.188 .188 0 0 0 .362 -.097 l-.089 -.33 .42 .242 a.187 .187 0 1 0 .187 -.325 L5.11 4 l.332 -.088 a.187 .187 0 1 0 -.097 -.362 l-.694 .185 -.582 -.336 a.196 .196 0 0 0 -.014 -.007 1.123 1.123 0 0 0 0 -.786 .194 .194 0 0 0 .014 -.007 l.582 -.336 .694 .186 a.188 .188 0 0 0 .097 -.363 L5.11 2 l.42 -.243 a.188 .188 0 0 0 -.188 -.325 l-.42 .243 .089 -.331 a.188 .188 0 0 0 -.362 -.097 l-.186 .693 -.582 .336 a.187 .187 0 0 0 -.014 .008 1.124 1.124 0 0 0 -.68 -.393 v-.687 l.508 -.508 A.188 .188 0 0 0 3.43 .43 l-.243 .242 V.187Z m2.937 11.937 a.125 .125 0 0 0 -.25 0 v.323 l-.162 -.161 a.125 .125 0 0 0 -.176 .176 l.338 .339 v.448 a.13 .13 0 0 0 0 .01 .75 .75 0 0 0 -.453 .262 .183 .183 0 0 0 -.009 -.005 l-.388 -.224 -.124 -.463 a.125 .125 0 1 0 -.241 .065 l.059 .22 -.28 -.16 a.125 .125 0 0 0 -.125 .216 l.28 .161 -.221 .06 a.125 .125 0 0 0 .065 .241 l.462 -.124 .388 .224 a.161 .161 0 0 0 .01 .005 .748 .748 0 0 0 0 .524 .161 .161 0 0 0 -.01 .005 l-.388 .224 -.462 -.124 a.125 .125 0 1 0 -.065 .241 l.22 .06 -.28 .161 a.125 .125 0 0 0 .126 .217 l.28 -.162 -.06 .22 a.125 .125 0 0 0 .242 .066 l.124 -.463 .388 -.224 a.13 .13 0 0 0 .009 -.005 .75 .75 0 0 0 .453 .262 .13 .13 0 0 0 0 .01 v.448 l-.338 .339 a.125 .125 0 0 0 .176 .176 l.162 -.161 v.323 a.125 .125 0 0 0 .25 0 v-.323 l.162 .161 a.125 .125 0 0 0 .176 -.176 l-.338 -.339 v-.448 a.13 .13 0 0 0 0 -.01 .75 .75 0 0 0 .453 -.262 .184 .184 0 0 0 .01 .005 l.387 .224 .124 .463 a.125 .125 0 1 0 .242 -.065 l-.06 -.22 .28 .16 a.125 .125 0 0 0 .125 -.216 l-.28 -.161 .221 -.06 a.125 .125 0 0 0 -.065 -.241 l-.462 .124 -.388 -.224 a.115 .115 0 0 0 -.009 -.005 .748 .748 0 0 0 0 -.524 .117 .117 0 0 0 .01 -.005 l.387 -.224 .462 .124 a.125 .125 0 1 0 .065 -.241 l-.22 -.06 .28 -.161 a.125 .125 0 0 0 -.126 -.217 l-.28 .162 .06 -.22 a.125 .125 0 0 0 -.242 -.066 l-.124 .463 -.388 .224 a.184 .184 0 0 0 -.009 .005 .75 .75 0 0 0 -.453 -.262 .13 .13 0 0 0 0 -.01 v-.448 l.338 -.339 a.125 .125 0 0 0 -.176 -.176 l-.162 .161 v-.323Z m5 -11 a.125 .125 0 1 0 -.25 0 v.323 l-.162 -.161 a.125 .125 0 1 0 -.176 .176 l.338 .339 v.458 a.75 .75 0 0 0 -.453 .262 .123 .123 0 0 0 -.009 -.005 l-.388 -.224 L9.9 1.83 a.125 .125 0 1 0 -.241 .065 l.059 .22 -.28 -.16 a.125 .125 0 1 0 -.125 .216 l.28 .161 -.221 .06 a.125 .125 0 1 0 .065 .241 l.462 -.124 .388 .224 a.123 .123 0 0 0 .01 .005 .746 .746 0 0 0 0 .524 .12 .12 0 0 0 -.01 .005 L9.9 3.49 l-.462 -.124 a.125 .125 0 1 0 -.065 .241 l.22 .06 -.28 .161 a.125 .125 0 1 0 .126 .217 l.28 -.162 -.06 .22 a.125 .125 0 0 0 .242 .066 l.124 -.463 .388 -.224 a.11 .11 0 0 0 .009 -.005 .75 .75 0 0 0 .454 .262 l-.001 .01 v.448 l-.338 .339 a.125 .125 0 1 0 .176 .176 l.162 -.161 v.323 a.125 .125 0 0 0 .25 0 v-.323 l.162 .161 a.125 .125 0 1 0 .176 -.176 l-.338 -.339 V3.74 a.75 .75 0 0 0 .453 -.262 .107 .107 0 0 0 .01 .005 l.387 .224 .124 .463 a.125 .125 0 0 0 .242 -.065 l-.06 -.22 .28 .16 a.125 .125 0 0 0 .125 -.216 l-.28 -.161 .221 -.06 a.125 .125 0 0 0 -.065 -.241 l-.462 .124 -.388 -.224 a.12 .12 0 0 0 -.009 -.005 .749 .749 0 0 0 0 -.524 .12 .12 0 0 0 .01 -.005 l.387 -.224 .463 .124 a.125 .125 0 0 0 .064 -.241 l-.22 -.06 .28 -.161 a.125 .125 0 1 0 -.126 -.217 l-.28 .162 .06 -.22 a.125 .125 0 0 0 -.242 -.066 l-.124 .463 -.388 .224 a.105 .105 0 0 0 -.009 .005 .75 .75 0 0 0 -.453 -.262 v-.458 l.338 -.339 a.125 .125 0 0 0 -.176 -.176 l-.162 .161 v-.323Z M2 7 h6 a1 1 0 1 0 -.943 -1.333 .5 .5 0 1 1 -.943 -.334 A2 2 0 1 1 8 8 H2 a.5 .5 0 0 1 0 -1Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 3.188 0.188
                moveTo(x = 3.188f, y = 0.188f)
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
                // L 2.57 0.43
                lineTo(x = 2.57f, y = 0.43f)
                // a 0.188 0.188 0 0 0 -0.265 0.265
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.265f,
                    dy1 = 0.265f,
                )
                // l 0.507 0.508
                lineToRelative(dx = 0.507f, dy = 0.508f)
                // v 0.672
                verticalLineToRelative(dy = 0.672f)
                // l 0.001 0.015
                lineToRelative(dx = 0.001f, dy = 0.015f)
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
                // L 0.89 2
                lineTo(x = 0.89f, y = 2.0f)
                // l -0.332 0.088
                lineToRelative(dx = -0.332f, dy = 0.088f)
                // a 0.187 0.187 0 1 0 0.097 0.362
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.097f,
                    dy1 = 0.362f,
                )
                // l 0.694 -0.185
                lineToRelative(dx = 0.694f, dy = -0.185f)
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
                // L 0.89 4
                lineTo(x = 0.89f, y = 4.0f)
                // l -0.42 0.243
                lineToRelative(dx = -0.42f, dy = 0.243f)
                // a 0.188 0.188 0 0 0 0.188 0.325
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.188f,
                    dy1 = 0.325f,
                )
                // l 0.42 -0.243
                lineToRelative(dx = 0.42f, dy = -0.243f)
                // l -0.089 0.331
                lineToRelative(dx = -0.089f, dy = 0.331f)
                // a 0.188 0.188 0 0 0 0.362 0.097
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.362f,
                    dy1 = 0.097f,
                )
                // l 0.186 -0.693
                lineToRelative(dx = 0.186f, dy = -0.693f)
                // l 0.582 -0.336
                lineToRelative(dx = 0.582f, dy = -0.336f)
                // a 0.186 0.186 0 0 0 0.014 -0.008
                arcToRelative(
                    a = 0.186f,
                    b = 0.186f,
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
                // a 0.187 0.187 0 1 0 0.265 0.265
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.265f,
                    dy1 = 0.265f,
                )
                // l 0.243 -0.242
                lineToRelative(dx = 0.243f, dy = -0.242f)
                // v 0.484
                verticalLineToRelative(dy = 0.484f)
                // a 0.188 0.188 0 0 0 0.374 0
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.374f,
                    dy1 = 0.0f,
                )
                // v -0.484
                verticalLineToRelative(dy = -0.484f)
                // l 0.243 0.242
                lineToRelative(dx = 0.243f, dy = 0.242f)
                // a 0.187 0.187 0 1 0 0.265 -0.265
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.265f,
                    dy1 = -0.265f,
                )
                // l -0.507 -0.508
                lineToRelative(dx = -0.507f, dy = -0.508f)
                // v -0.672
                verticalLineToRelative(dy = -0.672f)
                // l -0.001 -0.015
                lineToRelative(dx = -0.001f, dy = -0.015f)
                // c 0.272 -0.046 0.512 -0.19 0.68 -0.393
                curveToRelative(
                    dx1 = 0.272f,
                    dy1 = -0.046f,
                    dx2 = 0.512f,
                    dy2 = -0.19f,
                    dx3 = 0.68f,
                    dy3 = -0.393f,
                )
                // a 0.187 0.187 0 0 0 0.014 0.008
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
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
                // a 0.187 0.187 0 1 0 0.187 -0.325
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.187f,
                    dy1 = -0.325f,
                )
                // L 5.11 4
                lineTo(x = 5.11f, y = 4.0f)
                // l 0.332 -0.088
                lineToRelative(dx = 0.332f, dy = -0.088f)
                // a 0.187 0.187 0 1 0 -0.097 -0.362
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.097f,
                    dy1 = -0.362f,
                )
                // l -0.694 0.185
                lineToRelative(dx = -0.694f, dy = 0.185f)
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
                // a 0.188 0.188 0 0 0 0.097 -0.363
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.097f,
                    dy1 = -0.363f,
                )
                // L 5.11 2
                lineTo(x = 5.11f, y = 2.0f)
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
                // a 0.188 0.188 0 0 0 -0.362 -0.097
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
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
                // A 0.188 0.188 0 0 0 3.43 0.43
                arcTo(
                    horizontalEllipseRadius = 0.188f,
                    verticalEllipseRadius = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 3.43f,
                    y1 = 0.43f,
                )
                // l -0.243 0.242
                lineToRelative(dx = -0.243f, dy = 0.242f)
                // V 0.187z
                verticalLineTo(y = 0.187f)
                close()
                // m 2.937 11.937
                moveToRelative(dx = 2.937f, dy = 11.937f)
                // a 0.125 0.125 0 0 0 -0.25 0
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.25f,
                    dy1 = 0.0f,
                )
                // v 0.323
                verticalLineToRelative(dy = 0.323f)
                // l -0.162 -0.161
                lineToRelative(dx = -0.162f, dy = -0.161f)
                // a 0.125 0.125 0 0 0 -0.176 0.176
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.176f,
                    dy1 = 0.176f,
                )
                // l 0.338 0.339
                lineToRelative(dx = 0.338f, dy = 0.339f)
                // v 0.448
                verticalLineToRelative(dy = 0.448f)
                // a 0.13 0.13 0 0 0 0 0.01
                arcToRelative(
                    a = 0.13f,
                    b = 0.13f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.01f,
                )
                // a 0.75 0.75 0 0 0 -0.453 0.262
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.453f,
                    dy1 = 0.262f,
                )
                // a 0.183 0.183 0 0 0 -0.009 -0.005
                arcToRelative(
                    a = 0.183f,
                    b = 0.183f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.009f,
                    dy1 = -0.005f,
                )
                // l -0.388 -0.224
                lineToRelative(dx = -0.388f, dy = -0.224f)
                // l -0.124 -0.463
                lineToRelative(dx = -0.124f, dy = -0.463f)
                // a 0.125 0.125 0 1 0 -0.241 0.065
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.241f,
                    dy1 = 0.065f,
                )
                // l 0.059 0.22
                lineToRelative(dx = 0.059f, dy = 0.22f)
                // l -0.28 -0.16
                lineToRelative(dx = -0.28f, dy = -0.16f)
                // a 0.125 0.125 0 0 0 -0.125 0.216
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.125f,
                    dy1 = 0.216f,
                )
                // l 0.28 0.161
                lineToRelative(dx = 0.28f, dy = 0.161f)
                // l -0.221 0.06
                lineToRelative(dx = -0.221f, dy = 0.06f)
                // a 0.125 0.125 0 0 0 0.065 0.241
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.065f,
                    dy1 = 0.241f,
                )
                // l 0.462 -0.124
                lineToRelative(dx = 0.462f, dy = -0.124f)
                // l 0.388 0.224
                lineToRelative(dx = 0.388f, dy = 0.224f)
                // a 0.161 0.161 0 0 0 0.01 0.005
                arcToRelative(
                    a = 0.161f,
                    b = 0.161f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.01f,
                    dy1 = 0.005f,
                )
                // a 0.748 0.748 0 0 0 0 0.524
                arcToRelative(
                    a = 0.748f,
                    b = 0.748f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.524f,
                )
                // a 0.161 0.161 0 0 0 -0.01 0.005
                arcToRelative(
                    a = 0.161f,
                    b = 0.161f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.01f,
                    dy1 = 0.005f,
                )
                // l -0.388 0.224
                lineToRelative(dx = -0.388f, dy = 0.224f)
                // l -0.462 -0.124
                lineToRelative(dx = -0.462f, dy = -0.124f)
                // a 0.125 0.125 0 1 0 -0.065 0.241
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.065f,
                    dy1 = 0.241f,
                )
                // l 0.22 0.06
                lineToRelative(dx = 0.22f, dy = 0.06f)
                // l -0.28 0.161
                lineToRelative(dx = -0.28f, dy = 0.161f)
                // a 0.125 0.125 0 0 0 0.126 0.217
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.126f,
                    dy1 = 0.217f,
                )
                // l 0.28 -0.162
                lineToRelative(dx = 0.28f, dy = -0.162f)
                // l -0.06 0.22
                lineToRelative(dx = -0.06f, dy = 0.22f)
                // a 0.125 0.125 0 0 0 0.242 0.066
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.242f,
                    dy1 = 0.066f,
                )
                // l 0.124 -0.463
                lineToRelative(dx = 0.124f, dy = -0.463f)
                // l 0.388 -0.224
                lineToRelative(dx = 0.388f, dy = -0.224f)
                // a 0.13 0.13 0 0 0 0.009 -0.005
                arcToRelative(
                    a = 0.13f,
                    b = 0.13f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.009f,
                    dy1 = -0.005f,
                )
                // a 0.75 0.75 0 0 0 0.453 0.262
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.453f,
                    dy1 = 0.262f,
                )
                // a 0.13 0.13 0 0 0 0 0.01
                arcToRelative(
                    a = 0.13f,
                    b = 0.13f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.01f,
                )
                // v 0.448
                verticalLineToRelative(dy = 0.448f)
                // l -0.338 0.339
                lineToRelative(dx = -0.338f, dy = 0.339f)
                // a 0.125 0.125 0 0 0 0.176 0.176
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.176f,
                    dy1 = 0.176f,
                )
                // l 0.162 -0.161
                lineToRelative(dx = 0.162f, dy = -0.161f)
                // v 0.323
                verticalLineToRelative(dy = 0.323f)
                // a 0.125 0.125 0 0 0 0.25 0
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.25f,
                    dy1 = 0.0f,
                )
                // v -0.323
                verticalLineToRelative(dy = -0.323f)
                // l 0.162 0.161
                lineToRelative(dx = 0.162f, dy = 0.161f)
                // a 0.125 0.125 0 0 0 0.176 -0.176
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.176f,
                    dy1 = -0.176f,
                )
                // l -0.338 -0.339
                lineToRelative(dx = -0.338f, dy = -0.339f)
                // v -0.448
                verticalLineToRelative(dy = -0.448f)
                // a 0.13 0.13 0 0 0 0 -0.01
                arcToRelative(
                    a = 0.13f,
                    b = 0.13f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.01f,
                )
                // a 0.75 0.75 0 0 0 0.453 -0.262
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.453f,
                    dy1 = -0.262f,
                )
                // a 0.184 0.184 0 0 0 0.01 0.005
                arcToRelative(
                    a = 0.184f,
                    b = 0.184f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.01f,
                    dy1 = 0.005f,
                )
                // l 0.387 0.224
                lineToRelative(dx = 0.387f, dy = 0.224f)
                // l 0.124 0.463
                lineToRelative(dx = 0.124f, dy = 0.463f)
                // a 0.125 0.125 0 1 0 0.242 -0.065
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.242f,
                    dy1 = -0.065f,
                )
                // l -0.06 -0.22
                lineToRelative(dx = -0.06f, dy = -0.22f)
                // l 0.28 0.16
                lineToRelative(dx = 0.28f, dy = 0.16f)
                // a 0.125 0.125 0 0 0 0.125 -0.216
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.125f,
                    dy1 = -0.216f,
                )
                // l -0.28 -0.161
                lineToRelative(dx = -0.28f, dy = -0.161f)
                // l 0.221 -0.06
                lineToRelative(dx = 0.221f, dy = -0.06f)
                // a 0.125 0.125 0 0 0 -0.065 -0.241
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.065f,
                    dy1 = -0.241f,
                )
                // l -0.462 0.124
                lineToRelative(dx = -0.462f, dy = 0.124f)
                // l -0.388 -0.224
                lineToRelative(dx = -0.388f, dy = -0.224f)
                // a 0.115 0.115 0 0 0 -0.009 -0.005
                arcToRelative(
                    a = 0.115f,
                    b = 0.115f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.009f,
                    dy1 = -0.005f,
                )
                // a 0.748 0.748 0 0 0 0 -0.524
                arcToRelative(
                    a = 0.748f,
                    b = 0.748f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.524f,
                )
                // a 0.117 0.117 0 0 0 0.01 -0.005
                arcToRelative(
                    a = 0.117f,
                    b = 0.117f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.01f,
                    dy1 = -0.005f,
                )
                // l 0.387 -0.224
                lineToRelative(dx = 0.387f, dy = -0.224f)
                // l 0.462 0.124
                lineToRelative(dx = 0.462f, dy = 0.124f)
                // a 0.125 0.125 0 1 0 0.065 -0.241
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.065f,
                    dy1 = -0.241f,
                )
                // l -0.22 -0.06
                lineToRelative(dx = -0.22f, dy = -0.06f)
                // l 0.28 -0.161
                lineToRelative(dx = 0.28f, dy = -0.161f)
                // a 0.125 0.125 0 0 0 -0.126 -0.217
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.126f,
                    dy1 = -0.217f,
                )
                // l -0.28 0.162
                lineToRelative(dx = -0.28f, dy = 0.162f)
                // l 0.06 -0.22
                lineToRelative(dx = 0.06f, dy = -0.22f)
                // a 0.125 0.125 0 0 0 -0.242 -0.066
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.242f,
                    dy1 = -0.066f,
                )
                // l -0.124 0.463
                lineToRelative(dx = -0.124f, dy = 0.463f)
                // l -0.388 0.224
                lineToRelative(dx = -0.388f, dy = 0.224f)
                // a 0.184 0.184 0 0 0 -0.009 0.005
                arcToRelative(
                    a = 0.184f,
                    b = 0.184f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.009f,
                    dy1 = 0.005f,
                )
                // a 0.75 0.75 0 0 0 -0.453 -0.262
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.453f,
                    dy1 = -0.262f,
                )
                // a 0.13 0.13 0 0 0 0 -0.01
                arcToRelative(
                    a = 0.13f,
                    b = 0.13f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.01f,
                )
                // v -0.448
                verticalLineToRelative(dy = -0.448f)
                // l 0.338 -0.339
                lineToRelative(dx = 0.338f, dy = -0.339f)
                // a 0.125 0.125 0 0 0 -0.176 -0.176
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.176f,
                    dy1 = -0.176f,
                )
                // l -0.162 0.161
                lineToRelative(dx = -0.162f, dy = 0.161f)
                // v -0.323z
                verticalLineToRelative(dy = -0.323f)
                close()
                // m 5 -11
                moveToRelative(dx = 5.0f, dy = -11.0f)
                // a 0.125 0.125 0 1 0 -0.25 0
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.25f,
                    dy1 = 0.0f,
                )
                // v 0.323
                verticalLineToRelative(dy = 0.323f)
                // l -0.162 -0.161
                lineToRelative(dx = -0.162f, dy = -0.161f)
                // a 0.125 0.125 0 1 0 -0.176 0.176
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.176f,
                    dy1 = 0.176f,
                )
                // l 0.338 0.339
                lineToRelative(dx = 0.338f, dy = 0.339f)
                // v 0.458
                verticalLineToRelative(dy = 0.458f)
                // a 0.75 0.75 0 0 0 -0.453 0.262
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.453f,
                    dy1 = 0.262f,
                )
                // a 0.123 0.123 0 0 0 -0.009 -0.005
                arcToRelative(
                    a = 0.123f,
                    b = 0.123f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.009f,
                    dy1 = -0.005f,
                )
                // l -0.388 -0.224
                lineToRelative(dx = -0.388f, dy = -0.224f)
                // L 9.9 1.83
                lineTo(x = 9.9f, y = 1.83f)
                // a 0.125 0.125 0 1 0 -0.241 0.065
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.241f,
                    dy1 = 0.065f,
                )
                // l 0.059 0.22
                lineToRelative(dx = 0.059f, dy = 0.22f)
                // l -0.28 -0.16
                lineToRelative(dx = -0.28f, dy = -0.16f)
                // a 0.125 0.125 0 1 0 -0.125 0.216
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.125f,
                    dy1 = 0.216f,
                )
                // l 0.28 0.161
                lineToRelative(dx = 0.28f, dy = 0.161f)
                // l -0.221 0.06
                lineToRelative(dx = -0.221f, dy = 0.06f)
                // a 0.125 0.125 0 1 0 0.065 0.241
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.065f,
                    dy1 = 0.241f,
                )
                // l 0.462 -0.124
                lineToRelative(dx = 0.462f, dy = -0.124f)
                // l 0.388 0.224
                lineToRelative(dx = 0.388f, dy = 0.224f)
                // a 0.123 0.123 0 0 0 0.01 0.005
                arcToRelative(
                    a = 0.123f,
                    b = 0.123f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.01f,
                    dy1 = 0.005f,
                )
                // a 0.746 0.746 0 0 0 0 0.524
                arcToRelative(
                    a = 0.746f,
                    b = 0.746f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.524f,
                )
                // a 0.12 0.12 0 0 0 -0.01 0.005
                arcToRelative(
                    a = 0.12f,
                    b = 0.12f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.01f,
                    dy1 = 0.005f,
                )
                // L 9.9 3.49
                lineTo(x = 9.9f, y = 3.49f)
                // l -0.462 -0.124
                lineToRelative(dx = -0.462f, dy = -0.124f)
                // a 0.125 0.125 0 1 0 -0.065 0.241
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.065f,
                    dy1 = 0.241f,
                )
                // l 0.22 0.06
                lineToRelative(dx = 0.22f, dy = 0.06f)
                // l -0.28 0.161
                lineToRelative(dx = -0.28f, dy = 0.161f)
                // a 0.125 0.125 0 1 0 0.126 0.217
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.126f,
                    dy1 = 0.217f,
                )
                // l 0.28 -0.162
                lineToRelative(dx = 0.28f, dy = -0.162f)
                // l -0.06 0.22
                lineToRelative(dx = -0.06f, dy = 0.22f)
                // a 0.125 0.125 0 0 0 0.242 0.066
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.242f,
                    dy1 = 0.066f,
                )
                // l 0.124 -0.463
                lineToRelative(dx = 0.124f, dy = -0.463f)
                // l 0.388 -0.224
                lineToRelative(dx = 0.388f, dy = -0.224f)
                // a 0.11 0.11 0 0 0 0.009 -0.005
                arcToRelative(
                    a = 0.11f,
                    b = 0.11f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.009f,
                    dy1 = -0.005f,
                )
                // a 0.75 0.75 0 0 0 0.454 0.262
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.454f,
                    dy1 = 0.262f,
                )
                // l -0.001 0.01
                lineToRelative(dx = -0.001f, dy = 0.01f)
                // v 0.448
                verticalLineToRelative(dy = 0.448f)
                // l -0.338 0.339
                lineToRelative(dx = -0.338f, dy = 0.339f)
                // a 0.125 0.125 0 1 0 0.176 0.176
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.176f,
                    dy1 = 0.176f,
                )
                // l 0.162 -0.161
                lineToRelative(dx = 0.162f, dy = -0.161f)
                // v 0.323
                verticalLineToRelative(dy = 0.323f)
                // a 0.125 0.125 0 0 0 0.25 0
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.25f,
                    dy1 = 0.0f,
                )
                // v -0.323
                verticalLineToRelative(dy = -0.323f)
                // l 0.162 0.161
                lineToRelative(dx = 0.162f, dy = 0.161f)
                // a 0.125 0.125 0 1 0 0.176 -0.176
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.176f,
                    dy1 = -0.176f,
                )
                // l -0.338 -0.339
                lineToRelative(dx = -0.338f, dy = -0.339f)
                // V 3.74
                verticalLineTo(y = 3.74f)
                // a 0.75 0.75 0 0 0 0.453 -0.262
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.453f,
                    dy1 = -0.262f,
                )
                // a 0.107 0.107 0 0 0 0.01 0.005
                arcToRelative(
                    a = 0.107f,
                    b = 0.107f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.01f,
                    dy1 = 0.005f,
                )
                // l 0.387 0.224
                lineToRelative(dx = 0.387f, dy = 0.224f)
                // l 0.124 0.463
                lineToRelative(dx = 0.124f, dy = 0.463f)
                // a 0.125 0.125 0 0 0 0.242 -0.065
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.242f,
                    dy1 = -0.065f,
                )
                // l -0.06 -0.22
                lineToRelative(dx = -0.06f, dy = -0.22f)
                // l 0.28 0.16
                lineToRelative(dx = 0.28f, dy = 0.16f)
                // a 0.125 0.125 0 0 0 0.125 -0.216
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.125f,
                    dy1 = -0.216f,
                )
                // l -0.28 -0.161
                lineToRelative(dx = -0.28f, dy = -0.161f)
                // l 0.221 -0.06
                lineToRelative(dx = 0.221f, dy = -0.06f)
                // a 0.125 0.125 0 0 0 -0.065 -0.241
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.065f,
                    dy1 = -0.241f,
                )
                // l -0.462 0.124
                lineToRelative(dx = -0.462f, dy = 0.124f)
                // l -0.388 -0.224
                lineToRelative(dx = -0.388f, dy = -0.224f)
                // a 0.12 0.12 0 0 0 -0.009 -0.005
                arcToRelative(
                    a = 0.12f,
                    b = 0.12f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.009f,
                    dy1 = -0.005f,
                )
                // a 0.749 0.749 0 0 0 0 -0.524
                arcToRelative(
                    a = 0.749f,
                    b = 0.749f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.524f,
                )
                // a 0.12 0.12 0 0 0 0.01 -0.005
                arcToRelative(
                    a = 0.12f,
                    b = 0.12f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.01f,
                    dy1 = -0.005f,
                )
                // l 0.387 -0.224
                lineToRelative(dx = 0.387f, dy = -0.224f)
                // l 0.463 0.124
                lineToRelative(dx = 0.463f, dy = 0.124f)
                // a 0.125 0.125 0 0 0 0.064 -0.241
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.064f,
                    dy1 = -0.241f,
                )
                // l -0.22 -0.06
                lineToRelative(dx = -0.22f, dy = -0.06f)
                // l 0.28 -0.161
                lineToRelative(dx = 0.28f, dy = -0.161f)
                // a 0.125 0.125 0 1 0 -0.126 -0.217
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.126f,
                    dy1 = -0.217f,
                )
                // l -0.28 0.162
                lineToRelative(dx = -0.28f, dy = 0.162f)
                // l 0.06 -0.22
                lineToRelative(dx = 0.06f, dy = -0.22f)
                // a 0.125 0.125 0 0 0 -0.242 -0.066
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.242f,
                    dy1 = -0.066f,
                )
                // l -0.124 0.463
                lineToRelative(dx = -0.124f, dy = 0.463f)
                // l -0.388 0.224
                lineToRelative(dx = -0.388f, dy = 0.224f)
                // a 0.105 0.105 0 0 0 -0.009 0.005
                arcToRelative(
                    a = 0.105f,
                    b = 0.105f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.009f,
                    dy1 = 0.005f,
                )
                // a 0.75 0.75 0 0 0 -0.453 -0.262
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.453f,
                    dy1 = -0.262f,
                )
                // v -0.458
                verticalLineToRelative(dy = -0.458f)
                // l 0.338 -0.339
                lineToRelative(dx = 0.338f, dy = -0.339f)
                // a 0.125 0.125 0 0 0 -0.176 -0.176
                arcToRelative(
                    a = 0.125f,
                    b = 0.125f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.176f,
                    dy1 = -0.176f,
                )
                // l -0.162 0.161
                lineToRelative(dx = -0.162f, dy = 0.161f)
                // v -0.323z
                verticalLineToRelative(dy = -0.323f)
                close()
                // M 2 7
                moveTo(x = 2.0f, y = 7.0f)
                // h 6
                horizontalLineToRelative(dx = 6.0f)
                // a 1 1 0 1 0 -0.943 -1.333
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.943f,
                    dy1 = -1.333f,
                )
                // a 0.5 0.5 0 1 1 -0.943 -0.334
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.943f,
                    dy1 = -0.334f,
                )
                // A 2 2 0 1 1 8 8
                arcTo(
                    horizontalEllipseRadius = 2.0f,
                    verticalEllipseRadius = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 8.0f,
                    y1 = 8.0f,
                )
                // H 2
                horizontalLineTo(x = 2.0f)
                // a 0.5 0.5 0 0 1 0 -1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                close()
            }
            // M11.079 6.375 A2.5 2.5 0 0 1 16 7 c0 1.397 -1.24 2.5 -2.5 2.5 H.5 a.5 .5 0 0 1 0 -1 h13 c.74 0 1.5 -.688 1.5 -1.5 a1.5 1.5 0 0 0 -2.953 -.375 .5 .5 0 1 1 -.968 -.25Z M2.5 10.5 A.5 .5 0 0 1 3 10 h8 a2 2 0 1 1 -1.886 2.667 .5 .5 0 1 1 .943 -.334 A1 1 0 1 0 11 11 H3 a.5 .5 0 0 1 -.5 -.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.079 6.375
                moveTo(x = 11.079f, y = 6.375f)
                // A 2.5 2.5 0 0 1 16 7
                arcTo(
                    horizontalEllipseRadius = 2.5f,
                    verticalEllipseRadius = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 16.0f,
                    y1 = 7.0f,
                )
                // c 0 1.397 -1.24 2.5 -2.5 2.5
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.397f,
                    dx2 = -1.24f,
                    dy2 = 2.5f,
                    dx3 = -2.5f,
                    dy3 = 2.5f,
                )
                // H 0.5
                horizontalLineTo(x = 0.5f)
                // a 0.5 0.5 0 0 1 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h 13
                horizontalLineToRelative(dx = 13.0f)
                // c 0.74 0 1.5 -0.688 1.5 -1.5
                curveToRelative(
                    dx1 = 0.74f,
                    dy1 = 0.0f,
                    dx2 = 1.5f,
                    dy2 = -0.688f,
                    dx3 = 1.5f,
                    dy3 = -1.5f,
                )
                // a 1.5 1.5 0 0 0 -2.953 -0.375
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.953f,
                    dy1 = -0.375f,
                )
                // a 0.5 0.5 0 1 1 -0.968 -0.25z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.968f,
                    dy1 = -0.25f,
                )
                close()
                // M 2.5 10.5
                moveTo(x = 2.5f, y = 10.5f)
                // A 0.5 0.5 0 0 1 3 10
                arcTo(
                    horizontalEllipseRadius = 0.5f,
                    verticalEllipseRadius = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 3.0f,
                    y1 = 10.0f,
                )
                // h 8
                horizontalLineToRelative(dx = 8.0f)
                // a 2 2 0 1 1 -1.886 2.667
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.886f,
                    dy1 = 2.667f,
                )
                // a 0.5 0.5 0 1 1 0.943 -0.334
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.943f,
                    dy1 = -0.334f,
                )
                // A 1 1 0 1 0 11 11
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    x1 = 11.0f,
                    y1 = 11.0f,
                )
                // H 3
                horizontalLineTo(x = 3.0f)
                // a 0.5 0.5 0 0 1 -0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                close()
            }
        }.build().also { _ic2213 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2213: ImageVector? = null
