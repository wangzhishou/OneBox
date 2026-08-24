package com.wanbaohe.textcard.domain.model

import androidx.annotation.StringRes
import com.wanbaohe.textcard.R

/**
 * 背景层内容规格。不透明度不放在此处,由组件单独持有(作用于整个背景层)。
 */
sealed interface BackgroundSpec {

    /** 无背景:卡片保持纯白底 */
    data object None : BackgroundSpec

    /** 纸张纹理:代码绘制(横线/方格)或纯色+边框近似(牛皮纸/信纸/彩色纸) */
    data class Paper(val kind: PaperKind) : BackgroundSpec

    /**
     * Mesh 渐变:3×3 控制点网格(归一化坐标 + ARGB 颜色),
     * 预览走 Modifier.meshGradient,导出走同一 PointData 插值的离屏渲染,两端共用本模型。
     */
    data class Gradient(
        val points: List<List<MeshPoint>>,
    ) : BackgroundSpec

    /** 相册自定义图片,居中裁剪铺满;offset 为归一化拖动偏移(X 相对画布宽、Y 相对画布高) */
    data class Image(
        val uri: String,
        val offsetX: Float = 0f,
        val offsetY: Float = 0f,
    ) : BackgroundSpec
}

/** Mesh 控制点:可序列化基础类型,不引 Compose 类型 */
data class MeshPoint(
    val offsetX: Float,
    val offsetY: Float,
    val argb: Long,
)

enum class PaperKind(@param:StringRes val labelRes: Int) {
    Lined(R.string.textcard_paper_lined),
    Grid(R.string.textcard_paper_grid),
    Kraft(R.string.textcard_paper_kraft),
    Letter(R.string.textcard_paper_letter),
    Colorful(R.string.textcard_paper_colorful),
}

/** 内置 mesh 渐变色卡(设计稿 02 的淡紫/粉橙/青绿风格,3×3 网格) */
object GradientPresets {

    private val lattice = listOf(0f, 0.5f, 1f)

    /** 3×3 网格,按行填入 9 个颜色 */
    private fun of(vararg colors: Long): BackgroundSpec.Gradient {
        require(colors.size == 9)
        return BackgroundSpec.Gradient(
            points = List(3) { row ->
                List(3) { col ->
                    MeshPoint(
                        offsetX = lattice[col],
                        offsetY = lattice[row],
                        argb = colors[row * 3 + col]
                    )
                }
            }
        )
    }

    val all: List<BackgroundSpec.Gradient> = listOf(
        // 淡紫蓝(设计稿默认)
        of(
            0xFFE8F0FE, 0xFFE3E6FA, 0xFFE8DCF7,
            0xFFDFE9FD, 0xFFE4DFF6, 0xFFEAD9F2,
            0xFFE4E7FB, 0xFFE9DDF6, 0xFFF0E1F4
        ),
        // 粉橙
        of(
            0xFFFFD9E3, 0xFFFFCFD8, 0xFFFFDABF,
            0xFFFFCBD6, 0xFFFFCBB8, 0xFFFFE0B8,
            0xFFFFC9C0, 0xFFFFD6AD, 0xFFFFE8C9
        ),
        // 青绿
        of(
            0xFFCBF5E4, 0xFFBFEFDF, 0xFFB5E8E8,
            0xFFB9F0DC, 0xFFA9E9DD, 0xFFA8DEEE,
            0xFFB2ECCB, 0xFFA0E3D9, 0xFF9FD8F0
        ),
        // 暖杏
        of(
            0xFFFFF1DD, 0xFFFFE8D2, 0xFFFFE3C8,
            0xFFFFE9D6, 0xFFFFDFC9, 0xFFFFDDC0,
            0xFFFFE4CD, 0xFFFFDAC6, 0xFFFFD8BC
        ),
    )

    val default: BackgroundSpec.Gradient = all.first()
}
