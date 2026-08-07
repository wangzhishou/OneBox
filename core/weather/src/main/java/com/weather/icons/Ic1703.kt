package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1703: ImageVector
    get() {
        val current = _ic1703
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1703",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.85 12.017 H8 c-.1 0 -.15 .05 -.15 .1 -.3 .75 -.7 1.8 -.85 2.3 -.15 .35 .15 .65 .5 .6 .2 -.05 .3 -.15 .35 -.35 .45 -1.25 .45 -1.05 .9 -2.3 .05 -.1 .1 -.2 .1 -.35Z m-2.6 0 H5.4 c-.1 0 -.15 .05 -.15 .1 -.3 .75 -1.05 2.9 -1.2 3.4 -.15 .35 .15 .65 .5 .6 .2 -.05 .3 -.15 .35 -.35 .45 -1.25 .8 -2.15 1.25 -3.4 0 -.1 .05 -.2 .1 -.35Z m-2.4 0 H3 c-.05 0 -.1 .05 -.1 .05 -.05 .25 -.15 .45 -.25 .7 -.25 .65 -.45 1.25 -.7 1.9 -.1 .3 .05 .5 .3 .65 .2 .05 .45 -.05 .5 -.3 .25 -.65 .5 -1.3 .7 -1.9 .15 -.4 .25 -.75 .4 -1.1Z m-1.4 -6.15 c.4 -.3 .9 -.45 1.4 -.45 .4 0 .85 .1 1.2 .3 l.6 .35 .1 -.7 c.35 -2.15 2.15 -3.7 4.35 -3.7 2.45 0 4.4 2 4.4 4.4 0 .8 -.2 1.55 -.65 2.25 .3 .05 .6 .2 .85 .35 .4 -.75 .65 -1.65 .65 -2.55 0 -2.9 -2.4 -5.3 -5.3 -5.3 -2.45 0 -4.5 1.6 -5.1 3.9 -.4 -.25 -.8 -.35 -1.15 -.35 -.7 0 -1.35 .2 -1.9 .65 -.95 .65 -1.55 1.7 -1.55 2.85 0 1.4 .85 2.7 2.2 3.2 .3 .15 .7 .3 1.05 .3 h6 c.05 -.3 .05 -.65 .15 -.95 h-6.1 c-.2 0 -.4 -.05 -.65 -.15 l-.05 -.05 c-.95 -.4 -1.65 -1.3 -1.65 -2.35 0 -.75 .45 -1.55 1.15 -2Z m4.9 10.2 a.45 .45 0 1 1 -.9 0 .45 .45 0 0 1 .9 0Z m5.26 -5.646 c-.02 -.176 .16 -.327 .39 -.327 s.41 .151 .39 .327 l-.177 1.548 h-.426 l-.176 -1.548Z m.705 2.173 a.313 .313 0 1 1 -.625 0 .313 .313 0 0 1 .625 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.85 12.017
                moveTo(x = 8.85f, y = 12.017f)
                // H 8
                horizontalLineTo(x = 8.0f)
                // c -0.1 0 -0.15 0.05 -0.15 0.1
                curveToRelative(
                    dx1 = -0.1f,
                    dy1 = 0.0f,
                    dx2 = -0.15f,
                    dy2 = 0.05f,
                    dx3 = -0.15f,
                    dy3 = 0.1f,
                )
                // c -0.3 0.75 -0.7 1.8 -0.85 2.3
                curveToRelative(
                    dx1 = -0.3f,
                    dy1 = 0.75f,
                    dx2 = -0.7f,
                    dy2 = 1.8f,
                    dx3 = -0.85f,
                    dy3 = 2.3f,
                )
                // c -0.15 0.35 0.15 0.65 0.5 0.6
                curveToRelative(
                    dx1 = -0.15f,
                    dy1 = 0.35f,
                    dx2 = 0.15f,
                    dy2 = 0.65f,
                    dx3 = 0.5f,
                    dy3 = 0.6f,
                )
                // c 0.2 -0.05 0.3 -0.15 0.35 -0.35
                curveToRelative(
                    dx1 = 0.2f,
                    dy1 = -0.05f,
                    dx2 = 0.3f,
                    dy2 = -0.15f,
                    dx3 = 0.35f,
                    dy3 = -0.35f,
                )
                // c 0.45 -1.25 0.45 -1.05 0.9 -2.3
                curveToRelative(
                    dx1 = 0.45f,
                    dy1 = -1.25f,
                    dx2 = 0.45f,
                    dy2 = -1.05f,
                    dx3 = 0.9f,
                    dy3 = -2.3f,
                )
                // c 0.05 -0.1 0.1 -0.2 0.1 -0.35z
                curveToRelative(
                    dx1 = 0.05f,
                    dy1 = -0.1f,
                    dx2 = 0.1f,
                    dy2 = -0.2f,
                    dx3 = 0.1f,
                    dy3 = -0.35f,
                )
                close()
                // m -2.6 0
                moveToRelative(dx = -2.6f, dy = 0.0f)
                // H 5.4
                horizontalLineTo(x = 5.4f)
                // c -0.1 0 -0.15 0.05 -0.15 0.1
                curveToRelative(
                    dx1 = -0.1f,
                    dy1 = 0.0f,
                    dx2 = -0.15f,
                    dy2 = 0.05f,
                    dx3 = -0.15f,
                    dy3 = 0.1f,
                )
                // c -0.3 0.75 -1.05 2.9 -1.2 3.4
                curveToRelative(
                    dx1 = -0.3f,
                    dy1 = 0.75f,
                    dx2 = -1.05f,
                    dy2 = 2.9f,
                    dx3 = -1.2f,
                    dy3 = 3.4f,
                )
                // c -0.15 0.35 0.15 0.65 0.5 0.6
                curveToRelative(
                    dx1 = -0.15f,
                    dy1 = 0.35f,
                    dx2 = 0.15f,
                    dy2 = 0.65f,
                    dx3 = 0.5f,
                    dy3 = 0.6f,
                )
                // c 0.2 -0.05 0.3 -0.15 0.35 -0.35
                curveToRelative(
                    dx1 = 0.2f,
                    dy1 = -0.05f,
                    dx2 = 0.3f,
                    dy2 = -0.15f,
                    dx3 = 0.35f,
                    dy3 = -0.35f,
                )
                // c 0.45 -1.25 0.8 -2.15 1.25 -3.4
                curveToRelative(
                    dx1 = 0.45f,
                    dy1 = -1.25f,
                    dx2 = 0.8f,
                    dy2 = -2.15f,
                    dx3 = 1.25f,
                    dy3 = -3.4f,
                )
                // c 0 -0.1 0.05 -0.2 0.1 -0.35z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.1f,
                    dx2 = 0.05f,
                    dy2 = -0.2f,
                    dx3 = 0.1f,
                    dy3 = -0.35f,
                )
                close()
                // m -2.4 0
                moveToRelative(dx = -2.4f, dy = 0.0f)
                // H 3
                horizontalLineTo(x = 3.0f)
                // c -0.05 0 -0.1 0.05 -0.1 0.05
                curveToRelative(
                    dx1 = -0.05f,
                    dy1 = 0.0f,
                    dx2 = -0.1f,
                    dy2 = 0.05f,
                    dx3 = -0.1f,
                    dy3 = 0.05f,
                )
                // c -0.05 0.25 -0.15 0.45 -0.25 0.7
                curveToRelative(
                    dx1 = -0.05f,
                    dy1 = 0.25f,
                    dx2 = -0.15f,
                    dy2 = 0.45f,
                    dx3 = -0.25f,
                    dy3 = 0.7f,
                )
                // c -0.25 0.65 -0.45 1.25 -0.7 1.9
                curveToRelative(
                    dx1 = -0.25f,
                    dy1 = 0.65f,
                    dx2 = -0.45f,
                    dy2 = 1.25f,
                    dx3 = -0.7f,
                    dy3 = 1.9f,
                )
                // c -0.1 0.3 0.05 0.5 0.3 0.65
                curveToRelative(
                    dx1 = -0.1f,
                    dy1 = 0.3f,
                    dx2 = 0.05f,
                    dy2 = 0.5f,
                    dx3 = 0.3f,
                    dy3 = 0.65f,
                )
                // c 0.2 0.05 0.45 -0.05 0.5 -0.3
                curveToRelative(
                    dx1 = 0.2f,
                    dy1 = 0.05f,
                    dx2 = 0.45f,
                    dy2 = -0.05f,
                    dx3 = 0.5f,
                    dy3 = -0.3f,
                )
                // c 0.25 -0.65 0.5 -1.3 0.7 -1.9
                curveToRelative(
                    dx1 = 0.25f,
                    dy1 = -0.65f,
                    dx2 = 0.5f,
                    dy2 = -1.3f,
                    dx3 = 0.7f,
                    dy3 = -1.9f,
                )
                // c 0.15 -0.4 0.25 -0.75 0.4 -1.1z
                curveToRelative(
                    dx1 = 0.15f,
                    dy1 = -0.4f,
                    dx2 = 0.25f,
                    dy2 = -0.75f,
                    dx3 = 0.4f,
                    dy3 = -1.1f,
                )
                close()
                // m -1.4 -6.15
                moveToRelative(dx = -1.4f, dy = -6.15f)
                // c 0.4 -0.3 0.9 -0.45 1.4 -0.45
                curveToRelative(
                    dx1 = 0.4f,
                    dy1 = -0.3f,
                    dx2 = 0.9f,
                    dy2 = -0.45f,
                    dx3 = 1.4f,
                    dy3 = -0.45f,
                )
                // c 0.4 0 0.85 0.1 1.2 0.3
                curveToRelative(
                    dx1 = 0.4f,
                    dy1 = 0.0f,
                    dx2 = 0.85f,
                    dy2 = 0.1f,
                    dx3 = 1.2f,
                    dy3 = 0.3f,
                )
                // l 0.6 0.35
                lineToRelative(dx = 0.6f, dy = 0.35f)
                // l 0.1 -0.7
                lineToRelative(dx = 0.1f, dy = -0.7f)
                // c 0.35 -2.15 2.15 -3.7 4.35 -3.7
                curveToRelative(
                    dx1 = 0.35f,
                    dy1 = -2.15f,
                    dx2 = 2.15f,
                    dy2 = -3.7f,
                    dx3 = 4.35f,
                    dy3 = -3.7f,
                )
                // c 2.45 0 4.4 2 4.4 4.4
                curveToRelative(
                    dx1 = 2.45f,
                    dy1 = 0.0f,
                    dx2 = 4.4f,
                    dy2 = 2.0f,
                    dx3 = 4.4f,
                    dy3 = 4.4f,
                )
                // c 0 0.8 -0.2 1.55 -0.65 2.25
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.8f,
                    dx2 = -0.2f,
                    dy2 = 1.55f,
                    dx3 = -0.65f,
                    dy3 = 2.25f,
                )
                // c 0.3 0.05 0.6 0.2 0.85 0.35
                curveToRelative(
                    dx1 = 0.3f,
                    dy1 = 0.05f,
                    dx2 = 0.6f,
                    dy2 = 0.2f,
                    dx3 = 0.85f,
                    dy3 = 0.35f,
                )
                // c 0.4 -0.75 0.65 -1.65 0.65 -2.55
                curveToRelative(
                    dx1 = 0.4f,
                    dy1 = -0.75f,
                    dx2 = 0.65f,
                    dy2 = -1.65f,
                    dx3 = 0.65f,
                    dy3 = -2.55f,
                )
                // c 0 -2.9 -2.4 -5.3 -5.3 -5.3
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -2.9f,
                    dx2 = -2.4f,
                    dy2 = -5.3f,
                    dx3 = -5.3f,
                    dy3 = -5.3f,
                )
                // c -2.45 0 -4.5 1.6 -5.1 3.9
                curveToRelative(
                    dx1 = -2.45f,
                    dy1 = 0.0f,
                    dx2 = -4.5f,
                    dy2 = 1.6f,
                    dx3 = -5.1f,
                    dy3 = 3.9f,
                )
                // c -0.4 -0.25 -0.8 -0.35 -1.15 -0.35
                curveToRelative(
                    dx1 = -0.4f,
                    dy1 = -0.25f,
                    dx2 = -0.8f,
                    dy2 = -0.35f,
                    dx3 = -1.15f,
                    dy3 = -0.35f,
                )
                // c -0.7 0 -1.35 0.2 -1.9 0.65
                curveToRelative(
                    dx1 = -0.7f,
                    dy1 = 0.0f,
                    dx2 = -1.35f,
                    dy2 = 0.2f,
                    dx3 = -1.9f,
                    dy3 = 0.65f,
                )
                // c -0.95 0.65 -1.55 1.7 -1.55 2.85
                curveToRelative(
                    dx1 = -0.95f,
                    dy1 = 0.65f,
                    dx2 = -1.55f,
                    dy2 = 1.7f,
                    dx3 = -1.55f,
                    dy3 = 2.85f,
                )
                // c 0 1.4 0.85 2.7 2.2 3.2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.4f,
                    dx2 = 0.85f,
                    dy2 = 2.7f,
                    dx3 = 2.2f,
                    dy3 = 3.2f,
                )
                // c 0.3 0.15 0.7 0.3 1.05 0.3
                curveToRelative(
                    dx1 = 0.3f,
                    dy1 = 0.15f,
                    dx2 = 0.7f,
                    dy2 = 0.3f,
                    dx3 = 1.05f,
                    dy3 = 0.3f,
                )
                // h 6
                horizontalLineToRelative(dx = 6.0f)
                // c 0.05 -0.3 0.05 -0.65 0.15 -0.95
                curveToRelative(
                    dx1 = 0.05f,
                    dy1 = -0.3f,
                    dx2 = 0.05f,
                    dy2 = -0.65f,
                    dx3 = 0.15f,
                    dy3 = -0.95f,
                )
                // h -6.1
                horizontalLineToRelative(dx = -6.1f)
                // c -0.2 0 -0.4 -0.05 -0.65 -0.15
                curveToRelative(
                    dx1 = -0.2f,
                    dy1 = 0.0f,
                    dx2 = -0.4f,
                    dy2 = -0.05f,
                    dx3 = -0.65f,
                    dy3 = -0.15f,
                )
                // l -0.05 -0.05
                lineToRelative(dx = -0.05f, dy = -0.05f)
                // c -0.95 -0.4 -1.65 -1.3 -1.65 -2.35
                curveToRelative(
                    dx1 = -0.95f,
                    dy1 = -0.4f,
                    dx2 = -1.65f,
                    dy2 = -1.3f,
                    dx3 = -1.65f,
                    dy3 = -2.35f,
                )
                // c 0 -0.75 0.45 -1.55 1.15 -2z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.75f,
                    dx2 = 0.45f,
                    dy2 = -1.55f,
                    dx3 = 1.15f,
                    dy3 = -2.0f,
                )
                close()
                // m 4.9 10.2
                moveToRelative(dx = 4.9f, dy = 10.2f)
                // a 0.45 0.45 0 1 1 -0.9 0
                arcToRelative(
                    a = 0.45f,
                    b = 0.45f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.9f,
                    dy1 = 0.0f,
                )
                // a 0.45 0.45 0 0 1 0.9 0z
                arcToRelative(
                    a = 0.45f,
                    b = 0.45f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.9f,
                    dy1 = 0.0f,
                )
                close()
                // m 5.26 -5.646
                moveToRelative(dx = 5.26f, dy = -5.646f)
                // c -0.02 -0.176 0.16 -0.327 0.39 -0.327
                curveToRelative(
                    dx1 = -0.02f,
                    dy1 = -0.176f,
                    dx2 = 0.16f,
                    dy2 = -0.327f,
                    dx3 = 0.39f,
                    dy3 = -0.327f,
                )
                // s 0.41 0.151 0.39 0.327
                reflectiveCurveToRelative(
                    dx1 = 0.41f,
                    dy1 = 0.151f,
                    dx2 = 0.39f,
                    dy2 = 0.327f,
                )
                // l -0.177 1.548
                lineToRelative(dx = -0.177f, dy = 1.548f)
                // h -0.426
                horizontalLineToRelative(dx = -0.426f)
                // l -0.176 -1.548z
                lineToRelative(dx = -0.176f, dy = -1.548f)
                close()
                // m 0.705 2.173
                moveToRelative(dx = 0.705f, dy = 2.173f)
                // a 0.313 0.313 0 1 1 -0.625 0
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.625f,
                    dy1 = 0.0f,
                )
                // a 0.313 0.313 0 0 1 0.625 0z
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.625f,
                    dy1 = 0.0f,
                )
                close()
            }
            // M10.5 11.5 a2.5 2.5 0 1 0 5 0 2.5 2.5 0 0 0 -5 0Z m4.594 0 a2.094 2.094 0 1 1 -4.188 0 2.094 2.094 0 0 1 4.188 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10.5 11.5
                moveTo(x = 10.5f, y = 11.5f)
                // a 2.5 2.5 0 1 0 5 0
                arcToRelative(
                    a = 2.5f,
                    b = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 5.0f,
                    dy1 = 0.0f,
                )
                // a 2.5 2.5 0 0 0 -5 0z
                arcToRelative(
                    a = 2.5f,
                    b = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -5.0f,
                    dy1 = 0.0f,
                )
                close()
                // m 4.594 0
                moveToRelative(dx = 4.594f, dy = 0.0f)
                // a 2.094 2.094 0 1 1 -4.188 0
                arcToRelative(
                    a = 2.094f,
                    b = 2.094f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -4.188f,
                    dy1 = 0.0f,
                )
                // a 2.094 2.094 0 0 1 4.188 0z
                arcToRelative(
                    a = 2.094f,
                    b = 2.094f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 4.188f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic1703 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1703: ImageVector? = null
