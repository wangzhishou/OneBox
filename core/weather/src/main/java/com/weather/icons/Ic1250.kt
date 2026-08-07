package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1250: ImageVector
    get() {
        val current = _ic1250
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1250",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M5.973 10.328 9 4.866 7.073 .142 a.243 .243 0 0 0 -.08 -.105 A.186 .186 0 0 0 6.88 0 a.18 .18 0 0 0 -.113 .042 .247 .247 0 0 0 -.077 .107 L.022 15.635 a.296 .296 0 0 0 .01 .246 .232 .232 0 0 0 .077 .088 c.031 .02 .067 .031 .103 .031 l4.696 -.016 1.76 -3.927 -.695 -1.729Z m10.009 4.348 L13.5 10 10 14.969 l5.82 .031 c.132 0 .22 -.174 .162 -.324Z M12.065 3 l-.46 4.56 1.748 .146 L7.7 15.068 l1.81 -5.23 -1.606 .212 L12.065 3Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 5.973 10.328
                moveTo(x = 5.973f, y = 10.328f)
                // L 9 4.866
                lineTo(x = 9.0f, y = 4.866f)
                // L 7.073 0.142
                lineTo(x = 7.073f, y = 0.142f)
                // a 0.243 0.243 0 0 0 -0.08 -0.105
                arcToRelative(
                    a = 0.243f,
                    b = 0.243f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.08f,
                    dy1 = -0.105f,
                )
                // A 0.186 0.186 0 0 0 6.88 0
                arcTo(
                    horizontalEllipseRadius = 0.186f,
                    verticalEllipseRadius = 0.186f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 6.88f,
                    y1 = 0.0f,
                )
                // a 0.18 0.18 0 0 0 -0.113 0.042
                arcToRelative(
                    a = 0.18f,
                    b = 0.18f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.113f,
                    dy1 = 0.042f,
                )
                // a 0.247 0.247 0 0 0 -0.077 0.107
                arcToRelative(
                    a = 0.247f,
                    b = 0.247f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.077f,
                    dy1 = 0.107f,
                )
                // L 0.022 15.635
                lineTo(x = 0.022f, y = 15.635f)
                // a 0.296 0.296 0 0 0 0.01 0.246
                arcToRelative(
                    a = 0.296f,
                    b = 0.296f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.01f,
                    dy1 = 0.246f,
                )
                // a 0.232 0.232 0 0 0 0.077 0.088
                arcToRelative(
                    a = 0.232f,
                    b = 0.232f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.077f,
                    dy1 = 0.088f,
                )
                // c 0.031 0.02 0.067 0.031 0.103 0.031
                curveToRelative(
                    dx1 = 0.031f,
                    dy1 = 0.02f,
                    dx2 = 0.067f,
                    dy2 = 0.031f,
                    dx3 = 0.103f,
                    dy3 = 0.031f,
                )
                // l 4.696 -0.016
                lineToRelative(dx = 4.696f, dy = -0.016f)
                // l 1.76 -3.927
                lineToRelative(dx = 1.76f, dy = -3.927f)
                // l -0.695 -1.729z
                lineToRelative(dx = -0.695f, dy = -1.729f)
                close()
                // m 10.009 4.348
                moveToRelative(dx = 10.009f, dy = 4.348f)
                // L 13.5 10
                lineTo(x = 13.5f, y = 10.0f)
                // L 10 14.969
                lineTo(x = 10.0f, y = 14.969f)
                // l 5.82 0.031
                lineToRelative(dx = 5.82f, dy = 0.031f)
                // c 0.132 0 0.22 -0.174 0.162 -0.324z
                curveToRelative(
                    dx1 = 0.132f,
                    dy1 = 0.0f,
                    dx2 = 0.22f,
                    dy2 = -0.174f,
                    dx3 = 0.162f,
                    dy3 = -0.324f,
                )
                close()
                // M 12.065 3
                moveTo(x = 12.065f, y = 3.0f)
                // l -0.46 4.56
                lineToRelative(dx = -0.46f, dy = 4.56f)
                // l 1.748 0.146
                lineToRelative(dx = 1.748f, dy = 0.146f)
                // L 7.7 15.068
                lineTo(x = 7.7f, y = 15.068f)
                // l 1.81 -5.23
                lineToRelative(dx = 1.81f, dy = -5.23f)
                // l -1.606 0.212
                lineToRelative(dx = -1.606f, dy = 0.212f)
                // L 12.065 3z
                lineTo(x = 12.065f, y = 3.0f)
                close()
            }
        }.build().also { _ic1250 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1250: ImageVector? = null
