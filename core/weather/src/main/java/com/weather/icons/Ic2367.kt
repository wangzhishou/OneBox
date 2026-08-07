package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2367: ImageVector
    get() {
        val current = _ic2367
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2367",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.502 6.62 c-.026 -.226 .205 -.42 .498 -.42 .293 0 .524 .194 .498 .42 L8.273 8.6 h-.546 l-.225 -1.98Z m.901 2.78 a.4 .4 0 1 1 -.8 0 .4 .4 0 0 1 .8 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.502 6.62
                moveTo(x = 7.502f, y = 6.62f)
                // c -0.026 -0.226 0.205 -0.42 0.498 -0.42
                curveToRelative(
                    dx1 = -0.026f,
                    dy1 = -0.226f,
                    dx2 = 0.205f,
                    dy2 = -0.42f,
                    dx3 = 0.498f,
                    dy3 = -0.42f,
                )
                // c 0.293 0 0.524 0.194 0.498 0.42
                curveToRelative(
                    dx1 = 0.293f,
                    dy1 = 0.0f,
                    dx2 = 0.524f,
                    dy2 = 0.194f,
                    dx3 = 0.498f,
                    dy3 = 0.42f,
                )
                // L 8.273 8.6
                lineTo(x = 8.273f, y = 8.6f)
                // h -0.546
                horizontalLineToRelative(dx = -0.546f)
                // l -0.225 -1.98z
                lineToRelative(dx = -0.225f, dy = -1.98f)
                close()
                // m 0.901 2.78
                moveToRelative(dx = 0.901f, dy = 2.78f)
                // a 0.4 0.4 0 1 1 -0.8 0
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.8f,
                    dy1 = 0.0f,
                )
                // a 0.4 0.4 0 0 1 0.8 0z
                arcToRelative(
                    a = 0.4f,
                    b = 0.4f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.8f,
                    dy1 = 0.0f,
                )
                close()
            }
            // M6.7 1.75 a6.184 6.184 0 0 0 -2.364 2.03 5.699 5.699 0 0 1 2.202 -1.131 5.774 5.774 0 0 1 2.484 -.097 5.721 5.721 0 0 1 2.287 .956 5.577 5.577 0 0 1 1.647 1.823 7.064 7.064 0 0 1 .62 5.476 7.23 7.23 0 0 1 -3.519 4.301 7.494 7.494 0 0 1 -5.594 .607 A7.326 7.326 0 0 1 .07 12.27 a.543 .543 0 0 1 -.048 -.422 .556 .556 0 0 1 .271 -.33 .576 .576 0 0 1 .768 .218 6.148 6.148 0 0 0 2.162 2.235 6.335 6.335 0 0 0 6.077 .277 6.184 6.184 0 0 0 2.364 -2.028 5.698 5.698 0 0 1 -2.202 1.13 5.785 5.785 0 0 1 -2.484 .097 5.72 5.72 0 0 1 -2.287 -.956 5.578 5.578 0 0 1 -1.647 -1.823 7.065 7.065 0 0 1 -.62 -5.476 A7.23 7.23 0 0 1 5.943 .89 a7.494 7.494 0 0 1 5.595 -.606 A7.327 7.327 0 0 1 15.93 3.73 c.072 .129 .09 .28 .048 .422 a.556 .556 0 0 1 -.271 .33 .577 .577 0 0 1 -.43 .047 .564 .564 0 0 1 -.338 -.265 6.148 6.148 0 0 0 -2.162 -2.235 A6.335 6.335 0 0 0 6.7 1.751Z M11 8 a3 3 0 1 0 -6 0 3 3 0 0 0 6 0Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6.7 1.75
                moveTo(x = 6.7f, y = 1.75f)
                // a 6.184 6.184 0 0 0 -2.364 2.03
                arcToRelative(
                    a = 6.184f,
                    b = 6.184f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.364f,
                    dy1 = 2.03f,
                )
                // a 5.699 5.699 0 0 1 2.202 -1.131
                arcToRelative(
                    a = 5.699f,
                    b = 5.699f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.202f,
                    dy1 = -1.131f,
                )
                // a 5.774 5.774 0 0 1 2.484 -0.097
                arcToRelative(
                    a = 5.774f,
                    b = 5.774f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.484f,
                    dy1 = -0.097f,
                )
                // a 5.721 5.721 0 0 1 2.287 0.956
                arcToRelative(
                    a = 5.721f,
                    b = 5.721f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.287f,
                    dy1 = 0.956f,
                )
                // a 5.577 5.577 0 0 1 1.647 1.823
                arcToRelative(
                    a = 5.577f,
                    b = 5.577f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.647f,
                    dy1 = 1.823f,
                )
                // a 7.064 7.064 0 0 1 0.62 5.476
                arcToRelative(
                    a = 7.064f,
                    b = 7.064f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.62f,
                    dy1 = 5.476f,
                )
                // a 7.23 7.23 0 0 1 -3.519 4.301
                arcToRelative(
                    a = 7.23f,
                    b = 7.23f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.519f,
                    dy1 = 4.301f,
                )
                // a 7.494 7.494 0 0 1 -5.594 0.607
                arcToRelative(
                    a = 7.494f,
                    b = 7.494f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -5.594f,
                    dy1 = 0.607f,
                )
                // A 7.326 7.326 0 0 1 0.07 12.27
                arcTo(
                    horizontalEllipseRadius = 7.326f,
                    verticalEllipseRadius = 7.326f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 0.07f,
                    y1 = 12.27f,
                )
                // a 0.543 0.543 0 0 1 -0.048 -0.422
                arcToRelative(
                    a = 0.543f,
                    b = 0.543f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.048f,
                    dy1 = -0.422f,
                )
                // a 0.556 0.556 0 0 1 0.271 -0.33
                arcToRelative(
                    a = 0.556f,
                    b = 0.556f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.271f,
                    dy1 = -0.33f,
                )
                // a 0.576 0.576 0 0 1 0.768 0.218
                arcToRelative(
                    a = 0.576f,
                    b = 0.576f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.768f,
                    dy1 = 0.218f,
                )
                // a 6.148 6.148 0 0 0 2.162 2.235
                arcToRelative(
                    a = 6.148f,
                    b = 6.148f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.162f,
                    dy1 = 2.235f,
                )
                // a 6.335 6.335 0 0 0 6.077 0.277
                arcToRelative(
                    a = 6.335f,
                    b = 6.335f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 6.077f,
                    dy1 = 0.277f,
                )
                // a 6.184 6.184 0 0 0 2.364 -2.028
                arcToRelative(
                    a = 6.184f,
                    b = 6.184f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.364f,
                    dy1 = -2.028f,
                )
                // a 5.698 5.698 0 0 1 -2.202 1.13
                arcToRelative(
                    a = 5.698f,
                    b = 5.698f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.202f,
                    dy1 = 1.13f,
                )
                // a 5.785 5.785 0 0 1 -2.484 0.097
                arcToRelative(
                    a = 5.785f,
                    b = 5.785f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.484f,
                    dy1 = 0.097f,
                )
                // a 5.72 5.72 0 0 1 -2.287 -0.956
                arcToRelative(
                    a = 5.72f,
                    b = 5.72f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.287f,
                    dy1 = -0.956f,
                )
                // a 5.578 5.578 0 0 1 -1.647 -1.823
                arcToRelative(
                    a = 5.578f,
                    b = 5.578f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.647f,
                    dy1 = -1.823f,
                )
                // a 7.065 7.065 0 0 1 -0.62 -5.476
                arcToRelative(
                    a = 7.065f,
                    b = 7.065f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.62f,
                    dy1 = -5.476f,
                )
                // A 7.23 7.23 0 0 1 5.943 0.89
                arcTo(
                    horizontalEllipseRadius = 7.23f,
                    verticalEllipseRadius = 7.23f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 5.943f,
                    y1 = 0.89f,
                )
                // a 7.494 7.494 0 0 1 5.595 -0.606
                arcToRelative(
                    a = 7.494f,
                    b = 7.494f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 5.595f,
                    dy1 = -0.606f,
                )
                // A 7.327 7.327 0 0 1 15.93 3.73
                arcTo(
                    horizontalEllipseRadius = 7.327f,
                    verticalEllipseRadius = 7.327f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 15.93f,
                    y1 = 3.73f,
                )
                // c 0.072 0.129 0.09 0.28 0.048 0.422
                curveToRelative(
                    dx1 = 0.072f,
                    dy1 = 0.129f,
                    dx2 = 0.09f,
                    dy2 = 0.28f,
                    dx3 = 0.048f,
                    dy3 = 0.422f,
                )
                // a 0.556 0.556 0 0 1 -0.271 0.33
                arcToRelative(
                    a = 0.556f,
                    b = 0.556f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.271f,
                    dy1 = 0.33f,
                )
                // a 0.577 0.577 0 0 1 -0.43 0.047
                arcToRelative(
                    a = 0.577f,
                    b = 0.577f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.43f,
                    dy1 = 0.047f,
                )
                // a 0.564 0.564 0 0 1 -0.338 -0.265
                arcToRelative(
                    a = 0.564f,
                    b = 0.564f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.338f,
                    dy1 = -0.265f,
                )
                // a 6.148 6.148 0 0 0 -2.162 -2.235
                arcToRelative(
                    a = 6.148f,
                    b = 6.148f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.162f,
                    dy1 = -2.235f,
                )
                // A 6.335 6.335 0 0 0 6.7 1.751z
                arcTo(
                    horizontalEllipseRadius = 6.335f,
                    verticalEllipseRadius = 6.335f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 6.7f,
                    y1 = 1.751f,
                )
                close()
                // M 11 8
                moveTo(x = 11.0f, y = 8.0f)
                // a 3 3 0 1 0 -6 0
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -6.0f,
                    dy1 = 0.0f,
                )
                // a 3 3 0 0 0 6 0z
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 6.0f,
                    dy1 = 0.0f,
                )
                close()
            }
        }.build().also { _ic2367 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2367: ImageVector? = null
