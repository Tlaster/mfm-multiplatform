package moe.tlaster.mfm.parser

import moe.tlaster.mfm.parser.tree.BoldNode
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
    private val nestLimit: Int = 20,
    private val initialDepth: Int = 0,
) {
    private enum class FrameKind {
        Root,
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
        val booleanArgs: Set<String>? = null,
        val closeMarker: String? = null,
    ) {
        val content = arrayListOf<Node>()
        var pendingStart = -1
        var pendingEnd = -1
        var pendingText: StringBuilder? = null
        var pendingPlain = false
    }

    private val frames = arrayListOf(Frame(FrameKind.Root, rangeStart, rangeStart))
    private var emojiSource: String? = null
    private var cursor = rangeStart
    private var textStart = rangeStart
    private var lineContentStart = rangeStart
    private var lineHadNode = false

    fun parse(): RootNode = RootNode(content = parseContent())

    private fun parseContent(): ArrayList<Node> {
        if (isNestLimitReached()) parseLimitedContent()
        while (cursor < rangeEnd) {
            val current = source[cursor]
            if (current.isPlainAsciiLetter()) {
                do {
                    cursor++
                } while (cursor < rangeEnd && source[cursor].isPlainAsciiLetter())
                continue
            }
            if (current.code < 0xA9) {
                if (current.mayStartKeycapEmoji() && tryUnicodeEmoji(unicodeEmojiFastLengthAt(source, cursor))) continue
            } else if (current != '検') {
                consumeUnicodeTextOrEmoji()
                continue
            }
            if (current == ':' && tryEmojiCode()) continue
            if (emojiOnly) {
                if (current == '<' && tryPlainOnly()) continue
                advanceCharacter()
                continue
            }

            when (current) {
                '\n', '\r' -> handleLineBreak()
                ']' -> consumeFunctionClose()
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
                '$' -> if (!tryFunction()) advanceCharacter()
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

    private fun consumeUnicodeTextOrEmoji() {
        val emojiLength = unicodeEmojiFastLengthAt(source, cursor)
        if (emojiLength != 0) {
            if (!tryUnicodeEmoji(emojiLength)) cursor++
            return
        }
        do {
            cursor++
        } while (
            cursor < rangeEnd &&
                source[cursor].code >= 0x80 &&
                source[cursor] != '検' &&
                unicodeEmojiFastLengthAt(source, cursor) == 0
        )
    }

    private fun consumeFunctionClose() {
        if (closeFrame(FrameKind.Fn, cursor, cursor + 1)) return
        do {
            cursor++
        } while (cursor < rangeEnd && source[cursor] == ']')
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
            val newlineLength = newlineLengthAt(index)
            if (newlineLength > 0) {
                lineContentStart = index + newlineLength
                lineHadNode = false
                index += newlineLength
                continue
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
        booleanArgs: Set<String>? = null,
        closeMarker: String? = null,
    ) {
        flushSource(start)
        commitText(frames.last())
        frames.add(Frame(kind, start, openEnd, name, args, booleanArgs, closeMarker))
        advanceTo(openEnd, semantic = true)
        if (isNestLimitReached()) parseLimitedContent()
    }

    private fun parseLimitedContent() {
        while (cursor < rangeEnd) {
            if (tryCloseLimitedFrame()) return
            if (newlineLengthAt(cursor) > 0) handleLineBreak() else advanceCharacter()
        }
    }

    private fun tryCloseLimitedFrame(): Boolean {
        val frame = frames.lastOrNull() ?: return false
        val marker = frame.closeMarker ?: return false
        if (!matches(cursor, marker)) return false
        val closeEnd =
            if (frame.kind == FrameKind.Center) {
                if (!isLineEnd(cursor + marker.length)) return false
                consumeBoundaryAfter(cursor + marker.length, 1)
            } else {
                cursor + marker.length
            }
        return closeFrame(frame.kind, cursor, closeEnd)
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
        if (frame.content.isEmpty()) {
            appendRange(frames.last(), frame.start, closeEnd)
        } else {
            appendNode(frames.last(), frame.toNode())
        }
        cursor = closeStart
        advanceTo(closeEnd, semantic = true)
        return true
    }

    private fun Frame.toNode(): Node =
        when (kind) {
            FrameKind.Center -> {
                trimSurroundingLineBreak(content)
                CenterNode(publicPosition(start), content)
            }
            FrameKind.Bold -> normalizeBold(publicPosition(start), content)
            FrameKind.Small -> SmallNode(publicPosition(start), content)
            FrameKind.Italic -> ItalicNode(publicPosition(start), content)
            FrameKind.Strike -> StrikeNode(publicPosition(start), content)
            FrameKind.Fn ->
                FnNode(
                    publicPosition(start),
                    name.orEmpty(),
                    content,
                    args ?: hashMapOf(),
                    booleanArgs ?: emptySet(),
                )
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
        if (first is TextNode) {
            val length = leadingNewlineLength(first.content)
            if (length > 0) {
                val value = first.content.drop(length)
                if (value.isEmpty()) content.removeAt(0) else content[0] = TextNode(value, first.plain)
            }
        }
        val lastIndex = content.lastIndex
        val last = content.getOrNull(lastIndex)
        if (last is TextNode) {
            val length = trailingNewlineLength(last.content)
            if (length > 0) {
                val value = last.content.dropLast(length)
                if (value.isEmpty()) content.removeAt(lastIndex) else content[lastIndex] = TextNode(value, last.plain)
            }
        }
    }

    private fun leadingNewlineLength(value: String): Int =
        when {
            value.startsWith("\r\n") -> 2
            value.startsWith('\r') || value.startsWith('\n') -> 1
            else -> 0
        }

    private fun trailingNewlineLength(value: String): Int =
        when {
            value.endsWith("\r\n") -> 2
            value.endsWith('\r') || value.endsWith('\n') -> 1
            else -> 0
        }

    private fun finishAtEnd() {
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

    private fun tryUnicodeEmoji(fastLength: Int): Boolean {
        val length =
            if (fastLength >= 0) {
                fastLength
            } else {
                val encodedSource = emojiSource ?: encodeUnicodeEmojiInput(source).also { emojiSource = it }
                unicodeEmojiComplexLengthAt(encodedSource, cursor)
            }
        if (length == 0 || cursor + length > rangeEnd) return false
        val end = cursor + length
        emitNode(UnicodeEmojiNode(source.substring(cursor, end)), end)
        return true
    }

    private fun tryEmojiCode(): Boolean {
        if (source[cursor] != ':' || cursor + 2 >= rangeEnd || !source[cursor + 1].isEmojiNameChar()) return false
        var end = cursor + 2
        while (end < rangeEnd && source[end].isEmojiNameChar()) end++
        if (end >= rangeEnd || source[end] != ':') return false
        val after = source.getOrNull(end + 1)
        if (after?.isAsciiAlphanumeric() == true) return false
        emitNode(EmojiCodeNode(source.substring(cursor + 1, end)), end + 1)
        return true
    }

    private fun tryQuote(): Boolean {
        if (frames.lastIndex != 0 || source[cursor] != '>' || !isLineBegin(cursor)) return false
        val innerSource = StringBuilder()
        var lineCount = 0
        var index = cursor
        var end = cursor
        while (index < rangeEnd && source[index] == '>') {
            var contentStart = index + 1
            if (source.getOrNull(contentStart)?.isMfmSpace() == true) contentStart++
            var lineEnd = contentStart
            while (lineEnd < rangeEnd && newlineLengthAt(lineEnd) == 0) lineEnd++
            if (lineCount > 0) innerSource.append('\n')
            innerSource.append(source, contentStart, lineEnd)
            lineCount++
            end = lineEnd
            val newlineLength = newlineLengthAt(lineEnd)
            if (newlineLength == 0 || source.getOrNull(lineEnd + newlineLength) != '>') break
            index = lineEnd + newlineLength
        }
        if (lineCount == 1 && innerSource.isEmpty()) return false

        val content =
            DirectParser(
                source = innerSource.toString(),
                emojiOnly = false,
                nestLimit = nestLimit,
                initialDepth = initialDepth + 1,
            ).parse().content
        consumeBoundaryBefore(cursor, 2)
        emitNode(QuoteNode(publicPosition(cursor), content), consumeBoundaryAfter(end, 2))
        return true
    }

    private fun handleLineBreak() {
        cursor += newlineLengthAt(cursor)
        lineContentStart = cursor
        lineHadNode = false
    }

    private fun trySearch(): Boolean {
        if (frames.lastIndex != 0) return false
        val markerLength =
            when {
                matches(cursor, "[Search]", ignoreCase = true) -> 8
                matches(cursor, "[検索]") -> 4
                matches(cursor, "Search", ignoreCase = true) -> 6
                matches(cursor, "検索") -> 2
                else -> return false
            }
        val markerEnd = cursor + markerLength
        if (!isLineEnd(markerEnd)) return false
        val separator = cursor - 1
        if (separator < lineContentStart || source[separator].isMfmSpace().not()) return false
        if (lineHadNode || lineContentStart >= cursor || textStart > lineContentStart) return false

        val frame = frames.last()
        consumeBoundaryBefore(lineContentStart, 1)
        val query = source.substring(lineContentStart, separator)
        if (query.isEmpty()) return false
        appendNode(
            frame,
            SearchNode(
                query = query,
                search = source.substring(cursor, markerEnd),
                content = source.substring(lineContentStart, markerEnd),
            ),
        )
        advanceTo(consumeBoundaryAfter(markerEnd, 1), semantic = true)
        return true
    }

    private fun tryCode(): Boolean {
        if (frames.lastIndex == 0 && matches(cursor, "```")) return tryCodeBlock()

        var end = cursor + 1
        while (end < rangeEnd) {
            val current = source[end]
            if (current == '`') {
                if (end == cursor + 1) return false
                emitNode(InlineCodeNode(source.substring(cursor + 1, end)), end + 1)
                return true
            }
            if (current == '\n' || current == '\r' || current == '´') {
                return false
            }
            end++
        }
        return false
    }

    private fun tryCodeBlock(): Boolean {
        if (!isLineBegin(cursor)) return false
        val languageStart = cursor + 3
        var headerEnd = languageStart
        while (headerEnd < rangeEnd && newlineLengthAt(headerEnd) == 0) headerEnd++
        val headerBreak = newlineLengthAt(headerEnd)
        if (headerBreak == 0) return false
        val bodyStart = headerEnd + headerBreak
        var close = bodyStart
        var codeEnd = -1
        while (close < rangeEnd) {
            if (isLineBegin(close) && matches(close, "```") && isLineEnd(close + 3)) {
                codeEnd = previousNewlineStart(close)
                if (codeEnd >= bodyStart) break
            }
            close++
        }
        if (close >= rangeEnd || codeEnd <= bodyStart) return false

        val language = source.substring(languageStart, headerEnd).trim()
        val code = source.substring(bodyStart, codeEnd)
        consumeBoundaryBefore(cursor, 1)
        emitNode(CodeBlockNode(code, language.takeIf { it.isNotEmpty() }), consumeBoundaryAfter(close + 3, 1))
        return true
    }

    private fun tryMath(): Boolean {
        if (matches(cursor, "\\(")) {
            var end = cursor + 2
            while (end < rangeEnd && newlineLengthAt(end) == 0) {
                if (matches(end, "\\)")) {
                    if (end == cursor + 2) return false
                    emitNode(MathInlineNode(source.substring(cursor + 2, end)), end + 2)
                    return true
                }
                end++
            }
            return false
        }
        if (frames.lastIndex != 0 || !matches(cursor, "\\[") || !isLineBegin(cursor)) return false

        var end = cursor + 2
        while (end < rangeEnd && !(matches(end, "\\]") && isLineEnd(end + 2))) end++
        if (end >= rangeEnd) return false
        val markerEnd = end + 2
        var formulaStart = cursor + 2
        formulaStart += newlineLengthAt(formulaStart)
        var formulaEnd = end
        previousNewlineStart(formulaEnd).takeIf { it >= formulaStart }?.let { formulaEnd = it }
        if (formulaStart >= formulaEnd) return false
        consumeBoundaryBefore(cursor, 1)
        emitNode(MathBlockNode(source.substring(formulaStart, formulaEnd)), consumeBoundaryAfter(markerEnd, 1))
        return true
    }

    private fun tryTag(): Boolean {
        if (cursor + 1 >= rangeEnd) return false
        val first = source[cursor + 1]
        if (first != '/' && !first.isAsciiAlpha()) return false
        var end = cursor + 1
        while (end < rangeEnd && source[end] != '>' && !source[end].isTagWhitespace()) end++
        if (end >= rangeEnd || source[end] != '>') return false
        val tagEnd = end + 1
        val closing = first == '/'
        val nameStart = cursor + if (closing) 2 else 1
        val name = source.substring(nameStart, end)

        if (!closing && (name.startsWith("http://") || name.startsWith("https://"))) {
            emitNode(UrlNode(name, brackets = true), tagEnd)
            return true
        }

        if (closing) {
            val kind = tagKind(name) ?: return false
            if (kind == FrameKind.Center && !isLineEnd(tagEnd)) {
                cursor = tagEnd
                return true
            }
            val closeEnd = if (kind == FrameKind.Center) consumeBoundaryAfter(tagEnd, 1) else tagEnd
            if (!closeFrame(kind, cursor, closeEnd)) cursor = tagEnd
            return true
        }

        when (name) {
            "center" -> {
                if (frames.lastIndex != 0 || !isLineBegin(cursor) || !hasValidCenterBody(tagEnd)) {
                    cursor = tagEnd
                } else {
                    consumeBoundaryBefore(cursor, 1)
                    pushFrame(
                        FrameKind.Center,
                        cursor,
                        tagEnd + newlineLengthAt(tagEnd),
                        closeMarker = "</center>",
                    )
                }
            }
            "b" -> pushFrame(FrameKind.Bold, cursor, tagEnd, closeMarker = "</b>")
            "small" -> pushFrame(FrameKind.Small, cursor, tagEnd, closeMarker = "</small>")
            "i" -> pushFrame(FrameKind.Italic, cursor, tagEnd, closeMarker = "</i>")
            "s" -> pushFrame(FrameKind.Strike, cursor, tagEnd, closeMarker = "</s>")
            "plain" -> if (!parsePlainTag(tagEnd)) cursor = tagEnd
            else -> return false
        }
        return true
    }

    private fun hasValidCenterBody(openEnd: Int): Boolean {
        var close = openEnd
        while (close < rangeEnd) {
            close = indexOf("</center>", close)
            if (close < 0) return false
            val closeEnd = close + "</center>".length
            if (isLineEnd(closeEnd)) {
                var contentStart = openEnd + newlineLengthAt(openEnd)
                var contentEnd = close
                previousNewlineStart(contentEnd).takeIf { it >= contentStart }?.let { contentEnd = it }
                return contentStart < contentEnd
            }
            close++
        }
        return false
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

    private fun tryPlainOnly(): Boolean {
        if (!matches(cursor, "<plain>")) return false
        return parsePlainTag(cursor + "<plain>".length)
    }

    private fun parsePlainTag(openEnd: Int): Boolean {
        val close = indexOf("</plain>", openEnd)
        if (close < 0) return false
        val contentEnd = close
        var valueStart = openEnd
        var valueEnd = contentEnd
        valueStart += newlineLengthAt(valueStart)
        valueEnd = previousNewlineStart(valueEnd).takeIf { it >= valueStart } ?: valueEnd
        if (valueStart >= valueEnd) return false

        flushSource(cursor)
        appendRange(frames.last(), valueStart, valueEnd, plain = true)
        advanceTo(close + "</plain>".length, semantic = true)
        return true
    }

    private fun tryAsterisk(): Boolean {
        val run = markerRun('*')
        if (run == 3 && lastFrameIndex(FrameKind.Tada) >= 0) {
            return closeFrame(FrameKind.Tada, cursor, cursor + 3)
        }
        if (run == 3 && lastFrameIndex(FrameKind.Bold) < 0) {
            pushFrame(FrameKind.Tada, cursor, cursor + 3, closeMarker = "***")
            return true
        }
        if (run >= 2) {
            if (!closeFrame(FrameKind.Bold, cursor, cursor + 2)) {
                pushFrame(FrameKind.Bold, cursor, cursor + 2, closeMarker = "**")
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
        if (end >= rangeEnd || source[end] != marker) {
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
            var close = cursor + 2
            while (close < rangeEnd && !matches(close, "~~")) {
                if (newlineLengthAt(close) > 0) {
                    advanceTextTo(close)
                    return true
                }
                close++
            }
            if (close >= rangeEnd) {
                advanceTextTo(rangeEnd)
                return true
            }
            if (close == cursor + 2) return false
            pushFrame(FrameKind.Strike, cursor, cursor + 2, closeMarker = "~~")
        }
        return true
    }

    private fun tryFunction(): Boolean {
        if (!matches(cursor, "\$[")) return false
        var index = cursor + 2
        val nameStart = index
        while (index < rangeEnd && source[index].isFnNameChar()) index++
        if (index == nameStart) return false
        val name = source.substring(nameStart, index)
        val args = hashMapOf<String, String>()
        var booleanArgs: HashSet<String>? = null
        if (source.getOrNull(index) == '.') {
            index++
            while (true) {
                val keyStart = index
                while (index < rangeEnd && source[index].isFnNameChar()) index++
                if (index == keyStart) return false
                val key = source.substring(keyStart, index)
                if (source.getOrNull(index) == '=') {
                    index++
                    val valueStart = index
                    while (index < rangeEnd && source[index].isFnValueChar()) index++
                    if (index == valueStart) return false
                    args[key] = source.substring(valueStart, index)
                } else {
                    args[key] = "true"
                    val flags = booleanArgs ?: hashSetOf<String>().also { booleanArgs = it }
                    flags.add(key)
                }
                if (source.getOrNull(index) != ',') break
                index++
            }
        }
        if (source.getOrNull(index) != ' ') return false
        pushFrame(FrameKind.Fn, cursor, index + 1, name, args, booleanArgs, closeMarker = "]")
        return true
    }

    private fun tryMention(): Boolean {
        if (source[cursor] != '@' || source.getOrNull(cursor - 1)?.isAsciiAlphanumeric() == true) return false
        val nameStart = cursor + 1
        if (source.getOrNull(nameStart)?.isMentionChar() != true) return false
        val nameEnd = scanMentionPart(nameStart)
        val rawName = source.substring(nameStart, nameEnd)
        var end = nameEnd
        var rawHost: String? = null
        if (source.getOrNull(end) == '@' && source.getOrNull(end + 1)?.isMentionChar() == true) {
            val hostStart = end + 1
            val hostEnd = scanMentionPart(hostStart)
            rawHost = source.substring(hostStart, hostEnd)
            end = hostEnd
        }

        var host = rawHost?.trimEnd('.', '-')
        var name = rawName
        var invalid = host != null && (host.isEmpty() || host[0] == '.' || host[0] == '-')
        if (name.endsWith('.') || name.endsWith('-')) {
            if (host == null) name = name.trimEnd('.', '-') else invalid = true
        }
        if (name.isEmpty() || name[0] == '.' || name[0] == '-') invalid = true
        if (invalid) {
            advanceTextTo(end)
            return true
        }

        val mentionEnd = cursor + 1 + name.length + if (host == null) 0 else 1 + host.length
        emitNode(MentionNode(name, host), mentionEnd)
        return true
    }

    private fun scanMentionPart(start: Int): Int {
        var end = start
        while (end < rangeEnd) {
            val current = source[end]
            if (!current.isMentionChar()) break
            end++
        }
        return end
    }

    private fun tryHashtag(): Boolean {
        if (source[cursor] != '#' || source.getOrNull(cursor - 1)?.isAsciiAlphanumeric() == true) return false
        var end = cursor + 1
        while (end < rangeEnd) {
            val balancedEnd = balancedHashtagItemEnd(end)
            if (balancedEnd >= 0) {
                end = balancedEnd
            } else if (!source[end].isHashtagExcluded()) {
                end++
            } else {
                break
            }
        }
        if (end == cursor + 1) return false
        val tag = source.substring(cursor + 1, end)
        if (tag.all { it in '0'..'9' }) return false
        emitNode(HashtagNode(tag), end)
        return true
    }

    private fun balancedHashtagItemEnd(start: Int): Int {
        val firstClose = source.getOrNull(start)?.hashtagClose() ?: return -1
        val closings = arrayListOf(firstClose)
        var index = start + 1
        while (index < rangeEnd) {
            val current = source[index]
            val nestedClose = current.hashtagClose()
            when {
                nestedClose != null -> {
                    if (initialDepth + frames.lastIndex + closings.size >= nestLimit) return -1
                    closings.add(nestedClose)
                }
                current == closings.last() -> {
                    closings.removeAt(closings.lastIndex)
                    if (closings.isEmpty()) return index + 1
                }
                current.isHashtagExcluded() -> return -1
            }
            index++
        }
        return -1
    }

    private fun tryUrl(): Boolean {
        val schemeLength =
            when {
                matches(cursor, "https://") -> 8
                matches(cursor, "http://") -> 7
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
        val rawEnd = end
        while (end > cursor + schemeLength && (source[end - 1] == '.' || source[end - 1] == ',')) end--
        if (end == cursor + schemeLength) {
            advanceTextTo(rawEnd)
            return true
        }
        emitNode(UrlNode(source.substring(cursor, end)), end)
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
                        return false
                    }
                }
            }
            index++
        }
        val labelEnd = if (index < rangeEnd && source[index] == ']' && source.getOrNull(index + 1) == '(') index else candidate
        if (labelEnd < 0 || labelEnd == labelStart) return false

        val hrefStart = labelEnd + 2
        val angle = source.getOrNull(hrefStart) == '<'
        val hrefEnd = if (angle) scanAngleUrlEnd(hrefStart) else scanUrlEnd(hrefStart)
        if (hrefEnd < 0 || source.getOrNull(hrefEnd) != ')') return false
        val rawEnd = hrefEnd + 1
        val href =
            if (angle) {
                source.substring(hrefStart + 1, hrefEnd - 1)
            } else {
                source.substring(hrefStart, hrefEnd)
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
                nestLimit = nestLimit,
                initialDepth = initialDepth + frames.lastIndex + 1,
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

    private fun scanUrlEnd(start: Int): Int {
        val schemeLength =
            when {
                matches(start, "https://") -> 8
                matches(start, "http://") -> 7
                else -> return -1
            }
        var end = start + schemeLength
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
        while (end > start + schemeLength && (source[end - 1] == '.' || source[end - 1] == ',')) end--
        return end.takeIf { it > start + schemeLength } ?: -1
    }

    private fun scanAngleUrlEnd(start: Int): Int {
        if (source.getOrNull(start) != '<') return -1
        val schemeEnd =
            when {
                matches(start + 1, "https://") -> start + 9
                matches(start + 1, "http://") -> start + 8
                else -> return -1
            }
        var end = schemeEnd
        while (end < rangeEnd && source[end] != '>' && !source[end].isMfmSpace()) end++
        return if (end > schemeEnd && source.getOrNull(end) == '>') end + 1 else -1
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
                '(' -> {
                    if (initialDepth + frames.lastIndex + closing.size >= nestLimit) return -1
                    closing.add(')')
                }
                '[' -> {
                    if (initialDepth + frames.lastIndex + closing.size >= nestLimit) return -1
                    closing.add(']')
                }
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

    private fun advanceTextTo(end: Int) {
        var index = cursor
        while (index < end) {
            val newlineLength = newlineLengthAt(index)
            if (newlineLength > 0) {
                lineContentStart = index + newlineLength
                lineHadNode = false
                index += newlineLength
                continue
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
        val index = source.indexOf(value, maxOf(start, rangeStart))
        return if (index >= 0 && index + value.length <= rangeEnd) index else -1
    }

    private fun newlineLengthAt(index: Int): Int =
        when {
            index !in rangeStart until rangeEnd -> 0
            source[index] == '\r' && source.getOrNull(index + 1) == '\n' && index + 1 < rangeEnd -> 2
            source[index] == '\r' || source[index] == '\n' -> 1
            else -> 0
        }

    private fun isLineBegin(index: Int): Boolean = index == rangeStart || previousNewlineStart(index) >= 0

    private fun isLineEnd(index: Int): Boolean = index == rangeEnd || newlineLengthAt(index) > 0

    private fun consumeBoundaryBefore(
        blockStart: Int,
        limit: Int,
    ) {
        var boundary = blockStart
        repeat(limit) {
            val previous = previousNewlineStart(boundary)
            if (previous < rangeStart) return@repeat
            boundary = previous
        }
        if (textStart < boundary) flushSource(boundary)
        textStart = blockStart
    }

    private fun consumeBoundaryAfter(
        blockEnd: Int,
        limit: Int,
    ): Int {
        var end = blockEnd
        repeat(limit) {
            val length = newlineLengthAt(end)
            if (length > 0) end += length
        }
        return end
    }

    private fun previousNewlineStart(index: Int): Int {
        if (index <= rangeStart) return -1
        return when {
            source[index - 1] == '\n' && index - 2 >= rangeStart && source[index - 2] == '\r' -> index - 2
            source[index - 1] == '\n' || source[index - 1] == '\r' -> index - 1
            else -> -1
        }
    }

    private fun isNestLimitReached(): Boolean =
        !emojiOnly &&
            (initialDepth != 0 || frames.lastIndex != 0) &&
            initialDepth + frames.lastIndex >= nestLimit

    private fun Char.isAsciiAlpha(): Boolean = this in 'A'..'Z' || this in 'a'..'z'

    private fun Char.isPlainAsciiLetter(): Boolean =
        (this in 'a'..'z' && this != 'h' && this != 's') ||
            (this in 'A'..'Z' && this != 'S')

    private fun Char.isAsciiAlphanumeric(): Boolean = isAsciiAlpha() || this in '0'..'9'

    private fun Char.isEmojiNameChar(): Boolean = isAsciiAlphanumeric() || this == '_' || this == '-' || this == '+'

    private fun Char.mayStartKeycapEmoji(): Boolean = this == '#' || this == '*' || this in '0'..'9'

    private fun Char.isEmphasisContent(): Boolean = isAsciiAlphanumeric() || isMfmSpace()

    private fun Char.isFnNameChar(): Boolean = isAsciiAlphanumeric() || this == '_'

    private fun Char.isFnValueChar(): Boolean = isFnNameChar() || this == '-' || this == '.'

    private fun Char.isMentionChar(): Boolean = isAsciiAlphanumeric() || this == '_' || this == '-' || this == '.'

    private fun Char.isMfmSpace(): Boolean = this == ' ' || this == '\t' || this == '\u3000'

    private fun Char.isTagWhitespace(): Boolean = isMfmSpace() || this == '\n' || this == '\r' || this == '\u000C'

    private fun Char.hashtagClose(): Char? =
        when (this) {
            '(' -> ')'
            '[' -> ']'
            '「' -> '」'
            '（' -> '）'
            else -> null
        }

    private fun Char.isHashtagExcluded(): Boolean =
            this == ' ' ||
            this == '\t' ||
            this == '\n' ||
            this == '\r' ||
            this == '\u3000' ||
            this in ".,!?'\"#:/[]【】()「」（）<>"

    private fun Char.isUrlChar(): Boolean =
        isAsciiAlphanumeric() || this in ".,_/:%#@\$&?!~=+-"
}
