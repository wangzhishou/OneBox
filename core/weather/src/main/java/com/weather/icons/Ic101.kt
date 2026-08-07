package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic101: ImageVector
    get() {
        val current = _ic101
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic101",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.406 15.985 a4.766 4.766 0 0 0 3.446 -1.449 .323 .323 0 0 1 .341 -.074 c.31 .112 .645 .173 .995 .173 1.553 0 2.812 -1.209 2.812 -2.7 s-1.26 -2.7 -2.813 -2.7 c-.157 0 -.312 .013 -.463 .037 a.32 .32 0 0 1 -.32 -.137 c-.824 -1.29 -2.306 -2.15 -3.998 -2.15 -1.682 0 -3.157 .85 -3.984 2.128 a.318 .318 0 0 1 -.3 .138 2.962 2.962 0 0 0 -.31 -.016 C2.26 9.235 1 10.444 1 11.935 s1.26 2.7 2.813 2.7 c.302 0 .594 -.046 .867 -.13 a.322 .322 0 0 1 .324 .076 4.768 4.768 0 0 0 3.402 1.404Z m3.661 -2.606 c-.161 -.116 -.41 -.088 -.52 .074 a3.788 3.788 0 0 1 -3.14 1.632 3.792 3.792 0 0 1 -3.091 -1.56 c-.106 -.147 -.326 -.178 -.482 -.08 -.294 .183 -.645 .29 -1.021 .29 -1.036 0 -1.876 -.806 -1.876 -1.8 s.84 -1.8 1.875 -1.8 c.241 0 .471 .044 .683 .123 .173 .065 .383 -.008 .455 -.172 .569 -1.293 1.902 -2.2 3.456 -2.2 1.575 0 2.924 .931 3.48 2.253 .075 .18 .314 .254 .496 .17 .244 -.111 .517 -.174 .806 -.174 1.035 0 1.874 .806 1.874 1.8 s-.839 1.8 -1.874 1.8 c-.42 0 -.808 -.132 -1.12 -.356Z M4.995 1.762 a.516 .516 0 1 0 1.007 -.224 L5.746 .388 A.516 .516 0 0 0 4.74 .612 l.255 1.15Z M1.273 3.52 l.994 .633 a.516 .516 0 0 0 .555 -.87 l-.995 -.633 a.516 .516 0 0 0 -.554 .87Z M.878 8.028 l1.15 -.256 a.516 .516 0 0 0 -.223 -1.008 l-1.15 .256 a.516 .516 0 1 0 .223 1.008Z m10.238 -2.28 a.535 .535 0 0 0 .112 -.012 l1.15 -.256 a.516 .516 0 1 0 -.224 -1.008 l-1.15 .256 a.516 .516 0 0 0 .112 1.02Z M8.772 2.713 a.516 .516 0 0 0 .712 -.158 l.633 -.994 a.516 .516 0 0 0 -.87 -.554 l-.633 .994 a.516 .516 0 0 0 .158 .712Z M3.07 7.017 c.07 .303 .182 .596 .33 .87 a3.13 3.13 0 0 0 .909 -.486 2.453 2.453 0 0 1 -.233 -.608 2.504 2.504 0 0 1 4.888 -1.088 c.003 .013 .002 .026 .005 .038 a5.42 5.42 0 0 1 1.063 .25 3.497 3.497 0 0 0 -.061 -.512 A3.535 3.535 0 1 0 3.07 7.017Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.406 15.985
                moveTo(x = 8.406f, y = 15.985f)
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
                // a 0.323 0.323 0 0 1 0.341 -0.074
                arcToRelative(
                    a = 0.323f,
                    b = 0.323f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.341f,
                    dy1 = -0.074f,
                )
                // c 0.31 0.112 0.645 0.173 0.995 0.173
                curveToRelative(
                    dx1 = 0.31f,
                    dy1 = 0.112f,
                    dx2 = 0.645f,
                    dy2 = 0.173f,
                    dx3 = 0.995f,
                    dy3 = 0.173f,
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
                // c -0.157 0 -0.312 0.013 -0.463 0.037
                curveToRelative(
                    dx1 = -0.157f,
                    dy1 = 0.0f,
                    dx2 = -0.312f,
                    dy2 = 0.013f,
                    dx3 = -0.463f,
                    dy3 = 0.037f,
                )
                // a 0.32 0.32 0 0 1 -0.32 -0.137
                arcToRelative(
                    a = 0.32f,
                    b = 0.32f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.32f,
                    dy1 = -0.137f,
                )
                // c -0.824 -1.29 -2.306 -2.15 -3.998 -2.15
                curveToRelative(
                    dx1 = -0.824f,
                    dy1 = -1.29f,
                    dx2 = -2.306f,
                    dy2 = -2.15f,
                    dx3 = -3.998f,
                    dy3 = -2.15f,
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
                // C 2.26 9.235 1 10.444 1 11.935
                curveTo(
                    x1 = 2.26f,
                    y1 = 9.235f,
                    x2 = 1.0f,
                    y2 = 10.444f,
                    x3 = 1.0f,
                    y3 = 11.935f,
                )
                // s 1.26 2.7 2.813 2.7
                reflectiveCurveToRelative(
                    dx1 = 1.26f,
                    dy1 = 2.7f,
                    dx2 = 2.813f,
                    dy2 = 2.7f,
                )
                // c 0.302 0 0.594 -0.046 0.867 -0.13
                curveToRelative(
                    dx1 = 0.302f,
                    dy1 = 0.0f,
                    dx2 = 0.594f,
                    dy2 = -0.046f,
                    dx3 = 0.867f,
                    dy3 = -0.13f,
                )
                // a 0.322 0.322 0 0 1 0.324 0.076
                arcToRelative(
                    a = 0.322f,
                    b = 0.322f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.324f,
                    dy1 = 0.076f,
                )
                // a 4.768 4.768 0 0 0 3.402 1.404z
                arcToRelative(
                    a = 4.768f,
                    b = 4.768f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 3.402f,
                    dy1 = 1.404f,
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
                // c 0.173 0.065 0.383 -0.008 0.455 -0.172
                curveToRelative(
                    dx1 = 0.173f,
                    dy1 = 0.065f,
                    dx2 = 0.383f,
                    dy2 = -0.008f,
                    dx3 = 0.455f,
                    dy3 = -0.172f,
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
                // c 1.575 0 2.924 0.931 3.48 2.253
                curveToRelative(
                    dx1 = 1.575f,
                    dy1 = 0.0f,
                    dx2 = 2.924f,
                    dy2 = 0.931f,
                    dx3 = 3.48f,
                    dy3 = 2.253f,
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
                // c -0.42 0 -0.808 -0.132 -1.12 -0.356z
                curveToRelative(
                    dx1 = -0.42f,
                    dy1 = 0.0f,
                    dx2 = -0.808f,
                    dy2 = -0.132f,
                    dx3 = -1.12f,
                    dy3 = -0.356f,
                )
                close()
                // M 4.995 1.762
                moveTo(x = 4.995f, y = 1.762f)
                // a 0.516 0.516 0 1 0 1.007 -0.224
                arcToRelative(
                    a = 0.516f,
                    b = 0.516f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 1.007f,
                    dy1 = -0.224f,
                )
                // L 5.746 0.388
                lineTo(x = 5.746f, y = 0.388f)
                // A 0.516 0.516 0 0 0 4.74 0.612
                arcTo(
                    horizontalEllipseRadius = 0.516f,
                    verticalEllipseRadius = 0.516f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 4.74f,
                    y1 = 0.612f,
                )
                // l 0.255 1.15z
                lineToRelative(dx = 0.255f, dy = 1.15f)
                close()
                // M 1.273 3.52
                moveTo(x = 1.273f, y = 3.52f)
                // l 0.994 0.633
                lineToRelative(dx = 0.994f, dy = 0.633f)
                // a 0.516 0.516 0 0 0 0.555 -0.87
                arcToRelative(
                    a = 0.516f,
                    b = 0.516f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.555f,
                    dy1 = -0.87f,
                )
                // l -0.995 -0.633
                lineToRelative(dx = -0.995f, dy = -0.633f)
                // a 0.516 0.516 0 0 0 -0.554 0.87z
                arcToRelative(
                    a = 0.516f,
                    b = 0.516f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.554f,
                    dy1 = 0.87f,
                )
                close()
                // M 0.878 8.028
                moveTo(x = 0.878f, y = 8.028f)
                // l 1.15 -0.256
                lineToRelative(dx = 1.15f, dy = -0.256f)
                // a 0.516 0.516 0 0 0 -0.223 -1.008
                arcToRelative(
                    a = 0.516f,
                    b = 0.516f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.223f,
                    dy1 = -1.008f,
                )
                // l -1.15 0.256
                lineToRelative(dx = -1.15f, dy = 0.256f)
                // a 0.516 0.516 0 1 0 0.223 1.008z
                arcToRelative(
                    a = 0.516f,
                    b = 0.516f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.223f,
                    dy1 = 1.008f,
                )
                close()
                // m 10.238 -2.28
                moveToRelative(dx = 10.238f, dy = -2.28f)
                // a 0.535 0.535 0 0 0 0.112 -0.012
                arcToRelative(
                    a = 0.535f,
                    b = 0.535f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.112f,
                    dy1 = -0.012f,
                )
                // l 1.15 -0.256
                lineToRelative(dx = 1.15f, dy = -0.256f)
                // a 0.516 0.516 0 1 0 -0.224 -1.008
                arcToRelative(
                    a = 0.516f,
                    b = 0.516f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.224f,
                    dy1 = -1.008f,
                )
                // l -1.15 0.256
                lineToRelative(dx = -1.15f, dy = 0.256f)
                // a 0.516 0.516 0 0 0 0.112 1.02z
                arcToRelative(
                    a = 0.516f,
                    b = 0.516f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.112f,
                    dy1 = 1.02f,
                )
                close()
                // M 8.772 2.713
                moveTo(x = 8.772f, y = 2.713f)
                // a 0.516 0.516 0 0 0 0.712 -0.158
                arcToRelative(
                    a = 0.516f,
                    b = 0.516f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.712f,
                    dy1 = -0.158f,
                )
                // l 0.633 -0.994
                lineToRelative(dx = 0.633f, dy = -0.994f)
                // a 0.516 0.516 0 0 0 -0.87 -0.554
                arcToRelative(
                    a = 0.516f,
                    b = 0.516f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.87f,
                    dy1 = -0.554f,
                )
                // l -0.633 0.994
                lineToRelative(dx = -0.633f, dy = 0.994f)
                // a 0.516 0.516 0 0 0 0.158 0.712z
                arcToRelative(
                    a = 0.516f,
                    b = 0.516f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.158f,
                    dy1 = 0.712f,
                )
                close()
                // M 3.07 7.017
                moveTo(x = 3.07f, y = 7.017f)
                // c 0.07 0.303 0.182 0.596 0.33 0.87
                curveToRelative(
                    dx1 = 0.07f,
                    dy1 = 0.303f,
                    dx2 = 0.182f,
                    dy2 = 0.596f,
                    dx3 = 0.33f,
                    dy3 = 0.87f,
                )
                // a 3.13 3.13 0 0 0 0.909 -0.486
                arcToRelative(
                    a = 3.13f,
                    b = 3.13f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.909f,
                    dy1 = -0.486f,
                )
                // a 2.453 2.453 0 0 1 -0.233 -0.608
                arcToRelative(
                    a = 2.453f,
                    b = 2.453f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.233f,
                    dy1 = -0.608f,
                )
                // a 2.504 2.504 0 0 1 4.888 -1.088
                arcToRelative(
                    a = 2.504f,
                    b = 2.504f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 4.888f,
                    dy1 = -1.088f,
                )
                // c 0.003 0.013 0.002 0.026 0.005 0.038
                curveToRelative(
                    dx1 = 0.003f,
                    dy1 = 0.013f,
                    dx2 = 0.002f,
                    dy2 = 0.026f,
                    dx3 = 0.005f,
                    dy3 = 0.038f,
                )
                // a 5.42 5.42 0 0 1 1.063 0.25
                arcToRelative(
                    a = 5.42f,
                    b = 5.42f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.063f,
                    dy1 = 0.25f,
                )
                // a 3.497 3.497 0 0 0 -0.061 -0.512
                arcToRelative(
                    a = 3.497f,
                    b = 3.497f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.061f,
                    dy1 = -0.512f,
                )
                // A 3.535 3.535 0 1 0 3.07 7.017z
                arcTo(
                    horizontalEllipseRadius = 3.535f,
                    verticalEllipseRadius = 3.535f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    x1 = 3.07f,
                    y1 = 7.017f,
                )
                close()
            }
        }.build().also { _ic101 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic101: ImageVector? = null
