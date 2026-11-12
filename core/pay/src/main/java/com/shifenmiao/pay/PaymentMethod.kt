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

package com.shifenmiao.pay

import android.content.Context
import com.shifenmiao.model.pay.PayResult
import com.shifenmiao.model.pay.alipay.PayPrice

interface PaymentMethod<T> {
    val id: Int
    val displayName: String

    /**
     * 图标是否自带品牌文案与底板(支付宝/微信的 120x52 品牌徽标)。
     * 为 true 时 UI 只渲染徽标本身, 不再重复展示 [displayName]; 默认 false(纯图标, 需要文字补全含义)。
     */
    val iconIncludesLabel: Boolean
        get() = false

    fun setPaymentResultCallback(callback: PaymentResultCallback)
    fun getIcon(): Int
    fun pay(context: Context, payPrice: PayPrice, prePayResult: T)
    fun onPaySuccess(payResult: PayResult)
    fun onPayFailure(error: String)
    fun onPayIncomplete()
}