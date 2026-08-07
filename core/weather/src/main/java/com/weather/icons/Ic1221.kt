package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1221: ImageVector
    get() {
        val current = _ic1221
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1221",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M12.533 4 c0 -.162 .048 -.395 .156 -.696 .107 -.295 .26 -.625 .447 -.973 .254 -.471 .558 -.958 .864 -1.407 .306 .449 .61 .936 .864 1.407 .186 .348 .34 .678 .447 .973 .108 .301 .156 .534 .156 .696 a1.467 1.467 0 0 1 -2.934 0Z m1.14 -3.543 C12.861 1.627 12 3.12 12 4 a2 2 0 1 0 4 0 c0 -.88 -.86 -2.373 -1.673 -3.543 A18.44 18.44 0 0 0 14 0 c-.107 .145 -.216 .298 -.327 .457Z m-8.34 3.21 C4.417 1.833 3.042 0 3.042 0 S0 4.042 0 6 a3.015 3.015 0 0 0 2.583 3 c.584 -1.708 1.709 -3.708 2.75 -5.333Z M9.25 13 a3.715 3.715 0 0 1 2.792 -3.625 C11.167 6.25 7.75 1.708 7.75 1.708 s-4.542 5.959 -4.542 8.959 c0 2.5 2 4.5 4.5 4.5 .709 0 1.334 -.167 1.959 -.459 A3.83 3.83 0 0 1 9.25 13Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 12.533 4
                moveTo(x = 12.533f, y = 4.0f)
                // c 0 -0.162 0.048 -0.395 0.156 -0.696
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.162f,
                    dx2 = 0.048f,
                    dy2 = -0.395f,
                    dx3 = 0.156f,
                    dy3 = -0.696f,
                )
                // c 0.107 -0.295 0.26 -0.625 0.447 -0.973
                curveToRelative(
                    dx1 = 0.107f,
                    dy1 = -0.295f,
                    dx2 = 0.26f,
                    dy2 = -0.625f,
                    dx3 = 0.447f,
                    dy3 = -0.973f,
                )
                // c 0.254 -0.471 0.558 -0.958 0.864 -1.407
                curveToRelative(
                    dx1 = 0.254f,
                    dy1 = -0.471f,
                    dx2 = 0.558f,
                    dy2 = -0.958f,
                    dx3 = 0.864f,
                    dy3 = -1.407f,
                )
                // c 0.306 0.449 0.61 0.936 0.864 1.407
                curveToRelative(
                    dx1 = 0.306f,
                    dy1 = 0.449f,
                    dx2 = 0.61f,
                    dy2 = 0.936f,
                    dx3 = 0.864f,
                    dy3 = 1.407f,
                )
                // c 0.186 0.348 0.34 0.678 0.447 0.973
                curveToRelative(
                    dx1 = 0.186f,
                    dy1 = 0.348f,
                    dx2 = 0.34f,
                    dy2 = 0.678f,
                    dx3 = 0.447f,
                    dy3 = 0.973f,
                )
                // c 0.108 0.301 0.156 0.534 0.156 0.696
                curveToRelative(
                    dx1 = 0.108f,
                    dy1 = 0.301f,
                    dx2 = 0.156f,
                    dy2 = 0.534f,
                    dx3 = 0.156f,
                    dy3 = 0.696f,
                )
                // a 1.467 1.467 0 0 1 -2.934 0z
                arcToRelative(
                    a = 1.467f,
                    b = 1.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.934f,
                    dy1 = 0.0f,
                )
                close()
                // m 1.14 -3.543
                moveToRelative(dx = 1.14f, dy = -3.543f)
                // C 12.861 1.627 12 3.12 12 4
                curveTo(
                    x1 = 12.861f,
                    y1 = 1.627f,
                    x2 = 12.0f,
                    y2 = 3.12f,
                    x3 = 12.0f,
                    y3 = 4.0f,
                )
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
                // c 0 -0.88 -0.86 -2.373 -1.673 -3.543
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.88f,
                    dx2 = -0.86f,
                    dy2 = -2.373f,
                    dx3 = -1.673f,
                    dy3 = -3.543f,
                )
                // A 18.44 18.44 0 0 0 14 0
                arcTo(
                    horizontalEllipseRadius = 18.44f,
                    verticalEllipseRadius = 18.44f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 14.0f,
                    y1 = 0.0f,
                )
                // c -0.107 0.145 -0.216 0.298 -0.327 0.457z
                curveToRelative(
                    dx1 = -0.107f,
                    dy1 = 0.145f,
                    dx2 = -0.216f,
                    dy2 = 0.298f,
                    dx3 = -0.327f,
                    dy3 = 0.457f,
                )
                close()
                // m -8.34 3.21
                moveToRelative(dx = -8.34f, dy = 3.21f)
                // C 4.417 1.833 3.042 0 3.042 0
                curveTo(
                    x1 = 4.417f,
                    y1 = 1.833f,
                    x2 = 3.042f,
                    y2 = 0.0f,
                    x3 = 3.042f,
                    y3 = 0.0f,
                )
                // S 0 4.042 0 6
                reflectiveCurveTo(
                    x1 = 0.0f,
                    y1 = 4.042f,
                    x2 = 0.0f,
                    y2 = 6.0f,
                )
                // a 3.015 3.015 0 0 0 2.583 3
                arcToRelative(
                    a = 3.015f,
                    b = 3.015f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.583f,
                    dy1 = 3.0f,
                )
                // c 0.584 -1.708 1.709 -3.708 2.75 -5.333z
                curveToRelative(
                    dx1 = 0.584f,
                    dy1 = -1.708f,
                    dx2 = 1.709f,
                    dy2 = -3.708f,
                    dx3 = 2.75f,
                    dy3 = -5.333f,
                )
                close()
                // M 9.25 13
                moveTo(x = 9.25f, y = 13.0f)
                // a 3.715 3.715 0 0 1 2.792 -3.625
                arcToRelative(
                    a = 3.715f,
                    b = 3.715f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.792f,
                    dy1 = -3.625f,
                )
                // C 11.167 6.25 7.75 1.708 7.75 1.708
                curveTo(
                    x1 = 11.167f,
                    y1 = 6.25f,
                    x2 = 7.75f,
                    y2 = 1.708f,
                    x3 = 7.75f,
                    y3 = 1.708f,
                )
                // s -4.542 5.959 -4.542 8.959
                reflectiveCurveToRelative(
                    dx1 = -4.542f,
                    dy1 = 5.959f,
                    dx2 = -4.542f,
                    dy2 = 8.959f,
                )
                // c 0 2.5 2 4.5 4.5 4.5
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 2.5f,
                    dx2 = 2.0f,
                    dy2 = 4.5f,
                    dx3 = 4.5f,
                    dy3 = 4.5f,
                )
                // c 0.709 0 1.334 -0.167 1.959 -0.459
                curveToRelative(
                    dx1 = 0.709f,
                    dy1 = 0.0f,
                    dx2 = 1.334f,
                    dy2 = -0.167f,
                    dx3 = 1.959f,
                    dy3 = -0.459f,
                )
                // A 3.83 3.83 0 0 1 9.25 13z
                arcTo(
                    horizontalEllipseRadius = 3.83f,
                    verticalEllipseRadius = 3.83f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 9.25f,
                    y1 = 13.0f,
                )
                close()
            }
            // M13 16.042 a3.042 3.042 0 1 0 0 -6.084 3.042 3.042 0 0 0 0 6.084Z m1.333 -1.334 c-.083 0 -.208 -.041 -.25 -.125 L13 13.5 l-1.083 1.083 c-.125 .125 -.209 .125 -.292 .125 -.083 0 -.208 -.041 -.25 -.125 -.167 -.166 -.167 -.375 0 -.541 l1.083 -1.084 -1.125 -1.125 c-.166 -.166 -.166 -.375 0 -.541 .167 -.167 .375 -.167 .542 0 L13 12.417 l1.125 -1.125 c.167 -.167 .375 -.167 .542 0 .166 .166 .166 .375 0 .541 l-1.125 1.125 1.083 1.084 c.167 .166 .167 .375 0 .541 -.083 .125 -.167 .125 -.292 .125Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 13 16.042
                moveTo(x = 13.0f, y = 16.042f)
                // a 3.042 3.042 0 1 0 0 -6.084
                arcToRelative(
                    a = 3.042f,
                    b = 3.042f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -6.084f,
                )
                // a 3.042 3.042 0 0 0 0 6.084z
                arcToRelative(
                    a = 3.042f,
                    b = 3.042f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 6.084f,
                )
                close()
                // m 1.333 -1.334
                moveToRelative(dx = 1.333f, dy = -1.334f)
                // c -0.083 0 -0.208 -0.041 -0.25 -0.125
                curveToRelative(
                    dx1 = -0.083f,
                    dy1 = 0.0f,
                    dx2 = -0.208f,
                    dy2 = -0.041f,
                    dx3 = -0.25f,
                    dy3 = -0.125f,
                )
                // L 13 13.5
                lineTo(x = 13.0f, y = 13.5f)
                // l -1.083 1.083
                lineToRelative(dx = -1.083f, dy = 1.083f)
                // c -0.125 0.125 -0.209 0.125 -0.292 0.125
                curveToRelative(
                    dx1 = -0.125f,
                    dy1 = 0.125f,
                    dx2 = -0.209f,
                    dy2 = 0.125f,
                    dx3 = -0.292f,
                    dy3 = 0.125f,
                )
                // c -0.083 0 -0.208 -0.041 -0.25 -0.125
                curveToRelative(
                    dx1 = -0.083f,
                    dy1 = 0.0f,
                    dx2 = -0.208f,
                    dy2 = -0.041f,
                    dx3 = -0.25f,
                    dy3 = -0.125f,
                )
                // c -0.167 -0.166 -0.167 -0.375 0 -0.541
                curveToRelative(
                    dx1 = -0.167f,
                    dy1 = -0.166f,
                    dx2 = -0.167f,
                    dy2 = -0.375f,
                    dx3 = 0.0f,
                    dy3 = -0.541f,
                )
                // l 1.083 -1.084
                lineToRelative(dx = 1.083f, dy = -1.084f)
                // l -1.125 -1.125
                lineToRelative(dx = -1.125f, dy = -1.125f)
                // c -0.166 -0.166 -0.166 -0.375 0 -0.541
                curveToRelative(
                    dx1 = -0.166f,
                    dy1 = -0.166f,
                    dx2 = -0.166f,
                    dy2 = -0.375f,
                    dx3 = 0.0f,
                    dy3 = -0.541f,
                )
                // c 0.167 -0.167 0.375 -0.167 0.542 0
                curveToRelative(
                    dx1 = 0.167f,
                    dy1 = -0.167f,
                    dx2 = 0.375f,
                    dy2 = -0.167f,
                    dx3 = 0.542f,
                    dy3 = 0.0f,
                )
                // L 13 12.417
                lineTo(x = 13.0f, y = 12.417f)
                // l 1.125 -1.125
                lineToRelative(dx = 1.125f, dy = -1.125f)
                // c 0.167 -0.167 0.375 -0.167 0.542 0
                curveToRelative(
                    dx1 = 0.167f,
                    dy1 = -0.167f,
                    dx2 = 0.375f,
                    dy2 = -0.167f,
                    dx3 = 0.542f,
                    dy3 = 0.0f,
                )
                // c 0.166 0.166 0.166 0.375 0 0.541
                curveToRelative(
                    dx1 = 0.166f,
                    dy1 = 0.166f,
                    dx2 = 0.166f,
                    dy2 = 0.375f,
                    dx3 = 0.0f,
                    dy3 = 0.541f,
                )
                // l -1.125 1.125
                lineToRelative(dx = -1.125f, dy = 1.125f)
                // l 1.083 1.084
                lineToRelative(dx = 1.083f, dy = 1.084f)
                // c 0.167 0.166 0.167 0.375 0 0.541
                curveToRelative(
                    dx1 = 0.167f,
                    dy1 = 0.166f,
                    dx2 = 0.167f,
                    dy2 = 0.375f,
                    dx3 = 0.0f,
                    dy3 = 0.541f,
                )
                // c -0.083 0.125 -0.167 0.125 -0.292 0.125z
                curveToRelative(
                    dx1 = -0.083f,
                    dy1 = 0.125f,
                    dx2 = -0.167f,
                    dy2 = 0.125f,
                    dx3 = -0.292f,
                    dy3 = 0.125f,
                )
                close()
            }
        }.build().also { _ic1221 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1221: ImageVector? = null
