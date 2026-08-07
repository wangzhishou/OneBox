package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2418: ImageVector
    get() {
        val current = _ic2418
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2418",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.327 3.055 c.047 -.316 .43 -.495 .83 -.389 .398 .107 .64 .454 .523 .751 L8.653 6.026 l-.74 -.199 .414 -2.772Z M8.54 7.16 a.563 .563 0 1 1 -1.087 -.291 .563 .563 0 0 1 1.086 .29Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.327 3.055
                moveTo(x = 8.327f, y = 3.055f)
                // c 0.047 -0.316 0.43 -0.495 0.83 -0.389
                curveToRelative(
                    dx1 = 0.047f,
                    dy1 = -0.316f,
                    dx2 = 0.43f,
                    dy2 = -0.495f,
                    dx3 = 0.83f,
                    dy3 = -0.389f,
                )
                // c 0.398 0.107 0.64 0.454 0.523 0.751
                curveToRelative(
                    dx1 = 0.398f,
                    dy1 = 0.107f,
                    dx2 = 0.64f,
                    dy2 = 0.454f,
                    dx3 = 0.523f,
                    dy3 = 0.751f,
                )
                // L 8.653 6.026
                lineTo(x = 8.653f, y = 6.026f)
                // l -0.74 -0.199
                lineToRelative(dx = -0.74f, dy = -0.199f)
                // l 0.414 -2.772z
                lineToRelative(dx = 0.414f, dy = -2.772f)
                close()
                // M 8.54 7.16
                moveTo(x = 8.54f, y = 7.16f)
                // a 0.563 0.563 0 1 1 -1.087 -0.291
                arcToRelative(
                    a = 0.563f,
                    b = 0.563f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.087f,
                    dy1 = -0.291f,
                )
                // a 0.563 0.563 0 0 1 1.086 0.29z
                arcToRelative(
                    a = 0.563f,
                    b = 0.563f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.086f,
                    dy1 = 0.29f,
                )
                close()
            }
            // M4.155 3.947 a4.501 4.501 0 0 0 2.473 5.257 l-.971 3.625 .12 .06 a.5 .5 0 0 0 .447 0 l1.105 -.553 .015 -.007 .733 -2.737 a4.501 4.501 0 1 0 -3.922 -5.645Z m7.987 2.14 A3.769 3.769 0 1 1 4.86 4.136 a3.769 3.769 0 0 1 7.28 1.95Z M.084 14.283 a.5 .5 0 0 1 .693 -.139 l1.018 .678 a.5 .5 0 0 0 .5 .031 l1.034 -.517 a1.5 1.5 0 0 1 1.342 0 l1.105 .553 a.5 .5 0 0 0 .448 0 l1.105 -.553 a1.5 1.5 0 0 1 1.342 0 l1.105 .553 a.5 .5 0 0 0 .448 0 l1.105 -.553 a1.5 1.5 0 0 1 1.342 0 l1.034 .517 a.5 .5 0 0 0 .5 -.03 l1.018 -.679 a.5 .5 0 1 1 .554 .832 l-1.017 .678 a1.5 1.5 0 0 1 -1.503 .094 l-1.033 -.517 a.5 .5 0 0 0 -.448 0 l-1.105 .553 a1.5 1.5 0 0 1 -1.342 0 l-1.105 -.553 a.5 .5 0 0 0 -.448 0 l-1.105 .553 a1.5 1.5 0 0 1 -1.342 0 l-1.105 -.553 a.5 .5 0 0 0 -.448 0 l-1.033 .517 a1.5 1.5 0 0 1 -1.503 -.094 l-1.017 -.678 a.5 .5 0 0 1 -.139 -.693Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.155 3.947
                moveTo(x = 4.155f, y = 3.947f)
                // a 4.501 4.501 0 0 0 2.473 5.257
                arcToRelative(
                    a = 4.501f,
                    b = 4.501f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.473f,
                    dy1 = 5.257f,
                )
                // l -0.971 3.625
                lineToRelative(dx = -0.971f, dy = 3.625f)
                // l 0.12 0.06
                lineToRelative(dx = 0.12f, dy = 0.06f)
                // a 0.5 0.5 0 0 0 0.447 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.447f,
                    dy1 = 0.0f,
                )
                // l 1.105 -0.553
                lineToRelative(dx = 1.105f, dy = -0.553f)
                // l 0.015 -0.007
                lineToRelative(dx = 0.015f, dy = -0.007f)
                // l 0.733 -2.737
                lineToRelative(dx = 0.733f, dy = -2.737f)
                // a 4.501 4.501 0 1 0 -3.922 -5.645z
                arcToRelative(
                    a = 4.501f,
                    b = 4.501f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -3.922f,
                    dy1 = -5.645f,
                )
                close()
                // m 7.987 2.14
                moveToRelative(dx = 7.987f, dy = 2.14f)
                // A 3.769 3.769 0 1 1 4.86 4.136
                arcTo(
                    horizontalEllipseRadius = 3.769f,
                    verticalEllipseRadius = 3.769f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 4.86f,
                    y1 = 4.136f,
                )
                // a 3.769 3.769 0 0 1 7.28 1.95z
                arcToRelative(
                    a = 3.769f,
                    b = 3.769f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 7.28f,
                    dy1 = 1.95f,
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
        }.build().also { _ic2418 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2418: ImageVector? = null
