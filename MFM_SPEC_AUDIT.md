# MFM 官方行为对齐报告

审计与修复日期：2026-08-31
官方参考：`mfm-js` 0.26.0，`develop@61c9dd10d29a054489a346629b0bb420659aa626`
Emoji 数据：`@misskey-dev/emoji-data` 17.0.0

## 结论

当前 parser 已与上述固定版本的官方实现对齐。AST 规范化后，官方测试向量、补充边界用例、确定性随机输入以及官方完整 Emoji 列表均为 **0 差异**。

| 差分集 | 数量 | 修复前不一致 | 修复后不一致 |
|---|---:|---:|---:|
| 官方默认 parser 向量 | 150 | 13 | 0 |
| 官方显式 `nestLimit` 向量 | 14 | API 无法表达 | 0 |
| 补充 FullParser 边界向量 | 130 | 84 | 0 |
| 补充 SimpleParser 边界向量 | 16 | 15 | 0 |
| 固定种子随机 fuzz | 5,000 | — | 0 |
| 官方 Emoji 17.0 列表 | 1,915 | 未覆盖 | 0 |

随机输入使用固定种子 `0x4d464d`，长度为 1–40，字符集覆盖主要 MFM 标记、空白、CR/LF 与 URL 字符。差分以官方 `parse` / `parseSimple` 的输出为判据，不以本库原有测试期望替代官方行为。

## 已落地的对齐

- Unicode Emoji 改为与官方 Emoji 17.0 数据等价的最长匹配；支持 ZWJ 家庭、肤色、旗帜、tag sequence、keycap 与 variation selector，不再把任意 surrogate pair 当作 Emoji，也不会合并连续 Emoji 节点。
- Full/Simple parser 都正确处理 `<plain>`；空标签和未闭合标签回退为 text，plain 内不再解析 Emoji。
- URL 和 link 保留原始 percent escape，接受 `localhost`，对齐 angle URL 的 `brackets`，并修正普通 URL 的终止字符和失败 link 的逐字符回退。
- quote、search、code block、math block、center 对齐块前后换行消费规则，并统一支持 CRLF、CR、LF。
- function 对齐名称、参数、flag、正文分隔符和空正文规则；兼容属性 `args` 继续用字符串 `"true"` 表示 flag，新增的 `typedArgs` 则按官方模型暴露布尔 `true`；`$tag` 按官方规则保留为 text。
- bold、italic、strike、inline code 对齐空内容、换行、后缀和非法输入回退规则。
- hashtag 支持官方的成对与嵌套 `()`、`[]`、`「」`、`（）`。
- mention、emoji code、search content 等边界规则与公开属性对齐。
- 增加官方默认 `nestLimit = 20`，调用方可通过 `MFMParser(nestLimit = ...)` 配置。

## API/ABI 兼容性

这次对齐保留 `0.2.7` 已发布的构造器、data class 参数和属性类型，并以增量接口表达官方行为：

- `MFMParser` 新增可选参数 `nestLimit: Int = 20`。
- `FnNode.args` 保持 `HashMap<String, String>`；新增 `typedArgs: Map<String, Any>`，用于区分 `left=true` 与 flag `left`。
- `TextNode.plain`、`UrlNode.brackets` 改为 public。
- `SearchNode` 新增官方完整 `content`；原双参数构造器、data class 形状和 `search` 字段保留。
- `MentionNode` 新增可公开读取的派生属性 `acct`。
- `CashNode` 类型暂时保留以避免直接删除公开类型，但 parser 不再生成它；`$tag` 现在是普通 text。

仓库同时提交 JVM 与 KLib ABI dump，并在 CI 的 `checkLegacyAbi` 中阻止后续无意破坏公开签名。

本库仍保留 `RootNode`、节点 `start` 和 `SearchNode.search` 这些扩展。官方独立的 `plain` 节点继续映射为 `TextNode(plain = true)`；这些形状差异均可无损规范化，不再造成解析语义差异。

## 性能回归结果

官方对齐后重新运行 JVM stress benchmark（Corretto 21、单线程、3 × 1 s 预热、5 × 1 s 测量）。下表与旧 `Tokenizer -> TreeBuilder -> normalizeCompat` 基线使用相同参数：

| 场景 | 旧 parser ops/s | 当前 ops/s | 加速 |
|---|---:|---:|---:|
| 批量 1,000 posts | 8.992 | 85.304 | 9.486× |
| 大纯文本 1 MiB | 44.221 | 1,801.877 | 40.747× |
| 大富文本 1 MiB | 11.990 | 115.674 | 9.648× |
| 函数密集 64 KiB | 164.948 | 2,291.737 | 13.894× |
| 750 层函数（显式不设限） | 1,598.149 | 26,145.530 | 16.360× |
| 无匹配 `]` 4 KiB | 2,099.543 | 156,196.629 | 74.396× |
| 无匹配 `]` 32 KiB | 27.807 | 19,658.426 | 706.956× |
| 无匹配 `]` 128 KiB | 2.002 | 4,910.525 | 2,453.354× |

额外 smoke 结果：短纯文本 280 chars 为 `5,261.119 ops/ms`，短富文本 2 KiB 为 `74.485 ops/ms`。普通 ASCII 字母连续段采用批量推进；Emoji 正则和编码输入保持懒初始化，纯文本不会承担完整 Emoji matcher 的初始化成本。

## 验证记录

后续 P0/P1 与 Emoji matcher 优化未改变上述行为，完整数据见 [P0/P1 性能优化报告](benchmark-results/P0_P1_OPTIMIZATION.md)和 [Emoji matcher 正确性与性能报告](benchmark-results/EMOJI_MATCHER.md)。

- `./gradlew jvmTest --no-daemon`：98/98 通过，其中 Emoji 快表与精确正则穷举比较 383,238 个输入形态，并包含旧 JVM 签名的反射守卫。
- `./gradlew iosSimulatorArm64Test --no-daemon`：96/96 通过。
- `./gradlew compileTestKotlinWasmJs --no-daemon`：通过。
- 官方差分：150 + 14 + 130 + 16 + 5,000 + 1,915 条，全部 0 mismatch。
- `./gradlew jvmBenchmarkSmokeBenchmark --rerun-tasks --no-daemon`：通过。
- `./gradlew jvmBenchmarkStressBenchmark --rerun-tasks --no-daemon`：通过。

## 官方来源

- [Misskey Hub：MFM](https://misskey-hub.net/en/docs/for-users/features/mfm/)
- [mfm.js 语法文档（固定提交）](https://github.com/misskey-dev/mfm.js/blob/61c9dd10d29a054489a346629b0bb420659aa626/docs/syntax.md)
- [官方 parser 实现（固定提交）](https://github.com/misskey-dev/mfm.js/blob/61c9dd10d29a054489a346629b0bb420659aa626/src/internal/parser.ts)
- [官方 parser 测试（固定提交）](https://github.com/misskey-dev/mfm.js/blob/61c9dd10d29a054489a346629b0bb420659aa626/test/parser.ts)
- [官方节点模型（固定提交）](https://github.com/misskey-dev/mfm.js/blob/61c9dd10d29a054489a346629b0bb420659aa626/src/node.ts)
