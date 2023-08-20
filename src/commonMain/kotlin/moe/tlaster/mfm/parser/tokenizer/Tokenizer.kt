package moe.tlaster.mfm.parser.tokenizer

internal interface Tokenizer {
    fun parse(reader: Reader): List<Token>
    fun emit(tokenCharacter: TokenCharacter)
    fun switch(state: State)

    // if last token builder is not text, fallback to text
    fun reject()

    // build last token builder
    fun accept()
}

internal class MFMTokenizer : Tokenizer {
    private var currentState: State = DataState
    private var currentBuilder: TokenBuilder? = null
    private val tokens = arrayListOf<Token>()

    override fun parse(reader: Reader): List<Token> {
        while (reader.hasNext()) {
            currentState.read(this, reader)
        }
        return tokens
    }

    override fun emit(tokenCharacter: TokenCharacter) {
        currentBuilder?.takeIf {
            it.canAccept(tokenCharacter)
        }?.accept(tokenCharacter) ?: run {
            accept()
            currentBuilder = tokenCharacter.createBuilder().apply {
                accept(tokenCharacter)
            }
        }
    }

    override fun switch(state: State) {
        currentState = state
    }

    override fun reject() {
        currentBuilder?.let {
            currentBuilder = TextTokenBuilder(it.raw)
        }
    }

    override fun accept() {
        currentBuilder?.let {
            tokens.add(it.build())
            currentBuilder = null
        }
    }
}
