/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
