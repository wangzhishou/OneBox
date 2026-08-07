package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1010: ImageVector
    get() {
        val current = _ic1010
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1010",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.01 2.503 a.5 .5 0 0 1 -.501 -.5 v-1.5 a.5 .5 0 0 1 1 0 v1.5 a.5 .5 0 0 1 -.5 .5Z M3.766 4.255 a.498 .498 0 0 1 -.353 -.147 l-1.062 -1.06 a.5 .5 0 0 1 .707 -.707 L4.122 3.4 a.5 .5 0 0 1 -.355 .854 v.001Z M2.004 8.493 h-1.5 a.5 .5 0 1 1 0 -1 h1.5 a.5 .5 0 1 1 0 1Z m.691 5.303 a.5 .5 0 0 1 -.354 -.854 l1.062 -1.06 a.5 .5 0 0 1 .708 .707 l-1.063 1.06 a.498 .498 0 0 1 -.353 .147Z m5.301 2.201 a.5 .5 0 0 1 -.5 -.5 v-1.5 a.5 .5 0 0 1 1 0 v1.5 a.5 .5 0 0 1 -.5 .5Z m-.869 -3.583 a4.5 4.5 0 0 0 1.373 .059 V3.527 a4.503 4.503 0 0 0 -4.652 2.75 4.5 4.5 0 0 0 3.28 6.137Z m8.876 -4.407 A.506 .506 0 0 0 16 7.952 v.04 a.5 .5 0 0 0 -.2 -.392 2.952 2.952 0 0 0 -.846 -.463 A2.96 2.96 0 0 0 14 7 c-.629 0 -1.037 .364 -1.304 .601 l-.028 .025 C12.37 7.891 12.222 8 12 8 c-.273 0 -.468 -.029 -.638 -.085 A1.967 1.967 0 0 1 10.8 7.6 a.5 .5 0 0 0 -.6 .8 c.276 .207 .544 .363 .846 .463 .301 .1 .61 .137 .954 .137 .629 0 1.038 -.364 1.304 -.601 l.028 -.025 C13.63 8.109 13.778 8 14 8 c.273 0 .468 .029 .638 .085 .17 .057 .344 .151 .562 .315 a.5 .5 0 0 0 .8 -.392 v.054 a.506 .506 0 0 0 .003 -.055Z M12 6 c.629 0 1.038 -.364 1.304 -.601 l.028 -.025 C13.63 5.109 13.778 5 14 5 c.273 0 .468 .029 .638 .085 .17 .057 .344 .151 .562 .315 a.5 .5 0 0 0 .6 -.8 2.952 2.952 0 0 0 -.846 -.463 A2.96 2.96 0 0 0 14 4 c-.629 0 -1.037 .364 -1.304 .601 l-.028 .025 C12.37 4.891 12.222 5 12 5 c-.273 0 -.468 -.029 -.638 -.085 A1.967 1.967 0 0 1 10.8 4.6 a.5 .5 0 0 0 -.6 .8 c.276 .207 .544 .363 .846 .463 .301 .1 .61 .137 .954 .137Z m0 6 c.629 0 1.038 -.364 1.304 -.601 l.028 -.025 c.298 -.264 .446 -.374 .668 -.374 .273 0 .468 .029 .638 .085 .17 .057 .344 .151 .562 .315 a.5 .5 0 0 0 .6 -.8 2.951 2.951 0 0 0 -.846 -.463 c-.301 -.1 -.61 -.137 -.954 -.137 -.629 0 -1.037 .364 -1.304 .601 l-.028 .025 c-.298 .264 -.446 .374 -.668 .374 -.273 0 -.468 -.029 -.638 -.085 a1.968 1.968 0 0 1 -.562 -.315 .5 .5 0 0 0 -.6 .8 c.276 .207 .544 .363 .846 .463 .301 .1 .61 .137 .954 .137Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.01 2.503
                moveTo(x = 8.01f, y = 2.503f)
                // a 0.5 0.5 0 0 1 -0.501 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.501f,
                    dy1 = -0.5f,
                )
                // v -1.5
                verticalLineToRelative(dy = -1.5f)
                // a 0.5 0.5 0 0 1 1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                // v 1.5
                verticalLineToRelative(dy = 1.5f)
                // a 0.5 0.5 0 0 1 -0.5 0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = 0.5f,
                )
                close()
                // M 3.766 4.255
                moveTo(x = 3.766f, y = 4.255f)
                // a 0.498 0.498 0 0 1 -0.353 -0.147
                arcToRelative(
                    a = 0.498f,
                    b = 0.498f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.353f,
                    dy1 = -0.147f,
                )
                // l -1.062 -1.06
                lineToRelative(dx = -1.062f, dy = -1.06f)
                // a 0.5 0.5 0 0 1 0.707 -0.707
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.707f,
                    dy1 = -0.707f,
                )
                // L 4.122 3.4
                lineTo(x = 4.122f, y = 3.4f)
                // a 0.5 0.5 0 0 1 -0.355 0.854
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.355f,
                    dy1 = 0.854f,
                )
                // v 0.001z
                verticalLineToRelative(dy = 0.001f)
                close()
                // M 2.004 8.493
                moveTo(x = 2.004f, y = 8.493f)
                // h -1.5
                horizontalLineToRelative(dx = -1.5f)
                // a 0.5 0.5 0 1 1 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h 1.5
                horizontalLineToRelative(dx = 1.5f)
                // a 0.5 0.5 0 1 1 0 1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                close()
                // m 0.691 5.303
                moveToRelative(dx = 0.691f, dy = 5.303f)
                // a 0.5 0.5 0 0 1 -0.354 -0.854
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.354f,
                    dy1 = -0.854f,
                )
                // l 1.062 -1.06
                lineToRelative(dx = 1.062f, dy = -1.06f)
                // a 0.5 0.5 0 0 1 0.708 0.707
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.708f,
                    dy1 = 0.707f,
                )
                // l -1.063 1.06
                lineToRelative(dx = -1.063f, dy = 1.06f)
                // a 0.498 0.498 0 0 1 -0.353 0.147z
                arcToRelative(
                    a = 0.498f,
                    b = 0.498f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.353f,
                    dy1 = 0.147f,
                )
                close()
                // m 5.301 2.201
                moveToRelative(dx = 5.301f, dy = 2.201f)
                // a 0.5 0.5 0 0 1 -0.5 -0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                // v -1.5
                verticalLineToRelative(dy = -1.5f)
                // a 0.5 0.5 0 0 1 1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                // v 1.5
                verticalLineToRelative(dy = 1.5f)
                // a 0.5 0.5 0 0 1 -0.5 0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = 0.5f,
                )
                close()
                // m -0.869 -3.583
                moveToRelative(dx = -0.869f, dy = -3.583f)
                // a 4.5 4.5 0 0 0 1.373 0.059
                arcToRelative(
                    a = 4.5f,
                    b = 4.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.373f,
                    dy1 = 0.059f,
                )
                // V 3.527
                verticalLineTo(y = 3.527f)
                // a 4.503 4.503 0 0 0 -4.652 2.75
                arcToRelative(
                    a = 4.503f,
                    b = 4.503f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -4.652f,
                    dy1 = 2.75f,
                )
                // a 4.5 4.5 0 0 0 3.28 6.137z
                arcToRelative(
                    a = 4.5f,
                    b = 4.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 3.28f,
                    dy1 = 6.137f,
                )
                close()
                // m 8.876 -4.407
                moveToRelative(dx = 8.876f, dy = -4.407f)
                // A 0.506 0.506 0 0 0 16 7.952
                arcTo(
                    horizontalEllipseRadius = 0.506f,
                    verticalEllipseRadius = 0.506f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 16.0f,
                    y1 = 7.952f,
                )
                // v 0.04
                verticalLineToRelative(dy = 0.04f)
                // a 0.5 0.5 0 0 0 -0.2 -0.392
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.2f,
                    dy1 = -0.392f,
                )
                // a 2.952 2.952 0 0 0 -0.846 -0.463
                arcToRelative(
                    a = 2.952f,
                    b = 2.952f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.846f,
                    dy1 = -0.463f,
                )
                // A 2.96 2.96 0 0 0 14 7
                arcTo(
                    horizontalEllipseRadius = 2.96f,
                    verticalEllipseRadius = 2.96f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 14.0f,
                    y1 = 7.0f,
                )
                // c -0.629 0 -1.037 0.364 -1.304 0.601
                curveToRelative(
                    dx1 = -0.629f,
                    dy1 = 0.0f,
                    dx2 = -1.037f,
                    dy2 = 0.364f,
                    dx3 = -1.304f,
                    dy3 = 0.601f,
                )
                // l -0.028 0.025
                lineToRelative(dx = -0.028f, dy = 0.025f)
                // C 12.37 7.891 12.222 8 12 8
                curveTo(
                    x1 = 12.37f,
                    y1 = 7.891f,
                    x2 = 12.222f,
                    y2 = 8.0f,
                    x3 = 12.0f,
                    y3 = 8.0f,
                )
                // c -0.273 0 -0.468 -0.029 -0.638 -0.085
                curveToRelative(
                    dx1 = -0.273f,
                    dy1 = 0.0f,
                    dx2 = -0.468f,
                    dy2 = -0.029f,
                    dx3 = -0.638f,
                    dy3 = -0.085f,
                )
                // A 1.967 1.967 0 0 1 10.8 7.6
                arcTo(
                    horizontalEllipseRadius = 1.967f,
                    verticalEllipseRadius = 1.967f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 10.8f,
                    y1 = 7.6f,
                )
                // a 0.5 0.5 0 0 0 -0.6 0.8
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.6f,
                    dy1 = 0.8f,
                )
                // c 0.276 0.207 0.544 0.363 0.846 0.463
                curveToRelative(
                    dx1 = 0.276f,
                    dy1 = 0.207f,
                    dx2 = 0.544f,
                    dy2 = 0.363f,
                    dx3 = 0.846f,
                    dy3 = 0.463f,
                )
                // c 0.301 0.1 0.61 0.137 0.954 0.137
                curveToRelative(
                    dx1 = 0.301f,
                    dy1 = 0.1f,
                    dx2 = 0.61f,
                    dy2 = 0.137f,
                    dx3 = 0.954f,
                    dy3 = 0.137f,
                )
                // c 0.629 0 1.038 -0.364 1.304 -0.601
                curveToRelative(
                    dx1 = 0.629f,
                    dy1 = 0.0f,
                    dx2 = 1.038f,
                    dy2 = -0.364f,
                    dx3 = 1.304f,
                    dy3 = -0.601f,
                )
                // l 0.028 -0.025
                lineToRelative(dx = 0.028f, dy = -0.025f)
                // C 13.63 8.109 13.778 8 14 8
                curveTo(
                    x1 = 13.63f,
                    y1 = 8.109f,
                    x2 = 13.778f,
                    y2 = 8.0f,
                    x3 = 14.0f,
                    y3 = 8.0f,
                )
                // c 0.273 0 0.468 0.029 0.638 0.085
                curveToRelative(
                    dx1 = 0.273f,
                    dy1 = 0.0f,
                    dx2 = 0.468f,
                    dy2 = 0.029f,
                    dx3 = 0.638f,
                    dy3 = 0.085f,
                )
                // c 0.17 0.057 0.344 0.151 0.562 0.315
                curveToRelative(
                    dx1 = 0.17f,
                    dy1 = 0.057f,
                    dx2 = 0.344f,
                    dy2 = 0.151f,
                    dx3 = 0.562f,
                    dy3 = 0.315f,
                )
                // a 0.5 0.5 0 0 0 0.8 -0.392
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.8f,
                    dy1 = -0.392f,
                )
                // v 0.054
                verticalLineToRelative(dy = 0.054f)
                // a 0.506 0.506 0 0 0 0.003 -0.055z
                arcToRelative(
                    a = 0.506f,
                    b = 0.506f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.003f,
                    dy1 = -0.055f,
                )
                close()
                // M 12 6
                moveTo(x = 12.0f, y = 6.0f)
                // c 0.629 0 1.038 -0.364 1.304 -0.601
                curveToRelative(
                    dx1 = 0.629f,
                    dy1 = 0.0f,
                    dx2 = 1.038f,
                    dy2 = -0.364f,
                    dx3 = 1.304f,
                    dy3 = -0.601f,
                )
                // l 0.028 -0.025
                lineToRelative(dx = 0.028f, dy = -0.025f)
                // C 13.63 5.109 13.778 5 14 5
                curveTo(
                    x1 = 13.63f,
                    y1 = 5.109f,
                    x2 = 13.778f,
                    y2 = 5.0f,
                    x3 = 14.0f,
                    y3 = 5.0f,
                )
                // c 0.273 0 0.468 0.029 0.638 0.085
                curveToRelative(
                    dx1 = 0.273f,
                    dy1 = 0.0f,
                    dx2 = 0.468f,
                    dy2 = 0.029f,
                    dx3 = 0.638f,
                    dy3 = 0.085f,
                )
                // c 0.17 0.057 0.344 0.151 0.562 0.315
                curveToRelative(
                    dx1 = 0.17f,
                    dy1 = 0.057f,
                    dx2 = 0.344f,
                    dy2 = 0.151f,
                    dx3 = 0.562f,
                    dy3 = 0.315f,
                )
                // a 0.5 0.5 0 0 0 0.6 -0.8
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.6f,
                    dy1 = -0.8f,
                )
                // a 2.952 2.952 0 0 0 -0.846 -0.463
                arcToRelative(
                    a = 2.952f,
                    b = 2.952f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.846f,
                    dy1 = -0.463f,
                )
                // A 2.96 2.96 0 0 0 14 4
                arcTo(
                    horizontalEllipseRadius = 2.96f,
                    verticalEllipseRadius = 2.96f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 14.0f,
                    y1 = 4.0f,
                )
                // c -0.629 0 -1.037 0.364 -1.304 0.601
                curveToRelative(
                    dx1 = -0.629f,
                    dy1 = 0.0f,
                    dx2 = -1.037f,
                    dy2 = 0.364f,
                    dx3 = -1.304f,
                    dy3 = 0.601f,
                )
                // l -0.028 0.025
                lineToRelative(dx = -0.028f, dy = 0.025f)
                // C 12.37 4.891 12.222 5 12 5
                curveTo(
                    x1 = 12.37f,
                    y1 = 4.891f,
                    x2 = 12.222f,
                    y2 = 5.0f,
                    x3 = 12.0f,
                    y3 = 5.0f,
                )
                // c -0.273 0 -0.468 -0.029 -0.638 -0.085
                curveToRelative(
                    dx1 = -0.273f,
                    dy1 = 0.0f,
                    dx2 = -0.468f,
                    dy2 = -0.029f,
                    dx3 = -0.638f,
                    dy3 = -0.085f,
                )
                // A 1.967 1.967 0 0 1 10.8 4.6
                arcTo(
                    horizontalEllipseRadius = 1.967f,
                    verticalEllipseRadius = 1.967f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 10.8f,
                    y1 = 4.6f,
                )
                // a 0.5 0.5 0 0 0 -0.6 0.8
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.6f,
                    dy1 = 0.8f,
                )
                // c 0.276 0.207 0.544 0.363 0.846 0.463
                curveToRelative(
                    dx1 = 0.276f,
                    dy1 = 0.207f,
                    dx2 = 0.544f,
                    dy2 = 0.363f,
                    dx3 = 0.846f,
                    dy3 = 0.463f,
                )
                // c 0.301 0.1 0.61 0.137 0.954 0.137z
                curveToRelative(
                    dx1 = 0.301f,
                    dy1 = 0.1f,
                    dx2 = 0.61f,
                    dy2 = 0.137f,
                    dx3 = 0.954f,
                    dy3 = 0.137f,
                )
                close()
                // m 0 6
                moveToRelative(dx = 0.0f, dy = 6.0f)
                // c 0.629 0 1.038 -0.364 1.304 -0.601
                curveToRelative(
                    dx1 = 0.629f,
                    dy1 = 0.0f,
                    dx2 = 1.038f,
                    dy2 = -0.364f,
                    dx3 = 1.304f,
                    dy3 = -0.601f,
                )
                // l 0.028 -0.025
                lineToRelative(dx = 0.028f, dy = -0.025f)
                // c 0.298 -0.264 0.446 -0.374 0.668 -0.374
                curveToRelative(
                    dx1 = 0.298f,
                    dy1 = -0.264f,
                    dx2 = 0.446f,
                    dy2 = -0.374f,
                    dx3 = 0.668f,
                    dy3 = -0.374f,
                )
                // c 0.273 0 0.468 0.029 0.638 0.085
                curveToRelative(
                    dx1 = 0.273f,
                    dy1 = 0.0f,
                    dx2 = 0.468f,
                    dy2 = 0.029f,
                    dx3 = 0.638f,
                    dy3 = 0.085f,
                )
                // c 0.17 0.057 0.344 0.151 0.562 0.315
                curveToRelative(
                    dx1 = 0.17f,
                    dy1 = 0.057f,
                    dx2 = 0.344f,
                    dy2 = 0.151f,
                    dx3 = 0.562f,
                    dy3 = 0.315f,
                )
                // a 0.5 0.5 0 0 0 0.6 -0.8
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.6f,
                    dy1 = -0.8f,
                )
                // a 2.951 2.951 0 0 0 -0.846 -0.463
                arcToRelative(
                    a = 2.951f,
                    b = 2.951f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.846f,
                    dy1 = -0.463f,
                )
                // c -0.301 -0.1 -0.61 -0.137 -0.954 -0.137
                curveToRelative(
                    dx1 = -0.301f,
                    dy1 = -0.1f,
                    dx2 = -0.61f,
                    dy2 = -0.137f,
                    dx3 = -0.954f,
                    dy3 = -0.137f,
                )
                // c -0.629 0 -1.037 0.364 -1.304 0.601
                curveToRelative(
                    dx1 = -0.629f,
                    dy1 = 0.0f,
                    dx2 = -1.037f,
                    dy2 = 0.364f,
                    dx3 = -1.304f,
                    dy3 = 0.601f,
                )
                // l -0.028 0.025
                lineToRelative(dx = -0.028f, dy = 0.025f)
                // c -0.298 0.264 -0.446 0.374 -0.668 0.374
                curveToRelative(
                    dx1 = -0.298f,
                    dy1 = 0.264f,
                    dx2 = -0.446f,
                    dy2 = 0.374f,
                    dx3 = -0.668f,
                    dy3 = 0.374f,
                )
                // c -0.273 0 -0.468 -0.029 -0.638 -0.085
                curveToRelative(
                    dx1 = -0.273f,
                    dy1 = 0.0f,
                    dx2 = -0.468f,
                    dy2 = -0.029f,
                    dx3 = -0.638f,
                    dy3 = -0.085f,
                )
                // a 1.968 1.968 0 0 1 -0.562 -0.315
                arcToRelative(
                    a = 1.968f,
                    b = 1.968f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.562f,
                    dy1 = -0.315f,
                )
                // a 0.5 0.5 0 0 0 -0.6 0.8
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.6f,
                    dy1 = 0.8f,
                )
                // c 0.276 0.207 0.544 0.363 0.846 0.463
                curveToRelative(
                    dx1 = 0.276f,
                    dy1 = 0.207f,
                    dx2 = 0.544f,
                    dy2 = 0.363f,
                    dx3 = 0.846f,
                    dy3 = 0.463f,
                )
                // c 0.301 0.1 0.61 0.137 0.954 0.137z
                curveToRelative(
                    dx1 = 0.301f,
                    dy1 = 0.1f,
                    dx2 = 0.61f,
                    dy2 = 0.137f,
                    dx3 = 0.954f,
                    dy3 = 0.137f,
                )
                close()
            }
        }.build().also { _ic1010 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1010: ImageVector? = null
