package moe.tlaster.mfm.parser

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State
import org.openjdk.jmh.annotations.OperationsPerInvocation

private const val MATCHER_CALLS = 4_096
private const val MATCHER_OPERATIONS = 4_096
private const val SIMPLE_EMOJI = "😇"
private const val COMPONENT_EMOJI = "🏽"
private const val VARIATION_EMOJI = "☀️"
private const val COMPLEX_EMOJI_SEQUENCE = "👨‍👩‍👧‍👦"
private const val FLAG_EMOJI = "🇯🇵"

@State(Scope.Benchmark)
open class EmojiMatcherBenchmark {
    private val ascii = "a".repeat(MATCHER_CALLS)
    private val cjk = "日".repeat(MATCHER_CALLS)
    private val punctuation = "。".repeat(MATCHER_CALLS)
    private val simpleEmoji = SIMPLE_EMOJI.repeat(MATCHER_CALLS)
    private val componentEmoji = COMPONENT_EMOJI.repeat(MATCHER_CALLS)
    private val variationEmoji = VARIATION_EMOJI.repeat(MATCHER_CALLS)
    private val complexEmoji = COMPLEX_EMOJI_SEQUENCE.repeat(MATCHER_CALLS)
    private val flagEmoji = FLAG_EMOJI.repeat(MATCHER_CALLS)

    private val encodedCjk = encodeUnicodeEmojiInput(cjk)
    private val encodedSimpleEmoji = encodeUnicodeEmojiInput(simpleEmoji)
    private val encodedComplexEmoji = encodeUnicodeEmojiInput(complexEmoji)
    private val encodedFlagEmoji = encodeUnicodeEmojiInput(flagEmoji)

    @Benchmark
    @OperationsPerInvocation(MATCHER_OPERATIONS)
    open fun fastAsciiMiss(): Int = fastBatch(ascii, 1)

    @Benchmark
    @OperationsPerInvocation(MATCHER_OPERATIONS)
    open fun fastCjkMiss(): Int = fastBatch(cjk, 1)

    @Benchmark
    @OperationsPerInvocation(MATCHER_OPERATIONS)
    open fun fastPunctuationMiss(): Int = fastBatch(punctuation, 1)

    @Benchmark
    @OperationsPerInvocation(MATCHER_OPERATIONS)
    open fun fastSimpleEmoji(): Int = fastBatch(simpleEmoji, SIMPLE_EMOJI.length)

    @Benchmark
    @OperationsPerInvocation(MATCHER_OPERATIONS)
    open fun fastComponentEmoji(): Int = fastBatch(componentEmoji, COMPONENT_EMOJI.length)

    @Benchmark
    @OperationsPerInvocation(MATCHER_OPERATIONS)
    open fun fastVariationEmoji(): Int = fastBatch(variationEmoji, VARIATION_EMOJI.length)

    @Benchmark
    @OperationsPerInvocation(MATCHER_OPERATIONS)
    open fun fastComplexFallback(): Int = fastBatch(complexEmoji, COMPLEX_EMOJI_SEQUENCE.length)

    @Benchmark
    @OperationsPerInvocation(MATCHER_OPERATIONS)
    open fun regexCjkMiss(): Int = regexBatch(encodedCjk, 1)

    @Benchmark
    @OperationsPerInvocation(MATCHER_OPERATIONS)
    open fun regexSimpleEmoji(): Int = regexBatch(encodedSimpleEmoji, SIMPLE_EMOJI.length)

    @Benchmark
    @OperationsPerInvocation(MATCHER_OPERATIONS)
    open fun regexComplexEmoji(): Int = regexBatch(encodedComplexEmoji, COMPLEX_EMOJI_SEQUENCE.length)

    @Benchmark
    @OperationsPerInvocation(MATCHER_OPERATIONS)
    open fun combinedComplexEmoji(): Int = combinedBatch(complexEmoji, encodedComplexEmoji, COMPLEX_EMOJI_SEQUENCE.length)

    @Benchmark
    @OperationsPerInvocation(MATCHER_OPERATIONS)
    open fun combinedFlagEmoji(): Int = combinedBatch(flagEmoji, encodedFlagEmoji, FLAG_EMOJI.length)

    private fun fastBatch(
        input: String,
        stride: Int,
    ): Int {
        var checksum = 0
        var index = 0
        repeat(MATCHER_CALLS) {
            checksum = checksum * 31 + unicodeEmojiFastLengthAt(input, index)
            index += stride
        }
        return checksum
    }

    private fun regexBatch(
        encodedInput: String,
        stride: Int,
    ): Int {
        var checksum = 0
        var index = 0
        repeat(MATCHER_CALLS) {
            checksum = checksum * 31 + unicodeEmojiComplexLengthAt(encodedInput, index)
            index += stride
        }
        return checksum
    }

    private fun combinedBatch(
        input: String,
        encodedInput: String,
        stride: Int,
    ): Int {
        var checksum = 0
        var index = 0
        repeat(MATCHER_CALLS) {
            val fastLength = unicodeEmojiFastLengthAt(input, index)
            val length = if (fastLength >= 0) fastLength else unicodeEmojiComplexLengthAt(encodedInput, index)
            checksum = checksum * 31 + length
            index += stride
        }
        return checksum
    }
}
