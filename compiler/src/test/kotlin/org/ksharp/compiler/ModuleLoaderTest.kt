package org.ksharp.compiler

import io.kotest.core.spec.style.StringSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.ksharp.common.new
import org.ksharp.compiler.loader.DirectorySourceLoader
import org.ksharp.compiler.loader.ModuleInfoInterface
import org.ksharp.compiler.loader.ModuleLoader
import org.ksharp.compiler.loader.ModuleLoaderErrorCode
import org.ksharp.module.prelude.preludeModule
import org.ksharp.test.shouldBeLeft
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.types.toFunctionType
import java.io.File
import java.nio.file.Files

class ModuleLoaderTest : StringSpec({
    val binaries = tempdir().toPath()
    val sources = DirectorySourceLoader(
        File("src/test/resources").absoluteFile.toPath(),
        binaries
    )

    "Compile a file using ModuleLoader" {
        val loader = ModuleLoader(sources, preludeModule)
        loader.load("ten", "")
            .shouldBeRight()
            .map {
                it.shouldBeInstanceOf<ModuleInfoInterface>()
                it.name.shouldBe("ten")
                it.dependencies.shouldBeEmpty()
                it.impls.shouldBeEmpty()
                it.functions.mapValues { entry ->
                    entry.value.types.toFunctionType(it.typeSystem).representation
                }.shouldBe(mapOf("ten/0" to "(Unit -> (Num numeric<Long>))"))
                it.executable.execute("ten/0").shouldBe(10L)
                Files.exists(binaries.resolve("ten.ksm")).shouldBeTrue()
                Files.exists(binaries.resolve("ten.ksc")).shouldBeTrue()
            }

        val loader2 = ModuleLoader(sources, preludeModule)
        loader2.load("ten", "")
            .shouldBeRight()
            .map {
                it.shouldBeInstanceOf<ModuleInfoInterface>()
                it.name.shouldBe("ten")
                it.dependencies.shouldBeEmpty()
                it.impls.shouldBeEmpty()
                it.functions.mapValues { entry ->
                    entry.value.types.toFunctionType(it.typeSystem).representation
                }.shouldBe(mapOf("ten/0" to "(Unit -> (Num numeric<Long>))"))
                it.executable.execute("ten/0").shouldBe(10L)
            }
    }

    "Load a file that not exists" {
        val loader = ModuleLoader(sources, preludeModule)
        loader.load("ten2", "")
            .shouldBeLeft(
                listOf(ModuleLoaderErrorCode.ModuleNotFound.new("name" to "ten2"))
            )
    }
})
