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
 * 授权码校验器。
 *
 * 业务模块实现此接口,告诉全局授权码系统:
 * 1. 是否已经设置过授权码 (决定锁屏进入 Unlock 还是 Setup 模式)
 * 2. 用户的输入是否正确 (解锁时验证)
 *
 * 实现放在具体的 feature 模块,通过 Hilt `@Binds @Singleton` 注入到
 * [com.shifenmiao.base.auth.AuthorizationCodeStateHolder]。
 *
 * 设计约定:
 * - 实现本身应保持轻量,只做必要参数 / 业务接口转发;加密 / 落库等核心 IO
 *   委托给已有的 Service 层,避免在 verifier 内堆叠业务规则
 * - suspend 函数可安全访问数据库 / 网络,无需在调用方另开线程
 */
interface AuthCodeVerifier {

    /**
     * 校验器的稳定标识,用于日志 / 调试。
     */
    val id: String

    /**
     * 是否已存在授权码。返回 true → 锁屏进入 Unlock,否则进入 Setup。
     */
    suspend fun hasCode(): Boolean

    /**
     * 校验用户输入的 [code] 是否正确。
     *
     * Setup 模式下,在用户输入第二次确认码时也会调用此方法,用于决定两次输入
     * 是否一致 (在状态机层处理)。Setup 模式下若返回 true 视为确认一致。
     */
    suspend fun verify(code: String): Boolean
}
