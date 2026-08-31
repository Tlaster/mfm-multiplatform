package moe.tlaster.mfm.parser

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
import moe.tlaster.mfm.parser.tree.Node
import moe.tlaster.mfm.parser.tree.QuoteNode
import moe.tlaster.mfm.parser.tree.RootNode
import moe.tlaster.mfm.parser.tree.SearchNode
import moe.tlaster.mfm.parser.tree.SmallNode
import moe.tlaster.mfm.parser.tree.StrikeNode
import moe.tlaster.mfm.parser.tree.TextNode
import moe.tlaster.mfm.parser.tree.UnicodeEmojiNode
import moe.tlaster.mfm.parser.tree.UrlNode
import kotlin.test.Test
import kotlin.test.assertEquals

class UpstreamParserCompatibilityTest {
    private sealed interface PNode

    private data class PText(
        val text: String,
    ) : PNode

    private data class PCenter(
        val children: List<PNode>,
    ) : PNode

    private data class PFn(
        val name: String,
        val args: Map<String, Any>,
        val children: List<PNode>,
    ) : PNode

    private data class PUniEmoji(
        val emoji: String,
    ) : PNode

    private data class PMention(
        val username: String,
        val host: String?,
        val acct: String,
    ) : PNode

    private data class PEmojiCode(
        val name: String,
    ) : PNode

    private data class PHashtag(
        val hashtag: String,
    ) : PNode

    private data class PUrl(
        val url: String,
        val brackets: Boolean = false,
    ) : PNode

    private data class PBold(
        val children: List<PNode>,
    ) : PNode

    private data class PSmall(
        val children: List<PNode>,
    ) : PNode

    private data class PItalic(
        val children: List<PNode>,
    ) : PNode

    private data class PStrike(
        val children: List<PNode>,
    ) : PNode

    private data class PQuote(
        val children: List<PNode>,
    ) : PNode

    private data class PMathBlock(
        val formula: String,
    ) : PNode

    private data class PSearch(
        val query: String,
        val content: String,
    ) : PNode

    private data class PCodeBlock(
        val code: String,
        val lang: String?,
    ) : PNode

    private data class PLink(
        val silent: Boolean,
        val url: String,
        val children: List<PNode>,
    ) : PNode

    private data class PInlineCode(
        val code: String,
    ) : PNode

    private data class PMathInline(
        val formula: String,
    ) : PNode

    private data class PPlain(
        val text: String,
    ) : PNode

    private fun TEXT(text: String): PNode = PText(text)

    private fun CENTER(children: List<PNode>): PNode = PCenter(children)

    private fun FN(
        name: String,
        args: Map<String, Any> = emptyMap(),
        children: List<PNode>,
    ): PNode = PFn(name, args, children)

    private fun UNI_EMOJI(emoji: String): PNode = PUniEmoji(emoji)

    private fun MENTION(
        username: String,
        host: String?,
        acct: String,
    ): PNode = PMention(username, host, acct)

    private fun EMOJI_CODE(name: String): PNode = PEmojiCode(name)

    private fun HASHTAG(hashtag: String): PNode = PHashtag(hashtag)

    private fun N_URL(
        url: String,
        brackets: Boolean = false,
    ): PNode = PUrl(url, brackets)

    private fun BOLD(children: List<PNode>): PNode = PBold(children)

    private fun SMALL(children: List<PNode>): PNode = PSmall(children)

    private fun ITALIC(children: List<PNode>): PNode = PItalic(children)

    private fun STRIKE(children: List<PNode>): PNode = PStrike(children)

    private fun QUOTE(children: List<PNode>): PNode = PQuote(children)

    private fun MATH_BLOCK(formula: String): PNode = PMathBlock(formula)

    private fun SEARCH(
        query: String,
        content: String,
    ): PNode = PSearch(query, content)

    private fun CODE_BLOCK(
        code: String,
        lang: String?,
    ): PNode = PCodeBlock(code, lang)

    private fun LINK(
        silent: Boolean,
        url: String,
        children: List<PNode>,
    ): PNode = PLink(silent, url, children)

    private fun INLINE_CODE(code: String): PNode = PInlineCode(code)

    private fun MATH_INLINE(formula: String): PNode = PMathInline(formula)

    private fun PLAIN(text: String): PNode = PPlain(text)

    private fun parseSimple(input: String): List<PNode> = normalize(MFMParser(emojiOnly = true).parse(input).content)

    private fun parseFull(input: String): List<PNode> = normalize(MFMParser().parse(input).content)

    private fun parseFull(
        input: String,
        nestLimit: Int,
    ): List<PNode> = normalize(MFMParser(nestLimit = nestLimit).parse(input).content)

    private fun normalize(nodes: List<Node>): List<PNode> {
        val result = arrayListOf<PNode>()
        for (node in nodes) {
            val normalized =
                when (node) {
                    is TextNode -> if (node.plain) PLAIN(node.content) else TEXT(node.content)
                    is EmojiCodeNode -> EMOJI_CODE(node.emoji)
                    is UnicodeEmojiNode -> UNI_EMOJI(node.emoji)
                    is MentionNode -> MENTION(node.userName, node.host, node.acct)
                    is HashtagNode -> HASHTAG(node.tag)
                    is UrlNode -> N_URL(node.url, node.brackets)
                    is InlineCodeNode -> INLINE_CODE(node.code)
                    is MathInlineNode -> MATH_INLINE(node.formula)
                    is CashNode -> TEXT("\$${node.content}")
                    is BoldNode -> BOLD(normalize(node.content))
                    is SmallNode -> SMALL(normalize(node.content))
                    is ItalicNode -> ITALIC(normalize(node.content))
                    is StrikeNode -> STRIKE(normalize(node.content))
                    is LinkNode -> LINK(node.silent, node.url, normalize(node.content))
                    is FnNode -> FN(node.name, node.typedArgs, normalize(node.content))
                    is QuoteNode -> QUOTE(normalize(node.content))
                    is SearchNode -> SEARCH(node.query, node.content)
                    is CodeBlockNode -> CODE_BLOCK(node.code, node.language)
                    is MathBlockNode -> MATH_BLOCK(node.formula)
                    is CenterNode -> CENTER(normalize(node.content))
                    is RootNode -> error("Unexpected nested root node")
                }
            appendMerged(result, normalized)
        }
        return result
    }

    private fun appendMerged(
        nodes: MutableList<PNode>,
        node: PNode,
    ) {
        val previous = nodes.lastOrNull()
        if (previous is PText && node is PText) {
            nodes[nodes.lastIndex] = TEXT(previous.text + node.text)
        } else {
            nodes.add(node)
        }
    }

    @Test
    fun simpleParserTextBasic() {
        assertEquals(listOf(TEXT("abc")), parseSimple("abc"))
    }

    @Test
    fun simpleParserIgnoreHashtag() {
        assertEquals(listOf(TEXT("abc#abc")), parseSimple("abc#abc"))
    }

    @Test
    fun simpleParserKeycapNumberSign() {
        assertEquals(listOf(TEXT("abc"), UNI_EMOJI("#️⃣"), TEXT("abc")), parseSimple("abc#️⃣abc"))
    }

    @Test
    fun simpleParserEmojiBasic() {
        assertEquals(listOf(EMOJI_CODE("foo")), parseSimple(":foo:"))
    }

    @Test
    fun simpleParserEmojiBetweenTexts() {
        assertEquals(listOf(TEXT("foo:bar:baz")), parseSimple("foo:bar:baz"))
        assertEquals(listOf(TEXT("12:34:56")), parseSimple("12:34:56"))
        assertEquals(listOf(TEXT("あ"), EMOJI_CODE("bar"), TEXT("い")), parseSimple("あ:bar:い"))
    }

    @Test
    fun simpleParserIgnoreVariationSelector() {
        assertEquals(listOf(TEXT("\uFE0F")), parseSimple("\uFE0F"))
    }

    @Test
    fun simpleParserDisallowOtherSyntaxes() {
        assertEquals(listOf(TEXT("foo **bar** baz")), parseSimple("foo **bar** baz"))
    }

    @Test
    fun simpleParserPlainSuppressesEmojiParsing() {
        assertEquals(listOf(PLAIN(":foo:")), parseSimple("<plain>:foo:</plain>"))
        assertEquals(listOf(PLAIN("😇")), parseSimple("<plain>😇</plain>"))
        assertEquals(listOf(TEXT("<plain></plain>")), parseSimple("<plain></plain>"))
        assertEquals(listOf(TEXT("<plain>abc")), parseSimple("<plain>abc"))
    }

    @Test
    fun fullParserTextBasic() {
        assertEquals(listOf(TEXT("abc")), parseFull("abc"))
    }

    @Test
    fun fullParserQuoteCases() {
        assertEquals(listOf(QUOTE(listOf(TEXT("abc")))), parseFull("> abc"))
        assertEquals(listOf(QUOTE(listOf(TEXT("abc\n123")))), parseFull("\n> abc\n> 123\n"))
        assertEquals(
            listOf(
                QUOTE(
                    listOf(
                        CENTER(
                            listOf(TEXT("a")),
                        ),
                    ),
                ),
            ),
            parseFull("\n> <center>\n> a\n> </center>\n"),
        )
        assertEquals(
            listOf(
                QUOTE(
                    listOf(
                        CENTER(
                            listOf(
                                TEXT("I'm "),
                                MENTION("ai", null, "@ai"),
                                TEXT(", An bot of misskey!"),
                            ),
                        ),
                    ),
                ),
            ),
            parseFull("\n> <center>\n> I'm @ai, An bot of misskey!\n> </center>\n"),
        )
        assertEquals(listOf(QUOTE(listOf(TEXT("abc\n\n123")))), parseFull("\n> abc\n>\n> 123\n"))
        assertEquals(listOf(TEXT("> ")), parseFull("> "))
        assertEquals(
            listOf(
                QUOTE(listOf(TEXT("foo\nbar"))),
                TEXT("hoge"),
            ),
            parseFull("\n> foo\n> bar\n\nhoge"),
        )
        assertEquals(
            listOf(
                QUOTE(listOf(TEXT("foo"))),
                QUOTE(listOf(TEXT("bar"))),
                TEXT("hoge"),
            ),
            parseFull("\n> foo\n\n> bar\n\nhoge"),
        )
    }

    @Test
    fun fullParserSearchCases() {
        assertEquals(listOf(SEARCH("MFM 書き方 123", "MFM 書き方 123 Search")), parseFull("MFM 書き方 123 Search"))
        assertEquals(listOf(SEARCH("MFM 書き方 123", "MFM 書き方 123 [Search]")), parseFull("MFM 書き方 123 [Search]"))
        assertEquals(listOf(SEARCH("MFM 書き方 123", "MFM 書き方 123 search")), parseFull("MFM 書き方 123 search"))
        assertEquals(listOf(SEARCH("MFM 書き方 123", "MFM 書き方 123 [search]")), parseFull("MFM 書き方 123 [search]"))
        assertEquals(listOf(SEARCH("MFM 書き方 123", "MFM 書き方 123 検索")), parseFull("MFM 書き方 123 検索"))
        assertEquals(listOf(SEARCH("MFM 書き方 123", "MFM 書き方 123 [検索]")), parseFull("MFM 書き方 123 [検索]"))
        assertEquals(listOf(SEARCH("query", "query\tSearch")), parseFull("query\tSearch"))
        assertEquals(
            listOf(
                TEXT("abc"),
                SEARCH("hoge piyo bebeyo", "hoge piyo bebeyo 検索"),
                TEXT("123"),
            ),
            parseFull("abc\nhoge piyo bebeyo 検索\n123"),
        )
    }

    @Test
    fun fullParserCodeBlockCases() {
        assertEquals(listOf(CODE_BLOCK("abc", null)), parseFull("```\nabc\n```"))
        assertEquals(listOf(CODE_BLOCK("a\nb\nc", null)), parseFull("```\na\nb\nc\n```"))
        assertEquals(listOf(CODE_BLOCK("const a = 1;", "js")), parseFull("```js\nconst a = 1;\n```"))
        assertEquals(
            listOf(
                TEXT("abc"),
                CODE_BLOCK("const abc = 1;", null),
                TEXT("123"),
            ),
            parseFull("abc\n```\nconst abc = 1;\n```\n123"),
        )
        assertEquals(listOf(CODE_BLOCK("aaa```bbb", null)), parseFull("```\naaa```bbb\n```"))
        assertEquals(listOf(CODE_BLOCK("foo", null), TEXT("bar")), parseFull("```\nfoo\n```\nbar"))
    }

    @Test
    fun fullParserMathBlockCases() {
        assertEquals(listOf(MATH_BLOCK("math1")), parseFull("\\[math1\\]"))
        assertEquals(
            listOf(
                TEXT("abc"),
                MATH_BLOCK("math1"),
                TEXT("123"),
            ),
            parseFull("abc\n\\[math1\\]\n123"),
        )
        assertEquals(listOf(TEXT("\\[aaa\\]after")), parseFull("\\[aaa\\]after"))
        assertEquals(listOf(TEXT("before\\[aaa\\]")), parseFull("before\\[aaa\\]"))
    }

    @Test
    fun fullParserCenterCases() {
        assertEquals(listOf(CENTER(listOf(TEXT("abc")))), parseFull("<center>abc</center>"))
        assertEquals(
            listOf(
                TEXT("before"),
                CENTER(listOf(TEXT("abc\n123\n\npiyo"))),
                TEXT("after"),
            ),
            parseFull("before\n<center>\nabc\n123\n\npiyo\n</center>\nafter"),
        )
        assertEquals(
            listOf(TEXT("before"), CENTER(listOf(TEXT("abc"))), TEXT("after")),
            parseFull("before\r\n<center>abc</center>\r\nafter"),
        )
        assertEquals(
            listOf(TEXT("before"), CENTER(listOf(TEXT("abc"))), TEXT("after")),
            parseFull("before\r\n<center>\r\nabc\r\n</center>\r\nafter"),
        )
    }

    @Test
    fun fullParserEmojiCodeBasic() {
        assertEquals(listOf(EMOJI_CODE("abc")), parseFull(":abc:"))
    }

    @Test
    fun fullParserUnicodeEmojiCases() {
        assertEquals(listOf(TEXT("今起きた"), UNI_EMOJI("😇")), parseFull("今起きた😇"))
        assertEquals(listOf(TEXT("abc"), UNI_EMOJI("#️⃣"), TEXT("123")), parseFull("abc#️⃣123"))
        assertEquals(listOf(TEXT("𠮷𝄞")), parseFull("𠮷𝄞"))
        assertEquals(listOf(UNI_EMOJI("☀️"), UNI_EMOJI("❤️"), UNI_EMOJI("©️")), parseFull("☀️❤️©️"))
        assertEquals(listOf(UNI_EMOJI("1️⃣"), UNI_EMOJI("*️⃣")), parseFull("1️⃣*️⃣"))
        assertEquals(listOf(UNI_EMOJI("👨‍👩‍👧‍👦")), parseFull("👨‍👩‍👧‍👦"))
        assertEquals(listOf(UNI_EMOJI("😇"), UNI_EMOJI("🙂")), parseFull("😇🙂"))
        for (component in listOf("🏻", "🏼", "🏽", "🏾", "🏿", "🦰", "🦱", "🦲", "🦳")) {
            assertEquals(listOf(UNI_EMOJI(component)), parseFull(component))
            assertEquals(listOf(UNI_EMOJI(component), TEXT("\uFE0E")), parseFull("$component\uFE0E"))
            assertEquals(listOf(UNI_EMOJI(component), TEXT("\uFE0F")), parseFull("$component\uFE0F"))
        }
    }

    @Test
    fun fullParserBigCases() {
        assertEquals(listOf(FN("tada", emptyMap(), listOf(TEXT("abc")))), parseFull("***abc***"))
        assertEquals(
            listOf(
                FN(
                    "tada",
                    emptyMap(),
                    listOf(
                        TEXT("123"),
                        BOLD(listOf(TEXT("abc"))),
                        TEXT("123"),
                    ),
                ),
            ),
            parseFull("***123**abc**123***"),
        )
        assertEquals(
            listOf(
                FN(
                    "tada",
                    emptyMap(),
                    listOf(
                        TEXT("123\n"),
                        BOLD(listOf(TEXT("abc"))),
                        TEXT("\n123"),
                    ),
                ),
            ),
            parseFull("***123\n**abc**\n123***"),
        )
    }

    @Test
    fun fullParserBoldCases() {
        assertEquals(listOf(BOLD(listOf(TEXT("abc")))), parseFull("<b>abc</b>"))
        assertEquals(
            listOf(
                BOLD(
                    listOf(
                        TEXT("123"),
                        STRIKE(listOf(TEXT("abc"))),
                        TEXT("123"),
                    ),
                ),
            ),
            parseFull("<b>123~~abc~~123</b>"),
        )
        assertEquals(
            listOf(
                BOLD(
                    listOf(
                        TEXT("123\n"),
                        STRIKE(listOf(TEXT("abc"))),
                        TEXT("\n123"),
                    ),
                ),
            ),
            parseFull("<b>123\n~~abc~~\n123</b>"),
        )
        assertEquals(listOf(BOLD(listOf(TEXT("abc")))), parseFull("**abc**"))
        assertEquals(
            listOf(
                BOLD(
                    listOf(
                        TEXT("123"),
                        STRIKE(listOf(TEXT("abc"))),
                        TEXT("123"),
                    ),
                ),
            ),
            parseFull("**123~~abc~~123**"),
        )
        assertEquals(
            listOf(
                BOLD(
                    listOf(
                        TEXT("123\n"),
                        STRIKE(listOf(TEXT("abc"))),
                        TEXT("\n123"),
                    ),
                ),
            ),
            parseFull("**123\n~~abc~~\n123**"),
        )
        assertEquals(listOf(TEXT("<b></b>")), parseFull("<b></b>"))
        assertEquals(listOf(TEXT("****")), parseFull("****"))
    }

    @Test
    fun fullParserSmallCases() {
        assertEquals(listOf(SMALL(listOf(TEXT("abc")))), parseFull("<small>abc</small>"))
        assertEquals(
            listOf(
                SMALL(
                    listOf(
                        TEXT("abc"),
                        BOLD(listOf(TEXT("123"))),
                        TEXT("abc"),
                    ),
                ),
            ),
            parseFull("<small>abc**123**abc</small>"),
        )
        assertEquals(
            listOf(
                SMALL(
                    listOf(
                        TEXT("abc\n"),
                        BOLD(listOf(TEXT("123"))),
                        TEXT("\nabc"),
                    ),
                ),
            ),
            parseFull("<small>abc\n**123**\nabc</small>"),
        )
    }

    @Test
    fun fullParserItalicCases() {
        assertEquals(listOf(ITALIC(listOf(TEXT("abc")))), parseFull("<i>abc</i>"))
        assertEquals(
            listOf(
                ITALIC(
                    listOf(
                        TEXT("abc"),
                        BOLD(listOf(TEXT("123"))),
                        TEXT("abc"),
                    ),
                ),
            ),
            parseFull("<i>abc**123**abc</i>"),
        )
        assertEquals(
            listOf(
                ITALIC(
                    listOf(
                        TEXT("abc\n"),
                        BOLD(listOf(TEXT("123"))),
                        TEXT("\nabc"),
                    ),
                ),
            ),
            parseFull("<i>abc\n**123**\nabc</i>"),
        )
        assertEquals(listOf(ITALIC(listOf(TEXT("abc")))), parseFull("*abc*"))
        assertEquals(
            listOf(
                TEXT("before "),
                ITALIC(listOf(TEXT("abc"))),
                TEXT(" after"),
            ),
            parseFull("before *abc* after"),
        )
        assertEquals(listOf(TEXT("before*abc*after")), parseFull("before*abc*after"))
        assertEquals(
            listOf(
                TEXT("あいう"),
                ITALIC(listOf(TEXT("abc"))),
                TEXT("えお"),
            ),
            parseFull("あいう*abc*えお"),
        )
        assertEquals(listOf(ITALIC(listOf(TEXT("abc")))), parseFull("_abc_"))
        assertEquals(
            listOf(
                TEXT("before "),
                ITALIC(listOf(TEXT("abc"))),
                TEXT(" after"),
            ),
            parseFull("before _abc_ after"),
        )
        assertEquals(listOf(TEXT("before_abc_after")), parseFull("before_abc_after"))
        assertEquals(
            listOf(
                TEXT("あいう"),
                ITALIC(listOf(TEXT("abc"))),
                TEXT("えお"),
            ),
            parseFull("あいう_abc_えお"),
        )
        assertEquals(listOf(ITALIC(listOf(TEXT("abc"))), TEXT("a")), parseFull("*abc*a"))
        assertEquals(listOf(TEXT("__a\nb__")), parseFull("__a\nb__"))
    }

    @Test
    fun fullParserStrikeCases() {
        assertEquals(listOf(STRIKE(listOf(TEXT("foo")))), parseFull("<s>foo</s>"))
        assertEquals(listOf(STRIKE(listOf(TEXT("foo")))), parseFull("~~foo~~"))
        assertEquals(listOf(TEXT("~~a\nb~~")), parseFull("~~a\nb~~"))
        assertEquals(listOf(STRIKE(listOf(TEXT("a~b")))), parseFull("~~a~b~~"))
        assertEquals(listOf(TEXT("<s></s>")), parseFull("<s></s>"))
        assertEquals(listOf(TEXT("~~~~")), parseFull("~~~~"))
    }

    @Test
    fun fullParserInlineCodeCases() {
        assertEquals(listOf(INLINE_CODE("var x = \"Strawberry Pasta\";")), parseFull("`var x = \"Strawberry Pasta\";`"))
        assertEquals(listOf(TEXT("`foo\nbar`")), parseFull("`foo\nbar`"))
        assertEquals(listOf(TEXT("`foo´bar`")), parseFull("`foo´bar`"))
        assertEquals(listOf(INLINE_CODE("日本語")), parseFull("`日本語`"))
        assertEquals(listOf(INLINE_CODE("😇")), parseFull("`😇`"))
    }

    @Test
    fun fullParserMathInlineBasic() {
        assertEquals(
            listOf(MATH_INLINE("x = {-b \\pm \\sqrt{b^2-4ac} \\over 2a}")),
            parseFull("\\(x = {-b \\pm \\sqrt{b^2-4ac} \\over 2a}\\)"),
        )
    }

    @Test
    fun fullParserMentionCases() {
        assertEquals(listOf(MENTION("abc", null, "@abc")), parseFull("@abc"))
        assertEquals(listOf(TEXT("before "), MENTION("abc", null, "@abc"), TEXT(" after")), parseFull("before @abc after"))
        assertEquals(listOf(MENTION("abc", "misskey.io", "@abc@misskey.io")), parseFull("@abc@misskey.io"))
        assertEquals(
            listOf(
                TEXT("before "),
                MENTION("abc", "misskey.io", "@abc@misskey.io"),
                TEXT(" after"),
            ),
            parseFull("before @abc@misskey.io after"),
        )
        assertEquals(
            listOf(
                TEXT("before\n"),
                MENTION("abc", "misskey.io", "@abc@misskey.io"),
                TEXT("\nafter"),
            ),
            parseFull("before\n@abc@misskey.io\nafter"),
        )
        assertEquals(listOf(TEXT("abc@example.com")), parseFull("abc@example.com"))
        assertEquals(listOf(TEXT("あいう"), MENTION("abc", null, "@abc")), parseFull("あいう@abc"))
        assertEquals(listOf(TEXT("@-")), parseFull("@-"))
        assertEquals(listOf(TEXT("@abc@.")), parseFull("@abc@."))
        assertEquals(listOf(MENTION("abc-d", null, "@abc-d")), parseFull("@abc-d"))
        assertEquals(
            listOf(MENTION("bsky.brid.gy", "bsky.brid.gy", "@bsky.brid.gy@bsky.brid.gy")),
            parseFull("@bsky.brid.gy@bsky.brid.gy"),
        )
        assertEquals(listOf(TEXT("@-abc")), parseFull("@-abc"))
        assertEquals(listOf(MENTION("abc", null, "@abc"), TEXT("-")), parseFull("@abc-"))
        assertEquals(listOf(TEXT("@.abc")), parseFull("@.abc"))
        assertEquals(listOf(MENTION("abc", null, "@abc"), TEXT(".")), parseFull("@abc."))
        assertEquals(listOf(TEXT("@abc@.aaa")), parseFull("@abc@.aaa"))
        assertEquals(listOf(MENTION("abc", "aaa", "@abc@aaa"), TEXT(".")), parseFull("@abc@aaa."))
        assertEquals(listOf(TEXT("@abc@-aaa")), parseFull("@abc@-aaa"))
        assertEquals(listOf(MENTION("abc", "aaa", "@abc@aaa"), TEXT("-")), parseFull("@abc@aaa-"))
    }

    @Test
    fun fullParserHashtagCases() {
        assertEquals(listOf(HASHTAG("abc")), parseFull("#abc"))
        assertEquals(listOf(TEXT("before "), HASHTAG("abc"), TEXT(" after")), parseFull("before #abc after"))
        assertEquals(listOf(UNI_EMOJI("#️⃣"), TEXT("abc123 "), HASHTAG("abc")), parseFull("#️⃣abc123 #abc"))
        assertEquals(listOf(TEXT("abc\n"), UNI_EMOJI("#️⃣"), TEXT("abc")), parseFull("abc\n#️⃣abc"))
        assertEquals(listOf(TEXT("abc#abc")), parseFull("abc#abc"))
        assertEquals(listOf(TEXT("あいう"), HASHTAG("abc")), parseFull("あいう#abc"))
        assertEquals(listOf(TEXT("Foo "), HASHTAG("bar"), TEXT(", baz "), HASHTAG("piyo"), TEXT(".")), parseFull("Foo #bar, baz #piyo."))
        assertEquals(listOf(HASHTAG("Foo"), TEXT("!")), parseFull("#Foo!"))
        assertEquals(listOf(HASHTAG("Foo"), TEXT(":")), parseFull("#Foo:"))
        assertEquals(listOf(HASHTAG("Foo"), TEXT("'")), parseFull("#Foo'"))
        assertEquals(listOf(HASHTAG("Foo"), TEXT("\"")), parseFull("#Foo\""))
        assertEquals(listOf(HASHTAG("Foo"), TEXT("]")), parseFull("#Foo]"))
        assertEquals(listOf(HASHTAG("foo"), TEXT("/bar")), parseFull("#foo/bar"))
        assertEquals(listOf(HASHTAG("foo"), TEXT("<bar>")), parseFull("#foo<bar>"))
        assertEquals(listOf(HASHTAG("foo123")), parseFull("#foo123"))
        assertEquals(listOf(TEXT("("), HASHTAG("foo"), TEXT(")")), parseFull("(#foo)"))
        assertEquals(listOf(TEXT("「"), HASHTAG("foo"), TEXT("」")), parseFull("「#foo」"))
        assertEquals(listOf(TEXT("「"), HASHTAG("foo(bar)"), TEXT("」")), parseFull("「#foo(bar)」"))
        assertEquals(listOf(TEXT("(bar "), HASHTAG("foo"), TEXT(")")), parseFull("(bar #foo)"))
        assertEquals(listOf(TEXT("「bar "), HASHTAG("foo"), TEXT("」")), parseFull("「bar #foo」"))
        assertEquals(listOf(TEXT("#123")), parseFull("#123"))
        assertEquals(listOf(TEXT("(#123)")), parseFull("(#123)"))
        assertEquals(listOf(HASHTAG("foo(bar)baz")), parseFull("#foo(bar)baz"))
        assertEquals(listOf(HASHTAG("foo(a(b)c)d")), parseFull("#foo(a(b)c)d"))
        assertEquals(listOf(TEXT("["), HASHTAG("abc"), TEXT("]")), parseFull("[#abc]"))
    }

    @Test
    fun fullParserUrlCases() {
        assertEquals(listOf(N_URL("https://misskey.io/@ai")), parseFull("https://misskey.io/@ai"))
        assertEquals(
            listOf(
                TEXT("official instance: "),
                N_URL("https://misskey.io/@ai"),
                TEXT("."),
            ),
            parseFull("official instance: https://misskey.io/@ai."),
        )
        assertEquals(listOf(N_URL("https://misskey.io/@ai"), TEXT(".")), parseFull("https://misskey.io/@ai."))
        assertEquals(listOf(TEXT("https://.")), parseFull("https://."))
        assertEquals(listOf(N_URL("https://misskey.io/@ai"), TEXT("...")), parseFull("https://misskey.io/@ai..."))
        assertEquals(listOf(N_URL("https://example.com/foo?bar=a,b")), parseFull("https://example.com/foo?bar=a,b"))
        assertEquals(listOf(N_URL("https://example.com/foo"), TEXT(", bar")), parseFull("https://example.com/foo, bar"))
        assertEquals(listOf(N_URL("https://example.com/foo(bar)")), parseFull("https://example.com/foo(bar)"))
        assertEquals(listOf(TEXT("("), N_URL("https://example.com/foo"), TEXT(")")), parseFull("(https://example.com/foo)"))
        assertEquals(listOf(TEXT("(foo "), N_URL("https://example.com/foo"), TEXT(")")), parseFull("(foo https://example.com/foo)"))
        assertEquals(listOf(TEXT("("), N_URL("https://example.com/foo(bar)"), TEXT(")")), parseFull("(https://example.com/foo(bar))"))
        assertEquals(listOf(TEXT("foo ["), N_URL("https://example.com/foo"), TEXT("] bar")), parseFull("foo [https://example.com/foo] bar"))
        assertEquals(listOf(TEXT("https://大石泉すき.example.com")), parseFull("https://大石泉すき.example.com"))
        assertEquals(listOf(N_URL("https://大石泉すき.example.com", true)), parseFull("<https://大石泉すき.example.com>"))
        assertEquals(listOf(TEXT("javascript:foo")), parseFull("javascript:foo"))
    }

    @Test
    fun fullParserUrlSpecialCharacterCases() {
        val nestedUrl =
            "https://bsky.brid.gy/r/https://bsky.app/profile/" +
                "did:plc:e5utgfbui36qwd7qg74n6rgg/post/3mtyxnfgd4s2j"
        val plusUrl = "https://example.com/?campaign=We+warned+this+could+happen&done=true"
        val parenthesizedPathUrl = "https://www.cell.com/current-biology/fulltext/S0960-9822(26)00948-6"
        val tildeUrl = "https://vote.debian.org/~secretary/gr_ltm/results.txt"
        val portAndPathColonUrl = "https://example.com:8080/a:b"
        val nestedParenthesesUrl = "https://example.com/a(b(c)d)e"
        val squareBracketsUrl = "https://example.com/a[b]c"

        assertEquals(listOf(N_URL(nestedUrl)), parseFull(nestedUrl))
        assertEquals(listOf(N_URL(plusUrl)), parseFull(plusUrl))
        assertEquals(listOf(N_URL(parenthesizedPathUrl)), parseFull(parenthesizedPathUrl))
        assertEquals(listOf(N_URL(tildeUrl)), parseFull(tildeUrl))
        assertEquals(listOf(N_URL(portAndPathColonUrl)), parseFull(portAndPathColonUrl))
        assertEquals(listOf(N_URL(nestedParenthesesUrl)), parseFull(nestedParenthesesUrl))
        assertEquals(listOf(N_URL(squareBracketsUrl)), parseFull(squareBracketsUrl))
        assertEquals(
            listOf(N_URL("https://example.com/a"), TEXT("(b")),
            parseFull("https://example.com/a(b"),
        )
        assertEquals(listOf(N_URL("http://localhost")), parseFull("http://localhost"))
        assertEquals(listOf(N_URL("https://example.com/a%20b")), parseFull("https://example.com/a%20b"))
        assertEquals(listOf(N_URL("https://example.com", brackets = true)), parseFull("<https://example.com>"))
        assertEquals(
            listOf(N_URL("https://example.com"), TEXT("「outside」")),
            parseFull("https://example.com「outside」"),
        )
    }

    @Test
    fun fullParserLinkCases() {
        assertEquals(
            listOf(
                LINK(false, "https://misskey.io/@ai", listOf(TEXT("official instance"))),
                TEXT("."),
            ),
            parseFull("[official instance](https://misskey.io/@ai)."),
        )
        assertEquals(
            listOf(
                LINK(true, "https://misskey.io/@ai", listOf(TEXT("official instance"))),
                TEXT("."),
            ),
            parseFull("?[official instance](https://misskey.io/@ai)."),
        )
        assertEquals(
            listOf(
                LINK(false, "https://misskey.io/@ai", listOf(TEXT("official instance"))),
                TEXT("."),
            ),
            parseFull("[official instance](<https://misskey.io/@ai>)."),
        )
        assertEquals(listOf(TEXT("[click here](javascript:foo)")), parseFull("[click here](javascript:foo)"))
        assertEquals(
            listOf(
                TEXT("official instance: "),
                LINK(false, "https://misskey.io/@ai", listOf(TEXT("https://misskey.io/@ai"))),
                TEXT("."),
            ),
            parseFull("official instance: [https://misskey.io/@ai](https://misskey.io/@ai)."),
        )
        assertEquals(
            listOf(
                TEXT("official instance: "),
                LINK(
                    false,
                    "https://misskey.io/@ai",
                    listOf(
                        TEXT("https://misskey.io/@ai"),
                        BOLD(listOf(TEXT("https://misskey.io/@ai"))),
                    ),
                ),
                TEXT("."),
            ),
            parseFull("official instance: [https://misskey.io/@ai**https://misskey.io/@ai**](https://misskey.io/@ai)."),
        )
        assertEquals(
            listOf(
                TEXT("official instance: "),
                LINK(false, "https://misskey.io/@ai", listOf(TEXT("[https://misskey.io/@ai"))),
                TEXT("]("),
                N_URL("https://misskey.io/@ai"),
                TEXT(")."),
            ),
            parseFull("official instance: [[https://misskey.io/@ai](https://misskey.io/@ai)](https://misskey.io/@ai)."),
        )
        assertEquals(
            listOf(
                TEXT("official instance: "),
                LINK(
                    false,
                    "https://misskey.io/@ai",
                    listOf(
                        BOLD(listOf(TEXT("[https://misskey.io/@ai](https://misskey.io/@ai)"))),
                    ),
                ),
                TEXT("."),
            ),
            parseFull("official instance: [**[https://misskey.io/@ai](https://misskey.io/@ai)**](https://misskey.io/@ai)."),
        )
        assertEquals(listOf(LINK(false, "https://example.com", listOf(TEXT("@example")))), parseFull("[@example](https://example.com)"))
        assertEquals(
            listOf(
                LINK(
                    false,
                    "https://example.com",
                    listOf(
                        TEXT("@example"),
                        BOLD(listOf(TEXT("@example"))),
                    ),
                ),
            ),
            parseFull("[@example**@example**](https://example.com)"),
        )
        assertEquals(
            listOf(LINK(false, "https://example.com/foo(bar)", listOf(TEXT("foo")))),
            parseFull("[foo](https://example.com/foo(bar))"),
        )
        assertEquals(
            listOf(
                TEXT("("),
                LINK(false, "https://example.com/foo(bar)", listOf(TEXT("foo"))),
                TEXT(")"),
            ),
            parseFull("([foo](https://example.com/foo(bar)))"),
        )
        assertEquals(
            listOf(
                TEXT("[test] foo "),
                LINK(false, "https://example.com", listOf(TEXT("bar"))),
            ),
            parseFull("[test] foo [bar](https://example.com)"),
        )
        assertEquals(listOf(TEXT("[test](http://..)")), parseFull("[test](http://..)"))
        assertEquals(listOf(LINK(false, "http://localhost", listOf(TEXT("x")))), parseFull("[x](http://localhost)"))
        assertEquals(
            listOf(LINK(false, "https://example.com/a%20b", listOf(TEXT("x")))),
            parseFull("[x](https://example.com/a%20b)"),
        )
        assertEquals(listOf(TEXT("["), BOLD(listOf(TEXT("bold"))), TEXT("]")), parseFull("[**bold**]"))
    }

    @Test
    fun fullParserFnCases() {
        assertEquals(listOf(FN("tada", emptyMap(), listOf(TEXT("abc")))), parseFull("\$[tada abc]"))
        assertEquals(listOf(FN("spin", mapOf("speed" to "1.1s"), listOf(TEXT("a")))), parseFull("\$[spin.speed=1.1s a]"))
        assertEquals(listOf(FN("position", mapOf("x" to "-3"), listOf(TEXT("a")))), parseFull("\$[position.x=-3 a]"))
        assertEquals(listOf(TEXT("\$[関数 text]")), parseFull("\$[関数 text]"))
        assertEquals(
            listOf(
                FN(
                    "spin",
                    mapOf("speed" to "1.1s"),
                    listOf(
                        FN("shake", emptyMap(), listOf(TEXT("a"))),
                    ),
                ),
            ),
            parseFull("\$[spin.speed=1.1s \$[shake a]]"),
        )
        assertEquals(listOf(FN("spin", mapOf("left" to true), listOf(TEXT("x")))), parseFull("\$[spin.left x]"))
        assertEquals(
            listOf(FN("flip", mapOf("h" to true, "v" to true), listOf(TEXT("x")))),
            parseFull("\$[flip.h,v x]"),
        )
        assertEquals(
            listOf(FN("x", mapOf("a" to "1", "b" to "2"), listOf(TEXT("c")))),
            parseFull("\$[x.a=1,b=2 c]"),
        )
        assertEquals(listOf(TEXT("\$tag")), parseFull("\$tag"))
        assertEquals(listOf(TEXT("\$[shake ]")), parseFull("\$[shake ]"))
        assertEquals(listOf(TEXT("\$[foo-bar x]")), parseFull("\$[foo-bar x]"))
        assertEquals(listOf(TEXT("\$[shake\tx]")), parseFull("\$[shake\tx]"))
    }

    @Test
    fun fullParserPlainCases() {
        assertEquals(
            listOf(
                TEXT("a\n"),
                PLAIN("**Hello**\nworld"),
                TEXT("\nb"),
            ),
            parseFull("a\n<plain>\n**Hello**\nworld\n</plain>\nb"),
        )
        assertEquals(
            listOf(
                TEXT("a\n"),
                PLAIN("**Hello** world"),
                TEXT("\nb"),
            ),
            parseFull("a\n<plain>**Hello** world</plain>\nb"),
        )
    }

    @Test
    fun nestingLimitMatchesUpstream() {
        assertEquals(
            listOf(QUOTE(listOf(QUOTE(listOf(TEXT("> abc")))))),
            parseFull(">>> abc", nestLimit = 2),
        )
        assertEquals(
            listOf(BOLD(listOf(BOLD(listOf(TEXT("***abc***")))))),
            parseFull("<b><b>***abc***</b></b>", nestLimit = 2),
        )
        assertEquals(
            listOf(ITALIC(listOf(ITALIC(listOf(TEXT("<b>abc</b>")))))),
            parseFull("<i><i><b>abc</b></i></i>", nestLimit = 2),
        )
        assertEquals(
            listOf(BOLD(listOf(HASHTAG("abc"), TEXT("(x(y)z)")))),
            parseFull("<b>#abc(x(y)z)</b>", nestLimit = 2),
        )
        assertEquals(
            listOf(BOLD(listOf(N_URL("https://example.com/abc"), TEXT("(x(y)z)")))),
            parseFull("<b>https://example.com/abc(x(y)z)</b>", nestLimit = 2),
        )
        assertEquals(
            listOf(BOLD(listOf(BOLD(listOf(TEXT("\$[a b]")))))),
            parseFull("<b><b>\$[a b]</b></b>", nestLimit = 2),
        )
    }

    @Test
    fun fullParserComposite() {
        val input =
            """
            before
            <center>
            Hello ${'$'}[tada everynyan! 🎉]
            
            I'm @ai, A bot of misskey!
            
            https://github.com/syuilo/ai
            </center>
            after
            """.trimIndent()
        assertEquals(
            listOf(
                TEXT("before"),
                CENTER(
                    listOf(
                        TEXT("Hello "),
                        FN("tada", emptyMap(), listOf(TEXT("everynyan! "), UNI_EMOJI("🎉"))),
                        TEXT("\n\nI'm "),
                        MENTION("ai", null, "@ai"),
                        TEXT(", A bot of misskey!\n\n"),
                        N_URL("https://github.com/syuilo/ai"),
                    ),
                ),
                TEXT("after"),
            ),
            parseFull(input),
        )
    }
}
