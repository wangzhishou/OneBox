package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic507: ImageVector
    get() {
        val current = _ic507
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic507",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M14.556 7.467 H1.007 a.215 .215 0 0 0 -.074 .07 A.786 .786 0 0 0 .8 8 c0 .212 .065 .373 .133 .463 .037 .05 .063 .066 .074 .07 h13.549 L15.05 8 l-.495 -.533Z m1.358 .287 c.115 .123 .115 .369 0 .492 l-1.571 1.692 c-.141 .152 -.343 .007 -.343 -.246 v-.359 H1 c-.552 0 -1 -.597 -1 -1.333 s.448 -1.333 1 -1.333 h13 v-.359 c0 -.253 .202 -.398 .343 -.246 l1.571 1.692Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 14.556 7.467
                moveTo(x = 14.556f, y = 7.467f)
                // H 1.007
                horizontalLineTo(x = 1.007f)
                // a 0.215 0.215 0 0 0 -0.074 0.07
                arcToRelative(
                    a = 0.215f,
                    b = 0.215f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.074f,
                    dy1 = 0.07f,
                )
                // A 0.786 0.786 0 0 0 0.8 8
                arcTo(
                    horizontalEllipseRadius = 0.786f,
                    verticalEllipseRadius = 0.786f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 0.8f,
                    y1 = 8.0f,
                )
                // c 0 0.212 0.065 0.373 0.133 0.463
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.212f,
                    dx2 = 0.065f,
                    dy2 = 0.373f,
                    dx3 = 0.133f,
                    dy3 = 0.463f,
                )
                // c 0.037 0.05 0.063 0.066 0.074 0.07
                curveToRelative(
                    dx1 = 0.037f,
                    dy1 = 0.05f,
                    dx2 = 0.063f,
                    dy2 = 0.066f,
                    dx3 = 0.074f,
                    dy3 = 0.07f,
                )
                // h 13.549
                horizontalLineToRelative(dx = 13.549f)
                // L 15.05 8
                lineTo(x = 15.05f, y = 8.0f)
                // l -0.495 -0.533z
                lineToRelative(dx = -0.495f, dy = -0.533f)
                close()
                // m 1.358 0.287
                moveToRelative(dx = 1.358f, dy = 0.287f)
                // c 0.115 0.123 0.115 0.369 0 0.492
                curveToRelative(
                    dx1 = 0.115f,
                    dy1 = 0.123f,
                    dx2 = 0.115f,
                    dy2 = 0.369f,
                    dx3 = 0.0f,
                    dy3 = 0.492f,
                )
                // l -1.571 1.692
                lineToRelative(dx = -1.571f, dy = 1.692f)
                // c -0.141 0.152 -0.343 0.007 -0.343 -0.246
                curveToRelative(
                    dx1 = -0.141f,
                    dy1 = 0.152f,
                    dx2 = -0.343f,
                    dy2 = 0.007f,
                    dx3 = -0.343f,
                    dy3 = -0.246f,
                )
                // v -0.359
                verticalLineToRelative(dy = -0.359f)
                // H 1
                horizontalLineTo(x = 1.0f)
                // c -0.552 0 -1 -0.597 -1 -1.333
                curveToRelative(
                    dx1 = -0.552f,
                    dy1 = 0.0f,
                    dx2 = -1.0f,
                    dy2 = -0.597f,
                    dx3 = -1.0f,
                    dy3 = -1.333f,
                )
                // s 0.448 -1.333 1 -1.333
                reflectiveCurveToRelative(
                    dx1 = 0.448f,
                    dy1 = -1.333f,
                    dx2 = 1.0f,
                    dy2 = -1.333f,
                )
                // h 13
                horizontalLineToRelative(dx = 13.0f)
                // v -0.359
                verticalLineToRelative(dy = -0.359f)
                // c 0 -0.253 0.202 -0.398 0.343 -0.246
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.253f,
                    dx2 = 0.202f,
                    dy2 = -0.398f,
                    dx3 = 0.343f,
                    dy3 = -0.246f,
                )
                // l 1.571 1.692z
                lineToRelative(dx = 1.571f, dy = 1.692f)
                close()
            }
            // M10.462 2.99 c.388 .39 1.052 .476 1.516 .156 .44 -.303 .536 -.862 .177 -1.238 a7.232 7.232 0 0 0 -1.52 -1.241 C9.91 .222 9.06 0 8.082 0 6.82 0 5.788 .354 4.984 1.063 c-.788 .694 -1.182 1.548 -1.182 2.562 0 .472 .102 .944 .307 1.417 .205 .472 .528 .916 .97 1.333 .097 .088 .21 .186 .341 .292 h3.284 c-1.158 -.77 -1.909 -1.333 -2.254 -1.688 -.378 -.389 -.567 -.84 -.567 -1.354 0 -.528 .197 -.958 .59 -1.292 .41 -.333 .907 -.5 1.49 -.5 .6 0 1.12 .132 1.561 .396 .276 .156 .588 .41 .938 .761Z m1.558 6.343 H9.125 c.406 .327 .729 .626 .967 .896 .488 .542 .733 1.118 .733 1.73 0 .61 -.292 1.145 -.875 1.604 -.584 .458 -1.285 .687 -2.105 .687 -1.06 0 -2.033 -.58 -2.92 -1.738 a1.175 1.175 0 0 0 -1.43 -.328 c-.464 .236 -.643 .754 -.359 1.154 C4.396 15.113 6.044 16 8.082 16 c1.308 0 2.451 -.41 3.428 -1.23 .993 -.832 1.49 -1.812 1.49 -2.937 0 -.86 -.315 -1.68 -.946 -2.458 l-.034 -.042Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10.462 2.99
                moveTo(x = 10.462f, y = 2.99f)
                // c 0.388 0.39 1.052 0.476 1.516 0.156
                curveToRelative(
                    dx1 = 0.388f,
                    dy1 = 0.39f,
                    dx2 = 1.052f,
                    dy2 = 0.476f,
                    dx3 = 1.516f,
                    dy3 = 0.156f,
                )
                // c 0.44 -0.303 0.536 -0.862 0.177 -1.238
                curveToRelative(
                    dx1 = 0.44f,
                    dy1 = -0.303f,
                    dx2 = 0.536f,
                    dy2 = -0.862f,
                    dx3 = 0.177f,
                    dy3 = -1.238f,
                )
                // a 7.232 7.232 0 0 0 -1.52 -1.241
                arcToRelative(
                    a = 7.232f,
                    b = 7.232f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.52f,
                    dy1 = -1.241f,
                )
                // C 9.91 0.222 9.06 0 8.082 0
                curveTo(
                    x1 = 9.91f,
                    y1 = 0.222f,
                    x2 = 9.06f,
                    y2 = 0.0f,
                    x3 = 8.082f,
                    y3 = 0.0f,
                )
                // C 6.82 0 5.788 0.354 4.984 1.063
                curveTo(
                    x1 = 6.82f,
                    y1 = 0.0f,
                    x2 = 5.788f,
                    y2 = 0.354f,
                    x3 = 4.984f,
                    y3 = 1.063f,
                )
                // c -0.788 0.694 -1.182 1.548 -1.182 2.562
                curveToRelative(
                    dx1 = -0.788f,
                    dy1 = 0.694f,
                    dx2 = -1.182f,
                    dy2 = 1.548f,
                    dx3 = -1.182f,
                    dy3 = 2.562f,
                )
                // c 0 0.472 0.102 0.944 0.307 1.417
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.472f,
                    dx2 = 0.102f,
                    dy2 = 0.944f,
                    dx3 = 0.307f,
                    dy3 = 1.417f,
                )
                // c 0.205 0.472 0.528 0.916 0.97 1.333
                curveToRelative(
                    dx1 = 0.205f,
                    dy1 = 0.472f,
                    dx2 = 0.528f,
                    dy2 = 0.916f,
                    dx3 = 0.97f,
                    dy3 = 1.333f,
                )
                // c 0.097 0.088 0.21 0.186 0.341 0.292
                curveToRelative(
                    dx1 = 0.097f,
                    dy1 = 0.088f,
                    dx2 = 0.21f,
                    dy2 = 0.186f,
                    dx3 = 0.341f,
                    dy3 = 0.292f,
                )
                // h 3.284
                horizontalLineToRelative(dx = 3.284f)
                // c -1.158 -0.77 -1.909 -1.333 -2.254 -1.688
                curveToRelative(
                    dx1 = -1.158f,
                    dy1 = -0.77f,
                    dx2 = -1.909f,
                    dy2 = -1.333f,
                    dx3 = -2.254f,
                    dy3 = -1.688f,
                )
                // c -0.378 -0.389 -0.567 -0.84 -0.567 -1.354
                curveToRelative(
                    dx1 = -0.378f,
                    dy1 = -0.389f,
                    dx2 = -0.567f,
                    dy2 = -0.84f,
                    dx3 = -0.567f,
                    dy3 = -1.354f,
                )
                // c 0 -0.528 0.197 -0.958 0.59 -1.292
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.528f,
                    dx2 = 0.197f,
                    dy2 = -0.958f,
                    dx3 = 0.59f,
                    dy3 = -1.292f,
                )
                // c 0.41 -0.333 0.907 -0.5 1.49 -0.5
                curveToRelative(
                    dx1 = 0.41f,
                    dy1 = -0.333f,
                    dx2 = 0.907f,
                    dy2 = -0.5f,
                    dx3 = 1.49f,
                    dy3 = -0.5f,
                )
                // c 0.6 0 1.12 0.132 1.561 0.396
                curveToRelative(
                    dx1 = 0.6f,
                    dy1 = 0.0f,
                    dx2 = 1.12f,
                    dy2 = 0.132f,
                    dx3 = 1.561f,
                    dy3 = 0.396f,
                )
                // c 0.276 0.156 0.588 0.41 0.938 0.761z
                curveToRelative(
                    dx1 = 0.276f,
                    dy1 = 0.156f,
                    dx2 = 0.588f,
                    dy2 = 0.41f,
                    dx3 = 0.938f,
                    dy3 = 0.761f,
                )
                close()
                // m 1.558 6.343
                moveToRelative(dx = 1.558f, dy = 6.343f)
                // H 9.125
                horizontalLineTo(x = 9.125f)
                // c 0.406 0.327 0.729 0.626 0.967 0.896
                curveToRelative(
                    dx1 = 0.406f,
                    dy1 = 0.327f,
                    dx2 = 0.729f,
                    dy2 = 0.626f,
                    dx3 = 0.967f,
                    dy3 = 0.896f,
                )
                // c 0.488 0.542 0.733 1.118 0.733 1.73
                curveToRelative(
                    dx1 = 0.488f,
                    dy1 = 0.542f,
                    dx2 = 0.733f,
                    dy2 = 1.118f,
                    dx3 = 0.733f,
                    dy3 = 1.73f,
                )
                // c 0 0.61 -0.292 1.145 -0.875 1.604
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.61f,
                    dx2 = -0.292f,
                    dy2 = 1.145f,
                    dx3 = -0.875f,
                    dy3 = 1.604f,
                )
                // c -0.584 0.458 -1.285 0.687 -2.105 0.687
                curveToRelative(
                    dx1 = -0.584f,
                    dy1 = 0.458f,
                    dx2 = -1.285f,
                    dy2 = 0.687f,
                    dx3 = -2.105f,
                    dy3 = 0.687f,
                )
                // c -1.06 0 -2.033 -0.58 -2.92 -1.738
                curveToRelative(
                    dx1 = -1.06f,
                    dy1 = 0.0f,
                    dx2 = -2.033f,
                    dy2 = -0.58f,
                    dx3 = -2.92f,
                    dy3 = -1.738f,
                )
                // a 1.175 1.175 0 0 0 -1.43 -0.328
                arcToRelative(
                    a = 1.175f,
                    b = 1.175f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.43f,
                    dy1 = -0.328f,
                )
                // c -0.464 0.236 -0.643 0.754 -0.359 1.154
                curveToRelative(
                    dx1 = -0.464f,
                    dy1 = 0.236f,
                    dx2 = -0.643f,
                    dy2 = 0.754f,
                    dx3 = -0.359f,
                    dy3 = 1.154f,
                )
                // C 4.396 15.113 6.044 16 8.082 16
                curveTo(
                    x1 = 4.396f,
                    y1 = 15.113f,
                    x2 = 6.044f,
                    y2 = 16.0f,
                    x3 = 8.082f,
                    y3 = 16.0f,
                )
                // c 1.308 0 2.451 -0.41 3.428 -1.23
                curveToRelative(
                    dx1 = 1.308f,
                    dy1 = 0.0f,
                    dx2 = 2.451f,
                    dy2 = -0.41f,
                    dx3 = 3.428f,
                    dy3 = -1.23f,
                )
                // c 0.993 -0.832 1.49 -1.812 1.49 -2.937
                curveToRelative(
                    dx1 = 0.993f,
                    dy1 = -0.832f,
                    dx2 = 1.49f,
                    dy2 = -1.812f,
                    dx3 = 1.49f,
                    dy3 = -2.937f,
                )
                // c 0 -0.86 -0.315 -1.68 -0.946 -2.458
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.86f,
                    dx2 = -0.315f,
                    dy2 = -1.68f,
                    dx3 = -0.946f,
                    dy3 = -2.458f,
                )
                // l -0.034 -0.042z
                lineToRelative(dx = -0.034f, dy = -0.042f)
                close()
            }
        }.build().also { _ic507 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic507: ImageVector? = null
