package moe.tlaster.mfm.parser.tokenizer

import kotlin.jvm.JvmInline


internal sealed interface TokenCharacter {
    val value: Char
}

internal sealed interface TokenBuilder {
    val raw: String
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

internal data object EOFTokenCharacter : TokenCharacter {
    override val value: Char
        get() = eof
}

@JvmInline
internal value class Character(override val value: Char) : TokenCharacter
internal data class EmojiNameCharacter(override val value: Char) : TokenCharacter
internal data class HashTagCharacter(override val value: Char) : TokenCharacter
internal data class UserNameCharacter(override val value: Char) : TokenCharacter
internal data class UserHostCharacter(override val value: Char) : TokenCharacter
internal data class CashCharacter(override val value: Char) : TokenCharacter
internal data class InlineCodeCharacter(override val value: Char) : TokenCharacter
internal data class CodeBlockCharacter(override val value: Char) : TokenCharacter
internal data class CodeBlockLanguageCharacter(override val value: Char) : TokenCharacter
internal data class ItalicCharacter(override val value: Char) : TokenCharacter
internal data class BoldCharacter(override val value: Char) : TokenCharacter
internal data class StrikeCharacter(override val value: Char) : TokenCharacter
internal data class MathBlockCharacter(override val value: Char) : TokenCharacter
internal data class InlineMathCharacter(override val value: Char) : TokenCharacter
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
internal data class BracketCharacter(override val value: Char) : TokenCharacter
internal data class RoundBracketCharacter(override val value: Char) : TokenCharacter
internal data class UrlCharacter(override val value: Char) : TokenCharacter
internal data class FnBracketCharacter(override val value: Char) : TokenCharacter
internal data class FnCharacter(override val value: Char) : TokenCharacter


internal class TextTokenBuilder : TokenBuilder {
    private val builder = StringBuilder()
    override val raw: String
        get() = builder.toString()

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
    private val builder = StringBuilder()
    override val raw: String
        get() = builder.toString()

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is EmojiNameCharacter
    }

    override fun accept(character: TokenCharacter) {
        if (character is EmojiNameCharacter) {
            builder.append(character.value)
        }
    }

    override fun build(): Token {
        return EmojiNameToken(builder.toString().trim(':'))
    }
}

internal class HashTagTokenBuilder : TokenBuilder {
    private val builder = StringBuilder()
    override val raw: String
        get() = builder.toString()

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is HashTagCharacter
    }

    override fun accept(character: TokenCharacter) {
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
    private val userNameBuilder = StringBuilder()
    private val hostBuilder = StringBuilder()
    override val raw: String
        get() = rawBuilder.toString()

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is UserNameCharacter || character is UserHostCharacter
    }

    override fun accept(character: TokenCharacter) {
        if (character is UserNameCharacter) {
            userNameBuilder.append(character.value)
            rawBuilder.append(character.value)
        } else if (character is UserHostCharacter) {
            hostBuilder.append(character.value)
            rawBuilder.append(character.value)
        }
    }

    override fun build(): Token {
        return UserNameToken(userNameBuilder.toString(), hostBuilder.toString().takeIf { it.isNotEmpty() })
    }
}

internal class CashTokenBuilder : TokenBuilder {
    private val builder = StringBuilder()
    override val raw: String
        get() = builder.toString()

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is CashCharacter
    }

    override fun accept(character: TokenCharacter) {
        if (character is CashCharacter) {
            builder.append(character.value)
        }
    }

    override fun build(): Token {
        return CashToken(builder.toString())
    }
}

internal class InlineCodeTokenBuilder : TokenBuilder {
    private val builder = StringBuilder()
    override val raw: String
        get() = builder.toString()

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is InlineCodeCharacter
    }

    override fun accept(character: TokenCharacter) {
        if (character is InlineCodeCharacter) {
            builder.append(character.value)
        }
    }

    override fun build(): Token {
        return InlineCodeToken(builder.toString().trim('`'))
    }
}

internal class CodeBlockTokenBuilder : TokenBuilder {
    private val contentBuilder = StringBuilder()
    private val languageBuilder = StringBuilder()
    override val raw: String
        get() = contentBuilder.toString()

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is CodeBlockCharacter || character is CodeBlockLanguageCharacter
    }

    override fun accept(character: TokenCharacter) {
        if (character is CodeBlockCharacter) {
            contentBuilder.append(character.value)
        } else if (character is CodeBlockLanguageCharacter) {
            languageBuilder.append(character.value)
        }
    }

    override fun build(): Token {
        return CodeBlockToken(contentBuilder.toString().trim(), languageBuilder.toString().takeIf { it.isNotEmpty() })
    }
}

internal class ItalicTokenBuilder : TokenBuilder {
    private val builder = StringBuilder()
    override val raw: String
        get() = builder.toString()

    override fun canAccept(character: TokenCharacter): Boolean {
        return character is ItalicCharacter
    }

    override fun accept(character: TokenCharacter) {
        if (character is ItalicCharacter) {
            builder.append(character.value)
        }
    }

    override fun build(): Token {
        return ItalicToken(builder.toString().trim('*'))
    }
}