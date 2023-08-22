package moe.tlaster.mfm.parser.tokenizer

internal enum class TokenCharacterType {
    Eof,
    LineBreak,
    Character,
    EmojiNameStart,
    EmojiName,
    HashTagStart,
    HashTag,
    UserAt,
    UserName,
    UserHost,
    CashStart,
    Cash,
    InlineCodeStart,
    InlineCode,
    CodeBlockStart,
    CodeBlock,
    CodeBlockLanguage,
    CodeBlockLanguageEnd,
    AsteriskItalicStart,
    UnderscoreItalicStart,
    Italic,
    AsteriskBold,
    UnderscoreBoldStart,
    Bold,
    Strike,
    MathBlock,
    MathBlockContent,
    InlineMath,
    InlineMathContent,
    Blockquote,
    TagOpen,
    EndTagOpen,
    Tag,
    TagClose,
    Search,
    SilentLink,
    LinkOpen,
    LinkClose,
    LinkContent,
    LinkHrefOpen,
    LinkHrefClose,
    LinkHref,
    Url,
    FnEndBracket,
    Fn,
    FnContent
}

internal enum class TokenType {
    Text,
    EmojiName,
    HashTag,
    UserName,
    Cash,
    InlineCode,
    CodeBlock,
    Italic,
    Bold,
    Strike,
    MathBlock,
    InlineMath,
    Blockquote,
    Tag,
    EndTag,
    Bracket,
    RoundBracket,
    Url,
    Fn,
    FnEnd,
    EOF
}

internal sealed interface Token {
    val range: IntRange
}

internal data class TextToken(
    override val range: IntRange
) : Token

internal data class EmojiNameToken(
    override val range: IntRange,
    val emojiRange: IntRange
) : Token

internal data class HashTagToken(
    override val range: IntRange,
    val tagRange: IntRange
) : Token

internal data class UserNameToken(
    override val range: IntRange,
    val nameRange: IntRange,
    val hostRange: IntRange
) : Token

internal data class CashToken(
    override val range: IntRange,
    val cashRange: IntRange
) : Token

internal data class InlineCodeToken(
    override val range: IntRange,
    val codeRange: IntRange
) : Token

internal data class CodeBlockToken(
    override val range: IntRange,
    val codeRange: IntRange
) : Token

internal data class ItalicToken(
    override val range: IntRange
) : Token

internal data class BoldToken(
    override val range: IntRange
) : Token

internal data class StrikeToken(
    override val range: IntRange
) : Token

internal data class MathBlockToken(
    override val range: IntRange,
    val formulaRange: IntRange
) : Token

internal data class InlineMathToken(
    override val range: IntRange,
    val formulaRange: IntRange
) : Token

internal data class BlockquoteToken(
    override val range: IntRange
) : Token

internal data class TagToken(
    override val range: IntRange,
    val tagRange: IntRange
) : Token

internal data class EndTagToken(
    override val range: IntRange,
    val tagRange: IntRange
) : Token

internal data class LinkToken(
    override val range: IntRange,
    val hrefRange: IntRange,
    val contentRange: IntRange
) : Token

internal data class UrlToken(
    override val range: IntRange
) : Token

internal data class FnToken(
    override val range: IntRange,
    val contentRange: IntRange
) : Token

internal data class FnEndToken(
    override val range: IntRange
) : Token

internal data class EOFToken(
    override val range: IntRange
) : Token
