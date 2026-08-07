package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2553: ImageVector
    get() {
        val current = _ic2553
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2553",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M11.762 6.62 H9.524 l1.567 -2.162 .019 -.02 L14 0 H7.043 L2.101 8.917 a.783 .783 0 0 0 .204 1.003 c.131 .097 .3 .155 .448 .155 h2.575 l-.542 5.038 c-.019 .25 .057 .52 .243 .694 a.752 .752 0 0 0 .504 .193 .717 .717 0 0 0 .578 -.29 l6.267 -7.797 a.807 .807 0 0 0 0 -1.003 c-.15 -.213 -.392 -.29 -.616 -.29Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.762 6.62
                moveTo(x = 11.762f, y = 6.62f)
                // H 9.524
                horizontalLineTo(x = 9.524f)
                // l 1.567 -2.162
                lineToRelative(dx = 1.567f, dy = -2.162f)
                // l 0.019 -0.02
                lineToRelative(dx = 0.019f, dy = -0.02f)
                // L 14 0
                lineTo(x = 14.0f, y = 0.0f)
                // H 7.043
                horizontalLineTo(x = 7.043f)
                // L 2.101 8.917
                lineTo(x = 2.101f, y = 8.917f)
                // a 0.783 0.783 0 0 0 0.204 1.003
                arcToRelative(
                    a = 0.783f,
                    b = 0.783f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.204f,
                    dy1 = 1.003f,
                )
                // c 0.131 0.097 0.3 0.155 0.448 0.155
                curveToRelative(
                    dx1 = 0.131f,
                    dy1 = 0.097f,
                    dx2 = 0.3f,
                    dy2 = 0.155f,
                    dx3 = 0.448f,
                    dy3 = 0.155f,
                )
                // h 2.575
                horizontalLineToRelative(dx = 2.575f)
                // l -0.542 5.038
                lineToRelative(dx = -0.542f, dy = 5.038f)
                // c -0.019 0.25 0.057 0.52 0.243 0.694
                curveToRelative(
                    dx1 = -0.019f,
                    dy1 = 0.25f,
                    dx2 = 0.057f,
                    dy2 = 0.52f,
                    dx3 = 0.243f,
                    dy3 = 0.694f,
                )
                // a 0.752 0.752 0 0 0 0.504 0.193
                arcToRelative(
                    a = 0.752f,
                    b = 0.752f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.504f,
                    dy1 = 0.193f,
                )
                // a 0.717 0.717 0 0 0 0.578 -0.29
                arcToRelative(
                    a = 0.717f,
                    b = 0.717f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.578f,
                    dy1 = -0.29f,
                )
                // l 6.267 -7.797
                lineToRelative(dx = 6.267f, dy = -7.797f)
                // a 0.807 0.807 0 0 0 0 -1.003
                arcToRelative(
                    a = 0.807f,
                    b = 0.807f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.003f,
                )
                // c -0.15 -0.213 -0.392 -0.29 -0.616 -0.29z
                curveToRelative(
                    dx1 = -0.15f,
                    dy1 = -0.213f,
                    dx2 = -0.392f,
                    dy2 = -0.29f,
                    dx3 = -0.616f,
                    dy3 = -0.29f,
                )
                close()
            }
        }.build().also { _ic2553 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2553: ImageVector? = null
