package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1021: ImageVector
    get() {
        val current = _ic1021
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1021",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M16 4 6.5 9 l3.5 3 -6 4 h9 l2.5 -5 L11 9 l5 -3 V4Z m-1.45 1.878 -.1 -.173 .5 -.291 .1 .172 -.5 .292Z m-2 1.167 -.1 -.173 1 -.583 .1 .172 -1 .584Z m-2 1.166 -.1 -.172 1 -.584 .1 .173 -1 .583Z m-1 .584 -.376 .22 .265 .22 -.128 .154 -.485 -.403 .624 -.364 .1 .173Z m1.389 1.69 -.128 .154 -.75 -.625 .128 -.153 .75 .625Z m1.196 .998 -.366 .513 -.163 -.117 .259 -.362 -.304 -.253 .128 -.153 .446 .372Z m-1.616 2.263 -.163 -.117 .625 -.875 .163 .117 -.625 .875Z m-.938 1.312 -.162 -.116 .312 -.438 .163 .117 -.313 .437Z M3.535 2.467 a.467 .467 0 0 1 .933 0 v.725 l.628 -.363 a.467 .467 0 1 1 .467 .808 L4.935 4 l.628 .363 a.467 .467 0 0 1 -.467 .808 l-.628 -.363 v.725 a.467 .467 0 1 1 -.933 0 v-.725 l-.628 .363 a.467 .467 0 0 1 -.467 -.808 L3.068 4 l-.628 -.363 a.467 .467 0 1 1 .467 -.808 l.628 .363 v-.725Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 16 4
                moveTo(x = 16.0f, y = 4.0f)
                // L 6.5 9
                lineTo(x = 6.5f, y = 9.0f)
                // l 3.5 3
                lineToRelative(dx = 3.5f, dy = 3.0f)
                // l -6 4
                lineToRelative(dx = -6.0f, dy = 4.0f)
                // h 9
                horizontalLineToRelative(dx = 9.0f)
                // l 2.5 -5
                lineToRelative(dx = 2.5f, dy = -5.0f)
                // L 11 9
                lineTo(x = 11.0f, y = 9.0f)
                // l 5 -3
                lineToRelative(dx = 5.0f, dy = -3.0f)
                // V 4z
                verticalLineTo(y = 4.0f)
                close()
                // m -1.45 1.878
                moveToRelative(dx = -1.45f, dy = 1.878f)
                // l -0.1 -0.173
                lineToRelative(dx = -0.1f, dy = -0.173f)
                // l 0.5 -0.291
                lineToRelative(dx = 0.5f, dy = -0.291f)
                // l 0.1 0.172
                lineToRelative(dx = 0.1f, dy = 0.172f)
                // l -0.5 0.292z
                lineToRelative(dx = -0.5f, dy = 0.292f)
                close()
                // m -2 1.167
                moveToRelative(dx = -2.0f, dy = 1.167f)
                // l -0.1 -0.173
                lineToRelative(dx = -0.1f, dy = -0.173f)
                // l 1 -0.583
                lineToRelative(dx = 1.0f, dy = -0.583f)
                // l 0.1 0.172
                lineToRelative(dx = 0.1f, dy = 0.172f)
                // l -1 0.584z
                lineToRelative(dx = -1.0f, dy = 0.584f)
                close()
                // m -2 1.166
                moveToRelative(dx = -2.0f, dy = 1.166f)
                // l -0.1 -0.172
                lineToRelative(dx = -0.1f, dy = -0.172f)
                // l 1 -0.584
                lineToRelative(dx = 1.0f, dy = -0.584f)
                // l 0.1 0.173
                lineToRelative(dx = 0.1f, dy = 0.173f)
                // l -1 0.583z
                lineToRelative(dx = -1.0f, dy = 0.583f)
                close()
                // m -1 0.584
                moveToRelative(dx = -1.0f, dy = 0.584f)
                // l -0.376 0.22
                lineToRelative(dx = -0.376f, dy = 0.22f)
                // l 0.265 0.22
                lineToRelative(dx = 0.265f, dy = 0.22f)
                // l -0.128 0.154
                lineToRelative(dx = -0.128f, dy = 0.154f)
                // l -0.485 -0.403
                lineToRelative(dx = -0.485f, dy = -0.403f)
                // l 0.624 -0.364
                lineToRelative(dx = 0.624f, dy = -0.364f)
                // l 0.1 0.173z
                lineToRelative(dx = 0.1f, dy = 0.173f)
                close()
                // m 1.389 1.69
                moveToRelative(dx = 1.389f, dy = 1.69f)
                // l -0.128 0.154
                lineToRelative(dx = -0.128f, dy = 0.154f)
                // l -0.75 -0.625
                lineToRelative(dx = -0.75f, dy = -0.625f)
                // l 0.128 -0.153
                lineToRelative(dx = 0.128f, dy = -0.153f)
                // l 0.75 0.625z
                lineToRelative(dx = 0.75f, dy = 0.625f)
                close()
                // m 1.196 0.998
                moveToRelative(dx = 1.196f, dy = 0.998f)
                // l -0.366 0.513
                lineToRelative(dx = -0.366f, dy = 0.513f)
                // l -0.163 -0.117
                lineToRelative(dx = -0.163f, dy = -0.117f)
                // l 0.259 -0.362
                lineToRelative(dx = 0.259f, dy = -0.362f)
                // l -0.304 -0.253
                lineToRelative(dx = -0.304f, dy = -0.253f)
                // l 0.128 -0.153
                lineToRelative(dx = 0.128f, dy = -0.153f)
                // l 0.446 0.372z
                lineToRelative(dx = 0.446f, dy = 0.372f)
                close()
                // m -1.616 2.263
                moveToRelative(dx = -1.616f, dy = 2.263f)
                // l -0.163 -0.117
                lineToRelative(dx = -0.163f, dy = -0.117f)
                // l 0.625 -0.875
                lineToRelative(dx = 0.625f, dy = -0.875f)
                // l 0.163 0.117
                lineToRelative(dx = 0.163f, dy = 0.117f)
                // l -0.625 0.875z
                lineToRelative(dx = -0.625f, dy = 0.875f)
                close()
                // m -0.938 1.312
                moveToRelative(dx = -0.938f, dy = 1.312f)
                // l -0.162 -0.116
                lineToRelative(dx = -0.162f, dy = -0.116f)
                // l 0.312 -0.438
                lineToRelative(dx = 0.312f, dy = -0.438f)
                // l 0.163 0.117
                lineToRelative(dx = 0.163f, dy = 0.117f)
                // l -0.313 0.437z
                lineToRelative(dx = -0.313f, dy = 0.437f)
                close()
                // M 3.535 2.467
                moveTo(x = 3.535f, y = 2.467f)
                // a 0.467 0.467 0 0 1 0.933 0
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.933f,
                    dy1 = 0.0f,
                )
                // v 0.725
                verticalLineToRelative(dy = 0.725f)
                // l 0.628 -0.363
                lineToRelative(dx = 0.628f, dy = -0.363f)
                // a 0.467 0.467 0 1 1 0.467 0.808
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.467f,
                    dy1 = 0.808f,
                )
                // L 4.935 4
                lineTo(x = 4.935f, y = 4.0f)
                // l 0.628 0.363
                lineToRelative(dx = 0.628f, dy = 0.363f)
                // a 0.467 0.467 0 0 1 -0.467 0.808
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.467f,
                    dy1 = 0.808f,
                )
                // l -0.628 -0.363
                lineToRelative(dx = -0.628f, dy = -0.363f)
                // v 0.725
                verticalLineToRelative(dy = 0.725f)
                // a 0.467 0.467 0 1 1 -0.933 0
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.933f,
                    dy1 = 0.0f,
                )
                // v -0.725
                verticalLineToRelative(dy = -0.725f)
                // l -0.628 0.363
                lineToRelative(dx = -0.628f, dy = 0.363f)
                // a 0.467 0.467 0 0 1 -0.467 -0.808
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.467f,
                    dy1 = -0.808f,
                )
                // L 3.068 4
                lineTo(x = 3.068f, y = 4.0f)
                // l -0.628 -0.363
                lineToRelative(dx = -0.628f, dy = -0.363f)
                // a 0.467 0.467 0 1 1 0.467 -0.808
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.467f,
                    dy1 = -0.808f,
                )
                // l 0.628 0.363
                lineToRelative(dx = 0.628f, dy = 0.363f)
                // v -0.725z
                verticalLineToRelative(dy = -0.725f)
                close()
            }
            // M7.324 1.872 4.175 .048 a.347 .347 0 0 0 -.35 0 L.675 1.872 a.352 .352 0 0 0 -.175 .304 v3.648 c0 .126 .067 .242 .175 .305 l3.15 1.824 a.344 .344 0 0 0 .35 0 l3.149 -1.824 a.351 .351 0 0 0 .176 -.305 V2.176 a.353 .353 0 0 0 -.176 -.304Z m-.526 3.75 L4 7.241 l-2.798 -1.62 V2.38 L4 .758 6.798 2.38 v3.242Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.324 1.872
                moveTo(x = 7.324f, y = 1.872f)
                // L 4.175 0.048
                lineTo(x = 4.175f, y = 0.048f)
                // a 0.347 0.347 0 0 0 -0.35 0
                arcToRelative(
                    a = 0.347f,
                    b = 0.347f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = 0.0f,
                )
                // L 0.675 1.872
                lineTo(x = 0.675f, y = 1.872f)
                // a 0.352 0.352 0 0 0 -0.175 0.304
                arcToRelative(
                    a = 0.352f,
                    b = 0.352f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.175f,
                    dy1 = 0.304f,
                )
                // v 3.648
                verticalLineToRelative(dy = 3.648f)
                // c 0 0.126 0.067 0.242 0.175 0.305
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.126f,
                    dx2 = 0.067f,
                    dy2 = 0.242f,
                    dx3 = 0.175f,
                    dy3 = 0.305f,
                )
                // l 3.15 1.824
                lineToRelative(dx = 3.15f, dy = 1.824f)
                // a 0.344 0.344 0 0 0 0.35 0
                arcToRelative(
                    a = 0.344f,
                    b = 0.344f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.35f,
                    dy1 = 0.0f,
                )
                // l 3.149 -1.824
                lineToRelative(dx = 3.149f, dy = -1.824f)
                // a 0.351 0.351 0 0 0 0.176 -0.305
                arcToRelative(
                    a = 0.351f,
                    b = 0.351f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.176f,
                    dy1 = -0.305f,
                )
                // V 2.176
                verticalLineTo(y = 2.176f)
                // a 0.353 0.353 0 0 0 -0.176 -0.304z
                arcToRelative(
                    a = 0.353f,
                    b = 0.353f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.176f,
                    dy1 = -0.304f,
                )
                close()
                // m -0.526 3.75
                moveToRelative(dx = -0.526f, dy = 3.75f)
                // L 4 7.241
                lineTo(x = 4.0f, y = 7.241f)
                // l -2.798 -1.62
                lineToRelative(dx = -2.798f, dy = -1.62f)
                // V 2.38
                verticalLineTo(y = 2.38f)
                // L 4 0.758
                lineTo(x = 4.0f, y = 0.758f)
                // L 6.798 2.38
                lineTo(x = 6.798f, y = 2.38f)
                // v 3.242z
                verticalLineToRelative(dy = 3.242f)
                close()
            }
        }.build().also { _ic1021 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1021: ImageVector? = null
