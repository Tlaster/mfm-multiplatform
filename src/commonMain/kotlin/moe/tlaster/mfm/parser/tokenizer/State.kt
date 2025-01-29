package moe.tlaster.mfm.parser.tokenizer

internal sealed interface State {
    fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    )
}

private fun prevIsNotAsciiAlphanumeric(
    tokenizer: Tokenizer,
    reader: Reader,
): Boolean {
    if (reader.position != 2) {
        reader.pushback(3) // push back 1 for char before *, 1 for *
        val before = reader.consume() // char before *
        reader.consume() // *
        if (before !in asciiAlphanumericAndEmpty) {
            tokenizer.emit(TokenCharacterType.Character, reader.position)
            tokenizer.switch(DataState)
            return true
        } else {
            reader.consume() // back to current char
        }
    }
    return false
}

private val asciiUppercase = 'A'..'Z'
private val asciiLowercase = 'a'..'z'
private val asciiAlpha = asciiUppercase + asciiLowercase
private val asciiDigit = '0'..'9'
private val asciiAlphanumeric = asciiAlpha + asciiDigit
private val asciiAlphanumericUnderscore = asciiAlphanumeric + '_'
private val asciiAlphanumericUnderscoreDash = asciiAlphanumericUnderscore + '-'
private val asciiAlphanumericUnderscoreDashPlus = asciiAlphanumericUnderscoreDash + '+'
private val asciiUpperHexDigit = 'A'..'F'
private val asciiLowerHexDigit = 'a'..'f'
private val asciiHexDigit = asciiUpperHexDigit + asciiLowerHexDigit
private const val NULL = '\u0000'
private const val TAB = '\u0009'
private const val LF = '\u000A'
private val emptyChar = listOf(TAB, LF, '\u000C', '\u0020')
private const val FULLWIDTHSPACE = '\u3000'
private val hashTagExclude = "[ \t.,!?'\"#:/[]【】()「」（）<>]".toList() + EOF + emptyChar + FULLWIDTHSPACE
private val asciiAlphanumericAndEmpty = asciiAlphanumeric + ' ' + TAB + LF + FULLWIDTHSPACE

internal data object DataState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            ']' if !tokenizer.emojiOnly -> tokenizer.emit(TokenCharacterType.FnEndBracket, reader.position)
            'h' if !tokenizer.emojiOnly -> tokenizer.switch(HState)
            '?' if !tokenizer.emojiOnly -> tokenizer.switch(QuestionState)
            '[' if !tokenizer.emojiOnly -> tokenizer.switch(BracketOpenState)
            '<' if !tokenizer.emojiOnly -> tokenizer.switch(TagOpenState)
            '>' if !tokenizer.emojiOnly -> tokenizer.switch(BlockquoteState)
            '\\' if !tokenizer.emojiOnly -> tokenizer.switch(EscapeState)
            '~' if !tokenizer.emojiOnly -> tokenizer.switch(TildeState)
            '_' if !tokenizer.emojiOnly -> tokenizer.switch(UnderscoreState)
            '*' if !tokenizer.emojiOnly -> tokenizer.switch(AsteriskState)
            '`' if !tokenizer.emojiOnly -> tokenizer.switch(BacktickState)
            '$' if !tokenizer.emojiOnly -> tokenizer.switch(DollarState)
            '@' if !tokenizer.emojiOnly -> tokenizer.switch(AtState)
            '#' if !tokenizer.emojiOnly -> tokenizer.switch(HashState)
            ':' -> tokenizer.switch(ColonState)
            EOF -> tokenizer.emit(TokenCharacterType.Eof, reader.position)
            LF -> tokenizer.emit(TokenCharacterType.LineBreak, reader.position)
            else -> tokenizer.emit(TokenCharacterType.Character, reader.position)
        }
    }
}

internal data object HState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        if (reader.isFollowedBy("ttps://", ignoreCase = true)) {
            tokenizer.emitRange(TokenCharacterType.Url, reader.position - 1, reader.position - 1 + "https://".length)
            tokenizer.switch(UrlState)
            reader.consume("ttps://".length)
        } else if (reader.isFollowedBy("ttp://", ignoreCase = true)) {
            tokenizer.emitRange(TokenCharacterType.Url, reader.position - 1, reader.position - 1 + "http://".length)
            tokenizer.switch(UrlState)
            reader.consume("ttp://".length)
        } else {
            tokenizer.emit(TokenCharacterType.Character, reader.position)
            tokenizer.switch(DataState)
        }
    }
}

internal data object UrlState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            in emptyChar + EOF -> {
                tokenizer.accept()
                tokenizer.switch(DataState)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(TokenCharacterType.Url, reader.position)
            }
        }
    }
}

internal data object QuestionState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            '[' -> {
                if (reader.isFollowedBy("Search]", ignoreCase = true)) {
                    tokenizer.emit(TokenCharacterType.Character, reader.position - 1)
                    tokenizer.emitRange(TokenCharacterType.Search, reader.position - 1, reader.position - 1 + "[Search]".length)
                    tokenizer.accept()
                    tokenizer.switch(DataState)
                    reader.consume("Search]".length)
                } else if (reader.isFollowedBy("検索]")) {
                    tokenizer.emit(TokenCharacterType.Character, reader.position - 1)
                    tokenizer.emitRange(TokenCharacterType.Search, reader.position - 1, reader.position - 1 + "[検索]".length)
                    tokenizer.accept()
                    tokenizer.switch(DataState)
                    reader.consume("検索]".length)
                } else {
                    // link
                    tokenizer.emit(TokenCharacterType.SilentLink, reader.position - 1)
                    tokenizer.emit(TokenCharacterType.LinkOpen, reader.position)
                    tokenizer.switch(LinkNameState)
                }
            }

            else -> {
                tokenizer.emit(TokenCharacterType.Character, reader.position - 1)
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object LinkNameState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        var stackCount = 0
        while (reader.hasNext()) {
            when (val current = reader.consume()) {
                '[' -> {
                    stackCount++
                    tokenizer.emit(TokenCharacterType.LinkContent, reader.position)
                }
                ']' -> {
                    if (stackCount == 0) {
                        if (reader.next() == '(') {
                            tokenizer.emit(TokenCharacterType.LinkClose, reader.position)
                            tokenizer.emit(TokenCharacterType.LinkHrefOpen, reader.position + 1)
                            tokenizer.switch(LinkHrefState)
                            reader.consume()
                            break
                        } else {
                            tokenizer.reject(reader.position)
                            tokenizer.emit(TokenCharacterType.Character, reader.position)
                            tokenizer.switch(DataState)
                            break
                        }
                    } else {
                        stackCount--
                        tokenizer.emit(TokenCharacterType.LinkContent, reader.position)
                    }
                }
                in listOf(LF, EOF) -> {
                    if (stackCount == 0 || current == EOF) {
                        tokenizer.reject(reader.position)
                        tokenizer.switch(DataState)
                        reader.pushback()
                        break
                    } else {
                        tokenizer.emit(TokenCharacterType.LinkContent, reader.position)
                    }
                }
                else -> {
                    tokenizer.emit(TokenCharacterType.LinkContent, reader.position)
                }
            }
        }
    }
}

internal data object LinkHrefState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            ')' -> {
                tokenizer.emit(TokenCharacterType.LinkHrefClose, reader.position)
                tokenizer.accept()
                tokenizer.switch(DataState)
            }

            in emptyChar + EOF -> {
                tokenizer.reject(reader.position)
                tokenizer.switch(DataState)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(TokenCharacterType.LinkHref, reader.position)
            }
        }
    }
}

internal data object BracketOpenState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        if (reader.isFollowedBy("Search]", ignoreCase = true)) {
            tokenizer.emitRange(TokenCharacterType.Search, reader.position - 1, reader.position - 1 + "[Search]".length)
            tokenizer.accept()
            tokenizer.switch(DataState)
            reader.consume("Search]".length)
        } else if (reader.isFollowedBy("検索]")) {
            tokenizer.emitRange(TokenCharacterType.Search, reader.position - 1, reader.position - 1 + "[検索]".length)
            tokenizer.accept()
            tokenizer.switch(DataState)
            reader.consume("検索]".length)
        } else {
            when (val current = reader.consume()) {
                in listOf('[', ']', '<', '>') -> {
                    tokenizer.emit(TokenCharacterType.Character, reader.position - 1)
                    tokenizer.switch(DataState)
                    reader.pushback()
                }

                else -> {
                    tokenizer.emit(TokenCharacterType.LinkOpen, reader.position - 1)
                    tokenizer.switch(LinkNameState)
                    reader.pushback()
                }
            }
        }
    }
}

internal data object TagOpenState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            '/' -> {
                tokenizer.emit(TokenCharacterType.EndTagOpen, reader.position - 1)
                tokenizer.emit(TokenCharacterType.EndTagOpen, reader.position)
                tokenizer.switch(TagNameState)
            }

            in asciiAlpha -> {
                tokenizer.emit(TokenCharacterType.TagOpen, reader.position - 1)
                tokenizer.switch(TagNameState)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(TokenCharacterType.Character, reader.position - 1)
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object TagNameState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            in emptyChar + EOF -> {
                tokenizer.reject(reader.position)
                tokenizer.switch(DataState)
                reader.pushback()
            }

            '>' -> {
                tokenizer.emit(TokenCharacterType.TagClose, reader.position)
                tokenizer.accept()
                tokenizer.switch(DataState)
            }

            else -> {
                tokenizer.emit(TokenCharacterType.Tag, reader.position)
            }
        }
    }
}

internal data object BlockquoteState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        tokenizer.emit(TokenCharacterType.Blockquote, reader.position)
        if (reader.next() == ' ') {
            reader.consume()
            tokenizer.emit(TokenCharacterType.Blockquote, reader.position)
        }
        tokenizer.switch(DataState)
    }
}

internal data object EscapeState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            '[' -> {
                // math block
                tokenizer.emit(TokenCharacterType.MathBlock, reader.position - 1)
                tokenizer.emit(TokenCharacterType.MathBlock, reader.position)
                tokenizer.switch(MathBlockBodyState)
            }

            '(' -> {
                // inline math
                tokenizer.emit(TokenCharacterType.InlineMath, reader.position - 1)
                tokenizer.emit(TokenCharacterType.InlineMath, reader.position)
                tokenizer.switch(InlineMathBodyState)
            }

            else -> {
                tokenizer.emit(TokenCharacterType.Character, reader.position - 1)
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object InlineMathBodyState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            LF -> {
                tokenizer.reject(reader.position)
                tokenizer.switch(DataState)
                reader.pushback()
            }

            '\\' -> {
                tokenizer.emit(TokenCharacterType.InlineMath, reader.position)
                tokenizer.switch(InlineMathBodyMightEndState)
            }

            else -> {
                tokenizer.emit(TokenCharacterType.InlineMathContent, reader.position)
            }
        }
    }
}

internal data object InlineMathBodyMightEndState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            ')' -> {
                tokenizer.emit(TokenCharacterType.InlineMath, reader.position)
                tokenizer.accept()
                tokenizer.switch(DataState)
            }

            else -> {
                tokenizer.emit(TokenCharacterType.InlineMathContent, reader.position)
                tokenizer.switch(InlineMathBodyState)
            }
        }
    }
}

internal data object MathBlockBodyState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            '\\' -> {
                tokenizer.emit(TokenCharacterType.MathBlock, reader.position)
                tokenizer.switch(MathBlockBodyMightEndState)
            }

            else -> {
                tokenizer.emit(TokenCharacterType.MathBlockContent, reader.position)
            }
        }
    }
}

internal data object MathBlockBodyMightEndState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            ']' -> {
                tokenizer.emit(TokenCharacterType.MathBlock, reader.position)
                tokenizer.accept()
                tokenizer.switch(DataState)
            }

            else -> {
                tokenizer.emit(TokenCharacterType.MathBlockContent, reader.position)
                tokenizer.switch(MathBlockBodyState)
            }
        }
    }
}

internal data object TildeState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            '~' -> {
                tokenizer.emit(TokenCharacterType.Strike, reader.position - 1)
                tokenizer.emit(TokenCharacterType.Strike, reader.position)
                tokenizer.accept()
                tokenizer.switch(DataState)
            }

            else -> {
                tokenizer.emit(TokenCharacterType.Character, reader.position - 1)
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object UnderscoreState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            in asciiAlphanumericAndEmpty -> {
                if (prevIsNotAsciiAlphanumeric(tokenizer, reader)) {
                    return
                }
                tokenizer.switch(UnderscoreItalicState)
                tokenizer.emit(TokenCharacterType.UnderscoreItalicStart, reader.position - 1)
                reader.pushback()
            }

            '_' -> {
                tokenizer.switch(UnderscoreBoldStartState)
            }

            else -> {
                tokenizer.emit(TokenCharacterType.Character, reader.position - 1)
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object UnderscoreBoldStartState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            in asciiAlphanumericAndEmpty -> {
                tokenizer.emit(TokenCharacterType.UnderscoreBoldStart, reader.position - 2)
                tokenizer.emit(TokenCharacterType.UnderscoreBoldStart, reader.position - 1)
                tokenizer.switch(UnderscoreBoldState)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(TokenCharacterType.Character, reader.position - 2)
                tokenizer.emit(TokenCharacterType.Character, reader.position - 1)
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object UnderscoreBoldState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            in asciiAlphanumericAndEmpty -> {
                tokenizer.emit(TokenCharacterType.Bold, reader.position)
            }

            '_' -> {
                tokenizer.switch(UnderscoreBoldEndState)
            }

            else -> {
                tokenizer.reject(reader.position)
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object UnderscoreBoldEndState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            '_' -> {
                tokenizer.emit(TokenCharacterType.UnderscoreBoldStart, reader.position - 1)
                tokenizer.emit(TokenCharacterType.UnderscoreBoldStart, reader.position)
                tokenizer.accept()
                tokenizer.switch(DataState)
            }

            in asciiAlphanumeric -> {
                tokenizer.reject(reader.position)
                tokenizer.emit(TokenCharacterType.Character, reader.position - 1)
                tokenizer.switch(DataState)
                reader.pushback()
            }

            else -> {
                // TODO: fallback to italic
                tokenizer.reject(reader.position)
                tokenizer.emit(TokenCharacterType.Character, reader.position - 1)
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object UnderscoreItalicState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            in asciiAlphanumericAndEmpty -> {
                tokenizer.emit(TokenCharacterType.Italic, reader.position)
            }

            '_' -> {
                tokenizer.emit(TokenCharacterType.UnderscoreItalicStart, reader.position)
                tokenizer.accept()
                tokenizer.switch(DataState)
            }

            else -> {
                tokenizer.reject(reader.position)
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object AsteriskState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            in asciiAlphanumericAndEmpty -> {
                if (prevIsNotAsciiAlphanumeric(tokenizer, reader)) {
                    return
                }
                tokenizer.emit(TokenCharacterType.AsteriskItalicStart, reader.position - 1)
                tokenizer.switch(AsteriskItalicState)
                reader.pushback()
            }

            '*' -> {
                tokenizer.emit(TokenCharacterType.AsteriskBold, reader.position - 1)
                tokenizer.emit(TokenCharacterType.AsteriskBold, reader.position)
                tokenizer.accept()
                tokenizer.switch(DataState)
            }

            else -> {
                tokenizer.emit(TokenCharacterType.Character, reader.position - 1)
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object AsteriskItalicState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            in asciiAlphanumericAndEmpty -> {
                tokenizer.emit(TokenCharacterType.Italic, reader.position)
            }

            '*' -> {
                tokenizer.emit(TokenCharacterType.AsteriskItalicStart, reader.position)
                tokenizer.accept()
                tokenizer.switch(DataState)
            }

            else -> {
                tokenizer.reject(reader.position)
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object BacktickState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            '`' -> {
                if (reader.hasNext() && reader.next() == '`') {
                    // code block
                    tokenizer.switch(CodeBlockOpenState)
                    tokenizer.emitRange(TokenCharacterType.CodeBlockStart, reader.position - 2, reader.position + 1)
                    reader.consume() // consume next `
                } else {
                    tokenizer.emit(TokenCharacterType.Character, reader.position - 1)
                    tokenizer.emit(TokenCharacterType.Character, reader.position)
                    tokenizer.switch(DataState)
                }
            }

            LF -> {
                tokenizer.emit(TokenCharacterType.Character, reader.position - 1)
                tokenizer.switch(DataState)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(TokenCharacterType.InlineCodeStart, reader.position - 1)
                tokenizer.switch(InlineCodeState)
                reader.pushback()
            }
        }
    }
}

internal data object CodeBlockOpenState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            in asciiAlphanumericUnderscore -> {
                // language
                tokenizer.emit(TokenCharacterType.CodeBlockLanguage, reader.position)
            }

            LF -> {
                // code body
                tokenizer.emit(TokenCharacterType.CodeBlockLanguageEnd, reader.position)
                tokenizer.switch(CodeBlockBodyState)
            }

            else -> {
                // code body
                tokenizer.switch(CodeBlockBodyState)
                reader.pushback()
            }
        }
    }
}

internal data object CodeBlockBodyState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            '`' -> {
                tokenizer.emit(TokenCharacterType.CodeBlockStart, reader.position)
                tokenizer.switch(CodeBlockBodyMightEndState)
            }

            EOF -> {
                tokenizer.reject(reader.position)
                tokenizer.switch(DataState)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(TokenCharacterType.CodeBlock, reader.position)
            }
        }
    }
}

internal data object CodeBlockBodyMightEndState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            '`' -> {
                tokenizer.emit(TokenCharacterType.CodeBlockStart, reader.position)
                if (reader.hasNext() && reader.next() == '`') {
                    tokenizer.emit(TokenCharacterType.CodeBlockStart, reader.position + 1)
                    tokenizer.accept()
                    tokenizer.switch(DataState)
                    reader.consume() // consume next `
                } else {
                    tokenizer.switch(CodeBlockBodyState)
                }
            }

            EOF -> {
                tokenizer.reject(reader.position)
                tokenizer.switch(DataState)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(TokenCharacterType.CodeBlock, reader.position)
                tokenizer.switch(CodeBlockBodyState)
            }
        }
    }
}

internal data object InlineCodeState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            LF -> {
                tokenizer.reject(reader.position)
                tokenizer.switch(DataState)
                reader.pushback()
            }

            '`' -> {
                // end inlinecode
                tokenizer.emit(TokenCharacterType.InlineCodeStart, reader.position)
                tokenizer.accept()
                tokenizer.switch(DataState)
            }

            EOF -> {
                tokenizer.reject(reader.position)
                tokenizer.switch(DataState)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(TokenCharacterType.InlineCode, reader.position)
            }
        }
    }
}

internal data object DollarState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            in asciiAlphanumeric -> {
                tokenizer.emit(TokenCharacterType.CashStart, reader.position - 1)
                tokenizer.switch(CashTagState)
                reader.pushback()
            }
            // TODO: fn
            '[' -> {
                tokenizer.switch(FnOpenState)
            }

            else -> {
                tokenizer.emit(TokenCharacterType.Character, reader.position - 1)
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object FnOpenState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            in asciiAlphanumeric -> {
                tokenizer.emit(TokenCharacterType.Fn, reader.position - 2)
                tokenizer.emit(TokenCharacterType.Fn, reader.position - 1)
                tokenizer.switch(FnNameState)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(TokenCharacterType.Character, reader.position - 2)
                tokenizer.emit(TokenCharacterType.Character, reader.position - 1)
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object FnNameState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        val vaildFnContent = asciiAlphanumericUnderscoreDash + '.'
        when (val current = reader.consume()) {
            in emptyChar -> {
                tokenizer.emit(TokenCharacterType.Fn, reader.position)
                tokenizer.accept()
                tokenizer.switch(DataState)
            }

            in vaildFnContent -> {
                tokenizer.emit(TokenCharacterType.FnContent, reader.position)
            }

            in listOf('.', ',', '=') -> {
                if (reader.next() in vaildFnContent) {
                    tokenizer.emit(TokenCharacterType.FnContent, reader.position)
                } else {
                    tokenizer.reject(reader.position)
                    tokenizer.switch(DataState)
                    reader.pushback()
                }
            }
            ']' -> {
                // TODO: fallback to link
                tokenizer.reject(reader.position)
                tokenizer.switch(DataState)
                reader.pushback()
            }
            else -> {
                tokenizer.reject(reader.position)
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object CashTagState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            in asciiAlphanumeric -> {
                tokenizer.emit(TokenCharacterType.Cash, reader.position)
            }

            else -> {
                tokenizer.accept()
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object AtState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
//        if (reader.position != 1) {
//            reader.pushback(2) // push back 1 for char before @, 1 for @
//            val before = reader.consume() // char before @
//            reader.consume() // @
//            if (before !in asciiAlphanumericAndEmpty) {
//                tokenizer.emit(TokenCharacterType.Character, reader.position - 1)
//                tokenizer.switch(DataState)
//                return
//            }
//        }
        when (val current = reader.consume()) {
            in asciiAlphanumericUnderscoreDash -> {
//                if (prevIsNotAsciiAlphanumeric(tokenizer, reader)) {
//                    return
//                }
                tokenizer.emit(TokenCharacterType.UserAt, reader.position - 1)
                tokenizer.switch(UserNameState)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(TokenCharacterType.Character, reader.position - 1)
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object UserNameState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            in asciiAlphanumericUnderscore -> {
                tokenizer.emit(TokenCharacterType.UserName, reader.position)
            }

            '-' -> {
                if (reader.hasNext() && reader.next() !in asciiAlphanumericUnderscore + '@') {
                    tokenizer.accept()
                    tokenizer.switch(DataState)
                    reader.pushback()
                } else {
                    tokenizer.emit(TokenCharacterType.UserName, reader.position)
                }
            }

            '@' -> {
                if (reader.hasNext() && reader.next() in asciiAlphanumericUnderscore) {
                    tokenizer.emit(TokenCharacterType.UserAt, reader.position)
                    tokenizer.switch(UserHostState)
                } else {
                    tokenizer.emit(TokenCharacterType.Character, reader.position)
                    tokenizer.switch(DataState)
                }
            }

            else -> {
                tokenizer.accept()
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object UserHostState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            in asciiAlphanumericUnderscore -> {
                tokenizer.emit(TokenCharacterType.UserHost, reader.position)
            }

            '-', '.' -> {
                if (reader.hasNext() && reader.next() !in asciiAlphanumericUnderscore) {
                    tokenizer.accept()
                    tokenizer.switch(DataState)
                    reader.pushback()
                } else {
                    tokenizer.emit(TokenCharacterType.UserHost, reader.position)
                }
            }

            else -> {
                tokenizer.accept()
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object HashState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
//        if (reader.position != 1) {
//            reader.pushback(2) // push back 1 for char before #, 1 for #
//            val before = reader.consume() // char before #
//            reader.consume() // #
//            if (before !in asciiAlphanumericAndEmpty) {
//                tokenizer.emit(TokenCharacterType.Character, reader.position - 1)
//                tokenizer.switch(DataState)
//                return
//            }
//        }

        when (val current = reader.consume()) {
            in hashTagExclude -> {
                if (prevIsNotAsciiAlphanumeric(tokenizer, reader)) {
                    return
                }
                // TODO: 括弧は対になっている時のみ内容に含めることができる。対象: () [] 「」 （）
                tokenizer.emit(TokenCharacterType.Character, reader.position - 1)
                tokenizer.emit(TokenCharacterType.Character, reader.position)
                tokenizer.switch(DataState)
            }

            else -> {
                if (prevIsNotAsciiAlphanumeric(tokenizer, reader)) {
                    return
                }
                tokenizer.emit(TokenCharacterType.HashTagStart, reader.position - 1)
                tokenizer.switch(HashNameState)
                reader.pushback()
            }
        }
    }
}

internal data object HashNameState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            in hashTagExclude -> {
                // TODO: 括弧は対になっている時のみ内容に含めることができる。対象: () [] 「」 （）
                tokenizer.accept()
                tokenizer.switch(DataState)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(TokenCharacterType.HashTag, reader.position)
            }
        }
    }
}

internal data object ColonState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            in asciiAlphanumericUnderscoreDashPlus -> {
                tokenizer.switch(EmojiNameState)
                tokenizer.emit(TokenCharacterType.EmojiNameStart, reader.position - 1)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(TokenCharacterType.Character, reader.position - 1)
                tokenizer.emit(TokenCharacterType.Character, reader.position)
                tokenizer.switch(DataState)
            }
        }
    }
}

internal data object EmojiNameState : State {
    override fun read(
        tokenizer: Tokenizer,
        reader: Reader,
    ) {
        when (val current = reader.consume()) {
            in asciiAlphanumericUnderscoreDashPlus -> {
                tokenizer.emit(TokenCharacterType.EmojiName, reader.position)
            }

            ':' -> {
                tokenizer.emit(TokenCharacterType.EmojiNameStart, reader.position)
                tokenizer.accept()
                tokenizer.switch(DataState)
            }

            else -> {
                tokenizer.reject(reader.position)
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}
