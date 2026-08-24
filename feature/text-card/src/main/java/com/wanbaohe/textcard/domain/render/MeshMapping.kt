package com.wanbaohe.textcard.domain.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.wanbaohe.textcard.domain.model.BackgroundSpec

/**
 * Mesh 渐变模型 → core/ui meshGradient 渲染所需的控制点结构。
 * 预览(Modifier.meshGradient)与导出(离屏 drawMeshGradient)共用本映射与分辨率常量,
 * 保证两端同一份网格数据与插值参数。
 */
fun BackgroundSpec.Gradient.toPointPairs(): List<List<Pair<Offset, Color>>> =
    points.map { row ->
        row.map { point ->
            Offset(point.offsetX, point.offsetY) to Color(point.argb)
        }
    }

/** mesh 插值分辨率(双端一致) */
const val MESH_RESOLUTION = 16
