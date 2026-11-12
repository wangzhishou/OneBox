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

/**
 * 业务异常(anomaly)与"只有消息、没有异常对象"的错误的载体。
 *
 * 渠道崩溃统计(v1 = Firebase Crashlytics)都以**异常**为上报单位, 而 anomaly 本来没有
 * 异常对象 —— 用这个类型承载。
 *
 * 关键在栈帧: Crashlytics 按「异常类型 + 栈帧」聚合 issue, 而所有 anomaly 都在上报
 * 封装里同一行创建, 真实栈完全一样 —— 不处理的话几十种业务异常会挤成同一个 issue,
 * Non-fatal 面板就没法看了。所以这里用 [source] 合成一帧, 让每个出错位置各自成 issue。
 */
class ReportedAnomaly(
    val source: String,
    val detail: String,
) : Exception("[$source] $detail") {

    init {
        stackTrace = arrayOf(source.toSyntheticFrame())
    }
}

/**
 * 把 `AIEngineCatalogManager.saveEngineConfigOnly` 转成 Crashlytics 里可读的一帧:
 * `com.shifenmiao.report.AIEngineCatalogManager.saveEngineConfigOnly` ——
 * issue 列表里直接看到是哪块代码在报错。
 */
private fun String.toSyntheticFrame(): StackTraceElement {
    val owner = substringBefore('.').toSafeIdentifier()
    val method = substringAfter('.', missingDelimiterValue = "report").toSafeIdentifier()
    return StackTraceElement(
        "com.shifenmiao.report.$owner",
        method,
        "$owner.kt",
        0,
    )
}

/** StackTraceElement 不接受空串或奇怪字符, 统一收敛成合法标识符 */
private fun String.toSafeIdentifier(): String = map { char ->
    if (char.isLetterOrDigit() || char == '_' || char == '$') char else '_'
}.joinToString(separator = "")
    .take(48)
    .ifBlank { "Unknown" }
