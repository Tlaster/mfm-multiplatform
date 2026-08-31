package moe.tlaster.mfm.parser

import kotlin.test.Test
import kotlin.test.fail

class EmojiMatcherConsistencyTest {
    @Test
    fun fastTablesMatchExactRegexForEverySupportedCodePoint() {
        for (codePoint in 0..0x1FAFF) {
            if (codePoint in 0xD800..0xDFFF) continue
            val scalar = Character.toChars(codePoint).concatToString()
            assertFastMatcherMatchesExactRegex(scalar)
            assertFastMatcherMatchesExactRegex("$scalar\uFE0E")
            assertFastMatcherMatchesExactRegex("$scalar\uFE0F")
        }

        for (keycap in listOf("#\u20E3", "#\uFE0F\u20E3", "*\u20E3", "*\uFE0F\u20E3", "1\u20E3", "1\uFE0F\u20E3")) {
            assertFastMatcherMatchesExactRegex(keycap)
        }
    }

    private fun assertFastMatcherMatchesExactRegex(input: String) {
        val exactLength = unicodeEmojiComplexLengthAt(encodeUnicodeEmojiInput(input), 0)
        val fastLength = unicodeEmojiFastLengthAt(input, 0)
        if (fastLength != exactLength) {
            fail("Fast matcher returned $fastLength but exact regex returned $exactLength for ${input.toCodePoints()}")
        }
    }

    private fun String.toCodePoints(): String =
        codePoints().toArray().joinToString(separator = " ") { "U+${it.toString(16).uppercase().padStart(4, '0')}" }
}
