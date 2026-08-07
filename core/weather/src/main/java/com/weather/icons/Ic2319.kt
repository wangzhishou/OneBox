package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2319: ImageVector
    get() {
        val current = _ic2319
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2319",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M10.653 1.243 a.329 .329 0 0 0 -.549 -.147 l-6.61 6.616 a.324 .324 0 0 0 .15 .542 l3.788 1.015 -.933 3.483 .83 -.416 a1.5 1.5 0 0 1 .873 -.144 l.679 -2.535 3.788 1.016 a.324 .324 0 0 0 .4 -.396 l-2.416 -9.034Z m-1.72 2.313 c.053 -.35 .48 -.55 .922 -.432 .443 .12 .712 .505 .582 .835 l-1.14 2.898 -.824 -.22 .46 -3.08Z m.236 4.561 a.625 .625 0 1 1 -1.207 -.323 .625 .625 0 0 1 1.207 .323Z M.084 14.283 a.5 .5 0 0 1 .693 -.139 l1.018 .678 a.5 .5 0 0 0 .5 .031 l1.034 -.517 a1.5 1.5 0 0 1 1.342 0 l1.105 .553 a.5 .5 0 0 0 .448 0 l1.105 -.553 a1.5 1.5 0 0 1 1.342 0 l1.105 .553 a.5 .5 0 0 0 .448 0 l1.105 -.553 a1.5 1.5 0 0 1 1.342 0 l1.034 .517 a.5 .5 0 0 0 .5 -.03 l1.018 -.679 a.5 .5 0 1 1 .554 .832 l-1.017 .678 a1.5 1.5 0 0 1 -1.503 .094 l-1.033 -.517 a.5 .5 0 0 0 -.448 0 l-1.105 .553 a1.5 1.5 0 0 1 -1.342 0 l-1.105 -.553 a.5 .5 0 0 0 -.448 0 l-1.105 .553 a1.5 1.5 0 0 1 -1.342 0 l-1.105 -.553 a.5 .5 0 0 0 -.448 0 l-1.033 .517 a1.5 1.5 0 0 1 -1.503 -.094 l-1.017 -.678 a.5 .5 0 0 1 -.139 -.693Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10.653 1.243
                moveTo(x = 10.653f, y = 1.243f)
                // a 0.329 0.329 0 0 0 -0.549 -0.147
                arcToRelative(
                    a = 0.329f,
                    b = 0.329f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.549f,
                    dy1 = -0.147f,
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
                // l 3.788 1.015
                lineToRelative(dx = 3.788f, dy = 1.015f)
                // l -0.933 3.483
                lineToRelative(dx = -0.933f, dy = 3.483f)
                // l 0.83 -0.416
                lineToRelative(dx = 0.83f, dy = -0.416f)
                // a 1.5 1.5 0 0 1 0.873 -0.144
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.873f,
                    dy1 = -0.144f,
                )
                // l 0.679 -2.535
                lineToRelative(dx = 0.679f, dy = -2.535f)
                // l 3.788 1.016
                lineToRelative(dx = 3.788f, dy = 1.016f)
                // a 0.324 0.324 0 0 0 0.4 -0.396
                arcToRelative(
                    a = 0.324f,
                    b = 0.324f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.4f,
                    dy1 = -0.396f,
                )
                // l -2.416 -9.034z
                lineToRelative(dx = -2.416f, dy = -9.034f)
                close()
                // m -1.72 2.313
                moveToRelative(dx = -1.72f, dy = 2.313f)
                // c 0.053 -0.35 0.48 -0.55 0.922 -0.432
                curveToRelative(
                    dx1 = 0.053f,
                    dy1 = -0.35f,
                    dx2 = 0.48f,
                    dy2 = -0.55f,
                    dx3 = 0.922f,
                    dy3 = -0.432f,
                )
                // c 0.443 0.12 0.712 0.505 0.582 0.835
                curveToRelative(
                    dx1 = 0.443f,
                    dy1 = 0.12f,
                    dx2 = 0.712f,
                    dy2 = 0.505f,
                    dx3 = 0.582f,
                    dy3 = 0.835f,
                )
                // l -1.14 2.898
                lineToRelative(dx = -1.14f, dy = 2.898f)
                // l -0.824 -0.22
                lineToRelative(dx = -0.824f, dy = -0.22f)
                // l 0.46 -3.08z
                lineToRelative(dx = 0.46f, dy = -3.08f)
                close()
                // m 0.236 4.561
                moveToRelative(dx = 0.236f, dy = 4.561f)
                // a 0.625 0.625 0 1 1 -1.207 -0.323
                arcToRelative(
                    a = 0.625f,
                    b = 0.625f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.207f,
                    dy1 = -0.323f,
                )
                // a 0.625 0.625 0 0 1 1.207 0.323z
                arcToRelative(
                    a = 0.625f,
                    b = 0.625f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.207f,
                    dy1 = 0.323f,
                )
                close()
                // M 0.084 14.283
                moveTo(x = 0.084f, y = 14.283f)
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
            // M.084 12.283 a.5 .5 0 0 1 .693 -.139 l1.018 .678 a.5 .5 0 0 0 .5 .031 l1.034 -.517 a1.5 1.5 0 0 1 1.342 0 l1.105 .553 a.5 .5 0 0 0 .448 0 l1.105 -.553 a1.5 1.5 0 0 1 1.342 0 l1.105 .553 a.5 .5 0 0 0 .448 0 l1.105 -.553 a1.5 1.5 0 0 1 1.342 0 l1.034 .517 a.5 .5 0 0 0 .5 -.03 l1.018 -.679 a.5 .5 0 1 1 .554 .832 l-1.017 .678 a1.5 1.5 0 0 1 -1.503 .094 l-1.033 -.517 a.5 .5 0 0 0 -.448 0 l-1.105 .553 a1.5 1.5 0 0 1 -1.342 0 l-1.105 -.553 a.5 .5 0 0 0 -.448 0 l-1.105 .553 a1.5 1.5 0 0 1 -1.342 0 l-1.105 -.553 a.5 .5 0 0 0 -.448 0 l-1.033 .517 a1.5 1.5 0 0 1 -1.503 -.094 l-1.017 -.678 a.5 .5 0 0 1 -.139 -.693Z
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
            }
        }.build().also { _ic2319 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2319: ImageVector? = null
