package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1048: ImageVector
    get() {
        val current = _ic1048
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1048",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M5.5 4.2 a.7 .7 0 0 0 .702 -.702 .7 .7 0 1 0 -1.403 0 .7 .7 0 0 0 .345 .61 .698 .698 0 0 0 .356 .093Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 5.5 4.2
                moveTo(x = 5.5f, y = 4.2f)
                // a 0.7 0.7 0 0 0 0.702 -0.702
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.702f,
                    dy1 = -0.702f,
                )
                // a 0.7 0.7 0 1 0 -1.403 0
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -1.403f,
                    dy1 = 0.0f,
                )
                // a 0.7 0.7 0 0 0 0.345 0.61
                arcToRelative(
                    a = 0.7f,
                    b = 0.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.345f,
                    dy1 = 0.61f,
                )
                // a 0.698 0.698 0 0 0 0.356 0.093z
                arcToRelative(
                    a = 0.698f,
                    b = 0.698f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.356f,
                    dy1 = 0.093f,
                )
                close()
            }
            // M3.5 0 a.5 .5 0 0 0 -.5 .5 V10 H.593 c-.595 0 -.818 .804 -.314 1.129 l7.407 4.778 a.576 .576 0 0 0 .628 0 l7.407 -4.778 c.504 -.325 .28 -1.129 -.314 -1.129 H13 V.5 a.5 .5 0 0 0 -.5 -.5 h-9Z m2 5 c-.277 0 -.528 -.067 -.755 -.2 a1.542 1.542 0 0 1 -.542 -.543 A1.465 1.465 0 0 1 4 3.498 a1.51 1.51 0 0 1 .745 -1.296 C4.972 2.069 5.223 2 5.5 2 s.528 .068 .755 .203 A1.509 1.509 0 0 1 7 3.498 c0 .277 -.068 .53 -.203 .759 a1.542 1.542 0 0 1 -.542 .543 c-.227 .133 -.478 .2 -.755 .2Z m5.464 .05 a1.354 1.354 0 0 0 -.163 -.464 1.223 1.223 0 0 0 -.7 -.562 1.536 1.536 0 0 0 -.486 -.074 c-.308 0 -.582 .08 -.82 .24 a1.559 1.559 0 0 0 -.56 .695 c-.134 .304 -.202 .676 -.202 1.115 0 .447 .068 .823 .202 1.13 .137 .303 .324 .534 .56 .69 .238 .153 .51 .23 .817 .23 .17 0 .329 -.022 .477 -.068 a1.29 1.29 0 0 0 .401 -.208 1.293 1.293 0 0 0 .474 -.793 L12 6.987 a2.343 2.343 0 0 1 -.754 1.426 2.297 2.297 0 0 1 -.725 .433 A2.637 2.637 0 0 1 9.598 9 a2.53 2.53 0 0 1 -1.335 -.356 2.464 2.464 0 0 1 -.925 -1.03 C7.113 7.165 7 6.627 7 6 c0 -.63 .114 -1.167 .34 -1.614 a2.48 2.48 0 0 1 .929 -1.03 A2.511 2.511 0 0 1 9.599 3 c.315 0 .609 .046 .88 .137 .272 .091 .514 .225 .726 .402 .212 .175 .387 .39 .524 .644 .138 .253 .229 .542 .271 .867 h-1.036Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 3.5 0
                moveTo(x = 3.5f, y = 0.0f)
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
                // V 10
                verticalLineTo(y = 10.0f)
                // H 0.593
                horizontalLineTo(x = 0.593f)
                // c -0.595 0 -0.818 0.804 -0.314 1.129
                curveToRelative(
                    dx1 = -0.595f,
                    dy1 = 0.0f,
                    dx2 = -0.818f,
                    dy2 = 0.804f,
                    dx3 = -0.314f,
                    dy3 = 1.129f,
                )
                // l 7.407 4.778
                lineToRelative(dx = 7.407f, dy = 4.778f)
                // a 0.576 0.576 0 0 0 0.628 0
                arcToRelative(
                    a = 0.576f,
                    b = 0.576f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.628f,
                    dy1 = 0.0f,
                )
                // l 7.407 -4.778
                lineToRelative(dx = 7.407f, dy = -4.778f)
                // c 0.504 -0.325 0.28 -1.129 -0.314 -1.129
                curveToRelative(
                    dx1 = 0.504f,
                    dy1 = -0.325f,
                    dx2 = 0.28f,
                    dy2 = -1.129f,
                    dx3 = -0.314f,
                    dy3 = -1.129f,
                )
                // H 13
                horizontalLineTo(x = 13.0f)
                // V 0.5
                verticalLineTo(y = 0.5f)
                // a 0.5 0.5 0 0 0 -0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                // h -9z
                horizontalLineToRelative(dx = -9.0f)
                close()
                // m 2 5
                moveToRelative(dx = 2.0f, dy = 5.0f)
                // c -0.277 0 -0.528 -0.067 -0.755 -0.2
                curveToRelative(
                    dx1 = -0.277f,
                    dy1 = 0.0f,
                    dx2 = -0.528f,
                    dy2 = -0.067f,
                    dx3 = -0.755f,
                    dy3 = -0.2f,
                )
                // a 1.542 1.542 0 0 1 -0.542 -0.543
                arcToRelative(
                    a = 1.542f,
                    b = 1.542f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.542f,
                    dy1 = -0.543f,
                )
                // A 1.465 1.465 0 0 1 4 3.498
                arcTo(
                    horizontalEllipseRadius = 1.465f,
                    verticalEllipseRadius = 1.465f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 4.0f,
                    y1 = 3.498f,
                )
                // a 1.51 1.51 0 0 1 0.745 -1.296
                arcToRelative(
                    a = 1.51f,
                    b = 1.51f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.745f,
                    dy1 = -1.296f,
                )
                // C 4.972 2.069 5.223 2 5.5 2
                curveTo(
                    x1 = 4.972f,
                    y1 = 2.069f,
                    x2 = 5.223f,
                    y2 = 2.0f,
                    x3 = 5.5f,
                    y3 = 2.0f,
                )
                // s 0.528 0.068 0.755 0.203
                reflectiveCurveToRelative(
                    dx1 = 0.528f,
                    dy1 = 0.068f,
                    dx2 = 0.755f,
                    dy2 = 0.203f,
                )
                // A 1.509 1.509 0 0 1 7 3.498
                arcTo(
                    horizontalEllipseRadius = 1.509f,
                    verticalEllipseRadius = 1.509f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.0f,
                    y1 = 3.498f,
                )
                // c 0 0.277 -0.068 0.53 -0.203 0.759
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.277f,
                    dx2 = -0.068f,
                    dy2 = 0.53f,
                    dx3 = -0.203f,
                    dy3 = 0.759f,
                )
                // a 1.542 1.542 0 0 1 -0.542 0.543
                arcToRelative(
                    a = 1.542f,
                    b = 1.542f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.542f,
                    dy1 = 0.543f,
                )
                // c -0.227 0.133 -0.478 0.2 -0.755 0.2z
                curveToRelative(
                    dx1 = -0.227f,
                    dy1 = 0.133f,
                    dx2 = -0.478f,
                    dy2 = 0.2f,
                    dx3 = -0.755f,
                    dy3 = 0.2f,
                )
                close()
                // m 5.464 0.05
                moveToRelative(dx = 5.464f, dy = 0.05f)
                // a 1.354 1.354 0 0 0 -0.163 -0.464
                arcToRelative(
                    a = 1.354f,
                    b = 1.354f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.163f,
                    dy1 = -0.464f,
                )
                // a 1.223 1.223 0 0 0 -0.7 -0.562
                arcToRelative(
                    a = 1.223f,
                    b = 1.223f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.7f,
                    dy1 = -0.562f,
                )
                // a 1.536 1.536 0 0 0 -0.486 -0.074
                arcToRelative(
                    a = 1.536f,
                    b = 1.536f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.486f,
                    dy1 = -0.074f,
                )
                // c -0.308 0 -0.582 0.08 -0.82 0.24
                curveToRelative(
                    dx1 = -0.308f,
                    dy1 = 0.0f,
                    dx2 = -0.582f,
                    dy2 = 0.08f,
                    dx3 = -0.82f,
                    dy3 = 0.24f,
                )
                // a 1.559 1.559 0 0 0 -0.56 0.695
                arcToRelative(
                    a = 1.559f,
                    b = 1.559f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.56f,
                    dy1 = 0.695f,
                )
                // c -0.134 0.304 -0.202 0.676 -0.202 1.115
                curveToRelative(
                    dx1 = -0.134f,
                    dy1 = 0.304f,
                    dx2 = -0.202f,
                    dy2 = 0.676f,
                    dx3 = -0.202f,
                    dy3 = 1.115f,
                )
                // c 0 0.447 0.068 0.823 0.202 1.13
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.447f,
                    dx2 = 0.068f,
                    dy2 = 0.823f,
                    dx3 = 0.202f,
                    dy3 = 1.13f,
                )
                // c 0.137 0.303 0.324 0.534 0.56 0.69
                curveToRelative(
                    dx1 = 0.137f,
                    dy1 = 0.303f,
                    dx2 = 0.324f,
                    dy2 = 0.534f,
                    dx3 = 0.56f,
                    dy3 = 0.69f,
                )
                // c 0.238 0.153 0.51 0.23 0.817 0.23
                curveToRelative(
                    dx1 = 0.238f,
                    dy1 = 0.153f,
                    dx2 = 0.51f,
                    dy2 = 0.23f,
                    dx3 = 0.817f,
                    dy3 = 0.23f,
                )
                // c 0.17 0 0.329 -0.022 0.477 -0.068
                curveToRelative(
                    dx1 = 0.17f,
                    dy1 = 0.0f,
                    dx2 = 0.329f,
                    dy2 = -0.022f,
                    dx3 = 0.477f,
                    dy3 = -0.068f,
                )
                // a 1.29 1.29 0 0 0 0.401 -0.208
                arcToRelative(
                    a = 1.29f,
                    b = 1.29f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.401f,
                    dy1 = -0.208f,
                )
                // a 1.293 1.293 0 0 0 0.474 -0.793
                arcToRelative(
                    a = 1.293f,
                    b = 1.293f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.474f,
                    dy1 = -0.793f,
                )
                // L 12 6.987
                lineTo(x = 12.0f, y = 6.987f)
                // a 2.343 2.343 0 0 1 -0.754 1.426
                arcToRelative(
                    a = 2.343f,
                    b = 2.343f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.754f,
                    dy1 = 1.426f,
                )
                // a 2.297 2.297 0 0 1 -0.725 0.433
                arcToRelative(
                    a = 2.297f,
                    b = 2.297f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.725f,
                    dy1 = 0.433f,
                )
                // A 2.637 2.637 0 0 1 9.598 9
                arcTo(
                    horizontalEllipseRadius = 2.637f,
                    verticalEllipseRadius = 2.637f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 9.598f,
                    y1 = 9.0f,
                )
                // a 2.53 2.53 0 0 1 -1.335 -0.356
                arcToRelative(
                    a = 2.53f,
                    b = 2.53f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.335f,
                    dy1 = -0.356f,
                )
                // a 2.464 2.464 0 0 1 -0.925 -1.03
                arcToRelative(
                    a = 2.464f,
                    b = 2.464f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.925f,
                    dy1 = -1.03f,
                )
                // C 7.113 7.165 7 6.627 7 6
                curveTo(
                    x1 = 7.113f,
                    y1 = 7.165f,
                    x2 = 7.0f,
                    y2 = 6.627f,
                    x3 = 7.0f,
                    y3 = 6.0f,
                )
                // c 0 -0.63 0.114 -1.167 0.34 -1.614
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.63f,
                    dx2 = 0.114f,
                    dy2 = -1.167f,
                    dx3 = 0.34f,
                    dy3 = -1.614f,
                )
                // a 2.48 2.48 0 0 1 0.929 -1.03
                arcToRelative(
                    a = 2.48f,
                    b = 2.48f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.929f,
                    dy1 = -1.03f,
                )
                // A 2.511 2.511 0 0 1 9.599 3
                arcTo(
                    horizontalEllipseRadius = 2.511f,
                    verticalEllipseRadius = 2.511f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 9.599f,
                    y1 = 3.0f,
                )
                // c 0.315 0 0.609 0.046 0.88 0.137
                curveToRelative(
                    dx1 = 0.315f,
                    dy1 = 0.0f,
                    dx2 = 0.609f,
                    dy2 = 0.046f,
                    dx3 = 0.88f,
                    dy3 = 0.137f,
                )
                // c 0.272 0.091 0.514 0.225 0.726 0.402
                curveToRelative(
                    dx1 = 0.272f,
                    dy1 = 0.091f,
                    dx2 = 0.514f,
                    dy2 = 0.225f,
                    dx3 = 0.726f,
                    dy3 = 0.402f,
                )
                // c 0.212 0.175 0.387 0.39 0.524 0.644
                curveToRelative(
                    dx1 = 0.212f,
                    dy1 = 0.175f,
                    dx2 = 0.387f,
                    dy2 = 0.39f,
                    dx3 = 0.524f,
                    dy3 = 0.644f,
                )
                // c 0.138 0.253 0.229 0.542 0.271 0.867
                curveToRelative(
                    dx1 = 0.138f,
                    dy1 = 0.253f,
                    dx2 = 0.229f,
                    dy2 = 0.542f,
                    dx3 = 0.271f,
                    dy3 = 0.867f,
                )
                // h -1.036z
                horizontalLineToRelative(dx = -1.036f)
                close()
            }
        }.build().also { _ic1048 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1048: ImageVector? = null
