package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic503Fill: ImageVector
    get() {
        val current = _ic503Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic503Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M4.5 1.75 a.75 .75 0 1 1 -1.5 0 .75 .75 0 0 1 1.5 0Z m4 0 a.75 .75 0 1 1 -1.5 0 .75 .75 0 0 1 1.5 0Z M5.75 4.5 a.75 .75 0 1 0 0 -1.5 .75 .75 0 0 0 0 1.5Z M3.5 5.75 a.75 .75 0 1 1 -1.5 0 .75 .75 0 0 1 1.5 0Z M5.75 7.5 a.75 .75 0 1 0 0 -1.5 .75 .75 0 0 0 0 1.5Z M2.5 10.75 a.75 .75 0 1 1 -1.5 0 .75 .75 0 0 1 1.5 0Z m.25 4.75 a.75 .75 0 1 0 0 -1.5 .75 .75 0 0 0 0 1.5Z m3.25 -5 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z m3.75 -5 a.75 .75 0 1 0 0 -1.5 .75 .75 0 0 0 0 1.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.5 1.75
                moveTo(x = 4.5f, y = 1.75f)
                // a 0.75 0.75 0 1 1 -1.5 0
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.5f,
                    dy1 = 0.0f,
                )
                // a 0.75 0.75 0 0 1 1.5 0z
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.5f,
                    dy1 = 0.0f,
                )
                close()
                // m 4 0
                moveToRelative(dx = 4.0f, dy = 0.0f)
                // a 0.75 0.75 0 1 1 -1.5 0
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.5f,
                    dy1 = 0.0f,
                )
                // a 0.75 0.75 0 0 1 1.5 0z
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.5f,
                    dy1 = 0.0f,
                )
                close()
                // M 5.75 4.5
                moveTo(x = 5.75f, y = 4.5f)
                // a 0.75 0.75 0 1 0 0 -1.5
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.5f,
                )
                // a 0.75 0.75 0 0 0 0 1.5z
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.5f,
                )
                close()
                // M 3.5 5.75
                moveTo(x = 3.5f, y = 5.75f)
                // a 0.75 0.75 0 1 1 -1.5 0
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.5f,
                    dy1 = 0.0f,
                )
                // a 0.75 0.75 0 0 1 1.5 0z
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.5f,
                    dy1 = 0.0f,
                )
                close()
                // M 5.75 7.5
                moveTo(x = 5.75f, y = 7.5f)
                // a 0.75 0.75 0 1 0 0 -1.5
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.5f,
                )
                // a 0.75 0.75 0 0 0 0 1.5z
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.5f,
                )
                close()
                // M 2.5 10.75
                moveTo(x = 2.5f, y = 10.75f)
                // a 0.75 0.75 0 1 1 -1.5 0
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.5f,
                    dy1 = 0.0f,
                )
                // a 0.75 0.75 0 0 1 1.5 0z
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.5f,
                    dy1 = 0.0f,
                )
                close()
                // m 0.25 4.75
                moveToRelative(dx = 0.25f, dy = 4.75f)
                // a 0.75 0.75 0 1 0 0 -1.5
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.5f,
                )
                // a 0.75 0.75 0 0 0 0 1.5z
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.5f,
                )
                close()
                // m 3.25 -5
                moveToRelative(dx = 3.25f, dy = -5.0f)
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
                // m 3.75 -5
                moveToRelative(dx = 3.75f, dy = -5.0f)
                // a 0.75 0.75 0 1 0 0 -1.5
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.5f,
                )
                // a 0.75 0.75 0 0 0 0 1.5z
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.5f,
                )
                close()
            }
            // M15.999 5.106 c0 -1.863 -2.498 -3.458 -6.03 -4.106 1.904 .808 3.099 1.968 3.099 3.257 0 2.44 -4.275 4.418 -9.55 4.418 A19.596 19.596 0 0 1 0 8.363 a16.95 16.95 0 0 0 6.452 1.16 17.35 17.35 0 0 0 6.099 -1.018 c.006 .058 .01 .117 .01 .176 0 1.83 -3.206 3.313 -7.16 3.313 a14.628 14.628 0 0 1 -2.64 -.234 12.7 12.7 0 0 0 4.837 .871 13.361 13.361 0 0 0 4.267 -.653 c0 .02 .006 .039 .006 .058 0 1.373 -2.405 2.484 -5.371 2.484 a10.992 10.992 0 0 1 -1.98 -.174 9.53 9.53 0 0 0 3.629 .653 c2.965 0 5.371 -1.113 5.371 -2.486 a1.502 1.502 0 0 0 -.143 -.57 1.454 1.454 0 0 0 -.35 -.466 c1.079 -.58 1.734 -1.335 1.734 -2.16 a2.084 2.084 0 0 0 -.231 -.836 2.016 2.016 0 0 0 -.548 -.66 3.332 3.332 0 0 0 1.36 -1.078 A3.49 3.49 0 0 0 16 5.106 h-.001Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 15.999 5.106
                moveTo(x = 15.999f, y = 5.106f)
                // c 0 -1.863 -2.498 -3.458 -6.03 -4.106
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.863f,
                    dx2 = -2.498f,
                    dy2 = -3.458f,
                    dx3 = -6.03f,
                    dy3 = -4.106f,
                )
                // c 1.904 0.808 3.099 1.968 3.099 3.257
                curveToRelative(
                    dx1 = 1.904f,
                    dy1 = 0.808f,
                    dx2 = 3.099f,
                    dy2 = 1.968f,
                    dx3 = 3.099f,
                    dy3 = 3.257f,
                )
                // c 0 2.44 -4.275 4.418 -9.55 4.418
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 2.44f,
                    dx2 = -4.275f,
                    dy2 = 4.418f,
                    dx3 = -9.55f,
                    dy3 = 4.418f,
                )
                // A 19.596 19.596 0 0 1 0 8.363
                arcTo(
                    horizontalEllipseRadius = 19.596f,
                    verticalEllipseRadius = 19.596f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 0.0f,
                    y1 = 8.363f,
                )
                // a 16.95 16.95 0 0 0 6.452 1.16
                arcToRelative(
                    a = 16.95f,
                    b = 16.95f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 6.452f,
                    dy1 = 1.16f,
                )
                // a 17.35 17.35 0 0 0 6.099 -1.018
                arcToRelative(
                    a = 17.35f,
                    b = 17.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 6.099f,
                    dy1 = -1.018f,
                )
                // c 0.006 0.058 0.01 0.117 0.01 0.176
                curveToRelative(
                    dx1 = 0.006f,
                    dy1 = 0.058f,
                    dx2 = 0.01f,
                    dy2 = 0.117f,
                    dx3 = 0.01f,
                    dy3 = 0.176f,
                )
                // c 0 1.83 -3.206 3.313 -7.16 3.313
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.83f,
                    dx2 = -3.206f,
                    dy2 = 3.313f,
                    dx3 = -7.16f,
                    dy3 = 3.313f,
                )
                // a 14.628 14.628 0 0 1 -2.64 -0.234
                arcToRelative(
                    a = 14.628f,
                    b = 14.628f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.64f,
                    dy1 = -0.234f,
                )
                // a 12.7 12.7 0 0 0 4.837 0.871
                arcToRelative(
                    a = 12.7f,
                    b = 12.7f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 4.837f,
                    dy1 = 0.871f,
                )
                // a 13.361 13.361 0 0 0 4.267 -0.653
                arcToRelative(
                    a = 13.361f,
                    b = 13.361f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 4.267f,
                    dy1 = -0.653f,
                )
                // c 0 0.02 0.006 0.039 0.006 0.058
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.02f,
                    dx2 = 0.006f,
                    dy2 = 0.039f,
                    dx3 = 0.006f,
                    dy3 = 0.058f,
                )
                // c 0 1.373 -2.405 2.484 -5.371 2.484
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.373f,
                    dx2 = -2.405f,
                    dy2 = 2.484f,
                    dx3 = -5.371f,
                    dy3 = 2.484f,
                )
                // a 10.992 10.992 0 0 1 -1.98 -0.174
                arcToRelative(
                    a = 10.992f,
                    b = 10.992f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.98f,
                    dy1 = -0.174f,
                )
                // a 9.53 9.53 0 0 0 3.629 0.653
                arcToRelative(
                    a = 9.53f,
                    b = 9.53f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 3.629f,
                    dy1 = 0.653f,
                )
                // c 2.965 0 5.371 -1.113 5.371 -2.486
                curveToRelative(
                    dx1 = 2.965f,
                    dy1 = 0.0f,
                    dx2 = 5.371f,
                    dy2 = -1.113f,
                    dx3 = 5.371f,
                    dy3 = -2.486f,
                )
                // a 1.502 1.502 0 0 0 -0.143 -0.57
                arcToRelative(
                    a = 1.502f,
                    b = 1.502f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.143f,
                    dy1 = -0.57f,
                )
                // a 1.454 1.454 0 0 0 -0.35 -0.466
                arcToRelative(
                    a = 1.454f,
                    b = 1.454f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = -0.466f,
                )
                // c 1.079 -0.58 1.734 -1.335 1.734 -2.16
                curveToRelative(
                    dx1 = 1.079f,
                    dy1 = -0.58f,
                    dx2 = 1.734f,
                    dy2 = -1.335f,
                    dx3 = 1.734f,
                    dy3 = -2.16f,
                )
                // a 2.084 2.084 0 0 0 -0.231 -0.836
                arcToRelative(
                    a = 2.084f,
                    b = 2.084f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.231f,
                    dy1 = -0.836f,
                )
                // a 2.016 2.016 0 0 0 -0.548 -0.66
                arcToRelative(
                    a = 2.016f,
                    b = 2.016f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.548f,
                    dy1 = -0.66f,
                )
                // a 3.332 3.332 0 0 0 1.36 -1.078
                arcToRelative(
                    a = 3.332f,
                    b = 3.332f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.36f,
                    dy1 = -1.078f,
                )
                // A 3.49 3.49 0 0 0 16 5.106
                arcTo(
                    horizontalEllipseRadius = 3.49f,
                    verticalEllipseRadius = 3.49f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 16.0f,
                    y1 = 5.106f,
                )
                // h -0.001z
                horizontalLineToRelative(dx = -0.001f)
                close()
            }
        }.build().also { _ic503Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic503Fill: ImageVector? = null
