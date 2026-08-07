package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic311Fill: ImageVector
    get() {
        val current = _ic311Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic311Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.943 11.704 a.494 .494 0 0 0 -.157 -.641 c-.205 -.13 -.467 -.053 -.586 .172 l-2.143 4.061 a.494 .494 0 0 0 .157 .641 c.205 .13 .467 .053 .586 -.172 l2.143 -4.061Z M2.8 11.556 c.191 .115 .257 .37 .146 .57 l-1.2 2.165 a.391 .391 0 0 1 -.546 .153 .427 .427 0 0 1 -.146 -.57 l1.2 -2.165 a.39 .39 0 0 1 .546 -.153Z m3 0 c.191 .115 .257 .37 .146 .57 l-1.2 2.165 a.391 .391 0 0 1 -.546 .153 .427 .427 0 0 1 -.146 -.57 l1.2 -2.165 a.39 .39 0 0 1 .546 -.153Z m9 0 c.191 .115 .257 .37 .146 .57 l-1.2 2.165 a.391 .391 0 0 1 -.546 .153 .427 .427 0 0 1 -.146 -.57 l1.2 -2.165 a.39 .39 0 0 1 .546 -.153Z m-2.854 .569 a.427 .427 0 0 0 -.146 -.57 .391 .391 0 0 0 -.546 .154 l-1.2 2.166 a.428 .428 0 0 0 .146 .57 .391 .391 0 0 0 .546 -.154 l1.2 -2.166Z m-.219 -3.908 A4.99 4.99 0 0 1 7.9 10 a4.988 4.988 0 0 1 -3.773 -1.719 3 3 0 1 1 -.586 -5.732 A4.998 4.998 0 0 1 7.9 0 a4.999 4.999 0 0 1 4.38 2.587 3 3 0 1 1 -.553 5.63Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.943 11.704
                moveTo(x = 8.943f, y = 11.704f)
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
                // m -0.219 -3.908
                moveToRelative(dx = -0.219f, dy = -3.908f)
                // A 4.99 4.99 0 0 1 7.9 10
                arcTo(
                    horizontalEllipseRadius = 4.99f,
                    verticalEllipseRadius = 4.99f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.9f,
                    y1 = 10.0f,
                )
                // a 4.988 4.988 0 0 1 -3.773 -1.719
                arcToRelative(
                    a = 4.988f,
                    b = 4.988f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.773f,
                    dy1 = -1.719f,
                )
                // a 3 3 0 1 1 -0.586 -5.732
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.586f,
                    dy1 = -5.732f,
                )
                // A 4.998 4.998 0 0 1 7.9 0
                arcTo(
                    horizontalEllipseRadius = 4.998f,
                    verticalEllipseRadius = 4.998f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.9f,
                    y1 = 0.0f,
                )
                // a 4.999 4.999 0 0 1 4.38 2.587
                arcToRelative(
                    a = 4.999f,
                    b = 4.999f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 4.38f,
                    dy1 = 2.587f,
                )
                // a 3 3 0 1 1 -0.553 5.63z
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.553f,
                    dy1 = 5.63f,
                )
                close()
            }
        }.build().also { _ic311Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic311Fill: ImageVector? = null
