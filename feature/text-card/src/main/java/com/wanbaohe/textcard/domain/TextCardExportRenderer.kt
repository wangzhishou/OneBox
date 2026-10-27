package com.wanbaohe.textcard.domain

import android.graphics.Bitmap
import com.wanbaohe.textcard.domain.model.TextCardRenderState

/**
 * 文字卡片导出渲染器:按实际画布规格离屏绘制完整卡片。
 * 排版几何与颜色常量与预览侧共用(见 [com.wanbaohe.textcard.domain.render.CardLayout])。
 */
interface TextCardExportRenderer {

    suspend fun render(state: TextCardRenderState): Bitmap
}
