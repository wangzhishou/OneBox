package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2550: ImageVector
    get() {
        val current = _ic2550
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2550",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M6.776 15.74 a.523 .523 0 0 1 -.453 -.264 .507 .507 0 0 1 .013 -.519 l.863 -1.442 a.514 .514 0 0 1 .53 -.246 c.068 .01 .133 .035 .192 .072 a.516 .516 0 0 1 .24 .32 .506 .506 0 0 1 -.065 .392 l-.861 1.443 a.552 .552 0 0 1 -.459 .243Z M16 8.647 V8.3 c-.017 -.145 -.034 -.29 -.053 -.434 -.3 -1.495 -1.531 -2.625 -3.15 -2.799 -.16 -.017 -.318 -.017 -.476 -.017 -.458 0 -.933 .07 -1.338 .243 -.017 0 -.017 0 -.035 .017 a4.933 4.933 0 0 0 -.563 -1.46 l-.053 -.103 s0 -.018 -.017 -.018 a5.488 5.488 0 0 0 -2.024 -2 A5.57 5.57 0 0 0 5.527 1 a5.479 5.479 0 0 0 -3.204 1.008 5.352 5.352 0 0 0 -1.302 1.287 5 5 0 0 0 -.634 1.164 c-.017 .035 -.035 .087 -.053 .122 A5.545 5.545 0 0 0 0 6.457 c0 .453 .053 .887 .159 1.287 .02 .05 .031 .103 .035 .157 0 .034 .017 .052 .017 .086 a5.457 5.457 0 0 0 1.973 2.826 5.564 5.564 0 0 0 3.29 1.102 h7.093 c1.848 0 3.361 -1.442 3.432 -3.25 L16 8.648Z M9.716 16 a.522 .522 0 0 1 -.453 -.263 .507 .507 0 0 1 .013 -.52 l1.039 -1.738 a.516 .516 0 0 1 .53 -.245 .52 .52 0 0 1 .192 .072 .514 .514 0 0 1 .247 .522 .508 .508 0 0 1 -.072 .19 l-1.038 1.739 a.552 .552 0 0 1 -.458 .243Z m-6.16 0 a.522 .522 0 0 1 -.454 -.263 .507 .507 0 0 1 .013 -.52 l1.04 -1.738 a.515 .515 0 0 1 .528 -.245 .52 .52 0 0 1 .193 .072 .515 .515 0 0 1 .247 .522 .507 .507 0 0 1 -.072 .19 l-1.038 1.739 a.55 .55 0 0 1 -.458 .243Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6.776 15.74
                moveTo(x = 6.776f, y = 15.74f)
                // a 0.523 0.523 0 0 1 -0.453 -0.264
                arcToRelative(
                    a = 0.523f,
                    b = 0.523f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.453f,
                    dy1 = -0.264f,
                )
                // a 0.507 0.507 0 0 1 0.013 -0.519
                arcToRelative(
                    a = 0.507f,
                    b = 0.507f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.013f,
                    dy1 = -0.519f,
                )
                // l 0.863 -1.442
                lineToRelative(dx = 0.863f, dy = -1.442f)
                // a 0.514 0.514 0 0 1 0.53 -0.246
                arcToRelative(
                    a = 0.514f,
                    b = 0.514f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.53f,
                    dy1 = -0.246f,
                )
                // c 0.068 0.01 0.133 0.035 0.192 0.072
                curveToRelative(
                    dx1 = 0.068f,
                    dy1 = 0.01f,
                    dx2 = 0.133f,
                    dy2 = 0.035f,
                    dx3 = 0.192f,
                    dy3 = 0.072f,
                )
                // a 0.516 0.516 0 0 1 0.24 0.32
                arcToRelative(
                    a = 0.516f,
                    b = 0.516f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.24f,
                    dy1 = 0.32f,
                )
                // a 0.506 0.506 0 0 1 -0.065 0.392
                arcToRelative(
                    a = 0.506f,
                    b = 0.506f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.065f,
                    dy1 = 0.392f,
                )
                // l -0.861 1.443
                lineToRelative(dx = -0.861f, dy = 1.443f)
                // a 0.552 0.552 0 0 1 -0.459 0.243z
                arcToRelative(
                    a = 0.552f,
                    b = 0.552f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.459f,
                    dy1 = 0.243f,
                )
                close()
                // M 16 8.647
                moveTo(x = 16.0f, y = 8.647f)
                // V 8.3
                verticalLineTo(y = 8.3f)
                // c -0.017 -0.145 -0.034 -0.29 -0.053 -0.434
                curveToRelative(
                    dx1 = -0.017f,
                    dy1 = -0.145f,
                    dx2 = -0.034f,
                    dy2 = -0.29f,
                    dx3 = -0.053f,
                    dy3 = -0.434f,
                )
                // c -0.3 -1.495 -1.531 -2.625 -3.15 -2.799
                curveToRelative(
                    dx1 = -0.3f,
                    dy1 = -1.495f,
                    dx2 = -1.531f,
                    dy2 = -2.625f,
                    dx3 = -3.15f,
                    dy3 = -2.799f,
                )
                // c -0.16 -0.017 -0.318 -0.017 -0.476 -0.017
                curveToRelative(
                    dx1 = -0.16f,
                    dy1 = -0.017f,
                    dx2 = -0.318f,
                    dy2 = -0.017f,
                    dx3 = -0.476f,
                    dy3 = -0.017f,
                )
                // c -0.458 0 -0.933 0.07 -1.338 0.243
                curveToRelative(
                    dx1 = -0.458f,
                    dy1 = 0.0f,
                    dx2 = -0.933f,
                    dy2 = 0.07f,
                    dx3 = -1.338f,
                    dy3 = 0.243f,
                )
                // c -0.017 0 -0.017 0 -0.035 0.017
                curveToRelative(
                    dx1 = -0.017f,
                    dy1 = 0.0f,
                    dx2 = -0.017f,
                    dy2 = 0.0f,
                    dx3 = -0.035f,
                    dy3 = 0.017f,
                )
                // a 4.933 4.933 0 0 0 -0.563 -1.46
                arcToRelative(
                    a = 4.933f,
                    b = 4.933f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.563f,
                    dy1 = -1.46f,
                )
                // l -0.053 -0.103
                lineToRelative(dx = -0.053f, dy = -0.103f)
                // s 0 -0.018 -0.017 -0.018
                reflectiveCurveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.018f,
                    dx2 = -0.017f,
                    dy2 = -0.018f,
                )
                // a 5.488 5.488 0 0 0 -2.024 -2
                arcToRelative(
                    a = 5.488f,
                    b = 5.488f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.024f,
                    dy1 = -2.0f,
                )
                // A 5.57 5.57 0 0 0 5.527 1
                arcTo(
                    horizontalEllipseRadius = 5.57f,
                    verticalEllipseRadius = 5.57f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 5.527f,
                    y1 = 1.0f,
                )
                // a 5.479 5.479 0 0 0 -3.204 1.008
                arcToRelative(
                    a = 5.479f,
                    b = 5.479f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -3.204f,
                    dy1 = 1.008f,
                )
                // a 5.352 5.352 0 0 0 -1.302 1.287
                arcToRelative(
                    a = 5.352f,
                    b = 5.352f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.302f,
                    dy1 = 1.287f,
                )
                // a 5 5 0 0 0 -0.634 1.164
                arcToRelative(
                    a = 5.0f,
                    b = 5.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.634f,
                    dy1 = 1.164f,
                )
                // c -0.017 0.035 -0.035 0.087 -0.053 0.122
                curveToRelative(
                    dx1 = -0.017f,
                    dy1 = 0.035f,
                    dx2 = -0.035f,
                    dy2 = 0.087f,
                    dx3 = -0.053f,
                    dy3 = 0.122f,
                )
                // A 5.545 5.545 0 0 0 0 6.457
                arcTo(
                    horizontalEllipseRadius = 5.545f,
                    verticalEllipseRadius = 5.545f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 0.0f,
                    y1 = 6.457f,
                )
                // c 0 0.453 0.053 0.887 0.159 1.287
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.453f,
                    dx2 = 0.053f,
                    dy2 = 0.887f,
                    dx3 = 0.159f,
                    dy3 = 1.287f,
                )
                // c 0.02 0.05 0.031 0.103 0.035 0.157
                curveToRelative(
                    dx1 = 0.02f,
                    dy1 = 0.05f,
                    dx2 = 0.031f,
                    dy2 = 0.103f,
                    dx3 = 0.035f,
                    dy3 = 0.157f,
                )
                // c 0 0.034 0.017 0.052 0.017 0.086
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.034f,
                    dx2 = 0.017f,
                    dy2 = 0.052f,
                    dx3 = 0.017f,
                    dy3 = 0.086f,
                )
                // a 5.457 5.457 0 0 0 1.973 2.826
                arcToRelative(
                    a = 5.457f,
                    b = 5.457f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.973f,
                    dy1 = 2.826f,
                )
                // a 5.564 5.564 0 0 0 3.29 1.102
                arcToRelative(
                    a = 5.564f,
                    b = 5.564f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 3.29f,
                    dy1 = 1.102f,
                )
                // h 7.093
                horizontalLineToRelative(dx = 7.093f)
                // c 1.848 0 3.361 -1.442 3.432 -3.25
                curveToRelative(
                    dx1 = 1.848f,
                    dy1 = 0.0f,
                    dx2 = 3.361f,
                    dy2 = -1.442f,
                    dx3 = 3.432f,
                    dy3 = -3.25f,
                )
                // L 16 8.648z
                lineTo(x = 16.0f, y = 8.648f)
                close()
                // M 9.716 16
                moveTo(x = 9.716f, y = 16.0f)
                // a 0.522 0.522 0 0 1 -0.453 -0.263
                arcToRelative(
                    a = 0.522f,
                    b = 0.522f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.453f,
                    dy1 = -0.263f,
                )
                // a 0.507 0.507 0 0 1 0.013 -0.52
                arcToRelative(
                    a = 0.507f,
                    b = 0.507f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.013f,
                    dy1 = -0.52f,
                )
                // l 1.039 -1.738
                lineToRelative(dx = 1.039f, dy = -1.738f)
                // a 0.516 0.516 0 0 1 0.53 -0.245
                arcToRelative(
                    a = 0.516f,
                    b = 0.516f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.53f,
                    dy1 = -0.245f,
                )
                // a 0.52 0.52 0 0 1 0.192 0.072
                arcToRelative(
                    a = 0.52f,
                    b = 0.52f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.192f,
                    dy1 = 0.072f,
                )
                // a 0.514 0.514 0 0 1 0.247 0.522
                arcToRelative(
                    a = 0.514f,
                    b = 0.514f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.247f,
                    dy1 = 0.522f,
                )
                // a 0.508 0.508 0 0 1 -0.072 0.19
                arcToRelative(
                    a = 0.508f,
                    b = 0.508f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.072f,
                    dy1 = 0.19f,
                )
                // l -1.038 1.739
                lineToRelative(dx = -1.038f, dy = 1.739f)
                // a 0.552 0.552 0 0 1 -0.458 0.243z
                arcToRelative(
                    a = 0.552f,
                    b = 0.552f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.458f,
                    dy1 = 0.243f,
                )
                close()
                // m -6.16 0
                moveToRelative(dx = -6.16f, dy = 0.0f)
                // a 0.522 0.522 0 0 1 -0.454 -0.263
                arcToRelative(
                    a = 0.522f,
                    b = 0.522f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.454f,
                    dy1 = -0.263f,
                )
                // a 0.507 0.507 0 0 1 0.013 -0.52
                arcToRelative(
                    a = 0.507f,
                    b = 0.507f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.013f,
                    dy1 = -0.52f,
                )
                // l 1.04 -1.738
                lineToRelative(dx = 1.04f, dy = -1.738f)
                // a 0.515 0.515 0 0 1 0.528 -0.245
                arcToRelative(
                    a = 0.515f,
                    b = 0.515f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.528f,
                    dy1 = -0.245f,
                )
                // a 0.52 0.52 0 0 1 0.193 0.072
                arcToRelative(
                    a = 0.52f,
                    b = 0.52f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.193f,
                    dy1 = 0.072f,
                )
                // a 0.515 0.515 0 0 1 0.247 0.522
                arcToRelative(
                    a = 0.515f,
                    b = 0.515f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.247f,
                    dy1 = 0.522f,
                )
                // a 0.507 0.507 0 0 1 -0.072 0.19
                arcToRelative(
                    a = 0.507f,
                    b = 0.507f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.072f,
                    dy1 = 0.19f,
                )
                // l -1.038 1.739
                lineToRelative(dx = -1.038f, dy = 1.739f)
                // a 0.55 0.55 0 0 1 -0.458 0.243z
                arcToRelative(
                    a = 0.55f,
                    b = 0.55f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.458f,
                    dy1 = 0.243f,
                )
                close()
            }
        }.build().also { _ic2550 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2550: ImageVector? = null
