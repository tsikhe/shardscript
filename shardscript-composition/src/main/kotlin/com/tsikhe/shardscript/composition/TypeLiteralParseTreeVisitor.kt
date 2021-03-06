package com.tsikhe.shardscript.composition

import com.tsikhe.shardscript.grammar.ShardScriptParser
import com.tsikhe.shardscript.grammar.ShardScriptParserBaseVisitor
import com.tsikhe.shardscript.semantics.core.*

internal class TypeLiteralParseTreeVisitor(private val fileName: String) : ShardScriptParserBaseVisitor<Signifier>() {
    override fun visitMultiTypePath(ctx: ShardScriptParser.MultiTypePathContext): Signifier {
        val res = PathSignifier(ctx.IDENTIFIER().map {
            val gid = Identifier(it.text)
            gid.ctx = createContext(fileName, it.symbol)
            gid
        })
        res.ctx = createContext(fileName, ctx.start)
        return res
    }

    override fun visitSingleTypePath(ctx: ShardScriptParser.SingleTypePathContext): Signifier {
        val res = Identifier(ctx.IDENTIFIER().text.toString())
        res.ctx = createContext(fileName, ctx.IDENTIFIER().symbol)
        return res
    }

    override fun visitGroundType(ctx: ShardScriptParser.GroundTypeContext): Signifier {
        return visit(ctx.id)
    }

    override fun visitParameterizedType(ctx: ShardScriptParser.ParameterizedTypeContext): Signifier {
        val tti = visit(ctx.id) as TerminalTextSignifier
        tti.ctx = createContext(fileName, ctx.id.start)

        val args: MutableList<Signifier> = ArrayList()
        ctx.params.typeExprWithOmicron().forEach {
            val param = visit(it)
            args.add(param)
        }

        val res = ParameterizedSignifier(tti, args)
        res.ctx = createContext(fileName, ctx.start)
        return res
    }

    override fun visitOmicronLiteral(ctx: ShardScriptParser.OmicronLiteralContext): Signifier {
        val res = OmicronLiteral(ctx.magnitude.text.toLong())
        res.ctx = createContext(fileName, ctx.magnitude)
        return res
    }

    override fun visitOmicronType(ctx: ShardScriptParser.OmicronTypeContext): Signifier {
        val res = Identifier(ctx.omicron.text.toString())
        res.ctx = createContext(fileName, ctx.omicron)
        return res
    }

    override fun visitNoOmicron(ctx: ShardScriptParser.NoOmicronContext): Signifier {
        return visit(ctx.te)
    }

    override fun visitMultiParamFunctionType(ctx: ShardScriptParser.MultiParamFunctionTypeContext): Signifier {
        val params: MutableList<Signifier> = ArrayList()
        val ret = visit(ctx.ret)

        ctx.params.typeExpr().forEach {
            val param = visit(it)
            params.add(param)
        }

        val res = FunctionTypeLiteral(params, ret)
        res.ctx = createContext(fileName, ctx.start)
        return res
    }

    override fun visitNoParamFunctionType(ctx: ShardScriptParser.NoParamFunctionTypeContext): Signifier {
        val params: MutableList<Signifier> = ArrayList()
        val ret = visit(ctx.ret)

        val res = FunctionTypeLiteral(params, ret)
        res.ctx = createContext(fileName, ctx.start)
        return res
    }

    override fun visitOneParamFunctionType(ctx: ShardScriptParser.OneParamFunctionTypeContext): Signifier {
        val params: MutableList<Signifier> = ArrayList()
        val ret = visit(ctx.ret)

        val input = visit(ctx.input)
        params.add(input)

        val res = FunctionTypeLiteral(params, ret)
        res.ctx = createContext(fileName, ctx.start)
        return res
    }
}
