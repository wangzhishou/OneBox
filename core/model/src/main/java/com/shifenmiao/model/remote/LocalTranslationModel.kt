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

package com.shifenmiao.model.remote

object LocalTranslationModel {
    const val FILE_NAME = "Hy-MT2-1.8B_int8.litertlm"
    const val MODEL_NAME = "Hy-MT2-1.8B_int8"
    const val DISPLAY_NAME = "Hy-MT2 1.8B"

    /**
     * 兜底下载地址：万宝盒 R2 镜像（与 gemma3 / Qwen3 同一 `models/` 前缀）。
     *
     * 线上地址以远程配置 `localTranslationModelUrl` 为准（见 DefaultLocalModelRepository，
     * 未下发或为空才回退到这里），这里只是配置取不到时的兜底；官方来源是 LiteRT 社区的
     * HuggingFace 仓库（litert-community/Hy-MT2-1.8B），国内不可直连。
     */
    const val DEFAULT_DOWNLOAD_URL =
        "https://images.oneboxable.com/models/Hy-MT2-1.8B_int8.litertlm"
}
