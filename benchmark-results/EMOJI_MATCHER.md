# Emoji matcher 正确性与性能报告

测试日期：2026-08-31
基线：P0/P1 优化后的实现，尚未补齐 standalone Emoji component 范围，复杂正则读取 `MatchResult.value`
优化版：当前工作区

## 结论

- 三张码点范围表应保留。优化版中，CJK miss 的快路径为 `3.290 ns/op`，直接正则为 `1,647.294 ns/op`，相差 **500.6×**；简单 Emoji 的快路径为 `12.681 ns/op`，直接正则为 `2,299.803 ns/op`，相差 **181.4×**。正则还会分别分配 `320 B/op` 和 `368 B/op`。
- 精确正则路径不再读取 `MatchResult.value`。复杂 ZWJ 序列的生产组合路径从 `943.451` 降至 `934.526 ns/op`，延迟基本中性；分配从 `464.006` 降至 `368.006 B/op`，降低 **20.69%**。旗帜序列分配降低 **16.36%**。
- 补齐官方正则接受、但原手工表遗漏的 9 个 standalone Emoji component：5 个肤色和 4 个发型。该场景从“两次范围查找后错误返回 0”变为“一次查找正确命中”，耗时从 `21.021` 降至 `12.417 ns/op`。由于基线结果错误，这一项不是同语义性能对比。
- iPhone 17 Pro / iOS 27.0 Simulator 的 64 KiB 复杂 Emoji 整体解析为 `51.548 → 51.788 ops/s`（`+0.47%`），中位延迟为 `19.466 → 19.267 ms/op`（`-1.03%`），属于中性波动；两版 AST hash 一致。

## Benchmark 覆盖

新增 `EmojiMatcherBenchmark`，把一次 invocation 中的 4,096 次匹配按 `@OperationsPerInvocation` 归一到单次调用，覆盖：

- ASCII、CJK、标点 miss；
- 简单 Emoji、variation selector、standalone component；
- ZWJ 与旗帜进入精确 fallback 的路径；
- 快路径、精确正则及生产组合路径的独立成本。

JVM 环境：Apple M1 Ultra、Amazon Corretto 21.0.6、JMH 1.37、单线程、单 fork、`-Xms512m -Xmx512m`、3 × 1 s 预热、5 × 1 s 测量。误差为 JMH 的 99.9% 置信区间。

## JVM 完整 A/B

`≈0` 表示 JMH 检测到的分配低于 `0.001 B/op`。

| 场景 | 基线 ns/op | 优化后 ns/op | 延迟变化 | 基线 B/op | 优化后 B/op | 分配变化 |
|---|---:|---:|---:|---:|---:|---:|
| combinedComplexEmoji | 943.451 ± 26.316 | 934.526 ± 35.387 | -0.95% | 464.006 | 368.006 | **-20.69%** |
| combinedFlagEmoji | 1,813.581 ± 126.382 | 1,771.937 ± 39.962 | -2.30% | 440.012 | 368.012 | **-16.36%** |
| fastAsciiMiss | 2.811 ± 0.032 | 2.817 ± 0.037 | +0.22% | ≈0 | ≈0 | — |
| fastCjkMiss | 3.129 ± 0.052 | 3.290 ± 0.027 | +5.16% | ≈0 | ≈0 | — |
| fastComplexFallback | 10.807 ± 0.843 | 10.949 ± 0.318 | +1.31% | ≈0 | ≈0 | — |
| fastComponentEmoji | 21.021 ± 0.426 | 12.417 ± 0.669 | -40.93% | ≈0 | ≈0 | — |
| fastPunctuationMiss | 17.082 ± 0.968 | 16.861 ± 1.365 | -1.29% | ≈0 | ≈0 | — |
| fastSimpleEmoji | 12.881 ± 0.696 | 12.681 ± 0.919 | -1.55% | ≈0 | ≈0 | — |
| fastVariationEmoji | 14.903 ± 0.414 | 14.424 ± 0.368 | -3.21% | ≈0 | ≈0 | — |
| regexCjkMiss | 1,720.519 ± 39.757 | 1,647.294 ± 69.118 | -4.26% | 320.012 | 320.011 | 0.00% |
| regexComplexEmoji | 1,017.248 ± 45.600 | 927.979 ± 18.232 | -8.78% | 464.007 | 368.006 | **-20.69%** |
| regexSimpleEmoji | 2,742.071 ± 613.751 | 2,299.803 ± 25.221 | -16.13% | 440.019 | 368.016 | **-16.36%** |

快路径中未涉及本次代码改动的数纳秒级波动，以及直接正则控制组的波动，不应解释为优化收益；两版是独立单 fork 运行。可归因的结果是 component 正确命中，以及移除 `MatchResult.value` 后精确匹配的分配下降。

## 正确性护栏

- 新增 JVM 等价性测试：遍历 `U+0000..U+1FAFF` 的所有非 surrogate 码点，分别测试无 selector、VS15、VS16，再覆盖 6 种 keycap，共 **383,238** 个输入形态；快表结果必须与精确正则完全相等。
- 新增 9 个 standalone component 的公共解析断言，覆盖无 selector、VS15、VS16。
- `jvmTest`：98/98 通过（含公开 ABI 兼容守卫）；`iosSimulatorArm64Test`：96/96 通过；`compileTestKotlinWasmJs`：通过。
- 官方差分：150 default + 14 nestLimit + 130 full probes + 16 simple probes + 5,000 fuzz + 1,915 Emoji，共 7,225 条，0 mismatch。

## 后续优化判断

当前最合适的结构仍是“无分配快表 + 少量精确 fallback”。把三张表删掉并统一走正则，会让最常见的 miss 和简单 Emoji 慢两个数量级。

本轮用穷举等价性测试解决数据漂移，没有把生成器接入常规构建。这样不引入 Node/npm 或循环生成依赖；更新 `@misskey-dev/emoji-data` 时，测试会直接指出首个不一致码点。若 Emoji 数据升级成为高频维护动作，再增加一个只在升级时运行的源码生成器更划算。

下一项有潜力的独立优化是：在构建时从锁定版本的官方数据生成压缩 trie/DFA，只替换复杂序列的正则 fallback，并继续保留当前三张快表。当前复杂路径约 99% 时间仍在正则中，因此有明显上限空间；但必须同时比较 JVM/iOS 延迟、冷启动、生成源码及二进制体积，不能仅凭微基准直接替换。

## 复现

```shell
./gradlew jvmBenchmarkEmojiMatcherBenchmark
```

带 JVM 分配统计的本次运行使用：

```shell
java -Xms512m -Xmx512m \
  -jar build/benchmarks/jvmBenchmark/jars/mfm-multiplatform-jvmBenchmark-jmh-0.2.8-SNAPSHOT-JMH.jar \
  'EmojiMatcherBenchmark.*' -wi 3 -i 5 -w 1s -r 1s -f 1 \
  -bm avgt -tu ns -prof gc -rf json -rff <result.json>
```

未取整数据见 [emoji-matcher.json](emoji-matcher.json)。
