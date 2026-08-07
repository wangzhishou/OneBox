package com.shifenmiao.base.authcode

import com.shifenmiao.database.authcode.entity.AuthCodeEntity
import com.shifenmiao.database.authcode.repo.AuthCodeRepository
import com.shifenmiao.database.item.dao.ItemEntityDao
import com.shifenmiao.database.item.entity.ItemWithCategories
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 授权码服务契约。
 *
 * 抽象在 [core:base] 中,实现与 Room 仓库绑定。
 * UI 层 (授权码设置页) 与全局锁屏 (AuthorizationCodeStateHolder) 都通过本接口访问,
 * 不直接依赖 DAO / Repository,以遵守依赖倒置。
 */
interface AuthCodeService {
    /** 是否已经设置过授权码。 */
    suspend fun hasCode(): Boolean

    /** 校验 [code] 是否与已设置的授权码一致。 */
    suspend fun verify(code: String): Boolean

    /** 设置或覆盖授权码(首次设置 / 修改都用这个入口)。 */
    suspend fun setCode(code: String)

    /** 清除授权码,回到未设置状态。 */
    suspend fun clear()

    /** 清除授权码并同时关闭所有应用的密码保护(总开关关闭时使用)。 */
    suspend fun clearCodeAndAllProtection()

    /** 观察已开启密码保护的应用列表 (Room 关系对象,内嵌 [ItemWithCategories.item] / userState)。 */
    fun observeProtectedItems(): Flow<List<ItemWithCategories>>

    /** 关闭指定应用的密码保护。 */
    suspend fun disableProtection(itemId: Int)
}

@Singleton
class AuthCodeServiceImpl @Inject constructor(
    private val repository: AuthCodeRepository,
    private val itemEntityDao: ItemEntityDao,
) : AuthCodeService {

    override suspend fun hasCode(): Boolean = repository.hasCode()

    override suspend fun verify(code: String): Boolean {
        if (code.isBlank()) return false
        val entity = repository.get() ?: return false
        val expected = hash(entity.salt, code)
        return constantTimeEquals(expected, entity.codeHash)
    }

    override suspend fun setCode(code: String) {
        val salt = generateSalt()
        val now = System.currentTimeMillis()
        val existing = repository.get()
        repository.save(
            AuthCodeEntity(
                id = AuthCodeEntity.SINGLETON_ID,
                codeHash = hash(salt, code),
                salt = salt,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            )
        )
    }

    override suspend fun clear() {
        repository.delete()
    }

    override suspend fun clearCodeAndAllProtection() {
        itemEntityDao.clearAllRequiresAuth(now = System.currentTimeMillis())
        repository.delete()
    }

    override fun observeProtectedItems(): Flow<List<ItemWithCategories>> =
        itemEntityDao.observeItemsRequiringAuth()

    override suspend fun disableProtection(itemId: Int) {
        itemEntityDao.setRequiresAuth(
            itemId = itemId,
            requiresAuth = false,
            now = System.currentTimeMillis()
        )
    }

    private fun generateSalt(): String {
        val bytes = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(bytes)
        return bytes.toHex()
    }

    private fun hash(salt: String, code: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val input = (salt + code).toByteArray(Charsets.UTF_8)
        return digest.digest(input).toHex()
    }

    private fun ByteArray.toHex(): String {
        val sb = StringBuilder(size * 2)
        for (b in this) {
            val v = b.toInt() and 0xFF
            sb.append(HEX_CHARS[v ushr 4])
            sb.append(HEX_CHARS[v and 0x0F])
        }
        return sb.toString()
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var diff = 0
        for (i in a.indices) {
            diff = diff or (a[i].code xor b[i].code)
        }
        return diff == 0
    }

    companion object {
        private const val SALT_BYTES = 16
        private val HEX_CHARS = "0123456789abcdef".toCharArray()
    }
}
