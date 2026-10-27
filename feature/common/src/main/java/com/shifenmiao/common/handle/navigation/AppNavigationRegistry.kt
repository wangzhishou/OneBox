package com.shifenmiao.common.handle.navigation

import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.net.toUri
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.DataItem
import com.shifenmiao.model.HomeTabKey
import com.shifenmiao.model.webview.WebViewParams
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.utils.navigation.ScreenCallback
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
                // AgentScreen/AgentDetail 需要完整的 Agent/ChatObject 参数，裸路由统一落到智能体 tab
                extraAliases = listOf(
                    "agents", "ai_agent", "智能体", "AI 智能体",
                    "agentscreen", "agent_screen", "agentdetail", "agent_detail"
                )
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
            staticScreen(
                screen = Screen.TextCard,
                aliases = listOf("textcard", "text_card", "文字卡片", "图文卡片")
            ),
            staticScreen(
                screen = Screen.MarkupLayers(),
                aliases = listOf(
                    "markuplayers", "markup_layers", "image_creation", "图片创作", "高级图片编辑"
                )
            ).copy(
                screenBuilder = { params ->
                    Screen.MarkupLayers(
                        uri = params.mediaUri(),
                        handoffId = (params["handoff_id"] ?: params["handoffId"])
                            ?.takeIf { it.isNotBlank() }
                    )
                }
            ),
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
                routeKey = Screen.RecordCenter().routeKey,
                canonicalName = "screen.${Screen.RecordCenter().routeKey}",
                title = "健康记录",
                description = "打开健康记录模块，支持打开指定记录类型的列表页或新增/编辑记录页",
                aliases = listOf("record_center", "health_record", "health_records", "health", "records", "记录中心", "健康记录", "健康", "记录"),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    Screen.RecordCenter().routeKey,
                ),
                screenBuilder = { params ->
                    val type = params["type"].orEmpty()
                    val recordType = params["record_type"]
                        ?: params["recordType"]
                    val editingRecordId = params["editing_record_id"]
                        ?: params["editingRecordId"]
                        ?: params["record_id"]
                        ?: params["recordId"]

                    when {
                        type.equals("list", ignoreCase = true) &&
                            !recordType.isNullOrBlank() -> {
                            Screen.RecordCenter(
                                Screen.RecordCenter.Type.RecordList(
                                    recordType = recordType
                                )
                            )
                        }

                        type.equals("add", ignoreCase = true) &&
                            !recordType.isNullOrBlank() -> {
                            Screen.RecordCenter(
                                Screen.RecordCenter.Type.AddRecord(
                                    recordType = recordType,
                                    editingRecordId = editingRecordId?.takeIf { it.isNotBlank() }
                                )
                            )
                        }

                        else -> Screen.RecordCenter()
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
                routeKey = Screen.HouseholdItems().routeKey,
                canonicalName = "screen.${Screen.HouseholdItems().routeKey}",
                title = "物品管理",
                description = "打开物品管理模块,支持直达指定物品的编辑页",
                aliases = listOf("household", "household_items", "物品", "物品管理", "家庭物品", "收纳"),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    Screen.HouseholdItems().routeKey,
                ),
                screenBuilder = { params ->
                    val type = params["type"].orEmpty()
                    val itemId = params["item_id"]
                        ?: params["itemId"]

                    when {
                        type.equals("edit_item", ignoreCase = true) -> {
                            Screen.HouseholdItems(
                                Screen.HouseholdItems.Type.EditItem(
                                    itemId = itemId?.takeIf { it.isNotBlank() }
                                )
                            )
                        }

                        else -> Screen.HouseholdItems()
                    }
                }
            ),
            AppNavigationTarget(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = Screen.Period().routeKey,
                canonicalName = "screen.${Screen.Period().routeKey}",
                title = "经期记录",
                description = "打开经期记录模块，支持列表、统计、新增、编辑与详情",
                aliases = listOf("period", "period_tracker", "menstruation", "cycle", "经期", "经期记录", "月经", "例假"),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    Screen.Period().routeKey,
                ),
                screenBuilder = { params ->
                    val type = params["type"].orEmpty()
                    val recordId = params["record_id"]
                        ?: params["recordId"]

                    when {
                        type.equals("add", ignoreCase = true) -> {
                            Screen.Period(Screen.Period.Type.Add)
                        }

                        type.equals("edit", ignoreCase = true) &&
                            !recordId.isNullOrBlank() -> {
                            Screen.Period(Screen.Period.Type.Edit(recordId))
                        }

                        type.equals("detail", ignoreCase = true) &&
                            !recordId.isNullOrBlank() -> {
                            Screen.Period(Screen.Period.Type.Detail(recordId))
                        }

                        type.equals("stats", ignoreCase = true) -> {
                            Screen.Period(Screen.Period.Type.Stats)
                        }

                        else -> Screen.Period()
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
                routeKey = "gomoku_router",
                canonicalName = "screen.gomoku_router",
                title = "五子棋",
                description = "打开五子棋对局模块，支持跳转到具体棋局与在线房间",
                aliases = listOf("gomoku", "五子棋"),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    "gomoku_router"
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
                            Screen.GomokuRouter(Screen.GomokuRouter.Type.JoinOnlineRoom(roomId))
                        }

                        gameId.isNotBlank() && type.equals("analysis", ignoreCase = true) ->
                            Screen.GomokuRouter(Screen.GomokuRouter.Type.Analysis(gameId))

                        gameId.isNotBlank() -> Screen.GomokuRouter(Screen.GomokuRouter.Type.Game(gameId))
                        else -> Screen.GomokuRouter()
                    }
                }
            ),
            AppNavigationTarget(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = "chess_router",
                canonicalName = "screen.chess_router",
                title = "国际象棋",
                description = "打开国际象棋对局模块，支持跳转到具体棋局与在线房间",
                aliases = listOf("chess", "international_chess", "国际象棋"),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    "chess_router"
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
                            Screen.ChessRouter(Screen.ChessRouter.Type.JoinOnlineRoom(roomId))
                        }

                        gameId.isNotBlank() && type.equals("analysis", ignoreCase = true) ->
                            Screen.ChessRouter(Screen.ChessRouter.Type.Analysis(gameId))

                        gameId.isNotBlank() -> Screen.ChessRouter(Screen.ChessRouter.Type.Game(gameId))
                        else -> Screen.ChessRouter()
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
                aliases = listOf("theme", "theme_settings")
            ),
            staticScreen(
                screen = Screen.DisplaySettings,
                aliases = listOf("display_settings", "display")
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
            staticScreen(
                screen = Screen.MemoryManagement,
                aliases = listOf("memory_management", "ai_memory", "memory_settings", "记忆管理", "AI记忆")
            ),
            staticScreen(
                screen = Screen.SkillManagement,
                aliases = listOf("skill_management", "ai_skills", "skill_settings", "技能管理", "AI技能")
            ),
            staticScreen(
                screen = Screen.Notification,
                aliases = listOf("notification", "notifications", "message_center", "消息中心", "消息", "通知")
            ),
            // 上游/历史页面：ChildProvider 里都有对应容器，深链应直达而不是掉回首页
            staticScreen(
                screen = Screen.Settings(),
                aliases = listOf("settings", "设置"),
                titleOverride = resolveText(com.shifenmiao.core.R.string.settings, "Settings"),
                descriptionOverride = "打开设置页"
            ),
            staticScreen(
                screen = Screen.LibrariesInfo,
                aliases = listOf("libraries", "libraries_info", "open_source_libraries", "开源库", "开源许可"),
                titleOverride = "开源许可",
                descriptionOverride = "查看 App 使用的开源库与许可"
            ),
            staticScreen(
                screen = Screen.Reorderable(),
                aliases = listOf(
                    "reorderable", "reorderable_category", "manage_categories",
                    "分类管理", "管理分类"
                )
            ),
            staticScreen(
                screen = Screen.BuyCoffee(),
                aliases = listOf("buy_coffee", "coffee", "donate", "打赏", "支持作者")
            ),
            staticScreen(
                screen = Screen.UserInfo(),
                aliases = listOf("user_info", "profile_user_info", "个人信息", "账号信息")
            ),
            staticScreen(
                screen = Screen.Demo(),
                aliases = listOf("demo", "demo_screen", "演示", "演示测试")
            ),
            // 底部主入口页（tab）：不在 Screen.entries 里，深链按页面打开
            staticScreen(
                screen = Screen.Profile(),
                aliases = listOf("profile", "my", "mine", "我的", "个人中心")
            ),
            staticScreen(
                screen = Screen.Online(),
                aliases = listOf("online", "activity_log", "favorites", "发现", "活动记录", "收藏")
            ),
            staticScreen(
                screen = Screen.Search(),
                aliases = listOf("search", "搜索")
            ),
            // 设置子页与工具子页：不在 Screen.entries 里，但都是无参可渲染页面
            staticScreen(
                screen = Screen.DataSync,
                aliases = listOf("data_sync", "sync", "backup_sync", "数据同步", "备份同步")
            ),
            staticScreen(
                screen = Screen.AboutUs(),
                aliases = listOf("about", "about_us", "关于", "关于我们")
            ),
            staticScreen(
                screen = Screen.AboutAIModel,
                aliases = listOf("about_ai_model", "ai_model_about", "关于AI模型")
            ),
            staticScreen(
                screen = Screen.EditPrompt(),
                aliases = listOf("edit_prompt", "编辑提示词")
            ),
            staticScreen(
                screen = Screen.StartEntrySettings,
                aliases = listOf("start_entry_settings", "start_page", "启动页", "启动页设置")
            ),
            staticScreen(
                screen = Screen.LocalModelManagement,
                aliases = listOf("local_model_management", "local_models", "本地模型")
            ),
            staticScreen(
                screen = Screen.AuthCodeSettings,
                aliases = listOf("auth_code_settings", "验证码设置")
            ),
            staticScreen(
                screen = Screen.TokenUsage,
                aliases = listOf("token_usage", "ai_usage", "用量统计")
            ),
            staticScreen(
                screen = Screen.AIPersonalization,
                aliases = listOf("ai_personalization", "AI个性化")
            ),
            staticScreen(
                screen = Screen.TTSSettings,
                aliases = listOf("tts_settings", "voice_settings", "语音设置")
            ),
            staticScreen(
                screen = Screen.ImageGenerationSettings,
                aliases = listOf("image_generation_settings", "生图设置")
            ),
            staticScreen(
                screen = Screen.VipLevel(),
                aliases = listOf("vip_level", "vip", "会员等级")
            ),
            staticScreen(
                screen = Screen.A2UI,
                aliases = listOf("a2ui", "生成式UI")
            ),
            staticScreen(
                screen = Screen.IChingHistory,
                aliases = listOf("iching_history", "易经历史")
            ),
            staticScreen(
                screen = Screen.BlessingWallRecord,
                aliases = listOf("blessing_wall_record", "祈福墙记录")
            ),
            staticScreen(
                screen = Screen.MeshGradients,
                aliases = listOf("mesh_gradients", "网格渐变")
            ),
            staticScreen(
                screen = Screen.LifeTimeSettings,
                aliases = listOf("life_time_settings", "lifetime_settings", "人生设置")
            ),
            staticScreen(
                screen = Screen.LifeTimeAddEvent,
                aliases = listOf("life_time_add_event", "lifetime_add_event", "人生添加事件")
            ),
            staticScreen(
                screen = Screen.LifeTimeWelcome,
                aliases = listOf("life_time_welcome", "lifetime_welcome")
            ),
            staticScreen(
                screen = Screen.LifeTimeAddMilestone,
                aliases = listOf("life_time_add_milestone", "人生里程碑")
            ),
            staticScreen(
                screen = Screen.LifeTimeAddCountdown,
                aliases = listOf("life_time_add_countdown", "人生倒数日")
            ),
            staticScreen(
                screen = Screen.CreateAIAgent(),
                aliases = listOf("create_ai_agent", "新建智能体")
            ),
            staticScreen(
                screen = Screen.CreateAIChatPrompt(),
                aliases = listOf("create_ai_chat_prompt", "新建提示词")
            ),
            AppNavigationTarget(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = "poem_search",
                canonicalName = "screen.poem_search",
                title = "诗词搜索",
                description = "按关键词检索中国古诗词（可选 q 参数）",
                aliases = listOf("poemsearch", "poem_search", "search_poem", "诗词搜索"),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    "poem_search"
                ),
                screenBuilder = { params ->
                    Screen.PoemSearch(
                        initialQuery = (params["q"] ?: params["query"])?.takeIf { it.isNotBlank() }
                    )
                }
            ),
            staticScreen(
                screen = Screen.OcrDocument(),
                // 本地文本识别（RecognizeText）已下线，旧入口统一落到服务端 OCR 页
                aliases = listOf(
                    "recognize_text", "recognizetext", "text_recognition", "ocr",
                    "文本识别", "文字识别"
                )
            ),
            AppNavigationTarget(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = "library_details",
                canonicalName = "screen.library_details",
                title = "开源库详情",
                description = "查看指定开源库的许可与说明，缺参数时回开源库列表",
                aliases = listOf("librarydetails", "library_details"),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    "library_details"
                ),
                screenBuilder = { params ->
                    val libraryName = params["name"] ?: params["library_name"]
                    val htmlDescription = params["description"] ?: params["html_description"]
                    if (libraryName.isNullOrBlank() || htmlDescription.isNullOrBlank()) {
                        // 深链不带库名/说明时没有可展示的详情，退回开源库列表
                        Screen.LibrariesInfo
                    } else {
                        Screen.LibraryDetails(
                            name = libraryName,
                            htmlDescription = htmlDescription
                        )
                    }
                }
            ),
            AppNavigationTarget(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = "web_view",
                canonicalName = "screen.web_view",
                title = "网页",
                description = "用内置浏览器打开指定 URL（需带 url 参数）",
                aliases = listOf("webview", "web_view", "h5"),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    "web_view"
                ),
                screenBuilder = { params ->
                    val url = params["url"].orEmpty()
                    if (url.isBlank()) {
                        // 缺 url 时打开空白 WebView 没有意义，回首页
                        Screen.NewApp()
                    } else {
                        Screen.WebView(
                            WebViewParams(
                                title = params["title"].orEmpty(),
                                url = url
                            )
                        )
                    }
                }
            ),
            AppNavigationTarget(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = "mini_program",
                canonicalName = "screen.mini_program",
                title = "小程序",
                description = "打开指定小程序（需带 url 参数）",
                aliases = listOf("miniprogram", "mini_program"),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    "mini_program"
                ),
                screenBuilder = { params ->
                    val url = params["url"].orEmpty()
                    if (url.isBlank()) {
                        Screen.NewApp()
                    } else {
                        Screen.MiniProgram(
                            DataItem(
                                id = params["id"]?.toIntOrNull() ?: -1,
                                title = params["title"].orEmpty(),
                                description = params["description"].orEmpty(),
                                url = url
                            )
                        )
                    }
                }
            ),
            // 必须带 id 的详情页：带参数直达，裸路由回父级列表，避免深链无反馈
            AppNavigationTarget(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = "system_prompt_detail",
                canonicalName = "screen.system_prompt_detail",
                title = "系统提示词详情",
                description = "查看/编辑指定系统提示词（需带 prompt_id），缺参数时回系统提示词管理",
                aliases = listOf("systempromptdetail", "system_prompt_detail"),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    "system_prompt_detail"
                ),
                screenBuilder = { params ->
                    val promptId = (params["prompt_id"] ?: params["id"])?.toIntOrNull()
                    if (promptId == null) {
                        Screen.SystemPromptManagement
                    } else {
                        Screen.SystemPromptDetail(promptId = promptId)
                    }
                }
            ),
            AppNavigationTarget(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = "skill_detail",
                canonicalName = "screen.skill_detail",
                title = "技能详情",
                description = "查看/编辑指定技能（需带 skill_id），缺参数时回技能管理",
                aliases = listOf("skilldetail", "skill_detail"),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    "skill_detail"
                ),
                screenBuilder = { params ->
                    val skillId = (params["skill_id"] ?: params["id"])?.takeIf { it.isNotBlank() }
                    if (skillId == null) {
                        Screen.SkillManagement
                    } else {
                        Screen.SkillDetail(skillId = skillId)
                    }
                }
            ),
            AppNavigationTarget(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = "life_time_milestone_detail",
                canonicalName = "screen.life_time_milestone_detail",
                title = "人生里程碑详情",
                description = "查看指定里程碑（需带 milestone_id），缺参数时回人生",
                aliases = listOf("lifetimemilestonedetail", "life_time_milestone_detail"),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    "life_time_milestone_detail"
                ),
                screenBuilder = { params ->
                    val milestoneId = (params["milestone_id"] ?: params["id"])?.toLongOrNull()
                    if (milestoneId == null) {
                        Screen.LifeTime
                    } else {
                        Screen.LifeTimeMilestoneDetail(milestoneId = milestoneId)
                    }
                }
            ),
            AppNavigationTarget(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = "life_time_countdown_detail",
                canonicalName = "screen.life_time_countdown_detail",
                title = "人生倒数日详情",
                description = "查看指定倒数日（需带 countdown_id），缺参数时回人生",
                aliases = listOf("lifetimecountdowndetail", "life_time_countdown_detail"),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    "life_time_countdown_detail"
                ),
                screenBuilder = { params ->
                    val countdownId = (params["countdown_id"] ?: params["id"])?.toLongOrNull()
                    if (countdownId == null) {
                        Screen.LifeTime
                    } else {
                        Screen.LifeTimeCountdownDetail(countdownId = countdownId)
                    }
                }
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
                routeKey = "decision_wheel",
                canonicalName = "screen.decision_wheel",
                title = "决策转盘",
                description = "打开决策转盘模块，支持直达指定转盘的转动页",
                aliases = listOf(
                    "decision_wheel_screen", "decisionwheel", "wheel", "spin",
                    "转盘", "决策转盘", "抽奖"
                ),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    "decision_wheel"
                ),
                screenBuilder = { params ->
                    Screen.DecisionWheelScreen(
                        initialWheelId = params["wheel_id"]?.takeIf { it.isNotBlank() }
                    )
                }
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
                routeKey = "calendar_share",
                canonicalName = "screen.calendar_share",
                title = "分享日历",
                description = "把万年历做成可分享的卡片，支持版式、配色、节日、天气与心语",
                aliases = listOf(
                    "calendar_share", "share_calendar", "calendar_card",
                    "日历分享", "分享日历", "日历卡片", "黄历分享", "分享图"
                ),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    "calendar_share"
                ),
                screenBuilder = { params ->
                    Screen.CalendarShare(
                        year = params["year"]?.toIntOrNull() ?: -1,
                        month = params["month"]?.toIntOrNull() ?: -1,
                        day = params["day"]?.toIntOrNull() ?: -1,
                    )
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
                    "file_explorer", "fileexplorer",
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
            ),
            // 媒体播放：通知栏用 onebox://screen/VideoPlayer?url=<uri>&play=0 回到播放页,
            // 必须在这里解析参数,否则命中注册表后会丢掉 url/play 只打开库页。
            AppNavigationTarget(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = Screen.VideoPlayer().routeKey,
                canonicalName = "screen.${Screen.VideoPlayer().routeKey}",
                title = "视频播放",
                description = "打开本地视频播放器，可带 url 指定要播放的视频（支持画中画）",
                aliases = listOf(
                    "video_player", "video", "player", "video_play",
                    "视频", "视频播放", "播放器", "视频播放器"
                ),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    Screen.VideoPlayer().routeKey,
                ),
                screenBuilder = { params ->
                    Screen.VideoPlayer(
                        uri = params.mediaUri(),
                        autoPlay = params.autoPlayParam(),
                    )
                }
            ),
            AppNavigationTarget(
                targetType = AppNavigationTargetType.SCREEN,
                routeKey = Screen.AudioPlayer().routeKey,
                canonicalName = "screen.${Screen.AudioPlayer().routeKey}",
                title = "音乐播放",
                description = "打开本地音频播放器，可带 url 指定要播放的音频（支持后台播放）",
                aliases = listOf(
                    "audio_player", "music", "music_player", "audio",
                    "音乐", "音乐播放", "音频", "音频播放", "音乐播放器"
                ),
                deeplink = buildStructuredDeeplink(
                    AppNavigationTargetType.SCREEN,
                    Screen.AudioPlayer().routeKey,
                ),
                screenBuilder = { params ->
                    Screen.AudioPlayer(
                        uri = params.mediaUri(),
                        autoPlay = params.autoPlayParam(),
                    )
                }
            )
        )

    /** 深链里的媒体 URI：支持 url / uri 两种参数名，空串按"没带"处理。 */
    private fun Map<String, String>.mediaUri(): Uri? =
        (this["url"] ?: this["uri"])
            ?.takeIf { it.isNotBlank() }
            ?.let { Uri.parse(it) }

    /** play=0 表示"只回到页面不要自动播放"，缺省按 true。 */
    private fun Map<String, String>.autoPlayParam(): Boolean =
        (this["play"] ?: this["autoplay"] ?: this["autoPlay"])?.let {
            !(it == "0" || it.equals("false", ignoreCase = true))
        } ?: true

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
        // extraScreenTargets 在前，同名时以它的 screenBuilder 为准，但保留两边全部别名：
        // 否则 entries 里的类名别名（如 MarkTodoRouter）会被 distinctBy 丢掉，
        // onebox://screen/<类名> 就只能回退到 Screen.valueOf。
        val merged = LinkedHashMap<String, AppNavigationTarget>()
        (extraScreenTargets + Screen.entries.map(::staticScreen)).forEach { target ->
            val existing = merged[target.routeKey]
            merged[target.routeKey] = if (existing == null) {
                target
            } else {
                existing.copy(aliases = (existing.aliases + target.aliases).distinct())
            }
        }
        merged.values.toList()
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
        aliases: List<String> = emptyList(),
        titleOverride: String? = null,
        descriptionOverride: String? = null
    ): AppNavigationTarget {
        val className = screen::class.simpleName.orEmpty()
        val title = titleOverride
            ?: resolveText(screen.title, screen.name.ifBlank { className.ifBlank { screen.routeKey } })
        val description = descriptionOverride ?: screen.description.ifBlank {
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
