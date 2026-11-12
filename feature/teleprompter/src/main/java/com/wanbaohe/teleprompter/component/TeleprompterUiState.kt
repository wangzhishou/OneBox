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

package com.wanbaohe.teleprompter.component

import com.shifenmiao.database.teleprompter.entity.TeleprompterScriptEntity

/** 文稿列表页 UI 状态 */
data class TeleprompterListUiState(
    val scripts: List<TeleprompterScriptEntity> = emptyList(),
    val isLoading: Boolean = true,
)

/** 文稿编辑页 UI 状态 */
data class TeleprompterEditorUiState(
    val scriptId: String? = null,
    val title: String = "",
    val content: String = "",
    val isSaving: Boolean = false,
    val isNew: Boolean = true,
    /** 初始标题（用于脏检测） */
    val originalTitle: String = "",
    /** 初始内容（用于脏检测） */
    val originalContent: String = "",
) {
    /** 内容是否有未保存的修改 */
    val isDirty: Boolean
        get() = title != originalTitle || content != originalContent
}

/** 提词播放页 UI 状态 */
data class TeleprompterPlayerUiState(
    val content: String = "",
    val isPlaying: Boolean = false,
    val fontSize: Float = 48f,
    val scrollSpeed: Float = 3f,
    val isMirrorMode: Boolean = false,
    val showControls: Boolean = true,
    val scrollProgress: Float = 0f,
) {
    companion object {
        const val MIN_FONT_SIZE = 24f
        const val MAX_FONT_SIZE = 96f
        const val MIN_SPEED = 0.5f
        const val MAX_SPEED = 10f
    }
}

