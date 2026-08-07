package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1024: ImageVector
    get() {
        val current = _ic1024
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1024",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M11.306 1.326 a.281 .281 0 0 0 .48 -.199 V.283 a.281 .281 0 1 0 -.562 0 v.844 c0 .075 .03 .146 .082 .2Z M9.011 2.373 a.281 .281 0 0 0 .307 -.46 l-.598 -.596 a.281 .281 0 0 0 -.398 .398 l.598 .597 a.28 .28 0 0 0 .091 .06Z M7.283 4.78 h.844 a.281 .281 0 0 0 0 -.564 h-.844 a.281 .281 0 0 0 0 .563Z M8.36 7.716 a.281 .281 0 0 0 .355 -.036 l.597 -.596 a.281 .281 0 0 0 -.398 -.398 l-.597 .597 a.281 .281 0 0 0 .042 .433Z m2.939 1.204 a.281 .281 0 0 0 .48 -.2 v-.844 a.281 .281 0 1 0 -.563 0 v.844 c0 .075 .03 .147 .083 .2Z m3.075 -1.173 a.28 .28 0 0 0 .367 -.152 .281 .281 0 0 0 -.061 -.307 l-.597 -.596 a.281 .281 0 1 0 -.397 .398 l.597 .596 a.28 .28 0 0 0 .09 .061Z m.502 -2.96 h.844 a.282 .282 0 0 0 0 -.563 h-.844 a.281 .281 0 1 0 0 .563Z m-1.142 -2.434 a.282 .282 0 0 0 .356 -.036 l.596 -.596 a.281 .281 0 0 0 -.398 -.398 l-.597 .596 a.281 .281 0 0 0 .043 .434Z m-3.637 .042 a2.532 2.532 0 1 1 2.812 4.21 2.532 2.532 0 0 1 -2.813 -4.21Z m2.5 .468 a1.969 1.969 0 1 0 -2.188 3.274 1.969 1.969 0 0 0 2.188 -3.274Z M5 6.5 a1.5 1.5 0 1 1 -3 0 1.5 1.5 0 0 1 3 0Z m-.939 2.251 L2.904 8.52 a1 1 0 0 0 -1.187 .848 l-.708 5.31 A1 1 0 0 0 2 15.81 h1.055 a1 1 0 0 0 .555 -.168 l2.651 -1.767 a.2 .2 0 0 1 .2 -.013 l3.791 1.896 a.5 .5 0 0 0 .224 .052 h.23 a.5 .5 0 0 0 .278 -.916 l-4.205 -2.803 a.8 .8 0 0 0 -.855 -.02 l-1.695 1.017 a.2 .2 0 0 1 -.3 -.205 l.365 -2.19 a.2 .2 0 0 1 .339 -.108 l.932 .932 a1 1 0 0 0 .707 .293 h2.086 a.5 .5 0 0 0 0 -1 H6.441 a.2 .2 0 0 1 -.142 -.058 L4.572 9.024 a1 1 0 0 0 -.51 -.273Z m.633 -7.132 a.218 .218 0 0 0 -.29 -.096 .213 .213 0 0 0 -.098 .287 l.21 .415 c.092 .182 .056 .4 -.09 .544 l-.16 .159 a.892 .892 0 0 0 -.17 1.038 l.21 .415 a.218 .218 0 0 0 .29 .096 .213 .213 0 0 0 .098 -.287 l-.21 -.415 a.467 .467 0 0 1 .09 -.544 l.16 -.159 a.892 .892 0 0 0 .17 -1.038 l-.21 -.415Z m-2.291 -.096 a.218 .218 0 0 1 .29 .096 l.21 .415 a.892 .892 0 0 1 -.17 1.038 l-.16 .159 a.467 .467 0 0 0 -.09 .544 l.21 .415 a.213 .213 0 0 1 -.096 .287 .218 .218 0 0 1 -.29 -.096 l-.21 -.415 a.892 .892 0 0 1 .17 -1.038 l.16 -.159 a.467 .467 0 0 0 .09 -.544 l-.21 -.415 a.213 .213 0 0 1 .096 -.287Z m1 0 a.218 .218 0 0 1 .29 .096 l.21 .415 a.892 .892 0 0 1 -.17 1.038 l-.16 .159 a.467 .467 0 0 0 -.09 .544 l.21 .415 a.213 .213 0 0 1 -.096 .287 .218 .218 0 0 1 -.29 -.096 l-.21 -.415 a.892 .892 0 0 1 .17 -1.038 l.16 -.159 a.467 .467 0 0 0 .09 -.544 l-.21 -.415 a.213 .213 0 0 1 .096 -.287Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.306 1.326
                moveTo(x = 11.306f, y = 1.326f)
                // a 0.281 0.281 0 0 0 0.48 -0.199
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.48f,
                    dy1 = -0.199f,
                )
                // V 0.283
                verticalLineTo(y = 0.283f)
                // a 0.281 0.281 0 1 0 -0.562 0
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.562f,
                    dy1 = 0.0f,
                )
                // v 0.844
                verticalLineToRelative(dy = 0.844f)
                // c 0 0.075 0.03 0.146 0.082 0.2z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.075f,
                    dx2 = 0.03f,
                    dy2 = 0.146f,
                    dx3 = 0.082f,
                    dy3 = 0.2f,
                )
                close()
                // M 9.011 2.373
                moveTo(x = 9.011f, y = 2.373f)
                // a 0.281 0.281 0 0 0 0.307 -0.46
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.307f,
                    dy1 = -0.46f,
                )
                // l -0.598 -0.596
                lineToRelative(dx = -0.598f, dy = -0.596f)
                // a 0.281 0.281 0 0 0 -0.398 0.398
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.398f,
                    dy1 = 0.398f,
                )
                // l 0.598 0.597
                lineToRelative(dx = 0.598f, dy = 0.597f)
                // a 0.28 0.28 0 0 0 0.091 0.06z
                arcToRelative(
                    a = 0.28f,
                    b = 0.28f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.091f,
                    dy1 = 0.06f,
                )
                close()
                // M 7.283 4.78
                moveTo(x = 7.283f, y = 4.78f)
                // h 0.844
                horizontalLineToRelative(dx = 0.844f)
                // a 0.281 0.281 0 0 0 0 -0.564
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.564f,
                )
                // h -0.844
                horizontalLineToRelative(dx = -0.844f)
                // a 0.281 0.281 0 0 0 0 0.563z
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.563f,
                )
                close()
                // M 8.36 7.716
                moveTo(x = 8.36f, y = 7.716f)
                // a 0.281 0.281 0 0 0 0.355 -0.036
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.355f,
                    dy1 = -0.036f,
                )
                // l 0.597 -0.596
                lineToRelative(dx = 0.597f, dy = -0.596f)
                // a 0.281 0.281 0 0 0 -0.398 -0.398
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.398f,
                    dy1 = -0.398f,
                )
                // l -0.597 0.597
                lineToRelative(dx = -0.597f, dy = 0.597f)
                // a 0.281 0.281 0 0 0 0.042 0.433z
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.042f,
                    dy1 = 0.433f,
                )
                close()
                // m 2.939 1.204
                moveToRelative(dx = 2.939f, dy = 1.204f)
                // a 0.281 0.281 0 0 0 0.48 -0.2
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.48f,
                    dy1 = -0.2f,
                )
                // v -0.844
                verticalLineToRelative(dy = -0.844f)
                // a 0.281 0.281 0 1 0 -0.563 0
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.563f,
                    dy1 = 0.0f,
                )
                // v 0.844
                verticalLineToRelative(dy = 0.844f)
                // c 0 0.075 0.03 0.147 0.083 0.2z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.075f,
                    dx2 = 0.03f,
                    dy2 = 0.147f,
                    dx3 = 0.083f,
                    dy3 = 0.2f,
                )
                close()
                // m 3.075 -1.173
                moveToRelative(dx = 3.075f, dy = -1.173f)
                // a 0.28 0.28 0 0 0 0.367 -0.152
                arcToRelative(
                    a = 0.28f,
                    b = 0.28f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.367f,
                    dy1 = -0.152f,
                )
                // a 0.281 0.281 0 0 0 -0.061 -0.307
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.061f,
                    dy1 = -0.307f,
                )
                // l -0.597 -0.596
                lineToRelative(dx = -0.597f, dy = -0.596f)
                // a 0.281 0.281 0 1 0 -0.397 0.398
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.397f,
                    dy1 = 0.398f,
                )
                // l 0.597 0.596
                lineToRelative(dx = 0.597f, dy = 0.596f)
                // a 0.28 0.28 0 0 0 0.09 0.061z
                arcToRelative(
                    a = 0.28f,
                    b = 0.28f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.09f,
                    dy1 = 0.061f,
                )
                close()
                // m 0.502 -2.96
                moveToRelative(dx = 0.502f, dy = -2.96f)
                // h 0.844
                horizontalLineToRelative(dx = 0.844f)
                // a 0.282 0.282 0 0 0 0 -0.563
                arcToRelative(
                    a = 0.282f,
                    b = 0.282f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.563f,
                )
                // h -0.844
                horizontalLineToRelative(dx = -0.844f)
                // a 0.281 0.281 0 1 0 0 0.563z
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.563f,
                )
                close()
                // m -1.142 -2.434
                moveToRelative(dx = -1.142f, dy = -2.434f)
                // a 0.282 0.282 0 0 0 0.356 -0.036
                arcToRelative(
                    a = 0.282f,
                    b = 0.282f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.356f,
                    dy1 = -0.036f,
                )
                // l 0.596 -0.596
                lineToRelative(dx = 0.596f, dy = -0.596f)
                // a 0.281 0.281 0 0 0 -0.398 -0.398
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.398f,
                    dy1 = -0.398f,
                )
                // l -0.597 0.596
                lineToRelative(dx = -0.597f, dy = 0.596f)
                // a 0.281 0.281 0 0 0 0.043 0.434z
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.043f,
                    dy1 = 0.434f,
                )
                close()
                // m -3.637 0.042
                moveToRelative(dx = -3.637f, dy = 0.042f)
                // a 2.532 2.532 0 1 1 2.812 4.21
                arcToRelative(
                    a = 2.532f,
                    b = 2.532f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 2.812f,
                    dy1 = 4.21f,
                )
                // a 2.532 2.532 0 0 1 -2.813 -4.21z
                arcToRelative(
                    a = 2.532f,
                    b = 2.532f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.813f,
                    dy1 = -4.21f,
                )
                close()
                // m 2.5 0.468
                moveToRelative(dx = 2.5f, dy = 0.468f)
                // a 1.969 1.969 0 1 0 -2.188 3.274
                arcToRelative(
                    a = 1.969f,
                    b = 1.969f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -2.188f,
                    dy1 = 3.274f,
                )
                // a 1.969 1.969 0 0 0 2.188 -3.274z
                arcToRelative(
                    a = 1.969f,
                    b = 1.969f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.188f,
                    dy1 = -3.274f,
                )
                close()
                // M 5 6.5
                moveTo(x = 5.0f, y = 6.5f)
                // a 1.5 1.5 0 1 1 -3 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -3.0f,
                    dy1 = 0.0f,
                )
                // a 1.5 1.5 0 0 1 3 0z
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 3.0f,
                    dy1 = 0.0f,
                )
                close()
                // m -0.939 2.251
                moveToRelative(dx = -0.939f, dy = 2.251f)
                // L 2.904 8.52
                lineTo(x = 2.904f, y = 8.52f)
                // a 1 1 0 0 0 -1.187 0.848
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.187f,
                    dy1 = 0.848f,
                )
                // l -0.708 5.31
                lineToRelative(dx = -0.708f, dy = 5.31f)
                // A 1 1 0 0 0 2 15.81
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 2.0f,
                    y1 = 15.81f,
                )
                // h 1.055
                horizontalLineToRelative(dx = 1.055f)
                // a 1 1 0 0 0 0.555 -0.168
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.555f,
                    dy1 = -0.168f,
                )
                // l 2.651 -1.767
                lineToRelative(dx = 2.651f, dy = -1.767f)
                // a 0.2 0.2 0 0 1 0.2 -0.013
                arcToRelative(
                    a = 0.2f,
                    b = 0.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.2f,
                    dy1 = -0.013f,
                )
                // l 3.791 1.896
                lineToRelative(dx = 3.791f, dy = 1.896f)
                // a 0.5 0.5 0 0 0 0.224 0.052
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.224f,
                    dy1 = 0.052f,
                )
                // h 0.23
                horizontalLineToRelative(dx = 0.23f)
                // a 0.5 0.5 0 0 0 0.278 -0.916
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.278f,
                    dy1 = -0.916f,
                )
                // l -4.205 -2.803
                lineToRelative(dx = -4.205f, dy = -2.803f)
                // a 0.8 0.8 0 0 0 -0.855 -0.02
                arcToRelative(
                    a = 0.8f,
                    b = 0.8f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.855f,
                    dy1 = -0.02f,
                )
                // l -1.695 1.017
                lineToRelative(dx = -1.695f, dy = 1.017f)
                // a 0.2 0.2 0 0 1 -0.3 -0.205
                arcToRelative(
                    a = 0.2f,
                    b = 0.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.3f,
                    dy1 = -0.205f,
                )
                // l 0.365 -2.19
                lineToRelative(dx = 0.365f, dy = -2.19f)
                // a 0.2 0.2 0 0 1 0.339 -0.108
                arcToRelative(
                    a = 0.2f,
                    b = 0.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.339f,
                    dy1 = -0.108f,
                )
                // l 0.932 0.932
                lineToRelative(dx = 0.932f, dy = 0.932f)
                // a 1 1 0 0 0 0.707 0.293
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.707f,
                    dy1 = 0.293f,
                )
                // h 2.086
                horizontalLineToRelative(dx = 2.086f)
                // a 0.5 0.5 0 0 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // H 6.441
                horizontalLineTo(x = 6.441f)
                // a 0.2 0.2 0 0 1 -0.142 -0.058
                arcToRelative(
                    a = 0.2f,
                    b = 0.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.142f,
                    dy1 = -0.058f,
                )
                // L 4.572 9.024
                lineTo(x = 4.572f, y = 9.024f)
                // a 1 1 0 0 0 -0.51 -0.273z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.51f,
                    dy1 = -0.273f,
                )
                close()
                // m 0.633 -7.132
                moveToRelative(dx = 0.633f, dy = -7.132f)
                // a 0.218 0.218 0 0 0 -0.29 -0.096
                arcToRelative(
                    a = 0.218f,
                    b = 0.218f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.29f,
                    dy1 = -0.096f,
                )
                // a 0.213 0.213 0 0 0 -0.098 0.287
                arcToRelative(
                    a = 0.213f,
                    b = 0.213f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.098f,
                    dy1 = 0.287f,
                )
                // l 0.21 0.415
                lineToRelative(dx = 0.21f, dy = 0.415f)
                // c 0.092 0.182 0.056 0.4 -0.09 0.544
                curveToRelative(
                    dx1 = 0.092f,
                    dy1 = 0.182f,
                    dx2 = 0.056f,
                    dy2 = 0.4f,
                    dx3 = -0.09f,
                    dy3 = 0.544f,
                )
                // l -0.16 0.159
                lineToRelative(dx = -0.16f, dy = 0.159f)
                // a 0.892 0.892 0 0 0 -0.17 1.038
                arcToRelative(
                    a = 0.892f,
                    b = 0.892f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.17f,
                    dy1 = 1.038f,
                )
                // l 0.21 0.415
                lineToRelative(dx = 0.21f, dy = 0.415f)
                // a 0.218 0.218 0 0 0 0.29 0.096
                arcToRelative(
                    a = 0.218f,
                    b = 0.218f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.29f,
                    dy1 = 0.096f,
                )
                // a 0.213 0.213 0 0 0 0.098 -0.287
                arcToRelative(
                    a = 0.213f,
                    b = 0.213f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.098f,
                    dy1 = -0.287f,
                )
                // l -0.21 -0.415
                lineToRelative(dx = -0.21f, dy = -0.415f)
                // a 0.467 0.467 0 0 1 0.09 -0.544
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.09f,
                    dy1 = -0.544f,
                )
                // l 0.16 -0.159
                lineToRelative(dx = 0.16f, dy = -0.159f)
                // a 0.892 0.892 0 0 0 0.17 -1.038
                arcToRelative(
                    a = 0.892f,
                    b = 0.892f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.17f,
                    dy1 = -1.038f,
                )
                // l -0.21 -0.415z
                lineToRelative(dx = -0.21f, dy = -0.415f)
                close()
                // m -2.291 -0.096
                moveToRelative(dx = -2.291f, dy = -0.096f)
                // a 0.218 0.218 0 0 1 0.29 0.096
                arcToRelative(
                    a = 0.218f,
                    b = 0.218f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.29f,
                    dy1 = 0.096f,
                )
                // l 0.21 0.415
                lineToRelative(dx = 0.21f, dy = 0.415f)
                // a 0.892 0.892 0 0 1 -0.17 1.038
                arcToRelative(
                    a = 0.892f,
                    b = 0.892f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.17f,
                    dy1 = 1.038f,
                )
                // l -0.16 0.159
                lineToRelative(dx = -0.16f, dy = 0.159f)
                // a 0.467 0.467 0 0 0 -0.09 0.544
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.09f,
                    dy1 = 0.544f,
                )
                // l 0.21 0.415
                lineToRelative(dx = 0.21f, dy = 0.415f)
                // a 0.213 0.213 0 0 1 -0.096 0.287
                arcToRelative(
                    a = 0.213f,
                    b = 0.213f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.096f,
                    dy1 = 0.287f,
                )
                // a 0.218 0.218 0 0 1 -0.29 -0.096
                arcToRelative(
                    a = 0.218f,
                    b = 0.218f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.29f,
                    dy1 = -0.096f,
                )
                // l -0.21 -0.415
                lineToRelative(dx = -0.21f, dy = -0.415f)
                // a 0.892 0.892 0 0 1 0.17 -1.038
                arcToRelative(
                    a = 0.892f,
                    b = 0.892f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.17f,
                    dy1 = -1.038f,
                )
                // l 0.16 -0.159
                lineToRelative(dx = 0.16f, dy = -0.159f)
                // a 0.467 0.467 0 0 0 0.09 -0.544
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.09f,
                    dy1 = -0.544f,
                )
                // l -0.21 -0.415
                lineToRelative(dx = -0.21f, dy = -0.415f)
                // a 0.213 0.213 0 0 1 0.096 -0.287z
                arcToRelative(
                    a = 0.213f,
                    b = 0.213f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.096f,
                    dy1 = -0.287f,
                )
                close()
                // m 1 0
                moveToRelative(dx = 1.0f, dy = 0.0f)
                // a 0.218 0.218 0 0 1 0.29 0.096
                arcToRelative(
                    a = 0.218f,
                    b = 0.218f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.29f,
                    dy1 = 0.096f,
                )
                // l 0.21 0.415
                lineToRelative(dx = 0.21f, dy = 0.415f)
                // a 0.892 0.892 0 0 1 -0.17 1.038
                arcToRelative(
                    a = 0.892f,
                    b = 0.892f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.17f,
                    dy1 = 1.038f,
                )
                // l -0.16 0.159
                lineToRelative(dx = -0.16f, dy = 0.159f)
                // a 0.467 0.467 0 0 0 -0.09 0.544
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.09f,
                    dy1 = 0.544f,
                )
                // l 0.21 0.415
                lineToRelative(dx = 0.21f, dy = 0.415f)
                // a 0.213 0.213 0 0 1 -0.096 0.287
                arcToRelative(
                    a = 0.213f,
                    b = 0.213f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.096f,
                    dy1 = 0.287f,
                )
                // a 0.218 0.218 0 0 1 -0.29 -0.096
                arcToRelative(
                    a = 0.218f,
                    b = 0.218f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.29f,
                    dy1 = -0.096f,
                )
                // l -0.21 -0.415
                lineToRelative(dx = -0.21f, dy = -0.415f)
                // a 0.892 0.892 0 0 1 0.17 -1.038
                arcToRelative(
                    a = 0.892f,
                    b = 0.892f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.17f,
                    dy1 = -1.038f,
                )
                // l 0.16 -0.159
                lineToRelative(dx = 0.16f, dy = -0.159f)
                // a 0.467 0.467 0 0 0 0.09 -0.544
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.09f,
                    dy1 = -0.544f,
                )
                // l -0.21 -0.415
                lineToRelative(dx = -0.21f, dy = -0.415f)
                // a 0.213 0.213 0 0 1 0.096 -0.287z
                arcToRelative(
                    a = 0.213f,
                    b = 0.213f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.096f,
                    dy1 = -0.287f,
                )
                close()
            }
        }.build().also { _ic1024 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1024: ImageVector? = null
