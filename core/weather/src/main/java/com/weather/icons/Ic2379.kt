package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2379: ImageVector
    get() {
        val current = _ic2379
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2379",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M9.985 10.75 a.188 .188 0 0 0 .265 -.265 L8.187 8.422 v.53 l1.798 1.798Z m-4.235 -.265 a.187 .187 0 1 0 .265 .265 l1.797 -1.797 v-.53 L5.75 10.484Z m4.5 -6.97 a.188 .188 0 0 0 -.265 -.265 L8.188 5.047 v.53 l2.062 -2.062Z M6.015 3.25 a.187 .187 0 1 0 -.265 .265 l2.062 2.063 v-.53 L6.015 3.25Z m-1.5 1.5 a.187 .187 0 1 0 -.265 .265 l1.797 1.797 h.53 L4.516 4.75Z M4.25 8.985 a.187 .187 0 1 0 .265 .265 l2.063 -2.063 h-.53 L4.25 8.986Z m7.5 -3.97 a.188 .188 0 0 0 -.265 -.265 L9.422 6.812 h.53 l1.798 -1.797Z m-.265 4.235 a.187 .187 0 1 0 .265 -.265 L9.953 7.188 h-.53 l2.062 2.062Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 9.985 10.75
                moveTo(x = 9.985f, y = 10.75f)
                // a 0.188 0.188 0 0 0 0.265 -0.265
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.265f,
                    dy1 = -0.265f,
                )
                // L 8.187 8.422
                lineTo(x = 8.187f, y = 8.422f)
                // v 0.53
                verticalLineToRelative(dy = 0.53f)
                // l 1.798 1.798z
                lineToRelative(dx = 1.798f, dy = 1.798f)
                close()
                // m -4.235 -0.265
                moveToRelative(dx = -4.235f, dy = -0.265f)
                // a 0.187 0.187 0 1 0 0.265 0.265
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.265f,
                    dy1 = 0.265f,
                )
                // l 1.797 -1.797
                lineToRelative(dx = 1.797f, dy = -1.797f)
                // v -0.53
                verticalLineToRelative(dy = -0.53f)
                // L 5.75 10.484z
                lineTo(x = 5.75f, y = 10.484f)
                close()
                // m 4.5 -6.97
                moveToRelative(dx = 4.5f, dy = -6.97f)
                // a 0.188 0.188 0 0 0 -0.265 -0.265
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.265f,
                    dy1 = -0.265f,
                )
                // L 8.188 5.047
                lineTo(x = 8.188f, y = 5.047f)
                // v 0.53
                verticalLineToRelative(dy = 0.53f)
                // l 2.062 -2.062z
                lineToRelative(dx = 2.062f, dy = -2.062f)
                close()
                // M 6.015 3.25
                moveTo(x = 6.015f, y = 3.25f)
                // a 0.187 0.187 0 1 0 -0.265 0.265
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.265f,
                    dy1 = 0.265f,
                )
                // l 2.062 2.063
                lineToRelative(dx = 2.062f, dy = 2.063f)
                // v -0.53
                verticalLineToRelative(dy = -0.53f)
                // L 6.015 3.25z
                lineTo(x = 6.015f, y = 3.25f)
                close()
                // m -1.5 1.5
                moveToRelative(dx = -1.5f, dy = 1.5f)
                // a 0.187 0.187 0 1 0 -0.265 0.265
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.265f,
                    dy1 = 0.265f,
                )
                // l 1.797 1.797
                lineToRelative(dx = 1.797f, dy = 1.797f)
                // h 0.53
                horizontalLineToRelative(dx = 0.53f)
                // L 4.516 4.75z
                lineTo(x = 4.516f, y = 4.75f)
                close()
                // M 4.25 8.985
                moveTo(x = 4.25f, y = 8.985f)
                // a 0.187 0.187 0 1 0 0.265 0.265
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.265f,
                    dy1 = 0.265f,
                )
                // l 2.063 -2.063
                lineToRelative(dx = 2.063f, dy = -2.063f)
                // h -0.53
                horizontalLineToRelative(dx = -0.53f)
                // L 4.25 8.986z
                lineTo(x = 4.25f, y = 8.986f)
                close()
                // m 7.5 -3.97
                moveToRelative(dx = 7.5f, dy = -3.97f)
                // a 0.188 0.188 0 0 0 -0.265 -0.265
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.265f,
                    dy1 = -0.265f,
                )
                // L 9.422 6.812
                lineTo(x = 9.422f, y = 6.812f)
                // h 0.53
                horizontalLineToRelative(dx = 0.53f)
                // l 1.798 -1.797z
                lineToRelative(dx = 1.798f, dy = -1.797f)
                close()
                // m -0.265 4.235
                moveToRelative(dx = -0.265f, dy = 4.235f)
                // a 0.187 0.187 0 1 0 0.265 -0.265
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.265f,
                    dy1 = -0.265f,
                )
                // L 9.953 7.188
                lineTo(x = 9.953f, y = 7.188f)
                // h -0.53
                horizontalLineToRelative(dx = -0.53f)
                // l 2.062 2.062z
                lineToRelative(dx = 2.062f, dy = 2.062f)
                close()
            }
            // M13.813 6.813 h-2.36 L12.5 5.764 a.188 .188 0 0 0 -.265 -.265 L11 6.735 a.185 .185 0 0 0 -.047 .078 h-2.5 l3.297 -3.298 a.188 .188 0 0 0 -.265 -.265 L8.188 6.547 v-2.5 A.187 .187 0 0 0 8.264 4 L9.5 2.765 a.187 .187 0 1 0 -.265 -.265 L8.188 3.547 v-2.36 a.187 .187 0 1 0 -.375 0 v2.36 L6.764 2.5 a.187 .187 0 1 0 -.265 .265 L7.735 4 c.022 .022 .05 .038 .078 .047 v2.5 L4.515 3.25 a.187 .187 0 1 0 -.265 .265 l3.297 3.297 h-2.47 L3.766 5.5 a.187 .187 0 1 0 -.265 .265 l1.047 1.048 h-2.36 a.187 .187 0 1 0 0 .375 h2.36 L3.5 8.235 a.187 .187 0 1 0 .265 .265 l1.313 -1.312 h2.47 L4.25 10.485 a.187 .187 0 1 0 .265 .265 l3.298 -3.297 v2.5 a.187 .187 0 0 0 -.078 .047 L6.5 11.235 a.187 .187 0 1 0 .265 .265 l1.048 -1.047 v2.36 a.187 .187 0 0 0 .375 0 v-2.36 L9.235 11.5 a.188 .188 0 0 0 .265 -.265 L8.265 10 a.187 .187 0 0 0 -.078 -.047 v-2.5 l3.298 3.297 a.187 .187 0 0 0 .265 -.265 L8.453 7.188 h2.5 a.185 .185 0 0 0 .047 .077 L12.235 8.5 a.187 .187 0 1 0 .265 -.265 l-1.047 -1.047 h2.36 a.187 .187 0 1 0 0 -.375Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 13.813 6.813
                moveTo(x = 13.813f, y = 6.813f)
                // h -2.36
                horizontalLineToRelative(dx = -2.36f)
                // L 12.5 5.764
                lineTo(x = 12.5f, y = 5.764f)
                // a 0.188 0.188 0 0 0 -0.265 -0.265
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.265f,
                    dy1 = -0.265f,
                )
                // L 11 6.735
                lineTo(x = 11.0f, y = 6.735f)
                // a 0.185 0.185 0 0 0 -0.047 0.078
                arcToRelative(
                    a = 0.185f,
                    b = 0.185f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.047f,
                    dy1 = 0.078f,
                )
                // h -2.5
                horizontalLineToRelative(dx = -2.5f)
                // l 3.297 -3.298
                lineToRelative(dx = 3.297f, dy = -3.298f)
                // a 0.188 0.188 0 0 0 -0.265 -0.265
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.265f,
                    dy1 = -0.265f,
                )
                // L 8.188 6.547
                lineTo(x = 8.188f, y = 6.547f)
                // v -2.5
                verticalLineToRelative(dy = -2.5f)
                // A 0.187 0.187 0 0 0 8.264 4
                arcTo(
                    horizontalEllipseRadius = 0.187f,
                    verticalEllipseRadius = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 8.264f,
                    y1 = 4.0f,
                )
                // L 9.5 2.765
                lineTo(x = 9.5f, y = 2.765f)
                // a 0.187 0.187 0 1 0 -0.265 -0.265
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.265f,
                    dy1 = -0.265f,
                )
                // L 8.188 3.547
                lineTo(x = 8.188f, y = 3.547f)
                // v -2.36
                verticalLineToRelative(dy = -2.36f)
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
                // v 2.36
                verticalLineToRelative(dy = 2.36f)
                // L 6.764 2.5
                lineTo(x = 6.764f, y = 2.5f)
                // a 0.187 0.187 0 1 0 -0.265 0.265
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.265f,
                    dy1 = 0.265f,
                )
                // L 7.735 4
                lineTo(x = 7.735f, y = 4.0f)
                // c 0.022 0.022 0.05 0.038 0.078 0.047
                curveToRelative(
                    dx1 = 0.022f,
                    dy1 = 0.022f,
                    dx2 = 0.05f,
                    dy2 = 0.038f,
                    dx3 = 0.078f,
                    dy3 = 0.047f,
                )
                // v 2.5
                verticalLineToRelative(dy = 2.5f)
                // L 4.515 3.25
                lineTo(x = 4.515f, y = 3.25f)
                // a 0.187 0.187 0 1 0 -0.265 0.265
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.265f,
                    dy1 = 0.265f,
                )
                // l 3.297 3.297
                lineToRelative(dx = 3.297f, dy = 3.297f)
                // h -2.47
                horizontalLineToRelative(dx = -2.47f)
                // L 3.766 5.5
                lineTo(x = 3.766f, y = 5.5f)
                // a 0.187 0.187 0 1 0 -0.265 0.265
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.265f,
                    dy1 = 0.265f,
                )
                // l 1.047 1.048
                lineToRelative(dx = 1.047f, dy = 1.048f)
                // h -2.36
                horizontalLineToRelative(dx = -2.36f)
                // a 0.187 0.187 0 1 0 0 0.375
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.375f,
                )
                // h 2.36
                horizontalLineToRelative(dx = 2.36f)
                // L 3.5 8.235
                lineTo(x = 3.5f, y = 8.235f)
                // a 0.187 0.187 0 1 0 0.265 0.265
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.265f,
                    dy1 = 0.265f,
                )
                // l 1.313 -1.312
                lineToRelative(dx = 1.313f, dy = -1.312f)
                // h 2.47
                horizontalLineToRelative(dx = 2.47f)
                // L 4.25 10.485
                lineTo(x = 4.25f, y = 10.485f)
                // a 0.187 0.187 0 1 0 0.265 0.265
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.265f,
                    dy1 = 0.265f,
                )
                // l 3.298 -3.297
                lineToRelative(dx = 3.298f, dy = -3.297f)
                // v 2.5
                verticalLineToRelative(dy = 2.5f)
                // a 0.187 0.187 0 0 0 -0.078 0.047
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.078f,
                    dy1 = 0.047f,
                )
                // L 6.5 11.235
                lineTo(x = 6.5f, y = 11.235f)
                // a 0.187 0.187 0 1 0 0.265 0.265
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.265f,
                    dy1 = 0.265f,
                )
                // l 1.048 -1.047
                lineToRelative(dx = 1.048f, dy = -1.047f)
                // v 2.36
                verticalLineToRelative(dy = 2.36f)
                // a 0.187 0.187 0 0 0 0.375 0
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.375f,
                    dy1 = 0.0f,
                )
                // v -2.36
                verticalLineToRelative(dy = -2.36f)
                // L 9.235 11.5
                lineTo(x = 9.235f, y = 11.5f)
                // a 0.188 0.188 0 0 0 0.265 -0.265
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.265f,
                    dy1 = -0.265f,
                )
                // L 8.265 10
                lineTo(x = 8.265f, y = 10.0f)
                // a 0.187 0.187 0 0 0 -0.078 -0.047
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.078f,
                    dy1 = -0.047f,
                )
                // v -2.5
                verticalLineToRelative(dy = -2.5f)
                // l 3.298 3.297
                lineToRelative(dx = 3.298f, dy = 3.297f)
                // a 0.187 0.187 0 0 0 0.265 -0.265
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.265f,
                    dy1 = -0.265f,
                )
                // L 8.453 7.188
                lineTo(x = 8.453f, y = 7.188f)
                // h 2.5
                horizontalLineToRelative(dx = 2.5f)
                // a 0.185 0.185 0 0 0 0.047 0.077
                arcToRelative(
                    a = 0.185f,
                    b = 0.185f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.047f,
                    dy1 = 0.077f,
                )
                // L 12.235 8.5
                lineTo(x = 12.235f, y = 8.5f)
                // a 0.187 0.187 0 1 0 0.265 -0.265
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.265f,
                    dy1 = -0.265f,
                )
                // l -1.047 -1.047
                lineToRelative(dx = -1.047f, dy = -1.047f)
                // h 2.36
                horizontalLineToRelative(dx = 2.36f)
                // a 0.187 0.187 0 1 0 0 -0.375z
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.375f,
                )
                close()
            }
            // M.5 9.5 a.5 .5 0 0 1 .5 .5 v5 h14 v-5 a.5 .5 0 0 1 1 0 v6 H0 v-6 a.5 .5 0 0 1 .5 -.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0.5 9.5
                moveTo(x = 0.5f, y = 9.5f)
                // a 0.5 0.5 0 0 1 0.5 0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = 0.5f,
                )
                // v 5
                verticalLineToRelative(dy = 5.0f)
                // h 14
                horizontalLineToRelative(dx = 14.0f)
                // v -5
                verticalLineToRelative(dy = -5.0f)
                // a 0.5 0.5 0 0 1 1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                // v 6
                verticalLineToRelative(dy = 6.0f)
                // H 0
                horizontalLineTo(x = 0.0f)
                // v -6
                verticalLineToRelative(dy = -6.0f)
                // a 0.5 0.5 0 0 1 0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = -0.5f,
                )
                close()
            }
        }.build().also { _ic2379 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2379: ImageVector? = null
