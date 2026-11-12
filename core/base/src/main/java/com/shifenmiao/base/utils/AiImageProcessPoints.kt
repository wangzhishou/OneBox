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
 * AI 图像处理单次积分成本:远程配置(RemoteConfig.aiImageProcessPoints)可动态调整,
 * 未下发时回退默认 [DEFAULT_AI_IMAGE_PROCESS_POINTS]。
 *
 * 自 markup-layers 下沉,供所有调用百度图像处理能力(markup-layers、证件照美化等)
 * 的模块统一取用;调用方自行定义积分来源 source(如 "markup_ai"、"id_photo_ai")。
 */
fun aiImageProcessPointsCost(): Int =
    RemoteConfigStorage.getRemoteConfig().aiImageProcessPoints ?: DEFAULT_AI_IMAGE_PROCESS_POINTS

/** AI 图像处理默认单次积分成本(远程未下发时) */
private const val DEFAULT_AI_IMAGE_PROCESS_POINTS = 200
