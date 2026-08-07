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
