package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1086: ImageVector
    get() {
        val current = _ic1086
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1086",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M4.5 7 a.5 .5 0 0 1 .5 .5 v2.063 a2 2 0 1 1 -1 0 V7.5 a.5 .5 0 0 1 .5 -.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.5 7
                moveTo(x = 4.5f, y = 7.0f)
                // a 0.5 0.5 0 0 1 0.5 0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = 0.5f,
                )
                // v 2.063
                verticalLineToRelative(dy = 2.063f)
                // a 2 2 0 1 1 -1 0
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // V 7.5
                verticalLineTo(y = 7.5f)
                // a 0.5 0.5 0 0 1 0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = -0.5f,
                )
                close()
            }
            // m5.8 8.399 .532 .356 a3.3 3.3 0 1 1 -3.665 0 l.533 -.356 V2.5 a1.3 1.3 0 0 1 2.6 0 v5.899Z M7 2.5 a2.5 2.5 0 0 0 -5 0 v5.258 a4.5 4.5 0 1 0 5 0 V2.5Z m5.465 -.033 a.467 .467 0 0 0 -.933 0 v.725 l-.628 -.363 a.467 .467 0 0 0 -.467 .808 l.628 .363 -.628 .363 a.467 .467 0 0 0 .467 .808 l.628 -.363 v.725 a.467 .467 0 1 0 .933 0 v-.725 l.628 .363 a.467 .467 0 1 0 .467 -.808 L12.932 4 l.628 -.363 a.467 .467 0 0 0 -.467 -.808 l-.628 .363 v-.725Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 5.8 8.399
                moveTo(x = 5.8f, y = 8.399f)
                // l 0.532 0.356
                lineToRelative(dx = 0.532f, dy = 0.356f)
                // a 3.3 3.3 0 1 1 -3.665 0
                arcToRelative(
                    a = 3.3f,
                    b = 3.3f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -3.665f,
                    dy1 = 0.0f,
                )
                // l 0.533 -0.356
                lineToRelative(dx = 0.533f, dy = -0.356f)
                // V 2.5
                verticalLineTo(y = 2.5f)
                // a 1.3 1.3 0 0 1 2.6 0
                arcToRelative(
                    a = 1.3f,
                    b = 1.3f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.6f,
                    dy1 = 0.0f,
                )
                // v 5.899z
                verticalLineToRelative(dy = 5.899f)
                close()
                // M 7 2.5
                moveTo(x = 7.0f, y = 2.5f)
                // a 2.5 2.5 0 0 0 -5 0
                arcToRelative(
                    a = 2.5f,
                    b = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -5.0f,
                    dy1 = 0.0f,
                )
                // v 5.258
                verticalLineToRelative(dy = 5.258f)
                // a 4.5 4.5 0 1 0 5 0
                arcToRelative(
                    a = 4.5f,
                    b = 4.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 5.0f,
                    dy1 = 0.0f,
                )
                // V 2.5z
                verticalLineTo(y = 2.5f)
                close()
                // m 5.465 -0.033
                moveToRelative(dx = 5.465f, dy = -0.033f)
                // a 0.467 0.467 0 0 0 -0.933 0
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.933f,
                    dy1 = 0.0f,
                )
                // v 0.725
                verticalLineToRelative(dy = 0.725f)
                // l -0.628 -0.363
                lineToRelative(dx = -0.628f, dy = -0.363f)
                // a 0.467 0.467 0 0 0 -0.467 0.808
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.467f,
                    dy1 = 0.808f,
                )
                // l 0.628 0.363
                lineToRelative(dx = 0.628f, dy = 0.363f)
                // l -0.628 0.363
                lineToRelative(dx = -0.628f, dy = 0.363f)
                // a 0.467 0.467 0 0 0 0.467 0.808
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.467f,
                    dy1 = 0.808f,
                )
                // l 0.628 -0.363
                lineToRelative(dx = 0.628f, dy = -0.363f)
                // v 0.725
                verticalLineToRelative(dy = 0.725f)
                // a 0.467 0.467 0 1 0 0.933 0
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.933f,
                    dy1 = 0.0f,
                )
                // v -0.725
                verticalLineToRelative(dy = -0.725f)
                // l 0.628 0.363
                lineToRelative(dx = 0.628f, dy = 0.363f)
                // a 0.467 0.467 0 1 0 0.467 -0.808
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.467f,
                    dy1 = -0.808f,
                )
                // L 12.932 4
                lineTo(x = 12.932f, y = 4.0f)
                // l 0.628 -0.363
                lineToRelative(dx = 0.628f, dy = -0.363f)
                // a 0.467 0.467 0 0 0 -0.467 -0.808
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.467f,
                    dy1 = -0.808f,
                )
                // l -0.628 0.363
                lineToRelative(dx = -0.628f, dy = 0.363f)
                // v -0.725z
                verticalLineToRelative(dy = -0.725f)
                close()
            }
            // m8.675 1.872 3.15 -1.824 a.346 .346 0 0 1 .35 0 l3.15 1.824 a.353 .353 0 0 1 .175 .304 v3.648 a.352 .352 0 0 1 -.175 .305 l-3.15 1.824 a.344 .344 0 0 1 -.35 0 L8.676 6.129 a.351 .351 0 0 1 -.176 -.305 V2.176 a.353 .353 0 0 1 .175 -.304Z m.526 3.75 L12 7.241 l2.799 -1.62 V2.38 L12 .758 9.201 2.38 v3.242Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.675 1.872
                moveTo(x = 8.675f, y = 1.872f)
                // l 3.15 -1.824
                lineToRelative(dx = 3.15f, dy = -1.824f)
                // a 0.346 0.346 0 0 1 0.35 0
                arcToRelative(
                    a = 0.346f,
                    b = 0.346f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.35f,
                    dy1 = 0.0f,
                )
                // l 3.15 1.824
                lineToRelative(dx = 3.15f, dy = 1.824f)
                // a 0.353 0.353 0 0 1 0.175 0.304
                arcToRelative(
                    a = 0.353f,
                    b = 0.353f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.175f,
                    dy1 = 0.304f,
                )
                // v 3.648
                verticalLineToRelative(dy = 3.648f)
                // a 0.352 0.352 0 0 1 -0.175 0.305
                arcToRelative(
                    a = 0.352f,
                    b = 0.352f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.175f,
                    dy1 = 0.305f,
                )
                // l -3.15 1.824
                lineToRelative(dx = -3.15f, dy = 1.824f)
                // a 0.344 0.344 0 0 1 -0.35 0
                arcToRelative(
                    a = 0.344f,
                    b = 0.344f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.35f,
                    dy1 = 0.0f,
                )
                // L 8.676 6.129
                lineTo(x = 8.676f, y = 6.129f)
                // a 0.351 0.351 0 0 1 -0.176 -0.305
                arcToRelative(
                    a = 0.351f,
                    b = 0.351f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.176f,
                    dy1 = -0.305f,
                )
                // V 2.176
                verticalLineTo(y = 2.176f)
                // a 0.353 0.353 0 0 1 0.175 -0.304z
                arcToRelative(
                    a = 0.353f,
                    b = 0.353f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.175f,
                    dy1 = -0.304f,
                )
                close()
                // m 0.526 3.75
                moveToRelative(dx = 0.526f, dy = 3.75f)
                // L 12 7.241
                lineTo(x = 12.0f, y = 7.241f)
                // l 2.799 -1.62
                lineToRelative(dx = 2.799f, dy = -1.62f)
                // V 2.38
                verticalLineTo(y = 2.38f)
                // L 12 0.758
                lineTo(x = 12.0f, y = 0.758f)
                // L 9.201 2.38
                lineTo(x = 9.201f, y = 2.38f)
                // v 3.242z
                verticalLineToRelative(dy = 3.242f)
                close()
            }
        }.build().also { _ic1086 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1086: ImageVector? = null
