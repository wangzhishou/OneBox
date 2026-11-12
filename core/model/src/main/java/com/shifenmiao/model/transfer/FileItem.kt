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

package com.shifenmiao.model.transfer

import kotlinx.serialization.Serializable

/**
 * 文件项数据模型
 */
@Serializable
data class FileItem(
    /** 文件名 */
    val name: String,
    /** 完整路径 */
    val path: String,
    /** 文件大小(字节) */
    val size: Long,
    /** 是否为目录 */
    val isDirectory: Boolean,
    /** MIME类型 */
    val mimeType: String?,
    /** 最后修改时间(毫秒时间戳) */
    val lastModified: Long,
    /** 是否可读 */
    val canRead: Boolean = true,
    /** 是否可写 */
    val canWrite: Boolean = true
)

/**
 * 文件列表响应
 */
@Serializable
data class FileListResponse(
    /** 是否成功 */
    val success: Boolean,
    /** 错误信息 */
    val message: String? = null,
    /** 文件列表 */
    val files: List<FileItem> = emptyList(),
    /** 当前路径 */
    val currentPath: String = "",
    /** 是否可以返回上级目录 */
    val canGoUp: Boolean = false,
    /** 父目录路径 */
    val parentPath: String? = null
)

/**
 * 上传结果响应
 */
@Serializable
data class UploadResponse(
    val success: Boolean,
    val message: String? = null,
    val fileName: String? = null,
    val filePath: String? = null
)

/**
 * 设备信息
 */
@Serializable
data class DeviceInfo(
    val deviceName: String,
    val totalSpace: Long,
    val freeSpace: Long,
    val allowUpload: Boolean
)

