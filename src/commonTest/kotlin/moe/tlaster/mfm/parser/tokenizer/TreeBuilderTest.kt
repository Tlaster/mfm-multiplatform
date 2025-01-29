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
        val content = "```kotlin\ntest```"
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
                        content =
                            arrayListOf(
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
                                    name = "flip.h,v",
                                    content =
                                        arrayListOf(
                                            TextNode(content = "Misskeyで"),
                                            FnNode(
                                                start = 21,
                                                name = "flip.h,v",
                                                content =
                                                    arrayListOf(
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
                    TextNode(content = "111111111111"),
                    TextNode("\n"),
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
                    TextNode("\n"),
                    TextNode(content = "wo!"),
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
}
