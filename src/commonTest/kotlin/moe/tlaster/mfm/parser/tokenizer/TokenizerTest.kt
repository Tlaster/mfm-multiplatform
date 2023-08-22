package moe.tlaster.mfm.parser.tokenizer

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class TokenizerTest {
    @Test
    fun testEmoji() {
        val tokenizer = MFMTokenizer()
        val content = ":test:"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.EmojiNameStart,
                TokenCharacterType.EmojiName,
                TokenCharacterType.EmojiName,
                TokenCharacterType.EmojiName,
                TokenCharacterType.EmojiName,
                TokenCharacterType.EmojiNameStart,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testColonNonEmoji() {
        val tokenizer = MFMTokenizer()
        val content = ":test"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testHashTag() {
        val tokenizer = MFMTokenizer()
        val content = "#test"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.HashTagStart,
                TokenCharacterType.HashTag,
                TokenCharacterType.HashTag,
                TokenCharacterType.HashTag,
                TokenCharacterType.HashTag,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testHashNonTag() {
        val tokenizer = MFMTokenizer()
        val content = "#!test"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testHashtagBefore() {
        val tokenizer = MFMTokenizer()
        val content = "asd#test"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.HashTagStart,
                TokenCharacterType.HashTag,
                TokenCharacterType.HashTag,
                TokenCharacterType.HashTag,
                TokenCharacterType.HashTag,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testNonHashtagBefore() {
        val tokenizer = MFMTokenizer()
        val content = ":#test"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testAtUserName() {
        val tokenizer = MFMTokenizer()
        val content = "@test"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.UserAt,
                TokenCharacterType.UserName,
                TokenCharacterType.UserName,
                TokenCharacterType.UserName,
                TokenCharacterType.UserName,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testAtUserNameWithHost() {
        val tokenizer = MFMTokenizer()
        val content = "@test@host"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.UserAt,
                TokenCharacterType.UserName,
                TokenCharacterType.UserName,
                TokenCharacterType.UserName,
                TokenCharacterType.UserName,
                TokenCharacterType.UserAt,
                TokenCharacterType.UserHost,
                TokenCharacterType.UserHost,
                TokenCharacterType.UserHost,
                TokenCharacterType.UserHost,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testAtUserNameWithHostBefore() {
        val tokenizer = MFMTokenizer()
        val content = "asd@test@host"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.UserAt,
                TokenCharacterType.UserName,
                TokenCharacterType.UserName,
                TokenCharacterType.UserName,
                TokenCharacterType.UserName,
                TokenCharacterType.UserAt,
                TokenCharacterType.UserHost,
                TokenCharacterType.UserHost,
                TokenCharacterType.UserHost,
                TokenCharacterType.UserHost,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testAtNonUserNameWithHostBefore() {
        val tokenizer = MFMTokenizer()
        val content = "asd!@test@host"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.UserAt,
                TokenCharacterType.UserName,
                TokenCharacterType.UserName,
                TokenCharacterType.UserName,
                TokenCharacterType.UserName,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testCash() {
        val tokenizer = MFMTokenizer()
        val content = "\$test"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.CashStart,
                TokenCharacterType.Cash,
                TokenCharacterType.Cash,
                TokenCharacterType.Cash,
                TokenCharacterType.Cash,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testInlineCode() {
        val tokenizer = MFMTokenizer()
        val content = "`test`"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.InlineCodeStart,
                TokenCharacterType.InlineCode,
                TokenCharacterType.InlineCode,
                TokenCharacterType.InlineCode,
                TokenCharacterType.InlineCode,
                TokenCharacterType.InlineCodeStart,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testNonInlineCode() {
        val tokenizer = MFMTokenizer()
        val content = "`test"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testCodeBlock() {
        val tokenizer = MFMTokenizer()
        val content = "```kotlin\ntest\n```"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.CodeBlockStart,
                TokenCharacterType.CodeBlockStart,
                TokenCharacterType.CodeBlockStart,
                TokenCharacterType.CodeBlockLanguage,
                TokenCharacterType.CodeBlockLanguage,
                TokenCharacterType.CodeBlockLanguage,
                TokenCharacterType.CodeBlockLanguage,
                TokenCharacterType.CodeBlockLanguage,
                TokenCharacterType.CodeBlockLanguage,
                TokenCharacterType.CodeBlockLanguageEnd,
                TokenCharacterType.CodeBlock,
                TokenCharacterType.CodeBlock,
                TokenCharacterType.CodeBlock,
                TokenCharacterType.CodeBlock,
                TokenCharacterType.CodeBlock,
                TokenCharacterType.CodeBlockStart,
                TokenCharacterType.CodeBlockStart,
                TokenCharacterType.CodeBlockStart,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testNonCodeBlock() {
        val tokenizer = MFMTokenizer()
        val content = "```kotlin\ntest\n``"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testAsteriskBold() {
        val tokenizer = MFMTokenizer()
        val content = "**test**"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.AsteriskBold,
                TokenCharacterType.AsteriskBold,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.AsteriskBold,
                TokenCharacterType.AsteriskBold,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testAsteriskItalic() {
        val tokenizer = MFMTokenizer()
        val content = "*test*"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.AsteriskItalicStart,
                TokenCharacterType.Italic,
                TokenCharacterType.Italic,
                TokenCharacterType.Italic,
                TokenCharacterType.Italic,
                TokenCharacterType.AsteriskItalicStart,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testAsteriskNonBold() {
        val tokenizer = MFMTokenizer()
        val content = "*test"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testAsteriskNonItalic() {
        val tokenizer = MFMTokenizer()
        val content = "!*test*"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            content.map {
                TokenCharacterType.Character
            }.plus(
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testUnderscoreBold() {
        val tokenizer = MFMTokenizer()
        val content = "__test__"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.UnderscoreBoldStart,
                TokenCharacterType.UnderscoreBoldStart,
                TokenCharacterType.Bold,
                TokenCharacterType.Bold,
                TokenCharacterType.Bold,
                TokenCharacterType.Bold,
                TokenCharacterType.UnderscoreBoldStart,
                TokenCharacterType.UnderscoreBoldStart,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testUnderscoreItalic() {
        val tokenizer = MFMTokenizer()
        val content = "_test_"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.UnderscoreItalicStart,
                TokenCharacterType.Italic,
                TokenCharacterType.Italic,
                TokenCharacterType.Italic,
                TokenCharacterType.Italic,
                TokenCharacterType.UnderscoreItalicStart,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testUnderscoreNonBold() {
        val tokenizer = MFMTokenizer()
        val content = "_test"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testUnderscoreNonItalic() {
        val tokenizer = MFMTokenizer()
        val content = "!_test_"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testInelineMath() {
        val tokenizer = MFMTokenizer()
        val content = "\\(test\\)"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.InlineMath,
                TokenCharacterType.InlineMath,
                TokenCharacterType.InlineMathContent,
                TokenCharacterType.InlineMathContent,
                TokenCharacterType.InlineMathContent,
                TokenCharacterType.InlineMathContent,
                TokenCharacterType.InlineMath,
                TokenCharacterType.InlineMath,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testMathBlock() {
        val tokenizer = MFMTokenizer()
        val content = "\\[test\\]"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.MathBlock,
                TokenCharacterType.MathBlock,
                TokenCharacterType.MathBlockContent,
                TokenCharacterType.MathBlockContent,
                TokenCharacterType.MathBlockContent,
                TokenCharacterType.MathBlockContent,
                TokenCharacterType.MathBlock,
                TokenCharacterType.MathBlock,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testQuote() {
        val tokenizer = MFMTokenizer()
        val content = ">test"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.Blockquote,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testQuoteWithSpace() {
        val tokenizer = MFMTokenizer()
        val content = "> test"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.Blockquote,
                TokenCharacterType.Blockquote,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testTag() {
        val tokenizer = MFMTokenizer()
        val content = "<test>"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.TagOpen,
                TokenCharacterType.Tag,
                TokenCharacterType.Tag,
                TokenCharacterType.Tag,
                TokenCharacterType.Tag,
                TokenCharacterType.TagClose,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testTagWithEndTag() {
        val tokenizer = MFMTokenizer()
        val content = "<test></test>"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.TagOpen,
                TokenCharacterType.Tag,
                TokenCharacterType.Tag,
                TokenCharacterType.Tag,
                TokenCharacterType.Tag,
                TokenCharacterType.TagClose,
                TokenCharacterType.EndTagOpen,
                TokenCharacterType.EndTagOpen,
                TokenCharacterType.Tag,
                TokenCharacterType.Tag,
                TokenCharacterType.Tag,
                TokenCharacterType.Tag,
                TokenCharacterType.TagClose,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testLink() {
        val tokenizer = MFMTokenizer()
        val content = "[test](https://test.com)"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.LinkOpen,
                TokenCharacterType.LinkContent,
                TokenCharacterType.LinkContent,
                TokenCharacterType.LinkContent,
                TokenCharacterType.LinkContent,
                TokenCharacterType.LinkClose,
                TokenCharacterType.LinkHrefOpen,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHrefClose,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testSilentLink() {
        val tokenizer = MFMTokenizer()
        val content = "?[test](https://test.com)"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.SilentLink,
                TokenCharacterType.LinkOpen,
                TokenCharacterType.LinkContent,
                TokenCharacterType.LinkContent,
                TokenCharacterType.LinkContent,
                TokenCharacterType.LinkContent,
                TokenCharacterType.LinkClose,
                TokenCharacterType.LinkHrefOpen,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHref,
                TokenCharacterType.LinkHrefClose,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testBracket() {
        val tokenizer = MFMTokenizer()
        val content = "asd?[Search]"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Search,
                TokenCharacterType.Search,
                TokenCharacterType.Search,
                TokenCharacterType.Search,
                TokenCharacterType.Search,
                TokenCharacterType.Search,
                TokenCharacterType.Search,
                TokenCharacterType.Search,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testSearch() {
        val tokenizer = MFMTokenizer()
        val content = "misskey [Search]"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Search,
                TokenCharacterType.Search,
                TokenCharacterType.Search,
                TokenCharacterType.Search,
                TokenCharacterType.Search,
                TokenCharacterType.Search,
                TokenCharacterType.Search,
                TokenCharacterType.Search,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testUrl() {
        val tokenizer = MFMTokenizer()
        val content = "https://test.com"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.Url,
                TokenCharacterType.Url,
                TokenCharacterType.Url,
                TokenCharacterType.Url,
                TokenCharacterType.Url,
                TokenCharacterType.Url,
                TokenCharacterType.Url,
                TokenCharacterType.Url,
                TokenCharacterType.Url,
                TokenCharacterType.Url,
                TokenCharacterType.Url,
                TokenCharacterType.Url,
                TokenCharacterType.Url,
                TokenCharacterType.Url,
                TokenCharacterType.Url,
                TokenCharacterType.Url,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testFn() {
        val tokenizer = MFMTokenizer()
        val content = "\$[flip.h,v MisskeyでFediverseの世界が広がります]"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.Fn,
                TokenCharacterType.Fn,
                TokenCharacterType.FnContent,
                TokenCharacterType.FnContent,
                TokenCharacterType.FnContent,
                TokenCharacterType.FnContent,
                TokenCharacterType.FnContent,
                TokenCharacterType.FnContent,
                TokenCharacterType.FnContent,
                TokenCharacterType.FnContent,
                TokenCharacterType.Fn,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.FnEndBracket,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testTildeStrikethrough() {
        val tokenizer = MFMTokenizer()
        val content = "~~test~~"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.Strike,
                TokenCharacterType.Strike,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Strike,
                TokenCharacterType.Strike,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testNonTildeStrikethrough() {
        val tokenizer = MFMTokenizer()
        val content = "~test"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            listOf(
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Character,
                TokenCharacterType.Eof
            ),
            result
        )
    }

    @Test
    fun testNonFn() {
        val tokenizer = MFMTokenizer()
        val content = "\$[flip.h,v Miss~~keyでFedivers*eの世**界が広_が__ります"
        val result = tokenizer.parse(StringReader(content))
        assertEquals(content.length, result.size - 1)
        assertContentEquals(
            "\$[".map { TokenCharacterType.Fn } +
                "flip.h,v".map { TokenCharacterType.FnContent } +
                " ".map { TokenCharacterType.Fn } +
                "Miss".map { TokenCharacterType.Character } +
                "~~".map { TokenCharacterType.Strike } +
                "keyでFedivers".map { TokenCharacterType.Character } +
                "*".map { TokenCharacterType.Character } +
                "eの世".map { TokenCharacterType.Character } +
                "**".map { TokenCharacterType.AsteriskBold } +
                "界が広".map { TokenCharacterType.Character } +
                "_".map { TokenCharacterType.Character } +
                "が".map { TokenCharacterType.Character } +
                "__".map { TokenCharacterType.Character } +
                "ります".map { TokenCharacterType.Character } +
                listOf(TokenCharacterType.Eof),
            result
        )
    }
}
