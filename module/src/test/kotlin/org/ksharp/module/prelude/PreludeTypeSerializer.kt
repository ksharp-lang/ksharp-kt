package org.ksharp.module.prelude

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.*
import org.ksharp.common.io.bufferView
import org.ksharp.common.io.newBufferWriter
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
        it.readType<T>(stringPoolView)
    }.shouldBe(this)
}

class PreludeTypeSerializer : StringSpec({
    "Serialize Char Type" {
        charType.shouldBeSerializable()
    }
    "Serialize Numeric Type" {
        NumericType(Numeric.BigDecimal).shouldBeSerializable()
    }
})