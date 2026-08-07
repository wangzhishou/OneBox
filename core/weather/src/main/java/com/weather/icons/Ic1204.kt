package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1204: ImageVector
    get() {
        val current = _ic1204
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1204",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M5.827 14.963 a3.43 3.43 0 0 0 .516 -.374 c.055 -.05 .108 -.098 .157 -.145 .05 .047 .102 .096 .158 .144 .16 .14 .356 .29 .573 .407 .214 .115 .48 .216 .77 .216 .29 0 .555 -.1 .768 -.216 a3.23 3.23 0 0 0 .574 -.406 c.055 -.05 .108 -.098 .157 -.145 .05 .047 .102 .096 .158 .144 .16 .14 .356 .29 .573 .407 .213 .115 .48 .216 .77 .216 .29 0 .555 -.1 .768 -.216 a3.281 3.281 0 0 0 .731 -.551 c.05 .047 .102 .096 .158 .144 .16 .14 .356 .29 .573 .407 .213 .115 .48 .216 .77 .216 .436 0 .843 -.11 1.189 -.34 .346 -.23 .604 -.564 .772 -.967 a.5 .5 0 0 0 -.923 -.385 1.118 1.118 0 0 1 -.404 .52 c-.16 .107 -.368 .172 -.635 .172 a.663 .663 0 0 1 -.294 -.096 c-.13 -.07 -.265 -.17 -.391 -.28 a3.87 3.87 0 0 1 -.419 -.43 l-.005 -.005 -.39 -.491 -.392 .49 v.001 l-.005 .006 -.011 .014 -.01 .011 a3.604 3.604 0 0 1 -.397 .403 c-.127 .11 -.262 .21 -.392 .281 a.663 .663 0 0 1 -.294 .096 .663 .663 0 0 1 -.294 -.096 c-.13 -.07 -.265 -.17 -.391 -.28 a3.87 3.87 0 0 1 -.419 -.43 l-.005 -.005 -.39 -.491 -.392 .49 v.001 l-.005 .006 a1.942 1.942 0 0 1 -.108 .124 3.868 3.868 0 0 1 -.31 .304 2.24 2.24 0 0 1 -.392 .281 .663 .663 0 0 1 -.294 .096 .663 .663 0 0 1 -.294 -.096 2.24 2.24 0 0 1 -.391 -.28 3.87 3.87 0 0 1 -.419 -.43 l-.005 -.005 -.39 -.491 -.392 .49 -.005 .007 a1.942 1.942 0 0 1 -.108 .124 3.868 3.868 0 0 1 -.351 .34 l.182 1.093Z m-.515 -3.092 c.168 -.04 .323 -.106 .457 -.179 a3.23 3.23 0 0 0 .574 -.406 l.157 -.144 c.05 .046 .102 .095 .158 .144 .16 .14 .356 .29 .573 .406 .214 .115 .48 .217 .77 .217 .29 0 .555 -.102 .768 -.217 a3.23 3.23 0 0 0 .574 -.406 l.157 -.144 c.05 .046 .102 .095 .158 .144 .16 .14 .356 .29 .573 .406 .213 .115 .48 .217 .77 .217 .29 0 .555 -.102 .768 -.217 .217 -.117 .413 -.267 .574 -.406 .056 -.05 .108 -.098 .157 -.144 .05 .046 .102 .095 .158 .144 .16 .14 .356 .29 .573 .406 .213 .115 .48 .217 .77 .217 .436 0 .843 -.11 1.189 -.34 a2.12 2.12 0 0 0 .772 -.968 .5 .5 0 1 0 -.923 -.385 1.118 1.118 0 0 1 -.404 .52 c-.16 .107 -.368 .173 -.635 .173 a.664 .664 0 0 1 -.294 -.097 c-.13 -.07 -.265 -.17 -.391 -.28 a3.87 3.87 0 0 1 -.419 -.43 l-.005 -.005 -.39 -.491 -.392 .49 v.001 l-.005 .006 a1.975 1.975 0 0 1 -.108 .125 3.87 3.87 0 0 1 -.31 .304 2.26 2.26 0 0 1 -.392 .28 .664 .664 0 0 1 -.294 .097 .664 .664 0 0 1 -.294 -.097 c-.13 -.07 -.265 -.17 -.391 -.28 a3.87 3.87 0 0 1 -.419 -.43 l-.005 -.005 -.39 -.491 -.392 .49 v.001 l-.005 .006 a2.643 2.643 0 0 1 -.108 .125 3.867 3.867 0 0 1 -.31 .304 2.24 2.24 0 0 1 -.392 .28 .664 .664 0 0 1 -.294 .097 .664 .664 0 0 1 -.294 -.097 2.24 2.24 0 0 1 -.391 -.28 3.87 3.87 0 0 1 -.419 -.43 l-.005 -.005 -.39 -.491 -.392 .49 -.005 .007 a2.643 2.643 0 0 1 -.108 .125 3.867 3.867 0 0 1 -.31 .304 2.24 2.24 0 0 1 -.392 .28 1.04 1.04 0 0 1 -.147 .067 l.165 .992Z m-.546 -3.276 a1.638 1.638 0 0 0 1.003 -.205 c.217 -.118 .413 -.268 .574 -.407 .055 -.049 .108 -.097 .157 -.144 .05 .047 .102 .095 .158 .144 .16 .14 .356 .29 .573 .406 .214 .116 .48 .217 .77 .217 .29 0 .555 -.101 .768 -.217 .217 -.117 .413 -.267 .574 -.406 .055 -.049 .108 -.097 .157 -.144 .05 .047 .102 .095 .158 .144 .16 .14 .356 .29 .573 .406 .213 .116 .48 .217 .77 .217 .29 0 .555 -.101 .768 -.217 .217 -.117 .413 -.267 .574 -.406 .056 -.049 .108 -.097 .157 -.144 .05 .047 .102 .095 .158 .144 .16 .14 .356 .29 .573 .406 .213 .116 .48 .217 .77 .217 .436 0 .843 -.11 1.189 -.34 a2.12 2.12 0 0 0 .772 -.968 .5 .5 0 1 0 -.923 -.385 1.118 1.118 0 0 1 -.404 .52 c-.16 .107 -.368 .173 -.635 .173 a.664 .664 0 0 1 -.294 -.096 2.24 2.24 0 0 1 -.391 -.281 3.878 3.878 0 0 1 -.419 -.429 l-.005 -.005 v-.001 l-.39 -.491 -.392 .49 v.002 l-.005 .005 a3.684 3.684 0 0 1 -.419 .429 2.24 2.24 0 0 1 -.391 .28 .664 .664 0 0 1 -.294 .097 .664 .664 0 0 1 -.294 -.096 2.24 2.24 0 0 1 -.391 -.281 3.878 3.878 0 0 1 -.419 -.429 l-.005 -.005 v-.001 l-.39 -.491 -.392 .49 v.002 l-.005 .005 a2.333 2.333 0 0 1 -.108 .125 3.876 3.876 0 0 1 -.31 .304 2.24 2.24 0 0 1 -.392 .28 .664 .664 0 0 1 -.294 .097 .664 .664 0 0 1 -.294 -.096 2.24 2.24 0 0 1 -.391 -.281 3.879 3.879 0 0 1 -.419 -.429 l-.005 -.005 v-.001 l-.39 -.491 -.392 .491 -.005 .006 a2.333 2.333 0 0 1 -.108 .125 3.876 3.876 0 0 1 -.31 .304 2.24 2.24 0 0 1 -.392 .28 .664 .664 0 0 1 -.294 .097 1.3 1.3 0 0 1 -.41 -.062 l.176 1.05Z M4.193 5.16 c.25 .096 .523 .143 .807 .143 .29 0 .556 -.101 .77 -.216 a3.368 3.368 0 0 0 .73 -.551 c.05 .047 .102 .095 .158 .144 .16 .14 .356 .29 .573 .407 .214 .115 .48 .216 .77 .216 .29 0 .555 -.101 .768 -.216 .217 -.118 .413 -.268 .574 -.407 .055 -.049 .108 -.097 .157 -.144 .05 .047 .102 .095 .158 .144 .16 .14 .356 .29 .573 .407 .213 .115 .48 .216 .77 .216 .29 0 .555 -.101 .768 -.216 .217 -.118 .413 -.268 .574 -.407 .056 -.049 .108 -.097 .157 -.144 .05 .047 .102 .095 .158 .144 .16 .14 .356 .29 .573 .407 .213 .115 .48 .216 .77 .216 .436 0 .843 -.11 1.189 -.34 a2.12 2.12 0 0 0 .772 -.968 .5 .5 0 1 0 -.923 -.384 1.118 1.118 0 0 1 -.404 .52 c-.16 .106 -.368 .172 -.635 .172 a.664 .664 0 0 1 -.294 -.096 2.24 2.24 0 0 1 -.391 -.281 3.878 3.878 0 0 1 -.419 -.429 l-.005 -.005 V3.49 L12.5 3 l-.392 .49 v.002 l-.005 .005 a3.67 3.67 0 0 1 -.419 .429 2.24 2.24 0 0 1 -.391 .28 .664 .664 0 0 1 -.294 .097 .664 .664 0 0 1 -.294 -.096 2.24 2.24 0 0 1 -.391 -.281 3.878 3.878 0 0 1 -.419 -.429 l-.005 -.005 V3.49 L9.5 3 l-.392 .49 v.002 l-.005 .005 a2.22 2.22 0 0 1 -.108 .125 3.876 3.876 0 0 1 -.31 .304 2.24 2.24 0 0 1 -.392 .28 .664 .664 0 0 1 -.294 .097 .664 .664 0 0 1 -.294 -.096 2.24 2.24 0 0 1 -.391 -.281 3.879 3.879 0 0 1 -.419 -.429 l-.005 -.005 V3.49 L6.5 3 l-.392 .491 -.005 .006 a2.22 2.22 0 0 1 -.108 .125 3.876 3.876 0 0 1 -.31 .304 2.24 2.24 0 0 1 -.392 .28 .664 .664 0 0 1 -.294 .097 c-.267 0 -.475 -.066 -.635 -.172 -.16 -.107 -.3 -.274 -.403 -.52 a.504 .504 0 0 0 -.04 -.076 l.271 1.625Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 5.827 14.963
                moveTo(x = 5.827f, y = 14.963f)
                // a 3.43 3.43 0 0 0 0.516 -0.374
                arcToRelative(
                    a = 3.43f,
                    b = 3.43f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.516f,
                    dy1 = -0.374f,
                )
                // c 0.055 -0.05 0.108 -0.098 0.157 -0.145
                curveToRelative(
                    dx1 = 0.055f,
                    dy1 = -0.05f,
                    dx2 = 0.108f,
                    dy2 = -0.098f,
                    dx3 = 0.157f,
                    dy3 = -0.145f,
                )
                // c 0.05 0.047 0.102 0.096 0.158 0.144
                curveToRelative(
                    dx1 = 0.05f,
                    dy1 = 0.047f,
                    dx2 = 0.102f,
                    dy2 = 0.096f,
                    dx3 = 0.158f,
                    dy3 = 0.144f,
                )
                // c 0.16 0.14 0.356 0.29 0.573 0.407
                curveToRelative(
                    dx1 = 0.16f,
                    dy1 = 0.14f,
                    dx2 = 0.356f,
                    dy2 = 0.29f,
                    dx3 = 0.573f,
                    dy3 = 0.407f,
                )
                // c 0.214 0.115 0.48 0.216 0.77 0.216
                curveToRelative(
                    dx1 = 0.214f,
                    dy1 = 0.115f,
                    dx2 = 0.48f,
                    dy2 = 0.216f,
                    dx3 = 0.77f,
                    dy3 = 0.216f,
                )
                // c 0.29 0 0.555 -0.1 0.768 -0.216
                curveToRelative(
                    dx1 = 0.29f,
                    dy1 = 0.0f,
                    dx2 = 0.555f,
                    dy2 = -0.1f,
                    dx3 = 0.768f,
                    dy3 = -0.216f,
                )
                // a 3.23 3.23 0 0 0 0.574 -0.406
                arcToRelative(
                    a = 3.23f,
                    b = 3.23f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.574f,
                    dy1 = -0.406f,
                )
                // c 0.055 -0.05 0.108 -0.098 0.157 -0.145
                curveToRelative(
                    dx1 = 0.055f,
                    dy1 = -0.05f,
                    dx2 = 0.108f,
                    dy2 = -0.098f,
                    dx3 = 0.157f,
                    dy3 = -0.145f,
                )
                // c 0.05 0.047 0.102 0.096 0.158 0.144
                curveToRelative(
                    dx1 = 0.05f,
                    dy1 = 0.047f,
                    dx2 = 0.102f,
                    dy2 = 0.096f,
                    dx3 = 0.158f,
                    dy3 = 0.144f,
                )
                // c 0.16 0.14 0.356 0.29 0.573 0.407
                curveToRelative(
                    dx1 = 0.16f,
                    dy1 = 0.14f,
                    dx2 = 0.356f,
                    dy2 = 0.29f,
                    dx3 = 0.573f,
                    dy3 = 0.407f,
                )
                // c 0.213 0.115 0.48 0.216 0.77 0.216
                curveToRelative(
                    dx1 = 0.213f,
                    dy1 = 0.115f,
                    dx2 = 0.48f,
                    dy2 = 0.216f,
                    dx3 = 0.77f,
                    dy3 = 0.216f,
                )
                // c 0.29 0 0.555 -0.1 0.768 -0.216
                curveToRelative(
                    dx1 = 0.29f,
                    dy1 = 0.0f,
                    dx2 = 0.555f,
                    dy2 = -0.1f,
                    dx3 = 0.768f,
                    dy3 = -0.216f,
                )
                // a 3.281 3.281 0 0 0 0.731 -0.551
                arcToRelative(
                    a = 3.281f,
                    b = 3.281f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.731f,
                    dy1 = -0.551f,
                )
                // c 0.05 0.047 0.102 0.096 0.158 0.144
                curveToRelative(
                    dx1 = 0.05f,
                    dy1 = 0.047f,
                    dx2 = 0.102f,
                    dy2 = 0.096f,
                    dx3 = 0.158f,
                    dy3 = 0.144f,
                )
                // c 0.16 0.14 0.356 0.29 0.573 0.407
                curveToRelative(
                    dx1 = 0.16f,
                    dy1 = 0.14f,
                    dx2 = 0.356f,
                    dy2 = 0.29f,
                    dx3 = 0.573f,
                    dy3 = 0.407f,
                )
                // c 0.213 0.115 0.48 0.216 0.77 0.216
                curveToRelative(
                    dx1 = 0.213f,
                    dy1 = 0.115f,
                    dx2 = 0.48f,
                    dy2 = 0.216f,
                    dx3 = 0.77f,
                    dy3 = 0.216f,
                )
                // c 0.436 0 0.843 -0.11 1.189 -0.34
                curveToRelative(
                    dx1 = 0.436f,
                    dy1 = 0.0f,
                    dx2 = 0.843f,
                    dy2 = -0.11f,
                    dx3 = 1.189f,
                    dy3 = -0.34f,
                )
                // c 0.346 -0.23 0.604 -0.564 0.772 -0.967
                curveToRelative(
                    dx1 = 0.346f,
                    dy1 = -0.23f,
                    dx2 = 0.604f,
                    dy2 = -0.564f,
                    dx3 = 0.772f,
                    dy3 = -0.967f,
                )
                // a 0.5 0.5 0 0 0 -0.923 -0.385
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.923f,
                    dy1 = -0.385f,
                )
                // a 1.118 1.118 0 0 1 -0.404 0.52
                arcToRelative(
                    a = 1.118f,
                    b = 1.118f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.404f,
                    dy1 = 0.52f,
                )
                // c -0.16 0.107 -0.368 0.172 -0.635 0.172
                curveToRelative(
                    dx1 = -0.16f,
                    dy1 = 0.107f,
                    dx2 = -0.368f,
                    dy2 = 0.172f,
                    dx3 = -0.635f,
                    dy3 = 0.172f,
                )
                // a 0.663 0.663 0 0 1 -0.294 -0.096
                arcToRelative(
                    a = 0.663f,
                    b = 0.663f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.294f,
                    dy1 = -0.096f,
                )
                // c -0.13 -0.07 -0.265 -0.17 -0.391 -0.28
                curveToRelative(
                    dx1 = -0.13f,
                    dy1 = -0.07f,
                    dx2 = -0.265f,
                    dy2 = -0.17f,
                    dx3 = -0.391f,
                    dy3 = -0.28f,
                )
                // a 3.87 3.87 0 0 1 -0.419 -0.43
                arcToRelative(
                    a = 3.87f,
                    b = 3.87f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.419f,
                    dy1 = -0.43f,
                )
                // l -0.005 -0.005
                lineToRelative(dx = -0.005f, dy = -0.005f)
                // l -0.39 -0.491
                lineToRelative(dx = -0.39f, dy = -0.491f)
                // l -0.392 0.49
                lineToRelative(dx = -0.392f, dy = 0.49f)
                // v 0.001
                verticalLineToRelative(dy = 0.001f)
                // l -0.005 0.006
                lineToRelative(dx = -0.005f, dy = 0.006f)
                // l -0.011 0.014
                lineToRelative(dx = -0.011f, dy = 0.014f)
                // l -0.01 0.011
                lineToRelative(dx = -0.01f, dy = 0.011f)
                // a 3.604 3.604 0 0 1 -0.397 0.403
                arcToRelative(
                    a = 3.604f,
                    b = 3.604f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.397f,
                    dy1 = 0.403f,
                )
                // c -0.127 0.11 -0.262 0.21 -0.392 0.281
                curveToRelative(
                    dx1 = -0.127f,
                    dy1 = 0.11f,
                    dx2 = -0.262f,
                    dy2 = 0.21f,
                    dx3 = -0.392f,
                    dy3 = 0.281f,
                )
                // a 0.663 0.663 0 0 1 -0.294 0.096
                arcToRelative(
                    a = 0.663f,
                    b = 0.663f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.294f,
                    dy1 = 0.096f,
                )
                // a 0.663 0.663 0 0 1 -0.294 -0.096
                arcToRelative(
                    a = 0.663f,
                    b = 0.663f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.294f,
                    dy1 = -0.096f,
                )
                // c -0.13 -0.07 -0.265 -0.17 -0.391 -0.28
                curveToRelative(
                    dx1 = -0.13f,
                    dy1 = -0.07f,
                    dx2 = -0.265f,
                    dy2 = -0.17f,
                    dx3 = -0.391f,
                    dy3 = -0.28f,
                )
                // a 3.87 3.87 0 0 1 -0.419 -0.43
                arcToRelative(
                    a = 3.87f,
                    b = 3.87f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.419f,
                    dy1 = -0.43f,
                )
                // l -0.005 -0.005
                lineToRelative(dx = -0.005f, dy = -0.005f)
                // l -0.39 -0.491
                lineToRelative(dx = -0.39f, dy = -0.491f)
                // l -0.392 0.49
                lineToRelative(dx = -0.392f, dy = 0.49f)
                // v 0.001
                verticalLineToRelative(dy = 0.001f)
                // l -0.005 0.006
                lineToRelative(dx = -0.005f, dy = 0.006f)
                // a 1.942 1.942 0 0 1 -0.108 0.124
                arcToRelative(
                    a = 1.942f,
                    b = 1.942f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.108f,
                    dy1 = 0.124f,
                )
                // a 3.868 3.868 0 0 1 -0.31 0.304
                arcToRelative(
                    a = 3.868f,
                    b = 3.868f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.31f,
                    dy1 = 0.304f,
                )
                // a 2.24 2.24 0 0 1 -0.392 0.281
                arcToRelative(
                    a = 2.24f,
                    b = 2.24f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.392f,
                    dy1 = 0.281f,
                )
                // a 0.663 0.663 0 0 1 -0.294 0.096
                arcToRelative(
                    a = 0.663f,
                    b = 0.663f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.294f,
                    dy1 = 0.096f,
                )
                // a 0.663 0.663 0 0 1 -0.294 -0.096
                arcToRelative(
                    a = 0.663f,
                    b = 0.663f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.294f,
                    dy1 = -0.096f,
                )
                // a 2.24 2.24 0 0 1 -0.391 -0.28
                arcToRelative(
                    a = 2.24f,
                    b = 2.24f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.391f,
                    dy1 = -0.28f,
                )
                // a 3.87 3.87 0 0 1 -0.419 -0.43
                arcToRelative(
                    a = 3.87f,
                    b = 3.87f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.419f,
                    dy1 = -0.43f,
                )
                // l -0.005 -0.005
                lineToRelative(dx = -0.005f, dy = -0.005f)
                // l -0.39 -0.491
                lineToRelative(dx = -0.39f, dy = -0.491f)
                // l -0.392 0.49
                lineToRelative(dx = -0.392f, dy = 0.49f)
                // l -0.005 0.007
                lineToRelative(dx = -0.005f, dy = 0.007f)
                // a 1.942 1.942 0 0 1 -0.108 0.124
                arcToRelative(
                    a = 1.942f,
                    b = 1.942f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.108f,
                    dy1 = 0.124f,
                )
                // a 3.868 3.868 0 0 1 -0.351 0.34
                arcToRelative(
                    a = 3.868f,
                    b = 3.868f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.351f,
                    dy1 = 0.34f,
                )
                // l 0.182 1.093z
                lineToRelative(dx = 0.182f, dy = 1.093f)
                close()
                // m -0.515 -3.092
                moveToRelative(dx = -0.515f, dy = -3.092f)
                // c 0.168 -0.04 0.323 -0.106 0.457 -0.179
                curveToRelative(
                    dx1 = 0.168f,
                    dy1 = -0.04f,
                    dx2 = 0.323f,
                    dy2 = -0.106f,
                    dx3 = 0.457f,
                    dy3 = -0.179f,
                )
                // a 3.23 3.23 0 0 0 0.574 -0.406
                arcToRelative(
                    a = 3.23f,
                    b = 3.23f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.574f,
                    dy1 = -0.406f,
                )
                // l 0.157 -0.144
                lineToRelative(dx = 0.157f, dy = -0.144f)
                // c 0.05 0.046 0.102 0.095 0.158 0.144
                curveToRelative(
                    dx1 = 0.05f,
                    dy1 = 0.046f,
                    dx2 = 0.102f,
                    dy2 = 0.095f,
                    dx3 = 0.158f,
                    dy3 = 0.144f,
                )
                // c 0.16 0.14 0.356 0.29 0.573 0.406
                curveToRelative(
                    dx1 = 0.16f,
                    dy1 = 0.14f,
                    dx2 = 0.356f,
                    dy2 = 0.29f,
                    dx3 = 0.573f,
                    dy3 = 0.406f,
                )
                // c 0.214 0.115 0.48 0.217 0.77 0.217
                curveToRelative(
                    dx1 = 0.214f,
                    dy1 = 0.115f,
                    dx2 = 0.48f,
                    dy2 = 0.217f,
                    dx3 = 0.77f,
                    dy3 = 0.217f,
                )
                // c 0.29 0 0.555 -0.102 0.768 -0.217
                curveToRelative(
                    dx1 = 0.29f,
                    dy1 = 0.0f,
                    dx2 = 0.555f,
                    dy2 = -0.102f,
                    dx3 = 0.768f,
                    dy3 = -0.217f,
                )
                // a 3.23 3.23 0 0 0 0.574 -0.406
                arcToRelative(
                    a = 3.23f,
                    b = 3.23f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.574f,
                    dy1 = -0.406f,
                )
                // l 0.157 -0.144
                lineToRelative(dx = 0.157f, dy = -0.144f)
                // c 0.05 0.046 0.102 0.095 0.158 0.144
                curveToRelative(
                    dx1 = 0.05f,
                    dy1 = 0.046f,
                    dx2 = 0.102f,
                    dy2 = 0.095f,
                    dx3 = 0.158f,
                    dy3 = 0.144f,
                )
                // c 0.16 0.14 0.356 0.29 0.573 0.406
                curveToRelative(
                    dx1 = 0.16f,
                    dy1 = 0.14f,
                    dx2 = 0.356f,
                    dy2 = 0.29f,
                    dx3 = 0.573f,
                    dy3 = 0.406f,
                )
                // c 0.213 0.115 0.48 0.217 0.77 0.217
                curveToRelative(
                    dx1 = 0.213f,
                    dy1 = 0.115f,
                    dx2 = 0.48f,
                    dy2 = 0.217f,
                    dx3 = 0.77f,
                    dy3 = 0.217f,
                )
                // c 0.29 0 0.555 -0.102 0.768 -0.217
                curveToRelative(
                    dx1 = 0.29f,
                    dy1 = 0.0f,
                    dx2 = 0.555f,
                    dy2 = -0.102f,
                    dx3 = 0.768f,
                    dy3 = -0.217f,
                )
                // c 0.217 -0.117 0.413 -0.267 0.574 -0.406
                curveToRelative(
                    dx1 = 0.217f,
                    dy1 = -0.117f,
                    dx2 = 0.413f,
                    dy2 = -0.267f,
                    dx3 = 0.574f,
                    dy3 = -0.406f,
                )
                // c 0.056 -0.05 0.108 -0.098 0.157 -0.144
                curveToRelative(
                    dx1 = 0.056f,
                    dy1 = -0.05f,
                    dx2 = 0.108f,
                    dy2 = -0.098f,
                    dx3 = 0.157f,
                    dy3 = -0.144f,
                )
                // c 0.05 0.046 0.102 0.095 0.158 0.144
                curveToRelative(
                    dx1 = 0.05f,
                    dy1 = 0.046f,
                    dx2 = 0.102f,
                    dy2 = 0.095f,
                    dx3 = 0.158f,
                    dy3 = 0.144f,
                )
                // c 0.16 0.14 0.356 0.29 0.573 0.406
                curveToRelative(
                    dx1 = 0.16f,
                    dy1 = 0.14f,
                    dx2 = 0.356f,
                    dy2 = 0.29f,
                    dx3 = 0.573f,
                    dy3 = 0.406f,
                )
                // c 0.213 0.115 0.48 0.217 0.77 0.217
                curveToRelative(
                    dx1 = 0.213f,
                    dy1 = 0.115f,
                    dx2 = 0.48f,
                    dy2 = 0.217f,
                    dx3 = 0.77f,
                    dy3 = 0.217f,
                )
                // c 0.436 0 0.843 -0.11 1.189 -0.34
                curveToRelative(
                    dx1 = 0.436f,
                    dy1 = 0.0f,
                    dx2 = 0.843f,
                    dy2 = -0.11f,
                    dx3 = 1.189f,
                    dy3 = -0.34f,
                )
                // a 2.12 2.12 0 0 0 0.772 -0.968
                arcToRelative(
                    a = 2.12f,
                    b = 2.12f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.772f,
                    dy1 = -0.968f,
                )
                // a 0.5 0.5 0 1 0 -0.923 -0.385
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.923f,
                    dy1 = -0.385f,
                )
                // a 1.118 1.118 0 0 1 -0.404 0.52
                arcToRelative(
                    a = 1.118f,
                    b = 1.118f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.404f,
                    dy1 = 0.52f,
                )
                // c -0.16 0.107 -0.368 0.173 -0.635 0.173
                curveToRelative(
                    dx1 = -0.16f,
                    dy1 = 0.107f,
                    dx2 = -0.368f,
                    dy2 = 0.173f,
                    dx3 = -0.635f,
                    dy3 = 0.173f,
                )
                // a 0.664 0.664 0 0 1 -0.294 -0.097
                arcToRelative(
                    a = 0.664f,
                    b = 0.664f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.294f,
                    dy1 = -0.097f,
                )
                // c -0.13 -0.07 -0.265 -0.17 -0.391 -0.28
                curveToRelative(
                    dx1 = -0.13f,
                    dy1 = -0.07f,
                    dx2 = -0.265f,
                    dy2 = -0.17f,
                    dx3 = -0.391f,
                    dy3 = -0.28f,
                )
                // a 3.87 3.87 0 0 1 -0.419 -0.43
                arcToRelative(
                    a = 3.87f,
                    b = 3.87f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.419f,
                    dy1 = -0.43f,
                )
                // l -0.005 -0.005
                lineToRelative(dx = -0.005f, dy = -0.005f)
                // l -0.39 -0.491
                lineToRelative(dx = -0.39f, dy = -0.491f)
                // l -0.392 0.49
                lineToRelative(dx = -0.392f, dy = 0.49f)
                // v 0.001
                verticalLineToRelative(dy = 0.001f)
                // l -0.005 0.006
                lineToRelative(dx = -0.005f, dy = 0.006f)
                // a 1.975 1.975 0 0 1 -0.108 0.125
                arcToRelative(
                    a = 1.975f,
                    b = 1.975f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.108f,
                    dy1 = 0.125f,
                )
                // a 3.87 3.87 0 0 1 -0.31 0.304
                arcToRelative(
                    a = 3.87f,
                    b = 3.87f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.31f,
                    dy1 = 0.304f,
                )
                // a 2.26 2.26 0 0 1 -0.392 0.28
                arcToRelative(
                    a = 2.26f,
                    b = 2.26f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.392f,
                    dy1 = 0.28f,
                )
                // a 0.664 0.664 0 0 1 -0.294 0.097
                arcToRelative(
                    a = 0.664f,
                    b = 0.664f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.294f,
                    dy1 = 0.097f,
                )
                // a 0.664 0.664 0 0 1 -0.294 -0.097
                arcToRelative(
                    a = 0.664f,
                    b = 0.664f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.294f,
                    dy1 = -0.097f,
                )
                // c -0.13 -0.07 -0.265 -0.17 -0.391 -0.28
                curveToRelative(
                    dx1 = -0.13f,
                    dy1 = -0.07f,
                    dx2 = -0.265f,
                    dy2 = -0.17f,
                    dx3 = -0.391f,
                    dy3 = -0.28f,
                )
                // a 3.87 3.87 0 0 1 -0.419 -0.43
                arcToRelative(
                    a = 3.87f,
                    b = 3.87f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.419f,
                    dy1 = -0.43f,
                )
                // l -0.005 -0.005
                lineToRelative(dx = -0.005f, dy = -0.005f)
                // l -0.39 -0.491
                lineToRelative(dx = -0.39f, dy = -0.491f)
                // l -0.392 0.49
                lineToRelative(dx = -0.392f, dy = 0.49f)
                // v 0.001
                verticalLineToRelative(dy = 0.001f)
                // l -0.005 0.006
                lineToRelative(dx = -0.005f, dy = 0.006f)
                // a 2.643 2.643 0 0 1 -0.108 0.125
                arcToRelative(
                    a = 2.643f,
                    b = 2.643f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.108f,
                    dy1 = 0.125f,
                )
                // a 3.867 3.867 0 0 1 -0.31 0.304
                arcToRelative(
                    a = 3.867f,
                    b = 3.867f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.31f,
                    dy1 = 0.304f,
                )
                // a 2.24 2.24 0 0 1 -0.392 0.28
                arcToRelative(
                    a = 2.24f,
                    b = 2.24f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.392f,
                    dy1 = 0.28f,
                )
                // a 0.664 0.664 0 0 1 -0.294 0.097
                arcToRelative(
                    a = 0.664f,
                    b = 0.664f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.294f,
                    dy1 = 0.097f,
                )
                // a 0.664 0.664 0 0 1 -0.294 -0.097
                arcToRelative(
                    a = 0.664f,
                    b = 0.664f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.294f,
                    dy1 = -0.097f,
                )
                // a 2.24 2.24 0 0 1 -0.391 -0.28
                arcToRelative(
                    a = 2.24f,
                    b = 2.24f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.391f,
                    dy1 = -0.28f,
                )
                // a 3.87 3.87 0 0 1 -0.419 -0.43
                arcToRelative(
                    a = 3.87f,
                    b = 3.87f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.419f,
                    dy1 = -0.43f,
                )
                // l -0.005 -0.005
                lineToRelative(dx = -0.005f, dy = -0.005f)
                // l -0.39 -0.491
                lineToRelative(dx = -0.39f, dy = -0.491f)
                // l -0.392 0.49
                lineToRelative(dx = -0.392f, dy = 0.49f)
                // l -0.005 0.007
                lineToRelative(dx = -0.005f, dy = 0.007f)
                // a 2.643 2.643 0 0 1 -0.108 0.125
                arcToRelative(
                    a = 2.643f,
                    b = 2.643f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.108f,
                    dy1 = 0.125f,
                )
                // a 3.867 3.867 0 0 1 -0.31 0.304
                arcToRelative(
                    a = 3.867f,
                    b = 3.867f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.31f,
                    dy1 = 0.304f,
                )
                // a 2.24 2.24 0 0 1 -0.392 0.28
                arcToRelative(
                    a = 2.24f,
                    b = 2.24f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.392f,
                    dy1 = 0.28f,
                )
                // a 1.04 1.04 0 0 1 -0.147 0.067
                arcToRelative(
                    a = 1.04f,
                    b = 1.04f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.147f,
                    dy1 = 0.067f,
                )
                // l 0.165 0.992z
                lineToRelative(dx = 0.165f, dy = 0.992f)
                close()
                // m -0.546 -3.276
                moveToRelative(dx = -0.546f, dy = -3.276f)
                // a 1.638 1.638 0 0 0 1.003 -0.205
                arcToRelative(
                    a = 1.638f,
                    b = 1.638f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.003f,
                    dy1 = -0.205f,
                )
                // c 0.217 -0.118 0.413 -0.268 0.574 -0.407
                curveToRelative(
                    dx1 = 0.217f,
                    dy1 = -0.118f,
                    dx2 = 0.413f,
                    dy2 = -0.268f,
                    dx3 = 0.574f,
                    dy3 = -0.407f,
                )
                // c 0.055 -0.049 0.108 -0.097 0.157 -0.144
                curveToRelative(
                    dx1 = 0.055f,
                    dy1 = -0.049f,
                    dx2 = 0.108f,
                    dy2 = -0.097f,
                    dx3 = 0.157f,
                    dy3 = -0.144f,
                )
                // c 0.05 0.047 0.102 0.095 0.158 0.144
                curveToRelative(
                    dx1 = 0.05f,
                    dy1 = 0.047f,
                    dx2 = 0.102f,
                    dy2 = 0.095f,
                    dx3 = 0.158f,
                    dy3 = 0.144f,
                )
                // c 0.16 0.14 0.356 0.29 0.573 0.406
                curveToRelative(
                    dx1 = 0.16f,
                    dy1 = 0.14f,
                    dx2 = 0.356f,
                    dy2 = 0.29f,
                    dx3 = 0.573f,
                    dy3 = 0.406f,
                )
                // c 0.214 0.116 0.48 0.217 0.77 0.217
                curveToRelative(
                    dx1 = 0.214f,
                    dy1 = 0.116f,
                    dx2 = 0.48f,
                    dy2 = 0.217f,
                    dx3 = 0.77f,
                    dy3 = 0.217f,
                )
                // c 0.29 0 0.555 -0.101 0.768 -0.217
                curveToRelative(
                    dx1 = 0.29f,
                    dy1 = 0.0f,
                    dx2 = 0.555f,
                    dy2 = -0.101f,
                    dx3 = 0.768f,
                    dy3 = -0.217f,
                )
                // c 0.217 -0.117 0.413 -0.267 0.574 -0.406
                curveToRelative(
                    dx1 = 0.217f,
                    dy1 = -0.117f,
                    dx2 = 0.413f,
                    dy2 = -0.267f,
                    dx3 = 0.574f,
                    dy3 = -0.406f,
                )
                // c 0.055 -0.049 0.108 -0.097 0.157 -0.144
                curveToRelative(
                    dx1 = 0.055f,
                    dy1 = -0.049f,
                    dx2 = 0.108f,
                    dy2 = -0.097f,
                    dx3 = 0.157f,
                    dy3 = -0.144f,
                )
                // c 0.05 0.047 0.102 0.095 0.158 0.144
                curveToRelative(
                    dx1 = 0.05f,
                    dy1 = 0.047f,
                    dx2 = 0.102f,
                    dy2 = 0.095f,
                    dx3 = 0.158f,
                    dy3 = 0.144f,
                )
                // c 0.16 0.14 0.356 0.29 0.573 0.406
                curveToRelative(
                    dx1 = 0.16f,
                    dy1 = 0.14f,
                    dx2 = 0.356f,
                    dy2 = 0.29f,
                    dx3 = 0.573f,
                    dy3 = 0.406f,
                )
                // c 0.213 0.116 0.48 0.217 0.77 0.217
                curveToRelative(
                    dx1 = 0.213f,
                    dy1 = 0.116f,
                    dx2 = 0.48f,
                    dy2 = 0.217f,
                    dx3 = 0.77f,
                    dy3 = 0.217f,
                )
                // c 0.29 0 0.555 -0.101 0.768 -0.217
                curveToRelative(
                    dx1 = 0.29f,
                    dy1 = 0.0f,
                    dx2 = 0.555f,
                    dy2 = -0.101f,
                    dx3 = 0.768f,
                    dy3 = -0.217f,
                )
                // c 0.217 -0.117 0.413 -0.267 0.574 -0.406
                curveToRelative(
                    dx1 = 0.217f,
                    dy1 = -0.117f,
                    dx2 = 0.413f,
                    dy2 = -0.267f,
                    dx3 = 0.574f,
                    dy3 = -0.406f,
                )
                // c 0.056 -0.049 0.108 -0.097 0.157 -0.144
                curveToRelative(
                    dx1 = 0.056f,
                    dy1 = -0.049f,
                    dx2 = 0.108f,
                    dy2 = -0.097f,
                    dx3 = 0.157f,
                    dy3 = -0.144f,
                )
                // c 0.05 0.047 0.102 0.095 0.158 0.144
                curveToRelative(
                    dx1 = 0.05f,
                    dy1 = 0.047f,
                    dx2 = 0.102f,
                    dy2 = 0.095f,
                    dx3 = 0.158f,
                    dy3 = 0.144f,
                )
                // c 0.16 0.14 0.356 0.29 0.573 0.406
                curveToRelative(
                    dx1 = 0.16f,
                    dy1 = 0.14f,
                    dx2 = 0.356f,
                    dy2 = 0.29f,
                    dx3 = 0.573f,
                    dy3 = 0.406f,
                )
                // c 0.213 0.116 0.48 0.217 0.77 0.217
                curveToRelative(
                    dx1 = 0.213f,
                    dy1 = 0.116f,
                    dx2 = 0.48f,
                    dy2 = 0.217f,
                    dx3 = 0.77f,
                    dy3 = 0.217f,
                )
                // c 0.436 0 0.843 -0.11 1.189 -0.34
                curveToRelative(
                    dx1 = 0.436f,
                    dy1 = 0.0f,
                    dx2 = 0.843f,
                    dy2 = -0.11f,
                    dx3 = 1.189f,
                    dy3 = -0.34f,
                )
                // a 2.12 2.12 0 0 0 0.772 -0.968
                arcToRelative(
                    a = 2.12f,
                    b = 2.12f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.772f,
                    dy1 = -0.968f,
                )
                // a 0.5 0.5 0 1 0 -0.923 -0.385
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.923f,
                    dy1 = -0.385f,
                )
                // a 1.118 1.118 0 0 1 -0.404 0.52
                arcToRelative(
                    a = 1.118f,
                    b = 1.118f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.404f,
                    dy1 = 0.52f,
                )
                // c -0.16 0.107 -0.368 0.173 -0.635 0.173
                curveToRelative(
                    dx1 = -0.16f,
                    dy1 = 0.107f,
                    dx2 = -0.368f,
                    dy2 = 0.173f,
                    dx3 = -0.635f,
                    dy3 = 0.173f,
                )
                // a 0.664 0.664 0 0 1 -0.294 -0.096
                arcToRelative(
                    a = 0.664f,
                    b = 0.664f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.294f,
                    dy1 = -0.096f,
                )
                // a 2.24 2.24 0 0 1 -0.391 -0.281
                arcToRelative(
                    a = 2.24f,
                    b = 2.24f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.391f,
                    dy1 = -0.281f,
                )
                // a 3.878 3.878 0 0 1 -0.419 -0.429
                arcToRelative(
                    a = 3.878f,
                    b = 3.878f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.419f,
                    dy1 = -0.429f,
                )
                // l -0.005 -0.005
                lineToRelative(dx = -0.005f, dy = -0.005f)
                // v -0.001
                verticalLineToRelative(dy = -0.001f)
                // l -0.39 -0.491
                lineToRelative(dx = -0.39f, dy = -0.491f)
                // l -0.392 0.49
                lineToRelative(dx = -0.392f, dy = 0.49f)
                // v 0.002
                verticalLineToRelative(dy = 0.002f)
                // l -0.005 0.005
                lineToRelative(dx = -0.005f, dy = 0.005f)
                // a 3.684 3.684 0 0 1 -0.419 0.429
                arcToRelative(
                    a = 3.684f,
                    b = 3.684f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.419f,
                    dy1 = 0.429f,
                )
                // a 2.24 2.24 0 0 1 -0.391 0.28
                arcToRelative(
                    a = 2.24f,
                    b = 2.24f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.391f,
                    dy1 = 0.28f,
                )
                // a 0.664 0.664 0 0 1 -0.294 0.097
                arcToRelative(
                    a = 0.664f,
                    b = 0.664f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.294f,
                    dy1 = 0.097f,
                )
                // a 0.664 0.664 0 0 1 -0.294 -0.096
                arcToRelative(
                    a = 0.664f,
                    b = 0.664f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.294f,
                    dy1 = -0.096f,
                )
                // a 2.24 2.24 0 0 1 -0.391 -0.281
                arcToRelative(
                    a = 2.24f,
                    b = 2.24f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.391f,
                    dy1 = -0.281f,
                )
                // a 3.878 3.878 0 0 1 -0.419 -0.429
                arcToRelative(
                    a = 3.878f,
                    b = 3.878f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.419f,
                    dy1 = -0.429f,
                )
                // l -0.005 -0.005
                lineToRelative(dx = -0.005f, dy = -0.005f)
                // v -0.001
                verticalLineToRelative(dy = -0.001f)
                // l -0.39 -0.491
                lineToRelative(dx = -0.39f, dy = -0.491f)
                // l -0.392 0.49
                lineToRelative(dx = -0.392f, dy = 0.49f)
                // v 0.002
                verticalLineToRelative(dy = 0.002f)
                // l -0.005 0.005
                lineToRelative(dx = -0.005f, dy = 0.005f)
                // a 2.333 2.333 0 0 1 -0.108 0.125
                arcToRelative(
                    a = 2.333f,
                    b = 2.333f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.108f,
                    dy1 = 0.125f,
                )
                // a 3.876 3.876 0 0 1 -0.31 0.304
                arcToRelative(
                    a = 3.876f,
                    b = 3.876f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.31f,
                    dy1 = 0.304f,
                )
                // a 2.24 2.24 0 0 1 -0.392 0.28
                arcToRelative(
                    a = 2.24f,
                    b = 2.24f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.392f,
                    dy1 = 0.28f,
                )
                // a 0.664 0.664 0 0 1 -0.294 0.097
                arcToRelative(
                    a = 0.664f,
                    b = 0.664f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.294f,
                    dy1 = 0.097f,
                )
                // a 0.664 0.664 0 0 1 -0.294 -0.096
                arcToRelative(
                    a = 0.664f,
                    b = 0.664f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.294f,
                    dy1 = -0.096f,
                )
                // a 2.24 2.24 0 0 1 -0.391 -0.281
                arcToRelative(
                    a = 2.24f,
                    b = 2.24f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.391f,
                    dy1 = -0.281f,
                )
                // a 3.879 3.879 0 0 1 -0.419 -0.429
                arcToRelative(
                    a = 3.879f,
                    b = 3.879f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.419f,
                    dy1 = -0.429f,
                )
                // l -0.005 -0.005
                lineToRelative(dx = -0.005f, dy = -0.005f)
                // v -0.001
                verticalLineToRelative(dy = -0.001f)
                // l -0.39 -0.491
                lineToRelative(dx = -0.39f, dy = -0.491f)
                // l -0.392 0.491
                lineToRelative(dx = -0.392f, dy = 0.491f)
                // l -0.005 0.006
                lineToRelative(dx = -0.005f, dy = 0.006f)
                // a 2.333 2.333 0 0 1 -0.108 0.125
                arcToRelative(
                    a = 2.333f,
                    b = 2.333f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.108f,
                    dy1 = 0.125f,
                )
                // a 3.876 3.876 0 0 1 -0.31 0.304
                arcToRelative(
                    a = 3.876f,
                    b = 3.876f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.31f,
                    dy1 = 0.304f,
                )
                // a 2.24 2.24 0 0 1 -0.392 0.28
                arcToRelative(
                    a = 2.24f,
                    b = 2.24f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.392f,
                    dy1 = 0.28f,
                )
                // a 0.664 0.664 0 0 1 -0.294 0.097
                arcToRelative(
                    a = 0.664f,
                    b = 0.664f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.294f,
                    dy1 = 0.097f,
                )
                // a 1.3 1.3 0 0 1 -0.41 -0.062
                arcToRelative(
                    a = 1.3f,
                    b = 1.3f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.41f,
                    dy1 = -0.062f,
                )
                // l 0.176 1.05z
                lineToRelative(dx = 0.176f, dy = 1.05f)
                close()
                // M 4.193 5.16
                moveTo(x = 4.193f, y = 5.16f)
                // c 0.25 0.096 0.523 0.143 0.807 0.143
                curveToRelative(
                    dx1 = 0.25f,
                    dy1 = 0.096f,
                    dx2 = 0.523f,
                    dy2 = 0.143f,
                    dx3 = 0.807f,
                    dy3 = 0.143f,
                )
                // c 0.29 0 0.556 -0.101 0.77 -0.216
                curveToRelative(
                    dx1 = 0.29f,
                    dy1 = 0.0f,
                    dx2 = 0.556f,
                    dy2 = -0.101f,
                    dx3 = 0.77f,
                    dy3 = -0.216f,
                )
                // a 3.368 3.368 0 0 0 0.73 -0.551
                arcToRelative(
                    a = 3.368f,
                    b = 3.368f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.73f,
                    dy1 = -0.551f,
                )
                // c 0.05 0.047 0.102 0.095 0.158 0.144
                curveToRelative(
                    dx1 = 0.05f,
                    dy1 = 0.047f,
                    dx2 = 0.102f,
                    dy2 = 0.095f,
                    dx3 = 0.158f,
                    dy3 = 0.144f,
                )
                // c 0.16 0.14 0.356 0.29 0.573 0.407
                curveToRelative(
                    dx1 = 0.16f,
                    dy1 = 0.14f,
                    dx2 = 0.356f,
                    dy2 = 0.29f,
                    dx3 = 0.573f,
                    dy3 = 0.407f,
                )
                // c 0.214 0.115 0.48 0.216 0.77 0.216
                curveToRelative(
                    dx1 = 0.214f,
                    dy1 = 0.115f,
                    dx2 = 0.48f,
                    dy2 = 0.216f,
                    dx3 = 0.77f,
                    dy3 = 0.216f,
                )
                // c 0.29 0 0.555 -0.101 0.768 -0.216
                curveToRelative(
                    dx1 = 0.29f,
                    dy1 = 0.0f,
                    dx2 = 0.555f,
                    dy2 = -0.101f,
                    dx3 = 0.768f,
                    dy3 = -0.216f,
                )
                // c 0.217 -0.118 0.413 -0.268 0.574 -0.407
                curveToRelative(
                    dx1 = 0.217f,
                    dy1 = -0.118f,
                    dx2 = 0.413f,
                    dy2 = -0.268f,
                    dx3 = 0.574f,
                    dy3 = -0.407f,
                )
                // c 0.055 -0.049 0.108 -0.097 0.157 -0.144
                curveToRelative(
                    dx1 = 0.055f,
                    dy1 = -0.049f,
                    dx2 = 0.108f,
                    dy2 = -0.097f,
                    dx3 = 0.157f,
                    dy3 = -0.144f,
                )
                // c 0.05 0.047 0.102 0.095 0.158 0.144
                curveToRelative(
                    dx1 = 0.05f,
                    dy1 = 0.047f,
                    dx2 = 0.102f,
                    dy2 = 0.095f,
                    dx3 = 0.158f,
                    dy3 = 0.144f,
                )
                // c 0.16 0.14 0.356 0.29 0.573 0.407
                curveToRelative(
                    dx1 = 0.16f,
                    dy1 = 0.14f,
                    dx2 = 0.356f,
                    dy2 = 0.29f,
                    dx3 = 0.573f,
                    dy3 = 0.407f,
                )
                // c 0.213 0.115 0.48 0.216 0.77 0.216
                curveToRelative(
                    dx1 = 0.213f,
                    dy1 = 0.115f,
                    dx2 = 0.48f,
                    dy2 = 0.216f,
                    dx3 = 0.77f,
                    dy3 = 0.216f,
                )
                // c 0.29 0 0.555 -0.101 0.768 -0.216
                curveToRelative(
                    dx1 = 0.29f,
                    dy1 = 0.0f,
                    dx2 = 0.555f,
                    dy2 = -0.101f,
                    dx3 = 0.768f,
                    dy3 = -0.216f,
                )
                // c 0.217 -0.118 0.413 -0.268 0.574 -0.407
                curveToRelative(
                    dx1 = 0.217f,
                    dy1 = -0.118f,
                    dx2 = 0.413f,
                    dy2 = -0.268f,
                    dx3 = 0.574f,
                    dy3 = -0.407f,
                )
                // c 0.056 -0.049 0.108 -0.097 0.157 -0.144
                curveToRelative(
                    dx1 = 0.056f,
                    dy1 = -0.049f,
                    dx2 = 0.108f,
                    dy2 = -0.097f,
                    dx3 = 0.157f,
                    dy3 = -0.144f,
                )
                // c 0.05 0.047 0.102 0.095 0.158 0.144
                curveToRelative(
                    dx1 = 0.05f,
                    dy1 = 0.047f,
                    dx2 = 0.102f,
                    dy2 = 0.095f,
                    dx3 = 0.158f,
                    dy3 = 0.144f,
                )
                // c 0.16 0.14 0.356 0.29 0.573 0.407
                curveToRelative(
                    dx1 = 0.16f,
                    dy1 = 0.14f,
                    dx2 = 0.356f,
                    dy2 = 0.29f,
                    dx3 = 0.573f,
                    dy3 = 0.407f,
                )
                // c 0.213 0.115 0.48 0.216 0.77 0.216
                curveToRelative(
                    dx1 = 0.213f,
                    dy1 = 0.115f,
                    dx2 = 0.48f,
                    dy2 = 0.216f,
                    dx3 = 0.77f,
                    dy3 = 0.216f,
                )
                // c 0.436 0 0.843 -0.11 1.189 -0.34
                curveToRelative(
                    dx1 = 0.436f,
                    dy1 = 0.0f,
                    dx2 = 0.843f,
                    dy2 = -0.11f,
                    dx3 = 1.189f,
                    dy3 = -0.34f,
                )
                // a 2.12 2.12 0 0 0 0.772 -0.968
                arcToRelative(
                    a = 2.12f,
                    b = 2.12f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.772f,
                    dy1 = -0.968f,
                )
                // a 0.5 0.5 0 1 0 -0.923 -0.384
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.923f,
                    dy1 = -0.384f,
                )
                // a 1.118 1.118 0 0 1 -0.404 0.52
                arcToRelative(
                    a = 1.118f,
                    b = 1.118f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.404f,
                    dy1 = 0.52f,
                )
                // c -0.16 0.106 -0.368 0.172 -0.635 0.172
                curveToRelative(
                    dx1 = -0.16f,
                    dy1 = 0.106f,
                    dx2 = -0.368f,
                    dy2 = 0.172f,
                    dx3 = -0.635f,
                    dy3 = 0.172f,
                )
                // a 0.664 0.664 0 0 1 -0.294 -0.096
                arcToRelative(
                    a = 0.664f,
                    b = 0.664f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.294f,
                    dy1 = -0.096f,
                )
                // a 2.24 2.24 0 0 1 -0.391 -0.281
                arcToRelative(
                    a = 2.24f,
                    b = 2.24f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.391f,
                    dy1 = -0.281f,
                )
                // a 3.878 3.878 0 0 1 -0.419 -0.429
                arcToRelative(
                    a = 3.878f,
                    b = 3.878f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.419f,
                    dy1 = -0.429f,
                )
                // l -0.005 -0.005
                lineToRelative(dx = -0.005f, dy = -0.005f)
                // V 3.49
                verticalLineTo(y = 3.49f)
                // L 12.5 3
                lineTo(x = 12.5f, y = 3.0f)
                // l -0.392 0.49
                lineToRelative(dx = -0.392f, dy = 0.49f)
                // v 0.002
                verticalLineToRelative(dy = 0.002f)
                // l -0.005 0.005
                lineToRelative(dx = -0.005f, dy = 0.005f)
                // a 3.67 3.67 0 0 1 -0.419 0.429
                arcToRelative(
                    a = 3.67f,
                    b = 3.67f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.419f,
                    dy1 = 0.429f,
                )
                // a 2.24 2.24 0 0 1 -0.391 0.28
                arcToRelative(
                    a = 2.24f,
                    b = 2.24f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.391f,
                    dy1 = 0.28f,
                )
                // a 0.664 0.664 0 0 1 -0.294 0.097
                arcToRelative(
                    a = 0.664f,
                    b = 0.664f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.294f,
                    dy1 = 0.097f,
                )
                // a 0.664 0.664 0 0 1 -0.294 -0.096
                arcToRelative(
                    a = 0.664f,
                    b = 0.664f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.294f,
                    dy1 = -0.096f,
                )
                // a 2.24 2.24 0 0 1 -0.391 -0.281
                arcToRelative(
                    a = 2.24f,
                    b = 2.24f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.391f,
                    dy1 = -0.281f,
                )
                // a 3.878 3.878 0 0 1 -0.419 -0.429
                arcToRelative(
                    a = 3.878f,
                    b = 3.878f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.419f,
                    dy1 = -0.429f,
                )
                // l -0.005 -0.005
                lineToRelative(dx = -0.005f, dy = -0.005f)
                // V 3.49
                verticalLineTo(y = 3.49f)
                // L 9.5 3
                lineTo(x = 9.5f, y = 3.0f)
                // l -0.392 0.49
                lineToRelative(dx = -0.392f, dy = 0.49f)
                // v 0.002
                verticalLineToRelative(dy = 0.002f)
                // l -0.005 0.005
                lineToRelative(dx = -0.005f, dy = 0.005f)
                // a 2.22 2.22 0 0 1 -0.108 0.125
                arcToRelative(
                    a = 2.22f,
                    b = 2.22f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.108f,
                    dy1 = 0.125f,
                )
                // a 3.876 3.876 0 0 1 -0.31 0.304
                arcToRelative(
                    a = 3.876f,
                    b = 3.876f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.31f,
                    dy1 = 0.304f,
                )
                // a 2.24 2.24 0 0 1 -0.392 0.28
                arcToRelative(
                    a = 2.24f,
                    b = 2.24f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.392f,
                    dy1 = 0.28f,
                )
                // a 0.664 0.664 0 0 1 -0.294 0.097
                arcToRelative(
                    a = 0.664f,
                    b = 0.664f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.294f,
                    dy1 = 0.097f,
                )
                // a 0.664 0.664 0 0 1 -0.294 -0.096
                arcToRelative(
                    a = 0.664f,
                    b = 0.664f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.294f,
                    dy1 = -0.096f,
                )
                // a 2.24 2.24 0 0 1 -0.391 -0.281
                arcToRelative(
                    a = 2.24f,
                    b = 2.24f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.391f,
                    dy1 = -0.281f,
                )
                // a 3.879 3.879 0 0 1 -0.419 -0.429
                arcToRelative(
                    a = 3.879f,
                    b = 3.879f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.419f,
                    dy1 = -0.429f,
                )
                // l -0.005 -0.005
                lineToRelative(dx = -0.005f, dy = -0.005f)
                // V 3.49
                verticalLineTo(y = 3.49f)
                // L 6.5 3
                lineTo(x = 6.5f, y = 3.0f)
                // l -0.392 0.491
                lineToRelative(dx = -0.392f, dy = 0.491f)
                // l -0.005 0.006
                lineToRelative(dx = -0.005f, dy = 0.006f)
                // a 2.22 2.22 0 0 1 -0.108 0.125
                arcToRelative(
                    a = 2.22f,
                    b = 2.22f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.108f,
                    dy1 = 0.125f,
                )
                // a 3.876 3.876 0 0 1 -0.31 0.304
                arcToRelative(
                    a = 3.876f,
                    b = 3.876f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.31f,
                    dy1 = 0.304f,
                )
                // a 2.24 2.24 0 0 1 -0.392 0.28
                arcToRelative(
                    a = 2.24f,
                    b = 2.24f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.392f,
                    dy1 = 0.28f,
                )
                // a 0.664 0.664 0 0 1 -0.294 0.097
                arcToRelative(
                    a = 0.664f,
                    b = 0.664f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.294f,
                    dy1 = 0.097f,
                )
                // c -0.267 0 -0.475 -0.066 -0.635 -0.172
                curveToRelative(
                    dx1 = -0.267f,
                    dy1 = 0.0f,
                    dx2 = -0.475f,
                    dy2 = -0.066f,
                    dx3 = -0.635f,
                    dy3 = -0.172f,
                )
                // c -0.16 -0.107 -0.3 -0.274 -0.403 -0.52
                curveToRelative(
                    dx1 = -0.16f,
                    dy1 = -0.107f,
                    dx2 = -0.3f,
                    dy2 = -0.274f,
                    dx3 = -0.403f,
                    dy3 = -0.52f,
                )
                // a 0.504 0.504 0 0 0 -0.04 -0.076
                arcToRelative(
                    a = 0.504f,
                    b = 0.504f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.04f,
                    dy1 = -0.076f,
                )
                // l 0.271 1.625z
                lineToRelative(dx = 0.271f, dy = 1.625f)
                close()
            }
            // M2.653 2 4.82 15 H1 V2 h1.653Z m.986 -.164 A1 1 0 0 0 2.653 1 H0 v15 h6 L3.64 1.836Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 2.653 2
                moveTo(x = 2.653f, y = 2.0f)
                // L 4.82 15
                lineTo(x = 4.82f, y = 15.0f)
                // H 1
                horizontalLineTo(x = 1.0f)
                // V 2
                verticalLineTo(y = 2.0f)
                // h 1.653z
                horizontalLineToRelative(dx = 1.653f)
                close()
                // m 0.986 -0.164
                moveToRelative(dx = 0.986f, dy = -0.164f)
                // A 1 1 0 0 0 2.653 1
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 2.653f,
                    y1 = 1.0f,
                )
                // H 0
                horizontalLineTo(x = 0.0f)
                // v 15
                verticalLineToRelative(dy = 15.0f)
                // h 6
                horizontalLineToRelative(dx = 6.0f)
                // L 3.64 1.836z
                lineTo(x = 3.64f, y = 1.836f)
                close()
            }
        }.build().also { _ic1204 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1204: ImageVector? = null
