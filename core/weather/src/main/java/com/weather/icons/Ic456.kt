package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic456: ImageVector
    get() {
        val current = _ic456
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic456",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M3.57 15 a1 1 0 1 0 2 0 c0 -.5 -.555 -1.395 -1 -2 -.445 .605 -1 1.5 -1 2Z m6.501 -2 a.35 .35 0 0 0 -.35 .35 v.544 l-.47 -.272 a.35 .35 0 1 0 -.35 .606 l.47 .272 -.47 .272 a.35 .35 0 1 0 .35 .606 l.47 -.272 v.544 a.35 .35 0 0 0 .7 0 v-.544 l.47 .272 a.35 .35 0 1 0 .35 -.606 l-.47 -.272 .47 -.272 a.35 .35 0 1 0 -.35 -.606 l-.47 .272 v-.544 a.35 .35 0 0 0 -.35 -.35Z m-2.665 -.5 a4.766 4.766 0 0 0 3.446 -1.449 .323 .323 0 0 1 .341 -.075 c.31 .113 .645 .174 .995 .174 C13.74 11.15 15 9.941 15 8.45 s-1.26 -2.7 -2.813 -2.7 c-.157 0 -.312 .013 -.463 .037 a.32 .32 0 0 1 -.32 -.138 C10.58 4.36 9.098 3.5 7.406 3.5 c-1.682 0 -3.157 .85 -3.984 2.128 a.318 .318 0 0 1 -.3 .138 2.962 2.962 0 0 0 -.31 -.016 C1.26 5.75 0 6.959 0 8.45 s1.26 2.7 2.813 2.7 c.302 0 .594 -.046 .867 -.131 a.322 .322 0 0 1 .324 .077 A4.768 4.768 0 0 0 7.406 12.5Z m3.661 -2.606 c-.161 -.116 -.41 -.088 -.52 .074 a3.788 3.788 0 0 1 -3.14 1.632 3.792 3.792 0 0 1 -3.091 -1.56 c-.106 -.147 -.326 -.178 -.482 -.08 a1.93 1.93 0 0 1 -1.022 .29 c-1.035 0 -1.874 -.806 -1.874 -1.8 s.839 -1.8 1.875 -1.8 c.24 0 .47 .044 .682 .123 .173 .065 .383 -.008 .455 -.173 .569 -1.293 1.902 -2.2 3.456 -2.2 1.575 0 2.924 .932 3.48 2.254 .075 .18 .314 .254 .496 .17 .244 -.111 .517 -.174 .806 -.174 1.035 0 1.874 .806 1.874 1.8 s-.839 1.8 -1.874 1.8 c-.42 0 -.808 -.133 -1.12 -.356Z m4.399 -5.679 a.31 .31 0 0 0 -.08 .01 3.066 3.066 0 0 1 -1.866 -.076 A3.183 3.183 0 0 1 11.492 .364 .29 .29 0 0 0 11.22 0 a.28 .28 0 0 0 -.104 .02 3.546 3.546 0 0 0 -2.21 3.096 c.34 .063 .671 .16 .99 .293 a2.56 2.56 0 0 1 .54 -1.671 4.166 4.166 0 0 0 2.755 3.356 c.274 .096 .558 .164 .846 .203 a2.611 2.611 0 0 1 -.239 .163 c.304 .173 .582 .39 .823 .643 a3.553 3.553 0 0 0 1.12 -1.504 .285 .285 0 0 0 -.275 -.384Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 3.57 15
                moveTo(x = 3.57f, y = 15.0f)
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
                // m 6.501 -2
                moveToRelative(dx = 6.501f, dy = -2.0f)
                // a 0.35 0.35 0 0 0 -0.35 0.35
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = 0.35f,
                )
                // v 0.544
                verticalLineToRelative(dy = 0.544f)
                // l -0.47 -0.272
                lineToRelative(dx = -0.47f, dy = -0.272f)
                // a 0.35 0.35 0 1 0 -0.35 0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = 0.606f,
                )
                // l 0.47 0.272
                lineToRelative(dx = 0.47f, dy = 0.272f)
                // l -0.47 0.272
                lineToRelative(dx = -0.47f, dy = 0.272f)
                // a 0.35 0.35 0 1 0 0.35 0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.35f,
                    dy1 = 0.606f,
                )
                // l 0.47 -0.272
                lineToRelative(dx = 0.47f, dy = -0.272f)
                // v 0.544
                verticalLineToRelative(dy = 0.544f)
                // a 0.35 0.35 0 0 0 0.7 0
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.7f,
                    dy1 = 0.0f,
                )
                // v -0.544
                verticalLineToRelative(dy = -0.544f)
                // l 0.47 0.272
                lineToRelative(dx = 0.47f, dy = 0.272f)
                // a 0.35 0.35 0 1 0 0.35 -0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.35f,
                    dy1 = -0.606f,
                )
                // l -0.47 -0.272
                lineToRelative(dx = -0.47f, dy = -0.272f)
                // l 0.47 -0.272
                lineToRelative(dx = 0.47f, dy = -0.272f)
                // a 0.35 0.35 0 1 0 -0.35 -0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = -0.606f,
                )
                // l -0.47 0.272
                lineToRelative(dx = -0.47f, dy = 0.272f)
                // v -0.544
                verticalLineToRelative(dy = -0.544f)
                // a 0.35 0.35 0 0 0 -0.35 -0.35z
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = -0.35f,
                )
                close()
                // m -2.665 -0.5
                moveToRelative(dx = -2.665f, dy = -0.5f)
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
                // C 13.74 11.15 15 9.941 15 8.45
                curveTo(
                    x1 = 13.74f,
                    y1 = 11.15f,
                    x2 = 15.0f,
                    y2 = 9.941f,
                    x3 = 15.0f,
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
                // C 10.58 4.36 9.098 3.5 7.406 3.5
                curveTo(
                    x1 = 10.58f,
                    y1 = 4.36f,
                    x2 = 9.098f,
                    y2 = 3.5f,
                    x3 = 7.406f,
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
                // C 1.26 5.75 0 6.959 0 8.45
                curveTo(
                    x1 = 1.26f,
                    y1 = 5.75f,
                    x2 = 0.0f,
                    y2 = 6.959f,
                    x3 = 0.0f,
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
                // A 4.768 4.768 0 0 0 7.406 12.5z
                arcTo(
                    horizontalEllipseRadius = 4.768f,
                    verticalEllipseRadius = 4.768f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.406f,
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
                // a 1.93 1.93 0 0 1 -1.022 0.29
                arcToRelative(
                    a = 1.93f,
                    b = 1.93f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.022f,
                    dy1 = 0.29f,
                )
                // c -1.035 0 -1.874 -0.806 -1.874 -1.8
                curveToRelative(
                    dx1 = -1.035f,
                    dy1 = 0.0f,
                    dx2 = -1.874f,
                    dy2 = -0.806f,
                    dx3 = -1.874f,
                    dy3 = -1.8f,
                )
                // s 0.839 -1.8 1.875 -1.8
                reflectiveCurveToRelative(
                    dx1 = 0.839f,
                    dy1 = -1.8f,
                    dx2 = 1.875f,
                    dy2 = -1.8f,
                )
                // c 0.24 0 0.47 0.044 0.682 0.123
                curveToRelative(
                    dx1 = 0.24f,
                    dy1 = 0.0f,
                    dx2 = 0.47f,
                    dy2 = 0.044f,
                    dx3 = 0.682f,
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
                // m 4.399 -5.679
                moveToRelative(dx = 4.399f, dy = -5.679f)
                // a 0.31 0.31 0 0 0 -0.08 0.01
                arcToRelative(
                    a = 0.31f,
                    b = 0.31f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.08f,
                    dy1 = 0.01f,
                )
                // a 3.066 3.066 0 0 1 -1.866 -0.076
                arcToRelative(
                    a = 3.066f,
                    b = 3.066f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.866f,
                    dy1 = -0.076f,
                )
                // A 3.183 3.183 0 0 1 11.492 0.364
                arcTo(
                    horizontalEllipseRadius = 3.183f,
                    verticalEllipseRadius = 3.183f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 11.492f,
                    y1 = 0.364f,
                )
                // A 0.29 0.29 0 0 0 11.22 0
                arcTo(
                    horizontalEllipseRadius = 0.29f,
                    verticalEllipseRadius = 0.29f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 11.22f,
                    y1 = 0.0f,
                )
                // a 0.28 0.28 0 0 0 -0.104 0.02
                arcToRelative(
                    a = 0.28f,
                    b = 0.28f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.104f,
                    dy1 = 0.02f,
                )
                // a 3.546 3.546 0 0 0 -2.21 3.096
                arcToRelative(
                    a = 3.546f,
                    b = 3.546f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.21f,
                    dy1 = 3.096f,
                )
                // c 0.34 0.063 0.671 0.16 0.99 0.293
                curveToRelative(
                    dx1 = 0.34f,
                    dy1 = 0.063f,
                    dx2 = 0.671f,
                    dy2 = 0.16f,
                    dx3 = 0.99f,
                    dy3 = 0.293f,
                )
                // a 2.56 2.56 0 0 1 0.54 -1.671
                arcToRelative(
                    a = 2.56f,
                    b = 2.56f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.54f,
                    dy1 = -1.671f,
                )
                // a 4.166 4.166 0 0 0 2.755 3.356
                arcToRelative(
                    a = 4.166f,
                    b = 4.166f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.755f,
                    dy1 = 3.356f,
                )
                // c 0.274 0.096 0.558 0.164 0.846 0.203
                curveToRelative(
                    dx1 = 0.274f,
                    dy1 = 0.096f,
                    dx2 = 0.558f,
                    dy2 = 0.164f,
                    dx3 = 0.846f,
                    dy3 = 0.203f,
                )
                // a 2.611 2.611 0 0 1 -0.239 0.163
                arcToRelative(
                    a = 2.611f,
                    b = 2.611f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.239f,
                    dy1 = 0.163f,
                )
                // c 0.304 0.173 0.582 0.39 0.823 0.643
                curveToRelative(
                    dx1 = 0.304f,
                    dy1 = 0.173f,
                    dx2 = 0.582f,
                    dy2 = 0.39f,
                    dx3 = 0.823f,
                    dy3 = 0.643f,
                )
                // a 3.553 3.553 0 0 0 1.12 -1.504
                arcToRelative(
                    a = 3.553f,
                    b = 3.553f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.12f,
                    dy1 = -1.504f,
                )
                // a 0.285 0.285 0 0 0 -0.275 -0.384z
                arcToRelative(
                    a = 0.285f,
                    b = 0.285f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.275f,
                    dy1 = -0.384f,
                )
                close()
            }
        }.build().also { _ic456 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic456: ImageVector? = null
