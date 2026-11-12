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

package com.wanbaohe.decisionwheel.service

import androidx.compose.ui.graphics.Color
import com.shifenmiao.model.decisionwheel.WheelDto
import com.shifenmiao.model.decisionwheel.WheelOptionDto
import com.shifenmiao.model.decisionwheel.WheelServiceInterface
import com.wanbaohe.com.color.ColorGenerator
import com.wanbaohe.decisionwheel.component.DecisionWheel
import com.wanbaohe.decisionwheel.component.WheelOption
import com.wanbaohe.decisionwheel.data.WheelRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 决策转盘业务 Service 实现：包装 [WheelRepository]，供 AI AgentTool 使用。
 */
class WheelServiceImpl @Inject constructor(
    private val wheelRepository: WheelRepository,
) : WheelServiceInterface {

    override suspend fun listWheels(): List<WheelDto> {
        return wheelRepository.getAllWheels().first().map { it.toDto() }
    }

    override suspend fun getWheel(wheelId: String): WheelDto? {
        return wheelRepository.getWheelById(wheelId)?.toDto()
    }

    override suspend fun createWheel(
        title: String,
        optionNames: List<String>,
    ): Result<WheelDto> = runCatching {
        val names = optionNames.normalizedNames()
        require(title.isNotBlank()) { "title is blank" }
        require(names.size >= MIN_OPTIONS) { "need at least $MIN_OPTIONS options" }
        val wheel = DecisionWheel(
            title = title.trim(),
            options = buildOptions(names),
            // 列表按 lastUsedAt 倒序，置为当前时间让新转盘置顶
            lastUsedAt = System.currentTimeMillis()
        )
        wheelRepository.saveWheel(wheel)
        wheel.toDto()
    }

    override suspend fun updateWheelOptions(
        wheelId: String,
        optionNames: List<String>,
    ): Result<WheelDto> = runCatching {
        val names = optionNames.normalizedNames()
        require(names.size >= MIN_OPTIONS) { "need at least $MIN_OPTIONS options" }
        val existing = wheelRepository.getWheelById(wheelId)
            ?: throw NoSuchElementException("wheel not found: $wheelId")
        val updated = existing.copy(options = buildOptions(names))
        wheelRepository.updateWheel(updated)
        updated.toDto()
    }

    override suspend fun deleteWheel(wheelId: String): Result<Unit> = runCatching {
        wheelRepository.deleteWheel(wheelId)
    }

    private fun List<String>.normalizedNames(): List<String> =
        map { it.trim() }.filter { it.isNotEmpty() }.distinct()

    private fun buildOptions(names: List<String>): List<WheelOption> {
        val colors = ColorGenerator.generateSegmentBackgrounds(DEFAULT_BASE_COLOR, names.size)
        return names.mapIndexed { index, name ->
            WheelOption(name = name, color = colors.getOrNull(index) ?: DEFAULT_BASE_COLOR)
        }
    }

    private fun DecisionWheel.toDto(): WheelDto = WheelDto(
        id = id,
        title = title,
        options = options.map { WheelOptionDto(id = it.id, name = it.name) }
    )

    companion object {
        private const val MIN_OPTIONS = 2

        // 与 DecisionWheelEditorComponent 的兜底基准色保持一致
        private val DEFAULT_BASE_COLOR = Color(0xFFA3B7F6)
    }
}
