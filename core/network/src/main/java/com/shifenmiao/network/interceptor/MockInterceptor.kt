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

package com.shifenmiao.network.interceptor


import okhttp3.Interceptor
import okhttp3.Interceptor.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import javax.inject.Inject


//class MockInterceptor @Inject constructor(
//    private val onlineRepository: OnlineRepository
//) : Interceptor {
//    @Throws(IOException::class)
//    override fun intercept(chain: Chain): Response {
//        val responseString = onlineRepository.getMockJson("mock/items.json")
//        return Response.Builder()
//            .code(200)
//            .message(responseString)
//            .request(chain.request())
//            .protocol(Protocol.HTTP_1_0)
//            .body(responseString.toByteArray().toResponseBody("application/json".toMediaType()))
//            .addHeader("content-type", "application/json")
//            .build()
//    }
//}