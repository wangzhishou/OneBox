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

package com.wanbaohe.textcard.domain

/**
 * 上次自定义画布尺寸的持久化(Component 不直接碰 IO,经接口注入)。
 */
interface CustomCanvasStore {

    /** 上次输入的自定义宽高,null = 从未自定义过 */
    fun lastCustom(): Pair<Int, Int>?

    fun saveLastCustom(width: Int, height: Int)
}
