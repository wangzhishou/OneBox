package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2070: ImageVector
    get() {
        val current = _ic2070
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2070",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M13.095 4.104 C13.095 2.392 11.651 1 9.876 1 H8.534 a.82 .82 0 0 0 -.548 .204 .665 .665 0 0 0 -.227 .494 c0 .185 .082 .363 .227 .494 a.82 .82 0 0 0 .548 .204 h1.342 c.92 0 1.668 .766 1.668 1.708 0 .941 -.748 1.707 -1.668 1.707 H.775 a.82 .82 0 0 0 -.548 .205 .665 .665 0 0 0 -.227 .493 c0 .186 .082 .363 .227 .494 a.82 .82 0 0 0 .548 .205 h9.103 c1.773 0 3.217 -1.393 3.217 -3.104Z M11.6 8.608 H1.825 a.82 .82 0 0 0 -.548 .204 .665 .665 0 0 0 -.227 .494 c0 .185 .082 .363 .227 .494 a.82 .82 0 0 0 .548 .204 h9.776 c1.02 0 1.848 .807 1.848 1.8 0 .992 -.829 1.8 -1.848 1.8 h-1.442 a.82 .82 0 0 0 -.548 .204 .665 .665 0 0 0 -.227 .494 c0 .185 .081 .363 .227 .494 a.82 .82 0 0 0 .548 .204 H11.6 c1.875 0 3.4 -1.434 3.4 -3.197 -.001 -1.76 -1.525 -3.195 -3.4 -3.195Z M3.5 1.75 a.75 .75 0 1 1 -1.5 0 .75 .75 0 0 1 1.5 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 13.095 4.104
                moveTo(x = 13.095f, y = 4.104f)
                // C 13.095 2.392 11.651 1 9.876 1
                curveTo(
                    x1 = 13.095f,
                    y1 = 2.392f,
                    x2 = 11.651f,
                    y2 = 1.0f,
                    x3 = 9.876f,
                    y3 = 1.0f,
                )
                // H 8.534
                horizontalLineTo(x = 8.534f)
                // a 0.82 0.82 0 0 0 -0.548 0.204
                arcToRelative(
                    a = 0.82f,
                    b = 0.82f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.548f,
                    dy1 = 0.204f,
                )
                // a 0.665 0.665 0 0 0 -0.227 0.494
                arcToRelative(
                    a = 0.665f,
                    b = 0.665f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.227f,
                    dy1 = 0.494f,
                )
                // c 0 0.185 0.082 0.363 0.227 0.494
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.185f,
                    dx2 = 0.082f,
                    dy2 = 0.363f,
                    dx3 = 0.227f,
                    dy3 = 0.494f,
                )
                // a 0.82 0.82 0 0 0 0.548 0.204
                arcToRelative(
                    a = 0.82f,
                    b = 0.82f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.548f,
                    dy1 = 0.204f,
                )
                // h 1.342
                horizontalLineToRelative(dx = 1.342f)
                // c 0.92 0 1.668 0.766 1.668 1.708
                curveToRelative(
                    dx1 = 0.92f,
                    dy1 = 0.0f,
                    dx2 = 1.668f,
                    dy2 = 0.766f,
                    dx3 = 1.668f,
                    dy3 = 1.708f,
                )
                // c 0 0.941 -0.748 1.707 -1.668 1.707
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.941f,
                    dx2 = -0.748f,
                    dy2 = 1.707f,
                    dx3 = -1.668f,
                    dy3 = 1.707f,
                )
                // H 0.775
                horizontalLineTo(x = 0.775f)
                // a 0.82 0.82 0 0 0 -0.548 0.205
                arcToRelative(
                    a = 0.82f,
                    b = 0.82f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.548f,
                    dy1 = 0.205f,
                )
                // a 0.665 0.665 0 0 0 -0.227 0.493
                arcToRelative(
                    a = 0.665f,
                    b = 0.665f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.227f,
                    dy1 = 0.493f,
                )
                // c 0 0.186 0.082 0.363 0.227 0.494
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.186f,
                    dx2 = 0.082f,
                    dy2 = 0.363f,
                    dx3 = 0.227f,
                    dy3 = 0.494f,
                )
                // a 0.82 0.82 0 0 0 0.548 0.205
                arcToRelative(
                    a = 0.82f,
                    b = 0.82f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.548f,
                    dy1 = 0.205f,
                )
                // h 9.103
                horizontalLineToRelative(dx = 9.103f)
                // c 1.773 0 3.217 -1.393 3.217 -3.104z
                curveToRelative(
                    dx1 = 1.773f,
                    dy1 = 0.0f,
                    dx2 = 3.217f,
                    dy2 = -1.393f,
                    dx3 = 3.217f,
                    dy3 = -3.104f,
                )
                close()
                // M 11.6 8.608
                moveTo(x = 11.6f, y = 8.608f)
                // H 1.825
                horizontalLineTo(x = 1.825f)
                // a 0.82 0.82 0 0 0 -0.548 0.204
                arcToRelative(
                    a = 0.82f,
                    b = 0.82f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.548f,
                    dy1 = 0.204f,
                )
                // a 0.665 0.665 0 0 0 -0.227 0.494
                arcToRelative(
                    a = 0.665f,
                    b = 0.665f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.227f,
                    dy1 = 0.494f,
                )
                // c 0 0.185 0.082 0.363 0.227 0.494
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.185f,
                    dx2 = 0.082f,
                    dy2 = 0.363f,
                    dx3 = 0.227f,
                    dy3 = 0.494f,
                )
                // a 0.82 0.82 0 0 0 0.548 0.204
                arcToRelative(
                    a = 0.82f,
                    b = 0.82f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.548f,
                    dy1 = 0.204f,
                )
                // h 9.776
                horizontalLineToRelative(dx = 9.776f)
                // c 1.02 0 1.848 0.807 1.848 1.8
                curveToRelative(
                    dx1 = 1.02f,
                    dy1 = 0.0f,
                    dx2 = 1.848f,
                    dy2 = 0.807f,
                    dx3 = 1.848f,
                    dy3 = 1.8f,
                )
                // c 0 0.992 -0.829 1.8 -1.848 1.8
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.992f,
                    dx2 = -0.829f,
                    dy2 = 1.8f,
                    dx3 = -1.848f,
                    dy3 = 1.8f,
                )
                // h -1.442
                horizontalLineToRelative(dx = -1.442f)
                // a 0.82 0.82 0 0 0 -0.548 0.204
                arcToRelative(
                    a = 0.82f,
                    b = 0.82f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.548f,
                    dy1 = 0.204f,
                )
                // a 0.665 0.665 0 0 0 -0.227 0.494
                arcToRelative(
                    a = 0.665f,
                    b = 0.665f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.227f,
                    dy1 = 0.494f,
                )
                // c 0 0.185 0.081 0.363 0.227 0.494
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.185f,
                    dx2 = 0.081f,
                    dy2 = 0.363f,
                    dx3 = 0.227f,
                    dy3 = 0.494f,
                )
                // a 0.82 0.82 0 0 0 0.548 0.204
                arcToRelative(
                    a = 0.82f,
                    b = 0.82f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.548f,
                    dy1 = 0.204f,
                )
                // H 11.6
                horizontalLineTo(x = 11.6f)
                // c 1.875 0 3.4 -1.434 3.4 -3.197
                curveToRelative(
                    dx1 = 1.875f,
                    dy1 = 0.0f,
                    dx2 = 3.4f,
                    dy2 = -1.434f,
                    dx3 = 3.4f,
                    dy3 = -3.197f,
                )
                // c -0.001 -1.76 -1.525 -3.195 -3.4 -3.195z
                curveToRelative(
                    dx1 = -0.001f,
                    dy1 = -1.76f,
                    dx2 = -1.525f,
                    dy2 = -3.195f,
                    dx3 = -3.4f,
                    dy3 = -3.195f,
                )
                close()
                // M 3.5 1.75
                moveTo(x = 3.5f, y = 1.75f)
                // a 0.75 0.75 0 1 1 -1.5 0
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.5f,
                    dy1 = 0.0f,
                )
                // a 0.75 0.75 0 0 1 1.5 0z
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.5f,
                    dy1 = 0.0f,
                )
                close()
            }
            // M6 5 a1 1 0 1 0 0 -2 1 1 0 0 0 0 2Z m-1 9 a1 1 0 1 0 0 -2 1 1 0 0 0 0 2Z m-3.25 -1.5 a.75 .75 0 1 0 0 -1.5 .75 .75 0 0 0 0 1.5Z m7.75 -.75 a.75 .75 0 1 1 -1.5 0 .75 .75 0 0 1 1.5 0Z M15 7 a1 1 0 1 0 0 -2 1 1 0 0 0 0 2Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6 5
                moveTo(x = 6.0f, y = 5.0f)
                // a 1 1 0 1 0 0 -2
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -2.0f,
                )
                // a 1 1 0 0 0 0 2z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 2.0f,
                )
                close()
                // m -1 9
                moveToRelative(dx = -1.0f, dy = 9.0f)
                // a 1 1 0 1 0 0 -2
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -2.0f,
                )
                // a 1 1 0 0 0 0 2z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 2.0f,
                )
                close()
                // m -3.25 -1.5
                moveToRelative(dx = -3.25f, dy = -1.5f)
                // a 0.75 0.75 0 1 0 0 -1.5
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.5f,
                )
                // a 0.75 0.75 0 0 0 0 1.5z
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.5f,
                )
                close()
                // m 7.75 -0.75
                moveToRelative(dx = 7.75f, dy = -0.75f)
                // a 0.75 0.75 0 1 1 -1.5 0
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.5f,
                    dy1 = 0.0f,
                )
                // a 0.75 0.75 0 0 1 1.5 0z
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.5f,
                    dy1 = 0.0f,
                )
                close()
                // M 15 7
                moveTo(x = 15.0f, y = 7.0f)
                // a 1 1 0 1 0 0 -2
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -2.0f,
                )
                // a 1 1 0 0 0 0 2z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 2.0f,
                )
                close()
            }
        }.build().also { _ic2070 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2070: ImageVector? = null
