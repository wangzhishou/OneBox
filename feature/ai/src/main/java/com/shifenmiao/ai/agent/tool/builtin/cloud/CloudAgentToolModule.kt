package com.shifenmiao.ai.agent.tool.builtin.cloud

import com.shifenmiao.ai.agent.tool.AgentTool
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * 远程存储 AgentTool 多绑定模块 —— 注入到 feature/ai 的 AgentToolRegistry。
 *
 * feature/ai 的 Hilt multibindings 会自动把以下工具加到全局 [AgentToolRegistry]，
 * 无需修改 feature/ai 任何代码。
 */
@Module
@InstallIn(SingletonComponent::class)
object CloudAgentToolModule {

    @Provides
    @IntoMap
    @StringKey("discover_cloud_connections")
    fun provideDiscoverCloudConnectionsTool(tool: DiscoverCloudConnectionsTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("browse_cloud_files")
    fun provideBrowseCloudFilesTool(tool: BrowseCloudFilesTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("read_cloud_file")
    fun provideReadCloudFileTool(tool: ReadCloudFileTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("search_cloud_files")
    fun provideSearchCloudFilesTool(tool: SearchCloudFilesTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("manage_cloud_files")
    fun provideManageCloudFilesTool(tool: ManageCloudFilesTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("upload_to_cloud")
    fun provideUploadToCloudTool(tool: UploadToCloudTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("manage_cloud_connections")
    fun provideManageCloudConnectionsTool(tool: ManageCloudConnectionsTool): AgentTool = tool
}
