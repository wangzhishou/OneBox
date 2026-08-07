package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Icqweather: ImageVector
    get() {
        val current = _icqweather
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Icqweather",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M14.936 11.53 c-.907 .799 -1.434 1.36 -2.616 1.634 a7.158 7.158 0 0 0 .715 -9.529 A7.27 7.27 0 0 0 8.66 .935 a7.313 7.313 0 0 0 -5.08 .858 A7.212 7.212 0 0 0 .354 5.778 a7.144 7.144 0 0 0 .253 5.104 7.229 7.229 0 0 0 3.604 3.652 c1.612 .74 3.44 .868 5.14 .361 a7.864 7.864 0 0 0 4.344 -.864 c1.126 -.589 1.588 -1.46 2.305 -2.5 h-1.064Z M6.51 14.152 a5.743 5.743 0 0 1 -3.551 -1.69 5.642 5.642 0 0 1 -.514 -7.317 5.727 5.727 0 0 1 3.28 -2.162 5.767 5.767 0 0 1 3.912 .45 5.696 5.696 0 0 1 2.692 2.851 5.628 5.628 0 0 1 .197 3.9 5.682 5.682 0 0 1 -2.39 3.105 7.005 7.005 0 0 1 -1.07 -.146 c-1.302 -.294 -2.437 -1.113 -3.237 -2.056 -.002 0 -.003 -.003 -.004 -.004 a3.2 3.2 0 0 1 -.7 -1.929 2.254 2.254 0 0 1 .548 -1.517 2.473 2.473 0 0 1 1.91 -.89 c.198 0 .396 .023 .589 .07 a1.423 1.423 0 0 1 .24 .07 c.327 .139 .603 .377 .784 .682 a1.48 1.48 0 0 1 -.44 1.98 1.509 1.509 0 0 1 -1.403 .162 .17 .17 0 0 0 -.192 .045 .167 .167 0 0 0 -.017 .195 1.675 1.675 0 0 0 1.426 .8 l.048 -.001 a2.821 2.821 0 0 0 1.203 -.342 A2.747 2.747 0 0 0 11.26 7.99 a2.862 2.862 0 0 0 -.47 -1.585 3.49 3.49 0 0 0 -.072 -.098 c-.02 -.028 -.042 -.055 -.064 -.083 l-.036 -.045 c-.79 -1.03 -2.033 -1.634 -3.45 -1.593 -1.27 .036 -2.417 .53 -3.223 1.382 a4.357 4.357 0 0 0 -.724 4.99 7.827 7.827 0 0 0 3.44 3.205 c-.051 -.004 -.101 -.006 -.151 -.011Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 14.936 11.53
                moveTo(x = 14.936f, y = 11.53f)
                // c -0.907 0.799 -1.434 1.36 -2.616 1.634
                curveToRelative(
                    dx1 = -0.907f,
                    dy1 = 0.799f,
                    dx2 = -1.434f,
                    dy2 = 1.36f,
                    dx3 = -2.616f,
                    dy3 = 1.634f,
                )
                // a 7.158 7.158 0 0 0 0.715 -9.529
                arcToRelative(
                    a = 7.158f,
                    b = 7.158f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.715f,
                    dy1 = -9.529f,
                )
                // A 7.27 7.27 0 0 0 8.66 0.935
                arcTo(
                    horizontalEllipseRadius = 7.27f,
                    verticalEllipseRadius = 7.27f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 8.66f,
                    y1 = 0.935f,
                )
                // a 7.313 7.313 0 0 0 -5.08 0.858
                arcToRelative(
                    a = 7.313f,
                    b = 7.313f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -5.08f,
                    dy1 = 0.858f,
                )
                // A 7.212 7.212 0 0 0 0.354 5.778
                arcTo(
                    horizontalEllipseRadius = 7.212f,
                    verticalEllipseRadius = 7.212f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 0.354f,
                    y1 = 5.778f,
                )
                // a 7.144 7.144 0 0 0 0.253 5.104
                arcToRelative(
                    a = 7.144f,
                    b = 7.144f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.253f,
                    dy1 = 5.104f,
                )
                // a 7.229 7.229 0 0 0 3.604 3.652
                arcToRelative(
                    a = 7.229f,
                    b = 7.229f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 3.604f,
                    dy1 = 3.652f,
                )
                // c 1.612 0.74 3.44 0.868 5.14 0.361
                curveToRelative(
                    dx1 = 1.612f,
                    dy1 = 0.74f,
                    dx2 = 3.44f,
                    dy2 = 0.868f,
                    dx3 = 5.14f,
                    dy3 = 0.361f,
                )
                // a 7.864 7.864 0 0 0 4.344 -0.864
                arcToRelative(
                    a = 7.864f,
                    b = 7.864f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 4.344f,
                    dy1 = -0.864f,
                )
                // c 1.126 -0.589 1.588 -1.46 2.305 -2.5
                curveToRelative(
                    dx1 = 1.126f,
                    dy1 = -0.589f,
                    dx2 = 1.588f,
                    dy2 = -1.46f,
                    dx3 = 2.305f,
                    dy3 = -2.5f,
                )
                // h -1.064z
                horizontalLineToRelative(dx = -1.064f)
                close()
                // M 6.51 14.152
                moveTo(x = 6.51f, y = 14.152f)
                // a 5.743 5.743 0 0 1 -3.551 -1.69
                arcToRelative(
                    a = 5.743f,
                    b = 5.743f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.551f,
                    dy1 = -1.69f,
                )
                // a 5.642 5.642 0 0 1 -0.514 -7.317
                arcToRelative(
                    a = 5.642f,
                    b = 5.642f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.514f,
                    dy1 = -7.317f,
                )
                // a 5.727 5.727 0 0 1 3.28 -2.162
                arcToRelative(
                    a = 5.727f,
                    b = 5.727f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 3.28f,
                    dy1 = -2.162f,
                )
                // a 5.767 5.767 0 0 1 3.912 0.45
                arcToRelative(
                    a = 5.767f,
                    b = 5.767f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 3.912f,
                    dy1 = 0.45f,
                )
                // a 5.696 5.696 0 0 1 2.692 2.851
                arcToRelative(
                    a = 5.696f,
                    b = 5.696f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.692f,
                    dy1 = 2.851f,
                )
                // a 5.628 5.628 0 0 1 0.197 3.9
                arcToRelative(
                    a = 5.628f,
                    b = 5.628f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.197f,
                    dy1 = 3.9f,
                )
                // a 5.682 5.682 0 0 1 -2.39 3.105
                arcToRelative(
                    a = 5.682f,
                    b = 5.682f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.39f,
                    dy1 = 3.105f,
                )
                // a 7.005 7.005 0 0 1 -1.07 -0.146
                arcToRelative(
                    a = 7.005f,
                    b = 7.005f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.07f,
                    dy1 = -0.146f,
                )
                // c -1.302 -0.294 -2.437 -1.113 -3.237 -2.056
                curveToRelative(
                    dx1 = -1.302f,
                    dy1 = -0.294f,
                    dx2 = -2.437f,
                    dy2 = -1.113f,
                    dx3 = -3.237f,
                    dy3 = -2.056f,
                )
                // c -0.002 0 -0.003 -0.003 -0.004 -0.004
                curveToRelative(
                    dx1 = -0.002f,
                    dy1 = 0.0f,
                    dx2 = -0.003f,
                    dy2 = -0.003f,
                    dx3 = -0.004f,
                    dy3 = -0.004f,
                )
                // a 3.2 3.2 0 0 1 -0.7 -1.929
                arcToRelative(
                    a = 3.2f,
                    b = 3.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.7f,
                    dy1 = -1.929f,
                )
                // a 2.254 2.254 0 0 1 0.548 -1.517
                arcToRelative(
                    a = 2.254f,
                    b = 2.254f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.548f,
                    dy1 = -1.517f,
                )
                // a 2.473 2.473 0 0 1 1.91 -0.89
                arcToRelative(
                    a = 2.473f,
                    b = 2.473f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.91f,
                    dy1 = -0.89f,
                )
                // c 0.198 0 0.396 0.023 0.589 0.07
                curveToRelative(
                    dx1 = 0.198f,
                    dy1 = 0.0f,
                    dx2 = 0.396f,
                    dy2 = 0.023f,
                    dx3 = 0.589f,
                    dy3 = 0.07f,
                )
                // a 1.423 1.423 0 0 1 0.24 0.07
                arcToRelative(
                    a = 1.423f,
                    b = 1.423f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.24f,
                    dy1 = 0.07f,
                )
                // c 0.327 0.139 0.603 0.377 0.784 0.682
                curveToRelative(
                    dx1 = 0.327f,
                    dy1 = 0.139f,
                    dx2 = 0.603f,
                    dy2 = 0.377f,
                    dx3 = 0.784f,
                    dy3 = 0.682f,
                )
                // a 1.48 1.48 0 0 1 -0.44 1.98
                arcToRelative(
                    a = 1.48f,
                    b = 1.48f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.44f,
                    dy1 = 1.98f,
                )
                // a 1.509 1.509 0 0 1 -1.403 0.162
                arcToRelative(
                    a = 1.509f,
                    b = 1.509f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.403f,
                    dy1 = 0.162f,
                )
                // a 0.17 0.17 0 0 0 -0.192 0.045
                arcToRelative(
                    a = 0.17f,
                    b = 0.17f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.192f,
                    dy1 = 0.045f,
                )
                // a 0.167 0.167 0 0 0 -0.017 0.195
                arcToRelative(
                    a = 0.167f,
                    b = 0.167f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.017f,
                    dy1 = 0.195f,
                )
                // a 1.675 1.675 0 0 0 1.426 0.8
                arcToRelative(
                    a = 1.675f,
                    b = 1.675f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.426f,
                    dy1 = 0.8f,
                )
                // l 0.048 -0.001
                lineToRelative(dx = 0.048f, dy = -0.001f)
                // a 2.821 2.821 0 0 0 1.203 -0.342
                arcToRelative(
                    a = 2.821f,
                    b = 2.821f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.203f,
                    dy1 = -0.342f,
                )
                // A 2.747 2.747 0 0 0 11.26 7.99
                arcTo(
                    horizontalEllipseRadius = 2.747f,
                    verticalEllipseRadius = 2.747f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 11.26f,
                    y1 = 7.99f,
                )
                // a 2.862 2.862 0 0 0 -0.47 -1.585
                arcToRelative(
                    a = 2.862f,
                    b = 2.862f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.47f,
                    dy1 = -1.585f,
                )
                // a 3.49 3.49 0 0 0 -0.072 -0.098
                arcToRelative(
                    a = 3.49f,
                    b = 3.49f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.072f,
                    dy1 = -0.098f,
                )
                // c -0.02 -0.028 -0.042 -0.055 -0.064 -0.083
                curveToRelative(
                    dx1 = -0.02f,
                    dy1 = -0.028f,
                    dx2 = -0.042f,
                    dy2 = -0.055f,
                    dx3 = -0.064f,
                    dy3 = -0.083f,
                )
                // l -0.036 -0.045
                lineToRelative(dx = -0.036f, dy = -0.045f)
                // c -0.79 -1.03 -2.033 -1.634 -3.45 -1.593
                curveToRelative(
                    dx1 = -0.79f,
                    dy1 = -1.03f,
                    dx2 = -2.033f,
                    dy2 = -1.634f,
                    dx3 = -3.45f,
                    dy3 = -1.593f,
                )
                // c -1.27 0.036 -2.417 0.53 -3.223 1.382
                curveToRelative(
                    dx1 = -1.27f,
                    dy1 = 0.036f,
                    dx2 = -2.417f,
                    dy2 = 0.53f,
                    dx3 = -3.223f,
                    dy3 = 1.382f,
                )
                // a 4.357 4.357 0 0 0 -0.724 4.99
                arcToRelative(
                    a = 4.357f,
                    b = 4.357f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.724f,
                    dy1 = 4.99f,
                )
                // a 7.827 7.827 0 0 0 3.44 3.205
                arcToRelative(
                    a = 7.827f,
                    b = 7.827f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 3.44f,
                    dy1 = 3.205f,
                )
                // c -0.051 -0.004 -0.101 -0.006 -0.151 -0.011z
                curveToRelative(
                    dx1 = -0.051f,
                    dy1 = -0.004f,
                    dx2 = -0.101f,
                    dy2 = -0.006f,
                    dx3 = -0.151f,
                    dy3 = -0.011f,
                )
                close()
            }
        }.build().also { _icqweather = it }
    }

@Suppress("ObjectPropertyName")
private var _icqweather: ImageVector? = null
