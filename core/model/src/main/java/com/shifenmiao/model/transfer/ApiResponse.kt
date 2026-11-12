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

/**
 * 基础API响应
 */
data class ApiResponse(
    val success: Boolean,
    val message: String? = null
)

/**
 * 创建文件夹请求
 */
data class MkdirRequest(
    val path: String,
    val name: String
)

/**
 * 重命名请求
 */
data class RenameRequest(
    val path: String,
    val newName: String
)

/**
 * 删除请求
 */
data class DeleteRequest(
    val paths: List<String>
)

/**
 * 删除响应
 */
data class DeleteResponse(
    val success: Boolean,
    val message: String? = null,
    val deletedCount: Int = 0,
    val failedPaths: List<String> = emptyList()
)

