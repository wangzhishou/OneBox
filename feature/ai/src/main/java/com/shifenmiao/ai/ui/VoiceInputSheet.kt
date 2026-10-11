package com.shifenmiao.ai.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.shifenmiao.ai.voice.AsrState
import com.shifenmiao.ai.voice.VoiceRecognizer
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost

/**
 * 语音输入录音面板(讯飞大模型识别)。
 *
 * 打开后先经 [VoiceRecognizer.prepare] 向网关换签名 wss URL,再直连讯飞流式识别;
 * 中部麦克风按钮随 [VoiceRecognizer.rms] 做脉冲缩放,点击 = 提前说完;
 * 下滑/点外部 = 放弃(不回填);识别完成后结果经 [onResult] 回填并自动关闭。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceInputSheet(
    visible: Boolean,
    recognizer: VoiceRecognizer,
    onResult: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val state by recognizer.state.collectAsState()
    val rms by recognizer.rms.collectAsState()
    // 已终结(成功/失败)标记,防止重复回调 onResult/onDismiss
    var terminated by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // 先作废并清掉上一轮残留状态(例如面板被划走之后最终结果才到), 再开新一轮
        recognizer.cancel()
        val auth = recognizer.prepare()
        if (auth == null) {
            AppToastHost.showToast(R.string.ai_voice_error)
            onDismiss()
            return@LaunchedEffect
        }
        recognizer.start(auth.first, auth.second, scope)
        // 直接收集 start() 之后产生的状态: 不会再消费到打开瞬间捕获的旧值
        // (修 bug: 第二次打开面板时把上一次的识别结果又回填一遍)
        recognizer.state.collect { s ->
            if (terminated) return@collect
            when (s) {
                // 停顿处的本段定稿: 立刻回填输入框, 面板继续收音(继续说时前面的话不会被冲掉)
                is AsrState.SegmentFinal -> if (s.text.isNotBlank()) onResult(s.text)

                is AsrState.Finished -> {
                    terminated = true
                    if (s.text.isNotBlank()) onResult(s.text)
                    recognizer.cancel()
                    onDismiss()
                }

                is AsrState.Error -> {
                    terminated = true
                    recognizer.cancel()
                    // 联调期带上识别错误详情(如讯飞 11201: licc failed),方便定位
                    AppToastHost.showToast(
                        context.getString(R.string.ai_voice_error) + " (" + s.message + ")"
                    )
                    onDismiss()
                }

                else -> Unit
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            recognizer.cancel()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.ai_voice_listening),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 大号语音按钮:整颗按钮就是 Lottie 律动动画(96dp,与原圆形按钮同尺寸),
            // 不再叠加玻璃圆背景;按图层名重着色跟随主题(白色图形 → onPrimaryContainer,
            // 紫色律动块 → 主题主色系),整体随录音音量做 1.0~1.25 倍脉冲缩放
            val targetScale = 1f + rms.coerceIn(0f, 1f) * 0.25f
            val pulseScale by animateFloatAsState(targetValue = targetScale, label = "rmsPulse")
            val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(com.shifenmiao.ai.R.raw.voice_recording)
            )
            val lottieProgress by animateLottieCompositionAsState(
                composition = composition,
                isPlaying = state is AsrState.Listening,
                iterations = LottieConstants.IterateForever,
            )
            val colorScheme = MaterialTheme.colorScheme
            val voiceLottieColors = rememberLottieDynamicProperties(
                rememberLottieDynamicProperty(LottieProperty.COLOR, colorScheme.primaryContainer.toArgb(), "01"),
                rememberLottieDynamicProperty(LottieProperty.COLOR, colorScheme.tertiary.toArgb(), "02"),
                rememberLottieDynamicProperty(LottieProperty.COLOR, colorScheme.secondary.toArgb(), "03"),
                rememberLottieDynamicProperty(LottieProperty.COLOR, colorScheme.primaryContainer.toArgb(), "04"),
                rememberLottieDynamicProperty(
                    LottieProperty.COLOR,
                    colorScheme.primaryContainer.copy(alpha = 0.5f).toArgb(),
                    "transparent1"
                ),
                rememberLottieDynamicProperty(
                    LottieProperty.COLOR,
                    colorScheme.tertiary.copy(alpha = 0.5f).toArgb(),
                    "transparent2"
                ),
                rememberLottieDynamicProperty(LottieProperty.COLOR, colorScheme.onPrimaryContainer.toArgb(), "base"),
                rememberLottieDynamicProperty(LottieProperty.STROKE_COLOR, colorScheme.onPrimaryContainer.toArgb(), "pill top"),
                rememberLottieDynamicProperty(LottieProperty.STROKE_COLOR, colorScheme.onPrimaryContainer.toArgb(), "pill down"),
                rememberLottieDynamicProperty(LottieProperty.STROKE_COLOR, colorScheme.onPrimaryContainer.toArgb(), "Half rign"),
            )
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                    .size(96.dp)
                    .clickable { recognizer.finish() },
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { lottieProgress },
                    dynamicProperties = voiceLottieColors,
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics {
                            contentDescription = context.getString(R.string.ai_voice_tap_to_finish)
                        }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.ai_voice_tap_to_finish),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 流式中间结果,自动滚到底
            val partialText = (state as? AsrState.Listening)?.partialText.orEmpty()
            val scrollState = rememberScrollState()
            LaunchedEffect(partialText) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
            Text(
                text = partialText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp, max = 120.dp)
                    .verticalScroll(scrollState)
            )
        }
    }
}
