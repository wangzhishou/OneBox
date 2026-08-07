package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1032: ImageVector
    get() {
        val current = _ic1032
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1032",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.81 8 c0 .473 -.077 .872 -.232 1.198 a1.686 1.686 0 0 1 -.638 .74 c-.272 .166 -.585 .25 -.94 .25 s-.668 -.084 -.94 -.25 a1.708 1.708 0 0 1 -.641 -.74 c-.153 -.326 -.23 -.726 -.23 -1.198 s.077 -.871 .23 -1.195 c.155 -.327 .368 -.573 .64 -.74 A1.75 1.75 0 0 1 6 5.813 c.355 0 .668 .084 .94 .252 .273 .167 .485 .413 .638 .74 .155 .324 .233 .723 .233 1.195Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.81 8
                moveTo(x = 7.81f, y = 8.0f)
                // c 0 0.473 -0.077 0.872 -0.232 1.198
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.473f,
                    dx2 = -0.077f,
                    dy2 = 0.872f,
                    dx3 = -0.232f,
                    dy3 = 1.198f,
                )
                // a 1.686 1.686 0 0 1 -0.638 0.74
                arcToRelative(
                    a = 1.686f,
                    b = 1.686f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.638f,
                    dy1 = 0.74f,
                )
                // c -0.272 0.166 -0.585 0.25 -0.94 0.25
                curveToRelative(
                    dx1 = -0.272f,
                    dy1 = 0.166f,
                    dx2 = -0.585f,
                    dy2 = 0.25f,
                    dx3 = -0.94f,
                    dy3 = 0.25f,
                )
                // s -0.668 -0.084 -0.94 -0.25
                reflectiveCurveToRelative(
                    dx1 = -0.668f,
                    dy1 = -0.084f,
                    dx2 = -0.94f,
                    dy2 = -0.25f,
                )
                // a 1.708 1.708 0 0 1 -0.641 -0.74
                arcToRelative(
                    a = 1.708f,
                    b = 1.708f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.641f,
                    dy1 = -0.74f,
                )
                // c -0.153 -0.326 -0.23 -0.726 -0.23 -1.198
                curveToRelative(
                    dx1 = -0.153f,
                    dy1 = -0.326f,
                    dx2 = -0.23f,
                    dy2 = -0.726f,
                    dx3 = -0.23f,
                    dy3 = -1.198f,
                )
                // s 0.077 -0.871 0.23 -1.195
                reflectiveCurveToRelative(
                    dx1 = 0.077f,
                    dy1 = -0.871f,
                    dx2 = 0.23f,
                    dy2 = -1.195f,
                )
                // c 0.155 -0.327 0.368 -0.573 0.64 -0.74
                curveToRelative(
                    dx1 = 0.155f,
                    dy1 = -0.327f,
                    dx2 = 0.368f,
                    dy2 = -0.573f,
                    dx3 = 0.64f,
                    dy3 = -0.74f,
                )
                // A 1.75 1.75 0 0 1 6 5.813
                arcTo(
                    horizontalEllipseRadius = 1.75f,
                    verticalEllipseRadius = 1.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 6.0f,
                    y1 = 5.813f,
                )
                // c 0.355 0 0.668 0.084 0.94 0.252
                curveToRelative(
                    dx1 = 0.355f,
                    dy1 = 0.0f,
                    dx2 = 0.668f,
                    dy2 = 0.084f,
                    dx3 = 0.94f,
                    dy3 = 0.252f,
                )
                // c 0.273 0.167 0.485 0.413 0.638 0.74
                curveToRelative(
                    dx1 = 0.273f,
                    dy1 = 0.167f,
                    dx2 = 0.485f,
                    dy2 = 0.413f,
                    dx3 = 0.638f,
                    dy3 = 0.74f,
                )
                // c 0.155 0.324 0.233 0.723 0.233 1.195z
                curveToRelative(
                    dx1 = 0.155f,
                    dy1 = 0.324f,
                    dx2 = 0.233f,
                    dy2 = 0.723f,
                    dx3 = 0.233f,
                    dy3 = 1.195f,
                )
                close()
            }
            // M13.99 13.245 a3 3 0 0 1 -4.49 2.354 A2.987 2.987 0 0 1 8 16 a2.987 2.987 0 0 1 -1.5 -.401 3 3 0 0 1 -4.49 -2.354 A2.993 2.993 0 0 1 1 11 c0 -.535 .14 -1.037 .385 -1.471 a2.998 2.998 0 0 1 -.304 -4.835 A3 3 0 0 1 3.755 1.01 2.993 2.993 0 0 1 6 0 c.768 0 1.47 .289 2 .764 A2.989 2.989 0 0 1 10 0 c.893 0 1.695 .39 2.245 1.01 a3 3 0 0 1 2.674 3.684 2.998 2.998 0 0 1 -.304 4.834 c.245 .435 .385 .937 .385 1.472 0 .893 -.39 1.695 -1.01 2.245Z M9 8 c0 -.671 -.132 -1.245 -.395 -1.722 A2.722 2.722 0 0 0 7.533 5.18 3.073 3.073 0 0 0 6 4.8 a3.09 3.09 0 0 0 -1.537 .38 c-.45 .254 -.807 .62 -1.07 1.098 C3.13 6.755 3 7.328 3 8 c0 .67 .13 1.243 .392 1.722 .264 .476 .62 .842 1.071 1.098 .453 .253 .965 .38 1.537 .38 s1.083 -.127 1.533 -.38 c.453 -.254 .81 -.619 1.072 -1.095 C8.868 9.246 9 8.67 9 8Z m1.71 2.654 c.226 .097 .482 .146 .77 .146 .296 0 .558 -.05 .787 -.148 .229 -.1 .408 -.235 .538 -.407 A.941 .941 0 0 0 13 9.654 a.836 .836 0 0 0 -.228 -.605 c-.149 -.16 -.364 -.258 -.647 -.296 a.018 .018 0 0 1 -.016 -.018 c0 -.009 .006 -.016 .015 -.018 a.939 .939 0 0 0 .523 -.274 .753 .753 0 0 0 .206 -.546 .94 .94 0 0 0 -.17 -.555 1.17 1.17 0 0 0 -.476 -.396 1.644 1.644 0 0 0 -.715 -.146 c-.27 0 -.512 .049 -.727 .146 -.213 .097 -.383 .23 -.51 .402 a.98 .98 0 0 0 -.173 .39 c-.022 .11 .071 .203 .184 .203 h.402 c.108 0 .191 -.09 .239 -.186 a.541 .541 0 0 1 .271 -.242 .774 .774 0 0 1 .603 .002 .47 .47 0 0 1 .268 .431 .46 .46 0 0 1 -.08 .27 .537 .537 0 0 1 -.228 .18 .81 .81 0 0 1 -.334 .064 h-.167 a.2 .2 0 0 0 -.201 .2 v.204 c0 .11 .09 .2 .2 .2 h.168 a.98 .98 0 0 1 .386 .069 .557 .557 0 0 1 .25 .19 c.06 .08 .09 .172 .09 .275 a.461 .461 0 0 1 -.084 .272 .553 .553 0 0 1 -.23 .185 .81 .81 0 0 1 -.337 .066 .877 .877 0 0 1 -.33 -.059 .582 .582 0 0 1 -.235 -.165 .436 .436 0 0 1 -.045 -.07 c-.05 -.093 -.133 -.181 -.24 -.181 h-.445 c-.113 0 -.206 .092 -.184 .202 a.99 .99 0 0 0 .178 .399 c.13 .172 .306 .308 .53 .407Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 13.99 13.245
                moveTo(x = 13.99f, y = 13.245f)
                // a 3 3 0 0 1 -4.49 2.354
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -4.49f,
                    dy1 = 2.354f,
                )
                // A 2.987 2.987 0 0 1 8 16
                arcTo(
                    horizontalEllipseRadius = 2.987f,
                    verticalEllipseRadius = 2.987f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 8.0f,
                    y1 = 16.0f,
                )
                // a 2.987 2.987 0 0 1 -1.5 -0.401
                arcToRelative(
                    a = 2.987f,
                    b = 2.987f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.5f,
                    dy1 = -0.401f,
                )
                // a 3 3 0 0 1 -4.49 -2.354
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -4.49f,
                    dy1 = -2.354f,
                )
                // A 2.993 2.993 0 0 1 1 11
                arcTo(
                    horizontalEllipseRadius = 2.993f,
                    verticalEllipseRadius = 2.993f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 1.0f,
                    y1 = 11.0f,
                )
                // c 0 -0.535 0.14 -1.037 0.385 -1.471
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.535f,
                    dx2 = 0.14f,
                    dy2 = -1.037f,
                    dx3 = 0.385f,
                    dy3 = -1.471f,
                )
                // a 2.998 2.998 0 0 1 -0.304 -4.835
                arcToRelative(
                    a = 2.998f,
                    b = 2.998f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.304f,
                    dy1 = -4.835f,
                )
                // A 3 3 0 0 1 3.755 1.01
                arcTo(
                    horizontalEllipseRadius = 3.0f,
                    verticalEllipseRadius = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 3.755f,
                    y1 = 1.01f,
                )
                // A 2.993 2.993 0 0 1 6 0
                arcTo(
                    horizontalEllipseRadius = 2.993f,
                    verticalEllipseRadius = 2.993f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 6.0f,
                    y1 = 0.0f,
                )
                // c 0.768 0 1.47 0.289 2 0.764
                curveToRelative(
                    dx1 = 0.768f,
                    dy1 = 0.0f,
                    dx2 = 1.47f,
                    dy2 = 0.289f,
                    dx3 = 2.0f,
                    dy3 = 0.764f,
                )
                // A 2.989 2.989 0 0 1 10 0
                arcTo(
                    horizontalEllipseRadius = 2.989f,
                    verticalEllipseRadius = 2.989f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 10.0f,
                    y1 = 0.0f,
                )
                // c 0.893 0 1.695 0.39 2.245 1.01
                curveToRelative(
                    dx1 = 0.893f,
                    dy1 = 0.0f,
                    dx2 = 1.695f,
                    dy2 = 0.39f,
                    dx3 = 2.245f,
                    dy3 = 1.01f,
                )
                // a 3 3 0 0 1 2.674 3.684
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.674f,
                    dy1 = 3.684f,
                )
                // a 2.998 2.998 0 0 1 -0.304 4.834
                arcToRelative(
                    a = 2.998f,
                    b = 2.998f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.304f,
                    dy1 = 4.834f,
                )
                // c 0.245 0.435 0.385 0.937 0.385 1.472
                curveToRelative(
                    dx1 = 0.245f,
                    dy1 = 0.435f,
                    dx2 = 0.385f,
                    dy2 = 0.937f,
                    dx3 = 0.385f,
                    dy3 = 1.472f,
                )
                // c 0 0.893 -0.39 1.695 -1.01 2.245z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.893f,
                    dx2 = -0.39f,
                    dy2 = 1.695f,
                    dx3 = -1.01f,
                    dy3 = 2.245f,
                )
                close()
                // M 9 8
                moveTo(x = 9.0f, y = 8.0f)
                // c 0 -0.671 -0.132 -1.245 -0.395 -1.722
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.671f,
                    dx2 = -0.132f,
                    dy2 = -1.245f,
                    dx3 = -0.395f,
                    dy3 = -1.722f,
                )
                // A 2.722 2.722 0 0 0 7.533 5.18
                arcTo(
                    horizontalEllipseRadius = 2.722f,
                    verticalEllipseRadius = 2.722f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.533f,
                    y1 = 5.18f,
                )
                // A 3.073 3.073 0 0 0 6 4.8
                arcTo(
                    horizontalEllipseRadius = 3.073f,
                    verticalEllipseRadius = 3.073f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 6.0f,
                    y1 = 4.8f,
                )
                // a 3.09 3.09 0 0 0 -1.537 0.38
                arcToRelative(
                    a = 3.09f,
                    b = 3.09f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.537f,
                    dy1 = 0.38f,
                )
                // c -0.45 0.254 -0.807 0.62 -1.07 1.098
                curveToRelative(
                    dx1 = -0.45f,
                    dy1 = 0.254f,
                    dx2 = -0.807f,
                    dy2 = 0.62f,
                    dx3 = -1.07f,
                    dy3 = 1.098f,
                )
                // C 3.13 6.755 3 7.328 3 8
                curveTo(
                    x1 = 3.13f,
                    y1 = 6.755f,
                    x2 = 3.0f,
                    y2 = 7.328f,
                    x3 = 3.0f,
                    y3 = 8.0f,
                )
                // c 0 0.67 0.13 1.243 0.392 1.722
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.67f,
                    dx2 = 0.13f,
                    dy2 = 1.243f,
                    dx3 = 0.392f,
                    dy3 = 1.722f,
                )
                // c 0.264 0.476 0.62 0.842 1.071 1.098
                curveToRelative(
                    dx1 = 0.264f,
                    dy1 = 0.476f,
                    dx2 = 0.62f,
                    dy2 = 0.842f,
                    dx3 = 1.071f,
                    dy3 = 1.098f,
                )
                // c 0.453 0.253 0.965 0.38 1.537 0.38
                curveToRelative(
                    dx1 = 0.453f,
                    dy1 = 0.253f,
                    dx2 = 0.965f,
                    dy2 = 0.38f,
                    dx3 = 1.537f,
                    dy3 = 0.38f,
                )
                // s 1.083 -0.127 1.533 -0.38
                reflectiveCurveToRelative(
                    dx1 = 1.083f,
                    dy1 = -0.127f,
                    dx2 = 1.533f,
                    dy2 = -0.38f,
                )
                // c 0.453 -0.254 0.81 -0.619 1.072 -1.095
                curveToRelative(
                    dx1 = 0.453f,
                    dy1 = -0.254f,
                    dx2 = 0.81f,
                    dy2 = -0.619f,
                    dx3 = 1.072f,
                    dy3 = -1.095f,
                )
                // C 8.868 9.246 9 8.67 9 8z
                curveTo(
                    x1 = 8.868f,
                    y1 = 9.246f,
                    x2 = 9.0f,
                    y2 = 8.67f,
                    x3 = 9.0f,
                    y3 = 8.0f,
                )
                close()
                // m 1.71 2.654
                moveToRelative(dx = 1.71f, dy = 2.654f)
                // c 0.226 0.097 0.482 0.146 0.77 0.146
                curveToRelative(
                    dx1 = 0.226f,
                    dy1 = 0.097f,
                    dx2 = 0.482f,
                    dy2 = 0.146f,
                    dx3 = 0.77f,
                    dy3 = 0.146f,
                )
                // c 0.296 0 0.558 -0.05 0.787 -0.148
                curveToRelative(
                    dx1 = 0.296f,
                    dy1 = 0.0f,
                    dx2 = 0.558f,
                    dy2 = -0.05f,
                    dx3 = 0.787f,
                    dy3 = -0.148f,
                )
                // c 0.229 -0.1 0.408 -0.235 0.538 -0.407
                curveToRelative(
                    dx1 = 0.229f,
                    dy1 = -0.1f,
                    dx2 = 0.408f,
                    dy2 = -0.235f,
                    dx3 = 0.538f,
                    dy3 = -0.407f,
                )
                // A 0.941 0.941 0 0 0 13 9.654
                arcTo(
                    horizontalEllipseRadius = 0.941f,
                    verticalEllipseRadius = 0.941f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 13.0f,
                    y1 = 9.654f,
                )
                // a 0.836 0.836 0 0 0 -0.228 -0.605
                arcToRelative(
                    a = 0.836f,
                    b = 0.836f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.228f,
                    dy1 = -0.605f,
                )
                // c -0.149 -0.16 -0.364 -0.258 -0.647 -0.296
                curveToRelative(
                    dx1 = -0.149f,
                    dy1 = -0.16f,
                    dx2 = -0.364f,
                    dy2 = -0.258f,
                    dx3 = -0.647f,
                    dy3 = -0.296f,
                )
                // a 0.018 0.018 0 0 1 -0.016 -0.018
                arcToRelative(
                    a = 0.018f,
                    b = 0.018f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.016f,
                    dy1 = -0.018f,
                )
                // c 0 -0.009 0.006 -0.016 0.015 -0.018
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.009f,
                    dx2 = 0.006f,
                    dy2 = -0.016f,
                    dx3 = 0.015f,
                    dy3 = -0.018f,
                )
                // a 0.939 0.939 0 0 0 0.523 -0.274
                arcToRelative(
                    a = 0.939f,
                    b = 0.939f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.523f,
                    dy1 = -0.274f,
                )
                // a 0.753 0.753 0 0 0 0.206 -0.546
                arcToRelative(
                    a = 0.753f,
                    b = 0.753f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.206f,
                    dy1 = -0.546f,
                )
                // a 0.94 0.94 0 0 0 -0.17 -0.555
                arcToRelative(
                    a = 0.94f,
                    b = 0.94f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.17f,
                    dy1 = -0.555f,
                )
                // a 1.17 1.17 0 0 0 -0.476 -0.396
                arcToRelative(
                    a = 1.17f,
                    b = 1.17f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.476f,
                    dy1 = -0.396f,
                )
                // a 1.644 1.644 0 0 0 -0.715 -0.146
                arcToRelative(
                    a = 1.644f,
                    b = 1.644f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.715f,
                    dy1 = -0.146f,
                )
                // c -0.27 0 -0.512 0.049 -0.727 0.146
                curveToRelative(
                    dx1 = -0.27f,
                    dy1 = 0.0f,
                    dx2 = -0.512f,
                    dy2 = 0.049f,
                    dx3 = -0.727f,
                    dy3 = 0.146f,
                )
                // c -0.213 0.097 -0.383 0.23 -0.51 0.402
                curveToRelative(
                    dx1 = -0.213f,
                    dy1 = 0.097f,
                    dx2 = -0.383f,
                    dy2 = 0.23f,
                    dx3 = -0.51f,
                    dy3 = 0.402f,
                )
                // a 0.98 0.98 0 0 0 -0.173 0.39
                arcToRelative(
                    a = 0.98f,
                    b = 0.98f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.173f,
                    dy1 = 0.39f,
                )
                // c -0.022 0.11 0.071 0.203 0.184 0.203
                curveToRelative(
                    dx1 = -0.022f,
                    dy1 = 0.11f,
                    dx2 = 0.071f,
                    dy2 = 0.203f,
                    dx3 = 0.184f,
                    dy3 = 0.203f,
                )
                // h 0.402
                horizontalLineToRelative(dx = 0.402f)
                // c 0.108 0 0.191 -0.09 0.239 -0.186
                curveToRelative(
                    dx1 = 0.108f,
                    dy1 = 0.0f,
                    dx2 = 0.191f,
                    dy2 = -0.09f,
                    dx3 = 0.239f,
                    dy3 = -0.186f,
                )
                // a 0.541 0.541 0 0 1 0.271 -0.242
                arcToRelative(
                    a = 0.541f,
                    b = 0.541f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.271f,
                    dy1 = -0.242f,
                )
                // a 0.774 0.774 0 0 1 0.603 0.002
                arcToRelative(
                    a = 0.774f,
                    b = 0.774f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.603f,
                    dy1 = 0.002f,
                )
                // a 0.47 0.47 0 0 1 0.268 0.431
                arcToRelative(
                    a = 0.47f,
                    b = 0.47f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.268f,
                    dy1 = 0.431f,
                )
                // a 0.46 0.46 0 0 1 -0.08 0.27
                arcToRelative(
                    a = 0.46f,
                    b = 0.46f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.08f,
                    dy1 = 0.27f,
                )
                // a 0.537 0.537 0 0 1 -0.228 0.18
                arcToRelative(
                    a = 0.537f,
                    b = 0.537f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.228f,
                    dy1 = 0.18f,
                )
                // a 0.81 0.81 0 0 1 -0.334 0.064
                arcToRelative(
                    a = 0.81f,
                    b = 0.81f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.334f,
                    dy1 = 0.064f,
                )
                // h -0.167
                horizontalLineToRelative(dx = -0.167f)
                // a 0.2 0.2 0 0 0 -0.201 0.2
                arcToRelative(
                    a = 0.2f,
                    b = 0.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.201f,
                    dy1 = 0.2f,
                )
                // v 0.204
                verticalLineToRelative(dy = 0.204f)
                // c 0 0.11 0.09 0.2 0.2 0.2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.11f,
                    dx2 = 0.09f,
                    dy2 = 0.2f,
                    dx3 = 0.2f,
                    dy3 = 0.2f,
                )
                // h 0.168
                horizontalLineToRelative(dx = 0.168f)
                // a 0.98 0.98 0 0 1 0.386 0.069
                arcToRelative(
                    a = 0.98f,
                    b = 0.98f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.386f,
                    dy1 = 0.069f,
                )
                // a 0.557 0.557 0 0 1 0.25 0.19
                arcToRelative(
                    a = 0.557f,
                    b = 0.557f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.25f,
                    dy1 = 0.19f,
                )
                // c 0.06 0.08 0.09 0.172 0.09 0.275
                curveToRelative(
                    dx1 = 0.06f,
                    dy1 = 0.08f,
                    dx2 = 0.09f,
                    dy2 = 0.172f,
                    dx3 = 0.09f,
                    dy3 = 0.275f,
                )
                // a 0.461 0.461 0 0 1 -0.084 0.272
                arcToRelative(
                    a = 0.461f,
                    b = 0.461f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.084f,
                    dy1 = 0.272f,
                )
                // a 0.553 0.553 0 0 1 -0.23 0.185
                arcToRelative(
                    a = 0.553f,
                    b = 0.553f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.23f,
                    dy1 = 0.185f,
                )
                // a 0.81 0.81 0 0 1 -0.337 0.066
                arcToRelative(
                    a = 0.81f,
                    b = 0.81f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.337f,
                    dy1 = 0.066f,
                )
                // a 0.877 0.877 0 0 1 -0.33 -0.059
                arcToRelative(
                    a = 0.877f,
                    b = 0.877f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.33f,
                    dy1 = -0.059f,
                )
                // a 0.582 0.582 0 0 1 -0.235 -0.165
                arcToRelative(
                    a = 0.582f,
                    b = 0.582f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.235f,
                    dy1 = -0.165f,
                )
                // a 0.436 0.436 0 0 1 -0.045 -0.07
                arcToRelative(
                    a = 0.436f,
                    b = 0.436f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.045f,
                    dy1 = -0.07f,
                )
                // c -0.05 -0.093 -0.133 -0.181 -0.24 -0.181
                curveToRelative(
                    dx1 = -0.05f,
                    dy1 = -0.093f,
                    dx2 = -0.133f,
                    dy2 = -0.181f,
                    dx3 = -0.24f,
                    dy3 = -0.181f,
                )
                // h -0.445
                horizontalLineToRelative(dx = -0.445f)
                // c -0.113 0 -0.206 0.092 -0.184 0.202
                curveToRelative(
                    dx1 = -0.113f,
                    dy1 = 0.0f,
                    dx2 = -0.206f,
                    dy2 = 0.092f,
                    dx3 = -0.184f,
                    dy3 = 0.202f,
                )
                // a 0.99 0.99 0 0 0 0.178 0.399
                arcToRelative(
                    a = 0.99f,
                    b = 0.99f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.178f,
                    dy1 = 0.399f,
                )
                // c 0.13 0.172 0.306 0.308 0.53 0.407z
                curveToRelative(
                    dx1 = 0.13f,
                    dy1 = 0.172f,
                    dx2 = 0.306f,
                    dy2 = 0.308f,
                    dx3 = 0.53f,
                    dy3 = 0.407f,
                )
                close()
            }
        }.build().also { _ic1032 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1032: ImageVector? = null
