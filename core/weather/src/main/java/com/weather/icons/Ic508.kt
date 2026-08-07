package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic508: ImageVector
    get() {
        val current = _ic508
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic508",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // m15.914 5.254 -1.571 -1.692 c-.141 -.152 -.343 -.007 -.343 .246 v.359 H1 c-.552 0 -1 .597 -1 1.333 s.448 1.333 1 1.333 h13 v.359 c0 .253 .202 .398 .343 .246 l1.571 -1.692 c.115 -.123 .115 -.369 0 -.492Z m-1.358 -.287 .495 .533 -.495 .533 H1.007 a.215 .215 0 0 1 -.074 -.07 A.786 .786 0 0 1 .8 5.5 c0 -.212 .065 -.373 .133 -.463 a.215 .215 0 0 1 .074 -.07 h13.549Z m1.358 5.287 -1.571 -1.692 c-.141 -.152 -.343 -.007 -.343 .246 v.359 H1 c-.552 0 -1 .597 -1 1.333 s.448 1.333 1 1.333 h13 v.359 c0 .253 .202 .398 .343 .246 l1.571 -1.692 c.115 -.123 .115 -.369 0 -.492Z m-1.358 -.287 .495 .533 -.495 .533 H1.007 a.214 .214 0 0 1 -.074 -.07 A.786 .786 0 0 1 .8 10.5 c0 -.212 .065 -.373 .133 -.463 a.215 .215 0 0 1 .074 -.07 h13.549Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 15.914 5.254
                moveTo(x = 15.914f, y = 5.254f)
                // l -1.571 -1.692
                lineToRelative(dx = -1.571f, dy = -1.692f)
                // c -0.141 -0.152 -0.343 -0.007 -0.343 0.246
                curveToRelative(
                    dx1 = -0.141f,
                    dy1 = -0.152f,
                    dx2 = -0.343f,
                    dy2 = -0.007f,
                    dx3 = -0.343f,
                    dy3 = 0.246f,
                )
                // v 0.359
                verticalLineToRelative(dy = 0.359f)
                // H 1
                horizontalLineTo(x = 1.0f)
                // c -0.552 0 -1 0.597 -1 1.333
                curveToRelative(
                    dx1 = -0.552f,
                    dy1 = 0.0f,
                    dx2 = -1.0f,
                    dy2 = 0.597f,
                    dx3 = -1.0f,
                    dy3 = 1.333f,
                )
                // s 0.448 1.333 1 1.333
                reflectiveCurveToRelative(
                    dx1 = 0.448f,
                    dy1 = 1.333f,
                    dx2 = 1.0f,
                    dy2 = 1.333f,
                )
                // h 13
                horizontalLineToRelative(dx = 13.0f)
                // v 0.359
                verticalLineToRelative(dy = 0.359f)
                // c 0 0.253 0.202 0.398 0.343 0.246
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.253f,
                    dx2 = 0.202f,
                    dy2 = 0.398f,
                    dx3 = 0.343f,
                    dy3 = 0.246f,
                )
                // l 1.571 -1.692
                lineToRelative(dx = 1.571f, dy = -1.692f)
                // c 0.115 -0.123 0.115 -0.369 0 -0.492z
                curveToRelative(
                    dx1 = 0.115f,
                    dy1 = -0.123f,
                    dx2 = 0.115f,
                    dy2 = -0.369f,
                    dx3 = 0.0f,
                    dy3 = -0.492f,
                )
                close()
                // m -1.358 -0.287
                moveToRelative(dx = -1.358f, dy = -0.287f)
                // l 0.495 0.533
                lineToRelative(dx = 0.495f, dy = 0.533f)
                // l -0.495 0.533
                lineToRelative(dx = -0.495f, dy = 0.533f)
                // H 1.007
                horizontalLineTo(x = 1.007f)
                // a 0.215 0.215 0 0 1 -0.074 -0.07
                arcToRelative(
                    a = 0.215f,
                    b = 0.215f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.074f,
                    dy1 = -0.07f,
                )
                // A 0.786 0.786 0 0 1 0.8 5.5
                arcTo(
                    horizontalEllipseRadius = 0.786f,
                    verticalEllipseRadius = 0.786f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 0.8f,
                    y1 = 5.5f,
                )
                // c 0 -0.212 0.065 -0.373 0.133 -0.463
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.212f,
                    dx2 = 0.065f,
                    dy2 = -0.373f,
                    dx3 = 0.133f,
                    dy3 = -0.463f,
                )
                // a 0.215 0.215 0 0 1 0.074 -0.07
                arcToRelative(
                    a = 0.215f,
                    b = 0.215f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.074f,
                    dy1 = -0.07f,
                )
                // h 13.549z
                horizontalLineToRelative(dx = 13.549f)
                close()
                // m 1.358 5.287
                moveToRelative(dx = 1.358f, dy = 5.287f)
                // l -1.571 -1.692
                lineToRelative(dx = -1.571f, dy = -1.692f)
                // c -0.141 -0.152 -0.343 -0.007 -0.343 0.246
                curveToRelative(
                    dx1 = -0.141f,
                    dy1 = -0.152f,
                    dx2 = -0.343f,
                    dy2 = -0.007f,
                    dx3 = -0.343f,
                    dy3 = 0.246f,
                )
                // v 0.359
                verticalLineToRelative(dy = 0.359f)
                // H 1
                horizontalLineTo(x = 1.0f)
                // c -0.552 0 -1 0.597 -1 1.333
                curveToRelative(
                    dx1 = -0.552f,
                    dy1 = 0.0f,
                    dx2 = -1.0f,
                    dy2 = 0.597f,
                    dx3 = -1.0f,
                    dy3 = 1.333f,
                )
                // s 0.448 1.333 1 1.333
                reflectiveCurveToRelative(
                    dx1 = 0.448f,
                    dy1 = 1.333f,
                    dx2 = 1.0f,
                    dy2 = 1.333f,
                )
                // h 13
                horizontalLineToRelative(dx = 13.0f)
                // v 0.359
                verticalLineToRelative(dy = 0.359f)
                // c 0 0.253 0.202 0.398 0.343 0.246
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.253f,
                    dx2 = 0.202f,
                    dy2 = 0.398f,
                    dx3 = 0.343f,
                    dy3 = 0.246f,
                )
                // l 1.571 -1.692
                lineToRelative(dx = 1.571f, dy = -1.692f)
                // c 0.115 -0.123 0.115 -0.369 0 -0.492z
                curveToRelative(
                    dx1 = 0.115f,
                    dy1 = -0.123f,
                    dx2 = 0.115f,
                    dy2 = -0.369f,
                    dx3 = 0.0f,
                    dy3 = -0.492f,
                )
                close()
                // m -1.358 -0.287
                moveToRelative(dx = -1.358f, dy = -0.287f)
                // l 0.495 0.533
                lineToRelative(dx = 0.495f, dy = 0.533f)
                // l -0.495 0.533
                lineToRelative(dx = -0.495f, dy = 0.533f)
                // H 1.007
                horizontalLineTo(x = 1.007f)
                // a 0.214 0.214 0 0 1 -0.074 -0.07
                arcToRelative(
                    a = 0.214f,
                    b = 0.214f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.074f,
                    dy1 = -0.07f,
                )
                // A 0.786 0.786 0 0 1 0.8 10.5
                arcTo(
                    horizontalEllipseRadius = 0.786f,
                    verticalEllipseRadius = 0.786f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 0.8f,
                    y1 = 10.5f,
                )
                // c 0 -0.212 0.065 -0.373 0.133 -0.463
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.212f,
                    dx2 = 0.065f,
                    dy2 = -0.373f,
                    dx3 = 0.133f,
                    dy3 = -0.463f,
                )
                // a 0.215 0.215 0 0 1 0.074 -0.07
                arcToRelative(
                    a = 0.215f,
                    b = 0.215f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.074f,
                    dy1 = -0.07f,
                )
                // h 13.549z
                horizontalLineToRelative(dx = 13.549f)
                close()
            }
            // M11.978 3.146 c-.464 .32 -1.128 .234 -1.516 -.156 -.35 -.351 -.662 -.605 -.938 -.76 -.441 -.265 -.961 -.397 -1.56 -.397 -.584 0 -1.08 .167 -1.49 .5 a1.614 1.614 0 0 0 -.591 1.292 c0 .189 .025 .37 .076 .542 H3.847 a3.304 3.304 0 0 1 -.045 -.542 c0 -1.014 .394 -1.868 1.182 -2.563 C5.788 .355 6.821 0 8.082 0 c.977 0 1.828 .222 2.553 .667 .515 .305 1.021 .719 1.52 1.24 .36 .377 .263 .936 -.177 1.239Z M5.63 6.833 a35.33 35.33 0 0 0 1.932 1.375 c.516 .342 .967 .661 1.352 .959 h2.963 c-.634 -.717 -1.576 -1.474 -2.826 -2.271 a61.018 61.018 0 0 1 -.095 -.063 H5.63Z m5.191 5 H13 c0 1.125 -.497 2.104 -1.49 2.938 C10.533 15.59 9.39 16 8.082 16 c-2.038 0 -3.686 -.887 -4.946 -2.662 -.284 -.4 -.105 -.918 .36 -1.154 a1.175 1.175 0 0 1 1.43 .328 c.886 1.159 1.859 1.738 2.92 1.738 .819 0 1.52 -.23 2.104 -.688 .583 -.458 .875 -.993 .875 -1.604 a2.26 2.26 0 0 0 -.004 -.125Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.978 3.146
                moveTo(x = 11.978f, y = 3.146f)
                // c -0.464 0.32 -1.128 0.234 -1.516 -0.156
                curveToRelative(
                    dx1 = -0.464f,
                    dy1 = 0.32f,
                    dx2 = -1.128f,
                    dy2 = 0.234f,
                    dx3 = -1.516f,
                    dy3 = -0.156f,
                )
                // c -0.35 -0.351 -0.662 -0.605 -0.938 -0.76
                curveToRelative(
                    dx1 = -0.35f,
                    dy1 = -0.351f,
                    dx2 = -0.662f,
                    dy2 = -0.605f,
                    dx3 = -0.938f,
                    dy3 = -0.76f,
                )
                // c -0.441 -0.265 -0.961 -0.397 -1.56 -0.397
                curveToRelative(
                    dx1 = -0.441f,
                    dy1 = -0.265f,
                    dx2 = -0.961f,
                    dy2 = -0.397f,
                    dx3 = -1.56f,
                    dy3 = -0.397f,
                )
                // c -0.584 0 -1.08 0.167 -1.49 0.5
                curveToRelative(
                    dx1 = -0.584f,
                    dy1 = 0.0f,
                    dx2 = -1.08f,
                    dy2 = 0.167f,
                    dx3 = -1.49f,
                    dy3 = 0.5f,
                )
                // a 1.614 1.614 0 0 0 -0.591 1.292
                arcToRelative(
                    a = 1.614f,
                    b = 1.614f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.591f,
                    dy1 = 1.292f,
                )
                // c 0 0.189 0.025 0.37 0.076 0.542
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.189f,
                    dx2 = 0.025f,
                    dy2 = 0.37f,
                    dx3 = 0.076f,
                    dy3 = 0.542f,
                )
                // H 3.847
                horizontalLineTo(x = 3.847f)
                // a 3.304 3.304 0 0 1 -0.045 -0.542
                arcToRelative(
                    a = 3.304f,
                    b = 3.304f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.045f,
                    dy1 = -0.542f,
                )
                // c 0 -1.014 0.394 -1.868 1.182 -2.563
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.014f,
                    dx2 = 0.394f,
                    dy2 = -1.868f,
                    dx3 = 1.182f,
                    dy3 = -2.563f,
                )
                // C 5.788 0.355 6.821 0 8.082 0
                curveTo(
                    x1 = 5.788f,
                    y1 = 0.355f,
                    x2 = 6.821f,
                    y2 = 0.0f,
                    x3 = 8.082f,
                    y3 = 0.0f,
                )
                // c 0.977 0 1.828 0.222 2.553 0.667
                curveToRelative(
                    dx1 = 0.977f,
                    dy1 = 0.0f,
                    dx2 = 1.828f,
                    dy2 = 0.222f,
                    dx3 = 2.553f,
                    dy3 = 0.667f,
                )
                // c 0.515 0.305 1.021 0.719 1.52 1.24
                curveToRelative(
                    dx1 = 0.515f,
                    dy1 = 0.305f,
                    dx2 = 1.021f,
                    dy2 = 0.719f,
                    dx3 = 1.52f,
                    dy3 = 1.24f,
                )
                // c 0.36 0.377 0.263 0.936 -0.177 1.239z
                curveToRelative(
                    dx1 = 0.36f,
                    dy1 = 0.377f,
                    dx2 = 0.263f,
                    dy2 = 0.936f,
                    dx3 = -0.177f,
                    dy3 = 1.239f,
                )
                close()
                // M 5.63 6.833
                moveTo(x = 5.63f, y = 6.833f)
                // a 35.33 35.33 0 0 0 1.932 1.375
                arcToRelative(
                    a = 35.33f,
                    b = 35.33f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.932f,
                    dy1 = 1.375f,
                )
                // c 0.516 0.342 0.967 0.661 1.352 0.959
                curveToRelative(
                    dx1 = 0.516f,
                    dy1 = 0.342f,
                    dx2 = 0.967f,
                    dy2 = 0.661f,
                    dx3 = 1.352f,
                    dy3 = 0.959f,
                )
                // h 2.963
                horizontalLineToRelative(dx = 2.963f)
                // c -0.634 -0.717 -1.576 -1.474 -2.826 -2.271
                curveToRelative(
                    dx1 = -0.634f,
                    dy1 = -0.717f,
                    dx2 = -1.576f,
                    dy2 = -1.474f,
                    dx3 = -2.826f,
                    dy3 = -2.271f,
                )
                // a 61.018 61.018 0 0 1 -0.095 -0.063
                arcToRelative(
                    a = 61.018f,
                    b = 61.018f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.095f,
                    dy1 = -0.063f,
                )
                // H 5.63z
                horizontalLineTo(x = 5.63f)
                close()
                // m 5.191 5
                moveToRelative(dx = 5.191f, dy = 5.0f)
                // H 13
                horizontalLineTo(x = 13.0f)
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
                // a 2.26 2.26 0 0 0 -0.004 -0.125z
                arcToRelative(
                    a = 2.26f,
                    b = 2.26f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.004f,
                    dy1 = -0.125f,
                )
                close()
            }
        }.build().also { _ic508 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic508: ImageVector? = null
