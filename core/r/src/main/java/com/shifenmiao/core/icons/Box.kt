package com.shifenmiao.core.icons
import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview


@Preview
@Composable
private fun VectorPreview() {
    Image(Box, null)
}

private var _Box: ImageVector? = null

public val Box: ImageVector
    get() {
        if (_Box != null) {
            return _Box!!
        }
        _Box = ImageVector.Builder(
            name = "Box",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(20.73f, 16.52f)
                curveTo(20.73f, 16.52f, 20.73f, 16.45f, 20.73f, 16.41f)
                verticalLineTo(7.58999f)
                curveTo(20.7297f, 7.4752f, 20.7022f, 7.3622f, 20.65f, 7.26f)
                curveTo(20.5764f, 7.1012f, 20.4488f, 6.9736f, 20.29f, 6.9f)
                lineTo(12.29f, 3.31999f)
                curveTo(12.1926f, 3.2758f, 12.0869f, 3.2529f, 11.98f, 3.2529f)
                curveTo(11.8731f, 3.2529f, 11.7674f, 3.2758f, 11.67f, 3.32f)
                lineTo(3.67001f, 6.89999f)
                curveTo(3.5413f, 6.9647f, 3.4325f, 7.063f, 3.3551f, 7.1845f)
                curveTo(3.2777f, 7.3059f, 3.2344f, 7.446f, 3.23f, 7.59f)
                verticalLineTo(16.41f)
                curveTo(3.2375f, 16.5532f, 3.282f, 16.6921f, 3.3591f, 16.813f)
                curveTo(3.4362f, 16.9339f, 3.5433f, 17.0328f, 3.67f, 17.1f)
                lineTo(11.67f, 20.68f)
                curveTo(11.7668f, 20.7262f, 11.8727f, 20.7501f, 11.98f, 20.7501f)
                curveTo(12.0873f, 20.7501f, 12.1932f, 20.7262f, 12.29f, 20.68f)
                lineTo(20.29f, 17.1f)
                curveTo(20.4055f, 17.0471f, 20.5061f, 16.9665f, 20.5829f, 16.8653f)
                curveTo(20.6597f, 16.7641f, 20.7102f, 16.6455f, 20.73f, 16.52f)
                close()
                moveTo(4.73001f, 8.73999f)
                lineTo(11.23f, 11.66f)
                verticalLineTo(18.84f)
                lineTo(4.73001f, 15.93f)
                verticalLineTo(8.73999f)
                close()
                moveTo(12.73f, 11.66f)
                lineTo(19.23f, 8.73999f)
                verticalLineTo(15.93f)
                lineTo(12.73f, 18.84f)
                verticalLineTo(11.66f)
                close()
                moveTo(12f, 4.81999f)
                lineTo(18.17f, 7.58999f)
                lineTo(12f, 10.35f)
                lineTo(5.83001f, 7.58999f)
                lineTo(12f, 4.81999f)
                close()
            }
        }.build()
        return _Box!!
    }


