package org.ksharp.compiler

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.ksharp.common.Line
import org.ksharp.common.Location
import org.ksharp.common.Offset
import org.ksharp.module.FunctionVisibility
import org.ksharp.nodes.semantic.AbstractionNode
import org.ksharp.nodes.semantic.ConstantNode
import org.ksharp.semantics.nodes.AbstractionSemanticInfo
import org.ksharp.semantics.nodes.TypeSemanticInfo
import org.ksharp.test.shouldBeRight
import java.io.File
import kotlin.io.path.Path

class CompilerTestModuleInfo : StringSpec({
    "Create a moduleinfo from a String" {
        """
        |ten = 10
        """.trimMargin()
            .moduleInfo("file1.ff")
            .shouldBeRight()
            .apply {
                map {
                    it.name.shouldBe("file1")
                    it.errors.shouldBeEmpty()
                    it.abstractions.shouldBe(
                        listOf(
                            AbstractionNode(
                                name = "ten",
                                expression = ConstantNode(
                                    value = 10.toLong(),
                                    info = TypeSemanticInfo(type = it.typeSystem.get("Byte")),
                                    location = Location(
                                        context = "file1.ff", position = Line(value = 1) to Offset(value = 0)
                                    )
                                ),
                                info = AbstractionSemanticInfo(
                                    visibility = FunctionVisibility.Internal,
                                    parameters = listOf()
                                ),
                                location = Location(
                                    context = "file1.ff",
                                    position = Line(value = 1) to Offset(value = 0)
                                )
                            )
                        )
                    )
                }
            }
    }
    "Create a moduleinfo from a String 2" {
        """
        |ten = 10
        """.trimMargin()
            .moduleInfo("file1")
            .shouldBeRight()
            .apply {
                map {
                    it.name.shouldBe("file1")
                    it.errors.shouldBeEmpty()
                    it.abstractions.shouldBe(
                        listOf(
                            AbstractionNode(
                                name = "ten",
                                expression = ConstantNode(
                                    value = 10.toLong(),
                                    info = TypeSemanticInfo(type = it.typeSystem.get("Byte")),
                                    location = Location(
                                        context = "file1", position = Line(value = 1) to Offset(value = 0)
                                    )
                                ),
                                info = AbstractionSemanticInfo(
                                    visibility = FunctionVisibility.Internal,
                                    parameters = listOf()
                                ),
                                location = Location(
                                    context = "file1",
                                    position = Line(value = 1) to Offset(value = 0)
                                )
                            )
                        )
                    )
                }
            }
    }
    "Create a moduleinfo from a File" {
        File("src/test/resources/ten.ff")
            .moduleInfo()
            .shouldBeRight()
            .apply {
                map {
                    it.name.shouldBe("ten")
                    it.errors.shouldBeEmpty()
                    it.abstractions.shouldBe(
                        listOf(
                            AbstractionNode(
                                name = "ten",
                                expression = ConstantNode(
                                    value = 10.toLong(),
                                    info = TypeSemanticInfo(type = it.typeSystem.get("Byte")),
                                    location = Location(
                                        context = "ten.ff", position = Line(value = 1) to Offset(value = 0)
                                    )
                                ),
                                info = AbstractionSemanticInfo(
                                    visibility = FunctionVisibility.Internal,
                                    parameters = listOf()
                                ),
                                location = Location(
                                    context = "ten.ff",
                                    position = Line(value = 1) to Offset(value = 0)
                                )
                            )
                        )
                    )
                }
            }
    }
    "Create a moduleinfo from a Path" {
        Path("src/test/resources/ten.ff")
            .moduleInfo()
            .shouldBeRight()
            .apply {
                map {
                    it.name.shouldBe("ten")
                    it.errors.shouldBeEmpty()
                    it.abstractions.shouldBe(
                        listOf(
                            AbstractionNode(
                                name = "ten",
                                expression = ConstantNode(
                                    value = 10.toLong(),
                                    info = TypeSemanticInfo(type = it.typeSystem.get("Byte")),
                                    location = Location(
                                        context = "ten.ff", position = Line(value = 1) to Offset(value = 0)
                                    )
                                ),
                                info = AbstractionSemanticInfo(
                                    visibility = FunctionVisibility.Internal,
                                    parameters = listOf()
                                ),
                                location = Location(
                                    context = "ten.ff",
                                    position = Line(value = 1) to Offset(value = 0)
                                )
                            )
                        )
                    )
                }
            }
    }
})