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

package com.wanbaohe.poem.component

import com.wanbaohe.poem.model.Poem

data class PoemUiState(
    val poem: Poem? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isGeneratingInsight: Boolean = false,
    val insightError: String? = null,
    /** 诗意解读流式生成中的累计内容快照(仅当前诗词有效,生成结束/切换诗词后清空) */
    val streamingInsight: String? = null,
    /** 深度思考内容快照(仅展示不落库;成功后保留至切换诗词/重新生成) */
    val streamingReasoning: String? = null,
    val isGeneratingTranslation: Boolean = false,
    val translationError: String? = null,
    /** 拼音生成中(静默后台任务,用于底部灰色状态提示) */
    val isGeneratingPinyin: Boolean = false,
    /** 诗朗诵语音合成中 */
    val isSynthesizingSpeech: Boolean = false,
    /** 诗朗诵播放中 */
    val isReciting: Boolean = false,
    /** 全部历史(时间倒序),含当前展示的诗词;主页滑动翻页与历史弹层共用 */
    val history: List<Poem> = emptyList(),
)
