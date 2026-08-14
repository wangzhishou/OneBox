package com.shifenmiao.model.imageprocess

import com.shifenmiao.core.constants.UrlConstants

/**
 * 百度 AI 图像处理能力(经 Go 网关代理,接口定义见
 * [com.shifenmiao.network.api.BaiduImageProcessApiService],执行入口为
 * `com.shifenmiao.network.repository.BaiduImageProcessRepository`)。
 *
 * 纯数据枚举,只携带接口路径;UI 文案/图标由调用方自行映射
 * (如 markup-layers 的 AiImageOp)。
 */
enum class ImageProcessOp(val path: String) {
    Dehaze(UrlConstants.BAIDU_IMAGE_PROCESS_DEHAZE_PATH),
    ContrastEnhance(UrlConstants.BAIDU_IMAGE_PROCESS_CONTRAST_ENHANCE_PATH),
    QualityEnhance(UrlConstants.BAIDU_IMAGE_PROCESS_QUALITY_ENHANCE_PATH),
    StretchRestore(UrlConstants.BAIDU_IMAGE_PROCESS_STRETCH_RESTORE_PATH),
    Inpainting(UrlConstants.BAIDU_IMAGE_PROCESS_INPAINTING_PATH),
    DefinitionEnhance(UrlConstants.BAIDU_IMAGE_PROCESS_DEFINITION_ENHANCE_PATH),
    ColorEnhance(UrlConstants.BAIDU_IMAGE_PROCESS_COLOR_ENHANCE_PATH),
    RemoveMoire(UrlConstants.BAIDU_IMAGE_PROCESS_REMOVE_MOIRE_PATH),
    DocRepair(UrlConstants.BAIDU_IMAGE_PROCESS_DOC_REPAIR_PATH),
    Segment(UrlConstants.BAIDU_IMAGE_PROCESS_SEGMENT_PATH),
}
