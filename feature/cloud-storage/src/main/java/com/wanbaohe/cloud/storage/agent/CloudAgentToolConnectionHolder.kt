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

package com.wanbaohe.cloud.storage.agent

import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 给 [DiscoverCloudConnectionsTool] / [BrowseCloudFilesTool] / [ManageCloudFilesTool] /
 * [ReadCloudFileTool] / [SearchCloudFilesTool] / [UploadToCloudTool] 注入的"当前连接快照"。
 *
 * Tool 只看这份快照，自身不依赖 [com.wanbaohe.cloud.storage.data.CloudStorageRepository]，
 * 避免把存储层反向暴露给 Agent 层。
 *
 * 由持有 Repository 引用的 Component / ViewModel 周期性 push 最新连接列表。
 */
@Singleton
class CloudAgentToolConnectionHolder @Inject constructor() {
    @Volatile
    private var snapshot: List<CloudStorageConnection> = emptyList()

    fun update(connections: List<CloudStorageConnection>) {
        snapshot = connections
    }

    fun current(): List<CloudStorageConnection> = snapshot
}
