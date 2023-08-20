package moe.tlaster.mfm.parser.tokenizer

import kotlin.test.Test
import kotlin.test.assertContentEquals

class TokenizerTest {
    @Test
    fun testBasicText() {
        val tokenizer = MFMTokenizer()
        val content = "test"
        val result = tokenizer.parse(StringReader(content))
        assertContentEquals(
            listOf(
                TextToken("test")
            ),
            result
        )
    }

    @Test
    fun testEmoji() {
        val tokenizer = MFMTokenizer()
        val content = ":test:"
        val result = tokenizer.parse(StringReader(content))
        assertContentEquals(
            listOf(
                EmojiNameToken("test")
            ),
            result
        )
    }

    @Test
    fun testColonNonEmoji() {
        val tokenizer = MFMTokenizer()
        val content = ":test"
        val result = tokenizer.parse(StringReader(content))
        assertContentEquals(
            listOf(
                TextToken(":test")
            ),
            result
        )
    }

    @Test
    fun testHashTag() {
        val tokenizer = MFMTokenizer()
        val content = "#test"
        val result = tokenizer.parse(StringReader(content))
        assertContentEquals(
            listOf(
                HashTagToken("test")
            ),
            result
        )
    }

    @Test
    fun testUserName() {
        val tokenizer = MFMTokenizer()
        val content = "@test"
        val result = tokenizer.parse(StringReader(content))
        assertContentEquals(
            listOf(
                UserNameToken("test", null)
            ),
            result
        )
    }

    @Test
    fun testUserNameWithHost() {
        val tokenizer = MFMTokenizer()
        val content = "@test@host"
        val result = tokenizer.parse(StringReader(content))
        assertContentEquals(
            listOf(
                UserNameToken("test", "host")
            ),
            result
        )
    }

    @Test
    fun testCashTag() {
        val tokenizer = MFMTokenizer()
        val content = "\$test"
        val result = tokenizer.parse(StringReader(content))
        assertContentEquals(
            listOf(
                CashToken("test")
            ),
            result
        )
    }

    @Test
    fun testInlineCode() {
        val tokenizer = MFMTokenizer()
        val content = "`test`"
        val result = tokenizer.parse(StringReader(content))
        assertContentEquals(
            listOf(
                InlineCodeToken("test")
            ),
            result
        )
    }

    @Test
    fun testCodeBlock() {
        val tokenizer = MFMTokenizer()
        val content = "```test```"
        val result = tokenizer.parse(StringReader(content))
        assertContentEquals(
            listOf(
                CodeBlockToken("test", null)
            ),
            result
        )
    }

    @Test
    fun testCodeBlockWithLanguage() {
        val tokenizer = MFMTokenizer()
        val content = "```lan\ntest```"
        val result = tokenizer.parse(StringReader(content))
        assertContentEquals(
            listOf(
                CodeBlockToken("test", "lan")
            ),
            result
        )
    }

    @Test
    fun testAsteriskBold() {
        val tokenizer = MFMTokenizer()
        val content = "**test**"
        val result = tokenizer.parse(StringReader(content))
        assertContentEquals(
            listOf(
                BoldToken("test")
            ),
            result
        )
    }

    @Test
    fun testAsteriskItalic() {
        val tokenizer = MFMTokenizer()
        val content = "*test*"
        val result = tokenizer.parse(StringReader(content))
        assertContentEquals(
            listOf(
                ItalicToken("test")
            ),
            result
        )
    }

    @Test
    fun testUnderscoreBold() {
        val tokenizer = MFMTokenizer()
        val content = "__test__"
        val result = tokenizer.parse(StringReader(content))
        assertContentEquals(
            listOf(
                BoldToken("test")
            ),
            result
        )
    }

    @Test
    fun testUnderscoreItalic() {
        val tokenizer = MFMTokenizer()
        val content = "_test_"
        val result = tokenizer.parse(StringReader(content))
        assertContentEquals(
            listOf(
                ItalicToken("test")
            ),
            result
        )
    }

    @Test
    fun testInelineMath() {
        val tokenizer = MFMTokenizer()
        val content = "\\(test\\)"
        val result = tokenizer.parse(StringReader(content))
        assertContentEquals(
            listOf(
                InlineMathToken("test")
            ),
            result
        )
    }

    @Test
    fun testMathBlock() {
        val tokenizer = MFMTokenizer()
        val content = "\\[test\\]"
        val result = tokenizer.parse(StringReader(content))
        assertContentEquals(
            listOf(
                MathBlockToken("test")
            ),
            result
        )
    }

    @Test
    fun testQuote() {
        val tokenizer = MFMTokenizer()
        val content = ">test"
        val result = tokenizer.parse(StringReader(content))
        assertContentEquals(
            listOf(
                BlockquoteToken,
                TextToken("test")
            ),
            result
        )
    }

    @Test
    fun testTag() {
        val tokenizer = MFMTokenizer()
        val content = "<test>"
        val result = tokenizer.parse(StringReader(content))
        assertContentEquals(
            listOf(
                TagToken("test")
            ),
            result
        )
    }

    @Test
    fun testTagWithEndTag() {
        val tokenizer = MFMTokenizer()
        val content = "<test></test>"
        val result = tokenizer.parse(StringReader(content))
        assertContentEquals(
            listOf(
                TagToken("test"),
                EndTagToken("test")
            ),
            result
        )
    }

    @Test
    fun testLink() {
        val tokenizer = MFMTokenizer()
        val content = "[test](https://example.com)"
        val result = tokenizer.parse(StringReader(content))
        assertContentEquals(
            listOf(
                BracketToken("test", false),
                RoundBracketToken("https://example.com")
            ),
            result
        )
    }

    @Test
    fun testSilentLink() {
        val tokenizer = MFMTokenizer()
        val content = "?[test](https://example.com)"
        val result = tokenizer.parse(StringReader(content))
        assertContentEquals(
            listOf(
                BracketToken("test", true),
                RoundBracketToken("https://example.com")
            ),
            result
        )
    }

    @Test
    fun testBracketLink() {
        val tokenizer = MFMTokenizer()
        val content = "asd?[Search]"
        val result = tokenizer.parse(StringReader(content))
        assertContentEquals(
            listOf(
                TextToken("asd"),
                BracketToken("Search", true)
            ),
            result
        )
    }

    @Test
    fun testUrl() {
        val tokenizer = MFMTokenizer()
        val content = "https://example.com"
        val result = tokenizer.parse(StringReader(content))
        assertContentEquals(
            listOf(
                UrlToken("https://example.com")
            ),
            result
        )
    }

    @Test
    fun testFn() {
        val tokenizer = MFMTokenizer()
        val content = "\$[flip.h,v MisskeyでFediverseの世界が広がります]"
        val result = tokenizer.parse(StringReader(content))
        assertContentEquals(
            listOf(
                FnToken("flip.h,v"),
                TextToken("MisskeyでFediverseの世界が広がります"),
                FnEndToken
            ),
            result
        )
    }

    @Test
    fun testTildeStrikethrough() {
        val tokenizer = MFMTokenizer()
        val content = "~~test~~"
        val result = tokenizer.parse(StringReader(content))
        assertContentEquals(
            listOf(
                StrikeToken,
                TextToken("test"),
                StrikeToken
            ),
            result
        )
    }
}
