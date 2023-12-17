package org.ksharp.lsp.actions

import org.ksharp.common.Error
import org.ksharp.compiler.loader.moduleInfoLoader
import org.ksharp.lsp.client.ClientWorkspaceModuleLoader
import org.ksharp.module.CodeArtifact
import org.ksharp.module.CodeModule
import org.ksharp.module.ModuleInfo
import org.ksharp.module.prelude.preludeModule
import org.ksharp.nodes.NodeData
import org.ksharp.parser.ksharp.toModuleNode
import org.ksharp.semantics.nodes.ModuleInfoLoader
import org.ksharp.semantics.nodes.toCodeModule
import org.ksharp.typesystem.typeSystem

val CodeModuleAction = ActionId<CodeModule>("CodeModuleAction")
val CodeModuleErrorsAction = ActionId<List<Error>>("CodeErrorsAction")

val emptyCodeModule =
    CodeModule(
        "",
        listOf(),
        ModuleInfo(emptyMap(), typeSystem { }.value, emptyMap(), emptySet()),
        CodeArtifact(emptyList()),
        emptyMap(),
        emptyMap()
    )

fun ActionCatalog.codeModuleAction(moduleName: String, builder: ActionsGraphBuilder<CodeModule>) =
    action<List<NodeData>, CodeModule>(
        CodeModuleAction,
        emptyCodeModule
    ) {
        execution { _, nodes ->
            val defaultModuleInfoLoader = ModuleInfoLoader { _, _ ->
                null
            }
            nodes.asSequence()
                .toModuleNode(moduleName)
                .toCodeModule(
                    preludeModule,
                    ClientWorkspaceModuleLoader.moduleLoader?.moduleInfoLoader ?: defaultModuleInfoLoader
                )
        }
        graphBuilder(builder)
    }

fun ActionCatalog.codeModuleErrorsAction(builder: ActionsGraphBuilder<List<Error>>) =
    action<CodeModule, List<Error>>(
        CodeModuleErrorsAction,
        emptyList()
    ) {
        execution { _, module ->
            module.errors
        }
        graphBuilder(builder)
    }
