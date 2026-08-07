# core:dynamic-ui

面向 AI 生成和声明式配置的新协议动态 UI 引擎。

## 核心规则

- 外层字段只放框架元数据。
- `props` 放组件全部 Compose 参数。
- `children` 放默认内容。
- `actions` 放事件到行为的映射。
- `listConfig` 放列表模板配置。

## 根结构

```json
{
  "dataContext": {
    "keyword": "",
    "items": []
  },
  "root": {
    "type": "Column",
    "id": "root",
    "if": "${state.visible}",
    "prompt": "可选",
    "props": {
      "modifier": [
        { "fillMaxSize": true },
        { "padding": 16 }
      ],
      "verticalArrangement": "Top",
      "horizontalAlignment": "Start"
    },
    "children": []
  }
}
```

## 保留字段

- `type`
- `id`
- `prompt`
- `if`
- `meta`
- `props`
- `children`
- `actions`
- `listConfig`

## 节点规则

- 组件参数必须全部进入 `props`。
- 顶层不允许再写 `text`、`label`、`topBar`、`trailingIcon` 这类组件参数。
- `props` 内允许 primitive、object、`UiNode`、`UiNode[]`。
- `actions` 的 key 必须是事件名，例如 `onClick`、`onValueChange`。

## Modifier 规则

- `modifier` 位于 `props.modifier`。
- `modifier` 必须是对象数组，以保留顺序。

```json
"props": {
  "modifier": [
    { "fillMaxWidth": true },
    { "padding": { "horizontal": 16, "vertical": 8 } },
    { "background": { "color": "#FFFFFF" } }
  ]
}
```

## 节点参数示例

`OutlinedTextField` 的 `trailingIcon` 是 Compose 参数，因此直接放在 `props`：

```json
{
  "type": "OutlinedTextField",
  "props": {
    "value": "${state.keyword}",
    "label": "关键词",
    "readOnly": true,
    "trailingIcon": {
      "type": "Icon",
      "props": {
        "name": "CalendarToday",
        "contentDescription": "选择日期"
      },
      "actions": {
        "onClick": {
          "type": "datePicker",
          "params": {
            "binding": "state.keyword"
          }
        }
      }
    }
  }
}
```

## 列表示例

```json
{
  "type": "LazyColumn",
  "props": {
    "modifier": [
      { "fillMaxSize": true }
    ]
  },
  "listConfig": {
    "dataSource": "${state.items}",
    "itemKey": "id",
    "itemTemplate": {
      "type": "Text",
      "props": {
        "text": "${item.title}"
      }
    }
  }
}
```

## 渲染入口

```kotlin
DynamicUi(
    json = jsonString,
    modifier = Modifier.fillMaxSize()
)
```

## 当前实现说明

- 解析入口只支持 JSON。
- parser 严格要求组件参数位于 `props`。
- `props.modifier` 会在解析期编译为运行时 modifier 链。
- `props` 内的嵌套节点会被识别为子节点参数。
- 运行时已优先支持 `Text`、`TextField`、`OutlinedTextField`、`Scaffold`、`TopAppBar` 等核心组件的新协议参数读取。
