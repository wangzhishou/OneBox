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

package com.t8rin.imagetoolbox.core.crash.data

import com.t8rin.imagetoolbox.core.domain.remote.AnalyticsManager
import com.t8rin.imagetoolbox.core.domain.remote.ErrorEvent
import com.t8rin.imagetoolbox.core.domain.remote.ErrorKind
import com.t8rin.imagetoolbox.core.domain.remote.ErrorReporter
import com.t8rin.imagetoolbox.core.domain.remote.ErrorReporter.Companion.SOURCE_KEY
import com.t8rin.logger.makeLog
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 统一错误上报的实现: 把 [ErrorEvent] 交给当前渠道的原生崩溃/异常统计。
 *
 * 唯一的后端是 [AnalyticsManager], 它按 flavor 分别实现:
 * - google 渠道 → Firebase Crashlytics(CI 已配 mapping 上传, 堆栈可反解)
 * - 国内渠道 / foss → no-op, 接入国产崩溃统计后自然就有数据了
 *   (要改的只有对应 `AnalyticsManagerImpl`, 见其中注释)
 *
 * 所以"换一家崩溃统计"的改动面始终是一个文件: 调用方只认 [ErrorReporter],
 * 不需要知道背后是谁, 也不需要判断渠道。
 *
 * 上报链路的异常在这里全部吞掉 —— 它不能升级成业务里的新异常。
 */
@Singleton
class ErrorReporterImpl @Inject constructor(
    private val analyticsManager: AnalyticsManager,
) : ErrorReporter {

    override fun report(event: ErrorEvent) {
        runCatching {
            val throwable = event.throwable ?: ReportedAnomaly(
                source = event.source,
                detail = event.message,
            )
            val keys = event.extra + mapOf(
                SOURCE_KEY to event.source,
                "error_kind" to event.kind.name.lowercase(Locale.ROOT),
            )
            when (event.kind) {
                // 致命: 与 GlobalExceptionHandler 走同一条通道
                ErrorKind.CRASH -> analyticsManager.sendReport(throwable)
                // 非致命: 用户还能继续用, 但这事不该发生
                ErrorKind.ERROR, ErrorKind.ANOMALY ->
                    analyticsManager.reportNonFatal(throwable = throwable, keys = keys)
            }
        }.onFailure {
            it.makeLog("ErrorReporter")
        }
    }
}
