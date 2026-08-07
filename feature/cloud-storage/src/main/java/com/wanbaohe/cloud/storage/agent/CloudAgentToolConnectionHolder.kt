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
