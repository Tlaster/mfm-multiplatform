package moe.tlaster.mfm.parser.tree

sealed interface Node

sealed interface BlockNode : Node

sealed interface InlineNode : Node

internal sealed interface ContainerNode : Node {
    val content: ArrayList<Node>
    val start: Int
}

data class RootNode(
    override val start: Int = 0,
    override val content: ArrayList<Node> = arrayListOf(),
) : Node,
    ContainerNode

data class QuoteNode(
    override val start: Int,
    override val content: ArrayList<Node> = arrayListOf(),
) : BlockNode,
    ContainerNode

data class SearchNode(
    val query: String,
    val search: String,
) : BlockNode {
    private var parsedContent: String? = null

    val content: String
        get() = parsedContent ?: "$query $search"

    internal constructor(
        query: String,
        search: String,
        content: String,
    ) : this(query, search) {
        parsedContent = content
    }
}

data class CodeBlockNode(
    val code: String,
    val language: String?,
) : BlockNode

data class MathBlockNode(
    val formula: String,
) : BlockNode

data class CenterNode(
    override val start: Int,
    override val content: ArrayList<Node> = arrayListOf(),
) : BlockNode,
    ContainerNode

data class EmojiCodeNode(
    val emoji: String,
) : InlineNode

data class UnicodeEmojiNode(
    val emoji: String,
) : InlineNode

data class BoldNode(
    override val start: Int,
    override val content: ArrayList<Node> = arrayListOf(),
) : InlineNode,
    ContainerNode

data class SmallNode(
    override val start: Int,
    override val content: ArrayList<Node> = arrayListOf(),
) : InlineNode,
    ContainerNode

data class ItalicNode(
    override val start: Int,
    override val content: ArrayList<Node> = arrayListOf(),
) : InlineNode,
    ContainerNode

data class StrikeNode(
    override val start: Int,
    override val content: ArrayList<Node> = arrayListOf(),
) : InlineNode,
    ContainerNode

data class InlineCodeNode(
    val code: String,
) : InlineNode

data class MathInlineNode(
    val formula: String,
) : InlineNode

data class MentionNode(
    val userName: String,
    val host: String?,
) : InlineNode {
    val acct: String = if (host == null) "@$userName" else "@$userName@$host"
}

data class HashtagNode(
    val tag: String,
) : InlineNode

data class UrlNode(
    val url: String,
    val brackets: Boolean = false,
) : InlineNode

data class LinkNode(
    val content: ArrayList<Node>,
    val url: String,
    val silent: Boolean,
) : InlineNode

data class FnNode(
    override val start: Int,
    val name: String,
    override val content: ArrayList<Node> = arrayListOf(),
    val args: HashMap<String, String> = hashMapOf(),
) : InlineNode,
    ContainerNode {
    private var booleanArgs: Set<String> = emptySet()

    // Record<string, string | true>
    val typedArgs: Map<String, Any>
        get() =
            if (booleanArgs.isEmpty()) {
                args
            } else {
                args.mapValues { (key, value) -> if (key in booleanArgs) true else value }
            }

    internal constructor(
        start: Int,
        name: String,
        content: ArrayList<Node>,
        args: HashMap<String, String>,
        booleanArgs: Set<String>,
    ) : this(start, name, content, args) {
        this.booleanArgs = booleanArgs
    }
}

data class TextNode(
    val content: String,
    val plain: Boolean = false,
) : InlineNode

data class CashNode(
    val content: String,
) : InlineNode

// data class TagNode(
//    override val start: Int,
//    val name: String,
//    override val content: ArrayList<Node> = arrayListOf()
// ) : ContainerNode
