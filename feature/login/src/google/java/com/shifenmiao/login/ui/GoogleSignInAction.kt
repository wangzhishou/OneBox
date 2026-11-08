@file:Suppress("DEPRECATION")

package com.shifenmiao.login.ui

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.shifenmiao.base.utils.ActionUtils

/**
 * 本机 Play 服务是否真能完成 Google 登录。
 * 渠道开关只说明这个包带了 GMS 依赖; 华为、纯净版和没装 Play 服务的机器上
 * 按钮点进去必失败, 调用方应据此把入口藏掉。
 * 从设置页启用 / 更新 Play 服务再回到登录页时重新查一次。
 */
@Composable
fun rememberGoogleSignInAvailable(): Boolean {
    val context = LocalContext.current
    var available by remember(context) { mutableStateOf(queryGooglePlayServices(context)) }
    LifecycleResumeEffect(context) {
        available = queryGooglePlayServices(context)
        onPauseOrDispose { }
    }
    return available
}

private fun queryGooglePlayServices(context: Context): Boolean = runCatching {
    GoogleApiAvailability.getInstance()
        .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
}.getOrDefault(false)

/**
 * google 渠道真实实现: 拉起 GMS Google 登录, 成功后回传 idToken。
 * 与 src/nogms 的 stub 签名保持一致, main 源集的 LoginOther 不直接 import GMS 类。
 */
@Composable
fun rememberGoogleSignInAction(
    onIdToken: (String) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val available = rememberGoogleSignInAvailable()
    // 没有 Play 服务时不要碰 Identity, 有的机型在这里直接抛, 会把整页登录一起打崩
    val signInClient = remember(context, available) {
        if (!available) null
        else runCatching { Identity.getSignInClient(context) }.getOrNull()
    }
    val signInRequest = remember {
        GetSignInIntentRequest.builder()
            .setServerClientId("335920196432-dmphr6uuv6rjhvf0gh8cg3qjt23ivccu.apps.googleusercontent.com")
            .build()
    }

    fun showFailure(error: Exception) {
        val statusCode = (error as? ApiException)?.statusCode
        if (statusCode != 12501) {
            ActionUtils.showToast("Google login failed: ${statusCode ?: error.message}")
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val client = signInClient ?: return@rememberLauncherForActivityResult
        try {
            val credential = result.data?.let { client.getSignInCredentialFromIntent(it) }
            val idToken = credential?.googleIdToken
            if (idToken != null) {
                onIdToken(idToken)
            } else {
                ActionUtils.showToast("Google login failed: no id token")
            }
        } catch (e: ApiException) {
            showFailure(e)
        }
    }

    return click@{
        val client = signInClient ?: return@click
        client.getSignInIntent(signInRequest)
            .addOnSuccessListener { pendingIntent ->
                googleSignInLauncher.launch(
                    IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                )
            }
            .addOnFailureListener(::showFailure)
    }
}
