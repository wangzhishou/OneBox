package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1078: ImageVector
    get() {
        val current = _ic1078
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1078",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M12.87 .884 a.188 .188 0 0 0 .32 -.133 V.19 a.187 .187 0 1 0 -.374 0 v.56 c0 .05 .02 .098 .055 .133Z m-1.53 .698 a.187 .187 0 0 0 .205 -.306 l-.398 -.398 a.188 .188 0 0 0 -.265 .265 l.398 .398 a.185 .185 0 0 0 .06 .04Z m-1.151 1.604 h.562 a.188 .188 0 0 0 0 -.375 h-.562 a.188 .188 0 1 0 0 .375Z m.717 1.958 a.188 .188 0 0 0 .237 -.024 l.398 -.397 a.188 .188 0 0 0 -.265 -.266 l-.398 .398 a.188 .188 0 0 0 .028 .289Z m1.96 .802 a.188 .188 0 0 0 .32 -.132 V5.25 a.187 .187 0 1 0 -.375 0 v.563 c0 .05 .02 .097 .055 .132Z m2.05 -.781 a.187 .187 0 0 0 .255 -.21 .188 .188 0 0 0 -.051 -.096 l-.398 -.398 a.187 .187 0 0 0 -.27 -.005 .188 .188 0 0 0 .005 .27 l.398 .398 a.187 .187 0 0 0 .06 .04Z m.334 -1.974 h.563 a.188 .188 0 1 0 0 -.375 h-.562 a.188 .188 0 0 0 0 .375Z m-.76 -1.623 a.188 .188 0 0 0 .236 -.023 l.398 -.398 a.19 .19 0 0 0 .04 -.204 .188 .188 0 0 0 -.306 -.061 l-.397 .398 a.188 .188 0 0 0 .028 .288Z m-2.426 .029 a1.688 1.688 0 1 1 1.875 2.806 1.688 1.688 0 0 1 -1.875 -2.806Z m1.667 .312 a1.312 1.312 0 1 0 -1.458 2.182 A1.312 1.312 0 0 0 13.73 1.91Z M2.032 6.364 a.75 .75 0 0 1 1.054 .117 l2 2.5 a.75 .75 0 0 1 -.056 1 l-1.565 1.565 L5.101 14 H7.8 l-1.423 -2.134 a.75 .75 0 0 1 .094 -.946 l1.525 -1.526 -1.58 -1.976 a.75 .75 0 0 1 1.17 -.937 l2 2.5 a.75 .75 0 0 1 -.055 1 l-1.565 1.565 L9.601 14 H12.3 l-1.423 -2.134 a.75 .75 0 0 1 .094 -.946 l1.525 -1.526 -1.58 -1.976 a.75 .75 0 1 1 1.17 -.937 l2 2.5 a.75 .75 0 0 1 -.054 1 l-1.566 1.565 L14.102 14 H15 a1 1 0 0 1 0 2 H1 a1 1 0 1 1 0 -2 h2.299 l-1.423 -2.134 a.75 .75 0 0 1 .094 -.946 l1.525 -1.526 -1.58 -1.976 a.75 .75 0 0 1 .117 -1.054Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 12.87 0.884
                moveTo(x = 12.87f, y = 0.884f)
                // a 0.188 0.188 0 0 0 0.32 -0.133
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.32f,
                    dy1 = -0.133f,
                )
                // V 0.19
                verticalLineTo(y = 0.19f)
                // a 0.187 0.187 0 1 0 -0.374 0
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.374f,
                    dy1 = 0.0f,
                )
                // v 0.56
                verticalLineToRelative(dy = 0.56f)
                // c 0 0.05 0.02 0.098 0.055 0.133z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.05f,
                    dx2 = 0.02f,
                    dy2 = 0.098f,
                    dx3 = 0.055f,
                    dy3 = 0.133f,
                )
                close()
                // m -1.53 0.698
                moveToRelative(dx = -1.53f, dy = 0.698f)
                // a 0.187 0.187 0 0 0 0.205 -0.306
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.205f,
                    dy1 = -0.306f,
                )
                // l -0.398 -0.398
                lineToRelative(dx = -0.398f, dy = -0.398f)
                // a 0.188 0.188 0 0 0 -0.265 0.265
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.265f,
                    dy1 = 0.265f,
                )
                // l 0.398 0.398
                lineToRelative(dx = 0.398f, dy = 0.398f)
                // a 0.185 0.185 0 0 0 0.06 0.04z
                arcToRelative(
                    a = 0.185f,
                    b = 0.185f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.06f,
                    dy1 = 0.04f,
                )
                close()
                // m -1.151 1.604
                moveToRelative(dx = -1.151f, dy = 1.604f)
                // h 0.562
                horizontalLineToRelative(dx = 0.562f)
                // a 0.188 0.188 0 0 0 0 -0.375
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.375f,
                )
                // h -0.562
                horizontalLineToRelative(dx = -0.562f)
                // a 0.188 0.188 0 1 0 0 0.375z
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.375f,
                )
                close()
                // m 0.717 1.958
                moveToRelative(dx = 0.717f, dy = 1.958f)
                // a 0.188 0.188 0 0 0 0.237 -0.024
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.237f,
                    dy1 = -0.024f,
                )
                // l 0.398 -0.397
                lineToRelative(dx = 0.398f, dy = -0.397f)
                // a 0.188 0.188 0 0 0 -0.265 -0.266
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.265f,
                    dy1 = -0.266f,
                )
                // l -0.398 0.398
                lineToRelative(dx = -0.398f, dy = 0.398f)
                // a 0.188 0.188 0 0 0 0.028 0.289z
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.028f,
                    dy1 = 0.289f,
                )
                close()
                // m 1.96 0.802
                moveToRelative(dx = 1.96f, dy = 0.802f)
                // a 0.188 0.188 0 0 0 0.32 -0.132
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.32f,
                    dy1 = -0.132f,
                )
                // V 5.25
                verticalLineTo(y = 5.25f)
                // a 0.187 0.187 0 1 0 -0.375 0
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.375f,
                    dy1 = 0.0f,
                )
                // v 0.563
                verticalLineToRelative(dy = 0.563f)
                // c 0 0.05 0.02 0.097 0.055 0.132z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.05f,
                    dx2 = 0.02f,
                    dy2 = 0.097f,
                    dx3 = 0.055f,
                    dy3 = 0.132f,
                )
                close()
                // m 2.05 -0.781
                moveToRelative(dx = 2.05f, dy = -0.781f)
                // a 0.187 0.187 0 0 0 0.255 -0.21
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.255f,
                    dy1 = -0.21f,
                )
                // a 0.188 0.188 0 0 0 -0.051 -0.096
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.051f,
                    dy1 = -0.096f,
                )
                // l -0.398 -0.398
                lineToRelative(dx = -0.398f, dy = -0.398f)
                // a 0.187 0.187 0 0 0 -0.27 -0.005
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.27f,
                    dy1 = -0.005f,
                )
                // a 0.188 0.188 0 0 0 0.005 0.27
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.005f,
                    dy1 = 0.27f,
                )
                // l 0.398 0.398
                lineToRelative(dx = 0.398f, dy = 0.398f)
                // a 0.187 0.187 0 0 0 0.06 0.04z
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.06f,
                    dy1 = 0.04f,
                )
                close()
                // m 0.334 -1.974
                moveToRelative(dx = 0.334f, dy = -1.974f)
                // h 0.563
                horizontalLineToRelative(dx = 0.563f)
                // a 0.188 0.188 0 1 0 0 -0.375
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.375f,
                )
                // h -0.562
                horizontalLineToRelative(dx = -0.562f)
                // a 0.188 0.188 0 0 0 0 0.375z
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.375f,
                )
                close()
                // m -0.76 -1.623
                moveToRelative(dx = -0.76f, dy = -1.623f)
                // a 0.188 0.188 0 0 0 0.236 -0.023
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.236f,
                    dy1 = -0.023f,
                )
                // l 0.398 -0.398
                lineToRelative(dx = 0.398f, dy = -0.398f)
                // a 0.19 0.19 0 0 0 0.04 -0.204
                arcToRelative(
                    a = 0.19f,
                    b = 0.19f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.04f,
                    dy1 = -0.204f,
                )
                // a 0.188 0.188 0 0 0 -0.306 -0.061
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.306f,
                    dy1 = -0.061f,
                )
                // l -0.397 0.398
                lineToRelative(dx = -0.397f, dy = 0.398f)
                // a 0.188 0.188 0 0 0 0.028 0.288z
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.028f,
                    dy1 = 0.288f,
                )
                close()
                // m -2.426 0.029
                moveToRelative(dx = -2.426f, dy = 0.029f)
                // a 1.688 1.688 0 1 1 1.875 2.806
                arcToRelative(
                    a = 1.688f,
                    b = 1.688f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 1.875f,
                    dy1 = 2.806f,
                )
                // a 1.688 1.688 0 0 1 -1.875 -2.806z
                arcToRelative(
                    a = 1.688f,
                    b = 1.688f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.875f,
                    dy1 = -2.806f,
                )
                close()
                // m 1.667 0.312
                moveToRelative(dx = 1.667f, dy = 0.312f)
                // a 1.312 1.312 0 1 0 -1.458 2.182
                arcToRelative(
                    a = 1.312f,
                    b = 1.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -1.458f,
                    dy1 = 2.182f,
                )
                // A 1.312 1.312 0 0 0 13.73 1.91z
                arcTo(
                    horizontalEllipseRadius = 1.312f,
                    verticalEllipseRadius = 1.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 13.73f,
                    y1 = 1.91f,
                )
                close()
                // M 2.032 6.364
                moveTo(x = 2.032f, y = 6.364f)
                // a 0.75 0.75 0 0 1 1.054 0.117
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.054f,
                    dy1 = 0.117f,
                )
                // l 2 2.5
                lineToRelative(dx = 2.0f, dy = 2.5f)
                // a 0.75 0.75 0 0 1 -0.056 1
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.056f,
                    dy1 = 1.0f,
                )
                // l -1.565 1.565
                lineToRelative(dx = -1.565f, dy = 1.565f)
                // L 5.101 14
                lineTo(x = 5.101f, y = 14.0f)
                // H 7.8
                horizontalLineTo(x = 7.8f)
                // l -1.423 -2.134
                lineToRelative(dx = -1.423f, dy = -2.134f)
                // a 0.75 0.75 0 0 1 0.094 -0.946
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.094f,
                    dy1 = -0.946f,
                )
                // l 1.525 -1.526
                lineToRelative(dx = 1.525f, dy = -1.526f)
                // l -1.58 -1.976
                lineToRelative(dx = -1.58f, dy = -1.976f)
                // a 0.75 0.75 0 0 1 1.17 -0.937
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.17f,
                    dy1 = -0.937f,
                )
                // l 2 2.5
                lineToRelative(dx = 2.0f, dy = 2.5f)
                // a 0.75 0.75 0 0 1 -0.055 1
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.055f,
                    dy1 = 1.0f,
                )
                // l -1.565 1.565
                lineToRelative(dx = -1.565f, dy = 1.565f)
                // L 9.601 14
                lineTo(x = 9.601f, y = 14.0f)
                // H 12.3
                horizontalLineTo(x = 12.3f)
                // l -1.423 -2.134
                lineToRelative(dx = -1.423f, dy = -2.134f)
                // a 0.75 0.75 0 0 1 0.094 -0.946
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.094f,
                    dy1 = -0.946f,
                )
                // l 1.525 -1.526
                lineToRelative(dx = 1.525f, dy = -1.526f)
                // l -1.58 -1.976
                lineToRelative(dx = -1.58f, dy = -1.976f)
                // a 0.75 0.75 0 1 1 1.17 -0.937
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 1.17f,
                    dy1 = -0.937f,
                )
                // l 2 2.5
                lineToRelative(dx = 2.0f, dy = 2.5f)
                // a 0.75 0.75 0 0 1 -0.054 1
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.054f,
                    dy1 = 1.0f,
                )
                // l -1.566 1.565
                lineToRelative(dx = -1.566f, dy = 1.565f)
                // L 14.102 14
                lineTo(x = 14.102f, y = 14.0f)
                // H 15
                horizontalLineTo(x = 15.0f)
                // a 1 1 0 0 1 0 2
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 2.0f,
                )
                // H 1
                horizontalLineTo(x = 1.0f)
                // a 1 1 0 1 1 0 -2
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -2.0f,
                )
                // h 2.299
                horizontalLineToRelative(dx = 2.299f)
                // l -1.423 -2.134
                lineToRelative(dx = -1.423f, dy = -2.134f)
                // a 0.75 0.75 0 0 1 0.094 -0.946
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.094f,
                    dy1 = -0.946f,
                )
                // l 1.525 -1.526
                lineToRelative(dx = 1.525f, dy = -1.526f)
                // l -1.58 -1.976
                lineToRelative(dx = -1.58f, dy = -1.976f)
                // a 0.75 0.75 0 0 1 0.117 -1.054z
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.117f,
                    dy1 = -1.054f,
                )
                close()
            }
        }.build().also { _ic1078 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1078: ImageVector? = null
