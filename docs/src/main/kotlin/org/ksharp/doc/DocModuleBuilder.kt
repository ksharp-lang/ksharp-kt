package org.ksharp.doc

import org.ksharp.module.ModuleInfo
import org.ksharp.nodes.AnnotationNode
import org.ksharp.nodes.FunctionTypeNode
import org.ksharp.nodes.ModuleNode
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.TraitType

private val List<AnnotationNode>?.documentationValue: String
    get() {
        if (this == null) return ""
        val docAnnotation = firstOrNull { a ->
            a.name == "doc"
        }
        return if (docAnnotation != null) {
            docAnnotation.attrs["default"]?.toString()?.trimIndent() ?: ""
        } else ""
    }

private fun ModuleNode.getTypes(typeSystem: TypeSystem) =
    this.types
        .mapNotNull {
            val t = typeSystem[it.name].valueOrNull
            if (t != null) {
                Type(
                    it.name,
                    t.representation,
                    it.annotations.documentationValue
                )
            } else null
        }

private fun ModuleNode.getTraits(typeSystem: TypeSystem) =
    this.traits
        .mapNotNull {
            val t = typeSystem[it.name].valueOrNull
            if (t != null && t is TraitType) {
                Trait(
                    it.name,
                    it.annotations.documentationValue,
                    it.definition.definitions.mapNotNull { abstraction ->
                        val fnNode = abstraction.type
                        if (fnNode is FunctionTypeNode) {
                            val name = "${abstraction.name}/${fnNode.arity}"
                            val traitFunction = t.methods[name]
                            if (traitFunction != null) {
                                DocAbstraction(
                                    name,
                                    traitFunction.representation,
                                    abstraction.annotations.documentationValue
                                )
                            } else null
                        } else null
                    },
                    emptyList()
                )
            } else null
        }

fun ModuleNode.toDocModule(moduleInfo: ModuleInfo): DocModule {
    val typeSystem = moduleInfo.typeSystem

    val types = getTypes(typeSystem)

    val traits = getTraits(typeSystem)

    val abstractions = functions.mapNotNull {
        val name = "${it.name}/${it.parameters.size}"
        val fn = moduleInfo.functions[name]
        if (fn != null) {
            DocAbstraction(
                name,
                fn.types.joinToString(" -> ") { t -> t.representation },
                it.annotations.documentationValue
            )
        } else null
    }

    return docModule(
        types,
        traits,
        abstractions
    )
}
