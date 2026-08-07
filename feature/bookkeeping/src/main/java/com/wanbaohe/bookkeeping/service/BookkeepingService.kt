package com.wanbaohe.bookkeeping.service

import android.content.Context
import android.net.Uri
import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.database.bookkeeping.entity.BookkeepingCategoryEntity
import com.shifenmiao.database.bookkeeping.entity.BookkeepingRecordEntity
import com.shifenmiao.database.bookkeeping.repo.BookkeepingRepository
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.bookkeeping.R
import com.wanbaohe.bookkeeping.model.BookkeepingBackupCategory
import com.wanbaohe.bookkeeping.model.BookkeepingBackupCodec
import com.wanbaohe.bookkeeping.model.BookkeepingBackupPayload
import com.wanbaohe.bookkeeping.model.BookkeepingBackupRecord
import com.wanbaohe.bookkeeping.model.BookkeepingRecordType
import com.wanbaohe.bookkeeping.model.BookkeepingRestoreResult
import com.wanbaohe.bookkeeping.model.localizedDefaultCategoryName
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 记账业务门面 — UI 层与 Agent 层共用的唯一写入入口。
 *
 * 职责:
 *  - 入参校验（金额、类型、分类）
 *  - 调用 [BookkeepingRepository] 写库
 *  - 调用 [ActivityLogRecorder] 写审计日志
 *
 * 不做的事:
 *  - 不持有 UI 状态
 *  - 不暴露 Flow（订阅仍走 Repository,Component 直接 inject Repository 用作只读）
 */
@Singleton
class BookkeepingService @Inject constructor(
    private val repository: BookkeepingRepository,
    private val activityLogRecorder: ActivityLogRecorder,
) {

    // ── 数据契约 ────────────────────────────────────────

    data class RecordInput(
        val categoryId: String,
        val type: BookkeepingRecordType,
        val amountCents: Long,
        val note: String?,
        val happenedDate: LocalDate,
    )

    data class RecordView(
        val id: String,
        val categoryId: String?,
        val categoryName: String?,
        val type: BookkeepingRecordType,
        val amountCents: Long,
        val note: String?,
        val happenedAt: Long,
        val happenedDate: LocalDate,
        val excludeFromStats: Boolean,
    )

    data class MonthSummary(
        val month: YearMonth,
        val expenseCents: Long,
        val incomeCents: Long,
        val recordCount: Int,
        val expenseByCategory: List<CategoryAmount>,
        val incomeByCategory: List<CategoryAmount>,
    )

    data class CategoryAmount(
        val categoryName: String,
        val amountCents: Long,
    )

    data class RecordsQuerySummary(
        val expenseCents: Long,
        val incomeCents: Long,
        val excludedCents: Long,
        val recordCount: Int,
    )

    data class RecordsQueryResult(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val type: BookkeepingRecordType?,
        val categoryName: String?,
        val summary: RecordsQuerySummary,
        val records: List<RecordView>,
    )

    data class BatchAddSuccess(
        val index: Int,
        val recordId: String,
    )

    data class BatchAddFailure(
        val index: Int,
        val reason: String,
    )

    data class BatchAddResult(
        val successes: List<BatchAddSuccess>,
        val failures: List<BatchAddFailure>,
    )

    // ── 写入：账目 ──────────────────────────────────────

    suspend fun addRecord(
        input: RecordInput,
        actor: String,
        source: String,
    ): Result<String> = runCatching {
        addRecordInternal(input = input, actor = actor, source = source)
    }

    suspend fun addRecordsBatch(
        inputs: List<RecordInput>,
        actor: String,
        source: String,
    ): BatchAddResult {
        val successes = mutableListOf<BatchAddSuccess>()
        val failures = mutableListOf<BatchAddFailure>()

        inputs.forEachIndexed { index, input ->
            runCatching {
                addRecordInternal(input = input, actor = actor, source = source)
            }.onSuccess { recordId ->
                successes += BatchAddSuccess(index = index, recordId = recordId)
            }.onFailure { error ->
                failures += BatchAddFailure(
                    index = index,
                    reason = error.message ?: "unknown_error",
                )
            }
        }

        return BatchAddResult(successes = successes, failures = failures)
    }

    suspend fun editRecord(
        recordId: String,
        input: RecordInput,
        actor: String,
        source: String,
    ): Result<Unit> = runCatching {
        validateAmount(input.amountCents)
        val previousEntity = repository.getAllRecords().firstOrNull { it.id == recordId }
            ?: error("record_not_found")
        val happenedAt = input.happenedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        repository.upsertRecord(
            BookkeepingRecordEntity(
                id = recordId,
                categoryId = input.categoryId,
                type = input.type.code,
                amountCents = input.amountCents,
                note = input.note?.takeIf { it.isNotBlank() },
                happenedAt = happenedAt,
                excludeFromStats = input.type == BookkeepingRecordType.EXCLUDED,
                createdAt = previousEntity.createdAt,
            )
        )
        logRecordChange(
            entityId = recordId,
            actor = actor,
            action = "UPDATE",
            source = source,
            input = input,
            previous = previousEntity,
        )
    }

    suspend fun deleteRecord(
        recordId: String,
        actor: String,
        source: String,
    ): Result<Unit> = runCatching {
        val previous = repository.getAllRecords().firstOrNull { it.id == recordId }
        val categoryName = previous?.categoryId
            ?.let { id -> repository.getAllCategories().firstOrNull { it.id == id }?.name }
        repository.deleteRecord(recordId)
        activityLogRecorder.recordBookkeeping(
            entityId = recordId,
            entityType = "BookkeepingRecord",
            actorType = actor,
            actionType = "DELETE",
            source = source,
            title = AppContext.getString(R.string.bookkeeping_log_record_deleted),
            description = previous?.let { describeRecordEntity(it, categoryName) } ?: "",
            snapshot = previous?.toSnapshotJson(),
        )
    }

    // ── 写入：分类 ──────────────────────────────────────

    suspend fun addCategory(
        name: String,
        type: BookkeepingRecordType,
        actor: String,
        source: String,
    ): Result<String> = runCatching {
        require(name.isNotBlank()) { "category_name_blank" }
        val existingCount = repository.getAllCategories().count { it.type == type.code }
        val id = UUID.randomUUID().toString()
        repository.upsertCategory(
            BookkeepingCategoryEntity(
                id = id,
                name = name.trim(),
                type = type.code,
                iconKey = when (type) {
                    BookkeepingRecordType.EXPENSE -> "expense"
                    BookkeepingRecordType.INCOME -> "income"
                    BookkeepingRecordType.EXCLUDED -> "excluded"
                },
                sortOrder = existingCount,
                isDefault = false,
            )
        )
        activityLogRecorder.recordBookkeeping(
            entityId = id,
            entityType = "BookkeepingCategory",
            actorType = actor,
            actionType = "CREATE",
            source = source,
            title = AppContext.getString(R.string.bookkeeping_log_category_created, name.trim()),
            description = type.localizedName(),
        )
        id
    }

    suspend fun renameCategory(
        categoryId: String,
        newName: String,
        actor: String,
        source: String,
    ): Result<Unit> = runCatching {
        require(newName.isNotBlank()) { "category_name_blank" }
        val target = repository.getAllCategories().firstOrNull { it.id == categoryId }
            ?: error("category_not_found")
        repository.upsertCategory(target.copy(name = newName.trim()))
        activityLogRecorder.recordBookkeeping(
            entityId = categoryId,
            entityType = "BookkeepingCategory",
            actorType = actor,
            actionType = "UPDATE",
            source = source,
            title = AppContext.getString(R.string.bookkeeping_log_category_renamed),
            description = AppContext.getContext().getString(
                R.string.bookkeeping_log_category_renamed_desc,
                target.name,
                newName.trim(),
            ),
        )
    }

    suspend fun removeCategory(
        categoryId: String,
        actor: String,
        source: String,
    ): Result<Unit> = runCatching {
        val target = repository.getAllCategories().firstOrNull { it.id == categoryId }
        repository.deleteCustomCategory(categoryId)
        activityLogRecorder.recordBookkeeping(
            entityId = categoryId,
            entityType = "BookkeepingCategory",
            actorType = actor,
            actionType = "DELETE",
            source = source,
            title = target?.let {
                AppContext.getString(R.string.bookkeeping_log_category_deleted, it.name)
            } ?: AppContext.getString(R.string.bookkeeping_log_category_deleted_fallback),
            description = "",
        )
    }

    /** 排序属于纯 UI 操作,不写日志。 */
    suspend fun reorderCategories(orderedIds: List<String>) {
        orderedIds.forEachIndexed { index, id ->
            repository.updateCategoryOrder(categoryId = id, order = index)
        }
    }

    // ── 备份 / 恢复 / CSV ─────────────────────────────

    suspend fun exportBackupJson(): String {
        val payload = BookkeepingBackupPayload(
            exportedAt = System.currentTimeMillis(),
            categories = repository.getAllCategories().map { it.toBackup() },
            records = repository.getAllRecords().map { it.toBackup() },
        )
        return BookkeepingBackupCodec.encode(payload)
    }

    suspend fun restoreBackup(
        json: String,
        actor: String,
        source: String,
    ): Result<BookkeepingRestoreResult> = runCatching {
        val payload = BookkeepingBackupCodec.decode(json)
        require(payload.categories.isNotEmpty()) { "categories_empty" }
        repository.replaceAll(
            categories = payload.categories.map { it.toEntity() },
            records = payload.records.map { it.toEntity() },
        )
        activityLogRecorder.recordBookkeeping(
            entityId = "*",
            entityType = "BookkeepingBackup",
            actorType = actor,
            actionType = "RESTORE",
            source = source,
            title = AppContext.getString(R.string.bookkeeping_log_backup_restored),
            description = AppContext.getContext().getString(
                R.string.bookkeeping_log_backup_restored_desc,
                payload.categories.size,
                payload.records.size,
            ),
        )
        BookkeepingRestoreResult(
            categoryCount = payload.categories.size,
            recordCount = payload.records.size,
        )
    }

    suspend fun exportCsv(context: Context, uri: Uri): Result<Unit> = runCatching {
        val allRecords = repository.getAllRecords()
        val allCategoryMap = repository.getAllCategories().associateBy({ it.id }, { it.name })
        val sb = StringBuilder()
        sb.appendLine("date,type,category,amount,note")
        allRecords.forEach { record ->
            val date = Instant.ofEpochMilli(record.happenedAt).atZone(ZoneId.systemDefault()).toLocalDate()
            val typeName = when (BookkeepingRecordType.fromCode(record.type)) {
                BookkeepingRecordType.EXPENSE -> "expense"
                BookkeepingRecordType.INCOME -> "income"
                BookkeepingRecordType.EXCLUDED -> "excluded"
            }
            val catName = record.categoryId?.let { allCategoryMap[it] } ?: ""
            val amount = "%.2f".format(record.amountCents / 100.0)
            val note = (record.note ?: "").replace("\"", "\"\"")
            sb.appendLine("$date,$typeName,\"$catName\",$amount,\"$note\"")
        }
        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use {
            it.write(sb.toString())
        } ?: error("output_stream_null")
    }

    suspend fun importCsv(
        context: Context,
        uri: Uri,
        actor: String,
        source: String,
    ): Result<Int> = runCatching {
        val text = context.contentResolver.openInputStream(uri)
            ?.bufferedReader()?.use { it.readText() }
            ?: error("input_stream_null")
        val lines = text.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) error("empty_file")
        val dataLines = if (lines.first().startsWith("date")) lines.drop(1) else lines
        val allCategories = repository.getAllCategories()
        val categoryMap = allCategories.associate { it.name.lowercase() to it.id }.toMutableMap()
        val firstByType = allCategories.groupBy { it.type }.mapValues { it.value.firstOrNull()?.id }
        var importedCount = 0
        dataLines.forEach { line ->
            val parts = parseCsvLine(line)
            if (parts.size < 4) return@forEach
            val date = runCatching { LocalDate.parse(parts[0]) }.getOrNull() ?: return@forEach
            val type = when (parts[1].lowercase()) {
                "income" -> BookkeepingRecordType.INCOME
                "excluded" -> BookkeepingRecordType.EXCLUDED
                else -> BookkeepingRecordType.EXPENSE
            }
            val catName = parts[2]
            val catId = categoryMap.getOrPut(catName.lowercase()) {
                firstByType[type.code] ?: ""
            }
            val amountCents = (parts[3].toDoubleOrNull() ?: 0.0).times(100).toLong()
            if (amountCents <= 0L) return@forEach
            val note = parts.getOrElse(4) { "" }
            val happenedAt = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            repository.upsertRecord(
                BookkeepingRecordEntity(
                    id = UUID.randomUUID().toString(),
                    categoryId = catId,
                    type = type.code,
                    amountCents = amountCents,
                    note = note.ifBlank { null },
                    happenedAt = happenedAt,
                    excludeFromStats = type == BookkeepingRecordType.EXCLUDED,
                )
            )
            importedCount++
        }
        activityLogRecorder.recordBookkeeping(
            entityId = "*",
            entityType = "BookkeepingRecord",
            actorType = actor,
            actionType = "IMPORT",
            source = source,
            title = AppContext.getContext().getString(
                R.string.bookkeeping_log_csv_imported_count,
                importedCount,
            ),
            description = "",
        )
        importedCount
    }

    // ── 只读查询（供 AgentTool 使用） ─────────────────

    suspend fun listCategoriesByType(type: BookkeepingRecordType): List<BookkeepingCategoryEntity> {
        return repository.getAllCategories()
            .filter { it.type == type.code }
            .sortedBy { it.sortOrder }
    }

    suspend fun findCategoryByName(name: String, type: BookkeepingRecordType?): BookkeepingCategoryEntity? {
        val all = repository.getAllCategories()
        val target = name.trim().lowercase()
        return all.firstOrNull { it.name.lowercase() == target && (type == null || it.type == type.code) }
    }

    suspend fun summarizeMonth(month: YearMonth): MonthSummary {
        val zone = ZoneId.systemDefault()
        val start = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = month.atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
        val records = repository.getAllRecords().filter { it.happenedAt in start..end }
        val categories = repository.getAllCategories().associateBy { it.id }
        val expenseAgg = mutableMapOf<String, Long>()
        val incomeAgg = mutableMapOf<String, Long>()
        var expenseCents = 0L
        var incomeCents = 0L
        records.forEach { rec ->
            if (rec.excludeFromStats) return@forEach
            val catName = rec.categoryId?.let { localizedDefaultCategoryName(it) ?: categories[it]?.name }
                ?: AppContext.getString(R.string.bookkeeping_unclassified)
            when (BookkeepingRecordType.fromCode(rec.type)) {
                BookkeepingRecordType.EXPENSE -> {
                    expenseCents += rec.amountCents
                    expenseAgg.merge(catName, rec.amountCents, Long::plus)
                }
                BookkeepingRecordType.INCOME -> {
                    incomeCents += rec.amountCents
                    incomeAgg.merge(catName, rec.amountCents, Long::plus)
                }
                BookkeepingRecordType.EXCLUDED -> Unit
            }
        }
        return MonthSummary(
            month = month,
            expenseCents = expenseCents,
            incomeCents = incomeCents,
            recordCount = records.size,
            expenseByCategory = expenseAgg.entries.sortedByDescending { it.value }
                .map { CategoryAmount(it.key, it.value) },
            incomeByCategory = incomeAgg.entries.sortedByDescending { it.value }
                .map { CategoryAmount(it.key, it.value) },
        )
    }

    suspend fun listRecordsInRange(
        startDate: LocalDate,
        endDate: LocalDate,
        type: BookkeepingRecordType? = null,
        limit: Int = 100,
    ): List<RecordView> {
        val zone = ZoneId.systemDefault()
        val start = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = endDate.atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
        val categories = repository.getAllCategories().associateBy { it.id }
        return repository.getAllRecords()
            .asSequence()
            .filter { it.happenedAt in start..end }
            .filter { type == null || it.type == type.code }
            .sortedByDescending { it.happenedAt }
            .take(limit)
            .map { rec ->
                RecordView(
                    id = rec.id,
                    categoryId = rec.categoryId,
                    categoryName = rec.categoryId?.let { categories[it]?.name },
                    type = BookkeepingRecordType.fromCode(rec.type),
                    amountCents = rec.amountCents,
                    note = rec.note,
                    happenedAt = rec.happenedAt,
                    happenedDate = Instant.ofEpochMilli(rec.happenedAt).atZone(zone).toLocalDate(),
                    excludeFromStats = rec.excludeFromStats,
                )
            }
            .toList()
    }

    suspend fun queryRecords(
        startDate: LocalDate,
        endDate: LocalDate,
        type: BookkeepingRecordType? = null,
        categoryName: String? = null,
        limit: Int = 100,
    ): RecordsQueryResult {
        require(!endDate.isBefore(startDate)) { "date_range_invalid" }

        val zone = ZoneId.systemDefault()
        val start = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = endDate.atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
        val normalizedCategory = categoryName?.trim()?.takeIf { it.isNotEmpty() }?.lowercase()
        val categories = repository.getAllCategories().associateBy { it.id }

        val filteredRecords = repository.getAllRecords()
            .asSequence()
            .filter { it.happenedAt in start..end }
            .filter { type == null || it.type == type.code }
            .map { rec ->
                RecordView(
                    id = rec.id,
                    categoryId = rec.categoryId,
                    categoryName = rec.categoryId?.let { categories[it]?.name },
                    type = BookkeepingRecordType.fromCode(rec.type),
                    amountCents = rec.amountCents,
                    note = rec.note,
                    happenedAt = rec.happenedAt,
                    happenedDate = Instant.ofEpochMilli(rec.happenedAt).atZone(zone).toLocalDate(),
                    excludeFromStats = rec.excludeFromStats,
                )
            }
            .filter { record ->
                normalizedCategory == null || record.categoryName?.lowercase() == normalizedCategory
            }
            .sortedByDescending { it.happenedAt }
            .toList()

        val summary = filteredRecords.fold(
            RecordsQuerySummary(
                expenseCents = 0L,
                incomeCents = 0L,
                excludedCents = 0L,
                recordCount = filteredRecords.size,
            )
        ) { acc, record ->
            when (record.type) {
                BookkeepingRecordType.EXPENSE -> acc.copy(expenseCents = acc.expenseCents + record.amountCents)
                BookkeepingRecordType.INCOME -> acc.copy(incomeCents = acc.incomeCents + record.amountCents)
                BookkeepingRecordType.EXCLUDED -> acc.copy(excludedCents = acc.excludedCents + record.amountCents)
            }
        }

        return RecordsQueryResult(
            startDate = startDate,
            endDate = endDate,
            type = type,
            categoryName = categoryName?.trim()?.takeIf { it.isNotEmpty() },
            summary = summary,
            records = filteredRecords.take(limit.coerceIn(1, 200)),
        )
    }

    // ── 工具方法 ──────────────────────────────────────

    /** 把 "12.34" 解析成分,失败返回 null。 */
    fun parseAmountToCents(amount: String): Long? {
        if (amount.isBlank()) return null
        return try {
            BigDecimal(amount)
                .setScale(2, RoundingMode.DOWN)
                .multiply(BigDecimal(100))
                .toLong()
        } catch (_: Exception) {
            null
        }
    }

    private fun validateAmount(amountCents: Long) {
        require(amountCents > 0L) { "amount_invalid" }
    }

    private suspend fun addRecordInternal(
        input: RecordInput,
        actor: String,
        source: String,
    ): String {
        validateAmount(input.amountCents)
        val recordId = UUID.randomUUID().toString()
        val happenedAt = input.happenedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        repository.upsertRecord(
            BookkeepingRecordEntity(
                id = recordId,
                categoryId = input.categoryId,
                type = input.type.code,
                amountCents = input.amountCents,
                note = input.note?.takeIf { it.isNotBlank() },
                happenedAt = happenedAt,
                excludeFromStats = input.type == BookkeepingRecordType.EXCLUDED,
            )
        )
        logRecordChange(
            entityId = recordId,
            actor = actor,
            action = "CREATE",
            source = source,
            input = input,
            previous = null,
        )
        return recordId
    }

    private suspend fun logRecordChange(
        entityId: String,
        actor: String,
        action: String,
        source: String,
        input: RecordInput,
        previous: BookkeepingRecordEntity?,
    ) {
        val title = when (action) {
            "CREATE" -> AppContext.getString(R.string.bookkeeping_log_record_created)
            "UPDATE" -> AppContext.getString(R.string.bookkeeping_log_record_updated)
            else -> AppContext.getString(R.string.bookkeeping_log_record_changed)
        }
        val categoryName = repository.getAllCategories()
            .firstOrNull { it.id == input.categoryId }?.name
        val description = describeRecord(input, categoryName)
        val snapshot = JSONObject().apply {
            put("id", entityId)
            put("categoryId", input.categoryId)
            put("type", input.type.code)
            put("amountCents", input.amountCents)
            put("note", input.note)
            put("happenedDate", input.happenedDate.toString())
            if (previous != null) {
                put("previousAmountCents", previous.amountCents)
                put("previousType", previous.type)
            }
        }.toString()
        activityLogRecorder.recordBookkeeping(
            entityId = entityId,
            entityType = "BookkeepingRecord",
            actorType = actor,
            actionType = action,
            source = source,
            title = title,
            description = description,
            snapshot = snapshot,
            timestamp = Date(),
        )
    }

    /** 把账目输入组装成 "支出 ¥12.50 · 餐饮 · 5月1日 · 午餐" 这样的人话描述。 */
    private fun describeRecord(input: RecordInput, categoryName: String?): String {
        val typeName = input.type.localizedName()
        val amount = formatAmount(input.amountCents)
        val category = categoryName ?: AppContext.getString(R.string.bookkeeping_unclassified)
        val dateText = formatHappenedDate(input.happenedDate)
        val note = input.note?.takeIf { it.isNotBlank() }
        return if (note != null) {
            AppContext.getContext().getString(
                R.string.bookkeeping_log_record_desc_full,
                typeName, amount, category, dateText, note,
            )
        } else {
            AppContext.getContext().getString(
                R.string.bookkeeping_log_record_desc_no_note,
                typeName, amount, category, dateText,
            )
        }
    }

    /** 删除场景：从已存在的 entity 反推一份描述（type / amount / happenedAt / category）。 */
    private fun describeRecordEntity(entity: BookkeepingRecordEntity, categoryName: String?): String {
        val type = BookkeepingRecordType.fromCode(entity.type)
        val date = Instant.ofEpochMilli(entity.happenedAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val typeName = type.localizedName()
        val amount = formatAmount(entity.amountCents)
        val category = categoryName ?: AppContext.getString(R.string.bookkeeping_unclassified)
        return AppContext.getContext().getString(
            R.string.bookkeeping_log_record_desc_no_note,
            typeName, amount, category, formatHappenedDate(date),
        )
    }

    private fun formatAmount(amountCents: Long): String =
        String.format(Locale.US, "%.2f", amountCents / 100.0)

    private fun formatHappenedDate(date: LocalDate): String {
        return date.format(LOCALIZED_DATE_FORMATTER)
    }

    private fun BookkeepingRecordType.localizedName(): String = AppContext.getString(
        when (this) {
            BookkeepingRecordType.EXPENSE -> R.string.bookkeeping_expense
            BookkeepingRecordType.INCOME -> R.string.bookkeeping_income
            BookkeepingRecordType.EXCLUDED -> R.string.bookkeeping_excluded
        }
    )

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        for (ch in line) {
            when {
                ch == '"' && !inQuotes -> inQuotes = true
                ch == '"' && inQuotes -> inQuotes = false
                ch == ',' && !inQuotes -> {
                    result.add(current.toString().trim())
                    current = StringBuilder()
                }
                else -> current.append(ch)
            }
        }
        result.add(current.toString().trim())
        return result
    }

    // ── Mapper ───────────────────────────────────────

    private fun BookkeepingRecordEntity.toSnapshotJson(): String = JSONObject().apply {
        put("id", id)
        put("categoryId", categoryId)
        put("type", type)
        put("amountCents", amountCents)
        put("note", note)
        put("happenedAt", happenedAt)
        put("excludeFromStats", excludeFromStats)
    }.toString()

    private fun BookkeepingCategoryEntity.toBackup(): BookkeepingBackupCategory = BookkeepingBackupCategory(
        id = id, name = name, type = type, iconKey = iconKey,
        sortOrder = sortOrder, isDefault = isDefault,
        createdAt = createdAt, updatedAt = updatedAt,
    )

    private fun BookkeepingRecordEntity.toBackup(): BookkeepingBackupRecord = BookkeepingBackupRecord(
        id = id, categoryId = categoryId, type = type, amountCents = amountCents,
        note = note, happenedAt = happenedAt, excludeFromStats = excludeFromStats,
        createdAt = createdAt, updatedAt = updatedAt,
    )

    private fun BookkeepingBackupCategory.toEntity(): BookkeepingCategoryEntity = BookkeepingCategoryEntity(
        id = id, name = name, type = type, iconKey = iconKey,
        sortOrder = sortOrder, isDefault = isDefault,
        createdAt = createdAt, updatedAt = updatedAt,
    )

    private fun BookkeepingBackupRecord.toEntity(): BookkeepingRecordEntity = BookkeepingRecordEntity(
        id = id, categoryId = categoryId, type = type, amountCents = amountCents,
        note = note, happenedAt = happenedAt, excludeFromStats = excludeFromStats,
        createdAt = createdAt, updatedAt = updatedAt,
    )

    companion object {
        const val ACTOR_USER = "USER"
        const val ACTOR_AGENT = "AGENT"
        const val ACTOR_SYSTEM = "SYSTEM"

        const val SOURCE_UI = "ui:bookkeeping"
        const val SOURCE_AGENT_ADD = "agent_tool:add_bookkeeping_record"
        const val SOURCE_AGENT_ADD_BATCH = "agent_tool:add_bookkeeping_record.batch"
        const val SOURCE_AGENT_QUERY = "agent_tool:query_bookkeeping_summary"

        private val LOCALIZED_DATE_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(Locale.getDefault())
    }
}
