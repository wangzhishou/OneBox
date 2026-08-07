package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic301: ImageVector
    get() {
        val current = _ic301
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic301",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M4.293 14.707 A1 1 0 0 1 4 14 c0 -.5 .555 -1.395 1 -2 .445 .605 1 1.5 1 2 a1 1 0 0 1 -1.707 .707Z m7 0 A1 1 0 0 1 11 14 c0 -.5 .555 -1.395 1 -2 .445 .605 1 1.5 1 2 a1 1 0 0 1 -1.707 .707Z M7.5 15 a1 1 0 1 0 2 0 c0 -.5 -.555 -1.395 -1 -2 -.445 .605 -1 1.5 -1 2Z m.906 -2.5 a4.766 4.766 0 0 0 3.446 -1.449 .323 .323 0 0 1 .341 -.075 c.31 .113 .645 .174 .995 .174 C14.74 11.15 16 9.941 16 8.45 s-1.26 -2.7 -2.813 -2.7 c-.157 0 -.312 .013 -.463 .037 a.32 .32 0 0 1 -.32 -.138 C11.58 4.36 10.098 3.5 8.406 3.5 c-1.682 0 -3.157 .85 -3.984 2.128 a.318 .318 0 0 1 -.3 .138 2.962 2.962 0 0 0 -.31 -.016 C2.26 5.75 1 6.959 1 8.45 s1.26 2.7 2.813 2.7 c.302 0 .594 -.046 .867 -.131 a.322 .322 0 0 1 .324 .077 A4.768 4.768 0 0 0 8.406 12.5Z m3.661 -2.606 c-.161 -.116 -.41 -.088 -.52 .074 a3.788 3.788 0 0 1 -3.14 1.632 3.792 3.792 0 0 1 -3.091 -1.56 c-.106 -.147 -.326 -.178 -.482 -.08 -.294 .183 -.645 .29 -1.021 .29 -1.036 0 -1.876 -.806 -1.876 -1.8 s.84 -1.8 1.875 -1.8 c.241 0 .471 .044 .683 .123 .173 .065 .383 -.008 .455 -.173 .569 -1.293 1.902 -2.2 3.456 -2.2 1.575 0 2.924 .932 3.48 2.254 .075 .18 .314 .254 .496 .17 .244 -.111 .517 -.174 .806 -.174 1.035 0 1.874 .806 1.874 1.8 s-.839 1.8 -1.874 1.8 c-.42 0 -.808 -.133 -1.12 -.356Z m-7.088 -7.99 h.007 a.5 .5 0 0 0 .493 -.506 L5.467 .493 a.5 .5 0 0 0 -.5 -.493 H4.96 a.5 .5 0 0 0 -.493 .506 l.012 .904 a.5 .5 0 0 0 .5 .494Z m-2.892 .946 a.5 .5 0 1 0 .698 -.716 l-.648 -.63 a.5 .5 0 1 0 -.697 .715 l.647 .631Z m-.179 2.203 a.5 .5 0 0 0 -.5 -.494 h-.007 l-.904 .012 a.5 .5 0 0 0 .006 1 H.51 l.905 -.012 a.5 .5 0 0 0 .493 -.506Z m5.638 -2.121 a.5 .5 0 0 0 .359 -.15 l.63 -.648 a.5 .5 0 0 0 -.716 -.698 l-.631 .647 a.5 .5 0 0 0 .358 .85 v-.001Z M2.254 5.315 a3.53 3.53 0 0 1 1.018 -.288 1.831 1.831 0 0 1 1.811 -1.603 c.188 .002 .375 .034 .553 .094 a4.927 4.927 0 0 1 1.282 -.404 2.82 2.82 0 0 0 -4.67 2.145 c0 .02 .006 .037 .006 .056Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.293 14.707
                moveTo(x = 4.293f, y = 14.707f)
                // A 1 1 0 0 1 4 14
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 4.0f,
                    y1 = 14.0f,
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
                // m 7 0
                moveToRelative(dx = 7.0f, dy = 0.0f)
                // A 1 1 0 0 1 11 14
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 11.0f,
                    y1 = 14.0f,
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
                // M 7.5 15
                moveTo(x = 7.5f, y = 15.0f)
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
                // m 0.906 -2.5
                moveToRelative(dx = 0.906f, dy = -2.5f)
                // a 4.766 4.766 0 0 0 3.446 -1.449
                arcToRelative(
                    a = 4.766f,
                    b = 4.766f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 3.446f,
                    dy1 = -1.449f,
                )
                // a 0.323 0.323 0 0 1 0.341 -0.075
                arcToRelative(
                    a = 0.323f,
                    b = 0.323f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.341f,
                    dy1 = -0.075f,
                )
                // c 0.31 0.113 0.645 0.174 0.995 0.174
                curveToRelative(
                    dx1 = 0.31f,
                    dy1 = 0.113f,
                    dx2 = 0.645f,
                    dy2 = 0.174f,
                    dx3 = 0.995f,
                    dy3 = 0.174f,
                )
                // C 14.74 11.15 16 9.941 16 8.45
                curveTo(
                    x1 = 14.74f,
                    y1 = 11.15f,
                    x2 = 16.0f,
                    y2 = 9.941f,
                    x3 = 16.0f,
                    y3 = 8.45f,
                )
                // s -1.26 -2.7 -2.813 -2.7
                reflectiveCurveToRelative(
                    dx1 = -1.26f,
                    dy1 = -2.7f,
                    dx2 = -2.813f,
                    dy2 = -2.7f,
                )
                // c -0.157 0 -0.312 0.013 -0.463 0.037
                curveToRelative(
                    dx1 = -0.157f,
                    dy1 = 0.0f,
                    dx2 = -0.312f,
                    dy2 = 0.013f,
                    dx3 = -0.463f,
                    dy3 = 0.037f,
                )
                // a 0.32 0.32 0 0 1 -0.32 -0.138
                arcToRelative(
                    a = 0.32f,
                    b = 0.32f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.32f,
                    dy1 = -0.138f,
                )
                // C 11.58 4.36 10.098 3.5 8.406 3.5
                curveTo(
                    x1 = 11.58f,
                    y1 = 4.36f,
                    x2 = 10.098f,
                    y2 = 3.5f,
                    x3 = 8.406f,
                    y3 = 3.5f,
                )
                // c -1.682 0 -3.157 0.85 -3.984 2.128
                curveToRelative(
                    dx1 = -1.682f,
                    dy1 = 0.0f,
                    dx2 = -3.157f,
                    dy2 = 0.85f,
                    dx3 = -3.984f,
                    dy3 = 2.128f,
                )
                // a 0.318 0.318 0 0 1 -0.3 0.138
                arcToRelative(
                    a = 0.318f,
                    b = 0.318f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.3f,
                    dy1 = 0.138f,
                )
                // a 2.962 2.962 0 0 0 -0.31 -0.016
                arcToRelative(
                    a = 2.962f,
                    b = 2.962f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.31f,
                    dy1 = -0.016f,
                )
                // C 2.26 5.75 1 6.959 1 8.45
                curveTo(
                    x1 = 2.26f,
                    y1 = 5.75f,
                    x2 = 1.0f,
                    y2 = 6.959f,
                    x3 = 1.0f,
                    y3 = 8.45f,
                )
                // s 1.26 2.7 2.813 2.7
                reflectiveCurveToRelative(
                    dx1 = 1.26f,
                    dy1 = 2.7f,
                    dx2 = 2.813f,
                    dy2 = 2.7f,
                )
                // c 0.302 0 0.594 -0.046 0.867 -0.131
                curveToRelative(
                    dx1 = 0.302f,
                    dy1 = 0.0f,
                    dx2 = 0.594f,
                    dy2 = -0.046f,
                    dx3 = 0.867f,
                    dy3 = -0.131f,
                )
                // a 0.322 0.322 0 0 1 0.324 0.077
                arcToRelative(
                    a = 0.322f,
                    b = 0.322f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.324f,
                    dy1 = 0.077f,
                )
                // A 4.768 4.768 0 0 0 8.406 12.5z
                arcTo(
                    horizontalEllipseRadius = 4.768f,
                    verticalEllipseRadius = 4.768f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 8.406f,
                    y1 = 12.5f,
                )
                close()
                // m 3.661 -2.606
                moveToRelative(dx = 3.661f, dy = -2.606f)
                // c -0.161 -0.116 -0.41 -0.088 -0.52 0.074
                curveToRelative(
                    dx1 = -0.161f,
                    dy1 = -0.116f,
                    dx2 = -0.41f,
                    dy2 = -0.088f,
                    dx3 = -0.52f,
                    dy3 = 0.074f,
                )
                // a 3.788 3.788 0 0 1 -3.14 1.632
                arcToRelative(
                    a = 3.788f,
                    b = 3.788f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.14f,
                    dy1 = 1.632f,
                )
                // a 3.792 3.792 0 0 1 -3.091 -1.56
                arcToRelative(
                    a = 3.792f,
                    b = 3.792f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.091f,
                    dy1 = -1.56f,
                )
                // c -0.106 -0.147 -0.326 -0.178 -0.482 -0.08
                curveToRelative(
                    dx1 = -0.106f,
                    dy1 = -0.147f,
                    dx2 = -0.326f,
                    dy2 = -0.178f,
                    dx3 = -0.482f,
                    dy3 = -0.08f,
                )
                // c -0.294 0.183 -0.645 0.29 -1.021 0.29
                curveToRelative(
                    dx1 = -0.294f,
                    dy1 = 0.183f,
                    dx2 = -0.645f,
                    dy2 = 0.29f,
                    dx3 = -1.021f,
                    dy3 = 0.29f,
                )
                // c -1.036 0 -1.876 -0.806 -1.876 -1.8
                curveToRelative(
                    dx1 = -1.036f,
                    dy1 = 0.0f,
                    dx2 = -1.876f,
                    dy2 = -0.806f,
                    dx3 = -1.876f,
                    dy3 = -1.8f,
                )
                // s 0.84 -1.8 1.875 -1.8
                reflectiveCurveToRelative(
                    dx1 = 0.84f,
                    dy1 = -1.8f,
                    dx2 = 1.875f,
                    dy2 = -1.8f,
                )
                // c 0.241 0 0.471 0.044 0.683 0.123
                curveToRelative(
                    dx1 = 0.241f,
                    dy1 = 0.0f,
                    dx2 = 0.471f,
                    dy2 = 0.044f,
                    dx3 = 0.683f,
                    dy3 = 0.123f,
                )
                // c 0.173 0.065 0.383 -0.008 0.455 -0.173
                curveToRelative(
                    dx1 = 0.173f,
                    dy1 = 0.065f,
                    dx2 = 0.383f,
                    dy2 = -0.008f,
                    dx3 = 0.455f,
                    dy3 = -0.173f,
                )
                // c 0.569 -1.293 1.902 -2.2 3.456 -2.2
                curveToRelative(
                    dx1 = 0.569f,
                    dy1 = -1.293f,
                    dx2 = 1.902f,
                    dy2 = -2.2f,
                    dx3 = 3.456f,
                    dy3 = -2.2f,
                )
                // c 1.575 0 2.924 0.932 3.48 2.254
                curveToRelative(
                    dx1 = 1.575f,
                    dy1 = 0.0f,
                    dx2 = 2.924f,
                    dy2 = 0.932f,
                    dx3 = 3.48f,
                    dy3 = 2.254f,
                )
                // c 0.075 0.18 0.314 0.254 0.496 0.17
                curveToRelative(
                    dx1 = 0.075f,
                    dy1 = 0.18f,
                    dx2 = 0.314f,
                    dy2 = 0.254f,
                    dx3 = 0.496f,
                    dy3 = 0.17f,
                )
                // c 0.244 -0.111 0.517 -0.174 0.806 -0.174
                curveToRelative(
                    dx1 = 0.244f,
                    dy1 = -0.111f,
                    dx2 = 0.517f,
                    dy2 = -0.174f,
                    dx3 = 0.806f,
                    dy3 = -0.174f,
                )
                // c 1.035 0 1.874 0.806 1.874 1.8
                curveToRelative(
                    dx1 = 1.035f,
                    dy1 = 0.0f,
                    dx2 = 1.874f,
                    dy2 = 0.806f,
                    dx3 = 1.874f,
                    dy3 = 1.8f,
                )
                // s -0.839 1.8 -1.874 1.8
                reflectiveCurveToRelative(
                    dx1 = -0.839f,
                    dy1 = 1.8f,
                    dx2 = -1.874f,
                    dy2 = 1.8f,
                )
                // c -0.42 0 -0.808 -0.133 -1.12 -0.356z
                curveToRelative(
                    dx1 = -0.42f,
                    dy1 = 0.0f,
                    dx2 = -0.808f,
                    dy2 = -0.133f,
                    dx3 = -1.12f,
                    dy3 = -0.356f,
                )
                close()
                // m -7.088 -7.99
                moveToRelative(dx = -7.088f, dy = -7.99f)
                // h 0.007
                horizontalLineToRelative(dx = 0.007f)
                // a 0.5 0.5 0 0 0 0.493 -0.506
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.493f,
                    dy1 = -0.506f,
                )
                // L 5.467 0.493
                lineTo(x = 5.467f, y = 0.493f)
                // a 0.5 0.5 0 0 0 -0.5 -0.493
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.5f,
                    dy1 = -0.493f,
                )
                // H 4.96
                horizontalLineTo(x = 4.96f)
                // a 0.5 0.5 0 0 0 -0.493 0.506
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.493f,
                    dy1 = 0.506f,
                )
                // l 0.012 0.904
                lineToRelative(dx = 0.012f, dy = 0.904f)
                // a 0.5 0.5 0 0 0 0.5 0.494z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.5f,
                    dy1 = 0.494f,
                )
                close()
                // m -2.892 0.946
                moveToRelative(dx = -2.892f, dy = 0.946f)
                // a 0.5 0.5 0 1 0 0.698 -0.716
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.698f,
                    dy1 = -0.716f,
                )
                // l -0.648 -0.63
                lineToRelative(dx = -0.648f, dy = -0.63f)
                // a 0.5 0.5 0 1 0 -0.697 0.715
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.697f,
                    dy1 = 0.715f,
                )
                // l 0.647 0.631z
                lineToRelative(dx = 0.647f, dy = 0.631f)
                close()
                // m -0.179 2.203
                moveToRelative(dx = -0.179f, dy = 2.203f)
                // a 0.5 0.5 0 0 0 -0.5 -0.494
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.5f,
                    dy1 = -0.494f,
                )
                // h -0.007
                horizontalLineToRelative(dx = -0.007f)
                // l -0.904 0.012
                lineToRelative(dx = -0.904f, dy = 0.012f)
                // a 0.5 0.5 0 0 0 0.006 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.006f,
                    dy1 = 1.0f,
                )
                // H 0.51
                horizontalLineTo(x = 0.51f)
                // l 0.905 -0.012
                lineToRelative(dx = 0.905f, dy = -0.012f)
                // a 0.5 0.5 0 0 0 0.493 -0.506z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.493f,
                    dy1 = -0.506f,
                )
                close()
                // m 5.638 -2.121
                moveToRelative(dx = 5.638f, dy = -2.121f)
                // a 0.5 0.5 0 0 0 0.359 -0.15
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.359f,
                    dy1 = -0.15f,
                )
                // l 0.63 -0.648
                lineToRelative(dx = 0.63f, dy = -0.648f)
                // a 0.5 0.5 0 0 0 -0.716 -0.698
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.716f,
                    dy1 = -0.698f,
                )
                // l -0.631 0.647
                lineToRelative(dx = -0.631f, dy = 0.647f)
                // a 0.5 0.5 0 0 0 0.358 0.85
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.358f,
                    dy1 = 0.85f,
                )
                // v -0.001z
                verticalLineToRelative(dy = -0.001f)
                close()
                // M 2.254 5.315
                moveTo(x = 2.254f, y = 5.315f)
                // a 3.53 3.53 0 0 1 1.018 -0.288
                arcToRelative(
                    a = 3.53f,
                    b = 3.53f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.018f,
                    dy1 = -0.288f,
                )
                // a 1.831 1.831 0 0 1 1.811 -1.603
                arcToRelative(
                    a = 1.831f,
                    b = 1.831f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.811f,
                    dy1 = -1.603f,
                )
                // c 0.188 0.002 0.375 0.034 0.553 0.094
                curveToRelative(
                    dx1 = 0.188f,
                    dy1 = 0.002f,
                    dx2 = 0.375f,
                    dy2 = 0.034f,
                    dx3 = 0.553f,
                    dy3 = 0.094f,
                )
                // a 4.927 4.927 0 0 1 1.282 -0.404
                arcToRelative(
                    a = 4.927f,
                    b = 4.927f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.282f,
                    dy1 = -0.404f,
                )
                // a 2.82 2.82 0 0 0 -4.67 2.145
                arcToRelative(
                    a = 2.82f,
                    b = 2.82f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -4.67f,
                    dy1 = 2.145f,
                )
                // c 0 0.02 0.006 0.037 0.006 0.056z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.02f,
                    dx2 = 0.006f,
                    dy2 = 0.037f,
                    dx3 = 0.006f,
                    dy3 = 0.056f,
                )
                close()
            }
        }.build().also { _ic301 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic301: ImageVector? = null
