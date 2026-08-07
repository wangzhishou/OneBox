package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1027: ImageVector
    get() {
        val current = _ic1027
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1027",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.07 4.933 a.933 .933 0 0 1 1.866 0 v1.45 l1.256 -.725 a.933 .933 0 1 1 .933 1.617 L9.87 8 l1.256 .725 a.933 .933 0 1 1 -.933 1.617 l-1.256 -.725 v1.45 a.933 .933 0 0 1 -1.867 0 v-1.45 l-1.256 .725 a.933 .933 0 1 1 -.933 -1.617 L6.136 8 4.88 7.275 a.933 .933 0 0 1 .933 -1.617 l1.256 .725 v-1.45Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.07 4.933
                moveTo(x = 7.07f, y = 4.933f)
                // a 0.933 0.933 0 0 1 1.866 0
                arcToRelative(
                    a = 0.933f,
                    b = 0.933f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.866f,
                    dy1 = 0.0f,
                )
                // v 1.45
                verticalLineToRelative(dy = 1.45f)
                // l 1.256 -0.725
                lineToRelative(dx = 1.256f, dy = -0.725f)
                // a 0.933 0.933 0 1 1 0.933 1.617
                arcToRelative(
                    a = 0.933f,
                    b = 0.933f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.933f,
                    dy1 = 1.617f,
                )
                // L 9.87 8
                lineTo(x = 9.87f, y = 8.0f)
                // l 1.256 0.725
                lineToRelative(dx = 1.256f, dy = 0.725f)
                // a 0.933 0.933 0 1 1 -0.933 1.617
                arcToRelative(
                    a = 0.933f,
                    b = 0.933f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.933f,
                    dy1 = 1.617f,
                )
                // l -1.256 -0.725
                lineToRelative(dx = -1.256f, dy = -0.725f)
                // v 1.45
                verticalLineToRelative(dy = 1.45f)
                // a 0.933 0.933 0 0 1 -1.867 0
                arcToRelative(
                    a = 0.933f,
                    b = 0.933f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.867f,
                    dy1 = 0.0f,
                )
                // v -1.45
                verticalLineToRelative(dy = -1.45f)
                // l -1.256 0.725
                lineToRelative(dx = -1.256f, dy = 0.725f)
                // a 0.933 0.933 0 1 1 -0.933 -1.617
                arcToRelative(
                    a = 0.933f,
                    b = 0.933f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.933f,
                    dy1 = -1.617f,
                )
                // L 6.136 8
                lineTo(x = 6.136f, y = 8.0f)
                // L 4.88 7.275
                lineTo(x = 4.88f, y = 7.275f)
                // a 0.933 0.933 0 0 1 0.933 -1.617
                arcToRelative(
                    a = 0.933f,
                    b = 0.933f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.933f,
                    dy1 = -1.617f,
                )
                // l 1.256 0.725
                lineToRelative(dx = 1.256f, dy = 0.725f)
                // v -1.45z
                verticalLineToRelative(dy = -1.45f)
                close()
            }
            // M14.649 3.743 8.35 .095 a.693 .693 0 0 0 -.701 0 L1.351 3.743 a.705 .705 0 0 0 -.351 .61 v7.295 c0 .251 .134 .483 .351 .61 l6.298 3.647 a.688 .688 0 0 0 .701 0 l6.298 -3.648 a.702 .702 0 0 0 .352 -.609 V4.352 a.705 .705 0 0 0 -.351 -.609Z m-1.052 7.5 L8 14.483 l-5.597 -3.242 V4.76 L8 1.516 l5.597 3.242 v6.484Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 14.649 3.743
                moveTo(x = 14.649f, y = 3.743f)
                // L 8.35 0.095
                lineTo(x = 8.35f, y = 0.095f)
                // a 0.693 0.693 0 0 0 -0.701 0
                arcToRelative(
                    a = 0.693f,
                    b = 0.693f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.701f,
                    dy1 = 0.0f,
                )
                // L 1.351 3.743
                lineTo(x = 1.351f, y = 3.743f)
                // a 0.705 0.705 0 0 0 -0.351 0.61
                arcToRelative(
                    a = 0.705f,
                    b = 0.705f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.351f,
                    dy1 = 0.61f,
                )
                // v 7.295
                verticalLineToRelative(dy = 7.295f)
                // c 0 0.251 0.134 0.483 0.351 0.61
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.251f,
                    dx2 = 0.134f,
                    dy2 = 0.483f,
                    dx3 = 0.351f,
                    dy3 = 0.61f,
                )
                // l 6.298 3.647
                lineToRelative(dx = 6.298f, dy = 3.647f)
                // a 0.688 0.688 0 0 0 0.701 0
                arcToRelative(
                    a = 0.688f,
                    b = 0.688f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.701f,
                    dy1 = 0.0f,
                )
                // l 6.298 -3.648
                lineToRelative(dx = 6.298f, dy = -3.648f)
                // a 0.702 0.702 0 0 0 0.352 -0.609
                arcToRelative(
                    a = 0.702f,
                    b = 0.702f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.352f,
                    dy1 = -0.609f,
                )
                // V 4.352
                verticalLineTo(y = 4.352f)
                // a 0.705 0.705 0 0 0 -0.351 -0.609z
                arcToRelative(
                    a = 0.705f,
                    b = 0.705f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.351f,
                    dy1 = -0.609f,
                )
                close()
                // m -1.052 7.5
                moveToRelative(dx = -1.052f, dy = 7.5f)
                // L 8 14.483
                lineTo(x = 8.0f, y = 14.483f)
                // l -5.597 -3.242
                lineToRelative(dx = -5.597f, dy = -3.242f)
                // V 4.76
                verticalLineTo(y = 4.76f)
                // L 8 1.516
                lineTo(x = 8.0f, y = 1.516f)
                // l 5.597 3.242
                lineToRelative(dx = 5.597f, dy = 3.242f)
                // v 6.484z
                verticalLineToRelative(dy = 6.484f)
                close()
            }
        }.build().also { _ic1027 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1027: ImageVector? = null
