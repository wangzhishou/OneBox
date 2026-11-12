/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.dsh.session

import com.wanbaohe.dsh.connection.ConnectionPhase
import com.wanbaohe.dsh.connection.DshApiClient
import com.wanbaohe.dsh.connection.DshConnectionController
import com.wanbaohe.dsh.wire.ApiTimeoutException
import com.wanbaohe.dsh.wire.CarrierException
import com.wanbaohe.dsh.wire.DshEndpoints
import com.wanbaohe.dsh.wire.RemoteEventFrame
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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 * 斜杠命令体系:服务端命令目录 + 执行入口(对齐 web 端 ui-commands 的
 * per-session directory 语义)。
 *
 * 契约(DSH 0.1.5-rc.2,活体主机 + harness 源码双重核实):
 * - [DshEndpoints.COMMANDS_LIST]:args 恰好 `{agentId}`(agentId = 会话 id,
 *   harness 里 Agent 的 wire 身份就是 SessionId);成功 value 是**裸数组**
 *   `[{definitionId?, name, description, input?:{hint, attachments?}}]`,已按 name
 *   升序(见 `packages/interaction/commands/src/index.ts` 的 `list()` 与
 *   `types.ts` 的 `CommandDescriptor`;typert 直连端点无内层信封,由
 *   [DshApiClient.callRemote] 按形状剥信封)。业务失败(session/not-found /
 *   session/agent-busy)在外层 RpcResult ok:false → [RpcBusinessException]。
 *   subagent 会话作 agentId → agent-busy(ownership fence)→ 空目录 + 错误位,
 *   菜单降级为 skill-only(内联提示 + 重试)
 * - [DshEndpoints.COMMANDS_EXECUTE]:args 恰好 `{agentId, line, submittedAttachments}`。
 *   line 是完整命令行(如 `/help` 或普通 prompt 文本);submittedAttachments 是
 *   附件数组——本客户端 composer 把带图行让给 session/prompt,这里恒为空数组。
 *   回值 `{commandId, result:{kind:'success'|'error', text?}}`;服务端没解析出命令
 *   (语法不合法 / 名字不在目录)时 **value 直接缺席**,调用方读到空值。
 *   结局另有 command/run|command/done 生命周期事件进会话日志
 * - 目录失效来自 `$events` 转发的 emit 帧([RemoteEventFrame.Emit]):
 *   commands/change(args 为空)→ 清全部会话缓存;agent-preset/selected
 *   (args = [sessionId, agentPreset])→ 只清该会话(preset 换了,同一会话解析到的
 *   命令/技能集就换了)。代际翻转(重连)→ 清空全部缓存
 * - 缓存 per-session(只缓存成功目录,失败不缓存,重试 = 重新拉取);skill 源复用
 *   [SkillCatalog],失效信号同时打到它的同名会话桶
 *
 * 与旧版(<= 0.1.1)的差异:
 * - 失效信号换通道:旧版读 `connection.hostFrames` 的 MuxFrame/HostFrame,
 *   0.1.5 线协议删掉了这两条帧流,改读 `connection.eventFrames`(转发 emit)
 * - 错误码换成斜杠词表:agent-busy → [RpcErrorCodes.SessionAgentBusy]
 * - 执行回值可用:旧版成功当 void 处理(未知命令被服务端静默吞);0.1.5 服务端会把
 *   未解析出的行以"value 缺席"回给发起方,handler 失败也会带 result.kind=error
 *   → 这里分别映射到既有的 [UnknownCommandException] / [CommandExecuteException]
 * - 端点常量集中到 [DshEndpoints](不再在类里写字符串字面量)
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
                    // 重连 = 新代际(可能已换主机):命令目录与技能目录一并作废
                    cache.clear()
                    skills.invalidate()
                }
            }
        }
        scope.launch {
            connection.eventFrames.collect { frame ->
                if (disposed) return@collect
                // $events 的单向 emit 帧才是目录失效信号(waterfall/ready 与此无关)
                if (frame !is RemoteEventFrame.Emit) return@collect
                when (frame.event) {
                    EventCommandsChange -> {
                        // 目录属 preset/命令集:软失效,丢弃全部会话缓存(消费端自行重拉)
                        cache.clear()
                    }
                    EventAgentPresetSelected -> {
                        // preset 只改该会话解析出的命令/技能集,别的会话缓存留着
                        val sessionId = (frame.args.firstOrNull() as? JsonPrimitive)?.contentOrNull
                        if (sessionId == null) {
                            cache.clear()
                            skills.invalidate()
                        } else {
                            cache.remove(sessionId)
                            skills.invalidate(sessionId)
                        }
                    }
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
                DshEndpoints.COMMANDS_LIST,
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

    /** 合并目录:commands + skills('/name' 形式),分组:命令/skill;skill 目录失败静默丢弃 */
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
     * 执行命令:客户端目录内预校验,未知命令本地拒绝(不碰服务端)。
     * 目录未就绪(未拉取/已被失效事件丢弃)同样本地拒绝。
     *
     * 服务端兜底:本地目录可能已过期 → commands/execute 对没解析出的行回"value 缺席",
     * 这里映射成 [UnknownCommandException];handler 失败(result.kind=error)则抛
     * [CommandExecuteException](其结局另有 command/done 会话事件)。
     */
    suspend fun execute(sessionId: String, line: String) {
        val name = commandNameOf(line)
        val directory = cache[sessionId]
            ?: throw UnknownCommandException(name.orEmpty(), directoryReady = false)
        if (name == null || directory.commands.none { it.name == name }) {
            throw UnknownCommandException(name.orEmpty())
        }
        val value = try {
            api.callRemote(
                DshEndpoints.COMMANDS_EXECUTE,
                buildJsonObject {
                    put("agentId", sessionId)
                    put("line", line)
                    // 恒空:带附件的行在 composer 层就走 session/prompt,不进命令执行
                    putJsonArray(SubmittedAttachmentsKey) {}
                }
            )
        } catch (e: RpcBusinessException) {
            throw CommandExecuteException("${e.error.code}: ${e.error.message}")
        } catch (e: ApiTimeoutException) {
            throw CommandExecuteException("timeout")
        } catch (e: CarrierException) {
            throw CommandExecuteException("transport: ${e.message}")
        }
        val execution = parseExecution(value)
            ?: throw UnknownCommandException(name.orEmpty())
        if (execution.kind == ExecutionKindError) {
            throw CommandExecuteException(execution.text ?: "command failed: /$name")
        }
    }

    companion object {
        /**
         * commands/execute 的 wire 参数名:harness 的形参是 `submittedAttachments`
         * (不是 attachments/images),typert 按形参名生成 wire 名,且网关
         * `assertExactArguments` 拒绝多余/缺失字段 —— 名字写错会直接
         * gateway/arguments-invalid。
         */
        private const val SubmittedAttachmentsKey = "submittedAttachments"

        /** commands/execute 回值 result.kind 的错误分支 */
        private const val ExecutionKindError = "error"

        /** 转发的远程失效事件名($events emit) */
        private const val EventCommandsChange = "commands/change"
        private const val EventAgentPresetSelected = "agent-preset/selected"

        /**
         * commands/list 解析:value 是裸数组
         * `[{definitionId?, name, description, input?:{hint, attachments?}}]`。
         * 只暴露菜单要用的 name/description/input.hint(0.1.5 的 input.attachments
         * 是"该命令是否收附件"的声明;composer 目前把带图行让给 prompt,暂不消费)。
         */
        private fun parseCommandList(value: JsonElement): List<CommandEntry> {
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

        /**
         * commands/execute 回值解析:`{commandId, result:{kind, text?}}`。
         * value 缺席(服务端没解析出命令)或形状不符 → null。
         */
        private fun parseExecution(value: JsonElement): CommandExecution? {
            val obj = value as? JsonObject ?: return null
            val result = obj["result"] as? JsonObject ?: return null
            val kind = (result["kind"] as? JsonPrimitive)?.contentOrNull ?: return null
            return CommandExecution(kind, (result["text"] as? JsonPrimitive)?.contentOrNull)
        }
    }
}

/** commands/execute 回值里发起方关心的部分:result.kind 与 result.text */
private data class CommandExecution(val kind: String, val text: String?)

/** 一条命令目录条目(裸数组元素 `{name, description, input?:{hint}}`) */
data class CommandEntry(
    val name: String,
    val description: String,
    /** input.hint(leadingInput 占位提示;无则 null) */
    val hint: String? = null
)

/** 目录错误(错误位);[isAgentBusy] 供菜单降级为 skill-only 判定 */
data class CommandListError(val code: String, val message: String?) {
    val isAgentBusy: Boolean get() = code == RpcErrorCodes.SessionAgentBusy
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

/** 本地预校验拒绝:命令名不在目录 / 目录未就绪;服务端回"没解析出命令"时也用它 */
class UnknownCommandException(name: String, directoryReady: Boolean = true) :
    CommandStoreException(
        if (directoryReady) "unknown command /$name" else "command directory not ready, rejected /$name"
    )

/** 远程执行失败(载波/业务/内层错误/handler 失败) */
class CommandExecuteException(message: String) : CommandStoreException(message)

/**
 * 从行文本解析命令名:'/goal set x' → 'goal';非斜杠行返回 null。
 * 只做本地仲裁的宽松切词;服务端 `parseCommand` 才是权威语法
 * (小写 `[a-z][a-z0-9_-]*` + 空白/行尾边界,不匹配就当普通文本)。
 */
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
