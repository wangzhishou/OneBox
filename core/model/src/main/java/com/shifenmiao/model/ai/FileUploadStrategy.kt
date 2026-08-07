package com.shifenmiao.model.ai

/**
 * 文件上传策略
 *
 * BASE64: 默认模式，本地压缩+转WebP+Base64编码，适合小图片和隐私场景
 * CLOUD: 云存储模式，上传到配置的云存储（阿里云OSS/腾讯COS/AWS S3/华为OBS）
 */
enum class FileUploadStrategy {
    BASE64,
    CLOUD
}
