package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1803: ImageVector
    get() {
        val current = _ic1803
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1803",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.916 2.053 a4.171 4.171 0 0 0 -.465 .562 6.445 6.445 0 0 1 5.008 .36 6.537 6.537 0 0 1 2.412 2.116 A6.64 6.64 0 0 1 16 8.11 a4.16 4.16 0 0 0 -.442 -1.209 4.17 4.17 0 0 0 -1.063 -1.227 4.115 4.115 0 0 0 -2.158 -.85 c.481 .43 .899 .931 1.238 1.488 a6.66 6.66 0 0 1 .352 6.253 6.576 6.576 0 0 1 -2.02 2.5 c.32 -.285 .594 -.62 .813 -.99 a4.218 4.218 0 0 0 .164 -3.867 6.643 6.643 0 0 1 -.654 1.832 6.564 6.564 0 0 1 -2.159 2.379 6.446 6.446 0 0 1 -6.166 .535 4.04 4.04 0 0 0 1.256 .218 4.11 4.11 0 0 0 3.388 -1.786 6.445 6.445 0 0 1 -5.008 -.36 6.538 6.538 0 0 1 -2.412 -2.116 A6.64 6.64 0 0 1 0 7.89 c.084 .423 .233 .83 .442 1.207 a4.17 4.17 0 0 0 1.063 1.228 4.117 4.117 0 0 0 2.16 .849 c-.482 -.43 -.9 -.93 -1.24 -1.488 a6.66 6.66 0 0 1 -.353 -6.253 6.576 6.576 0 0 1 2.022 -2.5 4.13 4.13 0 0 0 -.814 .991 A4.218 4.218 0 0 0 2.867 5.1 c.063 .237 .146 .468 .248 .69 a6.643 6.643 0 0 1 .655 -1.831 6.564 6.564 0 0 1 2.158 -2.38 6.446 6.446 0 0 1 6.167 -.534 4.042 4.042 0 0 0 -1.256 -.218 4.11 4.11 0 0 0 -2.923 1.226Z M8 10 a2 2 0 1 0 0 -4 2 2 0 0 0 0 4Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.916 2.053
                moveTo(x = 7.916f, y = 2.053f)
                // a 4.171 4.171 0 0 0 -0.465 0.562
                arcToRelative(
                    a = 4.171f,
                    b = 4.171f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.465f,
                    dy1 = 0.562f,
                )
                // a 6.445 6.445 0 0 1 5.008 0.36
                arcToRelative(
                    a = 6.445f,
                    b = 6.445f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 5.008f,
                    dy1 = 0.36f,
                )
                // a 6.537 6.537 0 0 1 2.412 2.116
                arcToRelative(
                    a = 6.537f,
                    b = 6.537f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.412f,
                    dy1 = 2.116f,
                )
                // A 6.64 6.64 0 0 1 16 8.11
                arcTo(
                    horizontalEllipseRadius = 6.64f,
                    verticalEllipseRadius = 6.64f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 16.0f,
                    y1 = 8.11f,
                )
                // a 4.16 4.16 0 0 0 -0.442 -1.209
                arcToRelative(
                    a = 4.16f,
                    b = 4.16f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.442f,
                    dy1 = -1.209f,
                )
                // a 4.17 4.17 0 0 0 -1.063 -1.227
                arcToRelative(
                    a = 4.17f,
                    b = 4.17f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.063f,
                    dy1 = -1.227f,
                )
                // a 4.115 4.115 0 0 0 -2.158 -0.85
                arcToRelative(
                    a = 4.115f,
                    b = 4.115f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.158f,
                    dy1 = -0.85f,
                )
                // c 0.481 0.43 0.899 0.931 1.238 1.488
                curveToRelative(
                    dx1 = 0.481f,
                    dy1 = 0.43f,
                    dx2 = 0.899f,
                    dy2 = 0.931f,
                    dx3 = 1.238f,
                    dy3 = 1.488f,
                )
                // a 6.66 6.66 0 0 1 0.352 6.253
                arcToRelative(
                    a = 6.66f,
                    b = 6.66f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.352f,
                    dy1 = 6.253f,
                )
                // a 6.576 6.576 0 0 1 -2.02 2.5
                arcToRelative(
                    a = 6.576f,
                    b = 6.576f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.02f,
                    dy1 = 2.5f,
                )
                // c 0.32 -0.285 0.594 -0.62 0.813 -0.99
                curveToRelative(
                    dx1 = 0.32f,
                    dy1 = -0.285f,
                    dx2 = 0.594f,
                    dy2 = -0.62f,
                    dx3 = 0.813f,
                    dy3 = -0.99f,
                )
                // a 4.218 4.218 0 0 0 0.164 -3.867
                arcToRelative(
                    a = 4.218f,
                    b = 4.218f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.164f,
                    dy1 = -3.867f,
                )
                // a 6.643 6.643 0 0 1 -0.654 1.832
                arcToRelative(
                    a = 6.643f,
                    b = 6.643f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.654f,
                    dy1 = 1.832f,
                )
                // a 6.564 6.564 0 0 1 -2.159 2.379
                arcToRelative(
                    a = 6.564f,
                    b = 6.564f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.159f,
                    dy1 = 2.379f,
                )
                // a 6.446 6.446 0 0 1 -6.166 0.535
                arcToRelative(
                    a = 6.446f,
                    b = 6.446f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -6.166f,
                    dy1 = 0.535f,
                )
                // a 4.04 4.04 0 0 0 1.256 0.218
                arcToRelative(
                    a = 4.04f,
                    b = 4.04f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.256f,
                    dy1 = 0.218f,
                )
                // a 4.11 4.11 0 0 0 3.388 -1.786
                arcToRelative(
                    a = 4.11f,
                    b = 4.11f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 3.388f,
                    dy1 = -1.786f,
                )
                // a 6.445 6.445 0 0 1 -5.008 -0.36
                arcToRelative(
                    a = 6.445f,
                    b = 6.445f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -5.008f,
                    dy1 = -0.36f,
                )
                // a 6.538 6.538 0 0 1 -2.412 -2.116
                arcToRelative(
                    a = 6.538f,
                    b = 6.538f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.412f,
                    dy1 = -2.116f,
                )
                // A 6.64 6.64 0 0 1 0 7.89
                arcTo(
                    horizontalEllipseRadius = 6.64f,
                    verticalEllipseRadius = 6.64f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 0.0f,
                    y1 = 7.89f,
                )
                // c 0.084 0.423 0.233 0.83 0.442 1.207
                curveToRelative(
                    dx1 = 0.084f,
                    dy1 = 0.423f,
                    dx2 = 0.233f,
                    dy2 = 0.83f,
                    dx3 = 0.442f,
                    dy3 = 1.207f,
                )
                // a 4.17 4.17 0 0 0 1.063 1.228
                arcToRelative(
                    a = 4.17f,
                    b = 4.17f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.063f,
                    dy1 = 1.228f,
                )
                // a 4.117 4.117 0 0 0 2.16 0.849
                arcToRelative(
                    a = 4.117f,
                    b = 4.117f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.16f,
                    dy1 = 0.849f,
                )
                // c -0.482 -0.43 -0.9 -0.93 -1.24 -1.488
                curveToRelative(
                    dx1 = -0.482f,
                    dy1 = -0.43f,
                    dx2 = -0.9f,
                    dy2 = -0.93f,
                    dx3 = -1.24f,
                    dy3 = -1.488f,
                )
                // a 6.66 6.66 0 0 1 -0.353 -6.253
                arcToRelative(
                    a = 6.66f,
                    b = 6.66f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.353f,
                    dy1 = -6.253f,
                )
                // a 6.576 6.576 0 0 1 2.022 -2.5
                arcToRelative(
                    a = 6.576f,
                    b = 6.576f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.022f,
                    dy1 = -2.5f,
                )
                // a 4.13 4.13 0 0 0 -0.814 0.991
                arcToRelative(
                    a = 4.13f,
                    b = 4.13f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.814f,
                    dy1 = 0.991f,
                )
                // A 4.218 4.218 0 0 0 2.867 5.1
                arcTo(
                    horizontalEllipseRadius = 4.218f,
                    verticalEllipseRadius = 4.218f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 2.867f,
                    y1 = 5.1f,
                )
                // c 0.063 0.237 0.146 0.468 0.248 0.69
                curveToRelative(
                    dx1 = 0.063f,
                    dy1 = 0.237f,
                    dx2 = 0.146f,
                    dy2 = 0.468f,
                    dx3 = 0.248f,
                    dy3 = 0.69f,
                )
                // a 6.643 6.643 0 0 1 0.655 -1.831
                arcToRelative(
                    a = 6.643f,
                    b = 6.643f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.655f,
                    dy1 = -1.831f,
                )
                // a 6.564 6.564 0 0 1 2.158 -2.38
                arcToRelative(
                    a = 6.564f,
                    b = 6.564f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.158f,
                    dy1 = -2.38f,
                )
                // a 6.446 6.446 0 0 1 6.167 -0.534
                arcToRelative(
                    a = 6.446f,
                    b = 6.446f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 6.167f,
                    dy1 = -0.534f,
                )
                // a 4.042 4.042 0 0 0 -1.256 -0.218
                arcToRelative(
                    a = 4.042f,
                    b = 4.042f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.256f,
                    dy1 = -0.218f,
                )
                // a 4.11 4.11 0 0 0 -2.923 1.226z
                arcToRelative(
                    a = 4.11f,
                    b = 4.11f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.923f,
                    dy1 = 1.226f,
                )
                close()
                // M 8 10
                moveTo(x = 8.0f, y = 10.0f)
                // a 2 2 0 1 0 0 -4
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -4.0f,
                )
                // a 2 2 0 0 0 0 4z
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 4.0f,
                )
                close()
            }
        }.build().also { _ic1803 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1803: ImageVector? = null
