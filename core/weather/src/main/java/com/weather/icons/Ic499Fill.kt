package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic499Fill: ImageVector
    get() {
        val current = _ic499Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic499Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.5 .5 a.5 .5 0 0 0 -1 0 v1.293 l-.646 -.647 a.5 .5 0 1 0 -.707 .708 L7.5 3.207 V5 c0 .014 0 .028 .002 .041 A2.997 2.997 0 0 0 5.687 6.09 a.507 .507 0 0 0 -.035 -.022 l-1.553 -.896 -.495 -1.85 a.5 .5 0 1 0 -.966 .26 l.237 .882 -1.12 -.646 a.5 .5 0 0 0 -.5 .866 l1.12 .646 -.883 .237 a.5 .5 0 1 0 .258 .966 l1.85 -.495 1.552 .896 a.505 .505 0 0 0 .036 .019 2.994 2.994 0 0 0 0 2.096 .51 .51 0 0 0 -.036 .019 l-1.553 .896 -1.849 -.495 a.5 .5 0 0 0 -.258 .966 l.883 .237 -1.12 .646 a.5 .5 0 1 0 .5 .866 l1.12 -.646 -.237 .883 a.5 .5 0 1 0 .966 .258 l.495 -1.849 1.553 -.896 a.505 .505 0 0 0 .035 -.022 3 3 0 0 0 1.815 1.048 .506 .506 0 0 0 -.002 .04 v1.793 l-1.353 1.353 a.5 .5 0 0 0 .707 .708 l.646 -.647 V15.5 a.5 .5 0 0 0 1 0 v-1.293 l.647 .647 a.5 .5 0 0 0 .707 -.708 L8.5 12.793 V11 c0 -.014 0 -.028 -.002 -.041 a2.997 2.997 0 0 0 1.815 -1.048 .515 .515 0 0 0 .035 .022 l1.553 .896 .495 1.85 a.5 .5 0 1 0 .966 -.26 l-.236 -.882 1.12 .646 a.5 .5 0 0 0 .5 -.866 l-1.12 -.646 .883 -.237 a.5 .5 0 1 0 -.26 -.966 l-1.848 .495 -1.553 -.896 a.53 .53 0 0 0 -.036 -.02 2.994 2.994 0 0 0 0 -2.095 .534 .534 0 0 0 .036 -.019 l1.553 -.896 1.849 .495 a.5 .5 0 1 0 .259 -.966 l-.883 -.237 1.12 -.646 a.5 .5 0 0 0 -.5 -.866 l-1.12 .646 .236 -.883 a.5 .5 0 1 0 -.966 -.258 l-.495 1.849 -1.553 .896 a.49 .49 0 0 0 -.035 .022 2.997 2.997 0 0 0 -1.815 -1.048 A.506 .506 0 0 0 8.5 5 V3.207 l1.354 -1.353 a.5 .5 0 0 0 -.707 -.708 l-.647 .647 V.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.5 0.5
                moveTo(x = 8.5f, y = 0.5f)
                // a 0.5 0.5 0 0 0 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // v 1.293
                verticalLineToRelative(dy = 1.293f)
                // l -0.646 -0.647
                lineToRelative(dx = -0.646f, dy = -0.647f)
                // a 0.5 0.5 0 1 0 -0.707 0.708
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.707f,
                    dy1 = 0.708f,
                )
                // L 7.5 3.207
                lineTo(x = 7.5f, y = 3.207f)
                // V 5
                verticalLineTo(y = 5.0f)
                // c 0 0.014 0 0.028 0.002 0.041
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.014f,
                    dx2 = 0.0f,
                    dy2 = 0.028f,
                    dx3 = 0.002f,
                    dy3 = 0.041f,
                )
                // A 2.997 2.997 0 0 0 5.687 6.09
                arcTo(
                    horizontalEllipseRadius = 2.997f,
                    verticalEllipseRadius = 2.997f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 5.687f,
                    y1 = 6.09f,
                )
                // a 0.507 0.507 0 0 0 -0.035 -0.022
                arcToRelative(
                    a = 0.507f,
                    b = 0.507f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.035f,
                    dy1 = -0.022f,
                )
                // l -1.553 -0.896
                lineToRelative(dx = -1.553f, dy = -0.896f)
                // l -0.495 -1.85
                lineToRelative(dx = -0.495f, dy = -1.85f)
                // a 0.5 0.5 0 1 0 -0.966 0.26
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.966f,
                    dy1 = 0.26f,
                )
                // l 0.237 0.882
                lineToRelative(dx = 0.237f, dy = 0.882f)
                // l -1.12 -0.646
                lineToRelative(dx = -1.12f, dy = -0.646f)
                // a 0.5 0.5 0 0 0 -0.5 0.866
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.5f,
                    dy1 = 0.866f,
                )
                // l 1.12 0.646
                lineToRelative(dx = 1.12f, dy = 0.646f)
                // l -0.883 0.237
                lineToRelative(dx = -0.883f, dy = 0.237f)
                // a 0.5 0.5 0 1 0 0.258 0.966
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.258f,
                    dy1 = 0.966f,
                )
                // l 1.85 -0.495
                lineToRelative(dx = 1.85f, dy = -0.495f)
                // l 1.552 0.896
                lineToRelative(dx = 1.552f, dy = 0.896f)
                // a 0.505 0.505 0 0 0 0.036 0.019
                arcToRelative(
                    a = 0.505f,
                    b = 0.505f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.036f,
                    dy1 = 0.019f,
                )
                // a 2.994 2.994 0 0 0 0 2.096
                arcToRelative(
                    a = 2.994f,
                    b = 2.994f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 2.096f,
                )
                // a 0.51 0.51 0 0 0 -0.036 0.019
                arcToRelative(
                    a = 0.51f,
                    b = 0.51f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.036f,
                    dy1 = 0.019f,
                )
                // l -1.553 0.896
                lineToRelative(dx = -1.553f, dy = 0.896f)
                // l -1.849 -0.495
                lineToRelative(dx = -1.849f, dy = -0.495f)
                // a 0.5 0.5 0 0 0 -0.258 0.966
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.258f,
                    dy1 = 0.966f,
                )
                // l 0.883 0.237
                lineToRelative(dx = 0.883f, dy = 0.237f)
                // l -1.12 0.646
                lineToRelative(dx = -1.12f, dy = 0.646f)
                // a 0.5 0.5 0 1 0 0.5 0.866
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.5f,
                    dy1 = 0.866f,
                )
                // l 1.12 -0.646
                lineToRelative(dx = 1.12f, dy = -0.646f)
                // l -0.237 0.883
                lineToRelative(dx = -0.237f, dy = 0.883f)
                // a 0.5 0.5 0 1 0 0.966 0.258
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.966f,
                    dy1 = 0.258f,
                )
                // l 0.495 -1.849
                lineToRelative(dx = 0.495f, dy = -1.849f)
                // l 1.553 -0.896
                lineToRelative(dx = 1.553f, dy = -0.896f)
                // a 0.505 0.505 0 0 0 0.035 -0.022
                arcToRelative(
                    a = 0.505f,
                    b = 0.505f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.035f,
                    dy1 = -0.022f,
                )
                // a 3 3 0 0 0 1.815 1.048
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.815f,
                    dy1 = 1.048f,
                )
                // a 0.506 0.506 0 0 0 -0.002 0.04
                arcToRelative(
                    a = 0.506f,
                    b = 0.506f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.002f,
                    dy1 = 0.04f,
                )
                // v 1.793
                verticalLineToRelative(dy = 1.793f)
                // l -1.353 1.353
                lineToRelative(dx = -1.353f, dy = 1.353f)
                // a 0.5 0.5 0 0 0 0.707 0.708
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.707f,
                    dy1 = 0.708f,
                )
                // l 0.646 -0.647
                lineToRelative(dx = 0.646f, dy = -0.647f)
                // V 15.5
                verticalLineTo(y = 15.5f)
                // a 0.5 0.5 0 0 0 1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                // v -1.293
                verticalLineToRelative(dy = -1.293f)
                // l 0.647 0.647
                lineToRelative(dx = 0.647f, dy = 0.647f)
                // a 0.5 0.5 0 0 0 0.707 -0.708
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.707f,
                    dy1 = -0.708f,
                )
                // L 8.5 12.793
                lineTo(x = 8.5f, y = 12.793f)
                // V 11
                verticalLineTo(y = 11.0f)
                // c 0 -0.014 0 -0.028 -0.002 -0.041
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.014f,
                    dx2 = 0.0f,
                    dy2 = -0.028f,
                    dx3 = -0.002f,
                    dy3 = -0.041f,
                )
                // a 2.997 2.997 0 0 0 1.815 -1.048
                arcToRelative(
                    a = 2.997f,
                    b = 2.997f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.815f,
                    dy1 = -1.048f,
                )
                // a 0.515 0.515 0 0 0 0.035 0.022
                arcToRelative(
                    a = 0.515f,
                    b = 0.515f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.035f,
                    dy1 = 0.022f,
                )
                // l 1.553 0.896
                lineToRelative(dx = 1.553f, dy = 0.896f)
                // l 0.495 1.85
                lineToRelative(dx = 0.495f, dy = 1.85f)
                // a 0.5 0.5 0 1 0 0.966 -0.26
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.966f,
                    dy1 = -0.26f,
                )
                // l -0.236 -0.882
                lineToRelative(dx = -0.236f, dy = -0.882f)
                // l 1.12 0.646
                lineToRelative(dx = 1.12f, dy = 0.646f)
                // a 0.5 0.5 0 0 0 0.5 -0.866
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.5f,
                    dy1 = -0.866f,
                )
                // l -1.12 -0.646
                lineToRelative(dx = -1.12f, dy = -0.646f)
                // l 0.883 -0.237
                lineToRelative(dx = 0.883f, dy = -0.237f)
                // a 0.5 0.5 0 1 0 -0.26 -0.966
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.26f,
                    dy1 = -0.966f,
                )
                // l -1.848 0.495
                lineToRelative(dx = -1.848f, dy = 0.495f)
                // l -1.553 -0.896
                lineToRelative(dx = -1.553f, dy = -0.896f)
                // a 0.53 0.53 0 0 0 -0.036 -0.02
                arcToRelative(
                    a = 0.53f,
                    b = 0.53f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.036f,
                    dy1 = -0.02f,
                )
                // a 2.994 2.994 0 0 0 0 -2.095
                arcToRelative(
                    a = 2.994f,
                    b = 2.994f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -2.095f,
                )
                // a 0.534 0.534 0 0 0 0.036 -0.019
                arcToRelative(
                    a = 0.534f,
                    b = 0.534f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.036f,
                    dy1 = -0.019f,
                )
                // l 1.553 -0.896
                lineToRelative(dx = 1.553f, dy = -0.896f)
                // l 1.849 0.495
                lineToRelative(dx = 1.849f, dy = 0.495f)
                // a 0.5 0.5 0 1 0 0.259 -0.966
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.259f,
                    dy1 = -0.966f,
                )
                // l -0.883 -0.237
                lineToRelative(dx = -0.883f, dy = -0.237f)
                // l 1.12 -0.646
                lineToRelative(dx = 1.12f, dy = -0.646f)
                // a 0.5 0.5 0 0 0 -0.5 -0.866
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.5f,
                    dy1 = -0.866f,
                )
                // l -1.12 0.646
                lineToRelative(dx = -1.12f, dy = 0.646f)
                // l 0.236 -0.883
                lineToRelative(dx = 0.236f, dy = -0.883f)
                // a 0.5 0.5 0 1 0 -0.966 -0.258
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.966f,
                    dy1 = -0.258f,
                )
                // l -0.495 1.849
                lineToRelative(dx = -0.495f, dy = 1.849f)
                // l -1.553 0.896
                lineToRelative(dx = -1.553f, dy = 0.896f)
                // a 0.49 0.49 0 0 0 -0.035 0.022
                arcToRelative(
                    a = 0.49f,
                    b = 0.49f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.035f,
                    dy1 = 0.022f,
                )
                // a 2.997 2.997 0 0 0 -1.815 -1.048
                arcToRelative(
                    a = 2.997f,
                    b = 2.997f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.815f,
                    dy1 = -1.048f,
                )
                // A 0.506 0.506 0 0 0 8.5 5
                arcTo(
                    horizontalEllipseRadius = 0.506f,
                    verticalEllipseRadius = 0.506f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 8.5f,
                    y1 = 5.0f,
                )
                // V 3.207
                verticalLineTo(y = 3.207f)
                // l 1.354 -1.353
                lineToRelative(dx = 1.354f, dy = -1.353f)
                // a 0.5 0.5 0 0 0 -0.707 -0.708
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.707f,
                    dy1 = -0.708f,
                )
                // l -0.647 0.647
                lineToRelative(dx = -0.647f, dy = 0.647f)
                // V 0.5z
                verticalLineTo(y = 0.5f)
                close()
            }
        }.build().also { _ic499Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic499Fill: ImageVector? = null
