package org.ksharp.module.bytecode

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.io.bufferView
import org.ksharp.module.ModuleInfo
import org.ksharp.module.functionInfo
import org.ksharp.module.traitInfo
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.typeSystem
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
                type(setOf(CommonAttribute.Public), "Int")
                type(setOf(CommonAttribute.Public), "String")
            }.value,
            mapOf(
                "sum/2" to functionInfo(
                    setOf(CommonAttribute.Native, CommonAttribute.Public),
                    "sum",
                    listOf(newParameter(), newParameter())
                ),
                "sum/3" to functionInfo(
                    setOf(CommonAttribute.Public),
                    "sum",
                    listOf(newParameter(), newParameter(), newParameter())
                ),
                "sub/2" to functionInfo(
                    setOf(CommonAttribute.Native, CommonAttribute.Public),
                    "sub",
                    listOf(newParameter(), newParameter())
                )
            ),
            mapOf(
                "Eq" to traitInfo(
                    "Eq",
                    mapOf(
                        "sum/2" to newParameter(),
                    ), mapOf(
                        "sum/2" to functionInfo(
                            setOf(CommonAttribute.Native, CommonAttribute.Public),
                            "sum",
                            listOf(newParameter(), newParameter())
                        )
                    )
                )
            ),
        ).shouldBeSerializable()
    }
})
