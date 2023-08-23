package moe.tlaster.mfm.parser.tree

import moe.tlaster.mfm.parser.tokenizer.Reader
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
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.FnEndBracket
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
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.Url
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.UserAt
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.UserHost
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType.UserName

internal data class TreeBuilderContext(
    var currentContainer: ContainerNode,
    val tokenCharacterTypes: List<TokenCharacterType>,
    val reader: Reader,
) {
    val stack: ArrayList<ContainerNode> = arrayListOf()
    fun isInNewLine(start: Int, end: Int): Boolean {
        val previous = tokenCharacterTypes.getOrNull(start - 1)
        val next = tokenCharacterTypes.getOrNull(end + 1)
        return (previous == LineBreak || previous == null) && (next == LineBreak || next == null || next == Eof)
    }
    inline fun <reified T : Node> endNode(start: Int) {
        val node = stack.findLast { it is T } as? Node
        if (node != null) {
            val index = stack.indexOf(node)
            if (index != stack.lastIndex) {
                // reject inner state
                val startIndex = stack[index + 1].start
                stack[index + 1].content.add(TextNode(reader.consume(startIndex - start)))
            }
            val count = stack.size - index
            repeat(count) {
                stack.removeAt(index)
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
        val text = StringBuilder()
        while (reader.hasNext()) {
            val token = tokenCharacterTypes[reader.position]
            if (token == LineBreak) {
                text.append(reader.consume())
            } else {
                break
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
            if (token == Character || token == Bold || token == Italic) {
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
                reader.consume()
            } else {
                break
            }
        }
        currentContainer.content.add(EmojiCodeNode(text.toString()))
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
        currentContainer.content.add(HashtagNode(text.toString()))
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
            currentContainer.content.add(CodeBlockNode(language.toString(), null))
        } else {
            currentContainer.content.add(CodeBlockNode(text.toString(), language.toString()))
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
                stack[index + 1].content.add(TextNode(reader.consume(startIndex - start)))
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
                stack[index + 1].content.add(TextNode(reader.consume(startIndex - start)))
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
                stack[index + 1].content.add(TextNode(reader.consume(startIndex - start)))
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
                    // TODO: check center start
//                    val prev = tokenCharacterTypes.getOrNull(start - 1)
//                    if (prev == LineBreak || prev == null) {
//                        currentContainer.content.add(CenterNode(start))
//                        val node = CenterNode(start)
//                        currentContainer.content.add(node)
//                        currentContainer = node
//                    } else {
//                        currentContainer.content.add(TextNode(reader.readAt(start, reader.position - start)))
//                    }
                    val node = CenterNode(start)
                    currentContainer.content.add(node)
                    stack.add(node)
                    currentContainer = node
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
                // TODO: check center end
                endNode<CenterNode>(start)
//                val next = tokenCharacterTypes.getOrNull(reader.position)
//                if (next == LineBreak || next == null || next == Eof) {
//                    endNode<CenterNode>(start)
//                } else {
//                    currentContainer.content.add(TextNode(reader.readAt(start, reader.position - start)))
//                }
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
        currentContainer.content.add(LinkNode(content.toString(), href.toString(), silent))
    }
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
        currentContainer.content.add(UrlNode(url.toString()))
    }
}

internal data object FnState : State {
    override fun TreeBuilderContext.build() {
        val start = reader.position
        val body = StringBuilder()
        while (reader.hasNext()) {
            val token = tokenCharacterTypes[reader.position]
            if (token == Fn) {
                // skip
                reader.consume()
            } else if (token == FnContent) {
                body.append(reader.consume())
            } else {
                break
            }
        }
        // todo: parse fn content
        val node = FnNode(start, body.toString())
        currentContainer.content.add(node)
        stack.add(node)
        currentContainer = node
    }
}

internal data object FnEndState : State {
    override fun TreeBuilderContext.build() {
        endNode<FnNode>(reader.position)
        while (reader.hasNext()) {
            val token = tokenCharacterTypes[reader.position]
            if (token == FnEndBracket) {
                reader.consume()
            } else {
                break
            }
        }
    }
}

internal data object EofState : State {
    override fun TreeBuilderContext.build() {
        if (stack.size > 1) {
            val node = stack.get(1)
            val root = stack.get(0)
            root.content.remove(node)
            root.content.add(TextNode(reader.readAt(node.start, reader.position - node.start)))
        }
        reader.consume()
    }
}
