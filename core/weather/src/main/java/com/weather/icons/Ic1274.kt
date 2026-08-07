package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1274: ImageVector
    get() {
        val current = _ic1274
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1274",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M10.875 5.313 c0 -.104 .084 -.188 .188 -.188 h2.25 a.187 .187 0 1 1 0 .375 h-2.25 a.187 .187 0 0 1 -.188 -.188Z m-1.5 .75 c0 -.104 .084 -.188 .188 -.188 h2.812 a.187 .187 0 1 1 0 .375 H9.562 a.188 .188 0 0 1 -.187 -.188Z m1.875 .75 c0 -.104 .084 -.188 .188 -.188 h3 a.187 .187 0 1 1 0 .375 h-3 a.187 .187 0 0 1 -.188 -.188Z m-1.5 .562 c0 -.104 .084 -.188 .188 -.188 h2.25 a.187 .187 0 1 1 0 .375 h-2.25 a.188 .188 0 0 1 -.188 -.187Z m1.125 .563 c0 -.104 .084 -.188 .188 -.188 h2.812 a.187 .187 0 1 1 0 .375 h-2.813 a.187 .187 0 0 1 -.187 -.188Z m-.563 .75 c0 -.104 .084 -.188 .188 -.188 h2.25 a.187 .187 0 1 1 0 .375 H10.5 a.187 .187 0 0 1 -.188 -.188Z M9.085 1.504 V1.5 A1.5 1.5 0 0 0 6.4 .583 a1 1 0 0 0 -1.395 1.004 A1.5 1.5 0 0 0 3 3 v.009 a1 1 0 0 0 -.653 1.608 .75 .75 0 0 0 .155 1.34 .5 .5 0 1 0 .872 -.29 .747 .747 0 0 0 .065 -.714 1.01 1.01 0 0 0 .6 -.525 1.499 1.499 0 0 0 1.774 -.702 .997 .997 0 0 0 1.243 .105 A1 1 0 0 0 9 3.5 a1 1 0 0 0 .085 -1.996Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10.875 5.313
                moveTo(x = 10.875f, y = 5.313f)
                // c 0 -0.104 0.084 -0.188 0.188 -0.188
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.104f,
                    dx2 = 0.084f,
                    dy2 = -0.188f,
                    dx3 = 0.188f,
                    dy3 = -0.188f,
                )
                // h 2.25
                horizontalLineToRelative(dx = 2.25f)
                // a 0.187 0.187 0 1 1 0 0.375
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.375f,
                )
                // h -2.25
                horizontalLineToRelative(dx = -2.25f)
                // a 0.187 0.187 0 0 1 -0.188 -0.188z
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.188f,
                    dy1 = -0.188f,
                )
                close()
                // m -1.5 0.75
                moveToRelative(dx = -1.5f, dy = 0.75f)
                // c 0 -0.104 0.084 -0.188 0.188 -0.188
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.104f,
                    dx2 = 0.084f,
                    dy2 = -0.188f,
                    dx3 = 0.188f,
                    dy3 = -0.188f,
                )
                // h 2.812
                horizontalLineToRelative(dx = 2.812f)
                // a 0.187 0.187 0 1 1 0 0.375
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.375f,
                )
                // H 9.562
                horizontalLineTo(x = 9.562f)
                // a 0.188 0.188 0 0 1 -0.187 -0.188z
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.187f,
                    dy1 = -0.188f,
                )
                close()
                // m 1.875 0.75
                moveToRelative(dx = 1.875f, dy = 0.75f)
                // c 0 -0.104 0.084 -0.188 0.188 -0.188
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.104f,
                    dx2 = 0.084f,
                    dy2 = -0.188f,
                    dx3 = 0.188f,
                    dy3 = -0.188f,
                )
                // h 3
                horizontalLineToRelative(dx = 3.0f)
                // a 0.187 0.187 0 1 1 0 0.375
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.375f,
                )
                // h -3
                horizontalLineToRelative(dx = -3.0f)
                // a 0.187 0.187 0 0 1 -0.188 -0.188z
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.188f,
                    dy1 = -0.188f,
                )
                close()
                // m -1.5 0.562
                moveToRelative(dx = -1.5f, dy = 0.562f)
                // c 0 -0.104 0.084 -0.188 0.188 -0.188
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.104f,
                    dx2 = 0.084f,
                    dy2 = -0.188f,
                    dx3 = 0.188f,
                    dy3 = -0.188f,
                )
                // h 2.25
                horizontalLineToRelative(dx = 2.25f)
                // a 0.187 0.187 0 1 1 0 0.375
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.375f,
                )
                // h -2.25
                horizontalLineToRelative(dx = -2.25f)
                // a 0.188 0.188 0 0 1 -0.188 -0.187z
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.188f,
                    dy1 = -0.187f,
                )
                close()
                // m 1.125 0.563
                moveToRelative(dx = 1.125f, dy = 0.563f)
                // c 0 -0.104 0.084 -0.188 0.188 -0.188
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.104f,
                    dx2 = 0.084f,
                    dy2 = -0.188f,
                    dx3 = 0.188f,
                    dy3 = -0.188f,
                )
                // h 2.812
                horizontalLineToRelative(dx = 2.812f)
                // a 0.187 0.187 0 1 1 0 0.375
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.375f,
                )
                // h -2.813
                horizontalLineToRelative(dx = -2.813f)
                // a 0.187 0.187 0 0 1 -0.187 -0.188z
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.187f,
                    dy1 = -0.188f,
                )
                close()
                // m -0.563 0.75
                moveToRelative(dx = -0.563f, dy = 0.75f)
                // c 0 -0.104 0.084 -0.188 0.188 -0.188
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.104f,
                    dx2 = 0.084f,
                    dy2 = -0.188f,
                    dx3 = 0.188f,
                    dy3 = -0.188f,
                )
                // h 2.25
                horizontalLineToRelative(dx = 2.25f)
                // a 0.187 0.187 0 1 1 0 0.375
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.375f,
                )
                // H 10.5
                horizontalLineTo(x = 10.5f)
                // a 0.187 0.187 0 0 1 -0.188 -0.188z
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.188f,
                    dy1 = -0.188f,
                )
                close()
                // M 9.085 1.504
                moveTo(x = 9.085f, y = 1.504f)
                // V 1.5
                verticalLineTo(y = 1.5f)
                // A 1.5 1.5 0 0 0 6.4 0.583
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 6.4f,
                    y1 = 0.583f,
                )
                // a 1 1 0 0 0 -1.395 1.004
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.395f,
                    dy1 = 1.004f,
                )
                // A 1.5 1.5 0 0 0 3 3
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 3.0f,
                    y1 = 3.0f,
                )
                // v 0.009
                verticalLineToRelative(dy = 0.009f)
                // a 1 1 0 0 0 -0.653 1.608
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.653f,
                    dy1 = 1.608f,
                )
                // a 0.75 0.75 0 0 0 0.155 1.34
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.155f,
                    dy1 = 1.34f,
                )
                // a 0.5 0.5 0 1 0 0.872 -0.29
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.872f,
                    dy1 = -0.29f,
                )
                // a 0.747 0.747 0 0 0 0.065 -0.714
                arcToRelative(
                    a = 0.747f,
                    b = 0.747f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.065f,
                    dy1 = -0.714f,
                )
                // a 1.01 1.01 0 0 0 0.6 -0.525
                arcToRelative(
                    a = 1.01f,
                    b = 1.01f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.6f,
                    dy1 = -0.525f,
                )
                // a 1.499 1.499 0 0 0 1.774 -0.702
                arcToRelative(
                    a = 1.499f,
                    b = 1.499f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.774f,
                    dy1 = -0.702f,
                )
                // a 0.997 0.997 0 0 0 1.243 0.105
                arcToRelative(
                    a = 0.997f,
                    b = 0.997f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.243f,
                    dy1 = 0.105f,
                )
                // A 1 1 0 0 0 9 3.5
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 9.0f,
                    y1 = 3.5f,
                )
                // a 1 1 0 0 0 0.085 -1.996z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.085f,
                    dy1 = -1.996f,
                )
                close()
            }
            // M2 7 h2 v4.91 l12 -2 V16 H0 v-3.424 l2 -.333 V7Z m13 4.09 L1 13.424 V15 h14 v-3.91Z M10 14 H9 v-1 h1 v1Z m2 0 h-1 v-1 h1 v1Z m2 0 h-1 v-1 h1 v1Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 2 7
                moveTo(x = 2.0f, y = 7.0f)
                // h 2
                horizontalLineToRelative(dx = 2.0f)
                // v 4.91
                verticalLineToRelative(dy = 4.91f)
                // l 12 -2
                lineToRelative(dx = 12.0f, dy = -2.0f)
                // V 16
                verticalLineTo(y = 16.0f)
                // H 0
                horizontalLineTo(x = 0.0f)
                // v -3.424
                verticalLineToRelative(dy = -3.424f)
                // l 2 -0.333
                lineToRelative(dx = 2.0f, dy = -0.333f)
                // V 7z
                verticalLineTo(y = 7.0f)
                close()
                // m 13 4.09
                moveToRelative(dx = 13.0f, dy = 4.09f)
                // L 1 13.424
                lineTo(x = 1.0f, y = 13.424f)
                // V 15
                verticalLineTo(y = 15.0f)
                // h 14
                horizontalLineToRelative(dx = 14.0f)
                // v -3.91z
                verticalLineToRelative(dy = -3.91f)
                close()
                // M 10 14
                moveTo(x = 10.0f, y = 14.0f)
                // H 9
                horizontalLineTo(x = 9.0f)
                // v -1
                verticalLineToRelative(dy = -1.0f)
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // v 1z
                verticalLineToRelative(dy = 1.0f)
                close()
                // m 2 0
                moveToRelative(dx = 2.0f, dy = 0.0f)
                // h -1
                horizontalLineToRelative(dx = -1.0f)
                // v -1
                verticalLineToRelative(dy = -1.0f)
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // v 1z
                verticalLineToRelative(dy = 1.0f)
                close()
                // m 2 0
                moveToRelative(dx = 2.0f, dy = 0.0f)
                // h -1
                horizontalLineToRelative(dx = -1.0f)
                // v -1
                verticalLineToRelative(dy = -1.0f)
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // v 1z
                verticalLineToRelative(dy = 1.0f)
                close()
            }
        }.build().also { _ic1274 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1274: ImageVector? = null
