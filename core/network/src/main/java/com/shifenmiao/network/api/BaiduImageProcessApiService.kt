package com.shifenmiao.network.api

import com.shifenmiao.core.constants.UrlConstants.BAIDU_IMAGE_PROCESS_COLOR_ENHANCE_PATH
import com.shifenmiao.core.constants.UrlConstants.BAIDU_IMAGE_PROCESS_CONTRAST_ENHANCE_PATH
import com.shifenmiao.core.constants.UrlConstants.BAIDU_IMAGE_PROCESS_DEFINITION_ENHANCE_PATH
import com.shifenmiao.core.constants.UrlConstants.BAIDU_IMAGE_PROCESS_DEHAZE_PATH
import com.shifenmiao.core.constants.UrlConstants.BAIDU_IMAGE_PROCESS_DOC_REPAIR_PATH
import com.shifenmiao.core.constants.UrlConstants.BAIDU_IMAGE_PROCESS_INPAINTING_PATH
import com.shifenmiao.core.constants.UrlConstants.BAIDU_IMAGE_PROCESS_QUALITY_ENHANCE_PATH
import com.shifenmiao.core.constants.UrlConstants.BAIDU_IMAGE_PROCESS_REMOVE_MOIRE_PATH
import com.shifenmiao.core.constants.UrlConstants.BAIDU_IMAGE_PROCESS_SEGMENT_PATH
import com.shifenmiao.core.constants.UrlConstants.BAIDU_IMAGE_PROCESS_STRETCH_RESTORE_PATH
import com.shifenmiao.model.imageprocess.ImageProcessResponse
import com.shifenmiao.model.imageprocess.ImageSegmentRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * 百度图像处理 API(经 Go 网关代理,与 [DocConvertApiService] 同模式:
 * access_token 由网关替换为真实百度 token,App 侧占位传入即可)。
 *
 * 除智能抠图(application/json)外均为 form-urlencoded,图片 base64 经
 * @Field 自动 URLEncode;base64 后 ≤10M,最长边 ≤3000px(抠图最短边 ≥128px)。
 */
interface BaiduImageProcessApiService {

    /** 图像去雾 */
    @FormUrlEncoded
    @POST(BAIDU_IMAGE_PROCESS_DEHAZE_PATH)
    suspend fun dehaze(
        @Query("access_token") accessToken: String,
        @Field("image") image: String
    ): Response<ImageProcessResponse>

    /** 对比度增强 */
    @FormUrlEncoded
    @POST(BAIDU_IMAGE_PROCESS_CONTRAST_ENHANCE_PATH)
    suspend fun contrastEnhance(
        @Query("access_token") accessToken: String,
        @Field("image") image: String
    ): Response<ImageProcessResponse>

    /** 图像无损放大 */
    @FormUrlEncoded
    @POST(BAIDU_IMAGE_PROCESS_QUALITY_ENHANCE_PATH)
    suspend fun imageQualityEnhance(
        @Query("access_token") accessToken: String,
        @Field("image") image: String
    ): Response<ImageProcessResponse>

    /** 拉伸图像恢复 */
    @FormUrlEncoded
    @POST(BAIDU_IMAGE_PROCESS_STRETCH_RESTORE_PATH)
    suspend fun stretchRestore(
        @Query("access_token") accessToken: String,
        @Field("image") image: String
    ): Response<ImageProcessResponse>

    /**
     * 图像修复(消除指定区域并智能补全)
     *
     * @param rectangle 修复区域 JSON 数组,如 [{"width":w,"height":h,"top":y,"left":x}],
     *                  坐标为所传图片的像素坐标
     */
    @FormUrlEncoded
    @POST(BAIDU_IMAGE_PROCESS_INPAINTING_PATH)
    suspend fun inpainting(
        @Query("access_token") accessToken: String,
        @Field("image") image: String,
        @Field("rectangle") rectangle: String
    ): Response<ImageProcessResponse>

    /** 图像清晰度增强 */
    @FormUrlEncoded
    @POST(BAIDU_IMAGE_PROCESS_DEFINITION_ENHANCE_PATH)
    suspend fun imageDefinitionEnhance(
        @Query("access_token") accessToken: String,
        @Field("image") image: String
    ): Response<ImageProcessResponse>

    /** 图像色彩增强 */
    @FormUrlEncoded
    @POST(BAIDU_IMAGE_PROCESS_COLOR_ENHANCE_PATH)
    suspend fun colorEnhance(
        @Query("access_token") accessToken: String,
        @Field("image") image: String
    ): Response<ImageProcessResponse>

    /** 图片去摩尔纹 */
    @FormUrlEncoded
    @POST(BAIDU_IMAGE_PROCESS_REMOVE_MOIRE_PATH)
    suspend fun removeMoire(
        @Query("access_token") accessToken: String,
        @Field("image") image: String
    ): Response<ImageProcessResponse>

    /** 文档图片去底纹 */
    @FormUrlEncoded
    @POST(BAIDU_IMAGE_PROCESS_DOC_REPAIR_PATH)
    suspend fun docRepair(
        @Query("access_token") accessToken: String,
        @Field("image") image: String
    ): Response<ImageProcessResponse>

    /** 智能抠图:唯一 application/json 接口,结果图 base64 在 foreground 字段 */
    @POST(BAIDU_IMAGE_PROCESS_SEGMENT_PATH)
    suspend fun segment(
        @Query("access_token") accessToken: String,
        @Body request: ImageSegmentRequest
    ): Response<ImageProcessResponse>
}
