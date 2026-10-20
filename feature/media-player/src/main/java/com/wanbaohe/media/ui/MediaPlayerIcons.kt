package com.wanbaohe.media.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * 媒体页自己的 Material 图标集（24dp 网格）。
 *
 * 全站默认的 `Line*` 图标是字体批量生成的，播放/暂停、上一首/下一首、分享、收藏
 * 这些高频控件在小尺寸下笔画粗细和视觉重心都不太对。这里按 Material 官方 24dp
 * 网格手写/收录常用路径，只在本模块使用；`Icon(tint=...)` 依然负责着色。
 */
private fun materialIcon(
    name: String,
    pathData: String,
    fillType: PathFillType = PathFillType.NonZero,
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).addPath(
    pathData = PathParser().parsePathString(pathData).toNodes(),
    pathFillType = fillType,
    fill = SolidColor(Color.Black),
).build()

// ── 播放控制 ─────────────────────────────────────────────────────────────

internal val MediaPlayIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    materialIcon("MediaPlay", "M8 5v14l11-7z")
}

internal val MediaPauseIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    materialIcon("MediaPause", "M6 19h4V5H6v14zm8-14v14h4V5h-4z")
}

internal val MediaSkipPreviousIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    materialIcon("MediaSkipPrevious", "M6 6h2v12H6zm3.5 6l8.5 6V6z")
}

internal val MediaSkipNextIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    materialIcon("MediaSkipNext", "M6 18l8.5-6L6 6v12zM16 6v12h2V6h-2z")
}

internal val MediaFullscreenIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    materialIcon(
        "MediaFullscreen",
        "M7 14H5v5h5v-2H7v-3zm-2-4h2V7h3V5H5v5zm12 7h-3v2h5v-5h-2v3zM14 5v2h3v3h2V5h-5z",
    )
}

internal val MediaFullscreenExitIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    materialIcon(
        "MediaFullscreenExit",
        "M5 16h3v3h2v-5H5v2zm3-8H5v2h5V5H8v3zm6 11h2v-3h3v-2h-5v5zm2-11V5h-2v5h5V8h-3z",
    )
}

// ── 操作 ─────────────────────────────────────────────────────────────────

internal val MediaFavoriteIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    materialIcon(
        "MediaFavorite",
        "M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3" +
            "c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5" +
            "c0 3.78-3.4 6.86-8.55 11.54L12 21.35z",
    )
}

internal val MediaFavoriteBorderIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    materialIcon(
        "MediaFavoriteBorder",
        "M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5" +
            "c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5" +
            " 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5" +
            " 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5" +
            "c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z",
        fillType = PathFillType.EvenOdd,
    )
}

internal val MediaShareIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    materialIcon(
        "MediaShare",
        "M18 16.08c-.76 0-1.44.3-1.96.77L8.91 12.7c.05-.23.09-.46.09-.7s-.04-.47-.09-.7" +
            "l7.05-4.11c.54.5 1.25.81 2.04.81 1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3" +
            "c0 .24.04.47.09.7L8.04 9.81C7.5 9.31 6.79 9 6 9c-1.66 0-3 1.34-3 3s1.34 3 3 3" +
            "c.79 0 1.5-.31 2.04-.81l7.12 4.16c-.05.21-.08.43-.08.65 0 1.61 1.31 2.92 2.92 2.92" +
            " 1.61 0 2.92-1.31 2.92-2.92s-1.31-2.92-2.92-2.92z",
    )
}

internal val MediaInfoIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    materialIcon(
        "MediaInfo",
        "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6z" +
            "m0-8h-2V7h2v2z",
        fillType = PathFillType.EvenOdd,
    )
}

internal val MediaOpenInNewIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    materialIcon(
        "MediaOpenInNew",
        "M19 19H5V5h7V3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2v-7h-2v7z" +
            "M14 3v2h3.59l-9.83 9.83 1.41 1.41L19 6.41V10h2V3h-7z",
    )
}

internal val MediaMoreIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    materialIcon(
        "MediaMore",
        "M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2" +
            " 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z",
    )
}

internal val MediaAddIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    materialIcon("MediaAdd", "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z")
}

// ── 媒体类型 / 列表 ───────────────────────────────────────────────────────

internal val MediaMovieIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    materialIcon(
        "MediaMovie",
        "M18 4l2 4h-3l-2-4h-2l2 4h-3l-2-4H8l2 4H7L5 4H4c-1.1 0-1.99.9-1.99 2L2 18" +
            "c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V4h-4z",
    )
}

internal val MediaMusicIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    materialIcon(
        "MediaMusic",
        "M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z",
    )
}

internal val MediaVideoLibraryIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    materialIcon(
        "MediaVideoLibrary",
        "M4 6H2v14c0 1.1.9 2 2 2h14v-2H4V6zm16-4H8c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12" +
            "c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-8 12.5v-9l6 4.5-6 4.5z",
    )
}

internal val MediaLibraryMusicIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    materialIcon(
        "MediaLibraryMusic",
        "M20 2H8c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z" +
            "m-2 5h-3v5.5c0 1.38-1.12 2.5-2.5 2.5S10 13.88 10 12.5s1.12-2.5 2.5-2.5" +
            "c.57 0 1.08.19 1.5.51V5h4v2zM4 6H2v14c0 1.1.9 2 2 2h14v-2H4V6z",
    )
}

internal val MediaQueueIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    materialIcon(
        "MediaQueue",
        "M15 6H3v2h12V6zm0 4H3v2h12v-2zM3 16h8v-2H3v2zM17 6v8.18c-.31-.11-.65-.18-1-.18" +
            "-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3V8h3V6h-5z",
    )
}

internal val MediaFolderIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    materialIcon(
        "MediaFolder",
        "M10 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2" +
            "h-8l-2-2z",
    )
}

// ── 播放模式 / 其它 ───────────────────────────────────────────────────────

internal val MediaRepeatIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    materialIcon(
        "MediaRepeat",
        "M7 7h10v3l4-4-4-4v3H5v6h2V7zm10 10H7v-3l-4 4 4 4v-3h12v-6h-2v4z",
    )
}

internal val MediaRepeatOneIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    materialIcon(
        "MediaRepeatOne",
        "M7 7h10v3l4-4-4-4v3H5v6h2V7zm10 10H7v-3l-4 4 4 4v-3h12v-6h-2v4zm-4-2V9h-1l-2 1v1" +
            "h1.5v4H13z",
    )
}

internal val MediaShuffleIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    materialIcon(
        "MediaShuffle",
        "M10.59 9.17L5.41 4 4 5.41l5.17 5.17 1.42-1.41zM14.5 4l2.04 2.04L4 18.59 5.41 20" +
            " 17.96 7.46 20 9.5V4h-5.5zm.33 9.41l-1.41 1.41 3.13 3.13L14.5 20H20v-5.5" +
            "l-2.04 2.04-3.13-3.13z",
    )
}

internal val MediaChevronRightIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    materialIcon("MediaChevronRight", "M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z")
}

internal val MediaCalendarIcon: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    materialIcon(
        "MediaCalendar",
        "M20 3h-1V1h-2v2H7V1H5v2H4c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V5" +
            "c0-1.1-.9-2-2-2zm0 18H4V8h16v13z",
    )
}
