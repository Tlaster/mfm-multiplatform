package moe.tlaster.mfm.parser.tokenizer

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class StateTest {
    @Test
    fun testEmoji() {
        val tokenizer = TestTokenizer()
        val content = ":test:"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(1, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        val acceptIndex = tokenizer.acceptIndex.first()
        assertEquals(content.length, acceptIndex)
        val token = tokenizer.tokens.subList(0, acceptIndex).map { it.value }.joinToString("")
        assertEquals(content, token)
        assertContentEquals(
            listOf(
                EmojiNameCharacter(':'),
                EmojiNameCharacter('t'),
                EmojiNameCharacter('e'),
                EmojiNameCharacter('s'),
                EmojiNameCharacter('t'),
                EmojiNameCharacter(':'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testColonNonEmoji() {
        val tokenizer = TestTokenizer()
        val content = ":test"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(0, tokenizer.acceptIndex.size)
        assertEquals(1, tokenizer.rejectIndex.size)
        val rejectIndex = tokenizer.rejectIndex.first()
        assertEquals(content.length, rejectIndex)
        val token = tokenizer.tokens.subList(0, rejectIndex).map { it.value }.joinToString("")
        assertEquals(":test", token)
        assertContentEquals(
            listOf(
                Character(':'),
                Character('t'),
                Character('e'),
                Character('s'),
                Character('t'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testHashTag() {
        val tokenizer = TestTokenizer()
        val content = "#test"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(1, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        val acceptIndex = tokenizer.acceptIndex.first()
        assertEquals(content.length, acceptIndex)
        val token = tokenizer.tokens.subList(0, acceptIndex).map { it.value }.joinToString("")
        assertEquals(content, token)
        assertContentEquals(
            listOf(
                HashTagCharacter('#'),
                HashTagCharacter('t'),
                HashTagCharacter('e'),
                HashTagCharacter('s'),
                HashTagCharacter('t'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testHashNonTag() {
        val tokenizer = TestTokenizer()
        val content = "#!test"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(0, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        val token = tokenizer.tokens.subList(0, content.length).map { it.value }.joinToString("")
        assertEquals("#!test", token)
        assertContentEquals(
            listOf(
                Character('#'),
                Character('!'),
                Character('t'),
                Character('e'),
                Character('s'),
                Character('t'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testHashtagBefore() {
        val tokenizer = TestTokenizer()
        val content = "asd#test"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(1, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        val acceptIndex = tokenizer.acceptIndex.first()
        assertEquals(content.length, acceptIndex)
        val token = tokenizer.tokens.subList("asd".length, acceptIndex).map { it.value }.joinToString("")
        assertEquals("#test", token)
        assertContentEquals(
            listOf(
                Character('a'),
                Character('s'),
                Character('d'),
                HashTagCharacter('#'),
                HashTagCharacter('t'),
                HashTagCharacter('e'),
                HashTagCharacter('s'),
                HashTagCharacter('t'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testNonHashtagBefore() {
        val tokenizer = TestTokenizer()
        val content = ":#test"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(0, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        val token = tokenizer.tokens.subList(0, content.length).map { it.value }.joinToString("")
        assertEquals(":#test", token)
        assertContentEquals(
            listOf(
                Character(':'),
                Character('#'),
                Character('t'),
                Character('e'),
                Character('s'),
                Character('t'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testAtUserName() {
        val tokenizer = TestTokenizer()
        val content = "@test"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(1, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        val acceptIndex = tokenizer.acceptIndex.first()
        assertEquals(content.length, acceptIndex)
        val token = tokenizer.tokens.subList(0, acceptIndex).map { it.value }.joinToString("")
        assertEquals(content, token)
        assertContentEquals(
            listOf(
                UserNameCharacter('@'),
                UserNameCharacter('t'),
                UserNameCharacter('e'),
                UserNameCharacter('s'),
                UserNameCharacter('t'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testAtUserNameWithHost() {
        val tokenizer = TestTokenizer()
        val content = "@test@host"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(1, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        val acceptIndex = tokenizer.acceptIndex.first()
        assertEquals(content.length, acceptIndex)
        val token = tokenizer.tokens.subList(0, acceptIndex).map { it.value }.joinToString("")
        assertEquals(content, token)
        assertContentEquals(
            listOf(
                UserNameCharacter('@'),
                UserNameCharacter('t'),
                UserNameCharacter('e'),
                UserNameCharacter('s'),
                UserNameCharacter('t'),
                UserHostCharacter('@'),
                UserHostCharacter('h'),
                UserHostCharacter('o'),
                UserHostCharacter('s'),
                UserHostCharacter('t'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testAtUserNameWithHostBefore() {
        val tokenizer = TestTokenizer()
        val content = "asd@test@host"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(1, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        val acceptIndex = tokenizer.acceptIndex.first()
        assertEquals(content.length, acceptIndex)
        val token = tokenizer.tokens.subList("asd".length, acceptIndex).map { it.value }.joinToString("")
        assertEquals("@test@host", token)
        assertContentEquals(
            listOf(
                Character('a'),
                Character('s'),
                Character('d'),
                UserNameCharacter('@'),
                UserNameCharacter('t'),
                UserNameCharacter('e'),
                UserNameCharacter('s'),
                UserNameCharacter('t'),
                UserHostCharacter('@'),
                UserHostCharacter('h'),
                UserHostCharacter('o'),
                UserHostCharacter('s'),
                UserHostCharacter('t'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testAtNonUserNameWithHostBefore() {
        val tokenizer = TestTokenizer()
        val content = "asd!@test@host"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(1, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        val acceptIndex = tokenizer.acceptIndex.first()
        assertEquals(content.length, acceptIndex)
        val token = tokenizer.tokens.subList("asd!@test".length, acceptIndex).map { it.value }.joinToString("")
        assertEquals("@host", token)
        assertContentEquals(
            listOf(
                Character('a'),
                Character('s'),
                Character('d'),
                Character('!'),
                Character('@'),
                Character('t'),
                Character('e'),
                Character('s'),
                Character('t'),
                UserNameCharacter('@'),
                UserNameCharacter('h'),
                UserNameCharacter('o'),
                UserNameCharacter('s'),
                UserNameCharacter('t'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testCash() {
        val tokenizer = TestTokenizer()
        val content = "\$test"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(1, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        val acceptIndex = tokenizer.acceptIndex.first()
        assertEquals(content.length, acceptIndex)
        val token = tokenizer.tokens.subList(0, acceptIndex).map { it.value }.joinToString("")
        assertEquals(content, token)
        assertContentEquals(
            listOf(
                CashCharacter('$'),
                CashCharacter('t'),
                CashCharacter('e'),
                CashCharacter('s'),
                CashCharacter('t'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testInlineCode() {
        val tokenizer = TestTokenizer()
        val content = "`test`"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(1, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        val acceptIndex = tokenizer.acceptIndex.first()
        assertEquals(content.length, acceptIndex)
        val token = tokenizer.tokens.subList(0, acceptIndex).map { it.value }.joinToString("")
        assertEquals(content, token)
        assertContentEquals(
            listOf(
                InlineCodeCharacter('`'),
                InlineCodeCharacter('t'),
                InlineCodeCharacter('e'),
                InlineCodeCharacter('s'),
                InlineCodeCharacter('t'),
                InlineCodeCharacter('`'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testNonInlineCode() {
        val tokenizer = TestTokenizer()
        val content = "`test"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(0, tokenizer.acceptIndex.size)
        assertEquals(1, tokenizer.rejectIndex.size)
        val rejectIndex = tokenizer.rejectIndex.first()
        assertEquals(content.length, rejectIndex)
        val token = tokenizer.tokens.subList(0, rejectIndex).map { it.value }.joinToString("")
        assertEquals("`test", token)
        assertContentEquals(
            listOf(
                Character('`'),
                Character('t'),
                Character('e'),
                Character('s'),
                Character('t'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testCodeBlock() {
        val tokenizer = TestTokenizer()
        val content = "```kotlin\ntest\n```"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(1, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        val acceptIndex = tokenizer.acceptIndex.first()
        assertEquals(content.length, acceptIndex)
        val token = tokenizer.tokens.subList(0, acceptIndex).map { it.value }.joinToString("")
        assertEquals(content, token)
        assertContentEquals(
            listOf(
                CodeBlockCharacter('`'),
                CodeBlockCharacter('`'),
                CodeBlockCharacter('`'),
                CodeBlockLanguageCharacter('k'),
                CodeBlockLanguageCharacter('o'),
                CodeBlockLanguageCharacter('t'),
                CodeBlockLanguageCharacter('l'),
                CodeBlockLanguageCharacter('i'),
                CodeBlockLanguageCharacter('n'),
                CodeBlockCharacter('\n'),
                CodeBlockCharacter('t'),
                CodeBlockCharacter('e'),
                CodeBlockCharacter('s'),
                CodeBlockCharacter('t'),
                CodeBlockCharacter('\n'),
                CodeBlockCharacter('`'),
                CodeBlockCharacter('`'),
                CodeBlockCharacter('`'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testNonCodeBlock() {
        val tokenizer = TestTokenizer()
        val content = "```kotlin\ntest\n``"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(0, tokenizer.acceptIndex.size)
        assertEquals(1, tokenizer.rejectIndex.size)
        val rejectIndex = tokenizer.rejectIndex.first()
        assertEquals(content.length, rejectIndex)
        val token = tokenizer.tokens.subList(0, rejectIndex).map { it.value }.joinToString("")
        assertEquals("```kotlin\ntest\n``", token)
        assertContentEquals(
            listOf(
                Character('`'),
                Character('`'),
                Character('`'),
                Character('k'),
                Character('o'),
                Character('t'),
                Character('l'),
                Character('i'),
                Character('n'),
                Character('\n'),
                Character('t'),
                Character('e'),
                Character('s'),
                Character('t'),
                Character('\n'),
                Character('`'),
                Character('`'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testAsteriskBold() {
        val tokenizer = TestTokenizer()
        val content = "**test**"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(1, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        val acceptIndex = tokenizer.acceptIndex.last()
        assertEquals(content.length, acceptIndex)
        val token = tokenizer.tokens.subList(0, acceptIndex).map { it.value }.joinToString("")
        assertEquals(content, token)
        assertContentEquals(
            listOf(
                BoldCharacter('*'),
                BoldCharacter('*'),
                BoldCharacter('t'),
                BoldCharacter('e'),
                BoldCharacter('s'),
                BoldCharacter('t'),
                BoldCharacter('*'),
                BoldCharacter('*'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testAsteriskItalic() {
        val tokenizer = TestTokenizer()
        val content = "*test*"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(1, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        val acceptIndex = tokenizer.acceptIndex.last()
        assertEquals(content.length, acceptIndex)
        val token = tokenizer.tokens.subList(0, acceptIndex).map { it.value }.joinToString("")
        assertEquals(content, token)
        assertContentEquals(
            listOf(
                ItalicCharacter('*'),
                ItalicCharacter('t'),
                ItalicCharacter('e'),
                ItalicCharacter('s'),
                ItalicCharacter('t'),
                ItalicCharacter('*'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testAsteriskNonBold() {
        val tokenizer = TestTokenizer()
        val content = "*test"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(0, tokenizer.acceptIndex.size)
        assertEquals(1, tokenizer.rejectIndex.size)
        val rejectIndex = tokenizer.rejectIndex.last()
        assertEquals(content.length, rejectIndex)
        val token = tokenizer.tokens.subList(0, rejectIndex).map { it.value }.joinToString("")
        assertEquals("*test", token)
        assertContentEquals(
            listOf(
                Character('*'),
                Character('t'),
                Character('e'),
                Character('s'),
                Character('t'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testAsteriskNonItalic() {
        val tokenizer = TestTokenizer()
        val content = "!*test*"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(0, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        val token = tokenizer.tokens.subList(0, content.length).map { it.value }.joinToString("")
        assertEquals("!*test*", token)
        assertContentEquals(
            listOf(
                Character('!'),
                Character('*'),
                Character('t'),
                Character('e'),
                Character('s'),
                Character('t'),
                Character('*'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testUnderscoreBold() {
        val tokenizer = TestTokenizer()
        val content = "__test__"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(1, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        val acceptIndex = tokenizer.acceptIndex.last()
        assertEquals(content.length, acceptIndex)
        val token = tokenizer.tokens.subList(0, acceptIndex).map { it.value }.joinToString("")
        assertEquals(content, token)
        assertContentEquals(
            listOf(
                BoldCharacter('_'),
                BoldCharacter('_'),
                BoldCharacter('t'),
                BoldCharacter('e'),
                BoldCharacter('s'),
                BoldCharacter('t'),
                BoldCharacter('_'),
                BoldCharacter('_'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testUnderscoreItalic() {
        val tokenizer = TestTokenizer()
        val content = "_test_"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(1, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        val acceptIndex = tokenizer.acceptIndex.last()
        assertEquals(content.length, acceptIndex)
        val token = tokenizer.tokens.subList(0, acceptIndex).map { it.value }.joinToString("")
        assertEquals(content, token)
        assertContentEquals(
            listOf(
                ItalicCharacter('_'),
                ItalicCharacter('t'),
                ItalicCharacter('e'),
                ItalicCharacter('s'),
                ItalicCharacter('t'),
                ItalicCharacter('_'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testUnderscoreNonBold() {
        val tokenizer = TestTokenizer()
        val content = "_test"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(0, tokenizer.acceptIndex.size)
        assertEquals(1, tokenizer.rejectIndex.size)
        val rejectIndex = tokenizer.rejectIndex.last()
        assertEquals(content.length, rejectIndex)
        val token = tokenizer.tokens.subList(0, rejectIndex).map { it.value }.joinToString("")
        assertEquals("_test", token)
        assertContentEquals(
            listOf(
                Character('_'),
                Character('t'),
                Character('e'),
                Character('s'),
                Character('t'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testUnderscoreNonItalic() {
        val tokenizer = TestTokenizer()
        val content = "!_test_"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(0, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        val token = tokenizer.tokens.subList(0, content.length).map { it.value }.joinToString("")
        assertEquals("!_test_", token)
        assertContentEquals(
            listOf(
                Character('!'),
                Character('_'),
                Character('t'),
                Character('e'),
                Character('s'),
                Character('t'),
                Character('_'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testInelineMath() {
        val tokenizer = TestTokenizer()
        val content = "\\(test\\)"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(1, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        assertEquals(content.length, tokenizer.acceptIndex.last())
        val token = tokenizer.tokens.subList(0, content.length).map { it.value }.joinToString("")
        assertEquals(content, token)
        assertContentEquals(
            listOf(
                InlineMathCharacter('\\'),
                InlineMathCharacter('('),
                InlineMathCharacter('t'),
                InlineMathCharacter('e'),
                InlineMathCharacter('s'),
                InlineMathCharacter('t'),
                InlineMathCharacter('\\'),
                InlineMathCharacter(')'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testMathBlock() {
        val tokenizer = TestTokenizer()
        val content = "\\[test\\]"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(1, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        assertEquals(content.length, tokenizer.acceptIndex.last())
        val token = tokenizer.tokens.subList(0, content.length).map { it.value }.joinToString("")
        assertEquals(content, token)
        assertContentEquals(
            listOf(
                MathBlockCharacter('\\'),
                MathBlockCharacter('['),
                MathBlockCharacter('t'),
                MathBlockCharacter('e'),
                MathBlockCharacter('s'),
                MathBlockCharacter('t'),
                MathBlockCharacter('\\'),
                MathBlockCharacter(']'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testQuote() {
        val tokenizer = TestTokenizer()
        val content = ">test"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(0, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        assertEquals(content.length, tokenizer.tokens.size - 1)
        val token = tokenizer.tokens.subList(0, content.length).map { it.value }.joinToString("")
        assertEquals(content, token)
        assertContentEquals(
            listOf(
                BlockquoteCharacter,
                Character('t'),
                Character('e'),
                Character('s'),
                Character('t'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testTag() {
        val tokenizer = TestTokenizer()
        val content = "<test>"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(1, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        assertEquals(content.length, tokenizer.tokens.size - 1)
        val token = tokenizer.tokens.subList(0, tokenizer.acceptIndex.last()).map { it.value }.joinToString("")
        assertEquals(content, token)
        assertContentEquals(
            listOf(
                TagOpenCharacter,
                TagCharacter('t'),
                TagCharacter('e'),
                TagCharacter('s'),
                TagCharacter('t'),
                TagCharacter('>'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testTagWithEndTag() {
        val tokenizer = TestTokenizer()
        val content = "<test></test>"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(2, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        assertEquals(content.length, tokenizer.tokens.size - 1)
        val token = tokenizer.tokens.subList(0, tokenizer.acceptIndex.last()).map { it.value }.joinToString("")
        assertEquals(content, token)
        assertContentEquals(
            listOf(
                TagOpenCharacter,
                TagCharacter('t'),
                TagCharacter('e'),
                TagCharacter('s'),
                TagCharacter('t'),
                TagCharacter('>'),
                EndTagOpenCharacter('<'),
                EndTagOpenCharacter('/'),
                TagCharacter('t'),
                TagCharacter('e'),
                TagCharacter('s'),
                TagCharacter('t'),
                TagCharacter('>'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testLink() {
        val tokenizer = TestTokenizer()
        val content = "[test](https://test.com)"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(2, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        assertEquals(content.length, tokenizer.tokens.size - 1)
        val token = tokenizer.tokens.subList(0, tokenizer.acceptIndex.last()).map { it.value }.joinToString("")
        assertEquals(content, token)
        assertContentEquals(
            listOf(
                BracketCharacter('['),
                BracketCharacter('t'),
                BracketCharacter('e'),
                BracketCharacter('s'),
                BracketCharacter('t'),
                BracketCharacter(']'),
                RoundBracketCharacter('('),
                RoundBracketCharacter('h'),
                RoundBracketCharacter('t'),
                RoundBracketCharacter('t'),
                RoundBracketCharacter('p'),
                RoundBracketCharacter('s'),
                RoundBracketCharacter(':'),
                RoundBracketCharacter('/'),
                RoundBracketCharacter('/'),
                RoundBracketCharacter('t'),
                RoundBracketCharacter('e'),
                RoundBracketCharacter('s'),
                RoundBracketCharacter('t'),
                RoundBracketCharacter('.'),
                RoundBracketCharacter('c'),
                RoundBracketCharacter('o'),
                RoundBracketCharacter('m'),
                RoundBracketCharacter(')'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testSilentLink() {
        val tokenizer = TestTokenizer()
        val content = "?[test](https://test.com)"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(2, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        assertEquals(content.length, tokenizer.tokens.size - 1)
        val token = tokenizer.tokens.subList(0, tokenizer.acceptIndex.last()).map { it.value }.joinToString("")
        assertEquals(content, token)
        assertContentEquals(
            listOf(
                BracketCharacter('?'),
                BracketCharacter('['),
                BracketCharacter('t'),
                BracketCharacter('e'),
                BracketCharacter('s'),
                BracketCharacter('t'),
                BracketCharacter(']'),
                RoundBracketCharacter('('),
                RoundBracketCharacter('h'),
                RoundBracketCharacter('t'),
                RoundBracketCharacter('t'),
                RoundBracketCharacter('p'),
                RoundBracketCharacter('s'),
                RoundBracketCharacter(':'),
                RoundBracketCharacter('/'),
                RoundBracketCharacter('/'),
                RoundBracketCharacter('t'),
                RoundBracketCharacter('e'),
                RoundBracketCharacter('s'),
                RoundBracketCharacter('t'),
                RoundBracketCharacter('.'),
                RoundBracketCharacter('c'),
                RoundBracketCharacter('o'),
                RoundBracketCharacter('m'),
                RoundBracketCharacter(')'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testBracket() {
        val tokenizer = TestTokenizer()
        val content = "asd?[Search]"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(1, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        assertEquals(content.length, tokenizer.tokens.size - 1)
        val token = tokenizer.tokens.subList(0, tokenizer.acceptIndex.last()).map { it.value }.joinToString("")
        assertEquals(content, token)
        assertContentEquals(
            listOf(
                Character('a'),
                Character('s'),
                Character('d'),
                BracketCharacter('?'),
                BracketCharacter('['),
                BracketCharacter('S'),
                BracketCharacter('e'),
                BracketCharacter('a'),
                BracketCharacter('r'),
                BracketCharacter('c'),
                BracketCharacter('h'),
                BracketCharacter(']'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testUrl() {
        val tokenizer = TestTokenizer()
        val content = "https://test.com"
        tokenizer.parse(StringReader(content))
        assertEquals(content.length, tokenizer.tokens.size - 1)
        assertEquals(1, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        assertEquals(content.length, tokenizer.tokens.size - 1)
        val token = tokenizer.tokens.subList(0, tokenizer.acceptIndex.last()).map { it.value }.joinToString("")
        assertEquals(content, token)
        assertContentEquals(
            listOf(
                UrlCharacter('h'),
                UrlCharacter('t'),
                UrlCharacter('t'),
                UrlCharacter('p'),
                UrlCharacter('s'),
                UrlCharacter(':'),
                UrlCharacter('/'),
                UrlCharacter('/'),
                UrlCharacter('t'),
                UrlCharacter('e'),
                UrlCharacter('s'),
                UrlCharacter('t'),
                UrlCharacter('.'),
                UrlCharacter('c'),
                UrlCharacter('o'),
                UrlCharacter('m'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )
    }

    @Test
    fun testFn() {
        val tokenizer = TestTokenizer()
        val content = "\$[flip.h,v MisskeyでFediverseの世界が広がります]"
        tokenizer.parse(StringReader(content))
        // content.length - 1: ignore empty space
        assertEquals(content.length - 1, tokenizer.tokens.size - 1)
        assertEquals(1, tokenizer.acceptIndex.size)
        assertEquals(0, tokenizer.rejectIndex.size)
        assertContentEquals(
            listOf(
                FnCharacter('$'),
                FnCharacter('['),
                FnCharacter('f'),
                FnCharacter('l'),
                FnCharacter('i'),
                FnCharacter('p'),
                FnCharacter('.'),
                FnCharacter('h'),
                FnCharacter(','),
                FnCharacter('v'),
                Character('M'),
                Character('i'),
                Character('s'),
                Character('s'),
                Character('k'),
                Character('e'),
                Character('y'),
                Character('で'),
                Character('F'),
                Character('e'),
                Character('d'),
                Character('i'),
                Character('v'),
                Character('e'),
                Character('r'),
                Character('s'),
                Character('e'),
                Character('の'),
                Character('世'),
                Character('界'),
                Character('が'),
                Character('広'),
                Character('が'),
                Character('り'),
                Character('ま'),
                Character('す'),
                FnBracketCharacter(']'),
                EOFTokenCharacter
            ),
            tokenizer.tokens
        )

    }

}

