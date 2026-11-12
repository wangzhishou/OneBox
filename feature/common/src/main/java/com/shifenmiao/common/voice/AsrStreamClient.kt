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

/** RemoteConfig.voiceInput.provider 的历史取值(仅 ≤139 老版本读它, 新版本用 `engine` 切换) */
const val VOICE_PROVIDER_IFLYTEK = "iflytek"

/**
 * 流式语音识别客户端契约。
 *
 * 两个实现:
 * - [IFlytekAsrClient]: App 持网关签名 URL 直连讯飞, 音频 base64 塞 JSON 帧;
 * - [FunAsrClient]: 连自家网关反代的 FunASR 2pass 服务, 音频走二进制帧。
 * 二者对上层([VoiceRecognizer] / UI)语义一致: 上行 PCM、回中间结果、可取消。
 */
interface AsrStreamClient {
    /** 上行一块 16k 单声道 PCM */
    fun sendPcm(chunk: ByteArray)

    /** 说完: 排空队列后发结束帧, 等服务端最终结果 */
    fun stop()

    /** 放弃: 立即断开 */
    fun cancel()
}
