# 浮层菜单统一改造记录

> 生成时间:2026-10-11 07:25(当日又经两轮实测修正,见第六节)
> 状态:**尚未提交**(菜单改动全部停留在 working tree;棋类 WIP 已另行提交为 `4afc39f6 feat(boardgames)`,HEAD 已前进)
> 分支:`main`(无新分支、无 stash)

---

## 一、这次要解决的问题

三件事,都是同一个出发点:App 里「⋯ 更多」入口和它弹出的菜单长得五花八门。

1. **图标不统一** —— 同一个「更多」入口有三套画法:自定义线框图标 `Icons.Outlined.LineMore`、Material `Icons.Outlined.MoreVert`、`Icons.Default.MoreVert`。
2. **菜单样式不统一** —— 各页面自己写 `DropdownMenu(containerColor = surfaceContainerHigh)`、自己写圆角、有没有阴影看运气。
3. **箭头指向不对** —— 参考截图(主题快捷设置弹窗)那种「圆角气泡 + 指向点击位置的小箭头」只有那一个弹窗有,且是手写的。

---

## 二、最终形态

统一到 `core/ui` 里的一个组件:`EnhancedDropdownMenu`。

```kotlin
Box {                                   // 外层 Box 必须包住入口按钮,菜单用它算箭头指向
    IconButton(onClick = { expanded = true }) {
        Icon(MaterialIcons.Filled.MoreVert, null)
    }
    EnhancedDropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        // 可选:
        // arrowAlignment = Alignment.End,     强制箭头贴哪一侧(默认自动判断)
        // expandUp = true,                    向上展开(气泡在上、箭头朝下)
        // alignToAnchor = true,               菜单挂在整条 Row 里、没有专属 Box 时用系统锚点
        // maxWidth = 220.dp,
        // contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    ) {
        DropdownMenuItem(text = { Text("…") }, onClick = { … })
    }
}
```

行为约定:

| 项 | 取值 | 说明 |
|---|---|---|
| 背景 | `colorScheme.surface` | 与参考弹窗一致(采样过截图,它就是纯 surface) |
| 阴影 | 2dp | 与参考弹窗一致;曾误改为「无阴影 + 描边」,丢了浮层感,已改回 |
| 描边 | 跟随设置 | 仅 `borderWidth > 0` 时画 `outlineVariant` 发丝描边,默认无(同参考弹窗) |
| 圆角 / 箭头 | 16dp / 10dp | `BubbleShape`,箭头与气泡连成一体 |
| 箭头位置 | 对准入口按钮中心 | 位置与偏移由同一套公式推导,见下文 |
| 展开方向 | 自动 | 入口在屏幕左半 → 箭头贴左;右半 → 贴右 |
| 宽度 | 按内容自适应 | 用 Material3 的 `width(IntrinsicSize.Max)` 做法 |

---

## 三、核心实现要点(踩过的坑)

### 1. 锚点容器会被量成 0×0

`TopAppBar` 的 `actions` 槽给子项的约束是 0dp,包住入口按钮的那个 `Box` 会量出 `0 x 0`,`containerBounds` 退化成左上角一个点,箭头就没法定位。

修法:给锚点容器加 `defaultMinSize(48.dp)`,让它在任何约束下都能量出真实矩形。

### 2. `BubbleShape.arrowOffset` 的语义

它是**从形状贴边那一侧量到箭尖**的距离(做过单独探针校准:300dp 方框配 `arrowOffset = 45.dp`,箭尖中心量到距右边缘 44.7dp)。

### 3. 位置与箭头偏移必须同源(含 clamp 与箭头净空)

箭头偏移不能只看气泡宽度,必须看 **clamp 之后的最终位置**:右上角的 ⋯ 会让气泡被屏幕右缘顶回去,若箭头还按理想居中位置算,就出现「气泡贴边、箭头在中间」(通知中心实测漂了 ~60dp)。更进一步,贴边时箭尖距边缘只剩 ~10dp,箭尾会骑上圆角弧线、视觉断开——所以定位本身就要保箭头净空。现在位置与偏移统一收敛到 `bodyLeftFor` 一个函数:

```kotlin
/** 气泡主体左边缘(窗口坐标):居中 → 边距 clamp → 箭头净空(边距让步到 hardMargin) */
private fun bodyLeftFor(
    anchorCenterX: Float, bodyWidth: Int, tipInset: Float, alignToEnd: Boolean,
    sideMargin: Float, hardMargin: Float, windowWidth: Int
): Float

/** 箭尖到气泡贴边侧的距离:内部复算 bodyLeftFor 后按入口中心反推,净空不足才收紧 */
private fun arrowOffsetFor(
    anchorCenterX: Float, bodyWidth: Int, tipInset: Float, sideMargin: Float,
    hardMargin: Float, windowWidth: Int, alignToEnd: Boolean
): Float
```

`tipInset = cornerRadius + arrowSize`(16+10=26dp,对齐主题弹窗的 24dp 固定 inset),保证箭尾完全落在直边上。

### 4. 左右翻转判定曾经写错(箭头偏移的根因)

最早的条件是「右侧剩余宽度 < 菜单宽度就翻到左边」。右上角的 ⋯ 按钮只有 48dp,它右边到屏幕边缘本来就不够一个菜单宽,于是**每次打开都被误判成放不下,气泡翻到了左边**,箭头跟着跑偏:

```
容器 = [1188..1332],屏幕宽 1344
右侧剩余 156 px < 菜单最小宽度 480 px  ⇒ 误判翻左
```

改成按「入口在哪半边」判断:

```kotlin
val centerX = bounds?.center?.x ?: (windowSize.width / 2f)
val remainsRight = windowSize.width - centerX
val remainsLeft = centerX
remainsRight >= minWidthPx || remainsRight >= remainsLeft   // 靠右
```

---

## 四、本次改动的文件清单

合计 **31 个文件,+905 / −644 行**(TopAppBarEmoji 已整体回退,不计入;棋类 WIP 已提交进 `4afc39f6`,此处统计为纯菜单改动)。

### 4.1 核心组件

| 文件 | 增/删 | 内容 |
|---|---|---|
| `core/ui/src/main/kotlin/.../widget/enhanced/EnhancedDropdownMenu.kt` | +363 / −28 | 组件重写:气泡+箭头、自动左右、`expandUp`、`alignToAnchor`、clamp 同源几何、2dp 阴影 |

### 4.2 其它浮层菜单对齐样式

| 文件 | 增/删 | 内容 |
|---|---|---|
| `core/ui/src/main/kotlin/.../widget/other/TopAppBarEmoji.kt` | 0 / 0 | ~~去 2dp 阴影、加发丝描边~~ **已回退**:原弹窗就是参考样式,保持不动 |
| `core/filters/.../widget/FilterItem.kt` | +4 / −3 | 换 kebab 图标;去掉写死的 `shape = ShapeDefaults.large` |
| `core/ui/.../picker/ChineseDatePickerDialog.kt` | +2 / −1 | 年月菜单改走统一组件 |
| `core/ui/.../picker/ChineseDateRangePickerDialog.kt` | +2 / −1 | 同上 |

### 4.3 各功能页(换图标 + 迁菜单)

| 文件 | 增/删 |
|---|---|
| `feature/markdown-edit/.../MarkdownEditorScreen.kt` | +5 / −6 |
| `feature/code-editor/.../CodeEditorScreen.kt` | +6 / −5 |
| `feature/notification/.../NotificationScreen.kt` | +5 / −5 |
| `feature/settings/.../skill/SkillManagementScreen.kt` | +2 / −2 |
| `feature/settings/.../ai/AIEngineSettingsScreen.kt` | +3 / −2 |
| `feature/teleprompter/.../ScriptCard.kt` | +5 / −8 |
| `feature/household-items/.../HouseholdItemsScreen.kt` | +3 / −2 |
| `feature/sudoku/.../SudokuGameContent.kt` | +5 / −4 |
| `feature/app/.../FavoriteScreen.kt` | +6 / −5 |
| `feature/webview/.../common/WebViewTopAppBar.kt` | +16 / −11 |
| `feature/webview/.../MarkdownRenderWebViewScreen.kt` | +16 / −11 |
| `feature/file-browser/.../FileBrowserScreen.kt` | +2 / −2 |
| `feature/calendar/.../CalendarTab.kt` | +2 / −2 |
| `feature/lifetime/.../LifeTimeAddEventScreen.kt` | +3 / −2 |
| `feature/dsh-client/.../ChatScreen.kt` | +7 / −4 |
| `feature/markup-layers/.../editor/EditorScaffold.kt` | +2 / −2 |
| `feature/markup-layers/.../components/LayersSheet.kt` | +2 / −2 |
| `feature/markup-layers/.../components/LayerControls.kt` | +2 / −2 |

### 4.4 棋类「新对局」菜单(删掉各自复制的实现)

| 文件 | 增/删 | 内容 |
|---|---|---|
| `feature/chess/.../NewGameDropMenu.kt` | +150 / −176 | 删本地 `Popup + BubbleShape(8dp) + 2dp 阴影`,改走统一组件,`expandUp = openAbove`,锚点 `matchParentSize()` |
| `feature/xiangqi/.../NewGameDropMenu.kt` | +148 / −174 | 同上 |
| `feature/gomoku/.../NewGameDropMenu.kt` | +148 / −174 | 同上 |

---

## 五、⚠️ 提交前必须注意

### 5.1 六个棋类文件现在是纯菜单改动

~~这些文件里混着未提交的对局/摆棋 WIP~~ —— WIP 已提交为 `4afc39f6 feat(boardgames)`,
这 6 个文件的剩余 diff **每个只有菜单那 2 行**(+2/−2),可以放心整文件 `git add`:

- `feature/chess/.../ChessGameScreen.kt`、`ChessSetupScreen.kt`
- `feature/xiangqi/.../XiangqiGameScreen.kt`、`XiangqiSetupScreen.kt`
- `feature/gomoku/.../GomokuGameScreen.kt`、`GomokuSetupScreen.kt`

### 5.2 与本次改造完全无关的改动(已随 `4afc39f6` 提交,working tree 已无残留)

- `docs/xiangqi-local-engine.md`
- `feature/boardgame/**`(含新增 `BoardGameControls.kt`、`BoardGameNavigationBar.kt`、`res/`)
- `feature/chess/.../ChessRouterScreen.kt`、`ChessPlayHomeScreen.kt`、`ChessPlayersRow.kt`
- `feature/gomoku/.../GomokuRouterScreen.kt`、`GomokuPlayHomeScreen.kt`
- `feature/xiangqi/.../XiangqiRouterScreen.kt`、`XiangqiPlayHomeScreen.kt`、`ui/board/XiangqiBoard.kt`、新增 `ui/board/XiangqiBoardGeometry.kt`、新增 `src/test/`
- 6 个棋类模块的 `res/values-zh-rCN|zh-rTW/strings.xml`

---

## 六、验证情况

- `./gradlew :app:assembleGoogleArm64Debug` 通过
- `./gradlew :app:assembleGoogleArm64Release` 通过(过 R8)
- 涉及模块单模块编译全部通过:`core:ui`、`core:filters`、`feature:markdown-edit|code-editor|notification|settings|teleprompter|household-items|sudoku|app|webview|file-browser|calendar|lifetime|dsh-client|markup-layers|chess|xiangqi|gomoku`
- 模拟器实测(OneBox_Dev / emulator-5582):
  - Markdown 编辑器:⋯ 图标为新实心竖三点;菜单成气泡、箭头对准图标
  - 代码编辑器:同上
  - 逐像素扫描确认:箭尖中心 x≈1252,⋯ 三点中心 x=1259.5,**偏差 < 3dp**
- 临时探针(`BubbleShapeProbe.kt`)、`Log.d` 诊断、空回调残留已全部清除

### 二次修正(2026-10-11 上午,用户反馈后)

第一版有两个实测问题,已修复并复测:

1. **阴影被误删** —— 曾把「阴影无、靠 1dp 描边」当成参考样式(还把主题快捷设置弹窗原有的
   2dp 阴影一起去掉了)。实际参考弹窗就是 **2dp 阴影、无描边**。已改回:统一组件
   `shadowElevation = 2.dp`、描边恢复跟随 `borderWidth` 设置(默认无);
   `TopAppBarEmoji.kt` 整体回退不动。
2. **贴边时箭头跑偏** —— 通知中心实测箭头偏离 ⋯ 图标 ~60dp。根因:气泡位置被屏幕边缘
   clamp 后,箭头偏移仍按未 clamp 的理想居中位置算(`arrowOffsetFor = popupWidth/2 + arrowSize`)。
   修复:`arrowOffsetFor` 改为内部先复算 `bubbleLeftFor` 的 clamp 结果、再按入口中心反推
   (安全区只限制箭尾不骑圆角弧线,`cornerRadius - arrowSize`;一开始用 `cornerRadius`
   收紧还把箭尖拽偏了 5dp,探针日志定位)。

复测(`:app:assembleGoogleArm64Debug` 装 OneBox_Dev 逐像素扫描):

| 页面 | 箭尖 vs ⋯ 图标中心 | 阴影 |
|---|---|---|
| 通知中心 ⋯ | 偏差 2.5px ≈ **0.8dp** | ✓ |
| Markdown 编辑器 ⋯ | 偏差 3px ≈ **1dp** | ✓ |
| 主题快捷设置弹窗(Base64 工具页入口) | 恢复原样,箭头指调色盘图标 | ✓(2dp) |

### 三次修正(2026-10-11,棋类菜单实测又抓到两处)

1. **内容水平内边距把气泡整体拽偏** —— 定位器里有 `x = bubbleLeft - sidePadding` 的"补偿",
   但 `contentPadding` 的水平 padding 在 Surface 内部,只会让气泡变宽、不会挪动主体。
   棋类菜单传了 `horizontal = 8.dp`,气泡被平白左移 16dp,贴边 clamp 后箭头够不到图标
   (象棋顶部 + 实测偏 11dp)。已删掉这个补偿(`sidePadding` 参数整个移除)。
2. **宽触发器锚点错位** —— 棋类底栏「更多」胶囊经 `Modifier.weight(1f)` 撑宽 ~136dp,
   组件自量的锚点 Box 只有 `defaultMinSize` 兜底的 48dp 且贴左上,箭头指向胶囊左侧而非中心
   (实测偏 ~26dp)。三个 `NewGameDropMenu.kt` 调用处补 `modifier = Modifier.matchParentSize()`,
   让锚点覆盖整个触发区域;顶部栏用法(48dp 紧凑 Box)不受此影响。

复测(探针日志 + 逐像素双验证):

| 场景 | 结果 |
|---|---|
| 象棋顶部 ⊕ 菜单 | 偏差 2.5px ≈ **0.8dp** |
| 象棋底栏「More」胶囊(expandUp 箭头朝下) | 探针:箭尖 x=1138,胶囊中心 1137.5,**数学上精确**;截图目视居中 |
| 五子棋/国际象棋 | 与象棋同一组件同一调用形态,随组件修复 |

### 四次修正(2026-10-11,箭头骑圆角)

- **现象**:贴边 clamp 时箭尖距气泡边缘只剩 ~10dp,箭尾压在圆角弧线上,箭头和气泡看着不像一体。
- **修法**:定位引入箭头净空约束 `tipInset = cornerRadius + arrowSize`(26dp,对齐主题弹窗的
  24dp 固定 inset),气泡被屏幕边缘顶回去时,边距可从 8dp 让步到 2dp 硬底线
  (`MENU_SCREEN_HARD_MARGIN`)来保净空;同时删掉气泡贴边侧那个无用的 10dp 空 padding
  (它吃掉屏幕预算,让右上角场景在数学上无法同时满足贴边 + 净空)。
  定位与箭头偏移统一收敛到 `bodyLeftFor`。
- **复测**:象棋顶部 / 通知中心偏差 2.5px ≈ 0.8dp,箭尾完全在直边上、不再骑圆角;
  底栏胶囊居中保持。

### 五次修正(2026-10-11,对局/摆棋底栏「更多」)

- **现象**:国际象棋对局页底栏 More 菜单气泡被系统钳位乱摆,箭头碎成一小片漂在远处。
- **根因**:6 个对局/摆棋页(三棋 × GameScreen/SetupScreen)迁移时只换了组件名,没传
  `expandUp`——锚点在屏幕底部,默认向下展开,下方空间不够,弹窗被系统钳位后箭头(朝上的)
  与气泡错位分离。这些入口还是 `Box(Modifier.weight(1f))` 宽单元格,同样缺 `matchParentSize()`。
- **修法**:6 处调用统一补 `expandUp = true` + `modifier = Modifier.matchParentSize()`。
- **复测**:国际象棋、象棋对局页 More —— 气泡在底栏上方完整展开,箭头朝下正对 ⋮ 图标。

---

## 七、建议的提交拆分

```bash
# 1) 统一菜单组件 + 各页面接入 + 图标统一
git add core/ui/.../enhanced/EnhancedDropdownMenu.kt \
        core/filters/.../FilterItem.kt \
        core/ui/.../picker/ChineseDatePickerDialog.kt \
        core/ui/.../picker/ChineseDateRangePickerDialog.kt \
        feature/markdown-edit/... feature/code-editor/... feature/notification/... \
        feature/settings/... feature/teleprompter/... feature/household-items/... \
        feature/sudoku/... feature/app/... feature/webview/... feature/file-browser/... \
        feature/calendar/... feature/lifetime/... feature/dsh-client/... feature/markup-layers/... \
        feature/chess/.../NewGameDropMenu.kt feature/xiangqi/.../NewGameDropMenu.kt feature/gomoku/.../NewGameDropMenu.kt
git commit -m "refactor(ui): 浮层菜单统一为带箭头气泡样式,⋯ 入口统一 MoreVert 图标"

# 2) 定位/箭头修正(可与 1 合并)
git commit -m "fix(ui): 气泡菜单箭头按 clamp 后最终位置反推,修复贴边跑偏与宽触发器锚点错位"

# 3) 6 个棋类文件的剩余 diff 已是纯菜单改动(WIP 已提交),直接整文件 add 即可
```
