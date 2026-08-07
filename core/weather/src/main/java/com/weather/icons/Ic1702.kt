package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1702: ImageVector
    get() {
        val current = _ic1702
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1702",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M9.1 .366 c-1.1 -.05 -1.65 .4 -1.95 .8 -.4 .6 -.45 1.35 -.3 1.8 .1 .2 .3 .3 .5 .25 .2 -.1 .3 -.3 .25 -.5 -.1 -.2 -.1 -.7 .2 -1.05 .25 -.35 .7 -.5 1.25 -.45 .6 .05 1.05 .25 1.35 .65 .3 .45 .4 1.1 .2 1.65 -.1 .2 -.2 .35 -.25 .5 -.35 .45 -.5 .4 -.75 .45 H.25 c-.15 .05 -.25 .15 -.25 .35 0 .2 .2 .4 .4 .4 h9.2 c.55 0 .85 -.15 1.25 -.6 .2 -.2 .4 -.5 .55 -.9 0 -.1 .05 -.2 .1 -.3 .15 -.75 0 -1.5 -.35 -2.1 -.35 -.4 -.95 -.9 -2.05 -.95Z m4.4 6 c.2 -.2 .35 -.5 .3 -1 v-.05 c-.1 -.4 -.5 -.85 -1 -.9 h-.15 c-.25 0 -.7 .15 -1.05 .75 a.326 .326 0 1 0 .55 .35 c.1 -.2 .3 -.4 .5 -.4 s.4 .25 .45 .4 c0 .55 -.35 .6 -.35 .6 H.35 c-.2 0 -.35 .15 -.35 .35 0 .2 .15 .35 .35 .35 0 0 12.3 .1 12.45 0 .15 -.1 .5 -.25 .7 -.45Z m-3.8 1.15 H.35 c-.2 0 -.35 .15 -.35 .35 0 .2 .15 .35 .35 .35 H9.7 s.4 .05 .35 .6 c-.05 .15 -.25 .35 -.45 .4 -.2 .05 -.4 -.2 -.5 -.4 a.326 .326 0 1 0 -.55 .35 c.35 .6 .8 .75 1.05 .75 h.15 c.5 -.05 .9 -.55 1 -.9 v-.05 c.05 -.5 -.15 -.85 -.3 -1 -.25 -.3 -.55 -.45 -.75 -.45Z m-1.95 5.45 c.4 -.6 .5 -1.35 .35 -2.1 0 -.1 -.05 -.2 -.1 -.3 -.1 -.4 -.3 -.65 -.5 -.9 -.4 -.4 -.7 -.6 -1.25 -.6 H1.2 c-.2 0 -.4 .2 -.4 .4 0 .15 .1 .3 .25 .35 H6.3 c.25 .05 .4 0 .75 .45 .1 .1 .2 .3 .25 .5 .2 .55 .1 1.2 -.2 1.65 -.3 .4 -.75 .65 -1.35 .65 -.6 0 -1.05 -.1 -1.25 -.45 -.25 -.35 -.25 -.85 -.2 -1.05 .05 -.2 -.05 -.4 -.25 -.5 -.2 -.05 -.4 .05 -.5 .25 -.15 .45 -.15 1.2 .3 1.8 .3 .4 .85 .85 1.95 .8 1.05 0 1.65 -.55 1.95 -.95Z m3.939 -1.829 c-.016 -.14 .128 -.262 .311 -.262 s.327 .121 .311 .262 l-.14 1.238 h-.341 l-.141 -1.238Z m.563 1.738 a.25 .25 0 1 1 -.5 0 .25 .25 0 0 1 .5 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 9.1 0.366
                moveTo(x = 9.1f, y = 0.366f)
                // c -1.1 -0.05 -1.65 0.4 -1.95 0.8
                curveToRelative(
                    dx1 = -1.1f,
                    dy1 = -0.05f,
                    dx2 = -1.65f,
                    dy2 = 0.4f,
                    dx3 = -1.95f,
                    dy3 = 0.8f,
                )
                // c -0.4 0.6 -0.45 1.35 -0.3 1.8
                curveToRelative(
                    dx1 = -0.4f,
                    dy1 = 0.6f,
                    dx2 = -0.45f,
                    dy2 = 1.35f,
                    dx3 = -0.3f,
                    dy3 = 1.8f,
                )
                // c 0.1 0.2 0.3 0.3 0.5 0.25
                curveToRelative(
                    dx1 = 0.1f,
                    dy1 = 0.2f,
                    dx2 = 0.3f,
                    dy2 = 0.3f,
                    dx3 = 0.5f,
                    dy3 = 0.25f,
                )
                // c 0.2 -0.1 0.3 -0.3 0.25 -0.5
                curveToRelative(
                    dx1 = 0.2f,
                    dy1 = -0.1f,
                    dx2 = 0.3f,
                    dy2 = -0.3f,
                    dx3 = 0.25f,
                    dy3 = -0.5f,
                )
                // c -0.1 -0.2 -0.1 -0.7 0.2 -1.05
                curveToRelative(
                    dx1 = -0.1f,
                    dy1 = -0.2f,
                    dx2 = -0.1f,
                    dy2 = -0.7f,
                    dx3 = 0.2f,
                    dy3 = -1.05f,
                )
                // c 0.25 -0.35 0.7 -0.5 1.25 -0.45
                curveToRelative(
                    dx1 = 0.25f,
                    dy1 = -0.35f,
                    dx2 = 0.7f,
                    dy2 = -0.5f,
                    dx3 = 1.25f,
                    dy3 = -0.45f,
                )
                // c 0.6 0.05 1.05 0.25 1.35 0.65
                curveToRelative(
                    dx1 = 0.6f,
                    dy1 = 0.05f,
                    dx2 = 1.05f,
                    dy2 = 0.25f,
                    dx3 = 1.35f,
                    dy3 = 0.65f,
                )
                // c 0.3 0.45 0.4 1.1 0.2 1.65
                curveToRelative(
                    dx1 = 0.3f,
                    dy1 = 0.45f,
                    dx2 = 0.4f,
                    dy2 = 1.1f,
                    dx3 = 0.2f,
                    dy3 = 1.65f,
                )
                // c -0.1 0.2 -0.2 0.35 -0.25 0.5
                curveToRelative(
                    dx1 = -0.1f,
                    dy1 = 0.2f,
                    dx2 = -0.2f,
                    dy2 = 0.35f,
                    dx3 = -0.25f,
                    dy3 = 0.5f,
                )
                // c -0.35 0.45 -0.5 0.4 -0.75 0.45
                curveToRelative(
                    dx1 = -0.35f,
                    dy1 = 0.45f,
                    dx2 = -0.5f,
                    dy2 = 0.4f,
                    dx3 = -0.75f,
                    dy3 = 0.45f,
                )
                // H 0.25
                horizontalLineTo(x = 0.25f)
                // c -0.15 0.05 -0.25 0.15 -0.25 0.35
                curveToRelative(
                    dx1 = -0.15f,
                    dy1 = 0.05f,
                    dx2 = -0.25f,
                    dy2 = 0.15f,
                    dx3 = -0.25f,
                    dy3 = 0.35f,
                )
                // c 0 0.2 0.2 0.4 0.4 0.4
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.2f,
                    dx2 = 0.2f,
                    dy2 = 0.4f,
                    dx3 = 0.4f,
                    dy3 = 0.4f,
                )
                // h 9.2
                horizontalLineToRelative(dx = 9.2f)
                // c 0.55 0 0.85 -0.15 1.25 -0.6
                curveToRelative(
                    dx1 = 0.55f,
                    dy1 = 0.0f,
                    dx2 = 0.85f,
                    dy2 = -0.15f,
                    dx3 = 1.25f,
                    dy3 = -0.6f,
                )
                // c 0.2 -0.2 0.4 -0.5 0.55 -0.9
                curveToRelative(
                    dx1 = 0.2f,
                    dy1 = -0.2f,
                    dx2 = 0.4f,
                    dy2 = -0.5f,
                    dx3 = 0.55f,
                    dy3 = -0.9f,
                )
                // c 0 -0.1 0.05 -0.2 0.1 -0.3
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.1f,
                    dx2 = 0.05f,
                    dy2 = -0.2f,
                    dx3 = 0.1f,
                    dy3 = -0.3f,
                )
                // c 0.15 -0.75 0 -1.5 -0.35 -2.1
                curveToRelative(
                    dx1 = 0.15f,
                    dy1 = -0.75f,
                    dx2 = 0.0f,
                    dy2 = -1.5f,
                    dx3 = -0.35f,
                    dy3 = -2.1f,
                )
                // c -0.35 -0.4 -0.95 -0.9 -2.05 -0.95z
                curveToRelative(
                    dx1 = -0.35f,
                    dy1 = -0.4f,
                    dx2 = -0.95f,
                    dy2 = -0.9f,
                    dx3 = -2.05f,
                    dy3 = -0.95f,
                )
                close()
                // m 4.4 6
                moveToRelative(dx = 4.4f, dy = 6.0f)
                // c 0.2 -0.2 0.35 -0.5 0.3 -1
                curveToRelative(
                    dx1 = 0.2f,
                    dy1 = -0.2f,
                    dx2 = 0.35f,
                    dy2 = -0.5f,
                    dx3 = 0.3f,
                    dy3 = -1.0f,
                )
                // v -0.05
                verticalLineToRelative(dy = -0.05f)
                // c -0.1 -0.4 -0.5 -0.85 -1 -0.9
                curveToRelative(
                    dx1 = -0.1f,
                    dy1 = -0.4f,
                    dx2 = -0.5f,
                    dy2 = -0.85f,
                    dx3 = -1.0f,
                    dy3 = -0.9f,
                )
                // h -0.15
                horizontalLineToRelative(dx = -0.15f)
                // c -0.25 0 -0.7 0.15 -1.05 0.75
                curveToRelative(
                    dx1 = -0.25f,
                    dy1 = 0.0f,
                    dx2 = -0.7f,
                    dy2 = 0.15f,
                    dx3 = -1.05f,
                    dy3 = 0.75f,
                )
                // a 0.326 0.326 0 1 0 0.55 0.35
                arcToRelative(
                    a = 0.326f,
                    b = 0.326f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.55f,
                    dy1 = 0.35f,
                )
                // c 0.1 -0.2 0.3 -0.4 0.5 -0.4
                curveToRelative(
                    dx1 = 0.1f,
                    dy1 = -0.2f,
                    dx2 = 0.3f,
                    dy2 = -0.4f,
                    dx3 = 0.5f,
                    dy3 = -0.4f,
                )
                // s 0.4 0.25 0.45 0.4
                reflectiveCurveToRelative(
                    dx1 = 0.4f,
                    dy1 = 0.25f,
                    dx2 = 0.45f,
                    dy2 = 0.4f,
                )
                // c 0 0.55 -0.35 0.6 -0.35 0.6
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.55f,
                    dx2 = -0.35f,
                    dy2 = 0.6f,
                    dx3 = -0.35f,
                    dy3 = 0.6f,
                )
                // H 0.35
                horizontalLineTo(x = 0.35f)
                // c -0.2 0 -0.35 0.15 -0.35 0.35
                curveToRelative(
                    dx1 = -0.2f,
                    dy1 = 0.0f,
                    dx2 = -0.35f,
                    dy2 = 0.15f,
                    dx3 = -0.35f,
                    dy3 = 0.35f,
                )
                // c 0 0.2 0.15 0.35 0.35 0.35
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.2f,
                    dx2 = 0.15f,
                    dy2 = 0.35f,
                    dx3 = 0.35f,
                    dy3 = 0.35f,
                )
                // c 0 0 12.3 0.1 12.45 0
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.0f,
                    dx2 = 12.3f,
                    dy2 = 0.1f,
                    dx3 = 12.45f,
                    dy3 = 0.0f,
                )
                // c 0.15 -0.1 0.5 -0.25 0.7 -0.45z
                curveToRelative(
                    dx1 = 0.15f,
                    dy1 = -0.1f,
                    dx2 = 0.5f,
                    dy2 = -0.25f,
                    dx3 = 0.7f,
                    dy3 = -0.45f,
                )
                close()
                // m -3.8 1.15
                moveToRelative(dx = -3.8f, dy = 1.15f)
                // H 0.35
                horizontalLineTo(x = 0.35f)
                // c -0.2 0 -0.35 0.15 -0.35 0.35
                curveToRelative(
                    dx1 = -0.2f,
                    dy1 = 0.0f,
                    dx2 = -0.35f,
                    dy2 = 0.15f,
                    dx3 = -0.35f,
                    dy3 = 0.35f,
                )
                // c 0 0.2 0.15 0.35 0.35 0.35
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.2f,
                    dx2 = 0.15f,
                    dy2 = 0.35f,
                    dx3 = 0.35f,
                    dy3 = 0.35f,
                )
                // H 9.7
                horizontalLineTo(x = 9.7f)
                // s 0.4 0.05 0.35 0.6
                reflectiveCurveToRelative(
                    dx1 = 0.4f,
                    dy1 = 0.05f,
                    dx2 = 0.35f,
                    dy2 = 0.6f,
                )
                // c -0.05 0.15 -0.25 0.35 -0.45 0.4
                curveToRelative(
                    dx1 = -0.05f,
                    dy1 = 0.15f,
                    dx2 = -0.25f,
                    dy2 = 0.35f,
                    dx3 = -0.45f,
                    dy3 = 0.4f,
                )
                // c -0.2 0.05 -0.4 -0.2 -0.5 -0.4
                curveToRelative(
                    dx1 = -0.2f,
                    dy1 = 0.05f,
                    dx2 = -0.4f,
                    dy2 = -0.2f,
                    dx3 = -0.5f,
                    dy3 = -0.4f,
                )
                // a 0.326 0.326 0 1 0 -0.55 0.35
                arcToRelative(
                    a = 0.326f,
                    b = 0.326f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.55f,
                    dy1 = 0.35f,
                )
                // c 0.35 0.6 0.8 0.75 1.05 0.75
                curveToRelative(
                    dx1 = 0.35f,
                    dy1 = 0.6f,
                    dx2 = 0.8f,
                    dy2 = 0.75f,
                    dx3 = 1.05f,
                    dy3 = 0.75f,
                )
                // h 0.15
                horizontalLineToRelative(dx = 0.15f)
                // c 0.5 -0.05 0.9 -0.55 1 -0.9
                curveToRelative(
                    dx1 = 0.5f,
                    dy1 = -0.05f,
                    dx2 = 0.9f,
                    dy2 = -0.55f,
                    dx3 = 1.0f,
                    dy3 = -0.9f,
                )
                // v -0.05
                verticalLineToRelative(dy = -0.05f)
                // c 0.05 -0.5 -0.15 -0.85 -0.3 -1
                curveToRelative(
                    dx1 = 0.05f,
                    dy1 = -0.5f,
                    dx2 = -0.15f,
                    dy2 = -0.85f,
                    dx3 = -0.3f,
                    dy3 = -1.0f,
                )
                // c -0.25 -0.3 -0.55 -0.45 -0.75 -0.45z
                curveToRelative(
                    dx1 = -0.25f,
                    dy1 = -0.3f,
                    dx2 = -0.55f,
                    dy2 = -0.45f,
                    dx3 = -0.75f,
                    dy3 = -0.45f,
                )
                close()
                // m -1.95 5.45
                moveToRelative(dx = -1.95f, dy = 5.45f)
                // c 0.4 -0.6 0.5 -1.35 0.35 -2.1
                curveToRelative(
                    dx1 = 0.4f,
                    dy1 = -0.6f,
                    dx2 = 0.5f,
                    dy2 = -1.35f,
                    dx3 = 0.35f,
                    dy3 = -2.1f,
                )
                // c 0 -0.1 -0.05 -0.2 -0.1 -0.3
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.1f,
                    dx2 = -0.05f,
                    dy2 = -0.2f,
                    dx3 = -0.1f,
                    dy3 = -0.3f,
                )
                // c -0.1 -0.4 -0.3 -0.65 -0.5 -0.9
                curveToRelative(
                    dx1 = -0.1f,
                    dy1 = -0.4f,
                    dx2 = -0.3f,
                    dy2 = -0.65f,
                    dx3 = -0.5f,
                    dy3 = -0.9f,
                )
                // c -0.4 -0.4 -0.7 -0.6 -1.25 -0.6
                curveToRelative(
                    dx1 = -0.4f,
                    dy1 = -0.4f,
                    dx2 = -0.7f,
                    dy2 = -0.6f,
                    dx3 = -1.25f,
                    dy3 = -0.6f,
                )
                // H 1.2
                horizontalLineTo(x = 1.2f)
                // c -0.2 0 -0.4 0.2 -0.4 0.4
                curveToRelative(
                    dx1 = -0.2f,
                    dy1 = 0.0f,
                    dx2 = -0.4f,
                    dy2 = 0.2f,
                    dx3 = -0.4f,
                    dy3 = 0.4f,
                )
                // c 0 0.15 0.1 0.3 0.25 0.35
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.15f,
                    dx2 = 0.1f,
                    dy2 = 0.3f,
                    dx3 = 0.25f,
                    dy3 = 0.35f,
                )
                // H 6.3
                horizontalLineTo(x = 6.3f)
                // c 0.25 0.05 0.4 0 0.75 0.45
                curveToRelative(
                    dx1 = 0.25f,
                    dy1 = 0.05f,
                    dx2 = 0.4f,
                    dy2 = 0.0f,
                    dx3 = 0.75f,
                    dy3 = 0.45f,
                )
                // c 0.1 0.1 0.2 0.3 0.25 0.5
                curveToRelative(
                    dx1 = 0.1f,
                    dy1 = 0.1f,
                    dx2 = 0.2f,
                    dy2 = 0.3f,
                    dx3 = 0.25f,
                    dy3 = 0.5f,
                )
                // c 0.2 0.55 0.1 1.2 -0.2 1.65
                curveToRelative(
                    dx1 = 0.2f,
                    dy1 = 0.55f,
                    dx2 = 0.1f,
                    dy2 = 1.2f,
                    dx3 = -0.2f,
                    dy3 = 1.65f,
                )
                // c -0.3 0.4 -0.75 0.65 -1.35 0.65
                curveToRelative(
                    dx1 = -0.3f,
                    dy1 = 0.4f,
                    dx2 = -0.75f,
                    dy2 = 0.65f,
                    dx3 = -1.35f,
                    dy3 = 0.65f,
                )
                // c -0.6 0 -1.05 -0.1 -1.25 -0.45
                curveToRelative(
                    dx1 = -0.6f,
                    dy1 = 0.0f,
                    dx2 = -1.05f,
                    dy2 = -0.1f,
                    dx3 = -1.25f,
                    dy3 = -0.45f,
                )
                // c -0.25 -0.35 -0.25 -0.85 -0.2 -1.05
                curveToRelative(
                    dx1 = -0.25f,
                    dy1 = -0.35f,
                    dx2 = -0.25f,
                    dy2 = -0.85f,
                    dx3 = -0.2f,
                    dy3 = -1.05f,
                )
                // c 0.05 -0.2 -0.05 -0.4 -0.25 -0.5
                curveToRelative(
                    dx1 = 0.05f,
                    dy1 = -0.2f,
                    dx2 = -0.05f,
                    dy2 = -0.4f,
                    dx3 = -0.25f,
                    dy3 = -0.5f,
                )
                // c -0.2 -0.05 -0.4 0.05 -0.5 0.25
                curveToRelative(
                    dx1 = -0.2f,
                    dy1 = -0.05f,
                    dx2 = -0.4f,
                    dy2 = 0.05f,
                    dx3 = -0.5f,
                    dy3 = 0.25f,
                )
                // c -0.15 0.45 -0.15 1.2 0.3 1.8
                curveToRelative(
                    dx1 = -0.15f,
                    dy1 = 0.45f,
                    dx2 = -0.15f,
                    dy2 = 1.2f,
                    dx3 = 0.3f,
                    dy3 = 1.8f,
                )
                // c 0.3 0.4 0.85 0.85 1.95 0.8
                curveToRelative(
                    dx1 = 0.3f,
                    dy1 = 0.4f,
                    dx2 = 0.85f,
                    dy2 = 0.85f,
                    dx3 = 1.95f,
                    dy3 = 0.8f,
                )
                // c 1.05 0 1.65 -0.55 1.95 -0.95z
                curveToRelative(
                    dx1 = 1.05f,
                    dy1 = 0.0f,
                    dx2 = 1.65f,
                    dy2 = -0.55f,
                    dx3 = 1.95f,
                    dy3 = -0.95f,
                )
                close()
                // m 3.939 -1.829
                moveToRelative(dx = 3.939f, dy = -1.829f)
                // c -0.016 -0.14 0.128 -0.262 0.311 -0.262
                curveToRelative(
                    dx1 = -0.016f,
                    dy1 = -0.14f,
                    dx2 = 0.128f,
                    dy2 = -0.262f,
                    dx3 = 0.311f,
                    dy3 = -0.262f,
                )
                // s 0.327 0.121 0.311 0.262
                reflectiveCurveToRelative(
                    dx1 = 0.327f,
                    dy1 = 0.121f,
                    dx2 = 0.311f,
                    dy2 = 0.262f,
                )
                // l -0.14 1.238
                lineToRelative(dx = -0.14f, dy = 1.238f)
                // h -0.341
                horizontalLineToRelative(dx = -0.341f)
                // l -0.141 -1.238z
                lineToRelative(dx = -0.141f, dy = -1.238f)
                close()
                // m 0.563 1.738
                moveToRelative(dx = 0.563f, dy = 1.738f)
                // a 0.25 0.25 0 1 1 -0.5 0
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = 0.0f,
                )
                // a 0.25 0.25 0 0 1 0.5 0z
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = 0.0f,
                )
                close()
            }
            // M10 12 a2 2 0 1 0 4 0 2 2 0 0 0 -4 0Z m3.675 0 a1.675 1.675 0 1 1 -3.35 0 1.675 1.675 0 0 1 3.35 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10 12
                moveTo(x = 10.0f, y = 12.0f)
                // a 2 2 0 1 0 4 0
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 4.0f,
                    dy1 = 0.0f,
                )
                // a 2 2 0 0 0 -4 0z
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -4.0f,
                    dy1 = 0.0f,
                )
                close()
                // m 3.675 0
                moveToRelative(dx = 3.675f, dy = 0.0f)
                // a 1.675 1.675 0 1 1 -3.35 0
                arcToRelative(
                    a = 1.675f,
                    b = 1.675f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -3.35f,
                    dy1 = 0.0f,
                )
                // a 1.675 1.675 0 0 1 3.35 0z
                arcToRelative(
                    a = 1.675f,
                    b = 1.675f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 3.35f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic1702 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1702: ImageVector? = null
