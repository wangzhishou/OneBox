package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic104Fill: ImageVector
    get() {
        val current = _ic104Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic104Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M11.727 14.217 A4.99 4.99 0 0 1 7.9 16 a4.988 4.988 0 0 1 -3.773 -1.719 3 3 0 1 1 -.586 -5.732 A4.998 4.998 0 0 1 7.9 6 a4.999 4.999 0 0 1 4.38 2.587 3 3 0 1 1 -.553 5.63Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.727 14.217
                moveTo(x = 11.727f, y = 14.217f)
                // A 4.99 4.99 0 0 1 7.9 16
                arcTo(
                    horizontalEllipseRadius = 4.99f,
                    verticalEllipseRadius = 4.99f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.9f,
                    y1 = 16.0f,
                )
                // a 4.988 4.988 0 0 1 -3.773 -1.719
                arcToRelative(
                    a = 4.988f,
                    b = 4.988f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.773f,
                    dy1 = -1.719f,
                )
                // a 3 3 0 1 1 -0.586 -5.732
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.586f,
                    dy1 = -5.732f,
                )
                // A 4.998 4.998 0 0 1 7.9 6
                arcTo(
                    horizontalEllipseRadius = 4.998f,
                    verticalEllipseRadius = 4.998f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.9f,
                    y1 = 6.0f,
                )
                // a 4.999 4.999 0 0 1 4.38 2.587
                arcToRelative(
                    a = 4.999f,
                    b = 4.999f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 4.38f,
                    dy1 = 2.587f,
                )
                // a 3 3 0 1 1 -0.553 5.63z
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.553f,
                    dy1 = 5.63f,
                )
                close()
            }
            // M4.008 6.637 a1.545 1.545 0 0 1 1.54 -1.467 .913 .913 0 0 1 .108 .012 l.084 .012 a1 1 0 0 0 .961 -.445 2.74 2.74 0 0 1 4.598 0 1 1 0 0 0 .961 .445 l.084 -.012 a.916 .916 0 0 1 .108 -.012 1.524 1.524 0 0 1 1.455 2.048 c.312 .135 .602 .316 .86 .538 A2.484 2.484 0 0 0 12.136 4.2 a3.74 3.74 0 0 0 -6.27 0 2.506 2.506 0 0 0 -.317 -.032 A2.548 2.548 0 0 0 3 6.717 c.005 .174 .028 .347 .069 .517 .238 -.3 .569 -.51 .94 -.597 h-.001Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.008 6.637
                moveTo(x = 4.008f, y = 6.637f)
                // a 1.545 1.545 0 0 1 1.54 -1.467
                arcToRelative(
                    a = 1.545f,
                    b = 1.545f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.54f,
                    dy1 = -1.467f,
                )
                // a 0.913 0.913 0 0 1 0.108 0.012
                arcToRelative(
                    a = 0.913f,
                    b = 0.913f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.108f,
                    dy1 = 0.012f,
                )
                // l 0.084 0.012
                lineToRelative(dx = 0.084f, dy = 0.012f)
                // a 1 1 0 0 0 0.961 -0.445
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.961f,
                    dy1 = -0.445f,
                )
                // a 2.74 2.74 0 0 1 4.598 0
                arcToRelative(
                    a = 2.74f,
                    b = 2.74f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 4.598f,
                    dy1 = 0.0f,
                )
                // a 1 1 0 0 0 0.961 0.445
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.961f,
                    dy1 = 0.445f,
                )
                // l 0.084 -0.012
                lineToRelative(dx = 0.084f, dy = -0.012f)
                // a 0.916 0.916 0 0 1 0.108 -0.012
                arcToRelative(
                    a = 0.916f,
                    b = 0.916f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.108f,
                    dy1 = -0.012f,
                )
                // a 1.524 1.524 0 0 1 1.455 2.048
                arcToRelative(
                    a = 1.524f,
                    b = 1.524f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.455f,
                    dy1 = 2.048f,
                )
                // c 0.312 0.135 0.602 0.316 0.86 0.538
                curveToRelative(
                    dx1 = 0.312f,
                    dy1 = 0.135f,
                    dx2 = 0.602f,
                    dy2 = 0.316f,
                    dx3 = 0.86f,
                    dy3 = 0.538f,
                )
                // A 2.484 2.484 0 0 0 12.136 4.2
                arcTo(
                    horizontalEllipseRadius = 2.484f,
                    verticalEllipseRadius = 2.484f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 12.136f,
                    y1 = 4.2f,
                )
                // a 3.74 3.74 0 0 0 -6.27 0
                arcToRelative(
                    a = 3.74f,
                    b = 3.74f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -6.27f,
                    dy1 = 0.0f,
                )
                // a 2.506 2.506 0 0 0 -0.317 -0.032
                arcToRelative(
                    a = 2.506f,
                    b = 2.506f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.317f,
                    dy1 = -0.032f,
                )
                // A 2.548 2.548 0 0 0 3 6.717
                arcTo(
                    horizontalEllipseRadius = 2.548f,
                    verticalEllipseRadius = 2.548f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 3.0f,
                    y1 = 6.717f,
                )
                // c 0.005 0.174 0.028 0.347 0.069 0.517
                curveToRelative(
                    dx1 = 0.005f,
                    dy1 = 0.174f,
                    dx2 = 0.028f,
                    dy2 = 0.347f,
                    dx3 = 0.069f,
                    dy3 = 0.517f,
                )
                // c 0.238 -0.3 0.569 -0.51 0.94 -0.597
                curveToRelative(
                    dx1 = 0.238f,
                    dy1 = -0.3f,
                    dx2 = 0.569f,
                    dy2 = -0.51f,
                    dx3 = 0.94f,
                    dy3 = -0.597f,
                )
                // h -0.001z
                horizontalLineToRelative(dx = -0.001f)
                close()
            }
        }.build().also { _ic104Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic104Fill: ImageVector? = null
