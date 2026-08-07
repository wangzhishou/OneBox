package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1039: ImageVector
    get() {
        val current = _ic1039
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1039",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M6 2.7 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6 2.7
                moveTo(x = 6.0f, y = 2.7f)
                // a 0.5 0.5 0 1 1 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // a 0.5 0.5 0 0 1 1 0z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                close()
            }
            // M7.833 11.939 a.258 .258 0 0 0 .335 0 l6.721 -5.737 a.317 .317 0 0 0 .09 -.35 .32 .32 0 0 0 -.12 -.15 .332 .332 0 0 0 -.185 -.057 h-2.442 V.498 a.492 .492 0 0 0 -.15 -.352 .517 .517 0 0 0 -.36 -.146 h-7.58 a.517 .517 0 0 0 -.36 .146 .491 .491 0 0 0 -.15 .352 v5.147 H1.327 a.332 .332 0 0 0 -.186 .056 .31 .31 0 0 0 -.029 .501 l6.72 5.737Z M7.832 2.99 a2.417 2.417 0 0 1 1.426 -.157 c.156 .03 .615 .131 1.068 .487 .263 .207 .496 .486 .597 .64 l-.52 .546 c-.119 -.192 -.524 -.662 -1.12 -.81 -.838 -.21 -2.077 .27 -2.089 1.621 -.01 1.33 1.186 1.911 2.185 1.654 .544 -.139 .885 -.536 1.03 -.74 l.51 .53 c-.246 .329 -.565 .6 -.932 .794 a2.28 2.28 0 0 1 -1.167 .254 h-.01 c-.213 -.006 -1.107 -.028 -1.77 -.696 -.673 -.678 -.703 -1.525 -.703 -1.827 0 -.68 .258 -1.263 .719 -1.75 .104 -.111 .383 -.37 .776 -.546Z m-3.02 -.936 a.935 .935 0 0 1 .674 -.266 .968 .968 0 0 1 .688 .255 .857 .857 0 0 1 .268 .645 .856 .856 0 0 1 -.268 .65 .938 .938 0 0 1 -.676 .255 .982 .982 0 0 1 -.688 -.261 .85 .85 0 0 1 -.28 -.637 c0 -.25 .094 -.463 .281 -.641Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.833 11.939
                moveTo(x = 7.833f, y = 11.939f)
                // a 0.258 0.258 0 0 0 0.335 0
                arcToRelative(
                    a = 0.258f,
                    b = 0.258f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.335f,
                    dy1 = 0.0f,
                )
                // l 6.721 -5.737
                lineToRelative(dx = 6.721f, dy = -5.737f)
                // a 0.317 0.317 0 0 0 0.09 -0.35
                arcToRelative(
                    a = 0.317f,
                    b = 0.317f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.09f,
                    dy1 = -0.35f,
                )
                // a 0.32 0.32 0 0 0 -0.12 -0.15
                arcToRelative(
                    a = 0.32f,
                    b = 0.32f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.12f,
                    dy1 = -0.15f,
                )
                // a 0.332 0.332 0 0 0 -0.185 -0.057
                arcToRelative(
                    a = 0.332f,
                    b = 0.332f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.185f,
                    dy1 = -0.057f,
                )
                // h -2.442
                horizontalLineToRelative(dx = -2.442f)
                // V 0.498
                verticalLineTo(y = 0.498f)
                // a 0.492 0.492 0 0 0 -0.15 -0.352
                arcToRelative(
                    a = 0.492f,
                    b = 0.492f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.15f,
                    dy1 = -0.352f,
                )
                // a 0.517 0.517 0 0 0 -0.36 -0.146
                arcToRelative(
                    a = 0.517f,
                    b = 0.517f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.36f,
                    dy1 = -0.146f,
                )
                // h -7.58
                horizontalLineToRelative(dx = -7.58f)
                // a 0.517 0.517 0 0 0 -0.36 0.146
                arcToRelative(
                    a = 0.517f,
                    b = 0.517f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.36f,
                    dy1 = 0.146f,
                )
                // a 0.491 0.491 0 0 0 -0.15 0.352
                arcToRelative(
                    a = 0.491f,
                    b = 0.491f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.15f,
                    dy1 = 0.352f,
                )
                // v 5.147
                verticalLineToRelative(dy = 5.147f)
                // H 1.327
                horizontalLineTo(x = 1.327f)
                // a 0.332 0.332 0 0 0 -0.186 0.056
                arcToRelative(
                    a = 0.332f,
                    b = 0.332f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.186f,
                    dy1 = 0.056f,
                )
                // a 0.31 0.31 0 0 0 -0.029 0.501
                arcToRelative(
                    a = 0.31f,
                    b = 0.31f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.029f,
                    dy1 = 0.501f,
                )
                // l 6.72 5.737z
                lineToRelative(dx = 6.72f, dy = 5.737f)
                close()
                // M 7.832 2.99
                moveTo(x = 7.832f, y = 2.99f)
                // a 2.417 2.417 0 0 1 1.426 -0.157
                arcToRelative(
                    a = 2.417f,
                    b = 2.417f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.426f,
                    dy1 = -0.157f,
                )
                // c 0.156 0.03 0.615 0.131 1.068 0.487
                curveToRelative(
                    dx1 = 0.156f,
                    dy1 = 0.03f,
                    dx2 = 0.615f,
                    dy2 = 0.131f,
                    dx3 = 1.068f,
                    dy3 = 0.487f,
                )
                // c 0.263 0.207 0.496 0.486 0.597 0.64
                curveToRelative(
                    dx1 = 0.263f,
                    dy1 = 0.207f,
                    dx2 = 0.496f,
                    dy2 = 0.486f,
                    dx3 = 0.597f,
                    dy3 = 0.64f,
                )
                // l -0.52 0.546
                lineToRelative(dx = -0.52f, dy = 0.546f)
                // c -0.119 -0.192 -0.524 -0.662 -1.12 -0.81
                curveToRelative(
                    dx1 = -0.119f,
                    dy1 = -0.192f,
                    dx2 = -0.524f,
                    dy2 = -0.662f,
                    dx3 = -1.12f,
                    dy3 = -0.81f,
                )
                // c -0.838 -0.21 -2.077 0.27 -2.089 1.621
                curveToRelative(
                    dx1 = -0.838f,
                    dy1 = -0.21f,
                    dx2 = -2.077f,
                    dy2 = 0.27f,
                    dx3 = -2.089f,
                    dy3 = 1.621f,
                )
                // c -0.01 1.33 1.186 1.911 2.185 1.654
                curveToRelative(
                    dx1 = -0.01f,
                    dy1 = 1.33f,
                    dx2 = 1.186f,
                    dy2 = 1.911f,
                    dx3 = 2.185f,
                    dy3 = 1.654f,
                )
                // c 0.544 -0.139 0.885 -0.536 1.03 -0.74
                curveToRelative(
                    dx1 = 0.544f,
                    dy1 = -0.139f,
                    dx2 = 0.885f,
                    dy2 = -0.536f,
                    dx3 = 1.03f,
                    dy3 = -0.74f,
                )
                // l 0.51 0.53
                lineToRelative(dx = 0.51f, dy = 0.53f)
                // c -0.246 0.329 -0.565 0.6 -0.932 0.794
                curveToRelative(
                    dx1 = -0.246f,
                    dy1 = 0.329f,
                    dx2 = -0.565f,
                    dy2 = 0.6f,
                    dx3 = -0.932f,
                    dy3 = 0.794f,
                )
                // a 2.28 2.28 0 0 1 -1.167 0.254
                arcToRelative(
                    a = 2.28f,
                    b = 2.28f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.167f,
                    dy1 = 0.254f,
                )
                // h -0.01
                horizontalLineToRelative(dx = -0.01f)
                // c -0.213 -0.006 -1.107 -0.028 -1.77 -0.696
                curveToRelative(
                    dx1 = -0.213f,
                    dy1 = -0.006f,
                    dx2 = -1.107f,
                    dy2 = -0.028f,
                    dx3 = -1.77f,
                    dy3 = -0.696f,
                )
                // c -0.673 -0.678 -0.703 -1.525 -0.703 -1.827
                curveToRelative(
                    dx1 = -0.673f,
                    dy1 = -0.678f,
                    dx2 = -0.703f,
                    dy2 = -1.525f,
                    dx3 = -0.703f,
                    dy3 = -1.827f,
                )
                // c 0 -0.68 0.258 -1.263 0.719 -1.75
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.68f,
                    dx2 = 0.258f,
                    dy2 = -1.263f,
                    dx3 = 0.719f,
                    dy3 = -1.75f,
                )
                // c 0.104 -0.111 0.383 -0.37 0.776 -0.546z
                curveToRelative(
                    dx1 = 0.104f,
                    dy1 = -0.111f,
                    dx2 = 0.383f,
                    dy2 = -0.37f,
                    dx3 = 0.776f,
                    dy3 = -0.546f,
                )
                close()
                // m -3.02 -0.936
                moveToRelative(dx = -3.02f, dy = -0.936f)
                // a 0.935 0.935 0 0 1 0.674 -0.266
                arcToRelative(
                    a = 0.935f,
                    b = 0.935f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.674f,
                    dy1 = -0.266f,
                )
                // a 0.968 0.968 0 0 1 0.688 0.255
                arcToRelative(
                    a = 0.968f,
                    b = 0.968f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.688f,
                    dy1 = 0.255f,
                )
                // a 0.857 0.857 0 0 1 0.268 0.645
                arcToRelative(
                    a = 0.857f,
                    b = 0.857f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.268f,
                    dy1 = 0.645f,
                )
                // a 0.856 0.856 0 0 1 -0.268 0.65
                arcToRelative(
                    a = 0.856f,
                    b = 0.856f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.268f,
                    dy1 = 0.65f,
                )
                // a 0.938 0.938 0 0 1 -0.676 0.255
                arcToRelative(
                    a = 0.938f,
                    b = 0.938f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.676f,
                    dy1 = 0.255f,
                )
                // a 0.982 0.982 0 0 1 -0.688 -0.261
                arcToRelative(
                    a = 0.982f,
                    b = 0.982f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.688f,
                    dy1 = -0.261f,
                )
                // a 0.85 0.85 0 0 1 -0.28 -0.637
                arcToRelative(
                    a = 0.85f,
                    b = 0.85f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.28f,
                    dy1 = -0.637f,
                )
                // c 0 -0.25 0.094 -0.463 0.281 -0.641z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.25f,
                    dx2 = 0.094f,
                    dy2 = -0.463f,
                    dx3 = 0.281f,
                    dy3 = -0.641f,
                )
                close()
            }
            // M14.4 8.94 h1.478 a.123 .123 0 0 1 .08 .215 L8.247 15.91 a.374 .374 0 0 1 -.492 0 L.04 9.155 a.123 .123 0 0 1 .081 -.215 H1.6 a.59 .59 0 0 1 .393 .148 l6.009 5.26 6.007 -5.26 a.594 .594 0 0 1 .393 -.148Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 14.4 8.94
                moveTo(x = 14.4f, y = 8.94f)
                // h 1.478
                horizontalLineToRelative(dx = 1.478f)
                // a 0.123 0.123 0 0 1 0.08 0.215
                arcToRelative(
                    a = 0.123f,
                    b = 0.123f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.08f,
                    dy1 = 0.215f,
                )
                // L 8.247 15.91
                lineTo(x = 8.247f, y = 15.91f)
                // a 0.374 0.374 0 0 1 -0.492 0
                arcToRelative(
                    a = 0.374f,
                    b = 0.374f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.492f,
                    dy1 = 0.0f,
                )
                // L 0.04 9.155
                lineTo(x = 0.04f, y = 9.155f)
                // a 0.123 0.123 0 0 1 0.081 -0.215
                arcToRelative(
                    a = 0.123f,
                    b = 0.123f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.081f,
                    dy1 = -0.215f,
                )
                // H 1.6
                horizontalLineTo(x = 1.6f)
                // a 0.59 0.59 0 0 1 0.393 0.148
                arcToRelative(
                    a = 0.59f,
                    b = 0.59f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.393f,
                    dy1 = 0.148f,
                )
                // l 6.009 5.26
                lineToRelative(dx = 6.009f, dy = 5.26f)
                // l 6.007 -5.26
                lineToRelative(dx = 6.007f, dy = -5.26f)
                // a 0.594 0.594 0 0 1 0.393 -0.148z
                arcToRelative(
                    a = 0.594f,
                    b = 0.594f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.393f,
                    dy1 = -0.148f,
                )
                close()
            }
        }.build().also { _ic1039 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1039: ImageVector? = null
