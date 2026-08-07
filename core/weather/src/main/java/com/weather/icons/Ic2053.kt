package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2053: ImageVector
    get() {
        val current = _ic2053
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2053",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // m1 10.5 -1 2 V16 h16 v-2 l-2 -2 -1 1 -8 -7 -3 7 -1 -2.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 1 10.5
                moveTo(x = 1.0f, y = 10.5f)
                // l -1 2
                lineToRelative(dx = -1.0f, dy = 2.0f)
                // V 16
                verticalLineTo(y = 16.0f)
                // h 16
                horizontalLineToRelative(dx = 16.0f)
                // v -2
                verticalLineToRelative(dy = -2.0f)
                // l -2 -2
                lineToRelative(dx = -2.0f, dy = -2.0f)
                // l -1 1
                lineToRelative(dx = -1.0f, dy = 1.0f)
                // l -8 -7
                lineToRelative(dx = -8.0f, dy = -7.0f)
                // l -3 7
                lineToRelative(dx = -3.0f, dy = 7.0f)
                // l -1 -2.5z
                lineToRelative(dx = -1.0f, dy = -2.5f)
                close()
            }
            // m2.883 6.325 .122 1.672 -.71 -.292 c-.251 .454 -.57 .876 -.995 1.195 -.298 .224 -.772 .488 -1.3 .488 v-1 c.214 0 .475 -.119 .7 -.288 .257 -.192 .473 -.456 .663 -.78 l-.76 -.313 1.41 -1.228 .156 -.48 c.055 -.172 .111 -.345 .17 -.52 .217 -.654 .473 -1.336 .863 -1.854 C3.614 2.38 4.19 2 5 2 c1.166 0 2.127 .512 2.937 1.237 .422 .377 .812 .82 1.177 1.29 l.442 -.367 .342 1.46 .153 .228 c.174 .262 .342 .522 .508 .777 .444 .681 .865 1.33 1.307 1.895 .464 .593 .91 1.039 1.363 1.307 l.156 -.686 1.464 1.2 c.456 .086 .832 .11 1.137 .102 l.028 1 a6.602 6.602 0 0 1 -1.581 -.166 l-1.624 .397 .19 -.838 c-.742 -.35 -1.367 -.991 -1.92 -1.7 -.472 -.602 -.93 -1.306 -1.38 -2 l-.343 -.525 -1.798 -.79 .787 -.654 A8.757 8.757 0 0 0 7.27 3.982 C6.579 3.363 5.846 3 5 3 c-.44 0 -.74 .183 -1 .528 -.28 .372 -.494 .91 -.713 1.568 l-.154 .472 c-.08 .247 -.161 .503 -.25 .757Z M13.642 .394 a.164 .164 0 0 0 -.284 0 l-2.336 4.05 a.162 .162 0 0 0 .142 .244 h4.672 a.162 .162 0 0 0 .142 -.243 L13.642 .395Z m-.531 1.34 c-.02 -.176 .16 -.328 .389 -.328 .23 0 .41 .152 .39 .328 l-.177 1.547 h-.426 l-.176 -1.547Z m.704 2.172 a.312 .312 0 1 1 -.625 0 .312 .312 0 0 1 .625 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 2.883 6.325
                moveTo(x = 2.883f, y = 6.325f)
                // l 0.122 1.672
                lineToRelative(dx = 0.122f, dy = 1.672f)
                // l -0.71 -0.292
                lineToRelative(dx = -0.71f, dy = -0.292f)
                // c -0.251 0.454 -0.57 0.876 -0.995 1.195
                curveToRelative(
                    dx1 = -0.251f,
                    dy1 = 0.454f,
                    dx2 = -0.57f,
                    dy2 = 0.876f,
                    dx3 = -0.995f,
                    dy3 = 1.195f,
                )
                // c -0.298 0.224 -0.772 0.488 -1.3 0.488
                curveToRelative(
                    dx1 = -0.298f,
                    dy1 = 0.224f,
                    dx2 = -0.772f,
                    dy2 = 0.488f,
                    dx3 = -1.3f,
                    dy3 = 0.488f,
                )
                // v -1
                verticalLineToRelative(dy = -1.0f)
                // c 0.214 0 0.475 -0.119 0.7 -0.288
                curveToRelative(
                    dx1 = 0.214f,
                    dy1 = 0.0f,
                    dx2 = 0.475f,
                    dy2 = -0.119f,
                    dx3 = 0.7f,
                    dy3 = -0.288f,
                )
                // c 0.257 -0.192 0.473 -0.456 0.663 -0.78
                curveToRelative(
                    dx1 = 0.257f,
                    dy1 = -0.192f,
                    dx2 = 0.473f,
                    dy2 = -0.456f,
                    dx3 = 0.663f,
                    dy3 = -0.78f,
                )
                // l -0.76 -0.313
                lineToRelative(dx = -0.76f, dy = -0.313f)
                // l 1.41 -1.228
                lineToRelative(dx = 1.41f, dy = -1.228f)
                // l 0.156 -0.48
                lineToRelative(dx = 0.156f, dy = -0.48f)
                // c 0.055 -0.172 0.111 -0.345 0.17 -0.52
                curveToRelative(
                    dx1 = 0.055f,
                    dy1 = -0.172f,
                    dx2 = 0.111f,
                    dy2 = -0.345f,
                    dx3 = 0.17f,
                    dy3 = -0.52f,
                )
                // c 0.217 -0.654 0.473 -1.336 0.863 -1.854
                curveToRelative(
                    dx1 = 0.217f,
                    dy1 = -0.654f,
                    dx2 = 0.473f,
                    dy2 = -1.336f,
                    dx3 = 0.863f,
                    dy3 = -1.854f,
                )
                // C 3.614 2.38 4.19 2 5 2
                curveTo(
                    x1 = 3.614f,
                    y1 = 2.38f,
                    x2 = 4.19f,
                    y2 = 2.0f,
                    x3 = 5.0f,
                    y3 = 2.0f,
                )
                // c 1.166 0 2.127 0.512 2.937 1.237
                curveToRelative(
                    dx1 = 1.166f,
                    dy1 = 0.0f,
                    dx2 = 2.127f,
                    dy2 = 0.512f,
                    dx3 = 2.937f,
                    dy3 = 1.237f,
                )
                // c 0.422 0.377 0.812 0.82 1.177 1.29
                curveToRelative(
                    dx1 = 0.422f,
                    dy1 = 0.377f,
                    dx2 = 0.812f,
                    dy2 = 0.82f,
                    dx3 = 1.177f,
                    dy3 = 1.29f,
                )
                // l 0.442 -0.367
                lineToRelative(dx = 0.442f, dy = -0.367f)
                // l 0.342 1.46
                lineToRelative(dx = 0.342f, dy = 1.46f)
                // l 0.153 0.228
                lineToRelative(dx = 0.153f, dy = 0.228f)
                // c 0.174 0.262 0.342 0.522 0.508 0.777
                curveToRelative(
                    dx1 = 0.174f,
                    dy1 = 0.262f,
                    dx2 = 0.342f,
                    dy2 = 0.522f,
                    dx3 = 0.508f,
                    dy3 = 0.777f,
                )
                // c 0.444 0.681 0.865 1.33 1.307 1.895
                curveToRelative(
                    dx1 = 0.444f,
                    dy1 = 0.681f,
                    dx2 = 0.865f,
                    dy2 = 1.33f,
                    dx3 = 1.307f,
                    dy3 = 1.895f,
                )
                // c 0.464 0.593 0.91 1.039 1.363 1.307
                curveToRelative(
                    dx1 = 0.464f,
                    dy1 = 0.593f,
                    dx2 = 0.91f,
                    dy2 = 1.039f,
                    dx3 = 1.363f,
                    dy3 = 1.307f,
                )
                // l 0.156 -0.686
                lineToRelative(dx = 0.156f, dy = -0.686f)
                // l 1.464 1.2
                lineToRelative(dx = 1.464f, dy = 1.2f)
                // c 0.456 0.086 0.832 0.11 1.137 0.102
                curveToRelative(
                    dx1 = 0.456f,
                    dy1 = 0.086f,
                    dx2 = 0.832f,
                    dy2 = 0.11f,
                    dx3 = 1.137f,
                    dy3 = 0.102f,
                )
                // l 0.028 1
                lineToRelative(dx = 0.028f, dy = 1.0f)
                // a 6.602 6.602 0 0 1 -1.581 -0.166
                arcToRelative(
                    a = 6.602f,
                    b = 6.602f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.581f,
                    dy1 = -0.166f,
                )
                // l -1.624 0.397
                lineToRelative(dx = -1.624f, dy = 0.397f)
                // l 0.19 -0.838
                lineToRelative(dx = 0.19f, dy = -0.838f)
                // c -0.742 -0.35 -1.367 -0.991 -1.92 -1.7
                curveToRelative(
                    dx1 = -0.742f,
                    dy1 = -0.35f,
                    dx2 = -1.367f,
                    dy2 = -0.991f,
                    dx3 = -1.92f,
                    dy3 = -1.7f,
                )
                // c -0.472 -0.602 -0.93 -1.306 -1.38 -2
                curveToRelative(
                    dx1 = -0.472f,
                    dy1 = -0.602f,
                    dx2 = -0.93f,
                    dy2 = -1.306f,
                    dx3 = -1.38f,
                    dy3 = -2.0f,
                )
                // l -0.343 -0.525
                lineToRelative(dx = -0.343f, dy = -0.525f)
                // l -1.798 -0.79
                lineToRelative(dx = -1.798f, dy = -0.79f)
                // l 0.787 -0.654
                lineToRelative(dx = 0.787f, dy = -0.654f)
                // A 8.757 8.757 0 0 0 7.27 3.982
                arcTo(
                    horizontalEllipseRadius = 8.757f,
                    verticalEllipseRadius = 8.757f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.27f,
                    y1 = 3.982f,
                )
                // C 6.579 3.363 5.846 3 5 3
                curveTo(
                    x1 = 6.579f,
                    y1 = 3.363f,
                    x2 = 5.846f,
                    y2 = 3.0f,
                    x3 = 5.0f,
                    y3 = 3.0f,
                )
                // c -0.44 0 -0.74 0.183 -1 0.528
                curveToRelative(
                    dx1 = -0.44f,
                    dy1 = 0.0f,
                    dx2 = -0.74f,
                    dy2 = 0.183f,
                    dx3 = -1.0f,
                    dy3 = 0.528f,
                )
                // c -0.28 0.372 -0.494 0.91 -0.713 1.568
                curveToRelative(
                    dx1 = -0.28f,
                    dy1 = 0.372f,
                    dx2 = -0.494f,
                    dy2 = 0.91f,
                    dx3 = -0.713f,
                    dy3 = 1.568f,
                )
                // l -0.154 0.472
                lineToRelative(dx = -0.154f, dy = 0.472f)
                // c -0.08 0.247 -0.161 0.503 -0.25 0.757z
                curveToRelative(
                    dx1 = -0.08f,
                    dy1 = 0.247f,
                    dx2 = -0.161f,
                    dy2 = 0.503f,
                    dx3 = -0.25f,
                    dy3 = 0.757f,
                )
                close()
                // M 13.642 0.394
                moveTo(x = 13.642f, y = 0.394f)
                // a 0.164 0.164 0 0 0 -0.284 0
                arcToRelative(
                    a = 0.164f,
                    b = 0.164f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.284f,
                    dy1 = 0.0f,
                )
                // l -2.336 4.05
                lineToRelative(dx = -2.336f, dy = 4.05f)
                // a 0.162 0.162 0 0 0 0.142 0.244
                arcToRelative(
                    a = 0.162f,
                    b = 0.162f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.142f,
                    dy1 = 0.244f,
                )
                // h 4.672
                horizontalLineToRelative(dx = 4.672f)
                // a 0.162 0.162 0 0 0 0.142 -0.243
                arcToRelative(
                    a = 0.162f,
                    b = 0.162f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.142f,
                    dy1 = -0.243f,
                )
                // L 13.642 0.395z
                lineTo(x = 13.642f, y = 0.395f)
                close()
                // m -0.531 1.34
                moveToRelative(dx = -0.531f, dy = 1.34f)
                // c -0.02 -0.176 0.16 -0.328 0.389 -0.328
                curveToRelative(
                    dx1 = -0.02f,
                    dy1 = -0.176f,
                    dx2 = 0.16f,
                    dy2 = -0.328f,
                    dx3 = 0.389f,
                    dy3 = -0.328f,
                )
                // c 0.23 0 0.41 0.152 0.39 0.328
                curveToRelative(
                    dx1 = 0.23f,
                    dy1 = 0.0f,
                    dx2 = 0.41f,
                    dy2 = 0.152f,
                    dx3 = 0.39f,
                    dy3 = 0.328f,
                )
                // l -0.177 1.547
                lineToRelative(dx = -0.177f, dy = 1.547f)
                // h -0.426
                horizontalLineToRelative(dx = -0.426f)
                // l -0.176 -1.547z
                lineToRelative(dx = -0.176f, dy = -1.547f)
                close()
                // m 0.704 2.172
                moveToRelative(dx = 0.704f, dy = 2.172f)
                // a 0.312 0.312 0 1 1 -0.625 0
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.625f,
                    dy1 = 0.0f,
                )
                // a 0.312 0.312 0 0 1 0.625 0z
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.625f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic2053 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2053: ImageVector? = null
