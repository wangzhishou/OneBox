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

package com.wanbaohe.cloud.storage.model

import androidx.annotation.StringRes
import com.wanbaohe.cloud.storage.R

/**
 * S3 兼容族子 vendor —— 仍属 S3 协议，由 [com.wanbaohe.cloud.storage.data.adapter.S3CompatAdapter]
 * 内部按 vendor 选择对应的 signer / endpoint 处理。
 */
enum class S3Vendor(
    @StringRes val titleRes: Int,
    val defaultEndpointHint: String,
) {
    ALIYUN_OSS(
        titleRes = R.string.cloud_vendor_aliyun_oss,
        defaultEndpointHint = "oss-cn-hangzhou.aliyuncs.com",
    ),
    TENCENT_COS(
        titleRes = R.string.cloud_vendor_tencent_cos,
        defaultEndpointHint = "cos.ap-guangzhou.myqcloud.com",
    ),
    HUAWEI_OBS(
        titleRes = R.string.cloud_vendor_huawei_obs,
        defaultEndpointHint = "obs.cn-north-4.myhuaweicloud.com",
    ),
    BAIDU_BOS(
        titleRes = R.string.cloud_vendor_baidu_bos,
        defaultEndpointHint = "s3.bj.bcebos.com",
    ),
    AWS_S3(
        titleRes = R.string.cloud_vendor_aws_s3,
        defaultEndpointHint = "s3.us-east-1.amazonaws.com",
    ),
    S3_COMPATIBLE(
        titleRes = R.string.cloud_vendor_s3_compatible,
        defaultEndpointHint = "play.min.io",
    ),
}
