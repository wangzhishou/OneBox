package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1601: ImageVector
    get() {
        val current = _ic1601
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1601",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M15.399 2.696 c.237 .266 .601 .675 .601 1.304 0 .344 -.036 .653 -.137 .954 -.1 .302 -.256 .57 -.463 .846 a.5 .5 0 0 1 -.8 -.6 c.164 -.218 .258 -.391 .315 -.562 .056 -.17 .085 -.365 .085 -.638 0 -.222 -.11 -.37 -.374 -.668 l-.025 -.028 C14.364 3.038 14 2.629 14 2 c0 -.344 .036 -.653 .137 -.954 .1 -.302 .256 -.57 .463 -.846 a.5 .5 0 0 1 .8 .6 1.967 1.967 0 0 0 -.315 .562 C15.03 1.532 15 1.727 15 2 c0 .222 .11 .37 .374 .668 l.025 .028Z m-11 -.899 c.237 .178 .601 .45 .601 .87 0 .23 -.036 .435 -.137 .636 -.1 .201 -.256 .38 -.463 .564 -.166 .147 -.48 .177 -.7 .066 -.22 -.11 -.266 -.32 -.1 -.466 .164 -.146 .258 -.261 .315 -.375 A.912 .912 0 0 0 4 2.667 c0 -.148 -.11 -.247 -.374 -.446 l-.025 -.018 C3.364 2.025 3 1.753 3 1.333 c0 -.23 .036 -.435 .137 -.636 .1 -.201 .256 -.38 .463 -.564 .166 -.147 .48 -.177 .7 -.066 .22 .11 .266 .32 .1 .466 a1.289 1.289 0 0 0 -.315 .375 .912 .912 0 0 0 -.085 .425 c0 .148 .11 .247 .374 .446 l.025 .018Z M2 4 c0 -.629 -.364 -1.038 -.601 -1.304 l-.025 -.028 C1.109 2.37 1 2.222 1 2 c0 -.273 .029 -.468 .085 -.638 A1.97 1.97 0 0 1 1.4 .8 .5 .5 0 1 0 .6 .2 a2.952 2.952 0 0 0 -.463 .846 C.037 1.347 0 1.656 0 2 c0 .629 .364 1.038 .601 1.304 l.025 .028 C.891 3.63 1 3.778 1 4 c0 .273 -.029 .468 -.085 .638 A1.97 1.97 0 0 1 .6 5.2 a.5 .5 0 1 0 .8 .6 c.207 -.276 .363 -.544 .463 -.846 .1 -.301 .137 -.61 .137 -.954Z m11 -1.333 c0 -.42 -.364 -.692 -.601 -.87 l-.025 -.018 C12.11 1.58 12 1.48 12 1.333 a.92 .92 0 0 1 .085 -.425 c.057 -.114 .151 -.229 .315 -.375 .166 -.147 .12 -.356 -.1 -.466 -.22 -.11 -.534 -.08 -.7 .066 -.207 .184 -.363 .363 -.463 .564 a1.38 1.38 0 0 0 -.137 .636 c0 .42 .364 .692 .601 .87 l.025 .018 c.264 .199 .374 .298 .374 .446 a.912 .912 0 0 1 -.085 .425 1.289 1.289 0 0 1 -.315 .375 c-.166 .147 -.12 .356 .1 .466 .22 .11 .534 .08 .7 -.066 .207 -.184 .363 -.363 .463 -.564 .1 -.201 .137 -.407 .137 -.636Z M6.235 8.595 A.454 .454 0 0 0 6 8.533 a.466 .466 0 0 0 -.235 .872 c.07 .041 .15 .062 .235 .062 a.454 .454 0 0 0 .403 -.23 A.444 .444 0 0 0 6.468 9 a.467 .467 0 0 0 -.233 -.406Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 15.399 2.696
                moveTo(x = 15.399f, y = 2.696f)
                // c 0.237 0.266 0.601 0.675 0.601 1.304
                curveToRelative(
                    dx1 = 0.237f,
                    dy1 = 0.266f,
                    dx2 = 0.601f,
                    dy2 = 0.675f,
                    dx3 = 0.601f,
                    dy3 = 1.304f,
                )
                // c 0 0.344 -0.036 0.653 -0.137 0.954
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.344f,
                    dx2 = -0.036f,
                    dy2 = 0.653f,
                    dx3 = -0.137f,
                    dy3 = 0.954f,
                )
                // c -0.1 0.302 -0.256 0.57 -0.463 0.846
                curveToRelative(
                    dx1 = -0.1f,
                    dy1 = 0.302f,
                    dx2 = -0.256f,
                    dy2 = 0.57f,
                    dx3 = -0.463f,
                    dy3 = 0.846f,
                )
                // a 0.5 0.5 0 0 1 -0.8 -0.6
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.8f,
                    dy1 = -0.6f,
                )
                // c 0.164 -0.218 0.258 -0.391 0.315 -0.562
                curveToRelative(
                    dx1 = 0.164f,
                    dy1 = -0.218f,
                    dx2 = 0.258f,
                    dy2 = -0.391f,
                    dx3 = 0.315f,
                    dy3 = -0.562f,
                )
                // c 0.056 -0.17 0.085 -0.365 0.085 -0.638
                curveToRelative(
                    dx1 = 0.056f,
                    dy1 = -0.17f,
                    dx2 = 0.085f,
                    dy2 = -0.365f,
                    dx3 = 0.085f,
                    dy3 = -0.638f,
                )
                // c 0 -0.222 -0.11 -0.37 -0.374 -0.668
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.222f,
                    dx2 = -0.11f,
                    dy2 = -0.37f,
                    dx3 = -0.374f,
                    dy3 = -0.668f,
                )
                // l -0.025 -0.028
                lineToRelative(dx = -0.025f, dy = -0.028f)
                // C 14.364 3.038 14 2.629 14 2
                curveTo(
                    x1 = 14.364f,
                    y1 = 3.038f,
                    x2 = 14.0f,
                    y2 = 2.629f,
                    x3 = 14.0f,
                    y3 = 2.0f,
                )
                // c 0 -0.344 0.036 -0.653 0.137 -0.954
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.344f,
                    dx2 = 0.036f,
                    dy2 = -0.653f,
                    dx3 = 0.137f,
                    dy3 = -0.954f,
                )
                // c 0.1 -0.302 0.256 -0.57 0.463 -0.846
                curveToRelative(
                    dx1 = 0.1f,
                    dy1 = -0.302f,
                    dx2 = 0.256f,
                    dy2 = -0.57f,
                    dx3 = 0.463f,
                    dy3 = -0.846f,
                )
                // a 0.5 0.5 0 0 1 0.8 0.6
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.8f,
                    dy1 = 0.6f,
                )
                // a 1.967 1.967 0 0 0 -0.315 0.562
                arcToRelative(
                    a = 1.967f,
                    b = 1.967f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.315f,
                    dy1 = 0.562f,
                )
                // C 15.03 1.532 15 1.727 15 2
                curveTo(
                    x1 = 15.03f,
                    y1 = 1.532f,
                    x2 = 15.0f,
                    y2 = 1.727f,
                    x3 = 15.0f,
                    y3 = 2.0f,
                )
                // c 0 0.222 0.11 0.37 0.374 0.668
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.222f,
                    dx2 = 0.11f,
                    dy2 = 0.37f,
                    dx3 = 0.374f,
                    dy3 = 0.668f,
                )
                // l 0.025 0.028z
                lineToRelative(dx = 0.025f, dy = 0.028f)
                close()
                // m -11 -0.899
                moveToRelative(dx = -11.0f, dy = -0.899f)
                // c 0.237 0.178 0.601 0.45 0.601 0.87
                curveToRelative(
                    dx1 = 0.237f,
                    dy1 = 0.178f,
                    dx2 = 0.601f,
                    dy2 = 0.45f,
                    dx3 = 0.601f,
                    dy3 = 0.87f,
                )
                // c 0 0.23 -0.036 0.435 -0.137 0.636
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.23f,
                    dx2 = -0.036f,
                    dy2 = 0.435f,
                    dx3 = -0.137f,
                    dy3 = 0.636f,
                )
                // c -0.1 0.201 -0.256 0.38 -0.463 0.564
                curveToRelative(
                    dx1 = -0.1f,
                    dy1 = 0.201f,
                    dx2 = -0.256f,
                    dy2 = 0.38f,
                    dx3 = -0.463f,
                    dy3 = 0.564f,
                )
                // c -0.166 0.147 -0.48 0.177 -0.7 0.066
                curveToRelative(
                    dx1 = -0.166f,
                    dy1 = 0.147f,
                    dx2 = -0.48f,
                    dy2 = 0.177f,
                    dx3 = -0.7f,
                    dy3 = 0.066f,
                )
                // c -0.22 -0.11 -0.266 -0.32 -0.1 -0.466
                curveToRelative(
                    dx1 = -0.22f,
                    dy1 = -0.11f,
                    dx2 = -0.266f,
                    dy2 = -0.32f,
                    dx3 = -0.1f,
                    dy3 = -0.466f,
                )
                // c 0.164 -0.146 0.258 -0.261 0.315 -0.375
                curveToRelative(
                    dx1 = 0.164f,
                    dy1 = -0.146f,
                    dx2 = 0.258f,
                    dy2 = -0.261f,
                    dx3 = 0.315f,
                    dy3 = -0.375f,
                )
                // A 0.912 0.912 0 0 0 4 2.667
                arcTo(
                    horizontalEllipseRadius = 0.912f,
                    verticalEllipseRadius = 0.912f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 4.0f,
                    y1 = 2.667f,
                )
                // c 0 -0.148 -0.11 -0.247 -0.374 -0.446
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.148f,
                    dx2 = -0.11f,
                    dy2 = -0.247f,
                    dx3 = -0.374f,
                    dy3 = -0.446f,
                )
                // l -0.025 -0.018
                lineToRelative(dx = -0.025f, dy = -0.018f)
                // C 3.364 2.025 3 1.753 3 1.333
                curveTo(
                    x1 = 3.364f,
                    y1 = 2.025f,
                    x2 = 3.0f,
                    y2 = 1.753f,
                    x3 = 3.0f,
                    y3 = 1.333f,
                )
                // c 0 -0.23 0.036 -0.435 0.137 -0.636
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.23f,
                    dx2 = 0.036f,
                    dy2 = -0.435f,
                    dx3 = 0.137f,
                    dy3 = -0.636f,
                )
                // c 0.1 -0.201 0.256 -0.38 0.463 -0.564
                curveToRelative(
                    dx1 = 0.1f,
                    dy1 = -0.201f,
                    dx2 = 0.256f,
                    dy2 = -0.38f,
                    dx3 = 0.463f,
                    dy3 = -0.564f,
                )
                // c 0.166 -0.147 0.48 -0.177 0.7 -0.066
                curveToRelative(
                    dx1 = 0.166f,
                    dy1 = -0.147f,
                    dx2 = 0.48f,
                    dy2 = -0.177f,
                    dx3 = 0.7f,
                    dy3 = -0.066f,
                )
                // c 0.22 0.11 0.266 0.32 0.1 0.466
                curveToRelative(
                    dx1 = 0.22f,
                    dy1 = 0.11f,
                    dx2 = 0.266f,
                    dy2 = 0.32f,
                    dx3 = 0.1f,
                    dy3 = 0.466f,
                )
                // a 1.289 1.289 0 0 0 -0.315 0.375
                arcToRelative(
                    a = 1.289f,
                    b = 1.289f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.315f,
                    dy1 = 0.375f,
                )
                // a 0.912 0.912 0 0 0 -0.085 0.425
                arcToRelative(
                    a = 0.912f,
                    b = 0.912f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.085f,
                    dy1 = 0.425f,
                )
                // c 0 0.148 0.11 0.247 0.374 0.446
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.148f,
                    dx2 = 0.11f,
                    dy2 = 0.247f,
                    dx3 = 0.374f,
                    dy3 = 0.446f,
                )
                // l 0.025 0.018z
                lineToRelative(dx = 0.025f, dy = 0.018f)
                close()
                // M 2 4
                moveTo(x = 2.0f, y = 4.0f)
                // c 0 -0.629 -0.364 -1.038 -0.601 -1.304
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.629f,
                    dx2 = -0.364f,
                    dy2 = -1.038f,
                    dx3 = -0.601f,
                    dy3 = -1.304f,
                )
                // l -0.025 -0.028
                lineToRelative(dx = -0.025f, dy = -0.028f)
                // C 1.109 2.37 1 2.222 1 2
                curveTo(
                    x1 = 1.109f,
                    y1 = 2.37f,
                    x2 = 1.0f,
                    y2 = 2.222f,
                    x3 = 1.0f,
                    y3 = 2.0f,
                )
                // c 0 -0.273 0.029 -0.468 0.085 -0.638
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.273f,
                    dx2 = 0.029f,
                    dy2 = -0.468f,
                    dx3 = 0.085f,
                    dy3 = -0.638f,
                )
                // A 1.97 1.97 0 0 1 1.4 0.8
                arcTo(
                    horizontalEllipseRadius = 1.97f,
                    verticalEllipseRadius = 1.97f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 1.4f,
                    y1 = 0.8f,
                )
                // A 0.5 0.5 0 1 0 0.6 0.2
                arcTo(
                    horizontalEllipseRadius = 0.5f,
                    verticalEllipseRadius = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    x1 = 0.6f,
                    y1 = 0.2f,
                )
                // a 2.952 2.952 0 0 0 -0.463 0.846
                arcToRelative(
                    a = 2.952f,
                    b = 2.952f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.463f,
                    dy1 = 0.846f,
                )
                // C 0.037 1.347 0 1.656 0 2
                curveTo(
                    x1 = 0.037f,
                    y1 = 1.347f,
                    x2 = 0.0f,
                    y2 = 1.656f,
                    x3 = 0.0f,
                    y3 = 2.0f,
                )
                // c 0 0.629 0.364 1.038 0.601 1.304
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.629f,
                    dx2 = 0.364f,
                    dy2 = 1.038f,
                    dx3 = 0.601f,
                    dy3 = 1.304f,
                )
                // l 0.025 0.028
                lineToRelative(dx = 0.025f, dy = 0.028f)
                // C 0.891 3.63 1 3.778 1 4
                curveTo(
                    x1 = 0.891f,
                    y1 = 3.63f,
                    x2 = 1.0f,
                    y2 = 3.778f,
                    x3 = 1.0f,
                    y3 = 4.0f,
                )
                // c 0 0.273 -0.029 0.468 -0.085 0.638
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.273f,
                    dx2 = -0.029f,
                    dy2 = 0.468f,
                    dx3 = -0.085f,
                    dy3 = 0.638f,
                )
                // A 1.97 1.97 0 0 1 0.6 5.2
                arcTo(
                    horizontalEllipseRadius = 1.97f,
                    verticalEllipseRadius = 1.97f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 0.6f,
                    y1 = 5.2f,
                )
                // a 0.5 0.5 0 1 0 0.8 0.6
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.8f,
                    dy1 = 0.6f,
                )
                // c 0.207 -0.276 0.363 -0.544 0.463 -0.846
                curveToRelative(
                    dx1 = 0.207f,
                    dy1 = -0.276f,
                    dx2 = 0.363f,
                    dy2 = -0.544f,
                    dx3 = 0.463f,
                    dy3 = -0.846f,
                )
                // c 0.1 -0.301 0.137 -0.61 0.137 -0.954z
                curveToRelative(
                    dx1 = 0.1f,
                    dy1 = -0.301f,
                    dx2 = 0.137f,
                    dy2 = -0.61f,
                    dx3 = 0.137f,
                    dy3 = -0.954f,
                )
                close()
                // m 11 -1.333
                moveToRelative(dx = 11.0f, dy = -1.333f)
                // c 0 -0.42 -0.364 -0.692 -0.601 -0.87
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.42f,
                    dx2 = -0.364f,
                    dy2 = -0.692f,
                    dx3 = -0.601f,
                    dy3 = -0.87f,
                )
                // l -0.025 -0.018
                lineToRelative(dx = -0.025f, dy = -0.018f)
                // C 12.11 1.58 12 1.48 12 1.333
                curveTo(
                    x1 = 12.11f,
                    y1 = 1.58f,
                    x2 = 12.0f,
                    y2 = 1.48f,
                    x3 = 12.0f,
                    y3 = 1.333f,
                )
                // a 0.92 0.92 0 0 1 0.085 -0.425
                arcToRelative(
                    a = 0.92f,
                    b = 0.92f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.085f,
                    dy1 = -0.425f,
                )
                // c 0.057 -0.114 0.151 -0.229 0.315 -0.375
                curveToRelative(
                    dx1 = 0.057f,
                    dy1 = -0.114f,
                    dx2 = 0.151f,
                    dy2 = -0.229f,
                    dx3 = 0.315f,
                    dy3 = -0.375f,
                )
                // c 0.166 -0.147 0.12 -0.356 -0.1 -0.466
                curveToRelative(
                    dx1 = 0.166f,
                    dy1 = -0.147f,
                    dx2 = 0.12f,
                    dy2 = -0.356f,
                    dx3 = -0.1f,
                    dy3 = -0.466f,
                )
                // c -0.22 -0.11 -0.534 -0.08 -0.7 0.066
                curveToRelative(
                    dx1 = -0.22f,
                    dy1 = -0.11f,
                    dx2 = -0.534f,
                    dy2 = -0.08f,
                    dx3 = -0.7f,
                    dy3 = 0.066f,
                )
                // c -0.207 0.184 -0.363 0.363 -0.463 0.564
                curveToRelative(
                    dx1 = -0.207f,
                    dy1 = 0.184f,
                    dx2 = -0.363f,
                    dy2 = 0.363f,
                    dx3 = -0.463f,
                    dy3 = 0.564f,
                )
                // a 1.38 1.38 0 0 0 -0.137 0.636
                arcToRelative(
                    a = 1.38f,
                    b = 1.38f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.137f,
                    dy1 = 0.636f,
                )
                // c 0 0.42 0.364 0.692 0.601 0.87
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.42f,
                    dx2 = 0.364f,
                    dy2 = 0.692f,
                    dx3 = 0.601f,
                    dy3 = 0.87f,
                )
                // l 0.025 0.018
                lineToRelative(dx = 0.025f, dy = 0.018f)
                // c 0.264 0.199 0.374 0.298 0.374 0.446
                curveToRelative(
                    dx1 = 0.264f,
                    dy1 = 0.199f,
                    dx2 = 0.374f,
                    dy2 = 0.298f,
                    dx3 = 0.374f,
                    dy3 = 0.446f,
                )
                // a 0.912 0.912 0 0 1 -0.085 0.425
                arcToRelative(
                    a = 0.912f,
                    b = 0.912f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.085f,
                    dy1 = 0.425f,
                )
                // a 1.289 1.289 0 0 1 -0.315 0.375
                arcToRelative(
                    a = 1.289f,
                    b = 1.289f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.315f,
                    dy1 = 0.375f,
                )
                // c -0.166 0.147 -0.12 0.356 0.1 0.466
                curveToRelative(
                    dx1 = -0.166f,
                    dy1 = 0.147f,
                    dx2 = -0.12f,
                    dy2 = 0.356f,
                    dx3 = 0.1f,
                    dy3 = 0.466f,
                )
                // c 0.22 0.11 0.534 0.08 0.7 -0.066
                curveToRelative(
                    dx1 = 0.22f,
                    dy1 = 0.11f,
                    dx2 = 0.534f,
                    dy2 = 0.08f,
                    dx3 = 0.7f,
                    dy3 = -0.066f,
                )
                // c 0.207 -0.184 0.363 -0.363 0.463 -0.564
                curveToRelative(
                    dx1 = 0.207f,
                    dy1 = -0.184f,
                    dx2 = 0.363f,
                    dy2 = -0.363f,
                    dx3 = 0.463f,
                    dy3 = -0.564f,
                )
                // c 0.1 -0.201 0.137 -0.407 0.137 -0.636z
                curveToRelative(
                    dx1 = 0.1f,
                    dy1 = -0.201f,
                    dx2 = 0.137f,
                    dy2 = -0.407f,
                    dx3 = 0.137f,
                    dy3 = -0.636f,
                )
                close()
                // M 6.235 8.595
                moveTo(x = 6.235f, y = 8.595f)
                // A 0.454 0.454 0 0 0 6 8.533
                arcTo(
                    horizontalEllipseRadius = 0.454f,
                    verticalEllipseRadius = 0.454f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 6.0f,
                    y1 = 8.533f,
                )
                // a 0.466 0.466 0 0 0 -0.235 0.872
                arcToRelative(
                    a = 0.466f,
                    b = 0.466f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.235f,
                    dy1 = 0.872f,
                )
                // c 0.07 0.041 0.15 0.062 0.235 0.062
                curveToRelative(
                    dx1 = 0.07f,
                    dy1 = 0.041f,
                    dx2 = 0.15f,
                    dy2 = 0.062f,
                    dx3 = 0.235f,
                    dy3 = 0.062f,
                )
                // a 0.454 0.454 0 0 0 0.403 -0.23
                arcToRelative(
                    a = 0.454f,
                    b = 0.454f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.403f,
                    dy1 = -0.23f,
                )
                // A 0.444 0.444 0 0 0 6.468 9
                arcTo(
                    horizontalEllipseRadius = 0.444f,
                    verticalEllipseRadius = 0.444f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 6.468f,
                    y1 = 9.0f,
                )
                // a 0.467 0.467 0 0 0 -0.233 -0.406z
                arcToRelative(
                    a = 0.467f,
                    b = 0.467f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.233f,
                    dy1 = -0.406f,
                )
                close()
            }
            // M3.097 15.223 a.5 .5 0 0 0 .649 .538 L8 14.4 l4.254 1.361 a.5 .5 0 0 0 .649 -.538 L12 8 h2.481 c.52 0 .716 -.804 .275 -1.129 L8.275 2.093 a.452 .452 0 0 0 -.55 0 L1.245 6.87 C.802 7.196 .997 8 1.518 8 H4 l-.903 7.223Z m2.4 -7.09 A.975 .975 0 0 1 6 8 c.184 0 .352 .044 .503 .133 .151 .09 .272 .211 .362 .362 A.98 .98 0 0 1 7 9.001 1.006 1.006 0 0 1 6 10 a1.006 1.006 0 0 1 -1 -.999 .98 .98 0 0 1 .135 -.506 c.09 -.15 .21 -.272 .362 -.362Z M11 10 h-.725 a.874 .874 0 0 0 -.322 -.542 .871 .871 0 0 0 -.283 -.142 1.125 1.125 0 0 0 -.34 -.05 c-.215 0 -.407 .053 -.573 .16 a1.05 1.05 0 0 0 -.392 .464 1.74 1.74 0 0 0 -.142 .743 c0 .298 .047 .549 .142 .753 .095 .203 .226 .356 .392 .46 .166 .103 .357 .154 .572 .154 .118 0 .23 -.015 .333 -.046 a.921 .921 0 0 0 .281 -.138 .84 .84 0 0 0 .332 -.528 l.725 .003 a1.541 1.541 0 0 1 -1.035 1.24 c-.194 .068 -.41 .102 -.646 .102 -.35 0 -.66 -.08 -.935 -.238 a1.672 1.672 0 0 1 -.647 -.686 c-.158 -.299 -.237 -.658 -.237 -1.076 0 -.42 .08 -.778 .239 -1.076 .159 -.3 .375 -.528 .65 -.686 .273 -.159 .583 -.238 .93 -.238 .22 0 .426 .03 .616 .091 s.36 .15 .508 .268 c.15 .117 .271 .26 .367 .43 .097 .169 .16 .361 .19 .578Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 3.097 15.223
                moveTo(x = 3.097f, y = 15.223f)
                // a 0.5 0.5 0 0 0 0.649 0.538
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.649f,
                    dy1 = 0.538f,
                )
                // L 8 14.4
                lineTo(x = 8.0f, y = 14.4f)
                // l 4.254 1.361
                lineToRelative(dx = 4.254f, dy = 1.361f)
                // a 0.5 0.5 0 0 0 0.649 -0.538
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.649f,
                    dy1 = -0.538f,
                )
                // L 12 8
                lineTo(x = 12.0f, y = 8.0f)
                // h 2.481
                horizontalLineToRelative(dx = 2.481f)
                // c 0.52 0 0.716 -0.804 0.275 -1.129
                curveToRelative(
                    dx1 = 0.52f,
                    dy1 = 0.0f,
                    dx2 = 0.716f,
                    dy2 = -0.804f,
                    dx3 = 0.275f,
                    dy3 = -1.129f,
                )
                // L 8.275 2.093
                lineTo(x = 8.275f, y = 2.093f)
                // a 0.452 0.452 0 0 0 -0.55 0
                arcToRelative(
                    a = 0.452f,
                    b = 0.452f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.55f,
                    dy1 = 0.0f,
                )
                // L 1.245 6.87
                lineTo(x = 1.245f, y = 6.87f)
                // C 0.802 7.196 0.997 8 1.518 8
                curveTo(
                    x1 = 0.802f,
                    y1 = 7.196f,
                    x2 = 0.997f,
                    y2 = 8.0f,
                    x3 = 1.518f,
                    y3 = 8.0f,
                )
                // H 4
                horizontalLineTo(x = 4.0f)
                // l -0.903 7.223z
                lineToRelative(dx = -0.903f, dy = 7.223f)
                close()
                // m 2.4 -7.09
                moveToRelative(dx = 2.4f, dy = -7.09f)
                // A 0.975 0.975 0 0 1 6 8
                arcTo(
                    horizontalEllipseRadius = 0.975f,
                    verticalEllipseRadius = 0.975f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 6.0f,
                    y1 = 8.0f,
                )
                // c 0.184 0 0.352 0.044 0.503 0.133
                curveToRelative(
                    dx1 = 0.184f,
                    dy1 = 0.0f,
                    dx2 = 0.352f,
                    dy2 = 0.044f,
                    dx3 = 0.503f,
                    dy3 = 0.133f,
                )
                // c 0.151 0.09 0.272 0.211 0.362 0.362
                curveToRelative(
                    dx1 = 0.151f,
                    dy1 = 0.09f,
                    dx2 = 0.272f,
                    dy2 = 0.211f,
                    dx3 = 0.362f,
                    dy3 = 0.362f,
                )
                // A 0.98 0.98 0 0 1 7 9.001
                arcTo(
                    horizontalEllipseRadius = 0.98f,
                    verticalEllipseRadius = 0.98f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.0f,
                    y1 = 9.001f,
                )
                // A 1.006 1.006 0 0 1 6 10
                arcTo(
                    horizontalEllipseRadius = 1.006f,
                    verticalEllipseRadius = 1.006f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 6.0f,
                    y1 = 10.0f,
                )
                // a 1.006 1.006 0 0 1 -1 -0.999
                arcToRelative(
                    a = 1.006f,
                    b = 1.006f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = -0.999f,
                )
                // a 0.98 0.98 0 0 1 0.135 -0.506
                arcToRelative(
                    a = 0.98f,
                    b = 0.98f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.135f,
                    dy1 = -0.506f,
                )
                // c 0.09 -0.15 0.21 -0.272 0.362 -0.362z
                curveToRelative(
                    dx1 = 0.09f,
                    dy1 = -0.15f,
                    dx2 = 0.21f,
                    dy2 = -0.272f,
                    dx3 = 0.362f,
                    dy3 = -0.362f,
                )
                close()
                // M 11 10
                moveTo(x = 11.0f, y = 10.0f)
                // h -0.725
                horizontalLineToRelative(dx = -0.725f)
                // a 0.874 0.874 0 0 0 -0.322 -0.542
                arcToRelative(
                    a = 0.874f,
                    b = 0.874f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.322f,
                    dy1 = -0.542f,
                )
                // a 0.871 0.871 0 0 0 -0.283 -0.142
                arcToRelative(
                    a = 0.871f,
                    b = 0.871f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.283f,
                    dy1 = -0.142f,
                )
                // a 1.125 1.125 0 0 0 -0.34 -0.05
                arcToRelative(
                    a = 1.125f,
                    b = 1.125f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.34f,
                    dy1 = -0.05f,
                )
                // c -0.215 0 -0.407 0.053 -0.573 0.16
                curveToRelative(
                    dx1 = -0.215f,
                    dy1 = 0.0f,
                    dx2 = -0.407f,
                    dy2 = 0.053f,
                    dx3 = -0.573f,
                    dy3 = 0.16f,
                )
                // a 1.05 1.05 0 0 0 -0.392 0.464
                arcToRelative(
                    a = 1.05f,
                    b = 1.05f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.392f,
                    dy1 = 0.464f,
                )
                // a 1.74 1.74 0 0 0 -0.142 0.743
                arcToRelative(
                    a = 1.74f,
                    b = 1.74f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.142f,
                    dy1 = 0.743f,
                )
                // c 0 0.298 0.047 0.549 0.142 0.753
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.298f,
                    dx2 = 0.047f,
                    dy2 = 0.549f,
                    dx3 = 0.142f,
                    dy3 = 0.753f,
                )
                // c 0.095 0.203 0.226 0.356 0.392 0.46
                curveToRelative(
                    dx1 = 0.095f,
                    dy1 = 0.203f,
                    dx2 = 0.226f,
                    dy2 = 0.356f,
                    dx3 = 0.392f,
                    dy3 = 0.46f,
                )
                // c 0.166 0.103 0.357 0.154 0.572 0.154
                curveToRelative(
                    dx1 = 0.166f,
                    dy1 = 0.103f,
                    dx2 = 0.357f,
                    dy2 = 0.154f,
                    dx3 = 0.572f,
                    dy3 = 0.154f,
                )
                // c 0.118 0 0.23 -0.015 0.333 -0.046
                curveToRelative(
                    dx1 = 0.118f,
                    dy1 = 0.0f,
                    dx2 = 0.23f,
                    dy2 = -0.015f,
                    dx3 = 0.333f,
                    dy3 = -0.046f,
                )
                // a 0.921 0.921 0 0 0 0.281 -0.138
                arcToRelative(
                    a = 0.921f,
                    b = 0.921f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.281f,
                    dy1 = -0.138f,
                )
                // a 0.84 0.84 0 0 0 0.332 -0.528
                arcToRelative(
                    a = 0.84f,
                    b = 0.84f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.332f,
                    dy1 = -0.528f,
                )
                // l 0.725 0.003
                lineToRelative(dx = 0.725f, dy = 0.003f)
                // a 1.541 1.541 0 0 1 -1.035 1.24
                arcToRelative(
                    a = 1.541f,
                    b = 1.541f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.035f,
                    dy1 = 1.24f,
                )
                // c -0.194 0.068 -0.41 0.102 -0.646 0.102
                curveToRelative(
                    dx1 = -0.194f,
                    dy1 = 0.068f,
                    dx2 = -0.41f,
                    dy2 = 0.102f,
                    dx3 = -0.646f,
                    dy3 = 0.102f,
                )
                // c -0.35 0 -0.66 -0.08 -0.935 -0.238
                curveToRelative(
                    dx1 = -0.35f,
                    dy1 = 0.0f,
                    dx2 = -0.66f,
                    dy2 = -0.08f,
                    dx3 = -0.935f,
                    dy3 = -0.238f,
                )
                // a 1.672 1.672 0 0 1 -0.647 -0.686
                arcToRelative(
                    a = 1.672f,
                    b = 1.672f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.647f,
                    dy1 = -0.686f,
                )
                // c -0.158 -0.299 -0.237 -0.658 -0.237 -1.076
                curveToRelative(
                    dx1 = -0.158f,
                    dy1 = -0.299f,
                    dx2 = -0.237f,
                    dy2 = -0.658f,
                    dx3 = -0.237f,
                    dy3 = -1.076f,
                )
                // c 0 -0.42 0.08 -0.778 0.239 -1.076
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.42f,
                    dx2 = 0.08f,
                    dy2 = -0.778f,
                    dx3 = 0.239f,
                    dy3 = -1.076f,
                )
                // c 0.159 -0.3 0.375 -0.528 0.65 -0.686
                curveToRelative(
                    dx1 = 0.159f,
                    dy1 = -0.3f,
                    dx2 = 0.375f,
                    dy2 = -0.528f,
                    dx3 = 0.65f,
                    dy3 = -0.686f,
                )
                // c 0.273 -0.159 0.583 -0.238 0.93 -0.238
                curveToRelative(
                    dx1 = 0.273f,
                    dy1 = -0.159f,
                    dx2 = 0.583f,
                    dy2 = -0.238f,
                    dx3 = 0.93f,
                    dy3 = -0.238f,
                )
                // c 0.22 0 0.426 0.03 0.616 0.091
                curveToRelative(
                    dx1 = 0.22f,
                    dy1 = 0.0f,
                    dx2 = 0.426f,
                    dy2 = 0.03f,
                    dx3 = 0.616f,
                    dy3 = 0.091f,
                )
                // s 0.36 0.15 0.508 0.268
                reflectiveCurveToRelative(
                    dx1 = 0.36f,
                    dy1 = 0.15f,
                    dx2 = 0.508f,
                    dy2 = 0.268f,
                )
                // c 0.15 0.117 0.271 0.26 0.367 0.43
                curveToRelative(
                    dx1 = 0.15f,
                    dy1 = 0.117f,
                    dx2 = 0.271f,
                    dy2 = 0.26f,
                    dx3 = 0.367f,
                    dy3 = 0.43f,
                )
                // c 0.097 0.169 0.16 0.361 0.19 0.578z
                curveToRelative(
                    dx1 = 0.097f,
                    dy1 = 0.169f,
                    dx2 = 0.16f,
                    dy2 = 0.361f,
                    dx3 = 0.19f,
                    dy3 = 0.578f,
                )
                close()
            }
        }.build().also { _ic1601 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1601: ImageVector? = null
