package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2150: ImageVector
    get() {
        val current = _ic2150
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2150",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M2 0 a1 1 0 0 1 .5 1.864 v.44 l3.647 .405 a.499 .499 0 0 1 .586 -.014 l5.769 4.014 .009 -.004 .003 -.002 .012 .023 2.76 1.92 .069 .056 a.5 .5 0 0 1 .128 .225 l.001 .002 v.002 l.002 .006 .003 .013 .009 .04 a2.364 2.364 0 0 1 .031 .546 2.99 2.99 0 0 1 -.412 1.32 2.999 2.999 0 0 1 -.936 1.016 2.362 2.362 0 0 1 -.489 .246 l-.039 .013 -.013 .004 -.01 .003 a.501 .501 0 0 1 -.331 -.026 L3.746 7.894 a.5 .5 0 0 1 -.173 -.788 L2.5 4.424 V15 H4 v1 H0 v-1 h1.5 V1.864 A.998 .998 0 0 1 2 0Z m11.339 8.531 a2.363 2.363 0 0 0 -.82 .824 c-.168 .29 -.25 .603 -.287 .856 a2.57 2.57 0 0 0 -.026 .292 v.031 l1.3 .575 a1.49 1.49 0 0 0 .123 -.072 2 2 0 0 0 .622 -.682 c.207 -.357 .267 -.668 .28 -.878 a1.52 1.52 0 0 0 .002 -.138 l-1.177 -.818 a2.276 2.276 0 0 1 -.017 .01Z M10.5 6.534 a4.925 4.925 0 0 0 -.311 .255 c-.258 .228 -.53 .517 -.703 .816 -.173 .3 -.286 .68 -.355 1.016 a4.993 4.993 0 0 0 -.066 .398 l-.01 .107 c0 .007 -.003 .012 -.003 .017 l1.242 .548 c.076 -.386 .219 -.862 .492 -1.336 a4.28 4.28 0 0 1 .845 -1.035 L10.5 6.534Z M7.365 4.716 c-.32 .323 -.673 .732 -.908 1.14 -.235 .407 -.413 .917 -.533 1.356 a8.69 8.69 0 0 0 -.12 .496 l1.351 .597 c.006 -.028 .01 -.057 .017 -.086 .09 -.439 .26 -1.051 .584 -1.614 .305 -.527 .717 -.959 1.043 -1.255 l-1.217 -.847 c-.066 .062 -.14 .135 -.217 .213Z M2.5 3.076 l1.475 3.687 .02 -.077 c.14 -.512 .371 -1.21 .73 -1.83 .358 -.622 .847 -1.17 1.22 -1.547 .01 -.012 .022 -.023 .034 -.035 a.468 .468 0 0 1 -.02 -.084 L2.5 2.806 v.27Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 2 0
                moveTo(x = 2.0f, y = 0.0f)
                // a 1 1 0 0 1 0.5 1.864
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = 1.864f,
                )
                // v 0.44
                verticalLineToRelative(dy = 0.44f)
                // l 3.647 0.405
                lineToRelative(dx = 3.647f, dy = 0.405f)
                // a 0.499 0.499 0 0 1 0.586 -0.014
                arcToRelative(
                    a = 0.499f,
                    b = 0.499f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.586f,
                    dy1 = -0.014f,
                )
                // l 5.769 4.014
                lineToRelative(dx = 5.769f, dy = 4.014f)
                // l 0.009 -0.004
                lineToRelative(dx = 0.009f, dy = -0.004f)
                // l 0.003 -0.002
                lineToRelative(dx = 0.003f, dy = -0.002f)
                // l 0.012 0.023
                lineToRelative(dx = 0.012f, dy = 0.023f)
                // l 2.76 1.92
                lineToRelative(dx = 2.76f, dy = 1.92f)
                // l 0.069 0.056
                lineToRelative(dx = 0.069f, dy = 0.056f)
                // a 0.5 0.5 0 0 1 0.128 0.225
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.128f,
                    dy1 = 0.225f,
                )
                // l 0.001 0.002
                lineToRelative(dx = 0.001f, dy = 0.002f)
                // v 0.002
                verticalLineToRelative(dy = 0.002f)
                // l 0.002 0.006
                lineToRelative(dx = 0.002f, dy = 0.006f)
                // l 0.003 0.013
                lineToRelative(dx = 0.003f, dy = 0.013f)
                // l 0.009 0.04
                lineToRelative(dx = 0.009f, dy = 0.04f)
                // a 2.364 2.364 0 0 1 0.031 0.546
                arcToRelative(
                    a = 2.364f,
                    b = 2.364f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.031f,
                    dy1 = 0.546f,
                )
                // a 2.99 2.99 0 0 1 -0.412 1.32
                arcToRelative(
                    a = 2.99f,
                    b = 2.99f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.412f,
                    dy1 = 1.32f,
                )
                // a 2.999 2.999 0 0 1 -0.936 1.016
                arcToRelative(
                    a = 2.999f,
                    b = 2.999f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.936f,
                    dy1 = 1.016f,
                )
                // a 2.362 2.362 0 0 1 -0.489 0.246
                arcToRelative(
                    a = 2.362f,
                    b = 2.362f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.489f,
                    dy1 = 0.246f,
                )
                // l -0.039 0.013
                lineToRelative(dx = -0.039f, dy = 0.013f)
                // l -0.013 0.004
                lineToRelative(dx = -0.013f, dy = 0.004f)
                // l -0.01 0.003
                lineToRelative(dx = -0.01f, dy = 0.003f)
                // a 0.501 0.501 0 0 1 -0.331 -0.026
                arcToRelative(
                    a = 0.501f,
                    b = 0.501f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.331f,
                    dy1 = -0.026f,
                )
                // L 3.746 7.894
                lineTo(x = 3.746f, y = 7.894f)
                // a 0.5 0.5 0 0 1 -0.173 -0.788
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.173f,
                    dy1 = -0.788f,
                )
                // L 2.5 4.424
                lineTo(x = 2.5f, y = 4.424f)
                // V 15
                verticalLineTo(y = 15.0f)
                // H 4
                horizontalLineTo(x = 4.0f)
                // v 1
                verticalLineToRelative(dy = 1.0f)
                // H 0
                horizontalLineTo(x = 0.0f)
                // v -1
                verticalLineToRelative(dy = -1.0f)
                // h 1.5
                horizontalLineToRelative(dx = 1.5f)
                // V 1.864
                verticalLineTo(y = 1.864f)
                // A 0.998 0.998 0 0 1 2 0z
                arcTo(
                    horizontalEllipseRadius = 0.998f,
                    verticalEllipseRadius = 0.998f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 2.0f,
                    y1 = 0.0f,
                )
                close()
                // m 11.339 8.531
                moveToRelative(dx = 11.339f, dy = 8.531f)
                // a 2.363 2.363 0 0 0 -0.82 0.824
                arcToRelative(
                    a = 2.363f,
                    b = 2.363f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.82f,
                    dy1 = 0.824f,
                )
                // c -0.168 0.29 -0.25 0.603 -0.287 0.856
                curveToRelative(
                    dx1 = -0.168f,
                    dy1 = 0.29f,
                    dx2 = -0.25f,
                    dy2 = 0.603f,
                    dx3 = -0.287f,
                    dy3 = 0.856f,
                )
                // a 2.57 2.57 0 0 0 -0.026 0.292
                arcToRelative(
                    a = 2.57f,
                    b = 2.57f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.026f,
                    dy1 = 0.292f,
                )
                // v 0.031
                verticalLineToRelative(dy = 0.031f)
                // l 1.3 0.575
                lineToRelative(dx = 1.3f, dy = 0.575f)
                // a 1.49 1.49 0 0 0 0.123 -0.072
                arcToRelative(
                    a = 1.49f,
                    b = 1.49f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.123f,
                    dy1 = -0.072f,
                )
                // a 2 2 0 0 0 0.622 -0.682
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.622f,
                    dy1 = -0.682f,
                )
                // c 0.207 -0.357 0.267 -0.668 0.28 -0.878
                curveToRelative(
                    dx1 = 0.207f,
                    dy1 = -0.357f,
                    dx2 = 0.267f,
                    dy2 = -0.668f,
                    dx3 = 0.28f,
                    dy3 = -0.878f,
                )
                // a 1.52 1.52 0 0 0 0.002 -0.138
                arcToRelative(
                    a = 1.52f,
                    b = 1.52f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.002f,
                    dy1 = -0.138f,
                )
                // l -1.177 -0.818
                lineToRelative(dx = -1.177f, dy = -0.818f)
                // a 2.276 2.276 0 0 1 -0.017 0.01z
                arcToRelative(
                    a = 2.276f,
                    b = 2.276f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.017f,
                    dy1 = 0.01f,
                )
                close()
                // M 10.5 6.534
                moveTo(x = 10.5f, y = 6.534f)
                // a 4.925 4.925 0 0 0 -0.311 0.255
                arcToRelative(
                    a = 4.925f,
                    b = 4.925f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.311f,
                    dy1 = 0.255f,
                )
                // c -0.258 0.228 -0.53 0.517 -0.703 0.816
                curveToRelative(
                    dx1 = -0.258f,
                    dy1 = 0.228f,
                    dx2 = -0.53f,
                    dy2 = 0.517f,
                    dx3 = -0.703f,
                    dy3 = 0.816f,
                )
                // c -0.173 0.3 -0.286 0.68 -0.355 1.016
                curveToRelative(
                    dx1 = -0.173f,
                    dy1 = 0.3f,
                    dx2 = -0.286f,
                    dy2 = 0.68f,
                    dx3 = -0.355f,
                    dy3 = 1.016f,
                )
                // a 4.993 4.993 0 0 0 -0.066 0.398
                arcToRelative(
                    a = 4.993f,
                    b = 4.993f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.066f,
                    dy1 = 0.398f,
                )
                // l -0.01 0.107
                lineToRelative(dx = -0.01f, dy = 0.107f)
                // c 0 0.007 -0.003 0.012 -0.003 0.017
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.007f,
                    dx2 = -0.003f,
                    dy2 = 0.012f,
                    dx3 = -0.003f,
                    dy3 = 0.017f,
                )
                // l 1.242 0.548
                lineToRelative(dx = 1.242f, dy = 0.548f)
                // c 0.076 -0.386 0.219 -0.862 0.492 -1.336
                curveToRelative(
                    dx1 = 0.076f,
                    dy1 = -0.386f,
                    dx2 = 0.219f,
                    dy2 = -0.862f,
                    dx3 = 0.492f,
                    dy3 = -1.336f,
                )
                // a 4.28 4.28 0 0 1 0.845 -1.035
                arcToRelative(
                    a = 4.28f,
                    b = 4.28f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.845f,
                    dy1 = -1.035f,
                )
                // L 10.5 6.534z
                lineTo(x = 10.5f, y = 6.534f)
                close()
                // M 7.365 4.716
                moveTo(x = 7.365f, y = 4.716f)
                // c -0.32 0.323 -0.673 0.732 -0.908 1.14
                curveToRelative(
                    dx1 = -0.32f,
                    dy1 = 0.323f,
                    dx2 = -0.673f,
                    dy2 = 0.732f,
                    dx3 = -0.908f,
                    dy3 = 1.14f,
                )
                // c -0.235 0.407 -0.413 0.917 -0.533 1.356
                curveToRelative(
                    dx1 = -0.235f,
                    dy1 = 0.407f,
                    dx2 = -0.413f,
                    dy2 = 0.917f,
                    dx3 = -0.533f,
                    dy3 = 1.356f,
                )
                // a 8.69 8.69 0 0 0 -0.12 0.496
                arcToRelative(
                    a = 8.69f,
                    b = 8.69f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.12f,
                    dy1 = 0.496f,
                )
                // l 1.351 0.597
                lineToRelative(dx = 1.351f, dy = 0.597f)
                // c 0.006 -0.028 0.01 -0.057 0.017 -0.086
                curveToRelative(
                    dx1 = 0.006f,
                    dy1 = -0.028f,
                    dx2 = 0.01f,
                    dy2 = -0.057f,
                    dx3 = 0.017f,
                    dy3 = -0.086f,
                )
                // c 0.09 -0.439 0.26 -1.051 0.584 -1.614
                curveToRelative(
                    dx1 = 0.09f,
                    dy1 = -0.439f,
                    dx2 = 0.26f,
                    dy2 = -1.051f,
                    dx3 = 0.584f,
                    dy3 = -1.614f,
                )
                // c 0.305 -0.527 0.717 -0.959 1.043 -1.255
                curveToRelative(
                    dx1 = 0.305f,
                    dy1 = -0.527f,
                    dx2 = 0.717f,
                    dy2 = -0.959f,
                    dx3 = 1.043f,
                    dy3 = -1.255f,
                )
                // l -1.217 -0.847
                lineToRelative(dx = -1.217f, dy = -0.847f)
                // c -0.066 0.062 -0.14 0.135 -0.217 0.213z
                curveToRelative(
                    dx1 = -0.066f,
                    dy1 = 0.062f,
                    dx2 = -0.14f,
                    dy2 = 0.135f,
                    dx3 = -0.217f,
                    dy3 = 0.213f,
                )
                close()
                // M 2.5 3.076
                moveTo(x = 2.5f, y = 3.076f)
                // l 1.475 3.687
                lineToRelative(dx = 1.475f, dy = 3.687f)
                // l 0.02 -0.077
                lineToRelative(dx = 0.02f, dy = -0.077f)
                // c 0.14 -0.512 0.371 -1.21 0.73 -1.83
                curveToRelative(
                    dx1 = 0.14f,
                    dy1 = -0.512f,
                    dx2 = 0.371f,
                    dy2 = -1.21f,
                    dx3 = 0.73f,
                    dy3 = -1.83f,
                )
                // c 0.358 -0.622 0.847 -1.17 1.22 -1.547
                curveToRelative(
                    dx1 = 0.358f,
                    dy1 = -0.622f,
                    dx2 = 0.847f,
                    dy2 = -1.17f,
                    dx3 = 1.22f,
                    dy3 = -1.547f,
                )
                // c 0.01 -0.012 0.022 -0.023 0.034 -0.035
                curveToRelative(
                    dx1 = 0.01f,
                    dy1 = -0.012f,
                    dx2 = 0.022f,
                    dy2 = -0.023f,
                    dx3 = 0.034f,
                    dy3 = -0.035f,
                )
                // a 0.468 0.468 0 0 1 -0.02 -0.084
                arcToRelative(
                    a = 0.468f,
                    b = 0.468f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.02f,
                    dy1 = -0.084f,
                )
                // L 2.5 2.806
                lineTo(x = 2.5f, y = 2.806f)
                // v 0.27z
                verticalLineToRelative(dy = 0.27f)
                close()
            }
        }.build().also { _ic2150 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2150: ImageVector? = null
