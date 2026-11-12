/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
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

package com.shifenmiao.base.utils

import com.shifenmiao.core.BuildConfig
import com.shifenmiao.core.constants.Constants
import com.shifenmiao.model.channel.NeedPrivacyPolicyDialog
import com.shifenmiao.model.remote.CanShowPermission
import com.shifenmiao.storage.AppSharedStorage
import kotlinx.coroutines.delay

object CoreUtils {

    fun isShowPrivacyPolicyDialog():Boolean {
        // 分渠道: google 渠道不要求首启隐私弹窗, 直接视为已同意
        if (!NeedPrivacyPolicyDialog.getConfigByFlavor().need) return false
        return AppSharedStorage.loadPrivacyPolicyVersion() < Constants.PRIVACY_POLICY_VERSION
    }

    /**
     * 阻塞直到用户同意隐私政策。用于需要在同意后才开始执行的敏感网络请求。
     */
    suspend fun awaitPrivacyPolicyAccepted() {
        while (isShowPrivacyPolicyDialog()) {
            delay(300)
        }
    }

    fun isOneBoxDebug():Boolean {
        return BuildConfig.DEBUG && BuildConfig.FLAVOR == "onebox"
    }

    fun isGoogleDebug():Boolean {
        return BuildConfig.DEBUG && BuildConfig.FLAVOR == "google"
    }

    fun isHuawei():Boolean {
        return CanShowPermission.getConfigByFlavor().canShow
    }
}