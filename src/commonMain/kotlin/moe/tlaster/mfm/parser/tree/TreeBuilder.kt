package moe.tlaster.mfm.parser.tree

import moe.tlaster.mfm.parser.tokenizer.Reader
import moe.tlaster.mfm.parser.tokenizer.TokenCharacterType

internal class TreeBuilder {
    fun build(reader: Reader, tokenCharacterTypes: List<TokenCharacterType>): RootNode {
        val root = RootNode()
        val context = TreeBuilderContext(
            currentContainer = root,
            tokenCharacterTypes = tokenCharacterTypes,
            reader = reader
        )
        context.stack.add(root)
        while (context.reader.hasNext()) {
            with(context) {
                when (tokenCharacterTypes[reader.position]) {
                    TokenCharacterType.LineBreak -> LineBreakState
                    TokenCharacterType.Character, TokenCharacterType.Italic, TokenCharacterType.Bold -> TextState
                    TokenCharacterType.EmojiNameStart -> EmojiNameState
                    TokenCharacterType.HashTagStart -> HashTagState
                    TokenCharacterType.UserAt -> UserNameState
                    TokenCharacterType.CashStart -> CashState
                    TokenCharacterType.InlineCodeStart -> InlineCodeState
                    TokenCharacterType.CodeBlockStart -> CodeBlockState
                    TokenCharacterType.AsteriskItalicStart, TokenCharacterType.UnderscoreItalicStart -> ItalicState
                    TokenCharacterType.AsteriskBold, TokenCharacterType.UnderscoreBoldStart -> BoldState
                    TokenCharacterType.Strike -> StrikeState
                    TokenCharacterType.MathBlock -> MathBlockState
                    TokenCharacterType.InlineMath -> InlineMathState
                    TokenCharacterType.Blockquote -> BlockquoteState
                    TokenCharacterType.TagOpen -> TagState
                    TokenCharacterType.EndTagOpen -> EndTagState
                    TokenCharacterType.Search -> SearchState
                    TokenCharacterType.LinkOpen, TokenCharacterType.SilentLink -> LinkState
                    TokenCharacterType.Url -> UrlState
                    TokenCharacterType.Fn -> FnState
                    TokenCharacterType.FnEndBracket -> FnEndState
                    TokenCharacterType.Eof -> EofState
                    else -> throw Exception("Unknown token ${tokenCharacterTypes[reader.position]}")
                }.apply {
                    with(this) {
                        build()
                    }
                }
            }
        }
        return root
    }
}
