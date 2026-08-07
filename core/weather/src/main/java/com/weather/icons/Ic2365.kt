package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2365: ImageVector
    get() {
        val current = _ic2365
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2365",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.502 6.62 c-.026 -.226 .205 -.42 .498 -.42 .293 0 .524 .194 .498 .42 L8.273 8.6 h-.546 l-.225 -1.98Z m.901 2.78 a.4 .4 0 1 1 -.8 0 .4 .4 0 0 1 .8 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.502 6.62
                moveTo(x = 7.502f, y = 6.62f)
                // c -0.026 -0.226 0.205 -0.42 0.498 -0.42
                curveToRelative(
                    dx1 = -0.026f,
                    dy1 = -0.226f,
                    dx2 = 0.205f,
                    dy2 = -0.42f,
                    dx3 = 0.498f,
                    dy3 = -0.42f,
                )
                // c 0.293 0 0.524 0.194 0.498 0.42
                curveToRelative(
                    dx1 = 0.293f,
                    dy1 = 0.0f,
                    dx2 = 0.524f,
                    dy2 = 0.194f,
                    dx3 = 0.498f,
                    dy3 = 0.42f,
                )
                // L 8.273 8.6
                lineTo(x = 8.273f, y = 8.6f)
                // h -0.546
                horizontalLineToRelative(dx = -0.546f)
                // l -0.225 -1.98z
                lineToRelative(dx = -0.225f, dy = -1.98f)
                close()
                // m 0.901 2.78
                moveToRelative(dx = 0.901f, dy = 2.78f)
                // a 0.4 0.4 0 1 1 -0.8 0
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.8f,
                    dy1 = 0.0f,
                )
                // a 0.4 0.4 0 0 1 0.8 0z
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.8f,
                    dy1 = 0.0f,
                )
                close()
            }
            // M7.45 2.615 A4.11 4.11 0 0 1 10.84 .828 c.441 0 .857 .088 1.256 .217 a6.446 6.446 0 0 0 -6.167 .535 A6.564 6.564 0 0 0 3.77 3.959 a6.643 6.643 0 0 0 -.655 1.83 4.216 4.216 0 0 1 -.354 -2.318 4.23 4.23 0 0 1 .519 -1.546 4.13 4.13 0 0 1 .814 -.99 6.576 6.576 0 0 0 -2.02 2.5 6.66 6.66 0 0 0 .352 6.252 6.6 6.6 0 0 0 1.239 1.488 4.095 4.095 0 0 1 -2.16 -.85 A4.17 4.17 0 0 1 .442 9.099 4.144 4.144 0 0 1 0 7.89 a6.64 6.64 0 0 0 1.13 3.02 6.538 6.538 0 0 0 2.41 2.116 6.45 6.45 0 0 0 5.009 .361 4.11 4.11 0 0 1 -3.388 1.786 4.04 4.04 0 0 1 -1.256 -.218 6.446 6.446 0 0 0 6.166 -.535 6.564 6.564 0 0 0 2.159 -2.38 6.643 6.643 0 0 0 .654 -1.83 4.223 4.223 0 0 1 -.164 3.865 4.132 4.132 0 0 1 -.814 .991 6.576 6.576 0 0 0 2.021 -2.5 6.66 6.66 0 0 0 -.352 -6.253 6.571 6.571 0 0 0 -1.238 -1.487 4.088 4.088 0 0 1 2.158 .849 c.43 .334 .792 .752 1.063 1.227 .221 .387 .354 .796 .442 1.21 a6.64 6.64 0 0 0 -1.13 -3.021 6.537 6.537 0 0 0 -2.41 -2.116 6.45 6.45 0 0 0 -5.01 -.36Z M11 8 a3 3 0 1 1 -6 0 3 3 0 0 1 6 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.45 2.615
                moveTo(x = 7.45f, y = 2.615f)
                // A 4.11 4.11 0 0 1 10.84 0.828
                arcTo(
                    horizontalEllipseRadius = 4.11f,
                    verticalEllipseRadius = 4.11f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 10.84f,
                    y1 = 0.828f,
                )
                // c 0.441 0 0.857 0.088 1.256 0.217
                curveToRelative(
                    dx1 = 0.441f,
                    dy1 = 0.0f,
                    dx2 = 0.857f,
                    dy2 = 0.088f,
                    dx3 = 1.256f,
                    dy3 = 0.217f,
                )
                // a 6.446 6.446 0 0 0 -6.167 0.535
                arcToRelative(
                    a = 6.446f,
                    b = 6.446f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -6.167f,
                    dy1 = 0.535f,
                )
                // A 6.564 6.564 0 0 0 3.77 3.959
                arcTo(
                    horizontalEllipseRadius = 6.564f,
                    verticalEllipseRadius = 6.564f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 3.77f,
                    y1 = 3.959f,
                )
                // a 6.643 6.643 0 0 0 -0.655 1.83
                arcToRelative(
                    a = 6.643f,
                    b = 6.643f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.655f,
                    dy1 = 1.83f,
                )
                // a 4.216 4.216 0 0 1 -0.354 -2.318
                arcToRelative(
                    a = 4.216f,
                    b = 4.216f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.354f,
                    dy1 = -2.318f,
                )
                // a 4.23 4.23 0 0 1 0.519 -1.546
                arcToRelative(
                    a = 4.23f,
                    b = 4.23f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.519f,
                    dy1 = -1.546f,
                )
                // a 4.13 4.13 0 0 1 0.814 -0.99
                arcToRelative(
                    a = 4.13f,
                    b = 4.13f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.814f,
                    dy1 = -0.99f,
                )
                // a 6.576 6.576 0 0 0 -2.02 2.5
                arcToRelative(
                    a = 6.576f,
                    b = 6.576f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.02f,
                    dy1 = 2.5f,
                )
                // a 6.66 6.66 0 0 0 0.352 6.252
                arcToRelative(
                    a = 6.66f,
                    b = 6.66f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.352f,
                    dy1 = 6.252f,
                )
                // a 6.6 6.6 0 0 0 1.239 1.488
                arcToRelative(
                    a = 6.6f,
                    b = 6.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.239f,
                    dy1 = 1.488f,
                )
                // a 4.095 4.095 0 0 1 -2.16 -0.85
                arcToRelative(
                    a = 4.095f,
                    b = 4.095f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.16f,
                    dy1 = -0.85f,
                )
                // A 4.17 4.17 0 0 1 0.442 9.099
                arcTo(
                    horizontalEllipseRadius = 4.17f,
                    verticalEllipseRadius = 4.17f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 0.442f,
                    y1 = 9.099f,
                )
                // A 4.144 4.144 0 0 1 0 7.89
                arcTo(
                    horizontalEllipseRadius = 4.144f,
                    verticalEllipseRadius = 4.144f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 0.0f,
                    y1 = 7.89f,
                )
                // a 6.64 6.64 0 0 0 1.13 3.02
                arcToRelative(
                    a = 6.64f,
                    b = 6.64f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.13f,
                    dy1 = 3.02f,
                )
                // a 6.538 6.538 0 0 0 2.41 2.116
                arcToRelative(
                    a = 6.538f,
                    b = 6.538f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.41f,
                    dy1 = 2.116f,
                )
                // a 6.45 6.45 0 0 0 5.009 0.361
                arcToRelative(
                    a = 6.45f,
                    b = 6.45f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 5.009f,
                    dy1 = 0.361f,
                )
                // a 4.11 4.11 0 0 1 -3.388 1.786
                arcToRelative(
                    a = 4.11f,
                    b = 4.11f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.388f,
                    dy1 = 1.786f,
                )
                // a 4.04 4.04 0 0 1 -1.256 -0.218
                arcToRelative(
                    a = 4.04f,
                    b = 4.04f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.256f,
                    dy1 = -0.218f,
                )
                // a 6.446 6.446 0 0 0 6.166 -0.535
                arcToRelative(
                    a = 6.446f,
                    b = 6.446f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 6.166f,
                    dy1 = -0.535f,
                )
                // a 6.564 6.564 0 0 0 2.159 -2.38
                arcToRelative(
                    a = 6.564f,
                    b = 6.564f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.159f,
                    dy1 = -2.38f,
                )
                // a 6.643 6.643 0 0 0 0.654 -1.83
                arcToRelative(
                    a = 6.643f,
                    b = 6.643f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.654f,
                    dy1 = -1.83f,
                )
                // a 4.223 4.223 0 0 1 -0.164 3.865
                arcToRelative(
                    a = 4.223f,
                    b = 4.223f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.164f,
                    dy1 = 3.865f,
                )
                // a 4.132 4.132 0 0 1 -0.814 0.991
                arcToRelative(
                    a = 4.132f,
                    b = 4.132f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.814f,
                    dy1 = 0.991f,
                )
                // a 6.576 6.576 0 0 0 2.021 -2.5
                arcToRelative(
                    a = 6.576f,
                    b = 6.576f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.021f,
                    dy1 = -2.5f,
                )
                // a 6.66 6.66 0 0 0 -0.352 -6.253
                arcToRelative(
                    a = 6.66f,
                    b = 6.66f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.352f,
                    dy1 = -6.253f,
                )
                // a 6.571 6.571 0 0 0 -1.238 -1.487
                arcToRelative(
                    a = 6.571f,
                    b = 6.571f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.238f,
                    dy1 = -1.487f,
                )
                // a 4.088 4.088 0 0 1 2.158 0.849
                arcToRelative(
                    a = 4.088f,
                    b = 4.088f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.158f,
                    dy1 = 0.849f,
                )
                // c 0.43 0.334 0.792 0.752 1.063 1.227
                curveToRelative(
                    dx1 = 0.43f,
                    dy1 = 0.334f,
                    dx2 = 0.792f,
                    dy2 = 0.752f,
                    dx3 = 1.063f,
                    dy3 = 1.227f,
                )
                // c 0.221 0.387 0.354 0.796 0.442 1.21
                curveToRelative(
                    dx1 = 0.221f,
                    dy1 = 0.387f,
                    dx2 = 0.354f,
                    dy2 = 0.796f,
                    dx3 = 0.442f,
                    dy3 = 1.21f,
                )
                // a 6.64 6.64 0 0 0 -1.13 -3.021
                arcToRelative(
                    a = 6.64f,
                    b = 6.64f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.13f,
                    dy1 = -3.021f,
                )
                // a 6.537 6.537 0 0 0 -2.41 -2.116
                arcToRelative(
                    a = 6.537f,
                    b = 6.537f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.41f,
                    dy1 = -2.116f,
                )
                // a 6.45 6.45 0 0 0 -5.01 -0.36z
                arcToRelative(
                    a = 6.45f,
                    b = 6.45f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -5.01f,
                    dy1 = -0.36f,
                )
                close()
                // M 11 8
                moveTo(x = 11.0f, y = 8.0f)
                // a 3 3 0 1 1 -6 0
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -6.0f,
                    dy1 = 0.0f,
                )
                // a 3 3 0 0 1 6 0z
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 6.0f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic2365 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2365: ImageVector? = null
