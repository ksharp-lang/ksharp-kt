package org.ksharp.module.bytecode

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.io.bufferView
import org.ksharp.module.FunctionInfo
import org.ksharp.module.FunctionVisibility
import org.ksharp.module.ModuleInfo
import org.ksharp.typesystem.annotations.Annotation
import org.ksharp.typesystem.typeSystem
import org.ksharp.typesystem.types.TypeVisibility
import org.ksharp.typesystem.types.newParameter
import org.ksharp.typesystem.types.type
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

private fun ModuleInfo.shouldBeSerializable() {
    val output = ByteArrayOutputStream()
    writeTo(output)
    val input = ByteArrayInputStream(output.toByteArray())
    input.bufferView {
        it.readModuleInfo()
    }.apply {
        dependencies.shouldBe(this@shouldBeSerializable.dependencies)
        functions.shouldBe(this@shouldBeSerializable.functions)
        typeSystem.asSequence().toList()
            .shouldBe(this@shouldBeSerializable.typeSystem.asSequence().toList())
    }
}

class ModuleInfoSerializerTest : StringSpec({
    "Serialize ModuleInfo" {
        ModuleInfo(
            listOf("module1", "module2"),
            typeSystem {
                type(TypeVisibility.Public, "Int")
                type(TypeVisibility.Public, "String")
            }.value,
            mapOf(
                "sum" to listOf(
                    FunctionInfo(
                        true,
                        FunctionVisibility.Public,
                        null,
                        listOf(Annotation("native", mapOf("flag" to true))),
                        "sum",
                        listOf(newParameter(), newParameter())
                    ), FunctionInfo(
                        false,
                        FunctionVisibility.Public,
                        "math",
                        listOf(),
                        "sum",
                        listOf(newParameter(), newParameter(), newParameter())
                    )
                ),
                "sub" to listOf(
                    FunctionInfo(
                        false,
                        FunctionVisibility.Public,
                        null,
                        listOf(Annotation("native", mapOf("flag" to true))),
                        "sub",
                        listOf(newParameter(), newParameter())
                    )
                )
            )
        ).shouldBeSerializable()
    }
})
