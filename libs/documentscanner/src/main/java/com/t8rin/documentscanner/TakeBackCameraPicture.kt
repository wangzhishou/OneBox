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
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract

/**
 * 与 [androidx.activity.result.contract.ActivityResultContracts.TakePicture] 相同,
 * 额外带上各家系统相机认的后置镜头参数。
 *
 * ACTION_IMAGE_CAPTURE 没有公开的镜头参数。国内相机(小米、华为、荣耀、OPPO、vivo)
 * 在参数缺省时默认打开前置。Camera1 的后置是 0,前置是 1。
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
        putExtra(EXTRA_CAMERA_FACING, CAMERA_FACING_BACK)
        putExtra(EXTRA_USE_FRONT_CAMERA, false)
        putExtra(EXTRA_ASSISTANT_USE_FRONT_CAMERA, false)
        putExtra(EXTRA_LENS_FACING_FRONT, 0)
        putExtra(EXTRA_SAMSUNG_CAMERA_FACING, "rear")
        putExtra(EXTRA_SAMSUNG_PREVIOUS_MODE, "Photo")

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
        const val EXTRA_CAMERA_FACING = "android.intent.extras.CAMERA_FACING"
        const val EXTRA_USE_FRONT_CAMERA = "android.intent.extra.USE_FRONT_CAMERA"
        const val EXTRA_ASSISTANT_USE_FRONT_CAMERA = "com.google.assistant.extra.USE_FRONT_CAMERA"
        const val EXTRA_LENS_FACING_FRONT = "android.intent.extras.LENS_FACING_FRONT"
        const val EXTRA_SAMSUNG_CAMERA_FACING = "camerafacing"
        const val EXTRA_SAMSUNG_PREVIOUS_MODE = "previous_mode"
        const val EXTRA_DEFAULT_CAMERA = "default_camera"
        const val EXTRA_DEFAULT_MODE = "default_mode"
    }
}
