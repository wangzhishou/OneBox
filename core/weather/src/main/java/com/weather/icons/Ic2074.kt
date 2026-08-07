package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2074: ImageVector
    get() {
        val current = _ic2074
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2074",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M3.002 1.313 a1.687 1.687 0 1 0 0 3.374 1.687 1.687 0 0 0 0 -3.375Z m.001 -.374 A.188 .188 0 0 1 2.816 .75 V.19 a.187 .187 0 0 1 .375 0 v.56 a.187 .187 0 0 1 -.188 .188Z m-1.59 .657 a.187 .187 0 0 1 -.133 -.056 l-.398 -.397 a.187 .187 0 0 1 .265 -.265 l.399 .397 a.188 .188 0 0 1 -.133 .32Z M.75 3.185 H.19 a.188 .188 0 1 1 0 -.375 h.56 a.188 .188 0 0 1 0 .375Z m.26 1.988 a.188 .188 0 0 1 -.133 -.32 l.398 -.397 a.188 .188 0 0 1 .265 .265 l-.398 .397 a.187 .187 0 0 1 -.132 .055Z M2.998 6 a.188 .188 0 0 1 -.187 -.188 V5.25 a.187 .187 0 1 1 .375 0 v.562 A.187 .187 0 0 1 2.998 6Z m1.99 -.822 a.187 .187 0 0 1 -.133 -.055 l-.398 -.397 a.187 .187 0 1 1 .265 -.265 l.398 .397 a.187 .187 0 0 1 -.133 .32Z m.826 -1.987 H5.25 a.188 .188 0 1 1 0 -.375 h.563 a.188 .188 0 0 1 0 .375Z M4.594 1.6 a.188 .188 0 0 1 -.133 -.32 l.397 -.398 a.188 .188 0 0 1 .266 .265 l-.398 .397 a.187 .187 0 0 1 -.132 .055Z m3.074 7.155 .532 -.356 V2.5 a1.3 1.3 0 0 1 2.6 0 v.9 a.6 .6 0 1 0 1.2 0 v-.9 a2.5 2.5 0 0 0 -5 0 v5.258 a4.5 4.5 0 1 0 5 0 V6.6 a.6 .6 0 1 0 -1.2 0 v1.799 l.532 .356 a3.3 3.3 0 1 1 -3.665 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 3.002 1.313
                moveTo(x = 3.002f, y = 1.313f)
                // a 1.687 1.687 0 1 0 0 3.374
                arcToRelative(
                    a = 1.687f,
                    b = 1.687f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 3.374f,
                )
                // a 1.687 1.687 0 0 0 0 -3.375z
                arcToRelative(
                    a = 1.687f,
                    b = 1.687f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -3.375f,
                )
                close()
                // m 0.001 -0.374
                moveToRelative(dx = 0.001f, dy = -0.374f)
                // A 0.188 0.188 0 0 1 2.816 0.75
                arcTo(
                    horizontalEllipseRadius = 0.188f,
                    verticalEllipseRadius = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 2.816f,
                    y1 = 0.75f,
                )
                // V 0.19
                verticalLineTo(y = 0.19f)
                // a 0.187 0.187 0 0 1 0.375 0
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.375f,
                    dy1 = 0.0f,
                )
                // v 0.56
                verticalLineToRelative(dy = 0.56f)
                // a 0.187 0.187 0 0 1 -0.188 0.188z
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.188f,
                    dy1 = 0.188f,
                )
                close()
                // m -1.59 0.657
                moveToRelative(dx = -1.59f, dy = 0.657f)
                // a 0.187 0.187 0 0 1 -0.133 -0.056
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.133f,
                    dy1 = -0.056f,
                )
                // l -0.398 -0.397
                lineToRelative(dx = -0.398f, dy = -0.397f)
                // a 0.187 0.187 0 0 1 0.265 -0.265
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.265f,
                    dy1 = -0.265f,
                )
                // l 0.399 0.397
                lineToRelative(dx = 0.399f, dy = 0.397f)
                // a 0.188 0.188 0 0 1 -0.133 0.32z
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.133f,
                    dy1 = 0.32f,
                )
                close()
                // M 0.75 3.185
                moveTo(x = 0.75f, y = 3.185f)
                // H 0.19
                horizontalLineTo(x = 0.19f)
                // a 0.188 0.188 0 1 1 0 -0.375
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -0.375f,
                )
                // h 0.56
                horizontalLineToRelative(dx = 0.56f)
                // a 0.188 0.188 0 0 1 0 0.375z
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.375f,
                )
                close()
                // m 0.26 1.988
                moveToRelative(dx = 0.26f, dy = 1.988f)
                // a 0.188 0.188 0 0 1 -0.133 -0.32
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.133f,
                    dy1 = -0.32f,
                )
                // l 0.398 -0.397
                lineToRelative(dx = 0.398f, dy = -0.397f)
                // a 0.188 0.188 0 0 1 0.265 0.265
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.265f,
                    dy1 = 0.265f,
                )
                // l -0.398 0.397
                lineToRelative(dx = -0.398f, dy = 0.397f)
                // a 0.187 0.187 0 0 1 -0.132 0.055z
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.132f,
                    dy1 = 0.055f,
                )
                close()
                // M 2.998 6
                moveTo(x = 2.998f, y = 6.0f)
                // a 0.188 0.188 0 0 1 -0.187 -0.188
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.187f,
                    dy1 = -0.188f,
                )
                // V 5.25
                verticalLineTo(y = 5.25f)
                // a 0.187 0.187 0 1 1 0.375 0
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.375f,
                    dy1 = 0.0f,
                )
                // v 0.562
                verticalLineToRelative(dy = 0.562f)
                // A 0.187 0.187 0 0 1 2.998 6z
                arcTo(
                    horizontalEllipseRadius = 0.187f,
                    verticalEllipseRadius = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 2.998f,
                    y1 = 6.0f,
                )
                close()
                // m 1.99 -0.822
                moveToRelative(dx = 1.99f, dy = -0.822f)
                // a 0.187 0.187 0 0 1 -0.133 -0.055
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.133f,
                    dy1 = -0.055f,
                )
                // l -0.398 -0.397
                lineToRelative(dx = -0.398f, dy = -0.397f)
                // a 0.187 0.187 0 1 1 0.265 -0.265
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.265f,
                    dy1 = -0.265f,
                )
                // l 0.398 0.397
                lineToRelative(dx = 0.398f, dy = 0.397f)
                // a 0.187 0.187 0 0 1 -0.133 0.32z
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.133f,
                    dy1 = 0.32f,
                )
                close()
                // m 0.826 -1.987
                moveToRelative(dx = 0.826f, dy = -1.987f)
                // H 5.25
                horizontalLineTo(x = 5.25f)
                // a 0.188 0.188 0 1 1 0 -0.375
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -0.375f,
                )
                // h 0.563
                horizontalLineToRelative(dx = 0.563f)
                // a 0.188 0.188 0 0 1 0 0.375z
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.375f,
                )
                close()
                // M 4.594 1.6
                moveTo(x = 4.594f, y = 1.6f)
                // a 0.188 0.188 0 0 1 -0.133 -0.32
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.133f,
                    dy1 = -0.32f,
                )
                // l 0.397 -0.398
                lineToRelative(dx = 0.397f, dy = -0.398f)
                // a 0.188 0.188 0 0 1 0.266 0.265
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.266f,
                    dy1 = 0.265f,
                )
                // l -0.398 0.397
                lineToRelative(dx = -0.398f, dy = 0.397f)
                // a 0.187 0.187 0 0 1 -0.132 0.055z
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.132f,
                    dy1 = 0.055f,
                )
                close()
                // m 3.074 7.155
                moveToRelative(dx = 3.074f, dy = 7.155f)
                // l 0.532 -0.356
                lineToRelative(dx = 0.532f, dy = -0.356f)
                // V 2.5
                verticalLineTo(y = 2.5f)
                // a 1.3 1.3 0 0 1 2.6 0
                arcToRelative(
                    a = 1.3f,
                    b = 1.3f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.6f,
                    dy1 = 0.0f,
                )
                // v 0.9
                verticalLineToRelative(dy = 0.9f)
                // a 0.6 0.6 0 1 0 1.2 0
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 1.2f,
                    dy1 = 0.0f,
                )
                // v -0.9
                verticalLineToRelative(dy = -0.9f)
                // a 2.5 2.5 0 0 0 -5 0
                arcToRelative(
                    a = 2.5f,
                    b = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -5.0f,
                    dy1 = 0.0f,
                )
                // v 5.258
                verticalLineToRelative(dy = 5.258f)
                // a 4.5 4.5 0 1 0 5 0
                arcToRelative(
                    a = 4.5f,
                    b = 4.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 5.0f,
                    dy1 = 0.0f,
                )
                // V 6.6
                verticalLineTo(y = 6.6f)
                // a 0.6 0.6 0 1 0 -1.2 0
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -1.2f,
                    dy1 = 0.0f,
                )
                // v 1.799
                verticalLineToRelative(dy = 1.799f)
                // l 0.532 0.356
                lineToRelative(dx = 0.532f, dy = 0.356f)
                // a 3.3 3.3 0 1 1 -3.665 0z
                arcToRelative(
                    a = 3.3f,
                    b = 3.3f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -3.665f,
                    dy1 = 0.0f,
                )
                close()
            }
            // M9.5 7 a.5 .5 0 0 0 -.5 .5 v2.063 a2 2 0 1 0 1 0 V7.5 a.5 .5 0 0 0 -.5 -.5Z m2 -2 c0 -.331 .18 -.6 .4 -.6 h3.2 c.22 0 .4 .269 .4 .6 0 .331 -.18 .6 -.4 .6 h-3.2 c-.22 0 -.4 -.269 -.4 -.6Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 9.5 7
                moveTo(x = 9.5f, y = 7.0f)
                // a 0.5 0.5 0 0 0 -0.5 0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.5f,
                    dy1 = 0.5f,
                )
                // v 2.063
                verticalLineToRelative(dy = 2.063f)
                // a 2 2 0 1 0 1 0
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                // V 7.5
                verticalLineTo(y = 7.5f)
                // a 0.5 0.5 0 0 0 -0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                close()
                // m 2 -2
                moveToRelative(dx = 2.0f, dy = -2.0f)
                // c 0 -0.331 0.18 -0.6 0.4 -0.6
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.331f,
                    dx2 = 0.18f,
                    dy2 = -0.6f,
                    dx3 = 0.4f,
                    dy3 = -0.6f,
                )
                // h 3.2
                horizontalLineToRelative(dx = 3.2f)
                // c 0.22 0 0.4 0.269 0.4 0.6
                curveToRelative(
                    dx1 = 0.22f,
                    dy1 = 0.0f,
                    dx2 = 0.4f,
                    dy2 = 0.269f,
                    dx3 = 0.4f,
                    dy3 = 0.6f,
                )
                // c 0 0.331 -0.18 0.6 -0.4 0.6
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.331f,
                    dx2 = -0.18f,
                    dy2 = 0.6f,
                    dx3 = -0.4f,
                    dy3 = 0.6f,
                )
                // h -3.2
                horizontalLineToRelative(dx = -3.2f)
                // c -0.22 0 -0.4 -0.269 -0.4 -0.6z
                curveToRelative(
                    dx1 = -0.22f,
                    dy1 = 0.0f,
                    dx2 = -0.4f,
                    dy2 = -0.269f,
                    dx3 = -0.4f,
                    dy3 = -0.6f,
                )
                close()
            }
        }.build().also { _ic2074 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2074: ImageVector? = null
