package com.wanbaohe.media.screen

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavItem
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.media.R
import com.wanbaohe.media.component.MediaPlayerComponent
import com.wanbaohe.media.component.MediaPlayerUiState
import com.wanbaohe.media.component.MediaTab
import com.wanbaohe.media.domain.MediaKind
import com.wanbaohe.media.ui.MediaAddIcon
import com.wanbaohe.media.ui.MediaInfoIcon
import com.wanbaohe.media.ui.MediaLibraryMusicIcon
import com.wanbaohe.media.ui.MediaMoreIcon
import com.wanbaohe.media.ui.MediaMovieIcon
import com.wanbaohe.media.ui.MediaMusicIcon
import com.wanbaohe.media.ui.MediaOpenInNewIcon
import com.wanbaohe.media.ui.MediaShareIcon
import com.wanbaohe.media.ui.MediaVideoLibraryIcon
import com.wanbaohe.media.ui.AudioPlayerContent
import com.wanbaohe.media.ui.MediaInfoDialog
import com.wanbaohe.media.ui.MediaLibraryContent
import com.wanbaohe.media.ui.PlayQueueSheet
import com.wanbaohe.media.ui.PlaybackModeSheet
import com.wanbaohe.media.ui.VideoPlayerContent
import com.wanbaohe.media.ui.VideoStage
import com.wanbaohe.media.ui.videoHasLandscapeDimensions

/**
 * 视频/音乐页面外壳。
 *
 * 一个页面两个 Tab（播放 / 媒体库），底部栏只在普通模式出现；进入画中画后整页只留视频画面，
 * 全屏模式则把视频铺满屏幕并转横屏。
 *
 * 打开入口有三条，最终都落到这里：
 * 1. 文件浏览器/最近文件点音频视频 → ContentRouter → VideoPlayer/AudioPlayer 路由
 * 2. 外部 Intent / 深链 → 同一条路由
 * 3. 媒体库空态/顶栏"+" → 系统选择器（可多选）→ 组件内 takePersistableUriPermission 后播放
 */
@Composable
fun MediaPlayerScreen(component: MediaPlayerComponent) {
    val state by component.uiState.collectAsState()
    val isInPip by component.inPictureInPicture.collectAsState()
    val context = LocalContext.current
    val activity = LocalActivity.current

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) component.onPickedUris(uris)
    }
    val openPicker: () -> Unit = {
        pickMedia.launch(
            arrayOf(if (state.kind == MediaKind.VIDEO) "video/*" else "audio/*")
        )
    }

    // 授权入口：组件置标记，页面负责拉起系统权限框，结果回给组件
    var showPermissionSettings by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.any { it }
        component.onMediaPermissionResult(granted)
        if (!granted) {
            // 永久拒绝（系统不再弹框）时给出"去设置"的出口，否则按钮点了永远没反应
            val permissions = component.requiredPermissions()
            val canAskAgain = activity != null && permissions.any { permission ->
                ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
            }
            if (!canAskAgain) showPermissionSettings = true
        }
    }
    LaunchedEffect(state.isPermissionRequestPending) {
        if (state.isPermissionRequestPending) {
            permissionLauncher.launch(component.requiredPermissions().toTypedArray())
        }
    }

    // Android 13+ 没有通知权限时后台播放没有控制入口，开始播放时补一次申请
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> component.onNotificationPermissionResult(granted) }
    LaunchedEffect(state.isNotificationPermissionPending) {
        if (state.isNotificationPermissionPending) {
            val permission = component.notificationPermission()
            if (permission != null) notificationPermissionLauncher.launch(permission)
            else component.onNotificationPermissionResult(true)
        }
    }

    // 页面被别的页面盖住/离开组合：视频暂停并撤掉画中画登记（音频继续后台放）
    DisposableEffect(Unit) {
        onDispose { component.onScreenLeft() }
    }

    /*
     * Activity 真正不可见时也走一次：Compose 在 Activity stop 后不会马上销毁，
     * 只靠 onDispose 的话按 Home/锁屏后视频的音频会一直响。
     * 画中画进入时 Activity 是 STARTED 而非 STOPPED，不会误暂停小窗里的播放。
     */
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) component.onScreenLeft()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 视频播放时保持亮屏；暂停/停止立刻释放，看列表时不会一直亮着
    val keepScreenOn = state.kind == MediaKind.VIDEO && state.isPlaying
    DisposableEffect(activity, keepScreenOn) {
        if (activity == null || !keepScreenOn) return@DisposableEffect onDispose {}
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // 播放页的返回先回媒体库列表，再按一次才退出；顶栏箭头和系统返回键都走这里
    val goBack: () -> Unit = {
        if (state.tab == MediaTab.PLAYER && state.current != null) {
            component.selectTab(MediaTab.LIBRARY)
        } else {
            component.onGoBack()
        }
    }

    if (isInPip && state.kind == MediaKind.VIDEO) {
        VideoStage(
            component = component,
            state = state,
            durationMs = state.playback.durationMs.takeIf { it > 0L } ?: state.metadata.durationMs,
            modifier = Modifier.fillMaxSize(),
            showControls = false,
        )
        return
    }

    if (state.isFullscreen && state.kind == MediaKind.VIDEO) {
        // 全屏时系统返回键先退出全屏，而不是直接离开播放页
        BackHandler { component.setFullscreen(false) }
        FullscreenVideo(
            component = component,
            state = state,
            onExit = { component.setFullscreen(false) },
        )
        return
    }

    BaseScreen(
        title = stringResource(titleOf(state)),
        onGoBack = goBack,
        isShowDefaultActions = false,
        showNavigationBarsPadding = false,
        actions = {
            TopBarActions(
                state = state,
                onAddFile = openPicker,
                onShare = component::share,
                onInfo = { component.showInfo(true) },
                onOpenWith = component::openWithOtherApp,
                onClearRecent = { component.showClearRecentDialog(true) },
                onStopPlayback = component::stopPlayback,
            )
        },
        content = {
            Box(modifier = Modifier.weight(1f)) {
                when (state.tab) {
                    MediaTab.PLAYER -> if (state.kind == MediaKind.VIDEO) {
                        VideoPlayerContent(
                            component = component,
                            state = state,
                            onAddFile = openPicker,
                        )
                    } else {
                        AudioPlayerContent(
                            component = component,
                            state = state,
                            onAddFile = openPicker,
                        )
                    }

                    MediaTab.LIBRARY -> MediaLibraryContent(
                        component = component,
                        state = state,
                        onAddFile = openPicker,
                    )
                }
            }

            MediaBottomTabs(
                kind = state.kind,
                selected = state.tab,
                onSelect = component::selectTab,
            )
        },
    )

    PlaybackModeSheet(
        visible = state.showModeSheet,
        current = state.mode,
        onSelect = component::setMode,
        onDismiss = { component.showModeSheet(false) },
    )

    PlayQueueSheet(
        visible = state.showQueueSheet,
        queue = state.queue,
        currentUri = state.current?.uri,
        onSelect = { entry ->
            component.play(entry)
            component.showQueue(false)
        },
        onDismiss = { component.showQueue(false) },
    )

    if (state.showInfoDialog) {
        MediaInfoDialog(
            state = state,
            onDismiss = { component.showInfo(false) },
        )
    }

    if (showPermissionSettings) {
        AlertDialog(
            onDismissRequest = { showPermissionSettings = false },
            title = { Text(stringResource(R.string.media_permission_settings_title)) },
            text = { Text(stringResource(R.string.media_permission_settings_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionSettings = false
                        runCatching {
                            context.startActivity(
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                            )
                        }
                    }
                ) {
                    Text(stringResource(R.string.media_open_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionSettings = false }) {
                    Text(stringResource(R.string.media_cancel))
                }
            },
        )
    }

    if (state.showClearRecentDialog) {
        AlertDialog(
            onDismissRequest = { component.showClearRecentDialog(false) },
            title = { Text(stringResource(R.string.media_clear_recent_confirm_title)) },
            text = { Text(stringResource(R.string.media_clear_recent_confirm_message)) },
            confirmButton = {
                TextButton(onClick = component::clearRecent) {
                    Text(stringResource(R.string.media_clear_recent_confirm_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { component.showClearRecentDialog(false) }) {
                    Text(stringResource(R.string.media_cancel))
                }
            },
        )
    }
}

/**
 * 全屏视频：隐藏系统栏，只留画面和浮层控件。
 *
 * 只有横屏素材才转横屏；竖屏视频保持用户当前方向，画面按比例居中留边，
 * 不再把 9:16 的竖屏视频掰成横屏再横向拉满。退出全屏/页面销毁时恢复方向与系统栏，
 * 避免把整个 App 留在横屏或隐藏状态栏。
 */
@Composable
private fun FullscreenVideo(
    component: MediaPlayerComponent,
    state: MediaPlayerUiState,
    onExit: () -> Unit,
) {
    val activity = LocalActivity.current
    // 尺寸未知时也不强制旋转：等 onVideoSizeChanged 回来再决定，避免一点全屏就乱转
    val forceLandscape = videoHasLandscapeDimensions(state)
    DisposableEffect(activity, forceLandscape) {
        if (activity == null) return@DisposableEffect onDispose {}
        val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        activity.requestedOrientation = if (forceLandscape) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
        onDispose {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        VideoStage(
            component = component,
            state = state,
            durationMs = state.playback.durationMs.takeIf { it > 0L } ?: state.metadata.durationMs,
            modifier = Modifier.fillMaxSize(),
            fullscreen = true,
            // 退出入口统一放在左上角常驻的 X；画面里的切换键在全屏时不再重复出现
            onToggleFullscreen = null,
        )
        IconButton(
            onClick = onExit,
            modifier = Modifier
                .align(Alignment.TopStart)
                .navigationBarsPadding()
                .padding(8.dp)
                .size(40.dp)
                .background(Color.Black.copy(alpha = 0.35f), androidx.compose.foundation.shape.CircleShape),
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.media_exit_fullscreen),
                tint = Color.White,
            )
        }
    }
}

/** 顶栏标题：库页用"视频库/音乐库"，播放页用"视频播放/正在播放"。 */
@StringRes
private fun titleOf(state: MediaPlayerUiState): Int = when {
    state.tab == MediaTab.LIBRARY && state.kind == MediaKind.VIDEO -> R.string.media_video_library_title
    state.tab == MediaTab.LIBRARY -> R.string.media_audio_library_title
    state.kind == MediaKind.VIDEO -> R.string.media_video_player_title
    else -> R.string.media_audio_player_title
}

/**
 * 顶栏右侧动作。
 *
 * 库页只留"添加文件 + 更多"（列表已按拍摄时间分组，搜索入口暂时移除），
 * 播放页给"分享 + 更多"；两套入口不混在一起。
 */
@Composable
private fun TopBarActions(
    state: MediaPlayerUiState,
    onAddFile: () -> Unit,
    onShare: () -> Unit,
    onInfo: () -> Unit,
    onOpenWith: () -> Unit,
    onClearRecent: () -> Unit,
    onStopPlayback: () -> Unit,
) {
    if (state.tab == MediaTab.LIBRARY) {
        IconButton(onClick = onAddFile) {
            Icon(
                imageVector = MediaAddIcon,
                contentDescription = stringResource(R.string.media_add_file),
            )
        }
    } else {
        IconButton(onClick = onShare, enabled = state.current != null) {
            Icon(
                imageVector = MediaShareIcon,
                contentDescription = stringResource(R.string.media_share),
            )
        }
    }

    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = MediaMoreIcon,
                contentDescription = stringResource(R.string.media_more),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (state.tab == MediaTab.PLAYER) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.media_file_info)) },
                    leadingIcon = {
                        Icon(MediaInfoIcon, contentDescription = null)
                    },
                    onClick = {
                        expanded = false
                        onInfo()
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.media_open_with)) },
                    leadingIcon = {
                        Icon(MediaOpenInNewIcon, contentDescription = null)
                    },
                    onClick = {
                        expanded = false
                        onOpenWith()
                    },
                )
                if (state.current != null) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.media_stop_playback)) },
                        onClick = {
                            expanded = false
                            onStopPlayback()
                        },
                    )
                }
            } else {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.media_clear_recent)) },
                    onClick = {
                        expanded = false
                        onClearRecent()
                    },
                )
            }
        }
    }
}

/** 底部两个 Tab：播放 / 媒体库。 */
@Composable
private fun MediaBottomTabs(
    kind: MediaKind,
    selected: MediaTab,
    onSelect: (MediaTab) -> Unit,
) {
    val items = listOf(
        BottomNavItem(
            id = MediaTab.PLAYER.name,
            label = stringResource(
                if (kind == MediaKind.VIDEO) R.string.media_tab_video else R.string.media_tab_play
            ),
            icon = if (kind == MediaKind.VIDEO) {
                MediaMovieIcon
            } else {
                MediaMusicIcon
            },
        ),
        BottomNavItem(
            id = MediaTab.LIBRARY.name,
            label = stringResource(
                if (kind == MediaKind.VIDEO) {
                    R.string.media_tab_video_library
                } else {
                    R.string.media_tab_music_library
                }
            ),
            icon = if (kind == MediaKind.VIDEO) {
                MediaVideoLibraryIcon
            } else {
                MediaLibraryMusicIcon
            },
        ),
    )

    BottomNavigationBar(
        items = items,
        selectedItemId = selected.name,
        onItemClick = { item ->
            MediaTab.entries.firstOrNull { it.name == item.id }?.let(onSelect)
        },
        modifier = Modifier.fillMaxWidth(),
    )
}
