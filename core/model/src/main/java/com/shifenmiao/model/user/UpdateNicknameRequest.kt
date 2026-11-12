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

package com.shifenmiao.model.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class UpdateNicknameRequest(
    val nickname: String
) : Parcelable

@Parcelize
@Serializable
data class ChangePasswordRequest(
    val code: String,
    val newPassword: String
) : Parcelable

@Parcelize
@Serializable
data class SendBindEmailCodeRequest(
    val email: String
) : Parcelable

@Parcelize
@Serializable
data class BindEmailRequest(
    val email: String,
    val code: String
) : Parcelable

@Parcelize
@Serializable
data class ConfirmEmailRequest(
    val code: String
) : Parcelable
