package com.wanbaohe.dsh.component

import android.content.Context
import android.text.format.Formatter
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.wanbaohe.dsh.R
import com.wanbaohe.dsh.connection.CloudRelayApi
import com.wanbaohe.dsh.connection.ConnectionPhase
import com.wanbaohe.dsh.connection.ConnectionSnapshot
import com.wanbaohe.dsh.connection.CredentialsStore
import com.wanbaohe.dsh.connection.DshApiClient
import com.wanbaohe.dsh.connection.DshConnectionController
import com.wanbaohe.dsh.connection.E2ECipher
import com.wanbaohe.dsh.connection.MutableTokenProvider
import com.wanbaohe.dsh.connection.PairingManager
import com.wanbaohe.dsh.connection.PrivilegeScope
import com.wanbaohe.dsh.connection.RemoteLoginSuccess
import com.wanbaohe.dsh.connection.StoredHost
import com.wanbaohe.dsh.connection.hostIdFor
import com.wanbaohe.dsh.connection.hostIdForBase
import com.wanbaohe.dsh.session.AttachmentRejectException
import com.wanbaohe.dsh.session.AttachmentRejection
import com.wanbaohe.dsh.session.CommandExecuteException
import com.wanbaohe.dsh.session.CommandMenu
import com.wanbaohe.dsh.session.CommandStore
import com.wanbaohe.dsh.session.FeedbackStore
import com.wanbaohe.dsh.session.GoalStore
import com.wanbaohe.dsh.session.InteractorStore
import com.wanbaohe.dsh.session.PendingApproval
import com.wanbaohe.dsh.session.PendingImage
import com.wanbaohe.dsh.session.PendingQuestion
import com.wanbaohe.dsh.session.QuestionAnswerDraft
import com.wanbaohe.dsh.session.QuestionSubmitOutcome
import com.wanbaohe.dsh.session.QueueStore
import com.wanbaohe.dsh.session.SessionStore
import com.wanbaohe.dsh.session.SettingsStore
import com.wanbaohe.dsh.session.SkillCatalog
import com.wanbaohe.dsh.session.SubagentStore
import com.wanbaohe.dsh.session.UnknownCommandException
import com.wanbaohe.dsh.session.WorkspaceStore
import com.wanbaohe.dsh.session.commandNameOf
import com.wanbaohe.dsh.wire.CarrierException
import com.wanbaohe.dsh.wire.RpcBusinessException
import com.wanbaohe.dsh.wire.model.GoalRef
import com.wanbaohe.dsh.wire.model.RespondReceipt
import com.wanbaohe.dsh.wire.model.SessionModelsValue
import com.wanbaohe.dsh.wire.model.SessionSearchValue
import com.wanbaohe.dsh.wire.model.SkillEntry
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.io.File
import javax.inject.Named

/**
 * DSH 根组件:连接页(地址输入 + 主机簿 + 配对入口)+ 配对页(P6)+ 聊天页。
 *
 * 内部页面状态 Connect → Chat:连接 ready 后自动进聊天页;聊天页返回键回连接页,
 * 连接保持不断(断开只能显式点「断开」)。Pairing 页为网关配对/密码登录(P6),
 * 成功即 adopt 凭证并整代连接;401 authBlocked 时重配 = 令牌原地刷新 + resume。
 * 控制器与 store 生命周期跟组件走:组件销毁时全部 dispose。
 */
class DshRootComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val apiClient: DshApiClient,
    @Named("DshOkHttpClient") private val okHttpClient: OkHttpClient,
    private val credentialsStore: CredentialsStore,
    private val cloudRelayApi: CloudRelayApi,
    @ApplicationContext private val appContext: Context
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(DshUiState())
    val uiState = _uiState.asStateFlow()

    private val _chatState = MutableStateFlow(ChatUiState())
    val chatState = _chatState.asStateFlow()

    private val _chatBundle = MutableStateFlow<ChatBundle?>(null)
    /** 当前连接实例的会话域 store 对;null = 未连接 */
    val chatBundle = _chatBundle.asStateFlow()

    private val _exportedZip = MutableSharedFlow<File>(extraBufferCapacity = 1)
    /** 导出完成的一次性事件(文件在应用私有目录,UI 经 FileProvider 分享) */
    val exportedZip = _exportedZip.asSharedFlow()

    private val _pairingManager = MutableStateFlow<PairingManager?>(null)
    /** 当前配对状态机(仅配对页存活期非 null) */
    val pairingManager = _pairingManager.asStateFlow()

    /** 令牌供给:控制器/ApiClient 每次请求时读取;重登/重配后原地刷新 + resume */
    private val tokenProvider = MutableTokenProvider()

    private var controller: DshConnectionController? = null
    private var observeJob: Job? = null

    /** 当前连接的主机簿条目(重配 prefill、就绪登记用) */
    private var currentEntry: StoredHost? = null

    /** 本次 connect() 后的首次 ready 自动进聊天页(重连代际不重复触发) */
    private var autoEnterChat = false

    init {
        // 云端中继可用性:登录用户专属(只看登录态 token,游客不可用;每次现取,无需订阅)
        _uiState.value = _uiState.value.copy(
            cloud = _uiState.value.cloud.copy(
                available = CloudRelayApi.currentLoginToken() != null
            )
        )
        // 主机簿条目实时进 UI
        componentScope.launch {
            credentialsStore.book.collect { book ->
                _uiState.value = _uiState.value.copy(savedHosts = book.hosts)
            }
        }
        // 设备名(配对/登录上报)实时进 UI
        componentScope.launch {
            credentialsStore.deviceName.collect { name ->
                _uiState.value = _uiState.value.copy(deviceName = name)
            }
        }
        // 启动决策(P6):有主机簿条目 → 按条目类型直连(LAN 纯地址)/静默连(有令牌);
        // 无 → 停留连接页
        componentScope.launch {
            val entry = credentialsStore.book.first().active ?: return@launch
            connectEntry(entry)
        }
        lifecycle.doOnDestroy {
            disposeController()
        }
    }

    fun onAddressChange(value: String) {
        _uiState.value = _uiState.value.copy(address = value)
    }

    /** LAN 直连:以输入地址为主机起一代连接(纯地址条目,无令牌) */
    fun connect() {
        val address = _uiState.value.address.trim()
        if (address.isEmpty()) return
        val normalized = DshConnectionController.normalizeBaseUri(address)
        connectEntry(StoredHost(id = hostIdForBase(normalized), baseUri = normalized))
    }

    /** 选中主机簿条目:按条目类型直接连(LAN 直连 / 有令牌静默连) */
    fun onSelectHost(entry: StoredHost) {
        connectEntry(entry)
    }

    /** 删除主机簿条目(远程条目 best-effort 吊销网关令牌) */
    fun onRemoveHost(entry: StoredHost) {
        componentScope.launch {
            credentialsStore.remove(entry.id)
        }
    }

    // ───────────────────────────── P7:云端中继 ─────────────────────────────

    /**
     * 申请一次性绑定码(POST {backend}/dsh/bind-codes,App JWT 鉴权)。
     * 用户把 6 位码输进 Mac 上的 dsh-connector CLI 完成绑定;码不进主机簿、不落盘。
     */
    fun requestCloudBindCode() {
        val token = CloudRelayApi.currentLoginToken()
        if (token == null) {
            _uiState.value = _uiState.value.copy(
                cloud = _uiState.value.cloud.copy(
                    available = false,
                    error = appContext.getString(R.string.dsh_cloud_login_required)
                )
            )
            return
        }
        if (_uiState.value.cloud.requesting) return
        _uiState.value = _uiState.value.copy(
            cloud = _uiState.value.cloud.copy(
                available = true,
                requesting = true,
                error = null
            )
        )
        componentScope.launch {
            try {
                val response = cloudRelayApi.requestBindCode(token)
                _uiState.value = _uiState.value.copy(
                    cloud = _uiState.value.cloud.copy(
                        requesting = false,
                        code = response.code,
                        expiresAtEpochMs = System.currentTimeMillis() +
                            response.expiresIn * 1000L
                    )
                )
            } catch (e: Throwable) {
                _uiState.value = _uiState.value.copy(
                    cloud = _uiState.value.cloud.copy(
                        requesting = false,
                        error = appContext.getString(
                            R.string.dsh_cloud_bind_code_failed,
                            e.message ?: e.toString()
                        )
                    )
                )
            }
        }
    }

    /**
     * 连接「我的电脑(云端中继)」:baseUri = {backend}/dsh/relay,
     * 鉴权头每次现取 App JWT(不落盘);条目进主机簿(kind = cloud)。
     * [e2eKey] 为扫码邀请带来的 E2E 密钥 base64url;缺省复用主机簿已存
     * 云端条目的密钥(支持扫码后直接「连接我的电脑」)。
     */
    fun connectCloud(e2eKey: String? = null) {
        if (CloudRelayApi.currentLoginToken() == null) {
            _uiState.value = _uiState.value.copy(
                cloud = _uiState.value.cloud.copy(
                    available = false,
                    error = appContext.getString(R.string.dsh_cloud_login_required)
                )
            )
            return
        }
        val relay = CloudRelayApi.relayBaseUri()
        val storedKey = e2eKey
            ?: _uiState.value.savedHosts.firstOrNull { it.isCloud }?.e2eKey
        connectEntry(
            StoredHost(
                id = hostIdFor(relay, StoredHost.KindCloud),
                baseUri = relay,
                kind = StoredHost.KindCloud,
                e2eKey = storedKey
            )
        )
    }

    /**
     * 扫码认领(P8):解析 onebox-dsh-bridge 插件二维码 → claim 配对会话 →
     * 成功即发起连接(等价「连接我的电脑」,带上邀请的 E2E 密钥;Mac 拿到设备
     * token 上线前后,连接控制器的退避重连会自然等到它就绪)。
     * 409 = 已被认领,410 = 已过期,401 = App 登录过期 —— 分别给提示。
     */
    fun claimCloudPair(raw: String) {
        val invite = CloudRelayApi.parseCloudPairInvite(raw)
        if (invite == null) {
            _uiState.value = _uiState.value.copy(
                cloud = _uiState.value.cloud.copy(
                    error = appContext.getString(R.string.dsh_cloud_scan_invalid)
                )
            )
            return
        }
        val token = CloudRelayApi.currentLoginToken()
        if (token == null) {
            _uiState.value = _uiState.value.copy(
                cloud = _uiState.value.cloud.copy(
                    available = false,
                    error = appContext.getString(R.string.dsh_cloud_login_required)
                )
            )
            return
        }
        if (_uiState.value.cloud.claiming) return
        _uiState.value = _uiState.value.copy(
            cloud = _uiState.value.cloud.copy(
                available = true,
                claiming = true,
                error = null
            )
        )
        componentScope.launch {
            try {
                cloudRelayApi.claimPairSession(invite, token)
                _uiState.value = _uiState.value.copy(
                    cloud = _uiState.value.cloud.copy(claiming = false)
                )
                connectCloud(invite.key)
            } catch (e: Throwable) {
                val status = (e as? CarrierException)?.httpStatus
                val message = when (status) {
                    HttpConflict -> appContext.getString(R.string.dsh_cloud_claim_conflict)
                    HttpGone -> appContext.getString(R.string.dsh_cloud_claim_expired)
                    HttpUnauthorized -> appContext.getString(R.string.dsh_cloud_auth_blocked)
                    else -> appContext.getString(
                        R.string.dsh_cloud_claim_failed,
                        e.message ?: e.toString()
                    )
                }
                _uiState.value = _uiState.value.copy(
                    cloud = _uiState.value.cloud.copy(claiming = false, error = message)
                )
            }
        }
    }

    /** 云端形态的鉴权头:App JWT 每次现取(登录/换号即时生效,不依赖主机簿) */
    private fun appTokenHeaders(): Map<String, String> =
        CloudRelayApi.currentAppToken()
            ?.let { mapOf("Authorization" to "Bearer $it") }
            .orEmpty()

    // ───────────────────────────── P6:配对 / 登录 / 401 ─────────────────────────────

    /** 打开配对页(新建状态机;[prefill] 预填网关地址 —— 401 重配场景带当前地址) */
    fun openPairing(prefill: String = "") {
        _pairingManager.value?.dispose()
        val manager = PairingManager(
            okHttpClient = okHttpClient,
            scope = componentScope,
            deviceNameProvider = { _uiState.value.deviceName },
            onSuccess = ::onRemoteLogin
        )
        if (prefill.isNotEmpty()) manager.onGatewayAddressChange(prefill)
        _pairingManager.value = manager
        _uiState.value = _uiState.value.copy(page = DshPage.Pairing)
    }

    /** 离开配对页(返回连接页;配对中材料随状态机一并销毁) */
    fun closePairing() {
        _pairingManager.value?.dispose()
        _pairingManager.value = null
        _uiState.value = _uiState.value.copy(page = DshPage.Connect)
    }

    /** 401 被拒:云端形态 = App 登录过期,令牌每次现取故直接 resume 重试;网关形态拉起配对页重登/重配 */
    fun reauthenticate() {
        if (currentEntry?.isCloud == true) {
            controller?.resume()
        } else {
            openPairing(prefill = currentEntry?.baseUri.orEmpty())
        }
    }

    /**
     * 配对/密码登录成功:凭证 adopt(复合键 upsert,同宿主原地刷新令牌)→
     * 若当前连接正被 401 阻断且目标未变 → 令牌原地刷新 + resume(整代不重建);
     * 否则按新条目整代连接。
     */
    private suspend fun onRemoteLogin(success: RemoteLoginSuccess) {
        val entry = credentialsStore.adopt(success)
        val blocked = controller != null && _uiState.value.authBlocked
        val sameTarget = currentEntry?.let { it.id == entry.id || it.baseUri == entry.baseUri } == true
        closePairing()
        if (blocked && sameTarget) {
            tokenProvider.token = success.token
            currentEntry = entry
            autoEnterChat = true
            controller?.resume()
        } else {
            connectEntry(entry)
        }
    }

    /** 修改设备名(清洗后落盘;空/泛称拒绝) */
    fun setDeviceName(name: String) {
        componentScope.launch {
            credentialsStore.setDeviceName(name)
        }
    }

    /** 连接:以条目为主机起一代连接控制器 + 会话域 store(旧实例先拆掉) */
    private fun connectEntry(entry: StoredHost) {
        disposeController()
        tokenProvider.token = entry.token
        currentEntry = entry
        val isCloud = entry.isCloud
        // E2E 密钥(仅云端条目可能有值;非法串已在扫码解析处拦截,此处再防一手)
        val cipher = entry.e2eKey?.let(E2ECipher::fromBase64Url)
        val newController = DshConnectionController(
            okHttpClient = okHttpClient,
            apiClient = apiClient,
            baseUri = entry.baseUri,
            // 云端形态:App JWT 每次现取;其余形态:主机簿令牌原地刷新
            authHeaders = { if (isCloud) appTokenHeaders() else tokenProvider.authHeaders() },
            payloadCipher = cipher,
            // 云端中继 = 已鉴权远程(经 App 后端隧道,特权面开放)
            authenticatedRemote = isCloud || entry.token != null,
            // 云端中继(strapi_go dshrelay)WS 桥不带 /api 段(…/dsh/relay/events.mux)
            wsPathPrefix = if (isCloud) "" else "/api"
        )
        controller = newController
        val skillCatalog = SkillCatalog(apiClient)
        val sessionStore = SessionStore(apiClient, newController, componentScope)
        val bundle = ChatBundle(
            sessionStore = sessionStore,
            workspaceStore = WorkspaceStore(apiClient, newController, componentScope),
            interactorStore = InteractorStore(apiClient, newController, componentScope),
            queueStore = QueueStore(apiClient, newController, componentScope),
            goalStore = GoalStore(apiClient),
            skillCatalog = skillCatalog,
            commandStore = CommandStore(apiClient, newController, skillCatalog, componentScope),
            subagentStore = SubagentStore(apiClient, newController, sessionStore, componentScope),
            settingsStore = SettingsStore(apiClient, newController, componentScope),
            feedbackStore = FeedbackStore(apiClient, newController, componentScope)
        )
        _chatBundle.value = bundle
        // 特权面可见性由连接目标决定(loopback 全开 / LAN 隐藏 / 已鉴权远程全开)
        _uiState.value = _uiState.value.copy(
            privilegeScope = newController.privilegeScope,
            currentHostIsCloud = isCloud
        )
        bundle.sessionStore.start()
        bundle.workspaceStore.start()
        bundle.interactorStore.start()
        bundle.queueStore.start()
        bundle.commandStore.start()
        bundle.subagentStore.start()
        bundle.settingsStore.start()
        bundle.feedbackStore.start()
        autoEnterChat = true
        observeJob = componentScope.launch {
            launch {
                newController.snapshots.collect { snapshot ->
                    _uiState.value = _uiState.value.copy(snapshot = snapshot)
                    // 就绪登记:LAN 直连/云端条目入主机簿(远程条目已在配对时 adopt);
                    // 本次连接的首次 ready 自动进聊天页
                    if (snapshot.phase == ConnectionPhase.Ready && snapshot.describe != null) {
                        if (isCloud) {
                            credentialsStore.adoptCloud(entry.baseUri, entry.e2eKey)
                        } else if (entry.token == null) {
                            credentialsStore.adoptLan(entry.baseUri)
                        }
                        if (autoEnterChat && _uiState.value.page == DshPage.Connect) {
                            autoEnterChat = false
                            _uiState.value = _uiState.value.copy(page = DshPage.Chat)
                        }
                    }
                }
            }
            launch {
                newController.authBlocked.collect { blocked ->
                    _uiState.value = _uiState.value.copy(authBlocked = blocked)
                }
            }
        }
        newController.start()
    }

    /** 主动断开:拆控制器与 store,复位快照并回连接页 */
    fun disconnect() {
        disposeController()
        tokenProvider.token = null
        currentEntry = null
        _uiState.value = _uiState.value.copy(
            snapshot = ConnectionSnapshot(
                generation = 0,
                phase = ConnectionPhase.Down
            ),
            authBlocked = false,
            page = DshPage.Connect,
            privilegeScope = PrivilegeScope.Lan,
            currentHostIsCloud = false
        )
    }

    // ───────────────────────────── 聊天页动作 ─────────────────────────────

    /** 聊天页返回键:回连接页,连接保持不断(断开需显式操作) */
    fun backToConnect() {
        _uiState.value = _uiState.value.copy(page = DshPage.Connect)
    }

    /** 连接页手动进聊天页(从聊天页退回连接页后再进入的场景) */
    fun enterChat() {
        if (_chatBundle.value == null) return
        _uiState.value = _uiState.value.copy(page = DshPage.Chat)
    }

    /** 选中会话:登记日志懒注册并装载历史尾页(幂等,seq 去重) */
    fun selectSession(sessionId: String) {
        _chatState.value = _chatState.value.copy(selectedSessionId = sessionId, error = null)
        val store = _chatBundle.value?.sessionStore ?: return
        componentScope.launch {
            try {
                store.loadHistory(sessionId)
            } catch (e: Throwable) {
                _chatState.value = _chatState.value.copy(error = chatError(e))
            }
        }
    }

    fun onChatInputChange(value: String) {
        _chatState.value = _chatState.value.copy(input = value)
    }

    /** composer 发送模式切换:queue(排队)↔ steer(插话进运行中轮次) */
    fun togglePromptMode() {
        val next = if (_chatState.value.promptMode == SessionStore.PromptModeSteer) {
            SessionStore.PromptModeQueue
        } else {
            SessionStore.PromptModeSteer
        }
        _chatState.value = _chatState.value.copy(promptMode = next)
    }

    /**
     * 发送(对齐 Flutter composer 斜杠仲裁):
     * 首 token 命中宿主命令目录 → commands/execute(目录内预校验,未知命令本地拒绝,
     * 服务端静默吞);skill 行/未知行放行走 session.prompt(host pre-step 识别 '/name')。
     * mode 取 composer 当前选择(queue 排队 / steer 插话);带图强制 queue(steer 仅文本语义)。
     */
    fun sendMessage() {
        val state = _chatState.value
        val text = state.input.trim()
        val images = state.attachments
        val sessionId = state.selectedSessionId
        val bundle = _chatBundle.value
        if ((text.isEmpty() && images.isEmpty()) || sessionId == null || bundle == null || state.sending) {
            return
        }
        _chatState.value = state.copy(sending = true, error = null)
        componentScope.launch {
            try {
                val commandName = commandNameOf(text)
                var handled = false
                if (commandName != null && images.isEmpty()) {
                    val directory = bundle.commandStore.listCommands(sessionId)
                    // 目录降级:agent-busy(子代理会话)视为空目录放行走 prompt;
                    // 其余失败强拒绝(目录未就绪时无法判断是不是命令)
                    if (directory.isDegraded && !directory.isAgentBusy) {
                        _chatState.value = _chatState.value.copy(
                            error = appContext.getString(
                                R.string.dsh_command_dir_not_ready,
                                commandName
                            )
                        )
                        return@launch
                    }
                    if (!directory.isDegraded &&
                        directory.commands.any { it.name == commandName }
                    ) {
                        bundle.commandStore.execute(sessionId, text)
                        handled = true
                    }
                }
                if (!handled) {
                    bundle.sessionStore.prompt(
                        sessionId,
                        text,
                        images,
                        mode = state.promptMode
                    )
                }
                _chatState.value = _chatState.value.copy(input = "", attachments = emptyList())
            } catch (e: Throwable) {
                _chatState.value = _chatState.value.copy(error = chatError(e))
            }
            _chatState.value = _chatState.value.copy(sending = false)
        }
    }

    /**
     * 命令菜单选中项派发(对齐 Flutter 菜单 dispatch 决策表):
     * leadingInput 命令(带 input.hint)→ 回填 '/name ' 到输入框;
     * 裸命令 → 立即 commands/execute;skill → 回填 '/name ' 纯文本(发送时 host 识别)。
     */
    fun pickCommandItem(sessionId: String, name: String, hint: String?, isCommand: Boolean) {
        if (isCommand && hint.isNullOrEmpty()) {
            executeCommand(sessionId, "/$name")
        } else {
            fillCommandPrompt(name)
        }
    }

    /** 命令/技能名回填输入框('/name ' 形式,续输参数) */
    fun fillCommandPrompt(name: String) {
        _chatState.value = _chatState.value.copy(input = "/$name ")
    }

    /** 立即执行命令(目录内预校验;业务错误进一次性横幅) */
    fun executeCommand(sessionId: String, line: String) {
        val store = _chatBundle.value?.commandStore ?: return
        componentScope.launch {
            try {
                store.execute(sessionId, line)
            } catch (e: UnknownCommandException) {
                _chatState.value = _chatState.value.copy(
                    error = appContext.getString(
                        R.string.dsh_unknown_command,
                        commandNameOf(line).orEmpty()
                    )
                )
            } catch (e: CommandExecuteException) {
                _chatState.value = _chatState.value.copy(
                    error = appContext.getString(R.string.dsh_command_execute_failed, e.message)
                )
            } catch (e: Throwable) {
                _chatState.value = _chatState.value.copy(error = chatError(e))
            }
        }
    }

    /** 命令与技能合并目录(commands/list + skill.list);失败降级为错误位菜单(UI 内联提示+重试) */
    suspend fun loadCommandMenu(sessionId: String, force: Boolean = false): CommandMenu? {
        val store = _chatBundle.value?.commandStore ?: return null
        return try {
            store.listAll(sessionId, force)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            null
        }
    }

    /** intake 完成的图片进待发列表(本地预拒在 UI 侧已完成,这里是纯暂存) */
    fun stageAttachments(images: List<PendingImage>) {
        if (images.isEmpty()) return
        _chatState.value = _chatState.value.copy(
            attachments = _chatState.value.attachments + images
        )
    }

    /** 预览条移除单张待发图片 */
    fun removeAttachment(index: Int) {
        val current = _chatState.value.attachments
        if (index !in current.indices) return
        _chatState.value = _chatState.value.copy(
            attachments = current.toMutableList().also { it.removeAt(index) }
        )
    }

    /** 新建会话([workspaceId] 非空 = 归入该工作区;null 时服务端用主机当前目录) */
    fun createSession(workspaceId: String? = null) {
        val store = _chatBundle.value?.sessionStore ?: return
        if (_chatState.value.creating) return
        _chatState.value = _chatState.value.copy(creating = true, error = null)
        componentScope.launch {
            try {
                val created = store.createSession(workspaceId = workspaceId)
                selectSession(created.sessionId)
            } catch (e: Throwable) {
                _chatState.value = _chatState.value.copy(error = chatError(e))
            }
            _chatState.value = _chatState.value.copy(creating = false)
        }
    }

    // ───────────────────────────── workspace 域动作 ─────────────────────────────

    /** workspace.create(按路径;同路径幂等回带既有行) */
    fun createWorkspace(path: String) {
        if (path.isBlank()) return
        runWorkspaceAction { it.create(path.trim()) }
    }

    /** workspace.rename(响应回带行落地,不等重取) */
    fun renameWorkspace(workspaceId: String, title: String) {
        if (title.isBlank()) return
        runWorkspaceAction { it.rename(workspaceId, title.trim()) }
    }

    /** workspace.delete(非破坏性:会话保留,移入未分组) */
    fun deleteWorkspace(workspaceId: String) {
        runWorkspaceAction { it.delete(workspaceId) }
    }

    /** workspace.archiveSession(归档,非破坏性;响应回带完整归档集合) */
    fun archiveSession(sessionId: String) {
        runWorkspaceAction { it.archiveSession(sessionId) }
    }

    /** workspace 动作公共回环:业务错误进一次性横幅(状态本体由 store 收敛) */
    private fun runWorkspaceAction(block: suspend (WorkspaceStore) -> Any) {
        componentScope.launch {
            val store = _chatBundle.value?.workspaceStore ?: return@launch
            try {
                block(store)
            } catch (e: Throwable) {
                _chatState.value = _chatState.value.copy(error = chatError(e))
            }
        }
    }

    /** 向前补一页更早历史(无更早时 no-op) */
    fun loadOlderHistory() {
        val sessionId = _chatState.value.selectedSessionId ?: return
        val store = _chatBundle.value?.sessionStore ?: return
        componentScope.launch {
            try {
                store.loadOlder(sessionId)
            } catch (e: Throwable) {
                _chatState.value = _chatState.value.copy(error = chatError(e))
            }
        }
    }

    fun dismissChatError() {
        _chatState.value = _chatState.value.copy(error = null)
    }

    // ───────────────────────────── P4 功能面动作 ─────────────────────────────

    /** 会话分叉:锚点须映射到已闭合 turn(turn 未闭合 → fork-unavailable,错误进横幅);成功切到新会话 */
    fun forkSession(sessionId: String) {
        val store = _chatBundle.value?.sessionStore ?: return
        componentScope.launch {
            try {
                val forked = store.fork(sessionId)
                selectSession(forked.sessionId)
            } catch (e: Throwable) {
                _chatState.value = _chatState.value.copy(error = chatError(e))
            }
        }
    }

    /** 会话重命名:规范化 title+seq 先落本地格,推送帧高 seq 覆盖 */
    fun renameSession(sessionId: String, title: String) {
        val store = _chatBundle.value?.sessionStore ?: return
        if (title.isBlank()) return
        componentScope.launch {
            try {
                store.rename(sessionId, title.trim())
            } catch (e: Throwable) {
                _chatState.value = _chatState.value.copy(error = chatError(e))
            }
        }
    }

    /**
     * 会话导出:GET /api/session.export 流式 ZIP 落应用私有目录,
     * 完成后经 [exportedZip] 一次性事件交给 UI 分享(FileProvider)。
     */
    fun exportSession(sessionId: String) {
        if (_chatState.value.exporting) return
        _chatState.value = _chatState.value.copy(exporting = true, error = null)
        componentScope.launch {
            try {
                val destination = File(
                    File(appContext.filesDir, "dsh-export"),
                    "dsh-session-$sessionId.zip"
                )
                apiClient.sessionExport(sessionId, destination, includeDescendants = true)
                _exportedZip.emit(destination)
            } catch (e: Throwable) {
                _chatState.value = _chatState.value.copy(error = chatError(e))
            }
            _chatState.value = _chatState.value.copy(exporting = false)
        }
    }

    /** 侧栏搜索:session.search(query ≤500 字符);失败返回 null(UI 静默,不弹横幅) */
    suspend fun searchSessions(query: String): SessionSearchValue? {
        val store = _chatBundle.value?.sessionStore ?: return null
        return try {
            store.search(query.take(MaxSearchQueryLength))
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            null
        }
    }

    /** 模型目录(session.models):目录 + 当前选择 + routable;失败返回 null(对话框内自提示) */
    suspend fun loadModelCatalog(sessionId: String): SessionModelsValue? {
        val store = _chatBundle.value?.sessionStore ?: return null
        return try {
            store.sessionModels(sessionId)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            null
        }
    }

    /** 应用模型选择(session.selectModel);选择可与目录成员无关(服务端语义) */
    fun applyModelSelection(
        sessionId: String,
        provider: String,
        model: String,
        reasoningEffort: String?
    ) {
        val store = _chatBundle.value?.sessionStore ?: return
        componentScope.launch {
            try {
                store.selectModel(sessionId, provider, model, reasoningEffort)
            } catch (e: Throwable) {
                _chatState.value = _chatState.value.copy(error = chatError(e))
            }
        }
    }

    /** 技能目录(skill.list);失败返回 null(弹层内自提示) */
    suspend fun loadSkills(sessionId: String): List<SkillEntry>? {
        val catalog = _chatBundle.value?.skillCatalog ?: return null
        return try {
            catalog.list(sessionId)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            null
        }
    }

    /** 选中技能:填入 "/name "(斜杠命令 = 单个 "/" 开头文本块的普通 prompt) */
    fun fillSkillPrompt(name: String) {
        _chatState.value = _chatState.value.copy(input = SkillCatalog.promptFor(name) + " ")
    }

    // ───────────────────────────── goal 动作(六方法) ─────────────────────────────

    fun goalCreate(objective: String, maxGoalRounds: Int? = null) {
        val sessionId = _chatState.value.selectedSessionId ?: return
        if (objective.isBlank()) return
        runGoalAction { it.create(sessionId, objective.trim(), maxGoalRounds) }
    }

    fun goalEdit(ref: GoalRef, objective: String? = null, maxGoalRounds: Int? = null) {
        val sessionId = _chatState.value.selectedSessionId ?: return
        runGoalAction { it.edit(sessionId, ref, objective, maxGoalRounds) }
    }

    fun goalPause(ref: GoalRef) {
        val sessionId = _chatState.value.selectedSessionId ?: return
        runGoalAction { it.pause(sessionId, ref) }
    }

    fun goalResume(ref: GoalRef) {
        val sessionId = _chatState.value.selectedSessionId ?: return
        runGoalAction { it.resume(sessionId, ref) }
    }

    fun goalComplete(ref: GoalRef) {
        val sessionId = _chatState.value.selectedSessionId ?: return
        runGoalAction { it.complete(sessionId, ref) }
    }

    fun goalClear(ref: GoalRef) {
        val sessionId = _chatState.value.selectedSessionId ?: return
        componentScope.launch {
            val store = _chatBundle.value?.goalStore ?: return@launch
            try {
                store.clear(sessionId, ref)
            } catch (e: Throwable) {
                _chatState.value = _chatState.value.copy(error = chatError(e))
            }
        }
    }

    /** goal 引用操作公共回环:CAS 冲突等业务错误进一次性横幅(状态本体由投影演进) */
    private fun runGoalAction(block: suspend (GoalStore) -> GoalRef) {
        componentScope.launch {
            val store = _chatBundle.value?.goalStore ?: return@launch
            try {
                block(store)
            } catch (e: Throwable) {
                _chatState.value = _chatState.value.copy(error = chatError(e))
            }
        }
    }

    // ───────────────────────────── 交互帧动作(审批/问答/队列) ─────────────────────────────

    /**
     * 审批应答(允许一次/拒绝);null = 通道不可用或已失败(错误进一次性横幅)。
     * not-pending 由 store 内部清场;bad-response 回执由调用方(UI)提示。
     */
    suspend fun respondApproval(approval: PendingApproval, allow: Boolean): RespondReceipt? {
        val store = _chatBundle.value?.interactorStore ?: return null
        return try {
            store.respondApproval(approval, allow)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            _chatState.value = _chatState.value.copy(error = chatError(e))
            null
        }
    }

    /** 问答提交:本地预校验 + respond;结局由 UI 内联展示(Accepted 时卡片随清场消失) */
    suspend fun submitQuestionAnswers(
        pending: PendingQuestion,
        drafts: List<QuestionAnswerDraft>
    ): QuestionSubmitOutcome {
        val store = _chatBundle.value?.interactorStore
            ?: return QuestionSubmitOutcome.TransportFailed("interactor unavailable")
        return store.respondQuestions(pending, drafts)
    }

    /** 删除队列项(queue-item-not-found 已在 store 折叠为无害;其余错误进一次性横幅) */
    fun deleteQueueItem(itemId: String) {
        val sessionId = _chatState.value.selectedSessionId ?: return
        val store = _chatBundle.value?.queueStore ?: return
        componentScope.launch {
            try {
                store.delete(sessionId, itemId)
            } catch (e: Throwable) {
                _chatState.value = _chatState.value.copy(error = chatError(e))
            }
        }
    }

    /** 取消当前 turn(保留 pending inbox;客户端永不重发/提升排队消息) */
    fun cancelCurrentTurn() {
        val sessionId = _chatState.value.selectedSessionId ?: return
        val store = _chatBundle.value?.queueStore ?: return
        componentScope.launch {
            try {
                store.cancel(sessionId)
            } catch (e: Throwable) {
                _chatState.value = _chatState.value.copy(error = chatError(e))
            }
        }
    }

    /** UI 侧本地化文案注入一次性横幅(如审批 bad-response 提示) */
    fun showChatError(message: String) {
        _chatState.value = _chatState.value.copy(error = message)
    }

    /** 业务错误取规范 message,附件本地预拒按 kind 本地化,其余折叠为 toString */
    private fun chatError(e: Throwable): String = when (e) {
        is RpcBusinessException -> e.error.message
        is AttachmentRejectException -> attachmentErrorText(e.rejection)
        else -> e.message ?: e.toString()
    }

    /** 附件本地预拒文案(字节数用系统短格式,如 12.3 MB) */
    private fun attachmentErrorText(rejection: AttachmentRejection): String = when (rejection) {
        is AttachmentRejection.TooMany ->
            appContext.getString(R.string.dsh_attach_too_many, rejection.count, rejection.max)

        is AttachmentRejection.UnsupportedType ->
            appContext.getString(R.string.dsh_attach_unsupported, rejection.mediaType)

        is AttachmentRejection.SingleTooLarge ->
            appContext.getString(
                R.string.dsh_attach_single_too_large,
                formatBytes(rejection.bytes),
                formatBytes(rejection.max)
            )

        is AttachmentRejection.PixelsTooLarge ->
            appContext.getString(R.string.dsh_attach_pixels_too_large, rejection.pixels, rejection.max)

        is AttachmentRejection.AggregateTooLarge ->
            appContext.getString(
                R.string.dsh_attach_aggregate_too_large,
                formatBytes(rejection.bytes),
                formatBytes(rejection.max)
            )
    }

    private fun formatBytes(bytes: Long): String =
        Formatter.formatShortFileSize(appContext, bytes)

    private fun disposeController() {
        observeJob?.cancel()
        observeJob = null
        controller?.dispose()
        controller = null
        _chatBundle.value?.dispose()
        _chatBundle.value = null
        _chatState.value = ChatUiState()
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit
        ): DshRootComponent
    }

    companion object {
        /** session.search query 上限(协议约束 500 字符) */
        private const val MaxSearchQueryLength = 500

        // 扫码认领的 HTTP 状态分流(409 已被认领 / 410 已过期 / 401 登录过期)
        private const val HttpUnauthorized = 401
        private const val HttpConflict = 409
        private const val HttpGone = 410
    }
}
