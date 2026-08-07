package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2527: ImageVector
    get() {
        val current = _ic2527
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2527",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M4.75 13.5 a1.25 1.25 0 1 1 0 2.5 1.25 1.25 0 0 1 0 -2.5Z m10 -2 a1.25 1.25 0 1 1 0 2.5 1.25 1.25 0 0 1 0 -2.5Z m-3.872 -2 c.486 0 .903 .18 1.247 .524 .344 .345 .513 .769 .513 1.254 0 .494 -.165 .911 -.513 1.255 a1.692 1.692 0 0 1 -1.247 .513 c-.493 0 -.91 -.169 -1.266 -.524 a.566 .566 0 0 1 -.168 -.406 c0 -.149 .05 -.278 .157 -.386 a.557 .557 0 0 1 .405 -.157 c.138 0 .264 .05 .394 .157 a.609 .609 0 0 0 .463 .207 .63 .63 0 0 0 .455 -.2 .63 .63 0 0 0 0 -.918 .64 .64 0 0 0 -.455 -.187 H.585 a.608 .608 0 0 1 -.417 -.168 C.061 10.376 0 10.25 0 10.07 c0 -.156 .06 -.287 .164 -.402 A.615 .615 0 0 1 .581 9.5 h10.297Z m-9.378 2 a.5 .5 0 1 1 0 1 .5 .5 0 0 1 0 -1Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.75 13.5
                moveTo(x = 4.75f, y = 13.5f)
                // a 1.25 1.25 0 1 1 0 2.5
                arcToRelative(
                    a = 1.25f,
                    b = 1.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 2.5f,
                )
                // a 1.25 1.25 0 0 1 0 -2.5z
                arcToRelative(
                    a = 1.25f,
                    b = 1.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -2.5f,
                )
                close()
                // m 10 -2
                moveToRelative(dx = 10.0f, dy = -2.0f)
                // a 1.25 1.25 0 1 1 0 2.5
                arcToRelative(
                    a = 1.25f,
                    b = 1.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 2.5f,
                )
                // a 1.25 1.25 0 0 1 0 -2.5z
                arcToRelative(
                    a = 1.25f,
                    b = 1.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -2.5f,
                )
                close()
                // m -3.872 -2
                moveToRelative(dx = -3.872f, dy = -2.0f)
                // c 0.486 0 0.903 0.18 1.247 0.524
                curveToRelative(
                    dx1 = 0.486f,
                    dy1 = 0.0f,
                    dx2 = 0.903f,
                    dy2 = 0.18f,
                    dx3 = 1.247f,
                    dy3 = 0.524f,
                )
                // c 0.344 0.345 0.513 0.769 0.513 1.254
                curveToRelative(
                    dx1 = 0.344f,
                    dy1 = 0.345f,
                    dx2 = 0.513f,
                    dy2 = 0.769f,
                    dx3 = 0.513f,
                    dy3 = 1.254f,
                )
                // c 0 0.494 -0.165 0.911 -0.513 1.255
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.494f,
                    dx2 = -0.165f,
                    dy2 = 0.911f,
                    dx3 = -0.513f,
                    dy3 = 1.255f,
                )
                // a 1.692 1.692 0 0 1 -1.247 0.513
                arcToRelative(
                    a = 1.692f,
                    b = 1.692f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.247f,
                    dy1 = 0.513f,
                )
                // c -0.493 0 -0.91 -0.169 -1.266 -0.524
                curveToRelative(
                    dx1 = -0.493f,
                    dy1 = 0.0f,
                    dx2 = -0.91f,
                    dy2 = -0.169f,
                    dx3 = -1.266f,
                    dy3 = -0.524f,
                )
                // a 0.566 0.566 0 0 1 -0.168 -0.406
                arcToRelative(
                    a = 0.566f,
                    b = 0.566f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.168f,
                    dy1 = -0.406f,
                )
                // c 0 -0.149 0.05 -0.278 0.157 -0.386
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.149f,
                    dx2 = 0.05f,
                    dy2 = -0.278f,
                    dx3 = 0.157f,
                    dy3 = -0.386f,
                )
                // a 0.557 0.557 0 0 1 0.405 -0.157
                arcToRelative(
                    a = 0.557f,
                    b = 0.557f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.405f,
                    dy1 = -0.157f,
                )
                // c 0.138 0 0.264 0.05 0.394 0.157
                curveToRelative(
                    dx1 = 0.138f,
                    dy1 = 0.0f,
                    dx2 = 0.264f,
                    dy2 = 0.05f,
                    dx3 = 0.394f,
                    dy3 = 0.157f,
                )
                // a 0.609 0.609 0 0 0 0.463 0.207
                arcToRelative(
                    a = 0.609f,
                    b = 0.609f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.463f,
                    dy1 = 0.207f,
                )
                // a 0.63 0.63 0 0 0 0.455 -0.2
                arcToRelative(
                    a = 0.63f,
                    b = 0.63f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.455f,
                    dy1 = -0.2f,
                )
                // a 0.63 0.63 0 0 0 0 -0.918
                arcToRelative(
                    a = 0.63f,
                    b = 0.63f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.918f,
                )
                // a 0.64 0.64 0 0 0 -0.455 -0.187
                arcToRelative(
                    a = 0.64f,
                    b = 0.64f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.455f,
                    dy1 = -0.187f,
                )
                // H 0.585
                horizontalLineTo(x = 0.585f)
                // a 0.608 0.608 0 0 1 -0.417 -0.168
                arcToRelative(
                    a = 0.608f,
                    b = 0.608f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.417f,
                    dy1 = -0.168f,
                )
                // C 0.061 10.376 0 10.25 0 10.07
                curveTo(
                    x1 = 0.061f,
                    y1 = 10.376f,
                    x2 = 0.0f,
                    y2 = 10.25f,
                    x3 = 0.0f,
                    y3 = 10.07f,
                )
                // c 0 -0.156 0.06 -0.287 0.164 -0.402
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.156f,
                    dx2 = 0.06f,
                    dy2 = -0.287f,
                    dx3 = 0.164f,
                    dy3 = -0.402f,
                )
                // A 0.615 0.615 0 0 1 0.581 9.5
                arcTo(
                    horizontalEllipseRadius = 0.615f,
                    verticalEllipseRadius = 0.615f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 0.581f,
                    y1 = 9.5f,
                )
                // h 10.297z
                horizontalLineToRelative(dx = 10.297f)
                close()
                // m -9.378 2
                moveToRelative(dx = -9.378f, dy = 2.0f)
                // a 0.5 0.5 0 1 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // a 0.5 0.5 0 0 1 0 -1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                close()
            }
            // M7 11.5 a.5 .5 0 1 1 0 1 .5 .5 0 0 1 0 -1Z m7.5 -3 a.5 .5 0 1 1 0 1 .5 .5 0 0 1 0 -1Z m-2 -1 a.5 .5 0 0 1 0 1 h-10 a.5 .5 0 0 1 0 -1 h10Z M1 5.5 a.5 .5 0 1 1 0 1 .5 .5 0 0 1 0 -1Z m13.238 -2.546 c.494 0 .9 .169 1.247 .513 .348 .344 .513 .761 .513 1.255 0 .485 -.169 .91 -.513 1.254 a1.709 1.709 0 0 1 -1.247 .524 H3.941 a.615 .615 0 0 1 -.417 -.168 .585 .585 0 0 1 -.164 -.402 c0 -.18 .061 -.306 .168 -.394 a.607 .607 0 0 1 .417 -.168 h10.278 a.64 .64 0 0 0 .455 -.187 .6 .6 0 0 0 .199 -.455 c0 -.18 -.07 -.337 -.2 -.463 a.632 .632 0 0 0 -.454 -.2 c-.18 0 -.337 .07 -.463 .208 a.603 .603 0 0 1 -.394 .156 .557 .557 0 0 1 -.405 -.156 .567 .567 0 0 1 .012 -.792 1.712 1.712 0 0 1 1.265 -.525Z M8.5 3 a.5 .5 0 1 1 0 1 .5 .5 0 0 1 0 -1Z M3.75 1 a1.25 1.25 0 1 1 0 2.5 1.25 1.25 0 0 1 0 -2.5Z m7 -1 a1.25 1.25 0 1 1 0 2.5 1.25 1.25 0 0 1 0 -2.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7 11.5
                moveTo(x = 7.0f, y = 11.5f)
                // a 0.5 0.5 0 1 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // a 0.5 0.5 0 0 1 0 -1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                close()
                // m 7.5 -3
                moveToRelative(dx = 7.5f, dy = -3.0f)
                // a 0.5 0.5 0 1 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // a 0.5 0.5 0 0 1 0 -1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                close()
                // m -2 -1
                moveToRelative(dx = -2.0f, dy = -1.0f)
                // a 0.5 0.5 0 0 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // h -10
                horizontalLineToRelative(dx = -10.0f)
                // a 0.5 0.5 0 0 1 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h 10z
                horizontalLineToRelative(dx = 10.0f)
                close()
                // M 1 5.5
                moveTo(x = 1.0f, y = 5.5f)
                // a 0.5 0.5 0 1 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // a 0.5 0.5 0 0 1 0 -1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                close()
                // m 13.238 -2.546
                moveToRelative(dx = 13.238f, dy = -2.546f)
                // c 0.494 0 0.9 0.169 1.247 0.513
                curveToRelative(
                    dx1 = 0.494f,
                    dy1 = 0.0f,
                    dx2 = 0.9f,
                    dy2 = 0.169f,
                    dx3 = 1.247f,
                    dy3 = 0.513f,
                )
                // c 0.348 0.344 0.513 0.761 0.513 1.255
                curveToRelative(
                    dx1 = 0.348f,
                    dy1 = 0.344f,
                    dx2 = 0.513f,
                    dy2 = 0.761f,
                    dx3 = 0.513f,
                    dy3 = 1.255f,
                )
                // c 0 0.485 -0.169 0.91 -0.513 1.254
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.485f,
                    dx2 = -0.169f,
                    dy2 = 0.91f,
                    dx3 = -0.513f,
                    dy3 = 1.254f,
                )
                // a 1.709 1.709 0 0 1 -1.247 0.524
                arcToRelative(
                    a = 1.709f,
                    b = 1.709f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.247f,
                    dy1 = 0.524f,
                )
                // H 3.941
                horizontalLineTo(x = 3.941f)
                // a 0.615 0.615 0 0 1 -0.417 -0.168
                arcToRelative(
                    a = 0.615f,
                    b = 0.615f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.417f,
                    dy1 = -0.168f,
                )
                // a 0.585 0.585 0 0 1 -0.164 -0.402
                arcToRelative(
                    a = 0.585f,
                    b = 0.585f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.164f,
                    dy1 = -0.402f,
                )
                // c 0 -0.18 0.061 -0.306 0.168 -0.394
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.18f,
                    dx2 = 0.061f,
                    dy2 = -0.306f,
                    dx3 = 0.168f,
                    dy3 = -0.394f,
                )
                // a 0.607 0.607 0 0 1 0.417 -0.168
                arcToRelative(
                    a = 0.607f,
                    b = 0.607f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.417f,
                    dy1 = -0.168f,
                )
                // h 10.278
                horizontalLineToRelative(dx = 10.278f)
                // a 0.64 0.64 0 0 0 0.455 -0.187
                arcToRelative(
                    a = 0.64f,
                    b = 0.64f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.455f,
                    dy1 = -0.187f,
                )
                // a 0.6 0.6 0 0 0 0.199 -0.455
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.199f,
                    dy1 = -0.455f,
                )
                // c 0 -0.18 -0.07 -0.337 -0.2 -0.463
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.18f,
                    dx2 = -0.07f,
                    dy2 = -0.337f,
                    dx3 = -0.2f,
                    dy3 = -0.463f,
                )
                // a 0.632 0.632 0 0 0 -0.454 -0.2
                arcToRelative(
                    a = 0.632f,
                    b = 0.632f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.454f,
                    dy1 = -0.2f,
                )
                // c -0.18 0 -0.337 0.07 -0.463 0.208
                curveToRelative(
                    dx1 = -0.18f,
                    dy1 = 0.0f,
                    dx2 = -0.337f,
                    dy2 = 0.07f,
                    dx3 = -0.463f,
                    dy3 = 0.208f,
                )
                // a 0.603 0.603 0 0 1 -0.394 0.156
                arcToRelative(
                    a = 0.603f,
                    b = 0.603f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.394f,
                    dy1 = 0.156f,
                )
                // a 0.557 0.557 0 0 1 -0.405 -0.156
                arcToRelative(
                    a = 0.557f,
                    b = 0.557f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.405f,
                    dy1 = -0.156f,
                )
                // a 0.567 0.567 0 0 1 0.012 -0.792
                arcToRelative(
                    a = 0.567f,
                    b = 0.567f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.012f,
                    dy1 = -0.792f,
                )
                // a 1.712 1.712 0 0 1 1.265 -0.525z
                arcToRelative(
                    a = 1.712f,
                    b = 1.712f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.265f,
                    dy1 = -0.525f,
                )
                close()
                // M 8.5 3
                moveTo(x = 8.5f, y = 3.0f)
                // a 0.5 0.5 0 1 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // a 0.5 0.5 0 0 1 0 -1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                close()
                // M 3.75 1
                moveTo(x = 3.75f, y = 1.0f)
                // a 1.25 1.25 0 1 1 0 2.5
                arcToRelative(
                    a = 1.25f,
                    b = 1.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 2.5f,
                )
                // a 1.25 1.25 0 0 1 0 -2.5z
                arcToRelative(
                    a = 1.25f,
                    b = 1.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -2.5f,
                )
                close()
                // m 7 -1
                moveToRelative(dx = 7.0f, dy = -1.0f)
                // a 1.25 1.25 0 1 1 0 2.5
                arcToRelative(
                    a = 1.25f,
                    b = 1.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 2.5f,
                )
                // a 1.25 1.25 0 0 1 0 -2.5z
                arcToRelative(
                    a = 1.25f,
                    b = 1.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -2.5f,
                )
                close()
            }
        }.build().also { _ic2527 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2527: ImageVector? = null
