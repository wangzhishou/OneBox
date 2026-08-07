package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2031: ImageVector
    get() {
        val current = _ic2031
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2031",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M6.36 0 c1.2 .028 2.157 1 2.16 2.19 a.406 .406 0 0 1 -.408 .404 .406 .406 0 0 1 -.407 -.405 A1.412 1.412 0 0 0 6.348 .808 h-.04 A1.396 1.396 0 0 0 4.91 2.194 v8.452 l-.216 .186 a2.464 2.464 0 0 0 -.289 3.487 c.595 .68 1.514 .99 2.403 .808 a2.456 2.456 0 0 0 1.115 -4.287 l-.22 -.186 V7.56 a.407 .407 0 0 1 .815 0 v2.72 a3.261 3.261 0 0 1 .986 3.303 3.293 3.293 0 0 1 -2.538 2.352 3.207 3.207 0 0 1 -.652 .065 3.306 3.306 0 0 1 -3.094 -2.102 3.263 3.263 0 0 1 .876 -3.619 V2.2 A2.21 2.21 0 0 1 6.304 0 h.056Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6.36 0
                moveTo(x = 6.36f, y = 0.0f)
                // c 1.2 0.028 2.157 1 2.16 2.19
                curveToRelative(
                    dx1 = 1.2f,
                    dy1 = 0.028f,
                    dx2 = 2.157f,
                    dy2 = 1.0f,
                    dx3 = 2.16f,
                    dy3 = 2.19f,
                )
                // a 0.406 0.406 0 0 1 -0.408 0.404
                arcToRelative(
                    a = 0.406f,
                    b = 0.406f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.408f,
                    dy1 = 0.404f,
                )
                // a 0.406 0.406 0 0 1 -0.407 -0.405
                arcToRelative(
                    a = 0.406f,
                    b = 0.406f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.407f,
                    dy1 = -0.405f,
                )
                // A 1.412 1.412 0 0 0 6.348 0.808
                arcTo(
                    horizontalEllipseRadius = 1.412f,
                    verticalEllipseRadius = 1.412f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 6.348f,
                    y1 = 0.808f,
                )
                // h -0.04
                horizontalLineToRelative(dx = -0.04f)
                // A 1.396 1.396 0 0 0 4.91 2.194
                arcTo(
                    horizontalEllipseRadius = 1.396f,
                    verticalEllipseRadius = 1.396f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 4.91f,
                    y1 = 2.194f,
                )
                // v 8.452
                verticalLineToRelative(dy = 8.452f)
                // l -0.216 0.186
                lineToRelative(dx = -0.216f, dy = 0.186f)
                // a 2.464 2.464 0 0 0 -0.289 3.487
                arcToRelative(
                    a = 2.464f,
                    b = 2.464f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.289f,
                    dy1 = 3.487f,
                )
                // c 0.595 0.68 1.514 0.99 2.403 0.808
                curveToRelative(
                    dx1 = 0.595f,
                    dy1 = 0.68f,
                    dx2 = 1.514f,
                    dy2 = 0.99f,
                    dx3 = 2.403f,
                    dy3 = 0.808f,
                )
                // a 2.456 2.456 0 0 0 1.115 -4.287
                arcToRelative(
                    a = 2.456f,
                    b = 2.456f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.115f,
                    dy1 = -4.287f,
                )
                // l -0.22 -0.186
                lineToRelative(dx = -0.22f, dy = -0.186f)
                // V 7.56
                verticalLineTo(y = 7.56f)
                // a 0.407 0.407 0 0 1 0.815 0
                arcToRelative(
                    a = 0.407f,
                    b = 0.407f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.815f,
                    dy1 = 0.0f,
                )
                // v 2.72
                verticalLineToRelative(dy = 2.72f)
                // a 3.261 3.261 0 0 1 0.986 3.303
                arcToRelative(
                    a = 3.261f,
                    b = 3.261f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.986f,
                    dy1 = 3.303f,
                )
                // a 3.293 3.293 0 0 1 -2.538 2.352
                arcToRelative(
                    a = 3.293f,
                    b = 3.293f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.538f,
                    dy1 = 2.352f,
                )
                // a 3.207 3.207 0 0 1 -0.652 0.065
                arcToRelative(
                    a = 3.207f,
                    b = 3.207f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.652f,
                    dy1 = 0.065f,
                )
                // a 3.306 3.306 0 0 1 -3.094 -2.102
                arcToRelative(
                    a = 3.306f,
                    b = 3.306f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.094f,
                    dy1 = -2.102f,
                )
                // a 3.263 3.263 0 0 1 0.876 -3.619
                arcToRelative(
                    a = 3.263f,
                    b = 3.263f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.876f,
                    dy1 = -3.619f,
                )
                // V 2.2
                verticalLineTo(y = 2.2f)
                // A 2.21 2.21 0 0 1 6.304 0
                arcTo(
                    horizontalEllipseRadius = 2.21f,
                    verticalEllipseRadius = 2.21f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 6.304f,
                    y1 = 0.0f,
                )
                // h 0.056z
                horizontalLineToRelative(dx = 0.056f)
                close()
            }
            // M6.31 11.862 a1.104 1.104 0 1 1 0 2.208 1.104 1.104 0 0 1 0 -2.208Z M12.5 4.5 a.5 .5 0 0 1 0 1 h-5 a.5 .5 0 1 1 0 -1 h5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6.31 11.862
                moveTo(x = 6.31f, y = 11.862f)
                // a 1.104 1.104 0 1 1 0 2.208
                arcToRelative(
                    a = 1.104f,
                    b = 1.104f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 2.208f,
                )
                // a 1.104 1.104 0 0 1 0 -2.208z
                arcToRelative(
                    a = 1.104f,
                    b = 1.104f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -2.208f,
                )
                close()
                // M 12.5 4.5
                moveTo(x = 12.5f, y = 4.5f)
                // a 0.5 0.5 0 0 1 0 1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                // h -5
                horizontalLineToRelative(dx = -5.0f)
                // a 0.5 0.5 0 1 1 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // h 5z
                horizontalLineToRelative(dx = 5.0f)
                close()
            }
        }.build().also { _ic2031 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2031: ImageVector? = null
