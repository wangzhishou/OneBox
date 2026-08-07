package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2120: ImageVector
    get() {
        val current = _ic2120
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2120",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M9.08 9.375 a.855 .855 0 0 0 -.701 .321 c-.173 .214 -.26 .485 -.26 .813 0 .33 .086 .602 .258 .814 .172 .213 .406 .319 .702 .319 .299 0 .533 -.106 .704 -.317 .171 -.211 .256 -.484 .256 -.816 0 -.337 -.085 -.61 -.255 -.819 -.168 -.211 -.403 -.315 -.704 -.315Z m0 1.855 c-.314 0 -.472 -.241 -.472 -.723 s.157 -.723 .472 -.723 c.314 0 .472 .241 .472 .723 -.001 .483 -.158 .723 -.472 .723Z M7.012 8.18 c.171 -.211 .256 -.484 .256 -.816 0 -.337 -.085 -.61 -.254 -.819 -.17 -.209 -.405 -.314 -.706 -.314 a.855 .855 0 0 0 -.701 .321 c-.173 .214 -.26 .485 -.26 .813 0 .33 .086 .602 .258 .814 .172 .213 .406 .319 .702 .319 .299 0 .534 -.106 .705 -.318Z m-1.176 -.816 c0 -.482 .157 -.723 .472 -.723 .315 0 .472 .241 .472 .723 s-.157 .723 -.472 .723 c-.315 0 -.472 -.241 -.472 -.723Z m3.486 -1.091 a.29 .29 0 0 0 -.398 .097 l-2.955 4.831 a.29 .29 0 0 0 .389 .403 .285 .285 0 0 0 .105 -.102 l2.955 -4.831 a.289 .289 0 0 0 -.096 -.398Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 9.08 9.375
                moveTo(x = 9.08f, y = 9.375f)
                // a 0.855 0.855 0 0 0 -0.701 0.321
                arcToRelative(
                    a = 0.855f,
                    b = 0.855f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.701f,
                    dy1 = 0.321f,
                )
                // c -0.173 0.214 -0.26 0.485 -0.26 0.813
                curveToRelative(
                    dx1 = -0.173f,
                    dy1 = 0.214f,
                    dx2 = -0.26f,
                    dy2 = 0.485f,
                    dx3 = -0.26f,
                    dy3 = 0.813f,
                )
                // c 0 0.33 0.086 0.602 0.258 0.814
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.33f,
                    dx2 = 0.086f,
                    dy2 = 0.602f,
                    dx3 = 0.258f,
                    dy3 = 0.814f,
                )
                // c 0.172 0.213 0.406 0.319 0.702 0.319
                curveToRelative(
                    dx1 = 0.172f,
                    dy1 = 0.213f,
                    dx2 = 0.406f,
                    dy2 = 0.319f,
                    dx3 = 0.702f,
                    dy3 = 0.319f,
                )
                // c 0.299 0 0.533 -0.106 0.704 -0.317
                curveToRelative(
                    dx1 = 0.299f,
                    dy1 = 0.0f,
                    dx2 = 0.533f,
                    dy2 = -0.106f,
                    dx3 = 0.704f,
                    dy3 = -0.317f,
                )
                // c 0.171 -0.211 0.256 -0.484 0.256 -0.816
                curveToRelative(
                    dx1 = 0.171f,
                    dy1 = -0.211f,
                    dx2 = 0.256f,
                    dy2 = -0.484f,
                    dx3 = 0.256f,
                    dy3 = -0.816f,
                )
                // c 0 -0.337 -0.085 -0.61 -0.255 -0.819
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.337f,
                    dx2 = -0.085f,
                    dy2 = -0.61f,
                    dx3 = -0.255f,
                    dy3 = -0.819f,
                )
                // c -0.168 -0.211 -0.403 -0.315 -0.704 -0.315z
                curveToRelative(
                    dx1 = -0.168f,
                    dy1 = -0.211f,
                    dx2 = -0.403f,
                    dy2 = -0.315f,
                    dx3 = -0.704f,
                    dy3 = -0.315f,
                )
                close()
                // m 0 1.855
                moveToRelative(dx = 0.0f, dy = 1.855f)
                // c -0.314 0 -0.472 -0.241 -0.472 -0.723
                curveToRelative(
                    dx1 = -0.314f,
                    dy1 = 0.0f,
                    dx2 = -0.472f,
                    dy2 = -0.241f,
                    dx3 = -0.472f,
                    dy3 = -0.723f,
                )
                // s 0.157 -0.723 0.472 -0.723
                reflectiveCurveToRelative(
                    dx1 = 0.157f,
                    dy1 = -0.723f,
                    dx2 = 0.472f,
                    dy2 = -0.723f,
                )
                // c 0.314 0 0.472 0.241 0.472 0.723
                curveToRelative(
                    dx1 = 0.314f,
                    dy1 = 0.0f,
                    dx2 = 0.472f,
                    dy2 = 0.241f,
                    dx3 = 0.472f,
                    dy3 = 0.723f,
                )
                // c -0.001 0.483 -0.158 0.723 -0.472 0.723z
                curveToRelative(
                    dx1 = -0.001f,
                    dy1 = 0.483f,
                    dx2 = -0.158f,
                    dy2 = 0.723f,
                    dx3 = -0.472f,
                    dy3 = 0.723f,
                )
                close()
                // M 7.012 8.18
                moveTo(x = 7.012f, y = 8.18f)
                // c 0.171 -0.211 0.256 -0.484 0.256 -0.816
                curveToRelative(
                    dx1 = 0.171f,
                    dy1 = -0.211f,
                    dx2 = 0.256f,
                    dy2 = -0.484f,
                    dx3 = 0.256f,
                    dy3 = -0.816f,
                )
                // c 0 -0.337 -0.085 -0.61 -0.254 -0.819
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.337f,
                    dx2 = -0.085f,
                    dy2 = -0.61f,
                    dx3 = -0.254f,
                    dy3 = -0.819f,
                )
                // c -0.17 -0.209 -0.405 -0.314 -0.706 -0.314
                curveToRelative(
                    dx1 = -0.17f,
                    dy1 = -0.209f,
                    dx2 = -0.405f,
                    dy2 = -0.314f,
                    dx3 = -0.706f,
                    dy3 = -0.314f,
                )
                // a 0.855 0.855 0 0 0 -0.701 0.321
                arcToRelative(
                    a = 0.855f,
                    b = 0.855f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.701f,
                    dy1 = 0.321f,
                )
                // c -0.173 0.214 -0.26 0.485 -0.26 0.813
                curveToRelative(
                    dx1 = -0.173f,
                    dy1 = 0.214f,
                    dx2 = -0.26f,
                    dy2 = 0.485f,
                    dx3 = -0.26f,
                    dy3 = 0.813f,
                )
                // c 0 0.33 0.086 0.602 0.258 0.814
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.33f,
                    dx2 = 0.086f,
                    dy2 = 0.602f,
                    dx3 = 0.258f,
                    dy3 = 0.814f,
                )
                // c 0.172 0.213 0.406 0.319 0.702 0.319
                curveToRelative(
                    dx1 = 0.172f,
                    dy1 = 0.213f,
                    dx2 = 0.406f,
                    dy2 = 0.319f,
                    dx3 = 0.702f,
                    dy3 = 0.319f,
                )
                // c 0.299 0 0.534 -0.106 0.705 -0.318z
                curveToRelative(
                    dx1 = 0.299f,
                    dy1 = 0.0f,
                    dx2 = 0.534f,
                    dy2 = -0.106f,
                    dx3 = 0.705f,
                    dy3 = -0.318f,
                )
                close()
                // m -1.176 -0.816
                moveToRelative(dx = -1.176f, dy = -0.816f)
                // c 0 -0.482 0.157 -0.723 0.472 -0.723
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.482f,
                    dx2 = 0.157f,
                    dy2 = -0.723f,
                    dx3 = 0.472f,
                    dy3 = -0.723f,
                )
                // c 0.315 0 0.472 0.241 0.472 0.723
                curveToRelative(
                    dx1 = 0.315f,
                    dy1 = 0.0f,
                    dx2 = 0.472f,
                    dy2 = 0.241f,
                    dx3 = 0.472f,
                    dy3 = 0.723f,
                )
                // s -0.157 0.723 -0.472 0.723
                reflectiveCurveToRelative(
                    dx1 = -0.157f,
                    dy1 = 0.723f,
                    dx2 = -0.472f,
                    dy2 = 0.723f,
                )
                // c -0.315 0 -0.472 -0.241 -0.472 -0.723z
                curveToRelative(
                    dx1 = -0.315f,
                    dy1 = 0.0f,
                    dx2 = -0.472f,
                    dy2 = -0.241f,
                    dx3 = -0.472f,
                    dy3 = -0.723f,
                )
                close()
                // m 3.486 -1.091
                moveToRelative(dx = 3.486f, dy = -1.091f)
                // a 0.29 0.29 0 0 0 -0.398 0.097
                arcToRelative(
                    a = 0.29f,
                    b = 0.29f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.398f,
                    dy1 = 0.097f,
                )
                // l -2.955 4.831
                lineToRelative(dx = -2.955f, dy = 4.831f)
                // a 0.29 0.29 0 0 0 0.389 0.403
                arcToRelative(
                    a = 0.29f,
                    b = 0.29f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.389f,
                    dy1 = 0.403f,
                )
                // a 0.285 0.285 0 0 0 0.105 -0.102
                arcToRelative(
                    a = 0.285f,
                    b = 0.285f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.105f,
                    dy1 = -0.102f,
                )
                // l 2.955 -4.831
                lineToRelative(dx = 2.955f, dy = -4.831f)
                // a 0.289 0.289 0 0 0 -0.096 -0.398z
                arcToRelative(
                    a = 0.289f,
                    b = 0.289f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.096f,
                    dy1 = -0.398f,
                )
                close()
            }
            // M7.694 .214 S2.503 7.726 2.503 10.595 a5.187 5.187 0 0 0 5.191 5.19 5.187 5.187 0 0 0 5.191 -5.19 C12.885 7.726 7.694 .214 7.694 .214Z m2.954 11.819 c-.396 .008 -.623 .08 -1.465 .351 l-.12 .038 c-.576 .23 -1.055 .355 -1.346 .355 -.49 0 -1.298 -.338 -1.306 -.342 l-.164 -.054 c-.883 -.283 -1.093 -.35 -1.446 -.354 -.322 0 -.645 .053 -.985 .16 a4.073 4.073 0 0 1 -.345 -1.624 c0 -1.581 2.236 -5.592 4.221 -8.66 1.985 3.069 4.222 7.081 4.222 8.66 0 .545 -.115 1.089 -.342 1.618 -.3 -.094 -.61 -.143 -.924 -.148Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.694 0.214
                moveTo(x = 7.694f, y = 0.214f)
                // S 2.503 7.726 2.503 10.595
                reflectiveCurveTo(
                    x1 = 2.503f,
                    y1 = 7.726f,
                    x2 = 2.503f,
                    y2 = 10.595f,
                )
                // a 5.187 5.187 0 0 0 5.191 5.19
                arcToRelative(
                    a = 5.187f,
                    b = 5.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 5.191f,
                    dy1 = 5.19f,
                )
                // a 5.187 5.187 0 0 0 5.191 -5.19
                arcToRelative(
                    a = 5.187f,
                    b = 5.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 5.191f,
                    dy1 = -5.19f,
                )
                // C 12.885 7.726 7.694 0.214 7.694 0.214z
                curveTo(
                    x1 = 12.885f,
                    y1 = 7.726f,
                    x2 = 7.694f,
                    y2 = 0.214f,
                    x3 = 7.694f,
                    y3 = 0.214f,
                )
                close()
                // m 2.954 11.819
                moveToRelative(dx = 2.954f, dy = 11.819f)
                // c -0.396 0.008 -0.623 0.08 -1.465 0.351
                curveToRelative(
                    dx1 = -0.396f,
                    dy1 = 0.008f,
                    dx2 = -0.623f,
                    dy2 = 0.08f,
                    dx3 = -1.465f,
                    dy3 = 0.351f,
                )
                // l -0.12 0.038
                lineToRelative(dx = -0.12f, dy = 0.038f)
                // c -0.576 0.23 -1.055 0.355 -1.346 0.355
                curveToRelative(
                    dx1 = -0.576f,
                    dy1 = 0.23f,
                    dx2 = -1.055f,
                    dy2 = 0.355f,
                    dx3 = -1.346f,
                    dy3 = 0.355f,
                )
                // c -0.49 0 -1.298 -0.338 -1.306 -0.342
                curveToRelative(
                    dx1 = -0.49f,
                    dy1 = 0.0f,
                    dx2 = -1.298f,
                    dy2 = -0.338f,
                    dx3 = -1.306f,
                    dy3 = -0.342f,
                )
                // l -0.164 -0.054
                lineToRelative(dx = -0.164f, dy = -0.054f)
                // c -0.883 -0.283 -1.093 -0.35 -1.446 -0.354
                curveToRelative(
                    dx1 = -0.883f,
                    dy1 = -0.283f,
                    dx2 = -1.093f,
                    dy2 = -0.35f,
                    dx3 = -1.446f,
                    dy3 = -0.354f,
                )
                // c -0.322 0 -0.645 0.053 -0.985 0.16
                curveToRelative(
                    dx1 = -0.322f,
                    dy1 = 0.0f,
                    dx2 = -0.645f,
                    dy2 = 0.053f,
                    dx3 = -0.985f,
                    dy3 = 0.16f,
                )
                // a 4.073 4.073 0 0 1 -0.345 -1.624
                arcToRelative(
                    a = 4.073f,
                    b = 4.073f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.345f,
                    dy1 = -1.624f,
                )
                // c 0 -1.581 2.236 -5.592 4.221 -8.66
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.581f,
                    dx2 = 2.236f,
                    dy2 = -5.592f,
                    dx3 = 4.221f,
                    dy3 = -8.66f,
                )
                // c 1.985 3.069 4.222 7.081 4.222 8.66
                curveToRelative(
                    dx1 = 1.985f,
                    dy1 = 3.069f,
                    dx2 = 4.222f,
                    dy2 = 7.081f,
                    dx3 = 4.222f,
                    dy3 = 8.66f,
                )
                // c 0 0.545 -0.115 1.089 -0.342 1.618
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.545f,
                    dx2 = -0.115f,
                    dy2 = 1.089f,
                    dx3 = -0.342f,
                    dy3 = 1.618f,
                )
                // c -0.3 -0.094 -0.61 -0.143 -0.924 -0.148z
                curveToRelative(
                    dx1 = -0.3f,
                    dy1 = -0.094f,
                    dx2 = -0.61f,
                    dy2 = -0.143f,
                    dx3 = -0.924f,
                    dy3 = -0.148f,
                )
                close()
            }
        }.build().also { _ic2120 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2120: ImageVector? = null
