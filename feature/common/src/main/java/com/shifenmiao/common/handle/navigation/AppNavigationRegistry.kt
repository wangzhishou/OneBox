package com.shifenmiao.common.handle.navigation

import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.net.toUri
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.HomeTabKey
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.navigation.ScreenCallback
import java.util.Locale

enum class AppNavigationTargetType {
    SCREEN,
    ACTION
}

data class AppNavigationTarget(
    val targetType: AppNavigationTargetType,
    val routeKey: String,
    val canonicalName: String,
    val title: String,
    val description: String,
    val aliases: List<String>,
    val deeplink: String,
    val supportsResultCallback: Boolean = false,
    val screenBuilder: (Map<String, String>) -> Screen,
    val callbackScreenBuilder: ((Map<String, String>, ScreenCallback) -> Screen)? = null
) {
    fun matches(query: String): Int {
        val normalized = query.trim().lowercase(Locale.ROOT)
        if (normalized.isBlank()) return defaultScore()

        val titleValue = title.lowercase(Locale.ROOT)
        val descriptionValue = description.lowercase(Locale.ROOT)
        val routeValue = routeKey.lowercase(Locale.ROOT)
        val canonicalValue = canonicalName.lowercase(Locale.ROOT)
        val aliasValues = aliases.map { it.lowercase(Locale.ROOT) }
        val deeplinkValue = deeplink.lowercase(Locale.ROOT)

        return when {
            normalized == routeValue -> 500
            normalized == canonicalValue -> 490
            normalized == deeplinkValue -> 480
            normalized == titleValue -> 460
            aliasValues.any { it == normalized } -> 440
            titleValue.contains(normalized) -> 320
            aliasValues.any { it.contains(normalized) } -> 260
            routeValue.contains(normalized) || canonicalValue.contains(normalized) -> 240
            descriptionValue.contains(normalized) -> 180
            deeplinkValue.contains(normalized) -> 140
            else -> 0
        }
    }

    private fun defaultScore(): Int {
        return when (targetType) {
            AppNavigationTargetType.ACTION -> 80
            AppNavigationTargetType.SCREEN -> 60
        }
    }
}

data class AppNavigationResolvedTarget(
    val target: AppNavigationTarget,
    val params: Map<String, String> = emptyMap(),
    val rawUrl: String? = null
) {
    val routeKey: String get() = target.routeKey
    val canonicalName: String get() = target.canonicalName
    val deeplink: String get() = target.deeplink
    val title: String get() = target.title
    val description: String get() = target.description
    val targetType: AppNavigationTargetType get() = target.targetType
    val supportsResultCallback: Boolean get() = target.supportsResultCallback

    fun buildScreen(): Screen = target.screenBuilder(params)

    fun buildScreen(onResult: ScreenCallback): Screen {
        return target.callbackScreenBuilder?.invoke(params, onResult) ?: target.screenBuilder(params)
    }
}

object AppNavigationRegistry {

    private fun homeTabTarget(tab: HomeTabKey): AppNavigationTarget {
        val titleRes: Int
        val descriptionRes: Int
        val extraAliases: List<String>
        when (tab) {
            HomeTabKey.TEXT -> {
                titleRes = com.shifenmiao.core.R.string.home_tab_text_title
                descriptionRes = com.shifenmiao.core.R.string.home_tab_text_description
                extraAliases = listOf("note", "notes", "record", "记录", "笔记", "本地创作")
            }
            HomeTabKey.APP -> {
                titleRes = com.shifenmiao.core.R.string.home_tab_app_title
                descriptionRes = com.shifenmiao.core.R.string.home_tab_app_description
                extraAliases = listOf("apps", "tools", "tool", "应用", "工具", "小应用")
            }
            HomeTabKey.AGENT -> {
                titleRes = com.shifenmiao.core.R.string.home_tab_agent_title
                descriptionRes = com.shifenmiao.core.R.string.home_tab_agent_description
                extraAliases = listOf("agents", "ai_agent", "智能体", "AI 智能体")
            }
            HomeTabKey.PROMPT -> {
                titleRes = com.shifenmiao.core.R.string.home_tab_prompt_title
                descriptionRes = com.shifenmiao.core.R.string.home_tab_prompt_description
                extraAliases = listOf("prompts", "ai_prompt", "提示词", "AI 提示词")
            }
            HomeTabKey.WEB -> {
                titleRes = com.shifenmiao.core.R.string.home_tab_web_title
                descriptionRes = com.shifenmiao.core.R.string.home_tab_web_description
                extraAliases = listOf("website", "url", "网址", "网站", "网页")
            }
            HomeTabKey.BLOG -> {
                titleRes = com.shifenmiao.core.R.string.playground
                descriptionRes = com.shifenmiao.core.R.string.playground_description
                extraAliases = listOf("blog", "playground", "玩法", "文章")
            }
        }
        val routeKey = "home/${tab.slug}"
        return AppNavigationTarget(
            targetType = AppNavigationTargetType.SCREEN,
            routeKey = routeKey,
            canonicalName = "screen.$routeKey",
            title = resolveText(titleRes, tab.name),
            description = resolveText(descriptionRes, ""),
            aliases = (listOf(tab.slug, tab.name.lowercase()) + extraAliases).distinct(),
            deeplink = buildStructuredDeeplink(
                AppNavigationTargetType.SCREEN,
                routeKey,
            ),
            screenBuilder = { Screen.NewApp(initialTab = tab) },
        )
    }

    private val extraScreenTargets: List<AppNavigationTarget>
        get() = HomeTabKey.entries.map(::homeTabTarget) + listOf(
            AppNavigationTarget(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = Screen.Bookkeeping().routeKey,
                canonicalName = "screen.${Screen.Bookkeeping().routeKey}",
                title = "记账本",
                description = "打开记账本模块，支持打开新增账目页或编辑指定账目",
                aliases = listOf("bookkeeping", "ledger", "finance", "account_book", "记账", "账本"),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    Screen.Bookkeeping().routeKey,
                ),
                screenBuilder = { params ->
                    val type = params["type"].orEmpty()
                    val editingRecordId = params["editing_record_id"]
                        ?: params["editingRecordId"]
                        ?: params["record_id"]
                        ?: params["recordId"]

                    when {
                        type.equals("add_record", ignoreCase = true) -> {
                            Screen.Bookkeeping(
                                Screen.Bookkeeping.Type.AddRecord(
                                    editingRecordId = editingRecordId?.takeIf { it.isNotBlank() }
                                )
                            )
                        }

                        else -> Screen.Bookkeeping()
                    }
                }
            ),
            AppNavigationTarget(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = Screen.HabitTracker().routeKey,
                canonicalName = "screen.${Screen.HabitTracker().routeKey}",
                title = "习惯打卡",
                description = "打开习惯打卡模块，支持打开指定习惯的编辑页",
                aliases = listOf("habit_tracker", "habit", "habittracker", "习惯", "打卡"),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    Screen.HabitTracker().routeKey,
                ),
                screenBuilder = { params ->
                    val type = params["type"].orEmpty()
                    val habitId = params["habit_id"]
                        ?: params["habitId"]

                    when {
                        type.equals("edit", ignoreCase = true) -> {
                            Screen.HabitTracker(
                                Screen.HabitTracker.Type.Edit(
                                    habitId = habitId?.takeIf { it.isNotBlank() }
                                )
                            )
                        }

                        type.equals("main", ignoreCase = true) -> {
                            Screen.HabitTracker(Screen.HabitTracker.Type.Main)
                        }

                        else -> Screen.HabitTracker()
                    }
                }
            ),
            AppNavigationTarget(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = "xiangqi_router",
                canonicalName = "screen.xiangqi_router",
                title = "象棋",
                description = "打开象棋对局模块，支持跳转到具体棋局",
                aliases = listOf("xiangqi", "chinese_chess", "chess"),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    "xiangqi_router"
                ),
                screenBuilder = { params ->
                    val gameId = params["game_id"].orEmpty()
                    val roomId = params["room_id"]
                        ?: params["roomId"]
                        ?: params["room"]
                        ?: ""
                    val action = params["action"].orEmpty()
                    val type = params["type"].orEmpty()
                    when {
                        roomId.isNotBlank() && action.equals("join_room", ignoreCase = true) -> {
                            Screen.XiangqiRouter(Screen.XiangqiRouter.Type.JoinOnlineRoom(roomId))
                        }

                        gameId.isNotBlank() && type.equals("analysis", ignoreCase = true) ->
                            Screen.XiangqiRouter(Screen.XiangqiRouter.Type.Analysis(gameId))

                        gameId.isNotBlank() -> Screen.XiangqiRouter(Screen.XiangqiRouter.Type.Game(gameId))
                        else -> Screen.XiangqiRouter()
                    }
                }
            ),
            AppNavigationTarget(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = Screen.Teleprompter().routeKey,
                canonicalName = "screen.${Screen.Teleprompter().routeKey}",
                title = "提词器",
                description = "打开提词器模块，支持直达指定文稿的编辑页或播放页",
                aliases = listOf("teleprompter", "prompter", "提词器", "提词板"),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    Screen.Teleprompter().routeKey,
                ),
                screenBuilder = { params ->
                    val type = params["type"].orEmpty()
                    val scriptId = params["script_id"]
                        ?: params["scriptId"]
                        ?: ""

                    when {
                        scriptId.isNotBlank() && type.equals("edit", ignoreCase = true) ->
                            Screen.Teleprompter(Screen.Teleprompter.Type.Edit(scriptId))

                        scriptId.isNotBlank() && type.equals("play", ignoreCase = true) ->
                            Screen.Teleprompter(Screen.Teleprompter.Type.Play(scriptId))

                        else -> Screen.Teleprompter()
                    }
                }
            ),
            staticScreen(
                screen = Screen.ThemeSettings,
                aliases = listOf("theme", "theme_settings", "display_settings")
            ),
            staticScreen(
                screen = Screen.Feedback(),
                aliases = listOf("feedback", "issue_feedback", "bug_feedback")
            ),
            staticScreen(
                screen = Screen.CreateFeedback(),
                aliases = listOf("create_feedback", "submit_feedback")
            ),
            staticScreen(
                screen = Screen.AIFeatureSettings,
                aliases = listOf("ai_feature_settings", "ai_chat_settings", "ai_toggle_settings")
            ),
            staticScreen(
                screen = Screen.AISettings(),
                aliases = listOf("ai_engine_settings", "model_settings", "ai_model_settings", "ai_working_model_settings", "working_model_settings")
            ),
            staticScreen(
                screen = Screen.SystemPromptManagement,
                aliases = listOf("system_prompt_management", "prompt_management")
            ),
            AppNavigationTarget(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = "mark_todo_router",
                canonicalName = "screen.mark_todo_router",
                title = "待办清单",
                description = "打开待办事项模块，管理分类和任务",
                aliases = listOf("marktodo", "todo", "todos", "待办"),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    "mark_todo_router"
                ),
                screenBuilder = { Screen.MarkTodoRouter() }
            ),
            AppNavigationTarget(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = "calendar",
                canonicalName = "screen.calendar",
                title = "万年历",
                description = "打开万年历，支持查看农历日历、八字排盘、择日查询、历法转换",
                aliases = listOf(
                    "calendar", "lunar_calendar", "wan_nian_li",
                    "bazi", "ba_zi", "eight_characters",
                    "auspicious_day", "ji_ri", "huang_dao_ji_ri",
                    "convert", "li_fa_zhuan_huan",
                    "万年历", "农历", "黄历", "老黄历",
                    "八字", "生辰八字", "四柱",
                    "择日", "吉日", "黄道吉日",
                    "历法转换", "公历农历转换"
                ),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    "calendar"
                ),
                screenBuilder = { params ->
                    val type = params["type"].orEmpty().lowercase()
                    val year = params["year"]?.toIntOrNull() ?: -1
                    val month = params["month"]?.toIntOrNull() ?: -1
                    val day = params["day"]?.toIntOrNull() ?: -1
                    val hour = params["hour"]?.toIntOrNull() ?: -1
                    val isAvoid = params.booleanParam("avoid")
                    val direction = params["direction"].orEmpty().lowercase()
                    val isSolarToLunar = direction != "lunar_to_solar"

                    val calendarType = when (type) {
                        "bazi" -> Screen.Calendar.Type.BaZi(year, month, day, hour)
                        "auspicious" -> Screen.Calendar.Type.Auspicious(isAvoid)
                        "convert" -> Screen.Calendar.Type.Convert(isSolarToLunar)
                        else -> Screen.Calendar.Type.CalendarView(year, month, day)
                    }
                    Screen.Calendar(type = calendarType)
                }
            ),
            AppNavigationTarget(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = "file_browser",
                canonicalName = "screen.file_browser",
                title = "文件浏览器",
                description = "打开文件浏览器，支持直接定位到指定目录或文件",
                aliases = listOf(
                    "filebrowser", "files", "explorer",
                    "file_browser", "文件管理", "文件浏览器"
                ),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    "file_browser"
                ),
                screenBuilder = { params ->
                    val uri = params["uri"]?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }
                    Screen.FileBrowser(initialUri = uri)
                }
            ),
            AppNavigationTarget(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = "password_vault",
                canonicalName = "screen.password_vault",
                title = "密码保险箱",
                description = "打开密码保险箱，支持查看列表、详情、添加或编辑记录",
                aliases = listOf(
                    "password_vault", "vault", "password",
                    "密码保险箱", "保险箱", "密码"
                ),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    "password_vault"
                ),
                screenBuilder = { params ->
                    val type = params["type"].orEmpty().lowercase()
                    val entryId = params["entry_id"]
                        ?: params["entryId"]
                        ?: params["id"]
                        ?: ""
                    val vaultType = when (type) {
                        "detail" -> entryId.takeIf { it.isNotBlank() }?.let {
                            Screen.PasswordVault.Type.Detail(it)
                        }
                        "add" -> Screen.PasswordVault.Type.Add
                        "edit" -> entryId.takeIf { it.isNotBlank() }?.let {
                            Screen.PasswordVault.Type.Edit(it)
                        }
                        else -> null
                    }
                    Screen.PasswordVault(type = vaultType)
                }
            ),
            AppNavigationTarget(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = "poem",
                canonicalName = "screen.poem",
                title = "中国古诗词",
                description = "打开中国古诗词，随机赏诗、AI 解读，支持直达指定诗词",
                aliases = listOf(
                    "poem", "poetry", "gushi", "gushici",
                    "古诗", "古诗词", "诗词", "中国古诗词"
                ),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    "poem"
                ),
                screenBuilder = { params ->
                    val poemId = params["poem_id"]?.toLongOrNull()
                        ?: params["poemId"]?.toLongOrNull()
                        ?: params["id"]?.toLongOrNull()
                    Screen.Poem(poemId = poemId)
                }
            ),
            AppNavigationTarget(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = "poem_search",
                canonicalName = "screen.poem_search",
                title = "诗词搜索",
                description = "打开诗词搜索，按关键词检索中国古诗词",
                aliases = listOf("poem_search", "search_poem", "poetry_search", "诗词搜索"),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    "poem_search"
                ),
                screenBuilder = { params ->
                    Screen.PoemSearch(
                        initialQuery = params["q"] ?: params["query"]
                    )
                }
            )
        )

    private val actionTargets: List<AppNavigationTarget>
        get() = listOf(
            AppNavigationTarget(
                targetType = AppNavigationTargetType.ACTION,
                routeKey = "open_file_picker",
                canonicalName = "action.open_file_picker",
                title = "选择文件",
                description = "打开系统文件选择器，可用于选择单个或多个文件",
                aliases = listOf(
                    "file_picker",
                    "pick_file",
                    "choose_file",
                    "open_document",
                    "select_file"
                ),
                deeplink = buildStructuredDeeplink(
                    targetType = AppNavigationTargetType.ACTION,
                    routeKey = "open_file_picker"
                ),
                supportsResultCallback = true,
                screenBuilder = { params ->
                    Screen.OpenFilePicker(
                        mimeTypes = parseMimeTypes(params),
                        allowMultiple = params.booleanParam("multiple")
                    )
                },
                callbackScreenBuilder = { params, onResult ->
                    Screen.OpenFilePicker(
                        mimeTypes = parseMimeTypes(params),
                        allowMultiple = params.booleanParam("multiple"),
                        onResult = onResult
                    )
                }
            ),
            AppNavigationTarget(
                targetType = AppNavigationTargetType.ACTION,
                routeKey = "open",
                canonicalName = "action.open",
                title = "打开文件",
                description = "智能打开指定 URI 的文件，自动选择最合适的查看器（图片/ImageViewer、PDF/PdfTools、文本/MarkdownEditor、文件夹/FileBrowser、其他/系统 chooser）",
                aliases = listOf(
                    "open_file", "view", "preview",
                    "打开文件", "查看", "预览"
                ),
                deeplink = buildStructuredDeeplink(
                    targetType = AppNavigationTargetType.ACTION,
                    routeKey = "open"
                ),
                screenBuilder = { params ->
                    // 正常路径：UrlNavigator.navigate() 会拦截 onebox://action/open
                    // 并委托 ContentRouter 做智能路由。此 screenBuilder 仅在绕过
                    // UrlNavigator 直接调用 registry.buildScreen() 时作为 fallback 生效。
                    val uri = params["uri"]?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }
                    Screen.FileBrowser(initialUri = uri)
                }
            )
        )

    val screenTargets: List<AppNavigationTarget> by lazy {
        // extraScreenTargets 在前，同名时覆盖 Screen.entries 中的默认注册
        (extraScreenTargets + Screen.entries.map(::staticScreen))
            .distinctBy { it.routeKey }
    }

    val allTargets: List<AppNavigationTarget> by lazy {
        (screenTargets + actionTargets)
            .distinctBy { it.canonicalName }
    }

    fun search(query: String, limit: Int = 8): List<AppNavigationTarget> {
        val normalized = query.trim()
        return allTargets
            .asSequence()
            .map { target -> target to target.matches(normalized) }
            .filter { (_, score) -> normalized.isBlank() || score > 0 }
            .sortedWith(
                compareByDescending<Pair<AppNavigationTarget, Int>> { it.second }
                    .thenBy { it.first.title }
            )
            .map { it.first }
            .take(limit.coerceIn(1, 20))
            .toList()
    }

    fun resolve(identifier: String): AppNavigationTarget? {
        val normalized = identifier.trim()
        if (normalized.isBlank()) return null
        return allTargets.firstOrNull { target ->
            normalized.equals(target.routeKey, ignoreCase = true) ||
                normalized.equals(target.canonicalName, ignoreCase = true) ||
                normalized.equals(target.deeplink, ignoreCase = true) ||
                target.aliases.any { it.equals(normalized, ignoreCase = true) }
        }
    }

    fun resolveByTitle(title: String): AppNavigationTarget? {
        val normalized = title.trim()
        if (normalized.isBlank()) return null
        return allTargets.firstOrNull { target ->
            target.title.equals(normalized, ignoreCase = true)
        }
    }

    fun resolveDeeplink(url: String): AppNavigationResolvedTarget? {
        val decodedUrl = Uri.decode(url)
        val uri = runCatching { decodedUrl.toUri() }.getOrNull() ?: return null
        val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: return null
        if (scheme != "onebox" && scheme != "app") return null

        val params = uri.queryParameterNames.associateWith { name ->
            uri.getQueryParameter(name).orEmpty()
        }

        val host = uri.host.orEmpty()
        val firstPath = uri.pathSegments.firstOrNull().orEmpty()

        val target = when (host.lowercase(Locale.ROOT)) {
            UrlConstants.DEEP_LINK_HOST_SCREEN -> resolve(firstPath)
                ?.takeIf { it.targetType == AppNavigationTargetType.SCREEN }
            UrlConstants.DEEP_LINK_HOST_ACTION -> resolve(firstPath)
                ?.takeIf { it.targetType == AppNavigationTargetType.ACTION }
            else -> resolve(host.ifBlank { firstPath })
        } ?: return null

        return AppNavigationResolvedTarget(
            target = target,
            params = params,
            rawUrl = decodedUrl
        )
    }

    fun buildStructuredDeeplink(
        targetType: AppNavigationTargetType,
        routeKey: String,
        params: Map<String, String> = emptyMap()
    ): String {
        val base = when (targetType) {
            AppNavigationTargetType.SCREEN -> "${UrlConstants.DEEP_LINKS_PREFIX}${UrlConstants.DEEP_LINK_HOST_SCREEN}/$routeKey"
            AppNavigationTargetType.ACTION -> "${UrlConstants.DEEP_LINKS_PREFIX}${UrlConstants.DEEP_LINK_HOST_ACTION}/$routeKey"
        }
        if (params.isEmpty()) return base
        return buildString {
            append(base)
            append('?')
            append(
                params.entries.joinToString("&") { (key, value) ->
                    "${Uri.encode(key)}=${Uri.encode(value)}"
                }
            )
        }
    }

    private fun staticScreen(
        screen: Screen,
        aliases: List<String> = emptyList()
    ): AppNavigationTarget {
        val className = screen::class.simpleName.orEmpty()
        val title = resolveText(screen.title, screen.name.ifBlank { className.ifBlank { screen.routeKey } })
        val description = screen.description.ifBlank {
            resolveText(screen.subtitle, "")
        }
        return AppNavigationTarget(
            targetType = AppNavigationTargetType.SCREEN,
            routeKey = screen.routeKey,
            canonicalName = screen.canonicalName,
            title = title,
            description = description,
            aliases = (
                listOfNotNull(
                    screen.simpleName.takeIf { it.isNotBlank() },
                    className.takeIf { it.isNotBlank() },
                    screen.slug.takeIf { it.isNotBlank() }
                ) + aliases
            ).distinct(),
            deeplink = buildStructuredDeeplink(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = screen.routeKey
            ),
            screenBuilder = { screen }
        )
    }

    private fun resolveText(@StringRes resId: Int, fallback: String): String {
        if (resId == 0) return fallback
        return runCatching { AppContext.getString(resId) }
            .getOrElse { fallback }
    }

    private fun parseMimeTypes(params: Map<String, String>): List<String> {
        val raw = params["mime_types"]
            ?: params["mimeTypes"]
            ?: params["mime_type"]
            ?: params["mimeType"]
            ?: params["mime"]
            ?: "*/*"
        return raw.split(',', '|')
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .ifEmpty { listOf("*/*") }
    }

    private fun Map<String, String>.booleanParam(name: String): Boolean {
        return this[name]?.trim()?.lowercase(Locale.ROOT) in setOf("1", "true", "yes", "y")
    }
}
