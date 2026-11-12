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

package com.shifenmiao.pay.google

/**
 * Play 商品详情的 SDK-free 视图, 供 main 源集 (PayComponent 等) 在不依赖
 * Play Billing SDK 的情况下消费商品信息。
 * 仅 google 渠道的真实 GooglePlayBilling 会填充非空列表。
 */
data class PlayProductInfo(
    val productId: String,
    val name: String,
    val formattedPrice: String,
)
