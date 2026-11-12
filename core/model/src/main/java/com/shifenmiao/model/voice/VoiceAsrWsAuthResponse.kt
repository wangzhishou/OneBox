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

package com.shifenmiao.model.voice

import kotlinx.serialization.Serializable

/**
 * 语音输入 WebSocket 鉴权响应。
 *
 * 由 Go 网关 `GET api/voice/asr/ws-auth` 下发：服务端持有讯飞密钥并签名，
 * App 只拿到一次性签名 wss URL 直连讯飞，密钥不出服务端。
 *
 * @param url 已签名的讯飞 WebSocket 地址
 * @param appId 讯飞应用 appid,首帧 header 需要
 * @param expiresIn 签名有效期(秒)
 */
@Serializable
data class VoiceAsrWsAuthResponse(
    val url: String,
    val appId: String,
    val expiresIn: Int
)
