package com.shifenmiao.ai.agent.tool.builtin

import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.builtin.browser.BrowserExecuteJsTool
import com.shifenmiao.ai.agent.tool.builtin.browser.BrowserNavigateTool
import com.shifenmiao.ai.agent.tool.builtin.browser.BrowserReadPageTool
import com.shifenmiao.ai.agent.tool.builtin.browser.BrowserScreenshotTool
import com.shifenmiao.ai.agent.tool.builtin.network.CheckUrlTool
import com.shifenmiao.ai.agent.tool.builtin.network.DownloadFileTool
import com.shifenmiao.ai.agent.tool.builtin.network.FetchWebpageTool
import com.shifenmiao.ai.agent.tool.builtin.network.GetNetworkInfoTool
import com.shifenmiao.ai.agent.tool.builtin.network.SearchWebTool
import com.shifenmiao.ai.agent.tool.builtin.network.SendHttpRequestTool
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

/**
 * 内置工具绑定模块。
 *
 * 通过 @IntoMap + @StringKey 将内置工具注册到 AgentToolRegistry。
 * 外部 feature 模块可创建类似的 Module 来注入自定义工具，零修改 AI 模块。
 *
 * 示例（在其他 feature 模块中）：
 * ```
 * @Module
 * @InstallIn(SingletonComponent::class)
 * object WeatherToolModule {
 *     @Provides
 *     @IntoMap
 *     @StringKey("get_weather")
 *     fun provideWeatherTool(impl: GetWeatherTool): AgentTool = impl
 * }
 * ```
 */
@Module
@InstallIn(SingletonComponent::class)
object BuiltinToolModule {

    @Provides
    @IntoMap
    @StringKey("get_current_time")
    fun provideGetCurrentTimeTool(tool: GetCurrentTimeTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("discover_tools")
    fun provideDiscoverToolsTool(tool: DiscoverToolsTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("discover_apps")
    fun provideDiscoverAppsTool(tool: DiscoverAppsTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("navigate_app_screen")
    fun provideNavigateAppScreenTool(tool: NavigateAppScreenTool): AgentTool = tool


    @Provides
    @IntoMap
    @StringKey("get_device_info")
    fun provideGetDeviceInfoTool(tool: GetDeviceInfoTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("ask_user")
    fun provideAskUserTool(tool: AskUserTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("create_note")
    fun provideCreateNoteTool(tool: CreateNoteTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("view_note")
    fun provideViewNoteTool(tool: ViewNoteTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("theme_setting")
    fun provideThemeSettingTool(tool: ThemeSettingTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("checksum_tool")
    fun provideChecksumTool(tool: ChecksumTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("base64_tool")
    fun provideBase64Tool(tool: Base64Tool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("webp_tool")
    fun provideWebpTool(tool: WebpTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("read_barcode_from_image")
    fun provideReadBarcodeFromImageTool(tool: ReadBarcodeFromImageTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("limit_resize_image")
    fun provideLimitResizeTool(tool: LimitResizeTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("compress_image")
    fun provideCompressImageTool(tool: CompressImageTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("browse_files")
    fun provideBrowseFilesTool(tool: BrowseFilesTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("pick_files")
    fun providePickFilesTool(tool: PickFilesTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("pick_folder")
    fun providePickFolderTool(tool: PickFolderTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("workspace_roots")
    fun provideWorkspaceRootsTool(tool: WorkspaceRootsTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("manage_files")
    fun provideManageFilesTool(tool: ManageFilesTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("glob_files")
    fun provideGlobFilesTool(tool: GlobFilesTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("grep_files")
    fun provideGrepFilesTool(tool: GrepFilesTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("stat_file")
    fun provideStatFileTool(tool: StatFileTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("read_multiple_files")
    fun provideReadMultipleFilesTool(tool: ReadMultipleFilesTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("edit_file")
    fun provideEditFileTool(tool: EditFileTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("apply_text_patch")
    fun provideApplyTextPatchTool(tool: ApplyTextPatchTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("apply_range_patch")
    fun provideApplyRangePatchTool(tool: ApplyRangePatchTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("process_file")
    fun provideProcessFileTool(tool: ProcessFileTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("show_effect")
    fun provideShowEffectTool(tool: ShowEffectTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("add_bookkeeping_record")
    fun provideAddBookkeepingRecordTool(tool: AddBookkeepingRecordTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("query_bookkeeping_summary")
    fun provideQueryBookkeepingSummaryTool(tool: QueryBookkeepingSummaryTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("query_bookkeeping_records")
    fun provideQueryBookkeepingRecordsTool(tool: QueryBookkeepingRecordsTool): AgentTool = tool

    // ========== 网络工具 ==========

    @Provides
    @IntoMap
    @StringKey("fetch_webpage")
    fun provideFetchWebpageTool(tool: FetchWebpageTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("search_web")
    fun provideSearchWebTool(tool: SearchWebTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("check_url")
    fun provideCheckUrlTool(tool: CheckUrlTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("get_network_info")
    fun provideGetNetworkInfoTool(tool: GetNetworkInfoTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("send_http_request")
    fun provideSendHttpRequestTool(tool: SendHttpRequestTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("download_file")
    fun provideDownloadFileTool(tool: DownloadFileTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("manage_bookmark")
    fun provideManageBookmarkTool(tool: ManageBookmarkTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("manage_todo")
    fun provideManageTodoTool(tool: ManageTodoTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("manage_xiangqi")
    fun provideManageXiangqiTool(tool: ManageXiangqiTool): AgentTool = tool

    // ========== 浏览器操控工具 ==========

    @Provides
    @IntoMap
    @StringKey("browser_navigate")
    fun provideBrowserNavigateTool(tool: BrowserNavigateTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("browser_read_page")
    fun provideBrowserReadPageTool(tool: BrowserReadPageTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("browser_execute_js")
    fun provideBrowserExecuteJsTool(tool: BrowserExecuteJsTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("browser_screenshot")
    fun provideBrowserScreenshotTool(tool: BrowserScreenshotTool): AgentTool = tool

    // ========== AI 引擎/模型管理工具 ==========

    @Provides
    @IntoMap
    @StringKey("add_local_ai_engine")
    fun provideAddLocalAiEngineTool(tool: AddLocalAiEngineTool): AgentTool = tool

    @Provides
    @IntoMap
    @StringKey("add_local_ai_model")
    fun provideAddLocalAiModelTool(tool: AddLocalAiModelTool): AgentTool = tool
}
