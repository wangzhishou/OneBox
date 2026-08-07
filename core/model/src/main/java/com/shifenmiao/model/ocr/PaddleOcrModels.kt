package com.shifenmiao.model.ocr

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * PaddleOCR-VL 文档解析 API 模型
 *
 * 该 API 为异步接口：
 * 1. 调用提交请求接口获取 task_id
 * 2. 调用获取结果接口轮询结果（建议提交请求后 5～10 秒轮询）
 *
 * @see <a href="https://ai.baidu.com/ai-doc/OCR/Ym5h0hkwt">API文档</a>
 */

// ==================== 提交请求相关 ====================

/**
 * 提交OCR解析任务请求参数
 *
 * @property fileData 文件的base64编码数据（和fileUrl二选一）
 *                    - 支持格式：pdf、jpg、jpeg、png、bmp、tif、tiff
 *                    - 图片最长边不大于4096px
 *                    - 文档大小不超过100M，PDF最大支持500页
 *                    - 若文档大小超过50M，须使用fileUrl方式上传
 * @property fileUrl 文件数据URL（和fileData二选一）
 *                   - URL长度不超过1024字节
 *                   - PDF文档大小不超过100M，最大支持500页
 * @property fileName 文件名，请保证文件名后缀正确，例如 "1.pdf"
 * @property analysisChart 是否对统计图表进行解析
 */
@Parcelize
@Serializable
data class PaddleOcrSubmitRequest(
    @SerializedName("file_data")
    val fileData: String? = null,
    @SerializedName("file_url")
    val fileUrl: String? = null,
    @SerializedName("file_name")
    val fileName: String,
    @SerializedName("analysis_chart")
    val analysisChart: Boolean? = null
) : Parcelable

/**
 * 提交OCR解析任务响应
 */
@Parcelize
@Serializable
data class PaddleOcrSubmitResponse(
    @SerializedName("log_id")
    val logId: String? = null,
    @SerializedName("error_code")
    val errorCode: Int = 0,
    @SerializedName("error_msg")
    val errorMsg: String? = null,
    @SerializedName("result")
    val result: PaddleOcrTaskResult? = null
) : Parcelable {
    fun isSuccess(): Boolean = errorCode == 0 && result?.taskId != null
}

@Parcelize
@Serializable
data class PaddleOcrTaskResult(
    @SerializedName("task_id")
    val taskId: String
) : Parcelable

// ==================== 查询结果相关 ====================

/**
 * 查询OCR解析结果请求参数
 */
@Parcelize
@Serializable
data class PaddleOcrQueryRequest(
    @SerializedName("task_id")
    val taskId: String
) : Parcelable

/**
 * 查询OCR解析结果响应
 */
@Parcelize
@Serializable
data class PaddleOcrQueryResponse(
    @SerializedName("log_id")
    val logId: String? = null,
    @SerializedName("error_code")
    val errorCode: Int = 0,
    @SerializedName("error_msg")
    val errorMsg: String? = null,
    @SerializedName("result")
    val result: PaddleOcrQueryResult? = null
) : Parcelable {
    fun isSuccess(): Boolean = errorCode == 0
    fun isCompleted(): Boolean = result?.status == OcrTaskStatus.SUCCESS.value
    fun isFailed(): Boolean = result?.status == OcrTaskStatus.FAILED.value
    fun isPending(): Boolean = result?.status == OcrTaskStatus.PENDING.value
    fun isProcessing(): Boolean = result?.status == OcrTaskStatus.PROCESSING.value
}

/**
 * 任务状态枚举
 */
enum class OcrTaskStatus(val value: String) {
    PENDING("pending"),       // 排队中
    PROCESSING("processing"), // 运行中
    SUCCESS("success"),       // 成功
    FAILED("failed")          // 失败
}

/**
 * 查询结果详情
 */
@Parcelize
@Serializable
data class PaddleOcrQueryResult(
    @SerializedName("task_id")
    val taskId: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("task_error")
    val taskError: String? = null,
    @SerializedName("markdown_url")
    val markdownUrl: String? = null,
    @SerializedName("parse_result_url")
    val parseResultUrl: String? = null
) : Parcelable

// ==================== 解析结果详细模型（从parse_result_url下载的JSON） ====================

/**
 * 完整的文档解析结果
 */
@Parcelize
@Serializable
data class PaddleOcrParseResult(
    @SerializedName("file_name")
    val fileName: String? = null,
    @SerializedName("file_id")
    val fileId: String? = null,
    @SerializedName("pages")
    val pages: List<OcrPage>? = null
) : Parcelable

/**
 * 单页解析内容
 */
@Parcelize
@Serializable
data class OcrPage(
    @SerializedName("page_id")
    val pageId: String? = null,
    @SerializedName("page_num")
    val pageNum: Int = 0,
    @SerializedName("text")
    val text: String? = null,
    @SerializedName("layouts")
    val layouts: List<OcrLayout>? = null,
    @SerializedName("tables")
    val tables: List<OcrTable>? = null,
    @SerializedName("images")
    val images: List<OcrImage>? = null,
    @SerializedName("meta")
    val meta: OcrPageMeta? = null
) : Parcelable

/**
 * 页面版式分析结果
 */
@Parcelize
@Serializable
data class OcrLayout(
    @SerializedName("layout_id")
    val layoutId: String? = null,
    @SerializedName("text")
    val text: String? = null,
    @SerializedName("position")
    val position: List<Int>? = null, // [x, y, w, h]
    @SerializedName("type")
    val type: String? = null
) : Parcelable

/**
 * Layout 元素类型
 */
object OcrLayoutType {
    const val ABSTRACT = "abstract"              // 摘要
    const val ALGORITHM = "algorithm"            // 算法
    const val ASIDE_TEXT = "aside_text"          // 旁注文本
    const val CHART = "chart"                    // 图表
    const val CONTENT = "content"                // 目录
    const val DISPLAY_FORMULA = "display_formula" // 公式
    const val DOC_TITLE = "doc_title"            // 文档标题
    const val FIGURE_TITLE = "figure_title"      // 图片标题
    const val FOOTER = "footer"                  // 页脚
    const val FOOTER_IMAGE = "footer_image"      // 页脚图片
    const val FOOTNOTE = "footnote"              // 脚注
    const val FORMULA_NUMBER = "formula_number"  // 公式编号
    const val HEADER = "header"                  // 页眉
    const val HEADER_IMAGE = "header_image"      // 页眉图片
    const val IMAGE = "image"                    // 图片
    const val INLINE_FORMULA = "inline_formula"  // 行内公式
    const val NUMBER = "number"                  // 页码
    const val PARAGRAPH_TITLE = "paragraph_title" // 段落标题
    const val REFERENCE = "reference"            // 参考文献
    const val REFERENCE_CONTENT = "reference_content" // 参考文献内容
    const val SEAL = "seal"                      // 印章
    const val TABLE = "table"                    // 表格
    const val TEXT = "text"                      // 文本
    const val VERTICAL_TEXT = "vertical_text"    // 竖排文本
}

/**
 * 表格解析结果
 */
@Parcelize
@Serializable
data class OcrTable(
    @SerializedName("layout_id")
    val layoutId: String? = null,
    @SerializedName("markdown")
    val markdown: String? = null,
    @SerializedName("position")
    val position: List<Int>? = null, // [x, y, w, h]
    @SerializedName("cells")
    val cells: List<OcrTableCell>? = null,
    @SerializedName("matrix")
    val matrix: List<List<Int>>? = null
) : Parcelable

/**
 * 表格单元格
 */
@Parcelize
@Serializable
data class OcrTableCell(
    @SerializedName("layout_id")
    val layoutId: String? = null,
    @SerializedName("text")
    val text: String? = null,
    @SerializedName("position")
    val position: List<Int>? = null,
    @SerializedName("type")
    val type: String? = null
) : Parcelable

/**
 * 图片解析结果
 */
@Parcelize
@Serializable
data class OcrImage(
    @SerializedName("layout_id")
    val layoutId: String? = null,
    @SerializedName("position")
    val position: List<Int>? = null, // [x, y, w, h]
    @SerializedName("data_url")
    val dataUrl: String? = null,
    @SerializedName("image_description")
    val imageDescription: String? = null // 对统计图表进行内容解析和描述，JSON字符串
) : Parcelable

/**
 * 页面元信息
 */
@Parcelize
@Serializable
data class OcrPageMeta(
    @SerializedName("page_width")
    val pageWidth: Int = 0,
    @SerializedName("page_height")
    val pageHeight: Int = 0
) : Parcelable

