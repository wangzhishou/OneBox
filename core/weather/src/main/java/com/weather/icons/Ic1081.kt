package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1081: ImageVector
    get() {
        val current = _ic1081
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1081",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M3.96 12.404 a2 2 0 1 1 0 -.8 57.509 57.509 0 0 1 1.997 .059 c.66 .031 1.372 .079 2.017 .15 .636 .071 1.235 .168 1.652 .307 .109 .037 .214 .08 .303 .137 .081 .052 .204 .15 .255 .316 a.507 .507 0 0 1 -.086 .454 .852 .852 0 0 1 -.225 .2 3.45 3.45 0 0 1 -.54 .26 c-.162 .064 -.346 .133 -.538 .205 l-.155 .057 c-.501 .189 -1.036 .398 -1.474 .646 -.31 .175 -.526 .346 -.657 .508 .086 .002 .195 0 .328 -.01 .56 -.042 1.359 -.194 2.191 -.385 a54.563 54.563 0 0 0 2.99 -.784 2 2 0 1 1 .056 .817 l-.533 .15 c-.64 .177 -1.492 .403 -2.334 .597 -.837 .191 -1.686 .356 -2.31 .402 -.303 .023 -.604 .022 -.832 -.045 a.689 .689 0 0 1 -.376 -.25 .572 .572 0 0 1 -.068 -.521 c.175 -.527 .669 -.902 1.15 -1.175 .5 -.283 1.09 -.512 1.589 -.698 l.161 -.061 c.161 -.06 .31 -.115 .443 -.168 a11.48 11.48 0 0 0 -1.078 -.164 32.291 32.291 0 0 0 -1.968 -.146 57.435 57.435 0 0 0 -1.959 -.058Z m-2.21 -.654 H1 v.5 h.75 V13 h.5 v-.75 H3 v-.5 h-.75 V11 h-.5 v.75Z M13 14.25 h2 v-.5 h-2 v.5Z M7.36 3.392 a.642 .642 0 0 1 1.283 0 v.997 l.864 -.499 a.642 .642 0 1 1 .642 1.112 l-.864 .498 .864 .498 a.642 .642 0 1 1 -.642 1.112 l-.864 -.499 v.997 a.642 .642 0 1 1 -1.283 0 v-.997 l-.863 .499 a.642 .642 0 0 1 -.642 -1.112 l.863 -.498 -.863 -.498 a.642 .642 0 0 1 .642 -1.112 l.863 .499 v-.997Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 3.96 12.404
                moveTo(x = 3.96f, y = 12.404f)
                // a 2 2 0 1 1 0 -0.8
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -0.8f,
                )
                // a 57.509 57.509 0 0 1 1.997 0.059
                arcToRelative(
                    a = 57.509f,
                    b = 57.509f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.997f,
                    dy1 = 0.059f,
                )
                // c 0.66 0.031 1.372 0.079 2.017 0.15
                curveToRelative(
                    dx1 = 0.66f,
                    dy1 = 0.031f,
                    dx2 = 1.372f,
                    dy2 = 0.079f,
                    dx3 = 2.017f,
                    dy3 = 0.15f,
                )
                // c 0.636 0.071 1.235 0.168 1.652 0.307
                curveToRelative(
                    dx1 = 0.636f,
                    dy1 = 0.071f,
                    dx2 = 1.235f,
                    dy2 = 0.168f,
                    dx3 = 1.652f,
                    dy3 = 0.307f,
                )
                // c 0.109 0.037 0.214 0.08 0.303 0.137
                curveToRelative(
                    dx1 = 0.109f,
                    dy1 = 0.037f,
                    dx2 = 0.214f,
                    dy2 = 0.08f,
                    dx3 = 0.303f,
                    dy3 = 0.137f,
                )
                // c 0.081 0.052 0.204 0.15 0.255 0.316
                curveToRelative(
                    dx1 = 0.081f,
                    dy1 = 0.052f,
                    dx2 = 0.204f,
                    dy2 = 0.15f,
                    dx3 = 0.255f,
                    dy3 = 0.316f,
                )
                // a 0.507 0.507 0 0 1 -0.086 0.454
                arcToRelative(
                    a = 0.507f,
                    b = 0.507f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.086f,
                    dy1 = 0.454f,
                )
                // a 0.852 0.852 0 0 1 -0.225 0.2
                arcToRelative(
                    a = 0.852f,
                    b = 0.852f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.225f,
                    dy1 = 0.2f,
                )
                // a 3.45 3.45 0 0 1 -0.54 0.26
                arcToRelative(
                    a = 3.45f,
                    b = 3.45f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.54f,
                    dy1 = 0.26f,
                )
                // c -0.162 0.064 -0.346 0.133 -0.538 0.205
                curveToRelative(
                    dx1 = -0.162f,
                    dy1 = 0.064f,
                    dx2 = -0.346f,
                    dy2 = 0.133f,
                    dx3 = -0.538f,
                    dy3 = 0.205f,
                )
                // l -0.155 0.057
                lineToRelative(dx = -0.155f, dy = 0.057f)
                // c -0.501 0.189 -1.036 0.398 -1.474 0.646
                curveToRelative(
                    dx1 = -0.501f,
                    dy1 = 0.189f,
                    dx2 = -1.036f,
                    dy2 = 0.398f,
                    dx3 = -1.474f,
                    dy3 = 0.646f,
                )
                // c -0.31 0.175 -0.526 0.346 -0.657 0.508
                curveToRelative(
                    dx1 = -0.31f,
                    dy1 = 0.175f,
                    dx2 = -0.526f,
                    dy2 = 0.346f,
                    dx3 = -0.657f,
                    dy3 = 0.508f,
                )
                // c 0.086 0.002 0.195 0 0.328 -0.01
                curveToRelative(
                    dx1 = 0.086f,
                    dy1 = 0.002f,
                    dx2 = 0.195f,
                    dy2 = 0.0f,
                    dx3 = 0.328f,
                    dy3 = -0.01f,
                )
                // c 0.56 -0.042 1.359 -0.194 2.191 -0.385
                curveToRelative(
                    dx1 = 0.56f,
                    dy1 = -0.042f,
                    dx2 = 1.359f,
                    dy2 = -0.194f,
                    dx3 = 2.191f,
                    dy3 = -0.385f,
                )
                // a 54.563 54.563 0 0 0 2.99 -0.784
                arcToRelative(
                    a = 54.563f,
                    b = 54.563f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.99f,
                    dy1 = -0.784f,
                )
                // a 2 2 0 1 1 0.056 0.817
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.056f,
                    dy1 = 0.817f,
                )
                // l -0.533 0.15
                lineToRelative(dx = -0.533f, dy = 0.15f)
                // c -0.64 0.177 -1.492 0.403 -2.334 0.597
                curveToRelative(
                    dx1 = -0.64f,
                    dy1 = 0.177f,
                    dx2 = -1.492f,
                    dy2 = 0.403f,
                    dx3 = -2.334f,
                    dy3 = 0.597f,
                )
                // c -0.837 0.191 -1.686 0.356 -2.31 0.402
                curveToRelative(
                    dx1 = -0.837f,
                    dy1 = 0.191f,
                    dx2 = -1.686f,
                    dy2 = 0.356f,
                    dx3 = -2.31f,
                    dy3 = 0.402f,
                )
                // c -0.303 0.023 -0.604 0.022 -0.832 -0.045
                curveToRelative(
                    dx1 = -0.303f,
                    dy1 = 0.023f,
                    dx2 = -0.604f,
                    dy2 = 0.022f,
                    dx3 = -0.832f,
                    dy3 = -0.045f,
                )
                // a 0.689 0.689 0 0 1 -0.376 -0.25
                arcToRelative(
                    a = 0.689f,
                    b = 0.689f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.376f,
                    dy1 = -0.25f,
                )
                // a 0.572 0.572 0 0 1 -0.068 -0.521
                arcToRelative(
                    a = 0.572f,
                    b = 0.572f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.068f,
                    dy1 = -0.521f,
                )
                // c 0.175 -0.527 0.669 -0.902 1.15 -1.175
                curveToRelative(
                    dx1 = 0.175f,
                    dy1 = -0.527f,
                    dx2 = 0.669f,
                    dy2 = -0.902f,
                    dx3 = 1.15f,
                    dy3 = -1.175f,
                )
                // c 0.5 -0.283 1.09 -0.512 1.589 -0.698
                curveToRelative(
                    dx1 = 0.5f,
                    dy1 = -0.283f,
                    dx2 = 1.09f,
                    dy2 = -0.512f,
                    dx3 = 1.589f,
                    dy3 = -0.698f,
                )
                // l 0.161 -0.061
                lineToRelative(dx = 0.161f, dy = -0.061f)
                // c 0.161 -0.06 0.31 -0.115 0.443 -0.168
                curveToRelative(
                    dx1 = 0.161f,
                    dy1 = -0.06f,
                    dx2 = 0.31f,
                    dy2 = -0.115f,
                    dx3 = 0.443f,
                    dy3 = -0.168f,
                )
                // a 11.48 11.48 0 0 0 -1.078 -0.164
                arcToRelative(
                    a = 11.48f,
                    b = 11.48f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.078f,
                    dy1 = -0.164f,
                )
                // a 32.291 32.291 0 0 0 -1.968 -0.146
                arcToRelative(
                    a = 32.291f,
                    b = 32.291f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.968f,
                    dy1 = -0.146f,
                )
                // a 57.435 57.435 0 0 0 -1.959 -0.058z
                arcToRelative(
                    a = 57.435f,
                    b = 57.435f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.959f,
                    dy1 = -0.058f,
                )
                close()
                // m -2.21 -0.654
                moveToRelative(dx = -2.21f, dy = -0.654f)
                // H 1
                horizontalLineTo(x = 1.0f)
                // v 0.5
                verticalLineToRelative(dy = 0.5f)
                // h 0.75
                horizontalLineToRelative(dx = 0.75f)
                // V 13
                verticalLineTo(y = 13.0f)
                // h 0.5
                horizontalLineToRelative(dx = 0.5f)
                // v -0.75
                verticalLineToRelative(dy = -0.75f)
                // H 3
                horizontalLineTo(x = 3.0f)
                // v -0.5
                verticalLineToRelative(dy = -0.5f)
                // h -0.75
                horizontalLineToRelative(dx = -0.75f)
                // V 11
                verticalLineTo(y = 11.0f)
                // h -0.5
                horizontalLineToRelative(dx = -0.5f)
                // v 0.75z
                verticalLineToRelative(dy = 0.75f)
                close()
                // M 13 14.25
                moveTo(x = 13.0f, y = 14.25f)
                // h 2
                horizontalLineToRelative(dx = 2.0f)
                // v -0.5
                verticalLineToRelative(dy = -0.5f)
                // h -2
                horizontalLineToRelative(dx = -2.0f)
                // v 0.5z
                verticalLineToRelative(dy = 0.5f)
                close()
                // M 7.36 3.392
                moveTo(x = 7.36f, y = 3.392f)
                // a 0.642 0.642 0 0 1 1.283 0
                arcToRelative(
                    a = 0.642f,
                    b = 0.642f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.283f,
                    dy1 = 0.0f,
                )
                // v 0.997
                verticalLineToRelative(dy = 0.997f)
                // l 0.864 -0.499
                lineToRelative(dx = 0.864f, dy = -0.499f)
                // a 0.642 0.642 0 1 1 0.642 1.112
                arcToRelative(
                    a = 0.642f,
                    b = 0.642f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.642f,
                    dy1 = 1.112f,
                )
                // l -0.864 0.498
                lineToRelative(dx = -0.864f, dy = 0.498f)
                // l 0.864 0.498
                lineToRelative(dx = 0.864f, dy = 0.498f)
                // a 0.642 0.642 0 1 1 -0.642 1.112
                arcToRelative(
                    a = 0.642f,
                    b = 0.642f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.642f,
                    dy1 = 1.112f,
                )
                // l -0.864 -0.499
                lineToRelative(dx = -0.864f, dy = -0.499f)
                // v 0.997
                verticalLineToRelative(dy = 0.997f)
                // a 0.642 0.642 0 1 1 -1.283 0
                arcToRelative(
                    a = 0.642f,
                    b = 0.642f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.283f,
                    dy1 = 0.0f,
                )
                // v -0.997
                verticalLineToRelative(dy = -0.997f)
                // l -0.863 0.499
                lineToRelative(dx = -0.863f, dy = 0.499f)
                // a 0.642 0.642 0 0 1 -0.642 -1.112
                arcToRelative(
                    a = 0.642f,
                    b = 0.642f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.642f,
                    dy1 = -1.112f,
                )
                // l 0.863 -0.498
                lineToRelative(dx = 0.863f, dy = -0.498f)
                // l -0.863 -0.498
                lineToRelative(dx = -0.863f, dy = -0.498f)
                // a 0.642 0.642 0 0 1 0.642 -1.112
                arcToRelative(
                    a = 0.642f,
                    b = 0.642f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.642f,
                    dy1 = -1.112f,
                )
                // l 0.863 0.499
                lineToRelative(dx = 0.863f, dy = 0.499f)
                // v -0.997z
                verticalLineToRelative(dy = -0.997f)
                close()
            }
            // M12.571 2.573 8.241 .065 a.476 .476 0 0 0 -.482 0 l-4.33 2.508 a.485 .485 0 0 0 -.241 .42 v5.015 c0 .173 .092 .332 .24 .419 l4.33 2.508 a.473 .473 0 0 0 .483 0 l4.33 -2.508 a.483 .483 0 0 0 .242 -.419 V2.992 a.484 .484 0 0 0 -.242 -.419Z m-.723 5.156 L8 9.958 4.152 7.729 V3.272 L8 1.042 l3.848 2.23 v4.457Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 12.571 2.573
                moveTo(x = 12.571f, y = 2.573f)
                // L 8.241 0.065
                lineTo(x = 8.241f, y = 0.065f)
                // a 0.476 0.476 0 0 0 -0.482 0
                arcToRelative(
                    a = 0.476f,
                    b = 0.476f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.482f,
                    dy1 = 0.0f,
                )
                // l -4.33 2.508
                lineToRelative(dx = -4.33f, dy = 2.508f)
                // a 0.485 0.485 0 0 0 -0.241 0.42
                arcToRelative(
                    a = 0.485f,
                    b = 0.485f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.241f,
                    dy1 = 0.42f,
                )
                // v 5.015
                verticalLineToRelative(dy = 5.015f)
                // c 0 0.173 0.092 0.332 0.24 0.419
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.173f,
                    dx2 = 0.092f,
                    dy2 = 0.332f,
                    dx3 = 0.24f,
                    dy3 = 0.419f,
                )
                // l 4.33 2.508
                lineToRelative(dx = 4.33f, dy = 2.508f)
                // a 0.473 0.473 0 0 0 0.483 0
                arcToRelative(
                    a = 0.473f,
                    b = 0.473f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.483f,
                    dy1 = 0.0f,
                )
                // l 4.33 -2.508
                lineToRelative(dx = 4.33f, dy = -2.508f)
                // a 0.483 0.483 0 0 0 0.242 -0.419
                arcToRelative(
                    a = 0.483f,
                    b = 0.483f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.242f,
                    dy1 = -0.419f,
                )
                // V 2.992
                verticalLineTo(y = 2.992f)
                // a 0.484 0.484 0 0 0 -0.242 -0.419z
                arcToRelative(
                    a = 0.484f,
                    b = 0.484f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.242f,
                    dy1 = -0.419f,
                )
                close()
                // m -0.723 5.156
                moveToRelative(dx = -0.723f, dy = 5.156f)
                // L 8 9.958
                lineTo(x = 8.0f, y = 9.958f)
                // L 4.152 7.729
                lineTo(x = 4.152f, y = 7.729f)
                // V 3.272
                verticalLineTo(y = 3.272f)
                // L 8 1.042
                lineTo(x = 8.0f, y = 1.042f)
                // l 3.848 2.23
                lineToRelative(dx = 3.848f, dy = 2.23f)
                // v 4.457z
                verticalLineToRelative(dy = 4.457f)
                close()
            }
        }.build().also { _ic1081 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1081: ImageVector? = null
