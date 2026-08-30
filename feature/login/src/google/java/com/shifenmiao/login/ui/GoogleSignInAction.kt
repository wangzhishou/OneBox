@file:Suppress("DEPRECATION")

package com.shifenmiao.login.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("335920196432-dmphr6uuv6rjhvf0gh8cg3qjt23ivccu.apps.googleusercontent.com")
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                onIdToken(idToken)
            } else {
                ActionUtils.showToast("Google login failed: no id token")
            }
        } catch (e: ApiException) {
            if (e.statusCode != 12501) { // 12501 = USER_CANCELED
                ActionUtils.showToast("Google login failed: ${e.statusCode}")
            }
        }
    }

    return {
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }
}
