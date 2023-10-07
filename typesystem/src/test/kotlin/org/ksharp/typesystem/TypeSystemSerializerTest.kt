package org.ksharp.typesystem

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.*
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import org.ksharp.common.io.bufferView
import org.ksharp.common.io.newBufferWriter
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.serializer.readType
import org.ksharp.typesystem.serializer.readTypeSystem
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

private fun mockStringTableView(items: List<String>) = BinaryTableView { index -> items[index] }

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
        it.readType<T>(stringPoolView).also { t -> println(t) }
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
        it.readTypeSystem(stringPoolView)
    }
}

class TypeSystemSerializerTest : StringSpec({
    "Serialize TypeSystem" {
        typeSystem {
            type(setOf(CommonAttribute.Public), "Int")
            type(setOf(CommonAttribute.Public), "String")
            parametricType(setOf(CommonAttribute.Public), "Map") {
                type("Int")
                type("Int")
            }
        }.value
            .shouldBeSerializable()
            .apply {
                size.shouldBe(3)
                get("Int").shouldBeType(Concrete(setOf(CommonAttribute.Public), "Int"), "Int")
                get("String").shouldBeType(Concrete(setOf(CommonAttribute.Public), "String"), "String")
                get("Map").shouldBeType(
                    ParametricType(
                        setOf(CommonAttribute.Public),
                        Alias("Map"),
                        listOf(Alias("Int"), Alias("Int"))
                    ),
                    "(Map Int Int)"
                )
            }
    }
    "Serialize Concrete Types" {
        Concrete(setOf(CommonAttribute.Public), "Int").shouldBeSerializable()
    }
    "Serialize Alias Types" {
        Alias("Int").shouldBeSerializable()
    }
    "Serialize Parameter Types" {
        Parameter("Int").shouldBeSerializable()
    }
    "Serialize Parametric Types" {
        ParametricType(
            setOf(CommonAttribute.Public),
            Alias("Map"),
            listOf(
                Concrete(setOf(CommonAttribute.Public), "String"),
                Concrete(setOf(CommonAttribute.Public), "Double")
            )
        ).shouldBeSerializable()
    }
    "Serialize Labeled Types" {
        Labeled(
            "Label",
            Concrete(setOf(CommonAttribute.Public), "String")
        ).shouldBeSerializable()
    }
    "Serialize Function Types" {
        FunctionType(
            setOf(CommonAttribute.Public),
            listOf(
                Concrete(setOf(CommonAttribute.Internal), "Int"),
                Concrete(setOf(CommonAttribute.Public), "Int2"),
                Concrete(setOf(CommonAttribute.Public), "Int3")
            )
        ).shouldBeSerializable()
    }
    "Serialize type without attributes" {
        FunctionType(
            NoAttributes,
            listOf(
                Concrete(setOf(CommonAttribute.Internal), "Int"),
                Concrete(setOf(CommonAttribute.Public), "Int2"),
                Concrete(setOf(CommonAttribute.Public), "Int3")
            )
        ).shouldBeSerializable()
    }
    "Serialize Intersection Types" {
        IntersectionType(
            setOf(CommonAttribute.Internal),
            listOf(Alias("String"), Alias("Int"))
        ).shouldBeSerializable()
    }
    "Serialize Tuple Types" {
        TupleType(
            setOf(CommonAttribute.Internal),
            listOf(Alias("String"), Alias("Int"))
        ).shouldBeSerializable()
    }
    "Serialize Union Types" {
        UnionType(
            setOf(CommonAttribute.Internal),
            mapOf(
                "String" to UnionType.ClassType(
                    "String",
                    listOf(Parameter("a"))
                ),
                "Int" to UnionType.ClassType(
                    "Int",
                    listOf(Parameter("b"))
                ),
                "Map" to UnionType.ClassType(
                    "Map",
                    listOf(
                        Concrete(setOf(CommonAttribute.Internal), "Int"),
                        Parameter("c")
                    )
                )
            )
        ).shouldBeSerializable()
    }
    "Serialize Trait Types" {
        TraitType(
            setOf(CommonAttribute.Internal),
            "Num",
            "a",
            mapOf(
                "sum" to TraitType.MethodType(
                    setOf(CommonAttribute.Public),
                    "sum",
                    listOf(
                        Parameter("a"),
                        Parameter("a"),
                        Parameter("a")
                    ), true
                ),
                "sub" to TraitType.MethodType(
                    setOf(CommonAttribute.Public),
                    "sub",
                    listOf(
                        Parameter("a"),
                        Parameter("a"),
                        Parameter("a")
                    ), false
                )
            )
        ).shouldBeSerializable()
    }
    "Serialize TypeConstructor Types" {
        TypeConstructor(
            setOf(CommonAttribute.Public),
            "True",
            "Bool"
        ).shouldBeSerializable()
    }
})
