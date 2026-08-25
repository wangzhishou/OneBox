package com.wanbaohe.textcard.domain

import com.wanbaohe.textcard.domain.model.RemotePaper

/**
 * 远程纸张仓库:拉取 Strapi text-card-paper 列表并把图片下载到本地。
 * 失败/无网/空列表返回空表(调用方静默降级只显示内置纸张)。
 */
interface TextCardPaperRepository {

    suspend fun loadLocalPapers(): List<RemotePaper>
}
