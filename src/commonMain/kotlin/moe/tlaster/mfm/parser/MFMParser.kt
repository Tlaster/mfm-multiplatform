package moe.tlaster.mfm.parser

import moe.tlaster.mfm.parser.tree.RootNode

class MFMParser(
    private val emojiOnly: Boolean = false,
) {
    private var nestLimit: Int = 20

    constructor(
        emojiOnly: Boolean = false,
        nestLimit: Int,
    ) : this(emojiOnly) {
        require(nestLimit >= 0) { "nestLimit must not be negative" }
        this.nestLimit = nestLimit
    }

    fun parse(text: String): RootNode = DirectParser(text, emojiOnly, nestLimit = nestLimit).parse()
}
