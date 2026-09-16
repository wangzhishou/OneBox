package com.shifenmiao.ai.agent.tool.predicate

/**
 * 端侧（LOCAL_ON_DEVICE）工具白名单。
 *
 * 筛选原则：纯本地执行、无需联网、无需登录，断网/未登录状态下结果依然正确。
 * 只存工具名字符串，与工具实现所在模块解耦。
 *
 * 覆盖四类：
 * - 时间与交互：get_current_time / ask_user
 * - 记忆与笔记：memory_write / memory_get / create_note / view_note / manage_todo
 *   （create_note 依赖 discover_apps，故一并放行，保证依赖闭包完整）
 * - 本地编解码：checksum_tool / base64_tool
 * - 本地文件：workspace_roots / glob_files / grep_files / read_multiple_files / edit_file
 *
 * 明确排除：网络工具（fetch_webpage 等）、浏览器操控、AI 引擎管理、
 * 图像生成/处理（部分走云端或需登录）等。
 */
object OnDeviceToolAllowlist {

    val toolNames: Set<String> = setOf(
        "get_current_time",
        "ask_user",
        "memory_write",
        "memory_get",
        "discover_apps",
        "create_note",
        "view_note",
        "manage_todo",
        "checksum_tool",
        "base64_tool",
        "workspace_roots",
        "glob_files",
        "grep_files",
        "read_multiple_files",
        "edit_file",
    )
}
