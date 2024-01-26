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
    this.shouldBe(input.bufferView {
        it.readType<T>(typeSystem, stringPoolView).also { t -> println(t) }
    })
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
    val mockHandle = MockHandlePromise<TypeSystem>()
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
                get("Int").shouldBeType(Concrete(handle, setOf(CommonAttribute.Public), "Int"), "Int")
                get("String").shouldBeType(Concrete(handle, setOf(CommonAttribute.Public), "String"), "String")
                get("Map").shouldBeType(
                    ParametricType(
                        handle,
                        setOf(CommonAttribute.Public),
                        Alias(handle, "Map"),
                        listOf(Alias(handle, "Int"), Alias(handle, "Int"))
                    ),
                    "(Map Int Int)"
                )
            }
    }
    "Serialize Concrete Types" {
        Concrete(mockHandle, setOf(CommonAttribute.Public), "Int").shouldBeSerializable()
    }
    "Serialize Alias Types" {
        Alias(mockHandle, "Int").shouldBeSerializable()
    }
    "Serialize Parameter Types" {
        Parameter(mockHandle, "Int").shouldBeSerializable()
    }
    "Serialize Parametric Types" {
        ParametricType(
            mockHandle, setOf(CommonAttribute.Public),
            Alias(mockHandle, "Map"),
            listOf(
                Concrete(mockHandle, setOf(CommonAttribute.Public), "String"),
                Concrete(mockHandle, setOf(CommonAttribute.Public), "Double")
            )
        ).shouldBeSerializable()
    }
    "Serialize Labeled Types" {
        Labeled(
            "Label",
            Concrete(mockHandle, setOf(CommonAttribute.Public), "String")
        ).shouldBeSerializable()
    }
    "Serialize Function Types" {
        FullFunctionType(
            mockHandle, setOf(CommonAttribute.Public),
            listOf(
                Concrete(mockHandle, setOf(CommonAttribute.Internal), "Int"),
                Concrete(mockHandle, setOf(CommonAttribute.Public), "Int2"),
                Concrete(mockHandle, setOf(CommonAttribute.Public), "Int3")
            )
        ).shouldBeSerializable()
    }
    "Serialize Partial Function Types" {
        PartialFunctionType(
            listOf(
                Concrete(mockHandle, setOf(CommonAttribute.Internal), "Int"),
                Concrete(mockHandle, setOf(CommonAttribute.Public), "Int2"),
            ),
            FullFunctionType(
                mockHandle, setOf(CommonAttribute.Public),
                listOf(
                    Concrete(mockHandle, setOf(CommonAttribute.Internal), "Int"),
                    Concrete(mockHandle, setOf(CommonAttribute.Public), "Int2"),
                    Concrete(mockHandle, setOf(CommonAttribute.Public), "Int3")
                )
            )
        ).shouldBeSerializable()
    }
    "Serialize type without attributes" {
        FullFunctionType(
            mockHandle, NoAttributes,
            listOf(
                Concrete(mockHandle, setOf(CommonAttribute.Internal), "Int"),
                Concrete(mockHandle, setOf(CommonAttribute.Public), "Int2"),
                Concrete(mockHandle, setOf(CommonAttribute.Public), "Int3")
            )
        ).shouldBeSerializable()
    }
    "Serialize Intersection Types" {
        IntersectionType(
            mockHandle, setOf(CommonAttribute.Internal),
            listOf(Alias(mockHandle, "String"), Alias(mockHandle, "Int"))
        ).shouldBeSerializable()
    }
    "Serialize Tuple Types" {
        TupleType(
            mockHandle, setOf(CommonAttribute.Internal),
            listOf(Alias(mockHandle, "String"), Alias(mockHandle, "Int"))
        ).shouldBeSerializable()
    }
    "Serialize Union Types" {
        UnionType(
            mockHandle, setOf(CommonAttribute.Internal),
            mapOf(
                "String" to UnionType.ClassType(
                    mockHandle, "String",
                    listOf(Parameter(mockHandle, "a"))
                ),
                "Int" to UnionType.ClassType(
                    mockHandle, "Int",
                    listOf(Parameter(mockHandle, "b"))
                ),
                "Map" to UnionType.ClassType(
                    mockHandle, "Map",
                    listOf(
                        Concrete(mockHandle, setOf(CommonAttribute.Internal), "Int"),
                        Parameter(mockHandle, "c")
                    )
                )
            )
        ).shouldBeSerializable()
    }
    "Serialize Trait Types" {
        TraitType(
            mockHandle, setOf(CommonAttribute.Internal),
            "module",
            "Num",
            "a",
            mapOf(
                "sum" to TraitType.MethodType(
                    mockHandle, setOf(CommonAttribute.Public),
                    "sum",
                    listOf(
                        Parameter(mockHandle, "a"),
                        Parameter(mockHandle, "a"),
                        Parameter(mockHandle, "a")
                    ), true
                ),
                "sub" to TraitType.MethodType(
                    mockHandle, setOf(CommonAttribute.Public),
                    "sub",
                    listOf(
                        Parameter(mockHandle, "a"),
                        Parameter(mockHandle, "a"),
                        Parameter(mockHandle, "a")
                    ), false
                )
            )
        ).shouldBeSerializable()
    }
    "Serialize Trait Types 2" {
        TraitType(
            mockHandle, setOf(CommonAttribute.Internal),
            "",
            "Num",
            "a",
            mapOf(
                "sum" to TraitType.MethodType(
                    mockHandle, setOf(CommonAttribute.Public),
                    "sum",
                    listOf(
                        Parameter(mockHandle, "a"),
                        Parameter(mockHandle, "a"),
                        Parameter(mockHandle, "a")
                    ), true
                ),
                "sub" to TraitType.MethodType(
                    mockHandle, setOf(CommonAttribute.Public),
                    "sub",
                    listOf(
                        Parameter(mockHandle, "a"),
                        Parameter(mockHandle, "a"),
                        Parameter(mockHandle, "a")
                    ), false
                )
            )
        ).shouldBeSerializable()
    }
    "Serialize TypeConstructor Types" {
        TypeConstructor(
            mockHandle, setOf(CommonAttribute.Public),
            "True",
            "Bool"
        ).shouldBeSerializable()
    }
    "Serialize FixedTrait Types" {
        FixedTraitType(
            TraitType(
                mockHandle, setOf(CommonAttribute.Internal),
                "",
                "Num",
                "a",
                mapOf(
                    "sum" to TraitType.MethodType(
                        mockHandle, setOf(CommonAttribute.Public),
                        "sum",
                        listOf(
                            Parameter(mockHandle, "a"),
                            Parameter(mockHandle, "a"),
                            Parameter(mockHandle, "a")
                        ), true
                    ),
                    "sub" to TraitType.MethodType(
                        mockHandle, setOf(CommonAttribute.Public),
                        "sub",
                        listOf(
                            Parameter(mockHandle, "a"),
                            Parameter(mockHandle, "a"),
                            Parameter(mockHandle, "a")
                        ), false
                    )
                )
            )
        ).shouldBeSerializable()
    }
    "Serialize Impl type" {
        ImplType(
            TraitType(
                mockHandle, setOf(CommonAttribute.Internal),
                "",
                "Num",
                "a",
                mapOf(
                    "sum" to TraitType.MethodType(
                        mockHandle, setOf(CommonAttribute.Public),
                        "sum",
                        listOf(
                            Parameter(mockHandle, "a"),
                            Parameter(mockHandle, "a"),
                            Parameter(mockHandle, "a")
                        ), true
                    ),
                    "sub" to TraitType.MethodType(
                        mockHandle, setOf(CommonAttribute.Public),
                        "sub",
                        listOf(
                            Parameter(mockHandle, "a"),
                            Parameter(mockHandle, "a"),
                            Parameter(mockHandle, "a")
                        ), false
                    )
                )
            ),
            Parameter(mockHandle, "A")
        ).shouldBeSerializable()
    }
})
