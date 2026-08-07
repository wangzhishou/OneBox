package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2521: ImageVector
    get() {
        val current = _ic2521
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2521",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M10.927 11.991 a.44 .44 0 0 1 .312 .13 l.716 .716 a1.456 1.456 0 0 1 -1.023 2.485 h-.014 a1.455 1.455 0 0 1 -1.02 -2.485 l.718 -.717 a.44 .44 0 0 1 .311 -.129Z m-.41 1.472 a.574 .574 0 0 0 .404 .978 h.003 a.558 .558 0 0 0 .406 -.167 .576 .576 0 0 0 0 -.812 l-.406 -.405 -.407 .406Z m-4.868 -1.517 c.117 0 .229 .047 .312 .13 l.72 .716 a1.461 1.461 0 0 1 -.471 2.376 1.45 1.45 0 0 1 -.558 .11 H5.65 a1.456 1.456 0 0 1 -1.03 -2.486 l.716 -.717 a.441 .441 0 0 1 .312 -.129Z m.001 1.064 -.405 .404 a.577 .577 0 0 0 0 .813 .575 .575 0 0 0 .812 -.813 l-.407 -.404Z m2.777 -4.278 a.443 .443 0 0 1 .313 .13 l.717 .719 a1.457 1.457 0 0 1 0 2.055 l-.006 .005 c-.27 .263 -.634 .411 -1.012 .41 v.002 h-.023 A1.456 1.456 0 0 1 7.39 9.587 l.726 -.726 a.44 .44 0 0 1 .31 -.129Z m-.408 1.474 a.575 .575 0 0 0 -.124 .625 .574 .574 0 1 0 .937 -.625 L8.426 9.8 l-.407 .407Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10.927 11.991
                moveTo(x = 10.927f, y = 11.991f)
                // a 0.44 0.44 0 0 1 0.312 0.13
                arcToRelative(
                    a = 0.44f,
                    b = 0.44f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.312f,
                    dy1 = 0.13f,
                )
                // l 0.716 0.716
                lineToRelative(dx = 0.716f, dy = 0.716f)
                // a 1.456 1.456 0 0 1 -1.023 2.485
                arcToRelative(
                    a = 1.456f,
                    b = 1.456f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.023f,
                    dy1 = 2.485f,
                )
                // h -0.014
                horizontalLineToRelative(dx = -0.014f)
                // a 1.455 1.455 0 0 1 -1.02 -2.485
                arcToRelative(
                    a = 1.455f,
                    b = 1.455f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.02f,
                    dy1 = -2.485f,
                )
                // l 0.718 -0.717
                lineToRelative(dx = 0.718f, dy = -0.717f)
                // a 0.44 0.44 0 0 1 0.311 -0.129z
                arcToRelative(
                    a = 0.44f,
                    b = 0.44f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.311f,
                    dy1 = -0.129f,
                )
                close()
                // m -0.41 1.472
                moveToRelative(dx = -0.41f, dy = 1.472f)
                // a 0.574 0.574 0 0 0 0.404 0.978
                arcToRelative(
                    a = 0.574f,
                    b = 0.574f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.404f,
                    dy1 = 0.978f,
                )
                // h 0.003
                horizontalLineToRelative(dx = 0.003f)
                // a 0.558 0.558 0 0 0 0.406 -0.167
                arcToRelative(
                    a = 0.558f,
                    b = 0.558f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.406f,
                    dy1 = -0.167f,
                )
                // a 0.576 0.576 0 0 0 0 -0.812
                arcToRelative(
                    a = 0.576f,
                    b = 0.576f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.812f,
                )
                // l -0.406 -0.405
                lineToRelative(dx = -0.406f, dy = -0.405f)
                // l -0.407 0.406z
                lineToRelative(dx = -0.407f, dy = 0.406f)
                close()
                // m -4.868 -1.517
                moveToRelative(dx = -4.868f, dy = -1.517f)
                // c 0.117 0 0.229 0.047 0.312 0.13
                curveToRelative(
                    dx1 = 0.117f,
                    dy1 = 0.0f,
                    dx2 = 0.229f,
                    dy2 = 0.047f,
                    dx3 = 0.312f,
                    dy3 = 0.13f,
                )
                // l 0.72 0.716
                lineToRelative(dx = 0.72f, dy = 0.716f)
                // a 1.461 1.461 0 0 1 -0.471 2.376
                arcToRelative(
                    a = 1.461f,
                    b = 1.461f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.471f,
                    dy1 = 2.376f,
                )
                // a 1.45 1.45 0 0 1 -0.558 0.11
                arcToRelative(
                    a = 1.45f,
                    b = 1.45f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.558f,
                    dy1 = 0.11f,
                )
                // H 5.65
                horizontalLineTo(x = 5.65f)
                // a 1.456 1.456 0 0 1 -1.03 -2.486
                arcToRelative(
                    a = 1.456f,
                    b = 1.456f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.03f,
                    dy1 = -2.486f,
                )
                // l 0.716 -0.717
                lineToRelative(dx = 0.716f, dy = -0.717f)
                // a 0.441 0.441 0 0 1 0.312 -0.129z
                arcToRelative(
                    a = 0.441f,
                    b = 0.441f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.312f,
                    dy1 = -0.129f,
                )
                close()
                // m 0.001 1.064
                moveToRelative(dx = 0.001f, dy = 1.064f)
                // l -0.405 0.404
                lineToRelative(dx = -0.405f, dy = 0.404f)
                // a 0.577 0.577 0 0 0 0 0.813
                arcToRelative(
                    a = 0.577f,
                    b = 0.577f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.813f,
                )
                // a 0.575 0.575 0 0 0 0.812 -0.813
                arcToRelative(
                    a = 0.575f,
                    b = 0.575f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.812f,
                    dy1 = -0.813f,
                )
                // l -0.407 -0.404z
                lineToRelative(dx = -0.407f, dy = -0.404f)
                close()
                // m 2.777 -4.278
                moveToRelative(dx = 2.777f, dy = -4.278f)
                // a 0.443 0.443 0 0 1 0.313 0.13
                arcToRelative(
                    a = 0.443f,
                    b = 0.443f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.313f,
                    dy1 = 0.13f,
                )
                // l 0.717 0.719
                lineToRelative(dx = 0.717f, dy = 0.719f)
                // a 1.457 1.457 0 0 1 0 2.055
                arcToRelative(
                    a = 1.457f,
                    b = 1.457f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 2.055f,
                )
                // l -0.006 0.005
                lineToRelative(dx = -0.006f, dy = 0.005f)
                // c -0.27 0.263 -0.634 0.411 -1.012 0.41
                curveToRelative(
                    dx1 = -0.27f,
                    dy1 = 0.263f,
                    dx2 = -0.634f,
                    dy2 = 0.411f,
                    dx3 = -1.012f,
                    dy3 = 0.41f,
                )
                // v 0.002
                verticalLineToRelative(dy = 0.002f)
                // h -0.023
                horizontalLineToRelative(dx = -0.023f)
                // A 1.456 1.456 0 0 1 7.39 9.587
                arcTo(
                    horizontalEllipseRadius = 1.456f,
                    verticalEllipseRadius = 1.456f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.39f,
                    y1 = 9.587f,
                )
                // l 0.726 -0.726
                lineToRelative(dx = 0.726f, dy = -0.726f)
                // a 0.44 0.44 0 0 1 0.31 -0.129z
                arcToRelative(
                    a = 0.44f,
                    b = 0.44f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.31f,
                    dy1 = -0.129f,
                )
                close()
                // m -0.408 1.474
                moveToRelative(dx = -0.408f, dy = 1.474f)
                // a 0.575 0.575 0 0 0 -0.124 0.625
                arcToRelative(
                    a = 0.575f,
                    b = 0.575f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.124f,
                    dy1 = 0.625f,
                )
                // a 0.574 0.574 0 1 0 0.937 -0.625
                arcToRelative(
                    a = 0.574f,
                    b = 0.574f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.937f,
                    dy1 = -0.625f,
                )
                // L 8.426 9.8
                lineTo(x = 8.426f, y = 9.8f)
                // l -0.407 0.407z
                lineToRelative(dx = -0.407f, dy = 0.407f)
                close()
            }
            // M7.927 1 a4.182 4.182 0 0 1 4.144 3.651 3.607 3.607 0 0 1 1.286 7.068 .442 .442 0 0 1 -.235 -.85 2.725 2.725 0 0 0 -1.357 -5.276 .442 .442 0 0 1 -.542 -.427 3.3 3.3 0 0 0 -3.295 -3.285 h-.011 a3.295 3.295 0 0 0 -3.274 3.038 A3.606 3.606 0 0 1 6.731 6.57 a.443 .443 0 0 1 -.032 .489 .446 .446 0 0 1 -.292 .169 .443 .443 0 0 1 -.44 -.217 2.725 2.725 0 0 0 -1.79 -1.304 l-.01 .001 a.44 .44 0 0 1 -.122 -.025 2.725 2.725 0 1 0 -.56 5.41 .44 .44 0 0 1 -.019 .88 h-.02 a3.608 3.608 0 0 1 -1.589 -6.756 3.607 3.607 0 0 1 1.792 -.452 c.04 0 .08 .003 .121 .005 A4.176 4.176 0 0 1 7.913 1 h.014Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.927 1
                moveTo(x = 7.927f, y = 1.0f)
                // a 4.182 4.182 0 0 1 4.144 3.651
                arcToRelative(
                    a = 4.182f,
                    b = 4.182f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 4.144f,
                    dy1 = 3.651f,
                )
                // a 3.607 3.607 0 0 1 1.286 7.068
                arcToRelative(
                    a = 3.607f,
                    b = 3.607f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.286f,
                    dy1 = 7.068f,
                )
                // a 0.442 0.442 0 0 1 -0.235 -0.85
                arcToRelative(
                    a = 0.442f,
                    b = 0.442f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.235f,
                    dy1 = -0.85f,
                )
                // a 2.725 2.725 0 0 0 -1.357 -5.276
                arcToRelative(
                    a = 2.725f,
                    b = 2.725f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.357f,
                    dy1 = -5.276f,
                )
                // a 0.442 0.442 0 0 1 -0.542 -0.427
                arcToRelative(
                    a = 0.442f,
                    b = 0.442f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.542f,
                    dy1 = -0.427f,
                )
                // a 3.3 3.3 0 0 0 -3.295 -3.285
                arcToRelative(
                    a = 3.3f,
                    b = 3.3f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -3.295f,
                    dy1 = -3.285f,
                )
                // h -0.011
                horizontalLineToRelative(dx = -0.011f)
                // a 3.295 3.295 0 0 0 -3.274 3.038
                arcToRelative(
                    a = 3.295f,
                    b = 3.295f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -3.274f,
                    dy1 = 3.038f,
                )
                // A 3.606 3.606 0 0 1 6.731 6.57
                arcTo(
                    horizontalEllipseRadius = 3.606f,
                    verticalEllipseRadius = 3.606f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 6.731f,
                    y1 = 6.57f,
                )
                // a 0.443 0.443 0 0 1 -0.032 0.489
                arcToRelative(
                    a = 0.443f,
                    b = 0.443f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.032f,
                    dy1 = 0.489f,
                )
                // a 0.446 0.446 0 0 1 -0.292 0.169
                arcToRelative(
                    a = 0.446f,
                    b = 0.446f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.292f,
                    dy1 = 0.169f,
                )
                // a 0.443 0.443 0 0 1 -0.44 -0.217
                arcToRelative(
                    a = 0.443f,
                    b = 0.443f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.44f,
                    dy1 = -0.217f,
                )
                // a 2.725 2.725 0 0 0 -1.79 -1.304
                arcToRelative(
                    a = 2.725f,
                    b = 2.725f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.79f,
                    dy1 = -1.304f,
                )
                // l -0.01 0.001
                lineToRelative(dx = -0.01f, dy = 0.001f)
                // a 0.44 0.44 0 0 1 -0.122 -0.025
                arcToRelative(
                    a = 0.44f,
                    b = 0.44f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.122f,
                    dy1 = -0.025f,
                )
                // a 2.725 2.725 0 1 0 -0.56 5.41
                arcToRelative(
                    a = 2.725f,
                    b = 2.725f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.56f,
                    dy1 = 5.41f,
                )
                // a 0.44 0.44 0 0 1 -0.019 0.88
                arcToRelative(
                    a = 0.44f,
                    b = 0.44f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.019f,
                    dy1 = 0.88f,
                )
                // h -0.02
                horizontalLineToRelative(dx = -0.02f)
                // a 3.608 3.608 0 0 1 -1.589 -6.756
                arcToRelative(
                    a = 3.608f,
                    b = 3.608f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.589f,
                    dy1 = -6.756f,
                )
                // a 3.607 3.607 0 0 1 1.792 -0.452
                arcToRelative(
                    a = 3.607f,
                    b = 3.607f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.792f,
                    dy1 = -0.452f,
                )
                // c 0.04 0 0.08 0.003 0.121 0.005
                curveToRelative(
                    dx1 = 0.04f,
                    dy1 = 0.0f,
                    dx2 = 0.08f,
                    dy2 = 0.003f,
                    dx3 = 0.121f,
                    dy3 = 0.005f,
                )
                // A 4.176 4.176 0 0 1 7.913 1
                arcTo(
                    horizontalEllipseRadius = 4.176f,
                    verticalEllipseRadius = 4.176f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.913f,
                    y1 = 1.0f,
                )
                // h 0.014z
                horizontalLineToRelative(dx = 0.014f)
                close()
            }
        }.build().also { _ic2521 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2521: ImageVector? = null
