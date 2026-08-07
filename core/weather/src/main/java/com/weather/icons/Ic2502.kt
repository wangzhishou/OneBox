package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2502: ImageVector
    get() {
        val current = _ic2502
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2502",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.9 10 a4.99 4.99 0 0 0 3.827 -1.783 3 3 0 1 0 .553 -5.63 A4.999 4.999 0 0 0 7.9 0 a4.998 4.998 0 0 0 -4.359 2.549 3 3 0 1 0 .586 5.732 A4.988 4.988 0 0 0 7.9 10Z m.495 -5.467 c-.024 .043 .011 .093 .065 .093 h1.468 c.06 0 .094 .065 .055 .107 L7.005 7.976 c-.052 .055 -.151 .006 -.124 -.063 L7.62 5.97 c.015 -.043 -.019 -.087 -.069 -.087 h-1.42 c-.096 0 -.16 -.09 -.118 -.17 L7.41 3.078 a.159 .159 0 0 1 .058 -.057 .147 .147 0 0 1 .076 -.02 h1.578 c.054 0 .089 .051 .065 .094 l-.792 1.439Z m.548 7.171 a.494 .494 0 0 0 -.157 -.641 c-.205 -.13 -.467 -.053 -.586 .172 l-2.143 4.061 a.494 .494 0 0 0 .157 .641 c.205 .13 .467 .053 .586 -.172 l2.143 -4.061Z M2.8 11.556 c.191 .115 .257 .37 .146 .57 l-1.2 2.165 a.391 .391 0 0 1 -.546 .153 .427 .427 0 0 1 -.146 -.57 l1.2 -2.165 a.39 .39 0 0 1 .546 -.153Z m3 0 c.191 .115 .257 .37 .146 .57 l-1.2 2.165 a.391 .391 0 0 1 -.546 .153 .427 .427 0 0 1 -.146 -.57 l1.2 -2.165 a.39 .39 0 0 1 .546 -.153Z m9 0 c.191 .115 .257 .37 .146 .57 l-1.2 2.165 a.391 .391 0 0 1 -.546 .153 .427 .427 0 0 1 -.146 -.57 l1.2 -2.165 a.39 .39 0 0 1 .546 -.153Z m-2.854 .569 a.427 .427 0 0 0 -.146 -.57 .391 .391 0 0 0 -.546 .154 l-1.2 2.166 a.428 .428 0 0 0 .146 .57 .391 .391 0 0 0 .546 -.154 l1.2 -2.166Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.9 10
                moveTo(x = 7.9f, y = 10.0f)
                // a 4.99 4.99 0 0 0 3.827 -1.783
                arcToRelative(
                    a = 4.99f,
                    b = 4.99f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 3.827f,
                    dy1 = -1.783f,
                )
                // a 3 3 0 1 0 0.553 -5.63
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.553f,
                    dy1 = -5.63f,
                )
                // A 4.999 4.999 0 0 0 7.9 0
                arcTo(
                    horizontalEllipseRadius = 4.999f,
                    verticalEllipseRadius = 4.999f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.9f,
                    y1 = 0.0f,
                )
                // a 4.998 4.998 0 0 0 -4.359 2.549
                arcToRelative(
                    a = 4.998f,
                    b = 4.998f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -4.359f,
                    dy1 = 2.549f,
                )
                // a 3 3 0 1 0 0.586 5.732
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.586f,
                    dy1 = 5.732f,
                )
                // A 4.988 4.988 0 0 0 7.9 10z
                arcTo(
                    horizontalEllipseRadius = 4.988f,
                    verticalEllipseRadius = 4.988f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.9f,
                    y1 = 10.0f,
                )
                close()
                // m 0.495 -5.467
                moveToRelative(dx = 0.495f, dy = -5.467f)
                // c -0.024 0.043 0.011 0.093 0.065 0.093
                curveToRelative(
                    dx1 = -0.024f,
                    dy1 = 0.043f,
                    dx2 = 0.011f,
                    dy2 = 0.093f,
                    dx3 = 0.065f,
                    dy3 = 0.093f,
                )
                // h 1.468
                horizontalLineToRelative(dx = 1.468f)
                // c 0.06 0 0.094 0.065 0.055 0.107
                curveToRelative(
                    dx1 = 0.06f,
                    dy1 = 0.0f,
                    dx2 = 0.094f,
                    dy2 = 0.065f,
                    dx3 = 0.055f,
                    dy3 = 0.107f,
                )
                // L 7.005 7.976
                lineTo(x = 7.005f, y = 7.976f)
                // c -0.052 0.055 -0.151 0.006 -0.124 -0.063
                curveToRelative(
                    dx1 = -0.052f,
                    dy1 = 0.055f,
                    dx2 = -0.151f,
                    dy2 = 0.006f,
                    dx3 = -0.124f,
                    dy3 = -0.063f,
                )
                // L 7.62 5.97
                lineTo(x = 7.62f, y = 5.97f)
                // c 0.015 -0.043 -0.019 -0.087 -0.069 -0.087
                curveToRelative(
                    dx1 = 0.015f,
                    dy1 = -0.043f,
                    dx2 = -0.019f,
                    dy2 = -0.087f,
                    dx3 = -0.069f,
                    dy3 = -0.087f,
                )
                // h -1.42
                horizontalLineToRelative(dx = -1.42f)
                // c -0.096 0 -0.16 -0.09 -0.118 -0.17
                curveToRelative(
                    dx1 = -0.096f,
                    dy1 = 0.0f,
                    dx2 = -0.16f,
                    dy2 = -0.09f,
                    dx3 = -0.118f,
                    dy3 = -0.17f,
                )
                // L 7.41 3.078
                lineTo(x = 7.41f, y = 3.078f)
                // a 0.159 0.159 0 0 1 0.058 -0.057
                arcToRelative(
                    a = 0.159f,
                    b = 0.159f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.058f,
                    dy1 = -0.057f,
                )
                // a 0.147 0.147 0 0 1 0.076 -0.02
                arcToRelative(
                    a = 0.147f,
                    b = 0.147f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.076f,
                    dy1 = -0.02f,
                )
                // h 1.578
                horizontalLineToRelative(dx = 1.578f)
                // c 0.054 0 0.089 0.051 0.065 0.094
                curveToRelative(
                    dx1 = 0.054f,
                    dy1 = 0.0f,
                    dx2 = 0.089f,
                    dy2 = 0.051f,
                    dx3 = 0.065f,
                    dy3 = 0.094f,
                )
                // l -0.792 1.439z
                lineToRelative(dx = -0.792f, dy = 1.439f)
                close()
                // m 0.548 7.171
                moveToRelative(dx = 0.548f, dy = 7.171f)
                // a 0.494 0.494 0 0 0 -0.157 -0.641
                arcToRelative(
                    a = 0.494f,
                    b = 0.494f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.157f,
                    dy1 = -0.641f,
                )
                // c -0.205 -0.13 -0.467 -0.053 -0.586 0.172
                curveToRelative(
                    dx1 = -0.205f,
                    dy1 = -0.13f,
                    dx2 = -0.467f,
                    dy2 = -0.053f,
                    dx3 = -0.586f,
                    dy3 = 0.172f,
                )
                // l -2.143 4.061
                lineToRelative(dx = -2.143f, dy = 4.061f)
                // a 0.494 0.494 0 0 0 0.157 0.641
                arcToRelative(
                    a = 0.494f,
                    b = 0.494f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.157f,
                    dy1 = 0.641f,
                )
                // c 0.205 0.13 0.467 0.053 0.586 -0.172
                curveToRelative(
                    dx1 = 0.205f,
                    dy1 = 0.13f,
                    dx2 = 0.467f,
                    dy2 = 0.053f,
                    dx3 = 0.586f,
                    dy3 = -0.172f,
                )
                // l 2.143 -4.061z
                lineToRelative(dx = 2.143f, dy = -4.061f)
                close()
                // M 2.8 11.556
                moveTo(x = 2.8f, y = 11.556f)
                // c 0.191 0.115 0.257 0.37 0.146 0.57
                curveToRelative(
                    dx1 = 0.191f,
                    dy1 = 0.115f,
                    dx2 = 0.257f,
                    dy2 = 0.37f,
                    dx3 = 0.146f,
                    dy3 = 0.57f,
                )
                // l -1.2 2.165
                lineToRelative(dx = -1.2f, dy = 2.165f)
                // a 0.391 0.391 0 0 1 -0.546 0.153
                arcToRelative(
                    a = 0.391f,
                    b = 0.391f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.546f,
                    dy1 = 0.153f,
                )
                // a 0.427 0.427 0 0 1 -0.146 -0.57
                arcToRelative(
                    a = 0.427f,
                    b = 0.427f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.146f,
                    dy1 = -0.57f,
                )
                // l 1.2 -2.165
                lineToRelative(dx = 1.2f, dy = -2.165f)
                // a 0.39 0.39 0 0 1 0.546 -0.153z
                arcToRelative(
                    a = 0.39f,
                    b = 0.39f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.546f,
                    dy1 = -0.153f,
                )
                close()
                // m 3 0
                moveToRelative(dx = 3.0f, dy = 0.0f)
                // c 0.191 0.115 0.257 0.37 0.146 0.57
                curveToRelative(
                    dx1 = 0.191f,
                    dy1 = 0.115f,
                    dx2 = 0.257f,
                    dy2 = 0.37f,
                    dx3 = 0.146f,
                    dy3 = 0.57f,
                )
                // l -1.2 2.165
                lineToRelative(dx = -1.2f, dy = 2.165f)
                // a 0.391 0.391 0 0 1 -0.546 0.153
                arcToRelative(
                    a = 0.391f,
                    b = 0.391f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.546f,
                    dy1 = 0.153f,
                )
                // a 0.427 0.427 0 0 1 -0.146 -0.57
                arcToRelative(
                    a = 0.427f,
                    b = 0.427f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.146f,
                    dy1 = -0.57f,
                )
                // l 1.2 -2.165
                lineToRelative(dx = 1.2f, dy = -2.165f)
                // a 0.39 0.39 0 0 1 0.546 -0.153z
                arcToRelative(
                    a = 0.39f,
                    b = 0.39f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.546f,
                    dy1 = -0.153f,
                )
                close()
                // m 9 0
                moveToRelative(dx = 9.0f, dy = 0.0f)
                // c 0.191 0.115 0.257 0.37 0.146 0.57
                curveToRelative(
                    dx1 = 0.191f,
                    dy1 = 0.115f,
                    dx2 = 0.257f,
                    dy2 = 0.37f,
                    dx3 = 0.146f,
                    dy3 = 0.57f,
                )
                // l -1.2 2.165
                lineToRelative(dx = -1.2f, dy = 2.165f)
                // a 0.391 0.391 0 0 1 -0.546 0.153
                arcToRelative(
                    a = 0.391f,
                    b = 0.391f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.546f,
                    dy1 = 0.153f,
                )
                // a 0.427 0.427 0 0 1 -0.146 -0.57
                arcToRelative(
                    a = 0.427f,
                    b = 0.427f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.146f,
                    dy1 = -0.57f,
                )
                // l 1.2 -2.165
                lineToRelative(dx = 1.2f, dy = -2.165f)
                // a 0.39 0.39 0 0 1 0.546 -0.153z
                arcToRelative(
                    a = 0.39f,
                    b = 0.39f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.546f,
                    dy1 = -0.153f,
                )
                close()
                // m -2.854 0.569
                moveToRelative(dx = -2.854f, dy = 0.569f)
                // a 0.427 0.427 0 0 0 -0.146 -0.57
                arcToRelative(
                    a = 0.427f,
                    b = 0.427f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.146f,
                    dy1 = -0.57f,
                )
                // a 0.391 0.391 0 0 0 -0.546 0.154
                arcToRelative(
                    a = 0.391f,
                    b = 0.391f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.546f,
                    dy1 = 0.154f,
                )
                // l -1.2 2.166
                lineToRelative(dx = -1.2f, dy = 2.166f)
                // a 0.428 0.428 0 0 0 0.146 0.57
                arcToRelative(
                    a = 0.428f,
                    b = 0.428f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.146f,
                    dy1 = 0.57f,
                )
                // a 0.391 0.391 0 0 0 0.546 -0.154
                arcToRelative(
                    a = 0.391f,
                    b = 0.391f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.546f,
                    dy1 = -0.154f,
                )
                // l 1.2 -2.166z
                lineToRelative(dx = 1.2f, dy = -2.166f)
                close()
            }
        }.build().also { _ic2502 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2502: ImageVector? = null
