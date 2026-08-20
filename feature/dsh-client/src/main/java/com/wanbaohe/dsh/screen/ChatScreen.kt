package com.wanbaohe.dsh.screen

import android.content.Intent
import android.graphics.BitmapFactory
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.utils.fileProviderAuthority
import com.wanbaohe.dsh.R
import com.wanbaohe.dsh.component.ChatUiState
import com.wanbaohe.dsh.component.DshRootComponent
import com.wanbaohe.dsh.component.DshUiState
import com.wanbaohe.dsh.connection.ConnectionPhase
import com.wanbaohe.dsh.screen.node.ChatNodeItem
import com.wanbaohe.dsh.screen.node.rememberMarkdownParser
import com.wanbaohe.dsh.session.AllowedImageMediaTypes
import com.wanbaohe.dsh.session.AttachmentRejectException
import com.wanbaohe.dsh.session.AttachmentRejection
import com.wanbaohe.dsh.session.ChatNode
import com.wanbaohe.dsh.session.CommandMenu
import com.wanbaohe.dsh.session.CommandMenuItem
import com.wanbaohe.dsh.session.FeedbackStore
import com.wanbaohe.dsh.session.PendingImage
import com.wanbaohe.dsh.session.SessionStore
import com.wanbaohe.dsh.session.SubagentCatalogPhase
import com.wanbaohe.dsh.session.extractNodes
import com.wanbaohe.dsh.session.filterMenu
import com.wanbaohe.dsh.session.readPendingImage
import com.wanbaohe.dsh.session.validateImages
import com.wanbaohe.dsh.wire.model.SessionSearchValue
import com.wanbaohe.dsh.wire.model.SessionSummary
import com.wanbaohe.dsh.wire.model.SubagentListEntry
import com.wanbaohe.dsh.wire.model.WorkspaceView
import com.wanbaohe.dsh.wire.model.displayTitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject

/**
 * DSH 聊天页(P5a 功能面):抽屉会话列表(搜索/长按菜单)+ 会话流节点渲染 +
 * 附件预览 + composer(mode 切换 / 停止 / 斜杠命令),顶栏挂模型选择器与 goal 面板入口。
 * 返回键回连接页,连接保持不断。
 */
@Composable
fun ChatScreen(component: DshRootComponent) {
    val uiState by component.uiState.collectAsState()
    val chatState by component.chatState.collectAsState()
    val bundle by component.chatBundle.collectAsState()
    val activeBundle = bundle
    if (activeBundle == null) {
        // 防御:无连接实例时不渲染聊天页(正常路径 page=Chat 时 bundle 必在)
        LaunchedEffect(Unit) { component.backToConnect() }
        return
    }

    val context = LocalContext.current
    val summaries by activeBundle.sessionStore.summaries.collectAsState()
    val workspaces by activeBundle.workspaceStore.workspaces.collectAsState()
    val archivedIds by activeBundle.workspaceStore.archivedSessionIds.collectAsState()
    val jobsBySession by activeBundle.queueStore.jobs.collectAsState()
    val subagentDescendants by activeBundle.subagentStore.descendants.collectAsState()
    val subagentCatalogs by activeBundle.subagentStore.catalogs.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val selected = summaries.firstOrNull { it.sessionId == chatState.selectedSessionId }

    // P4 弹层状态:模型选择器 / skill 弹层 / goal 面板 / 重命名对话框
    var showModelPicker by remember { mutableStateOf(false) }
    var showSkillSheet by remember { mutableStateOf(false) }
    var showCommandSheet by remember { mutableStateOf(false) }
    var goalPanelOpen by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<SessionSummary?>(null) }
    // P5b 弹层/页面状态:jobs / subagent / settings / 轨迹 / workspace 对话框
    var showJobsSheet by remember { mutableStateOf(false) }
    var showSubagentSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showTrajectory by remember { mutableStateOf(false) }
    var createWorkspaceOpen by remember { mutableStateOf(false) }
    var renameWorkspaceTarget by remember { mutableStateOf<WorkspaceView?>(null) }
    var deleteWorkspaceTarget by remember { mutableStateOf<WorkspaceView?>(null) }

    // 轨迹视图是整页覆盖:替换聊天页内容(数据 = SessionLog 快照,零新 RPC)
    val trajectorySessionId = chatState.selectedSessionId
    if (showTrajectory && trajectorySessionId != null) {
        TrajectoryPage(
            sessionId = trajectorySessionId,
            sessionStore = activeBundle.sessionStore,
            onLoadOlder = component::loadOlderHistory,
            onClose = { showTrajectory = false }
        )
        return
    }

    // 导出完成 → FileProvider 分享(应用私有目录,系统分享/保存由用户选)
    LaunchedEffect(Unit) {
        component.exportedZip.collect { file ->
            runCatching {
                val uri = FileProvider.getUriForFile(
                    context,
                    context.fileProviderAuthority,
                    file
                )
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "application/zip"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(
                    Intent.createChooser(send, context.getString(R.string.dsh_export_share))
                )
            }
        }
    }

    // 选图(PickVisualMedia 无需新权限):intake 读字节 + 纯头部尺寸探测 + 本地预拒
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            val newImages = mutableListOf<PendingImage>()
            var rejection: AttachmentRejection? = null
            for (uri in uris) {
                val mediaType = context.contentResolver.getType(uri)?.lowercase()
                if (mediaType == null || mediaType !in AllowedImageMediaTypes) {
                    rejection = AttachmentRejection.UnsupportedType(mediaType ?: "unknown")
                    continue
                }
                try {
                    readPendingImage(context, uri)?.let { newImages.add(it) }
                } catch (e: AttachmentRejectException) {
                    rejection = e.rejection
                }
            }
            // 合并既有待发后整体验证;imageLimits 投影缺席则跳过预检(服务端权威)
            val combined = chatState.attachments + newImages
            val limits = chatState.selectedSessionId
                ?.let(activeBundle.sessionStore::attachmentLimitsFor)
            val finalRejection = limits?.let { validateImages(combined, it) } ?: rejection
            withContext(Dispatchers.Main) {
                if (finalRejection != null) {
                    component.showChatError(attachmentRejectionText(context, finalRejection))
                } else {
                    component.stageAttachments(newImages)
                }
            }
        }
    }

    // 会话抽屉从右侧滑出:外层 RTL 使抽屉锚定 end 侧,抽屉与主内容各自恢复 LTR
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            // 抽屉在屏幕右侧:圆角应在朝屏幕中间的一侧(此处已恢复 LTR,即 start 边)
            ModalDrawerSheet(
                drawerShape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
            ) {
                SessionDrawer(
                    summaries = summaries,
                    workspaces = workspaces,
                    archivedIds = archivedIds,
                    selectedId = chatState.selectedSessionId,
                    creating = chatState.creating,
                    onSearch = component::searchSessions,
                    onSelect = {
                        component.selectSession(it)
                        scope.launch { drawerState.close() }
                    },
                    onCreate = { workspaceId ->
                        component.createSession(workspaceId)
                        scope.launch { drawerState.close() }
                    },
                    onFork = {
                        component.forkSession(it)
                        scope.launch { drawerState.close() }
                    },
                    onExport = component::exportSession,
                    onRename = { renameTarget = it },
                    onArchive = {
                        component.archiveSession(it)
                        scope.launch { drawerState.close() }
                    },
                    onCreateWorkspace = { createWorkspaceOpen = true },
                    onRenameWorkspace = { renameWorkspaceTarget = it },
                    onDeleteWorkspace = { deleteWorkspaceTarget = it }
                )
            }
            }
        }
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        // 顶栏标题:主机名(describe cwd 目录名;缺省回退会话名/页标题)+ 就绪绿色对勾徽章
        val hostTitle = uiState.snapshot.describe?.cwd
            ?.substringAfterLast('/')
            ?.takeIf { it.isNotBlank() }
            ?: selected?.let { sessionDisplayName(it) }
            ?: stringResource(R.string.dsh_chat_title)
        val isReady = uiState.snapshot.phase == ConnectionPhase.Ready
        BaseScreen(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = hostTitle,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isReady) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = DshSuccessGreen
                        )
                    }
                }
            },
            onGoBack = component::backToConnect,
            // composable title 重载的 showNavigationBarsPadding 默认 false,聊天页显式打开
            showNavigationBarsPadding = true,
            actions = {
                if (chatState.exporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                if (selected != null) {
                    // jobs 入口:activeJobCount 角标;无任务完全不渲染
                    val selectedJobs = jobsBySession[selected.sessionId].orEmpty()
                    if (selectedJobs.isNotEmpty()) {
                        val activeCount = activeBundle.queueStore.activeJobCount(selected.sessionId)
                        IconButton(onClick = { showJobsSheet = true }) {
                            if (activeCount > 0) {
                                BadgedBox(badge = { Badge { Text("$activeCount") } }) {
                                    Icon(
                                        imageVector = Icons.Outlined.WorkOutline,
                                        contentDescription = stringResource(R.string.dsh_jobs_entry)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.WorkOutline,
                                    contentDescription = stringResource(R.string.dsh_jobs_entry)
                                )
                            }
                        }
                    }
                    // subagent 入口:证据驱动可见性(目录 error / 有行 / 后代聚合>0)
                    val descendants = subagentDescendants[selected.sessionId]
                    val catalog = subagentCatalogs[selected.sessionId]
                    val subagentVisible = (descendants?.count ?: 0) > 0 ||
                        (catalog != null &&
                            (catalog.phase == SubagentCatalogPhase.Error || catalog.entries.isNotEmpty()))
                    if (subagentVisible) {
                        IconButton(onClick = { showSubagentSheet = true }) {
                            val runningCount = descendants?.runningCount ?: 0
                            if (runningCount > 0) {
                                BadgedBox(badge = { Badge { Text("$runningCount") } }) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccountTree,
                                        contentDescription = stringResource(R.string.dsh_subagent_entry)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.AccountTree,
                                    contentDescription = stringResource(R.string.dsh_subagent_entry)
                                )
                            }
                        }
                    }
                    IconButton(onClick = { showModelPicker = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Psychology,
                            contentDescription = stringResource(R.string.dsh_model_picker)
                        )
                    }
                    IconButton(onClick = { goalPanelOpen = !goalPanelOpen }) {
                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = stringResource(R.string.dsh_goal_panel)
                        )
                    }
                    IconButton(onClick = { showTrajectory = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Timeline,
                            contentDescription = stringResource(R.string.dsh_trajectory_entry)
                        )
                    }
                }
                // settings/credentials/llm 特权面:loopback(或预留的网关远程)可见;LAN 隐藏
                if (uiState.privilegeScope.showPrivilegedPanels) {
                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.dsh_settings_entry)
                        )
                    }
                }
                ConnectionBadge(uiState)
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = stringResource(R.string.dsh_open_sessions)
                    )
                }
            }
        ) {
            // 一次性错误横幅(发送/历史/创建失败)
            chatState.error?.let { error ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                    TextButton(onClick = component::dismissChatError) {
                        Text(stringResource(R.string.dsh_dismiss))
                    }
                }
            }

            // goal 面板:有 goal 投影时常驻;无目标时由顶栏入口打开(只露「新建目标」)
            val goalProjection = selected?.projections?.values?.get("goal") as? JsonObject
            if (goalProjection != null || goalPanelOpen) {
                GoalPanel(
                    projectionValue = goalProjection,
                    busy = selected?.running == true,
                    component = component,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            MessageList(
                component = component,
                chatState = chatState,
                hasSelected = selected != null,
                sessionStore = activeBundle.sessionStore,
                feedbackStore = activeBundle.feedbackStore,
                modifier = Modifier.weight(1f)
            )

            // P3 交互区:审批卡 + 问答表单 + 队列 Dock(固定在输入框上方)
            val approvals by activeBundle.interactorStore.approvals.collectAsState()
            val questions by activeBundle.interactorStore.questions.collectAsState()
            val queues by activeBundle.queueStore.queues.collectAsState()
            val badResponseText = stringResource(R.string.dsh_respond_bad_response)
            InteractorSection(
                approvals = approvals,
                questions = questions,
                queueItems = chatState.selectedSessionId?.let { queues[it] }.orEmpty(),
                currentSessionId = chatState.selectedSessionId,
                onApprovalRespond = { approval, allow ->
                    scope.launch {
                        val receipt = component.respondApproval(approval, allow)
                        if (receipt?.isMalformed == true) {
                            component.showChatError(badResponseText)
                        }
                    }
                },
                onQuestionSubmit = { pending, drafts ->
                    component.submitQuestionAnswers(pending, drafts)
                },
                onQueueRemove = { item ->
                    item.id?.let(component::deleteQueueItem)
                }
            )

            // 运行中 busy 状态(mode:queue 仍可发送,服务端排队);停止钮在 composer 行
            if (selected?.running == true) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.dsh_running),
                        fontSize = 12.sp,
                        color = AppTheme.colors.getOnInactiveContainerColor()
                    )
                }
            }

            // 待发图片预览条(intake 已完成本地预拒;此处只展示与移除)
            if (chatState.attachments.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chatState.attachments.forEachIndexed { index, image ->
                        AttachmentThumbnail(
                            image = image,
                            onRemove = { component.removeAttachment(index) }
                        )
                    }
                }
            }

            // 斜杠命令内联面板:输入以 '/' 开头时浮现在输入框上方(目录内模糊过滤)
            val slashQuery = chatState.input.trimStart()
                .let { if (it.startsWith("/")) it.drop(1).trim() else null }
            if (slashQuery != null && selected != null) {
                SlashCommandPanel(
                    sessionId = selected.sessionId,
                    query = slashQuery,
                    component = component,
                    onPick = { item ->
                        component.pickCommandItem(
                            selected.sessionId,
                            item.name,
                            item.hint,
                            item.isCommand
                        )
                    }
                )
            }

            // 底部输入条(Glass 风格):leadingIcon = 加号(图片/技能/命令菜单);
            // trailingIcon = [运行中停止钮] + 「排队/插话」切换 + primary 发送钮;
            // 输入框 fillMaxWidth 占满屏宽,框外不再放独立按钮
            var addMenuOpen by remember { mutableStateOf(false) }
            val canSend = (chatState.input.isNotBlank() || chatState.attachments.isNotEmpty()) &&
                selected != null && !chatState.sending
            GlassOutlinedTextField(
                value = chatState.input,
                onValueChange = component::onChatInputChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.dsh_input_hint)) },
                maxLines = 4,
                enabled = selected != null,
                leadingIcon = {
                    // 加号入口(图片/技能/命令)
                    Box {
                        IconButton(
                            onClick = { addMenuOpen = true },
                            enabled = selected != null
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.dsh_add_entry),
                                tint = if (selected != null) {
                                    AppTheme.colors.getPrimaryColor()
                                } else {
                                    AppTheme.colors.getOnInactiveContainerColor()
                                }
                            )
                        }
                        DropdownMenu(
                            expanded = addMenuOpen,
                            onDismissRequest = { addMenuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.dsh_pick_image)) },
                                onClick = {
                                    addMenuOpen = false
                                    imagePicker.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.dsh_pick_skill)) },
                                onClick = {
                                    addMenuOpen = false
                                    showSkillSheet = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.dsh_command_menu)) },
                                onClick = {
                                    addMenuOpen = false
                                    showCommandSheet = true
                                }
                            )
                        }
                    }
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 运行中停止按钮(session.cancel;只中止当前 turn,保留 pending inbox)
                        if (selected?.running == true) {
                            IconButton(onClick = component::cancelCurrentTurn) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = stringResource(R.string.dsh_stop_turn),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        // 发送模式切换:queue(排队)| steer(插话进运行中轮次;服务端仍可拒)
                        TextButton(
                            onClick = component::togglePromptMode,
                            enabled = selected != null
                        ) {
                            Text(
                                text = stringResource(
                                    if (chatState.promptMode == SessionStore.PromptModeSteer) {
                                        R.string.dsh_mode_steer
                                    } else {
                                        R.string.dsh_mode_queue
                                    }
                                ),
                                fontSize = 12.sp,
                                color = if (selected != null) {
                                    AppTheme.colors.getPrimaryColor()
                                } else {
                                    AppTheme.colors.getOnInactiveContainerColor()
                                }
                            )
                        }
                        // primary 填充圆角发送按钮
                        FilledIconButton(
                            onClick = component::sendMessage,
                            enabled = canSend,
                            modifier = Modifier.size(34.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = stringResource(R.string.dsh_send),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                }
            )
        }
        }
    }
    }

    // P4 弹层:模型选择器 / skill 弹层 / 重命名对话框
    if (showModelPicker && selected != null) {
        ModelPickerDialog(
            sessionId = selected.sessionId,
            component = component,
            onDismiss = { showModelPicker = false }
        )
    }
    if (showSkillSheet && selected != null) {
        SkillSheet(
            sessionId = selected.sessionId,
            component = component,
            onDismiss = { showSkillSheet = false }
        )
    }
    if (showCommandSheet && selected != null) {
        CommandMenuSheet(
            sessionId = selected.sessionId,
            loadMenu = component::loadCommandMenu,
            onPick = { item ->
                showCommandSheet = false
                component.pickCommandItem(
                    selected.sessionId,
                    item.name,
                    item.hint,
                    item.isCommand
                )
            },
            onDismiss = { showCommandSheet = false }
        )
    }
    renameTarget?.let { target ->
        RenameSessionDialog(
            target = target,
            onDismiss = { renameTarget = null },
            onConfirm = { title ->
                component.renameSession(target.sessionId, title)
                renameTarget = null
            }
        )
    }
    // P5b 弹层:jobs / subagent / settings / workspace 对话框
    if (showJobsSheet && selected != null) {
        JobsSheet(
            sessionId = selected.sessionId,
            queueStore = activeBundle.queueStore,
            onDismiss = { showJobsSheet = false }
        )
    }
    if (showSubagentSheet && selected != null) {
        SubagentSheet(
            sessionId = selected.sessionId,
            store = activeBundle.subagentStore,
            onDismiss = { showSubagentSheet = false }
        )
    }
    if (showSettingsSheet) {
        SettingsSheet(
            store = activeBundle.settingsStore,
            onDismiss = { showSettingsSheet = false }
        )
    }
    if (createWorkspaceOpen) {
        CreateWorkspaceDialog(
            onDismiss = { createWorkspaceOpen = false },
            onConfirm = { path ->
                component.createWorkspace(path)
                createWorkspaceOpen = false
            }
        )
    }
    renameWorkspaceTarget?.let { target ->
        RenameWorkspaceDialog(
            target = target,
            onDismiss = { renameWorkspaceTarget = null },
            onConfirm = { title ->
                component.renameWorkspace(target.workspaceId, title)
                renameWorkspaceTarget = null
            }
        )
    }
    deleteWorkspaceTarget?.let { target ->
        DeleteWorkspaceDialog(
            target = target,
            onDismiss = { deleteWorkspaceTarget = null },
            onConfirm = {
                component.deleteWorkspace(target.workspaceId)
                deleteWorkspaceTarget = null
            }
        )
    }
}

/** 附件本地预拒文案(intake 路径;send 路径的防御性复检文案在组件层) */
private fun attachmentRejectionText(
    context: android.content.Context,
    rejection: AttachmentRejection
): String = when (rejection) {
    is AttachmentRejection.TooMany ->
        context.getString(R.string.dsh_attach_too_many, rejection.count, rejection.max)

    is AttachmentRejection.UnsupportedType ->
        context.getString(R.string.dsh_attach_unsupported, rejection.mediaType)

    is AttachmentRejection.SingleTooLarge ->
        context.getString(
            R.string.dsh_attach_single_too_large,
            Formatter.formatShortFileSize(context, rejection.bytes),
            Formatter.formatShortFileSize(context, rejection.max)
        )

    is AttachmentRejection.PixelsTooLarge ->
        context.getString(R.string.dsh_attach_pixels_too_large, rejection.pixels, rejection.max)

    is AttachmentRejection.AggregateTooLarge ->
        context.getString(
            R.string.dsh_attach_aggregate_too_large,
            Formatter.formatShortFileSize(context, rejection.bytes),
            Formatter.formatShortFileSize(context, rejection.max)
        )
}

/** 待发图片缩略图(采样解码,不整读像素);右上角移除钮 */
@Composable
private fun AttachmentThumbnail(image: PendingImage, onRemove: () -> Unit) {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, image) {
        value = withContext(Dispatchers.Default) { decodeThumbnail(image.bytes) }
    }
    Box {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!,
                contentDescription = image.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.dsh_attachment_remove),
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(18.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                .clickable(onClick = onRemove)
                .padding(2.dp)
        )
    }
}

/** 采样解码缩略图(目标约 112px,只读必要像素) */
private fun decodeThumbnail(bytes: ByteArray): ImageBitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    var sample = 1
    while (bounds.outWidth / (sample * 2) >= ThumbnailTargetPx &&
        bounds.outHeight / (sample * 2) >= ThumbnailTargetPx
    ) {
        sample *= 2
    }
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)?.asImageBitmap()
}

private const val ThumbnailTargetPx = 112

/** 重命名对话框(session.rename;规范化 title+seq 先落本地格) */
@Composable
private fun RenameSessionDialog(
    target: SessionSummary,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var title by rememberSaveable(target.sessionId) {
        mutableStateOf(target.displayTitle().orEmpty())
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dsh_rename_title)) },
        text = {
            GlassOutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.dsh_rename_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title.trim()) },
                enabled = title.isNotBlank()
            ) {
                Text(stringResource(R.string.dsh_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dsh_cancel))
            }
        }
    )
}

/** 顶部连接状态徽章:阶段色圆点 + 文案(就绪 = 绿点「已连接」) */
@Composable
private fun ConnectionBadge(uiState: DshUiState) {
    val (textRes, color) = when (uiState.snapshot.phase) {
        ConnectionPhase.Connecting ->
            R.string.dsh_status_connecting to AppTheme.colors.getOnInactiveContainerColor()
        ConnectionPhase.Ready ->
            R.string.dsh_status_ready to DshSuccessGreen
        ConnectionPhase.Down ->
            R.string.dsh_status_down to MaterialTheme.colorScheme.error
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = stringResource(textRes),
            color = color,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}

/** 成功绿(在线状态/成功行专用,深浅色主题下同一色值,沿用全仓 0xFF4CAF50 惯例) */
internal val DshSuccessGreen = androidx.compose.ui.graphics.Color(0xFF4CAF50)

/**
 * 斜杠命令内联面板(输入以 '/' 开头时浮现):命令目录(commands/list)+ 技能合并,
 * fuzzy 过滤(前缀优先 + 子序列),最多露 6 行;目录降级时给一行轻提示。
 * 点击派发与命令菜单同一决策(leadingInput 回填,裸命令 execute,skill 回填)。
 */
@Composable
private fun SlashCommandPanel(
    sessionId: String,
    query: String,
    component: DshRootComponent,
    onPick: (CommandMenuItem) -> Unit
) {
    val menu by produceState<CommandMenu?>(initialValue = null, sessionId) {
        value = component.loadCommandMenu(sessionId)
    }
    val current = menu ?: return
    val items = remember(current, query) {
        filterMenu(current.commands + current.skills, query).take(SlashPanelMaxRows)
    }
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        if (current.degraded) {
            Text(
                text = if (current.errorCode == "agent-busy") {
                    stringResource(R.string.dsh_command_agent_busy)
                } else {
                    stringResource(R.string.dsh_command_load_failed)
                },
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
        if (items.isEmpty() && !current.degraded) {
            Text(
                text = stringResource(R.string.dsh_command_no_match),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                fontSize = 11.sp,
                color = AppTheme.colors.getOnInactiveContainerColor()
            )
        }
        for (item in items) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPick(item) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.slash,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.description.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = item.description,
                        modifier = Modifier.weight(1f),
                        fontSize = 11.sp,
                        color = AppTheme.colors.getOnInactiveContainerColor(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/** 内联面板最多露出的行数(超出由底部 sheet 的完整菜单承接) */
private const val SlashPanelMaxRows = 6

/** 消息流:reverseLayout 锚底;新消息自动滚到底,更早历史从顶部「加载更早」补页。
 * 事件日志经 [extractNodes] 升级为节点流(view 渲染意图来自 SessionLog);
 * item key = node.key(type#seq,稳定),加载更早历史前插不张冠李戴;
 * 定稿助手消息(有 messageId)下方挂消息反馈行(messageFeedback 远程端点) */
@Composable
private fun MessageList(
    component: DshRootComponent,
    chatState: ChatUiState,
    hasSelected: Boolean,
    sessionStore: SessionStore,
    feedbackStore: FeedbackStore,
    modifier: Modifier = Modifier
) {
    val log = remember(chatState.selectedSessionId, sessionStore) {
        chatState.selectedSessionId?.let { sessionStore.logFor(it) }
    }
    val eventsFlow = remember(log) { log?.events ?: flowOf(emptyList()) }
    val events by eventsFlow.collectAsState(emptyList())
    val hasOlderFlow = remember(log) { log?.hasOlder ?: flowOf(false) }
    val hasOlder by hasOlderFlow.collectAsState(false)

    // 事件日志 → 节点列表(纯函数产物;工具卡优先消费帧 view,缺席防御式从 data 提取)
    val nodes = remember(events, log) {
        log?.let { extractNodes(events, it::viewFor) }.orEmpty()
    }

    if (!hasSelected) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.dsh_select_session_hint),
                color = AppTheme.colors.getOnInactiveContainerColor(),
                fontSize = 14.sp
            )
        }
        return
    }
    if (nodes.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.dsh_empty_chat),
                color = AppTheme.colors.getOnInactiveContainerColor(),
                fontSize = 14.sp
            )
        }
        return
    }

    val parser = rememberMarkdownParser()
    val listState = rememberLazyListState()
    LaunchedEffect(nodes.size) {
        listState.animateScrollToItem(0)
    }
    val sessionId = chatState.selectedSessionId
    // 轮末统计挂到该轮最后一条定稿助手消息(对齐 web:stats 只出现在 turn tail)
    val statsByMessageKey = remember(nodes) {
        val map = HashMap<String, ChatNode.Stats>()
        var lastMessageKey: String? = null
        for (n in nodes) {
            if (n is ChatNode.AssistantMessage && !n.streaming && n.messageId != null) {
                lastMessageKey = n.key
            } else if (n is ChatNode.Stats) {
                lastMessageKey?.let { map[it] = n }
                lastMessageKey = null
            }
        }
        map
    }
    SelectionContainer {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        reverseLayout = true,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 16.dp,
            vertical = 12.dp
        )
    ) {
        items(nodes.asReversed(), key = { it.key }) { node ->
            // 定稿助手消息:气泡 + 反馈行(👍/👎/备注;streaming/messageId 缺席不挂)
            val messageId = (node as? ChatNode.AssistantMessage)
                ?.takeIf { !it.streaming }
                ?.messageId
            if (messageId != null && sessionId != null) {
                Column {
                    ChatNodeItem(node, parser)
                    FeedbackRow(
                        store = feedbackStore,
                        sessionId = sessionId,
                        messageId = messageId,
                        modifier = Modifier.padding(start = 4.dp),
                        messageText = (node as ChatNode.AssistantMessage).text,
                        messageTime = node.time,
                        stats = statsByMessageKey[node.key]
                    )
                }
            } else {
                ChatNodeItem(node, parser)
            }
        }
        if (hasOlder) {
            item(key = "load_older") {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TextButton(onClick = component::loadOlderHistory) {
                        Text(stringResource(R.string.dsh_load_older))
                    }
                }
            }
        }
    }
    }
}

/**
 * 抽屉会话列表(对齐 Flutter workspace_browser.dart 分组视图):
 * - 搜索框(防抖 session.search)+ 新建按钮 + workspace 分组(组头 + 会话行)
 * - 可见性:origin=='subagent' 隐藏;归档会话从分组视图消失;blank 仅当前选中可见
 * - 排序:组内按最近更新(updatedAt 倒序,sessionId tie-break);
 *   手动拖拽排序(workspace.insertSessionBefore)裁剪,store 方法已备
 * - 组头:「+」在该工作区新建会话;溢出菜单(重命名/删除工作区,删除非破坏性)
 * - 会话行长按菜单:重命名 / 分叉 / 导出 / 归档
 */
@Composable
private fun SessionDrawer(
    summaries: List<SessionSummary>,
    workspaces: List<WorkspaceView>,
    archivedIds: List<String>,
    selectedId: String?,
    creating: Boolean,
    onSearch: suspend (String) -> SessionSearchValue?,
    onSelect: (String) -> Unit,
    onCreate: (String?) -> Unit,
    onFork: (String) -> Unit,
    onExport: (String) -> Unit,
    onRename: (SessionSummary) -> Unit,
    onArchive: (String) -> Unit,
    onCreateWorkspace: () -> Unit,
    onRenameWorkspace: (WorkspaceView) -> Unit,
    onDeleteWorkspace: (WorkspaceView) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<SessionSearchValue?>(null) }
    // 防抖:停笔 300ms 后发 session.search;空串回普通列表
    LaunchedEffect(query) {
        if (query.isBlank()) {
            searchResults = null
            return@LaunchedEffect
        }
        delay(300)
        searchResults = onSearch(query.trim())
    }

    // 可见会话基表:归档 + subagent 从分组视图消失;blank 仅当前选中那条可见
    val visible = remember(summaries, archivedIds, selectedId) {
        summaries
            .filter { summary ->
                summary.origin != "subagent" &&
                    summary.sessionId !in archivedIds &&
                    (!summary.blank || summary.sessionId == selectedId)
            }
            .sortedWith { a, b ->
                if (a.updatedAt != b.updatedAt) {
                    b.updatedAt.compareTo(a.updatedAt)
                } else {
                    a.sessionId.compareTo(b.sessionId)
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.dsh_sessions),
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = onCreateWorkspace) {
                Text(stringResource(R.string.dsh_workspace_new))
            }
            TextButton(onClick = { onCreate(null) }, enabled = !creating) {
                Text(stringResource(R.string.dsh_new_session))
            }
        }
        GlassOutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.dsh_search_hint), fontSize = 13.sp) },
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        val results = searchResults
        when {
            // 搜索态:命中行(标题 + 片段);查询非空且结果为空 → 无匹配提示
            query.isNotBlank() && results != null -> {
                if (results.items.isEmpty()) {
                    Text(
                        text = stringResource(R.string.dsh_search_no_result),
                        color = AppTheme.colors.getOnInactiveContainerColor(),
                        fontSize = 14.sp
                    )
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(results.items, key = { it.sessionId }) { item ->
                        val summary = summaries.firstOrNull { it.sessionId == item.sessionId }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelect(item.sessionId) }
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = summary?.let { sessionDisplayName(it) }
                                    ?: item.sessionId.take(8),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (item.snippet.isNotBlank()) {
                                Text(
                                    text = item.snippet,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            visible.isEmpty() && workspaces.isEmpty() -> Text(
                text = stringResource(R.string.dsh_no_sessions),
                color = AppTheme.colors.getOnInactiveContainerColor(),
                fontSize = 14.sp
            )

            else -> {
                val inWorkspaceIds = remember(workspaces) {
                    workspaces.flatMap { it.sessionIds }.toSet()
                }
                val ungrouped = visible.filter { it.sessionId !in inWorkspaceIds }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    // workspace 分组(顺序即 wire 顺序)
                    for (workspace in workspaces) {
                        val groupSessions = visible.filter { it.sessionId in workspace.sessionIds }
                        item(key = "ws_${workspace.workspaceId}") {
                            WorkspaceHeader(
                                workspace = workspace,
                                onNewSession = { onCreate(workspace.workspaceId) },
                                onRename = { onRenameWorkspace(workspace) },
                                onDelete = { onDeleteWorkspace(workspace) }
                            )
                        }
                        items(groupSessions, key = { it.sessionId }) { summary ->
                            SessionRow(
                                summary = summary,
                                isSelected = summary.sessionId == selectedId,
                                onSelect = { onSelect(summary.sessionId) },
                                onFork = { onFork(summary.sessionId) },
                                onExport = { onExport(summary.sessionId) },
                                onRename = { onRename(summary) },
                                onArchive = { onArchive(summary.sessionId) }
                            )
                        }
                    }
                    // 未分组桶:有未分组会话时展示;无任何工作区时兜底
                    if (ungrouped.isNotEmpty() || workspaces.isEmpty()) {
                        item(key = "ws_ungrouped") {
                            Text(
                                text = stringResource(R.string.dsh_workspace_ungrouped),
                                modifier = Modifier.padding(start = 8.dp, top = 10.dp, bottom = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        items(ungrouped, key = { it.sessionId }) { summary ->
                            SessionRow(
                                summary = summary,
                                isSelected = summary.sessionId == selectedId,
                                onSelect = { onSelect(summary.sessionId) },
                                onFork = { onFork(summary.sessionId) },
                                onExport = { onExport(summary.sessionId) },
                                onRename = { onRename(summary) },
                                onArchive = { onArchive(summary.sessionId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/** workspace 组头:标题 + 路径 + 「+」新建会话 + 溢出菜单(重命名/删除) */
@Composable
private fun WorkspaceHeader(
    workspace: WorkspaceView,
    onNewSession: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 10.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = workspace.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = workspace.path,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        // 「+」:在本工作区新建会话(归入该分组)
        IconButton(onClick = onNewSession, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.dsh_workspace_new_session),
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box {
            IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.dsh_workspace_actions),
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.dsh_workspace_rename)) },
                    onClick = {
                        menuOpen = false
                        onRename()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.dsh_workspace_delete)) },
                    onClick = {
                        menuOpen = false
                        onDelete()
                    }
                )
            }
        }
    }
}

/** 单个会话行:running 状态点 + 标题;长按弹操作菜单(重命名/分叉/导出/归档) */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SessionRow(
    summary: SessionSummary,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onFork: () -> Unit,
    onExport: () -> Unit,
    onRename: () -> Unit,
    onArchive: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .combinedClickable(
                    onClick = onSelect,
                    onLongClick = { menuOpen = true }
                )
                .background(
                    if (isSelected) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                    }
                )
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // running 状态点
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (summary.running) {
                            AppTheme.colors.getPrimaryColor()
                        } else {
                            AppTheme.colors.getOnInactiveContainerColor().copy(alpha = 0.3f)
                        }
                    )
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = sessionDisplayName(summary),
                modifier = Modifier.weight(1f),
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.dsh_session_rename)) },
                onClick = {
                    menuOpen = false
                    onRename()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.dsh_session_fork)) },
                onClick = {
                    menuOpen = false
                    onFork()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.dsh_session_export)) },
                onClick = {
                    menuOpen = false
                    onExport()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.dsh_session_archive)) },
                onClick = {
                    menuOpen = false
                    onArchive()
                }
            )
        }
    }
}

/** 新建工作区对话框(workspace.create;path 为服务端目录路径,同路径幂等) */
@Composable
private fun CreateWorkspaceDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var path by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dsh_workspace_new)) },
        text = {
            GlassOutlinedTextField(
                value = path,
                onValueChange = { path = it },
                label = { Text(stringResource(R.string.dsh_workspace_path_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(path.trim()) },
                enabled = path.isNotBlank()
            ) {
                Text(stringResource(R.string.dsh_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dsh_cancel))
            }
        }
    )
}

/** 重命名工作区对话框(workspace.rename;响应回带行落地) */
@Composable
private fun RenameWorkspaceDialog(
    target: WorkspaceView,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var title by rememberSaveable(target.workspaceId) { mutableStateOf(target.title) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dsh_workspace_rename)) },
        text = {
            GlassOutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.dsh_workspace_title_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title.trim()) },
                enabled = title.isNotBlank()
            ) {
                Text(stringResource(R.string.dsh_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dsh_cancel))
            }
        }
    )
}

/** 删除工作区确认(workspace.delete;非破坏性 —— 会话保留,移入未分组) */
@Composable
private fun DeleteWorkspaceDialog(
    target: WorkspaceView,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dsh_workspace_delete_title, target.title)) },
        text = { Text(stringResource(R.string.dsh_workspace_delete_detail)) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.dsh_workspace_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dsh_cancel))
            }
        }
    )
}

/** 会话显示名:投影标题 → cwd 目录名 → 未命名占位 */
@Composable
private fun sessionDisplayName(summary: SessionSummary): String =
    summary.displayTitle()
        ?: summary.cwd?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.dsh_untitled_session)
