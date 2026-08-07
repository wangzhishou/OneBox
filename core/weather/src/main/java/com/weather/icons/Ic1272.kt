package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1272: ImageVector
    get() {
        val current = _ic1272
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1272",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M9.085 1.5 v.004 a1 1 0 0 1 .912 1.08 l.007 .003 a1 1 0 0 1 1.395 -1.004 1.5 1.5 0 0 1 2.687 .917 v.004 A1 1 0 0 1 14 4.5 a1 1 0 0 1 -1.944 .331 .996 .996 0 0 1 -1.243 -.105 1.5 1.5 0 0 1 -1.775 .702 c-.118 .248 -.334 .44 -.6 .525 a.747 .747 0 0 1 -.065 .714 .5 .5 0 1 1 -.872 .29 .75 .75 0 0 1 -.154 -1.34 .996 .996 0 0 1 .064 -1.308 1.003 1.003 0 0 1 -.355 -.478 .995 .995 0 0 1 -1.243 -.105 1.5 1.5 0 0 1 -1.775 .702 c-.118 .248 -.334 .44 -.6 .525 a.747 .747 0 0 1 -.065 .714 .5 .5 0 1 1 -.872 .29 .75 .75 0 0 1 -.154 -1.34 A1 1 0 0 1 3 3.01 V3 a1.5 1.5 0 0 1 2.004 -1.413 A1 1 0 0 1 6.399 .583 a1.5 1.5 0 0 1 2.687 .917Z M2 7 h2 v4.91 l3 -.5 V8 h2 v3.076 l7 -1.166 V16 H0 v-3.424 l2 -.333 V7Z m13 4.09 L1 13.424 V15 h14 v-3.91Z M10 14 H9 v-1 h1 v1Z m2 0 h-1 v-1 h1 v1Z m2 0 h-1 v-1 h1 v1Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 9.085 1.5
                moveTo(x = 9.085f, y = 1.5f)
                // v 0.004
                verticalLineToRelative(dy = 0.004f)
                // a 1 1 0 0 1 0.912 1.08
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.912f,
                    dy1 = 1.08f,
                )
                // l 0.007 0.003
                lineToRelative(dx = 0.007f, dy = 0.003f)
                // a 1 1 0 0 1 1.395 -1.004
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.395f,
                    dy1 = -1.004f,
                )
                // a 1.5 1.5 0 0 1 2.687 0.917
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.687f,
                    dy1 = 0.917f,
                )
                // v 0.004
                verticalLineToRelative(dy = 0.004f)
                // A 1 1 0 0 1 14 4.5
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 14.0f,
                    y1 = 4.5f,
                )
                // a 1 1 0 0 1 -1.944 0.331
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.944f,
                    dy1 = 0.331f,
                )
                // a 0.996 0.996 0 0 1 -1.243 -0.105
                arcToRelative(
                    a = 0.996f,
                    b = 0.996f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.243f,
                    dy1 = -0.105f,
                )
                // a 1.5 1.5 0 0 1 -1.775 0.702
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.775f,
                    dy1 = 0.702f,
                )
                // c -0.118 0.248 -0.334 0.44 -0.6 0.525
                curveToRelative(
                    dx1 = -0.118f,
                    dy1 = 0.248f,
                    dx2 = -0.334f,
                    dy2 = 0.44f,
                    dx3 = -0.6f,
                    dy3 = 0.525f,
                )
                // a 0.747 0.747 0 0 1 -0.065 0.714
                arcToRelative(
                    a = 0.747f,
                    b = 0.747f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.065f,
                    dy1 = 0.714f,
                )
                // a 0.5 0.5 0 1 1 -0.872 0.29
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.872f,
                    dy1 = 0.29f,
                )
                // a 0.75 0.75 0 0 1 -0.154 -1.34
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.154f,
                    dy1 = -1.34f,
                )
                // a 0.996 0.996 0 0 1 0.064 -1.308
                arcToRelative(
                    a = 0.996f,
                    b = 0.996f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.064f,
                    dy1 = -1.308f,
                )
                // a 1.003 1.003 0 0 1 -0.355 -0.478
                arcToRelative(
                    a = 1.003f,
                    b = 1.003f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.355f,
                    dy1 = -0.478f,
                )
                // a 0.995 0.995 0 0 1 -1.243 -0.105
                arcToRelative(
                    a = 0.995f,
                    b = 0.995f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.243f,
                    dy1 = -0.105f,
                )
                // a 1.5 1.5 0 0 1 -1.775 0.702
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.775f,
                    dy1 = 0.702f,
                )
                // c -0.118 0.248 -0.334 0.44 -0.6 0.525
                curveToRelative(
                    dx1 = -0.118f,
                    dy1 = 0.248f,
                    dx2 = -0.334f,
                    dy2 = 0.44f,
                    dx3 = -0.6f,
                    dy3 = 0.525f,
                )
                // a 0.747 0.747 0 0 1 -0.065 0.714
                arcToRelative(
                    a = 0.747f,
                    b = 0.747f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.065f,
                    dy1 = 0.714f,
                )
                // a 0.5 0.5 0 1 1 -0.872 0.29
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.872f,
                    dy1 = 0.29f,
                )
                // a 0.75 0.75 0 0 1 -0.154 -1.34
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.154f,
                    dy1 = -1.34f,
                )
                // A 1 1 0 0 1 3 3.01
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 3.0f,
                    y1 = 3.01f,
                )
                // V 3
                verticalLineTo(y = 3.0f)
                // a 1.5 1.5 0 0 1 2.004 -1.413
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.004f,
                    dy1 = -1.413f,
                )
                // A 1 1 0 0 1 6.399 0.583
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 6.399f,
                    y1 = 0.583f,
                )
                // a 1.5 1.5 0 0 1 2.687 0.917z
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.687f,
                    dy1 = 0.917f,
                )
                close()
                // M 2 7
                moveTo(x = 2.0f, y = 7.0f)
                // h 2
                horizontalLineToRelative(dx = 2.0f)
                // v 4.91
                verticalLineToRelative(dy = 4.91f)
                // l 3 -0.5
                lineToRelative(dx = 3.0f, dy = -0.5f)
                // V 8
                verticalLineTo(y = 8.0f)
                // h 2
                horizontalLineToRelative(dx = 2.0f)
                // v 3.076
                verticalLineToRelative(dy = 3.076f)
                // l 7 -1.166
                lineToRelative(dx = 7.0f, dy = -1.166f)
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
        }.build().also { _ic1272 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1272: ImageVector? = null
