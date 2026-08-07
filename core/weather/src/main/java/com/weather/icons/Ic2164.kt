package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2164: ImageVector
    get() {
        val current = _ic2164
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2164",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M15.859 8.141 14.34 9.66 a.2 .2 0 0 1 -.34 -.143 V9 H1 a1 1 0 1 1 0 -2 h13 v-.517 a.2 .2 0 0 1 .341 -.142 L15.86 7.86 a.2 .2 0 0 1 0 .282Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 15.859 8.141
                moveTo(x = 15.859f, y = 8.141f)
                // L 14.34 9.66
                lineTo(x = 14.34f, y = 9.66f)
                // a 0.2 0.2 0 0 1 -0.34 -0.143
                arcToRelative(
                    a = 0.2f,
                    b = 0.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.34f,
                    dy1 = -0.143f,
                )
                // V 9
                verticalLineTo(y = 9.0f)
                // H 1
                horizontalLineTo(x = 1.0f)
                // a 1 1 0 1 1 0 -2
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -2.0f,
                )
                // h 13
                horizontalLineToRelative(dx = 13.0f)
                // v -0.517
                verticalLineToRelative(dy = -0.517f)
                // a 0.2 0.2 0 0 1 0.341 -0.142
                arcToRelative(
                    a = 0.2f,
                    b = 0.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.341f,
                    dy1 = -0.142f,
                )
                // L 15.86 7.86
                lineTo(x = 15.86f, y = 7.86f)
                // a 0.2 0.2 0 0 1 0 0.282z
                arcToRelative(
                    a = 0.2f,
                    b = 0.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.282f,
                )
                close()
            }
            // M10.462 2.99 c.388 .39 1.052 .476 1.516 .156 .44 -.303 .536 -.862 .177 -1.238 a7.232 7.232 0 0 0 -1.52 -1.241 C9.91 .222 9.06 0 8.082 0 6.82 0 5.788 .354 4.984 1.063 c-.788 .694 -1.182 1.548 -1.182 2.562 0 .472 .102 .944 .307 1.417 .205 .472 .528 .916 .97 1.333 .191 .174 .447 .383 .768 .625 h3.366 a24.36 24.36 0 0 0 -.162 -.104 c-1.356 -.89 -2.223 -1.528 -2.6 -1.917 -.38 -.389 -.568 -.84 -.568 -1.354 0 -.528 .197 -.958 .59 -1.292 .41 -.333 .907 -.5 1.49 -.5 .6 0 1.12 .132 1.561 .396 .276 .156 .588 .41 .938 .761Z M11.725 9 c.12 .126 .229 .251 .33 .375 .63 .778 .945 1.597 .945 2.458 0 1.125 -.497 2.104 -1.49 2.938 C10.533 15.59 9.39 16 8.082 16 c-2.038 0 -3.686 -.887 -4.946 -2.662 -.284 -.4 -.105 -.918 .36 -1.154 a1.175 1.175 0 0 1 1.43 .328 c.886 1.159 1.859 1.738 2.92 1.738 .819 0 1.52 -.23 2.104 -.688 .583 -.458 .875 -.993 .875 -1.604 0 -.61 -.245 -1.187 -.733 -1.729 -.317 -.36 -.783 -.77 -1.398 -1.229 h3.031Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10.462 2.99
                moveTo(x = 10.462f, y = 2.99f)
                // c 0.388 0.39 1.052 0.476 1.516 0.156
                curveToRelative(
                    dx1 = 0.388f,
                    dy1 = 0.39f,
                    dx2 = 1.052f,
                    dy2 = 0.476f,
                    dx3 = 1.516f,
                    dy3 = 0.156f,
                )
                // c 0.44 -0.303 0.536 -0.862 0.177 -1.238
                curveToRelative(
                    dx1 = 0.44f,
                    dy1 = -0.303f,
                    dx2 = 0.536f,
                    dy2 = -0.862f,
                    dx3 = 0.177f,
                    dy3 = -1.238f,
                )
                // a 7.232 7.232 0 0 0 -1.52 -1.241
                arcToRelative(
                    a = 7.232f,
                    b = 7.232f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.52f,
                    dy1 = -1.241f,
                )
                // C 9.91 0.222 9.06 0 8.082 0
                curveTo(
                    x1 = 9.91f,
                    y1 = 0.222f,
                    x2 = 9.06f,
                    y2 = 0.0f,
                    x3 = 8.082f,
                    y3 = 0.0f,
                )
                // C 6.82 0 5.788 0.354 4.984 1.063
                curveTo(
                    x1 = 6.82f,
                    y1 = 0.0f,
                    x2 = 5.788f,
                    y2 = 0.354f,
                    x3 = 4.984f,
                    y3 = 1.063f,
                )
                // c -0.788 0.694 -1.182 1.548 -1.182 2.562
                curveToRelative(
                    dx1 = -0.788f,
                    dy1 = 0.694f,
                    dx2 = -1.182f,
                    dy2 = 1.548f,
                    dx3 = -1.182f,
                    dy3 = 2.562f,
                )
                // c 0 0.472 0.102 0.944 0.307 1.417
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.472f,
                    dx2 = 0.102f,
                    dy2 = 0.944f,
                    dx3 = 0.307f,
                    dy3 = 1.417f,
                )
                // c 0.205 0.472 0.528 0.916 0.97 1.333
                curveToRelative(
                    dx1 = 0.205f,
                    dy1 = 0.472f,
                    dx2 = 0.528f,
                    dy2 = 0.916f,
                    dx3 = 0.97f,
                    dy3 = 1.333f,
                )
                // c 0.191 0.174 0.447 0.383 0.768 0.625
                curveToRelative(
                    dx1 = 0.191f,
                    dy1 = 0.174f,
                    dx2 = 0.447f,
                    dy2 = 0.383f,
                    dx3 = 0.768f,
                    dy3 = 0.625f,
                )
                // h 3.366
                horizontalLineToRelative(dx = 3.366f)
                // a 24.36 24.36 0 0 0 -0.162 -0.104
                arcToRelative(
                    a = 24.36f,
                    b = 24.36f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.162f,
                    dy1 = -0.104f,
                )
                // c -1.356 -0.89 -2.223 -1.528 -2.6 -1.917
                curveToRelative(
                    dx1 = -1.356f,
                    dy1 = -0.89f,
                    dx2 = -2.223f,
                    dy2 = -1.528f,
                    dx3 = -2.6f,
                    dy3 = -1.917f,
                )
                // c -0.38 -0.389 -0.568 -0.84 -0.568 -1.354
                curveToRelative(
                    dx1 = -0.38f,
                    dy1 = -0.389f,
                    dx2 = -0.568f,
                    dy2 = -0.84f,
                    dx3 = -0.568f,
                    dy3 = -1.354f,
                )
                // c 0 -0.528 0.197 -0.958 0.59 -1.292
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.528f,
                    dx2 = 0.197f,
                    dy2 = -0.958f,
                    dx3 = 0.59f,
                    dy3 = -1.292f,
                )
                // c 0.41 -0.333 0.907 -0.5 1.49 -0.5
                curveToRelative(
                    dx1 = 0.41f,
                    dy1 = -0.333f,
                    dx2 = 0.907f,
                    dy2 = -0.5f,
                    dx3 = 1.49f,
                    dy3 = -0.5f,
                )
                // c 0.6 0 1.12 0.132 1.561 0.396
                curveToRelative(
                    dx1 = 0.6f,
                    dy1 = 0.0f,
                    dx2 = 1.12f,
                    dy2 = 0.132f,
                    dx3 = 1.561f,
                    dy3 = 0.396f,
                )
                // c 0.276 0.156 0.588 0.41 0.938 0.761z
                curveToRelative(
                    dx1 = 0.276f,
                    dy1 = 0.156f,
                    dx2 = 0.588f,
                    dy2 = 0.41f,
                    dx3 = 0.938f,
                    dy3 = 0.761f,
                )
                close()
                // M 11.725 9
                moveTo(x = 11.725f, y = 9.0f)
                // c 0.12 0.126 0.229 0.251 0.33 0.375
                curveToRelative(
                    dx1 = 0.12f,
                    dy1 = 0.126f,
                    dx2 = 0.229f,
                    dy2 = 0.251f,
                    dx3 = 0.33f,
                    dy3 = 0.375f,
                )
                // c 0.63 0.778 0.945 1.597 0.945 2.458
                curveToRelative(
                    dx1 = 0.63f,
                    dy1 = 0.778f,
                    dx2 = 0.945f,
                    dy2 = 1.597f,
                    dx3 = 0.945f,
                    dy3 = 2.458f,
                )
                // c 0 1.125 -0.497 2.104 -1.49 2.938
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.125f,
                    dx2 = -0.497f,
                    dy2 = 2.104f,
                    dx3 = -1.49f,
                    dy3 = 2.938f,
                )
                // C 10.533 15.59 9.39 16 8.082 16
                curveTo(
                    x1 = 10.533f,
                    y1 = 15.59f,
                    x2 = 9.39f,
                    y2 = 16.0f,
                    x3 = 8.082f,
                    y3 = 16.0f,
                )
                // c -2.038 0 -3.686 -0.887 -4.946 -2.662
                curveToRelative(
                    dx1 = -2.038f,
                    dy1 = 0.0f,
                    dx2 = -3.686f,
                    dy2 = -0.887f,
                    dx3 = -4.946f,
                    dy3 = -2.662f,
                )
                // c -0.284 -0.4 -0.105 -0.918 0.36 -1.154
                curveToRelative(
                    dx1 = -0.284f,
                    dy1 = -0.4f,
                    dx2 = -0.105f,
                    dy2 = -0.918f,
                    dx3 = 0.36f,
                    dy3 = -1.154f,
                )
                // a 1.175 1.175 0 0 1 1.43 0.328
                arcToRelative(
                    a = 1.175f,
                    b = 1.175f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.43f,
                    dy1 = 0.328f,
                )
                // c 0.886 1.159 1.859 1.738 2.92 1.738
                curveToRelative(
                    dx1 = 0.886f,
                    dy1 = 1.159f,
                    dx2 = 1.859f,
                    dy2 = 1.738f,
                    dx3 = 2.92f,
                    dy3 = 1.738f,
                )
                // c 0.819 0 1.52 -0.23 2.104 -0.688
                curveToRelative(
                    dx1 = 0.819f,
                    dy1 = 0.0f,
                    dx2 = 1.52f,
                    dy2 = -0.23f,
                    dx3 = 2.104f,
                    dy3 = -0.688f,
                )
                // c 0.583 -0.458 0.875 -0.993 0.875 -1.604
                curveToRelative(
                    dx1 = 0.583f,
                    dy1 = -0.458f,
                    dx2 = 0.875f,
                    dy2 = -0.993f,
                    dx3 = 0.875f,
                    dy3 = -1.604f,
                )
                // c 0 -0.61 -0.245 -1.187 -0.733 -1.729
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.61f,
                    dx2 = -0.245f,
                    dy2 = -1.187f,
                    dx3 = -0.733f,
                    dy3 = -1.729f,
                )
                // c -0.317 -0.36 -0.783 -0.77 -1.398 -1.229
                curveToRelative(
                    dx1 = -0.317f,
                    dy1 = -0.36f,
                    dx2 = -0.783f,
                    dy2 = -0.77f,
                    dx3 = -1.398f,
                    dy3 = -1.229f,
                )
                // h 3.031z
                horizontalLineToRelative(dx = 3.031f)
                close()
            }
        }.build().also { _ic2164 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2164: ImageVector? = null
