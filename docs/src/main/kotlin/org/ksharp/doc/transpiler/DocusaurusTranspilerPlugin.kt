package org.ksharp.doc.transpiler

import org.ksharp.doc.DocAbstraction
import org.ksharp.doc.Trait
import org.ksharp.doc.Type
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText

private fun String.cleanMarkdownText(): String =
    this.replace("<", "&lt;").replace(">", "&gt;")

private fun String.representationMarkdown(): String =
    """```haskell
        |${this}
        |```
    """.trimMargin()

class DocusaurusTranspilerPlugin(private val root: Path) : DocModuleTranspilerPlugin {
    override fun beginModule(name: String): DocModuleTranspiler {
        return DocusaurusModuleTranspiler(this, root, name)
    }

}

class DocusaurusModuleTranspiler(
    private val plugin: DocModuleTranspilerPlugin,
    private val root: Path,
    private val module: String
) : DocModuleTranspiler {
    private val content = StringBuilder().apply {
        appendLine("---")
        appendLine("title: ${module.cleanMarkdownText()}")
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
        val traitRoot = root.resolve(module)
        Files.createDirectories(traitRoot)
        traitRoot.resolve("_category_.yml").writeText("className: hidden", Charsets.UTF_8)
        return DocusaurusTraitsTranspiler(this, traitRoot, module, content)
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
        val modulePath = root.resolve("$module.mdx")
        modulePath.writeText(content.toString(), Charsets.UTF_8)
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
            appendLine("### ${type.name.cleanMarkdownText()}")
            appendLine()
            appendLine(type.representation.representationMarkdown())
            appendLine()
            appendLine(type.documentation.cleanMarkdownText())
            appendLine()
        }
    }

    override fun endTypes(): DocModuleTranspiler = moduleTranspiler

}

class DocusaurusTraitsTranspiler(
    private val moduleTranspiler: DocModuleTranspiler,
    private val traitRoot: Path,
    private val module: String,
    private val content: StringBuilder
) : DocModuleTraitsTranspiler {
    override fun appendTrait(type: Trait): DocModuleTraitTranspiler {
        val traitContent = StringBuilder().apply {
            appendLine("---")
            appendLine("title: ${type.name.cleanMarkdownText()}")
            appendLine("---")
            appendLine()
            appendLine(type.documentation.cleanMarkdownText())
            appendLine()
            appendLine("## Methods")
            appendLine()
        }
        content.apply {
            appendLine("### ${type.name.cleanMarkdownText()}")
            appendLine()
            appendLine(type.documentation.cleanMarkdownText())
            appendLine("[details]($module/${type.name})")
            appendLine()
        }
        return DocusaurusTraitTranspiler(this, traitRoot, type.name, traitContent)
    }

    override fun endTraits(): DocModuleTranspiler = moduleTranspiler

}


class DocusaurusTraitTranspiler(
    private val traitsTranspiler: DocModuleTraitsTranspiler,
    private val traitRoot: Path,
    private val name: String,
    private val content: StringBuilder
) : DocModuleTraitTranspiler {
    override fun appendTraitMethod(function: DocAbstraction) {
        content.apply {
            appendLine("### ${function.name.cleanMarkdownText()}")
            appendLine()
            appendLine(function.representation.representationMarkdown())
            appendLine()
            appendLine(function.documentation.cleanMarkdownText())
            appendLine()
        }
    }

    override fun endTrait(): DocModuleTraitsTranspiler {
        val modulePath = traitRoot.resolve("$name.mdx")
        modulePath.writeText(content.toString(), Charsets.UTF_8)
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
            appendLine("#### ${abstraction.name.cleanMarkdownText()}")
            appendLine()
            appendLine(abstraction.representation.representationMarkdown())
            appendLine()
            appendLine(abstraction.documentation.cleanMarkdownText())
            appendLine()
        }
    }

    override fun endAbstractions(): DocModuleTranspiler = moduleTranspiler

}
