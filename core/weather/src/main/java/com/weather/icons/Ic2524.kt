package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2524: ImageVector
    get() {
        val current = _ic2524
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2524",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // m5.558 11.495 .373 .05 c.882 .124 1.845 .33 2.75 .507 1.057 .207 2.035 .375 2.82 .375 a16.7 16.7 0 0 0 2.664 -.243 19.804 19.804 0 0 0 1.132 -.223 l.063 -.014 .02 -.005 .1 -.015 a.5 .5 0 0 1 .236 .951 l-.095 .034 h-.003 l-.005 .002 c-.004 .002 -.01 .002 -.019 .004 l-.07 .018 -.264 .059 c-.226 .048 -.547 .111 -.93 .175 a17.69 17.69 0 0 1 -2.83 .257 c-.895 0 -1.964 -.189 -3.01 -.393 -.935 -.183 -1.856 -.38 -2.694 -.498 l-.353 -.046 -.1 -.023 a.5 .5 0 0 1 .114 -.973 l.1 .002Z m-2.608 .016 a.5 .5 0 0 1 .101 .995 20.057 20.057 0 0 0 -2.246 .364 l-.139 .032 -.035 .008 a.496 .496 0 0 0 -.008 .002 H.621 a.5 .5 0 0 1 -.242 -.97 h.002 l.003 -.001 .01 -.003 .042 -.01 .15 -.034 a21.118 21.118 0 0 1 2.363 -.383Z m12.53 -2.478 a.5 .5 0 0 1 .236 .95 l-.095 .035 h-.002 l-.003 .001 c-.004 0 -.009 .002 -.014 .004 l-.05 .012 -.186 .042 a20.204 20.204 0 0 1 -2.83 .417 .5 .5 0 0 1 -.072 -.998 19.199 19.199 0 0 0 2.858 -.435 l.044 -.01 .011 -.003 h.002 l.1 -.015Z m-10.98 -.5 c.82 0 1.784 .158 2.74 .34 .974 .187 1.94 .399 2.835 .533 a.5 .5 0 1 1 -.149 .989 c-.92 -.138 -1.937 -.36 -2.874 -.54 -.956 -.182 -1.836 -.322 -2.552 -.322 -.957 0 -1.928 .12 -2.665 .243 a19.92 19.92 0 0 0 -1.194 .237 l-.02 .005 a.5 .5 0 0 1 -.242 -.97 h.003 l.005 -.003 .02 -.005 a9.387 9.387 0 0 1 .333 -.075 c.226 -.048 .547 -.112 .931 -.176 .766 -.127 1.796 -.256 2.83 -.256Z m10.98 -2.5 a.5 .5 0 0 1 .236 .95 l-.095 .035 h-.003 l-.005 .002 -.019 .005 a10.6 10.6 0 0 1 -.334 .075 c-.226 .048 -.547 .112 -.93 .176 -.766 .127 -1.796 .257 -2.83 .257 -.792 0 -1.725 -.149 -2.663 -.327 l-.934 -.183 -.097 -.03 a.5 .5 0 0 1 .191 -.96 l.1 .01 .936 .183 c.916 .174 1.766 .307 2.467 .307 .958 0 1.929 -.121 2.665 -.244 a19.941 19.941 0 0 0 1.195 -.237 l.02 -.005 .1 -.014Z m-10.98 -.5 c.336 0 .692 .026 1.058 .069 a.5 .5 0 1 1 -.115 .993 8.26 8.26 0 0 0 -.943 -.062 c-.957 0 -1.928 .12 -2.665 .243 a19.92 19.92 0 0 0 -1.194 .237 l-.02 .005 a.5 .5 0 0 1 -.242 -.97 h.003 l.005 -.003 .02 -.005 a9.387 9.387 0 0 1 .333 -.075 c.226 -.048 .547 -.112 .931 -.176 .766 -.127 1.796 -.256 2.83 -.256Z m1.058 -2.932 .373 .049 c.882 .125 1.845 .33 2.75 .508 1.057 .206 2.035 .375 2.82 .375 .957 0 1.928 -.121 2.664 -.244 a19.941 19.941 0 0 0 1.195 -.237 l.02 -.005 .1 -.014 a.5 .5 0 0 1 .236 .95 l-.095 .035 h-.003 l-.005 .002 -.019 .004 -.07 .017 a21.087 21.087 0 0 1 -1.194 .234 c-.766 .128 -1.796 .258 -2.83 .258 -.895 0 -1.964 -.19 -3.01 -.394 -.935 -.183 -1.856 -.38 -2.694 -.498 l-.353 -.046 -.1 -.022 a.5 .5 0 0 1 .114 -.973 l.1 .001Z m-2.608 .016 a.5 .5 0 0 1 .101 .995 20.102 20.102 0 0 0 -2.246 .363 l-.139 .032 -.035 .008 -.008 .002 -.002 .001 a.5 .5 0 0 1 -.242 -.97 h.002 l.003 -.002 .01 -.003 .042 -.01 L.586 3 a21.09 21.09 0 0 1 2.363 -.383Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 5.558 11.495
                moveTo(x = 5.558f, y = 11.495f)
                // l 0.373 0.05
                lineToRelative(dx = 0.373f, dy = 0.05f)
                // c 0.882 0.124 1.845 0.33 2.75 0.507
                curveToRelative(
                    dx1 = 0.882f,
                    dy1 = 0.124f,
                    dx2 = 1.845f,
                    dy2 = 0.33f,
                    dx3 = 2.75f,
                    dy3 = 0.507f,
                )
                // c 1.057 0.207 2.035 0.375 2.82 0.375
                curveToRelative(
                    dx1 = 1.057f,
                    dy1 = 0.207f,
                    dx2 = 2.035f,
                    dy2 = 0.375f,
                    dx3 = 2.82f,
                    dy3 = 0.375f,
                )
                // a 16.7 16.7 0 0 0 2.664 -0.243
                arcToRelative(
                    a = 16.7f,
                    b = 16.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.664f,
                    dy1 = -0.243f,
                )
                // a 19.804 19.804 0 0 0 1.132 -0.223
                arcToRelative(
                    a = 19.804f,
                    b = 19.804f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.132f,
                    dy1 = -0.223f,
                )
                // l 0.063 -0.014
                lineToRelative(dx = 0.063f, dy = -0.014f)
                // l 0.02 -0.005
                lineToRelative(dx = 0.02f, dy = -0.005f)
                // l 0.1 -0.015
                lineToRelative(dx = 0.1f, dy = -0.015f)
                // a 0.5 0.5 0 0 1 0.236 0.951
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.236f,
                    dy1 = 0.951f,
                )
                // l -0.095 0.034
                lineToRelative(dx = -0.095f, dy = 0.034f)
                // h -0.003
                horizontalLineToRelative(dx = -0.003f)
                // l -0.005 0.002
                lineToRelative(dx = -0.005f, dy = 0.002f)
                // c -0.004 0.002 -0.01 0.002 -0.019 0.004
                curveToRelative(
                    dx1 = -0.004f,
                    dy1 = 0.002f,
                    dx2 = -0.01f,
                    dy2 = 0.002f,
                    dx3 = -0.019f,
                    dy3 = 0.004f,
                )
                // l -0.07 0.018
                lineToRelative(dx = -0.07f, dy = 0.018f)
                // l -0.264 0.059
                lineToRelative(dx = -0.264f, dy = 0.059f)
                // c -0.226 0.048 -0.547 0.111 -0.93 0.175
                curveToRelative(
                    dx1 = -0.226f,
                    dy1 = 0.048f,
                    dx2 = -0.547f,
                    dy2 = 0.111f,
                    dx3 = -0.93f,
                    dy3 = 0.175f,
                )
                // a 17.69 17.69 0 0 1 -2.83 0.257
                arcToRelative(
                    a = 17.69f,
                    b = 17.69f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.83f,
                    dy1 = 0.257f,
                )
                // c -0.895 0 -1.964 -0.189 -3.01 -0.393
                curveToRelative(
                    dx1 = -0.895f,
                    dy1 = 0.0f,
                    dx2 = -1.964f,
                    dy2 = -0.189f,
                    dx3 = -3.01f,
                    dy3 = -0.393f,
                )
                // c -0.935 -0.183 -1.856 -0.38 -2.694 -0.498
                curveToRelative(
                    dx1 = -0.935f,
                    dy1 = -0.183f,
                    dx2 = -1.856f,
                    dy2 = -0.38f,
                    dx3 = -2.694f,
                    dy3 = -0.498f,
                )
                // l -0.353 -0.046
                lineToRelative(dx = -0.353f, dy = -0.046f)
                // l -0.1 -0.023
                lineToRelative(dx = -0.1f, dy = -0.023f)
                // a 0.5 0.5 0 0 1 0.114 -0.973
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.114f,
                    dy1 = -0.973f,
                )
                // l 0.1 0.002z
                lineToRelative(dx = 0.1f, dy = 0.002f)
                close()
                // m -2.608 0.016
                moveToRelative(dx = -2.608f, dy = 0.016f)
                // a 0.5 0.5 0 0 1 0.101 0.995
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.101f,
                    dy1 = 0.995f,
                )
                // a 20.057 20.057 0 0 0 -2.246 0.364
                arcToRelative(
                    a = 20.057f,
                    b = 20.057f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.246f,
                    dy1 = 0.364f,
                )
                // l -0.139 0.032
                lineToRelative(dx = -0.139f, dy = 0.032f)
                // l -0.035 0.008
                lineToRelative(dx = -0.035f, dy = 0.008f)
                // a 0.496 0.496 0 0 0 -0.008 0.002
                arcToRelative(
                    a = 0.496f,
                    b = 0.496f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.008f,
                    dy1 = 0.002f,
                )
                // H 0.621
                horizontalLineTo(x = 0.621f)
                // a 0.5 0.5 0 0 1 -0.242 -0.97
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.242f,
                    dy1 = -0.97f,
                )
                // h 0.002
                horizontalLineToRelative(dx = 0.002f)
                // l 0.003 -0.001
                lineToRelative(dx = 0.003f, dy = -0.001f)
                // l 0.01 -0.003
                lineToRelative(dx = 0.01f, dy = -0.003f)
                // l 0.042 -0.01
                lineToRelative(dx = 0.042f, dy = -0.01f)
                // l 0.15 -0.034
                lineToRelative(dx = 0.15f, dy = -0.034f)
                // a 21.118 21.118 0 0 1 2.363 -0.383z
                arcToRelative(
                    a = 21.118f,
                    b = 21.118f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.363f,
                    dy1 = -0.383f,
                )
                close()
                // m 12.53 -2.478
                moveToRelative(dx = 12.53f, dy = -2.478f)
                // a 0.5 0.5 0 0 1 0.236 0.95
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.236f,
                    dy1 = 0.95f,
                )
                // l -0.095 0.035
                lineToRelative(dx = -0.095f, dy = 0.035f)
                // h -0.002
                horizontalLineToRelative(dx = -0.002f)
                // l -0.003 0.001
                lineToRelative(dx = -0.003f, dy = 0.001f)
                // c -0.004 0 -0.009 0.002 -0.014 0.004
                curveToRelative(
                    dx1 = -0.004f,
                    dy1 = 0.0f,
                    dx2 = -0.009f,
                    dy2 = 0.002f,
                    dx3 = -0.014f,
                    dy3 = 0.004f,
                )
                // l -0.05 0.012
                lineToRelative(dx = -0.05f, dy = 0.012f)
                // l -0.186 0.042
                lineToRelative(dx = -0.186f, dy = 0.042f)
                // a 20.204 20.204 0 0 1 -2.83 0.417
                arcToRelative(
                    a = 20.204f,
                    b = 20.204f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.83f,
                    dy1 = 0.417f,
                )
                // a 0.5 0.5 0 0 1 -0.072 -0.998
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.072f,
                    dy1 = -0.998f,
                )
                // a 19.199 19.199 0 0 0 2.858 -0.435
                arcToRelative(
                    a = 19.199f,
                    b = 19.199f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.858f,
                    dy1 = -0.435f,
                )
                // l 0.044 -0.01
                lineToRelative(dx = 0.044f, dy = -0.01f)
                // l 0.011 -0.003
                lineToRelative(dx = 0.011f, dy = -0.003f)
                // h 0.002
                horizontalLineToRelative(dx = 0.002f)
                // l 0.1 -0.015z
                lineToRelative(dx = 0.1f, dy = -0.015f)
                close()
                // m -10.98 -0.5
                moveToRelative(dx = -10.98f, dy = -0.5f)
                // c 0.82 0 1.784 0.158 2.74 0.34
                curveToRelative(
                    dx1 = 0.82f,
                    dy1 = 0.0f,
                    dx2 = 1.784f,
                    dy2 = 0.158f,
                    dx3 = 2.74f,
                    dy3 = 0.34f,
                )
                // c 0.974 0.187 1.94 0.399 2.835 0.533
                curveToRelative(
                    dx1 = 0.974f,
                    dy1 = 0.187f,
                    dx2 = 1.94f,
                    dy2 = 0.399f,
                    dx3 = 2.835f,
                    dy3 = 0.533f,
                )
                // a 0.5 0.5 0 1 1 -0.149 0.989
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.149f,
                    dy1 = 0.989f,
                )
                // c -0.92 -0.138 -1.937 -0.36 -2.874 -0.54
                curveToRelative(
                    dx1 = -0.92f,
                    dy1 = -0.138f,
                    dx2 = -1.937f,
                    dy2 = -0.36f,
                    dx3 = -2.874f,
                    dy3 = -0.54f,
                )
                // c -0.956 -0.182 -1.836 -0.322 -2.552 -0.322
                curveToRelative(
                    dx1 = -0.956f,
                    dy1 = -0.182f,
                    dx2 = -1.836f,
                    dy2 = -0.322f,
                    dx3 = -2.552f,
                    dy3 = -0.322f,
                )
                // c -0.957 0 -1.928 0.12 -2.665 0.243
                curveToRelative(
                    dx1 = -0.957f,
                    dy1 = 0.0f,
                    dx2 = -1.928f,
                    dy2 = 0.12f,
                    dx3 = -2.665f,
                    dy3 = 0.243f,
                )
                // a 19.92 19.92 0 0 0 -1.194 0.237
                arcToRelative(
                    a = 19.92f,
                    b = 19.92f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.194f,
                    dy1 = 0.237f,
                )
                // l -0.02 0.005
                lineToRelative(dx = -0.02f, dy = 0.005f)
                // a 0.5 0.5 0 0 1 -0.242 -0.97
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.242f,
                    dy1 = -0.97f,
                )
                // h 0.003
                horizontalLineToRelative(dx = 0.003f)
                // l 0.005 -0.003
                lineToRelative(dx = 0.005f, dy = -0.003f)
                // l 0.02 -0.005
                lineToRelative(dx = 0.02f, dy = -0.005f)
                // a 9.387 9.387 0 0 1 0.333 -0.075
                arcToRelative(
                    a = 9.387f,
                    b = 9.387f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.333f,
                    dy1 = -0.075f,
                )
                // c 0.226 -0.048 0.547 -0.112 0.931 -0.176
                curveToRelative(
                    dx1 = 0.226f,
                    dy1 = -0.048f,
                    dx2 = 0.547f,
                    dy2 = -0.112f,
                    dx3 = 0.931f,
                    dy3 = -0.176f,
                )
                // c 0.766 -0.127 1.796 -0.256 2.83 -0.256z
                curveToRelative(
                    dx1 = 0.766f,
                    dy1 = -0.127f,
                    dx2 = 1.796f,
                    dy2 = -0.256f,
                    dx3 = 2.83f,
                    dy3 = -0.256f,
                )
                close()
                // m 10.98 -2.5
                moveToRelative(dx = 10.98f, dy = -2.5f)
                // a 0.5 0.5 0 0 1 0.236 0.95
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.236f,
                    dy1 = 0.95f,
                )
                // l -0.095 0.035
                lineToRelative(dx = -0.095f, dy = 0.035f)
                // h -0.003
                horizontalLineToRelative(dx = -0.003f)
                // l -0.005 0.002
                lineToRelative(dx = -0.005f, dy = 0.002f)
                // l -0.019 0.005
                lineToRelative(dx = -0.019f, dy = 0.005f)
                // a 10.6 10.6 0 0 1 -0.334 0.075
                arcToRelative(
                    a = 10.6f,
                    b = 10.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.334f,
                    dy1 = 0.075f,
                )
                // c -0.226 0.048 -0.547 0.112 -0.93 0.176
                curveToRelative(
                    dx1 = -0.226f,
                    dy1 = 0.048f,
                    dx2 = -0.547f,
                    dy2 = 0.112f,
                    dx3 = -0.93f,
                    dy3 = 0.176f,
                )
                // c -0.766 0.127 -1.796 0.257 -2.83 0.257
                curveToRelative(
                    dx1 = -0.766f,
                    dy1 = 0.127f,
                    dx2 = -1.796f,
                    dy2 = 0.257f,
                    dx3 = -2.83f,
                    dy3 = 0.257f,
                )
                // c -0.792 0 -1.725 -0.149 -2.663 -0.327
                curveToRelative(
                    dx1 = -0.792f,
                    dy1 = 0.0f,
                    dx2 = -1.725f,
                    dy2 = -0.149f,
                    dx3 = -2.663f,
                    dy3 = -0.327f,
                )
                // l -0.934 -0.183
                lineToRelative(dx = -0.934f, dy = -0.183f)
                // l -0.097 -0.03
                lineToRelative(dx = -0.097f, dy = -0.03f)
                // a 0.5 0.5 0 0 1 0.191 -0.96
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.191f,
                    dy1 = -0.96f,
                )
                // l 0.1 0.01
                lineToRelative(dx = 0.1f, dy = 0.01f)
                // l 0.936 0.183
                lineToRelative(dx = 0.936f, dy = 0.183f)
                // c 0.916 0.174 1.766 0.307 2.467 0.307
                curveToRelative(
                    dx1 = 0.916f,
                    dy1 = 0.174f,
                    dx2 = 1.766f,
                    dy2 = 0.307f,
                    dx3 = 2.467f,
                    dy3 = 0.307f,
                )
                // c 0.958 0 1.929 -0.121 2.665 -0.244
                curveToRelative(
                    dx1 = 0.958f,
                    dy1 = 0.0f,
                    dx2 = 1.929f,
                    dy2 = -0.121f,
                    dx3 = 2.665f,
                    dy3 = -0.244f,
                )
                // a 19.941 19.941 0 0 0 1.195 -0.237
                arcToRelative(
                    a = 19.941f,
                    b = 19.941f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.195f,
                    dy1 = -0.237f,
                )
                // l 0.02 -0.005
                lineToRelative(dx = 0.02f, dy = -0.005f)
                // l 0.1 -0.014z
                lineToRelative(dx = 0.1f, dy = -0.014f)
                close()
                // m -10.98 -0.5
                moveToRelative(dx = -10.98f, dy = -0.5f)
                // c 0.336 0 0.692 0.026 1.058 0.069
                curveToRelative(
                    dx1 = 0.336f,
                    dy1 = 0.0f,
                    dx2 = 0.692f,
                    dy2 = 0.026f,
                    dx3 = 1.058f,
                    dy3 = 0.069f,
                )
                // a 0.5 0.5 0 1 1 -0.115 0.993
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.115f,
                    dy1 = 0.993f,
                )
                // a 8.26 8.26 0 0 0 -0.943 -0.062
                arcToRelative(
                    a = 8.26f,
                    b = 8.26f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.943f,
                    dy1 = -0.062f,
                )
                // c -0.957 0 -1.928 0.12 -2.665 0.243
                curveToRelative(
                    dx1 = -0.957f,
                    dy1 = 0.0f,
                    dx2 = -1.928f,
                    dy2 = 0.12f,
                    dx3 = -2.665f,
                    dy3 = 0.243f,
                )
                // a 19.92 19.92 0 0 0 -1.194 0.237
                arcToRelative(
                    a = 19.92f,
                    b = 19.92f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.194f,
                    dy1 = 0.237f,
                )
                // l -0.02 0.005
                lineToRelative(dx = -0.02f, dy = 0.005f)
                // a 0.5 0.5 0 0 1 -0.242 -0.97
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.242f,
                    dy1 = -0.97f,
                )
                // h 0.003
                horizontalLineToRelative(dx = 0.003f)
                // l 0.005 -0.003
                lineToRelative(dx = 0.005f, dy = -0.003f)
                // l 0.02 -0.005
                lineToRelative(dx = 0.02f, dy = -0.005f)
                // a 9.387 9.387 0 0 1 0.333 -0.075
                arcToRelative(
                    a = 9.387f,
                    b = 9.387f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.333f,
                    dy1 = -0.075f,
                )
                // c 0.226 -0.048 0.547 -0.112 0.931 -0.176
                curveToRelative(
                    dx1 = 0.226f,
                    dy1 = -0.048f,
                    dx2 = 0.547f,
                    dy2 = -0.112f,
                    dx3 = 0.931f,
                    dy3 = -0.176f,
                )
                // c 0.766 -0.127 1.796 -0.256 2.83 -0.256z
                curveToRelative(
                    dx1 = 0.766f,
                    dy1 = -0.127f,
                    dx2 = 1.796f,
                    dy2 = -0.256f,
                    dx3 = 2.83f,
                    dy3 = -0.256f,
                )
                close()
                // m 1.058 -2.932
                moveToRelative(dx = 1.058f, dy = -2.932f)
                // l 0.373 0.049
                lineToRelative(dx = 0.373f, dy = 0.049f)
                // c 0.882 0.125 1.845 0.33 2.75 0.508
                curveToRelative(
                    dx1 = 0.882f,
                    dy1 = 0.125f,
                    dx2 = 1.845f,
                    dy2 = 0.33f,
                    dx3 = 2.75f,
                    dy3 = 0.508f,
                )
                // c 1.057 0.206 2.035 0.375 2.82 0.375
                curveToRelative(
                    dx1 = 1.057f,
                    dy1 = 0.206f,
                    dx2 = 2.035f,
                    dy2 = 0.375f,
                    dx3 = 2.82f,
                    dy3 = 0.375f,
                )
                // c 0.957 0 1.928 -0.121 2.664 -0.244
                curveToRelative(
                    dx1 = 0.957f,
                    dy1 = 0.0f,
                    dx2 = 1.928f,
                    dy2 = -0.121f,
                    dx3 = 2.664f,
                    dy3 = -0.244f,
                )
                // a 19.941 19.941 0 0 0 1.195 -0.237
                arcToRelative(
                    a = 19.941f,
                    b = 19.941f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.195f,
                    dy1 = -0.237f,
                )
                // l 0.02 -0.005
                lineToRelative(dx = 0.02f, dy = -0.005f)
                // l 0.1 -0.014
                lineToRelative(dx = 0.1f, dy = -0.014f)
                // a 0.5 0.5 0 0 1 0.236 0.95
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.236f,
                    dy1 = 0.95f,
                )
                // l -0.095 0.035
                lineToRelative(dx = -0.095f, dy = 0.035f)
                // h -0.003
                horizontalLineToRelative(dx = -0.003f)
                // l -0.005 0.002
                lineToRelative(dx = -0.005f, dy = 0.002f)
                // l -0.019 0.004
                lineToRelative(dx = -0.019f, dy = 0.004f)
                // l -0.07 0.017
                lineToRelative(dx = -0.07f, dy = 0.017f)
                // a 21.087 21.087 0 0 1 -1.194 0.234
                arcToRelative(
                    a = 21.087f,
                    b = 21.087f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.194f,
                    dy1 = 0.234f,
                )
                // c -0.766 0.128 -1.796 0.258 -2.83 0.258
                curveToRelative(
                    dx1 = -0.766f,
                    dy1 = 0.128f,
                    dx2 = -1.796f,
                    dy2 = 0.258f,
                    dx3 = -2.83f,
                    dy3 = 0.258f,
                )
                // c -0.895 0 -1.964 -0.19 -3.01 -0.394
                curveToRelative(
                    dx1 = -0.895f,
                    dy1 = 0.0f,
                    dx2 = -1.964f,
                    dy2 = -0.19f,
                    dx3 = -3.01f,
                    dy3 = -0.394f,
                )
                // c -0.935 -0.183 -1.856 -0.38 -2.694 -0.498
                curveToRelative(
                    dx1 = -0.935f,
                    dy1 = -0.183f,
                    dx2 = -1.856f,
                    dy2 = -0.38f,
                    dx3 = -2.694f,
                    dy3 = -0.498f,
                )
                // l -0.353 -0.046
                lineToRelative(dx = -0.353f, dy = -0.046f)
                // l -0.1 -0.022
                lineToRelative(dx = -0.1f, dy = -0.022f)
                // a 0.5 0.5 0 0 1 0.114 -0.973
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.114f,
                    dy1 = -0.973f,
                )
                // l 0.1 0.001z
                lineToRelative(dx = 0.1f, dy = 0.001f)
                close()
                // m -2.608 0.016
                moveToRelative(dx = -2.608f, dy = 0.016f)
                // a 0.5 0.5 0 0 1 0.101 0.995
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.101f,
                    dy1 = 0.995f,
                )
                // a 20.102 20.102 0 0 0 -2.246 0.363
                arcToRelative(
                    a = 20.102f,
                    b = 20.102f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.246f,
                    dy1 = 0.363f,
                )
                // l -0.139 0.032
                lineToRelative(dx = -0.139f, dy = 0.032f)
                // l -0.035 0.008
                lineToRelative(dx = -0.035f, dy = 0.008f)
                // l -0.008 0.002
                lineToRelative(dx = -0.008f, dy = 0.002f)
                // l -0.002 0.001
                lineToRelative(dx = -0.002f, dy = 0.001f)
                // a 0.5 0.5 0 0 1 -0.242 -0.97
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.242f,
                    dy1 = -0.97f,
                )
                // h 0.002
                horizontalLineToRelative(dx = 0.002f)
                // l 0.003 -0.002
                lineToRelative(dx = 0.003f, dy = -0.002f)
                // l 0.01 -0.003
                lineToRelative(dx = 0.01f, dy = -0.003f)
                // l 0.042 -0.01
                lineToRelative(dx = 0.042f, dy = -0.01f)
                // L 0.586 3
                lineTo(x = 0.586f, y = 3.0f)
                // a 21.09 21.09 0 0 1 2.363 -0.383z
                arcToRelative(
                    a = 21.09f,
                    b = 21.09f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.363f,
                    dy1 = -0.383f,
                )
                close()
            }
        }.build().also { _ic2524 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2524: ImageVector? = null
