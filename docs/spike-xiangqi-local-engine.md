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

权重是**数据不是代码**,所以有两种分发方式,都不需要 Play Feature Delivery:

- **外挂(推荐)**:`.so` 随包(约 1.6MB,APK 压缩后更小),权重按需下载到 `filesDir`。
  可直接复用现有的 `LocalModelManagementScreen` + `LocalModelDownloader`(它已经在做
  「从 `images.oneboxable.com` 下载模型文件到本地」这件事),也可以托管到自己的对象存储
  —— 权重许可允许再分发。
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
