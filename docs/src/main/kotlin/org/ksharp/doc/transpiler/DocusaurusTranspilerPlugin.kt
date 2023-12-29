package org.ksharp.doc.transpiler

import org.ksharp.doc.DocAbstraction
import org.ksharp.doc.Trait
import org.ksharp.doc.Type

private val String.markdownText: String
    get() =
        this.replace("<", "&lt;").replace(">", "&gt;")

private val String.representationMarkdown: String
    get() =
        """```haskell
        |${this}
        |```
    """.trimMargin()

class DocusaurusTranspilerPlugin(private val fileProducer: FileProducer) : DocModuleTranspilerPlugin {
    override fun beginModule(name: String): DocModuleTranspiler {
        return DocusaurusModuleTranspiler(this, fileProducer, name)
    }

}

class DocusaurusModuleTranspiler(
    private val plugin: DocModuleTranspilerPlugin,
    private val fileProducer: FileProducer,
    private val module: String
) : DocModuleTranspiler {
    private val content = StringBuilder().apply {
        appendLine("---")
        appendLine("title: $module")
        appendLine("---")
        appendLine()
    }

    override fun beginTypes(): DocModuleTypesTranspiler {
        content.apply {
            appendLine()
            appendLine("## Types")
            appendLine()
        }
        return DocusaurusTypesTranspiler(this, content)
    }

    override fun beginTraits(): DocModuleTraitsTranspiler {
        content.apply {
            appendLine()
            appendLine("## Traits")
            appendLine()
        }
        fileProducer.write("$module/_category_.yml", "className: hidden")
        return DocusaurusTraitsTranspiler(this, fileProducer, module, content)
    }

    override fun beginAbstractions(): DocModuleAbstractionsTranspiler {
        content.apply {
            appendLine()
            appendLine("## Functions")
            appendLine()
        }
        return DocusaurusAbstractionsTranspiler(this, content)
    }

    override fun endModule(): DocModuleTranspilerPlugin {
        fileProducer.write("$module.mdx", content.toString())
        return plugin
    }

}

class DocusaurusTypesTranspiler(
    private val moduleTranspiler: DocModuleTranspiler,
    private val content: StringBuilder
) :
    DocModuleTypesTranspiler {
    override fun appendType(type: Type) {
        content.apply {
            appendLine("### ${type.name.markdownText}")
            appendLine()
            appendLine(type.representation.representationMarkdown)
            appendLine()
            appendLine(type.documentation.markdownText)
            appendLine()
        }
    }

    override fun endTypes(): DocModuleTranspiler = moduleTranspiler

}

class DocusaurusTraitsTranspiler(
    private val moduleTranspiler: DocModuleTranspiler,
    private val fileProducer: FileProducer,
    private val module: String,
    private val content: StringBuilder
) : DocModuleTraitsTranspiler {
    override fun appendTrait(type: Trait): DocModuleTraitTranspiler {
        val traitContent = StringBuilder().apply {
            appendLine("---")
            appendLine("title: ${type.name}")
            appendLine("---")
            appendLine()
            appendLine(type.documentation.markdownText)
            appendLine()
            appendLine("## Methods")
            appendLine()
        }
        content.apply {
            appendLine("### ${type.name.markdownText}")
            appendLine()
            appendLine(type.documentation.markdownText)
            appendLine("[details]($module/${type.name})")
            appendLine()
        }
        return DocusaurusTraitTranspiler(this, fileProducer, module, type.name, traitContent)
    }

    override fun endTraits(): DocModuleTranspiler = moduleTranspiler

}


class DocusaurusTraitTranspiler(
    private val traitsTranspiler: DocModuleTraitsTranspiler,
    private val fileProducer: FileProducer,
    private val module: String,
    private val name: String,
    private val content: StringBuilder
) : DocModuleTraitTranspiler {
    override fun appendTraitMethod(function: DocAbstraction) {
        content.apply {
            appendLine("### ${function.name.markdownText}")
            appendLine()
            appendLine(function.representation.representationMarkdown)
            appendLine()
            appendLine(function.documentation.markdownText)
            appendLine()
        }
    }

    override fun endTrait(): DocModuleTraitsTranspiler {
        fileProducer.write("$module/$name.mdx", content.toString())
        return traitsTranspiler
    }

}

class DocusaurusAbstractionsTranspiler(
    private val moduleTranspiler: DocModuleTranspiler,
    private val content: StringBuilder
) :
    DocModuleAbstractionsTranspiler {
    override fun appendAbstraction(abstraction: DocAbstraction) {
        content.apply {
            appendLine("#### ${abstraction.name.markdownText}")
            appendLine()
            appendLine(abstraction.representation.representationMarkdown)
            appendLine()
            appendLine(abstraction.documentation.markdownText)
            appendLine()
        }
    }

    override fun endAbstractions(): DocModuleTranspiler = moduleTranspiler

}
