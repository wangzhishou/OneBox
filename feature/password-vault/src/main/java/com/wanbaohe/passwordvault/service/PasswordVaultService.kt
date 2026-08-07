package com.wanbaohe.passwordvault.service

import com.shifenmiao.database.passwordvault.entity.PasswordVaultCategoryEntity
import com.shifenmiao.database.passwordvault.entity.PasswordVaultEntryEntity
import com.shifenmiao.database.passwordvault.repo.PasswordVaultRepository
import com.wanbaohe.passwordvault.crypto.VaultCrypto
import com.wanbaohe.passwordvault.crypto.VaultWrongKeyException
import com.wanbaohe.passwordvault.model.PasswordVaultCategoryUi
import com.wanbaohe.passwordvault.model.SecretField
import com.wanbaohe.passwordvault.model.VaultEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface PasswordVaultService {
    suspend fun hasEntries(): Boolean
    fun observeEntrySummaries(): Flow<List<VaultEntry>>
    fun observeEntries(masterPassword: String): Flow<List<VaultEntry>>
    suspend fun getEntrySummary(entryId: String): VaultEntry?
    suspend fun getEntry(entryId: String, masterPassword: String): VaultEntry?
    suspend fun saveEntry(masterPassword: String, entry: VaultEntry)
    suspend fun deleteEntry(entryId: String)
    suspend fun verifyMasterPassword(masterPassword: String): Boolean

    fun observeCategories(): Flow<List<PasswordVaultCategoryUi>>
    suspend fun getAllCategories(): List<PasswordVaultCategoryUi>
    suspend fun addCategory(name: String): String
    suspend fun renameCategory(categoryId: String, newName: String)
    suspend fun deleteCategory(categoryId: String)
    suspend fun reorderCategories(orderedIds: List<String>)
    suspend fun ensureDefaultCategories()
}

@Singleton
class PasswordVaultServiceImpl @Inject constructor(
    private val repository: PasswordVaultRepository,
) : PasswordVaultService {

    private val json = Json { ignoreUnknownKeys = true }

    // ── Entry ────────────────────────────────────────────

    override suspend fun hasEntries(): Boolean {
        return repository.hasEntries()
    }

    override fun observeEntrySummaries(): Flow<List<VaultEntry>> {
        return combine(
            repository.observeAll(),
            repository.observeCategories(),
        ) { entries, categories ->
            val byId = categories.associateBy { it.id }
            entries.map { entity -> entity.toSummary(byId[entity.categoryId]) }
        }
    }

    override fun observeEntries(masterPassword: String): Flow<List<VaultEntry>> = flow {
        combine(
            repository.observeAll(),
            repository.observeCategories(),
        ) { entries, categories -> entries to categories.associateBy { it.id } }
            .collect { (entries, byId) ->
                emit(
                    entries.mapNotNull { entity ->
                        decryptEntity(masterPassword, entity, byId[entity.categoryId])
                    }
                )
            }
    }

    override suspend fun saveEntry(masterPassword: String, entry: VaultEntry) {
        val resolvedCategoryId = entry.categoryId.takeIf { it.isNotBlank() }
            ?: repository.getAllCategories().firstOrNull()?.id
            ?: error("no_category_available")

        val payload = VaultPayload(
            password = entry.password,
            secretFields = entry.secretFields,
            note = entry.note,
        )
        val jsonString = json.encodeToString(payload)
        val key = VaultCrypto.deriveKey(masterPassword)
        val encryptedBundle = VaultCrypto.encrypt(jsonString, key)

        val entity = PasswordVaultEntryEntity(
            id = entry.id.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
            title = entry.title.trim(),
            categoryId = resolvedCategoryId,
            account = entry.account?.trim(),
            encryptedPayload = encryptedBundle,
            salt = key.salt,
            createdAt = entry.createdAt.takeIf { it > 0 } ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
        repository.save(entity)
    }

    override suspend fun getEntrySummary(entryId: String): VaultEntry? {
        val entity = repository.getById(entryId) ?: return null
        val category = repository.getCategoryById(entity.categoryId)
        return entity.toSummary(category)
    }

    override suspend fun getEntry(entryId: String, masterPassword: String): VaultEntry? {
        val entity = repository.getById(entryId) ?: return null
        val category = repository.getCategoryById(entity.categoryId)
        return decryptEntity(masterPassword, entity, category)
    }

    override suspend fun deleteEntry(entryId: String) {
        repository.delete(entryId)
    }

    override suspend fun verifyMasterPassword(masterPassword: String): Boolean {
        if (!repository.hasEntries()) return masterPassword.isNotBlank()
        val first = repository.getAll().firstOrNull() ?: return masterPassword.isNotBlank()
        return try {
            VaultCrypto.decrypt(masterPassword, first.encryptedPayload)
            true
        } catch (_: VaultWrongKeyException) {
            false
        }
    }

    // ── Category ─────────────────────────────────────────

    override fun observeCategories(): Flow<List<PasswordVaultCategoryUi>> {
        return repository.observeCategories().map { list -> list.map { it.toUi() } }
    }

    override suspend fun getAllCategories(): List<PasswordVaultCategoryUi> {
        return repository.getAllCategories().map { it.toUi() }
    }

    override suspend fun addCategory(name: String): String {
        require(name.isNotBlank()) { "category_name_blank" }
        val existing = repository.getAllCategories()
        val id = UUID.randomUUID().toString()
        repository.upsertCategory(
            PasswordVaultCategoryEntity(
                id = id,
                name = name.trim(),
                sortOrder = existing.size,
                isDefault = false,
            )
        )
        return id
    }

    override suspend fun renameCategory(categoryId: String, newName: String) {
        require(newName.isNotBlank()) { "category_name_blank" }
        val target = repository.getAllCategories().firstOrNull { it.id == categoryId }
            ?: error("category_not_found")
        repository.upsertCategory(target.copy(name = newName.trim(), updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteCategory(categoryId: String) {
        repository.deleteCustomCategory(categoryId)
    }

    override suspend fun reorderCategories(orderedIds: List<String>) {
        orderedIds.forEachIndexed { index, id ->
            repository.updateCategoryOrder(categoryId = id, order = index)
        }
    }

    override suspend fun ensureDefaultCategories() {
        val existing = repository.getAllCategories()
        if (existing.isNotEmpty()) return
        val now = System.currentTimeMillis()
        repository.ensureDefaultCategories(
            listOf(
                PasswordVaultCategoryEntity(
                    id = DEFAULT_CATEGORY_LOGIN,
                    name = "登录",
                    sortOrder = 0,
                    isDefault = true,
                    createdAt = now,
                    updatedAt = now,
                ),
                PasswordVaultCategoryEntity(
                    id = DEFAULT_CATEGORY_BANK,
                    name = "银行",
                    sortOrder = 1,
                    isDefault = true,
                    createdAt = now,
                    updatedAt = now,
                ),
                PasswordVaultCategoryEntity(
                    id = DEFAULT_CATEGORY_EMAIL,
                    name = "邮箱",
                    sortOrder = 2,
                    isDefault = true,
                    createdAt = now,
                    updatedAt = now,
                ),
                PasswordVaultCategoryEntity(
                    id = DEFAULT_CATEGORY_WIFI,
                    name = "Wi-Fi",
                    sortOrder = 3,
                    isDefault = true,
                    createdAt = now,
                    updatedAt = now,
                ),
                PasswordVaultCategoryEntity(
                    id = DEFAULT_CATEGORY_OTHER,
                    name = "其他",
                    sortOrder = 4,
                    isDefault = true,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        )
    }

    // ── Helpers ──────────────────────────────────────────

    private suspend fun decryptEntity(
        masterPassword: String,
        entity: PasswordVaultEntryEntity,
        category: PasswordVaultCategoryEntity?,
    ): VaultEntry? {
        return try {
            val result = VaultCrypto.decrypt(masterPassword, entity.encryptedPayload)
            val payload = json.decodeFromString<VaultPayload>(result.plaintext)
            VaultEntry(
                id = entity.id,
                title = entity.title,
                categoryId = entity.categoryId,
                categoryName = category?.name.orEmpty(),
                account = entity.account,
                password = payload.password,
                secretFields = payload.secretFields,
                note = payload.note,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun PasswordVaultEntryEntity.toSummary(
        category: PasswordVaultCategoryEntity?,
    ): VaultEntry = VaultEntry(
        id = id,
        title = title,
        categoryId = this.categoryId,
        categoryName = category?.name.orEmpty(),
        account = account,
        password = "",
        secretFields = emptyList(),
        note = "",
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    private fun PasswordVaultCategoryEntity.toUi(): PasswordVaultCategoryUi =
        PasswordVaultCategoryUi(
            id = id,
            name = name,
            sortOrder = sortOrder,
            isDefault = isDefault,
        )

    @Serializable
    private data class VaultPayload(
        val password: String = "",
        val secretFields: List<SecretField> = emptyList(),
        val note: String = "",
    )

    companion object {
        const val DEFAULT_CATEGORY_LOGIN = "default-login"
        const val DEFAULT_CATEGORY_BANK = "default-bank"
        const val DEFAULT_CATEGORY_EMAIL = "default-email"
        const val DEFAULT_CATEGORY_WIFI = "default-wifi"
        const val DEFAULT_CATEGORY_OTHER = "default-other"
    }
}
