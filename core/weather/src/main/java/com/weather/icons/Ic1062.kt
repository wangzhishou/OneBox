package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1062: ImageVector
    get() {
        val current = _ic1062
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1062",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M6.053 10.177 a.312 .312 0 0 1 .433 -.087 l.636 .424 c.093 .062 .212 .07 .313 .02 l.646 -.324 a.937 .937 0 0 1 .838 0 l.691 .346 a.312 .312 0 0 0 .28 0 l.69 -.346 a.937 .937 0 0 1 .84 0 l.69 .346 a.312 .312 0 0 0 .28 0 l.69 -.346 a.937 .937 0 0 1 .84 0 l.645 .323 c.1 .05 .22 .043 .313 -.02 l.636 -.423 a.313 .313 0 0 1 .347 .52 l-.636 .424 a.937 .937 0 0 1 -.94 .058 l-.645 -.323 a.312 .312 0 0 0 -.28 0 l-.69 .346 a.938 .938 0 0 1 -.84 0 l-.69 -.346 a.313 .313 0 0 0 -.28 0 l-.69 .346 a.938 .938 0 0 1 -.84 0 l-.69 -.346 a.312 .312 0 0 0 -.28 0 l-.646 .323 c-.3 .15 -.66 .128 -.94 -.058 l-.635 -.424 a.312 .312 0 0 1 -.086 -.433Z m0 1.5 a.312 .312 0 0 1 .433 -.087 l.636 .424 c.093 .062 .212 .07 .313 .02 l.646 -.324 a.938 .938 0 0 1 .838 0 l.691 .346 a.312 .312 0 0 0 .28 0 l.69 -.346 a.938 .938 0 0 1 .84 0 l.69 .346 a.312 .312 0 0 0 .28 0 l.69 -.346 a.938 .938 0 0 1 .84 0 l.645 .323 c.1 .05 .22 .043 .313 -.02 l.636 -.423 a.312 .312 0 1 1 .347 .52 l-.636 .424 a.938 .938 0 0 1 -.94 .058 l-.645 -.323 a.312 .312 0 0 0 -.28 0 l-.69 .346 a.937 .937 0 0 1 -.84 0 l-.69 -.346 a.313 .313 0 0 0 -.28 0 l-.69 .346 a.937 .937 0 0 1 -.84 0 l-.69 -.346 a.312 .312 0 0 0 -.28 0 l-.646 .323 c-.3 .15 -.66 .128 -.94 -.058 l-.635 -.424 a.312 .312 0 0 1 -.086 -.433Z m.433 1.413 a.312 .312 0 1 0 -.347 .52 l.636 .424 c.28 .186 .638 .209 .94 .058 l.645 -.323 a.313 .313 0 0 1 .28 0 l.69 .346 a.937 .937 0 0 0 .84 0 l.69 -.346 a.313 .313 0 0 1 .28 0 l.69 .346 a.937 .937 0 0 0 .84 0 l.69 -.346 a.313 .313 0 0 1 .28 0 l.646 .323 c.3 .15 .66 .128 .94 -.058 l.635 -.424 a.313 .313 0 0 0 -.347 -.52 l-.636 .424 a.313 .313 0 0 1 -.313 .02 l-.646 -.324 a.938 .938 0 0 0 -.838 0 l-.691 .346 a.312 .312 0 0 1 -.28 0 l-.69 -.346 a.938 .938 0 0 0 -.84 0 l-.69 .346 a.312 .312 0 0 1 -.28 0 l-.69 -.346 a.938 .938 0 0 0 -.84 0 l-.645 .323 a.31 .31 0 0 1 -.313 -.02 l-.636 -.423Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6.053 10.177
                moveTo(x = 6.053f, y = 10.177f)
                // a 0.312 0.312 0 0 1 0.433 -0.087
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.433f,
                    dy1 = -0.087f,
                )
                // l 0.636 0.424
                lineToRelative(dx = 0.636f, dy = 0.424f)
                // c 0.093 0.062 0.212 0.07 0.313 0.02
                curveToRelative(
                    dx1 = 0.093f,
                    dy1 = 0.062f,
                    dx2 = 0.212f,
                    dy2 = 0.07f,
                    dx3 = 0.313f,
                    dy3 = 0.02f,
                )
                // l 0.646 -0.324
                lineToRelative(dx = 0.646f, dy = -0.324f)
                // a 0.937 0.937 0 0 1 0.838 0
                arcToRelative(
                    a = 0.937f,
                    b = 0.937f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.838f,
                    dy1 = 0.0f,
                )
                // l 0.691 0.346
                lineToRelative(dx = 0.691f, dy = 0.346f)
                // a 0.312 0.312 0 0 0 0.28 0
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.28f,
                    dy1 = 0.0f,
                )
                // l 0.69 -0.346
                lineToRelative(dx = 0.69f, dy = -0.346f)
                // a 0.937 0.937 0 0 1 0.84 0
                arcToRelative(
                    a = 0.937f,
                    b = 0.937f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.84f,
                    dy1 = 0.0f,
                )
                // l 0.69 0.346
                lineToRelative(dx = 0.69f, dy = 0.346f)
                // a 0.312 0.312 0 0 0 0.28 0
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.28f,
                    dy1 = 0.0f,
                )
                // l 0.69 -0.346
                lineToRelative(dx = 0.69f, dy = -0.346f)
                // a 0.937 0.937 0 0 1 0.84 0
                arcToRelative(
                    a = 0.937f,
                    b = 0.937f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.84f,
                    dy1 = 0.0f,
                )
                // l 0.645 0.323
                lineToRelative(dx = 0.645f, dy = 0.323f)
                // c 0.1 0.05 0.22 0.043 0.313 -0.02
                curveToRelative(
                    dx1 = 0.1f,
                    dy1 = 0.05f,
                    dx2 = 0.22f,
                    dy2 = 0.043f,
                    dx3 = 0.313f,
                    dy3 = -0.02f,
                )
                // l 0.636 -0.423
                lineToRelative(dx = 0.636f, dy = -0.423f)
                // a 0.313 0.313 0 0 1 0.347 0.52
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.347f,
                    dy1 = 0.52f,
                )
                // l -0.636 0.424
                lineToRelative(dx = -0.636f, dy = 0.424f)
                // a 0.937 0.937 0 0 1 -0.94 0.058
                arcToRelative(
                    a = 0.937f,
                    b = 0.937f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.94f,
                    dy1 = 0.058f,
                )
                // l -0.645 -0.323
                lineToRelative(dx = -0.645f, dy = -0.323f)
                // a 0.312 0.312 0 0 0 -0.28 0
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.28f,
                    dy1 = 0.0f,
                )
                // l -0.69 0.346
                lineToRelative(dx = -0.69f, dy = 0.346f)
                // a 0.938 0.938 0 0 1 -0.84 0
                arcToRelative(
                    a = 0.938f,
                    b = 0.938f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.84f,
                    dy1 = 0.0f,
                )
                // l -0.69 -0.346
                lineToRelative(dx = -0.69f, dy = -0.346f)
                // a 0.313 0.313 0 0 0 -0.28 0
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.28f,
                    dy1 = 0.0f,
                )
                // l -0.69 0.346
                lineToRelative(dx = -0.69f, dy = 0.346f)
                // a 0.938 0.938 0 0 1 -0.84 0
                arcToRelative(
                    a = 0.938f,
                    b = 0.938f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.84f,
                    dy1 = 0.0f,
                )
                // l -0.69 -0.346
                lineToRelative(dx = -0.69f, dy = -0.346f)
                // a 0.312 0.312 0 0 0 -0.28 0
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.28f,
                    dy1 = 0.0f,
                )
                // l -0.646 0.323
                lineToRelative(dx = -0.646f, dy = 0.323f)
                // c -0.3 0.15 -0.66 0.128 -0.94 -0.058
                curveToRelative(
                    dx1 = -0.3f,
                    dy1 = 0.15f,
                    dx2 = -0.66f,
                    dy2 = 0.128f,
                    dx3 = -0.94f,
                    dy3 = -0.058f,
                )
                // l -0.635 -0.424
                lineToRelative(dx = -0.635f, dy = -0.424f)
                // a 0.312 0.312 0 0 1 -0.086 -0.433z
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.086f,
                    dy1 = -0.433f,
                )
                close()
                // m 0 1.5
                moveToRelative(dx = 0.0f, dy = 1.5f)
                // a 0.312 0.312 0 0 1 0.433 -0.087
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.433f,
                    dy1 = -0.087f,
                )
                // l 0.636 0.424
                lineToRelative(dx = 0.636f, dy = 0.424f)
                // c 0.093 0.062 0.212 0.07 0.313 0.02
                curveToRelative(
                    dx1 = 0.093f,
                    dy1 = 0.062f,
                    dx2 = 0.212f,
                    dy2 = 0.07f,
                    dx3 = 0.313f,
                    dy3 = 0.02f,
                )
                // l 0.646 -0.324
                lineToRelative(dx = 0.646f, dy = -0.324f)
                // a 0.938 0.938 0 0 1 0.838 0
                arcToRelative(
                    a = 0.938f,
                    b = 0.938f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.838f,
                    dy1 = 0.0f,
                )
                // l 0.691 0.346
                lineToRelative(dx = 0.691f, dy = 0.346f)
                // a 0.312 0.312 0 0 0 0.28 0
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.28f,
                    dy1 = 0.0f,
                )
                // l 0.69 -0.346
                lineToRelative(dx = 0.69f, dy = -0.346f)
                // a 0.938 0.938 0 0 1 0.84 0
                arcToRelative(
                    a = 0.938f,
                    b = 0.938f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.84f,
                    dy1 = 0.0f,
                )
                // l 0.69 0.346
                lineToRelative(dx = 0.69f, dy = 0.346f)
                // a 0.312 0.312 0 0 0 0.28 0
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.28f,
                    dy1 = 0.0f,
                )
                // l 0.69 -0.346
                lineToRelative(dx = 0.69f, dy = -0.346f)
                // a 0.938 0.938 0 0 1 0.84 0
                arcToRelative(
                    a = 0.938f,
                    b = 0.938f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.84f,
                    dy1 = 0.0f,
                )
                // l 0.645 0.323
                lineToRelative(dx = 0.645f, dy = 0.323f)
                // c 0.1 0.05 0.22 0.043 0.313 -0.02
                curveToRelative(
                    dx1 = 0.1f,
                    dy1 = 0.05f,
                    dx2 = 0.22f,
                    dy2 = 0.043f,
                    dx3 = 0.313f,
                    dy3 = -0.02f,
                )
                // l 0.636 -0.423
                lineToRelative(dx = 0.636f, dy = -0.423f)
                // a 0.312 0.312 0 1 1 0.347 0.52
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.347f,
                    dy1 = 0.52f,
                )
                // l -0.636 0.424
                lineToRelative(dx = -0.636f, dy = 0.424f)
                // a 0.938 0.938 0 0 1 -0.94 0.058
                arcToRelative(
                    a = 0.938f,
                    b = 0.938f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.94f,
                    dy1 = 0.058f,
                )
                // l -0.645 -0.323
                lineToRelative(dx = -0.645f, dy = -0.323f)
                // a 0.312 0.312 0 0 0 -0.28 0
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.28f,
                    dy1 = 0.0f,
                )
                // l -0.69 0.346
                lineToRelative(dx = -0.69f, dy = 0.346f)
                // a 0.937 0.937 0 0 1 -0.84 0
                arcToRelative(
                    a = 0.937f,
                    b = 0.937f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.84f,
                    dy1 = 0.0f,
                )
                // l -0.69 -0.346
                lineToRelative(dx = -0.69f, dy = -0.346f)
                // a 0.313 0.313 0 0 0 -0.28 0
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.28f,
                    dy1 = 0.0f,
                )
                // l -0.69 0.346
                lineToRelative(dx = -0.69f, dy = 0.346f)
                // a 0.937 0.937 0 0 1 -0.84 0
                arcToRelative(
                    a = 0.937f,
                    b = 0.937f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.84f,
                    dy1 = 0.0f,
                )
                // l -0.69 -0.346
                lineToRelative(dx = -0.69f, dy = -0.346f)
                // a 0.312 0.312 0 0 0 -0.28 0
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.28f,
                    dy1 = 0.0f,
                )
                // l -0.646 0.323
                lineToRelative(dx = -0.646f, dy = 0.323f)
                // c -0.3 0.15 -0.66 0.128 -0.94 -0.058
                curveToRelative(
                    dx1 = -0.3f,
                    dy1 = 0.15f,
                    dx2 = -0.66f,
                    dy2 = 0.128f,
                    dx3 = -0.94f,
                    dy3 = -0.058f,
                )
                // l -0.635 -0.424
                lineToRelative(dx = -0.635f, dy = -0.424f)
                // a 0.312 0.312 0 0 1 -0.086 -0.433z
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.086f,
                    dy1 = -0.433f,
                )
                close()
                // m 0.433 1.413
                moveToRelative(dx = 0.433f, dy = 1.413f)
                // a 0.312 0.312 0 1 0 -0.347 0.52
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.347f,
                    dy1 = 0.52f,
                )
                // l 0.636 0.424
                lineToRelative(dx = 0.636f, dy = 0.424f)
                // c 0.28 0.186 0.638 0.209 0.94 0.058
                curveToRelative(
                    dx1 = 0.28f,
                    dy1 = 0.186f,
                    dx2 = 0.638f,
                    dy2 = 0.209f,
                    dx3 = 0.94f,
                    dy3 = 0.058f,
                )
                // l 0.645 -0.323
                lineToRelative(dx = 0.645f, dy = -0.323f)
                // a 0.313 0.313 0 0 1 0.28 0
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.28f,
                    dy1 = 0.0f,
                )
                // l 0.69 0.346
                lineToRelative(dx = 0.69f, dy = 0.346f)
                // a 0.937 0.937 0 0 0 0.84 0
                arcToRelative(
                    a = 0.937f,
                    b = 0.937f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.84f,
                    dy1 = 0.0f,
                )
                // l 0.69 -0.346
                lineToRelative(dx = 0.69f, dy = -0.346f)
                // a 0.313 0.313 0 0 1 0.28 0
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.28f,
                    dy1 = 0.0f,
                )
                // l 0.69 0.346
                lineToRelative(dx = 0.69f, dy = 0.346f)
                // a 0.937 0.937 0 0 0 0.84 0
                arcToRelative(
                    a = 0.937f,
                    b = 0.937f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.84f,
                    dy1 = 0.0f,
                )
                // l 0.69 -0.346
                lineToRelative(dx = 0.69f, dy = -0.346f)
                // a 0.313 0.313 0 0 1 0.28 0
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.28f,
                    dy1 = 0.0f,
                )
                // l 0.646 0.323
                lineToRelative(dx = 0.646f, dy = 0.323f)
                // c 0.3 0.15 0.66 0.128 0.94 -0.058
                curveToRelative(
                    dx1 = 0.3f,
                    dy1 = 0.15f,
                    dx2 = 0.66f,
                    dy2 = 0.128f,
                    dx3 = 0.94f,
                    dy3 = -0.058f,
                )
                // l 0.635 -0.424
                lineToRelative(dx = 0.635f, dy = -0.424f)
                // a 0.313 0.313 0 0 0 -0.347 -0.52
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.347f,
                    dy1 = -0.52f,
                )
                // l -0.636 0.424
                lineToRelative(dx = -0.636f, dy = 0.424f)
                // a 0.313 0.313 0 0 1 -0.313 0.02
                arcToRelative(
                    a = 0.313f,
                    b = 0.313f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.313f,
                    dy1 = 0.02f,
                )
                // l -0.646 -0.324
                lineToRelative(dx = -0.646f, dy = -0.324f)
                // a 0.938 0.938 0 0 0 -0.838 0
                arcToRelative(
                    a = 0.938f,
                    b = 0.938f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.838f,
                    dy1 = 0.0f,
                )
                // l -0.691 0.346
                lineToRelative(dx = -0.691f, dy = 0.346f)
                // a 0.312 0.312 0 0 1 -0.28 0
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.28f,
                    dy1 = 0.0f,
                )
                // l -0.69 -0.346
                lineToRelative(dx = -0.69f, dy = -0.346f)
                // a 0.938 0.938 0 0 0 -0.84 0
                arcToRelative(
                    a = 0.938f,
                    b = 0.938f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.84f,
                    dy1 = 0.0f,
                )
                // l -0.69 0.346
                lineToRelative(dx = -0.69f, dy = 0.346f)
                // a 0.312 0.312 0 0 1 -0.28 0
                arcToRelative(
                    a = 0.312f,
                    b = 0.312f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.28f,
                    dy1 = 0.0f,
                )
                // l -0.69 -0.346
                lineToRelative(dx = -0.69f, dy = -0.346f)
                // a 0.938 0.938 0 0 0 -0.84 0
                arcToRelative(
                    a = 0.938f,
                    b = 0.938f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.84f,
                    dy1 = 0.0f,
                )
                // l -0.645 0.323
                lineToRelative(dx = -0.645f, dy = 0.323f)
                // a 0.31 0.31 0 0 1 -0.313 -0.02
                arcToRelative(
                    a = 0.31f,
                    b = 0.31f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.313f,
                    dy1 = -0.02f,
                )
                // l -0.636 -0.423z
                lineToRelative(dx = -0.636f, dy = -0.423f)
                close()
            }
            // M15.497 3.077 S7.525 .722 4.845 .023 a.794 .794 0 0 0 -.956 .568 L.024 15 a.81 .81 0 0 0 .544 .968 .811 .811 0 0 0 1 -.554 l1.671 -6.23 12.373 -4.817 a.696 .696 0 0 0 -.115 -1.291Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 15.497 3.077
                moveTo(x = 15.497f, y = 3.077f)
                // S 7.525 0.722 4.845 0.023
                reflectiveCurveTo(
                    x1 = 7.525f,
                    y1 = 0.722f,
                    x2 = 4.845f,
                    y2 = 0.023f,
                )
                // a 0.794 0.794 0 0 0 -0.956 0.568
                arcToRelative(
                    a = 0.794f,
                    b = 0.794f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.956f,
                    dy1 = 0.568f,
                )
                // L 0.024 15
                lineTo(x = 0.024f, y = 15.0f)
                // a 0.81 0.81 0 0 0 0.544 0.968
                arcToRelative(
                    a = 0.81f,
                    b = 0.81f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.544f,
                    dy1 = 0.968f,
                )
                // a 0.811 0.811 0 0 0 1 -0.554
                arcToRelative(
                    a = 0.811f,
                    b = 0.811f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.0f,
                    dy1 = -0.554f,
                )
                // l 1.671 -6.23
                lineToRelative(dx = 1.671f, dy = -6.23f)
                // l 12.373 -4.817
                lineToRelative(dx = 12.373f, dy = -4.817f)
                // a 0.696 0.696 0 0 0 -0.115 -1.291z
                arcToRelative(
                    a = 0.696f,
                    b = 0.696f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.115f,
                    dy1 = -1.291f,
                )
                close()
            }
        }.build().also { _ic1062 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1062: ImageVector? = null
