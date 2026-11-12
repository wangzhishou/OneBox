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

package com.shifenmiao.storage

import com.shifenmiao.model.user.Login
import com.tencent.mmkv.MMKV

object TokenStorage {

    private val mmkv: MMKV = MMKV.mmkvWithID(MMKVName.TOKEN)
    private var loginCache: Login? = null

    private const val KEY_TOKEN = "key_token"

    /** 联系方式验证门槛日期(yyyy-MM-dd): 此日期之前注册的账号豁免, 发版时按实际发布日调整 */
    private const val VERIFIED_REQUIRED_SINCE = "2026-09-02"

    fun saveTokenToLocalStorage(login: Login) {
        loginCache = login
        mmkv.encode(KEY_TOKEN, login, 60 * 60 * 24 * 365)
    }

    fun getLoginInfoFromLocalStorage(): Login? {
        val loginInfo = mmkv.decodeParcelable(KEY_TOKEN, Login::class.java)
        if (loginInfo != null) {
            loginCache = loginInfo
        }
        return loginInfo
    }

    fun getTokenFromLocalStorage(): String? {
        if (loginCache != null && loginCache?.jwt?.isNotEmpty() == true) {
            return loginCache!!.jwt
        }
        val login = getLoginInfoFromLocalStorage()
        if (login != null) {
            return login.jwt
        }
        return null
    }

    fun getLoginInfo(): Login? {
        if (loginCache == null) {
            loginCache = getLoginInfoFromLocalStorage()
        }
        return loginCache
    }

    fun getUserVipLevel(): Int {
        val loginInfo = getLoginInfo()
        return loginInfo?.user?.vipLevel ?: 0
    }

    fun getUserTotalRechargeAmount(): Double {
        val loginInfo = getLoginInfo()
        return loginInfo?.user?.totalRechargeAmount ?: 0.0
    }

    fun isLogin(): Boolean {
        return getLoginInfo() != null && getLoginInfo()!!.jwt.isNotEmpty()
    }

    fun isBindPhone(): Boolean {
        val loginInfo = getLoginInfo()
        return loginInfo?.user?.phone?.isNotEmpty() ?: false
    }

    /**
     * 是否已完成联系方式验证: 邮箱已验证(confirmed) 或 已绑定手机号。
     * 邮箱注册的用户 confirmed=false, 需验证邮箱或绑定手机后才可使用 AI 功能。
     * 老用户豁免: 功能上线前注册的账号不受限(此前一直可以聊天); 旧缓存没有 createdAt 字段,
     * 一律按老用户放行(fail-open 方向只影响存量用户, 新注册用户必带 createdAt)。
     */
    fun isVerified(): Boolean {
        val user = getLoginInfo()?.user ?: return false
        if (user.confirmed || !user.phone.isNullOrEmpty()) return true
        val createdDate = user.createdAt?.take(10) ?: return true
        return createdDate < VERIFIED_REQUIRED_SINCE
    }

    fun isWeChatUser(): Boolean {
        val loginInfo = getLoginInfo()
        return loginInfo?.user?.openid?.isNotEmpty() ?: false
    }

    fun clearLoginInfo() {
        loginCache = null
        mmkv.remove(KEY_TOKEN)
    }

    fun canConsumePoints(point: Int = 0): Boolean {
        getLoginInfo()?.let {
            return (it.user.points ?: 0) >= point
        }
        return false
    }
}