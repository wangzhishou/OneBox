# 分类详情页使用指南

## 功能概述

分类详情页允许用户查看和管理单个分类下的所有待办任务，支持筛选、排序、添加、完成、标星和删除等操作。

## 核心特性

### 1. 任务展示
- **完整信息**：显示任务标题、备注、标签和截止日期
- **状态可视化**：已完成任务显示划线效果
- **星标标识**：已标星任务高亮显示

### 2. 任务筛选
- **全部**：显示所有任务
- **未完成**：只显示待处理任务
- **已完成**：只显示已完成任务
- **已标星**：只显示已标星任务

### 3. 任务操作
- **完成/取消完成**：点击复选框切换状态
- **标星/取消标星**：点击星标按钮
- **删除任务**：点击删除按钮
- **添加任务**：点击工具栏 + 按钮

### 4. 统计信息
顶部显示：
- 总任务数
- 已完成数
- 进行中数量

## 使用流程

### 进入详情页
1. 在 MarkTodo 主页
2. 点击任意分类卡片
3. 自动导航到该分类的详情页

### 管理任务
1. **查看任务**：
   - 滚动浏览任务列表
   - 查看任务的完整信息

2. **筛选任务**：
   - 点击顶部筛选按钮
   - 选择所需的筛选条件
   - 列表自动更新

3. **完成任务**：
   - 点击任务前的复选框
   - 任务立即显示划线效果
   - 统计数据自动更新

4. **标星任务**：
   - 点击任务右侧的星标按钮
   - 星标变为实心高亮状态

5. **删除任务**：
   - 点击任务右侧的删除按钮
   - 任务立即从列表移除
   - 显示删除成功提示

6. **添加任务**：
   - 点击右上角 + 按钮
   - 填写任务信息
   - 点击确认
   - 新任务立即出现在列表中

### 返回主页
- 点击左上角返回按钮
- 或使用系统返回手势

## 技术实现要点

### 状态管理
```kotlin
data class CategoryDetailUiState(
    val category: TodoCategory?,
    val filteredTasks: List<TodoTask>,
    val filterMode: TaskFilterMode,
    val sortMode: TaskSortMode,
    val isLoading: Boolean,
    val error: String?
)
```

### 事件处理
```kotlin
// 切换任务完成状态
component.handleEvent(
    CategoryDetailUiEvent.ToggleTaskComplete(task)
)

// 筛选任务
component.handleEvent(
    CategoryDetailUiEvent.ChangeFilter(TaskFilterMode.ACTIVE)
)

// 添加任务
component.handleEvent(
    CategoryDetailUiEvent.AddTaskClicked
)
```

### 乐观更新
所有操作都采用乐观更新策略：
1. 立即更新 UI 状态
2. 异步写入数据库
3. 如果失败再回滚（当前版本未实现回滚）

## 扩展开发

### 添加新的筛选条件
1. 在 `TaskFilterMode` 添加新枚举值
2. 在 `applyFiltersAndSort()` 添加筛选逻辑
3. 在 UI 添加对应的筛选按钮

### 添加新的排序方式
1. 在 `TaskSortMode` 添加新枚举值
2. 在 `applyFiltersAndSort()` 添加排序逻辑
3. 在 UI 添加排序菜单项

### 实现任务编辑
1. 创建 EditTaskDialog 组件
2. 在 `handleTaskClick()` 中打开编辑对话框
3. 添加 `UpdateTask` 事件和处理逻辑

## 性能优化

### 已实现
- ✅ StateFlow 细粒度状态更新
- ✅ LazyColumn 虚拟化列表
- ✅ key 参数确保高效更新
- ✅ 组件分离减少重组范围
- ✅ 乐观更新提升响应速度

### 可选优化
- 添加分页加载（任务数量很大时）
- 实现虚拟滚动优化
- 缓存筛选结果
- 防抖处理高频操作

## 故障排查

### 问题：点击分类卡片没反应
- 检查 MarkTodoComponent 是否正确传入 onNavigate
- 检查 Screen.CategoryDetail 是否在 ChildProvider 中配置

### 问题：任务列表不更新
- 检查 StateFlow 是否正确收集
- 检查 repository 方法是否正确调用
- 查看 logcat 日志

### 问题：筛选不生效
- 检查 filterMode 状态是否正确更新
- 检查 applyFiltersAndSort() 逻辑
- 确认 UI 收集了最新状态

## 相关文件

- **状态模型**：`model/CategoryDetailUiState.kt`
- **业务逻辑**：`screenLogic/CategoryDetailComponent.kt`
- **UI 界面**：`screen/CategoryDetailScreen.kt`
- **任务组件**：`components/TaskItem.kt`
- **导航配置**：`app/.../navigation/ChildProvider.kt`

## 设计理念

1. **单一职责**：每个组件只负责一个功能
2. **状态驱动**：UI 完全由状态驱动，无副作用
3. **事件驱动**：用户操作通过事件系统传递
4. **乐观更新**：优先考虑用户体验
5. **可测试性**：业务逻辑与 UI 完全分离

## 总结

分类详情页实现了完整的任务管理功能，采用现代化的 Compose 架构，遵循 Material Design 3 规范，提供流畅的用户体验。代码结构清晰，易于维护和扩展。

