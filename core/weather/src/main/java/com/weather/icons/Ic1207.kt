package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1207: ImageVector
    get() {
        val current = _ic1207
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1207",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M4.875 7.725 c-.427 -.081 -.835 -.22 -1.337 -.44 -.186 -.083 -.41 -.012 -.5 .157 -.09 .17 -.013 .374 .174 .455 .646 .284 1.17 .45 1.746 .534 a3.4 3.4 0 0 0 .832 1.593 A3.03 3.03 0 0 0 8 11 a3.03 3.03 0 0 0 2.21 -.976 3.45 3.45 0 0 0 .915 -2.357 c0 -1.516 -1.436 -4.121 -2.74 -6.098 A42.46 42.46 0 0 0 8 1 c-.126 .182 -.254 .373 -.384 .57 A30.85 30.85 0 0 0 6.12 4.067 c-1.03 .008 -1.64 -.12 -2.583 -.534 -.186 -.082 -.41 -.01 -.5 .158 -.09 .17 -.013 .374 .174 .455 .935 .411 1.613 .575 2.565 .6 -.192 .398 -.363 .788 -.503 1.161 -.586 -.062 -1.081 -.211 -1.736 -.499 -.186 -.082 -.41 -.01 -.5 .158 -.09 .17 -.013 .374 .174 .455 .682 .3 1.228 .468 1.843 .547 a4.126 4.126 0 0 0 -.18 1.156Z m.628 .078 a2.856 2.856 0 0 1 -.003 -.136 c0 -.268 .063 -.62 .195 -1.048 .158 .006 .323 .007 .497 .005 .78 -.01 1.246 -.08 1.988 -.3 .196 -.06 .303 -.252 .239 -.43 -.065 -.18 -.277 -.277 -.473 -.218 a5.49 5.49 0 0 1 -1.763 .267 h-.246 a15.048 15.048 0 0 1 .55 -1.201 c.613 -.023 1.049 -.102 1.693 -.294 .196 -.058 .303 -.25 .239 -.43 -.065 -.178 -.277 -.276 -.473 -.217 a5.663 5.663 0 0 1 -1.084 .233 c.357 -.647 .75 -1.291 1.138 -1.887 A28.34 28.34 0 0 1 9.533 4.78 c.295 .585 .54 1.146 .711 1.652 .174 .513 .256 .928 .256 1.235 a2.76 2.76 0 0 1 -.732 1.885 c-.47 .5 -1.105 .781 -1.768 .781 s-1.299 -.28 -1.768 -.78 a2.702 2.702 0 0 1 -.61 -1.062 c.18 .008 .369 .01 .57 .008 .78 -.01 1.246 -.08 1.988 -.3 .196 -.06 .303 -.252 .239 -.43 -.065 -.18 -.277 -.277 -.473 -.218 a5.49 5.49 0 0 1 -1.763 .267 8.734 8.734 0 0 1 -.68 -.015Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.875 7.725
                moveTo(x = 4.875f, y = 7.725f)
                // c -0.427 -0.081 -0.835 -0.22 -1.337 -0.44
                curveToRelative(
                    dx1 = -0.427f,
                    dy1 = -0.081f,
                    dx2 = -0.835f,
                    dy2 = -0.22f,
                    dx3 = -1.337f,
                    dy3 = -0.44f,
                )
                // c -0.186 -0.083 -0.41 -0.012 -0.5 0.157
                curveToRelative(
                    dx1 = -0.186f,
                    dy1 = -0.083f,
                    dx2 = -0.41f,
                    dy2 = -0.012f,
                    dx3 = -0.5f,
                    dy3 = 0.157f,
                )
                // c -0.09 0.17 -0.013 0.374 0.174 0.455
                curveToRelative(
                    dx1 = -0.09f,
                    dy1 = 0.17f,
                    dx2 = -0.013f,
                    dy2 = 0.374f,
                    dx3 = 0.174f,
                    dy3 = 0.455f,
                )
                // c 0.646 0.284 1.17 0.45 1.746 0.534
                curveToRelative(
                    dx1 = 0.646f,
                    dy1 = 0.284f,
                    dx2 = 1.17f,
                    dy2 = 0.45f,
                    dx3 = 1.746f,
                    dy3 = 0.534f,
                )
                // a 3.4 3.4 0 0 0 0.832 1.593
                arcToRelative(
                    a = 3.4f,
                    b = 3.4f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.832f,
                    dy1 = 1.593f,
                )
                // A 3.03 3.03 0 0 0 8 11
                arcTo(
                    horizontalEllipseRadius = 3.03f,
                    verticalEllipseRadius = 3.03f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 8.0f,
                    y1 = 11.0f,
                )
                // a 3.03 3.03 0 0 0 2.21 -0.976
                arcToRelative(
                    a = 3.03f,
                    b = 3.03f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.21f,
                    dy1 = -0.976f,
                )
                // a 3.45 3.45 0 0 0 0.915 -2.357
                arcToRelative(
                    a = 3.45f,
                    b = 3.45f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.915f,
                    dy1 = -2.357f,
                )
                // c 0 -1.516 -1.436 -4.121 -2.74 -6.098
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.516f,
                    dx2 = -1.436f,
                    dy2 = -4.121f,
                    dx3 = -2.74f,
                    dy3 = -6.098f,
                )
                // A 42.46 42.46 0 0 0 8 1
                arcTo(
                    horizontalEllipseRadius = 42.46f,
                    verticalEllipseRadius = 42.46f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 8.0f,
                    y1 = 1.0f,
                )
                // c -0.126 0.182 -0.254 0.373 -0.384 0.57
                curveToRelative(
                    dx1 = -0.126f,
                    dy1 = 0.182f,
                    dx2 = -0.254f,
                    dy2 = 0.373f,
                    dx3 = -0.384f,
                    dy3 = 0.57f,
                )
                // A 30.85 30.85 0 0 0 6.12 4.067
                arcTo(
                    horizontalEllipseRadius = 30.85f,
                    verticalEllipseRadius = 30.85f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 6.12f,
                    y1 = 4.067f,
                )
                // c -1.03 0.008 -1.64 -0.12 -2.583 -0.534
                curveToRelative(
                    dx1 = -1.03f,
                    dy1 = 0.008f,
                    dx2 = -1.64f,
                    dy2 = -0.12f,
                    dx3 = -2.583f,
                    dy3 = -0.534f,
                )
                // c -0.186 -0.082 -0.41 -0.01 -0.5 0.158
                curveToRelative(
                    dx1 = -0.186f,
                    dy1 = -0.082f,
                    dx2 = -0.41f,
                    dy2 = -0.01f,
                    dx3 = -0.5f,
                    dy3 = 0.158f,
                )
                // c -0.09 0.17 -0.013 0.374 0.174 0.455
                curveToRelative(
                    dx1 = -0.09f,
                    dy1 = 0.17f,
                    dx2 = -0.013f,
                    dy2 = 0.374f,
                    dx3 = 0.174f,
                    dy3 = 0.455f,
                )
                // c 0.935 0.411 1.613 0.575 2.565 0.6
                curveToRelative(
                    dx1 = 0.935f,
                    dy1 = 0.411f,
                    dx2 = 1.613f,
                    dy2 = 0.575f,
                    dx3 = 2.565f,
                    dy3 = 0.6f,
                )
                // c -0.192 0.398 -0.363 0.788 -0.503 1.161
                curveToRelative(
                    dx1 = -0.192f,
                    dy1 = 0.398f,
                    dx2 = -0.363f,
                    dy2 = 0.788f,
                    dx3 = -0.503f,
                    dy3 = 1.161f,
                )
                // c -0.586 -0.062 -1.081 -0.211 -1.736 -0.499
                curveToRelative(
                    dx1 = -0.586f,
                    dy1 = -0.062f,
                    dx2 = -1.081f,
                    dy2 = -0.211f,
                    dx3 = -1.736f,
                    dy3 = -0.499f,
                )
                // c -0.186 -0.082 -0.41 -0.01 -0.5 0.158
                curveToRelative(
                    dx1 = -0.186f,
                    dy1 = -0.082f,
                    dx2 = -0.41f,
                    dy2 = -0.01f,
                    dx3 = -0.5f,
                    dy3 = 0.158f,
                )
                // c -0.09 0.17 -0.013 0.374 0.174 0.455
                curveToRelative(
                    dx1 = -0.09f,
                    dy1 = 0.17f,
                    dx2 = -0.013f,
                    dy2 = 0.374f,
                    dx3 = 0.174f,
                    dy3 = 0.455f,
                )
                // c 0.682 0.3 1.228 0.468 1.843 0.547
                curveToRelative(
                    dx1 = 0.682f,
                    dy1 = 0.3f,
                    dx2 = 1.228f,
                    dy2 = 0.468f,
                    dx3 = 1.843f,
                    dy3 = 0.547f,
                )
                // a 4.126 4.126 0 0 0 -0.18 1.156z
                arcToRelative(
                    a = 4.126f,
                    b = 4.126f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.18f,
                    dy1 = 1.156f,
                )
                close()
                // m 0.628 0.078
                moveToRelative(dx = 0.628f, dy = 0.078f)
                // a 2.856 2.856 0 0 1 -0.003 -0.136
                arcToRelative(
                    a = 2.856f,
                    b = 2.856f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.003f,
                    dy1 = -0.136f,
                )
                // c 0 -0.268 0.063 -0.62 0.195 -1.048
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.268f,
                    dx2 = 0.063f,
                    dy2 = -0.62f,
                    dx3 = 0.195f,
                    dy3 = -1.048f,
                )
                // c 0.158 0.006 0.323 0.007 0.497 0.005
                curveToRelative(
                    dx1 = 0.158f,
                    dy1 = 0.006f,
                    dx2 = 0.323f,
                    dy2 = 0.007f,
                    dx3 = 0.497f,
                    dy3 = 0.005f,
                )
                // c 0.78 -0.01 1.246 -0.08 1.988 -0.3
                curveToRelative(
                    dx1 = 0.78f,
                    dy1 = -0.01f,
                    dx2 = 1.246f,
                    dy2 = -0.08f,
                    dx3 = 1.988f,
                    dy3 = -0.3f,
                )
                // c 0.196 -0.06 0.303 -0.252 0.239 -0.43
                curveToRelative(
                    dx1 = 0.196f,
                    dy1 = -0.06f,
                    dx2 = 0.303f,
                    dy2 = -0.252f,
                    dx3 = 0.239f,
                    dy3 = -0.43f,
                )
                // c -0.065 -0.18 -0.277 -0.277 -0.473 -0.218
                curveToRelative(
                    dx1 = -0.065f,
                    dy1 = -0.18f,
                    dx2 = -0.277f,
                    dy2 = -0.277f,
                    dx3 = -0.473f,
                    dy3 = -0.218f,
                )
                // a 5.49 5.49 0 0 1 -1.763 0.267
                arcToRelative(
                    a = 5.49f,
                    b = 5.49f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.763f,
                    dy1 = 0.267f,
                )
                // h -0.246
                horizontalLineToRelative(dx = -0.246f)
                // a 15.048 15.048 0 0 1 0.55 -1.201
                arcToRelative(
                    a = 15.048f,
                    b = 15.048f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.55f,
                    dy1 = -1.201f,
                )
                // c 0.613 -0.023 1.049 -0.102 1.693 -0.294
                curveToRelative(
                    dx1 = 0.613f,
                    dy1 = -0.023f,
                    dx2 = 1.049f,
                    dy2 = -0.102f,
                    dx3 = 1.693f,
                    dy3 = -0.294f,
                )
                // c 0.196 -0.058 0.303 -0.25 0.239 -0.43
                curveToRelative(
                    dx1 = 0.196f,
                    dy1 = -0.058f,
                    dx2 = 0.303f,
                    dy2 = -0.25f,
                    dx3 = 0.239f,
                    dy3 = -0.43f,
                )
                // c -0.065 -0.178 -0.277 -0.276 -0.473 -0.217
                curveToRelative(
                    dx1 = -0.065f,
                    dy1 = -0.178f,
                    dx2 = -0.277f,
                    dy2 = -0.276f,
                    dx3 = -0.473f,
                    dy3 = -0.217f,
                )
                // a 5.663 5.663 0 0 1 -1.084 0.233
                arcToRelative(
                    a = 5.663f,
                    b = 5.663f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.084f,
                    dy1 = 0.233f,
                )
                // c 0.357 -0.647 0.75 -1.291 1.138 -1.887
                curveToRelative(
                    dx1 = 0.357f,
                    dy1 = -0.647f,
                    dx2 = 0.75f,
                    dy2 = -1.291f,
                    dx3 = 1.138f,
                    dy3 = -1.887f,
                )
                // A 28.34 28.34 0 0 1 9.533 4.78
                arcTo(
                    horizontalEllipseRadius = 28.34f,
                    verticalEllipseRadius = 28.34f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 9.533f,
                    y1 = 4.78f,
                )
                // c 0.295 0.585 0.54 1.146 0.711 1.652
                curveToRelative(
                    dx1 = 0.295f,
                    dy1 = 0.585f,
                    dx2 = 0.54f,
                    dy2 = 1.146f,
                    dx3 = 0.711f,
                    dy3 = 1.652f,
                )
                // c 0.174 0.513 0.256 0.928 0.256 1.235
                curveToRelative(
                    dx1 = 0.174f,
                    dy1 = 0.513f,
                    dx2 = 0.256f,
                    dy2 = 0.928f,
                    dx3 = 0.256f,
                    dy3 = 1.235f,
                )
                // a 2.76 2.76 0 0 1 -0.732 1.885
                arcToRelative(
                    a = 2.76f,
                    b = 2.76f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.732f,
                    dy1 = 1.885f,
                )
                // c -0.47 0.5 -1.105 0.781 -1.768 0.781
                curveToRelative(
                    dx1 = -0.47f,
                    dy1 = 0.5f,
                    dx2 = -1.105f,
                    dy2 = 0.781f,
                    dx3 = -1.768f,
                    dy3 = 0.781f,
                )
                // s -1.299 -0.28 -1.768 -0.78
                reflectiveCurveToRelative(
                    dx1 = -1.299f,
                    dy1 = -0.28f,
                    dx2 = -1.768f,
                    dy2 = -0.78f,
                )
                // a 2.702 2.702 0 0 1 -0.61 -1.062
                arcToRelative(
                    a = 2.702f,
                    b = 2.702f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.61f,
                    dy1 = -1.062f,
                )
                // c 0.18 0.008 0.369 0.01 0.57 0.008
                curveToRelative(
                    dx1 = 0.18f,
                    dy1 = 0.008f,
                    dx2 = 0.369f,
                    dy2 = 0.01f,
                    dx3 = 0.57f,
                    dy3 = 0.008f,
                )
                // c 0.78 -0.01 1.246 -0.08 1.988 -0.3
                curveToRelative(
                    dx1 = 0.78f,
                    dy1 = -0.01f,
                    dx2 = 1.246f,
                    dy2 = -0.08f,
                    dx3 = 1.988f,
                    dy3 = -0.3f,
                )
                // c 0.196 -0.06 0.303 -0.252 0.239 -0.43
                curveToRelative(
                    dx1 = 0.196f,
                    dy1 = -0.06f,
                    dx2 = 0.303f,
                    dy2 = -0.252f,
                    dx3 = 0.239f,
                    dy3 = -0.43f,
                )
                // c -0.065 -0.18 -0.277 -0.277 -0.473 -0.218
                curveToRelative(
                    dx1 = -0.065f,
                    dy1 = -0.18f,
                    dx2 = -0.277f,
                    dy2 = -0.277f,
                    dx3 = -0.473f,
                    dy3 = -0.218f,
                )
                // a 5.49 5.49 0 0 1 -1.763 0.267
                arcToRelative(
                    a = 5.49f,
                    b = 5.49f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.763f,
                    dy1 = 0.267f,
                )
                // a 8.734 8.734 0 0 1 -0.68 -0.015z
                arcToRelative(
                    a = 8.734f,
                    b = 8.734f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.68f,
                    dy1 = -0.015f,
                )
                close()
            }
            // M9.907 4.122 c1.047 -.015 1.676 .057 2.735 .306 a.294 .294 0 0 0 .35 -.235 .31 .31 0 0 0 -.222 -.37 c-1.15 -.271 -1.834 -.343 -3.011 -.318 a5.569 5.569 0 0 0 -.185 .007 c.115 .203 .226 .407 .333 .61Z m.856 1.888 c.61 .031 1.14 .119 1.879 .293 a.294 .294 0 0 0 .35 -.235 .31 .31 0 0 0 -.222 -.37 c-.882 -.208 -1.49 -.299 -2.258 -.319 .093 .215 .177 .426 .251 .63Z m1.879 2.168 a10.879 10.879 0 0 0 -1.525 -.268 2.95 2.95 0 0 0 -.018 -.624 11.21 11.21 0 0 1 1.67 .286 .31 .31 0 0 1 .224 .37 .294 .294 0 0 1 -.351 .236Z M10.833 13 c1.809 -.038 2.828 .07 4.594 .485 a.47 .47 0 0 0 .562 -.377 .497 .497 0 0 0 -.357 -.593 c-1.84 -.432 -2.934 -.548 -4.818 -.508 -1.174 .025 -2.072 .274 -2.922 .51 l-.012 .003 c-.852 .236 -1.657 .458 -2.713 .48 -1.809 .038 -2.828 -.07 -4.594 -.485 a.47 .47 0 0 0 -.562 .377 .497 .497 0 0 0 .357 .593 c1.84 .432 2.934 .548 4.818 .508 1.174 -.025 2.072 -.274 2.922 -.51 l.012 -.003 c.852 -.236 1.657 -.458 2.713 -.48Z m0 2 c1.809 -.038 2.828 .07 4.594 .485 a.47 .47 0 0 0 .562 -.377 .497 .497 0 0 0 -.357 -.593 c-1.84 -.432 -2.934 -.548 -4.818 -.508 -1.174 .025 -2.072 .274 -2.922 .51 l-.012 .003 c-.852 .236 -1.657 .458 -2.713 .48 -1.809 .038 -2.828 -.07 -4.594 -.485 a.47 .47 0 0 0 -.562 .377 .497 .497 0 0 0 .357 .593 c1.84 .432 2.934 .547 4.818 .508 1.174 -.025 2.072 -.274 2.922 -.51 l.012 -.003 c.852 -.236 1.657 -.458 2.713 -.48Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 9.907 4.122
                moveTo(x = 9.907f, y = 4.122f)
                // c 1.047 -0.015 1.676 0.057 2.735 0.306
                curveToRelative(
                    dx1 = 1.047f,
                    dy1 = -0.015f,
                    dx2 = 1.676f,
                    dy2 = 0.057f,
                    dx3 = 2.735f,
                    dy3 = 0.306f,
                )
                // a 0.294 0.294 0 0 0 0.35 -0.235
                arcToRelative(
                    a = 0.294f,
                    b = 0.294f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.35f,
                    dy1 = -0.235f,
                )
                // a 0.31 0.31 0 0 0 -0.222 -0.37
                arcToRelative(
                    a = 0.31f,
                    b = 0.31f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.222f,
                    dy1 = -0.37f,
                )
                // c -1.15 -0.271 -1.834 -0.343 -3.011 -0.318
                curveToRelative(
                    dx1 = -1.15f,
                    dy1 = -0.271f,
                    dx2 = -1.834f,
                    dy2 = -0.343f,
                    dx3 = -3.011f,
                    dy3 = -0.318f,
                )
                // a 5.569 5.569 0 0 0 -0.185 0.007
                arcToRelative(
                    a = 5.569f,
                    b = 5.569f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.185f,
                    dy1 = 0.007f,
                )
                // c 0.115 0.203 0.226 0.407 0.333 0.61z
                curveToRelative(
                    dx1 = 0.115f,
                    dy1 = 0.203f,
                    dx2 = 0.226f,
                    dy2 = 0.407f,
                    dx3 = 0.333f,
                    dy3 = 0.61f,
                )
                close()
                // m 0.856 1.888
                moveToRelative(dx = 0.856f, dy = 1.888f)
                // c 0.61 0.031 1.14 0.119 1.879 0.293
                curveToRelative(
                    dx1 = 0.61f,
                    dy1 = 0.031f,
                    dx2 = 1.14f,
                    dy2 = 0.119f,
                    dx3 = 1.879f,
                    dy3 = 0.293f,
                )
                // a 0.294 0.294 0 0 0 0.35 -0.235
                arcToRelative(
                    a = 0.294f,
                    b = 0.294f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.35f,
                    dy1 = -0.235f,
                )
                // a 0.31 0.31 0 0 0 -0.222 -0.37
                arcToRelative(
                    a = 0.31f,
                    b = 0.31f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.222f,
                    dy1 = -0.37f,
                )
                // c -0.882 -0.208 -1.49 -0.299 -2.258 -0.319
                curveToRelative(
                    dx1 = -0.882f,
                    dy1 = -0.208f,
                    dx2 = -1.49f,
                    dy2 = -0.299f,
                    dx3 = -2.258f,
                    dy3 = -0.319f,
                )
                // c 0.093 0.215 0.177 0.426 0.251 0.63z
                curveToRelative(
                    dx1 = 0.093f,
                    dy1 = 0.215f,
                    dx2 = 0.177f,
                    dy2 = 0.426f,
                    dx3 = 0.251f,
                    dy3 = 0.63f,
                )
                close()
                // m 1.879 2.168
                moveToRelative(dx = 1.879f, dy = 2.168f)
                // a 10.879 10.879 0 0 0 -1.525 -0.268
                arcToRelative(
                    a = 10.879f,
                    b = 10.879f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.525f,
                    dy1 = -0.268f,
                )
                // a 2.95 2.95 0 0 0 -0.018 -0.624
                arcToRelative(
                    a = 2.95f,
                    b = 2.95f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.018f,
                    dy1 = -0.624f,
                )
                // a 11.21 11.21 0 0 1 1.67 0.286
                arcToRelative(
                    a = 11.21f,
                    b = 11.21f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.67f,
                    dy1 = 0.286f,
                )
                // a 0.31 0.31 0 0 1 0.224 0.37
                arcToRelative(
                    a = 0.31f,
                    b = 0.31f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.224f,
                    dy1 = 0.37f,
                )
                // a 0.294 0.294 0 0 1 -0.351 0.236z
                arcToRelative(
                    a = 0.294f,
                    b = 0.294f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.351f,
                    dy1 = 0.236f,
                )
                close()
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
        }.build().also { _ic1207 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1207: ImageVector? = null
