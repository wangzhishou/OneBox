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

package com.shifenmiao.model.ai

import java.util.concurrent.ConcurrentHashMap

/**
 * AI 引擎的全局内存缓存（进程级单例）
 *
 * 设计要点：
 * - 纯内存，无磁盘 IO，无 MMKV 依赖
 * - 由 AIEngineManager 在数据库加载完成后写入
 * - 供网络拦截器等无法注入 Hilt 的组件同步读取
 * - ConcurrentHashMap 保证线程安全
 * - 未命中缓存时 fallback 到 AiEngine.getDefaultEngine() 硬编码默认值
 */
object AIEngineProvider {

    private val cache = ConcurrentHashMap<String, AiEngine>()

    /** 写入单个引擎配置（AIEngineManager 调用） */
    fun put(engine: AiEngine) {
        cache[engine.name] = engine
    }

    /** 批量写入引擎配置 */
    fun putAll(engines: List<AiEngine>) {
        engines.forEach { cache[it.name] = it }
    }

    /** 获取引擎配置（拦截器调用），未命中则返回硬编码默认值 */
    fun get(providerName: String): AiEngine {
        return cache[providerName] ?: AiEngine.defaultEngine()
    }

    /** 清空缓存（登出/清数据时调用） */
    fun clear() {
        cache.clear()
    }

}

