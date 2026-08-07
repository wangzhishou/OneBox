package com.shifenmiao.model.automation

/**
 * Display dimensions used to map AI-returned coordinates (which are based on screenshot
 * pixels) to actual device pixels.
 *
 * Promoted from feature/visual-automation/capturer/ScreenshotCapturer.ScreenSize to
 * core/model so the data type can be referenced by AgentTool responses.
 */
data class ScreenSize(val width: Int, val height: Int)