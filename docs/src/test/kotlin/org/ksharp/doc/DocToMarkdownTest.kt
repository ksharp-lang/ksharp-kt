package org.ksharp.doc

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.io.bufferView
import org.ksharp.doc.transpiler.DocusaurusTranspilerPlugin
import org.ksharp.doc.transpiler.FileProducer
import org.ksharp.doc.transpiler.FileSystemProducer
import org.ksharp.doc.transpiler.transpile
import java.io.File
import java.nio.file.Files

class MemoryFileProducer : FileProducer {
    val content = mutableMapOf<String, String>()

    override fun write(path: String, content: String) {
        this.content[path] = content
    }
}

class DocToMarkdownTest : StringSpec({
    "Type to markdown" {
        val module = docModule(
            listOf(
                Type(
                    "Int",
                    "type Int = Int",
                    "Int is a 32-bit integer type"
                )
            ),
            emptyList(),
            emptyList()
        )
        val producer = MemoryFileProducer()
        module.transpile("test", DocusaurusTranspilerPlugin(producer))
        producer.content.shouldBe(
            mapOf(
                "test.mdx" to """
                    ---
                    title: test
                    ---
                    
                    
                    ## Types
                    
                    ### Int
                    
                    ```haskell
                    type Int = Int
                    ```
                    
                    Int is a 32-bit integer type
                    
                    
                """.trimIndent()
            )
        )
    }
    "Abstraction markdown" {
        val module = docModule(
            emptyList(),
            emptyList(),
            listOf(
                DocAbstraction(
                    "add",
                    "add :: Int -> Int -> Int",
                    "Adds two integers"
                )
            )
        )
        val producer = MemoryFileProducer()
        module.transpile("test", DocusaurusTranspilerPlugin(producer))
        producer.content.shouldBe(
            mapOf(
                "test.mdx" to """
                    ---
                    title: test
                    ---
                    
                    
                    ## Functions
                    
                    #### add
                    
                    ```haskell
                    add :: Int -> Int -> Int
                    ```
                    
                    Adds two integers
                    

                """.trimIndent()
            )
        )
    }
    "Trait markdown" {
        val module = docModule(
            emptyList(),
            listOf(
                Trait(
                    "Add",
                    "Test trait",
                    listOf(
                        DocAbstraction(
                            "add",
                            "add :: Int -> Int -> Int",
                            "Adds two integers"
                        )
                    ),
                    emptyList()
                )
            ),
            emptyList()
        )
        val producer = MemoryFileProducer()
        module.transpile("test", DocusaurusTranspilerPlugin(producer))
        producer.content.shouldBe(
            mapOf(
                "test/_category_.yml" to "className: hidden",
                "test/Add.mdx" to """
                    ---
                    title: Add
                    ---

                    Test trait

                    ## Methods

                    ### add

                    ```haskell
                    add :: Int -> Int -> Int
                    ```

                    Adds two integers


                """.trimIndent(),
                "test.mdx" to """
                    ---
                    title: test
                    ---
                    
                    
                    ## Traits
                    
                    ### Add
                    
                    Test trait
                    [details](test/Add)
                    
                    
                """.trimIndent()
            )
        )
    }
    "Create markdown for prelude module" {
        val prelude = preludeDocModule
        val root = File("preludeDoc").absoluteFile.toPath()
        Files.createDirectories(root)
        prelude.transpile("prelude", DocusaurusTranspilerPlugin(FileSystemProducer(root)))
        javaClass.getResourceAsStream("/strings.ksd")!!
            .bufferView { it.readDocModule() }
            .transpile("strings", DocusaurusTranspilerPlugin(FileSystemProducer(root)))
    }
})
