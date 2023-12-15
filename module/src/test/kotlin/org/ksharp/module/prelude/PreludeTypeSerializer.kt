package org.ksharp.module.prelude

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.MockHandlePromise
import org.ksharp.common.io.bufferView
import org.ksharp.common.io.newBufferWriter
import org.ksharp.common.listBuilder
import org.ksharp.module.bytecode.mockStringTable
import org.ksharp.module.bytecode.mockStringTableView
import org.ksharp.module.prelude.types.Numeric
import org.ksharp.module.prelude.types.NumericType
import org.ksharp.module.prelude.types.charType
import org.ksharp.typesystem.serializer.readType
import org.ksharp.typesystem.serializer.writeTo
import org.ksharp.typesystem.types.Type
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream


private inline fun <reified T : Type> T.shouldBeSerializable() {
    val stringPool = listBuilder<String>()
    val buffer = newBufferWriter()
    val table = mockStringTable(stringPool)
    val output = ByteArrayOutputStream()
    writeTo(buffer, table)
    buffer.transferTo(output)
    val stringPoolView = mockStringTableView(stringPool.build())
    val input = ByteArrayInputStream(output.toByteArray())
    input.bufferView {
        it.readType<T>(MockHandlePromise(), stringPoolView)
    }.shouldBe(this)
}

class PreludeTypeSerializer : StringSpec({
    "Serialize Char Type" {
        kernelTypeSystem
        charType.shouldBeSerializable()
    }
    "Serialize Numeric Type" {
        kernelTypeSystem
        NumericType(Numeric.BigDecimal).shouldBeSerializable()
    }
})
