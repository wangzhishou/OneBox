package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1705: ImageVector
    get() {
        val current = _ic1705
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1705",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M11.8 9.267 H1.15 c-.35 0 -.6 .2 -.6 .5 0 .25 .25 .5 .6 .5 h9.4 c.35 -.45 .8 -.8 1.25 -1Z M.55 11.617 c0 .3 .25 .5 .6 .5 H9.8 c.05 -.35 .1 -.65 .25 -1 h-8.9 c-.35 0 -.6 .2 -.6 .5Z m.6 1.25 c-.35 0 -.6 .2 -.6 .5 0 .25 .25 .5 .6 .5 h8.9 c-.15 -.3 -.2 -.6 -.25 -1 H1.15Z m.7 -4.15 h3 c2 -.05 2.7 -1.45 2.7 -1.5 .25 -.4 .35 -.9 .35 -1.4 .05 -1.6 -1.25 -2.9 -2.85 -2.9 -1.2 0 -2.3 .75 -2.7 1.9 -.5 -.1 -1.05 0 -1.5 .3 -.55 .4 -.85 1 -.85 1.65 0 .8 .5 1.5 1.2 1.8 .2 .1 .4 .15 .65 .15Z m-.45 -2.8 c.15 -.1 .35 -.15 .55 -.15 .15 0 .35 .05 .45 .15 l.6 .3 .1 -.7 c.15 -.95 .95 -1.65 1.95 -1.65 1.1 0 1.95 .9 1.95 1.95 0 .4 -.1 .7 -.25 1 -.65 .8 -1.6 1 -1.75 1 H1.85 c-.05 0 -.15 0 -.2 -.05 l-.05 -.05 c-.4 -.15 -.65 -.55 -.65 -.95 0 -.35 .15 -.65 .45 -.85Z m7.8 -4.7 c1.35 0 2.5 .75 3.1 1.9 .15 .3 .25 .6 .35 .9 .15 -.05 .25 -.05 .4 -.05 .2 0 .4 .05 .6 .1 .8 .25 1.35 .95 1.35 1.8 a1.9 1.9 0 0 1 -1.9 1.9 H7.95 c-.15 .25 -.45 .55 -.85 .85 .1 .05 .2 .1 .35 .1 h5.7 c1.6 0 2.85 -1.3 2.85 -2.85 0 -1.5 -1.15 -2.7 -2.6 -2.85 -.7 -1.6 -2.25 -2.75 -4.15 -2.75 -1.6 0 -3 .85 -3.8 2.1 .35 .05 .65 .15 .95 .25 .65 -.85 1.65 -1.4 2.8 -1.4Z m3.91 10.204 c-.02 -.176 .16 -.327 .39 -.327 s.41 .151 .39 .327 l-.177 1.548 h-.426 l-.176 -1.548Z m.705 2.173 a.313 .313 0 1 1 -.625 0 .313 .313 0 0 1 .625 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.8 9.267
                moveTo(x = 11.8f, y = 9.267f)
                // H 1.15
                horizontalLineTo(x = 1.15f)
                // c -0.35 0 -0.6 0.2 -0.6 0.5
                curveToRelative(
                    dx1 = -0.35f,
                    dy1 = 0.0f,
                    dx2 = -0.6f,
                    dy2 = 0.2f,
                    dx3 = -0.6f,
                    dy3 = 0.5f,
                )
                // c 0 0.25 0.25 0.5 0.6 0.5
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.25f,
                    dx2 = 0.25f,
                    dy2 = 0.5f,
                    dx3 = 0.6f,
                    dy3 = 0.5f,
                )
                // h 9.4
                horizontalLineToRelative(dx = 9.4f)
                // c 0.35 -0.45 0.8 -0.8 1.25 -1z
                curveToRelative(
                    dx1 = 0.35f,
                    dy1 = -0.45f,
                    dx2 = 0.8f,
                    dy2 = -0.8f,
                    dx3 = 1.25f,
                    dy3 = -1.0f,
                )
                close()
                // M 0.55 11.617
                moveTo(x = 0.55f, y = 11.617f)
                // c 0 0.3 0.25 0.5 0.6 0.5
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.3f,
                    dx2 = 0.25f,
                    dy2 = 0.5f,
                    dx3 = 0.6f,
                    dy3 = 0.5f,
                )
                // H 9.8
                horizontalLineTo(x = 9.8f)
                // c 0.05 -0.35 0.1 -0.65 0.25 -1
                curveToRelative(
                    dx1 = 0.05f,
                    dy1 = -0.35f,
                    dx2 = 0.1f,
                    dy2 = -0.65f,
                    dx3 = 0.25f,
                    dy3 = -1.0f,
                )
                // h -8.9
                horizontalLineToRelative(dx = -8.9f)
                // c -0.35 0 -0.6 0.2 -0.6 0.5z
                curveToRelative(
                    dx1 = -0.35f,
                    dy1 = 0.0f,
                    dx2 = -0.6f,
                    dy2 = 0.2f,
                    dx3 = -0.6f,
                    dy3 = 0.5f,
                )
                close()
                // m 0.6 1.25
                moveToRelative(dx = 0.6f, dy = 1.25f)
                // c -0.35 0 -0.6 0.2 -0.6 0.5
                curveToRelative(
                    dx1 = -0.35f,
                    dy1 = 0.0f,
                    dx2 = -0.6f,
                    dy2 = 0.2f,
                    dx3 = -0.6f,
                    dy3 = 0.5f,
                )
                // c 0 0.25 0.25 0.5 0.6 0.5
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.25f,
                    dx2 = 0.25f,
                    dy2 = 0.5f,
                    dx3 = 0.6f,
                    dy3 = 0.5f,
                )
                // h 8.9
                horizontalLineToRelative(dx = 8.9f)
                // c -0.15 -0.3 -0.2 -0.6 -0.25 -1
                curveToRelative(
                    dx1 = -0.15f,
                    dy1 = -0.3f,
                    dx2 = -0.2f,
                    dy2 = -0.6f,
                    dx3 = -0.25f,
                    dy3 = -1.0f,
                )
                // H 1.15z
                horizontalLineTo(x = 1.15f)
                close()
                // m 0.7 -4.15
                moveToRelative(dx = 0.7f, dy = -4.15f)
                // h 3
                horizontalLineToRelative(dx = 3.0f)
                // c 2 -0.05 2.7 -1.45 2.7 -1.5
                curveToRelative(
                    dx1 = 2.0f,
                    dy1 = -0.05f,
                    dx2 = 2.7f,
                    dy2 = -1.45f,
                    dx3 = 2.7f,
                    dy3 = -1.5f,
                )
                // c 0.25 -0.4 0.35 -0.9 0.35 -1.4
                curveToRelative(
                    dx1 = 0.25f,
                    dy1 = -0.4f,
                    dx2 = 0.35f,
                    dy2 = -0.9f,
                    dx3 = 0.35f,
                    dy3 = -1.4f,
                )
                // c 0.05 -1.6 -1.25 -2.9 -2.85 -2.9
                curveToRelative(
                    dx1 = 0.05f,
                    dy1 = -1.6f,
                    dx2 = -1.25f,
                    dy2 = -2.9f,
                    dx3 = -2.85f,
                    dy3 = -2.9f,
                )
                // c -1.2 0 -2.3 0.75 -2.7 1.9
                curveToRelative(
                    dx1 = -1.2f,
                    dy1 = 0.0f,
                    dx2 = -2.3f,
                    dy2 = 0.75f,
                    dx3 = -2.7f,
                    dy3 = 1.9f,
                )
                // c -0.5 -0.1 -1.05 0 -1.5 0.3
                curveToRelative(
                    dx1 = -0.5f,
                    dy1 = -0.1f,
                    dx2 = -1.05f,
                    dy2 = 0.0f,
                    dx3 = -1.5f,
                    dy3 = 0.3f,
                )
                // c -0.55 0.4 -0.85 1 -0.85 1.65
                curveToRelative(
                    dx1 = -0.55f,
                    dy1 = 0.4f,
                    dx2 = -0.85f,
                    dy2 = 1.0f,
                    dx3 = -0.85f,
                    dy3 = 1.65f,
                )
                // c 0 0.8 0.5 1.5 1.2 1.8
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.8f,
                    dx2 = 0.5f,
                    dy2 = 1.5f,
                    dx3 = 1.2f,
                    dy3 = 1.8f,
                )
                // c 0.2 0.1 0.4 0.15 0.65 0.15z
                curveToRelative(
                    dx1 = 0.2f,
                    dy1 = 0.1f,
                    dx2 = 0.4f,
                    dy2 = 0.15f,
                    dx3 = 0.65f,
                    dy3 = 0.15f,
                )
                close()
                // m -0.45 -2.8
                moveToRelative(dx = -0.45f, dy = -2.8f)
                // c 0.15 -0.1 0.35 -0.15 0.55 -0.15
                curveToRelative(
                    dx1 = 0.15f,
                    dy1 = -0.1f,
                    dx2 = 0.35f,
                    dy2 = -0.15f,
                    dx3 = 0.55f,
                    dy3 = -0.15f,
                )
                // c 0.15 0 0.35 0.05 0.45 0.15
                curveToRelative(
                    dx1 = 0.15f,
                    dy1 = 0.0f,
                    dx2 = 0.35f,
                    dy2 = 0.05f,
                    dx3 = 0.45f,
                    dy3 = 0.15f,
                )
                // l 0.6 0.3
                lineToRelative(dx = 0.6f, dy = 0.3f)
                // l 0.1 -0.7
                lineToRelative(dx = 0.1f, dy = -0.7f)
                // c 0.15 -0.95 0.95 -1.65 1.95 -1.65
                curveToRelative(
                    dx1 = 0.15f,
                    dy1 = -0.95f,
                    dx2 = 0.95f,
                    dy2 = -1.65f,
                    dx3 = 1.95f,
                    dy3 = -1.65f,
                )
                // c 1.1 0 1.95 0.9 1.95 1.95
                curveToRelative(
                    dx1 = 1.1f,
                    dy1 = 0.0f,
                    dx2 = 1.95f,
                    dy2 = 0.9f,
                    dx3 = 1.95f,
                    dy3 = 1.95f,
                )
                // c 0 0.4 -0.1 0.7 -0.25 1
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.4f,
                    dx2 = -0.1f,
                    dy2 = 0.7f,
                    dx3 = -0.25f,
                    dy3 = 1.0f,
                )
                // c -0.65 0.8 -1.6 1 -1.75 1
                curveToRelative(
                    dx1 = -0.65f,
                    dy1 = 0.8f,
                    dx2 = -1.6f,
                    dy2 = 1.0f,
                    dx3 = -1.75f,
                    dy3 = 1.0f,
                )
                // H 1.85
                horizontalLineTo(x = 1.85f)
                // c -0.05 0 -0.15 0 -0.2 -0.05
                curveToRelative(
                    dx1 = -0.05f,
                    dy1 = 0.0f,
                    dx2 = -0.15f,
                    dy2 = 0.0f,
                    dx3 = -0.2f,
                    dy3 = -0.05f,
                )
                // l -0.05 -0.05
                lineToRelative(dx = -0.05f, dy = -0.05f)
                // c -0.4 -0.15 -0.65 -0.55 -0.65 -0.95
                curveToRelative(
                    dx1 = -0.4f,
                    dy1 = -0.15f,
                    dx2 = -0.65f,
                    dy2 = -0.55f,
                    dx3 = -0.65f,
                    dy3 = -0.95f,
                )
                // c 0 -0.35 0.15 -0.65 0.45 -0.85z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.35f,
                    dx2 = 0.15f,
                    dy2 = -0.65f,
                    dx3 = 0.45f,
                    dy3 = -0.85f,
                )
                close()
                // m 7.8 -4.7
                moveToRelative(dx = 7.8f, dy = -4.7f)
                // c 1.35 0 2.5 0.75 3.1 1.9
                curveToRelative(
                    dx1 = 1.35f,
                    dy1 = 0.0f,
                    dx2 = 2.5f,
                    dy2 = 0.75f,
                    dx3 = 3.1f,
                    dy3 = 1.9f,
                )
                // c 0.15 0.3 0.25 0.6 0.35 0.9
                curveToRelative(
                    dx1 = 0.15f,
                    dy1 = 0.3f,
                    dx2 = 0.25f,
                    dy2 = 0.6f,
                    dx3 = 0.35f,
                    dy3 = 0.9f,
                )
                // c 0.15 -0.05 0.25 -0.05 0.4 -0.05
                curveToRelative(
                    dx1 = 0.15f,
                    dy1 = -0.05f,
                    dx2 = 0.25f,
                    dy2 = -0.05f,
                    dx3 = 0.4f,
                    dy3 = -0.05f,
                )
                // c 0.2 0 0.4 0.05 0.6 0.1
                curveToRelative(
                    dx1 = 0.2f,
                    dy1 = 0.0f,
                    dx2 = 0.4f,
                    dy2 = 0.05f,
                    dx3 = 0.6f,
                    dy3 = 0.1f,
                )
                // c 0.8 0.25 1.35 0.95 1.35 1.8
                curveToRelative(
                    dx1 = 0.8f,
                    dy1 = 0.25f,
                    dx2 = 1.35f,
                    dy2 = 0.95f,
                    dx3 = 1.35f,
                    dy3 = 1.8f,
                )
                // a 1.9 1.9 0 0 1 -1.9 1.9
                arcToRelative(
                    a = 1.9f,
                    b = 1.9f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.9f,
                    dy1 = 1.9f,
                )
                // H 7.95
                horizontalLineTo(x = 7.95f)
                // c -0.15 0.25 -0.45 0.55 -0.85 0.85
                curveToRelative(
                    dx1 = -0.15f,
                    dy1 = 0.25f,
                    dx2 = -0.45f,
                    dy2 = 0.55f,
                    dx3 = -0.85f,
                    dy3 = 0.85f,
                )
                // c 0.1 0.05 0.2 0.1 0.35 0.1
                curveToRelative(
                    dx1 = 0.1f,
                    dy1 = 0.05f,
                    dx2 = 0.2f,
                    dy2 = 0.1f,
                    dx3 = 0.35f,
                    dy3 = 0.1f,
                )
                // h 5.7
                horizontalLineToRelative(dx = 5.7f)
                // c 1.6 0 2.85 -1.3 2.85 -2.85
                curveToRelative(
                    dx1 = 1.6f,
                    dy1 = 0.0f,
                    dx2 = 2.85f,
                    dy2 = -1.3f,
                    dx3 = 2.85f,
                    dy3 = -2.85f,
                )
                // c 0 -1.5 -1.15 -2.7 -2.6 -2.85
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.5f,
                    dx2 = -1.15f,
                    dy2 = -2.7f,
                    dx3 = -2.6f,
                    dy3 = -2.85f,
                )
                // c -0.7 -1.6 -2.25 -2.75 -4.15 -2.75
                curveToRelative(
                    dx1 = -0.7f,
                    dy1 = -1.6f,
                    dx2 = -2.25f,
                    dy2 = -2.75f,
                    dx3 = -4.15f,
                    dy3 = -2.75f,
                )
                // c -1.6 0 -3 0.85 -3.8 2.1
                curveToRelative(
                    dx1 = -1.6f,
                    dy1 = 0.0f,
                    dx2 = -3.0f,
                    dy2 = 0.85f,
                    dx3 = -3.8f,
                    dy3 = 2.1f,
                )
                // c 0.35 0.05 0.65 0.15 0.95 0.25
                curveToRelative(
                    dx1 = 0.35f,
                    dy1 = 0.05f,
                    dx2 = 0.65f,
                    dy2 = 0.15f,
                    dx3 = 0.95f,
                    dy3 = 0.25f,
                )
                // c 0.65 -0.85 1.65 -1.4 2.8 -1.4z
                curveToRelative(
                    dx1 = 0.65f,
                    dy1 = -0.85f,
                    dx2 = 1.65f,
                    dy2 = -1.4f,
                    dx3 = 2.8f,
                    dy3 = -1.4f,
                )
                close()
                // m 3.91 10.204
                moveToRelative(dx = 3.91f, dy = 10.204f)
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
            // M11 12.5 a2.5 2.5 0 1 0 5 0 2.5 2.5 0 0 0 -5 0Z m4.594 0 a2.094 2.094 0 1 1 -4.188 0 2.094 2.094 0 0 1 4.188 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11 12.5
                moveTo(x = 11.0f, y = 12.5f)
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
        }.build().also { _ic1705 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1705: ImageVector? = null
