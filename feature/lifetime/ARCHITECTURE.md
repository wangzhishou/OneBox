# 时光里程碑 (LifeTime) - 架构设计图

## 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                    LifeTimeScreen (UI)                      │
│  ┌─────────────┐                           ┌──────────────┐ │
│  │   Tab A:    │                           │   Tab B:     │ │
│  │   回望      │ <──── Tab 切换动画 ─────> │ 只争朝夕      │ │
│  │  (Past)     │                           │ (Seize Day)  │ │
│  └─────────────┘                           └──────────────┘ │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              LifeTimeComponent (业务逻辑)                     │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  UiState:                                            │   │
│  │  - birthDate: LocalDate?                             │   │
│  │  - currentTab: LifeTimeTab                           │   │
│  │  - pastTimeData: LifeTimeData                        │   │
│  │  - remainingLifeData: RemainingLifeData              │   │
│  │  - festivalCount: FestivalCount                      │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  实时更新协程: while(isActive) { update(); delay(1s) } │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              LifeTimeCalculator (计算引擎)                    │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  calculatePastTime(birthDate)                        │   │
│  │    → LifeTimeData (年/月/日/时/分/秒)                 │   │
│  ├──────────────────────────────────────────────────────┤   │
│  │  calculateFestivals(birthDate)                       │   │
│  │    → FestivalCount (春节/中秋/圣诞)                   │   │
│  ├──────────────────────────────────────────────────────┤   │
│  │  calculateRemainingLife(birthDate, expectedAge)      │   │
│  │    → RemainingLifeData (剩余时间 + 进度)              │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│            LifeTimeRepository (数据存储)                      │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  DataStore (持久化)                                   │   │
│  │  - birthDateFlow: Flow<LocalDate?>                   │   │
│  │  - saveBirthDate(date)                               │   │
│  │  - clearBirthDate()                                  │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## UI 组件树

```
LifeTimeScreen
├── BaseScreen (继承)
│   ├── TopAppBar
│   │   ├── 标题: "时光里程碑"
│   │   └── 返回按钮
│   └── Content
│       ├── TabRow
│       │   ├── Tab("回望")
│       │   └── Tab("只争朝夕")
│       └── AnimatedContent (Tab 内容切换)
│           ├── PastTabContent
│           │   ├── TimeCard × 3 (年/月/日)
│           │   ├── CompactTimeCard × 3 (时/分/秒)
│           │   └── FestivalCard × 3 (春节/中秋/圣诞)
│           └── SeizeDayTabContent
│               ├── LifeProgressBar (圆形电池进度)
│               ├── TimeCard × 2 (剩余年/天)
│               ├── CompactTimeCard × 3 (剩余时/分/秒)
│               └── FestivalCard × 2 (春节/日出)
└── DatePickerDialog (首次进入)
```

## 数据流向

```
用户操作
   ↓
LifeTimeScreen (UI Event)
   ↓
LifeTimeComponent (Action)
   ↓
LifeTimeRepository / Calculator
   ↓
State 更新
   ↓
UI 重组 (Recomposition)
```

## 时间线流程

```
1. 应用启动
   └→ LifeTimeComponent.init()
      └→ loadBirthDate() from Repository
         ├─ 无数据 → 显示日期选择器
         └─ 有数据 → 开始实时更新
            └→ startRealTimeUpdate()
               └→ 每秒循环:
                  ├─ calculatePastTime()
                  ├─ calculateFestivals()
                  ├─ calculateRemainingLife()
                  └─ emit 新 State → UI 更新
```

## 动画时间轴

```
时间 0ms ──────────────────────────────────────→ 1000ms
          ╔══════════════════════════════════╗
数字动画   ║  Animatable: 23 → 24 (Rolling)   ║
          ╚══════════════════════════════════╝
          
Tab 切换   ▼ 点击 Tab
          ├─ slideOutHorizontally (300ms)
          └─ slideInHorizontally (300ms)
          
进度条     ╔════════════════════════════╗
动画       ║  animateFloatAsState       ║
          ║  0.752 → 0.753 (1000ms)   ║
          ╚════════════════════════════╝
```

## 依赖关系图

```
feature:lifetime
├── core:base (BaseComponent)
├── core:model (数据模型)
├── core:theme (AppTheme, AppColors)
├── core:storage (DataStore 支持)
├── feature:common (BaseScreen, AppComponent)
├── androidx.datastore:datastore-preferences
├── com.arkivanov.decompose (导航框架)
└── dagger.hilt (依赖注入)
```

## 关键性能优化

1. **@Immutable 数据类** → 避免不必要的 Recomposition
2. **LaunchedEffect 协程** → 后台更新，不阻塞主线程
3. **Flow 响应式** → 高效的数据流
4. **remember/Animatable** → 动画状态管理
5. **collectAsState** → 最小化重组范围

## 文件大小估算

```
LifeTimeCalculator.kt     ~4 KB   (核心算法)
LifeTimeRepository.kt     ~2 KB   (数据存储)
LifeTimeComponent.kt      ~5 KB   (业务逻辑)
LifeTimeScreen.kt         ~7 KB   (主屏幕)
LifeTimeTabContents.kt    ~6 KB   (Tab 内容)
TimeCard.kt               ~4 KB   (时间卡片)
LifeProgressBar.kt        ~6 KB   (进度条)
strings.xml               ~2 KB   (字符串资源)
───────────────────────────────
总计                      ~36 KB
```

---

**设计理念**: 极致扁平化 + 动画交互 + 情感化设计
**性能目标**: 60 FPS 流畅动画 + 低内存占用
**可维护性**: Clean Architecture + 模块化 + 注释完整

