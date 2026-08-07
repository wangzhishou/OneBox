package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2127: ImageVector
    get() {
        val current = _ic2127
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2127",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // m6.989 9.431 4.777 -.91 a.39 .39 0 0 1 .363 .132 l3.074 3.768 c.182 .223 -.048 .472 -.338 .366 l-1.107 -.403 L12.806 15 H16 v1 H0 v-1 h6.42 l1.7 -4.668 -1.107 -.403 c-.29 -.105 -.307 -.444 -.024 -.498Z m4.077 3.038 -.94 -.342 a.5 .5 0 0 0 -.64 .299 l-.342 .94 a.5 .5 0 0 0 .299 .64 l.94 .342 a.5 .5 0 0 0 .64 -.299 l.342 -.94 a.5 .5 0 0 0 -.299 -.64Z M1.6 2.4 h4.8 a.8 .8 0 1 0 -.755 -1.067 .4 .4 0 0 1 -.754 -.266 A1.6 1.6 0 1 1 6.4 3.2 H1.6 a.4 .4 0 0 1 0 -.8Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6.989 9.431
                moveTo(x = 6.989f, y = 9.431f)
                // l 4.777 -0.91
                lineToRelative(dx = 4.777f, dy = -0.91f)
                // a 0.39 0.39 0 0 1 0.363 0.132
                arcToRelative(
                    a = 0.39f,
                    b = 0.39f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.363f,
                    dy1 = 0.132f,
                )
                // l 3.074 3.768
                lineToRelative(dx = 3.074f, dy = 3.768f)
                // c 0.182 0.223 -0.048 0.472 -0.338 0.366
                curveToRelative(
                    dx1 = 0.182f,
                    dy1 = 0.223f,
                    dx2 = -0.048f,
                    dy2 = 0.472f,
                    dx3 = -0.338f,
                    dy3 = 0.366f,
                )
                // l -1.107 -0.403
                lineToRelative(dx = -1.107f, dy = -0.403f)
                // L 12.806 15
                lineTo(x = 12.806f, y = 15.0f)
                // H 16
                horizontalLineTo(x = 16.0f)
                // v 1
                verticalLineToRelative(dy = 1.0f)
                // H 0
                horizontalLineTo(x = 0.0f)
                // v -1
                verticalLineToRelative(dy = -1.0f)
                // h 6.42
                horizontalLineToRelative(dx = 6.42f)
                // l 1.7 -4.668
                lineToRelative(dx = 1.7f, dy = -4.668f)
                // l -1.107 -0.403
                lineToRelative(dx = -1.107f, dy = -0.403f)
                // c -0.29 -0.105 -0.307 -0.444 -0.024 -0.498z
                curveToRelative(
                    dx1 = -0.29f,
                    dy1 = -0.105f,
                    dx2 = -0.307f,
                    dy2 = -0.444f,
                    dx3 = -0.024f,
                    dy3 = -0.498f,
                )
                close()
                // m 4.077 3.038
                moveToRelative(dx = 4.077f, dy = 3.038f)
                // l -0.94 -0.342
                lineToRelative(dx = -0.94f, dy = -0.342f)
                // a 0.5 0.5 0 0 0 -0.64 0.299
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.64f,
                    dy1 = 0.299f,
                )
                // l -0.342 0.94
                lineToRelative(dx = -0.342f, dy = 0.94f)
                // a 0.5 0.5 0 0 0 0.299 0.64
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.299f,
                    dy1 = 0.64f,
                )
                // l 0.94 0.342
                lineToRelative(dx = 0.94f, dy = 0.342f)
                // a 0.5 0.5 0 0 0 0.64 -0.299
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.64f,
                    dy1 = -0.299f,
                )
                // l 0.342 -0.94
                lineToRelative(dx = 0.342f, dy = -0.94f)
                // a 0.5 0.5 0 0 0 -0.299 -0.64z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.299f,
                    dy1 = -0.64f,
                )
                close()
                // M 1.6 2.4
                moveTo(x = 1.6f, y = 2.4f)
                // h 4.8
                horizontalLineToRelative(dx = 4.8f)
                // a 0.8 0.8 0 1 0 -0.755 -1.067
                arcToRelative(
                    a = 0.8f,
                    b = 0.8f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.755f,
                    dy1 = -1.067f,
                )
                // a 0.4 0.4 0 0 1 -0.754 -0.266
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.754f,
                    dy1 = -0.266f,
                )
                // A 1.6 1.6 0 1 1 6.4 3.2
                arcTo(
                    horizontalEllipseRadius = 1.6f,
                    verticalEllipseRadius = 1.6f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 6.4f,
                    y1 = 3.2f,
                )
                // H 1.6
                horizontalLineTo(x = 1.6f)
                // a 0.4 0.4 0 0 1 0 -0.8z
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -0.8f,
                )
                close()
            }
            // M8.863 1.9 a2 2 0 0 1 3.937 .5 c0 1.118 -.993 2 -2 2 H.4 a.4 .4 0 0 1 0 -.8 h10.4 c.593 0 1.2 -.55 1.2 -1.2 a1.2 1.2 0 0 0 -2.362 -.3 .4 .4 0 1 1 -.775 -.2Z M2 5.2 c0 -.22 .18 -.4 .4 -.4 h6.4 a1.6 1.6 0 1 1 -1.509 2.133 .4 .4 0 1 1 .754 -.266 A.8 .8 0 1 0 8.8 5.6 H2.4 a.4 .4 0 0 1 -.4 -.4Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.863 1.9
                moveTo(x = 8.863f, y = 1.9f)
                // a 2 2 0 0 1 3.937 0.5
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 3.937f,
                    dy1 = 0.5f,
                )
                // c 0 1.118 -0.993 2 -2 2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.118f,
                    dx2 = -0.993f,
                    dy2 = 2.0f,
                    dx3 = -2.0f,
                    dy3 = 2.0f,
                )
                // H 0.4
                horizontalLineTo(x = 0.4f)
                // a 0.4 0.4 0 0 1 0 -0.8
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -0.8f,
                )
                // h 10.4
                horizontalLineToRelative(dx = 10.4f)
                // c 0.593 0 1.2 -0.55 1.2 -1.2
                curveToRelative(
                    dx1 = 0.593f,
                    dy1 = 0.0f,
                    dx2 = 1.2f,
                    dy2 = -0.55f,
                    dx3 = 1.2f,
                    dy3 = -1.2f,
                )
                // a 1.2 1.2 0 0 0 -2.362 -0.3
                arcToRelative(
                    a = 1.2f,
                    b = 1.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.362f,
                    dy1 = -0.3f,
                )
                // a 0.4 0.4 0 1 1 -0.775 -0.2z
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.775f,
                    dy1 = -0.2f,
                )
                close()
                // M 2 5.2
                moveTo(x = 2.0f, y = 5.2f)
                // c 0 -0.22 0.18 -0.4 0.4 -0.4
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.22f,
                    dx2 = 0.18f,
                    dy2 = -0.4f,
                    dx3 = 0.4f,
                    dy3 = -0.4f,
                )
                // h 6.4
                horizontalLineToRelative(dx = 6.4f)
                // a 1.6 1.6 0 1 1 -1.509 2.133
                arcToRelative(
                    a = 1.6f,
                    b = 1.6f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.509f,
                    dy1 = 2.133f,
                )
                // a 0.4 0.4 0 1 1 0.754 -0.266
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.754f,
                    dy1 = -0.266f,
                )
                // A 0.8 0.8 0 1 0 8.8 5.6
                arcTo(
                    horizontalEllipseRadius = 0.8f,
                    verticalEllipseRadius = 0.8f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    x1 = 8.8f,
                    y1 = 5.6f,
                )
                // H 2.4
                horizontalLineTo(x = 2.4f)
                // a 0.4 0.4 0 0 1 -0.4 -0.4z
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.4f,
                    dy1 = -0.4f,
                )
                close()
            }
        }.build().also { _ic2127 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2127: ImageVector? = null
