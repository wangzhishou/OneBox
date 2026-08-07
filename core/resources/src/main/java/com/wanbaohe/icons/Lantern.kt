package com.wanbaohe.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.Lantern: ImageVector
    get() {
        val current = _lantern
        if (current != null) return current

        return ImageVector.Builder(
            name = "AppTheme.Lantern",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            // m12.593 23.258 l-.011 .002 l-.071 .035 l-.02 .004 l-.014 -.004 l-.071 -.035 q-.016 -.005 -.024 .005 l-.004 .01 l-.017 .428 l.005 .02 l.01 .013 l.104 .074 l.015 .004 l.012 -.004 l.104 -.074 l.012 -.016 l.004 -.017 l-.017 -.427 q-.004 -.016 -.017 -.018 m.265 -.113 l-.013 .002 l-.185 .093 l-.01 .01 l-.003 .011 l.018 .43 l.005 .012 l.008 .007 l.201 .093 q.019 .005 .029 -.008 l.004 -.014 l-.034 -.614 q-.005 -.018 -.02 -.022 m-.715 .002 a.02 .02 0 0 0 -.027 .006 l-.006 .014 l-.034 .614 q.001 .018 .017 .024 l.015 -.002 l.201 -.093 l.01 -.008 l.004 -.011 l.017 -.43 l-.003 -.012 l-.01 -.01z
            path(
                pathFillType = PathFillType.EvenOdd,
            ) {
                // M 12.593 23.258
                moveTo(x = 12.593f, y = 23.258f)
                // l -0.011 0.002
                lineToRelative(dx = -0.011f, dy = 0.002f)
                // l -0.071 0.035
                lineToRelative(dx = -0.071f, dy = 0.035f)
                // l -0.02 0.004
                lineToRelative(dx = -0.02f, dy = 0.004f)
                // l -0.014 -0.004
                lineToRelative(dx = -0.014f, dy = -0.004f)
                // l -0.071 -0.035
                lineToRelative(dx = -0.071f, dy = -0.035f)
                // q -0.016 -0.005 -0.024 0.005
                quadToRelative(
                    dx1 = -0.016f,
                    dy1 = -0.005f,
                    dx2 = -0.024f,
                    dy2 = 0.005f,
                )
                // l -0.004 0.01
                lineToRelative(dx = -0.004f, dy = 0.01f)
                // l -0.017 0.428
                lineToRelative(dx = -0.017f, dy = 0.428f)
                // l 0.005 0.02
                lineToRelative(dx = 0.005f, dy = 0.02f)
                // l 0.01 0.013
                lineToRelative(dx = 0.01f, dy = 0.013f)
                // l 0.104 0.074
                lineToRelative(dx = 0.104f, dy = 0.074f)
                // l 0.015 0.004
                lineToRelative(dx = 0.015f, dy = 0.004f)
                // l 0.012 -0.004
                lineToRelative(dx = 0.012f, dy = -0.004f)
                // l 0.104 -0.074
                lineToRelative(dx = 0.104f, dy = -0.074f)
                // l 0.012 -0.016
                lineToRelative(dx = 0.012f, dy = -0.016f)
                // l 0.004 -0.017
                lineToRelative(dx = 0.004f, dy = -0.017f)
                // l -0.017 -0.427
                lineToRelative(dx = -0.017f, dy = -0.427f)
                // q -0.004 -0.016 -0.017 -0.018
                quadToRelative(
                    dx1 = -0.004f,
                    dy1 = -0.016f,
                    dx2 = -0.017f,
                    dy2 = -0.018f,
                )
                // m 0.265 -0.113
                moveToRelative(dx = 0.265f, dy = -0.113f)
                // l -0.013 0.002
                lineToRelative(dx = -0.013f, dy = 0.002f)
                // l -0.185 0.093
                lineToRelative(dx = -0.185f, dy = 0.093f)
                // l -0.01 0.01
                lineToRelative(dx = -0.01f, dy = 0.01f)
                // l -0.003 0.011
                lineToRelative(dx = -0.003f, dy = 0.011f)
                // l 0.018 0.43
                lineToRelative(dx = 0.018f, dy = 0.43f)
                // l 0.005 0.012
                lineToRelative(dx = 0.005f, dy = 0.012f)
                // l 0.008 0.007
                lineToRelative(dx = 0.008f, dy = 0.007f)
                // l 0.201 0.093
                lineToRelative(dx = 0.201f, dy = 0.093f)
                // q 0.019 0.005 0.029 -0.008
                quadToRelative(
                    dx1 = 0.019f,
                    dy1 = 0.005f,
                    dx2 = 0.029f,
                    dy2 = -0.008f,
                )
                // l 0.004 -0.014
                lineToRelative(dx = 0.004f, dy = -0.014f)
                // l -0.034 -0.614
                lineToRelative(dx = -0.034f, dy = -0.614f)
                // q -0.005 -0.018 -0.02 -0.022
                quadToRelative(
                    dx1 = -0.005f,
                    dy1 = -0.018f,
                    dx2 = -0.02f,
                    dy2 = -0.022f,
                )
                // m -0.715 0.002
                moveToRelative(dx = -0.715f, dy = 0.002f)
                // a 0.02 0.02 0 0 0 -0.027 0.006
                arcToRelative(
                    a = 0.02f,
                    b = 0.02f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.027f,
                    dy1 = 0.006f,
                )
                // l -0.006 0.014
                lineToRelative(dx = -0.006f, dy = 0.014f)
                // l -0.034 0.614
                lineToRelative(dx = -0.034f, dy = 0.614f)
                // q 0.001 0.018 0.017 0.024
                quadToRelative(
                    dx1 = 0.001f,
                    dy1 = 0.018f,
                    dx2 = 0.017f,
                    dy2 = 0.024f,
                )
                // l 0.015 -0.002
                lineToRelative(dx = 0.015f, dy = -0.002f)
                // l 0.201 -0.093
                lineToRelative(dx = 0.201f, dy = -0.093f)
                // l 0.01 -0.008
                lineToRelative(dx = 0.01f, dy = -0.008f)
                // l 0.004 -0.011
                lineToRelative(dx = 0.004f, dy = -0.011f)
                // l 0.017 -0.43
                lineToRelative(dx = 0.017f, dy = -0.43f)
                // l -0.003 -0.012
                lineToRelative(dx = -0.003f, dy = -0.012f)
                // l -0.01 -0.01z
                lineToRelative(dx = -0.01f, dy = -0.01f)
                close()
            }
            // M12 2 a1 1 0 0 1 .993 .883 L13 3 v1 h1 a1 1 0 0 1 .993 .883 L15 5 h1 a1 1 0 0 1 .997 .924 c.696 .278 1.341 .614 1.922 1.001 C20.726 8.13 22 9.905 22 12 s-1.274 3.87 -3.081 5.075 c-1.125 .75 -2.493 1.31 -4.004 1.627 l-.415 .08 V21 a1 1 0 0 1 -1.993 .117 L12.5 21 v-2.009 Q12.251 19 12 19 l-.25 -.002 l-.25 -.007 V21 a1 1 0 0 1 -1.993 .117 L9.5 21 v-2.218 c-1.675 -.297 -3.192 -.89 -4.419 -1.707 C3.274 15.87 2 14.095 2 12 s1.274 -3.87 3.081 -5.075 a10.5 10.5 0 0 1 1.922 -1.001 a1 1 0 0 1 .877 -.917 L8 5 h1 a1 1 0 0 1 .883 -.993 L10 4 h1 V3 a1 1 0 0 1 1 -1 m0 5.16 c-.34 .222 -.818 .572 -1.293 1.047 C9.817 9.098 9 10.36 9 12 s.816 2.902 1.707 3.793 A8 8 0 0 0 12 16.839 a8 8 0 0 0 1.293 -1.046 C14.183 14.902 15 13.64 15 12 s-.816 -2.902 -1.707 -3.793 A8 8 0 0 0 12 7.161 m-3.308 .296 A9 9 0 0 0 6.19 8.589 C4.74 9.556 4 10.781 4 12 s.74 2.444 2.19 3.41 a9 9 0 0 0 2.502 1.134 C7.795 15.45 7 13.932 7 12 s.795 -3.45 1.692 -4.544 m6.616 0 C16.205 8.55 17 10.068 17 12 s-.795 3.45 -1.692 4.544 a9 9 0 0 0 2.501 -1.133 C19.26 14.444 20 13.219 20 12 s-.74 -2.444 -2.19 -3.41 a9 9 0 0 0 -2.502 -1.134
            path(
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.EvenOdd,
            ) {
                // M 12 2
                moveTo(x = 12.0f, y = 2.0f)
                // a 1 1 0 0 1 0.993 0.883
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.993f,
                    dy1 = 0.883f,
                )
                // L 13 3
                lineTo(x = 13.0f, y = 3.0f)
                // v 1
                verticalLineToRelative(dy = 1.0f)
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // a 1 1 0 0 1 0.993 0.883
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.993f,
                    dy1 = 0.883f,
                )
                // L 15 5
                lineTo(x = 15.0f, y = 5.0f)
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // a 1 1 0 0 1 0.997 0.924
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.997f,
                    dy1 = 0.924f,
                )
                // c 0.696 0.278 1.341 0.614 1.922 1.001
                curveToRelative(
                    dx1 = 0.696f,
                    dy1 = 0.278f,
                    dx2 = 1.341f,
                    dy2 = 0.614f,
                    dx3 = 1.922f,
                    dy3 = 1.001f,
                )
                // C 20.726 8.13 22 9.905 22 12
                curveTo(
                    x1 = 20.726f,
                    y1 = 8.13f,
                    x2 = 22.0f,
                    y2 = 9.905f,
                    x3 = 22.0f,
                    y3 = 12.0f,
                )
                // s -1.274 3.87 -3.081 5.075
                reflectiveCurveToRelative(
                    dx1 = -1.274f,
                    dy1 = 3.87f,
                    dx2 = -3.081f,
                    dy2 = 5.075f,
                )
                // c -1.125 0.75 -2.493 1.31 -4.004 1.627
                curveToRelative(
                    dx1 = -1.125f,
                    dy1 = 0.75f,
                    dx2 = -2.493f,
                    dy2 = 1.31f,
                    dx3 = -4.004f,
                    dy3 = 1.627f,
                )
                // l -0.415 0.08
                lineToRelative(dx = -0.415f, dy = 0.08f)
                // V 21
                verticalLineTo(y = 21.0f)
                // a 1 1 0 0 1 -1.993 0.117
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.993f,
                    dy1 = 0.117f,
                )
                // L 12.5 21
                lineTo(x = 12.5f, y = 21.0f)
                // v -2.009
                verticalLineToRelative(dy = -2.009f)
                // Q 12.251 19 12 19
                quadTo(
                    x1 = 12.251f,
                    y1 = 19.0f,
                    x2 = 12.0f,
                    y2 = 19.0f,
                )
                // l -0.25 -0.002
                lineToRelative(dx = -0.25f, dy = -0.002f)
                // l -0.25 -0.007
                lineToRelative(dx = -0.25f, dy = -0.007f)
                // V 21
                verticalLineTo(y = 21.0f)
                // a 1 1 0 0 1 -1.993 0.117
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.993f,
                    dy1 = 0.117f,
                )
                // L 9.5 21
                lineTo(x = 9.5f, y = 21.0f)
                // v -2.218
                verticalLineToRelative(dy = -2.218f)
                // c -1.675 -0.297 -3.192 -0.89 -4.419 -1.707
                curveToRelative(
                    dx1 = -1.675f,
                    dy1 = -0.297f,
                    dx2 = -3.192f,
                    dy2 = -0.89f,
                    dx3 = -4.419f,
                    dy3 = -1.707f,
                )
                // C 3.274 15.87 2 14.095 2 12
                curveTo(
                    x1 = 3.274f,
                    y1 = 15.87f,
                    x2 = 2.0f,
                    y2 = 14.095f,
                    x3 = 2.0f,
                    y3 = 12.0f,
                )
                // s 1.274 -3.87 3.081 -5.075
                reflectiveCurveToRelative(
                    dx1 = 1.274f,
                    dy1 = -3.87f,
                    dx2 = 3.081f,
                    dy2 = -5.075f,
                )
                // a 10.5 10.5 0 0 1 1.922 -1.001
                arcToRelative(
                    a = 10.5f,
                    b = 10.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.922f,
                    dy1 = -1.001f,
                )
                // a 1 1 0 0 1 0.877 -0.917
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.877f,
                    dy1 = -0.917f,
                )
                // L 8 5
                lineTo(x = 8.0f, y = 5.0f)
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // a 1 1 0 0 1 0.883 -0.993
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.883f,
                    dy1 = -0.993f,
                )
                // L 10 4
                lineTo(x = 10.0f, y = 4.0f)
                // h 1
                horizontalLineToRelative(dx = 1.0f)
                // V 3
                verticalLineTo(y = 3.0f)
                // a 1 1 0 0 1 1 -1
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = -1.0f,
                )
                // m 0 5.16
                moveToRelative(dx = 0.0f, dy = 5.16f)
                // c -0.34 0.222 -0.818 0.572 -1.293 1.047
                curveToRelative(
                    dx1 = -0.34f,
                    dy1 = 0.222f,
                    dx2 = -0.818f,
                    dy2 = 0.572f,
                    dx3 = -1.293f,
                    dy3 = 1.047f,
                )
                // C 9.817 9.098 9 10.36 9 12
                curveTo(
                    x1 = 9.817f,
                    y1 = 9.098f,
                    x2 = 9.0f,
                    y2 = 10.36f,
                    x3 = 9.0f,
                    y3 = 12.0f,
                )
                // s 0.816 2.902 1.707 3.793
                reflectiveCurveToRelative(
                    dx1 = 0.816f,
                    dy1 = 2.902f,
                    dx2 = 1.707f,
                    dy2 = 3.793f,
                )
                // A 8 8 0 0 0 12 16.839
                arcTo(
                    horizontalEllipseRadius = 8.0f,
                    verticalEllipseRadius = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 12.0f,
                    y1 = 16.839f,
                )
                // a 8 8 0 0 0 1.293 -1.046
                arcToRelative(
                    a = 8.0f,
                    b = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.293f,
                    dy1 = -1.046f,
                )
                // C 14.183 14.902 15 13.64 15 12
                curveTo(
                    x1 = 14.183f,
                    y1 = 14.902f,
                    x2 = 15.0f,
                    y2 = 13.64f,
                    x3 = 15.0f,
                    y3 = 12.0f,
                )
                // s -0.816 -2.902 -1.707 -3.793
                reflectiveCurveToRelative(
                    dx1 = -0.816f,
                    dy1 = -2.902f,
                    dx2 = -1.707f,
                    dy2 = -3.793f,
                )
                // A 8 8 0 0 0 12 7.161
                arcTo(
                    horizontalEllipseRadius = 8.0f,
                    verticalEllipseRadius = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 12.0f,
                    y1 = 7.161f,
                )
                // m -3.308 0.296
                moveToRelative(dx = -3.308f, dy = 0.296f)
                // A 9 9 0 0 0 6.19 8.589
                arcTo(
                    horizontalEllipseRadius = 9.0f,
                    verticalEllipseRadius = 9.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 6.19f,
                    y1 = 8.589f,
                )
                // C 4.74 9.556 4 10.781 4 12
                curveTo(
                    x1 = 4.74f,
                    y1 = 9.556f,
                    x2 = 4.0f,
                    y2 = 10.781f,
                    x3 = 4.0f,
                    y3 = 12.0f,
                )
                // s 0.74 2.444 2.19 3.41
                reflectiveCurveToRelative(
                    dx1 = 0.74f,
                    dy1 = 2.444f,
                    dx2 = 2.19f,
                    dy2 = 3.41f,
                )
                // a 9 9 0 0 0 2.502 1.134
                arcToRelative(
                    a = 9.0f,
                    b = 9.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.502f,
                    dy1 = 1.134f,
                )
                // C 7.795 15.45 7 13.932 7 12
                curveTo(
                    x1 = 7.795f,
                    y1 = 15.45f,
                    x2 = 7.0f,
                    y2 = 13.932f,
                    x3 = 7.0f,
                    y3 = 12.0f,
                )
                // s 0.795 -3.45 1.692 -4.544
                reflectiveCurveToRelative(
                    dx1 = 0.795f,
                    dy1 = -3.45f,
                    dx2 = 1.692f,
                    dy2 = -4.544f,
                )
                // m 6.616 0
                moveToRelative(dx = 6.616f, dy = 0.0f)
                // C 16.205 8.55 17 10.068 17 12
                curveTo(
                    x1 = 16.205f,
                    y1 = 8.55f,
                    x2 = 17.0f,
                    y2 = 10.068f,
                    x3 = 17.0f,
                    y3 = 12.0f,
                )
                // s -0.795 3.45 -1.692 4.544
                reflectiveCurveToRelative(
                    dx1 = -0.795f,
                    dy1 = 3.45f,
                    dx2 = -1.692f,
                    dy2 = 4.544f,
                )
                // a 9 9 0 0 0 2.501 -1.133
                arcToRelative(
                    a = 9.0f,
                    b = 9.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.501f,
                    dy1 = -1.133f,
                )
                // C 19.26 14.444 20 13.219 20 12
                curveTo(
                    x1 = 19.26f,
                    y1 = 14.444f,
                    x2 = 20.0f,
                    y2 = 13.219f,
                    x3 = 20.0f,
                    y3 = 12.0f,
                )
                // s -0.74 -2.444 -2.19 -3.41
                reflectiveCurveToRelative(
                    dx1 = -0.74f,
                    dy1 = -2.444f,
                    dx2 = -2.19f,
                    dy2 = -3.41f,
                )
                // a 9 9 0 0 0 -2.502 -1.134
                arcToRelative(
                    a = 9.0f,
                    b = 9.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.502f,
                    dy1 = -1.134f,
                )
            }
        }.build().also { _lantern = it }
    }

@Suppress("ObjectPropertyName")
private var _lantern: ImageVector? = null
