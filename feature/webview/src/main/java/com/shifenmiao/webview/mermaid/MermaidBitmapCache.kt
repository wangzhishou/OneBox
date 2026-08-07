package com.shifenmiao.webview.mermaid

import android.util.Log
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.storage.MMKVName
import com.tencent.mmkv.MMKV
import org.json.JSONObject
import java.io.File

/**
 * Mermaid SVG 文件缓存（文件 + MMKV 元数据）
 *
 * SVG 文本写入 `cacheDir/mermaid/{cacheKey}.svg` 文件，
 * MMKV 仅存储轻量元数据（文件路径、高度、时间戳）。
 *
 * 优势（vs 旧版 MMKV 存 SVG 文本）：
 * - MMKV 不再膨胀（每条 ~100B 元数据 vs 旧版 ~2-10KB SVG 文本）
 * - 文件可直接通过 `file://` URI 传给 ImageViewer / Coil
 * - 便于调试：adb pull 即可查看 SVG 原文
 *
 * 缓存 key = `"${CACHE_VERSION}_${code.hashCode()}_${isDark}"`
 */
object MermaidBitmapCache {

    private const val TAG = "MermaidBitmapCache"

    /**
     * 缓存项
     *
     * @param svgFile   SVG 文件引用
     * @param heightPx  SVG 实际高度（像素），用于 Compose 侧精确布局
     */
    data class CachedMermaid(
        val svgFile: File,
        val heightPx: Int
    ) {
        /** 懒读取 SVG 文本内容（仅在确实需要字符串时才读文件） */
        val svgString: String by lazy { svgFile.readText(Charsets.UTF_8) }
    }

    // ── MMKV 存储（仅元数据） ────────────────────────────────────────────

    private val mmkv: MMKV by lazy { MMKV.mmkvWithID(MMKVName.MERMAID_CACHE) }

    // ── 文件目录 ──────────────────────────────────────────────────────────

    private val svgDir: File by lazy {
        File(AppContext.getContext().cacheDir, "mermaid").also { it.mkdirs() }
    }

    // ── Key 工具 ────────────────────────────────────────────────────────

    /**
     * 缓存格式版本号，变更时自动使旧缓存失效。
     *
     * v1: inlineSvgStyles 内联（有损，文字丢失）
     * v2: 保留原始 SVG CSS（AndroidSVG 原生解析，复杂选择器仍失效）
     * v3: getComputedStyle 内联文字属性 + foreignObject→text 转换
     * v4: SVG 文件存储（MMKV 仅存元数据）
     */
    private const val CACHE_VERSION = "v4"

    private fun cacheKey(code: String, isDark: Boolean): String {
        return "${CACHE_VERSION}_${code.hashCode()}_$isDark"
    }

    private fun metaKey(cacheKey: String): String = "meta_$cacheKey"

    private fun svgFile(cacheKey: String): File = File(svgDir, "$cacheKey.svg")

    // ── 公共 API ────────────────────────────────────────────────────────

    /**
     * 获取缓存的 SVG
     *
     * @param code   Mermaid 源码
     * @param isDark 当前是否暗色主题
     * @return 缓存项，未命中返回 null
     */
    fun get(code: String, isDark: Boolean): CachedMermaid? {
        val key = cacheKey(code, isDark)
        val json = mmkv.decodeString(metaKey(key)) ?: return null
        return try {
            val obj = JSONObject(json)
            val filePath = obj.optString(KEY_FILE_PATH)
            val file = if (filePath.isNotEmpty()) File(filePath) else svgFile(key)
            if (!file.exists() || file.length() == 0L) {
                // 文件丢失，清理元数据
                mmkv.remove(metaKey(key))
                return null
            }
            CachedMermaid(
                svgFile = file,
                heightPx = obj.optInt(KEY_HEIGHT_PX)
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read cache for key=$key", e)
            null
        }
    }

    /**
     * 写入缓存（SVG 文件 + 元数据，同步写入）
     *
     * @param code       Mermaid 源码
     * @param isDark     当前是否暗色主题
     * @param svgString  JS 提取的 SVG 字符串
     * @param heightPx   SVG 实际高度（像素）
     * @return 写入的 SVG 文件，失败返回 null
     */
    fun put(code: String, isDark: Boolean, svgString: String, heightPx: Int): File? {
        val key = cacheKey(code, isDark)
        val file = svgFile(key)
        return try {
            file.writeText(svgString, Charsets.UTF_8)
            val meta = JSONObject().apply {
                put(KEY_FILE_PATH, file.absolutePath)
                put(KEY_HEIGHT_PX, heightPx)
                put(KEY_TIMESTAMP, System.currentTimeMillis())
            }
            mmkv.encode(metaKey(key), meta.toString())
            Log.d(TAG, "Cached SVG: ${file.absolutePath} (${file.length()} bytes)")
            file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write SVG file: ${file.absolutePath}", e)
            null
        }
    }

    /**
     * 清空全部缓存（元数据 + 文件）
     */
    fun clear() {
        mmkv.clearAll()
        svgDir.listFiles()?.forEach { it.delete() }
    }

    /**
     * 清理过期缓存（基于写入时间）
     *
     * @param maxAgeMs 最大缓存时间（毫秒），默认 7 天
     */
    fun cleanExpired(maxAgeMs: Long = DEFAULT_MAX_AGE_MS) {
        val now = System.currentTimeMillis()
        val allKeys = mmkv.allKeys() ?: return

        for (key in allKeys) {
            if (!key.startsWith(META_PREFIX)) continue
            val json = mmkv.decodeString(key) ?: continue
            try {
                val obj = JSONObject(json)
                val timestamp = obj.optLong(KEY_TIMESTAMP, 0L)
                if (now - timestamp > maxAgeMs) {
                    // 删除文件
                    val filePath = obj.optString(KEY_FILE_PATH)
                    if (filePath.isNotEmpty()) File(filePath).delete()
                    // 删除元数据
                    mmkv.remove(key)
                }
            } catch (_: Exception) {
                mmkv.remove(key)
            }
        }
    }

    // ── 常量 ────────────────────────────────────────────────────────────

    private const val META_PREFIX = "meta_"
    private const val KEY_FILE_PATH = "filePath"
    private const val KEY_HEIGHT_PX = "heightPx"
    private const val KEY_TIMESTAMP = "timestamp"

    /** 默认过期时间：7 天 */
    private const val DEFAULT_MAX_AGE_MS = 7 * 24 * 60 * 60 * 1000L
}
