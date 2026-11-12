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

package com.wanbaohe.dsh.wire.model

/**
 * 主机基础事实(就绪快照)。
 *
 * DSH 0.1.5-rc.2 **删除了 `host.describe`**:这里的数据改为拼装:
 * - [home] 来自 `$events` 逻辑流首帧 `ready`(`host.home`,只用于缩写路径)
 * - [provider]/[model] 来自 `session/modelCatalog` 的 default(未配置模型时缺席)
 * - [cwd]/[version]/[attachedSessions] 不再有 Host 级来源:cwd 下沉到每会话
 *   (follow snapshot.header.cwd),版本号没有任何端点暴露,会话数由 session/list 现算
 *
 * 因此 UI 不应再展示"主机版本";[version] 仅保留为 App 侧协议锚点
 * (com.wanbaohe.dsh.wire.WireCompat.VERIFIED_HARNESS_VERSION)。
 */
data class HostInfo(
    /** App 侧唯一验证通过的 Harness 版本(非主机上报值) */
    val version: String,
    val home: String? = null,
    val provider: String? = null,
    val model: String? = null,
    val canOpenPath: Boolean = false,
    /** 会话数由 session/list 结果填充(旧协议是 host.describe 的 attachedSessions) */
    val attachedSessions: Int = 0
)
