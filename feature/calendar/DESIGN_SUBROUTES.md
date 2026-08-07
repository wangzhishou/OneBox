# 万年历子路由拆分设计文档

## 目标

将万年历从「Tab 导航」重构为「子路由导航」模式，参考 `PdfTools` 的实现方式：
- 每个子功能独立为工具入口（日历 / 八字 / 择日 / 转换）
- 外部导航可直接定位到任意子功能
- AI Agent 可为每个子功能独立注册工具
- 保留 Picker 着陆页，无子类型时展示工具选择列表

---

## 一、Screen.kt 变更

### 1.1 新 `Screen.Calendar` 定义

```kotlin
@Serializable
@SerialName("Calendar")
class Calendar(
    val type: Type? = null,
    val year: Int = -1,
    val month: Int = -1,
    val day: Int = -1,
) : Screen(
    id = 1004,
    title = R.string.calendar,
    subtitle = R.string.calendar_description,
) {
    @Serializable
    sealed class Type(
        @StringRes val title: Int,
        @StringRes val subtitle: Int,
    ) {
        val icon: ImageVector
            get() = when (this) {
                is CalendarView -> Icons.Outlined.CalendarMonth
                is BaZi -> Icons.Outlined.AutoAwesome
                is Auspicious -> Icons.Outlined.EventAvailable
                is Convert -> Icons.AutoMirrored.Outlined.CompareArrows
            }

        @Serializable
        @SerialName("CalendarView")
        data class CalendarView(
            val year: Int = -1,
            val month: Int = -1,
            val day: Int = -1,
        ) : Type(
            title = R.string.calendar_tab,
            subtitle = R.string.calendar_tab_description,
        )

        @Serializable
        @SerialName("BaZi")
        data class BaZi(
            val year: Int = -1,
            val month: Int = -1,
            val day: Int = -1,
            val hour: Int = -1,
        ) : Type(
            title = R.string.bazi_tab,
            subtitle = R.string.bazi_tab_description,
        )

        @Serializable
        @SerialName("Auspicious")
        data class Auspicious(
            val isAvoidMode: Boolean = false,
        ) : Type(
            title = R.string.auspicious_day_tab,
            subtitle = R.string.auspicious_day_tab_description,
        )

        @Serializable
        @SerialName("Convert")
        data class Convert(
            val isSolarToLunar: Boolean = true,
        ) : Type(
            title = R.string.convert_tab,
            subtitle = R.string.convert_tab_description,
        )
    }
}
```

### 1.2 向后兼容

旧导航 `Screen.Calendar(initialTab = 1, year = 2026, month = 6, day = 1)` 需要迁移。
搜索全项目替换为新的 Type 形式：
- `initialTab = 0` → `Screen.Calendar(Screen.Calendar.Type.CalendarView(year, month, day))`
- `initialTab = 1` → `Screen.Calendar(Screen.Calendar.Type.BaZi(year, month, day, hour))`
- `initialTab = 2` → `Screen.Calendar(Screen.Calendar.Type.Auspicious())`
- `initialTab = 3` → `Screen.Calendar(Screen.Calendar.Type.Convert())`

---

## 二、模块目录结构

```
feature/calendar/
├── src/main/java/com/wanbaohe/calendar/
│   ├── router/
│   │   ├── CalendarRouterScreen.kt          ← 主路由 Screen
│   │   └── screenLogic/
│   │       └── CalendarRouterComponent.kt   ← 路由 Component
│   ├── picker/
│   │   ├── CalendarPickerScreen.kt          ← 着陆页（工具选择）
│   │   └── screenLogic/
│   │       └── CalendarPickerComponent.kt
│   ├── calendar_view/
│   │   ├── CalendarViewScreen.kt            ← 原 CalendarTab
│   │   └── screenLogic/
│   │       └── CalendarViewComponent.kt     ← 原日历逻辑
│   ├── bazi/
│   │   ├── BaZiScreen.kt                    ← 原 BaZiTab
│   │   └── screenLogic/
│   │       └── BaZiComponent.kt             ← 原八字逻辑
│   ├── auspicious/
│   │   ├── AuspiciousScreen.kt              ← 原 AuspiciousDayTab
│   │   └── screenLogic/
│   │       └── AuspiciousComponent.kt       ← 原择日逻辑
│   ├── convert/
│   │   ├── ConvertScreen.kt                 ← 原 ConversionTab
│   │   └── screenLogic/
│   │       └── ConvertComponent.kt          ← 原转换逻辑
│   ├── ui/                                  ← 共享 UI 组件（保持不动）
│   │   ├── CalendarTab.kt
│   │   ├── BaZiTab.kt
│   │   ├── AuspiciousDayTab.kt
│   │   ├── ConversionTab.kt
│   │   └── CalendarAnimations.kt
│   ├── data/                                ← 共享数据层（保持不动）
│   │   ├── CalendarModels.kt
│   │   ├── LunarCalendarCalculator.kt
│   │   ├── BaZiCalculator.kt
│   │   ├── YiJiCalculator.kt
│   │   ├── AuspiciousDayFinder.kt
│   │   ├── ChineseHourSlots.kt
│   │   └── LunarJavaBridge.kt
│   └── di/
│       └── CalendarModule.kt                ← Hilt 模块（新增）
└── src/main/res/
    ├── values/strings.xml                    ← 已有，补充新文案
    ├── values/arrays.xml                     ← 新增（AI 工具用）
    └── raw/                                  ← 新增（AI 工具长描述）
```

---

## 三、Component 拆分设计

### 3.1 `CalendarRouterComponent` — 路由分发

```kotlin
class CalendarRouterComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val type: Screen.Calendar.Type?,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    // 各子组件 Factory
    private val pickerFactory: CalendarPickerComponent.Factory,
    private val calendarViewFactory: CalendarViewComponent.Factory,
    private val baZiFactory: BaZiComponent.Factory,
    private val auspiciousFactory: AuspiciousComponent.Factory,
    private val convertFactory: ConvertComponent.Factory,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    val child: CalendarChild = when (type) {
        is Screen.Calendar.Type.CalendarView -> CalendarChild.CalendarView(
            calendarViewFactory(
                componentContext = componentContext.childContext("calendar_view"),
                initialYear = type.year,
                initialMonth = type.month,
                initialDay = type.day,
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )
        is Screen.Calendar.Type.BaZi -> CalendarChild.BaZi(
            baZiFactory(
                componentContext = componentContext.childContext("bazi"),
                initialYear = type.year,
                initialMonth = type.month,
                initialDay = type.day,
                initialHour = type.hour,
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )
        is Screen.Calendar.Type.Auspicious -> CalendarChild.Auspicious(
            auspiciousFactory(
                componentContext = componentContext.childContext("auspicious"),
                isAvoidMode = type.isAvoidMode,
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )
        is Screen.Calendar.Type.Convert -> CalendarChild.Convert(
            convertFactory(
                componentContext = componentContext.childContext("convert"),
                isSolarToLunar = type.isSolarToLunar,
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )
        null -> CalendarChild.Picker(
            pickerFactory(
                componentContext = componentContext.childContext("picker"),
                onGoBack = onGoBack,
                onNavigate = onNavigate,
            )
        )
    }

    sealed interface CalendarChild {
        class Picker(val component: CalendarPickerComponent) : CalendarChild
        class CalendarView(val component: CalendarViewComponent) : CalendarChild
        class BaZi(val component: BaZiComponent) : CalendarChild
        class Auspicious(val component: AuspiciousComponent) : CalendarChild
        class Convert(val component: ConvertComponent) : CalendarChild
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            type: Screen.Calendar.Type?,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): CalendarRouterComponent
    }
}
```

### 3.2 `CalendarViewComponent` — 日历查看

**职责**：月历网格、选中日详情、宜忌、时辰、佛历道历、未来节日

**状态**（从原 `CalendarUiState` 提取）：

```kotlin
data class CalendarViewState(
    val currentYear: Int = ...,      // 当前展示的年
    val currentMonth: Int = ...,     // 当前展示的月
    val selectedDay: Int = ...,      // 选中日
    val lunarDate: LunarDate? = null,
    val yiJi: YiJi = YiJi(),
    val timeSlots: List<LunarTimeSlot> = emptyList(),
    val fotoData: FotoData? = null,
    val taoData: TaoData? = null,
    val calendarDays: List<CalendarDayInfo> = emptyList(),
    val nextSolarTerm: Pair<String, String>? = null,
    val upcomingFestivalItems: List<UpcomingFestivalItem> = emptyList(),
    val isDataReady: Boolean = false,
)
```

**方法**：
- `selectDate(year, month, day)` — 选日
- `previousMonth()` / `nextMonth()` — 翻月
- `goToToday()` — 回今天
- `shareBitmap()` — 分享截图

### 3.3 `BaZiComponent` — 八字排盘

**职责**：四柱展示、大运走势、五行分布、流年详批、AI 解盘

**状态**：

```kotlin
data class BaZiViewState(
    val year: Int = ...,             // 八字日期（独立于日历日期）
    val month: Int = ...,
    val day: Int = ...,
    val hour: Int = ...,             // 0-23
    val baZiData: BaZiData? = null,
    val daYunList: List<DaYunItem> = emptyList(),
    val fortuneData: FortuneData? = null,
    val isDataReady: Boolean = false,
)
```

**方法**：
- `updateDate(year, month, day)` — 更新八字日期
- `updateHour(hour)` — 更新时辰
- `openBaZiAI()` — AI 解盘
- `shareBitmap()` — 分享截图

### 3.4 `AuspiciousComponent` — 择日查询

**职责**：吉日择取 / 忌事避讳、事项多选、结果列表

**状态**：

```kotlin
data class AuspiciousViewState(
    val isAvoidMode: Boolean = false,
    val selectedItems: Set<String> = emptySet(),
    val results: List<AuspiciousDayResult> = emptyList(),
    val isLoading: Boolean = false,
)
```

**方法**：
- `toggleAvoidMode()` — 切换模式
- `toggleItem(item)` — 切换事项选中
- `navigateToCalendar(year, month, day)` — 跳转到日历查看指定日

### 3.5 `ConvertComponent` — 历法转换

**职责**：公历 ↔ 农历双向转换、时辰、闰月

**状态**：

```kotlin
data class ConvertViewState(
    val isSolarToLunar: Boolean = true,
    val year: Int = ...,
    val month: Int = ...,
    val day: Int = ...,
    val hour: Int = ...,
    val isLunarLeapMonth: Boolean = false,
    val convertResult: LunarDate? = null,
    val convertSolarResult: SolarDate? = null,
    val timeSlot: LunarTimeSlot? = null,
    val fotoData: FotoData? = null,
    val taoData: TaoData? = null,
)
```

**方法**：
- `updateDate(year, month, day)` — 更新输入日期
- `updateHour(hour)` — 更新时辰
- `toggleMode()` — 切换转换方向
- `toggleLeapMonth()` — 切换闰月
- `performConversion()` — 执行转换
- `shareBitmap()` — 分享截图

### 3.6 `CalendarPickerComponent` — 着陆页

**职责**：展示 4 个子工具卡片，点击导航到对应子页面

```kotlin
class CalendarPickerComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    fun navigateTo(type: Screen.Calendar.Type) {
        onNavigate(Screen.Calendar(type = type))
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): CalendarPickerComponent
    }
}
```

---

## 四、UI 层设计

### 4.1 `CalendarRouterScreen` — 路由分发 Screen

```kotlin
@Composable
fun CalendarRouterScreen(component: CalendarRouterComponent) {
    when (val child = component.child) {
        is CalendarRouterComponent.CalendarChild.Picker ->
            CalendarPickerScreen(child.component)
        is CalendarRouterComponent.CalendarChild.CalendarView ->
            CalendarViewScreen(child.component)
        is CalendarRouterComponent.CalendarChild.BaZi ->
            BaZiScreen(child.component)
        is CalendarRouterComponent.CalendarChild.Auspicious ->
            AuspiciousScreen(child.component)
        is CalendarRouterComponent.CalendarChild.Convert ->
            ConvertScreen(child.component)
    }
}
```

### 4.2 `CalendarPickerScreen` — 着陆页 UI

参考 `PdfPickerLandingScreen`，展示 4 张工具卡片：

```kotlin
@Composable
fun CalendarPickerScreen(component: CalendarPickerComponent) {
    BaseScreen(
        title = { Text(stringResource(R.string.calendar)) },
        onGoBack = component.onGoBack,
    ) {
        LazyVerticalGrid(columns = GridCells.Fixed(2)) {
            items(tools) { tool ->
                ToolCard(
                    icon = tool.icon,
                    title = stringResource(tool.title),
                    subtitle = stringResource(tool.subtitle),
                    onClick = { component.navigateTo(tool.type) }
                )
            }
        }
    }
}
```

工具列表：
1. 📅 日历查看 → `Type.CalendarView()`
2. 📿 八字排盘 → `Type.BaZi()`
3. 🎯 择日查询 → `Type.Auspicious()`
4. 🔄 历法转换 → `Type.Convert()`

### 4.3 各子 Screen

- `CalendarViewScreen` — 复用现有的 `CalendarTab.kt` Composable
- `BaZiScreen` — 复用现有的 `BaZiTab.kt` Composable
- `AuspiciousScreen` — 复用现有的 `AuspiciousDayTab.kt` Composable
- `ConvertScreen` — 复用现有的 `ConversionTab.kt` Composable

现有的 4 个 Tab Composable 基本可以直接复用，只需将回调从 `component::method` 改为对应子组件的回调。

---

## 五、依赖注入（Hilt）

新增 `CalendarModule.kt`：

```kotlin
@Module
@InstallIn(ActivityComponent::class)
abstract class CalendarModule {
    @Binds
    abstract fun bindCalendarRouterFactory(
        factory: CalendarRouterComponent.Factory
    ): CalendarRouterComponent.Factory

    @Binds
    abstract fun bindCalendarPickerFactory(
        factory: CalendarPickerComponent.Factory
    ): CalendarPickerComponent.Factory

    @Binds
    abstract fun bindCalendarViewFactory(
        factory: CalendarViewComponent.Factory
    ): CalendarViewComponent.Factory

    @Binds
    abstract fun bindBaZiFactory(
        factory: BaZiComponent.Factory
    ): BaZiComponent.Factory

    @Binds
    abstract fun bindAuspiciousFactory(
        factory: AuspiciousComponent.Factory
    ): AuspiciousComponent.Factory

    @Binds
    abstract fun bindConvertFactory(
        factory: ConvertComponent.Factory
    ): ConvertComponent.Factory
}
```

---

## 六、Navigation 层变更

### 6.1 `ChildProvider.kt`

```kotlin
is Screen.Calendar -> NavigationChild.Calendar(
    calendarRouterComponentFactory(
        componentContext = componentContext,
        type = config.type,
        onGoBack = ::navigateBack,
        onNavigate = ::navigateTo,
    )
)
```

### 6.2 `NavigationChild.kt`

```kotlin
class Calendar(
    private val component: CalendarRouterComponent
) : NavigationChild {
    @Composable
    override fun Content() = CalendarRouterScreen(component)
}
```

---

## 七、AI Agent 工具设计

为每个子功能提供独立的 `AgentTool`，所有工具复用现有的计算引擎（`LunarCalendarCalculator`, `BaZiCalculator` 等）。

### 7.1 `LunarCalendarTool` — 农历查询

```kotlin
class LunarCalendarTool @Inject constructor(
    private val textProvider: AgentToolTextProvider,
) : AgentTool {
    override val name = "lunar_calendar_query"
    override val description = textProvider.raw(R.raw.agent_tool_description_lunar_calendar)
    override val title = textProvider.string(R.string.agent_tool_lunar_calendar_title)
    override val summary = textProvider.string(R.string.agent_tool_lunar_calendar_summary)
    override val keywords = textProvider.array(R.array.agent_tool_lunar_calendar_keywords)
    override val examples = textProvider.array(R.array.agent_tool_lunar_calendar_examples)

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "year" to ToolParameterProperty(type = "integer", description = ...),
            "month" to ToolParameterProperty(type = "integer", description = ...),
            "day" to ToolParameterProperty(type = "integer", description = ...),
        ),
        required = listOf("year", "month", "day")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        val args = Gson().fromJson(arguments, LunarCalendarArgs::class.java)
        val lunar = LunarCalendarCalculator.solarToLunar(args.year, args.month, args.day)
        return AgentToolResult(content = formatLunarInfo(lunar))
    }
}
```

**功能**：查询指定公历日期的农历信息（干支、生肖、节气、星座、宜忌、时辰吉凶、二十八宿等）。

### 7.2 `BaZiTool` — 八字排盘

**功能**：输入出生年月日时，返回四柱八字、十神、五行分布、身强身弱、喜用神。

### 7.3 `AuspiciousDayTool` — 择日查询

**功能**：输入事项名称，返回未来 N 天内最适合做该事的日期列表。

### 7.4 `LunarConvertTool` — 历法转换

**功能**：公历转农历 / 农历转公历，支持闰月。

### 7.5 资源文件

```
res/values/strings.xml:
  agent_tool_lunar_calendar_title
  agent_tool_lunar_calendar_summary
  agent_tool_lunar_calendar_param_year
  agent_tool_lunar_calendar_param_month
  agent_tool_lunar_calendar_param_day
  agent_tool_lunar_calendar_success

res/values/arrays.xml:
  agent_tool_lunar_calendar_keywords
  agent_tool_lunar_calendar_examples

res/raw/agent_tool_description_lunar_calendar.txt:
  （长描述：工具用途、返回格式、使用场景）
```

### 7.6 Hilt 注册

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class BuiltinCalendarToolModule {
    @Binds
    @IntoMap
    @StringKey("lunar_calendar_query")
    abstract fun bindLunarCalendarTool(tool: LunarCalendarTool): AgentTool

    @Binds
    @IntoMap
    @StringKey("bazi_calculation")
    abstract fun bindBaZiTool(tool: BaZiTool): AgentTool

    @Binds
    @IntoMap
    @StringKey("auspicious_day_query")
    abstract fun bindAuspiciousDayTool(tool: AuspiciousDayTool): AgentTool

    @Binds
    @IntoMap
    @StringKey("lunar_solar_conversion")
    abstract fun bindLunarConvertTool(tool: LunarConvertTool): AgentTool
}
```

---

## 八、迁移步骤

| 步骤 | 文件 | 操作 |
|------|------|------|
| 1 | `Screen.kt` | 重构 `Screen.Calendar`，添加 `Type` sealed class |
| 2 | 新建 `router/` | 创建 `CalendarRouterScreen` + `CalendarRouterComponent` |
| 3 | 新建 `picker/` | 创建 `CalendarPickerScreen` + `CalendarPickerComponent` |
| 4 | 新建 `calendar_view/` | 从原 `CalendarComponent` 提取日历逻辑 |
| 5 | 新建 `bazi/` | 从原 `CalendarComponent` 提取八字逻辑 |
| 6 | 新建 `auspicious/` | 从原 `CalendarComponent` 提取择日逻辑 |
| 7 | 新建 `convert/` | 从原 `CalendarComponent` 提取转换逻辑 |
| 8 | `CalendarScreen.kt` | 删除旧文件（Tab 导航已废弃） |
| 9 | `CalendarComponent.kt` | 删除旧文件（逻辑已拆分到子组件） |
| 10 | `di/CalendarModule.kt` | 新增 Hilt 绑定 |
| 11 | `ChildProvider.kt` | 更新为 `CalendarRouterComponent` |
| 12 | `NavigationChild.kt` | 更新为 `CalendarRouterScreen` |
| 13 | 全局搜索 | 替换旧 `Screen.Calendar(initialTab = X)` 调用 |
| 14 | `feature/ai/.../builtin/` | 新增 4 个 AgentTool |
| 15 | `feature/ai/.../BuiltinToolModule.kt` | 注册新工具 |

---

## 九、状态拆分对照表

| 原状态字段 | 归属子组件 | 说明 |
|-----------|-----------|------|
| `currentYear/month/selectedDay` | `CalendarViewComponent` | 日历当前展示日期 |
| `lunarDate` | `CalendarViewComponent` | 选中日的农历信息 |
| `yiJi` | `CalendarViewComponent` | 选中日宜忌 |
| `timeSlots` | `CalendarViewComponent` | 时辰数据 |
| `fotoData/taoData` | `CalendarViewComponent` | 佛历/道历 |
| `calendarDays` | `CalendarViewComponent` | 月历网格 |
| `nextSolarTerm` | `CalendarViewComponent` | 下一节气 |
| `upcomingFestivalItems` | `CalendarViewComponent` | 未来节日 |
| `baZiYear/Month/Day/Hour` | `BaZiComponent` | 八字独立日期 |
| `baZiData` | `BaZiComponent` | 八字四柱 |
| `daYunList` | `BaZiComponent` | 大运 |
| `fortuneData` | `BaZiComponent` | 流年 |
| `isAvoidMode` | `AuspiciousComponent` | 择日模式 |
| `selectedAuspiciousItems` | `AuspiciousComponent` | 选中事项 |
| `auspiciousDayResults` | `AuspiciousComponent` | 搜索结果 |
| `isAuspiciousLoading` | `AuspiciousComponent` | 加载状态 |
| `isConvertSolarToLunar` | `ConvertComponent` | 转换方向 |
| `convertYear/Month/Day/Hour` | `ConvertComponent` | 转换输入 |
| `isConvertLunarLeapMonth` | `ConvertComponent` | 闰月标记 |
| `convertResult` | `ConvertComponent` | 转换结果 |
| `convertSolarResult` | `ConvertComponent` | 阳历结果 |
| `convertTimeSlot` | `ConvertComponent` | 转换结果时辰 |
| `convertFotoData/TaoData` | `ConvertComponent` | 转换结果佛道历 |
| `selectedTab` | **删除** | Tab 索引不再 needed |
| `isDataReady` | 各组件各自维护 | 分阶段加载状态 |

---

## 十、关键设计决策

### 10.1 为什么保留 `feature/calendar` 单模块，不拆成 4 个 feature？

- 数据层（计算引擎 + 模型）被 4 个子功能共享，拆模块会导致重复或需要下沉到 `:core`
- 当前模块复杂度可控，子路由拆分后每个子组件职责清晰
- 减少模块数量，降低 Gradle 配置负担

### 10.2 为什么 UI State 按 Tab 拆分而不是共享一个？

- 每个子功能的状态彼此独立，没有交叉依赖
- 拆分后单个 State 类从 118 字段降到 10~15 字段，可维护性大幅提升
- 子组件只需要关注自己的状态，减少不必要的 recomposition

### 10.3 Picker 着陆页是否必要？

- `PdfTools` 采用 Picker 模式，无 type 时展示工具列表
- 万年历同理：直接点击「万年历」入口时，展示 4 个子工具卡片让用户选择
- 外部导航（如 AI Agent 调用、Deep Link）可直接带 type 直达子功能
- 保持与项目中其他 Router 模块的一致性

---

## 十一、文件变更清单

### 新增文件（14 个）

```
feature/calendar/src/main/java/com/wanbaohe/calendar/
├── router/CalendarRouterScreen.kt
├── router/screenLogic/CalendarRouterComponent.kt
├── picker/CalendarPickerScreen.kt
├── picker/screenLogic/CalendarPickerComponent.kt
├── calendar_view/CalendarViewScreen.kt
├── calendar_view/screenLogic/CalendarViewComponent.kt
├── bazi/BaZiScreen.kt
├── bazi/screenLogic/BaZiComponent.kt
├── auspicious/AuspiciousScreen.kt
├── auspicious/screenLogic/AuspiciousComponent.kt
├── convert/ConvertScreen.kt
├── convert/screenLogic/ConvertComponent.kt
├── di/CalendarModule.kt
└── .../agent/tool/builtin/  (4 个 AI Tool)
```

### 删除文件（2 个）

```
feature/calendar/src/main/java/com/wanbaohe/calendar/
├── screen/CalendarScreen.kt          ← 删除
└── component/CalendarComponent.kt    ← 删除
```

### 修改文件（4 个）

```
core/ui/.../navigation/Screen.kt                          ← 重构 Screen.Calendar
feature/app/.../navigation/ChildProvider.kt               ← 更新 Calendar 创建
feature/app/.../navigation/NavigationChild.kt             ← 更新 Calendar 包装
feature/calendar/ui/*.kt                                   ← 微调回调签名
```

---

*设计完成。确认后按迁移步骤逐步实现。*
