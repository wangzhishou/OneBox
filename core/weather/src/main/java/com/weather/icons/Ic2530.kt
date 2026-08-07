package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2530: ImageVector
    get() {
        val current = _ic2530
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2530",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M11.176 .027 a2.35 2.35 0 0 1 2.5 1.964 c.02 .2 .025 .402 .013 .603 v6.408 c0 .14 .01 .278 .027 .417 a.26 .26 0 0 0 .083 .137 3.692 3.692 0 0 1 -1.607 6.368 c-.167 .037 -.336 .065 -.504 .096 h-.657 l.001 -.002 c-.223 -.046 -.45 -.078 -.67 -.14 a3.68 3.68 0 0 1 -2.693 -3.573 3.583 3.583 0 0 1 1.193 -2.689 .493 .493 0 0 0 .17 -.39 c0 -1.006 .003 -2.01 0 -3.016 -.006 -1.285 -.027 -2.57 -.023 -3.856 A2.35 2.35 0 0 1 11.176 .027Z M9.949 9.638 a.57 .57 0 0 1 -.24 .495 2.707 2.707 0 0 0 -1.096 2.054 2.76 2.76 0 0 0 3.206 2.85 2.624 2.624 0 0 0 2.199 -2.047 c.284 -1.147 -.086 -2.114 -1.01 -2.857 a.579 .579 0 0 1 -.24 -.496 c.006 -.411 .002 -.822 .002 -1.234 V8.25 c-.28 .084 -.535 .17 -.796 .234 l-.145 -.017 v2.252 a.188 .188 0 0 0 .134 .207 1.542 1.542 0 0 1 .368 2.61 1.54 1.54 0 1 1 -1.573 -2.612 .187 .187 0 0 0 .13 -.208 c-.004 -.683 -.002 -1.365 -.002 -2.047 0 -.046 -.005 -.093 -.007 -.126 l.001 -.188 -.934 -.11 c0 .468 -.004 .931 .003 1.393Z m1.305 2.109 a.6 .6 0 0 0 -.446 .808 .598 .598 0 0 0 .97 .206 .6 .6 0 0 0 .084 -.752 .597 .597 0 0 0 -.608 -.262Z M11.204 .975 a1.41 1.41 0 0 0 -1.255 1.402 c-.01 1.525 -.004 3.051 -.002 4.576 a.21 .21 0 0 0 .045 .14 c.246 .238 .557 .399 .893 .462 v-2.05 a1.27 1.27 0 0 1 .014 -.218 .464 .464 0 0 1 .403 -.379 .458 .458 0 0 1 .493 .3 .86 .86 0 0 1 .033 .276 c.002 1.395 .001 .461 .001 1.857 v.167 l.223 -.011 a1.89 1.89 0 0 0 .603 -.334 .29 .29 0 0 0 .116 -.253 c-.004 -1.484 -.002 -2.968 -.004 -4.452 .002 -.13 -.01 -.26 -.035 -.387 A1.41 1.41 0 0 0 11.203 .975Z M3.645 7.727 a.292 .292 0 0 1 .29 .3 v1.299 l1.122 -.655 a.29 .29 0 0 1 .427 .323 .29 .29 0 0 1 -.134 .176 l-1.13 .66 1.13 .66 a.29 .29 0 0 1 -.147 .54 v.001 a.285 .285 0 0 1 -.145 -.04 l-1.123 -.657 v1.3 a.29 .29 0 0 1 -.29 .29 .29 .29 0 0 1 -.29 -.29 v-1.3 l-1.121 .657 a.287 .287 0 0 1 -.323 -.02 .29 .29 0 0 1 .03 -.48 l1.13 -.661 -1.13 -.66 a.289 .289 0 0 1 .073 -.53 .29 .29 0 0 1 .22 .03 l1.122 .655 V8.026 a.293 .293 0 0 1 .08 -.21 .29 .29 0 0 1 .209 -.089Z m.812 -2.375 c.364 .001 .721 .106 1.027 .305 a1.082 1.082 0 0 0 1.18 .003 .405 .405 0 0 1 .442 .678 1.886 1.886 0 0 1 -1.03 .305 h.002 c-.365 0 -.723 -.107 -1.03 -.306 a1.083 1.083 0 0 0 -1.189 .003 1.888 1.888 0 0 1 -2.054 -.004 1.08 1.08 0 0 0 -1.179 0 .407 .407 0 0 1 -.304 .056 .405 .405 0 0 1 -.137 -.734 1.891 1.891 0 0 1 2.06 0 1.081 1.081 0 0 0 1.178 0 l.008 -.005 a1.886 1.886 0 0 1 1.026 -.3Z M4.456 2.84 c.364 0 .72 .106 1.025 .303 l.006 .005 a1.08 1.08 0 0 0 1.177 0 .406 .406 0 0 1 .442 .679 1.89 1.89 0 0 1 -2.056 .002 l-.007 -.005 a1.08 1.08 0 0 0 -1.178 0 l-.008 .006 a1.867 1.867 0 0 1 -1.02 .302 V4.13 a1.89 1.89 0 0 1 -1.033 -.307 1.082 1.082 0 0 0 -1.179 0 .404 .404 0 0 1 -.44 -.677 1.89 1.89 0 0 1 2.06 0 1.081 1.081 0 0 0 1.178 0 l.009 -.006 a1.886 1.886 0 0 1 1.024 -.301Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.176 0.027
                moveTo(x = 11.176f, y = 0.027f)
                // a 2.35 2.35 0 0 1 2.5 1.964
                arcToRelative(
                    a = 2.35f,
                    b = 2.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.5f,
                    dy1 = 1.964f,
                )
                // c 0.02 0.2 0.025 0.402 0.013 0.603
                curveToRelative(
                    dx1 = 0.02f,
                    dy1 = 0.2f,
                    dx2 = 0.025f,
                    dy2 = 0.402f,
                    dx3 = 0.013f,
                    dy3 = 0.603f,
                )
                // v 6.408
                verticalLineToRelative(dy = 6.408f)
                // c 0 0.14 0.01 0.278 0.027 0.417
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.14f,
                    dx2 = 0.01f,
                    dy2 = 0.278f,
                    dx3 = 0.027f,
                    dy3 = 0.417f,
                )
                // a 0.26 0.26 0 0 0 0.083 0.137
                arcToRelative(
                    a = 0.26f,
                    b = 0.26f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.083f,
                    dy1 = 0.137f,
                )
                // a 3.692 3.692 0 0 1 -1.607 6.368
                arcToRelative(
                    a = 3.692f,
                    b = 3.692f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.607f,
                    dy1 = 6.368f,
                )
                // c -0.167 0.037 -0.336 0.065 -0.504 0.096
                curveToRelative(
                    dx1 = -0.167f,
                    dy1 = 0.037f,
                    dx2 = -0.336f,
                    dy2 = 0.065f,
                    dx3 = -0.504f,
                    dy3 = 0.096f,
                )
                // h -0.657
                horizontalLineToRelative(dx = -0.657f)
                // l 0.001 -0.002
                lineToRelative(dx = 0.001f, dy = -0.002f)
                // c -0.223 -0.046 -0.45 -0.078 -0.67 -0.14
                curveToRelative(
                    dx1 = -0.223f,
                    dy1 = -0.046f,
                    dx2 = -0.45f,
                    dy2 = -0.078f,
                    dx3 = -0.67f,
                    dy3 = -0.14f,
                )
                // a 3.68 3.68 0 0 1 -2.693 -3.573
                arcToRelative(
                    a = 3.68f,
                    b = 3.68f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.693f,
                    dy1 = -3.573f,
                )
                // a 3.583 3.583 0 0 1 1.193 -2.689
                arcToRelative(
                    a = 3.583f,
                    b = 3.583f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.193f,
                    dy1 = -2.689f,
                )
                // a 0.493 0.493 0 0 0 0.17 -0.39
                arcToRelative(
                    a = 0.493f,
                    b = 0.493f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.17f,
                    dy1 = -0.39f,
                )
                // c 0 -1.006 0.003 -2.01 0 -3.016
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.006f,
                    dx2 = 0.003f,
                    dy2 = -2.01f,
                    dx3 = 0.0f,
                    dy3 = -3.016f,
                )
                // c -0.006 -1.285 -0.027 -2.57 -0.023 -3.856
                curveToRelative(
                    dx1 = -0.006f,
                    dy1 = -1.285f,
                    dx2 = -0.027f,
                    dy2 = -2.57f,
                    dx3 = -0.023f,
                    dy3 = -3.856f,
                )
                // A 2.35 2.35 0 0 1 11.176 0.027z
                arcTo(
                    horizontalEllipseRadius = 2.35f,
                    verticalEllipseRadius = 2.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 11.176f,
                    y1 = 0.027f,
                )
                close()
                // M 9.949 9.638
                moveTo(x = 9.949f, y = 9.638f)
                // a 0.57 0.57 0 0 1 -0.24 0.495
                arcToRelative(
                    a = 0.57f,
                    b = 0.57f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.24f,
                    dy1 = 0.495f,
                )
                // a 2.707 2.707 0 0 0 -1.096 2.054
                arcToRelative(
                    a = 2.707f,
                    b = 2.707f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.096f,
                    dy1 = 2.054f,
                )
                // a 2.76 2.76 0 0 0 3.206 2.85
                arcToRelative(
                    a = 2.76f,
                    b = 2.76f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 3.206f,
                    dy1 = 2.85f,
                )
                // a 2.624 2.624 0 0 0 2.199 -2.047
                arcToRelative(
                    a = 2.624f,
                    b = 2.624f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.199f,
                    dy1 = -2.047f,
                )
                // c 0.284 -1.147 -0.086 -2.114 -1.01 -2.857
                curveToRelative(
                    dx1 = 0.284f,
                    dy1 = -1.147f,
                    dx2 = -0.086f,
                    dy2 = -2.114f,
                    dx3 = -1.01f,
                    dy3 = -2.857f,
                )
                // a 0.579 0.579 0 0 1 -0.24 -0.496
                arcToRelative(
                    a = 0.579f,
                    b = 0.579f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.24f,
                    dy1 = -0.496f,
                )
                // c 0.006 -0.411 0.002 -0.822 0.002 -1.234
                curveToRelative(
                    dx1 = 0.006f,
                    dy1 = -0.411f,
                    dx2 = 0.002f,
                    dy2 = -0.822f,
                    dx3 = 0.002f,
                    dy3 = -1.234f,
                )
                // V 8.25
                verticalLineTo(y = 8.25f)
                // c -0.28 0.084 -0.535 0.17 -0.796 0.234
                curveToRelative(
                    dx1 = -0.28f,
                    dy1 = 0.084f,
                    dx2 = -0.535f,
                    dy2 = 0.17f,
                    dx3 = -0.796f,
                    dy3 = 0.234f,
                )
                // l -0.145 -0.017
                lineToRelative(dx = -0.145f, dy = -0.017f)
                // v 2.252
                verticalLineToRelative(dy = 2.252f)
                // a 0.188 0.188 0 0 0 0.134 0.207
                arcToRelative(
                    a = 0.188f,
                    b = 0.188f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.134f,
                    dy1 = 0.207f,
                )
                // a 1.542 1.542 0 0 1 0.368 2.61
                arcToRelative(
                    a = 1.542f,
                    b = 1.542f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.368f,
                    dy1 = 2.61f,
                )
                // a 1.54 1.54 0 1 1 -1.573 -2.612
                arcToRelative(
                    a = 1.54f,
                    b = 1.54f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.573f,
                    dy1 = -2.612f,
                )
                // a 0.187 0.187 0 0 0 0.13 -0.208
                arcToRelative(
                    a = 0.187f,
                    b = 0.187f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.13f,
                    dy1 = -0.208f,
                )
                // c -0.004 -0.683 -0.002 -1.365 -0.002 -2.047
                curveToRelative(
                    dx1 = -0.004f,
                    dy1 = -0.683f,
                    dx2 = -0.002f,
                    dy2 = -1.365f,
                    dx3 = -0.002f,
                    dy3 = -2.047f,
                )
                // c 0 -0.046 -0.005 -0.093 -0.007 -0.126
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.046f,
                    dx2 = -0.005f,
                    dy2 = -0.093f,
                    dx3 = -0.007f,
                    dy3 = -0.126f,
                )
                // l 0.001 -0.188
                lineToRelative(dx = 0.001f, dy = -0.188f)
                // l -0.934 -0.11
                lineToRelative(dx = -0.934f, dy = -0.11f)
                // c 0 0.468 -0.004 0.931 0.003 1.393z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.468f,
                    dx2 = -0.004f,
                    dy2 = 0.931f,
                    dx3 = 0.003f,
                    dy3 = 1.393f,
                )
                close()
                // m 1.305 2.109
                moveToRelative(dx = 1.305f, dy = 2.109f)
                // a 0.6 0.6 0 0 0 -0.446 0.808
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.446f,
                    dy1 = 0.808f,
                )
                // a 0.598 0.598 0 0 0 0.97 0.206
                arcToRelative(
                    a = 0.598f,
                    b = 0.598f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.97f,
                    dy1 = 0.206f,
                )
                // a 0.6 0.6 0 0 0 0.084 -0.752
                arcToRelative(
                    a = 0.6f,
                    b = 0.6f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.084f,
                    dy1 = -0.752f,
                )
                // a 0.597 0.597 0 0 0 -0.608 -0.262z
                arcToRelative(
                    a = 0.597f,
                    b = 0.597f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.608f,
                    dy1 = -0.262f,
                )
                close()
                // M 11.204 0.975
                moveTo(x = 11.204f, y = 0.975f)
                // a 1.41 1.41 0 0 0 -1.255 1.402
                arcToRelative(
                    a = 1.41f,
                    b = 1.41f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.255f,
                    dy1 = 1.402f,
                )
                // c -0.01 1.525 -0.004 3.051 -0.002 4.576
                curveToRelative(
                    dx1 = -0.01f,
                    dy1 = 1.525f,
                    dx2 = -0.004f,
                    dy2 = 3.051f,
                    dx3 = -0.002f,
                    dy3 = 4.576f,
                )
                // a 0.21 0.21 0 0 0 0.045 0.14
                arcToRelative(
                    a = 0.21f,
                    b = 0.21f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.045f,
                    dy1 = 0.14f,
                )
                // c 0.246 0.238 0.557 0.399 0.893 0.462
                curveToRelative(
                    dx1 = 0.246f,
                    dy1 = 0.238f,
                    dx2 = 0.557f,
                    dy2 = 0.399f,
                    dx3 = 0.893f,
                    dy3 = 0.462f,
                )
                // v -2.05
                verticalLineToRelative(dy = -2.05f)
                // a 1.27 1.27 0 0 1 0.014 -0.218
                arcToRelative(
                    a = 1.27f,
                    b = 1.27f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.014f,
                    dy1 = -0.218f,
                )
                // a 0.464 0.464 0 0 1 0.403 -0.379
                arcToRelative(
                    a = 0.464f,
                    b = 0.464f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.403f,
                    dy1 = -0.379f,
                )
                // a 0.458 0.458 0 0 1 0.493 0.3
                arcToRelative(
                    a = 0.458f,
                    b = 0.458f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.493f,
                    dy1 = 0.3f,
                )
                // a 0.86 0.86 0 0 1 0.033 0.276
                arcToRelative(
                    a = 0.86f,
                    b = 0.86f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.033f,
                    dy1 = 0.276f,
                )
                // c 0.002 1.395 0.001 0.461 0.001 1.857
                curveToRelative(
                    dx1 = 0.002f,
                    dy1 = 1.395f,
                    dx2 = 0.001f,
                    dy2 = 0.461f,
                    dx3 = 0.001f,
                    dy3 = 1.857f,
                )
                // v 0.167
                verticalLineToRelative(dy = 0.167f)
                // l 0.223 -0.011
                lineToRelative(dx = 0.223f, dy = -0.011f)
                // a 1.89 1.89 0 0 0 0.603 -0.334
                arcToRelative(
                    a = 1.89f,
                    b = 1.89f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.603f,
                    dy1 = -0.334f,
                )
                // a 0.29 0.29 0 0 0 0.116 -0.253
                arcToRelative(
                    a = 0.29f,
                    b = 0.29f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.116f,
                    dy1 = -0.253f,
                )
                // c -0.004 -1.484 -0.002 -2.968 -0.004 -4.452
                curveToRelative(
                    dx1 = -0.004f,
                    dy1 = -1.484f,
                    dx2 = -0.002f,
                    dy2 = -2.968f,
                    dx3 = -0.004f,
                    dy3 = -4.452f,
                )
                // c 0.002 -0.13 -0.01 -0.26 -0.035 -0.387
                curveToRelative(
                    dx1 = 0.002f,
                    dy1 = -0.13f,
                    dx2 = -0.01f,
                    dy2 = -0.26f,
                    dx3 = -0.035f,
                    dy3 = -0.387f,
                )
                // A 1.41 1.41 0 0 0 11.203 0.975z
                arcTo(
                    horizontalEllipseRadius = 1.41f,
                    verticalEllipseRadius = 1.41f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 11.203f,
                    y1 = 0.975f,
                )
                close()
                // M 3.645 7.727
                moveTo(x = 3.645f, y = 7.727f)
                // a 0.292 0.292 0 0 1 0.29 0.3
                arcToRelative(
                    a = 0.292f,
                    b = 0.292f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.29f,
                    dy1 = 0.3f,
                )
                // v 1.299
                verticalLineToRelative(dy = 1.299f)
                // l 1.122 -0.655
                lineToRelative(dx = 1.122f, dy = -0.655f)
                // a 0.29 0.29 0 0 1 0.427 0.323
                arcToRelative(
                    a = 0.29f,
                    b = 0.29f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.427f,
                    dy1 = 0.323f,
                )
                // a 0.29 0.29 0 0 1 -0.134 0.176
                arcToRelative(
                    a = 0.29f,
                    b = 0.29f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.134f,
                    dy1 = 0.176f,
                )
                // l -1.13 0.66
                lineToRelative(dx = -1.13f, dy = 0.66f)
                // l 1.13 0.66
                lineToRelative(dx = 1.13f, dy = 0.66f)
                // a 0.29 0.29 0 0 1 -0.147 0.54
                arcToRelative(
                    a = 0.29f,
                    b = 0.29f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.147f,
                    dy1 = 0.54f,
                )
                // v 0.001
                verticalLineToRelative(dy = 0.001f)
                // a 0.285 0.285 0 0 1 -0.145 -0.04
                arcToRelative(
                    a = 0.285f,
                    b = 0.285f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.145f,
                    dy1 = -0.04f,
                )
                // l -1.123 -0.657
                lineToRelative(dx = -1.123f, dy = -0.657f)
                // v 1.3
                verticalLineToRelative(dy = 1.3f)
                // a 0.29 0.29 0 0 1 -0.29 0.29
                arcToRelative(
                    a = 0.29f,
                    b = 0.29f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.29f,
                    dy1 = 0.29f,
                )
                // a 0.29 0.29 0 0 1 -0.29 -0.29
                arcToRelative(
                    a = 0.29f,
                    b = 0.29f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.29f,
                    dy1 = -0.29f,
                )
                // v -1.3
                verticalLineToRelative(dy = -1.3f)
                // l -1.121 0.657
                lineToRelative(dx = -1.121f, dy = 0.657f)
                // a 0.287 0.287 0 0 1 -0.323 -0.02
                arcToRelative(
                    a = 0.287f,
                    b = 0.287f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.323f,
                    dy1 = -0.02f,
                )
                // a 0.29 0.29 0 0 1 0.03 -0.48
                arcToRelative(
                    a = 0.29f,
                    b = 0.29f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.03f,
                    dy1 = -0.48f,
                )
                // l 1.13 -0.661
                lineToRelative(dx = 1.13f, dy = -0.661f)
                // l -1.13 -0.66
                lineToRelative(dx = -1.13f, dy = -0.66f)
                // a 0.289 0.289 0 0 1 0.073 -0.53
                arcToRelative(
                    a = 0.289f,
                    b = 0.289f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.073f,
                    dy1 = -0.53f,
                )
                // a 0.29 0.29 0 0 1 0.22 0.03
                arcToRelative(
                    a = 0.29f,
                    b = 0.29f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.22f,
                    dy1 = 0.03f,
                )
                // l 1.122 0.655
                lineToRelative(dx = 1.122f, dy = 0.655f)
                // V 8.026
                verticalLineTo(y = 8.026f)
                // a 0.293 0.293 0 0 1 0.08 -0.21
                arcToRelative(
                    a = 0.293f,
                    b = 0.293f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.08f,
                    dy1 = -0.21f,
                )
                // a 0.29 0.29 0 0 1 0.209 -0.089z
                arcToRelative(
                    a = 0.29f,
                    b = 0.29f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.209f,
                    dy1 = -0.089f,
                )
                close()
                // m 0.812 -2.375
                moveToRelative(dx = 0.812f, dy = -2.375f)
                // c 0.364 0.001 0.721 0.106 1.027 0.305
                curveToRelative(
                    dx1 = 0.364f,
                    dy1 = 0.001f,
                    dx2 = 0.721f,
                    dy2 = 0.106f,
                    dx3 = 1.027f,
                    dy3 = 0.305f,
                )
                // a 1.082 1.082 0 0 0 1.18 0.003
                arcToRelative(
                    a = 1.082f,
                    b = 1.082f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.18f,
                    dy1 = 0.003f,
                )
                // a 0.405 0.405 0 0 1 0.442 0.678
                arcToRelative(
                    a = 0.405f,
                    b = 0.405f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.442f,
                    dy1 = 0.678f,
                )
                // a 1.886 1.886 0 0 1 -1.03 0.305
                arcToRelative(
                    a = 1.886f,
                    b = 1.886f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.03f,
                    dy1 = 0.305f,
                )
                // h 0.002
                horizontalLineToRelative(dx = 0.002f)
                // c -0.365 0 -0.723 -0.107 -1.03 -0.306
                curveToRelative(
                    dx1 = -0.365f,
                    dy1 = 0.0f,
                    dx2 = -0.723f,
                    dy2 = -0.107f,
                    dx3 = -1.03f,
                    dy3 = -0.306f,
                )
                // a 1.083 1.083 0 0 0 -1.189 0.003
                arcToRelative(
                    a = 1.083f,
                    b = 1.083f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.189f,
                    dy1 = 0.003f,
                )
                // a 1.888 1.888 0 0 1 -2.054 -0.004
                arcToRelative(
                    a = 1.888f,
                    b = 1.888f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.054f,
                    dy1 = -0.004f,
                )
                // a 1.08 1.08 0 0 0 -1.179 0
                arcToRelative(
                    a = 1.08f,
                    b = 1.08f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.179f,
                    dy1 = 0.0f,
                )
                // a 0.407 0.407 0 0 1 -0.304 0.056
                arcToRelative(
                    a = 0.407f,
                    b = 0.407f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.304f,
                    dy1 = 0.056f,
                )
                // a 0.405 0.405 0 0 1 -0.137 -0.734
                arcToRelative(
                    a = 0.405f,
                    b = 0.405f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.137f,
                    dy1 = -0.734f,
                )
                // a 1.891 1.891 0 0 1 2.06 0
                arcToRelative(
                    a = 1.891f,
                    b = 1.891f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.06f,
                    dy1 = 0.0f,
                )
                // a 1.081 1.081 0 0 0 1.178 0
                arcToRelative(
                    a = 1.081f,
                    b = 1.081f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.178f,
                    dy1 = 0.0f,
                )
                // l 0.008 -0.005
                lineToRelative(dx = 0.008f, dy = -0.005f)
                // a 1.886 1.886 0 0 1 1.026 -0.3z
                arcToRelative(
                    a = 1.886f,
                    b = 1.886f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.026f,
                    dy1 = -0.3f,
                )
                close()
                // M 4.456 2.84
                moveTo(x = 4.456f, y = 2.84f)
                // c 0.364 0 0.72 0.106 1.025 0.303
                curveToRelative(
                    dx1 = 0.364f,
                    dy1 = 0.0f,
                    dx2 = 0.72f,
                    dy2 = 0.106f,
                    dx3 = 1.025f,
                    dy3 = 0.303f,
                )
                // l 0.006 0.005
                lineToRelative(dx = 0.006f, dy = 0.005f)
                // a 1.08 1.08 0 0 0 1.177 0
                arcToRelative(
                    a = 1.08f,
                    b = 1.08f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.177f,
                    dy1 = 0.0f,
                )
                // a 0.406 0.406 0 0 1 0.442 0.679
                arcToRelative(
                    a = 0.406f,
                    b = 0.406f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.442f,
                    dy1 = 0.679f,
                )
                // a 1.89 1.89 0 0 1 -2.056 0.002
                arcToRelative(
                    a = 1.89f,
                    b = 1.89f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.056f,
                    dy1 = 0.002f,
                )
                // l -0.007 -0.005
                lineToRelative(dx = -0.007f, dy = -0.005f)
                // a 1.08 1.08 0 0 0 -1.178 0
                arcToRelative(
                    a = 1.08f,
                    b = 1.08f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.178f,
                    dy1 = 0.0f,
                )
                // l -0.008 0.006
                lineToRelative(dx = -0.008f, dy = 0.006f)
                // a 1.867 1.867 0 0 1 -1.02 0.302
                arcToRelative(
                    a = 1.867f,
                    b = 1.867f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.02f,
                    dy1 = 0.302f,
                )
                // V 4.13
                verticalLineTo(y = 4.13f)
                // a 1.89 1.89 0 0 1 -1.033 -0.307
                arcToRelative(
                    a = 1.89f,
                    b = 1.89f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.033f,
                    dy1 = -0.307f,
                )
                // a 1.082 1.082 0 0 0 -1.179 0
                arcToRelative(
                    a = 1.082f,
                    b = 1.082f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.179f,
                    dy1 = 0.0f,
                )
                // a 0.404 0.404 0 0 1 -0.44 -0.677
                arcToRelative(
                    a = 0.404f,
                    b = 0.404f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.44f,
                    dy1 = -0.677f,
                )
                // a 1.89 1.89 0 0 1 2.06 0
                arcToRelative(
                    a = 1.89f,
                    b = 1.89f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.06f,
                    dy1 = 0.0f,
                )
                // a 1.081 1.081 0 0 0 1.178 0
                arcToRelative(
                    a = 1.081f,
                    b = 1.081f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.178f,
                    dy1 = 0.0f,
                )
                // l 0.009 -0.006
                lineToRelative(dx = 0.009f, dy = -0.006f)
                // a 1.886 1.886 0 0 1 1.024 -0.301z
                arcToRelative(
                    a = 1.886f,
                    b = 1.886f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.024f,
                    dy1 = -0.301f,
                )
                close()
            }
        }.build().also { _ic2530 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2530: ImageVector? = null
