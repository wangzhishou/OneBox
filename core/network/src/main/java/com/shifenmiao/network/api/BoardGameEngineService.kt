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
