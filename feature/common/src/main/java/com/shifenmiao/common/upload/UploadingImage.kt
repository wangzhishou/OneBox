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

package com.shifenmiao.common.upload

import com.shifenmiao.model.StrapiImage

/**
 * 上传中的图片状态机 (与 [com.shifenmiao.common.upload.rememberImageUploader] 配合使用).
 *
 * - 上传中: [progress] 0..1, [isUploaded]=false, [isError]=false
 * - 成功:   [strapiImage] 非空, [isUploaded]=true; [id] 同步写入方便发请求
 * - 失败:   [isError]=true, 可重试
 *
 * 本来位于 feature/blog/ui/ImageUploadGallery.kt, 提升到 common 供评论等其它
 * 上传场景复用.
 */
data class UploadingImage(
    val id: Int? = null,
    val localUri: String,
    var progress: Float = 0f,
    var strapiImage: StrapiImage? = null,
    var isUploaded: Boolean = false,
    var isError: Boolean = false,
)
