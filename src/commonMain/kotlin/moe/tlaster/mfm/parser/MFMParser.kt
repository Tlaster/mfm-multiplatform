package moe.tlaster.mfm.parser

import moe.tlaster.mfm.parser.tokenizer.StringReader
import moe.tlaster.mfm.parser.tokenizer.Tokenizer
import moe.tlaster.mfm.parser.tree.RootNode
import moe.tlaster.mfm.parser.tree.TreeBuilder

class MFMParser(
    private val emojiOnly: Boolean = false,
) {
    fun parse(text: String): RootNode {
        val tokenizer = Tokenizer(emojiOnly = emojiOnly)
        val tokenCharacterTypes = tokenizer.parse(StringReader(text))
        return TreeBuilder().build(StringReader(text), tokenCharacterTypes)
    }
}
