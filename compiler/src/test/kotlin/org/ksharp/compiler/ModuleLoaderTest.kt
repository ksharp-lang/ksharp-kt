package org.ksharp.compiler

import io.kotest.core.spec.style.StringSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.ksharp.compiler.loader.DirectorySourceLoader
import org.ksharp.compiler.loader.ModuleLoader
import org.ksharp.module.prelude.preludeModule
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.types.toFunctionType
import java.io.File
import java.nio.file.Files

class ModuleLoaderTest : StringSpec({
    val binaries = tempdir().toPath()
    "Compile a file using ModuleLoader" {
        val sources = DirectorySourceLoader(
            File("src/test/resources").absoluteFile.toPath(),
            binaries
        )
        val loader = ModuleLoader(sources, preludeModule)
        loader.load("ten", "")
            .shouldBeRight()
            .map {
                it.name.shouldBe("ten")
                it.dependencies.shouldBeEmpty()
                it.impls.shouldBeEmpty()
                it.functions.mapValues { entry ->
                    entry.value.types.toFunctionType(it.typeSystem).representation
                }.shouldBe(mapOf("ten/0" to "(Unit -> (Num numeric<Long>))"))
                Files.exists(binaries.resolve("ten.ksm")).shouldBeTrue()
                Files.exists(binaries.resolve("ten.ksc")).shouldBeTrue()
            }
    }
})
