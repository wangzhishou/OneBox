package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic314: ImageVector
    get() {
        val current = _ic314
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic314",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M9 11 a1 1 0 1 0 2 0 c0 -.5 -.555 -1.395 -1 -2 -.445 .605 -1 1.5 -1 2Z m-7.707 1.707 A1 1 0 0 1 1 12 c0 -.5 .555 -1.395 1 -2 .445 .605 1 1.5 1 2 a1 1 0 0 1 -1.707 .707Z m3 0 A1 1 0 0 1 4 12 c0 -.5 .555 -1.395 1 -2 .445 .605 1 1.5 1 2 a1 1 0 0 1 -1.707 .707Z m9 -1 A1 1 0 0 1 13 11 c0 -.5 .555 -1.395 1 -2 .445 .605 1 1.5 1 2 a1 1 0 0 1 -1.707 .707Z M11 14 a1 1 0 0 0 2 0 c0 -.5 -.555 -1.395 -1 -2 -.445 .605 -1 1.5 -1 2Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 9 11
                moveTo(x = 9.0f, y = 11.0f)
                // a 1 1 0 1 0 2 0
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 2.0f,
                    dy1 = 0.0f,
                )
                // c 0 -0.5 -0.555 -1.395 -1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                    dx2 = -0.555f,
                    dy2 = -1.395f,
                    dx3 = -1.0f,
                    dy3 = -2.0f,
                )
                // c -0.445 0.605 -1 1.5 -1 2z
                curveToRelative(
                    dx1 = -0.445f,
                    dy1 = 0.605f,
                    dx2 = -1.0f,
                    dy2 = 1.5f,
                    dx3 = -1.0f,
                    dy3 = 2.0f,
                )
                close()
                // m -7.707 1.707
                moveToRelative(dx = -7.707f, dy = 1.707f)
                // A 1 1 0 0 1 1 12
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 1.0f,
                    y1 = 12.0f,
                )
                // c 0 -0.5 0.555 -1.395 1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                    dx2 = 0.555f,
                    dy2 = -1.395f,
                    dx3 = 1.0f,
                    dy3 = -2.0f,
                )
                // c 0.445 0.605 1 1.5 1 2
                curveToRelative(
                    dx1 = 0.445f,
                    dy1 = 0.605f,
                    dx2 = 1.0f,
                    dy2 = 1.5f,
                    dx3 = 1.0f,
                    dy3 = 2.0f,
                )
                // a 1 1 0 0 1 -1.707 0.707z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.707f,
                    dy1 = 0.707f,
                )
                close()
                // m 3 0
                moveToRelative(dx = 3.0f, dy = 0.0f)
                // A 1 1 0 0 1 4 12
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 4.0f,
                    y1 = 12.0f,
                )
                // c 0 -0.5 0.555 -1.395 1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                    dx2 = 0.555f,
                    dy2 = -1.395f,
                    dx3 = 1.0f,
                    dy3 = -2.0f,
                )
                // c 0.445 0.605 1 1.5 1 2
                curveToRelative(
                    dx1 = 0.445f,
                    dy1 = 0.605f,
                    dx2 = 1.0f,
                    dy2 = 1.5f,
                    dx3 = 1.0f,
                    dy3 = 2.0f,
                )
                // a 1 1 0 0 1 -1.707 0.707z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.707f,
                    dy1 = 0.707f,
                )
                close()
                // m 9 -1
                moveToRelative(dx = 9.0f, dy = -1.0f)
                // A 1 1 0 0 1 13 11
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 13.0f,
                    y1 = 11.0f,
                )
                // c 0 -0.5 0.555 -1.395 1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                    dx2 = 0.555f,
                    dy2 = -1.395f,
                    dx3 = 1.0f,
                    dy3 = -2.0f,
                )
                // c 0.445 0.605 1 1.5 1 2
                curveToRelative(
                    dx1 = 0.445f,
                    dy1 = 0.605f,
                    dx2 = 1.0f,
                    dy2 = 1.5f,
                    dx3 = 1.0f,
                    dy3 = 2.0f,
                )
                // a 1 1 0 0 1 -1.707 0.707z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.707f,
                    dy1 = 0.707f,
                )
                close()
                // M 11 14
                moveTo(x = 11.0f, y = 14.0f)
                // a 1 1 0 0 0 2 0
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.0f,
                    dy1 = 0.0f,
                )
                // c 0 -0.5 -0.555 -1.395 -1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                    dx2 = -0.555f,
                    dy2 = -1.395f,
                    dx3 = -1.0f,
                    dy3 = -2.0f,
                )
                // c -0.445 0.605 -1 1.5 -1 2z
                curveToRelative(
                    dx1 = -0.445f,
                    dy1 = 0.605f,
                    dx2 = -1.0f,
                    dy2 = 1.5f,
                    dx3 = -1.0f,
                    dy3 = 2.0f,
                )
                close()
            }
            // M7.386 .342 A.5 .5 0 0 1 7.86 0 h.28 a.5 .5 0 0 1 .474 .342 l.167 .5 a.513 .513 0 0 1 .015 .055 c1.855 .14 3.535 .764 4.808 1.72 C15.056 3.707 16 5.252 16 7 a.5 .5 0 0 1 -.826 .38 h-4.348 l.001 -.002 h4.346 l-.003 -.002 a3.561 3.561 0 0 0 -.58 -.361 A3.597 3.597 0 0 0 13 6.633 c-.653 0 -1.202 .19 -1.59 .382 a3.561 3.561 0 0 0 -.58 .36 l-.003 .003 h-.654 l-.003 -.002 a3.557 3.557 0 0 0 -.58 -.361 A3.681 3.681 0 0 0 8.5 6.67 v.708 h1.673 l.001 .001 H8.5 V15.5 a.5 .5 0 0 1 -1 0 V7.38 H.826 A.5 .5 0 0 1 0 7 c0 -1.748 .944 -3.293 2.396 -4.383 C3.67 1.66 5.35 1.037 7.204 .897 A.513 .513 0 0 1 7.22 .842 l.167 -.5Z M7.5 7.379 V6.67 a3.681 3.681 0 0 0 -1.67 .706 l-.003 .002 L7.5 7.38Z M3 5.633 a4.59 4.59 0 0 1 1.964 .451 c.02 -.604 .118 -1.31 .348 -2.022 a5.848 5.848 0 0 1 1.148 -2.07 c-1.346 .223 -2.54 .73 -3.463 1.424 -.962 .723 -1.617 1.633 -1.875 2.629 A4.571 4.571 0 0 1 3 5.633Z M6.263 4.37 a6.471 6.471 0 0 0 -.3 1.75 h.003 A4.597 4.597 0 0 1 8 5.633 a4.59 4.59 0 0 1 2.034 .486 h.003 a6.47 6.47 0 0 0 -.3 -1.75 C9.444 3.461 8.91 2.572 8 1.956 c-.911 .616 -1.444 1.505 -1.737 2.413Z M13 5.633 c.76 0 1.402 .197 1.878 .412 -.258 -.996 -.913 -1.906 -1.875 -2.629 -.923 -.693 -2.117 -1.201 -3.463 -1.424 .556 .634 .92 1.36 1.148 2.07 .23 .712 .328 1.418 .348 2.022 A4.585 4.585 0 0 1 13 5.634Z M.83 7.376 l-.004 .003 h4.348 l-.004 -.003 a3.56 3.56 0 0 0 -.58 -.361 A3.597 3.597 0 0 0 3 6.633 c-.653 0 -1.202 .19 -1.59 .382 a3.56 3.56 0 0 0 -.58 .36Z m4.344 .003 a.5 .5 0 0 0 .652 0 h-.652Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.386 0.342
                moveTo(x = 7.386f, y = 0.342f)
                // A 0.5 0.5 0 0 1 7.86 0
                arcTo(
                    horizontalEllipseRadius = 0.5f,
                    verticalEllipseRadius = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.86f,
                    y1 = 0.0f,
                )
                // h 0.28
                horizontalLineToRelative(dx = 0.28f)
                // a 0.5 0.5 0 0 1 0.474 0.342
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.474f,
                    dy1 = 0.342f,
                )
                // l 0.167 0.5
                lineToRelative(dx = 0.167f, dy = 0.5f)
                // a 0.513 0.513 0 0 1 0.015 0.055
                arcToRelative(
                    a = 0.513f,
                    b = 0.513f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.015f,
                    dy1 = 0.055f,
                )
                // c 1.855 0.14 3.535 0.764 4.808 1.72
                curveToRelative(
                    dx1 = 1.855f,
                    dy1 = 0.14f,
                    dx2 = 3.535f,
                    dy2 = 0.764f,
                    dx3 = 4.808f,
                    dy3 = 1.72f,
                )
                // C 15.056 3.707 16 5.252 16 7
                curveTo(
                    x1 = 15.056f,
                    y1 = 3.707f,
                    x2 = 16.0f,
                    y2 = 5.252f,
                    x3 = 16.0f,
                    y3 = 7.0f,
                )
                // a 0.5 0.5 0 0 1 -0.826 0.38
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.826f,
                    dy1 = 0.38f,
                )
                // h -4.348
                horizontalLineToRelative(dx = -4.348f)
                // l 0.001 -0.002
                lineToRelative(dx = 0.001f, dy = -0.002f)
                // h 4.346
                horizontalLineToRelative(dx = 4.346f)
                // l -0.003 -0.002
                lineToRelative(dx = -0.003f, dy = -0.002f)
                // a 3.561 3.561 0 0 0 -0.58 -0.361
                arcToRelative(
                    a = 3.561f,
                    b = 3.561f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.58f,
                    dy1 = -0.361f,
                )
                // A 3.597 3.597 0 0 0 13 6.633
                arcTo(
                    horizontalEllipseRadius = 3.597f,
                    verticalEllipseRadius = 3.597f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 13.0f,
                    y1 = 6.633f,
                )
                // c -0.653 0 -1.202 0.19 -1.59 0.382
                curveToRelative(
                    dx1 = -0.653f,
                    dy1 = 0.0f,
                    dx2 = -1.202f,
                    dy2 = 0.19f,
                    dx3 = -1.59f,
                    dy3 = 0.382f,
                )
                // a 3.561 3.561 0 0 0 -0.58 0.36
                arcToRelative(
                    a = 3.561f,
                    b = 3.561f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.58f,
                    dy1 = 0.36f,
                )
                // l -0.003 0.003
                lineToRelative(dx = -0.003f, dy = 0.003f)
                // h -0.654
                horizontalLineToRelative(dx = -0.654f)
                // l -0.003 -0.002
                lineToRelative(dx = -0.003f, dy = -0.002f)
                // a 3.557 3.557 0 0 0 -0.58 -0.361
                arcToRelative(
                    a = 3.557f,
                    b = 3.557f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.58f,
                    dy1 = -0.361f,
                )
                // A 3.681 3.681 0 0 0 8.5 6.67
                arcTo(
                    horizontalEllipseRadius = 3.681f,
                    verticalEllipseRadius = 3.681f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 8.5f,
                    y1 = 6.67f,
                )
                // v 0.708
                verticalLineToRelative(dy = 0.708f)
                // h 1.673
                horizontalLineToRelative(dx = 1.673f)
                // l 0.001 0.001
                lineToRelative(dx = 0.001f, dy = 0.001f)
                // H 8.5
                horizontalLineTo(x = 8.5f)
                // V 15.5
                verticalLineTo(y = 15.5f)
                // a 0.5 0.5 0 0 1 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // V 7.38
                verticalLineTo(y = 7.38f)
                // H 0.826
                horizontalLineTo(x = 0.826f)
                // A 0.5 0.5 0 0 1 0 7
                arcTo(
                    horizontalEllipseRadius = 0.5f,
                    verticalEllipseRadius = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 0.0f,
                    y1 = 7.0f,
                )
                // c 0 -1.748 0.944 -3.293 2.396 -4.383
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.748f,
                    dx2 = 0.944f,
                    dy2 = -3.293f,
                    dx3 = 2.396f,
                    dy3 = -4.383f,
                )
                // C 3.67 1.66 5.35 1.037 7.204 0.897
                curveTo(
                    x1 = 3.67f,
                    y1 = 1.66f,
                    x2 = 5.35f,
                    y2 = 1.037f,
                    x3 = 7.204f,
                    y3 = 0.897f,
                )
                // A 0.513 0.513 0 0 1 7.22 0.842
                arcTo(
                    horizontalEllipseRadius = 0.513f,
                    verticalEllipseRadius = 0.513f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.22f,
                    y1 = 0.842f,
                )
                // l 0.167 -0.5z
                lineToRelative(dx = 0.167f, dy = -0.5f)
                close()
                // M 7.5 7.379
                moveTo(x = 7.5f, y = 7.379f)
                // V 6.67
                verticalLineTo(y = 6.67f)
                // a 3.681 3.681 0 0 0 -1.67 0.706
                arcToRelative(
                    a = 3.681f,
                    b = 3.681f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.67f,
                    dy1 = 0.706f,
                )
                // l -0.003 0.002
                lineToRelative(dx = -0.003f, dy = 0.002f)
                // L 7.5 7.38z
                lineTo(x = 7.5f, y = 7.38f)
                close()
                // M 3 5.633
                moveTo(x = 3.0f, y = 5.633f)
                // a 4.59 4.59 0 0 1 1.964 0.451
                arcToRelative(
                    a = 4.59f,
                    b = 4.59f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.964f,
                    dy1 = 0.451f,
                )
                // c 0.02 -0.604 0.118 -1.31 0.348 -2.022
                curveToRelative(
                    dx1 = 0.02f,
                    dy1 = -0.604f,
                    dx2 = 0.118f,
                    dy2 = -1.31f,
                    dx3 = 0.348f,
                    dy3 = -2.022f,
                )
                // a 5.848 5.848 0 0 1 1.148 -2.07
                arcToRelative(
                    a = 5.848f,
                    b = 5.848f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.148f,
                    dy1 = -2.07f,
                )
                // c -1.346 0.223 -2.54 0.73 -3.463 1.424
                curveToRelative(
                    dx1 = -1.346f,
                    dy1 = 0.223f,
                    dx2 = -2.54f,
                    dy2 = 0.73f,
                    dx3 = -3.463f,
                    dy3 = 1.424f,
                )
                // c -0.962 0.723 -1.617 1.633 -1.875 2.629
                curveToRelative(
                    dx1 = -0.962f,
                    dy1 = 0.723f,
                    dx2 = -1.617f,
                    dy2 = 1.633f,
                    dx3 = -1.875f,
                    dy3 = 2.629f,
                )
                // A 4.571 4.571 0 0 1 3 5.633z
                arcTo(
                    horizontalEllipseRadius = 4.571f,
                    verticalEllipseRadius = 4.571f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 3.0f,
                    y1 = 5.633f,
                )
                close()
                // M 6.263 4.37
                moveTo(x = 6.263f, y = 4.37f)
                // a 6.471 6.471 0 0 0 -0.3 1.75
                arcToRelative(
                    a = 6.471f,
                    b = 6.471f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.3f,
                    dy1 = 1.75f,
                )
                // h 0.003
                horizontalLineToRelative(dx = 0.003f)
                // A 4.597 4.597 0 0 1 8 5.633
                arcTo(
                    horizontalEllipseRadius = 4.597f,
                    verticalEllipseRadius = 4.597f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 8.0f,
                    y1 = 5.633f,
                )
                // a 4.59 4.59 0 0 1 2.034 0.486
                arcToRelative(
                    a = 4.59f,
                    b = 4.59f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.034f,
                    dy1 = 0.486f,
                )
                // h 0.003
                horizontalLineToRelative(dx = 0.003f)
                // a 6.47 6.47 0 0 0 -0.3 -1.75
                arcToRelative(
                    a = 6.47f,
                    b = 6.47f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.3f,
                    dy1 = -1.75f,
                )
                // C 9.444 3.461 8.91 2.572 8 1.956
                curveTo(
                    x1 = 9.444f,
                    y1 = 3.461f,
                    x2 = 8.91f,
                    y2 = 2.572f,
                    x3 = 8.0f,
                    y3 = 1.956f,
                )
                // c -0.911 0.616 -1.444 1.505 -1.737 2.413z
                curveToRelative(
                    dx1 = -0.911f,
                    dy1 = 0.616f,
                    dx2 = -1.444f,
                    dy2 = 1.505f,
                    dx3 = -1.737f,
                    dy3 = 2.413f,
                )
                close()
                // M 13 5.633
                moveTo(x = 13.0f, y = 5.633f)
                // c 0.76 0 1.402 0.197 1.878 0.412
                curveToRelative(
                    dx1 = 0.76f,
                    dy1 = 0.0f,
                    dx2 = 1.402f,
                    dy2 = 0.197f,
                    dx3 = 1.878f,
                    dy3 = 0.412f,
                )
                // c -0.258 -0.996 -0.913 -1.906 -1.875 -2.629
                curveToRelative(
                    dx1 = -0.258f,
                    dy1 = -0.996f,
                    dx2 = -0.913f,
                    dy2 = -1.906f,
                    dx3 = -1.875f,
                    dy3 = -2.629f,
                )
                // c -0.923 -0.693 -2.117 -1.201 -3.463 -1.424
                curveToRelative(
                    dx1 = -0.923f,
                    dy1 = -0.693f,
                    dx2 = -2.117f,
                    dy2 = -1.201f,
                    dx3 = -3.463f,
                    dy3 = -1.424f,
                )
                // c 0.556 0.634 0.92 1.36 1.148 2.07
                curveToRelative(
                    dx1 = 0.556f,
                    dy1 = 0.634f,
                    dx2 = 0.92f,
                    dy2 = 1.36f,
                    dx3 = 1.148f,
                    dy3 = 2.07f,
                )
                // c 0.23 0.712 0.328 1.418 0.348 2.022
                curveToRelative(
                    dx1 = 0.23f,
                    dy1 = 0.712f,
                    dx2 = 0.328f,
                    dy2 = 1.418f,
                    dx3 = 0.348f,
                    dy3 = 2.022f,
                )
                // A 4.585 4.585 0 0 1 13 5.634z
                arcTo(
                    horizontalEllipseRadius = 4.585f,
                    verticalEllipseRadius = 4.585f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 13.0f,
                    y1 = 5.634f,
                )
                close()
                // M 0.83 7.376
                moveTo(x = 0.83f, y = 7.376f)
                // l -0.004 0.003
                lineToRelative(dx = -0.004f, dy = 0.003f)
                // h 4.348
                horizontalLineToRelative(dx = 4.348f)
                // l -0.004 -0.003
                lineToRelative(dx = -0.004f, dy = -0.003f)
                // a 3.56 3.56 0 0 0 -0.58 -0.361
                arcToRelative(
                    a = 3.56f,
                    b = 3.56f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.58f,
                    dy1 = -0.361f,
                )
                // A 3.597 3.597 0 0 0 3 6.633
                arcTo(
                    horizontalEllipseRadius = 3.597f,
                    verticalEllipseRadius = 3.597f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 3.0f,
                    y1 = 6.633f,
                )
                // c -0.653 0 -1.202 0.19 -1.59 0.382
                curveToRelative(
                    dx1 = -0.653f,
                    dy1 = 0.0f,
                    dx2 = -1.202f,
                    dy2 = 0.19f,
                    dx3 = -1.59f,
                    dy3 = 0.382f,
                )
                // a 3.56 3.56 0 0 0 -0.58 0.36z
                arcToRelative(
                    a = 3.56f,
                    b = 3.56f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.58f,
                    dy1 = 0.36f,
                )
                close()
                // m 4.344 0.003
                moveToRelative(dx = 4.344f, dy = 0.003f)
                // a 0.5 0.5 0 0 0 0.652 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.652f,
                    dy1 = 0.0f,
                )
                // h -0.652z
                horizontalLineToRelative(dx = -0.652f)
                close()
            }
            // M10.174 7.38 h.652 a.5 .5 0 0 1 -.652 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10.174 7.38
                moveTo(x = 10.174f, y = 7.38f)
                // h 0.652
                horizontalLineToRelative(dx = 0.652f)
                // a 0.5 0.5 0 0 1 -0.652 0z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.652f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic314 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic314: ImageVector? = null
