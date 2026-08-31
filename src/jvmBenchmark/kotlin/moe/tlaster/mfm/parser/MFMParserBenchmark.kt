package moe.tlaster.mfm.parser

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State
import moe.tlaster.mfm.parser.tree.RootNode

private const val KIB = 1024
private const val MIB = KIB * KIB

private const val RICH_POST =
    "Hello :blobcat: **bold** [link](https://example.com/path?q=1) #kotlin @user@example.com\n"
private const val FUNCTION_POST = "$[tada.speed=1 hello] $[shake text] "

private fun String.repeatedToSize(size: Int): String = repeat((size + length - 1) / length).take(size)

@State(Scope.Benchmark)
open class MFMParserBenchmark {
    private val parser = MFMParser()
    private val shortPlain = "a".repeat(280)
    private val shortRich = RICH_POST.repeatedToSize(2 * KIB)
    private val largePlain = "a".repeat(MIB)
    private val largeRich = RICH_POST.repeatedToSize(MIB)
    private val functionHeavy = FUNCTION_POST.repeatedToSize(64 * KIB)
    private val unmatchedClosings4KiB = "]".repeat(4 * KIB)
    private val unmatchedClosings32KiB = "]".repeat(32 * KIB)
    private val unmatchedClosings128KiB = "]".repeat(128 * KIB)
    private val deeplyNestedFunctions = "$[x ".repeat(750) + "a" + "]".repeat(750)
    private val batch =
        Array(1_000) { index ->
            when (index % 3) {
                0 -> "plain text ".repeatedToSize(280)
                1 -> RICH_POST.repeatedToSize(2 * KIB)
                else -> FUNCTION_POST.repeatedToSize(2 * KIB)
            }
        }

    @Benchmark
    open fun parseShortPlain(): RootNode = parser.parse(shortPlain)

    @Benchmark
    open fun parseShortRich(): RootNode = parser.parse(shortRich)

    @Benchmark
    open fun parseBatchOf1000Posts(): Int {
        var checksum = 0
        for (text in batch) {
            checksum = checksum * 31 + parser.parse(text).content.size
        }
        return checksum
    }

    @Benchmark
    open fun parseLargePlain1MiB(): RootNode = parser.parse(largePlain)

    @Benchmark
    open fun parseLargeRich1MiB(): RootNode = parser.parse(largeRich)

    @Benchmark
    open fun parseFunctionHeavy64KiB(): RootNode = parser.parse(functionHeavy)

    @Benchmark
    open fun parseUnmatchedClosings4KiB(): RootNode = parser.parse(unmatchedClosings4KiB)

    @Benchmark
    open fun parseUnmatchedClosings32KiB(): RootNode = parser.parse(unmatchedClosings32KiB)

    @Benchmark
    open fun parseUnmatchedClosings128KiB(): RootNode = parser.parse(unmatchedClosings128KiB)

    @Benchmark
    open fun parseDeepFunctions750Levels(): RootNode = parser.parse(deeplyNestedFunctions)
}
