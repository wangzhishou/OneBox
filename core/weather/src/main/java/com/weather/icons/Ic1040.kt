package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1040: ImageVector
    get() {
        val current = _ic1040
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1040",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.07 .933 a.933 .933 0 0 1 1.866 0 v1.45 l1.256 -.725 a.933 .933 0 0 1 .933 1.617 L9.87 4 l1.256 .725 a.933 .933 0 0 1 -.933 1.617 l-1.256 -.725 v1.45 a.933 .933 0 0 1 -1.867 0 v-1.45 l-1.255 .725 a.933 .933 0 1 1 -.934 -1.617 L6.136 4 4.88 3.275 a.933 .933 0 0 1 .934 -1.617 l1.255 .725 V.933Z M2.686 9.817 a.817 .817 0 0 1 1.633 0 v1.268 l1.099 -.634 a.817 .817 0 1 1 .816 1.415 l-1.098 .634 1.098 .634 a.817 .817 0 0 1 -.816 1.415 l-1.099 -.634 v1.268 a.817 .817 0 1 1 -1.633 0 v-1.268 l-1.1 .634 a.817 .817 0 0 1 -.816 -1.415 l1.1 -.634 -1.1 -.634 a.817 .817 0 0 1 .817 -1.415 l1.099 .634 V9.817Z M12.502 9 a.817 .817 0 0 0 -.816 .817 v1.268 l-1.1 -.634 a.817 .817 0 1 0 -.816 1.415 l1.099 .634 -1.099 .634 a.817 .817 0 0 0 .817 1.415 l1.099 -.634 v1.268 a.817 .817 0 1 0 1.633 0 v-1.268 l1.099 .634 a.817 .817 0 0 0 .817 -1.415 l-1.1 -.634 1.1 -.634 a.817 .817 0 0 0 -.817 -1.415 l-1.099 .634 V9.817 A.817 .817 0 0 0 12.502 9Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.07 0.933
                moveTo(x = 7.07f, y = 0.933f)
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
                // a 0.933 0.933 0 0 1 0.933 1.617
                arcToRelative(
                    a = 0.933f,
                    b = 0.933f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.933f,
                    dy1 = 1.617f,
                )
                // L 9.87 4
                lineTo(x = 9.87f, y = 4.0f)
                // l 1.256 0.725
                lineToRelative(dx = 1.256f, dy = 0.725f)
                // a 0.933 0.933 0 0 1 -0.933 1.617
                arcToRelative(
                    a = 0.933f,
                    b = 0.933f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
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
                // l -1.255 0.725
                lineToRelative(dx = -1.255f, dy = 0.725f)
                // a 0.933 0.933 0 1 1 -0.934 -1.617
                arcToRelative(
                    a = 0.933f,
                    b = 0.933f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.934f,
                    dy1 = -1.617f,
                )
                // L 6.136 4
                lineTo(x = 6.136f, y = 4.0f)
                // L 4.88 3.275
                lineTo(x = 4.88f, y = 3.275f)
                // a 0.933 0.933 0 0 1 0.934 -1.617
                arcToRelative(
                    a = 0.933f,
                    b = 0.933f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.934f,
                    dy1 = -1.617f,
                )
                // l 1.255 0.725
                lineToRelative(dx = 1.255f, dy = 0.725f)
                // V 0.933z
                verticalLineTo(y = 0.933f)
                close()
                // M 2.686 9.817
                moveTo(x = 2.686f, y = 9.817f)
                // a 0.817 0.817 0 0 1 1.633 0
                arcToRelative(
                    a = 0.817f,
                    b = 0.817f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.633f,
                    dy1 = 0.0f,
                )
                // v 1.268
                verticalLineToRelative(dy = 1.268f)
                // l 1.099 -0.634
                lineToRelative(dx = 1.099f, dy = -0.634f)
                // a 0.817 0.817 0 1 1 0.816 1.415
                arcToRelative(
                    a = 0.817f,
                    b = 0.817f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.816f,
                    dy1 = 1.415f,
                )
                // l -1.098 0.634
                lineToRelative(dx = -1.098f, dy = 0.634f)
                // l 1.098 0.634
                lineToRelative(dx = 1.098f, dy = 0.634f)
                // a 0.817 0.817 0 0 1 -0.816 1.415
                arcToRelative(
                    a = 0.817f,
                    b = 0.817f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.816f,
                    dy1 = 1.415f,
                )
                // l -1.099 -0.634
                lineToRelative(dx = -1.099f, dy = -0.634f)
                // v 1.268
                verticalLineToRelative(dy = 1.268f)
                // a 0.817 0.817 0 1 1 -1.633 0
                arcToRelative(
                    a = 0.817f,
                    b = 0.817f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.633f,
                    dy1 = 0.0f,
                )
                // v -1.268
                verticalLineToRelative(dy = -1.268f)
                // l -1.1 0.634
                lineToRelative(dx = -1.1f, dy = 0.634f)
                // a 0.817 0.817 0 0 1 -0.816 -1.415
                arcToRelative(
                    a = 0.817f,
                    b = 0.817f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.816f,
                    dy1 = -1.415f,
                )
                // l 1.1 -0.634
                lineToRelative(dx = 1.1f, dy = -0.634f)
                // l -1.1 -0.634
                lineToRelative(dx = -1.1f, dy = -0.634f)
                // a 0.817 0.817 0 0 1 0.817 -1.415
                arcToRelative(
                    a = 0.817f,
                    b = 0.817f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.817f,
                    dy1 = -1.415f,
                )
                // l 1.099 0.634
                lineToRelative(dx = 1.099f, dy = 0.634f)
                // V 9.817z
                verticalLineTo(y = 9.817f)
                close()
                // M 12.502 9
                moveTo(x = 12.502f, y = 9.0f)
                // a 0.817 0.817 0 0 0 -0.816 0.817
                arcToRelative(
                    a = 0.817f,
                    b = 0.817f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.816f,
                    dy1 = 0.817f,
                )
                // v 1.268
                verticalLineToRelative(dy = 1.268f)
                // l -1.1 -0.634
                lineToRelative(dx = -1.1f, dy = -0.634f)
                // a 0.817 0.817 0 1 0 -0.816 1.415
                arcToRelative(
                    a = 0.817f,
                    b = 0.817f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.816f,
                    dy1 = 1.415f,
                )
                // l 1.099 0.634
                lineToRelative(dx = 1.099f, dy = 0.634f)
                // l -1.099 0.634
                lineToRelative(dx = -1.099f, dy = 0.634f)
                // a 0.817 0.817 0 0 0 0.817 1.415
                arcToRelative(
                    a = 0.817f,
                    b = 0.817f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.817f,
                    dy1 = 1.415f,
                )
                // l 1.099 -0.634
                lineToRelative(dx = 1.099f, dy = -0.634f)
                // v 1.268
                verticalLineToRelative(dy = 1.268f)
                // a 0.817 0.817 0 1 0 1.633 0
                arcToRelative(
                    a = 0.817f,
                    b = 0.817f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 1.633f,
                    dy1 = 0.0f,
                )
                // v -1.268
                verticalLineToRelative(dy = -1.268f)
                // l 1.099 0.634
                lineToRelative(dx = 1.099f, dy = 0.634f)
                // a 0.817 0.817 0 0 0 0.817 -1.415
                arcToRelative(
                    a = 0.817f,
                    b = 0.817f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.817f,
                    dy1 = -1.415f,
                )
                // l -1.1 -0.634
                lineToRelative(dx = -1.1f, dy = -0.634f)
                // l 1.1 -0.634
                lineToRelative(dx = 1.1f, dy = -0.634f)
                // a 0.817 0.817 0 0 0 -0.817 -1.415
                arcToRelative(
                    a = 0.817f,
                    b = 0.817f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.817f,
                    dy1 = -1.415f,
                )
                // l -1.099 0.634
                lineToRelative(dx = -1.099f, dy = 0.634f)
                // V 9.817
                verticalLineTo(y = 9.817f)
                // A 0.817 0.817 0 0 0 12.502 9z
                arcTo(
                    horizontalEllipseRadius = 0.817f,
                    verticalEllipseRadius = 0.817f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 12.502f,
                    y1 = 9.0f,
                )
                close()
            }
        }.build().also { _ic1040 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1040: ImageVector? = null
