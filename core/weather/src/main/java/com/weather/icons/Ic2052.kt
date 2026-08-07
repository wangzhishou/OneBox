package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2052: ImageVector
    get() {
        val current = _ic2052
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2052",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.5 15.5 a.5 .5 0 0 0 1 0 v-1.293 l.647 .647 a.5 .5 0 0 0 .707 -.708 L8.5 12.793 v-1.329 c.91 -.13 1.707 -.61 2.25 -1.299 l1.15 .665 .496 1.848 a.5 .5 0 0 0 .966 -.258 l-.236 -.883 1.12 .646 a.5 .5 0 1 0 .5 -.866 l-1.12 -.646 .883 -.237 a.5 .5 0 0 0 -.26 -.966 l-1.848 .495 -1.15 -.664 A3.5 3.5 0 0 0 11.25 6.7 l1.15 -.663 1.85 .495 a.5 .5 0 1 0 .259 -.966 l-.883 -.237 1.12 -.646 a.5 .5 0 0 0 -.5 -.866 l-1.12 .646 .236 -.883 a.5 .5 0 1 0 -.966 -.258 l-.495 1.849 -1.15 .664 a3.497 3.497 0 0 0 -2.25 -1.3 V3.207 l1.353 -1.353 a.5 .5 0 0 0 -.707 -.708 l-.647 .647 V.5 a.5 .5 0 0 0 -1 0 v1.293 l-.646 -.647 a.5 .5 0 1 0 -.707 .708 L7.5 3.207 v1.328 c-.91 .13 -1.707 .61 -2.25 1.3 L4.1 5.17 l-.496 -1.85 a.5 .5 0 1 0 -.966 .26 l.237 .882 -1.12 -.646 a.5 .5 0 0 0 -.5 .866 l1.12 .646 -.884 .237 a.5 .5 0 0 0 .26 .966 l1.848 -.495 1.15 .663 a3.506 3.506 0 0 0 .001 2.6 l-1.15 .663 -1.85 -.495 a.5 .5 0 0 0 -.259 .966 l.884 .237 -1.12 .646 a.5 .5 0 1 0 .5 .866 l1.12 -.646 -.237 .883 a.5 .5 0 0 0 .966 .258 l.495 -1.849 1.15 -.664 a3.497 3.497 0 0 0 2.251 1.3 v1.328 l-1.353 1.353 a.5 .5 0 0 0 .707 .708 l.646 -.647 V15.5Z m.105 -9.895 c.256 -.153 .551 -.133 .806 .023 l.07 .042 a.846 .846 0 0 1 .409 .853 L8.532 8.8 H7.454 L7.11 6.602 c-.057 -.362 .17 -.8 .496 -.997Z M8 10.5 a.6 .6 0 1 1 0 -1.2 .6 .6 0 0 1 0 1.2Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.5 15.5
                moveTo(x = 7.5f, y = 15.5f)
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
                // v -1.329
                verticalLineToRelative(dy = -1.329f)
                // c 0.91 -0.13 1.707 -0.61 2.25 -1.299
                curveToRelative(
                    dx1 = 0.91f,
                    dy1 = -0.13f,
                    dx2 = 1.707f,
                    dy2 = -0.61f,
                    dx3 = 2.25f,
                    dy3 = -1.299f,
                )
                // l 1.15 0.665
                lineToRelative(dx = 1.15f, dy = 0.665f)
                // l 0.496 1.848
                lineToRelative(dx = 0.496f, dy = 1.848f)
                // a 0.5 0.5 0 0 0 0.966 -0.258
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.966f,
                    dy1 = -0.258f,
                )
                // l -0.236 -0.883
                lineToRelative(dx = -0.236f, dy = -0.883f)
                // l 1.12 0.646
                lineToRelative(dx = 1.12f, dy = 0.646f)
                // a 0.5 0.5 0 1 0 0.5 -0.866
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.5f,
                    dy1 = -0.866f,
                )
                // l -1.12 -0.646
                lineToRelative(dx = -1.12f, dy = -0.646f)
                // l 0.883 -0.237
                lineToRelative(dx = 0.883f, dy = -0.237f)
                // a 0.5 0.5 0 0 0 -0.26 -0.966
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.26f,
                    dy1 = -0.966f,
                )
                // l -1.848 0.495
                lineToRelative(dx = -1.848f, dy = 0.495f)
                // l -1.15 -0.664
                lineToRelative(dx = -1.15f, dy = -0.664f)
                // A 3.5 3.5 0 0 0 11.25 6.7
                arcTo(
                    horizontalEllipseRadius = 3.5f,
                    verticalEllipseRadius = 3.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 11.25f,
                    y1 = 6.7f,
                )
                // l 1.15 -0.663
                lineToRelative(dx = 1.15f, dy = -0.663f)
                // l 1.85 0.495
                lineToRelative(dx = 1.85f, dy = 0.495f)
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
                // l -1.15 0.664
                lineToRelative(dx = -1.15f, dy = 0.664f)
                // a 3.497 3.497 0 0 0 -2.25 -1.3
                arcToRelative(
                    a = 3.497f,
                    b = 3.497f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.25f,
                    dy1 = -1.3f,
                )
                // V 3.207
                verticalLineTo(y = 3.207f)
                // l 1.353 -1.353
                lineToRelative(dx = 1.353f, dy = -1.353f)
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
                // V 0.5
                verticalLineTo(y = 0.5f)
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
                // v 1.328
                verticalLineToRelative(dy = 1.328f)
                // c -0.91 0.13 -1.707 0.61 -2.25 1.3
                curveToRelative(
                    dx1 = -0.91f,
                    dy1 = 0.13f,
                    dx2 = -1.707f,
                    dy2 = 0.61f,
                    dx3 = -2.25f,
                    dy3 = 1.3f,
                )
                // L 4.1 5.17
                lineTo(x = 4.1f, y = 5.17f)
                // l -0.496 -1.85
                lineToRelative(dx = -0.496f, dy = -1.85f)
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
                // l -0.884 0.237
                lineToRelative(dx = -0.884f, dy = 0.237f)
                // a 0.5 0.5 0 0 0 0.26 0.966
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.26f,
                    dy1 = 0.966f,
                )
                // l 1.848 -0.495
                lineToRelative(dx = 1.848f, dy = -0.495f)
                // l 1.15 0.663
                lineToRelative(dx = 1.15f, dy = 0.663f)
                // a 3.506 3.506 0 0 0 0.001 2.6
                arcToRelative(
                    a = 3.506f,
                    b = 3.506f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.001f,
                    dy1 = 2.6f,
                )
                // l -1.15 0.663
                lineToRelative(dx = -1.15f, dy = 0.663f)
                // l -1.85 -0.495
                lineToRelative(dx = -1.85f, dy = -0.495f)
                // a 0.5 0.5 0 0 0 -0.259 0.966
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.259f,
                    dy1 = 0.966f,
                )
                // l 0.884 0.237
                lineToRelative(dx = 0.884f, dy = 0.237f)
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
                // a 0.5 0.5 0 0 0 0.966 0.258
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.966f,
                    dy1 = 0.258f,
                )
                // l 0.495 -1.849
                lineToRelative(dx = 0.495f, dy = -1.849f)
                // l 1.15 -0.664
                lineToRelative(dx = 1.15f, dy = -0.664f)
                // a 3.497 3.497 0 0 0 2.251 1.3
                arcToRelative(
                    a = 3.497f,
                    b = 3.497f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.251f,
                    dy1 = 1.3f,
                )
                // v 1.328
                verticalLineToRelative(dy = 1.328f)
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
                // V 15.5z
                verticalLineTo(y = 15.5f)
                close()
                // m 0.105 -9.895
                moveToRelative(dx = 0.105f, dy = -9.895f)
                // c 0.256 -0.153 0.551 -0.133 0.806 0.023
                curveToRelative(
                    dx1 = 0.256f,
                    dy1 = -0.153f,
                    dx2 = 0.551f,
                    dy2 = -0.133f,
                    dx3 = 0.806f,
                    dy3 = 0.023f,
                )
                // l 0.07 0.042
                lineToRelative(dx = 0.07f, dy = 0.042f)
                // a 0.846 0.846 0 0 1 0.409 0.853
                arcToRelative(
                    a = 0.846f,
                    b = 0.846f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.409f,
                    dy1 = 0.853f,
                )
                // L 8.532 8.8
                lineTo(x = 8.532f, y = 8.8f)
                // H 7.454
                horizontalLineTo(x = 7.454f)
                // L 7.11 6.602
                lineTo(x = 7.11f, y = 6.602f)
                // c -0.057 -0.362 0.17 -0.8 0.496 -0.997z
                curveToRelative(
                    dx1 = -0.057f,
                    dy1 = -0.362f,
                    dx2 = 0.17f,
                    dy2 = -0.8f,
                    dx3 = 0.496f,
                    dy3 = -0.997f,
                )
                close()
                // M 8 10.5
                moveTo(x = 8.0f, y = 10.5f)
                // a 0.6 0.6 0 1 1 0 -1.2
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.2f,
                )
                // a 0.6 0.6 0 0 1 0 1.2z
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.2f,
                )
                close()
            }
        }.build().also { _ic2052 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2052: ImageVector? = null
