package com.tsikhe.shardscript.semantics.visitors

import com.tsikhe.shardscript.semantics.core.*

sealed class NestedDefinitionIndicator

object TopLevelIndicator : NestedDefinitionIndicator()
object WithinEnumIndicator : NestedDefinitionIndicator()
object WithinRecordIndicator : NestedDefinitionIndicator()
object OtherIndicator : NestedDefinitionIndicator()

class BanNestedDefinitionAstVisitor : ParameterizedUnitAstVisitor<NestedDefinitionIndicator>() {
    override fun visit(ast: StringInterpolationAst, param: NestedDefinitionIndicator) {
        super.visit(ast, OtherIndicator)
    }

    override fun visit(ast: LetAst, param: NestedDefinitionIndicator) {
        super.visit(ast, OtherIndicator)
    }

    override fun visit(ast: FileAst, param: NestedDefinitionIndicator) {
        super.visit(ast, TopLevelIndicator)
    }

    override fun visit(ast: BlockAst, param: NestedDefinitionIndicator) {
        super.visit(ast, OtherIndicator)
    }

    override fun visit(ast: FunctionAst, param: NestedDefinitionIndicator) {
        when (param) {
            is WithinRecordIndicator,
            is TopLevelIndicator -> Unit
            else -> errors.add(ast.ctx, InvalidDefinitionLocation(ast.identifier))
        }
        super.visit(ast, OtherIndicator)
    }

    override fun visit(ast: RecordDefinitionAst, param: NestedDefinitionIndicator) {
        when (param) {
            is WithinEnumIndicator,
            is TopLevelIndicator -> Unit
            else -> errors.add(ast.ctx, InvalidDefinitionLocation(ast.identifier))
        }
        super.visit(ast, WithinRecordIndicator)
    }

    override fun visit(ast: ObjectDefinitionAst, param: NestedDefinitionIndicator) {
        when (param) {
            is WithinEnumIndicator,
            is TopLevelIndicator -> Unit
            else -> errors.add(ast.ctx, InvalidDefinitionLocation(ast.identifier))
        }
        super.visit(ast, OtherIndicator)
    }

    override fun visit(ast: DotAst, param: NestedDefinitionIndicator) {
        super.visit(ast, OtherIndicator)
    }

    override fun visit(ast: GroundApplyAst, param: NestedDefinitionIndicator) {
        super.visit(ast, OtherIndicator)
    }

    override fun visit(ast: DotApplyAst, param: NestedDefinitionIndicator) {
        super.visit(ast, OtherIndicator)
    }

    override fun visit(ast: ForEachAst, param: NestedDefinitionIndicator) {
        super.visit(ast, OtherIndicator)
    }

    override fun visit(ast: AssignAst, param: NestedDefinitionIndicator) {
        super.visit(ast, OtherIndicator)
    }

    override fun visit(ast: DotAssignAst, param: NestedDefinitionIndicator) {
        super.visit(ast, OtherIndicator)
    }

    override fun visit(ast: IfAst, param: NestedDefinitionIndicator) {
        super.visit(ast, OtherIndicator)
    }

    override fun visit(ast: AsAst, param: NestedDefinitionIndicator) {
        super.visit(ast, OtherIndicator)
    }

    override fun visit(ast: IsAst, param: NestedDefinitionIndicator) {
        super.visit(ast, OtherIndicator)
    }
}