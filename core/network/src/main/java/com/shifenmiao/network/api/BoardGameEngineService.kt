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

package com.shifenmiao.network.api

import com.shifenmiao.model.ai.BoardGameEngineRequest
import com.shifenmiao.model.ai.BoardGameEngineResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * 服务端棋类引擎走棋（国际象棋 /gomoku 五子棋）。
 * 与 XiangqiEngineService 同模式:具体路径由调用方经 @Url 传入
 * （UrlConstants.CHESS_ENGINE_PROXY_PATH / GOMOKU_ENGINE_PROXY_PATH）。
 */
interface BoardGameEngineService {

    @POST
    fun bestMove(
        @Url url: String,
        @Body body: BoardGameEngineRequest,
    ): Call<BoardGameEngineResponse>
}
