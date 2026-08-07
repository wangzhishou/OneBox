package com.shifenmiao.model.file

interface AgentFileService {
    suspend fun workspaceRoots(): AgentFileOperationResult<AgentWorkspaceRootsData>

    suspend fun browseFiles(params: AgentBrowseFilesParams): AgentFileOperationResult<AgentBrowseFilesData>

    suspend fun locateFile(targetUri: String): AgentFileOperationResult<AgentLocateFileData>

    suspend fun globFiles(params: AgentGlobFilesParams): AgentFileOperationResult<AgentGlobFilesData>

    suspend fun grepFiles(params: AgentGrepFilesParams): AgentFileOperationResult<AgentGrepFilesData>

    suspend fun statFile(targetUri: String): AgentFileOperationResult<AgentStatFileData>

    suspend fun readFile(params: AgentReadFileParams): AgentFileOperationResult<AgentReadFileData>

    suspend fun readMultipleFiles(params: AgentReadMultipleFilesParams): AgentFileOperationResult<AgentReadMultipleFilesData>

    suspend fun searchInFile(params: AgentSearchFileParams): AgentFileOperationResult<AgentSearchFileData>

    suspend fun readSearchContext(params: AgentSearchContextParams): AgentFileOperationResult<AgentSearchContextData>

    suspend fun editFile(params: AgentEditFileParams): AgentFileOperationResult<AgentEditFileData>

    suspend fun applyTextPatch(params: AgentApplyTextPatchParams): AgentFileOperationResult<AgentApplyTextPatchData>

    suspend fun applyRangePatch(params: AgentApplyRangePatchParams): AgentFileOperationResult<AgentApplyRangePatchData>

    suspend fun manageFile(params: AgentManageFileParams): AgentFileOperationResult<AgentManageFileData>

    /**
     * 将 content:// / file:// / 普通路径统一解析为本地文件路径。
     *
     * 返回 `file://` URI 字符串；无法解析时返回 null。
     */
    suspend fun resolveContentUriToFile(uri: String): String?
}

