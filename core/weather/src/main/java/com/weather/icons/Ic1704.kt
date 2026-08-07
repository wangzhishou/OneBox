package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1704: ImageVector
    get() {
        val current = _ic1704
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1704",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M2.254 4.524 c-2.386 3.365 -.947 6.522 2 9.47 -1.438 .312 -2.421 .555 -3.754 .867 3.228 1.11 6.912 .729 9.58 -.798 a3.36 3.36 0 0 1 -.913 -2.289 3.372 3.372 0 0 1 3.368 -3.433 3.2 3.2 0 0 1 1.403 .311 c.316 -2.324 -.982 -4.474 -2.982 -6.555 C12.08 1.68 12.921 1.401 14.5 .811 9.202 .014 5.026 .57 2.254 4.524Z M7.482 6.12 c1.053 0 1.895 .832 1.895 1.873 0 1.04 -.842 1.874 -1.895 1.874 a1.878 1.878 0 0 1 -1.895 -1.874 c0 -1.04 .843 -1.873 1.895 -1.873Z m5.271 6.49 a.344 .344 0 1 1 0 .687 .344 .344 0 0 1 0 -.688Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 2.254 4.524
                moveTo(x = 2.254f, y = 4.524f)
                // c -2.386 3.365 -0.947 6.522 2 9.47
                curveToRelative(
                    dx1 = -2.386f,
                    dy1 = 3.365f,
                    dx2 = -0.947f,
                    dy2 = 6.522f,
                    dx3 = 2.0f,
                    dy3 = 9.47f,
                )
                // c -1.438 0.312 -2.421 0.555 -3.754 0.867
                curveToRelative(
                    dx1 = -1.438f,
                    dy1 = 0.312f,
                    dx2 = -2.421f,
                    dy2 = 0.555f,
                    dx3 = -3.754f,
                    dy3 = 0.867f,
                )
                // c 3.228 1.11 6.912 0.729 9.58 -0.798
                curveToRelative(
                    dx1 = 3.228f,
                    dy1 = 1.11f,
                    dx2 = 6.912f,
                    dy2 = 0.729f,
                    dx3 = 9.58f,
                    dy3 = -0.798f,
                )
                // a 3.36 3.36 0 0 1 -0.913 -2.289
                arcToRelative(
                    a = 3.36f,
                    b = 3.36f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.913f,
                    dy1 = -2.289f,
                )
                // a 3.372 3.372 0 0 1 3.368 -3.433
                arcToRelative(
                    a = 3.372f,
                    b = 3.372f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 3.368f,
                    dy1 = -3.433f,
                )
                // a 3.2 3.2 0 0 1 1.403 0.311
                arcToRelative(
                    a = 3.2f,
                    b = 3.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.403f,
                    dy1 = 0.311f,
                )
                // c 0.316 -2.324 -0.982 -4.474 -2.982 -6.555
                curveToRelative(
                    dx1 = 0.316f,
                    dy1 = -2.324f,
                    dx2 = -0.982f,
                    dy2 = -4.474f,
                    dx3 = -2.982f,
                    dy3 = -6.555f,
                )
                // C 12.08 1.68 12.921 1.401 14.5 0.811
                curveTo(
                    x1 = 12.08f,
                    y1 = 1.68f,
                    x2 = 12.921f,
                    y2 = 1.401f,
                    x3 = 14.5f,
                    y3 = 0.811f,
                )
                // C 9.202 0.014 5.026 0.57 2.254 4.524z
                curveTo(
                    x1 = 9.202f,
                    y1 = 0.014f,
                    x2 = 5.026f,
                    y2 = 0.57f,
                    x3 = 2.254f,
                    y3 = 4.524f,
                )
                close()
                // M 7.482 6.12
                moveTo(x = 7.482f, y = 6.12f)
                // c 1.053 0 1.895 0.832 1.895 1.873
                curveToRelative(
                    dx1 = 1.053f,
                    dy1 = 0.0f,
                    dx2 = 1.895f,
                    dy2 = 0.832f,
                    dx3 = 1.895f,
                    dy3 = 1.873f,
                )
                // c 0 1.04 -0.842 1.874 -1.895 1.874
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.04f,
                    dx2 = -0.842f,
                    dy2 = 1.874f,
                    dx3 = -1.895f,
                    dy3 = 1.874f,
                )
                // a 1.878 1.878 0 0 1 -1.895 -1.874
                arcToRelative(
                    a = 1.878f,
                    b = 1.878f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.895f,
                    dy1 = -1.874f,
                )
                // c 0 -1.04 0.843 -1.873 1.895 -1.873z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.04f,
                    dx2 = 0.843f,
                    dy2 = -1.873f,
                    dx3 = 1.895f,
                    dy3 = -1.873f,
                )
                close()
                // m 5.271 6.49
                moveToRelative(dx = 5.271f, dy = 6.49f)
                // a 0.344 0.344 0 1 1 0 0.687
                arcToRelative(
                    a = 0.344f,
                    b = 0.344f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.687f,
                )
                // a 0.344 0.344 0 0 1 0 -0.688z
                arcToRelative(
                    a = 0.344f,
                    b = 0.344f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -0.688f,
                )
                close()
            }
            // M12.75 10.203 c.252 0 .45 .167 .428 .36 l-.194 1.702 h-.468 l-.194 -1.702 c-.022 -.193 .176 -.36 .428 -.36Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 12.75 10.203
                moveTo(x = 12.75f, y = 10.203f)
                // c 0.252 0 0.45 0.167 0.428 0.36
                curveToRelative(
                    dx1 = 0.252f,
                    dy1 = 0.0f,
                    dx2 = 0.45f,
                    dy2 = 0.167f,
                    dx3 = 0.428f,
                    dy3 = 0.36f,
                )
                // l -0.194 1.702
                lineToRelative(dx = -0.194f, dy = 1.702f)
                // h -0.468
                horizontalLineToRelative(dx = -0.468f)
                // l -0.194 -1.702
                lineToRelative(dx = -0.194f, dy = -1.702f)
                // c -0.022 -0.193 0.176 -0.36 0.428 -0.36z
                curveToRelative(
                    dx1 = -0.022f,
                    dy1 = -0.193f,
                    dx2 = 0.176f,
                    dy2 = -0.36f,
                    dx3 = 0.428f,
                    dy3 = -0.36f,
                )
                close()
            }
            // M12.75 9 a2.75 2.75 0 1 1 0 5.5 2.75 2.75 0 0 1 0 -5.5Z m0 .447 a2.303 2.303 0 1 0 0 4.605 2.303 2.303 0 0 0 0 -4.605Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 12.75 9
                moveTo(x = 12.75f, y = 9.0f)
                // a 2.75 2.75 0 1 1 0 5.5
                arcToRelative(
                    a = 2.75f,
                    b = 2.75f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 5.5f,
                )
                // a 2.75 2.75 0 0 1 0 -5.5z
                arcToRelative(
                    a = 2.75f,
                    b = 2.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -5.5f,
                )
                close()
                // m 0 0.447
                moveToRelative(dx = 0.0f, dy = 0.447f)
                // a 2.303 2.303 0 1 0 0 4.605
                arcToRelative(
                    a = 2.303f,
                    b = 2.303f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 4.605f,
                )
                // a 2.303 2.303 0 0 0 0 -4.605z
                arcToRelative(
                    a = 2.303f,
                    b = 2.303f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -4.605f,
                )
                close()
            }
        }.build().also { _ic1704 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1704: ImageVector? = null
