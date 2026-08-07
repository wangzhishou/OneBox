package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1065: ImageVector
    get() {
        val current = _ic1065
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1065",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M.777 11.644 a.5 .5 0 1 0 -.554 .832 l1.017 .678 a1.5 1.5 0 0 0 1.503 .094 l1.033 -.517 a.5 .5 0 0 1 .448 0 l1.105 .553 a1.5 1.5 0 0 0 1.342 0 l1.105 -.553 a.5 .5 0 0 1 .448 0 l1.105 .553 a1.5 1.5 0 0 0 1.342 0 l1.105 -.553 a.5 .5 0 0 1 .448 0 l1.033 .517 a1.5 1.5 0 0 0 1.503 -.094 l1.017 -.678 a.5 .5 0 1 0 -.554 -.832 l-1.017 .678 a.5 .5 0 0 1 -.501 .031 l-1.034 -.517 a1.5 1.5 0 0 0 -1.342 0 l-1.105 .553 a.5 .5 0 0 1 -.448 0 l-1.105 -.553 a1.5 1.5 0 0 0 -1.342 0 l-1.105 .553 a.5 .5 0 0 1 -.448 0 l-1.105 -.553 a1.5 1.5 0 0 0 -1.342 0 l-1.034 .517 a.5 .5 0 0 1 -.5 -.03 l-1.018 -.679Z m0 2.5 a.5 .5 0 1 0 -.554 .832 l1.017 .678 a1.5 1.5 0 0 0 1.503 .094 l1.033 -.517 a.5 .5 0 0 1 .448 0 l1.105 .553 a1.5 1.5 0 0 0 1.342 0 l1.105 -.553 a.5 .5 0 0 1 .448 0 l1.105 .553 a1.5 1.5 0 0 0 1.342 0 l1.105 -.553 a.5 .5 0 0 1 .448 0 l1.033 .517 a1.5 1.5 0 0 0 1.503 -.094 l1.017 -.678 a.5 .5 0 1 0 -.554 -.832 l-1.017 .678 a.5 .5 0 0 1 -.501 .031 l-1.034 -.517 a1.5 1.5 0 0 0 -1.342 0 l-1.105 .553 a.5 .5 0 0 1 -.448 0 l-1.105 -.553 a1.5 1.5 0 0 0 -1.342 0 l-1.105 .553 a.5 .5 0 0 1 -.448 0 l-1.105 -.553 a1.5 1.5 0 0 0 -1.342 0 l-1.034 .517 a.5 .5 0 0 1 -.5 -.03 l-1.018 -.679Z m10.717 -6.248 A4.758 4.758 0 0 1 7.906 9.5 4.76 4.76 0 0 1 4.37 7.953 a2.907 2.907 0 0 1 -1.056 .197 C1.758 8.15 .5 6.941 .5 5.45 s1.26 -2.7 2.813 -2.7 c.173 0 .342 .015 .507 .044 C4.624 1.424 6.152 .5 7.906 .5 c1.58 0 2.977 .75 3.827 1.9 H10.25 a.25 .25 0 1 0 0 .5 h3.364 c1.02 .341 1.772 1.23 1.874 2.3 H10.25 a.25 .25 0 1 0 0 .5 h5.238 c-.131 1.374 -1.335 2.45 -2.8 2.45 a2.9 2.9 0 0 1 -1.194 -.254Z M8.25 3.8 a.25 .25 0 0 0 0 .5 h5.5 a.25 .25 0 1 0 0 -.5 h-5.5Z m-1 2.8 a.25 .25 0 0 0 0 .5 h5.5 a.25 .25 0 1 0 0 -.5 h-5.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0.777 11.644
                moveTo(x = 0.777f, y = 11.644f)
                // a 0.5 0.5 0 1 0 -0.554 0.832
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.554f,
                    dy1 = 0.832f,
                )
                // l 1.017 0.678
                lineToRelative(dx = 1.017f, dy = 0.678f)
                // a 1.5 1.5 0 0 0 1.503 0.094
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.503f,
                    dy1 = 0.094f,
                )
                // l 1.033 -0.517
                lineToRelative(dx = 1.033f, dy = -0.517f)
                // a 0.5 0.5 0 0 1 0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.448f,
                    dy1 = 0.0f,
                )
                // l 1.105 0.553
                lineToRelative(dx = 1.105f, dy = 0.553f)
                // a 1.5 1.5 0 0 0 1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.342f,
                    dy1 = 0.0f,
                )
                // l 1.105 -0.553
                lineToRelative(dx = 1.105f, dy = -0.553f)
                // a 0.5 0.5 0 0 1 0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.448f,
                    dy1 = 0.0f,
                )
                // l 1.105 0.553
                lineToRelative(dx = 1.105f, dy = 0.553f)
                // a 1.5 1.5 0 0 0 1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.342f,
                    dy1 = 0.0f,
                )
                // l 1.105 -0.553
                lineToRelative(dx = 1.105f, dy = -0.553f)
                // a 0.5 0.5 0 0 1 0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.448f,
                    dy1 = 0.0f,
                )
                // l 1.033 0.517
                lineToRelative(dx = 1.033f, dy = 0.517f)
                // a 1.5 1.5 0 0 0 1.503 -0.094
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.503f,
                    dy1 = -0.094f,
                )
                // l 1.017 -0.678
                lineToRelative(dx = 1.017f, dy = -0.678f)
                // a 0.5 0.5 0 1 0 -0.554 -0.832
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.554f,
                    dy1 = -0.832f,
                )
                // l -1.017 0.678
                lineToRelative(dx = -1.017f, dy = 0.678f)
                // a 0.5 0.5 0 0 1 -0.501 0.031
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.501f,
                    dy1 = 0.031f,
                )
                // l -1.034 -0.517
                lineToRelative(dx = -1.034f, dy = -0.517f)
                // a 1.5 1.5 0 0 0 -1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.342f,
                    dy1 = 0.0f,
                )
                // l -1.105 0.553
                lineToRelative(dx = -1.105f, dy = 0.553f)
                // a 0.5 0.5 0 0 1 -0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.448f,
                    dy1 = 0.0f,
                )
                // l -1.105 -0.553
                lineToRelative(dx = -1.105f, dy = -0.553f)
                // a 1.5 1.5 0 0 0 -1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.342f,
                    dy1 = 0.0f,
                )
                // l -1.105 0.553
                lineToRelative(dx = -1.105f, dy = 0.553f)
                // a 0.5 0.5 0 0 1 -0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.448f,
                    dy1 = 0.0f,
                )
                // l -1.105 -0.553
                lineToRelative(dx = -1.105f, dy = -0.553f)
                // a 1.5 1.5 0 0 0 -1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.342f,
                    dy1 = 0.0f,
                )
                // l -1.034 0.517
                lineToRelative(dx = -1.034f, dy = 0.517f)
                // a 0.5 0.5 0 0 1 -0.5 -0.03
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.03f,
                )
                // l -1.018 -0.679z
                lineToRelative(dx = -1.018f, dy = -0.679f)
                close()
                // m 0 2.5
                moveToRelative(dx = 0.0f, dy = 2.5f)
                // a 0.5 0.5 0 1 0 -0.554 0.832
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.554f,
                    dy1 = 0.832f,
                )
                // l 1.017 0.678
                lineToRelative(dx = 1.017f, dy = 0.678f)
                // a 1.5 1.5 0 0 0 1.503 0.094
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.503f,
                    dy1 = 0.094f,
                )
                // l 1.033 -0.517
                lineToRelative(dx = 1.033f, dy = -0.517f)
                // a 0.5 0.5 0 0 1 0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.448f,
                    dy1 = 0.0f,
                )
                // l 1.105 0.553
                lineToRelative(dx = 1.105f, dy = 0.553f)
                // a 1.5 1.5 0 0 0 1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.342f,
                    dy1 = 0.0f,
                )
                // l 1.105 -0.553
                lineToRelative(dx = 1.105f, dy = -0.553f)
                // a 0.5 0.5 0 0 1 0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.448f,
                    dy1 = 0.0f,
                )
                // l 1.105 0.553
                lineToRelative(dx = 1.105f, dy = 0.553f)
                // a 1.5 1.5 0 0 0 1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.342f,
                    dy1 = 0.0f,
                )
                // l 1.105 -0.553
                lineToRelative(dx = 1.105f, dy = -0.553f)
                // a 0.5 0.5 0 0 1 0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.448f,
                    dy1 = 0.0f,
                )
                // l 1.033 0.517
                lineToRelative(dx = 1.033f, dy = 0.517f)
                // a 1.5 1.5 0 0 0 1.503 -0.094
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.503f,
                    dy1 = -0.094f,
                )
                // l 1.017 -0.678
                lineToRelative(dx = 1.017f, dy = -0.678f)
                // a 0.5 0.5 0 1 0 -0.554 -0.832
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.554f,
                    dy1 = -0.832f,
                )
                // l -1.017 0.678
                lineToRelative(dx = -1.017f, dy = 0.678f)
                // a 0.5 0.5 0 0 1 -0.501 0.031
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.501f,
                    dy1 = 0.031f,
                )
                // l -1.034 -0.517
                lineToRelative(dx = -1.034f, dy = -0.517f)
                // a 1.5 1.5 0 0 0 -1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.342f,
                    dy1 = 0.0f,
                )
                // l -1.105 0.553
                lineToRelative(dx = -1.105f, dy = 0.553f)
                // a 0.5 0.5 0 0 1 -0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.448f,
                    dy1 = 0.0f,
                )
                // l -1.105 -0.553
                lineToRelative(dx = -1.105f, dy = -0.553f)
                // a 1.5 1.5 0 0 0 -1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.342f,
                    dy1 = 0.0f,
                )
                // l -1.105 0.553
                lineToRelative(dx = -1.105f, dy = 0.553f)
                // a 0.5 0.5 0 0 1 -0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.448f,
                    dy1 = 0.0f,
                )
                // l -1.105 -0.553
                lineToRelative(dx = -1.105f, dy = -0.553f)
                // a 1.5 1.5 0 0 0 -1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.342f,
                    dy1 = 0.0f,
                )
                // l -1.034 0.517
                lineToRelative(dx = -1.034f, dy = 0.517f)
                // a 0.5 0.5 0 0 1 -0.5 -0.03
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.03f,
                )
                // l -1.018 -0.679z
                lineToRelative(dx = -1.018f, dy = -0.679f)
                close()
                // m 10.717 -6.248
                moveToRelative(dx = 10.717f, dy = -6.248f)
                // A 4.758 4.758 0 0 1 7.906 9.5
                arcTo(
                    horizontalEllipseRadius = 4.758f,
                    verticalEllipseRadius = 4.758f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.906f,
                    y1 = 9.5f,
                )
                // A 4.76 4.76 0 0 1 4.37 7.953
                arcTo(
                    horizontalEllipseRadius = 4.76f,
                    verticalEllipseRadius = 4.76f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 4.37f,
                    y1 = 7.953f,
                )
                // a 2.907 2.907 0 0 1 -1.056 0.197
                arcToRelative(
                    a = 2.907f,
                    b = 2.907f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.056f,
                    dy1 = 0.197f,
                )
                // C 1.758 8.15 0.5 6.941 0.5 5.45
                curveTo(
                    x1 = 1.758f,
                    y1 = 8.15f,
                    x2 = 0.5f,
                    y2 = 6.941f,
                    x3 = 0.5f,
                    y3 = 5.45f,
                )
                // s 1.26 -2.7 2.813 -2.7
                reflectiveCurveToRelative(
                    dx1 = 1.26f,
                    dy1 = -2.7f,
                    dx2 = 2.813f,
                    dy2 = -2.7f,
                )
                // c 0.173 0 0.342 0.015 0.507 0.044
                curveToRelative(
                    dx1 = 0.173f,
                    dy1 = 0.0f,
                    dx2 = 0.342f,
                    dy2 = 0.015f,
                    dx3 = 0.507f,
                    dy3 = 0.044f,
                )
                // C 4.624 1.424 6.152 0.5 7.906 0.5
                curveTo(
                    x1 = 4.624f,
                    y1 = 1.424f,
                    x2 = 6.152f,
                    y2 = 0.5f,
                    x3 = 7.906f,
                    y3 = 0.5f,
                )
                // c 1.58 0 2.977 0.75 3.827 1.9
                curveToRelative(
                    dx1 = 1.58f,
                    dy1 = 0.0f,
                    dx2 = 2.977f,
                    dy2 = 0.75f,
                    dx3 = 3.827f,
                    dy3 = 1.9f,
                )
                // H 10.25
                horizontalLineTo(x = 10.25f)
                // a 0.25 0.25 0 1 0 0 0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                // h 3.364
                horizontalLineToRelative(dx = 3.364f)
                // c 1.02 0.341 1.772 1.23 1.874 2.3
                curveToRelative(
                    dx1 = 1.02f,
                    dy1 = 0.341f,
                    dx2 = 1.772f,
                    dy2 = 1.23f,
                    dx3 = 1.874f,
                    dy3 = 2.3f,
                )
                // H 10.25
                horizontalLineTo(x = 10.25f)
                // a 0.25 0.25 0 1 0 0 0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                // h 5.238
                horizontalLineToRelative(dx = 5.238f)
                // c -0.131 1.374 -1.335 2.45 -2.8 2.45
                curveToRelative(
                    dx1 = -0.131f,
                    dy1 = 1.374f,
                    dx2 = -1.335f,
                    dy2 = 2.45f,
                    dx3 = -2.8f,
                    dy3 = 2.45f,
                )
                // a 2.9 2.9 0 0 1 -1.194 -0.254z
                arcToRelative(
                    a = 2.9f,
                    b = 2.9f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.194f,
                    dy1 = -0.254f,
                )
                close()
                // M 8.25 3.8
                moveTo(x = 8.25f, y = 3.8f)
                // a 0.25 0.25 0 0 0 0 0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                // h 5.5
                horizontalLineToRelative(dx = 5.5f)
                // a 0.25 0.25 0 1 0 0 -0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                )
                // h -5.5z
                horizontalLineToRelative(dx = -5.5f)
                close()
                // m -1 2.8
                moveToRelative(dx = -1.0f, dy = 2.8f)
                // a 0.25 0.25 0 0 0 0 0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                // h 5.5
                horizontalLineToRelative(dx = 5.5f)
                // a 0.25 0.25 0 1 0 0 -0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                )
                // h -5.5z
                horizontalLineToRelative(dx = -5.5f)
                close()
            }
        }.build().also { _ic1065 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1065: ImageVector? = null
