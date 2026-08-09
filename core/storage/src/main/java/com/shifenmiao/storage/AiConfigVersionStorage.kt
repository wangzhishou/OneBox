package com.shifenmiao.storage

import com.shifenmiao.model.ai.AiEngine
import com.tencent.mmkv.MMKV

/**
 * AI 配置版本存储
 * 用于存储远程配置的版本号，支持增量更新
 */
object AiConfigVersionStorage {

    // 整个 store 按语言隔离：版本水位度量的是按语言分库的 ai_engine 目录，
    // 选中的引擎 Parcelable 内嵌当时语言的 title/description，每种语言各一套选择。
    private val mmkv: MMKV get() = localizedMmkv(MMKVName.AI_ENGINE_SETTING)

    private const val AI_CONFIG_VERSION = "ai_config_version"
    private const val AI_CONFIG_LAST_UPDATE_TIME = "ai_config_last_update_time"
    private const val AI_FLAVOR_PRESET_VERSION = "ai_flavor_preset_version"
    private const val AI_DEFAULT_ENGINE = "ai_default_engine"
    private const val AI_FAST_ENGINE = "ai_fast_engine"
    private const val AI_DUEL_ENGINE_A = "ai_duel_engine_a"
    private const val AI_DUEL_ENGINE_B = "ai_duel_engine_b"

    // 用户手动选择标记（防止 remote config 覆盖用户选择）
    private const val HAS_USER_SET_DEFAULT = "has_user_set_default"
    private const val HAS_USER_SET_FAST = "has_user_set_fast"
    private const val HAS_USER_SET_DUEL_A = "has_user_set_duel_a"
    private const val HAS_USER_SET_DUEL_B = "has_user_set_duel_b"

    /**
     * 保存配置版本号
     */
    fun saveConfigVersion(version: String) {
        mmkv.encode(AI_CONFIG_VERSION, version)
    }

    /**
     * 保存最后更新时间
     */
    fun saveLastUpdateTime(timestamp: Long = System.currentTimeMillis()) {
        mmkv.encode(AI_CONFIG_LAST_UPDATE_TIME, timestamp)
    }

    /**
     * 获取最后更新时间
     * @return 时间戳，如果没有则返回 0
     */
    fun getLastUpdateTime(): Long {
        return mmkv.decodeLong(AI_CONFIG_LAST_UPDATE_TIME, 0L)
    }

    /**
     * 检查是否需要更新配置
     * @param intervalMillis 更新间隔（毫秒）
     * @return true 表示需要更新
     */
    fun shouldUpdate(intervalMillis: Long): Boolean {
        val lastUpdateTime = getLastUpdateTime()
        return System.currentTimeMillis() - lastUpdateTime >= intervalMillis
    }

    /**
     * 获取配置版本号
     * @return 版本号，如果没有则返回 null
     */
    fun getConfigVersion(): String? {
        return mmkv.decodeString(AI_CONFIG_VERSION)
    }

    /**
     * 保存 flavor 预制引擎版本号
     */
    fun saveFlavorPresetVersion(version: Int) {
        mmkv.encode(AI_FLAVOR_PRESET_VERSION, version)
    }

    /**
     * 获取已应用的 flavor 预制引擎版本号, 未应用过返回 0
     */
    fun getFlavorPresetVersion(): Int {
        return mmkv.decodeInt(AI_FLAVOR_PRESET_VERSION, 0)
    }

    /**
     * 清除配置版本号
     */
    fun clearConfigVersion() {
        mmkv.remove(AI_CONFIG_VERSION)
    }

    fun saveDefaultEngine(engine: AiEngine) {
        mmkv.encode(AI_DEFAULT_ENGINE, engine)
    }

    fun getDefaultEngine(): AiEngine? {
        return mmkv.decodeParcelable(AI_DEFAULT_ENGINE, AiEngine::class.java)
    }

    fun clearDefaultEngine() {
        mmkv.remove(AI_DEFAULT_ENGINE)
    }

    fun saveFastEngine(engine: AiEngine) {
        mmkv.encode(AI_FAST_ENGINE, engine)
    }

    fun getFastEngine(): AiEngine? {
        return mmkv.decodeParcelable(AI_FAST_ENGINE, AiEngine::class.java)
    }

    fun clearFastEngine() {
        mmkv.remove(AI_FAST_ENGINE)
    }

    fun saveDuelEngineA(engine: AiEngine) {
        mmkv.encode(AI_DUEL_ENGINE_A, engine)
    }

    fun getDuelEngineA(): AiEngine? {
        return mmkv.decodeParcelable(AI_DUEL_ENGINE_A, AiEngine::class.java)
    }

    fun clearDuelEngineA() {
        mmkv.remove(AI_DUEL_ENGINE_A)
    }

    fun saveDuelEngineB(engine: AiEngine) {
        mmkv.encode(AI_DUEL_ENGINE_B, engine)
    }

    fun getDuelEngineB(): AiEngine? {
        return mmkv.decodeParcelable(AI_DUEL_ENGINE_B, AiEngine::class.java)
    }

    fun clearDuelEngineB() {
        mmkv.remove(AI_DUEL_ENGINE_B)
    }

    // ==================== 用户手动选择标记 ====================

    fun setHasUserSetDefault(hasSet: Boolean) {
        mmkv.encode(HAS_USER_SET_DEFAULT, hasSet)
    }

    fun hasUserSetDefault(): Boolean {
        return mmkv.decodeBool(HAS_USER_SET_DEFAULT, false)
    }

    fun setHasUserSetFast(hasSet: Boolean) {
        mmkv.encode(HAS_USER_SET_FAST, hasSet)
    }

    fun hasUserSetFast(): Boolean {
        return mmkv.decodeBool(HAS_USER_SET_FAST, false)
    }

    fun setHasUserSetDuelA(hasSet: Boolean) {
        mmkv.encode(HAS_USER_SET_DUEL_A, hasSet)
    }

    fun hasUserSetDuelA(): Boolean {
        return mmkv.decodeBool(HAS_USER_SET_DUEL_A, false)
    }

    fun setHasUserSetDuelB(hasSet: Boolean) {
        mmkv.encode(HAS_USER_SET_DUEL_B, hasSet)
    }

    fun hasUserSetDuelB(): Boolean {
        return mmkv.decodeBool(HAS_USER_SET_DUEL_B, false)
    }
}

