package com.shifenmiao.ai.agent.tool

import com.shifenmiao.storage.TokenStorage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentToolLoginChecker @Inject constructor() {

    fun isLoggedIn(): Boolean {
        return TokenStorage.isLogin()
    }
}
