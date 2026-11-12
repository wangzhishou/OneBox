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

package com.shifenmiao.model.state

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 机器人相关的状态
 */
data class RobotState(
    /**
     * 机器人是否初始化完成
     */
    val isInitialized: Boolean = false,
    /**
     * 机器人大小
     */
    val size: Dp = 130.dp,
    /**
     * 是否禁用机器人
     */
    val disabled: Boolean = false,

    /**
     * 是否显示机器人
     */
    val visible: Boolean = false,

    /**
     * 机器人Y轴偏移量，使用百分比(0f-1f)代替固定Dp值
     */
    val offsetXPercent: Float = 0.68f,  // 屏幕宽度的70%
    val offsetYPercent: Float = 0.28f,  // Y轴负值,在屏幕顶部外面

    /**
     * 机器人缩放比例
     */
    val scale: Float = 1f,

    /**
     * 机器人是否正在被拖动
     */
    val isDragging: Boolean = false,

    /**
     * 机器人已经被拖动过
     */
    val hasBeenDragged: Boolean = false,

    /**
     * 机器人是否被点击了两次
     */
    val isDoubleClicked: Boolean = true,

    /**
     * Whether we already have an initial computed position from the current screen's AppBar.
     * Used to avoid showing the robot at (0,0) before the first positioning pass.
     */
    val hasComputedInitialPosition: Boolean = false,
)
