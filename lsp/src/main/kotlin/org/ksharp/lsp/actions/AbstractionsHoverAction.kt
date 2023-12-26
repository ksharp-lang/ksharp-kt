package org.ksharp.lsp.actions

import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.MarkupKind
import org.eclipse.lsp4j.Position
import org.ksharp.module.CodeArtifact
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.nodes.semantic.SemanticInfo
import org.ksharp.semantics.inference.nameWithArity

val AbstractionsHoverAction = ActionId<Hover?>("AbstractionsHoverAction")

private val Map<*, CodeArtifact>.abstractions
    get() =
        asSequence().map {
            it.value.abstractions.asSequence()
        }.flatten()

private fun Sequence<AbstractionNode<SemanticInfo>>.first(position: Position) =
    run {
        val line = position.line + 1
        val offset = position.character + 1
        firstOrNull {
            val location = it.location
            val (startLine, startOffset) = location.start
            val (endLine, endOffset) = location.end
            when {
                startLine.value < line && endLine.value > line -> true
                startLine.value == line && endLine.value == line ->
                    startOffset.value <= offset && endOffset.value >= offset

                else -> false
            }
        }
    }

fun ActionCatalog.abstractionsHoverAction(builder: ActionsGraphBuilder<Hover?>) =
    action<Position, Hover?>(AbstractionsHoverAction, null) {
        execution { state, position ->
            val codeModule = state[CodeModuleAction]
            sequenceOf(
                codeModule.artifact.abstractions.asSequence(),
                codeModule.implArtifacts.abstractions,
                codeModule.traitArtifacts.abstractions
            ).flatten()
                .first(position)?.let {
                    it.info.getInferredType(it.location).valueOrNull?.let { type ->
                        "${it.nameWithArity} :: ${type.representation}"
                    }
                }?.let {
                    Hover().apply {
                        setContents(MarkupContent(MarkupKind.PLAINTEXT, it))
                    }
                }
        }
        graphBuilder(builder)
    }
