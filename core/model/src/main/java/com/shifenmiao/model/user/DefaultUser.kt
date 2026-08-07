package com.shifenmiao.model.user

import com.shifenmiao.core.R
import com.shifenmiao.interfaces.singleton.AppContext

object DefaultUser {
    val normal = User(
        username = AppContext.getContext().resources.getString(R.string.default_user_name),
        avatar = ""
    )
}