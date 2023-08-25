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
                content = arrayListOf(
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
                content = arrayListOf(
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
                content = arrayListOf(
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
    fun testHashTag() {
        val tokenizer = Tokenizer()
        val content = "#test"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content = arrayListOf(
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
                content = arrayListOf(
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
                content = arrayListOf(
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
                content = arrayListOf(
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
        val content = "```kotlin\ntest```"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content = arrayListOf(
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
    fun testAsteriskBold() {
        val tokenizer = Tokenizer()
        val content = "**test**"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)
        assertEquals(
            RootNode(
                content = arrayListOf(
                    BoldNode(
                        start = 0,
                        content = arrayListOf(
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
                content = arrayListOf(
                    ItalicNode(
                        start = 0,
                        content = arrayListOf(
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
                content = arrayListOf(
                    BoldNode(
                        start = 0,
                        content = arrayListOf(
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
                content = arrayListOf(
                    ItalicNode(
                        start = 0,
                        content = arrayListOf(
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
                content = arrayListOf(
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
                content = arrayListOf(
                    QuoteNode(
                        start = 0,
                        content = arrayListOf(
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
    fun testLink() {
        val tokenizer = Tokenizer()
        val content = "[test](https://test.com)"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)

        assertEquals(
            RootNode(
                content = arrayListOf(
                    LinkNode(
                        content = "test",
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
                content = arrayListOf(
                    LinkNode(
                        content = "test",
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
                content = arrayListOf(
                    UrlNode(
                        url = "https://test.com",
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
                content = arrayListOf(
                    FnNode(
                        start = 0,
                        name = "flip.h,v",
                        content = arrayListOf(TextNode(content = "MisskeyでFediverseの世界が広がります")),
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
                content = arrayListOf(
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
                content = arrayListOf(
                    StrikeNode(
                        start = 0,
                        content = arrayListOf(
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
                content = arrayListOf(
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
    fun testBoldTag() {
        val tokenizer = Tokenizer()
        val content = "<b>test</b>"
        val result = tokenizer.parse(StringReader(content))
        val builder = TreeBuilder()
        val builderResult = builder.build(StringReader(content), result)

        assertEquals(
            RootNode(
                content = arrayListOf(
                    BoldNode(
                        start = 0,
                        content = arrayListOf(
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
                content = arrayListOf(
                    ItalicNode(
                        start = 0,
                        content = arrayListOf(
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
                content = arrayListOf(
                    StrikeNode(
                        start = 0,
                        content = arrayListOf(
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
                content = arrayListOf(
                    SmallNode(
                        start = 0,
                        content = arrayListOf(
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
                content = arrayListOf(
                    CenterNode(
                        start = 0,
                        content = arrayListOf(
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
                content = arrayListOf(
                    TextNode(content = "<test>"),
                    TextNode(content = "test"),
                    TextNode(content = "</test>"),
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
                content = arrayListOf(
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

        val expected = RootNode(
            content = arrayListOf(
                TextNode(content = "test "),
                BoldNode(
                    start = 5,
                    content = arrayListOf(
                        TextNode(content = "test"),
                    ),
                ),
                TextNode(content = " "),
                ItalicNode(
                    start = 14,
                    content = arrayListOf(
                        TextNode(content = "test"),
                    ),
                ),
                TextNode(content = " "),
                StrikeNode(
                    start = 21,
                    content = arrayListOf(
                        TextNode(content = "test"),
                    ),
                ),
                TextNode(content = " "),
                InlineCodeNode(code = "test"),
                TextNode(content = " "),
                LinkNode(
                    content = "test",
                    url = "https://test.com",
                    silent = false,
                ),
                TextNode(content = " \$[test"),
                TextNode(content = "]"),
                TextNode(content = " \$[test"),
                TextNode(content = "]"),
                TextNode(content = "("),
                UrlNode(url = "https://test.com)"),
                TextNode(content = " "),
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
                        content = arrayListOf(
                            TextNode(content = "eefewfe"),
                            TextNode(content = "~~fds<b>afd</b>f"),
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
        val expected = RootNode(
            0,
            arrayListOf(
                StrikeNode(
                    start = 0,
                    content = arrayListOf(
                        FnNode(
                            start = 2,
                            name = "flip.h,v",
                            content = arrayListOf(
                                TextNode(content = "Misskeyで"),
                                FnNode(
                                    start = 21,
                                    name = "flip.h,v",
                                    content = arrayListOf(
                                        TextNode(content = "MisskeyでFediverseの世界が広がります"),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )
        assertEquals(expected, builderResult)
    }
}
