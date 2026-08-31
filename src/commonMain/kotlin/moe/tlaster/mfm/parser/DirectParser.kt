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

/** Parses directly from [source] into the public tree without a per-character token buffer. */
internal class DirectParser(
    private val source: String,
    private val emojiOnly: Boolean,
    private val rangeStart: Int = 0,
    private val rangeEnd: Int = source.length,
    private val positionBase: Int = 0,
    private val insideLinkLabel: Boolean = false,
) {
    private enum class FrameKind {
        Root,
        Quote,
        Center,
        Bold,
        Small,
        Italic,
        Strike,
        Fn,
        Tada,
    }

    private class Frame(
        val kind: FrameKind,
        val start: Int,
        val openEnd: Int,
        val name: String? = null,
        val args: HashMap<String, String>? = null,
    ) {
        val content = arrayListOf<Node>()
        var pendingStart = -1
        var pendingEnd = -1
        var pendingText: StringBuilder? = null
        var pendingPlain = false
    }

    private val frames = arrayListOf(Frame(FrameKind.Root, rangeStart, rangeStart))
    private var cursor = rangeStart
    private var textStart = rangeStart
    private var lineContentStart = rangeStart
    private var lineHadNode = false

    fun parse(): RootNode = RootNode(content = parseContent())

    private fun parseContent(): ArrayList<Node> {
        while (cursor < rangeEnd) {
            if (tryUnicodeEmoji() || tryEmojiCode()) {
                continue
            }
            if (emojiOnly) {
                advanceCharacter()
                continue
            }

            when (source[cursor]) {
                '\n' -> handleLineBreak()
                ']' -> if (!closeFrame(FrameKind.Fn, cursor, cursor + 1)) advanceCharacter()
                '>' -> if (!tryQuote()) advanceCharacter()
                '`' -> if (!tryCode()) advanceCharacter()
                '\\' -> if (!tryMath()) advanceCharacter()
                '<' -> if (insideLinkLabel || !tryTag()) advanceCharacter()
                '?' -> if (insideLinkLabel || !tryLink(silent = true)) advanceCharacter()
                '[' -> {
                    if (!trySearch() && !tryBracketedUrl() && (insideLinkLabel || !tryLink(silent = false))) {
                        advanceCharacter()
                    }
                }
                'h' -> if (insideLinkLabel || !tryUrl()) advanceCharacter()
                'S', 's', '検' -> if (!trySearch()) advanceCharacter()
                '~' -> if (!tryStrike()) advanceCharacter()
                '_' -> if (!tryUnderscore()) advanceCharacter()
                '*' -> if (!tryAsterisk()) advanceCharacter()
                '$' -> if (!tryFunction() && !tryCash()) advanceCharacter()
                '@' -> if (insideLinkLabel || !tryMention()) advanceCharacter()
                '#' -> if (!tryHashtag()) advanceCharacter()
                else -> advanceCharacter()
            }
        }

        finishAtEnd()
        return frames[0].content
    }

    private fun advanceCharacter() {
        cursor++
    }

    private fun appendRange(
        frame: Frame,
        start: Int,
        end: Int,
        plain: Boolean = false,
    ) {
        if (start >= end) return
        if (frame.hasPendingText() && frame.pendingPlain != plain) {
            commitText(frame)
        }
        frame.pendingPlain = plain
        val builder = frame.pendingText
        if (builder != null) {
            appendSource(builder, start, end)
        } else if (frame.pendingStart < 0) {
            frame.pendingStart = start
            frame.pendingEnd = end
        } else if (frame.pendingEnd == start) {
            frame.pendingEnd = end
        } else {
            val newBuilder = StringBuilder()
            appendSource(newBuilder, frame.pendingStart, frame.pendingEnd)
            appendSource(newBuilder, start, end)
            frame.pendingStart = -1
            frame.pendingEnd = -1
            frame.pendingText = newBuilder
        }
    }

    private fun appendText(
        frame: Frame,
        value: String,
        plain: Boolean = false,
    ) {
        if (value.isEmpty()) return
        if (frame.hasPendingText() && frame.pendingPlain != plain) {
            commitText(frame)
        }
        frame.pendingPlain = plain
        var builder = frame.pendingText
        if (builder == null) {
            builder = StringBuilder()
            if (frame.pendingStart >= 0) appendSource(builder, frame.pendingStart, frame.pendingEnd)
            frame.pendingStart = -1
            frame.pendingEnd = -1
            frame.pendingText = builder
        }
        builder.append(value)
    }

    private fun commitText(frame: Frame) {
        if (!frame.hasPendingText()) return
        val text = frame.pendingText?.toString() ?: source.substring(frame.pendingStart, frame.pendingEnd)
        frame.pendingStart = -1
        frame.pendingEnd = -1
        frame.pendingText = null
        val previous = frame.content.lastOrNull()
        if (previous is TextNode && previous.plain == frame.pendingPlain) {
            frame.content[frame.content.lastIndex] = TextNode(previous.content + text, previous.plain)
        } else {
            frame.content.add(TextNode(text, frame.pendingPlain))
        }
    }

    private fun Frame.hasPendingText(): Boolean = pendingStart >= 0 || pendingText != null

    private fun appendSource(
        destination: StringBuilder,
        start: Int,
        end: Int,
    ) {
        for (index in start until end) destination.append(source[index])
    }

    private fun appendNode(
        frame: Frame,
        node: Node,
    ) {
        if (node is TextNode) {
            appendText(frame, node.content, node.plain)
            return
        }
        commitText(frame)
        frame.content.add(node)
    }

    private fun flushSource(until: Int) {
        if (textStart < until) {
            appendRange(frames.last(), textStart, until)
        }
        textStart = until
    }

    private fun advanceTo(
        end: Int,
        semantic: Boolean,
    ) {
        var index = cursor
        while (index < end) {
            if (source[index] == '\n') {
                lineContentStart = index + 1
                lineHadNode = false
            }
            index++
        }
        cursor = end
        textStart = end
        if (semantic && cursor > lineContentStart) {
            lineHadNode = true
        }
    }

    private fun emitNode(
        node: Node,
        end: Int,
    ) {
        flushSource(cursor)
        appendNode(frames.last(), node)
        advanceTo(end, semantic = true)
    }

    private fun pushFrame(
        kind: FrameKind,
        start: Int,
        openEnd: Int,
        name: String? = null,
        args: HashMap<String, String>? = null,
    ) {
        flushSource(start)
        commitText(frames.last())
        frames.add(Frame(kind, start, openEnd, name, args))
        advanceTo(openEnd, semantic = true)
    }

    private fun lastFrameIndex(kind: FrameKind): Int {
        for (index in frames.lastIndex downTo 1) {
            if (frames[index].kind == kind) return index
        }
        return -1
    }

    private fun closeFrame(
        kind: FrameKind,
        closeStart: Int,
        closeEnd: Int,
    ): Boolean {
        val frameIndex = lastFrameIndex(kind)
        if (frameIndex < 0) return false

        flushSource(closeStart)
        val frame = frames[frameIndex]
        if (frameIndex != frames.lastIndex) {
            val rawStart = frames[frameIndex + 1].start
            while (frames.lastIndex > frameIndex) {
                frames.removeAt(frames.lastIndex)
            }
            appendRange(frame, rawStart, closeStart)
        }
        commitText(frame)
        frames.removeAt(frameIndex)
        appendNode(frames.last(), frame.toNode())
        cursor = closeStart
        advanceTo(closeEnd, semantic = true)
        return true
    }

    private fun Frame.toNode(): Node =
        when (kind) {
            FrameKind.Quote -> QuoteNode(publicPosition(start), content)
            FrameKind.Center -> {
                if (content.none { it is UrlNode }) trimSurroundingLineBreak(content)
                CenterNode(publicPosition(start), content)
            }
            FrameKind.Bold -> normalizeBold(publicPosition(start), content)
            FrameKind.Small -> SmallNode(publicPosition(start), content)
            FrameKind.Italic -> ItalicNode(publicPosition(start), content)
            FrameKind.Strike -> StrikeNode(publicPosition(start), content)
            FrameKind.Fn -> FnNode(publicPosition(start), name.orEmpty(), content, args ?: hashMapOf())
            FrameKind.Tada -> FnNode(publicPosition(start), "tada", content)
            FrameKind.Root -> error("The root frame is never materialized")
        }

    private fun normalizeBold(
        start: Int,
        content: ArrayList<Node>,
    ): Node {
        val first = content.firstOrNull()
        val last = content.lastOrNull()
        if (first !is ItalicNode || last !is ItalicNode) {
            return BoldNode(start, content)
        }

        val tada = arrayListOf<Node>()
        tada.addAll(first.content)
        if (content.size > 2) {
            tada.add(BoldNode(start, content.subList(1, content.lastIndex).toCollection(ArrayList())))
        }
        if (last !== first) tada.addAll(last.content)
        return FnNode(start, "tada", tada)
    }

    private fun trimSurroundingLineBreak(content: ArrayList<Node>) {
        val first = content.firstOrNull()
        if (first is TextNode && first.content.startsWith('\n')) {
            val value = first.content.drop(1)
            if (value.isEmpty()) content.removeAt(0) else content[0] = TextNode(value, first.plain)
        }
        val lastIndex = content.lastIndex
        val last = content.getOrNull(lastIndex)
        if (last is TextNode && last.content.endsWith('\n')) {
            val value = last.content.dropLast(1)
            if (value.isEmpty()) content.removeAt(lastIndex) else content[lastIndex] = TextNode(value, last.plain)
        }
    }

    private fun finishAtEnd() {
        val quoteIndex = lastFrameIndex(FrameKind.Quote)
        if (quoteIndex >= 0) {
            val quote = frames[quoteIndex]
            if (rangeEnd > quote.openEnd) {
                closeFrame(FrameKind.Quote, rangeEnd, rangeEnd)
            }
        }

        if (frames.size > 1) {
            val outer = frames[1]
            while (frames.size > 1) frames.removeAt(frames.lastIndex)
            appendRange(frames[0], outer.start, rangeEnd)
            textStart = rangeEnd
        } else {
            flushSource(rangeEnd)
        }
        commitText(frames[0])
    }

    private fun tryUnicodeEmoji(): Boolean {
        var end = cursor
        while (end < rangeEnd) {
            end =
                when {
                    source[end].isHighSurrogate() && end + 1 < rangeEnd && source[end + 1].isLowSurrogate() -> end + 2
                    source[end] == '#' && matches(end, "#\uFE0F\u20E3") -> end + 3
                    else -> break
                }
        }
        if (end == cursor) return false
        emitNode(UnicodeEmojiNode(source.substring(cursor, end)), end)
        return true
    }

    private fun tryEmojiCode(): Boolean {
        if (source[cursor] != ':' || cursor + 2 >= rangeEnd || !source[cursor + 1].isEmojiNameChar()) return false
        var end = cursor + 2
        while (end < rangeEnd && source[end].isEmojiNameChar()) end++
        if (end >= rangeEnd || source[end] != ':') return false
        val before = source.getOrNull(cursor - 1)
        val after = source.getOrNull(end + 1)
        if (before?.isAsciiAlphanumeric() == true && after?.isAsciiAlphanumeric() == true) return false
        emitNode(EmojiCodeNode(source.substring(cursor + 1, end)), end + 1)
        return true
    }

    private fun tryQuote(): Boolean {
        if (source[cursor] != '>' || (cursor != rangeStart && source[cursor - 1] != '\n')) return false
        val openEnd = if (cursor + 1 < rangeEnd && source[cursor + 1] == ' ') cursor + 2 else cursor + 1
        pushFrame(FrameKind.Quote, cursor, openEnd)
        lineContentStart = openEnd
        lineHadNode = false
        return true
    }

    private fun handleLineBreak() {
        val quoteIndex = lastFrameIndex(FrameKind.Quote)
        if (quoteIndex < 0) {
            cursor++
            lineContentStart = cursor
            lineHadNode = false
            return
        }

        var afterBreaks = cursor
        while (afterBreaks < rangeEnd && source[afterBreaks] == '\n') afterBreaks++
        if (afterBreaks - cursor > 1) {
            closeFrame(FrameKind.Quote, cursor, cursor)
            cursor = afterBreaks
            textStart = afterBreaks
            lineContentStart = afterBreaks
            lineHadNode = false
            return
        }

        if (afterBreaks < rangeEnd && source[afterBreaks] == '>') {
            var markerEnd = afterBreaks + 1
            if (markerEnd < rangeEnd && source[markerEnd] == ' ') markerEnd++
            val trailingBlankLine =
                markerEnd < rangeEnd &&
                    source[markerEnd] == '\n' &&
                    !(markerEnd + 1 < rangeEnd && source[markerEnd + 1] == '>')
            if (trailingBlankLine) {
                closeFrame(FrameKind.Quote, cursor, cursor)
            } else {
                flushSource(afterBreaks)
            }
            cursor = markerEnd
            textStart = markerEnd
            lineContentStart = markerEnd
            lineHadNode = false
            return
        }

        closeFrame(FrameKind.Quote, cursor, cursor)
    }

    private fun trySearch(): Boolean {
        val markerLength =
            when {
                matches(cursor, "[Search]", ignoreCase = true) -> 8
                matches(cursor, "[検索]") -> 4
                matches(cursor, "Search", ignoreCase = true) -> 6
                matches(cursor, "検索") -> 2
                else -> return false
            }
        val markerEnd = cursor + markerLength
        if (markerEnd < rangeEnd && source[markerEnd] != '\n') return false
        if (source[cursor] != '[') {
            val previous = source.getOrNull(cursor - 1)
            if (previous != ' ' && previous != '\t' && previous != '\u3000') return false
        }
        if (lineHadNode || lineContentStart >= cursor || textStart > lineContentStart) return false

        val frame = frames.last()
        appendRange(frame, textStart, lineContentStart)
        val query = source.substring(lineContentStart, cursor).trim()
        appendNode(frame, SearchNode(query, source.substring(cursor, markerEnd)))
        advanceTo(markerEnd, semantic = true)
        return true
    }

    private fun tryCode(): Boolean {
        if (matches(cursor, "```")) return tryCodeBlock()
        if (matches(cursor, "``")) {
            cursor += 2
            return true
        }

        var end = cursor + 1
        while (end < rangeEnd) {
            val current = source[end]
            if (current == '`') {
                emitNode(InlineCodeNode(source.substring(cursor + 1, end)), end + 1)
                return true
            }
            if (current == '\n' || current.code !in 0x20..0x7E) {
                cursor = end
                return true
            }
            end++
        }
        cursor = rangeEnd
        return true
    }

    private fun tryCodeBlock(): Boolean {
        val languageStart = cursor + 3
        var bodyStart = languageStart
        while (bodyStart < rangeEnd && source[bodyStart].isCodeLanguageChar()) bodyStart++
        val languageEnd = bodyStart
        if (bodyStart < rangeEnd && source[bodyStart] == '\n') bodyStart++

        var close = -1
        var lineStart = true
        var index = bodyStart
        while (index < rangeEnd) {
            if (lineStart && matches(index, "```") && (index + 3 == rangeEnd || source[index + 3] == '\n')) {
                close = index
                break
            }
            lineStart = source[index] == '\n'
            index++
        }
        if (close < 0) {
            advanceTextTo(rangeEnd)
            return true
        }

        val language = source.substring(languageStart, languageEnd)
        val rawCode = source.substring(bodyStart, close)
        val code = rawCode.removeSuffix("\n")
        val node =
            if (code.isEmpty() && language.isNotEmpty()) {
                CodeBlockNode(language.removeSuffix("\n"), null)
            } else {
                CodeBlockNode(code, language.takeIf { it.isNotEmpty() })
            }
        emitNode(node, close + 3)
        return true
    }

    private fun tryMath(): Boolean {
        if (matches(cursor, "\\(")) {
            var end = cursor + 2
            while (end < rangeEnd && source[end] != '\n') {
                if (matches(end, "\\)")) {
                    emitNode(MathInlineNode(source.substring(cursor + 2, end)), end + 2)
                    return true
                }
                end++
            }
            cursor = end
            return true
        }
        if (!matches(cursor, "\\[")) return false

        var end = cursor + 2
        while (end < rangeEnd && !matches(end, "\\]")) end++
        if (end >= rangeEnd) {
            advanceTextTo(rangeEnd)
            return true
        }
        val markerEnd = end + 2
        val onOwnLine =
            (cursor == rangeStart || source[cursor - 1] == '\n') &&
                (markerEnd == rangeEnd || source[markerEnd] == '\n')
        if (onOwnLine) {
            emitNode(MathBlockNode(source.substring(cursor + 2, end)), markerEnd)
        } else {
            advanceTextTo(markerEnd)
        }
        return true
    }

    private fun tryTag(): Boolean {
        if (cursor + 1 >= rangeEnd) return false
        val first = source[cursor + 1]
        if (first != '/' && !first.isAsciiAlpha()) return false
        var end = cursor + 1
        while (end < rangeEnd && source[end] != '>' && !source[end].isTagWhitespace()) end++
        if (end >= rangeEnd || source[end] != '>') {
            advanceTextTo(rangeEnd)
            return true
        }
        val tagEnd = end + 1
        val closing = first == '/'
        val nameStart = cursor + if (closing) 2 else 1
        val name = source.substring(nameStart, end)

        if (!closing && (name.startsWith("http://") || name.startsWith("https://"))) {
            emitNode(UrlNode(name, brackets = name.any { it.code > 0x7F }), tagEnd)
            return true
        }

        if (closing) {
            val kind = tagKind(name) ?: run {
                cursor = tagEnd
                return true
            }
            if (kind == FrameKind.Center && tagEnd < rangeEnd && source[tagEnd] != '\n') {
                cursor = tagEnd
                return true
            }
            if (!closeFrame(kind, cursor, tagEnd)) cursor = tagEnd
            return true
        }

        when (name) {
            "center" -> {
                if (cursor != lineContentStart) {
                    cursor = tagEnd
                } else {
                    pushFrame(FrameKind.Center, cursor, tagEnd)
                }
            }
            "b" -> pushFrame(FrameKind.Bold, cursor, tagEnd)
            "small" -> pushFrame(FrameKind.Small, cursor, tagEnd)
            "i" -> pushFrame(FrameKind.Italic, cursor, tagEnd)
            "s" -> pushFrame(FrameKind.Strike, cursor, tagEnd)
            "plain" -> parsePlainTag(tagEnd)
            else -> cursor = tagEnd
        }
        return true
    }

    private fun tagKind(name: String): FrameKind? =
        when (name) {
            "center" -> FrameKind.Center
            "b" -> FrameKind.Bold
            "small" -> FrameKind.Small
            "i" -> FrameKind.Italic
            "s" -> FrameKind.Strike
            else -> null
        }

    private fun parsePlainTag(openEnd: Int) {
        val close = indexOf("</plain>", openEnd)
        val contentEnd = if (close >= 0) close else rangeEnd
        var valueStart = openEnd
        var valueEnd = contentEnd
        if (valueStart < valueEnd && source[valueStart] == '\n') valueStart++
        if (valueStart < valueEnd && source[valueEnd - 1] == '\n') valueEnd--

        flushSource(cursor)
        appendRange(frames.last(), valueStart, valueEnd, plain = true)
        val end = if (close >= 0) close + "</plain>".length else rangeEnd
        advanceTo(end, semantic = true)
    }

    private fun tryAsterisk(): Boolean {
        val run = markerRun('*')
        if (run == 3 && lastFrameIndex(FrameKind.Tada) >= 0) {
            return closeFrame(FrameKind.Tada, cursor, cursor + 3)
        }
        if (run == 3 && lastFrameIndex(FrameKind.Bold) < 0) {
            pushFrame(FrameKind.Tada, cursor, cursor + 3)
            return true
        }
        if (run >= 2) {
            if (!closeFrame(FrameKind.Bold, cursor, cursor + 2)) {
                pushFrame(FrameKind.Bold, cursor, cursor + 2)
            }
            return true
        }
        return tryItalic('*')
    }

    private fun tryUnderscore(): Boolean {
        if (matches(cursor, "__") && tryUnderscoreBold()) return true
        return tryItalic('_')
    }

    private fun tryUnderscoreBold(): Boolean {
        if (cursor + 2 >= rangeEnd || !source[cursor + 2].isEmphasisContent()) return false
        var end = cursor + 2
        while (end < rangeEnd && source[end].isEmphasisContent()) end++
        if (!matches(end, "__")) return false
        val children = arrayListOf<Node>()
        if (end > cursor + 2) children.add(TextNode(source.substring(cursor + 2, end)))
        emitNode(BoldNode(publicPosition(cursor), children), end + 2)
        return true
    }

    private fun tryItalic(marker: Char): Boolean {
        val before = source.getOrNull(cursor - 1)
        if (before?.isAsciiAlphanumeric() == true || cursor + 1 >= rangeEnd || !source[cursor + 1].isEmphasisContent()) {
            return false
        }
        var end = cursor + 1
        while (end < rangeEnd && source[end].isEmphasisContent()) end++
        if (end >= rangeEnd || source[end] != marker || source.getOrNull(end + 1)?.isAsciiAlphanumeric() == true) {
            return false
        }
        emitNode(
            ItalicNode(publicPosition(cursor), arrayListOf(TextNode(source.substring(cursor + 1, end)))),
            end + 1,
        )
        return true
    }

    private fun tryStrike(): Boolean {
        if (!matches(cursor, "~~")) return false
        if (!closeFrame(FrameKind.Strike, cursor, cursor + 2)) {
            pushFrame(FrameKind.Strike, cursor, cursor + 2)
        }
        return true
    }

    private fun tryFunction(): Boolean {
        if (!matches(cursor, "\$[") || cursor + 2 >= rangeEnd || !source[cursor + 2].isAsciiAlphanumeric()) return false
        var headerEnd = cursor + 2
        while (headerEnd < rangeEnd) {
            val current = source[headerEnd]
            if (current.isFnWhitespace()) break
            if (current.isFnContent()) {
                headerEnd++
                continue
            }
            if ((current == ',' || current == '=') && source.getOrNull(headerEnd + 1)?.isFnContent() == true) {
                headerEnd++
                continue
            }
            return false
        }
        if (headerEnd >= rangeEnd || !source[headerEnd].isFnWhitespace()) return false
        val header = source.substring(cursor + 2, headerEnd)
        val (name, args) = parseFnHeader(header)
        pushFrame(FrameKind.Fn, cursor, headerEnd + 1, name, args)
        return true
    }

    private fun tryCash(): Boolean {
        if (source[cursor] != '$' || source.getOrNull(cursor + 1)?.isAsciiAlphanumeric() != true) return false
        var end = cursor + 1
        while (end < rangeEnd && source[end].isAsciiAlphanumeric()) end++
        emitNode(CashNode(source.substring(cursor + 1, end)), end)
        return true
    }

    private fun tryMention(): Boolean {
        if (source[cursor] != '@' || source.getOrNull(cursor - 1)?.isAsciiAlphanumeric() == true) return false
        val nameStart = cursor + 1
        if (source.getOrNull(nameStart)?.isMentionBasic() != true) return false
        val nameEnd = scanMentionPart(nameStart)
        var end = nameEnd
        var host: String? = null
        if (end < rangeEnd && source[end] == '@') {
            val hostStart = end + 1
            if (source.getOrNull(hostStart)?.isMentionBasic() != true) return false
            val hostEnd = scanMentionPart(hostStart)
            host = source.substring(hostStart, hostEnd)
            end = hostEnd
        }
        emitNode(MentionNode(source.substring(nameStart, nameEnd), host), end)
        return true
    }

    private fun scanMentionPart(start: Int): Int {
        var end = start
        while (end < rangeEnd) {
            val current = source[end]
            if (current.isMentionBasic()) {
                end++
            } else if ((current == '-' || current == '.') && source.getOrNull(end + 1)?.isMentionContinuation() == true) {
                end++
            } else {
                break
            }
        }
        return end
    }

    private fun tryHashtag(): Boolean {
        if (source[cursor] != '#' || source.getOrNull(cursor - 1)?.isAsciiAlphanumeric() == true) return false
        val first = source.getOrNull(cursor + 1) ?: return false
        if (first.isHashtagExcluded()) {
            cursor = minOf(cursor + 2, rangeEnd)
            return true
        }
        var end = cursor + 1
        while (end < rangeEnd && !source[end].isHashtagExcluded()) end++
        end = includeBalancedSuffix(end)
        val tag = source.substring(cursor + 1, end)
        if (tag.all { it in '0'..'9' }) {
            cursor = end
        } else {
            emitNode(HashtagNode(tag), end)
        }
        return true
    }

    private fun tryUrl(): Boolean {
        val schemeLength =
            when {
                matches(cursor, "https://", ignoreCase = true) -> 8
                matches(cursor, "http://", ignoreCase = true) -> 7
                else -> return false
            }
        var end = cursor + schemeLength
        while (end < rangeEnd) {
            val current = source[end]
            if (current == '(' || current == '[') {
                val balancedEnd = balancedUrlItemEnd(end)
                if (balancedEnd < 0) break
                end = balancedEnd
            } else if (current.isUrlChar()) {
                end++
            } else {
                break
            }
        }
        while (end > cursor + schemeLength && (source[end - 1] == '.' || source[end - 1] == ',')) end--
        if (!hasValidUrlAuthority(cursor, end)) {
            advanceTextTo(end)
            return true
        }
        val extendedEnd = includeBalancedSuffix(end)
        emitNode(UrlNode(decodePercentEncodedUrl(source.substring(cursor, extendedEnd))), extendedEnd)
        return true
    }

    private fun tryBracketedUrl(): Boolean {
        if (insideLinkLabel || source[cursor] != '[') return false
        val urlStart = cursor + 1
        if (!matches(urlStart, "https://") && !matches(urlStart, "http://")) return false
        val close = source.indexOf(']', urlStart)
        if (close < 0 || close >= rangeEnd) return false
        if (source.getOrNull(close + 1) == '(') return false
        flushSource(cursor)
        appendText(frames.last(), "[")
        appendNode(frames.last(), UrlNode(source.substring(urlStart, close)))
        appendText(frames.last(), "]")
        advanceTo(close + 1, semantic = true)
        return true
    }

    private fun tryLink(silent: Boolean): Boolean {
        val openLength = if (silent) 2 else 1
        if (silent) {
            if (!matches(cursor, "?[")) return false
        } else if (source[cursor] != '[') {
            return false
        }
        val labelStart = cursor + openLength
        var depth = 0
        var candidate = -1
        var index = labelStart
        while (index < rangeEnd && source[index] != '\n') {
            when (source[index]) {
                '[' -> depth++
                ']' -> {
                    if (index + 1 < rangeEnd && source[index + 1] == '(') {
                        candidate = index
                        if (depth == 0) break
                        depth--
                    } else if (depth > 0) {
                        depth--
                    } else {
                        cursor = index + 1
                        return true
                    }
                }
            }
            index++
        }
        val labelEnd = if (index < rangeEnd && source[index] == ']' && source.getOrNull(index + 1) == '(') index else candidate
        if (labelEnd < 0) {
            cursor = index
            return true
        }

        val hrefStart = labelEnd + 2
        var hrefEnd = hrefStart
        var parentheses = 0
        while (hrefEnd < rangeEnd) {
            val current = source[hrefEnd]
            if (current.isFnWhitespace() || current == '\u3000') {
                cursor = hrefEnd
                return true
            }
            if (current == '(') {
                parentheses++
            } else if (current == ')') {
                if (parentheses == 0) break
                parentheses--
            }
            hrefEnd++
        }
        if (hrefEnd >= rangeEnd || source[hrefEnd] != ')') {
            cursor = hrefEnd
            return true
        }
        val rawEnd = hrefEnd + 1
        var rawHrefStart = hrefStart
        var rawHrefEnd = hrefEnd
        if (rawHrefEnd - rawHrefStart >= 2 && source[rawHrefStart] == '<' && source[rawHrefEnd - 1] == '>') {
            rawHrefStart++
            rawHrefEnd--
        }
        val href = decodePercentEncodedUrl(source.substring(rawHrefStart, rawHrefEnd))
        if (!isSupportedLinkHref(href)) {
            cursor = rawEnd
            return true
        }

        flushSource(cursor)
        val label =
            DirectParser(
                source = source,
                emojiOnly = false,
                rangeStart = labelStart,
                rangeEnd = labelEnd,
                positionBase = labelStart,
                insideLinkLabel = true,
            ).parseContent()
        val link = LinkNode(label, href, silent)
        val nested = nestedLinkText(label)
        if (nested == null) {
            appendNode(frames.last(), link)
        } else {
            label.clear()
            label.add(TextNode("[${nested.first}"))
            appendNode(frames.last(), link)
            appendText(frames.last(), "](")
            appendNode(frames.last(), UrlNode(nested.second))
            appendText(frames.last(), ")")
        }
        advanceTo(rawEnd, semantic = true)
        return true
    }

    private fun nestedLinkText(content: ArrayList<Node>): Pair<String, String>? {
        val text = (content.singleOrNull() as? TextNode)?.content ?: return null
        if (!text.startsWith('[') || !text.endsWith(')')) return null
        val separator = text.indexOf("](", startIndex = 2)
        if (separator < 0) return null
        val label = text.substring(1, separator)
        val url = text.substring(separator + 2, text.length - 1)
        if (label.isEmpty() || (!url.startsWith("https://") && !url.startsWith("http://")) || ')' in url) return null
        return label to url
    }

    private fun balancedUrlItemEnd(start: Int): Int {
        val closing = arrayListOf(if (source[start] == '(') ')' else ']')
        var index = start + 1
        while (index < rangeEnd) {
            when (val current = source[index]) {
                '(' -> closing.add(')')
                '[' -> closing.add(']')
                ')', ']' -> {
                    if (closing.lastOrNull() != current) return -1
                    closing.removeAt(closing.lastIndex)
                    if (closing.isEmpty()) return index + 1
                }
                else -> if (!current.isUrlChar()) return -1
            }
            index++
        }
        return -1
    }

    private fun includeBalancedSuffix(start: Int): Int {
        if (start >= rangeEnd) return start
        val close =
            when (source[start]) {
                '(' -> ')'
                '[' -> ']'
                '「' -> '」'
                '（' -> '）'
                else -> return start
            }
        val end = source.indexOf(close, start + 1)
        return if (end in (start + 1) until rangeEnd) end + 1 else start
    }

    private fun hasValidUrlAuthority(
        start: Int,
        end: Int,
    ): Boolean {
        val authorityStart = start + if (matches(start, "https://", ignoreCase = true)) 8 else 7
        var authorityEnd = authorityStart
        while (authorityEnd < end && source[authorityEnd] != '/' && source[authorityEnd] != '?' && source[authorityEnd] != '#') {
            authorityEnd++
        }
        if (authorityStart == authorityEnd || source[authorityStart] == '.' || source[authorityEnd - 1] == '.') return false
        for (index in authorityStart until authorityEnd) {
            if (source[index] == '.') return true
        }
        return false
    }

    private fun isSupportedLinkHref(href: String): Boolean {
        val schemeLength =
            when {
                href.startsWith("https://") -> 8
                href.startsWith("http://") -> 7
                else -> return false
            }
        val authority = href.substring(schemeLength).takeWhile { it != '/' && it != '?' && it != '#' }
        return authority.contains('.') && !authority.startsWith('.') && !authority.endsWith('.')
    }

    private fun advanceTextTo(end: Int) {
        var index = cursor
        while (index < end) {
            if (source[index] == '\n') {
                lineContentStart = index + 1
                lineHadNode = false
            }
            index++
        }
        cursor = end
    }

    private fun markerRun(marker: Char): Int {
        var end = cursor
        while (end < rangeEnd && source[end] == marker) end++
        return end - cursor
    }

    private fun publicPosition(index: Int): Int = index - positionBase

    private fun matches(
        index: Int,
        value: String,
        ignoreCase: Boolean = false,
    ): Boolean {
        if (index < rangeStart || index + value.length > rangeEnd) return false
        for (offset in value.indices) {
            val actual = source[index + offset]
            val expected = value[offset]
            if (actual == expected) continue
            if (!ignoreCase || actual.lowercaseChar() != expected.lowercaseChar()) return false
        }
        return true
    }

    private fun indexOf(
        value: String,
        start: Int,
    ): Int {
        var index = start
        while (index + value.length <= rangeEnd) {
            if (matches(index, value)) return index
            index++
        }
        return -1
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
            while (index < header.length && header[index] != '=' && header[index] != '.' && header[index] != ',') index++
            if (keyStart == index) continue
            val key = header.substring(keyStart, index)
            if (index < header.length && header[index] == '=') {
                index++
                val valueStart = index
                while (index < header.length && !(header[index] == '.' && index + 1 < header.length && header[index + 1].isLetter())) {
                    index++
                }
                args[key] = header.substring(valueStart, index)
            } else {
                args[key] = "true"
                while (index < header.length && header[index] == ',') {
                    index++
                    val flagStart = index
                    while (index < header.length && header[index] != ',' && header[index] != '.') index++
                    if (flagStart < index) args[header.substring(flagStart, index)] = "true"
                }
            }
        }
        return name to args
    }

    private fun decodePercentEncodedUrl(value: String): String {
        if ('%' !in value) return value
        val result = StringBuilder()
        val bytes = arrayListOf<Byte>()
        var index = 0
        while (index < value.length) {
            if (value[index] == '%' && index + 2 < value.length) {
                val high = value[index + 1].hexDigit()
                val low = value[index + 2].hexDigit()
                if (high >= 0 && low >= 0) {
                    bytes.add(((high shl 4) + low).toByte())
                    index += 3
                    continue
                }
            }
            appendDecodedBytes(bytes, result)
            result.append(value[index])
            index++
        }
        appendDecodedBytes(bytes, result)
        return result.toString()
    }

    private fun appendDecodedBytes(
        bytes: ArrayList<Byte>,
        result: StringBuilder,
    ) {
        if (bytes.isEmpty()) return
        val data = ByteArray(bytes.size)
        for (index in bytes.indices) data[index] = bytes[index]
        result.append(data.decodeToString())
        bytes.clear()
    }

    private fun Char.hexDigit(): Int =
        when (this) {
            in '0'..'9' -> this - '0'
            in 'a'..'f' -> this - 'a' + 10
            in 'A'..'F' -> this - 'A' + 10
            else -> -1
        }

    private fun Char.isAsciiAlpha(): Boolean = this in 'A'..'Z' || this in 'a'..'z'

    private fun Char.isAsciiAlphanumeric(): Boolean = isAsciiAlpha() || this in '0'..'9'

    private fun Char.isEmojiNameChar(): Boolean = isAsciiAlphanumeric() || this == '_' || this == '-' || this == '+'

    private fun Char.isCodeLanguageChar(): Boolean = isEmojiNameChar() || this == '#' || this == '.'

    private fun Char.isEmphasisContent(): Boolean = isAsciiAlphanumeric() || this == ' ' || this == '\t' || this == '\n' || this == '\u3000'

    private fun Char.isFnWhitespace(): Boolean = this == ' ' || this == '\t' || this == '\n' || this == '\u000C'

    private fun Char.isFnContent(): Boolean = isAsciiAlphanumeric() || this == '_' || this == '-' || this == '.'

    private fun Char.isMentionBasic(): Boolean = isAsciiAlphanumeric() || this == '_'

    private fun Char.isMentionContinuation(): Boolean = isMentionBasic() || this == '-' || this == '.'

    private fun Char.isTagWhitespace(): Boolean = this == ' ' || this == '\t' || this == '\n' || this == '\u000C'

    private fun Char.isHashtagExcluded(): Boolean =
        this == ' ' ||
            this == '\t' ||
            this == '\n' ||
            this == '\u000C' ||
            this == '\u3000' ||
            this in ".,!?'\"#:/[]【】()「」（）<>"

    private fun Char.isUrlChar(): Boolean =
        isAsciiAlphanumeric() || this in ".,_/:%#@\$&?!~=+-"
}
