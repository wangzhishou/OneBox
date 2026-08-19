package com.wanbaohe.dsh.session

import com.wanbaohe.dsh.connection.ConnectionPhase
import com.wanbaohe.dsh.connection.DshApiClient
import com.wanbaohe.dsh.connection.DshConnectionController
import com.wanbaohe.dsh.wire.ApiTimeoutException
import com.wanbaohe.dsh.wire.CarrierException
import com.wanbaohe.dsh.wire.HostFrame
import com.wanbaohe.dsh.wire.RpcBusinessException
import com.wanbaohe.dsh.wire.RpcErrorCodes
import com.wanbaohe.dsh.wire.model.SkillEntry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put

/**
 * 斜杠命令体系(对齐 Flutter command_store.dart,DSH-PROTOCOL §9)。
 *
 * 契约(活体主机实测):
 * - commands/list:payload 恰好 {args:{agentId}}(agentId = 根会话 id);成功 value
 *   是**裸数组** [{name, description, input?:{hint}}](typert 直连端点,无内层信封,
 *   由 [DshApiClient.callRemote] 按形状剥信封);业务失败(agent-busy /
 *   session-not-found)在外层 RpcResult ok:false → [RpcBusinessException]。
 *   subagent 会话作 agentId → agent-busy(ownership fence)→ 空目录+错误位,
 *   菜单降级为 skill-only(内联提示+重试)
 * - commands/execute {args:{agentId, line}} 成功返回 void;**未知命令被服务端静默吞**
 *   (ok:true)→ 客户端必须目录内预校验,未知命令本地拒绝,不指望服务端拒绝;
 *   结局经 command/run|command/done 生命周期事件进会话日志
 * - 缓存 per-session(只缓存成功目录,失败不缓存,重试=重新拉取);
 *   失效:host/remote-event commands/change 与 agent-preset/selected → 丢弃;
 *   代际翻转(重连)→ 清空全部缓存
 * - skill 源:复用 [SkillCatalog];skill.list 失败 → 技能组静默丢弃(只显示可用组)
 *
 * 生命周期与连接实例绑定:由组件层创建并 [dispose](不做 @Singleton)。
 */
class CommandStore(
    private val api: DshApiClient,
    private val connection: DshConnectionController,
    private val skills: SkillCatalog,
    parentScope: CoroutineScope
) {

    /** 子 scope:dispose 只取消自己,不动组件 scope */
    private val scope = CoroutineScope(
        parentScope.coroutineContext + SupervisorJob(parentScope.coroutineContext[Job])
    )

    private val cache = HashMap<String, CommandListResult>()

    @Volatile
    private var disposed = false
    private var started = false
    private var lastReadyGeneration = 0

    fun start() {
        if (started) return
        started = true
        scope.launch {
            connection.snapshots.collect { snapshot ->
                if (!disposed &&
                    snapshot.phase == ConnectionPhase.Ready &&
                    snapshot.generation > lastReadyGeneration
                ) {
                    lastReadyGeneration = snapshot.generation
                    // 重连 = 新代际:命令目录可能已变,清空全部缓存
                    cache.clear()
                }
            }
        }
        scope.launch {
            connection.hostFrames.collect { frame ->
                if (disposed) return@collect
                if (frame is HostFrame.RemoteEvent &&
                    (frame.event == EventCommandsChange || frame.event == EventAgentPresetSelected)
                ) {
                    // 目录属 preset/命令集:软失效,丢弃全部会话缓存(消费端自行重拉)
                    cache.clear()
                }
            }
        }
    }

    fun dispose() {
        disposed = true
        scope.cancel()
    }

    /** 已缓存目录(发送仲裁用;未拉取/已失效返回 null,不触发网络) */
    fun cachedDirectory(sessionId: String): CommandListResult? = cache[sessionId]

    /** 拉取某会话命令目录(缓存命中即返回;force 强制重取)。失败不缓存,重试 = 重新拉取 */
    suspend fun listCommands(sessionId: String, force: Boolean = false): CommandListResult {
        if (!force) {
            cache[sessionId]?.let { return it }
        }
        return try {
            val value = api.callRemote(
                RemoteCommandsList,
                buildJsonObject { put("agentId", sessionId) }
            )
            val result = CommandListResult.Ok(parseCommandList(value))
            cache[sessionId] = result
            result
        } catch (e: CancellationException) {
            throw e
        } catch (e: RpcBusinessException) {
            // 业务失败在外层 RpcResult ok:false(agent-busy / session-not-found)
            CommandListResult.Degraded(CommandListError(e.error.code, e.error.message))
        } catch (e: ApiTimeoutException) {
            CommandListResult.Degraded(CommandListError("timeout", null))
        } catch (e: CarrierException) {
            CommandListResult.Degraded(CommandListError("transport", e.message))
        } catch (e: Throwable) {
            // 畸形响应等:降级 + 重试,绝不让 UI 崩
            CommandListResult.Degraded(CommandListError("malformed", e.message))
        }
    }

    /** 合并目录:commands + skills('/name' 形式),分组:命令/skill;skill.list 失败静默丢弃 */
    suspend fun listAll(sessionId: String, force: Boolean = false): CommandMenu {
        val result = listCommands(sessionId, force)
        val skillList = try {
            skills.list(sessionId, force)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            emptyList()
        }
        return CommandMenu(
            commands = result.commands.map { CommandMenuItem.command(it) },
            skills = skillList.map { CommandMenuItem.skill(it) },
            degraded = result.isDegraded,
            errorCode = result.error?.code,
            errorMessage = result.error?.message
        )
    }

    /**
     * 执行命令:客户端目录内预校验,未知命令本地拒绝(服务端静默吞)。
     * 目录未就绪(未拉取/已被失效事件丢弃)同样本地拒绝,不碰服务端。
     */
    suspend fun execute(sessionId: String, line: String) {
        val name = commandNameOf(line)
        val directory = cache[sessionId]
            ?: throw UnknownCommandException(name.orEmpty(), directoryReady = false)
        if (name == null || directory.commands.none { it.name == name }) {
            throw UnknownCommandException(name.orEmpty())
        }
        try {
            api.callRemote(
                RemoteCommandsExecute,
                buildJsonObject {
                    put("agentId", sessionId)
                    put("line", line)
                }
            )
        } catch (e: RpcBusinessException) {
            throw CommandExecuteException("${e.error.code}: ${e.error.message}")
        } catch (e: ApiTimeoutException) {
            throw CommandExecuteException("timeout")
        } catch (e: CarrierException) {
            throw CommandExecuteException("transport: ${e.message}")
        }
    }

    companion object {
        /** 远程端点方法名(斜杠命名,不在核心点号方法集里) */
        private const val RemoteCommandsList = "commands/list"
        private const val RemoteCommandsExecute = "commands/execute"

        /** 转发的远程失效事件(host/remote-event 的 event 字符串) */
        private const val EventCommandsChange = "commands/change"
        private const val EventAgentPresetSelected = "agent-preset/selected"

        /** commands/list 解析:value 是裸数组 [{name, description, input?:{hint}}] */
        private fun parseCommandList(value: kotlinx.serialization.json.JsonElement): List<CommandEntry> {
            val array = value as? JsonArray
                ?: throw CarrierException("commands/list: value 不是数组")
            return array.map { element ->
                val obj = element as? JsonObject
                CommandEntry(
                    name = (obj?.get("name") as? JsonPrimitive)?.contentOrNull.orEmpty(),
                    description = (obj?.get("description") as? JsonPrimitive)?.contentOrNull.orEmpty(),
                    hint = ((obj?.get("input") as? JsonObject)?.get("hint") as? JsonPrimitive)
                        ?.contentOrNull
                )
            }
        }
    }
}

/** 一条命令目录条目(裸数组元素 {name, description, input?:{hint}}) */
data class CommandEntry(
    val name: String,
    val description: String,
    /** input.hint(leadingInput 占位提示;无则 null) */
    val hint: String? = null
)

/** 目录错误(错误位);[isAgentBusy] 供菜单降级为 skill-only 判定 */
data class CommandListError(val code: String, val message: String?) {
    val isAgentBusy: Boolean get() = code == RpcErrorCodes.AgentBusy
}

/**
 * commands/list 的结果:成功目录 or 失败降级(空目录 + 错误位)。
 * 只缓存成功结果;失败(含 agent-busy)不缓存 —— 重试天然重新拉取。
 */
sealed class CommandListResult {
    abstract val commands: List<CommandEntry>
    abstract val error: CommandListError?

    val isDegraded: Boolean get() = error != null
    val isAgentBusy: Boolean get() = error?.isAgentBusy ?: false

    data class Ok(override val commands: List<CommandEntry>) : CommandListResult() {
        override val error: CommandListError? = null
    }

    data class Degraded(override val error: CommandListError) : CommandListResult() {
        override val commands: List<CommandEntry> = emptyList()
    }
}

/** 合并菜单条目的类型:host 命令 / skill */
enum class CommandMenuItemKind { Command, Skill }

/** 合并菜单条目(listAll 的输出,UI 只消费这个) */
data class CommandMenuItem(
    val kind: CommandMenuItemKind,
    val name: String,
    val description: String,
    /** 仅命令有:input.hint(占位提示) */
    val hint: String? = null,
    /** 仅 skill 有:modelInvocable(菜单用图标区分) */
    val skillModelInvocable: Boolean? = null
) {
    /** 点击派发的行文本:'/name' */
    val slash: String get() = "/$name"
    val isCommand: Boolean get() = kind == CommandMenuItemKind.Command

    companion object {
        fun command(entry: CommandEntry) = CommandMenuItem(
            kind = CommandMenuItemKind.Command,
            name = entry.name,
            description = entry.description,
            hint = entry.hint
        )

        fun skill(skill: SkillEntry) = CommandMenuItem(
            kind = CommandMenuItemKind.Skill,
            name = skill.name,
            description = skill.description,
            skillModelInvocable = skill.modelInvocable
        )
    }
}

/** 合并菜单目录(分组:命令/skill) */
data class CommandMenu(
    val commands: List<CommandMenuItem>,
    val skills: List<CommandMenuItem>,
    /** 命令目录降级(agent-busy/失败)→ UI 显示错误位 + 重试,菜单 skill-only */
    val degraded: Boolean,
    val errorCode: String? = null,
    val errorMessage: String? = null
) {
    val isEmpty: Boolean get() = commands.isEmpty() && skills.isEmpty()
}

/** 域异常基类 */
sealed class CommandStoreException(message: String) : Exception(message)

/** 本地预校验拒绝:命令名不在目录 / 目录未就绪 */
class UnknownCommandException(name: String, directoryReady: Boolean = true) :
    CommandStoreException(
        if (directoryReady) "unknown command /$name" else "command directory not ready, rejected /$name"
    )

/** 远程执行失败(载波/业务/内层错误) */
class CommandExecuteException(message: String) : CommandStoreException(message)

/** 从行文本解析命令名:'/goal set x' → 'goal';非斜杠行返回 null */
fun commandNameOf(line: String): String? {
    val t = line.trim()
    if (!t.startsWith("/")) return null
    val rest = t.substring(1)
    var end = rest.length
    for (i in rest.indices) {
        val c = rest[i]
        if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
            end = i
            break
        }
    }
    val name = rest.substring(0, end)
    return name.ifEmpty { null }
}

/** fuzzy:子序列不区分大小写匹配;前缀优先,其余保持原序(空查询返回原序) */
fun filterMenu(items: List<CommandMenuItem>, query: String): List<CommandMenuItem> {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return items.toList()
    val prefix = ArrayList<CommandMenuItem>()
    val subseq = ArrayList<CommandMenuItem>()
    for (item in items) {
        val name = item.name.lowercase()
        if (name.startsWith(q)) {
            prefix.add(item)
        } else if (isSubsequence(name, q)) {
            subseq.add(item)
        }
    }
    return prefix + subseq
}

private fun isSubsequence(haystack: String, needle: String): Boolean {
    if (needle.isEmpty()) return true
    var i = 0
    for (j in haystack.indices) {
        if (i >= needle.length) break
        if (haystack[j] == needle[i]) i++
    }
    return i == needle.length
}
