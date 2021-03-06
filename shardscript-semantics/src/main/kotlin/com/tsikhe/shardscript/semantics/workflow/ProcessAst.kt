package com.tsikhe.shardscript.semantics.workflow

import com.tsikhe.shardscript.semantics.core.*
import com.tsikhe.shardscript.semantics.prelude.Lang
import kotlin.math.sqrt

data class SystemScopes(
    val prelude: PreludeTable,
    val systemRoot: SystemRootNamespace
)

data class UserScopes(
    val systemScopes: SystemScopes,
    val imports: ImportTable,
    val userRoot: UserRootNamespace
)

data class SemanticArtifacts(
    val processedAst: FileAst,
    val userScopes: UserScopes,
    val file: Namespace,
    val sortedRecords: SortResult<Symbol>,
    val sortedFunctions: SortResult<Symbol>
)

fun createSystemScopes(architecture: Architecture): SystemScopes {
    val prelude = PreludeTable(NullSymbolTable)
    val root = SystemRootNamespace(prelude)
    Lang.initNamespace(architecture, prelude, root)
    return SystemScopes(prelude, root)
}

fun createUserScopes(systemScopes: SystemScopes): UserScopes {
    val imports = ImportTable(systemScopes.systemRoot, mutableMapOf())
    val userRoot = UserRootNamespace(imports)
    return UserScopes(systemScopes, imports, userRoot)
}

fun topologicallySortAllArtifacts(
    semanticArtifacts: SemanticArtifacts,
    existingArtifacts: List<SemanticArtifacts>
) {
    val allRecordNodes = semanticArtifacts.sortedRecords.nodes.toMutableSet()
    val allRecordEdges = semanticArtifacts.sortedRecords.edges.toMutableSet()

    val allFunctionNodes = semanticArtifacts.sortedFunctions.nodes.toMutableSet()
    val allFunctionEdges = semanticArtifacts.sortedFunctions.edges.toMutableSet()

    existingArtifacts.forEach {
        allRecordNodes.addAll(it.sortedRecords.nodes)
        allRecordEdges.addAll(it.sortedRecords.edges)

        allFunctionNodes.addAll(it.sortedFunctions.nodes)
        allFunctionEdges.addAll(it.sortedFunctions.edges)
    }

    val errors = LanguageErrors()

    when (val sortedRecords = topologicalSort(allRecordNodes, allRecordEdges)) {
        is Left -> {
            sortedRecords.value.forEach {
                errors.add(NotInSource, RecursiveRecordDetected(it))
            }

        }
        is Right -> Unit
    }

    when (val sortedFunctions = topologicalSort(allFunctionNodes, allFunctionEdges)) {
        is Left -> {
            sortedFunctions.value.forEach {
                errors.add(NotInSource, RecursiveFunctionDetected(it))
            }

        }
        is Right -> Unit
    }

    if (errors.toSet().isNotEmpty()) {
        filterThrow(errors.toSet())
    }
}

fun processAstAllPhases(
    systemScopes: SystemScopes,
    ast: FileAst,
    namespace: List<String>,
    architecture: Architecture,
    existingArtifacts: List<SemanticArtifacts>
): SemanticArtifacts {
    val userScopes = createUserScopes(systemScopes)
    existingArtifacts.forEach { artifacts ->
        registerImports(artifacts.processedAst, userScopes.imports)
        artifacts.userScopes.userRoot.symbolTable.toMap().forEach { entry ->
            userScopes.userRoot.define(entry.key, entry.value)
        }
    }

    val file = createFileScope(ast.ctx, namespace, userScopes.userRoot)

    bindScopes(ast, file, architecture, systemScopes.prelude)
    parameterScan(ast)
    val sortedRecords = simpleRecursiveRecordDetection(ast)
    recordScan(ast)
    functionScan(ast)
    propagateTypes(ast, architecture, systemScopes.prelude)
    checkTypes(ast, systemScopes.prelude)
    bans(ast)
    linearizeBans(ast)
    calculateCostMultipliers(ast, architecture)

    val sortedFunctions = sortFunctions(ast)
    sortedFunctions.sorted.forEach { calculateCost(it, architecture) }
    calculateCost(ast, architecture)
    enforceCostLimit(ast, architecture)

    val res = SemanticArtifacts(ast, userScopes, file, sortedRecords, sortedFunctions)
    topologicallySortAllArtifacts(res, existingArtifacts)
    return res
}

fun processAstResurrectOnly(
    systemScopes: SystemScopes,
    fileNamespace: Namespace,
    ast: FileAst,
    architecture: Architecture
) {
    bindScopes(ast, fileNamespace, architecture, systemScopes.prelude)
    propagateTypes(ast, architecture, systemScopes.prelude)
    checkTypes(ast, systemScopes.prelude)
    resurrectWhitelist(ast)
}