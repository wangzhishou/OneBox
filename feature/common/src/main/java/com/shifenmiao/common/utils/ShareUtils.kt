package com.shifenmiao.common.utils

import com.shifenmiao.base.utils.ImageUtils
import com.shifenmiao.core.R
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.wechat.Wechat
import com.shifenmiao.model.wechat.common.ImageObject
import com.shifenmiao.model.wechat.common.MediaMessage
import com.shifenmiao.model.wechat.common.WXScene

object ShareUtils {
    fun shareWechatQRCode() {
        if (!Wechat.isEnabled) return
        val bitmap =
            ImageUtils.resourceToBitmap(R.drawable.wechat_qrcode)
        Wechat.share(
            mediaMessage = MediaMessage(
                title = AppContext.getString(R.string.wechat_login_tips),
                description = "",
                thumbData = null,
                mediaObject = ImageObject(
                    imageData = ImageUtils.bitmapToByteArray(
                        bitmap
                    )
                ),
            ),
            scene = WXScene.Session
        )
    }
}