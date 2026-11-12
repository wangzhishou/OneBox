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

package com.wanbaohe.ocr.domain

/**
 * PP-OCRv6 small 的端侧推理约定。
 * 检测用 ImageNet 归一化（BGR），识别用 (x/255 - 0.5) / 0.5。
 * 这两套归一化来自官方 inference.yml，不能混用。
 */
object PpOcrSpec {
    const val PACK_ID = "ppocrv6-small-e47f928e"
    const val DEFAULT_PACK_URL = "https://images.oneboxable.com/models/$PACK_ID.zip"
    const val PACK_SHA256 = "e47f928e47d2058a917827a121a15e8fe5b98fd2a214e1a91584f984fb427a1e"

    const val DET_FILE = "det.onnx"
    const val REC_FILE = "rec.onnx"
    const val ORI_FILE = "ori.onnx"
    const val DICT_FILE = "dict.txt"

    const val DET_LIMIT = 960
    const val ALIGN = 32
    const val BIN_THRESH = 0.2
    const val BOX_THRESH = 0.45
    const val UNCLIP_RATIO = 1.4
    const val MIN_CONTOUR_AREA = 16.0
    const val MAX_BOXES = 120
    const val MAX_UNCLIP = 48

    const val REC_HEIGHT = 48
    const val REC_MIN_WIDTH = 8
    const val REC_MAX_WIDTH = 3200
    const val MIN_CONFIDENCE = 0.4f

    const val ORI_WIDTH = 160
    const val ORI_HEIGHT = 80
    const val ORI_FLIP_INDEX = 1
    const val ORI_FLIP_THRESH = 0.9f
    const val VERTICAL_RATIO = 1.5

    const val MEAN_B = 0.485f
    const val MEAN_G = 0.456f
    const val MEAN_R = 0.406f
    const val STD_B = 0.229f
    const val STD_G = 0.224f
    const val STD_R = 0.225f
    const val REC_SHIFT = 0.5f
    const val BYTE_SCALE = 255f

    const val BLANK_INDEX = 0
    const val INPUT_NAME = "x"

    val files = listOf(DET_FILE, REC_FILE, ORI_FILE, DICT_FILE)
}
