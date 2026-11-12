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

package com.wanbaohe.gomoku.data

/**
 * UI 层历史上使用的一套 DTO 别名。
 *
 * 这些类型曾是与 `application.dto` 逐字段重复的独立 data class（Component 层靠一层 `toLegacy()`
 * 纯搬运转换），改一个字段要同步两处、极易漏。现收敛为类型别名：**唯一事实源在 `application.dto`**。
 */
typealias GomokuGameSummary = com.wanbaohe.gomoku.application.dto.GameSummary

typealias GomokuPlyRecord = com.wanbaohe.gomoku.application.dto.PlyRecord

typealias TextExportLabels = com.wanbaohe.gomoku.application.dto.ExportLabels

data class GomokuSettings(
    val moveSoundUrl: String = "",
    val backgroundMusicUrl: String = "",
    val checkSoundUrl: String = "",
    val ttsEnabled: Boolean = true,
    val ttsTemplateTexts: Map<String, String> = emptyMap(),
    val soundEnabled: Boolean = true,
)

data class GomokuTTSTemplate(
    val tag: String,
    val labelResId: Int,
    val defaultTextRes: Int,
) {
    fun defaultText(): String = com.shifenmiao.interfaces.singleton.AppContext.getString(defaultTextRes)
}

object GomokuTTSTemplates {
    val WIN = GomokuTTSTemplate(
        "gomoku-win",
        com.wanbaohe.gomoku.R.string.gomoku_tts_win,
        com.wanbaohe.gomoku.R.string.gomoku_tts_default_win,
    )
    val DRAW = GomokuTTSTemplate(
        "gomoku-draw",
        com.wanbaohe.gomoku.R.string.gomoku_tts_draw,
        com.wanbaohe.gomoku.R.string.gomoku_tts_default_draw,
    )
    val ALL = listOf(WIN, DRAW)
}
