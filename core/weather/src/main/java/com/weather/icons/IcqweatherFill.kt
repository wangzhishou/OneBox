package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.IcqweatherFill: ImageVector
    get() {
        val current = _icqweatherFill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.IcqweatherFill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M3.905 11.625 a4.666 4.666 0 0 0 3.007 1.382 6.36 6.36 0 0 1 -2.795 -2.604 3.54 3.54 0 0 1 .589 -4.054 c.655 -.692 1.586 -1.093 2.619 -1.123 1.151 -.033 2.162 .458 2.803 1.295 a2.157 2.157 0 0 0 .029 .036 3.85 3.85 0 0 1 .11 .147 c.252 .383 .385 .83 .382 1.288 a2.235 2.235 0 0 1 -1.168 1.964 2.235 2.235 0 0 1 -.998 .279 h-.02 a1.36 1.36 0 0 1 -1.158 -.65 .135 .135 0 0 1 .086 -.202 .138 .138 0 0 1 .084 .007 1.236 1.236 0 0 0 1.14 -.13 1.213 1.213 0 0 0 .526 -1.036 1.202 1.202 0 0 0 -.168 -.574 1.357 1.357 0 0 0 -.638 -.554 1.156 1.156 0 0 0 -.162 -.05 l-.01 -.002 -.023 -.005 a2.026 2.026 0 0 0 -2.03 .666 1.831 1.831 0 0 0 -.444 1.233 2.6 2.6 0 0 0 .568 1.567 v.001 l.003 .002 c.65 .766 1.572 1.432 2.63 1.67 .286 .062 .577 .102 .87 .12 a4.616 4.616 0 0 0 1.942 -2.523 4.576 4.576 0 0 0 -.161 -3.17 A4.628 4.628 0 0 0 9.33 4.29 a4.686 4.686 0 0 0 -3.178 -.366 A4.653 4.653 0 0 0 3.487 5.68 a4.584 4.584 0 0 0 .418 5.945Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 3.905 11.625
                moveTo(x = 3.905f, y = 11.625f)
                // a 4.666 4.666 0 0 0 3.007 1.382
                arcToRelative(
                    a = 4.666f,
                    b = 4.666f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 3.007f,
                    dy1 = 1.382f,
                )
                // a 6.36 6.36 0 0 1 -2.795 -2.604
                arcToRelative(
                    a = 6.36f,
                    b = 6.36f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.795f,
                    dy1 = -2.604f,
                )
                // a 3.54 3.54 0 0 1 0.589 -4.054
                arcToRelative(
                    a = 3.54f,
                    b = 3.54f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.589f,
                    dy1 = -4.054f,
                )
                // c 0.655 -0.692 1.586 -1.093 2.619 -1.123
                curveToRelative(
                    dx1 = 0.655f,
                    dy1 = -0.692f,
                    dx2 = 1.586f,
                    dy2 = -1.093f,
                    dx3 = 2.619f,
                    dy3 = -1.123f,
                )
                // c 1.151 -0.033 2.162 0.458 2.803 1.295
                curveToRelative(
                    dx1 = 1.151f,
                    dy1 = -0.033f,
                    dx2 = 2.162f,
                    dy2 = 0.458f,
                    dx3 = 2.803f,
                    dy3 = 1.295f,
                )
                // a 2.157 2.157 0 0 0 0.029 0.036
                arcToRelative(
                    a = 2.157f,
                    b = 2.157f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.029f,
                    dy1 = 0.036f,
                )
                // a 3.85 3.85 0 0 1 0.11 0.147
                arcToRelative(
                    a = 3.85f,
                    b = 3.85f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.11f,
                    dy1 = 0.147f,
                )
                // c 0.252 0.383 0.385 0.83 0.382 1.288
                curveToRelative(
                    dx1 = 0.252f,
                    dy1 = 0.383f,
                    dx2 = 0.385f,
                    dy2 = 0.83f,
                    dx3 = 0.382f,
                    dy3 = 1.288f,
                )
                // a 2.235 2.235 0 0 1 -1.168 1.964
                arcToRelative(
                    a = 2.235f,
                    b = 2.235f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.168f,
                    dy1 = 1.964f,
                )
                // a 2.235 2.235 0 0 1 -0.998 0.279
                arcToRelative(
                    a = 2.235f,
                    b = 2.235f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.998f,
                    dy1 = 0.279f,
                )
                // h -0.02
                horizontalLineToRelative(dx = -0.02f)
                // a 1.36 1.36 0 0 1 -1.158 -0.65
                arcToRelative(
                    a = 1.36f,
                    b = 1.36f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.158f,
                    dy1 = -0.65f,
                )
                // a 0.135 0.135 0 0 1 0.086 -0.202
                arcToRelative(
                    a = 0.135f,
                    b = 0.135f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.086f,
                    dy1 = -0.202f,
                )
                // a 0.138 0.138 0 0 1 0.084 0.007
                arcToRelative(
                    a = 0.138f,
                    b = 0.138f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.084f,
                    dy1 = 0.007f,
                )
                // a 1.236 1.236 0 0 0 1.14 -0.13
                arcToRelative(
                    a = 1.236f,
                    b = 1.236f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.14f,
                    dy1 = -0.13f,
                )
                // a 1.213 1.213 0 0 0 0.526 -1.036
                arcToRelative(
                    a = 1.213f,
                    b = 1.213f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.526f,
                    dy1 = -1.036f,
                )
                // a 1.202 1.202 0 0 0 -0.168 -0.574
                arcToRelative(
                    a = 1.202f,
                    b = 1.202f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.168f,
                    dy1 = -0.574f,
                )
                // a 1.357 1.357 0 0 0 -0.638 -0.554
                arcToRelative(
                    a = 1.357f,
                    b = 1.357f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.638f,
                    dy1 = -0.554f,
                )
                // a 1.156 1.156 0 0 0 -0.162 -0.05
                arcToRelative(
                    a = 1.156f,
                    b = 1.156f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.162f,
                    dy1 = -0.05f,
                )
                // l -0.01 -0.002
                lineToRelative(dx = -0.01f, dy = -0.002f)
                // l -0.023 -0.005
                lineToRelative(dx = -0.023f, dy = -0.005f)
                // a 2.026 2.026 0 0 0 -2.03 0.666
                arcToRelative(
                    a = 2.026f,
                    b = 2.026f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.03f,
                    dy1 = 0.666f,
                )
                // a 1.831 1.831 0 0 0 -0.444 1.233
                arcToRelative(
                    a = 1.831f,
                    b = 1.831f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.444f,
                    dy1 = 1.233f,
                )
                // a 2.6 2.6 0 0 0 0.568 1.567
                arcToRelative(
                    a = 2.6f,
                    b = 2.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.568f,
                    dy1 = 1.567f,
                )
                // v 0.001
                verticalLineToRelative(dy = 0.001f)
                // l 0.003 0.002
                lineToRelative(dx = 0.003f, dy = 0.002f)
                // c 0.65 0.766 1.572 1.432 2.63 1.67
                curveToRelative(
                    dx1 = 0.65f,
                    dy1 = 0.766f,
                    dx2 = 1.572f,
                    dy2 = 1.432f,
                    dx3 = 2.63f,
                    dy3 = 1.67f,
                )
                // c 0.286 0.062 0.577 0.102 0.87 0.12
                curveToRelative(
                    dx1 = 0.286f,
                    dy1 = 0.062f,
                    dx2 = 0.577f,
                    dy2 = 0.102f,
                    dx3 = 0.87f,
                    dy3 = 0.12f,
                )
                // a 4.616 4.616 0 0 0 1.942 -2.523
                arcToRelative(
                    a = 4.616f,
                    b = 4.616f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.942f,
                    dy1 = -2.523f,
                )
                // a 4.576 4.576 0 0 0 -0.161 -3.17
                arcToRelative(
                    a = 4.576f,
                    b = 4.576f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.161f,
                    dy1 = -3.17f,
                )
                // A 4.628 4.628 0 0 0 9.33 4.29
                arcTo(
                    horizontalEllipseRadius = 4.628f,
                    verticalEllipseRadius = 4.628f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 9.33f,
                    y1 = 4.29f,
                )
                // a 4.686 4.686 0 0 0 -3.178 -0.366
                arcToRelative(
                    a = 4.686f,
                    b = 4.686f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -3.178f,
                    dy1 = -0.366f,
                )
                // A 4.653 4.653 0 0 0 3.487 5.68
                arcTo(
                    horizontalEllipseRadius = 4.653f,
                    verticalEllipseRadius = 4.653f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 3.487f,
                    y1 = 5.68f,
                )
                // a 4.584 4.584 0 0 0 0.418 5.945z
                arcToRelative(
                    a = 4.584f,
                    b = 4.584f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.418f,
                    dy1 = 5.945f,
                )
                close()
            }
            // M2 0 a2 2 0 0 0 -2 2 v12 a2 2 0 0 0 2 2 h12 a2 2 0 0 0 2 -2 V2 a2 2 0 0 0 -2 -2 H2Z m11.413 11.066 .222 -.198 h.865 c-.083 .12 -.162 .238 -.238 .352 -.462 .69 -.85 1.27 -1.634 1.68 a6.388 6.388 0 0 1 -3.53 .702 5.945 5.945 0 0 1 -4.176 -.293 5.873 5.873 0 0 1 -2.929 -2.967 5.805 5.805 0 0 1 -.205 -4.147 5.86 5.86 0 0 1 2.621 -3.238 5.942 5.942 0 0 1 4.128 -.697 5.906 5.906 0 0 1 3.554 2.194 5.816 5.816 0 0 1 -.58 7.742 c.859 -.199 1.292 -.586 1.902 -1.13Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 2 0
                moveTo(x = 2.0f, y = 0.0f)
                // a 2 2 0 0 0 -2 2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.0f,
                    dy1 = 2.0f,
                )
                // v 12
                verticalLineToRelative(dy = 12.0f)
                // a 2 2 0 0 0 2 2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.0f,
                    dy1 = 2.0f,
                )
                // h 12
                horizontalLineToRelative(dx = 12.0f)
                // a 2 2 0 0 0 2 -2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.0f,
                    dy1 = -2.0f,
                )
                // V 2
                verticalLineTo(y = 2.0f)
                // a 2 2 0 0 0 -2 -2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.0f,
                    dy1 = -2.0f,
                )
                // H 2z
                horizontalLineTo(x = 2.0f)
                close()
                // m 11.413 11.066
                moveToRelative(dx = 11.413f, dy = 11.066f)
                // l 0.222 -0.198
                lineToRelative(dx = 0.222f, dy = -0.198f)
                // h 0.865
                horizontalLineToRelative(dx = 0.865f)
                // c -0.083 0.12 -0.162 0.238 -0.238 0.352
                curveToRelative(
                    dx1 = -0.083f,
                    dy1 = 0.12f,
                    dx2 = -0.162f,
                    dy2 = 0.238f,
                    dx3 = -0.238f,
                    dy3 = 0.352f,
                )
                // c -0.462 0.69 -0.85 1.27 -1.634 1.68
                curveToRelative(
                    dx1 = -0.462f,
                    dy1 = 0.69f,
                    dx2 = -0.85f,
                    dy2 = 1.27f,
                    dx3 = -1.634f,
                    dy3 = 1.68f,
                )
                // a 6.388 6.388 0 0 1 -3.53 0.702
                arcToRelative(
                    a = 6.388f,
                    b = 6.388f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.53f,
                    dy1 = 0.702f,
                )
                // a 5.945 5.945 0 0 1 -4.176 -0.293
                arcToRelative(
                    a = 5.945f,
                    b = 5.945f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -4.176f,
                    dy1 = -0.293f,
                )
                // a 5.873 5.873 0 0 1 -2.929 -2.967
                arcToRelative(
                    a = 5.873f,
                    b = 5.873f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.929f,
                    dy1 = -2.967f,
                )
                // a 5.805 5.805 0 0 1 -0.205 -4.147
                arcToRelative(
                    a = 5.805f,
                    b = 5.805f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.205f,
                    dy1 = -4.147f,
                )
                // a 5.86 5.86 0 0 1 2.621 -3.238
                arcToRelative(
                    a = 5.86f,
                    b = 5.86f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.621f,
                    dy1 = -3.238f,
                )
                // a 5.942 5.942 0 0 1 4.128 -0.697
                arcToRelative(
                    a = 5.942f,
                    b = 5.942f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 4.128f,
                    dy1 = -0.697f,
                )
                // a 5.906 5.906 0 0 1 3.554 2.194
                arcToRelative(
                    a = 5.906f,
                    b = 5.906f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 3.554f,
                    dy1 = 2.194f,
                )
                // a 5.816 5.816 0 0 1 -0.58 7.742
                arcToRelative(
                    a = 5.816f,
                    b = 5.816f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.58f,
                    dy1 = 7.742f,
                )
                // c 0.859 -0.199 1.292 -0.586 1.902 -1.13z
                curveToRelative(
                    dx1 = 0.859f,
                    dy1 = -0.199f,
                    dx2 = 1.292f,
                    dy2 = -0.586f,
                    dx3 = 1.902f,
                    dy3 = -1.13f,
                )
                close()
            }
        }.build().also { _icqweatherFill = it }
    }

@Suppress("ObjectPropertyName")
private var _icqweatherFill: ImageVector? = null
