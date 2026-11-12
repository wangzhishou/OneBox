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

package com.wanbaohe.file_transfer.model

import kotlinx.serialization.Serializable

/**
 * 浏览器上传成功后在手机端留下的一条"最近接收"记录。
 *
 * 相对路径都相对于服务端根目录（TransferConfig.rootPath），
 * 这样即使手机存储根目录变了，历史记录仍然能定位。
 */
@Serializable
data class ReceivedFile(
    val id: String,
    val fileName: String,
    /** 相对 rootPath 的文件路径，例如 Download/xxx.apk */
    val relativePath: String,
    /** 相对 rootPath 的目录，例如 Download（根目录为空串） */
    val dir: String,
    val size: Long,
    val timestamp: Long,
    /** 上传来源（浏览器 UA 里的设备名等，可为空） */
    val source: String? = null
)
