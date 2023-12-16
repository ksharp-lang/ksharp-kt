package org.ksharp.lsp.actions

import org.ksharp.common.Error
import org.ksharp.module.CodeArtifact
import org.ksharp.module.CodeModule
import org.ksharp.module.ModuleInfo
import org.ksharp.module.prelude.preludeModule
import org.ksharp.nodes.NodeData
import org.ksharp.parser.ksharp.toModuleNode
import org.ksharp.semantics.nodes.toCodeModule
import org.ksharp.typesystem.typeSystem

const val CodeModuleAction = "CodeModuleAction"
const val CodeModuleErrorsAction = "CodeErrorsAction"

val emptyCodeModule =
    CodeModule(
        "",
        listOf(),
        ModuleInfo(emptyMap(), typeSystem { }.value, emptyMap(), emptySet()),
        CodeArtifact(emptyList()),
        emptyMap(),
        emptyMap()
    )

fun codeModuleAction(moduleName: String, builder: ActionsGraphBuilder<CodeModule>) =
    action<List<NodeData>, CodeModule>(
        CodeModuleAction,
        emptyCodeModule
    ) {
        execution { _, nodes ->
            nodes.asSequence()
                .toModuleNode(moduleName)
                .toCodeModule(preludeModule) { _, _ ->
                    //TODO Integrate reading modules using a module loader
                    null
                }
        }
        graphBuilder(builder)
    }

fun codeModuleErrorsAction(builder: ActionsGraphBuilder<List<Error>>) =
    action<CodeModule, List<Error>>(
        CodeModuleErrorsAction,
        emptyList()
    ) {
        execution { _, module ->
            module.errors
        }
        graphBuilder(builder)
    }
