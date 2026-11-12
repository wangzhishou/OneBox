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

package com.shifenmiao.ai.component

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 全局视觉效果宿主。
 *
 * AI 通过 show_effect 工具触发视觉效果（撒花、炸弹等），
 * 效果请求发布到此宿主，由 UI 层（GlobalToolInteractionHost）统一消费。
 */
@Singleton
class EffectHost @Inject constructor() {

    private val _effectRequest = MutableStateFlow<EffectRequest?>(null)
    val effectRequest: StateFlow<EffectRequest?> = _effectRequest

    fun triggerEffect(effect: String, message: String? = null) {
        _effectRequest.value = EffectRequest(effect, message)
    }

    fun consumeEffect() {
        _effectRequest.value = null
    }
}

data class EffectRequest(
    val effect: String,
    val message: String? = null
)
