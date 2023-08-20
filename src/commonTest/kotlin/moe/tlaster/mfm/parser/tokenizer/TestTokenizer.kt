package moe.tlaster.mfm.parser.tokenizer

internal class TestTokenizer(
    var state: State = DataState
) : Tokenizer {
    val acceptIndex = arrayListOf<Int>()
    val rejectIndex = arrayListOf<Int>()
    val tokens = arrayListOf<TokenCharacter>()
    override fun parse(reader: Reader): List<Token> {
        while (reader.hasNext()) {
            state.read(this, reader)
        }
        return emptyList()
    }

    override fun emit(tokenCharacter: TokenCharacter) {
        tokens.add(tokenCharacter)
    }

    override fun switch(state: State) {
        this.state = state
    }

    override fun reject() {
        val index = tokens.indexOfLast { it is Character } + 1
        tokens.subList(index, tokens.size).map {
            Character(it.value)
        }.let {
            val count = tokens.size - index
            repeat(count) {
                tokens.removeAt(index)
            }
            tokens.addAll(index, it)
        }
        rejectIndex.add(tokens.size)
    }

    override fun accept() {
        acceptIndex.add(tokens.size)
    }
}
