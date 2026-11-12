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

package com.shifenmiao.common.voice

import com.shifenmiao.model.channel.FlavorType
import com.shifenmiao.model.remote.VoiceInputConfig
import com.shifenmiao.storage.TokenStorage

/** 后台 `voiceInput.engine` 取值: 自建识别(经网关反代) */
const val VOICE_ENGINE_SELF = "self"

/** 后台 `voiceInput.engine` 取值: 讯飞大模型识别 */
const val VOICE_ENGINE_IFLYTEK = "iflytek"

/** 本轮语音输入实际使用的引擎 */
enum class VoiceInputEngine { SELF, IFLYTEK, SYSTEM }

/**
 * 语音输入引擎判定(UI 与识别器共用同一入口, 避免规则散落)。
 *
 * 用哪个引擎由**后台远程配置**决定(改完最迟 5 分钟生效, 不用发版):
 * - `engine = "self"`: 自建 FunASR —— 另需 ① 国内渠道 ② 已登录(网关 /api/voice/asr/ws 只认用户 JWT);
 * - `engine = "iflytek"` 或未下发: 讯飞大模型识别(国内既有链路);
 * - 海外渠道没有麦克风入口, 恒走系统语音识别。
 *
 * 老版本 App(≤139)读的是 [VoiceInputConfig.provider], 不认识 `engine`,
 * 所以开关无论怎么切都不影响它们; 而 `engine = "self"` 但前提不满足(海外/未登录)时自动回退讯飞,
 * 也不会让任何用户失去语音输入。
 */
fun resolveVoiceInputEngine(voiceInput: VoiceInputConfig?): VoiceInputEngine {
    val isDomestic = !FlavorType.fromName().isOverseas
    if (!isDomestic) return VoiceInputEngine.SYSTEM
    if (voiceInput?.engine == VOICE_ENGINE_SELF && TokenStorage.isLogin()) {
        return VoiceInputEngine.SELF
    }
    if (voiceInput?.engine == VOICE_ENGINE_IFLYTEK || !voiceInput?.provider.isNullOrBlank()) {
        return VoiceInputEngine.IFLYTEK
    }
    return VoiceInputEngine.SYSTEM
}
