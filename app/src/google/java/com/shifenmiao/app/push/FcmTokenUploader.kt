package com.shifenmiao.app.push

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.shifenmiao.network.api.ApiService
import com.shifenmiao.network.model.notification.FcmTokenRequest
import com.shifenmiao.storage.TokenStorage
import com.tencent.mmkv.MMKV
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * FCM token 上报器(仅 google 渠道编译, main 源集经反射调用)。
 *
 * - 已登录: 直接把 token POST 到 user/fcm-token, MMKV 去重(token 未变且已上报过则跳过)
 * - 未登录: token 存 MMKV(pending), 待登录后由 [tryUpload] 补报
 * - 失败一律静默, 不影响主流程
 *
 * 反射调用约定(参照 GoogleChannelInitializer): Kotlin object, 取 INSTANCE 后调实例方法。
 */
object FcmTokenUploader {

    private const val TAG = "FcmTokenUploader"
    private const val MMKV_ID = "fcm_push"
    private const val KEY_PENDING_TOKEN = "pending_token"
    private const val KEY_UPLOADED_TOKEN = "uploaded_token"

    private val mmkv: MMKV get() = MMKV.mmkvWithID(MMKV_ID)

    /** 冷启动 / 登录成功后补报: 拉当前 token, 已登录且未上报过则上传 */
    fun tryUpload(context: Context) {
        runCatching {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    if (!token.isNullOrBlank()) {
                        maybeUpload(context.applicationContext, token)
                    }
                }
                .addOnFailureListener {
                    Log.w(TAG, "fetch fcm token failed", it)
                }
        }.onFailure {
            Log.w(TAG, "FirebaseMessaging unavailable", it)
        }
    }

    /** Service onNewToken 回调: 已登录直报, 未登录存 MMKV 待补报 */
    fun onNewToken(context: Context, token: String) {
        if (token.isBlank()) return
        maybeUpload(context.applicationContext, token)
    }

    private fun maybeUpload(context: Context, token: String) {
        if (!TokenStorage.isLogin()) {
            mmkv.encode(KEY_PENDING_TOKEN, token)
            return
        }
        if (mmkv.decodeString(KEY_UPLOADED_TOKEN) == token) return
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val apiService = EntryPointAccessors.fromApplication(
                    context,
                    FcmPushEntryPoint::class.java,
                ).apiService()
                val response = apiService.uploadFcmToken(FcmTokenRequest(fcmToken = token))
                if (response.isSuccessful) {
                    mmkv.encode(KEY_UPLOADED_TOKEN, token)
                    mmkv.remove(KEY_PENDING_TOKEN)
                } else {
                    // 留着 pending, 下次冷启动/登录再补
                    mmkv.encode(KEY_PENDING_TOKEN, token)
                }
            }.onFailure {
                Log.w(TAG, "upload fcm token failed", it)
                mmkv.encode(KEY_PENDING_TOKEN, token)
            }
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FcmPushEntryPoint {
    fun apiService(): ApiService
}
