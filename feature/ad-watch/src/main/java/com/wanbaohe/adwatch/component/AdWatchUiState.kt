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

package com.wanbaohe.adwatch.component

data class AdWatchUiState(
    /** 单次观看奖励积分(来自远程配置, 默认 15) */
    val rewardPoints: Int = 15,
    /** 今日已观看次数 */
    val watchedToday: Int = 0,
    /** 今日剩余次数 */
    val remainingToday: Int = 10,
    /** 已看完、尚未发到账上的积分(未登录时先攒着, 登录或注册成功再发) */
    val unclaimedPoints: Int = 0,
    /** 是否正在展示广告(禁用按钮防重复点击) */
    val isShowing: Boolean = false,
)
