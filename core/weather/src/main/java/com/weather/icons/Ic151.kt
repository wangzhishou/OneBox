package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic151: ImageVector
    get() {
        val current = _ic151
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic151",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.406 16 a4.766 4.766 0 0 0 3.446 -1.449 .323 .323 0 0 1 .341 -.075 c.31 .113 .645 .174 .995 .174 1.553 0 2.812 -1.209 2.812 -2.7 s-1.26 -2.7 -2.813 -2.7 c-.157 0 -.312 .012 -.463 .037 a.32 .32 0 0 1 -.32 -.138 C10.58 7.86 9.098 7 7.406 7 c-1.682 0 -3.157 .85 -3.984 2.128 a.318 .318 0 0 1 -.3 .138 2.962 2.962 0 0 0 -.31 -.016 C1.26 9.25 0 10.459 0 11.95 s1.26 2.7 2.813 2.7 c.302 0 .594 -.046 .867 -.131 a.322 .322 0 0 1 .324 .077 A4.768 4.768 0 0 0 7.406 16Z m3.661 -2.607 c-.161 -.115 -.41 -.087 -.52 .075 a3.788 3.788 0 0 1 -3.14 1.632 3.792 3.792 0 0 1 -3.091 -1.56 c-.106 -.147 -.326 -.178 -.482 -.08 a1.93 1.93 0 0 1 -1.022 .29 c-1.035 0 -1.874 -.806 -1.874 -1.8 s.839 -1.8 1.875 -1.8 c.24 0 .47 .043 .682 .123 .173 .065 .383 -.008 .455 -.172 C4.52 8.806 5.852 7.9 7.406 7.9 c1.575 0 2.924 .932 3.48 2.254 .075 .18 .314 .254 .496 .17 .244 -.111 .517 -.174 .806 -.174 1.035 0 1.874 .806 1.874 1.8 s-.839 1.8 -1.874 1.8 c-.42 0 -.808 -.133 -1.12 -.357Z m4.538 -6.759 a.412 .412 0 0 0 -.109 .015 4.127 4.127 0 0 1 -1.082 .145 4.303 4.303 0 0 1 -1.424 -.248 4.276 4.276 0 0 1 -2.725 -5.086 A.389 .389 0 0 0 9.9 .972 a.374 .374 0 0 0 -.14 .027 A4.772 4.772 0 0 0 6.779 5.72 c.007 .12 .038 .233 .055 .35 a5.29 5.29 0 0 1 .667 -.045 c.113 0 .224 .012 .336 .02 a3.563 3.563 0 0 1 -.06 -.384 3.782 3.782 0 0 1 1.357 -3.138 c.003 .553 .092 1.103 .262 1.629 A5.25 5.25 0 0 0 12.66 7.49 c.563 .2 1.156 .302 1.754 .304 h.047 a3.79 3.79 0 0 1 -.886 .771 c.32 .165 .614 .374 .874 .622 a4.774 4.774 0 0 0 1.525 -2.037 .384 .384 0 0 0 -.37 -.516 h.001Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.406 16
                moveTo(x = 7.406f, y = 16.0f)
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
                // c 1.553 0 2.812 -1.209 2.812 -2.7
                curveToRelative(
                    dx1 = 1.553f,
                    dy1 = 0.0f,
                    dx2 = 2.812f,
                    dy2 = -1.209f,
                    dx3 = 2.812f,
                    dy3 = -2.7f,
                )
                // s -1.26 -2.7 -2.813 -2.7
                reflectiveCurveToRelative(
                    dx1 = -1.26f,
                    dy1 = -2.7f,
                    dx2 = -2.813f,
                    dy2 = -2.7f,
                )
                // c -0.157 0 -0.312 0.012 -0.463 0.037
                curveToRelative(
                    dx1 = -0.157f,
                    dy1 = 0.0f,
                    dx2 = -0.312f,
                    dy2 = 0.012f,
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
                // C 10.58 7.86 9.098 7 7.406 7
                curveTo(
                    x1 = 10.58f,
                    y1 = 7.86f,
                    x2 = 9.098f,
                    y2 = 7.0f,
                    x3 = 7.406f,
                    y3 = 7.0f,
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
                // C 1.26 9.25 0 10.459 0 11.95
                curveTo(
                    x1 = 1.26f,
                    y1 = 9.25f,
                    x2 = 0.0f,
                    y2 = 10.459f,
                    x3 = 0.0f,
                    y3 = 11.95f,
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
                // A 4.768 4.768 0 0 0 7.406 16z
                arcTo(
                    horizontalEllipseRadius = 4.768f,
                    verticalEllipseRadius = 4.768f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.406f,
                    y1 = 16.0f,
                )
                close()
                // m 3.661 -2.607
                moveToRelative(dx = 3.661f, dy = -2.607f)
                // c -0.161 -0.115 -0.41 -0.087 -0.52 0.075
                curveToRelative(
                    dx1 = -0.161f,
                    dy1 = -0.115f,
                    dx2 = -0.41f,
                    dy2 = -0.087f,
                    dx3 = -0.52f,
                    dy3 = 0.075f,
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
                // c 0.24 0 0.47 0.043 0.682 0.123
                curveToRelative(
                    dx1 = 0.24f,
                    dy1 = 0.0f,
                    dx2 = 0.47f,
                    dy2 = 0.043f,
                    dx3 = 0.682f,
                    dy3 = 0.123f,
                )
                // c 0.173 0.065 0.383 -0.008 0.455 -0.172
                curveToRelative(
                    dx1 = 0.173f,
                    dy1 = 0.065f,
                    dx2 = 0.383f,
                    dy2 = -0.008f,
                    dx3 = 0.455f,
                    dy3 = -0.172f,
                )
                // C 4.52 8.806 5.852 7.9 7.406 7.9
                curveTo(
                    x1 = 4.52f,
                    y1 = 8.806f,
                    x2 = 5.852f,
                    y2 = 7.9f,
                    x3 = 7.406f,
                    y3 = 7.9f,
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
                // c -0.42 0 -0.808 -0.133 -1.12 -0.357z
                curveToRelative(
                    dx1 = -0.42f,
                    dy1 = 0.0f,
                    dx2 = -0.808f,
                    dy2 = -0.133f,
                    dx3 = -1.12f,
                    dy3 = -0.357f,
                )
                close()
                // m 4.538 -6.759
                moveToRelative(dx = 4.538f, dy = -6.759f)
                // a 0.412 0.412 0 0 0 -0.109 0.015
                arcToRelative(
                    a = 0.412f,
                    b = 0.412f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.109f,
                    dy1 = 0.015f,
                )
                // a 4.127 4.127 0 0 1 -1.082 0.145
                arcToRelative(
                    a = 4.127f,
                    b = 4.127f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.082f,
                    dy1 = 0.145f,
                )
                // a 4.303 4.303 0 0 1 -1.424 -0.248
                arcToRelative(
                    a = 4.303f,
                    b = 4.303f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.424f,
                    dy1 = -0.248f,
                )
                // a 4.276 4.276 0 0 1 -2.725 -5.086
                arcToRelative(
                    a = 4.276f,
                    b = 4.276f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.725f,
                    dy1 = -5.086f,
                )
                // A 0.389 0.389 0 0 0 9.9 0.972
                arcTo(
                    horizontalEllipseRadius = 0.389f,
                    verticalEllipseRadius = 0.389f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 9.9f,
                    y1 = 0.972f,
                )
                // a 0.374 0.374 0 0 0 -0.14 0.027
                arcToRelative(
                    a = 0.374f,
                    b = 0.374f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.14f,
                    dy1 = 0.027f,
                )
                // A 4.772 4.772 0 0 0 6.779 5.72
                arcTo(
                    horizontalEllipseRadius = 4.772f,
                    verticalEllipseRadius = 4.772f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 6.779f,
                    y1 = 5.72f,
                )
                // c 0.007 0.12 0.038 0.233 0.055 0.35
                curveToRelative(
                    dx1 = 0.007f,
                    dy1 = 0.12f,
                    dx2 = 0.038f,
                    dy2 = 0.233f,
                    dx3 = 0.055f,
                    dy3 = 0.35f,
                )
                // a 5.29 5.29 0 0 1 0.667 -0.045
                arcToRelative(
                    a = 5.29f,
                    b = 5.29f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.667f,
                    dy1 = -0.045f,
                )
                // c 0.113 0 0.224 0.012 0.336 0.02
                curveToRelative(
                    dx1 = 0.113f,
                    dy1 = 0.0f,
                    dx2 = 0.224f,
                    dy2 = 0.012f,
                    dx3 = 0.336f,
                    dy3 = 0.02f,
                )
                // a 3.563 3.563 0 0 1 -0.06 -0.384
                arcToRelative(
                    a = 3.563f,
                    b = 3.563f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.06f,
                    dy1 = -0.384f,
                )
                // a 3.782 3.782 0 0 1 1.357 -3.138
                arcToRelative(
                    a = 3.782f,
                    b = 3.782f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.357f,
                    dy1 = -3.138f,
                )
                // c 0.003 0.553 0.092 1.103 0.262 1.629
                curveToRelative(
                    dx1 = 0.003f,
                    dy1 = 0.553f,
                    dx2 = 0.092f,
                    dy2 = 1.103f,
                    dx3 = 0.262f,
                    dy3 = 1.629f,
                )
                // A 5.25 5.25 0 0 0 12.66 7.49
                arcTo(
                    horizontalEllipseRadius = 5.25f,
                    verticalEllipseRadius = 5.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 12.66f,
                    y1 = 7.49f,
                )
                // c 0.563 0.2 1.156 0.302 1.754 0.304
                curveToRelative(
                    dx1 = 0.563f,
                    dy1 = 0.2f,
                    dx2 = 1.156f,
                    dy2 = 0.302f,
                    dx3 = 1.754f,
                    dy3 = 0.304f,
                )
                // h 0.047
                horizontalLineToRelative(dx = 0.047f)
                // a 3.79 3.79 0 0 1 -0.886 0.771
                arcToRelative(
                    a = 3.79f,
                    b = 3.79f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.886f,
                    dy1 = 0.771f,
                )
                // c 0.32 0.165 0.614 0.374 0.874 0.622
                curveToRelative(
                    dx1 = 0.32f,
                    dy1 = 0.165f,
                    dx2 = 0.614f,
                    dy2 = 0.374f,
                    dx3 = 0.874f,
                    dy3 = 0.622f,
                )
                // a 4.774 4.774 0 0 0 1.525 -2.037
                arcToRelative(
                    a = 4.774f,
                    b = 4.774f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.525f,
                    dy1 = -2.037f,
                )
                // a 0.384 0.384 0 0 0 -0.37 -0.516
                arcToRelative(
                    a = 0.384f,
                    b = 0.384f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.37f,
                    dy1 = -0.516f,
                )
                // h 0.001z
                horizontalLineToRelative(dx = 0.001f)
                close()
            }
        }.build().also { _ic151 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic151: ImageVector? = null
