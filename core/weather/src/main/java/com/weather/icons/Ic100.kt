package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic100: ImageVector
    get() {
        val current = _ic100
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic100",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.655 2.357 a.5 .5 0 0 0 .854 -.353 v-1.5 a.5 .5 0 0 0 -1 0 v1.5 a.5 .5 0 0 0 .146 .353Z m-4.08 1.861 c.06 .026 .126 .039 .191 .039 l.001 -.001 a.5 .5 0 0 0 .355 -.855 l-1.064 -1.06 a.5 .5 0 0 0 -.707 .708 l1.062 1.06 a.498 .498 0 0 0 .162 .11Z M.503 8.496 h1.5 a.5 .5 0 1 0 0 -1 h-1.5 a.5 .5 0 0 0 0 1Z m1.914 5.221 a.501 .501 0 0 0 .631 -.063 l1.063 -1.06 a.5 .5 0 0 0 -.708 -.707 l-1.062 1.06 a.5 .5 0 0 0 .076 .77Z m5.225 2.14 a.5 .5 0 0 0 .854 -.354 v-1.5 a.5 .5 0 0 0 -1 0 v1.5 a.5 .5 0 0 0 .146 .354Z m5.467 -2.084 a.5 .5 0 0 0 .544 -.816 l-1.06 -1.06 a.498 .498 0 0 0 -.832 .152 .5 .5 0 0 0 .126 .555 l1.06 1.06 a.496 .496 0 0 0 .162 .109Z m.893 -5.263 h1.5 a.5 .5 0 1 0 0 -1 h-1.5 a.5 .5 0 0 0 0 1Z m-2.031 -4.327 a.5 .5 0 0 0 .633 -.063 l1.06 -1.06 a.5 .5 0 1 0 -.708 -.708 l-1.06 1.06 a.5 .5 0 0 0 .075 .77Z m-6.466 .075 a4.5 4.5 0 1 1 5 7.484 4.5 4.5 0 0 1 -5 -7.484Z m4.445 .832 a3.5 3.5 0 1 0 -3.89 5.82 3.5 3.5 0 0 0 3.89 -5.82Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.655 2.357
                moveTo(x = 7.655f, y = 2.357f)
                // a 0.5 0.5 0 0 0 0.854 -0.353
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.854f,
                    dy1 = -0.353f,
                )
                // v -1.5
                verticalLineToRelative(dy = -1.5f)
                // a 0.5 0.5 0 0 0 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // v 1.5
                verticalLineToRelative(dy = 1.5f)
                // a 0.5 0.5 0 0 0 0.146 0.353z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.146f,
                    dy1 = 0.353f,
                )
                close()
                // m -4.08 1.861
                moveToRelative(dx = -4.08f, dy = 1.861f)
                // c 0.06 0.026 0.126 0.039 0.191 0.039
                curveToRelative(
                    dx1 = 0.06f,
                    dy1 = 0.026f,
                    dx2 = 0.126f,
                    dy2 = 0.039f,
                    dx3 = 0.191f,
                    dy3 = 0.039f,
                )
                // l 0.001 -0.001
                lineToRelative(dx = 0.001f, dy = -0.001f)
                // a 0.5 0.5 0 0 0 0.355 -0.855
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.355f,
                    dy1 = -0.855f,
                )
                // l -1.064 -1.06
                lineToRelative(dx = -1.064f, dy = -1.06f)
                // a 0.5 0.5 0 0 0 -0.707 0.708
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.707f,
                    dy1 = 0.708f,
                )
                // l 1.062 1.06
                lineToRelative(dx = 1.062f, dy = 1.06f)
                // a 0.498 0.498 0 0 0 0.162 0.11z
                arcToRelative(
                    a = 0.498f,
                    b = 0.498f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.162f,
                    dy1 = 0.11f,
                )
                close()
                // M 0.503 8.496
                moveTo(x = 0.503f, y = 8.496f)
                // h 1.5
                horizontalLineToRelative(dx = 1.5f)
                // a 0.5 0.5 0 1 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h -1.5
                horizontalLineToRelative(dx = -1.5f)
                // a 0.5 0.5 0 0 0 0 1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                close()
                // m 1.914 5.221
                moveToRelative(dx = 1.914f, dy = 5.221f)
                // a 0.501 0.501 0 0 0 0.631 -0.063
                arcToRelative(
                    a = 0.501f,
                    b = 0.501f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.631f,
                    dy1 = -0.063f,
                )
                // l 1.063 -1.06
                lineToRelative(dx = 1.063f, dy = -1.06f)
                // a 0.5 0.5 0 0 0 -0.708 -0.707
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.708f,
                    dy1 = -0.707f,
                )
                // l -1.062 1.06
                lineToRelative(dx = -1.062f, dy = 1.06f)
                // a 0.5 0.5 0 0 0 0.076 0.77z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.076f,
                    dy1 = 0.77f,
                )
                close()
                // m 5.225 2.14
                moveToRelative(dx = 5.225f, dy = 2.14f)
                // a 0.5 0.5 0 0 0 0.854 -0.354
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.854f,
                    dy1 = -0.354f,
                )
                // v -1.5
                verticalLineToRelative(dy = -1.5f)
                // a 0.5 0.5 0 0 0 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // v 1.5
                verticalLineToRelative(dy = 1.5f)
                // a 0.5 0.5 0 0 0 0.146 0.354z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.146f,
                    dy1 = 0.354f,
                )
                close()
                // m 5.467 -2.084
                moveToRelative(dx = 5.467f, dy = -2.084f)
                // a 0.5 0.5 0 0 0 0.544 -0.816
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.544f,
                    dy1 = -0.816f,
                )
                // l -1.06 -1.06
                lineToRelative(dx = -1.06f, dy = -1.06f)
                // a 0.498 0.498 0 0 0 -0.832 0.152
                arcToRelative(
                    a = 0.498f,
                    b = 0.498f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.832f,
                    dy1 = 0.152f,
                )
                // a 0.5 0.5 0 0 0 0.126 0.555
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.126f,
                    dy1 = 0.555f,
                )
                // l 1.06 1.06
                lineToRelative(dx = 1.06f, dy = 1.06f)
                // a 0.496 0.496 0 0 0 0.162 0.109z
                arcToRelative(
                    a = 0.496f,
                    b = 0.496f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.162f,
                    dy1 = 0.109f,
                )
                close()
                // m 0.893 -5.263
                moveToRelative(dx = 0.893f, dy = -5.263f)
                // h 1.5
                horizontalLineToRelative(dx = 1.5f)
                // a 0.5 0.5 0 1 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h -1.5
                horizontalLineToRelative(dx = -1.5f)
                // a 0.5 0.5 0 0 0 0 1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                close()
                // m -2.031 -4.327
                moveToRelative(dx = -2.031f, dy = -4.327f)
                // a 0.5 0.5 0 0 0 0.633 -0.063
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.633f,
                    dy1 = -0.063f,
                )
                // l 1.06 -1.06
                lineToRelative(dx = 1.06f, dy = -1.06f)
                // a 0.5 0.5 0 1 0 -0.708 -0.708
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.708f,
                    dy1 = -0.708f,
                )
                // l -1.06 1.06
                lineToRelative(dx = -1.06f, dy = 1.06f)
                // a 0.5 0.5 0 0 0 0.075 0.77z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.075f,
                    dy1 = 0.77f,
                )
                close()
                // m -6.466 0.075
                moveToRelative(dx = -6.466f, dy = 0.075f)
                // a 4.5 4.5 0 1 1 5 7.484
                arcToRelative(
                    a = 4.5f,
                    b = 4.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 5.0f,
                    dy1 = 7.484f,
                )
                // a 4.5 4.5 0 0 1 -5 -7.484z
                arcToRelative(
                    a = 4.5f,
                    b = 4.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -5.0f,
                    dy1 = -7.484f,
                )
                close()
                // m 4.445 0.832
                moveToRelative(dx = 4.445f, dy = 0.832f)
                // a 3.5 3.5 0 1 0 -3.89 5.82
                arcToRelative(
                    a = 3.5f,
                    b = 3.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -3.89f,
                    dy1 = 5.82f,
                )
                // a 3.5 3.5 0 0 0 3.89 -5.82z
                arcToRelative(
                    a = 3.5f,
                    b = 3.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 3.89f,
                    dy1 = -5.82f,
                )
                close()
            }
        }.build().also { _ic100 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic100: ImageVector? = null
