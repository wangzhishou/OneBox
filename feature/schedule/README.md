# Schedule Module

独立事件域模块，用于承接未来的 Google Calendar / 系统日历同步能力。

## 当前职责

- 提供独立 `schedule_event` 事件模型
- 提供 `schedule_provider_binding` 映射表，用于维护本地事件与外部 provider event id 的关系
- 提供 `schedule_sync_state` 同步游标状态
- 通过 `linkedTaskId` 预留与 `MarkTodo` 的弱关联桥接
- 提供基础日程中心页面，验证导航、落库与 UI 管线
- 提供第一阶段 `CalendarContract` 导出能力（通过系统日历插入页完成保存）
- 提供第二阶段系统日历 Provider 直写能力（权限授权后可选择目标日历并直接插入事件）
- 当本地事件已绑定系统日历后，后续更新/删除会尝试回写远端系统日历事件

## 为什么不是直接扩 `:feature:calendar`

`feature/calendar` 当前是万年历 / 黄历 / 八字 / 择日能力，不是“事件日程域”。
如果把 Google Calendar 同步直接塞进去，会把传统日历内容、账号同步、事件增删改查混成一个模块。

## 为什么不是直接扩 `:feature:marktodo`

`feature/marktodo` 的核心语义是任务清单，不是带 provider/sync token/remoteEventId 的事件系统。
未来一旦引入 recurrence、timezone、calendar account、多端同步，这些字段会迅速污染任务模型。

## 下一步建议

1. 接入 Google 账号授权与 scope 管理
2. 为系统日历写入补充回写映射（provider event id ↔ local event）
3. 增加 provider adapter（Google / ICS）
4. 实现 delta sync 与冲突策略

