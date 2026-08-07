# MarkTodo Module

## 概述
MarkTodo 是一个待办事项管理模块，采用极致扁平化的 UI 风格，提供直观的看板视图，帮助用户分类管理日常任务。

## 技术栈
- **开发语言**: Kotlin
- **UI 框架**: Jetpack Compose (Material3)
- **架构模式**: MVI (配合 Decompose 使用)
- **导航框架**: `com.arkivanov.decompose`
- **依赖注入**: Hilt

## 模块结构

```
feature/marktodo/
├── build.gradle.kts
├── src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/shifenmiao/marktodo/
│   │   ├── model/
│   │   │   ├── TodoCategory.kt      # 分类数据模型
│   │   │   └── TodoTask.kt          # 任务数据模型
│   │   ├── components/
│   │   │   ├── TaskItem.kt          # 任务项组件
│   │   │   ├── CategoryHeader.kt    # 分类头部组件
│   │   │   └── CategoryCard.kt      # 分类卡片组件
│   │   ├── screenLogic/
│   │   │   └── MarkTodoComponent.kt # Decompose 组件
│   │   └── screen/
│   │       └── MarkTodoScreen.kt    # 主屏幕 UI
│   └── res/values/
│       └── strings.xml              # 字符串资源
```

## 功能特性

### 1. 动态配色系统
- 基于当前应用主题色动态生成分类颜色
- 使用 `ColorGenerator` 工具生成基色和互补色的混合色
- 自动适配深色/浅色主题
- 确保文字与背景有足够的对比度

### 2. 分类管理
默认包含 6 个分类：
- 收集箱 (Inbox)
- 自媒体 (Social Media)
- 工作 (Work)
- 看书 (Reading)
- 电影 (Movies)
- 购物 (Shopping)

每个分类卡片包含：
- 图标和标题
- 任务进度（已完成/总数）
- 任务列表预览（最多 3 个）
- 快速添加按钮

### 3. 任务管理
任务项包含：
- 完成状态复选框
- 任务标题和备注
- 日期标签
- 自定义标签
- 收藏/星标功能

### 4. UI 设计
- **极致扁平**: 无阴影或极低阴影
- **圆角设计**: 卡片和标签使用圆角
- **瀑布流布局**: 使用 LazyVerticalStaggeredGrid
- **响应式**: 自适应不同屏幕尺寸

## 使用方法

### 1. 集成到应用

在导航组件中添加 MarkTodo 路由：

```kotlin
// 在 RootComponent 中注册
@AssistedFactory
interface MarkTodoComponentFactory {
    fun create(componentContext: ComponentContext): MarkTodoComponent
}

// 在 Screen 中使用
MarkTodoScreen(
    component = markTodoComponent,
    onGoBack = { /* 返回处理 */ }
)
```

### 2. 自定义分类

在 `MarkTodoComponent` 中修改 `initializeCategories()` 方法：

```kotlin
private fun initializeCategories() {
    val baseColor = AppTheme.colorScheme.primary
    val categoryColors = ColorGenerator.generateSegmentBackgrounds(baseColor, yourCategoryCount)
    
    categories = listOf(
        TodoCategory(/* 你的分类配置 */),
        // ...
    )
}
```

### 3. 主题颜色更新

当应用主题改变时，调用 `refreshCategoryColors()` 更新分类颜色：

```kotlin
component.refreshCategoryColors()
```

## 依赖项

```kotlin
// 核心依赖
implementation(projects.core.ui)
implementation(projects.core.theme)
implementation(projects.core.utils)
implementation(projects.feature.common)

// Decompose
implementation(libs.decompose)
implementation(libs.decompose.compose)

// Hilt
implementation(libs.dagger.hilt.android)
```

## TODO 功能扩展

- [ ] 数据持久化（Room 数据库）
- [ ] 任务详情页
- [ ] 任务编辑/删除
- [ ] 分类详情页（显示所有任务）
- [ ] 任务搜索和过滤
- [ ] 任务排序（按日期、优先级等）
- [ ] 通知提醒功能
- [ ] 数据导入/导出
- [ ] 自定义分类
- [ ] 任务统计和分析

## 开发规范

### 代码规范
- 所有 Public 类、函数、属性必须包含 KDoc 注释
- 字符串必须提取到 `strings.xml`
- 遵循 Material Design 3 设计规范
- 使用 Compose 最佳实践

### 颜色使用
- 优先使用 `MaterialTheme.colorScheme`
- 动态颜色通过 `ColorGenerator` 生成
- 确保无障碍性（对比度符合 WCAG 标准）

### 组件设计
- 遵循原子设计原则
- 组件高度解耦
- 支持预览（添加 `@Preview` 注解）

## 参考资料

- [需求文档](../../doc/mrd/MarkTodo_Requirement.md)
- [开发提示词](../../doc/mrd/MarkTodo_VibeCoding_Prompts.md)
- [ColorGenerator 工具](../../core/utils/src/main/java/com/wanbaohe/com/color/ColorGenerator.kt)
- [Decompose 官方文档](https://arkivanov.github.io/Decompose/)
- [Material 3 设计规范](https://m3.material.io/)

