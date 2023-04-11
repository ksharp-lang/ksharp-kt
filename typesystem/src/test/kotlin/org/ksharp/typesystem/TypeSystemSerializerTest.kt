package org.ksharp.typesystem

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.*
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import org.ksharp.common.io.bufferView
import org.ksharp.common.io.newBufferWriter
import org.ksharp.typesystem.serializer.readTypeFrom
import org.ksharp.typesystem.serializer.readTypeSystemFrom
import org.ksharp.typesystem.serializer.writeTo
import org.ksharp.typesystem.types.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicInteger

private fun mockStringTable(items: ListBuilder<String>) = object : BinaryTable {
    private val counter = AtomicInteger(-1)
    private val dictionary = mapBuilder<String, Int>()
    override fun add(name: String): Int =
        dictionary.get(name) ?: run {
            items.add(name)
            dictionary.put(name, counter.incrementAndGet())
            counter.get()
        }
}

private fun mockStringTableView(items: List<String>) = object : BinaryTableView {
    override fun get(index: Int): String = items[index]
}

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
        it.readTypeFrom<T>(stringPoolView).also { t -> println(t) }
    }.shouldBe(this)
}

private fun TypeSystem.shouldBeSerializable(): TypeSystem {
    val stringPool = listBuilder<String>()
    val buffer = newBufferWriter()
    val table = mockStringTable(stringPool)
    val output = ByteArrayOutputStream()
    writeTo(buffer, table)
    buffer.transferTo(output)
    val stringPoolView = mockStringTableView(stringPool.build())
    val input = ByteArrayInputStream(output.toByteArray())
    return input.bufferView {
        it.readTypeSystemFrom(stringPoolView)
    }
}

class TypeSystemSerializerTest : StringSpec({
    "Serialize TypeSystem" {
        typeSystem {
            type("Int")
            type("String")
            parametricType("Map") {
                type("Int")
                type("Int")
            }
        }.value
            .shouldBeSerializable()
            .apply {
                size.shouldBe(3)
                get("Int").shouldBeType(Concrete("Int"), "Int")
                get("String").shouldBeType(Concrete("String"), "String")
                get("Map").shouldBeType(
                    ParametricType(Alias("Map"), listOf(Alias("Int"), Alias("Int"))),
                    "(Map Int Int)"
                )
            }
    }
    "Serialize Concrete Types" {
        Concrete("Int").shouldBeSerializable()
    }
    "Serialize Alias Types" {
        Alias("Int").shouldBeSerializable()
    }
    "Serialize Parameter Types" {
        Parameter("Int").shouldBeSerializable()
    }
    "Serialize Parametric Types" {
        ParametricType(
            Alias("Map"),
            listOf(
                Concrete("String"),
                Concrete("Double")
            )
        ).shouldBeSerializable()
    }
    "Serialize Labeled Types" {
        Labeled(
            "Label",
            Concrete("String")
        ).shouldBeSerializable()
    }
    "Serialize Function Types" {
        FunctionType(
            listOf(
                Concrete("Int"),
                Concrete("Int2"),
                Concrete("Int3")
            )
        ).shouldBeSerializable()
    }
    "Serialize Intersection Types" {
        IntersectionType(
            listOf(Alias("String"), Alias("Int"))
        ).shouldBeSerializable()
    }
    "Serialize Tuple Types" {
        TupleType(
            listOf(Alias("String"), Alias("Int"))
        ).shouldBeSerializable()
    }
    "Serialize Union Types" {
        UnionType(
            mapOf(
                "String" to UnionType.ClassType("String", listOf(Parameter("a"))),
                "Int" to UnionType.ClassType("Int", listOf(Parameter("b"))),
                "Map" to UnionType.ClassType("Map", listOf(Concrete("Int"), Parameter("c")))
            )
        ).shouldBeSerializable()
    }
    "Serialize Trait Types" {
        TraitType(
            "Num",
            "a",
            mapOf(
                "sum" to TraitType.MethodType("sum", listOf(Parameter("a"), Parameter("a"), Parameter("a"))),
                "sub" to TraitType.MethodType("sub", listOf(Parameter("a"), Parameter("a"), Parameter("a")))
            )
        ).shouldBeSerializable()
    }
})