package org.ksharp.compiler

import io.kotest.core.spec.style.StringSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.ksharp.common.new
import org.ksharp.compiler.loader.*
import org.ksharp.module.ModuleInfo
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
                it.shouldBeInstanceOf<Module>()
                it.name.shouldBe("ten")
                it.info.dependencies.shouldBeEmpty()
                it.info.impls.shouldBeEmpty()
                it.info.functions.mapValues { entry ->
                    entry.value.types.toFunctionType(it.info.typeSystem).representation
                }.shouldBe(mapOf("ten/0" to "(Unit -> Long)"))
                it.executable.execute("ten/0").shouldBe(10L)
                it.documentation.documentation("ten/0").shouldBe("Number 10")
                it.documentation.representation("ten/0").shouldBe("Unit -> Long")
                Files.exists(binaries.resolve("ten.ksm")).shouldBeTrue()
                Files.exists(binaries.resolve("ten.ksd")).shouldBeTrue()
                Files.exists(binaries.resolve("ten.ksc")).shouldBeTrue()
            }

        val loader2 = ModuleLoader(sources, preludeModule)
        loader2.load("ten", "")
            .shouldBeRight()
            .map {
                it.shouldBeInstanceOf<Module>()
                it.name.shouldBe("ten")
                it.info.dependencies.shouldBeEmpty()
                it.info.impls.shouldBeEmpty()
                it.info.functions.mapValues { entry ->
                    entry.value.types.toFunctionType(it.info.typeSystem).representation
                }.shouldBe(mapOf("ten/0" to "(Unit -> Long)"))
                it.executable.execute("ten/0").shouldBe(10L)
                it.documentation.documentation("ten/0").shouldBe("Number 10")
                it.documentation.representation("ten/0").shouldBe("Unit -> Long")
            }

        loader2.moduleInfoLoader.load("ten", "")
            .shouldNotBeNull()
            .apply {
                shouldBeInstanceOf<ModuleInfo>()
                dependencies.shouldBeEmpty()
                impls.shouldBeEmpty()
                functions.mapValues { entry ->
                    entry.value.types.toFunctionType(typeSystem).representation
                }.shouldBe(mapOf("ten/0" to "(Unit -> Long)"))
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
