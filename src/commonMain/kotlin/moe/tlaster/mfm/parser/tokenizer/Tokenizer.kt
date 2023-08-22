package moe.tlaster.mfm.parser.tokenizer

internal interface Tokenizer {
    fun parse(reader: Reader): List<TokenCharacterType>
    fun emit(tokenCharacterType: TokenCharacterType, index: Int)
    fun emitRange(tokenCharacterType: TokenCharacterType, start: Int, end: Int)
    fun switch(state: State)

    // if last token builder is not text, fallback to text
    fun reject()

    // build last token builder
    fun accept()
}

internal class MFMTokenizer : Tokenizer {
    private var currentState: State = DataState
    private val tokens = arrayListOf<TokenCharacterType>()
    override fun parse(reader: Reader): List<TokenCharacterType> {
        while (reader.hasNext()) {
            currentState.read(this, reader)
        }
        return tokens
    }

    override fun emit(tokenCharacterType: TokenCharacterType, index: Int) {
        tokens.add(tokenCharacterType)
    }

    override fun emitRange(tokenCharacterType: TokenCharacterType, start: Int, end: Int) {
        val count = end - start + 1
        repeat(count) {
            tokens.add(tokenCharacterType)
        }
    }

    override fun switch(state: State) {
        currentState = state
    }

    override fun reject() {
        val index = tokens.indexOfLast { it == TokenCharacterType.Character } + 1
        tokens.subList(index, tokens.size).map {
            TokenCharacterType.Character
        }.let {
            val count = tokens.size - index
            repeat(count) {
                tokens.removeAt(index)
            }
            tokens.addAll(index, it)
        }
    }

    override fun accept() {
    }
}
