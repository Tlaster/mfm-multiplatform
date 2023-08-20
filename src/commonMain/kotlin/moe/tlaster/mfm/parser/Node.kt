package moe.tlaster.mfm.parser

sealed interface Node

sealed interface BlockNode : Node
sealed interface InlineNode : Node
internal sealed interface ContainerNode : Node {
    val content: List<Node>
}

data class QuoteNode(
    override val content: List<Node>
) : BlockNode, ContainerNode

data class SearchNode(
    val query: String,
    val content: String
) : BlockNode

data class CodeBlockNode(
    val code: String,
    val language: String?
) : BlockNode

data class MathBlockNode(
    val formula: String
) : BlockNode

data class CenterNode(
    override val content: List<InlineNode>
) : BlockNode, ContainerNode

data class UnicodeEmojiNode(
    val emoji: String
) : InlineNode

data class EmojiCodeNode(
    val emoji: String
) : InlineNode

data class BoldNode(
    override val content: List<InlineNode>
) : InlineNode, ContainerNode

data class SmallNode(
    override val content: List<InlineNode>
) : InlineNode, ContainerNode

data class ItalicNode(
    override val content: List<InlineNode>
) : InlineNode, ContainerNode

data class StrikeNode(
    override val content: List<InlineNode>
) : InlineNode, ContainerNode

data class InlineCodeNode(
    val code: String
) : InlineNode

data class MathInlineNode(
    val formula: String
) : InlineNode

data class MentionNode(
    val userName: String,
    val host: String?,
    val acct: String
) : InlineNode

data class HashtagNode(
    val tag: String
) : InlineNode

data class UrlNode(
    val url: String,
    val brackets: Boolean?
) : InlineNode

data class LinkNode(
    val url: String,
    val silent: Boolean,
    override val content: List<InlineNode>
) : InlineNode, ContainerNode

data class FnNode(
    val name: String,
    override val content: List<InlineNode>,
    // Record<string, string | true>
    val args: Map<String, String>
) : InlineNode, ContainerNode

data class PlainNode(
    val content: String
) : InlineNode

data class TextNode(
    val content: String
) : InlineNode
