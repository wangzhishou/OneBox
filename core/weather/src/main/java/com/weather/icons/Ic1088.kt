package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1088: ImageVector
    get() {
        val current = _ic1088
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1088",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M11.773 .63 a.263 .263 0 0 1 .454 0 l3.738 6.482 a.26 .26 0 0 1 -.227 .388 H8.262 a.26 .26 0 0 1 -.227 -.388 L11.773 .63Z M12 2.25 c-.367 0 -.655 .242 -.623 .524 l.282 2.476 h.682 l.282 -2.476 c.032 -.282 -.256 -.524 -.623 -.524Z m.004 4.5 a.5 .5 0 1 0 0 -1 .5 .5 0 0 0 0 1Z m-9.236 .982 .866 -.5 A2 2 0 0 0 4.366 4.5 L3.5 5 a2 2 0 0 0 -.732 2.732Z m.982 -2.299 .36 -.207 a1.5 1.5 0 0 1 -.726 1.573 l-.359 .207 a1.5 1.5 0 0 1 .725 -1.573Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.773 0.63
                moveTo(x = 11.773f, y = 0.63f)
                // a 0.263 0.263 0 0 1 0.454 0
                arcToRelative(
                    a = 0.263f,
                    b = 0.263f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.454f,
                    dy1 = 0.0f,
                )
                // l 3.738 6.482
                lineToRelative(dx = 3.738f, dy = 6.482f)
                // a 0.26 0.26 0 0 1 -0.227 0.388
                arcToRelative(
                    a = 0.26f,
                    b = 0.26f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.227f,
                    dy1 = 0.388f,
                )
                // H 8.262
                horizontalLineTo(x = 8.262f)
                // a 0.26 0.26 0 0 1 -0.227 -0.388
                arcToRelative(
                    a = 0.26f,
                    b = 0.26f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.227f,
                    dy1 = -0.388f,
                )
                // L 11.773 0.63z
                lineTo(x = 11.773f, y = 0.63f)
                close()
                // M 12 2.25
                moveTo(x = 12.0f, y = 2.25f)
                // c -0.367 0 -0.655 0.242 -0.623 0.524
                curveToRelative(
                    dx1 = -0.367f,
                    dy1 = 0.0f,
                    dx2 = -0.655f,
                    dy2 = 0.242f,
                    dx3 = -0.623f,
                    dy3 = 0.524f,
                )
                // l 0.282 2.476
                lineToRelative(dx = 0.282f, dy = 2.476f)
                // h 0.682
                horizontalLineToRelative(dx = 0.682f)
                // l 0.282 -2.476
                lineToRelative(dx = 0.282f, dy = -2.476f)
                // c 0.032 -0.282 -0.256 -0.524 -0.623 -0.524z
                curveToRelative(
                    dx1 = 0.032f,
                    dy1 = -0.282f,
                    dx2 = -0.256f,
                    dy2 = -0.524f,
                    dx3 = -0.623f,
                    dy3 = -0.524f,
                )
                close()
                // m 0.004 4.5
                moveToRelative(dx = 0.004f, dy = 4.5f)
                // a 0.5 0.5 0 1 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // a 0.5 0.5 0 0 0 0 1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                close()
                // m -9.236 0.982
                moveToRelative(dx = -9.236f, dy = 0.982f)
                // l 0.866 -0.5
                lineToRelative(dx = 0.866f, dy = -0.5f)
                // A 2 2 0 0 0 4.366 4.5
                arcTo(
                    horizontalEllipseRadius = 2.0f,
                    verticalEllipseRadius = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 4.366f,
                    y1 = 4.5f,
                )
                // L 3.5 5
                lineTo(x = 3.5f, y = 5.0f)
                // a 2 2 0 0 0 -0.732 2.732z
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.732f,
                    dy1 = 2.732f,
                )
                close()
                // m 0.982 -2.299
                moveToRelative(dx = 0.982f, dy = -2.299f)
                // l 0.36 -0.207
                lineToRelative(dx = 0.36f, dy = -0.207f)
                // a 1.5 1.5 0 0 1 -0.726 1.573
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.726f,
                    dy1 = 1.573f,
                )
                // l -0.359 0.207
                lineToRelative(dx = -0.359f, dy = 0.207f)
                // a 1.5 1.5 0 0 1 0.725 -1.573z
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.725f,
                    dy1 = -1.573f,
                )
                close()
            }
            // M.063 7.5 A2 2 0 0 0 2 9 h2 a2 2 0 0 0 2 -2 H5 a2 2 0 0 0 -2 2 2 2 0 0 0 -2 -2 H0 c0 .173 .022 .34 .063 .5Z M1 7.5 a1.5 1.5 0 0 1 1.415 1 H2 a1.5 1.5 0 0 1 -1.415 -1 H1Z m4 0 h.415 A1.5 1.5 0 0 1 4 8.5 h-.415 A1.5 1.5 0 0 1 5 7.5Z m11 1.99 v6.507 H0 V9.52 h.381 c1.907 0 3.157 .039 4.393 .212 1.212 .17 2.399 .467 4.17 .97 1.933 -.632 4.044 -.98 6.646 -1.18 L16 9.49Z m-8.282 1.662 c-1.22 -.331 -2.131 -.537 -3.05 -.666 a21.144 21.144 0 0 0 -2.369 -.182 c.294 .141 .559 .282 .816 .437 .648 .388 1.24 .858 2.149 1.621 a19.517 19.517 0 0 1 2.454 -1.21Z m-3.144 1.627 c-.79 -.66 -1.303 -1.057 -1.85 -1.385 -.522 -.313 -1.083 -.57 -1.962 -.928 v4.769 h.436 c1.19 -.966 2.294 -1.777 3.376 -2.456Z m-2.152 2.456 h3.51 c2.129 -2.233 4.348 -3.536 6.938 -4.651 -1.394 .207 -2.632 .49 -3.797 .879 -2.27 .758 -4.286 1.93 -6.651 3.772Z m11.255 -4.164 c-2.54 1.052 -4.66 2.213 -6.673 4.164 h3.626 c.68 -1.25 1.265 -2.174 1.964 -3.013 .33 -.397 .685 -.772 1.083 -1.151Z M11.5 15.235 h3.737 l.001 -4.53 c-.857 .716 -1.497 1.331 -2.058 2.005 -.588 .705 -1.098 1.483 -1.68 2.525Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0.063 7.5
                moveTo(x = 0.063f, y = 7.5f)
                // A 2 2 0 0 0 2 9
                arcTo(
                    horizontalEllipseRadius = 2.0f,
                    verticalEllipseRadius = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 2.0f,
                    y1 = 9.0f,
                )
                // h 2
                horizontalLineToRelative(dx = 2.0f)
                // a 2 2 0 0 0 2 -2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 2.0f,
                    dy1 = -2.0f,
                )
                // H 5
                horizontalLineTo(x = 5.0f)
                // a 2 2 0 0 0 -2 2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.0f,
                    dy1 = 2.0f,
                )
                // a 2 2 0 0 0 -2 -2
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.0f,
                    dy1 = -2.0f,
                )
                // H 0
                horizontalLineTo(x = 0.0f)
                // c 0 0.173 0.022 0.34 0.063 0.5z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.173f,
                    dx2 = 0.022f,
                    dy2 = 0.34f,
                    dx3 = 0.063f,
                    dy3 = 0.5f,
                )
                close()
                // M 1 7.5
                moveTo(x = 1.0f, y = 7.5f)
                // a 1.5 1.5 0 0 1 1.415 1
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.415f,
                    dy1 = 1.0f,
                )
                // H 2
                horizontalLineTo(x = 2.0f)
                // a 1.5 1.5 0 0 1 -1.415 -1
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.415f,
                    dy1 = -1.0f,
                )
                // H 1z
                horizontalLineTo(x = 1.0f)
                close()
                // m 4 0
                moveToRelative(dx = 4.0f, dy = 0.0f)
                // h 0.415
                horizontalLineToRelative(dx = 0.415f)
                // A 1.5 1.5 0 0 1 4 8.5
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 4.0f,
                    y1 = 8.5f,
                )
                // h -0.415
                horizontalLineToRelative(dx = -0.415f)
                // A 1.5 1.5 0 0 1 5 7.5z
                arcTo(
                    horizontalEllipseRadius = 1.5f,
                    verticalEllipseRadius = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 5.0f,
                    y1 = 7.5f,
                )
                close()
                // m 11 1.99
                moveToRelative(dx = 11.0f, dy = 1.99f)
                // v 6.507
                verticalLineToRelative(dy = 6.507f)
                // H 0
                horizontalLineTo(x = 0.0f)
                // V 9.52
                verticalLineTo(y = 9.52f)
                // h 0.381
                horizontalLineToRelative(dx = 0.381f)
                // c 1.907 0 3.157 0.039 4.393 0.212
                curveToRelative(
                    dx1 = 1.907f,
                    dy1 = 0.0f,
                    dx2 = 3.157f,
                    dy2 = 0.039f,
                    dx3 = 4.393f,
                    dy3 = 0.212f,
                )
                // c 1.212 0.17 2.399 0.467 4.17 0.97
                curveToRelative(
                    dx1 = 1.212f,
                    dy1 = 0.17f,
                    dx2 = 2.399f,
                    dy2 = 0.467f,
                    dx3 = 4.17f,
                    dy3 = 0.97f,
                )
                // c 1.933 -0.632 4.044 -0.98 6.646 -1.18
                curveToRelative(
                    dx1 = 1.933f,
                    dy1 = -0.632f,
                    dx2 = 4.044f,
                    dy2 = -0.98f,
                    dx3 = 6.646f,
                    dy3 = -1.18f,
                )
                // L 16 9.49z
                lineTo(x = 16.0f, y = 9.49f)
                close()
                // m -8.282 1.662
                moveToRelative(dx = -8.282f, dy = 1.662f)
                // c -1.22 -0.331 -2.131 -0.537 -3.05 -0.666
                curveToRelative(
                    dx1 = -1.22f,
                    dy1 = -0.331f,
                    dx2 = -2.131f,
                    dy2 = -0.537f,
                    dx3 = -3.05f,
                    dy3 = -0.666f,
                )
                // a 21.144 21.144 0 0 0 -2.369 -0.182
                arcToRelative(
                    a = 21.144f,
                    b = 21.144f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.369f,
                    dy1 = -0.182f,
                )
                // c 0.294 0.141 0.559 0.282 0.816 0.437
                curveToRelative(
                    dx1 = 0.294f,
                    dy1 = 0.141f,
                    dx2 = 0.559f,
                    dy2 = 0.282f,
                    dx3 = 0.816f,
                    dy3 = 0.437f,
                )
                // c 0.648 0.388 1.24 0.858 2.149 1.621
                curveToRelative(
                    dx1 = 0.648f,
                    dy1 = 0.388f,
                    dx2 = 1.24f,
                    dy2 = 0.858f,
                    dx3 = 2.149f,
                    dy3 = 1.621f,
                )
                // a 19.517 19.517 0 0 1 2.454 -1.21z
                arcToRelative(
                    a = 19.517f,
                    b = 19.517f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.454f,
                    dy1 = -1.21f,
                )
                close()
                // m -3.144 1.627
                moveToRelative(dx = -3.144f, dy = 1.627f)
                // c -0.79 -0.66 -1.303 -1.057 -1.85 -1.385
                curveToRelative(
                    dx1 = -0.79f,
                    dy1 = -0.66f,
                    dx2 = -1.303f,
                    dy2 = -1.057f,
                    dx3 = -1.85f,
                    dy3 = -1.385f,
                )
                // c -0.522 -0.313 -1.083 -0.57 -1.962 -0.928
                curveToRelative(
                    dx1 = -0.522f,
                    dy1 = -0.313f,
                    dx2 = -1.083f,
                    dy2 = -0.57f,
                    dx3 = -1.962f,
                    dy3 = -0.928f,
                )
                // v 4.769
                verticalLineToRelative(dy = 4.769f)
                // h 0.436
                horizontalLineToRelative(dx = 0.436f)
                // c 1.19 -0.966 2.294 -1.777 3.376 -2.456z
                curveToRelative(
                    dx1 = 1.19f,
                    dy1 = -0.966f,
                    dx2 = 2.294f,
                    dy2 = -1.777f,
                    dx3 = 3.376f,
                    dy3 = -2.456f,
                )
                close()
                // m -2.152 2.456
                moveToRelative(dx = -2.152f, dy = 2.456f)
                // h 3.51
                horizontalLineToRelative(dx = 3.51f)
                // c 2.129 -2.233 4.348 -3.536 6.938 -4.651
                curveToRelative(
                    dx1 = 2.129f,
                    dy1 = -2.233f,
                    dx2 = 4.348f,
                    dy2 = -3.536f,
                    dx3 = 6.938f,
                    dy3 = -4.651f,
                )
                // c -1.394 0.207 -2.632 0.49 -3.797 0.879
                curveToRelative(
                    dx1 = -1.394f,
                    dy1 = 0.207f,
                    dx2 = -2.632f,
                    dy2 = 0.49f,
                    dx3 = -3.797f,
                    dy3 = 0.879f,
                )
                // c -2.27 0.758 -4.286 1.93 -6.651 3.772z
                curveToRelative(
                    dx1 = -2.27f,
                    dy1 = 0.758f,
                    dx2 = -4.286f,
                    dy2 = 1.93f,
                    dx3 = -6.651f,
                    dy3 = 3.772f,
                )
                close()
                // m 11.255 -4.164
                moveToRelative(dx = 11.255f, dy = -4.164f)
                // c -2.54 1.052 -4.66 2.213 -6.673 4.164
                curveToRelative(
                    dx1 = -2.54f,
                    dy1 = 1.052f,
                    dx2 = -4.66f,
                    dy2 = 2.213f,
                    dx3 = -6.673f,
                    dy3 = 4.164f,
                )
                // h 3.626
                horizontalLineToRelative(dx = 3.626f)
                // c 0.68 -1.25 1.265 -2.174 1.964 -3.013
                curveToRelative(
                    dx1 = 0.68f,
                    dy1 = -1.25f,
                    dx2 = 1.265f,
                    dy2 = -2.174f,
                    dx3 = 1.964f,
                    dy3 = -3.013f,
                )
                // c 0.33 -0.397 0.685 -0.772 1.083 -1.151z
                curveToRelative(
                    dx1 = 0.33f,
                    dy1 = -0.397f,
                    dx2 = 0.685f,
                    dy2 = -0.772f,
                    dx3 = 1.083f,
                    dy3 = -1.151f,
                )
                close()
                // M 11.5 15.235
                moveTo(x = 11.5f, y = 15.235f)
                // h 3.737
                horizontalLineToRelative(dx = 3.737f)
                // l 0.001 -4.53
                lineToRelative(dx = 0.001f, dy = -4.53f)
                // c -0.857 0.716 -1.497 1.331 -2.058 2.005
                curveToRelative(
                    dx1 = -0.857f,
                    dy1 = 0.716f,
                    dx2 = -1.497f,
                    dy2 = 1.331f,
                    dx3 = -2.058f,
                    dy3 = 2.005f,
                )
                // c -0.588 0.705 -1.098 1.483 -1.68 2.525z
                curveToRelative(
                    dx1 = -0.588f,
                    dy1 = 0.705f,
                    dx2 = -1.098f,
                    dy2 = 1.483f,
                    dx3 = -1.68f,
                    dy3 = 2.525f,
                )
                close()
            }
        }.build().also { _ic1088 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1088: ImageVector? = null
