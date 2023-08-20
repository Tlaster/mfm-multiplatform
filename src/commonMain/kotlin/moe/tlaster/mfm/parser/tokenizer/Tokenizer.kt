package moe.tlaster.mfm.parser.tokenizer

internal interface Tokenizer {
    fun parse(reader: Reader)
    fun emit(tokenCharacter: TokenCharacter)
    fun switch(state: State)
    // if last token builder is not text, fallback to text
    fun reject()
    // build last token builder
    fun accept()
}

internal class MFMTokenizer : Tokenizer {
    private var currentState: State = DataState

    override fun parse(reader: Reader) {
        while (reader.hasNext()) {
            currentState.read(this, reader)
        }
    }

    override fun emit(tokenCharacter: TokenCharacter) {
        when (tokenCharacter) {
            BlockquoteCharacter -> TODO()
            is BoldCharacter -> TODO()
            is BracketCharacter -> TODO()
            is CashCharacter -> TODO()
            is Character -> TODO()
            is CodeBlockCharacter -> TODO()
            is CodeBlockLanguageCharacter -> TODO()
            EOFTokenCharacter -> TODO()
            is EmojiNameCharacter -> TODO()
            is EndTagOpenCharacter -> TODO()
            is FnBracketCharacter -> TODO()
            is FnCharacter -> TODO()
            is HashTagCharacter -> TODO()
            is InlineCodeCharacter -> TODO()
            is InlineMathCharacter -> TODO()
            is ItalicCharacter -> TODO()
            is MathBlockCharacter -> TODO()
            is RoundBracketCharacter -> TODO()
            is StrikeCharacter -> TODO()
            is TagCharacter -> TODO()
            TagOpenCharacter -> TODO()
            is UrlCharacter -> TODO()
            is UserHostCharacter -> TODO()
            is UserNameCharacter -> TODO()
        }
    }

    override fun switch(state: State) {
        currentState = state
    }

    override fun reject() {
    }

    override fun accept() {
    }
}