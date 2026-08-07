package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2415: ImageVector
    get() {
        val current = _ic2415
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2415",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M12.635 7.477 16 7 v9 H0 v-1.059 s7.605 -.09 10 -1.641 c1.944 -1.259 1.908 -4.44 2.176 -5.412 a.563 .563 0 0 1 .46 -.41Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 12.635 7.477
                moveTo(x = 12.635f, y = 7.477f)
                // L 16 7
                lineTo(x = 16.0f, y = 7.0f)
                // v 9
                verticalLineToRelative(dy = 9.0f)
                // H 0
                horizontalLineTo(x = 0.0f)
                // v -1.059
                verticalLineToRelative(dy = -1.059f)
                // s 7.605 -0.09 10 -1.641
                reflectiveCurveToRelative(
                    dx1 = 7.605f,
                    dy1 = -0.09f,
                    dx2 = 10.0f,
                    dy2 = -1.641f,
                )
                // c 1.944 -1.259 1.908 -4.44 2.176 -5.412
                curveToRelative(
                    dx1 = 1.944f,
                    dy1 = -1.259f,
                    dx2 = 1.908f,
                    dy2 = -4.44f,
                    dx3 = 2.176f,
                    dy3 = -5.412f,
                )
                // a 0.563 0.563 0 0 1 0.46 -0.41z
                arcToRelative(
                    a = 0.563f,
                    b = 0.563f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.46f,
                    dy1 = -0.41f,
                )
                close()
            }
            // m11.091 7.456 -.867 .433 a.5 .5 0 0 1 -.448 0 l-1.105 -.553 a1.5 1.5 0 0 0 -1.342 0 l-1.105 .553 a.5 .5 0 0 1 -.448 0 l-1.105 -.553 a1.5 1.5 0 0 0 -1.342 0 l-1.034 .517 a.5 .5 0 0 1 -.5 -.03 l-1.018 -.68 a.5 .5 0 1 0 -.554 .832 l1.017 .678 a1.5 1.5 0 0 0 1.503 .094 l1.033 -.517 a.5 .5 0 0 1 .448 0 l1.105 .553 a1.5 1.5 0 0 0 1.342 0 l1.105 -.553 a.5 .5 0 0 1 .448 0 l1.105 .553 a1.5 1.5 0 0 0 1.342 0 l.263 -.132 c.034 -.239 .062 -.466 .089 -.676 .024 -.19 .046 -.364 .068 -.52Z m-.419 2.609 -.448 .224 a.5 .5 0 0 1 -.448 0 l-1.105 -.553 a1.5 1.5 0 0 0 -1.342 0 l-1.105 .553 a.5 .5 0 0 1 -.448 0 l-1.105 -.553 a1.5 1.5 0 0 0 -1.342 0 l-1.034 .517 a.5 .5 0 0 1 -.5 -.03 l-1.018 -.68 a.5 .5 0 1 0 -.554 .832 l1.017 .678 a1.5 1.5 0 0 0 1.503 .094 l1.033 -.517 a.5 .5 0 0 1 .448 0 l1.105 .553 a1.5 1.5 0 0 0 1.342 0 l1.105 -.553 a.5 .5 0 0 1 .448 0 l1.105 .553 a1.5 1.5 0 0 0 .935 .135 7.89 7.89 0 0 0 .408 -1.254Z M9.5 12.55 l-.83 -.414 a1.5 1.5 0 0 0 -1.34 0 l-1.106 .553 a.5 .5 0 0 1 -.448 0 l-1.105 -.553 a1.5 1.5 0 0 0 -1.342 0 l-1.034 .517 a.5 .5 0 0 1 -.5 -.03 l-1.018 -.679 a.5 .5 0 1 0 -.554 .832 l1.017 .678 a1.5 1.5 0 0 0 1.503 .094 l1.033 -.517 a.5 .5 0 0 1 .448 0 l1.105 .553 a1.5 1.5 0 0 0 1.342 0 l1.105 -.553 a.5 .5 0 0 1 .448 0 l.404 .202 a3.16 3.16 0 0 0 .872 -.682Z M7.533 1.706 c-.024 -.212 .192 -.393 .467 -.393 s.49 .181 .467 .393 l-.211 1.857 h-.512 l-.21 -1.857Z m.845 2.607 a.375 .375 0 1 1 -.75 0 .375 .375 0 0 1 .75 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.091 7.456
                moveTo(x = 11.091f, y = 7.456f)
                // l -0.867 0.433
                lineToRelative(dx = -0.867f, dy = 0.433f)
                // a 0.5 0.5 0 0 1 -0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.448f,
                    dy1 = 0.0f,
                )
                // l -1.105 -0.553
                lineToRelative(dx = -1.105f, dy = -0.553f)
                // a 1.5 1.5 0 0 0 -1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.342f,
                    dy1 = 0.0f,
                )
                // l -1.105 0.553
                lineToRelative(dx = -1.105f, dy = 0.553f)
                // a 0.5 0.5 0 0 1 -0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.448f,
                    dy1 = 0.0f,
                )
                // l -1.105 -0.553
                lineToRelative(dx = -1.105f, dy = -0.553f)
                // a 1.5 1.5 0 0 0 -1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.342f,
                    dy1 = 0.0f,
                )
                // l -1.034 0.517
                lineToRelative(dx = -1.034f, dy = 0.517f)
                // a 0.5 0.5 0 0 1 -0.5 -0.03
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.03f,
                )
                // l -1.018 -0.68
                lineToRelative(dx = -1.018f, dy = -0.68f)
                // a 0.5 0.5 0 1 0 -0.554 0.832
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.554f,
                    dy1 = 0.832f,
                )
                // l 1.017 0.678
                lineToRelative(dx = 1.017f, dy = 0.678f)
                // a 1.5 1.5 0 0 0 1.503 0.094
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.503f,
                    dy1 = 0.094f,
                )
                // l 1.033 -0.517
                lineToRelative(dx = 1.033f, dy = -0.517f)
                // a 0.5 0.5 0 0 1 0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.448f,
                    dy1 = 0.0f,
                )
                // l 1.105 0.553
                lineToRelative(dx = 1.105f, dy = 0.553f)
                // a 1.5 1.5 0 0 0 1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.342f,
                    dy1 = 0.0f,
                )
                // l 1.105 -0.553
                lineToRelative(dx = 1.105f, dy = -0.553f)
                // a 0.5 0.5 0 0 1 0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.448f,
                    dy1 = 0.0f,
                )
                // l 1.105 0.553
                lineToRelative(dx = 1.105f, dy = 0.553f)
                // a 1.5 1.5 0 0 0 1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.342f,
                    dy1 = 0.0f,
                )
                // l 0.263 -0.132
                lineToRelative(dx = 0.263f, dy = -0.132f)
                // c 0.034 -0.239 0.062 -0.466 0.089 -0.676
                curveToRelative(
                    dx1 = 0.034f,
                    dy1 = -0.239f,
                    dx2 = 0.062f,
                    dy2 = -0.466f,
                    dx3 = 0.089f,
                    dy3 = -0.676f,
                )
                // c 0.024 -0.19 0.046 -0.364 0.068 -0.52z
                curveToRelative(
                    dx1 = 0.024f,
                    dy1 = -0.19f,
                    dx2 = 0.046f,
                    dy2 = -0.364f,
                    dx3 = 0.068f,
                    dy3 = -0.52f,
                )
                close()
                // m -0.419 2.609
                moveToRelative(dx = -0.419f, dy = 2.609f)
                // l -0.448 0.224
                lineToRelative(dx = -0.448f, dy = 0.224f)
                // a 0.5 0.5 0 0 1 -0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.448f,
                    dy1 = 0.0f,
                )
                // l -1.105 -0.553
                lineToRelative(dx = -1.105f, dy = -0.553f)
                // a 1.5 1.5 0 0 0 -1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.342f,
                    dy1 = 0.0f,
                )
                // l -1.105 0.553
                lineToRelative(dx = -1.105f, dy = 0.553f)
                // a 0.5 0.5 0 0 1 -0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.448f,
                    dy1 = 0.0f,
                )
                // l -1.105 -0.553
                lineToRelative(dx = -1.105f, dy = -0.553f)
                // a 1.5 1.5 0 0 0 -1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.342f,
                    dy1 = 0.0f,
                )
                // l -1.034 0.517
                lineToRelative(dx = -1.034f, dy = 0.517f)
                // a 0.5 0.5 0 0 1 -0.5 -0.03
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.03f,
                )
                // l -1.018 -0.68
                lineToRelative(dx = -1.018f, dy = -0.68f)
                // a 0.5 0.5 0 1 0 -0.554 0.832
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.554f,
                    dy1 = 0.832f,
                )
                // l 1.017 0.678
                lineToRelative(dx = 1.017f, dy = 0.678f)
                // a 1.5 1.5 0 0 0 1.503 0.094
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.503f,
                    dy1 = 0.094f,
                )
                // l 1.033 -0.517
                lineToRelative(dx = 1.033f, dy = -0.517f)
                // a 0.5 0.5 0 0 1 0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.448f,
                    dy1 = 0.0f,
                )
                // l 1.105 0.553
                lineToRelative(dx = 1.105f, dy = 0.553f)
                // a 1.5 1.5 0 0 0 1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.342f,
                    dy1 = 0.0f,
                )
                // l 1.105 -0.553
                lineToRelative(dx = 1.105f, dy = -0.553f)
                // a 0.5 0.5 0 0 1 0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.448f,
                    dy1 = 0.0f,
                )
                // l 1.105 0.553
                lineToRelative(dx = 1.105f, dy = 0.553f)
                // a 1.5 1.5 0 0 0 0.935 0.135
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.935f,
                    dy1 = 0.135f,
                )
                // a 7.89 7.89 0 0 0 0.408 -1.254z
                arcToRelative(
                    a = 7.89f,
                    b = 7.89f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.408f,
                    dy1 = -1.254f,
                )
                close()
                // M 9.5 12.55
                moveTo(x = 9.5f, y = 12.55f)
                // l -0.83 -0.414
                lineToRelative(dx = -0.83f, dy = -0.414f)
                // a 1.5 1.5 0 0 0 -1.34 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.34f,
                    dy1 = 0.0f,
                )
                // l -1.106 0.553
                lineToRelative(dx = -1.106f, dy = 0.553f)
                // a 0.5 0.5 0 0 1 -0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.448f,
                    dy1 = 0.0f,
                )
                // l -1.105 -0.553
                lineToRelative(dx = -1.105f, dy = -0.553f)
                // a 1.5 1.5 0 0 0 -1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.342f,
                    dy1 = 0.0f,
                )
                // l -1.034 0.517
                lineToRelative(dx = -1.034f, dy = 0.517f)
                // a 0.5 0.5 0 0 1 -0.5 -0.03
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.03f,
                )
                // l -1.018 -0.679
                lineToRelative(dx = -1.018f, dy = -0.679f)
                // a 0.5 0.5 0 1 0 -0.554 0.832
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.554f,
                    dy1 = 0.832f,
                )
                // l 1.017 0.678
                lineToRelative(dx = 1.017f, dy = 0.678f)
                // a 1.5 1.5 0 0 0 1.503 0.094
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.503f,
                    dy1 = 0.094f,
                )
                // l 1.033 -0.517
                lineToRelative(dx = 1.033f, dy = -0.517f)
                // a 0.5 0.5 0 0 1 0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.448f,
                    dy1 = 0.0f,
                )
                // l 1.105 0.553
                lineToRelative(dx = 1.105f, dy = 0.553f)
                // a 1.5 1.5 0 0 0 1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.342f,
                    dy1 = 0.0f,
                )
                // l 1.105 -0.553
                lineToRelative(dx = 1.105f, dy = -0.553f)
                // a 0.5 0.5 0 0 1 0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.448f,
                    dy1 = 0.0f,
                )
                // l 0.404 0.202
                lineToRelative(dx = 0.404f, dy = 0.202f)
                // a 3.16 3.16 0 0 0 0.872 -0.682z
                arcToRelative(
                    a = 3.16f,
                    b = 3.16f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.872f,
                    dy1 = -0.682f,
                )
                close()
                // M 7.533 1.706
                moveTo(x = 7.533f, y = 1.706f)
                // c -0.024 -0.212 0.192 -0.393 0.467 -0.393
                curveToRelative(
                    dx1 = -0.024f,
                    dy1 = -0.212f,
                    dx2 = 0.192f,
                    dy2 = -0.393f,
                    dx3 = 0.467f,
                    dy3 = -0.393f,
                )
                // s 0.49 0.181 0.467 0.393
                reflectiveCurveToRelative(
                    dx1 = 0.49f,
                    dy1 = 0.181f,
                    dx2 = 0.467f,
                    dy2 = 0.393f,
                )
                // l -0.211 1.857
                lineToRelative(dx = -0.211f, dy = 1.857f)
                // h -0.512
                horizontalLineToRelative(dx = -0.512f)
                // l -0.21 -1.857z
                lineToRelative(dx = -0.21f, dy = -1.857f)
                close()
                // m 0.845 2.607
                moveToRelative(dx = 0.845f, dy = 2.607f)
                // a 0.375 0.375 0 1 1 -0.75 0
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.75f,
                    dy1 = 0.0f,
                )
                // a 0.375 0.375 0 0 1 0.75 0z
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.75f,
                    dy1 = 0.0f,
                )
                close()
            }
            // M5 3 a3 3 0 1 0 6 0 3 3 0 0 0 -6 0Z m5.512 0 a2.512 2.512 0 1 1 -5.024 0 2.512 2.512 0 0 1 5.024 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 5 3
                moveTo(x = 5.0f, y = 3.0f)
                // a 3 3 0 1 0 6 0
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 6.0f,
                    dy1 = 0.0f,
                )
                // a 3 3 0 0 0 -6 0z
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -6.0f,
                    dy1 = 0.0f,
                )
                close()
                // m 5.512 0
                moveToRelative(dx = 5.512f, dy = 0.0f)
                // a 2.512 2.512 0 1 1 -5.024 0
                arcToRelative(
                    a = 2.512f,
                    b = 2.512f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -5.024f,
                    dy1 = 0.0f,
                )
                // a 2.512 2.512 0 0 1 5.024 0z
                arcToRelative(
                    a = 2.512f,
                    b = 2.512f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 5.024f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic2415 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2415: ImageVector? = null
