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

package com.shifenmiao.model.auth

/**
 * 请求授权码解锁 / 设置。
 *
 * 由调用方通过 [com.shifenmiao.model.event.AppEventBus] 发出,被全局
 * [com.shifenmiao.base.auth.AuthorizationCodeStateHolder] 订阅处理。
 *
 * @param source 调用来源,用于日志 / 调试
 * @param onSuccess 解锁或设置成功时回调
 * @param onFailure 用户取消或验证失败时回调,参数为可空的错误信息
 */
class RequestAuthorizationCodeEvent(
    val source: String = "",
    val onSuccess: () -> Unit = {},
    val onFailure: (String?) -> Unit = {},
)
