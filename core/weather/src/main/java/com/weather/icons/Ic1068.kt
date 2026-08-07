package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1068: ImageVector
    get() {
        val current = _ic1068
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1068",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M11.878 6.506 c.122 .405 .184 .903 .184 1.494 0 .59 -.062 1.09 -.184 1.498 -.12 .406 -.289 .714 -.503 .924 a1.036 1.036 0 0 1 -1.485 0 c-.214 -.21 -.383 -.518 -.505 -.924 -.121 -.408 -.181 -.907 -.181 -1.498 0 -.59 .06 -1.089 .18 -1.494 .123 -.408 .292 -.716 .506 -.924 a1.03 1.03 0 0 1 1.485 0 c.214 .208 .382 .516 .503 .924Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.878 6.506
                moveTo(x = 11.878f, y = 6.506f)
                // c 0.122 0.405 0.184 0.903 0.184 1.494
                curveToRelative(
                    dx1 = 0.122f,
                    dy1 = 0.405f,
                    dx2 = 0.184f,
                    dy2 = 0.903f,
                    dx3 = 0.184f,
                    dy3 = 1.494f,
                )
                // c 0 0.59 -0.062 1.09 -0.184 1.498
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.59f,
                    dx2 = -0.062f,
                    dy2 = 1.09f,
                    dx3 = -0.184f,
                    dy3 = 1.498f,
                )
                // c -0.12 0.406 -0.289 0.714 -0.503 0.924
                curveToRelative(
                    dx1 = -0.12f,
                    dy1 = 0.406f,
                    dx2 = -0.289f,
                    dy2 = 0.714f,
                    dx3 = -0.503f,
                    dy3 = 0.924f,
                )
                // a 1.036 1.036 0 0 1 -1.485 0
                arcToRelative(
                    a = 1.036f,
                    b = 1.036f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.485f,
                    dy1 = 0.0f,
                )
                // c -0.214 -0.21 -0.383 -0.518 -0.505 -0.924
                curveToRelative(
                    dx1 = -0.214f,
                    dy1 = -0.21f,
                    dx2 = -0.383f,
                    dy2 = -0.518f,
                    dx3 = -0.505f,
                    dy3 = -0.924f,
                )
                // c -0.121 -0.408 -0.181 -0.907 -0.181 -1.498
                curveToRelative(
                    dx1 = -0.121f,
                    dy1 = -0.408f,
                    dx2 = -0.181f,
                    dy2 = -0.907f,
                    dx3 = -0.181f,
                    dy3 = -1.498f,
                )
                // c 0 -0.59 0.06 -1.089 0.18 -1.494
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.59f,
                    dx2 = 0.06f,
                    dy2 = -1.089f,
                    dx3 = 0.18f,
                    dy3 = -1.494f,
                )
                // c 0.123 -0.408 0.292 -0.716 0.506 -0.924
                curveToRelative(
                    dx1 = 0.123f,
                    dy1 = -0.408f,
                    dx2 = 0.292f,
                    dy2 = -0.716f,
                    dx3 = 0.506f,
                    dy3 = -0.924f,
                )
                // a 1.03 1.03 0 0 1 1.485 0
                arcToRelative(
                    a = 1.03f,
                    b = 1.03f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.485f,
                    dy1 = 0.0f,
                )
                // c 0.214 0.208 0.382 0.516 0.503 0.924z
                curveToRelative(
                    dx1 = 0.214f,
                    dy1 = 0.208f,
                    dx2 = 0.382f,
                    dy2 = 0.516f,
                    dx3 = 0.503f,
                    dy3 = 0.924f,
                )
                close()
            }
            // M11 16 a3 3 0 0 0 2.99 -2.755 A2.993 2.993 0 0 0 15 11 c0 -.535 -.14 -1.037 -.385 -1.471 a2.998 2.998 0 0 0 .304 -4.835 3 3 0 0 0 -2.674 -3.684 A2.993 2.993 0 0 0 10 0 c-.768 0 -1.47 .289 -2 .764 A2.989 2.989 0 0 0 6 0 c-.893 0 -1.695 .39 -2.245 1.01 a3 3 0 0 0 -2.674 3.684 2.998 2.998 0 0 0 .304 4.834 A2.986 2.986 0 0 0 1 11 c0 .893 .39 1.695 1.01 2.245 a3 3 0 0 0 4.49 2.354 c.441 .255 .954 .401 1.5 .401 s1.059 -.146 1.5 -.401 c.441 .255 .954 .401 1.5 .401Z M6.6 6.734 a2.404 2.404 0 0 0 -.148 -.62 1.648 1.648 0 0 0 -.27 -.464 1.063 1.063 0 0 0 -.807 -.384 c-.28 0 -.528 .107 -.745 .32 -.216 .21 -.385 .52 -.508 .927 C4 6.92 3.938 7.414 3.938 8 c0 .596 .062 1.098 .184 1.506 .124 .405 .294 .712 .508 .92 a1.048 1.048 0 0 0 1.54 -.061 c.109 -.122 .2 -.271 .274 -.449 a2.27 2.27 0 0 0 .156 -.608 l.941 .008 a4.18 4.18 0 0 1 -.229 1.041 3.19 3.19 0 0 1 -.455 .86 2.12 2.12 0 0 1 -.66 .578 c-.25 .137 -.53 .205 -.837 .205 -.453 0 -.857 -.158 -1.213 -.475 -.355 -.317 -.635 -.775 -.84 -1.373 C3.102 9.554 3 8.836 3 8 c0 -.839 .103 -1.556 .31 -2.152 .206 -.598 .487 -1.056 .842 -1.373 A1.766 1.766 0 0 1 5.36 4 c.287 0 .553 .06 .8 .183 .247 .121 .466 .3 .66 .536 .192 .233 .35 .52 .475 .859 .125 .337 .208 .722 .246 1.156 h-.94Z m6.088 3.422 c-.206 .596 -.488 1.052 -.845 1.369 a1.779 1.779 0 0 1 -2.423 0 c-.356 -.32 -.637 -.777 -.845 -1.373 -.207 -.598 -.31 -1.316 -.31 -2.152 0 -.839 .103 -1.556 .31 -2.152 .208 -.598 .49 -1.056 .845 -1.373 a1.779 1.779 0 0 1 2.423 0 c.357 .317 .639 .775 .845 1.373 C12.896 6.444 13 7.16 13 8 c0 .839 -.104 1.558 -.312 2.156Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11 16
                moveTo(x = 11.0f, y = 16.0f)
                // a 3 3 0 0 0 2.99 -2.755
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.99f,
                    dy1 = -2.755f,
                )
                // A 2.993 2.993 0 0 0 15 11
                arcTo(
                    horizontalEllipseRadius = 2.993f,
                    verticalEllipseRadius = 2.993f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 15.0f,
                    y1 = 11.0f,
                )
                // c 0 -0.535 -0.14 -1.037 -0.385 -1.471
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.535f,
                    dx2 = -0.14f,
                    dy2 = -1.037f,
                    dx3 = -0.385f,
                    dy3 = -1.471f,
                )
                // a 2.998 2.998 0 0 0 0.304 -4.835
                arcToRelative(
                    a = 2.998f,
                    b = 2.998f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.304f,
                    dy1 = -4.835f,
                )
                // a 3 3 0 0 0 -2.674 -3.684
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.674f,
                    dy1 = -3.684f,
                )
                // A 2.993 2.993 0 0 0 10 0
                arcTo(
                    horizontalEllipseRadius = 2.993f,
                    verticalEllipseRadius = 2.993f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 10.0f,
                    y1 = 0.0f,
                )
                // c -0.768 0 -1.47 0.289 -2 0.764
                curveToRelative(
                    dx1 = -0.768f,
                    dy1 = 0.0f,
                    dx2 = -1.47f,
                    dy2 = 0.289f,
                    dx3 = -2.0f,
                    dy3 = 0.764f,
                )
                // A 2.989 2.989 0 0 0 6 0
                arcTo(
                    horizontalEllipseRadius = 2.989f,
                    verticalEllipseRadius = 2.989f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 6.0f,
                    y1 = 0.0f,
                )
                // c -0.893 0 -1.695 0.39 -2.245 1.01
                curveToRelative(
                    dx1 = -0.893f,
                    dy1 = 0.0f,
                    dx2 = -1.695f,
                    dy2 = 0.39f,
                    dx3 = -2.245f,
                    dy3 = 1.01f,
                )
                // a 3 3 0 0 0 -2.674 3.684
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.674f,
                    dy1 = 3.684f,
                )
                // a 2.998 2.998 0 0 0 0.304 4.834
                arcToRelative(
                    a = 2.998f,
                    b = 2.998f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.304f,
                    dy1 = 4.834f,
                )
                // A 2.986 2.986 0 0 0 1 11
                arcTo(
                    horizontalEllipseRadius = 2.986f,
                    verticalEllipseRadius = 2.986f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 1.0f,
                    y1 = 11.0f,
                )
                // c 0 0.893 0.39 1.695 1.01 2.245
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.893f,
                    dx2 = 0.39f,
                    dy2 = 1.695f,
                    dx3 = 1.01f,
                    dy3 = 2.245f,
                )
                // a 3 3 0 0 0 4.49 2.354
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 4.49f,
                    dy1 = 2.354f,
                )
                // c 0.441 0.255 0.954 0.401 1.5 0.401
                curveToRelative(
                    dx1 = 0.441f,
                    dy1 = 0.255f,
                    dx2 = 0.954f,
                    dy2 = 0.401f,
                    dx3 = 1.5f,
                    dy3 = 0.401f,
                )
                // s 1.059 -0.146 1.5 -0.401
                reflectiveCurveToRelative(
                    dx1 = 1.059f,
                    dy1 = -0.146f,
                    dx2 = 1.5f,
                    dy2 = -0.401f,
                )
                // c 0.441 0.255 0.954 0.401 1.5 0.401z
                curveToRelative(
                    dx1 = 0.441f,
                    dy1 = 0.255f,
                    dx2 = 0.954f,
                    dy2 = 0.401f,
                    dx3 = 1.5f,
                    dy3 = 0.401f,
                )
                close()
                // M 6.6 6.734
                moveTo(x = 6.6f, y = 6.734f)
                // a 2.404 2.404 0 0 0 -0.148 -0.62
                arcToRelative(
                    a = 2.404f,
                    b = 2.404f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.148f,
                    dy1 = -0.62f,
                )
                // a 1.648 1.648 0 0 0 -0.27 -0.464
                arcToRelative(
                    a = 1.648f,
                    b = 1.648f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.27f,
                    dy1 = -0.464f,
                )
                // a 1.063 1.063 0 0 0 -0.807 -0.384
                arcToRelative(
                    a = 1.063f,
                    b = 1.063f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.807f,
                    dy1 = -0.384f,
                )
                // c -0.28 0 -0.528 0.107 -0.745 0.32
                curveToRelative(
                    dx1 = -0.28f,
                    dy1 = 0.0f,
                    dx2 = -0.528f,
                    dy2 = 0.107f,
                    dx3 = -0.745f,
                    dy3 = 0.32f,
                )
                // c -0.216 0.21 -0.385 0.52 -0.508 0.927
                curveToRelative(
                    dx1 = -0.216f,
                    dy1 = 0.21f,
                    dx2 = -0.385f,
                    dy2 = 0.52f,
                    dx3 = -0.508f,
                    dy3 = 0.927f,
                )
                // C 4 6.92 3.938 7.414 3.938 8
                curveTo(
                    x1 = 4.0f,
                    y1 = 6.92f,
                    x2 = 3.938f,
                    y2 = 7.414f,
                    x3 = 3.938f,
                    y3 = 8.0f,
                )
                // c 0 0.596 0.062 1.098 0.184 1.506
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.596f,
                    dx2 = 0.062f,
                    dy2 = 1.098f,
                    dx3 = 0.184f,
                    dy3 = 1.506f,
                )
                // c 0.124 0.405 0.294 0.712 0.508 0.92
                curveToRelative(
                    dx1 = 0.124f,
                    dy1 = 0.405f,
                    dx2 = 0.294f,
                    dy2 = 0.712f,
                    dx3 = 0.508f,
                    dy3 = 0.92f,
                )
                // a 1.048 1.048 0 0 0 1.54 -0.061
                arcToRelative(
                    a = 1.048f,
                    b = 1.048f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.54f,
                    dy1 = -0.061f,
                )
                // c 0.109 -0.122 0.2 -0.271 0.274 -0.449
                curveToRelative(
                    dx1 = 0.109f,
                    dy1 = -0.122f,
                    dx2 = 0.2f,
                    dy2 = -0.271f,
                    dx3 = 0.274f,
                    dy3 = -0.449f,
                )
                // a 2.27 2.27 0 0 0 0.156 -0.608
                arcToRelative(
                    a = 2.27f,
                    b = 2.27f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.156f,
                    dy1 = -0.608f,
                )
                // l 0.941 0.008
                lineToRelative(dx = 0.941f, dy = 0.008f)
                // a 4.18 4.18 0 0 1 -0.229 1.041
                arcToRelative(
                    a = 4.18f,
                    b = 4.18f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.229f,
                    dy1 = 1.041f,
                )
                // a 3.19 3.19 0 0 1 -0.455 0.86
                arcToRelative(
                    a = 3.19f,
                    b = 3.19f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.455f,
                    dy1 = 0.86f,
                )
                // a 2.12 2.12 0 0 1 -0.66 0.578
                arcToRelative(
                    a = 2.12f,
                    b = 2.12f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.66f,
                    dy1 = 0.578f,
                )
                // c -0.25 0.137 -0.53 0.205 -0.837 0.205
                curveToRelative(
                    dx1 = -0.25f,
                    dy1 = 0.137f,
                    dx2 = -0.53f,
                    dy2 = 0.205f,
                    dx3 = -0.837f,
                    dy3 = 0.205f,
                )
                // c -0.453 0 -0.857 -0.158 -1.213 -0.475
                curveToRelative(
                    dx1 = -0.453f,
                    dy1 = 0.0f,
                    dx2 = -0.857f,
                    dy2 = -0.158f,
                    dx3 = -1.213f,
                    dy3 = -0.475f,
                )
                // c -0.355 -0.317 -0.635 -0.775 -0.84 -1.373
                curveToRelative(
                    dx1 = -0.355f,
                    dy1 = -0.317f,
                    dx2 = -0.635f,
                    dy2 = -0.775f,
                    dx3 = -0.84f,
                    dy3 = -1.373f,
                )
                // C 3.102 9.554 3 8.836 3 8
                curveTo(
                    x1 = 3.102f,
                    y1 = 9.554f,
                    x2 = 3.0f,
                    y2 = 8.836f,
                    x3 = 3.0f,
                    y3 = 8.0f,
                )
                // c 0 -0.839 0.103 -1.556 0.31 -2.152
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.839f,
                    dx2 = 0.103f,
                    dy2 = -1.556f,
                    dx3 = 0.31f,
                    dy3 = -2.152f,
                )
                // c 0.206 -0.598 0.487 -1.056 0.842 -1.373
                curveToRelative(
                    dx1 = 0.206f,
                    dy1 = -0.598f,
                    dx2 = 0.487f,
                    dy2 = -1.056f,
                    dx3 = 0.842f,
                    dy3 = -1.373f,
                )
                // A 1.766 1.766 0 0 1 5.36 4
                arcTo(
                    horizontalEllipseRadius = 1.766f,
                    verticalEllipseRadius = 1.766f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 5.36f,
                    y1 = 4.0f,
                )
                // c 0.287 0 0.553 0.06 0.8 0.183
                curveToRelative(
                    dx1 = 0.287f,
                    dy1 = 0.0f,
                    dx2 = 0.553f,
                    dy2 = 0.06f,
                    dx3 = 0.8f,
                    dy3 = 0.183f,
                )
                // c 0.247 0.121 0.466 0.3 0.66 0.536
                curveToRelative(
                    dx1 = 0.247f,
                    dy1 = 0.121f,
                    dx2 = 0.466f,
                    dy2 = 0.3f,
                    dx3 = 0.66f,
                    dy3 = 0.536f,
                )
                // c 0.192 0.233 0.35 0.52 0.475 0.859
                curveToRelative(
                    dx1 = 0.192f,
                    dy1 = 0.233f,
                    dx2 = 0.35f,
                    dy2 = 0.52f,
                    dx3 = 0.475f,
                    dy3 = 0.859f,
                )
                // c 0.125 0.337 0.208 0.722 0.246 1.156
                curveToRelative(
                    dx1 = 0.125f,
                    dy1 = 0.337f,
                    dx2 = 0.208f,
                    dy2 = 0.722f,
                    dx3 = 0.246f,
                    dy3 = 1.156f,
                )
                // h -0.94z
                horizontalLineToRelative(dx = -0.94f)
                close()
                // m 6.088 3.422
                moveToRelative(dx = 6.088f, dy = 3.422f)
                // c -0.206 0.596 -0.488 1.052 -0.845 1.369
                curveToRelative(
                    dx1 = -0.206f,
                    dy1 = 0.596f,
                    dx2 = -0.488f,
                    dy2 = 1.052f,
                    dx3 = -0.845f,
                    dy3 = 1.369f,
                )
                // a 1.779 1.779 0 0 1 -2.423 0
                arcToRelative(
                    a = 1.779f,
                    b = 1.779f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.423f,
                    dy1 = 0.0f,
                )
                // c -0.356 -0.32 -0.637 -0.777 -0.845 -1.373
                curveToRelative(
                    dx1 = -0.356f,
                    dy1 = -0.32f,
                    dx2 = -0.637f,
                    dy2 = -0.777f,
                    dx3 = -0.845f,
                    dy3 = -1.373f,
                )
                // c -0.207 -0.598 -0.31 -1.316 -0.31 -2.152
                curveToRelative(
                    dx1 = -0.207f,
                    dy1 = -0.598f,
                    dx2 = -0.31f,
                    dy2 = -1.316f,
                    dx3 = -0.31f,
                    dy3 = -2.152f,
                )
                // c 0 -0.839 0.103 -1.556 0.31 -2.152
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.839f,
                    dx2 = 0.103f,
                    dy2 = -1.556f,
                    dx3 = 0.31f,
                    dy3 = -2.152f,
                )
                // c 0.208 -0.598 0.49 -1.056 0.845 -1.373
                curveToRelative(
                    dx1 = 0.208f,
                    dy1 = -0.598f,
                    dx2 = 0.49f,
                    dy2 = -1.056f,
                    dx3 = 0.845f,
                    dy3 = -1.373f,
                )
                // a 1.779 1.779 0 0 1 2.423 0
                arcToRelative(
                    a = 1.779f,
                    b = 1.779f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.423f,
                    dy1 = 0.0f,
                )
                // c 0.357 0.317 0.639 0.775 0.845 1.373
                curveToRelative(
                    dx1 = 0.357f,
                    dy1 = 0.317f,
                    dx2 = 0.639f,
                    dy2 = 0.775f,
                    dx3 = 0.845f,
                    dy3 = 1.373f,
                )
                // C 12.896 6.444 13 7.16 13 8
                curveTo(
                    x1 = 12.896f,
                    y1 = 6.444f,
                    x2 = 13.0f,
                    y2 = 7.16f,
                    x3 = 13.0f,
                    y3 = 8.0f,
                )
                // c 0 0.839 -0.104 1.558 -0.312 2.156z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.839f,
                    dx2 = -0.104f,
                    dy2 = 1.558f,
                    dx3 = -0.312f,
                    dy3 = 2.156f,
                )
                close()
            }
        }.build().also { _ic1068 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1068: ImageVector? = null
