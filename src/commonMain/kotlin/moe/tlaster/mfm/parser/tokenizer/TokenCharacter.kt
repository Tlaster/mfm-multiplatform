package moe.tlaster.mfm.parser.tokenizer

import kotlin.jvm.JvmInline

internal sealed interface TokenCharacter {
    val value: Char
}

internal sealed interface TokenBuilder {
    val raw: StringBuilder
    fun canAccept(character: TokenCharacter): Boolean
    fun accept(character: TokenCharacter)
    fun build(): Token
}

internal sealed interface Token
internal data class TextToken(val content: String) : Token
internal data class EmojiNameToken(val name: String) : Token
internal data class HashTagToken(val name: String) : Token
internal data class UserNameToken(val name: String, val host: String?) : Token
internal data class CashToken(val content: String) : Token
internal data class InlineCodeToken(val content: String) : Token
internal data class CodeBlockToken(val content: String, val language: String?) : Token
internal data class ItalicToken(val content: String) : Token
internal data class BoldToken(val content: String) : Token
internal data object StrikeToken : Token {
    val content = "~~"
}
internal data class MathBlockToken(val content: String) : Token
internal data class InlineMathToken(val content: String) : Token
internal data object BlockquoteToken : Token
internal data class TagToken(val content: String) : Token
internal data class EndTagToken(val content: String) : Token
internal data class BracketToken(val content: String, val isSilent: Boolean) : Token
internal data class RoundBracketToken(val content: String) : Token
internal data class UrlToken(val content: String) : Token
internal data class FnToken(val content: String) : Token
internal data object FnEndToken : Token {
    val content = ']'
}

internal data object EOFToken : Token

internal data object EOFTokenCharacter : TokenCharacter {
    override val value: Char
        get() = eof
}

@JvmInline
internal value class Character(override val value: Char) : TokenCharacter
internal data object EmojiNameStartCharacter : TokenCharacter {
    override val value: Char
        get() = ':'
}
internal data class EmojiNameCharacter(override val value: Char) : TokenCharacter
internal data object HashTagStartCharacter : TokenCharacter {
    override val value: Char
        get() = '#'
}
internal data class HashTagCharacter(override val value: Char) : TokenCharacter
internal data object UserAtCharacter : TokenCharacter {
    override val value: Char
        get() = '@'
}
internal data class UserNameCharacter(override val value: Char) : TokenCharacter
internal data class UserHostCharacter(override val value: Char) : TokenCharacter
internal data object CashStartCharacter : TokenCharacter {
    override val value: Char
        get() = '$'
}
internal data class CashCharacter(override val value: Char) : TokenCharacter
internal data object InlineCodeStartCharacter : TokenCharacter {
    override val value: Char
        get() = '`'
}
internal data class InlineCodeCharacter(override val value: Char) : TokenCharacter
internal data object CodeBlockStartCharacter : TokenCharacter {
    override val value: Char
        get() = '`'
}
internal data class CodeBlockCharacter(override val value: Char) : TokenCharacter
internal data class CodeBlockLanguageCharacter(override val value: Char) : TokenCharacter
internal data object CodeBlockLanguageEndCharacter : TokenCharacter {
    override val value: Char
        get() = '\n'
}
internal data object AsteriskItalicStartCharacter : TokenCharacter {
    override val value: Char
        get() = '*'
}

internal data object UnderscoreItalicStartCharacter : TokenCharacter {
    override val value: Char
        get() = '_'
}
internal data class ItalicCharacter(override val value: Char) : TokenCharacter
internal data object AsteriskBoldStartCharacter : TokenCharacter {
    override val value: Char
        get() = '*'
}
internal data object UnderscoreBoldStartCharacter : TokenCharacter {
    override val value: Char
        get() = '_'
}
internal data class BoldCharacter(override val value: Char) : TokenCharacter
internal data object StrikeCharacter : TokenCharacter {
    override val value: Char
        get() = '~'
}

internal data class MathBlockCharacter(override val value: Char) : TokenCharacter
internal data class MathBlockContentCharacter(override val value: Char) : TokenCharacter
internal data class InlineMathCharacter(override val value: Char) : TokenCharacter
internal data class InlineMathContentCharacter(override val value: Char) : TokenCharacter
internal data object BlockquoteCharacter : TokenCharacter {
    override val value: Char
        get() = '>'
}

internal data object TagOpenCharacter : TokenCharacter {
    override val value: Char
        get() = '<'
}

internal data class EndTagOpenCharacter(override val value: Char) : TokenCharacter
internal data class TagCharacter(override val value: Char) : TokenCharacter
internal data object TagCloseCharacter : TokenCharacter {
    override val value: Char
        get() = '>'
}
internal data class BracketCharacter(override val value: Char) : TokenCharacter
internal data class BracketContentCharacter(override val value: Char) : TokenCharacter
internal data class RoundBracketCharacter(override val value: Char) : TokenCharacter
internal data class RoundBracketContentCharacter(override val value: Char) : TokenCharacter
internal data class UrlCharacter(override val value: Char) : TokenCharacter
internal data object FnEndBracketCharacter : TokenCharacter {
    override val value: Char
        get() = ']'
}

internal data class FnCharacter(override val value: Char) : TokenCharacter
internal data class FnContentCharacter(override val value: Char) : TokenCharacter

internal class TextTokenBuilder(
    private val builder: StringBuilder = StringBuilder()
) : TokenBuilder {
    override val raw: StringBuilder
        get() = builder

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is Character
    }

    override fun accept(character: TokenCharacter) {
        if (character is Character) {
            builder.append(character.value)
        }
    }

    override fun build(): Token {
        return TextToken(builder.toString())
    }
}

internal class EmojiNameTokenBuilder : TokenBuilder {
    private val rawBuilder = StringBuilder()
    private val builder = StringBuilder()
    override val raw: StringBuilder
        get() = rawBuilder

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is EmojiNameCharacter || character is EmojiNameStartCharacter
    }

    override fun accept(character: TokenCharacter) {
        rawBuilder.append(character.value)
        if (character is EmojiNameCharacter) {
            builder.append(character.value)
        }
    }

    override fun build(): Token {
        return EmojiNameToken(builder.toString())
    }
}

internal class HashTagTokenBuilder : TokenBuilder {
    private val rawBuilder = StringBuilder()
    private val builder = StringBuilder()
    override val raw: StringBuilder
        get() = rawBuilder

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is HashTagCharacter || character is HashTagStartCharacter
    }

    override fun accept(character: TokenCharacter) {
        rawBuilder.append(character.value)
        if (character is HashTagCharacter) {
            builder.append(character.value)
        }
    }

    override fun build(): Token {
        return HashTagToken(builder.toString())
    }
}

internal class UserNameTokenBuilder : TokenBuilder {
    private val rawBuilder = StringBuilder()
    private val builder = StringBuilder()
    private var hostBuilder: StringBuilder? = null
    override val raw: StringBuilder
        get() = rawBuilder

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is UserNameCharacter || character is UserAtCharacter || character is UserHostCharacter
    }

    override fun accept(character: TokenCharacter) {
        rawBuilder.append(character.value)
        if (character is UserNameCharacter) {
            builder.append(character.value)
        } else if (character is UserHostCharacter) {
            if (hostBuilder == null) {
                hostBuilder = StringBuilder()
            }
            hostBuilder?.append(character.value)
        }
    }

    override fun build(): Token {
        return UserNameToken(builder.toString(), hostBuilder?.toString())
    }
}

internal class CashTokenBuilder : TokenBuilder {
    private val rawBuilder = StringBuilder()
    private val builder = StringBuilder()
    override val raw: StringBuilder
        get() = rawBuilder

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is CashCharacter || character is CashStartCharacter
    }

    override fun accept(character: TokenCharacter) {
        rawBuilder.append(character.value)
        if (character is CashCharacter) {
            builder.append(character.value)
        }
    }

    override fun build(): Token {
        return CashToken(builder.toString())
    }
}

internal class InlineCodeTokenBuilder : TokenBuilder {
    private val rawBuilder = StringBuilder()
    private val builder = StringBuilder()
    override val raw: StringBuilder
        get() = rawBuilder

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is InlineCodeCharacter || character is InlineCodeStartCharacter
    }

    override fun accept(character: TokenCharacter) {
        rawBuilder.append(character.value)
        if (character is InlineCodeCharacter) {
            builder.append(character.value)
        }
    }

    override fun build(): Token {
        return InlineCodeToken(builder.toString())
    }
}

internal class CodeBlockTokenBuilder : TokenBuilder {
    private val rawBuilder = StringBuilder()
    private val builder = StringBuilder()
    private var languageBuilder: StringBuilder? = null
    override val raw: StringBuilder
        get() = rawBuilder

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is CodeBlockCharacter || character is CodeBlockStartCharacter || character is CodeBlockLanguageCharacter || character is CodeBlockLanguageEndCharacter
    }

    override fun accept(character: TokenCharacter) {
        rawBuilder.append(character.value)
        if (character is CodeBlockCharacter) {
            builder.append(character.value)
        } else if (character is CodeBlockLanguageCharacter) {
            if (languageBuilder == null) {
                languageBuilder = StringBuilder()
            }
            languageBuilder?.append(character.value)
        }
    }

    override fun build(): Token {
        if (builder.isEmpty() && languageBuilder != null) {
            return CodeBlockToken(content = languageBuilder.toString(), language = null)
        } else {
            return CodeBlockToken(builder.toString(), languageBuilder?.toString())
        }
    }
}

internal class ItalicTokenBuilder : TokenBuilder {
    private val rawBuilder = StringBuilder()
    private val builder = StringBuilder()
    override val raw: StringBuilder
        get() = rawBuilder

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is ItalicCharacter || character is AsteriskItalicStartCharacter || character is UnderscoreItalicStartCharacter
    }

    override fun accept(character: TokenCharacter) {
        rawBuilder.append(character.value)
        if (character is ItalicCharacter) {
            builder.append(character.value)
        }
    }

    override fun build(): Token {
        return ItalicToken(builder.toString())
    }
}

internal class BoldTokenBuilder : TokenBuilder {
    private val rawBuilder = StringBuilder()
    private val builder = StringBuilder()
    override val raw: StringBuilder
        get() = rawBuilder

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is BoldCharacter || character is AsteriskBoldStartCharacter || character is UnderscoreBoldStartCharacter
    }

    override fun accept(character: TokenCharacter) {
        rawBuilder.append(character.value)
        if (character is BoldCharacter) {
            builder.append(character.value)
        }
    }

    override fun build(): Token {
        return BoldToken(builder.toString())
    }
}

internal class StrikeTokenBuilder : TokenBuilder {
    private val rawBuilder = StringBuilder()
    private val builder = StringBuilder()
    override val raw: StringBuilder
        get() = rawBuilder

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is StrikeCharacter
    }

    override fun accept(character: TokenCharacter) {
        rawBuilder.append(character.value)
    }

    override fun build(): Token {
        return StrikeToken
    }
}

internal class MathBlockTokenBuilder : TokenBuilder {
    private val rawBuilder = StringBuilder()
    private val builder = StringBuilder()
    override val raw: StringBuilder
        get() = rawBuilder

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is MathBlockCharacter || character is MathBlockContentCharacter
    }

    override fun accept(character: TokenCharacter) {
        rawBuilder.append(character.value)
        if (character is MathBlockContentCharacter) {
            builder.append(character.value)
        }
    }

    override fun build(): Token {
        return MathBlockToken(builder.toString())
    }
}

internal class InlineMathTokenBuilder : TokenBuilder {
    private val rawBuilder = StringBuilder()
    private val builder = StringBuilder()
    private var isEscape = false
    override val raw: StringBuilder
        get() = rawBuilder

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is InlineMathCharacter || character is InlineMathContentCharacter
    }

    override fun accept(character: TokenCharacter) {
        rawBuilder.append(character.value)
        if (character is InlineMathContentCharacter) {
            builder.append(character.value)
        }
    }

    override fun build(): Token {
        return InlineMathToken(builder.toString())
    }
}

internal class BlockquoteTokenBuilder : TokenBuilder {
    private val rawBuilder = StringBuilder()
    override val raw: StringBuilder
        get() = rawBuilder

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is BlockquoteCharacter
    }

    override fun accept(character: TokenCharacter) {
        rawBuilder.append(character.value)
    }

    override fun build(): Token {
        return BlockquoteToken
    }
}

internal class TagTokenBuilder : TokenBuilder {
    private val rawBuilder = StringBuilder()
    private val builder = StringBuilder()
    private var isEnd = false
    override val raw: StringBuilder
        get() = rawBuilder

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is TagCharacter || character is TagOpenCharacter || character is EndTagOpenCharacter || character is TagCloseCharacter
    }

    override fun accept(character: TokenCharacter) {
        rawBuilder.append(character.value)
        if (character is TagCharacter) {
            builder.append(character.value)
        } else if (character is EndTagOpenCharacter) {
            isEnd = true
        }
    }

    override fun build(): Token {
        return if (isEnd) {
            EndTagToken(builder.toString())
        } else {
            TagToken(builder.toString())
        }
    }
}

internal class BracketTokenBuilder : TokenBuilder {
    private val rawBuilder = StringBuilder()
    private val builder = StringBuilder()
    private var isSilent = false
    override val raw: StringBuilder
        get() = rawBuilder

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is BracketCharacter || character is BracketContentCharacter
    }

    override fun accept(character: TokenCharacter) {
        rawBuilder.append(character.value)
        if (character is BracketContentCharacter) {
            builder.append(character.value)
        } else if (character is BracketCharacter && character.value == '?') {
            isSilent = true
        }
    }

    override fun build(): Token {
        return BracketToken(builder.toString(), isSilent)
    }
}

internal class RoundBracketTokenBuilder : TokenBuilder {
    private val rawBuilder = StringBuilder()
    private val builder = StringBuilder()
    override val raw: StringBuilder
        get() = rawBuilder

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is RoundBracketCharacter || character is RoundBracketContentCharacter
    }

    override fun accept(character: TokenCharacter) {
        rawBuilder.append(character.value)
        if (character is RoundBracketContentCharacter) {
            builder.append(character.value)
        }
    }

    override fun build(): Token {
        return RoundBracketToken(builder.toString())
    }
}

internal class UrlTokenBuilder : TokenBuilder {
    private val rawBuilder = StringBuilder()
    private val builder = StringBuilder()
    override val raw: StringBuilder
        get() = rawBuilder

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is UrlCharacter
    }

    override fun accept(character: TokenCharacter) {
        rawBuilder.append(character.value)
        if (character is UrlCharacter) {
            builder.append(character.value)
        }
    }

    override fun build(): Token {
        return UrlToken(builder.toString())
    }
}

internal class FnTokenBuilder : TokenBuilder {
    private val rawBuilder = StringBuilder()
    private val builder = StringBuilder()
    private var isEnd = false
    override val raw: StringBuilder
        get() = rawBuilder

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is FnEndBracketCharacter || character is FnCharacter || character is FnContentCharacter
    }

    override fun accept(character: TokenCharacter) {
        rawBuilder.append(character.value)
        if (character is FnContentCharacter) {
            builder.append(character.value)
        } else if (character is FnEndBracketCharacter) {
            isEnd = true
        }
    }

    override fun build(): Token {
        return if (isEnd) {
            FnEndToken
        } else {
            FnToken(builder.toString())
        }
    }
}

internal data object EofTokenBuilder : TokenBuilder {
    override val raw: StringBuilder
        get() = StringBuilder()

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is EOFTokenCharacter
    }

    override fun accept(character: TokenCharacter) {
        // do nothing
    }

    override fun build(): Token {
        return EOFToken
    }
}

internal fun TokenCharacter.createBuilder(): TokenBuilder {
    return when (this) {
        is Character -> TextTokenBuilder()
        is EmojiNameStartCharacter -> EmojiNameTokenBuilder()
        is EmojiNameCharacter -> EmojiNameTokenBuilder()
        is HashTagStartCharacter -> HashTagTokenBuilder()
        is HashTagCharacter -> HashTagTokenBuilder()
        is UserAtCharacter -> UserNameTokenBuilder()
        is UserNameCharacter -> UserNameTokenBuilder()
        is UserHostCharacter -> UserNameTokenBuilder()
        is CashStartCharacter -> CashTokenBuilder()
        is CashCharacter -> CashTokenBuilder()
        is InlineCodeStartCharacter -> InlineCodeTokenBuilder()
        is InlineCodeCharacter -> InlineCodeTokenBuilder()
        is CodeBlockLanguageEndCharacter -> CodeBlockTokenBuilder()
        is CodeBlockStartCharacter -> CodeBlockTokenBuilder()
        is CodeBlockCharacter -> CodeBlockTokenBuilder()
        is CodeBlockLanguageCharacter -> CodeBlockTokenBuilder()
        is AsteriskItalicStartCharacter -> ItalicTokenBuilder()
        is UnderscoreItalicStartCharacter -> ItalicTokenBuilder()
        is ItalicCharacter -> ItalicTokenBuilder()
        is AsteriskBoldStartCharacter -> BoldTokenBuilder()
        is UnderscoreBoldStartCharacter -> BoldTokenBuilder()
        is BoldCharacter -> BoldTokenBuilder()
        is StrikeCharacter -> StrikeTokenBuilder()
        is MathBlockCharacter -> MathBlockTokenBuilder()
        is MathBlockContentCharacter -> MathBlockTokenBuilder()
        is InlineMathCharacter -> InlineMathTokenBuilder()
        is InlineMathContentCharacter -> InlineMathTokenBuilder()
        is BlockquoteCharacter -> BlockquoteTokenBuilder()
        is TagOpenCharacter -> TagTokenBuilder()
        is EndTagOpenCharacter -> TagTokenBuilder()
        is TagCharacter -> TagTokenBuilder()
        is TagCloseCharacter -> TagTokenBuilder()
        is BracketCharacter -> BracketTokenBuilder()
        is BracketContentCharacter -> BracketTokenBuilder()
        is RoundBracketCharacter -> RoundBracketTokenBuilder()
        is RoundBracketContentCharacter -> RoundBracketTokenBuilder()
        is UrlCharacter -> UrlTokenBuilder()
        is FnEndBracketCharacter -> FnTokenBuilder()
        is FnCharacter -> FnTokenBuilder()
        is FnContentCharacter -> FnTokenBuilder()
        EOFTokenCharacter -> EofTokenBuilder
    }
}
