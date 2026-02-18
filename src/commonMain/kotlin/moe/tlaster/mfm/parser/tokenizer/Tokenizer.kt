package moe.tlaster.mfm.parser.tokenizer

internal class Tokenizer(
    val emojiOnly: Boolean = false,
) {
    private var currentState: State = DataState
    private lateinit var tokens: ArrayList<TokenCharacterType>

    fun parse(reader: Reader): List<TokenCharacterType> {
        tokens = (0..<reader.length).map { TokenCharacterType.UnKnown }.toCollection(ArrayList())
        while (reader.hasNext()) {
            currentState.read(this, reader)
        }
        return tokens.toList()
    }

    fun emit(
        tokenCharacterType: TokenCharacterType,
        index: Int,
    ) {
        tokens[index - 1] = tokenCharacterType
    }

    // start is not included, end is included
    fun emitRange(
        tokenCharacterType: TokenCharacterType,
        start: Int,
        end: Int,
    ) {
        repeat(end - start) {
            tokens[start + it] = tokenCharacterType
        }
    }

    fun switch(state: State) {
        currentState = state
    }

    fun reject(position: Int) {
        val subList = tokens.subList(0, position)
        var index = subList.indexOfLast { it == TokenCharacterType.Character } + 1
        if (index == 0) {
            // No Character token found. Look for a LinkOpen that started this construct
            // so we don't wipe previously accepted emoji/tag tokens before it.
            val linkStart =
                subList.indexOfLast {
                    it == TokenCharacterType.LinkOpen || it == TokenCharacterType.SilentLink
                }
            if (linkStart >= 0) {
                index = linkStart
            }
        }
        repeat(position - index - 1) {
            tokens[index + it] = TokenCharacterType.Character
        }
    }

    fun readAt(position: Int): TokenCharacterType = tokens[position]

    fun accept() {
    }
}
