package moe.tlaster.mfm.parser

import moe.tlaster.mfm.parser.tree.RootNode

class MFMParser(
    private val emojiOnly: Boolean = false,
) {
    fun parse(text: String): RootNode = DirectParser(text, emojiOnly).parse()
}
