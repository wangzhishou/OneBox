package com.shifenmiao.feature.document.repository

import android.util.Base64
import com.google.gson.Gson
import com.shifenmiao.database.ocr.dao.PaddleOcrTaskDao
import com.shifenmiao.database.ocr.entity.PaddleOcrTaskEntity
import com.shifenmiao.model.ocr.OcrTaskStatus
import com.shifenmiao.model.ocr.PaddleOcrParseResult
import com.shifenmiao.model.ocr.PaddleOcrQueryResponse
import com.shifenmiao.model.ocr.PaddleOcrQueryResult
import com.shifenmiao.model.ocr.PaddleOcrSubmitResponse
import com.shifenmiao.network.api.DocConvertApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.InputStream
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 百度 PaddleOCR-VL 文档解析仓库
 *
 * 处理异步 OCR 任务的提交和结果轮询
 */
@Singleton
class PaddleOcrRepository @Inject constructor(
    private val docConvertService: DocConvertApiService,
    private val paddleOcrTaskDao: PaddleOcrTaskDao
) {

    /**
     * Gson 实例用于 JSON 序列化和反序列化
     */
    val gson = Gson()

    companion object {
        /** 默认轮询间隔（毫秒） */
        private const val DEFAULT_POLL_INTERVAL_MS = 5000L

        /** 最大轮询次数 */
        private const val MAX_POLL_COUNT = 120

        /** 最大文件大小（50MB），超过需要使用 URL 方式 */
        private const val MAX_FILE_DATA_SIZE = 50 * 1024 * 1024L
    }

    /**
     * OCR 解析进度状态
     */
    sealed class OcrProgress {
        /** 任务已提交，正在等待处理 */
        data class Submitted(val taskId: String) : OcrProgress()

        /** 任务排队中 */
        data class Pending(val taskId: String) : OcrProgress()

        /** 任务处理中 */
        data class Processing(val taskId: String) : OcrProgress()

        /** 任务完成 */
        data class Completed(
            val taskId: String,
            val markdownUrl: String?,
            val parseResultUrl: String?
        ) : OcrProgress()

        /** 任务失败 */
        data class Failed(
            val taskId: String?,
            val errorCode: Int,
            val errorMsg: String?
        ) : OcrProgress()
    }

    /**
     * 提交 OCR 解析任务
     *
     * @param accessToken 百度 AI 平台的 access_token
     * @param file 要解析的文件
     * @param analysisChart 是否解析统计图表
     * @return 提交结果
     */
    suspend fun submitTask(
        accessToken: String,
        file: File,
        analysisChart: Boolean? = null
    ): Result<PaddleOcrSubmitResponse> {
        return runCatching {
            val fileName = file.name

            // 文件大小超过 50MB 时，应该使用 URL 方式上传
            if (file.length() > MAX_FILE_DATA_SIZE) {
                throw IllegalArgumentException(
                    "文件大小超过 50MB，请使用 submitTaskWithUrl 方法通过 URL 上传"
                )
            }

            val fileData = Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)

            val response = docConvertService.submitTask(
                accessToken = accessToken,
                fileData = fileData,
                fileName = fileName,
                analysisChart = analysisChart
            )

            if (response.isSuccessful) {
                response.body() ?: throw Exception("响应体为空")
            } else {
                throw Exception("请求失败: ${response.code()} ${response.message()}")
            }
        }
    }

    /**
     * 通过 InputStream 提交 OCR 解析任务
     *
     * @param accessToken 百度 AI 平台的 access_token
     * @param inputStream 文件输入流
     * @param fileName 文件名（需要正确的后缀，如 "document.pdf"）
     * @param analysisChart 是否解析统计图表
     * @return 提交结果
     */
    suspend fun submitTask(
        accessToken: String,
        inputStream: InputStream,
        fileName: String,
        analysisChart: Boolean? = null
    ): Result<PaddleOcrSubmitResponse> {
        return runCatching {
            val bytes = inputStream.readBytes()

            if (bytes.size > MAX_FILE_DATA_SIZE) {
                throw IllegalArgumentException(
                    "文件大小超过 50MB，请使用 submitTaskWithUrl 方法通过 URL 上传"
                )
            }

            val fileData = Base64.encodeToString(bytes, Base64.NO_WRAP)

            val response = docConvertService.submitTask(
                accessToken = accessToken,
                fileData = fileData,
                fileName = fileName,
                analysisChart = analysisChart
            )

            if (response.isSuccessful) {
                response.body() ?: throw Exception("响应体为空")
            } else {
                throw Exception("请求失败: ${response.code()} ${response.message()}")
            }
        }
    }

    /**
     * 通过 URL 提交 OCR 解析任务（适用于大文件）
     *
     * @param accessToken 百度 AI 平台的 access_token
     * @param fileUrl 文件的公开访问 URL
     * @param fileName 文件名（需要正确的后缀，如 "document.pdf"）
     * @param analysisChart 是否解析统计图表
     * @return 提交结果
     */
    suspend fun submitTaskWithUrl(
        accessToken: String,
        fileUrl: String,
        fileName: String,
        analysisChart: Boolean? = null
    ): Result<PaddleOcrSubmitResponse> {
        return runCatching {
            val response = docConvertService.submitTask(
                accessToken = accessToken,
                fileUrl = fileUrl,
                fileName = fileName,
                analysisChart = analysisChart
            )

            if (response.isSuccessful) {
                response.body() ?: throw Exception("响应体为空")
            } else {
                throw Exception("请求失败: ${response.code()} ${response.message()}")
            }
        }
    }

    /**
     * 查询 OCR 解析结果
     *
     * @param accessToken 百度 AI 平台的 access_token
     * @param taskId 任务 ID
     * @return 查询结果
     */
    suspend fun queryResult(
        accessToken: String,
        taskId: String
    ): Result<PaddleOcrQueryResponse> {
        return runCatching {
            val response = docConvertService.queryResult(
                accessToken = accessToken,
                taskId = taskId
            )

            if (response.isSuccessful) {
                response.body() ?: throw Exception("响应体为空")
            } else {
                throw Exception("请求失败: ${response.code()} ${response.message()}")
            }
        }
    }

    /**
     * 提交任务并轮询结果
     *
     * 返回 Flow，发射任务进度状态
     *
     * @param accessToken 百度 AI 平台的 access_token
     * @param file 要解析的文件
     * @param analysisChart 是否解析统计图表
     * @param pollIntervalMs 轮询间隔（毫秒），默认 5 秒
     * @param maxPollCount 最大轮询次数，默认 120 次（10 分钟）
     */
    fun submitAndPollResult(
        accessToken: String,
        file: File,
        analysisChart: Boolean? = null,
        pollIntervalMs: Long = DEFAULT_POLL_INTERVAL_MS,
        maxPollCount: Int = MAX_POLL_COUNT
    ): Flow<OcrProgress> = flow {
        // 1. 提交任务
        val submitResult = submitTask(accessToken, file, analysisChart)

        val submitResponse = submitResult.getOrElse { e ->
            emit(OcrProgress.Failed(null, -1, e.message))
            return@flow
        }

        if (!submitResponse.isSuccess()) {
            emit(OcrProgress.Failed(
                null,
                submitResponse.errorCode,
                submitResponse.errorMsg
            ))
            return@flow
        }

        val taskId = submitResponse.result!!.taskId
        emit(OcrProgress.Submitted(taskId))

        // 2. 轮询结果
        pollForResult(accessToken, taskId, pollIntervalMs, maxPollCount)
    }

    /**
     * 通过 URL 提交任务并轮询结果
     */
    fun submitWithUrlAndPollResult(
        accessToken: String,
        fileUrl: String,
        fileName: String,
        analysisChart: Boolean? = null,
        pollIntervalMs: Long = DEFAULT_POLL_INTERVAL_MS,
        maxPollCount: Int = MAX_POLL_COUNT
    ): Flow<OcrProgress> = flow {
        // 1. 提交任务
        val submitResult = submitTaskWithUrl(accessToken, fileUrl, fileName, analysisChart)

        val submitResponse = submitResult.getOrElse { e ->
            emit(OcrProgress.Failed(null, -1, e.message))
            return@flow
        }

        if (!submitResponse.isSuccess()) {
            emit(OcrProgress.Failed(
                null,
                submitResponse.errorCode,
                submitResponse.errorMsg
            ))
            return@flow
        }

        val taskId = submitResponse.result!!.taskId
        emit(OcrProgress.Submitted(taskId))

        // 2. 轮询结果
        pollForResult(accessToken, taskId, pollIntervalMs, maxPollCount)
    }

    /**
     * 轮询任务结果的内部实现
     */
    private suspend fun FlowCollector<OcrProgress>.pollForResult(
        accessToken: String,
        taskId: String,
        pollIntervalMs: Long,
        maxPollCount: Int
    ) {
        var pollCount = 0

        while (pollCount < maxPollCount) {
            delay(pollIntervalMs)
            pollCount++

            val queryResult = queryResult(accessToken, taskId)

            val queryResponse = queryResult.getOrElse { e ->
                emit(OcrProgress.Failed(taskId, -1, e.message))
                return
            }

            if (!queryResponse.isSuccess()) {
                emit(OcrProgress.Failed(
                    taskId,
                    queryResponse.errorCode,
                    queryResponse.errorMsg
                ))
                return
            }

            val result = queryResponse.result ?: continue

            when (result.status) {
                OcrTaskStatus.PENDING.value -> {
                    emit(OcrProgress.Pending(taskId))
                }
                OcrTaskStatus.PROCESSING.value -> {
                    emit(OcrProgress.Processing(taskId))
                }
                OcrTaskStatus.SUCCESS.value -> {
                    emit(OcrProgress.Completed(
                        taskId = taskId,
                        markdownUrl = result.markdownUrl,
                        parseResultUrl = result.parseResultUrl
                    ))
                    return
                }
                OcrTaskStatus.FAILED.value -> {
                    emit(OcrProgress.Failed(
                        taskId = taskId,
                        errorCode = queryResponse.errorCode,
                        errorMsg = result.taskError ?: queryResponse.errorMsg
                    ))
                    return
                }
            }
        }

        // 超过最大轮询次数
        emit(OcrProgress.Failed(taskId, -2, "轮询超时，请稍后重试"))
    }

    /**
     * 下载并解析 OCR 结果 JSON
     *
     * @param parseResultUrl parse_result_url 链接
     * @return 解析结果
     */
    fun downloadParseResult(parseResultUrl: String): Result<PaddleOcrParseResult> {
        return runCatching {
            val url = URL(parseResultUrl)
            val connection = url.openConnection()
            connection.connectTimeout = 30000
            connection.readTimeout = 30000

            val jsonText = connection.getInputStream().bufferedReader().use { it.readText() }
            gson.fromJson(jsonText, PaddleOcrParseResult::class.java)
        }
    }

    /**
     * 下载 Markdown 结果
     *
     * @param markdownUrl markdown_url 链接
     * @return Markdown 文本
     */
    fun downloadMarkdown(markdownUrl: String): Result<String> {
        return runCatching {
            val url = URL(markdownUrl)
            val connection = url.openConnection()
            connection.connectTimeout = 30000
            connection.readTimeout = 30000

            connection.getInputStream().bufferedReader().use { it.readText() }
        }
    }

    /**
     * 提交 OCR 解析任务并保存到数据库 (通用方法，支持 InputStream)
     *
     * @param accessToken 百度 AI 平台的 access_token
     * @param inputStream 文件输入流
     * @param fileName 文件名
     * @param analysisChart 是否解析统计图表
     * @return 任务ID
     */
    suspend fun submitFile(
        accessToken: String,
        inputStream: InputStream,
        fileName: String,
        analysisChart: Boolean? = null
    ): String? {
        val result = submitTask(accessToken, inputStream, fileName, analysisChart)
        return if (result.isSuccess && result.getOrNull()?.isSuccess() == true) {
            val response = result.getOrNull()!!
            val taskId = response.result!!.taskId
            // 这里我们只返回 taskId，具体的数据库插入操作由调用者决定，或者在这里进行
            // 为了保持灵活性，这里只返回 taskId，数据库操作在 Component 中进行
            // 或者我们可以重载一个带 save 的版本
            taskId
        } else {
            null
        }
    }

    suspend fun submitFileDetailed(
        accessToken: String,
        inputStream: InputStream,
        fileName: String,
        analysisChart: Boolean? = null
    ): Result<String> {
        val submitResult = submitTask(accessToken, inputStream, fileName, analysisChart)
        val response = submitResult.getOrElse { e ->
            return Result.failure(e)
        }

        if (!response.isSuccess()) {
            val msg = response.errorMsg?.takeIf { it.isNotBlank() }
                ?: "提交失败（错误码：${response.errorCode}）"
            return Result.failure(IllegalStateException(msg))
        }

        return Result.success(response.result!!.taskId)
    }

    suspend fun insertTask(task: PaddleOcrTaskEntity) {
        paddleOcrTaskDao.insertTask(task)
    }

    /**
     * 提交 OCR 解析任务并保存到数据库
     *
     * @param accessToken 百度 AI 平台的 access_token
     * @param file 要解析的文件
     * @param analysisChart 是否解析统计图表
     * @return 提交结果
     */
    suspend fun submitAndSaveTask(
        accessToken: String,
        file: File,
        analysisChart: Boolean? = null
    ): Result<PaddleOcrSubmitResponse> {
        val result = submitTask(accessToken, file, analysisChart)
        result.onSuccess { response ->
            if (response.isSuccess()) {
                val entity = PaddleOcrTaskEntity(
                    taskId = response.result!!.taskId,
                    fileName = file.name,
                    status = OcrTaskStatus.PENDING.value
                )
                paddleOcrTaskDao.insertTask(entity)
            }
        }
        return result
    }

    /**
     * 从数据库获取所有任务
     */
    fun getAllTasksFromDb(): Flow<List<PaddleOcrTaskEntity>> {
        return paddleOcrTaskDao.getAllTasks()
    }

    /**
     * 同步任务状态
     */
    suspend fun syncTaskStatus(accessToken: String, taskId: String): Result<PaddleOcrQueryResult> {
        return queryResult(accessToken, taskId).map { response ->
            val result = response.result
            if (result != null) {
                val entity = paddleOcrTaskDao.getTaskByTaskId(taskId)
                if (entity != null) {
                    val updatedEntity = entity.copy(
                        status = result.status ?: entity.status,
                        markdownUrl = result.markdownUrl ?: entity.markdownUrl,
                        parseResultUrl = result.parseResultUrl ?: entity.parseResultUrl,
                        errorMsg = result.taskError,
                        updatedAt = System.currentTimeMillis()
                    )
                    paddleOcrTaskDao.updateTask(updatedEntity)
                }
            }
            response.result!!
        }
    }

    /**
     * 更新本地路径
     */
    suspend fun updateLocalPath(taskId: String, localPath: String) {
        val entity = paddleOcrTaskDao.getTaskByTaskId(taskId)
        if (entity != null) {
            paddleOcrTaskDao.updateTask(entity.copy(localPath = localPath))
        }
    }

    suspend fun updateRawDownloadedPath(taskId: String, rawDownloadedPath: String) {
        val entity = paddleOcrTaskDao.getTaskByTaskId(taskId)
        if (entity != null) {
            paddleOcrTaskDao.updateTask(entity.copy(rawDownloadedPath = rawDownloadedPath))
        }
    }

    suspend fun getTaskByTaskId(taskId: String): PaddleOcrTaskEntity? {
        return paddleOcrTaskDao.getTaskByTaskId(taskId)
    }

    suspend fun touchTaskUpdatedAt(taskId: String, updatedAt: Long = System.currentTimeMillis()) {
        paddleOcrTaskDao.touchUpdatedAt(taskId, updatedAt)
    }

    suspend fun setTaskPollError(
        taskId: String,
        errorMsg: String?,
        updatedAt: Long = System.currentTimeMillis()
    ) {
        paddleOcrTaskDao.setErrorMsg(taskId, errorMsg, updatedAt)
    }

    /**
     * 删除任务
     */
    suspend fun deleteTask(task: PaddleOcrTaskEntity) {
        paddleOcrTaskDao.deleteTask(task)
    }
}
