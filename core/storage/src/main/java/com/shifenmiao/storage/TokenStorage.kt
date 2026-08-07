package com.shifenmiao.storage

import com.shifenmiao.model.user.Login
import com.tencent.mmkv.MMKV

object TokenStorage {

    private val mmkv: MMKV = MMKV.mmkvWithID(MMKVName.TOKEN)
    private var loginCache: Login? = null

    private const val KEY_TOKEN = "key_token"

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