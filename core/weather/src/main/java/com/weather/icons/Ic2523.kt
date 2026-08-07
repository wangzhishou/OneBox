package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2523: ImageVector
    get() {
        val current = _ic2523
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2523",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.435 13.27 a.433 .433 0 0 1 .433 .432 v.978 a.435 .435 0 0 1 -.74 .306 .434 .434 0 0 1 -.126 -.306 v-.978 a.433 .433 0 0 1 .433 -.433Z m-5.216 -.67 a.433 .433 0 0 1 .432 .433 v1.149 a.432 .432 0 0 1 -.866 0 v-1.149 a.433 .433 0 0 1 .434 -.432Z m8.873 .012 a.433 .433 0 0 1 .433 .433 v.985 a.434 .434 0 0 1 -.433 .434 l-.084 -.009 a.433 .433 0 0 1 -.35 -.425 v-.985 a.434 .434 0 0 1 .434 -.433Z m-6.704 -1.216 a.432 .432 0 0 1 .433 .433 v1.387 a.433 .433 0 0 1 -.866 0 v-1.387 a.434 .434 0 0 1 .433 -.433Z m4.729 -.974 a.434 .434 0 0 1 .433 .432 v1.435 a.433 .433 0 0 1 -.433 .433 l-.085 -.01 a.432 .432 0 0 1 -.348 -.423 v-1.435 a.434 .434 0 0 1 .433 -.432Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.435 13.27
                moveTo(x = 8.435f, y = 13.27f)
                // a 0.433 0.433 0 0 1 0.433 0.432
                arcToRelative(
                    a = 0.433f,
                    b = 0.433f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.433f,
                    dy1 = 0.432f,
                )
                // v 0.978
                verticalLineToRelative(dy = 0.978f)
                // a 0.435 0.435 0 0 1 -0.74 0.306
                arcToRelative(
                    a = 0.435f,
                    b = 0.435f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.74f,
                    dy1 = 0.306f,
                )
                // a 0.434 0.434 0 0 1 -0.126 -0.306
                arcToRelative(
                    a = 0.434f,
                    b = 0.434f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.126f,
                    dy1 = -0.306f,
                )
                // v -0.978
                verticalLineToRelative(dy = -0.978f)
                // a 0.433 0.433 0 0 1 0.433 -0.433z
                arcToRelative(
                    a = 0.433f,
                    b = 0.433f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.433f,
                    dy1 = -0.433f,
                )
                close()
                // m -5.216 -0.67
                moveToRelative(dx = -5.216f, dy = -0.67f)
                // a 0.433 0.433 0 0 1 0.432 0.433
                arcToRelative(
                    a = 0.433f,
                    b = 0.433f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.432f,
                    dy1 = 0.433f,
                )
                // v 1.149
                verticalLineToRelative(dy = 1.149f)
                // a 0.432 0.432 0 0 1 -0.866 0
                arcToRelative(
                    a = 0.432f,
                    b = 0.432f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.866f,
                    dy1 = 0.0f,
                )
                // v -1.149
                verticalLineToRelative(dy = -1.149f)
                // a 0.433 0.433 0 0 1 0.434 -0.432z
                arcToRelative(
                    a = 0.433f,
                    b = 0.433f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.434f,
                    dy1 = -0.432f,
                )
                close()
                // m 8.873 0.012
                moveToRelative(dx = 8.873f, dy = 0.012f)
                // a 0.433 0.433 0 0 1 0.433 0.433
                arcToRelative(
                    a = 0.433f,
                    b = 0.433f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.433f,
                    dy1 = 0.433f,
                )
                // v 0.985
                verticalLineToRelative(dy = 0.985f)
                // a 0.434 0.434 0 0 1 -0.433 0.434
                arcToRelative(
                    a = 0.434f,
                    b = 0.434f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.433f,
                    dy1 = 0.434f,
                )
                // l -0.084 -0.009
                lineToRelative(dx = -0.084f, dy = -0.009f)
                // a 0.433 0.433 0 0 1 -0.35 -0.425
                arcToRelative(
                    a = 0.433f,
                    b = 0.433f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.35f,
                    dy1 = -0.425f,
                )
                // v -0.985
                verticalLineToRelative(dy = -0.985f)
                // a 0.434 0.434 0 0 1 0.434 -0.433z
                arcToRelative(
                    a = 0.434f,
                    b = 0.434f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.434f,
                    dy1 = -0.433f,
                )
                close()
                // m -6.704 -1.216
                moveToRelative(dx = -6.704f, dy = -1.216f)
                // a 0.432 0.432 0 0 1 0.433 0.433
                arcToRelative(
                    a = 0.432f,
                    b = 0.432f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.433f,
                    dy1 = 0.433f,
                )
                // v 1.387
                verticalLineToRelative(dy = 1.387f)
                // a 0.433 0.433 0 0 1 -0.866 0
                arcToRelative(
                    a = 0.433f,
                    b = 0.433f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.866f,
                    dy1 = 0.0f,
                )
                // v -1.387
                verticalLineToRelative(dy = -1.387f)
                // a 0.434 0.434 0 0 1 0.433 -0.433z
                arcToRelative(
                    a = 0.434f,
                    b = 0.434f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.433f,
                    dy1 = -0.433f,
                )
                close()
                // m 4.729 -0.974
                moveToRelative(dx = 4.729f, dy = -0.974f)
                // a 0.434 0.434 0 0 1 0.433 0.432
                arcToRelative(
                    a = 0.434f,
                    b = 0.434f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.433f,
                    dy1 = 0.432f,
                )
                // v 1.435
                verticalLineToRelative(dy = 1.435f)
                // a 0.433 0.433 0 0 1 -0.433 0.433
                arcToRelative(
                    a = 0.433f,
                    b = 0.433f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.433f,
                    dy1 = 0.433f,
                )
                // l -0.085 -0.01
                lineToRelative(dx = -0.085f, dy = -0.01f)
                // a 0.432 0.432 0 0 1 -0.348 -0.423
                arcToRelative(
                    a = 0.432f,
                    b = 0.432f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.348f,
                    dy1 = -0.423f,
                )
                // v -1.435
                verticalLineToRelative(dy = -1.435f)
                // a 0.434 0.434 0 0 1 0.433 -0.432z
                arcToRelative(
                    a = 0.434f,
                    b = 0.434f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.433f,
                    dy1 = -0.432f,
                )
                close()
            }
            // M7.767 1.005 A4.173 4.173 0 0 1 12.07 4.67 a3.603 3.603 0 0 1 3.587 5.127 3.603 3.603 0 0 1 -1.764 1.737 .435 .435 0 0 1 -.18 .04 l-.074 -.006 a.433 .433 0 0 1 -.106 -.821 2.735 2.735 0 0 0 -1.766 -5.151 .433 .433 0 0 1 -.533 -.422 3.307 3.307 0 0 0 -6.604 -.24 3.604 3.604 0 0 1 2.117 1.693 .433 .433 0 1 1 -.756 .423 2.738 2.738 0 0 0 -1.926 -1.362 2.736 2.736 0 0 0 -2.092 4.895 .435 .435 0 0 1 -.003 .7 .434 .434 0 0 1 -.511 -.004 A3.603 3.603 0 0 1 2.62 4.92 c.375 -.105 .763 -.15 1.15 -.131 a4.174 4.174 0 0 1 3.996 -3.784Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.767 1.005
                moveTo(x = 7.767f, y = 1.005f)
                // A 4.173 4.173 0 0 1 12.07 4.67
                arcTo(
                    horizontalEllipseRadius = 4.173f,
                    verticalEllipseRadius = 4.173f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 12.07f,
                    y1 = 4.67f,
                )
                // a 3.603 3.603 0 0 1 3.587 5.127
                arcToRelative(
                    a = 3.603f,
                    b = 3.603f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 3.587f,
                    dy1 = 5.127f,
                )
                // a 3.603 3.603 0 0 1 -1.764 1.737
                arcToRelative(
                    a = 3.603f,
                    b = 3.603f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.764f,
                    dy1 = 1.737f,
                )
                // a 0.435 0.435 0 0 1 -0.18 0.04
                arcToRelative(
                    a = 0.435f,
                    b = 0.435f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.18f,
                    dy1 = 0.04f,
                )
                // l -0.074 -0.006
                lineToRelative(dx = -0.074f, dy = -0.006f)
                // a 0.433 0.433 0 0 1 -0.106 -0.821
                arcToRelative(
                    a = 0.433f,
                    b = 0.433f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.106f,
                    dy1 = -0.821f,
                )
                // a 2.735 2.735 0 0 0 -1.766 -5.151
                arcToRelative(
                    a = 2.735f,
                    b = 2.735f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.766f,
                    dy1 = -5.151f,
                )
                // a 0.433 0.433 0 0 1 -0.533 -0.422
                arcToRelative(
                    a = 0.433f,
                    b = 0.433f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.533f,
                    dy1 = -0.422f,
                )
                // a 3.307 3.307 0 0 0 -6.604 -0.24
                arcToRelative(
                    a = 3.307f,
                    b = 3.307f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -6.604f,
                    dy1 = -0.24f,
                )
                // a 3.604 3.604 0 0 1 2.117 1.693
                arcToRelative(
                    a = 3.604f,
                    b = 3.604f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.117f,
                    dy1 = 1.693f,
                )
                // a 0.433 0.433 0 1 1 -0.756 0.423
                arcToRelative(
                    a = 0.433f,
                    b = 0.433f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.756f,
                    dy1 = 0.423f,
                )
                // a 2.738 2.738 0 0 0 -1.926 -1.362
                arcToRelative(
                    a = 2.738f,
                    b = 2.738f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.926f,
                    dy1 = -1.362f,
                )
                // a 2.736 2.736 0 0 0 -2.092 4.895
                arcToRelative(
                    a = 2.736f,
                    b = 2.736f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.092f,
                    dy1 = 4.895f,
                )
                // a 0.435 0.435 0 0 1 -0.003 0.7
                arcToRelative(
                    a = 0.435f,
                    b = 0.435f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.003f,
                    dy1 = 0.7f,
                )
                // a 0.434 0.434 0 0 1 -0.511 -0.004
                arcToRelative(
                    a = 0.434f,
                    b = 0.434f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.511f,
                    dy1 = -0.004f,
                )
                // A 3.603 3.603 0 0 1 2.62 4.92
                arcTo(
                    horizontalEllipseRadius = 3.603f,
                    verticalEllipseRadius = 3.603f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 2.62f,
                    y1 = 4.92f,
                )
                // c 0.375 -0.105 0.763 -0.15 1.15 -0.131
                curveToRelative(
                    dx1 = 0.375f,
                    dy1 = -0.105f,
                    dx2 = 0.763f,
                    dy2 = -0.15f,
                    dx3 = 1.15f,
                    dy3 = -0.131f,
                )
                // a 4.174 4.174 0 0 1 3.996 -3.784z
                arcToRelative(
                    a = 4.174f,
                    b = 4.174f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 3.996f,
                    dy1 = -3.784f,
                )
                close()
            }
            // M7.586 9.607 a.433 .433 0 0 1 .448 .417 l.038 1.057 a.434 .434 0 0 1 -.417 .448 h-.016 l-.083 -.007 a.435 .435 0 0 1 -.35 -.41 l-.037 -1.056 a.434 .434 0 0 1 .417 -.449Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.586 9.607
                moveTo(x = 7.586f, y = 9.607f)
                // a 0.433 0.433 0 0 1 0.448 0.417
                arcToRelative(
                    a = 0.433f,
                    b = 0.433f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.448f,
                    dy1 = 0.417f,
                )
                // l 0.038 1.057
                lineToRelative(dx = 0.038f, dy = 1.057f)
                // a 0.434 0.434 0 0 1 -0.417 0.448
                arcToRelative(
                    a = 0.434f,
                    b = 0.434f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.417f,
                    dy1 = 0.448f,
                )
                // h -0.016
                horizontalLineToRelative(dx = -0.016f)
                // l -0.083 -0.007
                lineToRelative(dx = -0.083f, dy = -0.007f)
                // a 0.435 0.435 0 0 1 -0.35 -0.41
                arcToRelative(
                    a = 0.435f,
                    b = 0.435f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.35f,
                    dy1 = -0.41f,
                )
                // l -0.037 -1.056
                lineToRelative(dx = -0.037f, dy = -1.056f)
                // a 0.434 0.434 0 0 1 0.417 -0.449z
                arcToRelative(
                    a = 0.434f,
                    b = 0.434f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.417f,
                    dy1 = -0.449f,
                )
                close()
            }
        }.build().also { _ic2523 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2523: ImageVector? = null
