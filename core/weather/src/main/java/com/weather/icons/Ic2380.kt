package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2380: ImageVector
    get() {
        val current = _ic2380
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2380",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M2.5 1.866 a1 1 0 1 0 -1 0 V15 H0 v1 h4 v-1 H2.5 V4.292 l2.13 1.277 c.046 -.129 .105 -.298 .161 -.486 L2.986 4 4.79 2.917 a9.43 9.43 0 0 0 -.162 -.486 L2.5 3.708 V1.866Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 2.5 1.866
                moveTo(x = 2.5f, y = 1.866f)
                // a 1 1 0 1 0 -1 0
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // V 15
                verticalLineTo(y = 15.0f)
                // H 0
                horizontalLineTo(x = 0.0f)
                // v 1
                verticalLineToRelative(dy = 1.0f)
                // h 4
                horizontalLineToRelative(dx = 4.0f)
                // v -1
                verticalLineToRelative(dy = -1.0f)
                // H 2.5
                horizontalLineTo(x = 2.5f)
                // V 4.292
                verticalLineTo(y = 4.292f)
                // l 2.13 1.277
                lineToRelative(dx = 2.13f, dy = 1.277f)
                // c 0.046 -0.129 0.105 -0.298 0.161 -0.486
                curveToRelative(
                    dx1 = 0.046f,
                    dy1 = -0.129f,
                    dx2 = 0.105f,
                    dy2 = -0.298f,
                    dx3 = 0.161f,
                    dy3 = -0.486f,
                )
                // L 2.986 4
                lineTo(x = 2.986f, y = 4.0f)
                // L 4.79 2.917
                lineTo(x = 4.79f, y = 2.917f)
                // a 9.43 9.43 0 0 0 -0.162 -0.486
                arcToRelative(
                    a = 9.43f,
                    b = 9.43f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.162f,
                    dy1 = -0.486f,
                )
                // L 2.5 3.708
                lineTo(x = 2.5f, y = 3.708f)
                // V 1.866z
                verticalLineTo(y = 1.866f)
                close()
            }
            // M5.698 2.464 c.346 -.015 .8 -.032 1.302 -.042 V5.01 c-.46 .1 -.882 .198 -1.227 .281 l.048 -.171 C5.914 4.775 6 4.364 6 4 c0 -.383 -.096 -.82 -.195 -1.18 a9.243 9.243 0 0 0 -.107 -.356Z M8 2.414 c.509 .002 1.026 .015 1.5 .047 v2.094 c-.477 .063 -.992 .151 -1.5 .25 V2.413Z m2.5 .15 c.478 .067 .995 .159 1.5 .26 v1.59 a26.44 26.44 0 0 0 -1.5 .045 V2.564Z m2.5 .47 c.62 .137 1.16 .27 1.5 .354 v1.084 c-.368 -.017 -.9 -.039 -1.5 -.05 V3.034Z M5.11 1.493 a.601 .601 0 0 0 -.527 .814 c.06 .159 .167 .454 .258 .781 .093 .337 .159 .668 .159 .912 0 .23 -.058 .54 -.145 .86 a9.06 9.06 0 0 1 -.251 .778 c-.166 .444 .23 .92 .713 .796 .706 -.18 3.118 -.775 4.733 -.936 1.693 -.17 4.26 -.037 4.817 -.005 a.6 .6 0 0 0 .633 -.6 V3.077 a.6 .6 0 0 0 -.449 -.58 c-.47 -.122 -3.184 -.813 -5.001 -.995 -1.75 -.175 -4.328 -.044 -4.94 -.01Z m5.83 8.88 c-.03 -.301 .23 -.56 .56 -.56 .33 0 .59 .259 .56 .56 l-.253 2.646 h-.614 l-.253 -2.646Z m.982 3.517 a.422 .422 0 1 1 -.844 0 .422 .422 0 0 1 .844 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 5.698 2.464
                moveTo(x = 5.698f, y = 2.464f)
                // c 0.346 -0.015 0.8 -0.032 1.302 -0.042
                curveToRelative(
                    dx1 = 0.346f,
                    dy1 = -0.015f,
                    dx2 = 0.8f,
                    dy2 = -0.032f,
                    dx3 = 1.302f,
                    dy3 = -0.042f,
                )
                // V 5.01
                verticalLineTo(y = 5.01f)
                // c -0.46 0.1 -0.882 0.198 -1.227 0.281
                curveToRelative(
                    dx1 = -0.46f,
                    dy1 = 0.1f,
                    dx2 = -0.882f,
                    dy2 = 0.198f,
                    dx3 = -1.227f,
                    dy3 = 0.281f,
                )
                // l 0.048 -0.171
                lineToRelative(dx = 0.048f, dy = -0.171f)
                // C 5.914 4.775 6 4.364 6 4
                curveTo(
                    x1 = 5.914f,
                    y1 = 4.775f,
                    x2 = 6.0f,
                    y2 = 4.364f,
                    x3 = 6.0f,
                    y3 = 4.0f,
                )
                // c 0 -0.383 -0.096 -0.82 -0.195 -1.18
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.383f,
                    dx2 = -0.096f,
                    dy2 = -0.82f,
                    dx3 = -0.195f,
                    dy3 = -1.18f,
                )
                // a 9.243 9.243 0 0 0 -0.107 -0.356z
                arcToRelative(
                    a = 9.243f,
                    b = 9.243f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.107f,
                    dy1 = -0.356f,
                )
                close()
                // M 8 2.414
                moveTo(x = 8.0f, y = 2.414f)
                // c 0.509 0.002 1.026 0.015 1.5 0.047
                curveToRelative(
                    dx1 = 0.509f,
                    dy1 = 0.002f,
                    dx2 = 1.026f,
                    dy2 = 0.015f,
                    dx3 = 1.5f,
                    dy3 = 0.047f,
                )
                // v 2.094
                verticalLineToRelative(dy = 2.094f)
                // c -0.477 0.063 -0.992 0.151 -1.5 0.25
                curveToRelative(
                    dx1 = -0.477f,
                    dy1 = 0.063f,
                    dx2 = -0.992f,
                    dy2 = 0.151f,
                    dx3 = -1.5f,
                    dy3 = 0.25f,
                )
                // V 2.413z
                verticalLineTo(y = 2.413f)
                close()
                // m 2.5 0.15
                moveToRelative(dx = 2.5f, dy = 0.15f)
                // c 0.478 0.067 0.995 0.159 1.5 0.26
                curveToRelative(
                    dx1 = 0.478f,
                    dy1 = 0.067f,
                    dx2 = 0.995f,
                    dy2 = 0.159f,
                    dx3 = 1.5f,
                    dy3 = 0.26f,
                )
                // v 1.59
                verticalLineToRelative(dy = 1.59f)
                // a 26.44 26.44 0 0 0 -1.5 0.045
                arcToRelative(
                    a = 26.44f,
                    b = 26.44f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.5f,
                    dy1 = 0.045f,
                )
                // V 2.564z
                verticalLineTo(y = 2.564f)
                close()
                // m 2.5 0.47
                moveToRelative(dx = 2.5f, dy = 0.47f)
                // c 0.62 0.137 1.16 0.27 1.5 0.354
                curveToRelative(
                    dx1 = 0.62f,
                    dy1 = 0.137f,
                    dx2 = 1.16f,
                    dy2 = 0.27f,
                    dx3 = 1.5f,
                    dy3 = 0.354f,
                )
                // v 1.084
                verticalLineToRelative(dy = 1.084f)
                // c -0.368 -0.017 -0.9 -0.039 -1.5 -0.05
                curveToRelative(
                    dx1 = -0.368f,
                    dy1 = -0.017f,
                    dx2 = -0.9f,
                    dy2 = -0.039f,
                    dx3 = -1.5f,
                    dy3 = -0.05f,
                )
                // V 3.034z
                verticalLineTo(y = 3.034f)
                close()
                // M 5.11 1.493
                moveTo(x = 5.11f, y = 1.493f)
                // a 0.601 0.601 0 0 0 -0.527 0.814
                arcToRelative(
                    a = 0.601f,
                    b = 0.601f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.527f,
                    dy1 = 0.814f,
                )
                // c 0.06 0.159 0.167 0.454 0.258 0.781
                curveToRelative(
                    dx1 = 0.06f,
                    dy1 = 0.159f,
                    dx2 = 0.167f,
                    dy2 = 0.454f,
                    dx3 = 0.258f,
                    dy3 = 0.781f,
                )
                // c 0.093 0.337 0.159 0.668 0.159 0.912
                curveToRelative(
                    dx1 = 0.093f,
                    dy1 = 0.337f,
                    dx2 = 0.159f,
                    dy2 = 0.668f,
                    dx3 = 0.159f,
                    dy3 = 0.912f,
                )
                // c 0 0.23 -0.058 0.54 -0.145 0.86
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.23f,
                    dx2 = -0.058f,
                    dy2 = 0.54f,
                    dx3 = -0.145f,
                    dy3 = 0.86f,
                )
                // a 9.06 9.06 0 0 1 -0.251 0.778
                arcToRelative(
                    a = 9.06f,
                    b = 9.06f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.251f,
                    dy1 = 0.778f,
                )
                // c -0.166 0.444 0.23 0.92 0.713 0.796
                curveToRelative(
                    dx1 = -0.166f,
                    dy1 = 0.444f,
                    dx2 = 0.23f,
                    dy2 = 0.92f,
                    dx3 = 0.713f,
                    dy3 = 0.796f,
                )
                // c 0.706 -0.18 3.118 -0.775 4.733 -0.936
                curveToRelative(
                    dx1 = 0.706f,
                    dy1 = -0.18f,
                    dx2 = 3.118f,
                    dy2 = -0.775f,
                    dx3 = 4.733f,
                    dy3 = -0.936f,
                )
                // c 1.693 -0.17 4.26 -0.037 4.817 -0.005
                curveToRelative(
                    dx1 = 1.693f,
                    dy1 = -0.17f,
                    dx2 = 4.26f,
                    dy2 = -0.037f,
                    dx3 = 4.817f,
                    dy3 = -0.005f,
                )
                // a 0.6 0.6 0 0 0 0.633 -0.6
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.633f,
                    dy1 = -0.6f,
                )
                // V 3.077
                verticalLineTo(y = 3.077f)
                // a 0.6 0.6 0 0 0 -0.449 -0.58
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.449f,
                    dy1 = -0.58f,
                )
                // c -0.47 -0.122 -3.184 -0.813 -5.001 -0.995
                curveToRelative(
                    dx1 = -0.47f,
                    dy1 = -0.122f,
                    dx2 = -3.184f,
                    dy2 = -0.813f,
                    dx3 = -5.001f,
                    dy3 = -0.995f,
                )
                // c -1.75 -0.175 -4.328 -0.044 -4.94 -0.01z
                curveToRelative(
                    dx1 = -1.75f,
                    dy1 = -0.175f,
                    dx2 = -4.328f,
                    dy2 = -0.044f,
                    dx3 = -4.94f,
                    dy3 = -0.01f,
                )
                close()
                // m 5.83 8.88
                moveToRelative(dx = 5.83f, dy = 8.88f)
                // c -0.03 -0.301 0.23 -0.56 0.56 -0.56
                curveToRelative(
                    dx1 = -0.03f,
                    dy1 = -0.301f,
                    dx2 = 0.23f,
                    dy2 = -0.56f,
                    dx3 = 0.56f,
                    dy3 = -0.56f,
                )
                // c 0.33 0 0.59 0.259 0.56 0.56
                curveToRelative(
                    dx1 = 0.33f,
                    dy1 = 0.0f,
                    dx2 = 0.59f,
                    dy2 = 0.259f,
                    dx3 = 0.56f,
                    dy3 = 0.56f,
                )
                // l -0.253 2.646
                lineToRelative(dx = -0.253f, dy = 2.646f)
                // h -0.614
                horizontalLineToRelative(dx = -0.614f)
                // l -0.253 -2.646z
                lineToRelative(dx = -0.253f, dy = -2.646f)
                close()
                // m 0.982 3.517
                moveToRelative(dx = 0.982f, dy = 3.517f)
                // a 0.422 0.422 0 1 1 -0.844 0
                arcToRelative(
                    a = 0.422f,
                    b = 0.422f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.844f,
                    dy1 = 0.0f,
                )
                // a 0.422 0.422 0 0 1 0.844 0z
                arcToRelative(
                    a = 0.422f,
                    b = 0.422f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.844f,
                    dy1 = 0.0f,
                )
                close()
            }
            // M11.244 7.71 a.296 .296 0 0 1 .512 0 L15.96 15 a.292 .292 0 0 1 -.256 .438 h-8.41 A.292 .292 0 0 1 7.039 15 l4.205 -7.291Z m3.8 7.052 L11.5 8.617 l-3.544 6.145 h7.088Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.244 7.71
                moveTo(x = 11.244f, y = 7.71f)
                // a 0.296 0.296 0 0 1 0.512 0
                arcToRelative(
                    a = 0.296f,
                    b = 0.296f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.512f,
                    dy1 = 0.0f,
                )
                // L 15.96 15
                lineTo(x = 15.96f, y = 15.0f)
                // a 0.292 0.292 0 0 1 -0.256 0.438
                arcToRelative(
                    a = 0.292f,
                    b = 0.292f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.256f,
                    dy1 = 0.438f,
                )
                // h -8.41
                horizontalLineToRelative(dx = -8.41f)
                // A 0.292 0.292 0 0 1 7.039 15
                arcTo(
                    horizontalEllipseRadius = 0.292f,
                    verticalEllipseRadius = 0.292f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.039f,
                    y1 = 15.0f,
                )
                // l 4.205 -7.291z
                lineToRelative(dx = 4.205f, dy = -7.291f)
                close()
                // m 3.8 7.052
                moveToRelative(dx = 3.8f, dy = 7.052f)
                // L 11.5 8.617
                lineTo(x = 11.5f, y = 8.617f)
                // l -3.544 6.145
                lineToRelative(dx = -3.544f, dy = 6.145f)
                // h 7.088z
                horizontalLineToRelative(dx = 7.088f)
                close()
            }
        }.build().also { _ic2380 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2380: ImageVector? = null
