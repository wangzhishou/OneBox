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

package com.wanbaohe.cloud.storage.model

import androidx.annotation.StringRes
import com.wanbaohe.cloud.storage.R

/**
 * 顶层远程文件协议 —— 决定走哪个 [com.wanbaohe.cloud.storage.data.protocol.RemoteFileSystemAdapter]。
 *
 * 后续扩展新协议（FTP / SFTP / S3 兼容 V2 等）只需新增枚举值与对应 Adapter。
 */
enum class RemoteProtocol(
    @StringRes val titleRes: Int,
) {
    S3_COMPAT(
        titleRes = R.string.cloud_protocol_s3_compat,
    ),
    WEB_DAV(
        titleRes = R.string.cloud_protocol_webdav,
    ),
    SMB(
        titleRes = R.string.cloud_protocol_smb,
    ),
}
