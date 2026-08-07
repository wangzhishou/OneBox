package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2400: ImageVector
    get() {
        val current = _ic2400
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2400",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M.065 13.314 a.5 .5 0 0 1 .713 -.17 l1.017 .678 a.5 .5 0 0 0 .5 .031 l1.034 -.517 a1.5 1.5 0 0 1 1.342 0 l1.105 .553 a.5 .5 0 0 0 .448 0 l1.105 -.553 c.114 -.056 .233 -.098 .354 -.124 C6.566 11.845 5.75 8.158 7 6.5 c1.96 -2.599 7 -.5 7 -.5 s-3.553 -4.997 -8 -5 C1.358 .997 .3 10.194 .065 13.314Z m.019 .969 a.5 .5 0 0 1 .693 -.139 l1.018 .678 a.5 .5 0 0 0 .5 .031 l1.034 -.517 a1.5 1.5 0 0 1 1.342 0 l1.105 .553 a.5 .5 0 0 0 .448 0 l1.105 -.553 a1.5 1.5 0 0 1 1.342 0 l1.105 .553 a.5 .5 0 0 0 .448 0 l1.105 -.553 a1.5 1.5 0 0 1 1.342 0 l1.034 .517 a.5 .5 0 0 0 .5 -.03 l1.018 -.679 a.5 .5 0 1 1 .554 .832 l-1.017 .678 a1.5 1.5 0 0 1 -1.503 .094 l-1.033 -.517 a.5 .5 0 0 0 -.448 0 l-1.105 .553 a1.5 1.5 0 0 1 -1.342 0 l-1.105 -.553 a.5 .5 0 0 0 -.448 0 l-1.105 .553 a1.5 1.5 0 0 1 -1.342 0 l-1.105 -.553 a.5 .5 0 0 0 -.448 0 l-1.033 .517 a1.5 1.5 0 0 1 -1.503 -.094 l-1.017 -.678 a.5 .5 0 0 1 -.139 -.693Z m11.543 -5.034 c-.02 -.201 .153 -.374 .373 -.374 s.393 .173 .373 .374 l-.168 1.763 h-.41 l-.168 -1.763Z m.654 2.345 a.281 .281 0 1 1 -.562 0 .281 .281 0 0 1 .562 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0.065 13.314
                moveTo(x = 0.065f, y = 13.314f)
                // a 0.5 0.5 0 0 1 0.713 -0.17
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.713f,
                    dy1 = -0.17f,
                )
                // l 1.017 0.678
                lineToRelative(dx = 1.017f, dy = 0.678f)
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
                // c 0.114 -0.056 0.233 -0.098 0.354 -0.124
                curveToRelative(
                    dx1 = 0.114f,
                    dy1 = -0.056f,
                    dx2 = 0.233f,
                    dy2 = -0.098f,
                    dx3 = 0.354f,
                    dy3 = -0.124f,
                )
                // C 6.566 11.845 5.75 8.158 7 6.5
                curveTo(
                    x1 = 6.566f,
                    y1 = 11.845f,
                    x2 = 5.75f,
                    y2 = 8.158f,
                    x3 = 7.0f,
                    y3 = 6.5f,
                )
                // c 1.96 -2.599 7 -0.5 7 -0.5
                curveToRelative(
                    dx1 = 1.96f,
                    dy1 = -2.599f,
                    dx2 = 7.0f,
                    dy2 = -0.5f,
                    dx3 = 7.0f,
                    dy3 = -0.5f,
                )
                // s -3.553 -4.997 -8 -5
                reflectiveCurveToRelative(
                    dx1 = -3.553f,
                    dy1 = -4.997f,
                    dx2 = -8.0f,
                    dy2 = -5.0f,
                )
                // C 1.358 0.997 0.3 10.194 0.065 13.314z
                curveTo(
                    x1 = 1.358f,
                    y1 = 0.997f,
                    x2 = 0.3f,
                    y2 = 10.194f,
                    x3 = 0.065f,
                    y3 = 13.314f,
                )
                close()
                // m 0.019 0.969
                moveToRelative(dx = 0.019f, dy = 0.969f)
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
                // m 11.543 -5.034
                moveToRelative(dx = 11.543f, dy = -5.034f)
                // c -0.02 -0.201 0.153 -0.374 0.373 -0.374
                curveToRelative(
                    dx1 = -0.02f,
                    dy1 = -0.201f,
                    dx2 = 0.153f,
                    dy2 = -0.374f,
                    dx3 = 0.373f,
                    dy3 = -0.374f,
                )
                // s 0.393 0.173 0.373 0.374
                reflectiveCurveToRelative(
                    dx1 = 0.393f,
                    dy1 = 0.173f,
                    dx2 = 0.373f,
                    dy2 = 0.374f,
                )
                // l -0.168 1.763
                lineToRelative(dx = -0.168f, dy = 1.763f)
                // h -0.41
                horizontalLineToRelative(dx = -0.41f)
                // l -0.168 -1.763z
                lineToRelative(dx = -0.168f, dy = -1.763f)
                close()
                // m 0.654 2.345
                moveToRelative(dx = 0.654f, dy = 2.345f)
                // a 0.281 0.281 0 1 1 -0.562 0
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.562f,
                    dy1 = 0.0f,
                )
                // a 0.281 0.281 0 0 1 0.562 0z
                arcToRelative(
                    a = 0.281f,
                    b = 0.281f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.562f,
                    dy1 = 0.0f,
                )
                close()
            }
            // M11.83 7.473 a.197 .197 0 0 1 .34 0 l2.804 4.86 a.195 .195 0 0 1 -.17 .292 H9.196 a.195 .195 0 0 1 -.17 -.291 l2.803 -4.861Z m2.533 4.702 L12 8.078 l-2.363 4.097 h4.726Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.83 7.473
                moveTo(x = 11.83f, y = 7.473f)
                // a 0.197 0.197 0 0 1 0.34 0
                arcToRelative(
                    a = 0.197f,
                    b = 0.197f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.34f,
                    dy1 = 0.0f,
                )
                // l 2.804 4.86
                lineToRelative(dx = 2.804f, dy = 4.86f)
                // a 0.195 0.195 0 0 1 -0.17 0.292
                arcToRelative(
                    a = 0.195f,
                    b = 0.195f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.17f,
                    dy1 = 0.292f,
                )
                // H 9.196
                horizontalLineTo(x = 9.196f)
                // a 0.195 0.195 0 0 1 -0.17 -0.291
                arcToRelative(
                    a = 0.195f,
                    b = 0.195f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.17f,
                    dy1 = -0.291f,
                )
                // l 2.803 -4.861z
                lineToRelative(dx = 2.803f, dy = -4.861f)
                close()
                // m 2.533 4.702
                moveToRelative(dx = 2.533f, dy = 4.702f)
                // L 12 8.078
                lineTo(x = 12.0f, y = 8.078f)
                // l -2.363 4.097
                lineToRelative(dx = -2.363f, dy = 4.097f)
                // h 4.726z
                horizontalLineToRelative(dx = 4.726f)
                close()
            }
        }.build().also { _ic2400 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2400: ImageVector? = null
