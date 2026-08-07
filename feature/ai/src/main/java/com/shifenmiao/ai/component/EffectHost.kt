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
