package moe.tlaster.mfm.parser.tokenizer

internal sealed interface State {
    fun read(tokenizer: Tokenizer, reader: Reader)
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
private val hashTagExclude = "[ \u3000\t.,!?'\"#:/[]【】()「」（）<>]".toList() + eof

internal data object DataState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            ']' -> tokenizer.emit(FnEndBracketCharacter)
            'h' -> tokenizer.switch(HState)
            '?' -> tokenizer.switch(QuestionState)
            '[' -> tokenizer.switch(BracketOpenState)
            '<' -> tokenizer.switch(TagOpenState)
            '>' -> tokenizer.switch(BlockquoteState)
            '\\' -> tokenizer.switch(EscapeState)
            '~' -> tokenizer.switch(TildeState)
            '_' -> tokenizer.switch(UnderscoreState)
            '*' -> tokenizer.switch(AsteriskState)
            '`' -> tokenizer.switch(BacktickState)
            '$' -> tokenizer.switch(DollarState)
            '@' -> tokenizer.switch(AtState)
            '#' -> tokenizer.switch(HashState)
            ':' -> tokenizer.switch(ColonState)
            eof -> tokenizer.emit(EOFTokenCharacter)
            else -> tokenizer.emit(Character(current))
        }
    }
}

internal data object HState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        if (reader.isFollowedBy("ttps://", ignoreCase = true)) {
            tokenizer.emit(UrlCharacter('h'))
            tokenizer.emit(UrlCharacter('t'))
            tokenizer.emit(UrlCharacter('t'))
            tokenizer.emit(UrlCharacter('p'))
            tokenizer.emit(UrlCharacter('s'))
            tokenizer.emit(UrlCharacter(':'))
            tokenizer.emit(UrlCharacter('/'))
            tokenizer.emit(UrlCharacter('/'))
            tokenizer.switch(UrlState)
            reader.consume("ttps://".length)
        } else if (reader.isFollowedBy("ttp://", ignoreCase = true)) {
            tokenizer.emit(UrlCharacter('h'))
            tokenizer.emit(UrlCharacter('t'))
            tokenizer.emit(UrlCharacter('t'))
            tokenizer.emit(UrlCharacter('p'))
            tokenizer.emit(UrlCharacter(':'))
            tokenizer.emit(UrlCharacter('/'))
            tokenizer.emit(UrlCharacter('/'))
            tokenizer.switch(UrlState)
            reader.consume("ttp://".length)
        } else {
            tokenizer.emit(Character('h'))
        }
    }
}

internal data object UrlState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            in emptyChar + eof -> {
                tokenizer.accept()
                tokenizer.switch(DataState)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(UrlCharacter(current))
            }
        }
    }
}

internal data object QuestionState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            '[' -> {
                if (reader.next() !in listOf('[', ']', '<', '>')) {
                    tokenizer.emit(BracketCharacter('?'))
                    tokenizer.switch(BracketOpenState)
                } else {
                    tokenizer.emit(Character('?'))
                    tokenizer.switch(DataState)
                    reader.pushback()
                }
            }

            else -> {
                tokenizer.emit(Character('?'))
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object BracketOpenState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            in listOf('[', ']', '<', '>') -> {
                tokenizer.emit(Character('['))
                tokenizer.switch(DataState)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(BracketCharacter('['))
                tokenizer.switch(BracketBodyState)
                reader.pushback()
            }
        }
    }
}

internal data object BracketBodyState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            ']' -> {
                tokenizer.emit(BracketCharacter(']'))
                tokenizer.accept()
                if (reader.next() == '(') {
                    tokenizer.switch(RoundBracketOpenState)
                    reader.consume()
                } else {
                    tokenizer.switch(DataState)
                }
            }

            in listOf('[', '<') -> {
                tokenizer.reject()
                tokenizer.emit(Character('['))
                tokenizer.switch(DataState)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(BracketContentCharacter(current))
            }
        }
    }
}

internal data object RoundBracketOpenState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            in listOf('[', ']', '<', '>') -> {
                tokenizer.emit(Character('('))
                tokenizer.switch(DataState)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(RoundBracketCharacter('('))
                tokenizer.switch(RoundBracketBodyState)
                reader.pushback()
            }
        }
    }
}

internal data object RoundBracketBodyState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            ')' -> {
                tokenizer.emit(RoundBracketCharacter(')'))
                tokenizer.accept()
                tokenizer.switch(DataState)
            }

            else -> {
                tokenizer.emit(RoundBracketContentCharacter(current))
            }
        }
    }
}

internal data object TagOpenState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            '/' -> {
                tokenizer.emit(EndTagOpenCharacter('<'))
                tokenizer.emit(EndTagOpenCharacter('/'))
                tokenizer.switch(TagNameState)
            }

            in asciiAlpha -> {
                tokenizer.emit(TagOpenCharacter)
                tokenizer.switch(TagNameState)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(Character('<'))
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object TagNameState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            in emptyChar + eof -> {
                tokenizer.reject()
                tokenizer.switch(DataState)
                reader.pushback()
            }

            '>' -> {
                tokenizer.emit(TagCloseCharacter)
                tokenizer.accept()
                tokenizer.switch(DataState)
            }

            else -> {
                tokenizer.emit(TagCharacter(current))
            }
        }
    }
}

internal data object BlockquoteState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        tokenizer.emit(BlockquoteCharacter)
        tokenizer.switch(DataState)
    }
}

internal data object EscapeState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            '[' -> {
                // math block
                tokenizer.emit(MathBlockCharacter('\\'))
                tokenizer.emit(MathBlockCharacter('['))
                tokenizer.switch(MathBlockBodyState)
            }

            '(' -> {
                // inline math
                tokenizer.emit(InlineMathCharacter('\\'))
                tokenizer.emit(InlineMathCharacter('('))
                tokenizer.switch(InlineMathBodyState)
            }

            else -> {
                tokenizer.emit(Character('\\'))
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object InlineMathBodyState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            LF -> {
                tokenizer.reject()
                tokenizer.switch(DataState)
                reader.pushback()
            }

            '\\' -> {
                tokenizer.emit(InlineMathCharacter('\\'))
                tokenizer.switch(InlineMathBodyMightEndState)
            }

            else -> {
                tokenizer.emit(InlineMathContentCharacter(current))
            }
        }
    }
}

internal data object InlineMathBodyMightEndState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            ')' -> {
                tokenizer.emit(InlineMathCharacter(')'))
                tokenizer.accept()
                tokenizer.switch(DataState)
            }

            else -> {
                tokenizer.emit(InlineMathContentCharacter(current))
                tokenizer.switch(InlineMathBodyState)
            }
        }
    }
}

internal data object MathBlockBodyState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            '\\' -> {
                tokenizer.emit(MathBlockCharacter('\\'))
                tokenizer.switch(MathBlockBodyMightEndState)
            }

            else -> {
                tokenizer.emit(MathBlockContentCharacter(current))
            }
        }
    }
}

internal data object MathBlockBodyMightEndState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            ']' -> {
                tokenizer.emit(MathBlockCharacter(']'))
                tokenizer.accept()
                tokenizer.switch(DataState)
            }

            else -> {
                tokenizer.emit(MathBlockContentCharacter(current))
                tokenizer.switch(MathBlockBodyState)
            }
        }
    }
}

internal data object TildeState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            '~' -> {
                tokenizer.emit(StrikeCharacter)
                tokenizer.emit(StrikeCharacter)
                tokenizer.accept()
                tokenizer.switch(DataState)
            }

            else -> {
                tokenizer.emit(Character('~'))
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object UnderscoreState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        if (reader.position != 1) {
            reader.pushback(2) // push back 1 for char before _, 1 for _
            val before = reader.consume() // char before _
            reader.consume() // *
            if (before !in asciiAlphanumeric) {
                tokenizer.emit(Character('_'))
                tokenizer.switch(DataState)
                return
            }
        }
        when (val current = reader.consume()) {
            in asciiAlphanumeric + ' ' + TAB -> {
                tokenizer.switch(UnderscoreItalicState)
                tokenizer.emit(UnderscoreItalicStartCharacter)
                reader.pushback()
            }

            '_' -> {
                tokenizer.switch(UnderscoreBoldStartState)
            }

            else -> {
                tokenizer.emit(Character('_'))
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object UnderscoreBoldStartState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            in asciiAlphanumeric + ' ' + TAB -> {
                tokenizer.emit(UnderscoreBoldStartCharacter)
                tokenizer.emit(UnderscoreBoldStartCharacter)
                tokenizer.switch(UnderscoreBoldState)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(Character('_'))
                tokenizer.emit(Character('_'))
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object UnderscoreBoldState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            in asciiAlphanumeric + ' ' + TAB -> {
                tokenizer.emit(BoldCharacter(current))
            }

            '_' -> {
                tokenizer.switch(UnderscoreBoldEndState)
            }

            else -> {
                tokenizer.reject()
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object UnderscoreBoldEndState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            '_' -> {
                tokenizer.emit(UnderscoreBoldStartCharacter)
                tokenizer.emit(UnderscoreBoldStartCharacter)
                tokenizer.accept()
                tokenizer.switch(DataState)
            }

            in asciiAlphanumeric -> {
                tokenizer.emit(Character('_'))
                tokenizer.reject()
                tokenizer.switch(DataState)
                reader.pushback()
            }

            else -> {
                // TODO: fallback to italic
                tokenizer.emit(Character('_'))
                tokenizer.reject()
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object UnderscoreItalicState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            in asciiAlphanumeric + ' ' + TAB -> {
                tokenizer.emit(ItalicCharacter(current))
            }

            '_' -> {
                tokenizer.emit(UnderscoreItalicStartCharacter)
                tokenizer.accept()
                tokenizer.switch(DataState)
            }

            else -> {
                tokenizer.reject()
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object AsteriskState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        if (reader.position != 1) {
            reader.pushback(2) // push back 1 for char before *, 1 for *
            val before = reader.consume() // char before *
            reader.consume() // *
            if (before !in asciiAlphanumeric) {
                tokenizer.emit(Character('*'))
                tokenizer.switch(DataState)
                return
            }
        }
        when (val current = reader.consume()) {
            in asciiAlphanumeric + ' ' + TAB -> {
                tokenizer.switch(AsteriskItalicState)
                tokenizer.emit(AsteriskItalicStartCharacter)
                reader.pushback()
            }

            '*' -> {
                tokenizer.switch(AsteriskBoldStartState)
            }

            else -> {
                tokenizer.emit(Character('*'))
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object AsteriskBoldStartState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            in asciiAlphanumeric + ' ' + TAB -> {
                tokenizer.emit(AsteriskBoldStartCharacter)
                tokenizer.emit(AsteriskBoldStartCharacter)
                tokenizer.switch(AsteriskBoldState)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(Character('*'))
                tokenizer.emit(Character('*'))
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object AsteriskBoldState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            in asciiAlphanumeric + ' ' + TAB -> {
                tokenizer.emit(BoldCharacter(current))
            }

            '*' -> {
                tokenizer.switch(AsteriskBoldEndState)
            }

            else -> {
                tokenizer.reject()
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object AsteriskBoldEndState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            '*' -> {
                tokenizer.emit(AsteriskBoldStartCharacter)
                tokenizer.emit(AsteriskBoldStartCharacter)
                tokenizer.accept()
                tokenizer.switch(DataState)
            }

            in asciiAlphanumeric -> {
                tokenizer.emit(Character('*'))
                tokenizer.reject()
                tokenizer.switch(DataState)
                reader.pushback()
            }

            else -> {
                // TODO: fallback to italic
                tokenizer.emit(Character('*'))
                tokenizer.reject()
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object AsteriskItalicState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            in asciiAlphanumeric + ' ' + TAB -> {
                tokenizer.emit(ItalicCharacter(current))
            }

            '*' -> {
                tokenizer.emit(AsteriskItalicStartCharacter)
                tokenizer.accept()
                tokenizer.switch(DataState)
            }

            else -> {
                tokenizer.reject()
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object BacktickState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            '`' -> {
                if (reader.hasNext() && reader.next() == '`') {
                    // code block
                    tokenizer.switch(CodeBlockOpenState)
                    tokenizer.emit(CodeBlockStartCharacter)
                    tokenizer.emit(CodeBlockStartCharacter)
                    tokenizer.emit(CodeBlockStartCharacter)
                    reader.consume() // consume next `
                } else {
                    tokenizer.emit(Character('`'))
                    tokenizer.emit(Character('`'))
                    tokenizer.switch(DataState)
                }
            }

            LF -> {
                tokenizer.emit(Character('`'))
                tokenizer.switch(DataState)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(InlineCodeStartCharacter)
                tokenizer.switch(InlineCodeState)
                reader.pushback()
            }
        }
    }
}

internal data object CodeBlockOpenState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            in asciiAlphanumericUnderscore -> {
                // language
                tokenizer.emit(CodeBlockLanguageCharacter(current))
            }

            LF -> {
                // code body
                tokenizer.emit(CodeBlockLanguageEndCharacter)
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
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            '`' -> {
                tokenizer.emit(CodeBlockStartCharacter)
                tokenizer.switch(CodeBlockBodyMightEndState)
            }

            eof -> {
                tokenizer.reject()
                tokenizer.switch(DataState)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(CodeBlockCharacter(current))
            }
        }
    }
}

internal data object CodeBlockBodyMightEndState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            '`' -> {
                tokenizer.emit(CodeBlockStartCharacter)
                if (reader.hasNext() && reader.next() == '`') {
                    tokenizer.emit(CodeBlockStartCharacter)
                    tokenizer.accept()
                    tokenizer.switch(DataState)
                    reader.consume() // consume next `
                } else {
                    tokenizer.switch(CodeBlockBodyState)
                }
            }

            eof -> {
                tokenizer.reject()
                tokenizer.switch(DataState)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(CodeBlockCharacter(current))
                tokenizer.switch(CodeBlockBodyState)
            }
        }
    }
}

internal data object InlineCodeState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            LF -> {
                tokenizer.reject()
                tokenizer.switch(DataState)
                reader.pushback()
            }

            '`' -> {
                // end inlinecode
                tokenizer.emit(InlineCodeStartCharacter)
                tokenizer.accept()
                tokenizer.switch(DataState)
            }

            eof -> {
                tokenizer.reject()
                tokenizer.switch(DataState)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(InlineCodeCharacter(current))
            }
        }
    }
}

internal data object DollarState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            in asciiAlphanumeric -> {
                tokenizer.emit(CashStartCharacter)
                tokenizer.switch(CashTagState)
                reader.pushback()
            }
            // TODO: fn
            '[' -> {
                tokenizer.switch(FnOpenState)
            }

            else -> {
                tokenizer.emit(Character('$'))
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object FnOpenState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            in asciiAlphanumeric -> {
                tokenizer.emit(FnCharacter('$'))
                tokenizer.emit(FnCharacter('['))
                tokenizer.switch(FnNameState)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(Character('$'))
                tokenizer.emit(Character('['))
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object FnNameState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            in emptyChar -> {
                // ignore empty char since we're accepting
//                tokenizer.emit(FnCharacter(current))
                tokenizer.accept()
                tokenizer.switch(DataState)
            }
            in asciiAlphanumericUnderscore -> {
                tokenizer.emit(FnContentCharacter(current))
            }
            in listOf('.', ',', '=') -> {
                if (reader.next() in asciiAlphanumericUnderscore) {
                    tokenizer.emit(FnContentCharacter(current))
                } else {
                    tokenizer.reject()
                    tokenizer.switch(DataState)
                    reader.pushback()
                }
            }
        }
    }
}

internal data object CashTagState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            in asciiAlphanumeric -> {
                tokenizer.emit(CashCharacter(current))
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
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        if (reader.position != 1) {
            reader.pushback(2) // push back 1 for char before @, 1 for @
            val before = reader.consume() // char before @
            reader.consume() // @
            if (before !in asciiAlphanumeric) {
                tokenizer.emit(Character('@'))
                tokenizer.switch(DataState)
                return
            }
        }
        when (val current = reader.consume()) {
            in asciiAlphanumericUnderscoreDash -> {
                tokenizer.emit(UserAtCharacter)
                tokenizer.switch(UserNameState)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(Character('@'))
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}

internal data object UserNameState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            in asciiAlphanumericUnderscore -> {
                tokenizer.emit(UserNameCharacter(current))
            }

            '-' -> {
                if (reader.hasNext() && reader.next() !in asciiAlphanumericUnderscore + '@') {
                    tokenizer.accept()
                    tokenizer.switch(DataState)
                    reader.pushback()
                } else {
                    tokenizer.emit(UserNameCharacter(current))
                }
            }

            '@' -> {
                if (reader.hasNext() && reader.next() in asciiAlphanumericUnderscore) {
                    tokenizer.emit(UserAtCharacter)
                    tokenizer.switch(UserHostState)
                } else {
                    tokenizer.emit(Character('@'))
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
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            in asciiAlphanumericUnderscore -> {
                tokenizer.emit(UserHostCharacter(current))
            }

            '-', '.' -> {
                if (reader.hasNext() && reader.next() !in asciiAlphanumericUnderscore) {
                    tokenizer.accept()
                    tokenizer.switch(DataState)
                    reader.pushback()
                } else {
                    tokenizer.emit(UserHostCharacter(current))
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
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        if (reader.position != 1) {
            reader.pushback(2) // push back 1 for char before #, 1 for #
            val before = reader.consume() // char before #
            reader.consume() // #
            if (before !in asciiAlphanumeric) {
                tokenizer.emit(Character('#'))
                tokenizer.switch(DataState)
                return
            }
        }

        when (val current = reader.consume()) {
            in hashTagExclude -> {
                // TODO: 括弧は対になっている時のみ内容に含めることができる。対象: () [] 「」 （）
                tokenizer.emit(Character('#'))
                tokenizer.emit(Character(current))
                tokenizer.switch(DataState)
            }

            else -> {
                tokenizer.emit(HashTagStartCharacter)
                tokenizer.switch(HashNameState)
                reader.pushback()
            }
        }
    }
}

internal data object HashNameState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            in hashTagExclude -> {
                // TODO: 括弧は対になっている時のみ内容に含めることができる。対象: () [] 「」 （）
                tokenizer.accept()
                tokenizer.switch(DataState)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(HashTagCharacter(current))
            }
        }
    }
}

internal data object ColonState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            in asciiAlphanumericUnderscoreDashPlus -> {
                tokenizer.switch(EmojiNameState)
                tokenizer.emit(EmojiNameStartCharacter)
                reader.pushback()
            }

            else -> {
                tokenizer.emit(Character(':'))
                tokenizer.emit(Character(current))
                tokenizer.switch(DataState)
            }
        }
    }
}

internal data object EmojiNameState : State {
    override fun read(tokenizer: Tokenizer, reader: Reader) {
        when (val current = reader.consume()) {
            in asciiAlphanumericUnderscoreDashPlus -> {
                tokenizer.emit(EmojiNameCharacter(current))
            }

            ':' -> {
                tokenizer.emit(EmojiNameStartCharacter)
                tokenizer.accept()
                tokenizer.switch(DataState)
            }

            else -> {
                tokenizer.reject()
                tokenizer.switch(DataState)
                reader.pushback()
            }
        }
    }
}
