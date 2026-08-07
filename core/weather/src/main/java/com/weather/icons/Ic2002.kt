package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2002: ImageVector
    get() {
        val current = _ic2002
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2002",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.138 0 c.228 0 .414 .22 .414 .491 v3.985 l1.927 -1.856 a.5 .5 0 0 1 .47 -.121 c.168 .043 .3 .17 .345 .332 a.458 .458 0 0 1 -.126 .453 L8.552 5.82 v1.905 h1.9 l2.263 -2.342 a.456 .456 0 0 1 .659 0 .496 .496 0 0 1 0 .682 l-1.604 1.66 h3.739 c.271 0 .49 .184 .491 .413 0 .228 -.22 .414 -.491 .414 h-3.98 l1.85 1.916 a.498 .498 0 0 1 .103 .527 .504 .504 0 0 1 -.412 .316 .46 .46 0 0 1 -.332 -.141 L10.21 8.552 H8.552 v1.643 l2.618 2.527 a.454 .454 0 0 1 0 .656 .491 .491 0 0 1 -.685 0 L8.552 11.54 v3.969 c0 .271 -.186 .491 -.414 .491 -.228 0 -.413 -.22 -.413 -.491 v-3.725 l-1.657 1.599 a.492 .492 0 0 1 -.346 .135 .483 .483 0 0 1 -.445 -.29 .454 .454 0 0 1 .107 -.506 l2.34 -2.26 v-1.91 H6.1 l-2.55 2.655 a.452 .452 0 0 1 -.326 .102 .466 .466 0 0 1 -.43 -.302 .5 .5 0 0 1 .103 -.528 l1.85 -1.927 H.49 c-.27 0 -.49 -.186 -.49 -.414 0 -.229 .22 -.413 .491 -.413 h3.993 l-1.59 -1.659 a.499 .499 0 0 1 0 -.683 .452 .452 0 0 1 .657 0 l2.247 2.342 h1.927 V5.523 l-2.34 -2.235 a.459 .459 0 0 1 -.144 -.331 .46 .46 0 0 1 .144 -.332 .499 .499 0 0 1 .684 0 l1.656 1.594 V.49 c0 -.271 .185 -.49 .413 -.491Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.138 0
                moveTo(x = 8.138f, y = 0.0f)
                // c 0.228 0 0.414 0.22 0.414 0.491
                curveToRelative(
                    dx1 = 0.228f,
                    dy1 = 0.0f,
                    dx2 = 0.414f,
                    dy2 = 0.22f,
                    dx3 = 0.414f,
                    dy3 = 0.491f,
                )
                // v 3.985
                verticalLineToRelative(dy = 3.985f)
                // l 1.927 -1.856
                lineToRelative(dx = 1.927f, dy = -1.856f)
                // a 0.5 0.5 0 0 1 0.47 -0.121
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.47f,
                    dy1 = -0.121f,
                )
                // c 0.168 0.043 0.3 0.17 0.345 0.332
                curveToRelative(
                    dx1 = 0.168f,
                    dy1 = 0.043f,
                    dx2 = 0.3f,
                    dy2 = 0.17f,
                    dx3 = 0.345f,
                    dy3 = 0.332f,
                )
                // a 0.458 0.458 0 0 1 -0.126 0.453
                arcToRelative(
                    a = 0.458f,
                    b = 0.458f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.126f,
                    dy1 = 0.453f,
                )
                // L 8.552 5.82
                lineTo(x = 8.552f, y = 5.82f)
                // v 1.905
                verticalLineToRelative(dy = 1.905f)
                // h 1.9
                horizontalLineToRelative(dx = 1.9f)
                // l 2.263 -2.342
                lineToRelative(dx = 2.263f, dy = -2.342f)
                // a 0.456 0.456 0 0 1 0.659 0
                arcToRelative(
                    a = 0.456f,
                    b = 0.456f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.659f,
                    dy1 = 0.0f,
                )
                // a 0.496 0.496 0 0 1 0 0.682
                arcToRelative(
                    a = 0.496f,
                    b = 0.496f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.682f,
                )
                // l -1.604 1.66
                lineToRelative(dx = -1.604f, dy = 1.66f)
                // h 3.739
                horizontalLineToRelative(dx = 3.739f)
                // c 0.271 0 0.49 0.184 0.491 0.413
                curveToRelative(
                    dx1 = 0.271f,
                    dy1 = 0.0f,
                    dx2 = 0.49f,
                    dy2 = 0.184f,
                    dx3 = 0.491f,
                    dy3 = 0.413f,
                )
                // c 0 0.228 -0.22 0.414 -0.491 0.414
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.228f,
                    dx2 = -0.22f,
                    dy2 = 0.414f,
                    dx3 = -0.491f,
                    dy3 = 0.414f,
                )
                // h -3.98
                horizontalLineToRelative(dx = -3.98f)
                // l 1.85 1.916
                lineToRelative(dx = 1.85f, dy = 1.916f)
                // a 0.498 0.498 0 0 1 0.103 0.527
                arcToRelative(
                    a = 0.498f,
                    b = 0.498f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.103f,
                    dy1 = 0.527f,
                )
                // a 0.504 0.504 0 0 1 -0.412 0.316
                arcToRelative(
                    a = 0.504f,
                    b = 0.504f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.412f,
                    dy1 = 0.316f,
                )
                // a 0.46 0.46 0 0 1 -0.332 -0.141
                arcToRelative(
                    a = 0.46f,
                    b = 0.46f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.332f,
                    dy1 = -0.141f,
                )
                // L 10.21 8.552
                lineTo(x = 10.21f, y = 8.552f)
                // H 8.552
                horizontalLineTo(x = 8.552f)
                // v 1.643
                verticalLineToRelative(dy = 1.643f)
                // l 2.618 2.527
                lineToRelative(dx = 2.618f, dy = 2.527f)
                // a 0.454 0.454 0 0 1 0 0.656
                arcToRelative(
                    a = 0.454f,
                    b = 0.454f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.656f,
                )
                // a 0.491 0.491 0 0 1 -0.685 0
                arcToRelative(
                    a = 0.491f,
                    b = 0.491f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.685f,
                    dy1 = 0.0f,
                )
                // L 8.552 11.54
                lineTo(x = 8.552f, y = 11.54f)
                // v 3.969
                verticalLineToRelative(dy = 3.969f)
                // c 0 0.271 -0.186 0.491 -0.414 0.491
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.271f,
                    dx2 = -0.186f,
                    dy2 = 0.491f,
                    dx3 = -0.414f,
                    dy3 = 0.491f,
                )
                // c -0.228 0 -0.413 -0.22 -0.413 -0.491
                curveToRelative(
                    dx1 = -0.228f,
                    dy1 = 0.0f,
                    dx2 = -0.413f,
                    dy2 = -0.22f,
                    dx3 = -0.413f,
                    dy3 = -0.491f,
                )
                // v -3.725
                verticalLineToRelative(dy = -3.725f)
                // l -1.657 1.599
                lineToRelative(dx = -1.657f, dy = 1.599f)
                // a 0.492 0.492 0 0 1 -0.346 0.135
                arcToRelative(
                    a = 0.492f,
                    b = 0.492f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.346f,
                    dy1 = 0.135f,
                )
                // a 0.483 0.483 0 0 1 -0.445 -0.29
                arcToRelative(
                    a = 0.483f,
                    b = 0.483f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.445f,
                    dy1 = -0.29f,
                )
                // a 0.454 0.454 0 0 1 0.107 -0.506
                arcToRelative(
                    a = 0.454f,
                    b = 0.454f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.107f,
                    dy1 = -0.506f,
                )
                // l 2.34 -2.26
                lineToRelative(dx = 2.34f, dy = -2.26f)
                // v -1.91
                verticalLineToRelative(dy = -1.91f)
                // H 6.1
                horizontalLineTo(x = 6.1f)
                // l -2.55 2.655
                lineToRelative(dx = -2.55f, dy = 2.655f)
                // a 0.452 0.452 0 0 1 -0.326 0.102
                arcToRelative(
                    a = 0.452f,
                    b = 0.452f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.326f,
                    dy1 = 0.102f,
                )
                // a 0.466 0.466 0 0 1 -0.43 -0.302
                arcToRelative(
                    a = 0.466f,
                    b = 0.466f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.43f,
                    dy1 = -0.302f,
                )
                // a 0.5 0.5 0 0 1 0.103 -0.528
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.103f,
                    dy1 = -0.528f,
                )
                // l 1.85 -1.927
                lineToRelative(dx = 1.85f, dy = -1.927f)
                // H 0.49
                horizontalLineTo(x = 0.49f)
                // c -0.27 0 -0.49 -0.186 -0.49 -0.414
                curveToRelative(
                    dx1 = -0.27f,
                    dy1 = 0.0f,
                    dx2 = -0.49f,
                    dy2 = -0.186f,
                    dx3 = -0.49f,
                    dy3 = -0.414f,
                )
                // c 0 -0.229 0.22 -0.413 0.491 -0.413
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.229f,
                    dx2 = 0.22f,
                    dy2 = -0.413f,
                    dx3 = 0.491f,
                    dy3 = -0.413f,
                )
                // h 3.993
                horizontalLineToRelative(dx = 3.993f)
                // l -1.59 -1.659
                lineToRelative(dx = -1.59f, dy = -1.659f)
                // a 0.499 0.499 0 0 1 0 -0.683
                arcToRelative(
                    a = 0.499f,
                    b = 0.499f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -0.683f,
                )
                // a 0.452 0.452 0 0 1 0.657 0
                arcToRelative(
                    a = 0.452f,
                    b = 0.452f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.657f,
                    dy1 = 0.0f,
                )
                // l 2.247 2.342
                lineToRelative(dx = 2.247f, dy = 2.342f)
                // h 1.927
                horizontalLineToRelative(dx = 1.927f)
                // V 5.523
                verticalLineTo(y = 5.523f)
                // l -2.34 -2.235
                lineToRelative(dx = -2.34f, dy = -2.235f)
                // a 0.459 0.459 0 0 1 -0.144 -0.331
                arcToRelative(
                    a = 0.459f,
                    b = 0.459f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.144f,
                    dy1 = -0.331f,
                )
                // a 0.46 0.46 0 0 1 0.144 -0.332
                arcToRelative(
                    a = 0.46f,
                    b = 0.46f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.144f,
                    dy1 = -0.332f,
                )
                // a 0.499 0.499 0 0 1 0.684 0
                arcToRelative(
                    a = 0.499f,
                    b = 0.499f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.684f,
                    dy1 = 0.0f,
                )
                // l 1.656 1.594
                lineToRelative(dx = 1.656f, dy = 1.594f)
                // V 0.49
                verticalLineTo(y = 0.49f)
                // c 0 -0.271 0.185 -0.49 0.413 -0.491z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.271f,
                    dx2 = 0.185f,
                    dy2 = -0.49f,
                    dx3 = 0.413f,
                    dy3 = -0.491f,
                )
                close()
            }
        }.build().also { _ic2002 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2002: ImageVector? = null
