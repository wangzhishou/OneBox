package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2318: ImageVector
    get() {
        val current = _ic2318
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2318",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M6.956 3.479 4.25 0 1.544 3.479 h.974 L1.09 5.381 h1.311 L.5 7.52 h2.765 v1.892 c-.868 -.091 -1.53 -.308 -2.133 -.506 l-.16 -.047 a4.09 4.09 0 0 0 -.302 -.082 L0 8.567 v1.943 C0 13.522 2.607 16 5.814 16 h4.373 C13.393 16 16 13.522 16 10.51 V9.036 l-.514 -.13 -.05 -.012 c-1.138 -.295 -2.46 -.638 -3.614 -.8 l.015 -1.082 H13.5 L12.358 5.73 h.787 l-.856 -1.142 h.584 L11.25 2.5 9.626 4.587 h.585 L9.354 5.73 h.786 L9 7.012 h1.659 V8 h-.01 c-1.084 0 -2.152 .364 -3.186 .716 l-.037 .013 -.04 .013 c-.712 .239 -1.44 .483 -2.183 .615 l.025 -1.836 h2.771 L6.097 5.38 h1.311 L5.982 3.479 h.974Z m-5.927 7.015 v-.6 c.789 .26 1.75 .535 3.104 .535 1.307 0 2.48 -.397 3.618 -.782 l.035 -.011 c.994 -.34 1.955 -.664 2.864 -.664 1.149 0 2.932 .453 4.321 .81 v.712 c0 .071 -.002 .142 -.005 .213 -1.463 -.293 -2.506 -.372 -4.152 -.34 -1.174 .024 -2.072 .26 -2.922 .482 l-.012 .003 c-.852 .223 -1.657 .432 -2.713 .453 -1.639 .033 -2.63 -.047 -4.114 -.354 a4.313 4.313 0 0 1 -.024 -.457Z m.255 1.458 c1.336 .253 2.352 .322 3.902 .29 1.174 -.023 2.072 -.258 2.922 -.48 l.012 -.004 c.852 -.222 1.657 -.432 2.713 -.453 1.59 -.032 2.57 .043 3.985 .328 a4.372 4.372 0 0 1 -.602 1.297 c-1.105 -.18 -2.05 -.228 -3.402 -.201 -1.174 .023 -2.072 .258 -2.922 .48 l-.012 .004 c-.852 .223 -1.657 .432 -2.713 .453 -1.155 .023 -1.988 -.01 -2.899 -.139 a4.471 4.471 0 0 1 -.984 -1.575Z m2.53 2.647 c.42 .014 .868 .014 1.372 .004 1.174 -.023 2.072 -.258 2.922 -.48 l.012 -.004 c.852 -.223 1.657 -.432 2.713 -.453 1.047 -.021 1.83 .004 2.646 .105 a4.921 4.921 0 0 1 -3.292 1.241 H5.814 a5 5 0 0 1 -2 -.413Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6.956 3.479
                moveTo(x = 6.956f, y = 3.479f)
                // L 4.25 0
                lineTo(x = 4.25f, y = 0.0f)
                // L 1.544 3.479
                lineTo(x = 1.544f, y = 3.479f)
                // h 0.974
                horizontalLineToRelative(dx = 0.974f)
                // L 1.09 5.381
                lineTo(x = 1.09f, y = 5.381f)
                // h 1.311
                horizontalLineToRelative(dx = 1.311f)
                // L 0.5 7.52
                lineTo(x = 0.5f, y = 7.52f)
                // h 2.765
                horizontalLineToRelative(dx = 2.765f)
                // v 1.892
                verticalLineToRelative(dy = 1.892f)
                // c -0.868 -0.091 -1.53 -0.308 -2.133 -0.506
                curveToRelative(
                    dx1 = -0.868f,
                    dy1 = -0.091f,
                    dx2 = -1.53f,
                    dy2 = -0.308f,
                    dx3 = -2.133f,
                    dy3 = -0.506f,
                )
                // l -0.16 -0.047
                lineToRelative(dx = -0.16f, dy = -0.047f)
                // a 4.09 4.09 0 0 0 -0.302 -0.082
                arcToRelative(
                    a = 4.09f,
                    b = 4.09f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.302f,
                    dy1 = -0.082f,
                )
                // L 0 8.567
                lineTo(x = 0.0f, y = 8.567f)
                // v 1.943
                verticalLineToRelative(dy = 1.943f)
                // C 0 13.522 2.607 16 5.814 16
                curveTo(
                    x1 = 0.0f,
                    y1 = 13.522f,
                    x2 = 2.607f,
                    y2 = 16.0f,
                    x3 = 5.814f,
                    y3 = 16.0f,
                )
                // h 4.373
                horizontalLineToRelative(dx = 4.373f)
                // C 13.393 16 16 13.522 16 10.51
                curveTo(
                    x1 = 13.393f,
                    y1 = 16.0f,
                    x2 = 16.0f,
                    y2 = 13.522f,
                    x3 = 16.0f,
                    y3 = 10.51f,
                )
                // V 9.036
                verticalLineTo(y = 9.036f)
                // l -0.514 -0.13
                lineToRelative(dx = -0.514f, dy = -0.13f)
                // l -0.05 -0.012
                lineToRelative(dx = -0.05f, dy = -0.012f)
                // c -1.138 -0.295 -2.46 -0.638 -3.614 -0.8
                curveToRelative(
                    dx1 = -1.138f,
                    dy1 = -0.295f,
                    dx2 = -2.46f,
                    dy2 = -0.638f,
                    dx3 = -3.614f,
                    dy3 = -0.8f,
                )
                // l 0.015 -1.082
                lineToRelative(dx = 0.015f, dy = -1.082f)
                // H 13.5
                horizontalLineTo(x = 13.5f)
                // L 12.358 5.73
                lineTo(x = 12.358f, y = 5.73f)
                // h 0.787
                horizontalLineToRelative(dx = 0.787f)
                // l -0.856 -1.142
                lineToRelative(dx = -0.856f, dy = -1.142f)
                // h 0.584
                horizontalLineToRelative(dx = 0.584f)
                // L 11.25 2.5
                lineTo(x = 11.25f, y = 2.5f)
                // L 9.626 4.587
                lineTo(x = 9.626f, y = 4.587f)
                // h 0.585
                horizontalLineToRelative(dx = 0.585f)
                // L 9.354 5.73
                lineTo(x = 9.354f, y = 5.73f)
                // h 0.786
                horizontalLineToRelative(dx = 0.786f)
                // L 9 7.012
                lineTo(x = 9.0f, y = 7.012f)
                // h 1.659
                horizontalLineToRelative(dx = 1.659f)
                // V 8
                verticalLineTo(y = 8.0f)
                // h -0.01
                horizontalLineToRelative(dx = -0.01f)
                // c -1.084 0 -2.152 0.364 -3.186 0.716
                curveToRelative(
                    dx1 = -1.084f,
                    dy1 = 0.0f,
                    dx2 = -2.152f,
                    dy2 = 0.364f,
                    dx3 = -3.186f,
                    dy3 = 0.716f,
                )
                // l -0.037 0.013
                lineToRelative(dx = -0.037f, dy = 0.013f)
                // l -0.04 0.013
                lineToRelative(dx = -0.04f, dy = 0.013f)
                // c -0.712 0.239 -1.44 0.483 -2.183 0.615
                curveToRelative(
                    dx1 = -0.712f,
                    dy1 = 0.239f,
                    dx2 = -1.44f,
                    dy2 = 0.483f,
                    dx3 = -2.183f,
                    dy3 = 0.615f,
                )
                // l 0.025 -1.836
                lineToRelative(dx = 0.025f, dy = -1.836f)
                // h 2.771
                horizontalLineToRelative(dx = 2.771f)
                // L 6.097 5.38
                lineTo(x = 6.097f, y = 5.38f)
                // h 1.311
                horizontalLineToRelative(dx = 1.311f)
                // L 5.982 3.479
                lineTo(x = 5.982f, y = 3.479f)
                // h 0.974z
                horizontalLineToRelative(dx = 0.974f)
                close()
                // m -5.927 7.015
                moveToRelative(dx = -5.927f, dy = 7.015f)
                // v -0.6
                verticalLineToRelative(dy = -0.6f)
                // c 0.789 0.26 1.75 0.535 3.104 0.535
                curveToRelative(
                    dx1 = 0.789f,
                    dy1 = 0.26f,
                    dx2 = 1.75f,
                    dy2 = 0.535f,
                    dx3 = 3.104f,
                    dy3 = 0.535f,
                )
                // c 1.307 0 2.48 -0.397 3.618 -0.782
                curveToRelative(
                    dx1 = 1.307f,
                    dy1 = 0.0f,
                    dx2 = 2.48f,
                    dy2 = -0.397f,
                    dx3 = 3.618f,
                    dy3 = -0.782f,
                )
                // l 0.035 -0.011
                lineToRelative(dx = 0.035f, dy = -0.011f)
                // c 0.994 -0.34 1.955 -0.664 2.864 -0.664
                curveToRelative(
                    dx1 = 0.994f,
                    dy1 = -0.34f,
                    dx2 = 1.955f,
                    dy2 = -0.664f,
                    dx3 = 2.864f,
                    dy3 = -0.664f,
                )
                // c 1.149 0 2.932 0.453 4.321 0.81
                curveToRelative(
                    dx1 = 1.149f,
                    dy1 = 0.0f,
                    dx2 = 2.932f,
                    dy2 = 0.453f,
                    dx3 = 4.321f,
                    dy3 = 0.81f,
                )
                // v 0.712
                verticalLineToRelative(dy = 0.712f)
                // c 0 0.071 -0.002 0.142 -0.005 0.213
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.071f,
                    dx2 = -0.002f,
                    dy2 = 0.142f,
                    dx3 = -0.005f,
                    dy3 = 0.213f,
                )
                // c -1.463 -0.293 -2.506 -0.372 -4.152 -0.34
                curveToRelative(
                    dx1 = -1.463f,
                    dy1 = -0.293f,
                    dx2 = -2.506f,
                    dy2 = -0.372f,
                    dx3 = -4.152f,
                    dy3 = -0.34f,
                )
                // c -1.174 0.024 -2.072 0.26 -2.922 0.482
                curveToRelative(
                    dx1 = -1.174f,
                    dy1 = 0.024f,
                    dx2 = -2.072f,
                    dy2 = 0.26f,
                    dx3 = -2.922f,
                    dy3 = 0.482f,
                )
                // l -0.012 0.003
                lineToRelative(dx = -0.012f, dy = 0.003f)
                // c -0.852 0.223 -1.657 0.432 -2.713 0.453
                curveToRelative(
                    dx1 = -0.852f,
                    dy1 = 0.223f,
                    dx2 = -1.657f,
                    dy2 = 0.432f,
                    dx3 = -2.713f,
                    dy3 = 0.453f,
                )
                // c -1.639 0.033 -2.63 -0.047 -4.114 -0.354
                curveToRelative(
                    dx1 = -1.639f,
                    dy1 = 0.033f,
                    dx2 = -2.63f,
                    dy2 = -0.047f,
                    dx3 = -4.114f,
                    dy3 = -0.354f,
                )
                // a 4.313 4.313 0 0 1 -0.024 -0.457z
                arcToRelative(
                    a = 4.313f,
                    b = 4.313f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.024f,
                    dy1 = -0.457f,
                )
                close()
                // m 0.255 1.458
                moveToRelative(dx = 0.255f, dy = 1.458f)
                // c 1.336 0.253 2.352 0.322 3.902 0.29
                curveToRelative(
                    dx1 = 1.336f,
                    dy1 = 0.253f,
                    dx2 = 2.352f,
                    dy2 = 0.322f,
                    dx3 = 3.902f,
                    dy3 = 0.29f,
                )
                // c 1.174 -0.023 2.072 -0.258 2.922 -0.48
                curveToRelative(
                    dx1 = 1.174f,
                    dy1 = -0.023f,
                    dx2 = 2.072f,
                    dy2 = -0.258f,
                    dx3 = 2.922f,
                    dy3 = -0.48f,
                )
                // l 0.012 -0.004
                lineToRelative(dx = 0.012f, dy = -0.004f)
                // c 0.852 -0.222 1.657 -0.432 2.713 -0.453
                curveToRelative(
                    dx1 = 0.852f,
                    dy1 = -0.222f,
                    dx2 = 1.657f,
                    dy2 = -0.432f,
                    dx3 = 2.713f,
                    dy3 = -0.453f,
                )
                // c 1.59 -0.032 2.57 0.043 3.985 0.328
                curveToRelative(
                    dx1 = 1.59f,
                    dy1 = -0.032f,
                    dx2 = 2.57f,
                    dy2 = 0.043f,
                    dx3 = 3.985f,
                    dy3 = 0.328f,
                )
                // a 4.372 4.372 0 0 1 -0.602 1.297
                arcToRelative(
                    a = 4.372f,
                    b = 4.372f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.602f,
                    dy1 = 1.297f,
                )
                // c -1.105 -0.18 -2.05 -0.228 -3.402 -0.201
                curveToRelative(
                    dx1 = -1.105f,
                    dy1 = -0.18f,
                    dx2 = -2.05f,
                    dy2 = -0.228f,
                    dx3 = -3.402f,
                    dy3 = -0.201f,
                )
                // c -1.174 0.023 -2.072 0.258 -2.922 0.48
                curveToRelative(
                    dx1 = -1.174f,
                    dy1 = 0.023f,
                    dx2 = -2.072f,
                    dy2 = 0.258f,
                    dx3 = -2.922f,
                    dy3 = 0.48f,
                )
                // l -0.012 0.004
                lineToRelative(dx = -0.012f, dy = 0.004f)
                // c -0.852 0.223 -1.657 0.432 -2.713 0.453
                curveToRelative(
                    dx1 = -0.852f,
                    dy1 = 0.223f,
                    dx2 = -1.657f,
                    dy2 = 0.432f,
                    dx3 = -2.713f,
                    dy3 = 0.453f,
                )
                // c -1.155 0.023 -1.988 -0.01 -2.899 -0.139
                curveToRelative(
                    dx1 = -1.155f,
                    dy1 = 0.023f,
                    dx2 = -1.988f,
                    dy2 = -0.01f,
                    dx3 = -2.899f,
                    dy3 = -0.139f,
                )
                // a 4.471 4.471 0 0 1 -0.984 -1.575z
                arcToRelative(
                    a = 4.471f,
                    b = 4.471f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.984f,
                    dy1 = -1.575f,
                )
                close()
                // m 2.53 2.647
                moveToRelative(dx = 2.53f, dy = 2.647f)
                // c 0.42 0.014 0.868 0.014 1.372 0.004
                curveToRelative(
                    dx1 = 0.42f,
                    dy1 = 0.014f,
                    dx2 = 0.868f,
                    dy2 = 0.014f,
                    dx3 = 1.372f,
                    dy3 = 0.004f,
                )
                // c 1.174 -0.023 2.072 -0.258 2.922 -0.48
                curveToRelative(
                    dx1 = 1.174f,
                    dy1 = -0.023f,
                    dx2 = 2.072f,
                    dy2 = -0.258f,
                    dx3 = 2.922f,
                    dy3 = -0.48f,
                )
                // l 0.012 -0.004
                lineToRelative(dx = 0.012f, dy = -0.004f)
                // c 0.852 -0.223 1.657 -0.432 2.713 -0.453
                curveToRelative(
                    dx1 = 0.852f,
                    dy1 = -0.223f,
                    dx2 = 1.657f,
                    dy2 = -0.432f,
                    dx3 = 2.713f,
                    dy3 = -0.453f,
                )
                // c 1.047 -0.021 1.83 0.004 2.646 0.105
                curveToRelative(
                    dx1 = 1.047f,
                    dy1 = -0.021f,
                    dx2 = 1.83f,
                    dy2 = 0.004f,
                    dx3 = 2.646f,
                    dy3 = 0.105f,
                )
                // a 4.921 4.921 0 0 1 -3.292 1.241
                arcToRelative(
                    a = 4.921f,
                    b = 4.921f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.292f,
                    dy1 = 1.241f,
                )
                // H 5.814
                horizontalLineTo(x = 5.814f)
                // a 5 5 0 0 1 -2 -0.413z
                arcToRelative(
                    a = 5.0f,
                    b = 5.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.0f,
                    dy1 = -0.413f,
                )
                close()
            }
        }.build().also { _ic2318 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2318: ImageVector? = null
