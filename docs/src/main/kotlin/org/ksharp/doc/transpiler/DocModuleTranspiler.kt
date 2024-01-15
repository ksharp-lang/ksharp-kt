package org.ksharp.doc.transpiler

import org.ksharp.doc.DocAbstraction
import org.ksharp.doc.DocModule
import org.ksharp.doc.Trait
import org.ksharp.doc.Type

interface DocModuleTypesTranspiler {
    fun appendType(type: Type)
    fun endTypes(): DocModuleTranspiler

}

interface DocModuleTraitsTranspiler {
    fun appendTrait(type: Trait): DocModuleTraitTranspiler

    fun endTraits(): DocModuleTranspiler
}

interface DocModuleTraitTranspiler {
    fun appendTraitMethod(function: DocAbstraction)
    fun endTrait(): DocModuleTraitsTranspiler

}

interface DocModuleAbstractionsTranspiler {
    fun appendAbstraction(abstraction: DocAbstraction)
    fun endAbstractions(): DocModuleTranspiler

}

interface DocModuleTranspiler {
    fun beginTypes(): DocModuleTypesTranspiler
    fun beginTraits(): DocModuleTraitsTranspiler
    fun beginAbstractions(): DocModuleAbstractionsTranspiler
    fun endModule(): DocModuleTranspilerPlugin
}

fun interface DocModuleTranspilerPlugin {
    fun beginModule(name: String): DocModuleTranspiler

}

fun DocModule.transpile(name: String, plugin: DocModuleTranspilerPlugin) {
    plugin.beginModule(name)
        .apply {
            if (types.isNotEmpty())
                beginTypes().apply {
                    types.forEach(::appendType)
                }.endTypes()
            if (traits.isNotEmpty())
                beginTraits().apply {
                    traits.forEach {
                        appendTrait(it).apply {
                            it.abstractions.forEach(::appendTraitMethod)
                        }.endTrait()
                    }
                }.endTraits()
            if (abstractions.isNotEmpty())
                beginAbstractions().apply {
                    abstractions.forEach(::appendAbstraction)
                }.endAbstractions()
        }.endModule()
}
