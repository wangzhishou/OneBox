package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic504: ImageVector
    get() {
        val current = _ic504
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic504",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7 1.5 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z m-4 7 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z M7.5 8 a.5 .5 0 1 0 0 -1 .5 .5 0 0 0 0 1Z m0 6.75 a.75 .75 0 1 1 -1.5 0 .75 .75 0 0 1 1.5 0Z m6.25 -1.25 a.75 .75 0 1 0 0 -1.5 .75 .75 0 0 0 0 1.5Z M10 4 a1 1 0 1 1 -2 0 1 1 0 0 1 2 0Z M5 7 a1 1 0 1 0 0 -2 1 1 0 0 0 0 2Z m6.5 7.25 a1.25 1.25 0 1 1 -2.5 0 1.25 1.25 0 0 1 2.5 0Z M2 4 a1 1 0 1 0 0 -2 1 1 0 0 0 0 2Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7 1.5
                moveTo(x = 7.0f, y = 1.5f)
                // a 0.5 0.5 0 1 1 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // a 0.5 0.5 0 0 1 1 0z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                close()
                // m -4 7
                moveToRelative(dx = -4.0f, dy = 7.0f)
                // a 0.5 0.5 0 1 1 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // a 0.5 0.5 0 0 1 1 0z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                close()
                // M 7.5 8
                moveTo(x = 7.5f, y = 8.0f)
                // a 0.5 0.5 0 1 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // a 0.5 0.5 0 0 0 0 1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                close()
                // m 0 6.75
                moveToRelative(dx = 0.0f, dy = 6.75f)
                // a 0.75 0.75 0 1 1 -1.5 0
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.5f,
                    dy1 = 0.0f,
                )
                // a 0.75 0.75 0 0 1 1.5 0z
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.5f,
                    dy1 = 0.0f,
                )
                close()
                // m 6.25 -1.25
                moveToRelative(dx = 6.25f, dy = -1.25f)
                // a 0.75 0.75 0 1 0 0 -1.5
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.5f,
                )
                // a 0.75 0.75 0 0 0 0 1.5z
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.5f,
                )
                close()
                // M 10 4
                moveTo(x = 10.0f, y = 4.0f)
                // a 1 1 0 1 1 -2 0
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -2.0f,
                    dy1 = 0.0f,
                )
                // a 1 1 0 0 1 2 0z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.0f,
                    dy1 = 0.0f,
                )
                close()
                // M 5 7
                moveTo(x = 5.0f, y = 7.0f)
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
                // a 1 1 0 0 0 0 2z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 2.0f,
                )
                close()
                // m 6.5 7.25
                moveToRelative(dx = 6.5f, dy = 7.25f)
                // a 1.25 1.25 0 1 1 -2.5 0
                arcToRelative(
                    a = 1.25f,
                    b = 1.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -2.5f,
                    dy1 = 0.0f,
                )
                // a 1.25 1.25 0 0 1 2.5 0z
                arcToRelative(
                    a = 1.25f,
                    b = 1.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.5f,
                    dy1 = 0.0f,
                )
                close()
                // M 2 4
                moveTo(x = 2.0f, y = 4.0f)
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
                // a 1 1 0 0 0 0 2z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 2.0f,
                )
                close()
            }
            // M4.323 11.87 a13.333 13.333 0 0 1 -.804 -.139 A14.111 14.111 0 0 1 0 10.501 c1.162 .27 2.34 .404 3.519 .4 .286 0 .564 -.012 .834 -.037 C9.075 10.434 11.478 6.419 12 5 c.296 -.806 .238 -1.6 -.175 -2.323 -.356 -.623 -.974 -1.194 -1.856 -1.677 .93 .22 1.788 .525 2.547 .898 C14.643 2.943 16 4.526 16 6.298 c0 1.32 -.777 2.484 -2.018 3.502 a5.19 5.19 0 0 1 -1.43 .882 c-1.961 .907 -4.026 1.352 -6.1 1.315 a13.14 13.14 0 0 1 -2.129 -.127Z m8.815 -8.453 a3.767 3.767 0 0 1 -.2 1.928 c-.326 .888 -1.212 2.5 -2.728 3.902 a10.22 10.22 0 0 1 -2.49 1.71 13.09 13.09 0 0 0 4.412 -1.182 l.022 -.01 .023 -.01 a4.19 4.19 0 0 0 1.154 -.714 l.008 -.007 .009 -.007 C14.48 8.097 15 7.19 15 6.297 c0 -.83 -.428 -1.689 -1.344 -2.479 a6.397 6.397 0 0 0 -.518 -.4Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.323 11.87
                moveTo(x = 4.323f, y = 11.87f)
                // a 13.333 13.333 0 0 1 -0.804 -0.139
                arcToRelative(
                    a = 13.333f,
                    b = 13.333f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.804f,
                    dy1 = -0.139f,
                )
                // A 14.111 14.111 0 0 1 0 10.501
                arcTo(
                    horizontalEllipseRadius = 14.111f,
                    verticalEllipseRadius = 14.111f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 0.0f,
                    y1 = 10.501f,
                )
                // c 1.162 0.27 2.34 0.404 3.519 0.4
                curveToRelative(
                    dx1 = 1.162f,
                    dy1 = 0.27f,
                    dx2 = 2.34f,
                    dy2 = 0.404f,
                    dx3 = 3.519f,
                    dy3 = 0.4f,
                )
                // c 0.286 0 0.564 -0.012 0.834 -0.037
                curveToRelative(
                    dx1 = 0.286f,
                    dy1 = 0.0f,
                    dx2 = 0.564f,
                    dy2 = -0.012f,
                    dx3 = 0.834f,
                    dy3 = -0.037f,
                )
                // C 9.075 10.434 11.478 6.419 12 5
                curveTo(
                    x1 = 9.075f,
                    y1 = 10.434f,
                    x2 = 11.478f,
                    y2 = 6.419f,
                    x3 = 12.0f,
                    y3 = 5.0f,
                )
                // c 0.296 -0.806 0.238 -1.6 -0.175 -2.323
                curveToRelative(
                    dx1 = 0.296f,
                    dy1 = -0.806f,
                    dx2 = 0.238f,
                    dy2 = -1.6f,
                    dx3 = -0.175f,
                    dy3 = -2.323f,
                )
                // c -0.356 -0.623 -0.974 -1.194 -1.856 -1.677
                curveToRelative(
                    dx1 = -0.356f,
                    dy1 = -0.623f,
                    dx2 = -0.974f,
                    dy2 = -1.194f,
                    dx3 = -1.856f,
                    dy3 = -1.677f,
                )
                // c 0.93 0.22 1.788 0.525 2.547 0.898
                curveToRelative(
                    dx1 = 0.93f,
                    dy1 = 0.22f,
                    dx2 = 1.788f,
                    dy2 = 0.525f,
                    dx3 = 2.547f,
                    dy3 = 0.898f,
                )
                // C 14.643 2.943 16 4.526 16 6.298
                curveTo(
                    x1 = 14.643f,
                    y1 = 2.943f,
                    x2 = 16.0f,
                    y2 = 4.526f,
                    x3 = 16.0f,
                    y3 = 6.298f,
                )
                // c 0 1.32 -0.777 2.484 -2.018 3.502
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.32f,
                    dx2 = -0.777f,
                    dy2 = 2.484f,
                    dx3 = -2.018f,
                    dy3 = 3.502f,
                )
                // a 5.19 5.19 0 0 1 -1.43 0.882
                arcToRelative(
                    a = 5.19f,
                    b = 5.19f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.43f,
                    dy1 = 0.882f,
                )
                // c -1.961 0.907 -4.026 1.352 -6.1 1.315
                curveToRelative(
                    dx1 = -1.961f,
                    dy1 = 0.907f,
                    dx2 = -4.026f,
                    dy2 = 1.352f,
                    dx3 = -6.1f,
                    dy3 = 1.315f,
                )
                // a 13.14 13.14 0 0 1 -2.129 -0.127z
                arcToRelative(
                    a = 13.14f,
                    b = 13.14f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.129f,
                    dy1 = -0.127f,
                )
                close()
                // m 8.815 -8.453
                moveToRelative(dx = 8.815f, dy = -8.453f)
                // a 3.767 3.767 0 0 1 -0.2 1.928
                arcToRelative(
                    a = 3.767f,
                    b = 3.767f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.2f,
                    dy1 = 1.928f,
                )
                // c -0.326 0.888 -1.212 2.5 -2.728 3.902
                curveToRelative(
                    dx1 = -0.326f,
                    dy1 = 0.888f,
                    dx2 = -1.212f,
                    dy2 = 2.5f,
                    dx3 = -2.728f,
                    dy3 = 3.902f,
                )
                // a 10.22 10.22 0 0 1 -2.49 1.71
                arcToRelative(
                    a = 10.22f,
                    b = 10.22f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.49f,
                    dy1 = 1.71f,
                )
                // a 13.09 13.09 0 0 0 4.412 -1.182
                arcToRelative(
                    a = 13.09f,
                    b = 13.09f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 4.412f,
                    dy1 = -1.182f,
                )
                // l 0.022 -0.01
                lineToRelative(dx = 0.022f, dy = -0.01f)
                // l 0.023 -0.01
                lineToRelative(dx = 0.023f, dy = -0.01f)
                // a 4.19 4.19 0 0 0 1.154 -0.714
                arcToRelative(
                    a = 4.19f,
                    b = 4.19f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.154f,
                    dy1 = -0.714f,
                )
                // l 0.008 -0.007
                lineToRelative(dx = 0.008f, dy = -0.007f)
                // l 0.009 -0.007
                lineToRelative(dx = 0.009f, dy = -0.007f)
                // C 14.48 8.097 15 7.19 15 6.297
                curveTo(
                    x1 = 14.48f,
                    y1 = 8.097f,
                    x2 = 15.0f,
                    y2 = 7.19f,
                    x3 = 15.0f,
                    y3 = 6.297f,
                )
                // c 0 -0.83 -0.428 -1.689 -1.344 -2.479
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.83f,
                    dx2 = -0.428f,
                    dy2 = -1.689f,
                    dx3 = -1.344f,
                    dy3 = -2.479f,
                )
                // a 6.397 6.397 0 0 0 -0.518 -0.4z
                arcToRelative(
                    a = 6.397f,
                    b = 6.397f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.518f,
                    dy1 = -0.4f,
                )
                close()
            }
        }.build().also { _ic504 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic504: ImageVector? = null
