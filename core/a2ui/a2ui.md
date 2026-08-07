# A2UI 组件 Schema 参考文档

A2UI（Agent-to-User Interface）是 Google 发起的开放协议，让 AI 动态生成原生 Compose UI。

## 组件 JSON 结构

```json
{
  "id": "唯一标识",
  "component": "组件类型名",
  "属性1": "值1",
  "属性2": "值2",
  "children": ["子组件ID1", "子组件ID2"],
  "action": { "event": { "name": "submit", "context": { "prompt": "..." } } }
}
```

## 属性值类型

| 类型 | 格式 | 示例 |
|---|---|---|
| 字面量字符串 | 直接写值 | `"text": "标题"` |
| 字面量数字 | 直接写数字 | `"padding": 16` |
| 字面量布尔 | true/false | `"enabled": true` |
| 数据绑定 | 统一对象格式 | `"value": {"path": "/name"}` |
| 数组 | JSON 数组 | `"colors": ["#FF0000", "#00FF00"]` |

> ⚠️ 数据绑定**必须**使用 `{"path": "/x"}` 格式，不要写成字符串 `"/x"` 或其他形式。

## 布局容器

### Column
垂直布局容器。
- `padding`: 内边距 (Int, dp, 默认 0)
- `spacing`: 子组件间距 (Int, dp, 默认 0)
- `style`: 玻璃样式 (可选: transparent/thin/regular/medium/thick/dense)
- `alignment`: 水平对齐 (String: start/center/end, 默认 start)
- `children`: 子组件 ID 数组

### Row
水平布局容器。
- `padding`: 内边距 (Int, dp, 默认 0)
- `spacing`: 子组件间距 (Int, dp, 默认 0)
- `style`: 玻璃样式 (可选: transparent/thin/regular/medium/thick/dense)
- `alignment`: 垂直对齐 (String: top/center/bottom, 默认 top)
- `children`: 子组件 ID 数组

### Card
毛玻璃卡片容器。
- `padding`: 内边距 (Int, dp, 默认 16)
- `alignment`: 内部水平对齐 (String: start/center/end, 默认 start)
- `children`: 子组件 ID 数组

### List
懒加载垂直列表。
- `spacing`: 项间距 (Int, dp, 默认 0)
- `padding`: 外边距 (Int, dp, 默认 0)
- `alignment`: 项水平对齐 (String: start/center/end, 默认 start)
- `children`: 子组件 ID 数组

### Spacer
空白间距。
- `width`: 宽度 (Int, dp, 默认 0)
- `height`: 高度 (Int, dp, 默认 0)

## 输入控件

### Button
按钮。
- `label` / `text`: 按钮文字 (String)
- `variant`: 样式 (String: filled/outlined/text/tonal/ghost, 默认 filled)
- `enabled`: 是否可用 (Boolean, 默认 true)
- `action`: 点击动作 (可选)

### TextField
文本输入框。默认宽度填满父容器，支持多行。
- `label`: 标签 (String)
- `placeholder`: 占位文字 (String)
- `value`: 绑定路径 (JSON Pointer, 如 `{"path": "/name"}`)
- `enabled`: 是否可用 (Boolean, 默认 true)
- `error`: 是否错误 (Boolean, 默认 false)
- `supportingText`: 辅助文字 (String)
- `singleLine`: 是否单行 (Boolean, 默认 false)
- `minLines`: 最小行数 (Int, 默认 1)
- `maxLines`: 最大行数 (Int, 默认：singleLine 为 1，否则无限制)

### CheckBox
复选框。
- `label`: 标签 (String)
- `checked`: 绑定路径 (JSON Pointer, 如 `{"path": "/agree"}`)
- `enabled`: 是否可用 (Boolean, 默认 true)

### Switch
开关切换。属性同 CheckBox。
- `label`: 标签 (String)
- `checked`: 绑定路径 (JSON Pointer)
- `enabled`: 是否可用 (Boolean, 默认 true)

### Slider
滑块。
- `label`: 标签 (String)
- `value`: 绑定路径 (JSON Pointer)
- `min`: 最小值 (Number, 默认 0)
- `max`: 最大值 (Number, 默认 100)
- `steps`: 步数 (Int, 默认 0)
- `enabled`: 是否可用 (Boolean, 默认 true)

### Stepper
数字步进器。
- `label`: 标签 (String)
- `value`: 绑定路径 (JSON Pointer)
- `min`: 最小值 (Int, 默认 0)
- `max`: 最大值 (Int, 默认 100)
- `step`: 步长 (Int, 默认 1)
- `enabled`: 是否可用 (Boolean, 默认 true)

### ChoicePicker
分段选择器（2-5 个互斥选项横排单选）。
- `options`: 选项数组 (Array<String> 或 Array<{label, value}>)
- `selected`: 绑定路径 (JSON Pointer)
- `enabled`: 是否可用 (Boolean, 默认 true)

### RadioGroup
单选按钮组（竖排，带组标题）。
- `label`: 组标题 (String)
- `options`: 选项数组 (Array<String> 或 Array<{label, value}>)
- `value`: 绑定路径 (JSON Pointer)
- `spacing`: 选项间距 (Int, dp, 默认 0)
- `enabled`: 是否可用 (Boolean, 默认 true)

### DateInput
日期选择（必须用于日期场景）。
- `label`: 标签 (String)
- `value`: 绑定路径 (JSON Pointer)
- `mode`: 模式 (String: date/datetime/daterange, 默认 date)
  - `date` 输出格式如 `2024-01-01`
  - `datetime` 输出格式如 `2024-01-01T09:00:00`
  - `daterange` 输出格式如 `2024-01-01 ~ 2024-01-07`，可通过 `separator` 自定义连接符
- `separator`: 日期段连接符 (String, 默认 " ~ ")
- `enabled`: 是否可用 (Boolean, 默认 true)

### TimeInput
时间选择（必须用于时间场景）。
- `label`: 标签 (String)
- `value`: 绑定路径 (JSON Pointer)
- `mode`: 模式 (String: time/timerange, 默认 time)
  - `time` 输出格式如 `09:00`
  - `timerange` 输出格式如 `09:00 ~ 18:00`，可通过 `separator` 自定义连接符
- `separator`: 时间段连接符 (String, 默认 " ~ ")
- `enabled`: 是否可用 (Boolean, 默认 true)

### ColorPicker
颜色选择器（必须用于颜色场景）。
- `label`: 标签 (String)
- `colors`: 颜色数组 (Array<String>, HEX 格式 如 "#FF0000")；未提供时使用默认色板
- `value`: 绑定路径 (JSON Pointer)
- `allowAlpha`: 是否允许透明通道 (Boolean, 默认 false)
- `enabled`: 是否可用 (Boolean, 默认 true)

### LocationPicker
位置选择器，使用系统城市选择器（必须用于城市/地点场景）。
- `label`: 标签 (String)
- `value`: 绑定路径 (JSON Pointer)，存储格式如 `省 市 区`
- `layer`: 选择层级 (Int: 1/2/3, 默认 3，分别对应 省/省市/省市区)
- `separator`: 输出连接符 (String, 默认 " ")
- `provincePath`: 单独绑定省份的路径 (JSON Pointer, 可选)
- `cityPath`: 单独绑定城市的路径 (JSON Pointer, 可选)
- `districtPath`: 单独绑定区县的路径 (JSON Pointer, 可选)
- `enabled`: 是否可用 (Boolean, 默认 true)

### RowSelector
横向标签选择器（多选）。
- `label`: 标签 (String，空字符串不渲染)
- `value`: 绑定路径 (JSON Pointer)
- `options`: 选项数组 (Array<String> 或 Array<{label, value}>)
- `maxSelected`: 最大可选数量 (Int, 默认 0，0 表示不限制)
- `spacing`: 选项间距 (Int, dp, 默认 0)
- `padding`: 内边距 (Int, dp, 默认 0)
- `enabled`: 是否可用 (Boolean, 默认 true)

### ColumnSelector
纵向标签选择器（默认单选，可含自定义输入项）。
- `label`: 标签 (String，空字符串不渲染)
- `value`: 绑定路径 (JSON Pointer)
- `options`: 选项数组 (Array<String> 或 Array<{label, value, kind?}>，`kind="custom"` 表示自定义项)
- `maxSelected`: 最大可选数量 (Int, 默认 1)
- `selectIndex`: 默认选中索引 (Int, 默认 -1)
- `spacing`: 选项间距 (Int, dp, 默认 0)
- `padding`: 内边距 (Int, dp, 默认 0)
- `enabled`: 是否可用 (Boolean, 默认 true)
- `children`: 自定义输入子组件 ID 数组

### GridSelector
网格标签选择器（多选）。
- `label`: 标签 (String，空字符串不渲染)
- `value`: 绑定路径 (JSON Pointer)
- `options`: 选项数组 (Array<String> 或 Array<{label, value}>)
- `columns`: 列数 (Int, 默认 3)
- `maxSelected`: 最大可选数量 (Int, 默认 0，0 表示不限制)
- `spacing`: 选项间距 (Int, dp, 默认 0)
- `padding`: 内边距 (Int, dp, 默认 0)
- `enabled`: 是否可用 (Boolean, 默认 true)

### ListSelector
列表选择器（单选显示单选按钮，多选显示复选框）。
- `label`: 标签 (String，空字符串不渲染)
- `value`: 绑定路径 (JSON Pointer)
- `options`: 选项数组 (Array<String> 或 Array<{label, value}>)
- `maxSelected`: 最大可选数量 (Int, 默认 1)
- `spacing`: 选项间距 (Int, dp, 默认 0)
- `padding`: 内边距 (Int, dp, 默认 0)
- `enabled`: 是否可用 (Boolean, 默认 true)

## Selector 组件通用说明

### options 格式
`options` 支持两种写法：

1. 字符串数组：`["选项A", "选项B"]`
2. 对象数组：`[{"label": "显示文字", "value": "实际值"}]`

### 单选与多选
- `maxSelected = 1` 或未设置 → 单选，dataModel 字段类型为 **字符串** `""`
- `maxSelected = 0` → 多选不限数量，dataModel 字段类型为 **数组** `[]`
- `maxSelected > 1` → 多选且限制数量，dataModel 字段类型为 **数组** `[]`

### 选择指南
| 场景 | 推荐组件 | 默认选择模式 | 渲染形态 |
|---|---|---|---|
| 2-5 个选项横排单选 | ChoicePicker | 单选 | 分段按钮 |
| 选项较多竖排单选 | RadioGroup | 单选 | 单选按钮 |
| 横向标签多选 | RowSelector | 多选 | 横向 chip |
| 纵向标签单/多选 | ColumnSelector | 单选 | 纵向 chip |
| 网格多选 | GridSelector | 多选 | 网格 chip |
| 列表单/多选 | ListSelector | 单选 | 列表 |

## 展示控件

### Text
文本。
- `text`: 内容 (String)
- `style`: 样式 (String: displayLarge/displayMedium/displaySmall/headlineLarge/headlineMedium/headlineSmall/titleLarge/titleMedium/titleSmall/bodyLarge/bodyMedium/bodySmall/labelLarge/labelMedium/labelSmall 等，默认 bodyMedium)
- `weight` / `fontWeight`: 字重 (String: bold/semibold/medium/light/normal)
- `fontSize`: 字号 (Int, sp)
- `align` / `textAlign`: 对齐 (String: start/center/end/justify)
- `maxLines`: 最大行数 (Int)
- `color`: 颜色 (String, HEX)
- `italic`: 斜体 (Boolean)

### Image
图片。
- `src`: 图片 URL (String)
- `height`: 高度 (Int, dp, 默认 200)
- `scale`: 缩放模式 (String: fit/crop/fill/inside, 默认 crop)
- `description`: 无障碍描述 (String)

### Icon
图标。
- `name`: 图标名 (String, 如 "Home"/"Settings"/"Star")
- `size`: 大小 (Int, dp, 默认 24)
- `color`: 颜色 (String, HEX)
- `description`: 无障碍描述 (String)

### Divider
分隔线。
- `thickness`: 粗细 (Number, dp, 默认 1)
- `padding`: 垂直间距 (Int, dp, 默认 8)
- `color`: 颜色 (String, HEX)

### Tabs
标签页。
- `children`: Tab 子组件 ID 数组，每个子组件的 `title`/`label`/`text` 属性作为标签文字

### Badge
徽章。
- `text`: 文字 (String)
- `style`: 样式 (String: error/success/warning/info, 默认 success)

### AudioPlayer
音频播放器占位卡片。
- `src`: 音频 URL (String)
- `label`: 显示标签 (String, 默认 "Audio")

### Video
视频播放器占位卡片。
- `src`: 视频 URL (String)
- `height`: 高度 (Int, dp, 默认 200)

## 反馈控件

### Progress
进度指示器。
- `type`: 类型 (String: circular/linear, 默认 circular)
- `progress`: 进度值 (Number, 0-100, 不设则不确定模式)

### Modal
模态对话框。
- `title`: 标题 (String)
- `visible`: 可见性绑定路径 (JSON Pointer)
- `dismissText`: 关闭按钮文字 (String, 默认 "关闭")
- `confirmText`: 确认按钮文字 (String, 可选)
- `action`: 确认动作 (可选)
- `children`: 内容子组件 ID 数组

## 提交动作格式

```json
"action": {
  "event": {
    "name": "submit",
    "wantResponse": true,
    "context": {
      "prompt": "字段1: ${field1}\n字段2: ${field2}"
    }
  }
}
```

`${field}` 会被 `dataModel` 中对应字段的值替换。
