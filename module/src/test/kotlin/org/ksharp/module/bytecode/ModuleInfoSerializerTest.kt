package org.ksharp.module.bytecode

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.MockHandlePromise
import org.ksharp.common.io.bufferView
import org.ksharp.module.Impl
import org.ksharp.module.ModuleInfo
import org.ksharp.module.functionInfo
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
        it.readModuleInfo(MockHandlePromise())
    }.apply {
        this@shouldBeSerializable.dependencies.shouldBe(dependencies)
        this@shouldBeSerializable.functions.shouldBe(functions)
        this@shouldBeSerializable.typeSystem.asSequence().toList()
            .shouldBe(typeSystem.asSequence().toList())
    }
}

class ModuleInfoSerializerTest : StringSpec({
    val ts = typeSystem { }.value
    "Serialize ModuleInfo" {
        ModuleInfo(
            mapOf("m1" to "module1", "m2" to "module2"),
            typeSystem {
                type(setOf(CommonAttribute.Public), "Int")
                type(setOf(CommonAttribute.Public), "String")
            }.value,
            mapOf(
                "sum/2" to functionInfo(
                    setOf(CommonAttribute.Native, CommonAttribute.Public),
                    "sum",
                    listOf(ts.newParameter(), ts.newParameter())
                ),
                "sum/3" to functionInfo(
                    setOf(CommonAttribute.Public),
                    "sum",
                    listOf(ts.newParameter(), ts.newParameter(), ts.newParameter())
                ),
                "sub/2" to functionInfo(
                    setOf(CommonAttribute.Native, CommonAttribute.Public),
                    "sub",
                    listOf(ts.newParameter(), ts.newParameter())
                )
            ),
            setOf(
                Impl(setOf(CommonAttribute.Internal), "Eq", ts.newParameter()),
                Impl(emptySet(), "Eq2", ts.newParameter())
            )
        ).shouldBeSerializable()
    }
})
