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

package com.wanbaohe.unitconverter.domain

import androidx.annotation.StringRes

/**
 * 亲属计算结果。
 * title 以资源 ID 形式返回，由 UI 层解析为本地化文案。
 */
sealed class KinshipTitle {
    data class Fixed(@StringRes val resId: Int) : KinshipTitle()
    data class ByGender(
        @StringRes val maleResId: Int,
        @StringRes val femaleResId: Int,
    ) : KinshipTitle()

    /** 映射未命中时，用链路步骤拼出描述性称谓 */
    data class Descriptive(val steps: List<KinshipStep>) : KinshipTitle()
    object Empty : KinshipTitle()
}

data class KinshipResult(
    val title: KinshipTitle,
    val steps: List<KinshipStep>,
)
