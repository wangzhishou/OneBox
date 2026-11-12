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

package com.wanbaohe.decisionwheel.component

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import java.util.UUID

/**
 * 转盘选项数据
 *
 * @param weight 抽中权重，1f 为等概率；<=0 视为不参与
 * @param enabled "抽后移除"用，false 的扇区不再参与抽取（但仍在盘面上，只是变灰）
 */
@Immutable
data class WheelOption(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: Color,
    val weight: Float = 1f,
    val enabled: Boolean = true
)

/**
 * 转盘配置数据
 *
 * @param lastUsedAt 最近一次真实旋转的时间。编辑保存不应改动它，否则会把转盘顶到"最近使用"最前。
 */
@Immutable
data class DecisionWheel(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val options: List<WheelOption>,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = 0L
)

/**
 * 一次旋转的计划。
 *
 * 关键点：[winnerIndex] 由 Component 先按权重抽出，[targetRotation] 是"让该扇区中心对齐 12 点指针"
 * 反算出来的角度。UI 只负责把角度补间过去，绝不能反过来由角度推结果——动画一旦被打断
 * （配置变更、进程重建）就会指针与结果对不上。
 */
@Immutable
data class SpinPlan(
    val winnerIndex: Int,
    val targetRotation: Float,
    val durationMillis: Int,
    val turns: Int
)

/**
 * 转盘模块的本地设置。
 */
@Immutable
data class WheelSettings(
    val spinDurationMillis: Int = 4000,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val removeOnSpin: Boolean = false
)

/**
 * Spin 页可以跳转的目标页。定义在 component 层以避免反向依赖 router。
 */
enum class SpinDestination { Editor, History, Settings }
