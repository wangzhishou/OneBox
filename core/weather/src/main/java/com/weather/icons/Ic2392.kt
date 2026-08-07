package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2392: ImageVector
    get() {
        val current = _ic2392
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2392",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.762 3.753 c.055 -.332 .408 -.535 .762 -.44 .354 .095 .558 .447 .44 .762 L8.931 6.842 l-.658 -.177 .489 -2.912Z m.042 4.057 a.469 .469 0 1 1 -.906 -.242 .469 .469 0 0 1 .906 .243Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.762 3.753
                moveTo(x = 8.762f, y = 3.753f)
                // c 0.055 -0.332 0.408 -0.535 0.762 -0.44
                curveToRelative(
                    dx1 = 0.055f,
                    dy1 = -0.332f,
                    dx2 = 0.408f,
                    dy2 = -0.535f,
                    dx3 = 0.762f,
                    dy3 = -0.44f,
                )
                // c 0.354 0.095 0.558 0.447 0.44 0.762
                curveToRelative(
                    dx1 = 0.354f,
                    dy1 = 0.095f,
                    dx2 = 0.558f,
                    dy2 = 0.447f,
                    dx3 = 0.44f,
                    dy3 = 0.762f,
                )
                // L 8.931 6.842
                lineTo(x = 8.931f, y = 6.842f)
                // l -0.658 -0.177
                lineToRelative(dx = -0.658f, dy = -0.177f)
                // l 0.489 -2.912z
                lineToRelative(dx = 0.489f, dy = -2.912f)
                close()
                // m 0.042 4.057
                moveToRelative(dx = 0.042f, dy = 4.057f)
                // a 0.469 0.469 0 1 1 -0.906 -0.242
                arcToRelative(
                    a = 0.469f,
                    b = 0.469f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.906f,
                    dy1 = -0.242f,
                )
                // a 0.469 0.469 0 0 1 0.906 0.243z
                arcToRelative(
                    a = 0.469f,
                    b = 0.469f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.906f,
                    dy1 = 0.243f,
                )
                close()
            }
            // M10.403 1.13 a.329 .329 0 0 0 -.549 -.148 l-6.61 6.616 a.324 .324 0 0 0 .15 .542 l3.788 1.016 -1.006 3.754 a.51 .51 0 0 0 .048 -.02 l1.105 -.554 a1.5 1.5 0 0 1 .596 -.156 l.706 -2.636 3.788 1.015 a.324 .324 0 0 0 .4 -.395 l-2.416 -9.035Z m-.535 .9 2.036 7.614 -7.607 -2.038 5.57 -5.576Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10.403 1.13
                moveTo(x = 10.403f, y = 1.13f)
                // a 0.329 0.329 0 0 0 -0.549 -0.148
                arcToRelative(
                    a = 0.329f,
                    b = 0.329f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.549f,
                    dy1 = -0.148f,
                )
                // l -6.61 6.616
                lineToRelative(dx = -6.61f, dy = 6.616f)
                // a 0.324 0.324 0 0 0 0.15 0.542
                arcToRelative(
                    a = 0.324f,
                    b = 0.324f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.15f,
                    dy1 = 0.542f,
                )
                // l 3.788 1.016
                lineToRelative(dx = 3.788f, dy = 1.016f)
                // l -1.006 3.754
                lineToRelative(dx = -1.006f, dy = 3.754f)
                // a 0.51 0.51 0 0 0 0.048 -0.02
                arcToRelative(
                    a = 0.51f,
                    b = 0.51f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.048f,
                    dy1 = -0.02f,
                )
                // l 1.105 -0.554
                lineToRelative(dx = 1.105f, dy = -0.554f)
                // a 1.5 1.5 0 0 1 0.596 -0.156
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.596f,
                    dy1 = -0.156f,
                )
                // l 0.706 -2.636
                lineToRelative(dx = 0.706f, dy = -2.636f)
                // l 3.788 1.015
                lineToRelative(dx = 3.788f, dy = 1.015f)
                // a 0.324 0.324 0 0 0 0.4 -0.395
                arcToRelative(
                    a = 0.324f,
                    b = 0.324f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.4f,
                    dy1 = -0.395f,
                )
                // l -2.416 -9.035z
                lineToRelative(dx = -2.416f, dy = -9.035f)
                close()
                // m -0.535 0.9
                moveToRelative(dx = -0.535f, dy = 0.9f)
                // l 2.036 7.614
                lineToRelative(dx = 2.036f, dy = 7.614f)
                // l -7.607 -2.038
                lineToRelative(dx = -7.607f, dy = -2.038f)
                // l 5.57 -5.576z
                lineToRelative(dx = 5.57f, dy = -5.576f)
                close()
            }
            // M.084 12.283 a.5 .5 0 0 1 .693 -.139 l1.018 .678 a.5 .5 0 0 0 .5 .031 l1.034 -.517 a1.5 1.5 0 0 1 1.342 0 l1.105 .553 a.5 .5 0 0 0 .448 0 l1.105 -.553 a1.5 1.5 0 0 1 1.342 0 l1.105 .553 a.5 .5 0 0 0 .448 0 l1.105 -.553 a1.5 1.5 0 0 1 1.342 0 l1.034 .517 a.5 .5 0 0 0 .5 -.03 l1.018 -.679 a.5 .5 0 1 1 .554 .832 l-1.017 .678 a1.5 1.5 0 0 1 -1.503 .094 l-1.033 -.517 a.5 .5 0 0 0 -.448 0 l-1.105 .553 a1.5 1.5 0 0 1 -1.342 0 l-1.105 -.553 a.5 .5 0 0 0 -.448 0 l-1.105 .553 a1.5 1.5 0 0 1 -1.342 0 l-1.105 -.553 a.5 .5 0 0 0 -.448 0 l-1.033 .517 a1.5 1.5 0 0 1 -1.503 -.094 l-1.017 -.678 a.5 .5 0 0 1 -.139 -.693Z m0 2 a.5 .5 0 0 1 .693 -.139 l1.018 .678 a.5 .5 0 0 0 .5 .031 l1.034 -.517 a1.5 1.5 0 0 1 1.342 0 l1.105 .553 a.5 .5 0 0 0 .448 0 l1.105 -.553 a1.5 1.5 0 0 1 1.342 0 l1.105 .553 a.5 .5 0 0 0 .448 0 l1.105 -.553 a1.5 1.5 0 0 1 1.342 0 l1.034 .517 a.5 .5 0 0 0 .5 -.03 l1.018 -.679 a.5 .5 0 1 1 .554 .832 l-1.017 .678 a1.5 1.5 0 0 1 -1.503 .094 l-1.033 -.517 a.5 .5 0 0 0 -.448 0 l-1.105 .553 a1.5 1.5 0 0 1 -1.342 0 l-1.105 -.553 a.5 .5 0 0 0 -.448 0 l-1.105 .553 a1.5 1.5 0 0 1 -1.342 0 l-1.105 -.553 a.5 .5 0 0 0 -.448 0 l-1.033 .517 a1.5 1.5 0 0 1 -1.503 -.094 l-1.017 -.678 a.5 .5 0 0 1 -.139 -.693Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0.084 12.283
                moveTo(x = 0.084f, y = 12.283f)
                // a 0.5 0.5 0 0 1 0.693 -0.139
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.693f,
                    dy1 = -0.139f,
                )
                // l 1.018 0.678
                lineToRelative(dx = 1.018f, dy = 0.678f)
                // a 0.5 0.5 0 0 0 0.5 0.031
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.5f,
                    dy1 = 0.031f,
                )
                // l 1.034 -0.517
                lineToRelative(dx = 1.034f, dy = -0.517f)
                // a 1.5 1.5 0 0 1 1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.342f,
                    dy1 = 0.0f,
                )
                // l 1.105 0.553
                lineToRelative(dx = 1.105f, dy = 0.553f)
                // a 0.5 0.5 0 0 0 0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.448f,
                    dy1 = 0.0f,
                )
                // l 1.105 -0.553
                lineToRelative(dx = 1.105f, dy = -0.553f)
                // a 1.5 1.5 0 0 1 1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.342f,
                    dy1 = 0.0f,
                )
                // l 1.105 0.553
                lineToRelative(dx = 1.105f, dy = 0.553f)
                // a 0.5 0.5 0 0 0 0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.448f,
                    dy1 = 0.0f,
                )
                // l 1.105 -0.553
                lineToRelative(dx = 1.105f, dy = -0.553f)
                // a 1.5 1.5 0 0 1 1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.342f,
                    dy1 = 0.0f,
                )
                // l 1.034 0.517
                lineToRelative(dx = 1.034f, dy = 0.517f)
                // a 0.5 0.5 0 0 0 0.5 -0.03
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.5f,
                    dy1 = -0.03f,
                )
                // l 1.018 -0.679
                lineToRelative(dx = 1.018f, dy = -0.679f)
                // a 0.5 0.5 0 1 1 0.554 0.832
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.554f,
                    dy1 = 0.832f,
                )
                // l -1.017 0.678
                lineToRelative(dx = -1.017f, dy = 0.678f)
                // a 1.5 1.5 0 0 1 -1.503 0.094
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.503f,
                    dy1 = 0.094f,
                )
                // l -1.033 -0.517
                lineToRelative(dx = -1.033f, dy = -0.517f)
                // a 0.5 0.5 0 0 0 -0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.448f,
                    dy1 = 0.0f,
                )
                // l -1.105 0.553
                lineToRelative(dx = -1.105f, dy = 0.553f)
                // a 1.5 1.5 0 0 1 -1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.342f,
                    dy1 = 0.0f,
                )
                // l -1.105 -0.553
                lineToRelative(dx = -1.105f, dy = -0.553f)
                // a 0.5 0.5 0 0 0 -0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.448f,
                    dy1 = 0.0f,
                )
                // l -1.105 0.553
                lineToRelative(dx = -1.105f, dy = 0.553f)
                // a 1.5 1.5 0 0 1 -1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.342f,
                    dy1 = 0.0f,
                )
                // l -1.105 -0.553
                lineToRelative(dx = -1.105f, dy = -0.553f)
                // a 0.5 0.5 0 0 0 -0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.448f,
                    dy1 = 0.0f,
                )
                // l -1.033 0.517
                lineToRelative(dx = -1.033f, dy = 0.517f)
                // a 1.5 1.5 0 0 1 -1.503 -0.094
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.503f,
                    dy1 = -0.094f,
                )
                // l -1.017 -0.678
                lineToRelative(dx = -1.017f, dy = -0.678f)
                // a 0.5 0.5 0 0 1 -0.139 -0.693z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.139f,
                    dy1 = -0.693f,
                )
                close()
                // m 0 2
                moveToRelative(dx = 0.0f, dy = 2.0f)
                // a 0.5 0.5 0 0 1 0.693 -0.139
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.693f,
                    dy1 = -0.139f,
                )
                // l 1.018 0.678
                lineToRelative(dx = 1.018f, dy = 0.678f)
                // a 0.5 0.5 0 0 0 0.5 0.031
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.5f,
                    dy1 = 0.031f,
                )
                // l 1.034 -0.517
                lineToRelative(dx = 1.034f, dy = -0.517f)
                // a 1.5 1.5 0 0 1 1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.342f,
                    dy1 = 0.0f,
                )
                // l 1.105 0.553
                lineToRelative(dx = 1.105f, dy = 0.553f)
                // a 0.5 0.5 0 0 0 0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.448f,
                    dy1 = 0.0f,
                )
                // l 1.105 -0.553
                lineToRelative(dx = 1.105f, dy = -0.553f)
                // a 1.5 1.5 0 0 1 1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.342f,
                    dy1 = 0.0f,
                )
                // l 1.105 0.553
                lineToRelative(dx = 1.105f, dy = 0.553f)
                // a 0.5 0.5 0 0 0 0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.448f,
                    dy1 = 0.0f,
                )
                // l 1.105 -0.553
                lineToRelative(dx = 1.105f, dy = -0.553f)
                // a 1.5 1.5 0 0 1 1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.342f,
                    dy1 = 0.0f,
                )
                // l 1.034 0.517
                lineToRelative(dx = 1.034f, dy = 0.517f)
                // a 0.5 0.5 0 0 0 0.5 -0.03
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.5f,
                    dy1 = -0.03f,
                )
                // l 1.018 -0.679
                lineToRelative(dx = 1.018f, dy = -0.679f)
                // a 0.5 0.5 0 1 1 0.554 0.832
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.554f,
                    dy1 = 0.832f,
                )
                // l -1.017 0.678
                lineToRelative(dx = -1.017f, dy = 0.678f)
                // a 1.5 1.5 0 0 1 -1.503 0.094
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.503f,
                    dy1 = 0.094f,
                )
                // l -1.033 -0.517
                lineToRelative(dx = -1.033f, dy = -0.517f)
                // a 0.5 0.5 0 0 0 -0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.448f,
                    dy1 = 0.0f,
                )
                // l -1.105 0.553
                lineToRelative(dx = -1.105f, dy = 0.553f)
                // a 1.5 1.5 0 0 1 -1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.342f,
                    dy1 = 0.0f,
                )
                // l -1.105 -0.553
                lineToRelative(dx = -1.105f, dy = -0.553f)
                // a 0.5 0.5 0 0 0 -0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.448f,
                    dy1 = 0.0f,
                )
                // l -1.105 0.553
                lineToRelative(dx = -1.105f, dy = 0.553f)
                // a 1.5 1.5 0 0 1 -1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.342f,
                    dy1 = 0.0f,
                )
                // l -1.105 -0.553
                lineToRelative(dx = -1.105f, dy = -0.553f)
                // a 0.5 0.5 0 0 0 -0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.448f,
                    dy1 = 0.0f,
                )
                // l -1.033 0.517
                lineToRelative(dx = -1.033f, dy = 0.517f)
                // a 1.5 1.5 0 0 1 -1.503 -0.094
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.503f,
                    dy1 = -0.094f,
                )
                // l -1.017 -0.678
                lineToRelative(dx = -1.017f, dy = -0.678f)
                // a 0.5 0.5 0 0 1 -0.139 -0.693z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.139f,
                    dy1 = -0.693f,
                )
                close()
            }
        }.build().also { _ic2392 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2392: ImageVector? = null
