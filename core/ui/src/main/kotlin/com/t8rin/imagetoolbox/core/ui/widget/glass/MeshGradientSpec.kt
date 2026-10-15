package com.t8rin.imagetoolbox.core.ui.widget.glass

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.MeshGradientPainter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Vertices
import androidx.compose.ui.graphics.VertexMode
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas

/** 每条 patch 边上的细分段数:网格渐变很软,10 段足够平,又不至于每帧塞几万个三角形。 */
private const val PATCH_SUBDIVISIONS = 10

/**
 * 一份网格渐变数据:rows × columns 个 patch 的顶点位置、颜色,以及**可选**的贝塞尔控制点。
 *
 * 之所以自己留一份 spec,而不是只留一个 [MeshGradientPainter]:
 * Compose 的网格渐变在 Android 14(API 34)以下是 `Canvas.drawVertices` 回退实现,
 * 那里用的是默认 `android.graphics.Paint`(不透明黑);而平台画顶点色时固定按 Modulate
 * 把顶点色乘上 paint 颜色(Skia `SkDraw::drawVertices`:paint 没有 shader 时用
 * `SkShaders::Color(paint.getColor4f().makeOpaque())` 参与混合)——
 * 于是整块渐变的 RGB 全被乘成 0,只剩 alpha:表现就是一片灰雾 + 深色尖刺。
 * API 34 及以上走平台 Mesh API,没这一步乘法,所以只在低版本出问题。
 *
 * 低版本由 [buildMesh] 自己铺三角网(同一套贝塞尔曲面 + Catmull-Rom 颜色),
 * 用**白色** paint 画顶点色,把那次乘法变成恒等。
 */
internal class MeshGradientSpec(
    val rows: Int,
    val columns: Int,
    private val positions: List<Offset>,
    private val colors: List<Color>,
    /** 顶点指向右边/下边邻居的控制点偏移,未指定为 [Offset.Unspecified](交给平台推断)。 */
    private val rightTangents: List<Offset>,
    private val bottomTangents: List<Offset>,
) {
    val vertexRows: Int = rows + 1
    val vertexColumns: Int = columns + 1

    /**
     * 每条边的切线(指向邻居的偏移)。同一条边两侧共用一条切线:
     * 相邻 patch 的公共边完全重合,不会因为控制点各说各话而漏缝或叠出一道深色尖刺。
     */
    private val edgeTangents: EdgeTangents by lazy { computeEdgeTangents() }

    /** API 34 及以上用:交给平台 Mesh API 画。四条切线都显式给,相邻 patch 才拼得严丝合缝。 */
    fun toPainter(): MeshGradientPainter = MeshGradientPainter(
        rows = rows,
        columns = columns,
        hasBicubicColor = true,
    ) {
        val tangents = edgeTangents
        for (row in 0 until vertexRows) {
            for (column in 0 until vertexColumns) {
                val index = row * vertexColumns + column
                setVertex(
                    row = row,
                    column = column,
                    position = positions[index],
                    color = colors[index],
                    leftControlPoint = tangents.left[index],
                    topControlPoint = tangents.top[index],
                    rightControlPoint = tangents.right[index],
                    bottomControlPoint = tangents.bottom[index],
                )
            }
        }
    }

    /** API 33 及以下用:自己铺三角网,坐标保持归一化,绘制时整体缩放到画布尺寸。 */
    fun buildMesh(): MeshGradientMesh = samplePatches(edgeTangents).toMesh()

    private fun computeEdgeTangents(): EdgeTangents {
        val count = vertexRows * vertexColumns
        val tangents = EdgeTangents(count)
        for (row in 0 until vertexRows) {
            for (column in 0 until vertexColumns) {
                val index = row * vertexColumns + column
                if (column < vertexColumns - 1) {
                    val next = index + 1
                    val tangent = rightTangents[index].takeIf { it.isSpecified }
                        ?: straightTangent(positions[index], positions[next])
                    tangents.right[index] = tangent
                    tangents.left[next] = -tangent
                }
                if (row < vertexRows - 1) {
                    val below = index + vertexColumns
                    val tangent = bottomTangents[index].takeIf { it.isSpecified }
                        ?: straightTangent(positions[index], positions[below])
                    tangents.bottom[index] = tangent
                    tangents.top[below] = -tangent
                }
            }
        }
        return tangents
    }

    private fun samplePatches(tangents: EdgeTangents): MeshGrid {
        val gridSide = PATCH_SUBDIVISIONS + 1
        val step = 1f / PATCH_SUBDIVISIONS
        val positionsWeights = List(gridSide) { cubicWeights(it * step) }
        val colorWeights = List(gridSide) { catmullRomWeights(it * step) }
        val vertexCount = rows * columns * gridSide * gridSide
        val grid = MeshGrid(
            positions = ArrayList(vertexCount),
            colors = ArrayList(vertexCount),
            indices = ArrayList(rows * columns * PATCH_SUBDIVISIONS * PATCH_SUBDIVISIONS * 6),
        )
        for (patchRow in 0 until rows) {
            for (patchColumn in 0 until columns) {
                val base = grid.positions.size
                val controls = patchControlPoints(patchRow, patchColumn, tangents)
                val patchColors = patchColorNeighborhood(patchRow, patchColumn)
                for (v in 0 until gridSide) {
                    for (u in 0 until gridSide) {
                        grid.positions += bezierPoint(
                            controls = controls,
                            uWeights = positionsWeights[u],
                            vWeights = positionsWeights[v],
                        )
                        grid.colors += catmullRomColor(
                            oklab = patchColors,
                            uWeights = colorWeights[u],
                            vWeights = colorWeights[v],
                        )
                    }
                }
                grid.appendPatchIndices(base, gridSide)
            }
        }
        return grid
    }

    /** 单个 patch 的 4×4 控制点网格,下标是 `行(v) * 4 + 列(u)`,与桌面实现一致。 */
    private fun patchControlPoints(
        patchRow: Int,
        patchColumn: Int,
        tangents: EdgeTangents,
    ): Array<Offset> {
        val topLeft = patchRow * vertexColumns + patchColumn
        val topRight = topLeft + 1
        val bottomLeft = topLeft + vertexColumns
        val bottomRight = bottomLeft + 1
        val controls = Array(CONTROL_GRID_SIZE * CONTROL_GRID_SIZE) { Offset.Zero }
        controls[0] = positions[topLeft]
        controls[3] = positions[topRight]
        controls[12] = positions[bottomLeft]
        controls[15] = positions[bottomRight]
        controls[1] = controls[0] + tangents.right[topLeft]
        controls[2] = controls[3] + tangents.left[topRight]
        controls[13] = controls[12] + tangents.right[bottomLeft]
        controls[14] = controls[15] + tangents.left[bottomRight]
        controls[4] = controls[0] + tangents.bottom[topLeft]
        controls[8] = controls[12] + tangents.top[bottomLeft]
        controls[7] = controls[3] + tangents.bottom[topRight]
        controls[11] = controls[15] + tangents.top[bottomRight]
        controls[5] = controls[1] + controls[4] - controls[0]
        controls[6] = controls[2] + controls[7] - controls[3]
        controls[9] = controls[8] + controls[13] - controls[12]
        controls[10] = controls[11] + controls[14] - controls[15]
        return controls
    }

    /** patch 周围 4×4 个顶点的颜色,越界按 clamp 取边上的顶点(与平台回退实现一致),已转成 Oklab。 */
    private fun patchColorNeighborhood(patchRow: Int, patchColumn: Int): FloatArray {
        val oklab = FloatArray(CONTROL_GRID_SIZE * CONTROL_GRID_SIZE * OKLAB_CHANNELS)
        for (row in 0 until CONTROL_GRID_SIZE) {
            for (column in 0 until CONTROL_GRID_SIZE) {
                val sourceRow = (patchRow - 1 + row).coerceIn(0, rows)
                val sourceColumn = (patchColumn - 1 + column).coerceIn(0, columns)
                val color = colors[sourceRow * vertexColumns + sourceColumn]
                    .convert(ColorSpaces.Oklab)
                val base = (row * CONTROL_GRID_SIZE + column) * OKLAB_CHANNELS
                oklab[base] = color.red
                oklab[base + 1] = color.green
                oklab[base + 2] = color.blue
                oklab[base + 3] = color.alpha
            }
        }
        return oklab
    }
}

internal class MeshGradientMesh(
    positions: List<Offset>,
    colors: List<Color>,
    indices: List<Int>,
) {
    /**
     * 必须是**不透明白**:平台固定按 Modulate 把顶点色乘上 paint 颜色,
     * 白色等于不做乘法(默认黑 paint 会把整块渐变乘成黑色,只剩 alpha)。
     */
    private val paint = Paint().apply { color = Color.White }

    private val vertices = Vertices(
        vertexMode = VertexMode.Triangles,
        positions = positions,
        textureCoordinates = positions,
        colors = colors,
        indices = indices,
    )

    fun DrawScope.draw() {
        drawIntoCanvas { canvas -> canvas.drawVertices(vertices, BlendMode.SrcOver, paint) }
    }
}

private const val CONTROL_GRID_SIZE = 4
private const val OKLAB_CHANNELS = 4

/** 每条边两侧共享的切线;边界上没用到的那几条保持 [Offset.Zero]。 */
private class EdgeTangents(vertexCount: Int) {
    val right = Array(vertexCount) { Offset.Zero }
    val left = Array(vertexCount) { Offset.Zero }
    val bottom = Array(vertexCount) { Offset.Zero }
    val top = Array(vertexCount) { Offset.Zero }
}

private class MeshGrid(
    val positions: MutableList<Offset>,
    val colors: MutableList<Color>,
    val indices: MutableList<Int>,
) {
    fun appendPatchIndices(base: Int, gridSide: Int) {
        for (v in 0 until gridSide - 1) {
            for (u in 0 until gridSide - 1) {
                val topLeft = base + v * gridSide + u
                val topRight = topLeft + 1
                val bottomLeft = topLeft + gridSide
                val bottomRight = bottomLeft + 1
                indices += topLeft
                indices += topRight
                indices += bottomRight
                indices += topLeft
                indices += bottomRight
                indices += bottomLeft
            }
        }
    }

    fun toMesh(): MeshGradientMesh = MeshGradientMesh(
        positions = positions,
        colors = colors,
        indices = indices,
    )
}

/** 直线边的控制点:三等分点,和相邻 patch 拼起来正好是一条直线。 */
private fun straightTangent(from: Offset, to: Offset): Offset = (to - from) / 3f

private fun bezierPoint(
    controls: Array<Offset>,
    uWeights: FloatArray,
    vWeights: FloatArray,
): Offset {
    var x = 0f
    var y = 0f
    for (row in 0 until CONTROL_GRID_SIZE) {
        for (column in 0 until CONTROL_GRID_SIZE) {
            val weight = vWeights[row] * uWeights[column]
            val point = controls[row * CONTROL_GRID_SIZE + column]
            x += weight * point.x
            y += weight * point.y
        }
    }
    return Offset(x, y)
}

private fun catmullRomColor(
    oklab: FloatArray,
    uWeights: FloatArray,
    vWeights: FloatArray,
): Color {
    val channels = FloatArray(OKLAB_CHANNELS)
    for (channel in 0 until OKLAB_CHANNELS) {
        var value = 0f
        for (row in 0 until CONTROL_GRID_SIZE) {
            var rowValue = 0f
            for (column in 0 until CONTROL_GRID_SIZE) {
                rowValue += uWeights[column] *
                    oklab[(row * CONTROL_GRID_SIZE + column) * OKLAB_CHANNELS + channel]
            }
            value += vWeights[row] * rowValue
        }
        channels[channel] = value
    }
    return Color(
        red = channels[0],
        green = channels[1],
        blue = channels[2],
        alpha = channels[3],
        colorSpace = ColorSpaces.Oklab,
    ).convert(ColorSpaces.Srgb)
}

private fun cubicWeights(t: Float): FloatArray {
    val inverse = 1f - t
    return floatArrayOf(
        inverse * inverse * inverse,
        3f * t * inverse * inverse,
        3f * t * t * inverse,
        t * t * t,
    )
}

/** 均匀 Catmull-Rom 基函数,与平台回退实现的系数一致。 */
private fun catmullRomWeights(t: Float): FloatArray {
    val t2 = t * t
    val t3 = t2 * t
    return floatArrayOf(
        0.5f * (-t3 + 2f * t2 - t),
        0.5f * (3f * t3 - 5f * t2 + 2f),
        0.5f * (-3f * t3 + 4f * t2 + t),
        0.5f * (t3 - t2),
    )
}
