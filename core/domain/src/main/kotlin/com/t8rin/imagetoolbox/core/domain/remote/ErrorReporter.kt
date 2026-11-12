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

package com.t8rin.imagetoolbox.core.domain.remote

/**
 * 错误事件的级别。
 *
 * - [CRASH] 未捕获异常, 进程即将退出
 * - [ERROR] 被 catch 住、但用户能感知到的失败(请求失败、保存失败)
 * - [ANOMALY] 没有抛异常、但逻辑上不该出现的状态(例如"保存成功却读不回来")
 *
 * 分开是为了在后台按级别过滤: 崩溃要立刻看, 业务异常只需要看趋势。
 */
enum class ErrorKind {
    CRASH,
    ERROR,
    ANOMALY,
}

/**
 * 一条错误事件。
 *
 * @param source 出错位置, 用 `类名.方法名` 标识, 后台按它聚合
 * @param throwable 可选。有它才会采集到调用栈
 * @param extra 只放**低基数**的上下文(引擎名、协议这类枚举值)。
 *   不要放 token、带 key 的完整 URL 或用户输入原文 —— 上报是明文出网。
 */
data class ErrorEvent(
    val kind: ErrorKind,
    val source: String,
    val message: String,
    val throwable: Throwable? = null,
    val extra: Map<String, String> = emptyMap(),
    val occurredAt: Long = System.currentTimeMillis(),
)

/**
 * 统一错误上报入口, 任何页面/模块都可以注入使用。
 *
 * 它只是一层封装: 事件最终交给当前渠道的原生崩溃/异常统计(google = Crashlytics),
 * 渠道没有实现时是 no-op —— 调用方因此不需要判断渠道, 国内接入崩溃 SDK 后
 * 这些调用会自动开始有数据。
 *
 * 行为约定:
 * - **永不抛异常、永不阻塞调用方**
 * - **尊重隐私开关**: 用户在设置里关掉"崩溃报告"后, 渠道实现内部会直接丢弃
 */
interface ErrorReporter {

    fun report(event: ErrorEvent)

    fun reportCrash(
        source: String,
        throwable: Throwable,
        extra: Map<String, String> = emptyMap(),
    ) = report(
        ErrorEvent(
            kind = ErrorKind.CRASH,
            source = source,
            message = throwable.message ?: throwable::class.java.simpleName,
            throwable = throwable,
            extra = extra,
        )
    )

    fun reportError(
        source: String,
        throwable: Throwable,
        extra: Map<String, String> = emptyMap(),
    ) = report(
        ErrorEvent(
            kind = ErrorKind.ERROR,
            source = source,
            message = throwable.message ?: throwable::class.java.simpleName,
            throwable = throwable,
            extra = extra,
        )
    )

    fun reportError(
        source: String,
        message: String,
        extra: Map<String, String> = emptyMap(),
    ) = report(ErrorEvent(kind = ErrorKind.ERROR, source = source, message = message, extra = extra))

    fun reportAnomaly(
        source: String,
        message: String,
        extra: Map<String, String> = emptyMap(),
    ) = report(ErrorEvent(kind = ErrorKind.ANOMALY, source = source, message = message, extra = extra))

    companion object {
        /** 上报时携带出错位置的自定义键名, 渠道后台按它聚合 */
        const val SOURCE_KEY = "source"
    }
}
