package com.tsikhe.shardscript.semantics.visitors

import com.tsikhe.shardscript.semantics.core.*

class RecordScanAstVisitor : UnitAstVisitor() {
    private fun scanRecord(ast: RecordDefinitionAst) {
        if (ast.typeParams.isEmpty()) {
            val groundRecordTypeSymbol = ast.scope as GroundRecordTypeSymbol
            groundRecordTypeSymbol.fields = ast.fields.map {
                val ofTypeSymbol = groundRecordTypeSymbol.fetch(it.ofType)
                val fieldSymbol = FieldSymbol(groundRecordTypeSymbol, it.identifier, ofTypeSymbol, it.mutable)
                groundRecordTypeSymbol.define(it.identifier, fieldSymbol)
                fieldSymbol
            }
        } else {
            val parameterizedRecordSymbol = ast.scope as ParameterizedRecordTypeSymbol
            parameterizedRecordSymbol.fields = ast.fields.map {
                val ofTypeSymbol = parameterizedRecordSymbol.fetch(it.ofType)
                val fieldSymbol = FieldSymbol(parameterizedRecordSymbol, it.identifier, ofTypeSymbol, it.mutable)
                parameterizedRecordSymbol.define(it.identifier, fieldSymbol)
                fieldSymbol
            }
        }
    }

    override fun visit(ast: RecordDefinitionAst) {
        try {
            scanRecord(ast)
            super.visit(ast)
        } catch (ex: LanguageException) {
            errors.addAll(ast.ctx, ex.errors)
        }
    }
}