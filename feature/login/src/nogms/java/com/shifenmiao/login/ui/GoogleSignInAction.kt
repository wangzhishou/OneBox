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

package com.shifenmiao.login.ui

import androidx.compose.runtime.Composable
import com.shifenmiao.base.utils.ActionUtils

/**
 * 非 google 渠道 (国内 + foss) stub: 不打包 GMS, 登录入口不可用。
 * 与 src/google 的真实实现签名保持一致, main 源集据此隐藏按钮。
 */
@Composable
fun rememberGoogleSignInAvailable(): Boolean = false

@Composable
fun rememberGoogleSignInAction(
    onIdToken: (String) -> Unit,
): () -> Unit = {
    ActionUtils.showToast("Google login is not available in this build")
}
