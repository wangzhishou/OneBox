package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1084: ImageVector
    get() {
        val current = _ic1084
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1084",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M0 15.2 s1 -.2 3.5 -.2 6.5 .4 8.5 .4 4 -.2 4 -.2 v.8 H0 v-.8Z M15.438 3.813 c-.844 .28 -1.126 1.03 -1.032 1.5 -.656 -.75 -.656 -1.594 -.562 -2.907 -1.969 .75 -1.5 3 -1.594 3.75 -.469 -.468 -.563 -1.406 -.563 -1.406 -.562 .281 -.843 1.031 -.843 1.688 0 1.5 1.219 2.53 2.625 2.53 A2.52 2.52 0 0 0 16 6.439 c0 -1.032 -.563 -1.313 -.563 -2.625Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0 15.2
                moveTo(x = 0.0f, y = 15.2f)
                // s 1 -0.2 3.5 -0.2
                reflectiveCurveToRelative(
                    dx1 = 1.0f,
                    dy1 = -0.2f,
                    dx2 = 3.5f,
                    dy2 = -0.2f,
                )
                // s 6.5 0.4 8.5 0.4
                reflectiveCurveToRelative(
                    dx1 = 6.5f,
                    dy1 = 0.4f,
                    dx2 = 8.5f,
                    dy2 = 0.4f,
                )
                // s 4 -0.2 4 -0.2
                reflectiveCurveToRelative(
                    dx1 = 4.0f,
                    dy1 = -0.2f,
                    dx2 = 4.0f,
                    dy2 = -0.2f,
                )
                // v 0.8
                verticalLineToRelative(dy = 0.8f)
                // H 0
                horizontalLineTo(x = 0.0f)
                // v -0.8z
                verticalLineToRelative(dy = -0.8f)
                close()
                // M 15.438 3.813
                moveTo(x = 15.438f, y = 3.813f)
                // c -0.844 0.28 -1.126 1.03 -1.032 1.5
                curveToRelative(
                    dx1 = -0.844f,
                    dy1 = 0.28f,
                    dx2 = -1.126f,
                    dy2 = 1.03f,
                    dx3 = -1.032f,
                    dy3 = 1.5f,
                )
                // c -0.656 -0.75 -0.656 -1.594 -0.562 -2.907
                curveToRelative(
                    dx1 = -0.656f,
                    dy1 = -0.75f,
                    dx2 = -0.656f,
                    dy2 = -1.594f,
                    dx3 = -0.562f,
                    dy3 = -2.907f,
                )
                // c -1.969 0.75 -1.5 3 -1.594 3.75
                curveToRelative(
                    dx1 = -1.969f,
                    dy1 = 0.75f,
                    dx2 = -1.5f,
                    dy2 = 3.0f,
                    dx3 = -1.594f,
                    dy3 = 3.75f,
                )
                // c -0.469 -0.468 -0.563 -1.406 -0.563 -1.406
                curveToRelative(
                    dx1 = -0.469f,
                    dy1 = -0.468f,
                    dx2 = -0.563f,
                    dy2 = -1.406f,
                    dx3 = -0.563f,
                    dy3 = -1.406f,
                )
                // c -0.562 0.281 -0.843 1.031 -0.843 1.688
                curveToRelative(
                    dx1 = -0.562f,
                    dy1 = 0.281f,
                    dx2 = -0.843f,
                    dy2 = 1.031f,
                    dx3 = -0.843f,
                    dy3 = 1.688f,
                )
                // c 0 1.5 1.219 2.53 2.625 2.53
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.5f,
                    dx2 = 1.219f,
                    dy2 = 2.53f,
                    dx3 = 2.625f,
                    dy3 = 2.53f,
                )
                // A 2.52 2.52 0 0 0 16 6.439
                arcTo(
                    horizontalEllipseRadius = 2.52f,
                    verticalEllipseRadius = 2.52f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 16.0f,
                    y1 = 6.439f,
                )
                // c 0 -1.032 -0.563 -1.313 -0.563 -2.625z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.032f,
                    dx2 = -0.563f,
                    dy2 = -1.313f,
                    dx3 = -0.563f,
                    dy3 = -2.625f,
                )
                close()
            }
            // M9.906 6.344 c0 1.687 1.125 3 2.625 3.375 h-2.156 l3.632 3.977 a.2 .2 0 0 1 -.148 .335 H8.688 v1.217 l-.49 -.031 c-.52 -.034 -1.05 -.068 -1.573 -.099 v-1.087 H1.447 a.2 .2 0 0 1 -.15 -.333 l3.64 -4.073 H3.1 a.2 .2 0 0 1 -.157 -.324 L5.5 6.063 H4.215 a.2 .2 0 0 1 -.159 -.321 L7.5 1.206 a.2 .2 0 0 1 .316 -.003 l2.653 3.36 c-.375 .562 -.563 1.218 -.563 1.78Z M2.392 .737 a.156 .156 0 0 0 .267 -.11 v-.47 a.156 .156 0 0 0 -.313 0 v.47 c0 .04 .017 .08 .046 .11Z m-1.275 .581 a.156 .156 0 0 0 .17 -.255 L.957 .732 a.156 .156 0 0 0 -.221 .22 l.332 .332 a.156 .156 0 0 0 .05 .034Z m-.96 1.337 h.47 a.156 .156 0 0 0 0 -.313 h-.47 a.156 .156 0 0 0 0 .313Z m.598 1.632 a.156 .156 0 0 0 .198 -.02 l.332 -.331 a.156 .156 0 0 0 -.222 -.221 l-.331 .33 a.156 .156 0 0 0 .023 .242Z m1.633 .668 a.156 .156 0 0 0 .267 -.11 v-.47 a.156 .156 0 0 0 -.313 0 v.47 c0 .041 .017 .08 .046 .11Z m1.708 -.651 a.156 .156 0 0 0 .213 -.175 .156 .156 0 0 0 -.042 -.08 l-.332 -.331 a.156 .156 0 1 0 -.22 .22 l.33 .332 a.156 .156 0 0 0 .051 .034Z m.28 -1.644 h.469 a.156 .156 0 1 0 0 -.313 h-.47 a.156 .156 0 1 0 0 .312Z m-.635 -1.353 a.156 .156 0 0 0 .198 -.02 L4.27 .956 a.156 .156 0 1 0 -.221 -.221 l-.332 .331 a.156 .156 0 0 0 .024 .241Z M1.72 1.33 a1.406 1.406 0 1 1 1.563 2.34 A1.406 1.406 0 0 1 1.72 1.33Z m1.39 .26 a1.094 1.094 0 1 0 -1.216 1.82 1.094 1.094 0 0 0 1.215 -1.82Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 9.906 6.344
                moveTo(x = 9.906f, y = 6.344f)
                // c 0 1.687 1.125 3 2.625 3.375
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 1.687f,
                    dx2 = 1.125f,
                    dy2 = 3.0f,
                    dx3 = 2.625f,
                    dy3 = 3.375f,
                )
                // h -2.156
                horizontalLineToRelative(dx = -2.156f)
                // l 3.632 3.977
                lineToRelative(dx = 3.632f, dy = 3.977f)
                // a 0.2 0.2 0 0 1 -0.148 0.335
                arcToRelative(
                    a = 0.2f,
                    b = 0.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.148f,
                    dy1 = 0.335f,
                )
                // H 8.688
                horizontalLineTo(x = 8.688f)
                // v 1.217
                verticalLineToRelative(dy = 1.217f)
                // l -0.49 -0.031
                lineToRelative(dx = -0.49f, dy = -0.031f)
                // c -0.52 -0.034 -1.05 -0.068 -1.573 -0.099
                curveToRelative(
                    dx1 = -0.52f,
                    dy1 = -0.034f,
                    dx2 = -1.05f,
                    dy2 = -0.068f,
                    dx3 = -1.573f,
                    dy3 = -0.099f,
                )
                // v -1.087
                verticalLineToRelative(dy = -1.087f)
                // H 1.447
                horizontalLineTo(x = 1.447f)
                // a 0.2 0.2 0 0 1 -0.15 -0.333
                arcToRelative(
                    a = 0.2f,
                    b = 0.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.15f,
                    dy1 = -0.333f,
                )
                // l 3.64 -4.073
                lineToRelative(dx = 3.64f, dy = -4.073f)
                // H 3.1
                horizontalLineTo(x = 3.1f)
                // a 0.2 0.2 0 0 1 -0.157 -0.324
                arcToRelative(
                    a = 0.2f,
                    b = 0.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.157f,
                    dy1 = -0.324f,
                )
                // L 5.5 6.063
                lineTo(x = 5.5f, y = 6.063f)
                // H 4.215
                horizontalLineTo(x = 4.215f)
                // a 0.2 0.2 0 0 1 -0.159 -0.321
                arcToRelative(
                    a = 0.2f,
                    b = 0.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.159f,
                    dy1 = -0.321f,
                )
                // L 7.5 1.206
                lineTo(x = 7.5f, y = 1.206f)
                // a 0.2 0.2 0 0 1 0.316 -0.003
                arcToRelative(
                    a = 0.2f,
                    b = 0.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.316f,
                    dy1 = -0.003f,
                )
                // l 2.653 3.36
                lineToRelative(dx = 2.653f, dy = 3.36f)
                // c -0.375 0.562 -0.563 1.218 -0.563 1.78z
                curveToRelative(
                    dx1 = -0.375f,
                    dy1 = 0.562f,
                    dx2 = -0.563f,
                    dy2 = 1.218f,
                    dx3 = -0.563f,
                    dy3 = 1.78f,
                )
                close()
                // M 2.392 0.737
                moveTo(x = 2.392f, y = 0.737f)
                // a 0.156 0.156 0 0 0 0.267 -0.11
                arcToRelative(
                    a = 0.156f,
                    b = 0.156f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.267f,
                    dy1 = -0.11f,
                )
                // v -0.47
                verticalLineToRelative(dy = -0.47f)
                // a 0.156 0.156 0 0 0 -0.313 0
                arcToRelative(
                    a = 0.156f,
                    b = 0.156f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.313f,
                    dy1 = 0.0f,
                )
                // v 0.47
                verticalLineToRelative(dy = 0.47f)
                // c 0 0.04 0.017 0.08 0.046 0.11z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.04f,
                    dx2 = 0.017f,
                    dy2 = 0.08f,
                    dx3 = 0.046f,
                    dy3 = 0.11f,
                )
                close()
                // m -1.275 0.581
                moveToRelative(dx = -1.275f, dy = 0.581f)
                // a 0.156 0.156 0 0 0 0.17 -0.255
                arcToRelative(
                    a = 0.156f,
                    b = 0.156f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.17f,
                    dy1 = -0.255f,
                )
                // L 0.957 0.732
                lineTo(x = 0.957f, y = 0.732f)
                // a 0.156 0.156 0 0 0 -0.221 0.22
                arcToRelative(
                    a = 0.156f,
                    b = 0.156f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.221f,
                    dy1 = 0.22f,
                )
                // l 0.332 0.332
                lineToRelative(dx = 0.332f, dy = 0.332f)
                // a 0.156 0.156 0 0 0 0.05 0.034z
                arcToRelative(
                    a = 0.156f,
                    b = 0.156f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.05f,
                    dy1 = 0.034f,
                )
                close()
                // m -0.96 1.337
                moveToRelative(dx = -0.96f, dy = 1.337f)
                // h 0.47
                horizontalLineToRelative(dx = 0.47f)
                // a 0.156 0.156 0 0 0 0 -0.313
                arcToRelative(
                    a = 0.156f,
                    b = 0.156f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.313f,
                )
                // h -0.47
                horizontalLineToRelative(dx = -0.47f)
                // a 0.156 0.156 0 0 0 0 0.313z
                arcToRelative(
                    a = 0.156f,
                    b = 0.156f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.313f,
                )
                close()
                // m 0.598 1.632
                moveToRelative(dx = 0.598f, dy = 1.632f)
                // a 0.156 0.156 0 0 0 0.198 -0.02
                arcToRelative(
                    a = 0.156f,
                    b = 0.156f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.198f,
                    dy1 = -0.02f,
                )
                // l 0.332 -0.331
                lineToRelative(dx = 0.332f, dy = -0.331f)
                // a 0.156 0.156 0 0 0 -0.222 -0.221
                arcToRelative(
                    a = 0.156f,
                    b = 0.156f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.222f,
                    dy1 = -0.221f,
                )
                // l -0.331 0.33
                lineToRelative(dx = -0.331f, dy = 0.33f)
                // a 0.156 0.156 0 0 0 0.023 0.242z
                arcToRelative(
                    a = 0.156f,
                    b = 0.156f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.023f,
                    dy1 = 0.242f,
                )
                close()
                // m 1.633 0.668
                moveToRelative(dx = 1.633f, dy = 0.668f)
                // a 0.156 0.156 0 0 0 0.267 -0.11
                arcToRelative(
                    a = 0.156f,
                    b = 0.156f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.267f,
                    dy1 = -0.11f,
                )
                // v -0.47
                verticalLineToRelative(dy = -0.47f)
                // a 0.156 0.156 0 0 0 -0.313 0
                arcToRelative(
                    a = 0.156f,
                    b = 0.156f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.313f,
                    dy1 = 0.0f,
                )
                // v 0.47
                verticalLineToRelative(dy = 0.47f)
                // c 0 0.041 0.017 0.08 0.046 0.11z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.041f,
                    dx2 = 0.017f,
                    dy2 = 0.08f,
                    dx3 = 0.046f,
                    dy3 = 0.11f,
                )
                close()
                // m 1.708 -0.651
                moveToRelative(dx = 1.708f, dy = -0.651f)
                // a 0.156 0.156 0 0 0 0.213 -0.175
                arcToRelative(
                    a = 0.156f,
                    b = 0.156f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.213f,
                    dy1 = -0.175f,
                )
                // a 0.156 0.156 0 0 0 -0.042 -0.08
                arcToRelative(
                    a = 0.156f,
                    b = 0.156f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.042f,
                    dy1 = -0.08f,
                )
                // l -0.332 -0.331
                lineToRelative(dx = -0.332f, dy = -0.331f)
                // a 0.156 0.156 0 1 0 -0.22 0.22
                arcToRelative(
                    a = 0.156f,
                    b = 0.156f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.22f,
                    dy1 = 0.22f,
                )
                // l 0.33 0.332
                lineToRelative(dx = 0.33f, dy = 0.332f)
                // a 0.156 0.156 0 0 0 0.051 0.034z
                arcToRelative(
                    a = 0.156f,
                    b = 0.156f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.051f,
                    dy1 = 0.034f,
                )
                close()
                // m 0.28 -1.644
                moveToRelative(dx = 0.28f, dy = -1.644f)
                // h 0.469
                horizontalLineToRelative(dx = 0.469f)
                // a 0.156 0.156 0 1 0 0 -0.313
                arcToRelative(
                    a = 0.156f,
                    b = 0.156f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.313f,
                )
                // h -0.47
                horizontalLineToRelative(dx = -0.47f)
                // a 0.156 0.156 0 1 0 0 0.312z
                arcToRelative(
                    a = 0.156f,
                    b = 0.156f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.312f,
                )
                close()
                // m -0.635 -1.353
                moveToRelative(dx = -0.635f, dy = -1.353f)
                // a 0.156 0.156 0 0 0 0.198 -0.02
                arcToRelative(
                    a = 0.156f,
                    b = 0.156f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.198f,
                    dy1 = -0.02f,
                )
                // L 4.27 0.956
                lineTo(x = 4.27f, y = 0.956f)
                // a 0.156 0.156 0 1 0 -0.221 -0.221
                arcToRelative(
                    a = 0.156f,
                    b = 0.156f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.221f,
                    dy1 = -0.221f,
                )
                // l -0.332 0.331
                lineToRelative(dx = -0.332f, dy = 0.331f)
                // a 0.156 0.156 0 0 0 0.024 0.241z
                arcToRelative(
                    a = 0.156f,
                    b = 0.156f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.024f,
                    dy1 = 0.241f,
                )
                close()
                // M 1.72 1.33
                moveTo(x = 1.72f, y = 1.33f)
                // a 1.406 1.406 0 1 1 1.563 2.34
                arcToRelative(
                    a = 1.406f,
                    b = 1.406f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 1.563f,
                    dy1 = 2.34f,
                )
                // A 1.406 1.406 0 0 1 1.72 1.33z
                arcTo(
                    horizontalEllipseRadius = 1.406f,
                    verticalEllipseRadius = 1.406f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 1.72f,
                    y1 = 1.33f,
                )
                close()
                // m 1.39 0.26
                moveToRelative(dx = 1.39f, dy = 0.26f)
                // a 1.094 1.094 0 1 0 -1.216 1.82
                arcToRelative(
                    a = 1.094f,
                    b = 1.094f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -1.216f,
                    dy1 = 1.82f,
                )
                // a 1.094 1.094 0 0 0 1.215 -1.82z
                arcToRelative(
                    a = 1.094f,
                    b = 1.094f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.215f,
                    dy1 = -1.82f,
                )
                close()
            }
        }.build().also { _ic1084 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1084: ImageVector? = null
