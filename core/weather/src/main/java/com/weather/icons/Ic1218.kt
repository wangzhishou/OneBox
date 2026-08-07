package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1218: ImageVector
    get() {
        val current = _ic1218
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1218",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.004 5.996 C6.953 5.461 7.414 5 8 5 c.587 0 1.047 .46 .996 .996 l-.45 4.704 H7.454 l-.45 -4.704Z M8.75 12.25 a.75 .75 0 1 1 -1.5 0 .75 .75 0 0 1 1.5 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.004 5.996
                moveTo(x = 7.004f, y = 5.996f)
                // C 6.953 5.461 7.414 5 8 5
                curveTo(
                    x1 = 6.953f,
                    y1 = 5.461f,
                    x2 = 7.414f,
                    y2 = 5.0f,
                    x3 = 8.0f,
                    y3 = 5.0f,
                )
                // c 0.587 0 1.047 0.46 0.996 0.996
                curveToRelative(
                    dx1 = 0.587f,
                    dy1 = 0.0f,
                    dx2 = 1.047f,
                    dy2 = 0.46f,
                    dx3 = 0.996f,
                    dy3 = 0.996f,
                )
                // l -0.45 4.704
                lineToRelative(dx = -0.45f, dy = 4.704f)
                // H 7.454
                horizontalLineTo(x = 7.454f)
                // l -0.45 -4.704z
                lineToRelative(dx = -0.45f, dy = -4.704f)
                close()
                // M 8.75 12.25
                moveTo(x = 8.75f, y = 12.25f)
                // a 0.75 0.75 0 1 1 -1.5 0
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.5f,
                    dy1 = 0.0f,
                )
                // a 0.75 0.75 0 0 1 1.5 0z
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.5f,
                    dy1 = 0.0f,
                )
                close()
            }
            // M7.545 1.261 a.526 .526 0 0 1 .91 0 l7.475 12.963 a.519 .519 0 0 1 -.454 .776 H.524 a.519 .519 0 0 1 -.454 -.776 L7.545 1.26Z M14.301 13.8 8 2.875 1.7 13.8 h12.6Z M11.493 .81 a.4 .4 0 0 1 .48 -.3 c1.306 .303 2.367 1.03 2.976 2.084 .609 1.055 .708 2.337 .317 3.619 a.4 .4 0 1 1 -.765 -.233 c.336 -1.103 .237 -2.15 -.245 -2.986 -.482 -.835 -1.34 -1.444 -2.463 -1.704 a.4 .4 0 0 1 -.3 -.48Z m-.52 1.213 a.4 .4 0 0 1 .492 -.278 c.947 .262 1.717 .819 2.172 1.607 .455 .787 .552 1.733 .306 2.684 a.4 .4 0 0 1 -.775 -.2 c.201 -.777 .112 -1.502 -.224 -2.084 -.336 -.583 -.92 -1.022 -1.693 -1.237 a.4 .4 0 0 1 -.279 -.492Z m-.568 1.322 a.4 .4 0 0 1 .508 -.25 c.574 .196 1.045 .56 1.334 1.06 .288 .5 .368 1.089 .25 1.684 a.4 .4 0 0 1 -.784 -.155 c.084 -.428 .022 -.817 -.16 -1.13 -.18 -.313 -.485 -.561 -.898 -.702 a.4 .4 0 0 1 -.25 -.507Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.545 1.261
                moveTo(x = 7.545f, y = 1.261f)
                // a 0.526 0.526 0 0 1 0.91 0
                arcToRelative(
                    a = 0.526f,
                    b = 0.526f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.91f,
                    dy1 = 0.0f,
                )
                // l 7.475 12.963
                lineToRelative(dx = 7.475f, dy = 12.963f)
                // a 0.519 0.519 0 0 1 -0.454 0.776
                arcToRelative(
                    a = 0.519f,
                    b = 0.519f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.454f,
                    dy1 = 0.776f,
                )
                // H 0.524
                horizontalLineTo(x = 0.524f)
                // a 0.519 0.519 0 0 1 -0.454 -0.776
                arcToRelative(
                    a = 0.519f,
                    b = 0.519f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.454f,
                    dy1 = -0.776f,
                )
                // L 7.545 1.26z
                lineTo(x = 7.545f, y = 1.26f)
                close()
                // M 14.301 13.8
                moveTo(x = 14.301f, y = 13.8f)
                // L 8 2.875
                lineTo(x = 8.0f, y = 2.875f)
                // L 1.7 13.8
                lineTo(x = 1.7f, y = 13.8f)
                // h 12.6z
                horizontalLineToRelative(dx = 12.6f)
                close()
                // M 11.493 0.81
                moveTo(x = 11.493f, y = 0.81f)
                // a 0.4 0.4 0 0 1 0.48 -0.3
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.48f,
                    dy1 = -0.3f,
                )
                // c 1.306 0.303 2.367 1.03 2.976 2.084
                curveToRelative(
                    dx1 = 1.306f,
                    dy1 = 0.303f,
                    dx2 = 2.367f,
                    dy2 = 1.03f,
                    dx3 = 2.976f,
                    dy3 = 2.084f,
                )
                // c 0.609 1.055 0.708 2.337 0.317 3.619
                curveToRelative(
                    dx1 = 0.609f,
                    dy1 = 1.055f,
                    dx2 = 0.708f,
                    dy2 = 2.337f,
                    dx3 = 0.317f,
                    dy3 = 3.619f,
                )
                // a 0.4 0.4 0 1 1 -0.765 -0.233
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.765f,
                    dy1 = -0.233f,
                )
                // c 0.336 -1.103 0.237 -2.15 -0.245 -2.986
                curveToRelative(
                    dx1 = 0.336f,
                    dy1 = -1.103f,
                    dx2 = 0.237f,
                    dy2 = -2.15f,
                    dx3 = -0.245f,
                    dy3 = -2.986f,
                )
                // c -0.482 -0.835 -1.34 -1.444 -2.463 -1.704
                curveToRelative(
                    dx1 = -0.482f,
                    dy1 = -0.835f,
                    dx2 = -1.34f,
                    dy2 = -1.444f,
                    dx3 = -2.463f,
                    dy3 = -1.704f,
                )
                // a 0.4 0.4 0 0 1 -0.3 -0.48z
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.3f,
                    dy1 = -0.48f,
                )
                close()
                // m -0.52 1.213
                moveToRelative(dx = -0.52f, dy = 1.213f)
                // a 0.4 0.4 0 0 1 0.492 -0.278
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.492f,
                    dy1 = -0.278f,
                )
                // c 0.947 0.262 1.717 0.819 2.172 1.607
                curveToRelative(
                    dx1 = 0.947f,
                    dy1 = 0.262f,
                    dx2 = 1.717f,
                    dy2 = 0.819f,
                    dx3 = 2.172f,
                    dy3 = 1.607f,
                )
                // c 0.455 0.787 0.552 1.733 0.306 2.684
                curveToRelative(
                    dx1 = 0.455f,
                    dy1 = 0.787f,
                    dx2 = 0.552f,
                    dy2 = 1.733f,
                    dx3 = 0.306f,
                    dy3 = 2.684f,
                )
                // a 0.4 0.4 0 0 1 -0.775 -0.2
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.775f,
                    dy1 = -0.2f,
                )
                // c 0.201 -0.777 0.112 -1.502 -0.224 -2.084
                curveToRelative(
                    dx1 = 0.201f,
                    dy1 = -0.777f,
                    dx2 = 0.112f,
                    dy2 = -1.502f,
                    dx3 = -0.224f,
                    dy3 = -2.084f,
                )
                // c -0.336 -0.583 -0.92 -1.022 -1.693 -1.237
                curveToRelative(
                    dx1 = -0.336f,
                    dy1 = -0.583f,
                    dx2 = -0.92f,
                    dy2 = -1.022f,
                    dx3 = -1.693f,
                    dy3 = -1.237f,
                )
                // a 0.4 0.4 0 0 1 -0.279 -0.492z
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.279f,
                    dy1 = -0.492f,
                )
                close()
                // m -0.568 1.322
                moveToRelative(dx = -0.568f, dy = 1.322f)
                // a 0.4 0.4 0 0 1 0.508 -0.25
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.508f,
                    dy1 = -0.25f,
                )
                // c 0.574 0.196 1.045 0.56 1.334 1.06
                curveToRelative(
                    dx1 = 0.574f,
                    dy1 = 0.196f,
                    dx2 = 1.045f,
                    dy2 = 0.56f,
                    dx3 = 1.334f,
                    dy3 = 1.06f,
                )
                // c 0.288 0.5 0.368 1.089 0.25 1.684
                curveToRelative(
                    dx1 = 0.288f,
                    dy1 = 0.5f,
                    dx2 = 0.368f,
                    dy2 = 1.089f,
                    dx3 = 0.25f,
                    dy3 = 1.684f,
                )
                // a 0.4 0.4 0 0 1 -0.784 -0.155
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.784f,
                    dy1 = -0.155f,
                )
                // c 0.084 -0.428 0.022 -0.817 -0.16 -1.13
                curveToRelative(
                    dx1 = 0.084f,
                    dy1 = -0.428f,
                    dx2 = 0.022f,
                    dy2 = -0.817f,
                    dx3 = -0.16f,
                    dy3 = -1.13f,
                )
                // c -0.18 -0.313 -0.485 -0.561 -0.898 -0.702
                curveToRelative(
                    dx1 = -0.18f,
                    dy1 = -0.313f,
                    dx2 = -0.485f,
                    dy2 = -0.561f,
                    dx3 = -0.898f,
                    dy3 = -0.702f,
                )
                // a 0.4 0.4 0 0 1 -0.25 -0.507z
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.25f,
                    dy1 = -0.507f,
                )
                close()
            }
        }.build().also { _ic1218 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1218: ImageVector? = null
