package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1005: ImageVector
    get() {
        val current = _ic1005
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1005",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.5 .5 a.5 .5 0 1 0 -1 0 v1.293 l-.646 -.647 a.5 .5 0 1 0 -.708 .708 L7.5 3.207 V5 c0 .014 0 .028 .002 .041 A2.997 2.997 0 0 0 5.687 6.09 a.512 .512 0 0 0 -.035 -.022 l-1.553 -.896 -.495 -1.85 a.5 .5 0 1 0 -.966 .26 l.237 .882 -1.12 -.646 a.5 .5 0 0 0 -.5 .866 l1.12 .646 -.884 .237 a.5 .5 0 0 0 .26 .966 l1.848 -.495 1.553 .896 a.503 .503 0 0 0 .036 .019 2.994 2.994 0 0 0 0 2.096 .508 .508 0 0 0 -.036 .019 l-1.553 .896 -1.849 -.495 a.5 .5 0 0 0 -.259 .966 l.884 .237 -1.12 .646 a.5 .5 0 1 0 .5 .866 l1.12 -.646 -.237 .883 a.5 .5 0 1 0 .966 .258 l.495 -1.849 1.553 -.896 a.507 .507 0 0 0 .035 -.022 3 3 0 0 0 1.815 1.048 .51 .51 0 0 0 -.002 .04 v1.793 l-1.354 1.353 a.5 .5 0 0 0 .708 .708 l.646 -.647 V15.5 a.5 .5 0 0 0 1 0 v-1.293 l.646 .647 a.5 .5 0 0 0 .708 -.708 L8.5 12.793 V10.99 c0 -.01 0 -.02 -.002 -.031 H8.5 V5.04 h-.002 l.002 -.03 V3.207 l1.354 -1.353 a.5 .5 0 1 0 -.707 -.708 l-.647 .647 V.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.5 0.5
                moveTo(x = 8.5f, y = 0.5f)
                // a 0.5 0.5 0 1 0 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // v 1.293
                verticalLineToRelative(dy = 1.293f)
                // l -0.646 -0.647
                lineToRelative(dx = -0.646f, dy = -0.647f)
                // a 0.5 0.5 0 1 0 -0.708 0.708
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.708f,
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
                // a 0.512 0.512 0 0 0 -0.035 -0.022
                arcToRelative(
                    a = 0.512f,
                    b = 0.512f,
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
                // l 1.553 0.896
                lineToRelative(dx = 1.553f, dy = 0.896f)
                // a 0.503 0.503 0 0 0 0.036 0.019
                arcToRelative(
                    a = 0.503f,
                    b = 0.503f,
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
                // a 0.508 0.508 0 0 0 -0.036 0.019
                arcToRelative(
                    a = 0.508f,
                    b = 0.508f,
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
                // a 0.507 0.507 0 0 0 0.035 -0.022
                arcToRelative(
                    a = 0.507f,
                    b = 0.507f,
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
                // a 0.51 0.51 0 0 0 -0.002 0.04
                arcToRelative(
                    a = 0.51f,
                    b = 0.51f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.002f,
                    dy1 = 0.04f,
                )
                // v 1.793
                verticalLineToRelative(dy = 1.793f)
                // l -1.354 1.353
                lineToRelative(dx = -1.354f, dy = 1.353f)
                // a 0.5 0.5 0 0 0 0.708 0.708
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.708f,
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
                // l 0.646 0.647
                lineToRelative(dx = 0.646f, dy = 0.647f)
                // a 0.5 0.5 0 0 0 0.708 -0.708
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.708f,
                    dy1 = -0.708f,
                )
                // L 8.5 12.793
                lineTo(x = 8.5f, y = 12.793f)
                // V 10.99
                verticalLineTo(y = 10.99f)
                // c 0 -0.01 0 -0.02 -0.002 -0.031
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.01f,
                    dx2 = 0.0f,
                    dy2 = -0.02f,
                    dx3 = -0.002f,
                    dy3 = -0.031f,
                )
                // H 8.5
                horizontalLineTo(x = 8.5f)
                // V 5.04
                verticalLineTo(y = 5.04f)
                // h -0.002
                horizontalLineToRelative(dx = -0.002f)
                // l 0.002 -0.03
                lineToRelative(dx = 0.002f, dy = -0.03f)
                // V 3.207
                verticalLineTo(y = 3.207f)
                // l 1.354 -1.353
                lineToRelative(dx = 1.354f, dy = -1.353f)
                // a 0.5 0.5 0 1 0 -0.707 -0.708
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
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
            // M16 5.064 13.897 4 l-1.946 .985 -1.722 -.435 -.229 .928 L12.061 6 l1.836 -.93 1.68 .85 .423 -.856Z m0 3 L13.897 7 l-1.946 .985 -1.722 -.435 -.229 .928 L12.061 9 l1.836 -.93 1.68 .85 .423 -.856Z M13.897 10 16 11.065 l-.423 .856 -1.68 -.85 -1.836 .929 L10 11.478 l.23 -.928 1.721 .435 L13.897 10Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 16 5.064
                moveTo(x = 16.0f, y = 5.064f)
                // L 13.897 4
                lineTo(x = 13.897f, y = 4.0f)
                // l -1.946 0.985
                lineToRelative(dx = -1.946f, dy = 0.985f)
                // l -1.722 -0.435
                lineToRelative(dx = -1.722f, dy = -0.435f)
                // l -0.229 0.928
                lineToRelative(dx = -0.229f, dy = 0.928f)
                // L 12.061 6
                lineTo(x = 12.061f, y = 6.0f)
                // l 1.836 -0.93
                lineToRelative(dx = 1.836f, dy = -0.93f)
                // l 1.68 0.85
                lineToRelative(dx = 1.68f, dy = 0.85f)
                // l 0.423 -0.856z
                lineToRelative(dx = 0.423f, dy = -0.856f)
                close()
                // m 0 3
                moveToRelative(dx = 0.0f, dy = 3.0f)
                // L 13.897 7
                lineTo(x = 13.897f, y = 7.0f)
                // l -1.946 0.985
                lineToRelative(dx = -1.946f, dy = 0.985f)
                // l -1.722 -0.435
                lineToRelative(dx = -1.722f, dy = -0.435f)
                // l -0.229 0.928
                lineToRelative(dx = -0.229f, dy = 0.928f)
                // L 12.061 9
                lineTo(x = 12.061f, y = 9.0f)
                // l 1.836 -0.93
                lineToRelative(dx = 1.836f, dy = -0.93f)
                // l 1.68 0.85
                lineToRelative(dx = 1.68f, dy = 0.85f)
                // l 0.423 -0.856z
                lineToRelative(dx = 0.423f, dy = -0.856f)
                close()
                // M 13.897 10
                moveTo(x = 13.897f, y = 10.0f)
                // L 16 11.065
                lineTo(x = 16.0f, y = 11.065f)
                // l -0.423 0.856
                lineToRelative(dx = -0.423f, dy = 0.856f)
                // l -1.68 -0.85
                lineToRelative(dx = -1.68f, dy = -0.85f)
                // l -1.836 0.929
                lineToRelative(dx = -1.836f, dy = 0.929f)
                // L 10 11.478
                lineTo(x = 10.0f, y = 11.478f)
                // l 0.23 -0.928
                lineToRelative(dx = 0.23f, dy = -0.928f)
                // l 1.721 0.435
                lineToRelative(dx = 1.721f, dy = 0.435f)
                // L 13.897 10z
                lineTo(x = 13.897f, y = 10.0f)
                close()
            }
        }.build().also { _ic1005 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1005: ImageVector? = null
