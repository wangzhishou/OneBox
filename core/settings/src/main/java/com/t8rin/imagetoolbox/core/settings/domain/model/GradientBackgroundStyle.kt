package com.t8rin.imagetoolbox.core.settings.domain.model

/**
 * 预置渐变背景样式。
 *
 * 每种样式按名字决定色相范围、饱和度和网格密度
 * （顶点数不固定，双三次插值 + 贝塞尔控制点）。雾面收在邻近色里、饱和度低；
 * 流光溢彩才铺满色环。经典直接用当前 ColorScheme 的色调角色，不另调色相。
 * 玻璃本身保持薄霜，颜色从卡片里透出来。
 * 使用 [ordinal2] 持久化到 DataStore，通过 [fromOrdinal] 反序列化。
 *
 * | 名称        | 描述                                                   |
 * |-------------|--------------------------------------------------------|
 * | Classic     | 经典——当前 ColorScheme 的主色、辅色、三级色和容器色调点 |
 * | Aurora      | 极光——青绿光幕 + 紫罗兰，竖向长网格，中高饱和           |
 * | Ocean       | 深海——只有蓝和青绿，饱和度偏低                          |
 * | Sunset      | 日落——玫瑰 + 琥珀 + 金色，比焦糖余晖更淡                |
 * | SakuraMist  | 樱花雾——樱粉 + 蜜桃 + 淡紫，低饱和、少顶点              |
 * | MintBreeze  | 薄荷苏打——薄荷绿 + 汽水蓝 + 青柠 + 柠檬                 |
 * | StarryNight | 午夜星河——浅色银靛薄暮，深色海军蓝，饱和度低            |
 * | Lavender    | 薰衣草田——紫罗兰到丁香，横向铺开，低饱和                |
 * | WarmGlow    | 焦糖余晖——蜜桃 + 琥珀 + 焦糖，只有暖色                  |
 * | Ethereal    | 丝雾——主题色上的薄雾，饱和度最低、网格最疏              |
 * | NeonCyber   | 电子梦境——电青 + 品红 + 紫罗兰 + 荧光绿，饱和度最高     |
 * | PrismFlow   | 流光溢彩——色环铺满，网格最密                            |
 */
enum class GradientBackgroundStyle(val ordinal2: Int) {
    Classic(0),
    Aurora(1),
    Ocean(2),
    Sunset(3),
    SakuraMist(4),
    MintBreeze(5),
    StarryNight(6),
    Lavender(7),
    WarmGlow(8),
    Ethereal(9),
    NeonCyber(10),
    PrismFlow(11);

    companion object {
        val entries2: List<GradientBackgroundStyle> = values().toList()

        fun fromOrdinal(ordinal: Int?): GradientBackgroundStyle =
            values().firstOrNull { it.ordinal2 == ordinal } ?: Classic
    }
}
