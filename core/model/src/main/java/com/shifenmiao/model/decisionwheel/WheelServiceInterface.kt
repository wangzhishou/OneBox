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

package com.shifenmiao.model.decisionwheel

/**
 * 决策转盘业务 Service 接口。
 *
 * 供 AI AgentTool 层依赖的契约。
 * 实现类位于 feature/decision-wheel/service/。
 *
 * 职责:
 *  - 封装写操作（创建转盘 / 重置选项 / 删除）
 *  - 提供只读查询（供 Agent 使用）
 */
interface WheelServiceInterface {

    // ── 读操作（供 AgentTool 使用） ──────────────────────

    suspend fun listWheels(): List<WheelDto>

    suspend fun getWheel(wheelId: String): WheelDto?

    // ── 写操作 ──────────────────────────────────────

    /**
     * 创建转盘并自动生成选项配色；新转盘置顶（lastUsedAt 置当前时间）。
     */
    suspend fun createWheel(
        title: String,
        optionNames: List<String>,
    ): Result<WheelDto>

    /**
     * 整体替换指定转盘的选项列表并重新配色。
     */
    suspend fun updateWheelOptions(
        wheelId: String,
        optionNames: List<String>,
    ): Result<WheelDto>

    suspend fun deleteWheel(wheelId: String): Result<Unit>
}
