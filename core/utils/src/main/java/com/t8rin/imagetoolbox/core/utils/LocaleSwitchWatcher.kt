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

package com.t8rin.imagetoolbox.core.utils

import android.content.Context
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 语言切换守卫：Room 数据库与部分 MMKV 缓存按语言隔离（见 AppDatabase.dbNameForLocale /
 * localizedMmkv），但 Hilt @Singleton 绑定的 DB/DAO 只在进程创建时注入一次，
 * 因此运行中切换语言后必须冷重启进程才能整体切到新语言的数据源。
 *
 * 使用方式（主进程）：
 * - Application.onCreate 主进程路径调 [onProcessStart] 记录本进程启动时的语言；
 * - 启动期代码主动改写语言（如英文兜底）后调 [onLocaleOverriddenAtStartup]，避免误判；
 * - Application.onConfigurationChanged 调 [onConfigurationChanged]，
 *   语言在进程存活期间发生变化时冷重启。
 *
 * 比较基准是进程启动时的语言，因此全新进程（系统杀进程后重开）永远不会误触发重启。
 * 应用内语言选择与系统 per-app 语言设置（API 33+）两条路径都会经过 onConfigurationChanged，
 * 均被覆盖。
 */
object LocaleSwitchWatcher {

    private val restarting = AtomicBoolean(false)

    @Volatile
    private var processStartLocaleTag: String? = null

    fun onProcessStart() {
        processStartLocaleTag = LocaleUtils.getCurrentLocaleTag()
    }

    fun onLocaleOverriddenAtStartup(tag: String) {
        processStartLocaleTag = tag
        // 同时把 LocaleUtils 的进程级缓存对齐到新语言,
        // 避免后台初始化在配置回调到达前读到旧语言
        LocaleUtils.overrideCachedTagAtStartup(tag)
    }

    fun onConfigurationChanged(context: Context) {
        val startTag = processStartLocaleTag ?: return
        // 必须先刷新 LocaleUtils 的进程级缓存再比较, 否则拿到的还是旧语言
        val current = LocaleUtils.refreshCurrentLocaleTag()
        if (current == startTag) return
        if (!restarting.compareAndSet(false, true)) return
        AppRestarter.restartToColdStart(context.applicationContext)
    }

    /**
     * 应用内语言选择完成后直接冷重启（不依赖 onConfigurationChanged 回调——
     * 实测部分设备上应用内改语言该回调不触发/时机不定）。
     * 仅用于语言选择已被系统持久化的场景（API 33+ LocaleManager）；
     * 更低版本语言选择只在内存里，进程死亡会丢失选择，不能走这条路。
     */
    fun restartForLocaleSwitch(context: Context, newTag: String) {
        // 进程死亡前把 LocaleUtils 进程级缓存对齐到新语言, 避免临终读写落到旧语言文件
        LocaleUtils.overrideCachedTagAtStartup(newTag)
        if (!restarting.compareAndSet(false, true)) return
        AppRestarter.restartToColdStart(context.applicationContext)
    }
}
