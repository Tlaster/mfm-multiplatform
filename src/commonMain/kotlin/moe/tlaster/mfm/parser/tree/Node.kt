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
) : BlockNode

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
) : InlineNode

data class HashtagNode(
    val tag: String,
) : InlineNode

data class UrlNode(
    val url: String,
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
    // Record<string, string | true>
    val args: HashMap<String, String> = hashMapOf(),
) : InlineNode,
    ContainerNode

data class TextNode(
    val content: String,
) : InlineNode

data class CashNode(
    val content: String,
) : InlineNode

// data class TagNode(
//    override val start: Int,
//    val name: String,
//    override val content: ArrayList<Node> = arrayListOf()
// ) : ContainerNode
