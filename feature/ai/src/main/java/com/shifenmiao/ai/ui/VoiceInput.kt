package com.shifenmiao.ai.ui

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.shifenmiao.ai.voice.VoiceInputEngine
import com.shifenmiao.ai.voice.resolveVoiceInputEngine
import com.shifenmiao.core.R
import com.shifenmiao.model.event.PermissionRequest
import com.shifenmiao.storage.RemoteConfigStorage
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.helper.ContextUtils
import java.util.Locale

/**
 * 语音输入:输入框为空时发送按钮切换为麦克风,点击后按远程配置分流:
 * - `voiceInput.provider` 为 "iflytek"(讯飞大模型识别)或 "self"(自建 FunASR,经网关反代)时:
 *   先申请录音权限,回调 [onShowIflytekSheet] 打开识别面板(两条链路的 UI 与状态流完全一致);
 * - 其余(默认/回退):唤起系统语音识别界面(Google 渠道即 Google 语音输入),结果经 [onResult] 回填。
 *   系统识别由系统服务持有录音权限,应用无需申请 RECORD_AUDIO(google 渠道 manifest 也未声明该权限)。
 */
@Composable
fun rememberVoiceInputLauncher(
    onResult: (String) -> Unit,
    onShowIflytekSheet: () -> Unit,
): () -> Unit {
    val context = LocalContext.current

    val recognitionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                ?.takeIf { it.isNotBlank() }
                ?.let(onResult)
        }
    }

    return remember(context, recognitionLauncher, onResult, onShowIflytekSheet) {
        {
            val engine = resolveVoiceInputEngine(RemoteConfigStorage.getRemoteConfig().voiceInput)
            if (engine != VoiceInputEngine.SYSTEM) {
                ContextUtils.requestPermissionAndExecute(
                    permissions = arrayOf(Manifest.permission.RECORD_AUDIO),
                    permissionRequest = PermissionRequest.MICROPHONE,
                    onGranted = onShowIflytekSheet
                )
            } else {
                launchSpeechRecognition(context) { intent ->
                    recognitionLauncher.launch(intent)
                }
            }
        }
    }
}

/** 系统语音识别是否可用(无 GMS / 去服务化的设备上不可用) */
fun isSystemSpeechRecognitionAvailable(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        SpeechRecognizer.isRecognitionAvailable(context)
    } else {
        @Suppress("DEPRECATION")
        context.packageManager.queryIntentActivities(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH),
            0
        ).isNotEmpty()
    }

private fun launchSpeechRecognition(
    context: Context,
    launch: (Intent) -> Unit
) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
        putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            context.getString(R.string.ai_input_voice_prompt)
        )
    }
    if (!isSystemSpeechRecognitionAvailable(context)) {
        AppToastHost.showToast(R.string.ai_input_voice_unavailable)
        return
    }
    try {
        launch(intent)
    } catch (e: ActivityNotFoundException) {
        AppToastHost.showToast(R.string.ai_input_voice_unavailable)
    }
}
