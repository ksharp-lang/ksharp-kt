package org.ksharp.typesystem

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.*
import org.ksharp.common.io.BinaryTable
import org.ksharp.common.io.BinaryTableView
import org.ksharp.common.io.bufferView
import org.ksharp.common.io.newBufferWriter
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
            type(TypeVisibility.Public, "Int")
            type(TypeVisibility.Public, "String")
            parametricType(TypeVisibility.Public, "Map") {
                type("Int")
                type("Int")
            }
        }.value
            .shouldBeSerializable()
            .apply {
                size.shouldBe(3)
                get("Int").shouldBeType(Concrete(TypeVisibility.Public, "Int"), "Int")
                get("String").shouldBeType(Concrete(TypeVisibility.Public, "String"), "String")
                get("Map").shouldBeType(
                    ParametricType(
                        TypeVisibility.Public,
                        Alias(TypeVisibility.Public, "Map"),
                        listOf(Alias(TypeVisibility.Public, "Int"), Alias(TypeVisibility.Public, "Int"))
                    ),
                    "(Map Int Int)"
                )
            }
    }
    "Serialize Concrete Types" {
        Concrete(TypeVisibility.Public, "Int").shouldBeSerializable()
    }
    "Serialize Alias Types" {
        Alias(TypeVisibility.Public, "Int").shouldBeSerializable()
    }
    "Serialize Parameter Types" {
        Parameter(TypeVisibility.Public, "Int").shouldBeSerializable()
    }
    "Serialize Parametric Types" {
        ParametricType(
            TypeVisibility.Public,
            Alias(TypeVisibility.Public, "Map"),
            listOf(
                Concrete(TypeVisibility.Public, "String"),
                Concrete(TypeVisibility.Public, "Double")
            )
        ).shouldBeSerializable()
    }
    "Serialize Labeled Types" {
        Labeled(
            "Label",
            Concrete(TypeVisibility.Public, "String")
        ).shouldBeSerializable()
    }
    "Serialize Function Types" {
        FunctionType(
            TypeVisibility.Public,
            listOf(
                Concrete(TypeVisibility.Internal, "Int"),
                Concrete(TypeVisibility.Public, "Int2"),
                Concrete(TypeVisibility.Public, "Int3")
            )
        ).shouldBeSerializable()
    }
    "Serialize Intersection Types" {
        IntersectionType(
            TypeVisibility.Internal,
            listOf(Alias(TypeVisibility.Public, "String"), Alias(TypeVisibility.Internal, "Int"))
        ).shouldBeSerializable()
    }
    "Serialize Tuple Types" {
        TupleType(
            TypeVisibility.Internal,
            listOf(Alias(TypeVisibility.Internal, "String"), Alias(TypeVisibility.Internal, "Int"))
        ).shouldBeSerializable()
    }
    "Serialize Union Types" {
        UnionType(
            TypeVisibility.Internal,
            mapOf(
                "String" to UnionType.ClassType(
                    TypeVisibility.Internal,
                    "String",
                    listOf(Parameter(TypeVisibility.Internal, "a"))
                ),
                "Int" to UnionType.ClassType(
                    TypeVisibility.Internal,
                    "Int",
                    listOf(Parameter(TypeVisibility.Internal, "b"))
                ),
                "Map" to UnionType.ClassType(
                    TypeVisibility.Internal,
                    "Map",
                    listOf(Concrete(TypeVisibility.Internal, "Int"), Parameter(TypeVisibility.Internal, "c"))
                )
            )
        ).shouldBeSerializable()
    }
    "Serialize Trait Types" {
        TraitType(
            TypeVisibility.Internal,
            "Num",
            "a",
            mapOf(
                "sum" to TraitType.MethodType(
                    TypeVisibility.Public,
                    "sum",
                    listOf(
                        Parameter(TypeVisibility.Public, "a"),
                        Parameter(TypeVisibility.Public, "a"),
                        Parameter(TypeVisibility.Public, "a")
                    )
                ),
                "sub" to TraitType.MethodType(
                    TypeVisibility.Public,
                    "sub",
                    listOf(
                        Parameter(TypeVisibility.Public, "a"),
                        Parameter(TypeVisibility.Public, "a"),
                        Parameter(TypeVisibility.Public, "a")
                    )
                )
            )
        ).shouldBeSerializable()
    }
    "Serialize TypeConstructor Types" {
        TypeConstructor(
            TypeVisibility.Public,
            "True",
            "Bool"
        ).shouldBeSerializable()
    }
})