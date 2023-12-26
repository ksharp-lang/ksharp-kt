package org.ksharp.doc

import org.ksharp.module.ModuleInfo
import org.ksharp.nodes.AnnotationNode
import org.ksharp.nodes.ModuleNode
import org.ksharp.typesystem.types.TraitType

private val List<AnnotationNode>?.documentationValue: String
    get() {
        if (this == null) return ""
        val docAnnotation = firstOrNull { a ->
            a.name == "doc"
        }
        return if (docAnnotation != null) {
            docAnnotation.attrs["default"]?.toString() ?: ""
        } else ""
    }

fun ModuleNode.toDocModule(moduleInfo: ModuleInfo): DocModule {
    val typeSystem = moduleInfo.typeSystem

    val types = this.types
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

    val traits = this.traits
        .mapNotNull {
            val t = typeSystem[it.name].valueOrNull
            if (t != null && t is TraitType) {
                Trait(
                    it.name,
                    it.annotations.documentationValue,
                    it.definition.definitions.map { abstraction ->
                        DocAbstraction(
                            abstraction.name,
                            "",
                            ""
                        )
                    },
                    emptyList()
                )
            } else null
        }

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
