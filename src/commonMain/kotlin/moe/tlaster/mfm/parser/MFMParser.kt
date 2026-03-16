package moe.tlaster.mfm.parser

import moe.tlaster.mfm.parser.tokenizer.StringReader
import moe.tlaster.mfm.parser.tokenizer.Tokenizer
import moe.tlaster.mfm.parser.tree.BoldNode
import moe.tlaster.mfm.parser.tree.CenterNode
import moe.tlaster.mfm.parser.tree.ContainerNode
import moe.tlaster.mfm.parser.tree.FnNode
import moe.tlaster.mfm.parser.tree.HashtagNode
import moe.tlaster.mfm.parser.tree.ItalicNode
import moe.tlaster.mfm.parser.tree.LinkNode
import moe.tlaster.mfm.parser.tree.Node
import moe.tlaster.mfm.parser.tree.QuoteNode
import moe.tlaster.mfm.parser.tree.RootNode
import moe.tlaster.mfm.parser.tree.SmallNode
import moe.tlaster.mfm.parser.tree.StrikeNode
import moe.tlaster.mfm.parser.tree.TextNode
import moe.tlaster.mfm.parser.tree.TreeBuilder
import moe.tlaster.mfm.parser.tree.UrlNode

class MFMParser(
    private val emojiOnly: Boolean = false,
) {
    fun parse(text: String): RootNode {
        val tokenizer = Tokenizer(emojiOnly = emojiOnly)
        val tokenCharacterTypes = tokenizer.parse(StringReader(text))
        return TreeBuilder().build(StringReader(text), tokenCharacterTypes).also { normalizeCompat(it) }
    }
}

private fun normalizeCompat(root: RootNode) {
    normalizeNodes(root.content)
}

private fun normalizeNodes(
    nodes: ArrayList<Node>,
    insideLink: Boolean = false,
) {
    for (i in nodes.indices) {
        nodes[i] = normalizeNode(nodes[i], insideLink)
    }

    var index = 0
    while (index < nodes.size) {
        val current = nodes[index]

        if (!insideLink && current is TextNode) {
            val match = Regex("""^(.*\[)(https?://[^\]]+)(].*)$""").matchEntire(current.content)
            if (match != null) {
                val prefix = match.groupValues[1]
                val url = match.groupValues[2]
                val suffix = match.groupValues[3]
                nodes[index] = TextNode(prefix, current.plain)
                nodes.add(index + 1, UrlNode(url))
                nodes.add(index + 2, TextNode(suffix, current.plain))
                index += 3
                continue
            }
        }

        if (current is HashtagNode) {
            val next = nodes.getOrNull(index + 1)
            if (next is TextNode && next.content.isNotEmpty()) {
                var consumed = 0
                val alnumSuffix = next.content.takeWhile { it.isLetterOrDigit() }
                var mergedTag = current.tag
                if (alnumSuffix.isNotEmpty()) {
                    mergedTag += alnumSuffix
                    consumed += alnumSuffix.length
                }
                val bracketPrefix = takeBalancedPrefix(next.content.drop(consumed))
                if (bracketPrefix != null) {
                    mergedTag += bracketPrefix
                    consumed += bracketPrefix.length
                }
                if (mergedTag != current.tag) {
                    nodes[index] = HashtagNode(mergedTag)
                    val rest = next.content.drop(consumed)
                    if (rest.isEmpty()) {
                        nodes.removeAt(index + 1)
                    } else {
                        nodes[index + 1] = TextNode(rest, next.plain)
                    }
                }
            }
        }

        if (current is LinkNode && current.content.size == 1) {
            val only = current.content.singleOrNull()
            if (only is TextNode) {
                val match = Regex("""^\[(.+?)]\((https?://[^)]+)\)$""").matchEntire(only.content)
                if (match != null) {
                    val label = match.groupValues[1]
                    val innerUrl = match.groupValues[2]
                    current.content.clear()
                    current.content.add(TextNode("[$label"))
                    nodes.add(index + 1, TextNode("]("))
                    nodes.add(index + 2, UrlNode(innerUrl))
                    nodes.add(index + 3, TextNode(")"))
                    index += 3
                }
            }
        }

        if (current is LinkNode) {
            val next = nodes.getOrNull(index + 1)
            if (next is TextNode && next.content.startsWith(")") && current.url.count { it == '(' } > current.url.count { it == ')' }) {
                nodes[index] = current.copy(url = current.url + ")")
                val rest = next.content.drop(1)
                if (rest.isEmpty()) {
                    nodes.removeAt(index + 1)
                } else {
                    nodes[index + 1] = TextNode(rest, next.plain)
                }
            }
        }

        if (current is UrlNode) {
            val next = nodes.getOrNull(index + 1)
            if (next is TextNode) {
                val suffix = takeBalancedPrefix(next.content)
                if (suffix != null) {
                    nodes[index] = UrlNode(current.url + suffix)
                    val rest = next.content.drop(suffix.length)
                    if (rest.isEmpty()) {
                        nodes.removeAt(index + 1)
                    } else {
                        nodes[index + 1] = TextNode(rest, next.plain)
                    }
                }
            }
        }

        index++
    }
}

private fun normalizeNode(
    node: Node,
    insideLink: Boolean,
): Node =
    when (node) {
        is RootNode -> node.apply { normalizeNodes(content, insideLink) }
        is QuoteNode -> node.apply { normalizeNodes(content, insideLink) }
        is CenterNode -> node.apply { normalizeNodes(content, insideLink) }
        is SmallNode -> node.apply { normalizeNodes(content, insideLink) }
        is ItalicNode -> node.apply { normalizeNodes(content, insideLink) }
        is StrikeNode -> node.apply { normalizeNodes(content, insideLink) }
        is LinkNode -> node.apply { normalizeNodes(content, insideLink = true) }
        is FnNode -> node.apply { normalizeNodes(content, insideLink) }
        is BoldNode -> {
            normalizeNodes(node.content, insideLink)
            normalizeBigNode(node)
        }
        is UrlNode -> if (node.url.any { it.code > 0x7F }) UrlNode(node.url, brackets = true) else node
        else -> node
    }

private fun normalizeBigNode(node: BoldNode): Node {
    val children = node.content
    val first = children.firstOrNull()
    val last = children.lastOrNull()
    if (first !is ItalicNode || last !is ItalicNode) {
        return node
    }

    val tadaContent = arrayListOf<Node>()
    tadaContent.addAll(first.content)

    val middle =
        if (children.size > 1) {
            children.subList(1, children.lastIndex).toCollection(ArrayList())
        } else {
            arrayListOf()
        }
    if (middle.isNotEmpty()) {
        tadaContent.add(BoldNode(node.start, middle))
    }

    if (last !== first) {
        tadaContent.addAll(last.content)
    }

    return FnNode(node.start, "tada", tadaContent)
}

private fun takeBalancedPrefix(text: String): String? {
    val pairs = listOf('(' to ')', '[' to ']', '「' to '」', '（' to '）')
    for ((open, close) in pairs) {
        if (!text.startsWith(open)) {
            continue
        }
        val end = text.indexOf(close, startIndex = 1)
        if (end > 0) {
            return text.substring(0, end + 1)
        }
    }
    return null
}
