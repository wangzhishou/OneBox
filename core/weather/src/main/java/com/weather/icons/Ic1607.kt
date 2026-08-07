package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1607: ImageVector
    get() {
        val current = _ic1607
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1607",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M6.235 4.81 A.454 .454 0 0 1 6 4.872 .465 .465 0 0 1 5.765 4 .454 .454 0 0 1 6 3.938 a.454 .454 0 0 1 .403 .23 c.043 .071 .065 .15 .065 .236 a.467 .467 0 0 1 -.233 .406Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6.235 4.81
                moveTo(x = 6.235f, y = 4.81f)
                // A 0.454 0.454 0 0 1 6 4.872
                arcTo(
                    horizontalEllipseRadius = 0.454f,
                    verticalEllipseRadius = 0.454f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 6.0f,
                    y1 = 4.872f,
                )
                // A 0.465 0.465 0 0 1 5.765 4
                arcTo(
                    horizontalEllipseRadius = 0.465f,
                    verticalEllipseRadius = 0.465f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 5.765f,
                    y1 = 4.0f,
                )
                // A 0.454 0.454 0 0 1 6 3.938
                arcTo(
                    horizontalEllipseRadius = 0.454f,
                    verticalEllipseRadius = 0.454f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 6.0f,
                    y1 = 3.938f,
                )
                // a 0.454 0.454 0 0 1 0.403 0.23
                arcToRelative(
                    a = 0.454f,
                    b = 0.454f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.403f,
                    dy1 = 0.23f,
                )
                // c 0.043 0.071 0.065 0.15 0.065 0.236
                curveToRelative(
                    dx1 = 0.043f,
                    dy1 = 0.071f,
                    dx2 = 0.065f,
                    dy2 = 0.15f,
                    dx3 = 0.065f,
                    dy3 = 0.236f,
                )
                // a 0.467 0.467 0 0 1 -0.233 0.406z
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.233f,
                    dy1 = 0.406f,
                )
                close()
            }
            // M3.097 .777 a.5 .5 0 0 1 .649 -.538 L8 1.6 12.254 .239 a.5 .5 0 0 1 .649 .538 L12 8 h2.481 c.52 0 .716 .804 .275 1.129 l-6.481 4.778 a.452 .452 0 0 1 -.55 0 L1.245 9.13 C.802 8.804 .997 8 1.518 8 H4 L3.097 .777Z m2.4 4.495 A.974 .974 0 0 0 6 5.405 a.974 .974 0 0 0 .503 -.133 c.151 -.09 .272 -.211 .362 -.363 A.976 .976 0 0 0 7 4.404 a1.006 1.006 0 0 0 -1 -.999 1.006 1.006 0 0 0 -1 .999 c0 .184 .045 .353 .135 .505 .09 .152 .21 .272 .362 .363Z M11 5.367 a1.578 1.578 0 0 0 -.19 -.578 1.434 1.434 0 0 0 -.367 -.43 1.59 1.59 0 0 0 -.508 -.268 A2.012 2.012 0 0 0 9.32 4 c-.347 0 -.657 .08 -.93 .238 a1.67 1.67 0 0 0 -.65 .686 C7.578 5.222 7.5 5.58 7.5 6 c0 .418 .079 .777 .237 1.076 .157 .3 .373 .528 .647 .686 .274 .159 .586 .238 .935 .238 .236 0 .452 -.034 .646 -.103 A1.541 1.541 0 0 0 11 6.657 l-.725 -.003 a.837 .837 0 0 1 -.332 .529 .92 .92 0 0 1 -.281 .138 c-.103 .03 -.215 .046 -.334 .046 -.214 0 -.405 -.051 -.572 -.154 a1.059 1.059 0 0 1 -.391 -.46 A1.788 1.788 0 0 1 8.223 6 c0 -.293 .047 -.54 .142 -.743 .094 -.204 .225 -.359 .391 -.464 .167 -.107 .359 -.16 .574 -.16 .122 0 .235 .017 .34 .05 a.84 .84 0 0 1 .49 .375 .858 .858 0 0 1 .115 .309 H11Z m-9.706 5.229 a.5 .5 0 0 0 -.588 .808 l5.5 4 a.5 .5 0 0 0 .588 -.808 l-5.5 -4Z m-.741 3.18 a.5 .5 0 0 1 .67 -.223 l3 1.5 a.5 .5 0 1 1 -.447 .894 l-3 -1.5 a.5 .5 0 0 1 -.223 -.67Z m14.894 0 a.5 .5 0 0 0 -.67 -.223 l-3 1.5 a.5 .5 0 1 0 .447 .894 l3 -1.5 a.5 .5 0 0 0 .223 -.67Z m-.043 -3.07 a.5 .5 0 0 0 -.698 -.11 l-5.5 4 a.5 .5 0 0 0 .588 .808 l5.5 -4 a.5 .5 0 0 0 .11 -.698Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 3.097 0.777
                moveTo(x = 3.097f, y = 0.777f)
                // a 0.5 0.5 0 0 1 0.649 -0.538
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.649f,
                    dy1 = -0.538f,
                )
                // L 8 1.6
                lineTo(x = 8.0f, y = 1.6f)
                // L 12.254 0.239
                lineTo(x = 12.254f, y = 0.239f)
                // a 0.5 0.5 0 0 1 0.649 0.538
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.649f,
                    dy1 = 0.538f,
                )
                // L 12 8
                lineTo(x = 12.0f, y = 8.0f)
                // h 2.481
                horizontalLineToRelative(dx = 2.481f)
                // c 0.52 0 0.716 0.804 0.275 1.129
                curveToRelative(
                    dx1 = 0.52f,
                    dy1 = 0.0f,
                    dx2 = 0.716f,
                    dy2 = 0.804f,
                    dx3 = 0.275f,
                    dy3 = 1.129f,
                )
                // l -6.481 4.778
                lineToRelative(dx = -6.481f, dy = 4.778f)
                // a 0.452 0.452 0 0 1 -0.55 0
                arcToRelative(
                    a = 0.452f,
                    b = 0.452f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.55f,
                    dy1 = 0.0f,
                )
                // L 1.245 9.13
                lineTo(x = 1.245f, y = 9.13f)
                // C 0.802 8.804 0.997 8 1.518 8
                curveTo(
                    x1 = 0.802f,
                    y1 = 8.804f,
                    x2 = 0.997f,
                    y2 = 8.0f,
                    x3 = 1.518f,
                    y3 = 8.0f,
                )
                // H 4
                horizontalLineTo(x = 4.0f)
                // L 3.097 0.777z
                lineTo(x = 3.097f, y = 0.777f)
                close()
                // m 2.4 4.495
                moveToRelative(dx = 2.4f, dy = 4.495f)
                // A 0.974 0.974 0 0 0 6 5.405
                arcTo(
                    horizontalEllipseRadius = 0.974f,
                    verticalEllipseRadius = 0.974f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 6.0f,
                    y1 = 5.405f,
                )
                // a 0.974 0.974 0 0 0 0.503 -0.133
                arcToRelative(
                    a = 0.974f,
                    b = 0.974f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.503f,
                    dy1 = -0.133f,
                )
                // c 0.151 -0.09 0.272 -0.211 0.362 -0.363
                curveToRelative(
                    dx1 = 0.151f,
                    dy1 = -0.09f,
                    dx2 = 0.272f,
                    dy2 = -0.211f,
                    dx3 = 0.362f,
                    dy3 = -0.363f,
                )
                // A 0.976 0.976 0 0 0 7 4.404
                arcTo(
                    horizontalEllipseRadius = 0.976f,
                    verticalEllipseRadius = 0.976f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.0f,
                    y1 = 4.404f,
                )
                // a 1.006 1.006 0 0 0 -1 -0.999
                arcToRelative(
                    a = 1.006f,
                    b = 1.006f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.0f,
                    dy1 = -0.999f,
                )
                // a 1.006 1.006 0 0 0 -1 0.999
                arcToRelative(
                    a = 1.006f,
                    b = 1.006f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.0f,
                    dy1 = 0.999f,
                )
                // c 0 0.184 0.045 0.353 0.135 0.505
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.184f,
                    dx2 = 0.045f,
                    dy2 = 0.353f,
                    dx3 = 0.135f,
                    dy3 = 0.505f,
                )
                // c 0.09 0.152 0.21 0.272 0.362 0.363z
                curveToRelative(
                    dx1 = 0.09f,
                    dy1 = 0.152f,
                    dx2 = 0.21f,
                    dy2 = 0.272f,
                    dx3 = 0.362f,
                    dy3 = 0.363f,
                )
                close()
                // M 11 5.367
                moveTo(x = 11.0f, y = 5.367f)
                // a 1.578 1.578 0 0 0 -0.19 -0.578
                arcToRelative(
                    a = 1.578f,
                    b = 1.578f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.19f,
                    dy1 = -0.578f,
                )
                // a 1.434 1.434 0 0 0 -0.367 -0.43
                arcToRelative(
                    a = 1.434f,
                    b = 1.434f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.367f,
                    dy1 = -0.43f,
                )
                // a 1.59 1.59 0 0 0 -0.508 -0.268
                arcToRelative(
                    a = 1.59f,
                    b = 1.59f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.508f,
                    dy1 = -0.268f,
                )
                // A 2.012 2.012 0 0 0 9.32 4
                arcTo(
                    horizontalEllipseRadius = 2.012f,
                    verticalEllipseRadius = 2.012f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 9.32f,
                    y1 = 4.0f,
                )
                // c -0.347 0 -0.657 0.08 -0.93 0.238
                curveToRelative(
                    dx1 = -0.347f,
                    dy1 = 0.0f,
                    dx2 = -0.657f,
                    dy2 = 0.08f,
                    dx3 = -0.93f,
                    dy3 = 0.238f,
                )
                // a 1.67 1.67 0 0 0 -0.65 0.686
                arcToRelative(
                    a = 1.67f,
                    b = 1.67f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.65f,
                    dy1 = 0.686f,
                )
                // C 7.578 5.222 7.5 5.58 7.5 6
                curveTo(
                    x1 = 7.578f,
                    y1 = 5.222f,
                    x2 = 7.5f,
                    y2 = 5.58f,
                    x3 = 7.5f,
                    y3 = 6.0f,
                )
                // c 0 0.418 0.079 0.777 0.237 1.076
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.418f,
                    dx2 = 0.079f,
                    dy2 = 0.777f,
                    dx3 = 0.237f,
                    dy3 = 1.076f,
                )
                // c 0.157 0.3 0.373 0.528 0.647 0.686
                curveToRelative(
                    dx1 = 0.157f,
                    dy1 = 0.3f,
                    dx2 = 0.373f,
                    dy2 = 0.528f,
                    dx3 = 0.647f,
                    dy3 = 0.686f,
                )
                // c 0.274 0.159 0.586 0.238 0.935 0.238
                curveToRelative(
                    dx1 = 0.274f,
                    dy1 = 0.159f,
                    dx2 = 0.586f,
                    dy2 = 0.238f,
                    dx3 = 0.935f,
                    dy3 = 0.238f,
                )
                // c 0.236 0 0.452 -0.034 0.646 -0.103
                curveToRelative(
                    dx1 = 0.236f,
                    dy1 = 0.0f,
                    dx2 = 0.452f,
                    dy2 = -0.034f,
                    dx3 = 0.646f,
                    dy3 = -0.103f,
                )
                // A 1.541 1.541 0 0 0 11 6.657
                arcTo(
                    horizontalEllipseRadius = 1.541f,
                    verticalEllipseRadius = 1.541f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 11.0f,
                    y1 = 6.657f,
                )
                // l -0.725 -0.003
                lineToRelative(dx = -0.725f, dy = -0.003f)
                // a 0.837 0.837 0 0 1 -0.332 0.529
                arcToRelative(
                    a = 0.837f,
                    b = 0.837f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.332f,
                    dy1 = 0.529f,
                )
                // a 0.92 0.92 0 0 1 -0.281 0.138
                arcToRelative(
                    a = 0.92f,
                    b = 0.92f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.281f,
                    dy1 = 0.138f,
                )
                // c -0.103 0.03 -0.215 0.046 -0.334 0.046
                curveToRelative(
                    dx1 = -0.103f,
                    dy1 = 0.03f,
                    dx2 = -0.215f,
                    dy2 = 0.046f,
                    dx3 = -0.334f,
                    dy3 = 0.046f,
                )
                // c -0.214 0 -0.405 -0.051 -0.572 -0.154
                curveToRelative(
                    dx1 = -0.214f,
                    dy1 = 0.0f,
                    dx2 = -0.405f,
                    dy2 = -0.051f,
                    dx3 = -0.572f,
                    dy3 = -0.154f,
                )
                // a 1.059 1.059 0 0 1 -0.391 -0.46
                arcToRelative(
                    a = 1.059f,
                    b = 1.059f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.391f,
                    dy1 = -0.46f,
                )
                // A 1.788 1.788 0 0 1 8.223 6
                arcTo(
                    horizontalEllipseRadius = 1.788f,
                    verticalEllipseRadius = 1.788f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 8.223f,
                    y1 = 6.0f,
                )
                // c 0 -0.293 0.047 -0.54 0.142 -0.743
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.293f,
                    dx2 = 0.047f,
                    dy2 = -0.54f,
                    dx3 = 0.142f,
                    dy3 = -0.743f,
                )
                // c 0.094 -0.204 0.225 -0.359 0.391 -0.464
                curveToRelative(
                    dx1 = 0.094f,
                    dy1 = -0.204f,
                    dx2 = 0.225f,
                    dy2 = -0.359f,
                    dx3 = 0.391f,
                    dy3 = -0.464f,
                )
                // c 0.167 -0.107 0.359 -0.16 0.574 -0.16
                curveToRelative(
                    dx1 = 0.167f,
                    dy1 = -0.107f,
                    dx2 = 0.359f,
                    dy2 = -0.16f,
                    dx3 = 0.574f,
                    dy3 = -0.16f,
                )
                // c 0.122 0 0.235 0.017 0.34 0.05
                curveToRelative(
                    dx1 = 0.122f,
                    dy1 = 0.0f,
                    dx2 = 0.235f,
                    dy2 = 0.017f,
                    dx3 = 0.34f,
                    dy3 = 0.05f,
                )
                // a 0.84 0.84 0 0 1 0.49 0.375
                arcToRelative(
                    a = 0.84f,
                    b = 0.84f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.49f,
                    dy1 = 0.375f,
                )
                // a 0.858 0.858 0 0 1 0.115 0.309
                arcToRelative(
                    a = 0.858f,
                    b = 0.858f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.115f,
                    dy1 = 0.309f,
                )
                // H 11z
                horizontalLineTo(x = 11.0f)
                close()
                // m -9.706 5.229
                moveToRelative(dx = -9.706f, dy = 5.229f)
                // a 0.5 0.5 0 0 0 -0.588 0.808
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.588f,
                    dy1 = 0.808f,
                )
                // l 5.5 4
                lineToRelative(dx = 5.5f, dy = 4.0f)
                // a 0.5 0.5 0 0 0 0.588 -0.808
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.588f,
                    dy1 = -0.808f,
                )
                // l -5.5 -4z
                lineToRelative(dx = -5.5f, dy = -4.0f)
                close()
                // m -0.741 3.18
                moveToRelative(dx = -0.741f, dy = 3.18f)
                // a 0.5 0.5 0 0 1 0.67 -0.223
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.67f,
                    dy1 = -0.223f,
                )
                // l 3 1.5
                lineToRelative(dx = 3.0f, dy = 1.5f)
                // a 0.5 0.5 0 1 1 -0.447 0.894
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.447f,
                    dy1 = 0.894f,
                )
                // l -3 -1.5
                lineToRelative(dx = -3.0f, dy = -1.5f)
                // a 0.5 0.5 0 0 1 -0.223 -0.67z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.223f,
                    dy1 = -0.67f,
                )
                close()
                // m 14.894 0
                moveToRelative(dx = 14.894f, dy = 0.0f)
                // a 0.5 0.5 0 0 0 -0.67 -0.223
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.67f,
                    dy1 = -0.223f,
                )
                // l -3 1.5
                lineToRelative(dx = -3.0f, dy = 1.5f)
                // a 0.5 0.5 0 1 0 0.447 0.894
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.447f,
                    dy1 = 0.894f,
                )
                // l 3 -1.5
                lineToRelative(dx = 3.0f, dy = -1.5f)
                // a 0.5 0.5 0 0 0 0.223 -0.67z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.223f,
                    dy1 = -0.67f,
                )
                close()
                // m -0.043 -3.07
                moveToRelative(dx = -0.043f, dy = -3.07f)
                // a 0.5 0.5 0 0 0 -0.698 -0.11
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.698f,
                    dy1 = -0.11f,
                )
                // l -5.5 4
                lineToRelative(dx = -5.5f, dy = 4.0f)
                // a 0.5 0.5 0 0 0 0.588 0.808
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.588f,
                    dy1 = 0.808f,
                )
                // l 5.5 -4
                lineToRelative(dx = 5.5f, dy = -4.0f)
                // a 0.5 0.5 0 0 0 0.11 -0.698z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.11f,
                    dy1 = -0.698f,
                )
                close()
            }
        }.build().also { _ic1607 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1607: ImageVector? = null
