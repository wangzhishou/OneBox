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

package com.shifenmiao.model.xiangqi

/**
 * 象棋模块对外暴露的只读/写入 DTO。
 *
 * 枚举字段统一用 String 表示（如 mode = "HUMAN_VS_LLM"），
 * 避免 core/model 直接依赖 feature 层的枚举类。
 */

data class XiangqiGameSummaryDto(
    val id: String,
    val title: String,
    /** GameMode.name: LOCAL_PVP / HUMAN_VS_LLM / LLM_VS_LLM / ONLINE_PVP */
    val mode: String,
    /** GameStatus.name: NOT_STARTED / IN_PROGRESS / CHECK / RED_WINS / BLACK_WINS / DRAW / RESIGNED */
    val status: String,
    val resultText: String,
    val updatedAt: Long,
)

data class XiangqiGameDetailDto(
    val id: String,
    val title: String,
    val mode: String,
    val status: String,
    val initialFen: String,
    val currentFen: String,
    val currentPly: Int,
    val moves: List<XiangqiMoveDto>,
)

data class XiangqiMoveDto(
    val ply: Int,
    val moveUcci: String,
    val moveCn: String,
)
