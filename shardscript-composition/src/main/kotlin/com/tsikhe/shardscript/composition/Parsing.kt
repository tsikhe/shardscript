package com.tsikhe.shardscript.composition

import com.tsikhe.shardscript.grammar.ShardScriptLexer
import com.tsikhe.shardscript.grammar.ShardScriptParser
import com.tsikhe.shardscript.semantics.core.*
import com.tsikhe.shardscript.semantics.prelude.BinaryOperator
import com.tsikhe.shardscript.semantics.prelude.CollectionMethods
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Token
import org.apache.commons.text.StringEscapeUtils

data class Parser(val grammar: ShardScriptParser, val errors: LanguageErrors)

fun createParser(fileName: String, contents: String): Parser {
    val syntaxListener = SyntaxErrorListener(fileName)
    val stream = CharStreams.fromString(contents)
    val lexer = ShardScriptLexer(stream)
    lexer.removeErrorListeners()
    lexer.addErrorListener(syntaxListener)
    val grammar = ShardScriptParser(CommonTokenStream(lexer))
    grammar.removeErrorListeners()
    grammar.addErrorListener(syntaxListener)
    return Parser(grammar, syntaxListener.errors)
}

internal fun createContext(fileName: String, token: Token): SourceContext =
    InSource(fileName, token.line, token.charPositionInLine)

internal fun rewriteAsGroundApply(
    args: List<Ast>,
    gid: Identifier,
    sourceContext: SourceContext
): GroundApplyAst {
    val res = GroundApplyAst(gid, args)
    res.ctx = sourceContext
    return res
}

internal fun rewriteAsDotApply(
    left: Ast,
    args: List<Ast>,
    op: BinaryOperator,
    sourceContext: SourceContext
): DotApplyAst {
    val res = DotApplyAst(left, Identifier(op.idStr), args)
    res.ctx = sourceContext
    return res
}

internal fun rewriteAsDotApply(
    left: Ast,
    args: List<Ast>,
    collectionMethod: CollectionMethods,
    sourceContext: SourceContext
): DotApplyAst {
    val res = DotApplyAst(left, Identifier(collectionMethod.idStr), args)
    res.ctx = sourceContext
    return res
}

fun resurrectString(original: String): String {
    val trimmed = original.replace(Regex.fromLiteral("^\""), "").replace(Regex.fromLiteral("\"$"), "")
    return StringEscapeUtils.unescapeJava(trimmed)
}

fun persistString(original: String): String {
    val escaped = StringEscapeUtils.escapeJava(original)
    return "\"$escaped\""
}

fun resurrectChar(original: String): Char {
    val trimmed = original.removeSurrounding("'")
    return StringEscapeUtils.unescapeJava(trimmed).toCharArray().first()
}

fun persistChar(original: Char): String {
    val escaped = StringEscapeUtils.escapeJava(original.toString())
    return "\'$escaped\'"
}
