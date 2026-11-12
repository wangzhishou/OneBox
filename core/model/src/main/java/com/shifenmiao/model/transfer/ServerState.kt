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

package com.shifenmiao.model.transfer

/**
 * 服务器运行状态
 */
sealed class ServerState {
    /** 服务已停止 */
    data object Stopped : ServerState()

    /** 服务正在启动 */
    data object Starting : ServerState()

    /** 服务运行中 */
    data class Running(
        val port: Int,
        val address: String
    ) : ServerState()

    /** 服务出错 */
    data class Error(val message: String) : ServerState()
}

