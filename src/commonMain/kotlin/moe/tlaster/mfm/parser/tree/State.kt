package moe.tlaster.mfm.parser.tree

import moe.tlaster.mfm.parser.tokenizer.Reader
import moe.tlaster.mfm.parser.tokenizer.StringReader
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.AsteriskBold
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.AsteriskItalicStart
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.Blockquote
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.Bold
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.Cash
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.CashStart
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.Character
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.CodeBlock
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.CodeBlockLanguage
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.CodeBlockLanguageEnd
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.CodeBlockStart
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.EmojiName
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.EmojiNameStart
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.EndTagOpen
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.Eof
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.Fn
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.FnContent
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.HashTag
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.HashTagStart
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.InlineCode
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.InlineCodeStart
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.InlineMath
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.InlineMathContent
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.Italic
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.LineBreak
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.LinkClose
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.LinkContent
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.LinkHref
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.LinkHrefClose
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.LinkHrefOpen
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.LinkOpen
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.MathBlock
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.MathBlockContent
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.Search
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.SilentLink
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.Strike
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.Tag
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.TagClose
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.TagOpen
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.UnderscoreBoldStart
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.UnderscoreItalicStart
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.UnicodeEmoji
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.Url
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.UserAt
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.UserHost
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.UserName
import moe.tlaster.mfm.parser.tokenizer.Tokenizer

internal data class TreeBuilderContext(
    var currentContainer: ContainerNode,
    val tokenCharacterTypes: List<TokenCharacterType>,
    val reader: Reader,
) {
    val stack: ArrayList<ContainerNode> = arrayListOf()

    fun isInNewLine(
        start: Int,
        end: Int,
    ): Boolean {
        val previous = tokenCharacterTypes.getOrNull(start - 1)
        val next = tokenCharacterTypes.getOrNull(end + 1)
        return (previous == LineBreak || previous == null) && (next == LineBreak || next == null || next == Eof)
    }

    fun isCurrentClosingTag(tagName: String): Boolean {
        val closingTag = "</$tagName>"
        return reader.position + closingTag.length <= reader.length && reader.readAt(reader.position, closingTag.length) == closingTag
    }

    fun trimSurroundingSingleLineBreak(node: ContainerNode) {
        val first = node.content.firstOrNull()
        if (first is TextNode && first.content.startsWith("\n")) {
            val trimmed = first.content.removePrefix("\n")
            if (trimmed.isEmpty()) {
                node.content.removeAt(0)
            } else {
                node.content[0] = TextNode(trimmed)
            }
        }

        val lastIndex = node.content.lastIndex
        val last = node.content.getOrNull(lastIndex)
        if (last is TextNode && last.content.endsWith("\n")) {
            val trimmed = last.content.removeSuffix("\n")
            if (trimmed.isEmpty()) {
                node.content.removeAt(lastIndex)
            } else {
                node.content[lastIndex] = TextNode(trimmed)
            }
        }
    }

    inline fun <reified T : Node> endNode(start: Int) {
        val node = stack.findLast { it is T } as? Node
        if (node != null) {
            val index = stack.indexOf(node)
            if (index != stack.lastIndex) {
                // reject inner state
                val startIndex = stack[index + 1].start
                stack[index].content.apply {
                    val count = stack.size - index
                    repeat(count) {
                        remove(stack.removeLast())
                    }
                    add(TextNode(reader.readAt(startIndex, start - startIndex)))
                }
            } else {
                stack.remove(node)
            }
            currentContainer = stack.last()
        } else {
            currentContainer.content.add(TextNode(reader.readAt(start, 1)))
        }
    }
}

internal sealed interface State {
    fun TreeBuilderContext.build()
}

internal data object LineBreakState : State {
    override fun TreeBuilderContext.build() {
        val start = reader.position
        val isInQuote = stack.findLast { it is QuoteNode } != null

        val text = StringBuilder()
        while (reader.hasNext()) {
            val token = tokenCharacterTypes[reader.position]
            if (token == LineBreak) {
                text.append(reader.consume())
            } else {
                break
            }
        }

        if (isInQuote) {
            val next = tokenCharacterTypes.getOrNull(reader.position)
            if (next == Blockquote) {
                if (text.length > 1) {
                    endNode<QuoteNode>(start)
                    return
                }

                var lookahead = reader.position
                while (tokenCharacterTypes.getOrNull(lookahead) == Blockquote) {
                    lookahead++
                }

                val afterQuoteMarks = tokenCharacterTypes.getOrNull(lookahead)
                val isTrailingBlankQuoteLine =
                    afterQuoteMarks == LineBreak && tokenCharacterTypes.getOrNull(lookahead + 1) != Blockquote

                while (reader.hasNext() && tokenCharacterTypes[reader.position] == Blockquote) {
                    reader.consume()
                }

                if (isTrailingBlankQuoteLine) {
                    endNode<QuoteNode>(start)
                    return
                }

                currentContainer.content.add(TextNode(text.toString()))
                return
            }

            endNode<QuoteNode>(start)

            if (text.length > 1) {
                return
            }
        }

        currentContainer.content.add(TextNode(text.toString()))
    }
}

internal data object TextState : State {
    override fun TreeBuilderContext.build() {
        val text = StringBuilder()
        while (reader.hasNext()) {
            val token = tokenCharacterTypes[reader.position]
            if (token == Character || token == Bold || token == Italic || token == TokenCharacterType.UnKnown) {
                text.append(reader.consume())
            } else {
                break
            }
        }
        currentContainer.content.add(TextNode(text.toString()))
    }
}

internal data object EmojiNameState : State {
    override fun TreeBuilderContext.build() {
        val text = StringBuilder()
        while (reader.hasNext()) {
            val token = tokenCharacterTypes[reader.position]
            if (token == EmojiName) {
                text.append(reader.consume())
            } else if (token == EmojiNameStart) {
                // skip
                if (text.isEmpty()) {
                    reader.consume()
                } else {
                    reader.consume()
                    break
                }
            } else {
                break
            }
        }
        currentContainer.content.add(EmojiCodeNode(text.toString()))
    }
}

internal data object UnicodeEmojiState : State {
    override fun TreeBuilderContext.build() {
        val text = StringBuilder()
        while (reader.hasNext() && tokenCharacterTypes[reader.position] == UnicodeEmoji) {
            text.append(reader.consume())
        }
        currentContainer.content.add(UnicodeEmojiNode(text.toString()))
    }
}

internal data object HashTagState : State {
    override fun TreeBuilderContext.build() {
        val text = StringBuilder()
        while (reader.hasNext()) {
            val token = tokenCharacterTypes[reader.position]
            if (token == HashTag) {
                text.append(reader.consume())
            } else if (token == HashTagStart) {
                // skip
                reader.consume()
            } else {
                break
            }
        }
        val tag = text.toString()
        if (tag.all { it in '0'..'9' }) {
            currentContainer.content.add(TextNode("#$tag"))
        } else {
            currentContainer.content.add(HashtagNode(tag))
        }
    }
}

internal data object UserNameState : State {
    override fun TreeBuilderContext.build() {
        val text = StringBuilder()
        val host = StringBuilder()
        while (reader.hasNext()) {
            val token = tokenCharacterTypes[reader.position]
            if (token == UserName) {
                text.append(reader.consume())
            } else if (token == UserAt) {
                // skip
                reader.consume()
            } else if (token == UserHost) {
                host.append(reader.consume())
            } else {
                break
            }
        }
        currentContainer.content.add(MentionNode(text.toString(), host.takeIf { it.isNotEmpty() }?.toString()))
    }
}

internal data object CashState : State {
    override fun TreeBuilderContext.build() {
        val text = StringBuilder()
        while (reader.hasNext()) {
            val token = tokenCharacterTypes[reader.position]
            if (token == Cash) {
                text.append(reader.consume())
            } else if (token == CashStart) {
                // skip
                reader.consume()
            } else {
                break
            }
        }
        currentContainer.content.add(CashNode(text.toString()))
    }
}

internal data object InlineCodeState : State {
    override fun TreeBuilderContext.build() {
        val text = StringBuilder()
        while (reader.hasNext()) {
            val token = tokenCharacterTypes[reader.position]
            if (token == InlineCode) {
                text.append(reader.consume())
            } else if (token == InlineCodeStart) {
                // skip
                reader.consume()
            } else {
                break
            }
        }
        currentContainer.content.add(InlineCodeNode(text.toString()))
    }
}

internal data object CodeBlockState : State {
    override fun TreeBuilderContext.build() {
        val text = StringBuilder()
        val language = StringBuilder()
        while (reader.hasNext()) {
            val token = tokenCharacterTypes[reader.position]
            if (token == CodeBlock) {
                text.append(reader.consume())
            } else if (token == CodeBlockStart) {
                // skip
                reader.consume()
            } else if (token == CodeBlockLanguage) {
                language.append(reader.consume())
            } else if (token == CodeBlockLanguageEnd) {
                // skip
                reader.consume()
            } else {
                break
            }
        }
        if (text.isEmpty() && language.isNotEmpty()) {
            currentContainer.content.add(CodeBlockNode(language.toString().removeSuffix("\n"), null))
        } else {
            currentContainer.content.add(
                CodeBlockNode(
                    text.toString().removeSuffix("\n"),
                    language.toString().takeIf { it.isNotEmpty() },
                ),
            )
        }
    }
}

internal data object ItalicState : State {
    override fun TreeBuilderContext.build() {
        val start = reader.position
        stack.findLast { it is ItalicNode }?.let {
            val index = stack.indexOf(it)
            if (index != stack.lastIndex) {
                // reject inner state
                val startIndex = stack[index + 1].start
                stack[index + 1].content.add(TextNode(reader.readAt(startIndex, start - startIndex)))
            }
            val count = stack.size - index
            repeat(count) {
                stack.removeAt(index)
            }
            currentContainer = stack.last()
        } ?: run {
            val node = ItalicNode(start)
            currentContainer.content.add(node)
            stack.add(node)
            currentContainer = node
        }
        while (reader.hasNext()) {
            val token = tokenCharacterTypes[reader.position]
            if (token == AsteriskItalicStart || token == UnderscoreItalicStart) {
                reader.consume()
            } else {
                break
            }
        }
    }
}

internal data object BoldState : State {
    override fun TreeBuilderContext.build() {
        val start = reader.position
        stack.findLast { it is BoldNode }?.let {
            val index = stack.indexOf(it)
            if (index != stack.lastIndex) {
                // reject inner state
                val startIndex = stack[index + 1].start
                stack[index + 1].content.add(TextNode(reader.readAt(startIndex, start - startIndex)))
            }
            val count = stack.size - index
            repeat(count) {
                stack.removeAt(index)
            }
            currentContainer = stack.last()
        } ?: run {
            val node = BoldNode(start)
            currentContainer.content.add(node)
            stack.add(node)
            currentContainer = node
        }
        while (reader.hasNext()) {
            val token = tokenCharacterTypes[reader.position]
            if (token == AsteriskBold || token == UnderscoreBoldStart) {
                reader.consume()
            } else {
                break
            }
        }
    }
}

internal data object StrikeState : State {
    override fun TreeBuilderContext.build() {
        val start = reader.position
        stack.findLast { it is StrikeNode }?.let {
            val index = stack.indexOf(it)
            if (index != stack.lastIndex) {
                // reject inner state
                val startIndex = stack[index + 1].start
                stack[index + 1].content.add(TextNode(reader.readAt(startIndex, start - startIndex)))
            }
            val count = stack.size - index
            repeat(count) {
                stack.removeAt(index)
            }
            currentContainer = stack.last()
        } ?: run {
            val node = StrikeNode(start)
            currentContainer.content.add(node)
            stack.add(node)
            currentContainer = node
        }
        while (reader.hasNext()) {
            val token = tokenCharacterTypes[reader.position]
            if (token == Strike) {
                reader.consume()
            } else {
                break
            }
        }
    }
}

internal data object MathBlockState : State {
    override fun TreeBuilderContext.build() {
        val text = StringBuilder()
        val start = reader.position
        var end = reader.position
        while (reader.hasNext()) {
            val token = tokenCharacterTypes[reader.position]
            if (token == MathBlockContent) {
                text.append(reader.consume())
            } else if (token == MathBlock) {
                end = reader.position
                reader.consume()
            } else {
                break
            }
        }
        if (isInNewLine(start, end)) {
            currentContainer.content.add(MathBlockNode(text.toString()))
        } else {
            currentContainer.content.add(TextNode(reader.readAt(start, end - start + 1)))
        }
    }
}

internal data object InlineMathState : State {
    override fun TreeBuilderContext.build() {
        val text = StringBuilder()
        while (reader.hasNext()) {
            val token = tokenCharacterTypes[reader.position]
            if (token == InlineMathContent) {
                text.append(reader.consume())
            } else if (token == InlineMath) {
                // skip
                reader.consume()
            } else {
                break
            }
        }
        currentContainer.content.add(MathInlineNode(text.toString()))
    }
}

internal data object BlockquoteState : State {
    override fun TreeBuilderContext.build() {
        val start = reader.position
        val node = QuoteNode(start)
        currentContainer.content.add(node)
        currentContainer = node
        stack.add(node)
        while (reader.hasNext()) {
            val token = tokenCharacterTypes[reader.position]
            if (token == Blockquote) {
                reader.consume()
            } else {
                break
            }
        }
    }
}

internal data object TagState : State {
    override fun TreeBuilderContext.build() {
        val start = reader.position
        val name = StringBuilder()
        while (reader.hasNext()) {
            val token = tokenCharacterTypes[reader.position]
            if (token == Tag) {
                name.append(reader.consume())
            } else if (token == TagOpen) {
                // skip
                reader.consume()
            } else if (token == TagClose) {
                // skip
                reader.consume()
                break
            } else {
                break
            }
        }
        if (name.startsWith("http://") || name.startsWith("https://")) {
            currentContainer.content.add(UrlNode(name.toString()))
        } else {
            when (name.toString()) {
                "center" -> {
                    val prevToken = if (start > 0) tokenCharacterTypes[start - 1] else null
                    if (prevToken == LineBreak || prevToken == null || prevToken == Blockquote) {
                        val node = CenterNode(start)
                        currentContainer.content.add(node)
                        stack.add(node)
                        currentContainer = node
                    } else {
                        currentContainer.content.add(TextNode(reader.readAt(start, reader.position - start)))
                    }
                }
                "b" -> {
                    val node = BoldNode(start)
                    currentContainer.content.add(node)
                    stack.add(node)
                    currentContainer = node
                }
                "small" -> {
                    val node = SmallNode(start)
                    currentContainer.content.add(node)
                    stack.add(node)
                    currentContainer = node
                }
                "plain" -> {
                    val text = StringBuilder()
                    while (reader.hasNext() && tokenCharacterTypes[reader.position] != Eof && !isCurrentClosingTag("plain")) {
                        text.append(reader.consume())
                    }
                    var content = text.toString()
                    if (content.startsWith("\n")) {
                        content = content.drop(1)
                    }
                    if (content.endsWith("\n")) {
                        content = content.dropLast(1)
                    }
                    currentContainer.content.add(TextNode(content, plain = true))
                    if (isCurrentClosingTag("plain")) {
                        reader.consume("</plain>".length)
                    }
                }
                "i" -> {
                    val node = ItalicNode(start)
                    currentContainer.content.add(node)
                    stack.add(node)
                    currentContainer = node
                }
                "s" -> {
                    val node = StrikeNode(start)
                    currentContainer.content.add(node)
                    stack.add(node)
                    currentContainer = node
                }
                else -> {
                    currentContainer.content.add(TextNode(reader.readAt(start, reader.position - start)))
                }
            }
        }
    }
}

internal data object EndTagState : State {
    override fun TreeBuilderContext.build() {
        val start = reader.position
        val name = StringBuilder()
        while (reader.hasNext()) {
            val token = tokenCharacterTypes[reader.position]
            if (token == Tag) {
                name.append(reader.consume())
            } else if (token == EndTagOpen) {
                // skip
                reader.consume()
            } else if (token == TagClose) {
                // skip
                reader.consume()
                break
            } else {
                break
            }
        }

        when (name.toString()) {
            "center" -> {
                val nextToken = if (reader.hasNext()) tokenCharacterTypes[reader.position] else null
                if (nextToken == LineBreak || nextToken == null || nextToken == Eof) {
                    stack
                        .findLast { it is CenterNode }
                        ?.let { center ->
                            if (center.content.none { it is UrlNode }) {
                                trimSurroundingSingleLineBreak(center)
                            }
                        }
                    endNode<CenterNode>(start)
                } else {
                    currentContainer.content.add(TextNode(reader.readAt(start, reader.position - start)))
                }
            }
            "b" -> {
                endNode<BoldNode>(start)
            }
            "small" -> {
                endNode<SmallNode>(start)
            }
            "i" -> {
                endNode<ItalicNode>(start)
            }
            "s" -> {
                endNode<StrikeNode>(start)
            }
            else -> {
                currentContainer.content.add(TextNode(reader.readAt(start, reader.position - start)))
            }
        }
    }
}

internal data object SearchState : State {
    override fun TreeBuilderContext.build() {
        val start = reader.position
        val content = StringBuilder()
        while (reader.hasNext()) {
            val token = tokenCharacterTypes[reader.position]
            if (token == Search) {
                content.append(reader.consume())
            } else {
                break
            }
        }
        val prev = currentContainer.content.getOrNull(currentContainer.content.lastIndex)
        val prevPrev = currentContainer.content.getOrNull(currentContainer.content.lastIndex - 1)
        val next = tokenCharacterTypes.getOrNull(reader.position)
        if ((prevPrev == TextNode("\n") || prevPrev == null) && (next == LineBreak || next == null || next == Eof) && prev is TextNode) {
            val query = prev.content.trim()
            currentContainer.content.remove(prev)
            currentContainer.content.add(SearchNode(query, content.toString()))
        } else {
            currentContainer.content.add(TextNode(reader.readAt(start, reader.position - start)))
        }
    }
}

internal data object LinkState : State {
    override fun TreeBuilderContext.build() {
        val content = StringBuilder()
        val href = StringBuilder()
        var silent = false
        while (reader.hasNext()) {
            val token = tokenCharacterTypes[reader.position]
            if (token == LinkContent) {
                content.append(reader.consume())
            } else if (token == LinkHrefOpen) {
                // skip
                reader.consume()
            } else if (token == LinkHref) {
                href.append(reader.consume())
            } else if (token == LinkHrefClose || token == LinkOpen) {
                // skip
                reader.consume()
            } else if (token == SilentLink) {
                silent = true
                reader.consume()
            } else if (token == LinkClose) {
                reader.consume()
            } else {
                break
            }
        }
        val tokenizer = Tokenizer(emojiOnly = false)
        val reader = StringReader(content.toString())
        val tokens =
            tokenizer
                .parse(reader)
                .map { token ->
                    when (token) {
                        UserAt,
                        UserName,
                        UserHost,
                        Url,
                        LinkOpen,
                        LinkClose,
                        LinkContent,
                        LinkHrefOpen,
                        LinkHrefClose,
                        LinkHref,
                        SilentLink,
                        TagOpen,
                        EndTagOpen,
                        Tag,
                        TagClose,
                        -> Character
                        else -> token
                    }
                }
        reader.reset()
        val node = TreeBuilder().build(reader, tokens)
        mergeAdjacentTextNodes(node)
        val rawHref = href.toString()
        val normalizedHref =
            decodePercentEncodedUrl(
                if (rawHref.startsWith("<") && rawHref.endsWith(">") && rawHref.length >= 2) {
                    rawHref.substring(1, rawHref.length - 1)
                } else {
                    rawHref
                },
            )
        if (!isSupportedLinkHref(normalizedHref)) {
            val prefix = if (silent) "?[" else "["
            currentContainer.content.add(TextNode("$prefix$content]($rawHref)"))
        } else {
            currentContainer.content.add(LinkNode(node.content, normalizedHref, silent))
        }
    }
}

internal fun mergeAdjacentTextNodes(container: ContainerNode) {
    val merged = arrayListOf<Node>()
    for (node in container.content) {
        if (node is ContainerNode) {
            mergeAdjacentTextNodes(node)
        }
        val previous = merged.lastOrNull()
        if (previous is TextNode && node is TextNode && previous.plain == node.plain) {
            merged[merged.lastIndex] = TextNode(previous.content + node.content, previous.plain)
        } else {
            merged.add(node)
        }
    }
    container.content.clear()
    container.content.addAll(merged)
}

private fun parseFnHeader(header: String): Pair<String, HashMap<String, String>> {
    val args = hashMapOf<String, String>()
    val nameEnd = header.indexOf('.').let { if (it >= 0) it else header.length }
    val name = header.substring(0, nameEnd)
    var index = nameEnd
    while (index < header.length) {
        if (header[index] != '.') {
            index++
            continue
        }
        index++
        val keyStart = index
        while (index < header.length && header[index] != '=' && header[index] != '.' && header[index] != ',') {
            index++
        }
        if (keyStart == index) {
            continue
        }
        val key = header.substring(keyStart, index)
        if (index < header.length && header[index] == '=') {
            index++
            val valueStart = index
            while (index < header.length) {
                if (header[index] == '.' && index + 1 < header.length && header[index + 1].isLetter()) {
                    break
                }
                index++
            }
            args[key] = header.substring(valueStart, index)
        } else {
            args[key] = "true"
            while (index < header.length && header[index] == ',') {
                index++
                val flagStart = index
                while (index < header.length && header[index] != ',' && header[index] != '.') {
                    index++
                }
                val flag = header.substring(flagStart, index)
                if (flag.isNotEmpty()) {
                    args[flag] = "true"
                }
            }
        }
    }
    return name to args
}

private fun isSupportedLinkHref(href: String): Boolean {
    if (!href.startsWith("https://") && !href.startsWith("http://")) {
        return false
    }
    val authority =
        href
            .removePrefix("https://")
            .removePrefix("http://")
            .takeWhile { it != '/' && it != '?' && it != '#' }
    return authority.contains('.') && !authority.startsWith('.') && !authority.endsWith('.')
}

internal data object UrlState : State {
    override fun TreeBuilderContext.build() {
        val url = StringBuilder()
        while (reader.hasNext()) {
            val token = tokenCharacterTypes[reader.position]
            if (token == Url) {
                url.append(reader.consume())
            } else {
                break
            }
        }
        currentContainer.content.add(UrlNode(decodePercentEncodedUrl(url.toString())))
    }
}

internal data object FnState : State {
    override fun TreeBuilderContext.build() {
        val start = reader.position
        val body = StringBuilder()
        while (reader.hasNext()) {
            val token = tokenCharacterTypes[reader.position]
            if (token == Fn) {
                if (body.isEmpty()) {
                    // skip
                    reader.consume()
                } else {
                    // nested fn
                    reader.consume()
                    break
                }
            } else if (token == FnContent) {
                body.append(reader.consume())
            } else {
                break
            }
        }
        val (name, args) = parseFnHeader(body.toString())
        val node = FnNode(start, name, args = args)
        currentContainer.content.add(node)
        stack.add(node)
        currentContainer = node
    }
}

internal data object FnEndState : State {
    override fun TreeBuilderContext.build() {
        endNode<FnNode>(reader.position)
        reader.consume()
//        while (reader.hasNext()) {
//            val token = tokenCharacterTypes[reader.position]
//            if (token == FnEndBracket) {
//                reader.consume()
//            } else {
//                break
//            }
//        }
    }
}

internal data object EofState : State {
    override fun TreeBuilderContext.build() {
        val start = reader.position

        stack.findLast { it is QuoteNode }?.let { quote ->
            if (quote.content.isEmpty()) {
                stack.remove(quote)
                currentContainer = stack.last()
                currentContainer.content.remove(quote)
                currentContainer.content.add(TextNode(reader.readAt(quote.start, start - quote.start)))
            } else {
                endNode<QuoteNode>(start)
            }
        }

        if (stack.size > 1) {
            val node = stack.get(1)
            val root = stack.get(0)
            root.content.remove(node)
            root.content.add(TextNode(reader.readAt(node.start, reader.position - node.start)))
        }
        reader.consume()
    }
}

private fun decodePercentEncodedUrl(value: String): String {
    if (!value.contains('%')) {
        return value
    }
    val decoded = StringBuilder()
    val buffer = mutableListOf<Byte>()
    var index = 0
    while (index < value.length) {
        val current = value[index]
        if (current == '%' && index + 2 < value.length) {
            val highDigit = hexDigitOf(value[index + 1])
            val lowDigit = hexDigitOf(value[index + 2])
            if (highDigit >= 0 && lowDigit >= 0) {
                buffer.add(((highDigit shl 4) + lowDigit).toByte())
                index += 3
                continue
            }
        }
        appendDecodedBuffer(buffer, decoded)
        decoded.append(current)
        index++
    }
    appendDecodedBuffer(buffer, decoded)
    return decoded.toString()
}

private fun appendDecodedBuffer(
    buffer: MutableList<Byte>,
    destination: StringBuilder,
) {
    if (buffer.isEmpty()) {
        return
    }
    val bytes = ByteArray(buffer.size)
    buffer.forEachIndexed { index, byte -> bytes[index] = byte }
    destination.append(bytes.decodeToString())
    buffer.clear()
}

private fun hexDigitOf(value: Char): Int =
    when (value) {
        in '0'..'9' -> value - '0'
        in 'a'..'f' -> value - 'a' + 10
        in 'A'..'F' -> value - 'A' + 10
        else -> -1
    }
