package moe.tlaster.mfm.parser.tokenizer

import moe.tlaster.mfm.parser.tree.BoldNode
import moe.tlaster.mfm.parser.tree.CashNode
import moe.tlaster.mfm.parser.tree.CenterNode
import moe.tlaster.mfm.parser.tree.CodeBlockNode
import moe.tlaster.mfm.parser.tree.EmojiCodeNode
import moe.tlaster.mfm.parser.tree.FnNode
import moe.tlaster.mfm.parser.tree.HashtagNode
import moe.tlaster.mfm.parser.tree.InlineCodeNode
import moe.tlaster.mfm.parser.tree.ItalicNode
import moe.tlaster.mfm.parser.tree.LinkNode
import moe.tlaster.mfm.parser.tree.MathBlockNode
import moe.tlaster.mfm.parser.tree.MathInlineNode
import moe.tlaster.mfm.parser.tree.MentionNode
import moe.tlaster.mfm.parser.tree.QuoteNode
import moe.tlaster.mfm.parser.tree.RootNode
import moe.tlaster.mfm.parser.tree.SearchNode
import moe.tlaster.mfm.parser.tree.SmallNode
import moe.tlaster.mfm.parser.tree.StrikeNode
import moe.tlaster.mfm.parser.tree.TextNode
import moe.tlaster.mfm.parser.tree.TreeBuilder
import moe.tlaster.mfm.parser.tree.UrlNode
import kotlin.test.Test
import kotlin.test.assertEquals

class TreeBuilderTest {
    @Test
    fun testText() {
        val tokenizer = Tokenizer()
        val content = ":test:"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        EmojiCodeNode(
                            emoji = "test",
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testUserName() {
        val tokenizer = Tokenizer()
        val content = "@test"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        MentionNode(
                            userName = "test",
                            host = null,
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testUserNameWithHost() {
        val tokenizer = Tokenizer()
        val content = "@test@host"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        MentionNode(
                            userName = "test",
                            host = "host",
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testMentionAllowsDotsInUsername() {
        val tokenizer = Tokenizer()
        val content = "@first.last@misskey.io"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        MentionNode(
                            userName = "first.last",
                            host = "misskey.io",
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testMentionMustNotStartAfterAsciiAlphanumeric() {
        val tokenizer = Tokenizer()
        val content = "a@test"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        TextNode(
                            content = "a@test",
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testMentionMustNotStartWithHyphen() {
        val tokenizer = Tokenizer()
        val content = "@-test"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        TextNode(
                            content = "@-test",
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testHashTag() {
        val tokenizer = Tokenizer()
        val content = "#test"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        HashtagNode(
                            tag = "test",
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testCashTag() {
        val tokenizer = Tokenizer()
        val content = "\$test"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        CashNode(
                            content = "test",
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testInlineCode() {
        val tokenizer = Tokenizer()
        val content = "`test`"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        InlineCodeNode(
                            code = "test",
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testCodeBlock() {
        val tokenizer = Tokenizer()
        val content = "```test```"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        CodeBlockNode(
                            code = "test",
                            language = null,
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testCodeBlockWithLanguage() {
        val tokenizer = Tokenizer()
        val content = "```kotlin\ntest\n```"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        CodeBlockNode(
                            code = "test",
                            language = "kotlin",
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testCodeBlockWithSpecialLanguage() {
        val tokenizer = Tokenizer()
        val content = "```objective-c\ntest\n```"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        CodeBlockNode(
                            code = "test",
                            language = "objective-c",
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testAsteriskBold() {
        val tokenizer = Tokenizer()
        val content = "**test**"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        BoldNode(
                            start = 0,
                            content =
                                arrayListOf(
                                    TextNode(
                                        content = "test",
                                    ),
                                ),
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testAsteriskItalic() {
        val tokenizer = Tokenizer()
        val content = "*test*"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        ItalicNode(
                            start = 0,
                            content =
                                arrayListOf(
                                    TextNode(
                                        content = "test",
                                    ),
                                ),
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testUnderscoreBold() {
        val tokenizer = Tokenizer()
        val content = "__test__"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        BoldNode(
                            start = 0,
                            content =
                                arrayListOf(
                                    TextNode(
                                        content = "test",
                                    ),
                                ),
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testUnderscoreItalic() {
        val tokenizer = Tokenizer()
        val content = "_test_"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        ItalicNode(
                            start = 0,
                            content =
                                arrayListOf(
                                    TextNode(
                                        content = "test",
                                    ),
                                ),
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testInelineMath() {
        val tokenizer = Tokenizer()
        val content = "\\(test\\)"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        MathInlineNode(
                            formula = "test",
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testMathBlock() {
        val tokenizer = Tokenizer()
        val content = "\\[test\\]"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(RootNode(content = arrayListOf(MathBlockNode(formula = "test"))), builderResult)
    }

    @Test
    fun testQuote() {
        val tokenizer = Tokenizer()
        val content = "> test"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)

        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        QuoteNode(
                            start = 0,
                            content =
                                arrayListOf(
                                    TextNode(
                                        content = "test",
                                    ),
                                ),
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testQuoteMergesAdjacentLines() {
        val tokenizer = Tokenizer()
        val content = "> foo\n> bar"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)

        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        QuoteNode(
                            start = 0,
                            content =
                                arrayListOf(
                                    TextNode("foo\nbar"),
                                ),
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testQuoteAllowsBlankLinesInside() {
        val tokenizer = Tokenizer()
        val content = "> foo\n>\n> bar"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)

        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        QuoteNode(
                            start = 0,
                            content =
                                arrayListOf(
                                    TextNode("foo\n\nbar"),
                                ),
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testQuoteIgnoresTrailingBlankLine() {
        val tokenizer = Tokenizer()
        val content = "> foo\n>\noutside"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)

        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        QuoteNode(
                            start = 0,
                            content = arrayListOf(TextNode("foo")),
                        ),
                        TextNode("\noutside"),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testLink() {
        val tokenizer = Tokenizer()
        val content = "[test](https://test.com)"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)

        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        LinkNode(
                            content = arrayListOf(TextNode("test")),
                            url = "https://test.com",
                            silent = false,
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testSilentLink() {
        val tokenizer = Tokenizer()
        val content = "?[test](https://test.com)"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)

        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        LinkNode(
                            content = arrayListOf(TextNode("test")),
                            url = "https://test.com",
                            silent = true,
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testUrl() {
        val tokenizer = Tokenizer()
        val content = "https://test.com"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)

        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        UrlNode(
                            url = "https://test.com",
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testCJKUrl() {
        val tokenizer = Tokenizer()
        val encoded = "https://example.com/%E6%B5%8B%E8%AF%95"
        val decoded = "https://example.com/测试"
        val result = tokenizer.parse(StringReader(encoded))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(encoded), result)

        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        UrlNode(
                            url = decoded,
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testFn() {
        val tokenizer = Tokenizer()
        val content = "\$[flip.h,v MisskeyでFediverseの世界が広がります]"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        FnNode(
                            start = 0,
                            name = "flip",
                            content = arrayListOf(TextNode(content = "MisskeyでFediverseの世界が広がります")),
                            args = hashMapOf("h" to "true", "v" to "true"),
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testNonFn() {
        val tokenizer = Tokenizer()
        val content = "\$[flip.h,v Miss~~keyでFedivers*eの世**界が広_が__ります"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        TextNode(
                            content = "\$[flip.h,v Miss~~keyでFedivers*eの世**界が広_が__ります",
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testTildeStrikethrough() {
        val tokenizer = Tokenizer()
        val content = "~~test~~"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)

        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        StrikeNode(
                            start = 0,
                            content =
                                arrayListOf(
                                    TextNode(
                                        content = "test",
                                    ),
                                ),
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testSearch() {
        val tokenizer = Tokenizer()
        val content = "misskey [Search]"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)

        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        SearchNode(
                            query = "misskey",
                            search = "[Search]",
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testPlainSearch() {
        val tokenizer = Tokenizer()
        val content = "misskey Search"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)

        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        SearchNode(
                            query = "misskey",
                            search = "Search",
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testPlainSearchIgnoreCase() {
        val tokenizer = Tokenizer()
        val content = "misskey search"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)

        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        SearchNode(
                            query = "misskey",
                            search = "search",
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testPlainSearchJapanese() {
        val tokenizer = Tokenizer()
        val content = "misskey 検索"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)

        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        SearchNode(
                            query = "misskey",
                            search = "検索",
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testBoldTag() {
        val tokenizer = Tokenizer()
        val content = "<b>test</b>"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)

        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        BoldNode(
                            start = 0,
                            content =
                                arrayListOf(
                                    TextNode(
                                        content = "test",
                                    ),
                                ),
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testItalicTag() {
        val tokenizer = Tokenizer()
        val content = "<i>test</i>"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)

        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        ItalicNode(
                            start = 0,
                            content =
                                arrayListOf(
                                    TextNode(
                                        content = "test",
                                    ),
                                ),
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testStrikeTag() {
        val tokenizer = Tokenizer()
        val content = "<s>test</s>"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)

        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        StrikeNode(
                            start = 0,
                            content =
                                arrayListOf(
                                    TextNode(
                                        content = "test",
                                    ),
                                ),
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testSmallTag() {
        val tokenizer = Tokenizer()
        val content = "<small>test</small>"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)

        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        SmallNode(
                            start = 0,
                            content =
                                arrayListOf(
                                    TextNode(
                                        content = "test",
                                    ),
                                ),
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testCenterTag() {
        val tokenizer = Tokenizer()
        val content = "<center>test</center>"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)

        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        CenterNode(
                            start = 0,
                            content =
                                arrayListOf(
                                    TextNode(
                                        content = "test",
                                    ),
                                ),
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testNonTag() {
        val tokenizer = Tokenizer()
        val content = "<test>test</test>"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)

        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        TextNode(content = "<test>test</test>"),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testNonTag2() {
        val tokenizer = Tokenizer()
        val content = "<b>test<small</center>"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)

        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        TextNode(content = "<b>test<small</center>"),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testMixed() {
        val tokenizer = Tokenizer()
        val content =
            "test **test** *test* ~~test~~ `test` [test](https://test.com) \$[test] \$[test](https://test.com) #test @test@host \$test"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)

        val expected =
            RootNode(
                content =
                    arrayListOf(
                        TextNode(content = "test "),
                        BoldNode(
                            start = 5,
                            content =
                                arrayListOf(
                                    TextNode(content = "test"),
                                ),
                        ),
                        TextNode(content = " "),
                        ItalicNode(
                            start = 14,
                            content =
                                arrayListOf(
                                    TextNode(content = "test"),
                                ),
                        ),
                        TextNode(content = " "),
                        StrikeNode(
                            start = 21,
                            content =
                                arrayListOf(
                                    TextNode(content = "test"),
                                ),
                        ),
                        TextNode(content = " "),
                        InlineCodeNode(code = "test"),
                        TextNode(content = " "),
                        LinkNode(
                            content = arrayListOf(TextNode("test")),
                            url = "https://test.com",
                            silent = false,
                        ),
                        TextNode(content = " \$[test] \$[test]("),
                        UrlNode(url = "https://test.com"),
                        TextNode(content = ") "),
                        HashtagNode(tag = "test"),
                        TextNode(content = " "),
                        MentionNode(
                            userName = "test",
                            host = "host",
                        ),
                        TextNode(content = " "),
                        CashNode(content = "test"),
                    ),
            )

        assertEquals(expected, builderResult)
    }

    @Test
    fun testMixed2() {
        val tokenizer = Tokenizer()
        val content = "\$[flip eefewfe~~fds<b>afd</b>f]~~"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                0,
                arrayListOf(
                    FnNode(
                        start = 0,
                        name = "flip",
                        content =
                            arrayListOf(
                                TextNode(content = "eefewfe~~fds<b>afd</b>f"),
                            ),
                    ),
                    TextNode(content = "~~"),
                ),
            ),
            builderResult,
        )
    }

    @Test
    fun testMixed3() {
        val tokenizer = Tokenizer()
        val content = "~~\$[flip.h,v Misskeyで\$[flip.h,v MisskeyでFediverseの世界が広がります]]~~"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        val expected =
            RootNode(
                0,
                arrayListOf(
                    StrikeNode(
                        start = 0,
                        content =
                            arrayListOf(
                                FnNode(
                                    start = 2,
                                    name = "flip",
                                    content =
                                        arrayListOf(
                                            TextNode(content = "Misskeyで"),
                                            FnNode(
                                                start = 21,
                                                name = "flip",
                                                content =
                                                    arrayListOf(
                                                        TextNode(content = "MisskeyでFediverseの世界が広がります"),
                                                    ),
                                                args = hashMapOf("h" to "true", "v" to "true"),
                                            ),
                                        ),
                                    args = hashMapOf("h" to "true", "v" to "true"),
                                ),
                            ),
                    ),
                ),
            )
        assertEquals(expected, builderResult)
    }

    @Test
    fun testSearch2() {
        val tokenizer = Tokenizer()
        val content = "111111111111\nfewfew few few [Search]"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        val expected =
            RootNode(
                0,
                arrayListOf(
                    TextNode(content = "111111111111\n"),
                    SearchNode(
                        query = "fewfew few few",
                        search = "[Search]",
                    ),
                ),
            )
        assertEquals(expected, builderResult)
    }

    @Test
    fun testQuote2() {
        val tokenizer = Tokenizer()
        val content = "> haha!\nwo!"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        val expected =
            RootNode(
                0,
                arrayListOf(
                    QuoteNode(
                        start = 0,
                        content =
                            arrayListOf(
                                TextNode(content = "haha!"),
                            ),
                    ),
                    TextNode(content = "\nwo!"),
                ),
            )
        assertEquals(expected, builderResult)
    }

    @Test
    fun testQuoteOnlyAtLineStart() {
        val tokenizer = Tokenizer()
        val content = "a->b"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        val expected =
            RootNode(
                0,
                arrayListOf(
                    TextNode(content = "a->b"),
                ),
            )
        assertEquals(expected, builderResult)
    }

    @Test
    fun testPlain() {
        val tokenizer = Tokenizer()
        val content = "<plain>**bold** @user $[x2 test]</plain>"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        val expected =
            RootNode(
                0,
                arrayListOf(
                    TextNode(content = "**bold** @user $[x2 test]", plain = true),
                ),
            )
        assertEquals(expected, builderResult)
    }

    @Test
    fun testMixed5() {
        val tokenizer = Tokenizer()
        val content = "?[:mikan_muite_agemasyoune:りしちか](https://misskey.io/@Lysitka)さんに勝ちました♪"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        val expected =
            RootNode(
                0,
                arrayListOf(
                    LinkNode(
                        content =
                            arrayListOf(
                                EmojiCodeNode("mikan_muite_agemasyoune"),
                                TextNode("りしちか"),
                            ),
                        url = "https://misskey.io/@Lysitka",
                        silent = true,
                    ),
                    TextNode(content = "さんに勝ちました♪"),
                ),
            )
        assertEquals(expected, builderResult)
    }

    @Test
    fun testMixed6() {
        val tokenizer = Tokenizer()
        val content =
            "<center>:role_nyanpuppu:$[border.width=2,color=0000 $[border.radius=4,width=0 $[bg.color=00385C $[position.x=1.5,y=1 $[jump.speed=20s $[twitch.speed=30s $[scale.x=2,y=2 $[flip :meowbongopeak:]]]]]$[position.x=-.9 $[border.width=0 **$[position.x=-.6,y=.1 #にゃんぷっぷー同盟]**]]$[position.x=-1.2 $[border.width=0 $[position.y=1 $[flip $[spin.speed=1s,alternate $[flip $[spin.speed=1s,alternate,delay=.01s $[position.y=-1 :blobcatmeltlove:]]]]]]]]]]]:blobcat_mudamudamuda::dododododo::dododododo::dododododo::dododododo::resonyance::tuuti_hakai::ga::hoshii:あと:5000t_5000tyouen::5000t_hosii:</center>"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
    }

    @Test
    fun testMixed7() {
        val tokenizer = Tokenizer()
        val content =
            """
            バリくそ極小絵描き

            もうひとつの俺@setsna




            きりたん→@setsna@kiritan.work
            https://knoow.jp/@/setsna
            $[border.width=2,radius=40,color=83a5b9 $[fg.color=f5b2b2 $[bg.color=dcdcdc $[scale.x=0.8,y=0.8 ?[$[fg.color=f5b2b2 ガキ貿易大臣]](https://msk.kitazawa.me/@setuna)]]]] $[border.width=2,radius=40,color=10b5x9 $[fg.color=f5b2b2 $[bg.color=dcdcdc $[scale.x=0.8,y=0.8 ?[$[fg.color=f5 千葉のガキ使い見習い]](https://msk.kitazawa.me/@setuna)]]]] $[border.width=2,radius=40,color=93a0b0 $[fg.color=f5b2b2 $[bg.color=ddcdc $[scale.x=0.8,y=0.8 ?[$[fg.color=c2b2f2 マスカットキャラクター:character_muscat:]](https://msk.kitazawa.me/@setuna)]]]]
            """.trimIndent()
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
    }

    @Test
    fun testSpaceInLink() {
        val tokenizer = Tokenizer()
        val content = "[test link](https://test.com)"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        LinkNode(
                            content = arrayListOf(TextNode("test link")),
                            url = "https://test.com",
                            silent = false,
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testEmojiOnlyMode() {
        val tokenizer = Tokenizer(emojiOnly = true)
        val content = "test :emoji: with [link in :emoji:](https://test.com)"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        TextNode("test "),
                        EmojiCodeNode("emoji"),
                        TextNode(" with [link in "),
                        EmojiCodeNode("emoji"),
                        TextNode("](https://test.com)"),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testHashtagMultiline() {
        val tokenizer = Tokenizer()
        val content = "#test　#test\n#test #test"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        HashtagNode(tag = "test"),
                        TextNode("　"),
                        HashtagNode(tag = "test"),
                        TextNode("\n"),
                        HashtagNode(tag = "test"),
                        TextNode(" "),
                        HashtagNode(tag = "test"),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testText1() {
        val tokenizer = Tokenizer()
        val content =
            """
            <center>#きょうのにゃんぷっぷー は
            $[tada.speed=0s $[tada.speed=0s :blobcat:]]
            タグ：ブロブキャット, catblob
            にゃぷあつめ率：0.48% https://misskey.io/play/9pcmdcebfyat037j</center>
            """.trimIndent()
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        CenterNode(
                            start = 0,
                            content =
                                arrayListOf(
                                    HashtagNode(tag = "きょうのにゃんぷっぷー"),
                                    TextNode(" は\n"),
                                    FnNode(
                                        start = 23,
                                        name = "tada",
                                        content =
                                            arrayListOf(
                                                FnNode(
                                                    start = 39,
                                                    name = "tada",
                                                    content = arrayListOf(EmojiCodeNode("blobcat")),
                                                    args = hashMapOf("speed" to "0s"),
                                                ),
                                            ),
                                        args = hashMapOf("speed" to "0s"),
                                    ),
                                    TextNode("\nタグ：ブロブキャット, catblob\nにゃぷあつめ率：0.48% "),
                                    UrlNode("https://misskey.io/play/9pcmdcebfyat037j"),
                                ),
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testLink2() {
        val tokenizer = Tokenizer()
        val content = "[[test link]](https://test.com)"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        LinkNode(
                            content = arrayListOf(TextNode("[test link]")),
                            url = "https://test.com",
                            silent = false,
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testLinkLabelDoesNotParseMention() {
        val tokenizer = Tokenizer()
        val content = "[@alice](https://test.com)"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        LinkNode(
                            content = arrayListOf(TextNode("@alice")),
                            url = "https://test.com",
                            silent = false,
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testLinkLabelDoesNotParseUrl() {
        val tokenizer = Tokenizer()
        val content = "[https://example.com/@alice](https://test.com)"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        LinkNode(
                            content = arrayListOf(TextNode("https://example.com/@alice")),
                            url = "https://test.com",
                            silent = false,
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testLinkLabelDoesNotParseNestedLink() {
        val tokenizer = Tokenizer()
        val content = "[[inner](https://inner.test)](https://outer.test)"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        LinkNode(
                            content = arrayListOf(TextNode("[inner](https://inner.test)")),
                            url = "https://outer.test",
                            silent = false,
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testComplexMfm() {
        val tokenizer = Tokenizer()
        val content =
            "オープンワールドRPG『:genshin:』の総合チャンネルです。\n" +
                "キャラクター、ストーリー、育成、アプデ等々、原神に関連するものであれば何でも投稿を歓迎しております！\n" +
                "<center>\$[x2 \$[scale.x=5 \$[scale.x=5 ―]]]</center>\n\n" +
                "<center>**このチャンネルのルール**</center>\n\n" +
                "・公式が許可していない話題は禁止とします\n" +
                "<small>リーク、MOD、プラベ鯖を含め[原神利用規約](http://t.ly/Yrb7Q)に抵触する話題等</small>\n" +
                "・一部の内容は「内容を隠す」機能の使用を推奨します\n" +
                "<small>最新のネタバレやNSFW、センシティブなものを含む</small>\n" +
                "・実装済みの範囲内でネタバレを含む可能性があります\n" +
                "・**ほんわかレス推奨です！**\n" +
                "<center>\$[x2 \$[scale.x=5 \$[scale.x=5 ―]]]</center>\n" +
                "<small>最終更新 :\$[unixtime 1773656919.3219297]</small>\n" +
                "<center>\$[x2 :blank:]**ゲームタイマー**\$[x2 :blank:]</center>\n" +
                "[\$[unixtime 1773676800]HoYoLabログボ更新](https://is.gd/cB406l)\n" +
                "\$[unixtime 1773691200]デイリー更新\n" +
                "\$[unixtime 1774209600]ウィークリー更新\n" +
                "<center>\$[x2 \$[scale.x=5 \$[scale.x=5 ―]]]</center>\n" +
                "公式SNS：[:unicode_1d54f_bg_black:](http://is.gd/rRlhOa)｜[:youtube:](http://is.gd/g3wqTi)｜[:twitch:<small>[EN]</small>](http://is.gd/sQII2q)\n" +
                "便利リンク\n" +
                "┣[公式HP](http://t.ly/GlOB5)\n" +
                "┣[マップ](http://t.ly/E0AR-)\n" +
                "┣[パイモン](http://t.ly/MS3NK)\n" +
                "┗[Discord](http://t.ly/xld6t)\n\n" +
                "外部ツール\n" +
                "┣[祈願カウンター](http://paimon.moe/wish)\n" +
                "┣[ビルドランキング](http://akasha.cv)\n" +
                "┗[聖遺物計算](http://is.gd/VAtEEU)\n\n" +
                "管理者：@ms"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content = arrayListOf(
TextNode(content = """オープンワールドRPG『"""),
EmojiCodeNode("""genshin"""),
TextNode(content = """』の総合チャンネルです。
キャラクター、ストーリー、育成、アプデ等々、原神に関連するものであれば何でも投稿を歓迎しております！
"""),
CenterNode(
                        start = 85,
                        content = arrayListOf(
FnNode(
                                start = 93,
                                name = "x2",
                                args = hashMapOf(),
                                content = arrayListOf(
FnNode(
                                        start = 98,
                                        name = "scale",
                                        args = hashMapOf("x" to "5"),
                                        content = arrayListOf(
FnNode(
                                                start = 110,
                                                name = "scale",
                                                args = hashMapOf("x" to "5"),
                                                content = arrayListOf(
TextNode(content = """―""")
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    ),
TextNode(content = """

"""),
CenterNode(
                        start = 137,
                        content = arrayListOf(
BoldNode(
                                start = 145,
                                content = arrayListOf(
TextNode(content = """このチャンネルのルール""")
                                )
                            )
                        )
                    ),
TextNode(content = """

・公式が許可していない話題は禁止とします
"""),
SmallNode(
                        start = 192,
                        content = arrayListOf(
TextNode(content = """リーク、MOD、プラベ鯖を含め"""),
LinkNode(
                                url = "http://t.ly/Yrb7Q",
                                silent = false,
                                content = arrayListOf(
TextNode(content = """原神利用規約""")
                                )
                            ),
TextNode(content = """に抵触する話題等""")
                        )
                    ),
TextNode(content = """
・一部の内容は「内容を隠す」機能の使用を推奨します
"""),
SmallNode(
                        start = 284,
                        content = arrayListOf(
TextNode(content = """最新のネタバレやNSFW、センシティブなものを含む""")
                        )
                    ),
TextNode(content = """
・実装済みの範囲内でネタバレを含む可能性があります
・"""),
BoldNode(
                        start = 352,
                        content = arrayListOf(
TextNode(content = """ほんわかレス推奨です！""")
                        )
                    ),
TextNode(content = """
"""),
CenterNode(
                        start = 368,
                        content = arrayListOf(
FnNode(
                                start = 376,
                                name = "x2",
                                args = hashMapOf(),
                                content = arrayListOf(
FnNode(
                                        start = 381,
                                        name = "scale",
                                        args = hashMapOf("x" to "5"),
                                        content = arrayListOf(
FnNode(
                                                start = 393,
                                                name = "scale",
                                                args = hashMapOf("x" to "5"),
                                                content = arrayListOf(
TextNode(content = """―""")
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    ),
TextNode(content = """
"""),
SmallNode(
                        start = 419,
                        content = arrayListOf(
TextNode(content = """最終更新 :"""),
FnNode(
                                start = 432,
                                name = "unixtime",
                                args = hashMapOf(),
                                content = arrayListOf(
TextNode(content = """1773656919.3219297""")
                                )
                            )
                        )
                    ),
TextNode(content = """
"""),
CenterNode(
                        start = 471,
                        content = arrayListOf(
FnNode(
                                start = 479,
                                name = "x2",
                                args = hashMapOf(),
                                content = arrayListOf(
EmojiCodeNode("""blank""")
                                )
                            ),
BoldNode(
                                start = 492,
                                content = arrayListOf(
TextNode(content = """ゲームタイマー""")
                                )
                            ),
FnNode(
                                start = 503,
                                name = "x2",
                                args = hashMapOf(),
                                content = arrayListOf(
EmojiCodeNode("""blank""")
                                )
                            )
                        )
                    ),
TextNode(content = """
"""),
LinkNode(
                        url = "https://is.gd/cB406l",
                        silent = false,
                        content = arrayListOf(
FnNode(
                                start = 0,
                                name = "unixtime",
                                args = hashMapOf(),
                                content = arrayListOf(
TextNode(content = """1773676800""")
                                )
                            ),
TextNode(content = """HoYoLabログボ更新""")
                        )
                    ),
TextNode(content = """
"""),
FnNode(
                        start = 585,
                        name = "unixtime",
                        args = hashMapOf(),
                        content = arrayListOf(
TextNode(content = """1773691200""")
                        )
                    ),
TextNode(content = """デイリー更新
"""),
FnNode(
                        start = 614,
                        name = "unixtime",
                        args = hashMapOf(),
                        content = arrayListOf(
TextNode(content = """1774209600""")
                        )
                    ),
TextNode(content = """ウィークリー更新
"""),
CenterNode(
                        start = 645,
                        content = arrayListOf(
FnNode(
                                start = 653,
                                name = "x2",
                                args = hashMapOf(),
                                content = arrayListOf(
FnNode(
                                        start = 658,
                                        name = "scale",
                                        args = hashMapOf("x" to "5"),
                                        content = arrayListOf(
FnNode(
                                                start = 670,
                                                name = "scale",
                                                args = hashMapOf("x" to "5"),
                                                content = arrayListOf(
TextNode(content = """―""")
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    ),
TextNode(content = """
公式SNS："""),
LinkNode(
                        url = "http://is.gd/rRlhOa",
                        silent = false,
                        content = arrayListOf(
EmojiCodeNode("""unicode_1d54f_bg_black""")
                        )
                    ),
TextNode(content = """｜"""),
LinkNode(
                        url = "http://is.gd/g3wqTi",
                        silent = false,
                        content = arrayListOf(
EmojiCodeNode("""youtube""")
                        )
                    ),
TextNode(content = """｜"""),
LinkNode(
                        url = "http://is.gd/sQII2q",
                        silent = false,
                        content = arrayListOf(
EmojiCodeNode("""twitch"""),
TextNode(content = """<small>[EN]</small>""")
                        )
                    ),
TextNode(content = """
便利リンク
┣"""),
LinkNode(
                        url = "http://t.ly/GlOB5",
                        silent = false,
                        content = arrayListOf(
TextNode(content = """公式HP""")
                        )
                    ),
TextNode(content = """
┣"""),
LinkNode(
                        url = "http://t.ly/E0AR-",
                        silent = false,
                        content = arrayListOf(
TextNode(content = """マップ""")
                        )
                    ),
TextNode(content = """
┣"""),
LinkNode(
                        url = "http://t.ly/MS3NK",
                        silent = false,
                        content = arrayListOf(
TextNode(content = """パイモン""")
                        )
                    ),
TextNode(content = """
┗"""),
LinkNode(
                        url = "http://t.ly/xld6t",
                        silent = false,
                        content = arrayListOf(
TextNode(content = """Discord""")
                        )
                    ),
TextNode(content = """

外部ツール
┣"""),
LinkNode(
                        url = "http://paimon.moe/wish",
                        silent = false,
                        content = arrayListOf(
TextNode(content = """祈願カウンター""")
                        )
                    ),
TextNode(content = """
┣"""),
LinkNode(
                        url = "http://akasha.cv",
                        silent = false,
                        content = arrayListOf(
TextNode(content = """ビルドランキング""")
                        )
                    ),
TextNode(content = """
┗"""),
LinkNode(
                        url = "http://is.gd/VAtEEU",
                        silent = false,
                        content = arrayListOf(
TextNode(content = """聖遺物計算""")
                        )
                    ),
TextNode(content = """

管理者："""),
MentionNode(
                        userName = "ms",
    host = null
                    )
                )
            ),
            builderResult,
        )
    }

    @Test
    fun testLink3() {
        val tokenizer = Tokenizer()
        val content = "[[test link](https://test.com)"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        LinkNode(
                            content = arrayListOf(TextNode("[test link")),
                            url = "https://test.com",
                            silent = false,
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testLinkWithCenterTag() {
        val tokenizer = Tokenizer()
        val content = "[<center>haha!</center>](https://www.google.com)"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        LinkNode(
                            content = arrayListOf(TextNode("<center>haha!</center>")),
                            url = "https://www.google.com",
                            silent = false,
                        ),
                    ),
            ),
            builderResult,
        )
    }

    @Test
    fun testCenterOnlyAtLineStartEnd() {
        val tokenizer = Tokenizer()
        val content = "123<center>abc</center>213123"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content =
                    arrayListOf(
                        TextNode(content = "123<center>abc</center>213123"),
                    ),
            ),
            builderResult,
        )
    }
}
