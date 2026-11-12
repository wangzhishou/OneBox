/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
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

package com.t8rin.imagetoolbox.core.domain.startup

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 启动阶段中央控制器。
 *
 * 单例，通过 Hilt 注入到需要感知启动阶段的各层。
 * 提供声明式的启动阶段管理，是启动优化的统一挂载点。
 *
 * 使用方式：
 * ```
 * @Inject lateinit var startupPhaseController: StartupPhaseController
 *
 * // 推进阶段
 * startupPhaseController.advanceTo(StartupPhase.SHELL_VISIBLE)
 *
 * // 订阅阶段
 * val phase by startupPhaseController.phase.collectAsState()
 * if (phase.isAtLeast(StartupPhase.CONTENT_HYDRATED)) { ... }
 * ```
 *
 * 与 [com.t8rin.imagetoolbox.core.domain.performance.StartupTrace] 配合，
 * 可以按阶段输出耗时诊断数据。
 */
@Singleton
class StartupPhaseController @Inject constructor() {

    private val _phase = MutableStateFlow(StartupPhase.SPLASH_RENDERING)
    val phase: StateFlow<StartupPhase> = _phase.asStateFlow()

    /**
     * 推进到指定阶段。只能向前推进（ordinal 更大的阶段），
     * 重复调用或回退调用会被忽略。
     */
    fun advanceTo(phase: StartupPhase) {
        if (phase.ordinal > _phase.value.ordinal) {
            _phase.value = phase
        }
    }
}
