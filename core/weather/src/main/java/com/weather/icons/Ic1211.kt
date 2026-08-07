package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1211: ImageVector
    get() {
        val current = _ic1211
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1211",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M10.833 13 c1.809 -.038 2.828 .07 4.594 .485 a.47 .47 0 0 0 .562 -.377 .497 .497 0 0 0 -.357 -.593 c-1.84 -.432 -2.934 -.548 -4.818 -.508 -1.174 .025 -2.072 .274 -2.922 .51 l-.012 .003 c-.852 .236 -1.657 .458 -2.713 .48 -1.809 .038 -2.828 -.07 -4.594 -.485 a.47 .47 0 0 0 -.562 .377 .497 .497 0 0 0 .357 .593 c1.84 .432 2.934 .548 4.818 .508 1.174 -.025 2.072 -.274 2.922 -.51 l.012 -.003 c.852 -.236 1.657 -.458 2.713 -.48Z m0 2 c1.809 -.038 2.828 .07 4.594 .485 a.47 .47 0 0 0 .562 -.377 .497 .497 0 0 0 -.357 -.593 c-1.84 -.432 -2.934 -.548 -4.818 -.508 -1.174 .025 -2.072 .274 -2.922 .51 l-.012 .003 c-.852 .236 -1.657 .458 -2.713 .48 -1.809 .038 -2.828 -.07 -4.594 -.485 a.47 .47 0 0 0 -.562 .377 .497 .497 0 0 0 .357 .593 c1.84 .432 2.934 .547 4.818 .508 1.174 -.025 2.072 -.274 2.922 -.51 l.012 -.003 c.852 -.236 1.657 -.458 2.713 -.48Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10.833 13
                moveTo(x = 10.833f, y = 13.0f)
                // c 1.809 -0.038 2.828 0.07 4.594 0.485
                curveToRelative(
                    dx1 = 1.809f,
                    dy1 = -0.038f,
                    dx2 = 2.828f,
                    dy2 = 0.07f,
                    dx3 = 4.594f,
                    dy3 = 0.485f,
                )
                // a 0.47 0.47 0 0 0 0.562 -0.377
                arcToRelative(
                    a = 0.47f,
                    b = 0.47f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.562f,
                    dy1 = -0.377f,
                )
                // a 0.497 0.497 0 0 0 -0.357 -0.593
                arcToRelative(
                    a = 0.497f,
                    b = 0.497f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.357f,
                    dy1 = -0.593f,
                )
                // c -1.84 -0.432 -2.934 -0.548 -4.818 -0.508
                curveToRelative(
                    dx1 = -1.84f,
                    dy1 = -0.432f,
                    dx2 = -2.934f,
                    dy2 = -0.548f,
                    dx3 = -4.818f,
                    dy3 = -0.508f,
                )
                // c -1.174 0.025 -2.072 0.274 -2.922 0.51
                curveToRelative(
                    dx1 = -1.174f,
                    dy1 = 0.025f,
                    dx2 = -2.072f,
                    dy2 = 0.274f,
                    dx3 = -2.922f,
                    dy3 = 0.51f,
                )
                // l -0.012 0.003
                lineToRelative(dx = -0.012f, dy = 0.003f)
                // c -0.852 0.236 -1.657 0.458 -2.713 0.48
                curveToRelative(
                    dx1 = -0.852f,
                    dy1 = 0.236f,
                    dx2 = -1.657f,
                    dy2 = 0.458f,
                    dx3 = -2.713f,
                    dy3 = 0.48f,
                )
                // c -1.809 0.038 -2.828 -0.07 -4.594 -0.485
                curveToRelative(
                    dx1 = -1.809f,
                    dy1 = 0.038f,
                    dx2 = -2.828f,
                    dy2 = -0.07f,
                    dx3 = -4.594f,
                    dy3 = -0.485f,
                )
                // a 0.47 0.47 0 0 0 -0.562 0.377
                arcToRelative(
                    a = 0.47f,
                    b = 0.47f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.562f,
                    dy1 = 0.377f,
                )
                // a 0.497 0.497 0 0 0 0.357 0.593
                arcToRelative(
                    a = 0.497f,
                    b = 0.497f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.357f,
                    dy1 = 0.593f,
                )
                // c 1.84 0.432 2.934 0.548 4.818 0.508
                curveToRelative(
                    dx1 = 1.84f,
                    dy1 = 0.432f,
                    dx2 = 2.934f,
                    dy2 = 0.548f,
                    dx3 = 4.818f,
                    dy3 = 0.508f,
                )
                // c 1.174 -0.025 2.072 -0.274 2.922 -0.51
                curveToRelative(
                    dx1 = 1.174f,
                    dy1 = -0.025f,
                    dx2 = 2.072f,
                    dy2 = -0.274f,
                    dx3 = 2.922f,
                    dy3 = -0.51f,
                )
                // l 0.012 -0.003
                lineToRelative(dx = 0.012f, dy = -0.003f)
                // c 0.852 -0.236 1.657 -0.458 2.713 -0.48z
                curveToRelative(
                    dx1 = 0.852f,
                    dy1 = -0.236f,
                    dx2 = 1.657f,
                    dy2 = -0.458f,
                    dx3 = 2.713f,
                    dy3 = -0.48f,
                )
                close()
                // m 0 2
                moveToRelative(dx = 0.0f, dy = 2.0f)
                // c 1.809 -0.038 2.828 0.07 4.594 0.485
                curveToRelative(
                    dx1 = 1.809f,
                    dy1 = -0.038f,
                    dx2 = 2.828f,
                    dy2 = 0.07f,
                    dx3 = 4.594f,
                    dy3 = 0.485f,
                )
                // a 0.47 0.47 0 0 0 0.562 -0.377
                arcToRelative(
                    a = 0.47f,
                    b = 0.47f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.562f,
                    dy1 = -0.377f,
                )
                // a 0.497 0.497 0 0 0 -0.357 -0.593
                arcToRelative(
                    a = 0.497f,
                    b = 0.497f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.357f,
                    dy1 = -0.593f,
                )
                // c -1.84 -0.432 -2.934 -0.548 -4.818 -0.508
                curveToRelative(
                    dx1 = -1.84f,
                    dy1 = -0.432f,
                    dx2 = -2.934f,
                    dy2 = -0.548f,
                    dx3 = -4.818f,
                    dy3 = -0.508f,
                )
                // c -1.174 0.025 -2.072 0.274 -2.922 0.51
                curveToRelative(
                    dx1 = -1.174f,
                    dy1 = 0.025f,
                    dx2 = -2.072f,
                    dy2 = 0.274f,
                    dx3 = -2.922f,
                    dy3 = 0.51f,
                )
                // l -0.012 0.003
                lineToRelative(dx = -0.012f, dy = 0.003f)
                // c -0.852 0.236 -1.657 0.458 -2.713 0.48
                curveToRelative(
                    dx1 = -0.852f,
                    dy1 = 0.236f,
                    dx2 = -1.657f,
                    dy2 = 0.458f,
                    dx3 = -2.713f,
                    dy3 = 0.48f,
                )
                // c -1.809 0.038 -2.828 -0.07 -4.594 -0.485
                curveToRelative(
                    dx1 = -1.809f,
                    dy1 = 0.038f,
                    dx2 = -2.828f,
                    dy2 = -0.07f,
                    dx3 = -4.594f,
                    dy3 = -0.485f,
                )
                // a 0.47 0.47 0 0 0 -0.562 0.377
                arcToRelative(
                    a = 0.47f,
                    b = 0.47f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.562f,
                    dy1 = 0.377f,
                )
                // a 0.497 0.497 0 0 0 0.357 0.593
                arcToRelative(
                    a = 0.497f,
                    b = 0.497f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.357f,
                    dy1 = 0.593f,
                )
                // c 1.84 0.432 2.934 0.547 4.818 0.508
                curveToRelative(
                    dx1 = 1.84f,
                    dy1 = 0.432f,
                    dx2 = 2.934f,
                    dy2 = 0.547f,
                    dx3 = 4.818f,
                    dy3 = 0.508f,
                )
                // c 1.174 -0.025 2.072 -0.274 2.922 -0.51
                curveToRelative(
                    dx1 = 1.174f,
                    dy1 = -0.025f,
                    dx2 = 2.072f,
                    dy2 = -0.274f,
                    dx3 = 2.922f,
                    dy3 = -0.51f,
                )
                // l 0.012 -0.003
                lineToRelative(dx = 0.012f, dy = -0.003f)
                // c 0.852 -0.236 1.657 -0.458 2.713 -0.48z
                curveToRelative(
                    dx1 = 0.852f,
                    dy1 = -0.236f,
                    dx2 = 1.657f,
                    dy2 = -0.458f,
                    dx3 = 2.713f,
                    dy3 = -0.48f,
                )
                close()
            }
            // M5.536 5.293 a.398 .398 0 0 1 .385 .103 L9.418 9.03 c.207 .215 -.008 .496 -.317 .413 l-1.176 -.315 -.974 3.636 A8.37 8.37 0 0 1 5.167 13 c-1.801 .038 -2.82 -.068 -4.571 -.48 l1.34 -4.997 -1.177 -.315 C.451 7.125 .405 6.774 .691 6.69 l4.845 -1.398Z M4.16 9.203 a.52 .52 0 0 0 -.634 .371 l-.271 1.011 a.52 .52 0 0 0 .363 .64 l.999 .267 a.52 .52 0 0 0 .634 -.372 l.271 -1.01 a.52 .52 0 0 0 -.363 -.64 l-.999 -.267Z M14.291 4.05 a.2 .2 0 0 1 .322 .086 l.94 2.777 a.2 .2 0 0 1 -.242 .258 l-.363 -.098 .584 1.91 a.2 .2 0 0 1 -.243 .252 l-.328 -.088 .545 1.903 a.2 .2 0 0 1 -.245 .248 l-1.66 -.444 -.33 1.234 A17.27 17.27 0 0 0 11.224 12 l.446 -1.665 -1.522 -.408 a.2 .2 0 0 1 -.087 -.337 l1.423 -1.375 -.328 -.088 a.2 .2 0 0 1 -.084 -.34 l1.46 -1.362 -.363 -.097 a.2 .2 0 0 1 -.08 -.344 l2.202 -1.935Z M.293 2.707 A1 1 0 0 1 0 2 C0 1.5 .555 .605 1 0 c.445 .605 1 1.5 1 2 a1 1 0 0 1 -1.707 .707Z m6 0 A1 1 0 0 1 6 2 c0 -.5 .555 -1.395 1 -2 .445 .605 1 1.5 1 2 a1 1 0 0 1 -1.707 .707Z M3 2 a1 1 0 0 0 2 0 c0 -.5 -.555 -1.395 -1 -2 -.445 .605 -1 1.5 -1 2Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 5.536 5.293
                moveTo(x = 5.536f, y = 5.293f)
                // a 0.398 0.398 0 0 1 0.385 0.103
                arcToRelative(
                    a = 0.398f,
                    b = 0.398f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.385f,
                    dy1 = 0.103f,
                )
                // L 9.418 9.03
                lineTo(x = 9.418f, y = 9.03f)
                // c 0.207 0.215 -0.008 0.496 -0.317 0.413
                curveToRelative(
                    dx1 = 0.207f,
                    dy1 = 0.215f,
                    dx2 = -0.008f,
                    dy2 = 0.496f,
                    dx3 = -0.317f,
                    dy3 = 0.413f,
                )
                // l -1.176 -0.315
                lineToRelative(dx = -1.176f, dy = -0.315f)
                // l -0.974 3.636
                lineToRelative(dx = -0.974f, dy = 3.636f)
                // A 8.37 8.37 0 0 1 5.167 13
                arcTo(
                    horizontalEllipseRadius = 8.37f,
                    verticalEllipseRadius = 8.37f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 5.167f,
                    y1 = 13.0f,
                )
                // c -1.801 0.038 -2.82 -0.068 -4.571 -0.48
                curveToRelative(
                    dx1 = -1.801f,
                    dy1 = 0.038f,
                    dx2 = -2.82f,
                    dy2 = -0.068f,
                    dx3 = -4.571f,
                    dy3 = -0.48f,
                )
                // l 1.34 -4.997
                lineToRelative(dx = 1.34f, dy = -4.997f)
                // l -1.177 -0.315
                lineToRelative(dx = -1.177f, dy = -0.315f)
                // C 0.451 7.125 0.405 6.774 0.691 6.69
                curveTo(
                    x1 = 0.451f,
                    y1 = 7.125f,
                    x2 = 0.405f,
                    y2 = 6.774f,
                    x3 = 0.691f,
                    y3 = 6.69f,
                )
                // l 4.845 -1.398z
                lineToRelative(dx = 4.845f, dy = -1.398f)
                close()
                // M 4.16 9.203
                moveTo(x = 4.16f, y = 9.203f)
                // a 0.52 0.52 0 0 0 -0.634 0.371
                arcToRelative(
                    a = 0.52f,
                    b = 0.52f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.634f,
                    dy1 = 0.371f,
                )
                // l -0.271 1.011
                lineToRelative(dx = -0.271f, dy = 1.011f)
                // a 0.52 0.52 0 0 0 0.363 0.64
                arcToRelative(
                    a = 0.52f,
                    b = 0.52f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.363f,
                    dy1 = 0.64f,
                )
                // l 0.999 0.267
                lineToRelative(dx = 0.999f, dy = 0.267f)
                // a 0.52 0.52 0 0 0 0.634 -0.372
                arcToRelative(
                    a = 0.52f,
                    b = 0.52f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.634f,
                    dy1 = -0.372f,
                )
                // l 0.271 -1.01
                lineToRelative(dx = 0.271f, dy = -1.01f)
                // a 0.52 0.52 0 0 0 -0.363 -0.64
                arcToRelative(
                    a = 0.52f,
                    b = 0.52f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.363f,
                    dy1 = -0.64f,
                )
                // l -0.999 -0.267z
                lineToRelative(dx = -0.999f, dy = -0.267f)
                close()
                // M 14.291 4.05
                moveTo(x = 14.291f, y = 4.05f)
                // a 0.2 0.2 0 0 1 0.322 0.086
                arcToRelative(
                    a = 0.2f,
                    b = 0.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.322f,
                    dy1 = 0.086f,
                )
                // l 0.94 2.777
                lineToRelative(dx = 0.94f, dy = 2.777f)
                // a 0.2 0.2 0 0 1 -0.242 0.258
                arcToRelative(
                    a = 0.2f,
                    b = 0.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.242f,
                    dy1 = 0.258f,
                )
                // l -0.363 -0.098
                lineToRelative(dx = -0.363f, dy = -0.098f)
                // l 0.584 1.91
                lineToRelative(dx = 0.584f, dy = 1.91f)
                // a 0.2 0.2 0 0 1 -0.243 0.252
                arcToRelative(
                    a = 0.2f,
                    b = 0.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.243f,
                    dy1 = 0.252f,
                )
                // l -0.328 -0.088
                lineToRelative(dx = -0.328f, dy = -0.088f)
                // l 0.545 1.903
                lineToRelative(dx = 0.545f, dy = 1.903f)
                // a 0.2 0.2 0 0 1 -0.245 0.248
                arcToRelative(
                    a = 0.2f,
                    b = 0.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.245f,
                    dy1 = 0.248f,
                )
                // l -1.66 -0.444
                lineToRelative(dx = -1.66f, dy = -0.444f)
                // l -0.33 1.234
                lineToRelative(dx = -0.33f, dy = 1.234f)
                // A 17.27 17.27 0 0 0 11.224 12
                arcTo(
                    horizontalEllipseRadius = 17.27f,
                    verticalEllipseRadius = 17.27f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 11.224f,
                    y1 = 12.0f,
                )
                // l 0.446 -1.665
                lineToRelative(dx = 0.446f, dy = -1.665f)
                // l -1.522 -0.408
                lineToRelative(dx = -1.522f, dy = -0.408f)
                // a 0.2 0.2 0 0 1 -0.087 -0.337
                arcToRelative(
                    a = 0.2f,
                    b = 0.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.087f,
                    dy1 = -0.337f,
                )
                // l 1.423 -1.375
                lineToRelative(dx = 1.423f, dy = -1.375f)
                // l -0.328 -0.088
                lineToRelative(dx = -0.328f, dy = -0.088f)
                // a 0.2 0.2 0 0 1 -0.084 -0.34
                arcToRelative(
                    a = 0.2f,
                    b = 0.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.084f,
                    dy1 = -0.34f,
                )
                // l 1.46 -1.362
                lineToRelative(dx = 1.46f, dy = -1.362f)
                // l -0.363 -0.097
                lineToRelative(dx = -0.363f, dy = -0.097f)
                // a 0.2 0.2 0 0 1 -0.08 -0.344
                arcToRelative(
                    a = 0.2f,
                    b = 0.2f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.08f,
                    dy1 = -0.344f,
                )
                // l 2.202 -1.935z
                lineToRelative(dx = 2.202f, dy = -1.935f)
                close()
                // M 0.293 2.707
                moveTo(x = 0.293f, y = 2.707f)
                // A 1 1 0 0 1 0 2
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 0.0f,
                    y1 = 2.0f,
                )
                // C 0 1.5 0.555 0.605 1 0
                curveTo(
                    x1 = 0.0f,
                    y1 = 1.5f,
                    x2 = 0.555f,
                    y2 = 0.605f,
                    x3 = 1.0f,
                    y3 = 0.0f,
                )
                // c 0.445 0.605 1 1.5 1 2
                curveToRelative(
                    dx1 = 0.445f,
                    dy1 = 0.605f,
                    dx2 = 1.0f,
                    dy2 = 1.5f,
                    dx3 = 1.0f,
                    dy3 = 2.0f,
                )
                // a 1 1 0 0 1 -1.707 0.707z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.707f,
                    dy1 = 0.707f,
                )
                close()
                // m 6 0
                moveToRelative(dx = 6.0f, dy = 0.0f)
                // A 1 1 0 0 1 6 2
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 6.0f,
                    y1 = 2.0f,
                )
                // c 0 -0.5 0.555 -1.395 1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                    dx2 = 0.555f,
                    dy2 = -1.395f,
                    dx3 = 1.0f,
                    dy3 = -2.0f,
                )
                // c 0.445 0.605 1 1.5 1 2
                curveToRelative(
                    dx1 = 0.445f,
                    dy1 = 0.605f,
                    dx2 = 1.0f,
                    dy2 = 1.5f,
                    dx3 = 1.0f,
                    dy3 = 2.0f,
                )
                // a 1 1 0 0 1 -1.707 0.707z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.707f,
                    dy1 = 0.707f,
                )
                close()
                // M 3 2
                moveTo(x = 3.0f, y = 2.0f)
                // a 1 1 0 0 0 2 0
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.0f,
                    dy1 = 0.0f,
                )
                // c 0 -0.5 -0.555 -1.395 -1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                    dx2 = -0.555f,
                    dy2 = -1.395f,
                    dx3 = -1.0f,
                    dy3 = -2.0f,
                )
                // c -0.445 0.605 -1 1.5 -1 2z
                curveToRelative(
                    dx1 = -0.445f,
                    dy1 = 0.605f,
                    dx2 = -1.0f,
                    dy2 = 1.5f,
                    dx3 = -1.0f,
                    dy3 = 2.0f,
                )
                close()
            }
        }.build().also { _ic1211 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1211: ImageVector? = null
