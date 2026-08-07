package com.shifenmiao.feature.document.repository

import android.util.Base64
import com.shifenmiao.database.docconvert.dao.DocConvertTaskDao
import com.shifenmiao.database.docconvert.entity.DocConvertTaskEntity
import com.shifenmiao.model.ocr.DocConvertQueryResult
import com.shifenmiao.model.ocr.OcrTaskStatus
import com.shifenmiao.network.api.DocConvertApiService
import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocConvertRepository @Inject constructor(
    private val docConvertApiService: DocConvertApiService,
    private val docConvertTaskDao: DocConvertTaskDao
) {
    fun getAllTasksFromDb(): Flow<List<DocConvertTaskEntity>> = docConvertTaskDao.getAllTasks()

    suspend fun insertTask(task: DocConvertTaskEntity): Long = docConvertTaskDao.insertTask(task)

    suspend fun deleteTask(task: DocConvertTaskEntity) = docConvertTaskDao.deleteTask(task)

    suspend fun getTaskByTaskId(taskId: String): DocConvertTaskEntity? = docConvertTaskDao.getTaskByTaskId(taskId)

    suspend fun touchTaskUpdatedAt(taskId: String, updatedAt: Long = System.currentTimeMillis()) {
        docConvertTaskDao.touchUpdatedAt(taskId, updatedAt)
    }

    suspend fun setTaskPollError(
        taskId: String,
        errorMsg: String?,
        updatedAt: Long = System.currentTimeMillis()
    ) {
        docConvertTaskDao.setErrorMsg(taskId, errorMsg, updatedAt)
    }

    suspend fun submitFile(
        accessToken: String,
        inputStream: InputStream,
        fileName: String,
        isPdf: Boolean
    ): Result<String> {
        return runCatching {
            val bytes = inputStream.readBytes()
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            val response = if (isPdf) {
                docConvertApiService.requestDocConvert(
                    accessToken = accessToken,
                    pdfFile = base64
                )
            } else {
                docConvertApiService.requestDocConvert(
                    accessToken = accessToken,
                    image = base64
                )
            }

            if (!response.isSuccessful) {
                throw Exception("请求失败: ${response.code()} ${response.message()}")
            }

            val body = response.body() ?: throw Exception("响应体为空")
            val taskId = body.result?.taskId?.takeIf { it.isNotBlank() }
            if (taskId != null) return@runCatching taskId

            val msg = body.errorMsg
                ?.takeIf { it.isNotBlank() }
                ?: body.message
                    ?.takeIf { it.isNotBlank() }
                ?: "Create task failed"
            throw Exception(msg)
        }
    }

    suspend fun syncTaskStatus(accessToken: String, taskId: String): Result<DocConvertQueryResult> {
        return runCatching {
            val response = docConvertApiService.getDocConvertResult(
                accessToken = accessToken,
                taskId = taskId
            )
            if (!response.isSuccessful) {
                throw Exception("请求失败: ${response.code()} ${response.message()}")
            }

            val body = response.body() ?: throw Exception("响应体为空")
            val result = body.result ?: run {
                val msg = body.errorMsg
                    ?.takeIf { it.isNotBlank() }
                    ?: body.message
                        ?.takeIf { it.isNotBlank() }
                    ?: "Query task failed"
                throw Exception(msg)
            }

            val entity = docConvertTaskDao.getTaskByTaskId(taskId)
            if (entity != null) {
                val mappedStatus = when (result.retCode) {
                    1 -> OcrTaskStatus.PENDING.value
                    2 -> OcrTaskStatus.PROCESSING.value
                    3 -> OcrTaskStatus.SUCCESS.value
                    else -> OcrTaskStatus.FAILED.value
                }

                val updatedEntity = entity.copy(
                    status = mappedStatus,
                    percent = result.percent ?: entity.percent,
                    wordUrl = normalizeDownloadUrl(result.resultData?.word) ?: entity.wordUrl,
                    excelUrl = normalizeDownloadUrl(result.resultData?.excel) ?: entity.excelUrl,
                    errorMsg = if (mappedStatus == OcrTaskStatus.FAILED.value) {
                        result.retMsg ?: body.message ?: entity.errorMsg
                    } else {
                        null
                    },
                    updatedAt = System.currentTimeMillis()
                )
                docConvertTaskDao.updateTask(updatedEntity)
            }

            result
        }
    }

    suspend fun updateLocalWordPath(taskId: String, localPath: String) {
        val entity = docConvertTaskDao.getTaskByTaskId(taskId) ?: return
        docConvertTaskDao.updateTask(entity.copy(localWordPath = localPath))
    }

    suspend fun updateLocalExcelPath(taskId: String, localPath: String) {
        val entity = docConvertTaskDao.getTaskByTaskId(taskId) ?: return
        docConvertTaskDao.updateTask(entity.copy(localExcelPath = localPath))
    }

    private fun normalizeDownloadUrl(raw: String?): String? {
        val value = raw?.trim().orEmpty()
        if (value.isBlank()) return null
        if (value.startsWith("http://", ignoreCase = true) || value.startsWith("https://", ignoreCase = true)) {
            return value
        }
        if (value.startsWith("//")) return "https:$value"
        if (value.startsWith("/")) return "https://aip.baidubce.com$value"
        if (value.contains('.') && !value.contains("://")) return "https://$value"
        return null
    }
}
