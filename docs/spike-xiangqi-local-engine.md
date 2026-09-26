# Spike:端侧象棋引擎(Fairy-Stockfish + 象棋 NNUE)可行性

> 分支 `spike/fairy-stockfish-android`。目的:验证「离线也有强棋力」这条路能不能走通、
> 代价多大、许可是否干净。**只做构建与实测,不含 JNI 接入,不含任何产品代码改动。**
> 复现脚本:[`scripts/build_fairy_stockfish_android.sh`](../scripts/build_fairy_stockfish_android.sh)

## 结论速览

| 项目 | 结果 |
|---|---|
| 能否用 NDK 编出 Android arm64 产物 | **能**,已在本机 NDK 28.1.13356709 上编出可执行文件与共享库,并在真机跑通 |
| 可执行文件(arm64, strip 后) | **1.52 MB**(静态 libc++,自包含,可直接推 `/data/local/tmp` 跑) |
| 共享库(.so,arm64, strip 后) | **0.92 MB**(动态 libc++,App 已自带 `libc++_shared.so`);静态链接则 1.77 MB |
| 象棋 NNUE 权重 | **10.74 MB**(`xiangqi-83f16c17fe26.nnue`,sha256 已固定校验) |
| 合计体积 | **≈ 11.7 MB**(其中 10.74 MB 是可按需下载的数据,进包的只有 <1 MB 的引擎) |
| 真机验证 | 物理机 arm64 / Android 16 上 `NNUE evaluation ... enabled`,709ms 搜到 **depth 15** |
| 权重许可 | **无「禁商用」条款**(与皮卡鱼权重形成关键差别,详见下文) |
| 主要工作量 | JNI 桥接 + 权重分发,外加 GPL-3.0 合规处理 |

对比当初的预估(「45MB 权重 + 棋力比皮卡鱼低一档」):**权重只有 10.74MB,而且有象棋专用 NNUE**,
之前按 45.51MB 估是因为那 `nn-3475407dc199.nnue` 是国际象棋用的通用网络。

### 覆盖面:这套数字只适用于中国象棋

Fairy-Stockfish 是**变体引擎**,一个 `.so` 同时覆盖国际象棋与中国象棋(引擎那 0.92MB 是两者共用的),
但**NNUE 权重按棋种分开**,而且它并不支持五子棋:

| 棋种 | 引擎 | 权重 | 本 spike 的验证状态 |
|---|---|---|---|
| 中国象棋 | 同一个 `.so` | `xiangqi-*.nnue` **10.74MB**(唯一可选) | ✅ 构建 + 真机 + 对局全部实测 |
| 国际象棋 | 同一个 `.so` | 默认 `nn-*.nnue` **45.51MB**;仓库另有 3.3MB / 20MB 的小网络,但能否被当前引擎版本加载取决于架构版本匹配,**未测** | ⚠️ 只确认过引擎能加载该网络,未做棋力对局 |
| 五子棋 | **不支持**(源码中 `gomoku`/`renju` 零命中) | — | ❌ 未验证;要本地化需另换引擎(服务端现用 Rapfi,GPL-3.0),其体积与权重许可均未核实 |

另外要分清本 spike 证明的边界:**证明了「编得出、跑得动、体积可控、许可干净」,没有证明「值不值得」**。
棋力只与自己的兜底对局过(见第 5 节),尚未与线上 Pikafish 做同等 movetime 的对照。

## 1. 构建:三步踩坑记录

官方 Makefile 自带 Android 交叉编译(`COMP=ndk` / `OS=Android`),但直接用会连撞三个坑,
完整配方见脚本,这里只记结论:

1. **必须 `largeboards=yes`。**
   `xiangqi` 变体在 `variant.cpp` 里被 `#ifdef LARGEBOARDS` 包着(`variants.init()` 内),
   默认 `largeboards=no` 会把它整段编译掉 —— 表现是引擎能跑、但 `UCI_Variant` 的可选值里
   根本没有 `xiangqi`,发 `setoption name UCI_Variant value xiangqi` 被静默忽略,
   引擎照下国际象棋。代价只有 **+0.13 MB**(1.40 → 1.53 MB)。
   附带好处:象棋是**编译期内置变体**,不需要分发 `variants.ini`(那个文件是给自定义变体的)。

2. **必须显式 `KERNEL=Linux OS=Android`。**
   在 macOS 上 `KERNEL` 被探测为 `Darwin`,Makefile 会追加 `-arch armv8
   -mmacosx-version-min=10.14 -mdynamic-no-pic` → 交叉编译直接失败;
   而 `OS` 不为 `Android` 时又会追加 `-lpthread` → bionic 没有独立 `libpthread`,链接失败。

3. **共享库不能走 Makefile 的默认链接。**
   `OS=Android` 会追加 `-fPIE`(CXXFLAGS)与 `-fPIE -pie`(LDFLAGS)。在 `-flto` 下这些标志
   被 LTO 后端采用,链接共享库时报
   `relocation R_AARCH64_ADR_PREL_PG_HI21 cannot be used against symbol ... recompile with -fPIC`。
   绕法:命令行覆盖 `CXXFLAGS`/`LDFLAGS`(Make 的命令行变量会屏蔽 Makefile 的 `+=`),
   自己给全部标志并把 `-fPIE` 换成 `-fPIC`。脚本里就是这么做的。

## 2. 体积账

| 组成 | 体积 | 说明 |
|---|---|---|
| 引擎(arm64 .so, strip 后) | **0.92 MB** | 动态 libc++;App 本来就在打包 `libc++_shared.so`,所以这 0.92MB 就是净增量。若静态链接 libc++ 则 1.77MB |
| 象棋 NNUE 权重 | 10.74 MB | 数据文件,可运行时下载,不进 APK |
| `variants.ini` | 不需要 | 象棋是内置变体 |

### 权重已上传 R2(可直接使用)

```
https://images.oneboxable.com/models/xiangqi-83f16c17fe26.nnue
11,261,915 字节 (10.74 MB)
sha256 83f16c17fe266f8d0904cb7cd8997777ee6a618a82b5d7fd32d52f570c760a25
```

- bucket `onebox-images`,key `models/xiangqi-83f16c17fe26.nnue`,与现有
  `models/*.litertlm` 同一前缀、同一公开域名;
- 已验证:公开 URL 返回 200 且 `content-length` 与 sha256 均与本地一致;
- 文件名内嵌 sha256 前 12 位,便于将来发新权重时并存而不覆盖旧客户端;
- 上传方式:`wrangler r2 object put onebox-images/models/<key> --file=<path> --remote`
  (凭据取 `~/.onebox-secrets/cloudflare/r2.env` 的 `CF_API_TOKEN`)。
  注意第一次 `put` 曾出现「无报错但对象不存在」,重跑一次才成功 —— 上传后务必回读校验。

**渠道覆盖**:引擎是普通 arm64 `.so`,不依赖 GMS/Play 服务,也不涉及 Play 的动态代码政策
(我们是把代码打进包,不是下载代码),所以 6 个国内渠道 + google + foss 都能带。
权重走 R2 也是本仓库既有做法(音效与 LiteRT-LM 模型都放 `images.oneboxable.com`)。
唯一需要实测的是**国内网络拉 10.74MB 的 R2 速度**(既有先例都是小文件);
若不理想,可镜像到阿里云 OSS 并按 flavor 切换下载源 —— 建议一开始就把 URL 收在一个
resolver 里,别散落在调用点。

权重是**数据不是代码**,所以有两种分发方式,都不需要 Play Feature Delivery:

- **外挂(推荐)**:`.so` 随包(约 0.92MB),权重按需下载到 `filesDir`。
  可直接复用现有的 `LocalModelManagementScreen` + `LocalModelDownloader`(它已经在做
  「从 `images.oneboxable.com` 下载模型文件到本地」这件事),权重许可也允许自行再分发。
- **内嵌**:照官方 specialized release 的做法,把权重改名成 `evaluate.h` 里的
  `EvalFileDefaultName` 再 `nnue=yes` 构建,得到一个自带权重的单文件。
  好处是没有「权重缺失」状态;代价是每次引擎更新都要重新打包权重。

## 3. 许可(与皮卡鱼的关键差别)

| 件 | 许可 | 商用 |
|---|---|---|
| Fairy-Stockfish 引擎 | GPL-3.0 | 允许 |
| `xiangqi-83f16c17fe26.nnue` 等象棋权重 | 随 [Fairy-Stockfish-NNUE](https://github.com/fairy-stockfish/Fairy-Stockfish-NNUE)(GPL-3.0 仓库)分发 | **允许**(GPL-3.0 无禁商用条款) |
| `pikafish.nnue` | 官方 NNUE-License.txt:**未经许可不得用于商业用途** | **禁止** |

皮卡鱼官网自己写明:他们为 Fairy-Stockfish 中国象棋变体训练的权重采用 **CC0**,不受皮卡鱼权重协议约束。
也就是说这条路的权重在「禁商用」这一点上是干净的 —— 这正是当初评估皮卡鱼方案时卡住的地方。

仍需处理的是 **GPL-3.0 的传染性**:引擎与权重都以 GPL-3.0 分发,动态链接在 FSF 的解释下
构成结合作品,因此**分发时整个 App 需按 GPL-3.0 条款提供对应源码**。本项目本身开源,
义务可履行,但需要:
- 在「关于」页放引擎源码获取方式与 GPL-3.0 声明;
- 确认与仓库现有 Apache-2.0 许可头并存的表述(建议交给法务过一遍);
- 注意 `foss`(F-Droid)渠道对 GPL 是欢迎的,反而更简单。

## 4. 集成要点(留给下一步)

1. **UCI 纵线基准不同,必须换算。**
   Fairy-Stockfish 的象棋纵线是 **1-based(`a1`–`i10`,红方底线 = 1)**;
   本模块与标准 UCCI / 现有服务端 Pikafish 接口是 **0-based(`a0`–`i9`,红方底线 = 0)**。
   关系:`引擎纵线 = 我们的 UCCI 纵线 + 1`;横线(a–i)一致,无镜像。
   踩过一次:`position fen` 传对了、引擎也回了合法着法,但拿返回值直接比 `notationUcci`
   一条都对不上,表现为「AI 每步都兜底」。
2. **`position fen` 要用六段式。** 本模块 `FenCodec.encode` 输出只有四段
   (`board side halfmove fullmove`),给引擎时要补成 `board side - - halfmove fullmove`。
   另外本模块 FEN 的行序是 **rank 0(黑方底线)在前**,与内部 `BoardPoint` 一致。
3. **NNUE 只在权重文件名以 `xiangqi` 开头时启用。**
   见 `src/evaluate.cpp` 的 `NNUE::init()`:它拿 `EvalFile` 的 basename 与变体名 / `nnueAlias`
   做前缀匹配。象棋的 `nnueAlias` 为空,所以通用 `nn-*.nnue` 只会让国际象棋用上 NNUE,
   象棋会静默退回经典评估(输出 `info string classical evaluation enabled`)。多个网络可用
   Unix 下的 `:` 串起来一次传入。
4. **JNI 形态**建议把引擎源码并入 App 的 CMake 目标(而不是链接上面这个 .so),
   这样由 AGP 统一给 `-fPIC`/libc++ 标志,可绕开本文第 1.3 条的 Makefile 链接问题。
   `.so` 导出了 2400+ 个 C++ 符号但没有现成的 C API(`ffishjs.cpp` 的 `ffish_*` 默认不参与构建),
   所以 JNI 侧需要自己包一层(或直接调 `UCI::loop`)。

## 5. 棋力实测

用同一个 harness 打了两组对局(host 版引擎走 UCI 子进程,双方轮流执红,由本模块
`GameArbiter` 判定合法性与胜负):

| 对局 | 结果 | 耗时 |
|---|---|---|
| 新兜底(自研搜索 250ms) vs 旧兜底(贪心) | **6 胜 0 负 0 和** | 10s |
| Fairy-Stockfish(象棋 NNUE,150ms) vs 新兜底(250ms) | **4 胜 0 负 0 和** | 32s |

两点读法:

- 第一组直接验证了已合入 main 的兜底改进是实质有效的(新旧各执红黑 3 局,全胜)。
- 第二组里 Fairy-Stockfish **在让了 100ms 思考时间的情况下红黑通吃**,而且 4 局只用了 32 秒,
  说明是速胜而非磨局 —— 棋力差距是「一个量级」而不是「略强」。
  参考深度:同硬件下象棋 NNUE 在手机上 709ms 搜到 depth 15,而自研兜底 700ms 约 depth 4。

样本量很小(个位数局),只能支持「强一个量级」这个定性结论,不足以给出可信的 Elo 差;
要落地还应该与线上 Pikafish 打几十局同等 movetime 的对照。

## 6. 建议

1. **这条路可行,且比预估便宜**:≈11.7MB(其中 10.74MB 是可按需下载的权重数据,真正进 APK 的引擎 <1MB),权重无禁商用条款。
2. **先补棋力实测再谈落地**:本 spike 只证明了「编得出、跑得动、体积可控」。
   用 Fairy-Stockfish 与线上 Pikafish 打几十局(双方同等 movetime),确认端侧经典/NNUE 的
   实际差距是否值得这套复杂度。
3. **兜底改进已经够用一阵**:新搜索已是明确的提升(6:0),且零包体零许可成本。
   端侧强引擎属于「离线也要强棋力」的产品决策,不是修 bug。

## 7. App 内技术验证结果(已完成)

按「先只做技术验证、GPL 源码暂不入库」执行:引擎二进制放本地并加 `.gitignore`,
在 debug APK + arm64 模拟器(Android 16)上把 App 内全链路跑通。

### 验证到的(每一步都有实证)

| 环节 | 证据 |
|---|---|
| 引擎随包并由 AGP 按 ABI 打包 | APK 内含 `lib/arm64-v8a/libfairystockfish.so`,1,602,464 字节 |
| **App 沙箱内可执行**(Android 10+ 关注点) | native 库目录权限 `-rwxr-xr-x`,用 `run-as <pkg>` 直接执行成功并正常 `uciok`/`readyok` |
| App 内从 R2 下载权重 | 日志:进度 25/50/75/100%,`11,261,915` 字节,sha256 校验通过,落到 `files/local_engines/` |
| UCI 进程封装在 App 内启动 + 加载权重 | 日志:`LocalXiangqiEngine: 端侧象棋引擎就绪 net=xiangqi-83f16c17fe26.nnue` |
| 引擎出招并被采纳、坐标换算正确 | 引擎以 depth 15 / cp=312 出招,实际是**炮隔自家兵跳吃我刚过河的兵**——一步正确的战术着法,证明 1-based↔0-based 换算无误 |
| UI 正确标注来源 | 对局页底部显示「引擎不可用,已改用本地兜底着法 / 原因:`local-engine cp=312 depth=15 \| pikafish: http 400`」 |

验证时远端恰好不通(debug 版 API 指向 `127.0.0.1:18080` 本地代理),因此**天然覆盖了「远端不可用」
这条主路径**——这正是本地引擎要解决的场景。

### 实现形态:子进程 UCI,而不是 JNI 内嵌

引擎本身就是 UCI 程序,`Position`/`Search`/`Thread` 这些内部 API 并非为嵌入设计;
子进程还能把第三方 native 崩溃隔离在 App 之外(内嵌崩溃就是闪退)。
产物做成 `lib*.so` 形式的 PIE 可执行文件,由 AGP 按 ABI 打包,安装时解压到可执行的 native 库目录。

### 代码位置与「不入库」的边界

- 自有代码(未提交,见分支 `spike/fairy-stockfish-android` 的工作区):
  `feature/xiangqi/.../data/local/LocalXiangqiEngine.kt`(UCI 进程封装 + 坐标换算)、
  `.../data/local/XiangqiEngineWeights.kt`(R2 下载 + sha256 校验),
  `PikafishMoveChooser` 里插入回退顺序「本地引擎 → 自研浅层搜索」。
- **GPL 侧一律不入库**:`feature/xiangqi/src/main/jniLibs/` 已加进 `.gitignore`。
  副作用是这份代码在没有二进制的仓库里是**惰性的**:`isPackaged()` 为 false →
  不下载也不调用,自动退回浅层搜索,不会让别人的构建或对局出问题。

### 正式落地前还缺的

1. **vendor 源码 + 用 CMake/AGP 从源码构建**(F-Droid 要求可复现,不接受预编译 .so;
   同时也要在此处补 GPL-3.0 声明与源码获取方式)。
2. **下载入口与进度 UI**:现在靠「开局走棋时后台静默下载」,用户不知道在用流量下 10.74MB;
   应改为设置页显式入口 + 进度/失败提示。
3. **引擎选择项**:把「本地引擎」作为可选项暴露到象棋 AI 选择器(当前只是远端失败时的兜底)。
4. **进程回收**:空闲一段时间或内存压力时 `release()`,避免常驻一个后端进程。
5. **国内下载速度实测**:R2 拉 10.74MB 是否可接受;必要时按 flavor 切 OSS 镜像。

## 8. 下载入口 + RemoteConfig 下载地址(已完成)

### 下载地址走 RemoteConfig

新增字段 `RemoteConfig.xiangqiEngineWeightsUrl`(`core/model/.../remote/RemoteConfig.kt`),
并接进 `mergeWithNetwork` / `equals` / `hashCode`。消费点在
`XiangqiEngineWeights.downloadUrl()`:每次下载都实时读,取不到就回退内置常量
`DEFAULT_DOWNLOAD_URL`。

服务端**不需要改 Strapi schema**:`remote-config` 内容类型的 `config` 是 `json` 字段,
在对应渠道那条记录的 `config` 里加一个 key 即可,例如:

```json
{ "xiangqiEngineWeightsUrl": "https://<镜像域名>/models/xiangqi-83f16c17fe26.nnue" }
```

几点约定与注意:

- 字段名即 JSON key —— `RemoteConfig` 全类没有用 `@SerialName`,所以 key 必须是
  `xiangqiEngineWeightsUrl`(驼峰);
- 国内外是两套后端(`api.wanbaohe.com` / `api.oneboxable.com`),**同一个 key 可以各下发各的**,
  所以国内要换镜像不必写 flavor 分支代码;
- **只下发 URL,不下发校验值**:换源时文件内容不变,长度与 sha256 仍由客户端常量校验。
  换权重文件本身属于要发版的改动(文件名内嵌 sha256),不是远程配置要解决的场景;
- **生效时机**:客户端读的是本地缓存的 remote config,所以改完 CMS 要等客户端下一次同步
  (启动同步间隔 `appLaunchSyncIntervalSeconds`,当前 3 天)才生效。域名突然被墙这类急事,
  要么等同步,要么走应用内手动刷新入口;
- 下载那一刻才读地址 —— 不会出现「打开 App 时缓存了旧地址,之后改 CMS 也没用」的错觉,
  但已经下到一半的下载不会中途换源。

### 下载入口与进度 UI

位置:**中国象棋 → 设置 → 离线引擎**(`XiangqiSettingsScreen` 的 `LocalEngineRow`),三态:

| 状态 | 展示 |
|---|---|
| 未内置(该构建没打包引擎) | 「当前构建未内置该引擎。」—— 不给下载入口,免得下完权重发现引擎不在包里 |
| 未下载 | 「尚未下载。下载一次(10.7 MB)后即可离线对弈。」+ 下载按钮 |
| 下载中 | 「正在下载… N%」+ 已下载/总大小 + 进度条 + 取消按钮 |
| 已就绪 | 「已就绪(10.7 MB),对局不再依赖服务端引擎。」+ 删除按钮(带确认弹窗) |

两个实现取舍:

1. **下载跑在 `XiangqiEngineWeights` 单例自己的作用域**,不是页面的 `componentScope` ——
   10.74MB 不该因为用户退出设置页而中断(退出即取消等于白费流量)。页面只观察
   `InstallState`。
2. **移除了走棋路径里的静默下载**:原来 `PikafishMoveChooser` 会在开局走棋时后台偷下 10.74MB,
   现在下载只由用户在设置页显式发起。代价是「没下过就用不上离线引擎」,这是有意的
   (不偷跑流量);后续如需可在「远端失败且未装权重」时给一次提示。

### 本轮实测(模拟器 arm64 / Android 16)

- 未下载态 → 点下载 → 抓到了「正在下载… 0%」+ 进度条 + 取消;
- 日志:25% / 50% / 75% / 100%,`11,261,915` 字节,sha256 校验通过;
- 完成后显示「已就绪(10.7 MB)」+ 删除按钮;点删除 → 确认弹窗 → 回到「尚未下载」,
  且 `files/local_engines/` 目录确实被清空。

**未做实测的一项**:RemoteConfig 覆盖值这条路径没有做端到端实测 —— debug 版的 API 走
本地 HTTPS 代理(`127.0.0.1:18080`)且远端配置缓存为空,没有可注入的入口。
目前只有代码层面的保证(字段存在于模型、`mergeWithNetwork` 已接入、读取处编译通过,
key 名与序列化一致)。建议在 CMS 配一个值后用真机确认一次。

## 9. 落地形态:vendor 源码 + 构建期编译(已完成)

按「提交推送后可用」的要求,把它做成了可持续的形态 —— 关键取舍是
**提交源码、不提交二进制**:

| | 做法 | 为什么 |
|---|---|---|
| 引擎源码 | vendor 到 `feature/xiangqi/third_party/fairy-stockfish/`(GPL-3.0,commit 固定) | GPL 要求分发二进制时能提供对应源码;F-Droid 也要求从仓库源码可复现构建 |
| 引擎二进制 | 由 `feature/xiangqi/build.gradle.kts` 的 `buildFairyStockfish` 任务从源码编译到 `build/generated/fairyStockfish/<abi>/`(`.gitignore` 覆盖) | 不进仓库:预编译 `.so` 会同时破坏 GPL 合规与 F-Droid 校验 |
| NNUE 权重 | 仍在 R2(10.74MB),运行时下载到 `filesDir/local_engines/` | 数据不是代码;既不进仓库也不进 APK,首装体积只增加约 1.5MB |

构建任务要点(踩过的坑都在注释里):`largeboards=yes` 必须有(否则象棋变体被整个编译掉)、
`KERNEL=Linux OS=Android` 必须显式给(macOS 上会被当成 Darwin)、`-static-libstdc++`
(Android 不提供 `libc++_shared.so` 作公开库,而这是要 exec 的可执行文件)、
产物是 PIE 可执行文件但命名 `lib*.so` 以便 AGP 按 ABI 打包并解压到可执行目录。

几个工程细节:

- 编译在 `build/fairyStockfish-work/` 的副本里进行 —— 上游 Makefile 是就地编译的,
  直接在 vendor 目录里编会往仓库树里丢 `*.o`;vendor 目录另有一份 `.gitignore` 兜底;
- 任务声明了源码目录为输入、产物目录为输出,所以源码不变时不会重编(Gradle 缓存命中);
- jniLibs 目录通过 `androidComponents.onVariants { it.sources.jniLibs?.addStaticSourceDirectory(...) }`
  挂上 —— **不能用 `android.sourceSets.jniLibs.srcDir`**,在本项目的 AGP 9 + 约定插件组合下
  会抛 `ClassCastException(DefaultAndroidLibrarySourceSet_Decorated -> AndroidLibrarySourceSet)`;
- 该任务标记了 `notCompatibleWithConfigurationCache`(NDK 目录只能在执行期解析,
  与 app 模块覆盖 `libc++_shared.so` 的任务同一处理方式),因此构建日志里那句
  「Configuration cache entry discarded with 1 problem」是既有现象,不是新增问题;
- NDK 版本改为 `libs.versions.toml` 的 `androidNdk` 单一来源,app 与本模块共用。

### 本轮实测

- `buildFairyStockfish` 从源码编译成功:**1564 KB**(strip 后);
- APK 内确认存在 `lib/arm64-v8a/libfairystockfish.so`(1,602,464 字节);
- 真机(模拟器 arm64 / Android 16)在 **App 自己的沙箱内**执行该产物:
  `run-as <pkg>` → 正常启动、`id name Fairy-Stockfish 260926 LB`、
  `info string NNUE evaluation using .../xiangqi-83f16c17fe26.nnue enabled`、返回 bestmove。

### 已知影响与后续

- **CI 构建时间**:引擎只在源码/参数变化时重编(有输入输出声明 + Gradle 缓存),
  首次构建会多约 1~1.5 分钟;若 CI 上成为瓶颈,可考虑接入 remote build cache;
- **新增 ABI 时**要同步改 `feature/xiangqi/build.gradle.kts` 的 `engineAbi` 与工具链三元组;
- 权重仍在 R2、下载入口在设置页(第 8 节),这两条不受本次改动影响。
