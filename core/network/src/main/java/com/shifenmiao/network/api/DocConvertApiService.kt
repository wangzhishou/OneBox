package com.shifenmiao.network.api

import com.shifenmiao.core.constants.UrlConstants.BAIDU_DOC_CONVERT_PATH
import com.shifenmiao.core.constants.UrlConstants.BAIDU_DOC_CONVERT_REQUEST_RESULT_PATH
import com.shifenmiao.core.constants.UrlConstants.BAIDU_OCR_QUERY_RESULT_PATH
import com.shifenmiao.core.constants.UrlConstants.BAIDU_OCR_SUBMIT_TASK_PATH
import com.shifenmiao.model.ocr.DocConvertQueryResponse
import com.shifenmiao.model.ocr.DocConvertRequestResponse
import com.shifenmiao.model.ocr.PaddleOcrQueryResponse
import com.shifenmiao.model.ocr.PaddleOcrSubmitResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query

interface DocConvertApiService {

    // ==================== PaddleOCR-VL 文档解析 API ====================
    /**
     * 提交PaddleOCR-VL文档解析任务
     *
     * 提交请求接口 QPS 为 2
     *
     * @param accessToken 通过API Key和Secret Key获取的access_token
     * @param fileData 文件的base64编码数据（和fileUrl二选一）
     *                 - 支持格式：pdf、jpg、jpeg、png、bmp、tif、tiff
     *                 - 图片最长边不大于4096px
     *                 - 文档大小不超过100M，PDF最大支持500页
     *                 - 若文档大小超过50M，须使用fileUrl方式上传
     * @param fileUrl 文件数据URL（和fileData二选一）
     *                - URL长度不超过1024字节
     *                - PDF文档大小不超过100M，最大支持500页
     * @param fileName 文件名，请保证文件名后缀正确，例如 "1.pdf"
     * @param analysisChart 是否对统计图表进行解析
     */
    @FormUrlEncoded
    @POST(BAIDU_OCR_SUBMIT_TASK_PATH)
    suspend fun submitTask(
        @Query("access_token") accessToken: String,
        @Field("file_data") fileData: String? = null,
        @Field("file_url") fileUrl: String? = null,
        @Field("file_name") fileName: String,
        @Field("analysis_chart") analysisChart: Boolean? = null
    ): Response<PaddleOcrSubmitResponse>

    /**
     * 查询PaddleOCR-VL文档解析结果
     *
     * 获取结果接口 QPS 为 10
     *
     * @param accessToken 通过API Key和Secret Key获取的access_token
     * @param taskId 发送提交请求时返回的task_id
     */
    @FormUrlEncoded
    @POST(BAIDU_OCR_QUERY_RESULT_PATH)
    suspend fun queryResult(
        @Query("access_token") accessToken: String,
        @Field("task_id") taskId: String
    ): Response<PaddleOcrQueryResponse>

    @FormUrlEncoded
    @POST(BAIDU_DOC_CONVERT_PATH)
    suspend fun requestDocConvert(
        @Query("access_token") accessToken: String,
        @Field("image") image: String? = null,
        @Field("url") url: String? = null,
        @Field("pdf_file") pdfFile: String? = null,
        @Field("pdf_file_num") pdfFileNum: String? = null
    ): Response<DocConvertRequestResponse>

    @FormUrlEncoded
    @POST(BAIDU_DOC_CONVERT_REQUEST_RESULT_PATH)
    suspend fun getDocConvertResult(
        @Query("access_token") accessToken: String,
        @Field("task_id") taskId: String
    ): Response<DocConvertQueryResponse>
}

