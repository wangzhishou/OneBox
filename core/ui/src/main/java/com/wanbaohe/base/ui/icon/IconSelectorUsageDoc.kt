/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.base.ui.icon

/**
 * # 图标选择器架构说明
 *
 * ## 核心原则
 *
 * 所有图标通过 `IconRegistry` 静态直接引用注册，**不使用反射**。
 * R8 可以 tree-shake 未被引用的 Material 图标类，显著减小 APK 体积。
 *
 * ## 关键文件
 *
 * | 文件 | 职责 |
 * |------|------|
 * | `ImageVectorMap.kt` → `IconRegistry` | 统一注册表，分 `appIcons` / `materialIcons` 两个 Map，自动合并 |
 * | `IconPickerSheet.kt` | BottomSheet 图标选择器，分"应用/通用/全部"Tab + 搜索 |
 * | `IconSelector.kt` | 轻量内联选择器（单行滚动 / 网格），适合对话框 |
 * | `Icons.kt` | 工具函数：`BuildCustomIcon`、`IconOutlinedByName`、`LetterIcon` 等 |
 *
 * ## IconRegistry API
 *
 * ```kotlin
 * IconRegistry.resolve("Home")       // → ImageVector?  按 key 获取图标
 * IconRegistry.contains("Home")      // → Boolean       判断 key 是否存在
 * IconRegistry.appKeys               // → List<String>  应用自定义图标 key（字母排序）
 * IconRegistry.materialKeys          // → List<String>  Material 标准图标 key（字母排序）
 * IconRegistry.allKeys               // → List<String>  全部图标 key（字母排序）
 * ```
 *
 * ## 添加新图标
 *
 * 1. 在 `IconRegistry` 的 `appIcons` 或 `materialIcons` 中添加 entry
 * 2. 分类自动生效，无需手动同步任何 Set
 * 3. 无需修改 ProGuard 规则（直接引用，R8 自动保留）
 *
 * ## 性能特点
 *
 * - lambda 工厂模式：Map 仅存 lambda（~16 bytes/个），ImageVector 按需创建
 * - 零反射，零运行时扫描
 * - R8 友好：未注册的 Material 图标类会被 tree-shake 移除
 * - LazyVerticalGrid：仅渲染可见区域的图标
 */
object IconSelectorUsageDoc

