package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic999Fill: ImageVector
    get() {
        val current = _ic999Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic999Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.9 13 a4.99 4.99 0 0 0 3.827 -1.783 3 3 0 1 0 .553 -5.63 A4.999 4.999 0 0 0 7.9 3 a4.998 4.998 0 0 0 -4.359 2.549 3 3 0 1 0 .586 5.732 A4.988 4.988 0 0 0 7.9 13Z m.138 -3.503 h-.064 c-.378 0 -.69 -.296 -.641 -.648 a1.8 1.8 0 0 1 .109 -.447 c.087 -.204 .31 -.437 .671 -.7 l.366 -.268 a1.24 1.24 0 0 0 .291 -.278 .907 .907 0 0 0 .2 -.569 c0 -.238 -.074 -.454 -.225 -.648 -.147 -.198 -.418 -.297 -.812 -.297 -.388 0 -.663 .121 -.827 .362 a1.65 1.65 0 0 0 -.07 .114 c-.18 .326 -.488 .639 -.88 .639 -.404 0 -.738 -.32 -.638 -.686 .146 -.54 .445 -.945 .896 -1.214 .394 -.238 .879 -.357 1.454 -.357 .755 0 1.382 .17 1.88 .508 .501 .338 .752 .84 .752 1.504 0 .408 -.109 .75 -.326 1.03 -.127 .169 -.37 .385 -.732 .648 l-.356 .259 a1.016 1.016 0 0 0 -.386 .494 c-.1 .295 -.331 .554 -.662 .554Z M8.016 11.5 H7.97 c-.4 0 -.725 -.304 -.725 -.68 0 -.375 .324 -.679 .725 -.679 h.045 c.4 0 .724 .304 .724 .68 0 .375 -.324 .679 -.724 .679Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.9 13
                moveTo(x = 7.9f, y = 13.0f)
                // a 4.99 4.99 0 0 0 3.827 -1.783
                arcToRelative(
                    a = 4.99f,
                    b = 4.99f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 3.827f,
                    dy1 = -1.783f,
                )
                // a 3 3 0 1 0 0.553 -5.63
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.553f,
                    dy1 = -5.63f,
                )
                // A 4.999 4.999 0 0 0 7.9 3
                arcTo(
                    horizontalEllipseRadius = 4.999f,
                    verticalEllipseRadius = 4.999f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.9f,
                    y1 = 3.0f,
                )
                // a 4.998 4.998 0 0 0 -4.359 2.549
                arcToRelative(
                    a = 4.998f,
                    b = 4.998f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -4.359f,
                    dy1 = 2.549f,
                )
                // a 3 3 0 1 0 0.586 5.732
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.586f,
                    dy1 = 5.732f,
                )
                // A 4.988 4.988 0 0 0 7.9 13z
                arcTo(
                    horizontalEllipseRadius = 4.988f,
                    verticalEllipseRadius = 4.988f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.9f,
                    y1 = 13.0f,
                )
                close()
                // m 0.138 -3.503
                moveToRelative(dx = 0.138f, dy = -3.503f)
                // h -0.064
                horizontalLineToRelative(dx = -0.064f)
                // c -0.378 0 -0.69 -0.296 -0.641 -0.648
                curveToRelative(
                    dx1 = -0.378f,
                    dy1 = 0.0f,
                    dx2 = -0.69f,
                    dy2 = -0.296f,
                    dx3 = -0.641f,
                    dy3 = -0.648f,
                )
                // a 1.8 1.8 0 0 1 0.109 -0.447
                arcToRelative(
                    a = 1.8f,
                    b = 1.8f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.109f,
                    dy1 = -0.447f,
                )
                // c 0.087 -0.204 0.31 -0.437 0.671 -0.7
                curveToRelative(
                    dx1 = 0.087f,
                    dy1 = -0.204f,
                    dx2 = 0.31f,
                    dy2 = -0.437f,
                    dx3 = 0.671f,
                    dy3 = -0.7f,
                )
                // l 0.366 -0.268
                lineToRelative(dx = 0.366f, dy = -0.268f)
                // a 1.24 1.24 0 0 0 0.291 -0.278
                arcToRelative(
                    a = 1.24f,
                    b = 1.24f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.291f,
                    dy1 = -0.278f,
                )
                // a 0.907 0.907 0 0 0 0.2 -0.569
                arcToRelative(
                    a = 0.907f,
                    b = 0.907f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.2f,
                    dy1 = -0.569f,
                )
                // c 0 -0.238 -0.074 -0.454 -0.225 -0.648
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.238f,
                    dx2 = -0.074f,
                    dy2 = -0.454f,
                    dx3 = -0.225f,
                    dy3 = -0.648f,
                )
                // c -0.147 -0.198 -0.418 -0.297 -0.812 -0.297
                curveToRelative(
                    dx1 = -0.147f,
                    dy1 = -0.198f,
                    dx2 = -0.418f,
                    dy2 = -0.297f,
                    dx3 = -0.812f,
                    dy3 = -0.297f,
                )
                // c -0.388 0 -0.663 0.121 -0.827 0.362
                curveToRelative(
                    dx1 = -0.388f,
                    dy1 = 0.0f,
                    dx2 = -0.663f,
                    dy2 = 0.121f,
                    dx3 = -0.827f,
                    dy3 = 0.362f,
                )
                // a 1.65 1.65 0 0 0 -0.07 0.114
                arcToRelative(
                    a = 1.65f,
                    b = 1.65f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.07f,
                    dy1 = 0.114f,
                )
                // c -0.18 0.326 -0.488 0.639 -0.88 0.639
                curveToRelative(
                    dx1 = -0.18f,
                    dy1 = 0.326f,
                    dx2 = -0.488f,
                    dy2 = 0.639f,
                    dx3 = -0.88f,
                    dy3 = 0.639f,
                )
                // c -0.404 0 -0.738 -0.32 -0.638 -0.686
                curveToRelative(
                    dx1 = -0.404f,
                    dy1 = 0.0f,
                    dx2 = -0.738f,
                    dy2 = -0.32f,
                    dx3 = -0.638f,
                    dy3 = -0.686f,
                )
                // c 0.146 -0.54 0.445 -0.945 0.896 -1.214
                curveToRelative(
                    dx1 = 0.146f,
                    dy1 = -0.54f,
                    dx2 = 0.445f,
                    dy2 = -0.945f,
                    dx3 = 0.896f,
                    dy3 = -1.214f,
                )
                // c 0.394 -0.238 0.879 -0.357 1.454 -0.357
                curveToRelative(
                    dx1 = 0.394f,
                    dy1 = -0.238f,
                    dx2 = 0.879f,
                    dy2 = -0.357f,
                    dx3 = 1.454f,
                    dy3 = -0.357f,
                )
                // c 0.755 0 1.382 0.17 1.88 0.508
                curveToRelative(
                    dx1 = 0.755f,
                    dy1 = 0.0f,
                    dx2 = 1.382f,
                    dy2 = 0.17f,
                    dx3 = 1.88f,
                    dy3 = 0.508f,
                )
                // c 0.501 0.338 0.752 0.84 0.752 1.504
                curveToRelative(
                    dx1 = 0.501f,
                    dy1 = 0.338f,
                    dx2 = 0.752f,
                    dy2 = 0.84f,
                    dx3 = 0.752f,
                    dy3 = 1.504f,
                )
                // c 0 0.408 -0.109 0.75 -0.326 1.03
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.408f,
                    dx2 = -0.109f,
                    dy2 = 0.75f,
                    dx3 = -0.326f,
                    dy3 = 1.03f,
                )
                // c -0.127 0.169 -0.37 0.385 -0.732 0.648
                curveToRelative(
                    dx1 = -0.127f,
                    dy1 = 0.169f,
                    dx2 = -0.37f,
                    dy2 = 0.385f,
                    dx3 = -0.732f,
                    dy3 = 0.648f,
                )
                // l -0.356 0.259
                lineToRelative(dx = -0.356f, dy = 0.259f)
                // a 1.016 1.016 0 0 0 -0.386 0.494
                arcToRelative(
                    a = 1.016f,
                    b = 1.016f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.386f,
                    dy1 = 0.494f,
                )
                // c -0.1 0.295 -0.331 0.554 -0.662 0.554z
                curveToRelative(
                    dx1 = -0.1f,
                    dy1 = 0.295f,
                    dx2 = -0.331f,
                    dy2 = 0.554f,
                    dx3 = -0.662f,
                    dy3 = 0.554f,
                )
                close()
                // M 8.016 11.5
                moveTo(x = 8.016f, y = 11.5f)
                // H 7.97
                horizontalLineTo(x = 7.97f)
                // c -0.4 0 -0.725 -0.304 -0.725 -0.68
                curveToRelative(
                    dx1 = -0.4f,
                    dy1 = 0.0f,
                    dx2 = -0.725f,
                    dy2 = -0.304f,
                    dx3 = -0.725f,
                    dy3 = -0.68f,
                )
                // c 0 -0.375 0.324 -0.679 0.725 -0.679
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.375f,
                    dx2 = 0.324f,
                    dy2 = -0.679f,
                    dx3 = 0.725f,
                    dy3 = -0.679f,
                )
                // h 0.045
                horizontalLineToRelative(dx = 0.045f)
                // c 0.4 0 0.724 0.304 0.724 0.68
                curveToRelative(
                    dx1 = 0.4f,
                    dy1 = 0.0f,
                    dx2 = 0.724f,
                    dy2 = 0.304f,
                    dx3 = 0.724f,
                    dy3 = 0.68f,
                )
                // c 0 0.375 -0.324 0.679 -0.724 0.679z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.375f,
                    dx2 = -0.324f,
                    dy2 = 0.679f,
                    dx3 = -0.724f,
                    dy3 = 0.679f,
                )
                close()
            }
        }.build().also { _ic999Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic999Fill: ImageVector? = null
