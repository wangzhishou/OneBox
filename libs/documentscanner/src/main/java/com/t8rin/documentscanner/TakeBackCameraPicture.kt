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

package com.t8rin.documentscanner

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract

/**
 * 与 [androidx.activity.result.contract.ActivityResultContracts.TakePicture] 相同,
 * 额外带上各家系统相机认的后置镜头参数。
 *
 * ACTION_IMAGE_CAPTURE 没有公开的镜头参数,而国内相机(小米、华为、荣耀、OPPO、vivo、三星)
 * 在参数缺省时可能默认打开前置(S22/23/24 系列实测:不带参数必开前置)。这里把几套
 * 流传中的约定一起带上:
 * - Camera1 的 `android.intent.extras.CAMERA_FACING`:0 = 后置,1 = 前置;
 * - CameraCharacteristics 的 `android.intent.extras.LENS_FACING_BACK`:1 = 后置;
 * - 三星相机认的字符串 `camerafacing`:`"back"` = 后置,`"front"` = 前置(实测 `"rear"` 无效)。
 */
internal class TakeBackCameraPicture : ActivityResultContract<Uri, Boolean>() {

    override fun createIntent(context: Context, input: Uri): Intent {
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, input)
            putBackCameraExtras()
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return resultCode == Activity.RESULT_OK
    }

    private fun Intent.putBackCameraExtras() {
        // Camera1 约定
        putExtra(EXTRA_CAMERA_FACING, CAMERA_FACING_BACK)

        // Camera2 / CameraCharacteristics 约定
        putExtra(EXTRA_LENS_FACING_BACK, CameraCharacteristics.LENS_FACING_BACK)

        // Google 系相机与 Assistant 的开关
        putExtra(EXTRA_USE_FRONT_CAMERA, false)
        putExtra(EXTRA_ASSISTANT_USE_FRONT_CAMERA, false)

        // 三星等厂商相机读的字符串参数
        putExtra(EXTRA_OEM_CAMERA_FACING, OEM_CAMERA_FACING_BACK)

        when (Build.MANUFACTURER.lowercase()) {
            "huawei" -> {
                putExtra(EXTRA_DEFAULT_CAMERA, "0")
                putExtra(EXTRA_DEFAULT_MODE, "com.huawei.camera2.mode.photo.PhotoMode")
            }
            "honor" -> {
                putExtra(EXTRA_DEFAULT_CAMERA, "0")
                putExtra(EXTRA_DEFAULT_MODE, "com.hihonor.camera2.mode.photo.PhotoMode")
            }
        }
    }

    private companion object {
        const val CAMERA_FACING_BACK = 0
        const val OEM_CAMERA_FACING_BACK = "back"
        const val EXTRA_CAMERA_FACING = "android.intent.extras.CAMERA_FACING"
        const val EXTRA_LENS_FACING_BACK = "android.intent.extras.LENS_FACING_BACK"
        const val EXTRA_USE_FRONT_CAMERA = "android.intent.extra.USE_FRONT_CAMERA"
        const val EXTRA_ASSISTANT_USE_FRONT_CAMERA = "com.google.assistant.extra.USE_FRONT_CAMERA"
        const val EXTRA_OEM_CAMERA_FACING = "camerafacing"
        const val EXTRA_DEFAULT_CAMERA = "default_camera"
        const val EXTRA_DEFAULT_MODE = "default_mode"
    }
}
