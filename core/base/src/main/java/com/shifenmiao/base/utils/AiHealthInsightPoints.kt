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

package com.shifenmiao.base.utils

import com.shifenmiao.storage.RemoteConfigStorage

/**
 * AI 解读类能力(如健康记录「AI 解读」)单次积分成本:
 * 远程配置(RemoteConfig.aiHealthInsightPoints)可动态调整,
 * 未下发时回退默认 [DEFAULT_AI_HEALTH_INSIGHT_POINTS]。
 *
 * 与 [aiImageProcessPointsCost] 同款约定;调用方自行定义积分来源 source。
 */
fun aiHealthInsightPointsCost(): Int =
    RemoteConfigStorage.getRemoteConfig().aiHealthInsightPoints ?: DEFAULT_AI_HEALTH_INSIGHT_POINTS

/** AI 解读默认单次积分成本(远程未下发时) */
private const val DEFAULT_AI_HEALTH_INSIGHT_POINTS = 200
