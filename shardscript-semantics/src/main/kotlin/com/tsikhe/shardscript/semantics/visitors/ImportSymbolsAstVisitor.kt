package com.tsikhe.shardscript.semantics.visitors

import com.tsikhe.shardscript.semantics.core.*

class ImportSymbolsAstVisitor(val importTable: ImportTable) : UnitAstVisitor() {
    override fun visit(ast: FunctionAst) {
        importTable.register(ast.identifier, ast.definitionSpace)
    }

    override fun visit(ast: RecordDefinitionAst) {
        importTable.register(ast.identifier, ast.definitionSpace)
    }

    override fun visit(ast: ObjectDefinitionAst) {
        importTable.register(ast.identifier, ast.definitionSpace)
    }
}