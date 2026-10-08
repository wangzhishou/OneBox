/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2026 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.t8rin.imagetoolbox.core.ui.utils.helper

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.compose.ui.graphics.vector.ImageVector
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.FolderOff
import com.t8rin.imagetoolbox.core.resources.icons.Save
import com.t8rin.imagetoolbox.core.ui.utils.confetti.ConfettiHostState
import com.t8rin.imagetoolbox.core.ui.utils.confetti.ConfettiIntensity
import com.t8rin.imagetoolbox.core.ui.utils.blessing.BlessingEffectHostState
import com.t8rin.imagetoolbox.core.ui.utils.blessing.BlessingEffectType
import com.t8rin.imagetoolbox.core.ui.widget.other.ActionToastVisuals
import com.t8rin.imagetoolbox.core.ui.widget.other.ToastDuration
import com.t8rin.imagetoolbox.core.ui.widget.other.ToastHostState
import com.t8rin.imagetoolbox.core.ui.widget.other.showFailureToast
import com.t8rin.imagetoolbox.core.utils.appContext
import com.t8rin.imagetoolbox.core.utils.getString
import com.wanbaohe.core.ui.review.ReviewPromptHost
import com.wanbaohe.core.ui.review.ReviewPromptTrigger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolderOff
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave

data object AppToastHost {

    private var context: CoroutineContext = Dispatchers.Main.immediate + SupervisorJob()
    private val scope by lazy { CoroutineScope(context) }

    val state = ToastHostState()

    val confettiState = ConfettiHostState()
    val blessingEffectState = BlessingEffectHostState()

    fun init(context: CoroutineContext) {
        this.context = context
    }

    fun showToast(
        message: String,
        icon: ImageVector? = null,
        duration: ToastDuration = ToastDuration.Short
    ) {
        scope.launch {
            state.showToast(
                message = message,
                icon = icon,
                duration = duration
            )
        }
    }

    fun showToast(
        message: Int,
        icon: ImageVector? = null,
        duration: ToastDuration = ToastDuration.Short
    ) {
        scope.launch {
            state.showToast(
                message = getString(message),
                icon = icon,
                duration = duration
            )
        }
    }

    fun showFailureToast(throwable: Throwable) {
        scope.launch {
            state.showFailureToast(
                throwable = throwable
            )
        }
    }

    fun showFailureToast(message: String) {
        scope.launch {
            state.showFailureToast(
                message = message
            )
        }
    }

    fun showFailureToast(res: Int) {
        scope.launch {
            state.showFailureToast(
                message = appContext.getString(res)
            )
        }
    }

    /**
     * 全局文件打开处理器,由根界面组装时注册(ImageToolboxCompositionLocals),
     * 内部走统一的 ContentRouter 决定打开方式。
     */
    @Volatile
    var fileOpenHandler: ((Uri) -> Unit)? = null

    fun showActionToast(
        message: String,
        actionLabel: String,
        onAction: () -> Unit,
        icon: ImageVector? = null,
        duration: ToastDuration = ToastDuration.Long
    ) {
        scope.launch {
            state.showToast(
                ActionToastVisualsImpl(
                    message = message,
                    icon = icon,
                    duration = duration,
                    actionLabel = actionLabel,
                    onAction = onAction
                )
            )
        }
    }

    /**
     * 文件保存成功提示:带「打开」按钮的快捷条,点击直接打开刚保存的文件。
     * Uri 为空或打开处理器未注册时退化为普通成功提示。
     * Toast 消失后把这次成功时刻报给 [ReviewPromptHost](google 渠道的应用内评分弹层,
     * 避免评分层与 Toast 互相遮挡)。
     */
    fun showFileSuccessToast(
        uri: Uri?,
        message: String,
        icon: ImageVector = Icons.Outlined.LineSave
    ) {
        val handler = fileOpenHandler
        scope.launch {
            if (uri == null || handler == null) {
                state.showToast(
                    message = message,
                    icon = icon,
                    duration = ToastDuration.Long
                )
            } else {
                state.showToast(
                    ActionToastVisualsImpl(
                        message = message,
                        icon = icon,
                        duration = ToastDuration.Long,
                        actionLabel = getString(R.string.open),
                        onAction = { handler(uri) }
                    )
                )
            }
            // Toast 消失后再上报,避免评分弹层与 Toast 互相遮挡;
            // 上报内部自带 runCatching,异常绝不能影响保存主流程
            ReviewPromptHost.notifySuccess(ReviewPromptTrigger.SAVE_SUCCESS)
        }
    }

    private class ActionToastVisualsImpl(
        override val message: String,
        override val icon: ImageVector?,
        override val duration: ToastDuration,
        override val actionLabel: String,
        override val onAction: () -> Unit
    ) : ActionToastVisuals

    fun dismissToasts() {
        state.currentToastData?.dismiss()
        confettiState.currentToastData?.dismiss()
        blessingEffectState.dismiss()
    }

    @JvmStatic
    fun showConfetti() {
        confettiState.showConfetti(ConfettiIntensity.Normal)
    }

    fun showConfetti(intensity: ConfettiIntensity) {
        confettiState.showConfetti(intensity)
    }

    fun showBlessingEffect(type: BlessingEffectType): Boolean {
        return blessingEffectState.show(type)
    }

    fun handleFileSystemFailure(throwable: Throwable) {
        when (throwable) {
            is ActivityNotFoundException -> showActivateFilesToast()
            else -> showFailureToast(throwable)
        }
    }

    const val PERMISSION = "REQUEST_PERMISSION"

    fun showActivateFilesToast() {
        showToast(
            message = appContext.getString(R.string.activate_files),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFolderOff,
            duration = ToastDuration.Long
        )
    }

}
