package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1241: ImageVector
    get() {
        val current = _ic1241
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1241",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // m.421 16 8 -16 v3.2 l1.263 2.133 L8.421 6.4 l-.842 3.2 .842 1.067 -.842 2.666 2.105 1.6 .842 1.067 H.421Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0.421 16
                moveTo(x = 0.421f, y = 16.0f)
                // l 8 -16
                lineToRelative(dx = 8.0f, dy = -16.0f)
                // v 3.2
                verticalLineToRelative(dy = 3.2f)
                // l 1.263 2.133
                lineToRelative(dx = 1.263f, dy = 2.133f)
                // L 8.421 6.4
                lineTo(x = 8.421f, y = 6.4f)
                // l -0.842 3.2
                lineToRelative(dx = -0.842f, dy = 3.2f)
                // l 0.842 1.067
                lineToRelative(dx = 0.842f, dy = 1.067f)
                // l -0.842 2.666
                lineToRelative(dx = -0.842f, dy = 2.666f)
                // l 2.105 1.6
                lineToRelative(dx = 2.105f, dy = 1.6f)
                // l 0.842 1.067
                lineToRelative(dx = 0.842f, dy = 1.067f)
                // H 0.421z
                horizontalLineTo(x = 0.421f)
                close()
            }
            // M13.164 4.19 a.197 .197 0 0 1 .329 .087 l1.45 5.421 a.195 .195 0 0 1 -.24 .237 l-5.416 -1.45 a.195 .195 0 0 1 -.09 -.326 l3.967 -3.97Z m-.15 1.216 c-.266 -.07 -.521 .049 -.553 .26 l-.277 1.848 .495 .132 .684 -1.739 c.078 -.198 -.083 -.43 -.35 -.5Z m-.87 3.261 a.375 .375 0 1 0 .193 -.724 .375 .375 0 0 0 -.194 .724Z m-1.618 -4.4 -.42 -2.134 1.683 2.134 h-1.263Z m2.527 5.866 -.842 1.6 1.684 1.067 -.842 -2.667Z m-3.369 3.734 .842 -1.6 2.948 2.133 -2.106 1.067 -1.684 -1.6Z m5.053 .533 .842 -1.067 .421 2.134 -1.263 -1.067Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 13.164 4.19
                moveTo(x = 13.164f, y = 4.19f)
                // a 0.197 0.197 0 0 1 0.329 0.087
                arcToRelative(
                    a = 0.197f,
                    b = 0.197f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.329f,
                    dy1 = 0.087f,
                )
                // l 1.45 5.421
                lineToRelative(dx = 1.45f, dy = 5.421f)
                // a 0.195 0.195 0 0 1 -0.24 0.237
                arcToRelative(
                    a = 0.195f,
                    b = 0.195f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.24f,
                    dy1 = 0.237f,
                )
                // l -5.416 -1.45
                lineToRelative(dx = -5.416f, dy = -1.45f)
                // a 0.195 0.195 0 0 1 -0.09 -0.326
                arcToRelative(
                    a = 0.195f,
                    b = 0.195f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.09f,
                    dy1 = -0.326f,
                )
                // l 3.967 -3.97z
                lineToRelative(dx = 3.967f, dy = -3.97f)
                close()
                // m -0.15 1.216
                moveToRelative(dx = -0.15f, dy = 1.216f)
                // c -0.266 -0.07 -0.521 0.049 -0.553 0.26
                curveToRelative(
                    dx1 = -0.266f,
                    dy1 = -0.07f,
                    dx2 = -0.521f,
                    dy2 = 0.049f,
                    dx3 = -0.553f,
                    dy3 = 0.26f,
                )
                // l -0.277 1.848
                lineToRelative(dx = -0.277f, dy = 1.848f)
                // l 0.495 0.132
                lineToRelative(dx = 0.495f, dy = 0.132f)
                // l 0.684 -1.739
                lineToRelative(dx = 0.684f, dy = -1.739f)
                // c 0.078 -0.198 -0.083 -0.43 -0.35 -0.5z
                curveToRelative(
                    dx1 = 0.078f,
                    dy1 = -0.198f,
                    dx2 = -0.083f,
                    dy2 = -0.43f,
                    dx3 = -0.35f,
                    dy3 = -0.5f,
                )
                close()
                // m -0.87 3.261
                moveToRelative(dx = -0.87f, dy = 3.261f)
                // a 0.375 0.375 0 1 0 0.193 -0.724
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.193f,
                    dy1 = -0.724f,
                )
                // a 0.375 0.375 0 0 0 -0.194 0.724z
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.194f,
                    dy1 = 0.724f,
                )
                close()
                // m -1.618 -4.4
                moveToRelative(dx = -1.618f, dy = -4.4f)
                // l -0.42 -2.134
                lineToRelative(dx = -0.42f, dy = -2.134f)
                // l 1.683 2.134
                lineToRelative(dx = 1.683f, dy = 2.134f)
                // h -1.263z
                horizontalLineToRelative(dx = -1.263f)
                close()
                // m 2.527 5.866
                moveToRelative(dx = 2.527f, dy = 5.866f)
                // l -0.842 1.6
                lineToRelative(dx = -0.842f, dy = 1.6f)
                // l 1.684 1.067
                lineToRelative(dx = 1.684f, dy = 1.067f)
                // l -0.842 -2.667z
                lineToRelative(dx = -0.842f, dy = -2.667f)
                close()
                // m -3.369 3.734
                moveToRelative(dx = -3.369f, dy = 3.734f)
                // l 0.842 -1.6
                lineToRelative(dx = 0.842f, dy = -1.6f)
                // l 2.948 2.133
                lineToRelative(dx = 2.948f, dy = 2.133f)
                // l -2.106 1.067
                lineToRelative(dx = -2.106f, dy = 1.067f)
                // l -1.684 -1.6z
                lineToRelative(dx = -1.684f, dy = -1.6f)
                close()
                // m 5.053 0.533
                moveToRelative(dx = 5.053f, dy = 0.533f)
                // l 0.842 -1.067
                lineToRelative(dx = 0.842f, dy = -1.067f)
                // l 0.421 2.134
                lineToRelative(dx = 0.421f, dy = 2.134f)
                // l -1.263 -1.067z
                lineToRelative(dx = -1.263f, dy = -1.067f)
                close()
            }
        }.build().also { _ic1241 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1241: ImageVector? = null
