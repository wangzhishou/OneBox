@file:Suppress("DEPRECATION")

package com.shifenmiao.login.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.shifenmiao.base.utils.ActionUtils

/**
 * google 渠道真实实现: 拉起 GMS Google 登录, 成功后回传 idToken。
 * 与 src/nogms 的 stub 签名保持一致, main 源集的 LoginOther 不直接 import GMS 类。
 */
@Composable
fun rememberGoogleSignInAction(
    onIdToken: (String) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val signInClient = remember { Identity.getSignInClient(context) }
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
        try {
            val credential = result.data?.let(signInClient::getSignInCredentialFromIntent)
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

    return {
        signInClient.getSignInIntent(signInRequest)
            .addOnSuccessListener { pendingIntent ->
                googleSignInLauncher.launch(
                    IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                )
            }
            .addOnFailureListener(::showFailure)
    }
}
