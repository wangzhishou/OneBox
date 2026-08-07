# 时光里程碑 (LifeTime) - 集成指南

## 快速集成步骤

### 1. 模块已添加到 settings.gradle.kts ✅
```kotlin
include(":feature:lifetime")
```

### 2. 在主工程 app 模块添加依赖

在 `app/build.gradle.kts` 的 dependencies 中添加：
```kotlin
implementation(projects.feature.lifetime)
```

### 3. 在 Decompose 导航中注册 LifeTime 路由

假设你的项目使用类似以下的导航结构：

#### 3.1 在 Screen sealed class 中添加 LifeTime 路由
```kotlin
// 在你的 Screen.kt 或类似文件中
sealed interface Screen {
    // ...existing screens...
    
    @Serializable
    data object LifeTime : Screen
}
```

#### 3.2 在 RootComponent 中添加 Factory 注入
```kotlin
class RootComponent @AssistedInject constructor(
    // ...existing parameters...
    private val lifeTimeComponentFactory: LifeTimeComponent.Factory,
) : ComponentContext by componentContext {
    // ...existing code...
}
```

#### 3.3 在导航 when 语句中添加处理
```kotlin
when (val child = childStack.active.instance) {
    // ...existing cases...
    
    is RootComponent.Child.LifeTime -> {
        LifeTimeScreen(
            component = child.component,
            appComponent = appComponent
        )
    }
}
```

#### 3.4 在 Child sealed class 中添加类型
```kotlin
sealed interface Child {
    // ...existing children...
    
    data class LifeTime(
        val component: LifeTimeComponent
    ) : Child
}
```

#### 3.5 在 childFactory 中创建 Component
```kotlin
private fun childFactory(
    config: Config,
    componentContext: ComponentContext
): Child = when (config) {
    // ...existing cases...
    
    is Config.LifeTime -> Child.LifeTime(
        component = lifeTimeComponentFactory(componentContext)
    )
}
```

### 4. 添加导航入口

在你的主菜单或功能列表中添加导航到 LifeTime 的入口：

```kotlin
// 例如在某个列表项中
FeatureItem(
    icon = Icons.Default.Schedule, // 或其他合适的图标
    title = "时光里程碑",
    description = "计算你的生命时间",
    onClick = {
        // 导航到 LifeTime
        navigation.navigate(Screen.LifeTime)
    }
)
```

### 5. （可选）添加图标资源

如果需要自定义图标，可以在 `res/drawable/` 中添加：
```xml
<!-- ic_lifetime.xml -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@color/primary"
        android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM12,20c-4.41,0 -8,-3.59 -8,-8s3.59,-8 8,-8 8,3.59 8,8 -3.59,8 -8,8zM12.5,7H11v6l5.25,3.15 0.75,-1.23 -4.5,-2.67z"/>
</vector>
```

## 使用示例

### 简单导航示例
```kotlin
@Composable
fun MainMenu() {
    Column {
        Button(
            onClick = { 
                // 假设你有一个 navigator
                navigator.push(Screen.LifeTime) 
            }
        ) {
            Text("打开时光里程碑")
        }
    }
}
```

### 带参数的导航（如果需要预设日期）
虽然当前实现会让用户选择日期，但如果需要从外部传入参数：

```kotlin
// 1. 修改 Screen 定义
@Serializable
data class LifeTime(
    val presetBirthDate: Long? = null  // epoch day
) : Screen

// 2. 在 Component 中处理
class LifeTimeComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val presetBirthDate: Long?,
    // ...
) {
    init {
        presetBirthDate?.let { epochDay ->
            componentScope.launch {
                repository.saveBirthDate(LocalDate.ofEpochDay(epochDay))
            }
        }
        // ...
    }
}

// 3. 使用 AssistedFactory 时传参
@AssistedFactory
interface Factory {
    operator fun invoke(
        componentContext: ComponentContext,
        presetBirthDate: Long? = null
    ): LifeTimeComponent
}
```

## 功能测试清单

集成完成后，测试以下功能：

- [ ] 首次打开显示日期选择器
- [ ] 选择出生日期后正确保存
- [ ] "回望" Tab 显示正确的已度过时间
- [ ] "只争朝夕" Tab 显示正确的剩余时间
- [ ] 数字每秒实时更新
- [ ] Tab 切换动画流畅
- [ ] 数字变化有跳动动画
- [ ] 进度条显示正确
- [ ] 返回按钮正常工作
- [ ] Toast 提示正常显示
- [ ] 旋转屏幕后状态保持
- [ ] 应用重启后数据保持

## 常见问题

### Q: 如何修改预期寿命？
A: 当前默认是 100 岁。可以在 `LifeTimeCalculator` 中修改 `EXPECTED_LIFESPAN_YEARS` 常量，或者在 UI 中添加设置选项。

### Q: 如何添加更多节日？
A: 在 `LifeTimeCalculator.calculateFestivals()` 方法中添加更多节日的计算逻辑。

### Q: 数据存储在哪里？
A: 使用 DataStore，数据存储在 `datastore/lifetime_preferences.preferences_pb`。

### Q: 如何清除用户数据？
A: 调用 `repository.clearBirthDate()` 或者在设置中添加"重置"选项。

### Q: 为什么春节和中秋的计算是简化的？
A: 因为农历计算较为复杂。如需精确计算，需要集成农历库（如 `com.github.heyanLE:LunarCalendar`）。

## 性能监控

建议添加性能监控点：

```kotlin
// 在 LifeTimeComponent 中
private val updateDuration = MutableStateFlow(0L)

private suspend fun updateTimeData(birthDate: LocalDate) {
    val startTime = System.currentTimeMillis()
    
    // ...计算逻辑...
    
    val duration = System.currentTimeMillis() - startTime
    updateDuration.emit(duration)
    
    // 如果更新耗时超过 16ms (60fps)，需要优化
    if (duration > 16) {
        Log.w("LifeTime", "Update took ${duration}ms")
    }
}
```

## 下一步优化建议

1. **农历节日支持**：集成农历计算库
2. **自定义预期寿命**：在 UI 中添加设置
3. **生命事件**：允许用户添加重要日期
4. **数据导出**：导出为图片或 PDF
5. **桌面小部件**：实时显示倒计时
6. **通知提醒**：重要日期到来时提醒
7. **主题定制**：允许选择不同的视觉风格
8. **分享功能**：分享生命时光卡片

---

**集成难度**: ⭐⭐ (简单)
**预计集成时间**: 15-30 分钟
**技术支持**: 查看 IMPLEMENTATION_SUMMARY.md 和 ARCHITECTURE.md

