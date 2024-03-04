package org.ksharp.semantics.inference

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.shouldBe
import org.ksharp.common.Either
import org.ksharp.common.Location
import org.ksharp.common.MockHandlePromise
import org.ksharp.common.cast
import org.ksharp.module.prelude.preludeModule
import org.ksharp.nodes.semantic.*
import org.ksharp.semantics.toSemanticModuleInfo
import org.ksharp.test.shouldBeRight
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.types.*

class InferenceLambdaTest : StringSpec({
    val typeSystem = preludeModule.typeSystem
    "Inference unit lambda" {
        """
            tenFn = \-> 10
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeRight()
            .map {
                val longType = typeSystem["Long"]
                it.abstractions
                    .apply {
                        size.shouldBe(1)
                    }
                    .first()
                    .shouldBe(
                        AbstractionNode(
                            attributes = setOf(CommonAttribute.Internal),
                            name = "tenFn",
                            expression = AbstractionLambdaNode(
                                expression = ConstantNode(
                                    value = 10.toLong(), info = TypeSemanticInfo(longType),
                                    location = Location.NoProvided
                                ),
                                info = AbstractionSemanticInfo(
                                    emptyList(),
                                    returnType = TypeSemanticInfo(Either.Right(newParameterForTesting(1)))
                                ),
                                location = Location.NoProvided
                            ),
                            info = AbstractionSemanticInfo(
                                emptyList(),
                                returnType = TypeSemanticInfo(Either.Right(newParameterForTesting(0)))
                            ),
                            location = Location.NoProvided
                        )
                    )
            }
    }
    "Inference lambda with arguments" {
        """
            doubleFn = \a -> a * 2
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeRight()
            .map {
                val longType = typeSystem["Long"]
                val longTypeImpl = ImplType(typeSystem["Num"].valueOrNull!!.cast(), longType.valueOrNull!!)
                it.abstractions
                    .apply {
                        size.shouldBe(1)
                    }
                    .first()
                    .shouldBe(
                        AbstractionNode(
                            attributes = setOf(CommonAttribute.Internal),
                            name = "doubleFn",
                            expression = AbstractionLambdaNode(
                                expression = ApplicationNode(
                                    functionName = ApplicationName(pck = null, name = "(*)"),
                                    arguments = listOf(
                                        VarNode(
                                            name = "a", info = Symbol(
                                                name = "a", type = TypeSemanticInfo(
                                                    type = Either.Right(
                                                        newParameterForTesting(1)
                                                    )
                                                )
                                            ), location = Location.NoProvided
                                        ),
                                        ConstantNode(
                                            value = 2.toLong(), info = TypeSemanticInfo(type = longType),
                                            location = Location.NoProvided
                                        )
                                    ),
                                    info = ApplicationSemanticInfo(
                                        function =
                                        listOf(longTypeImpl, longTypeImpl, longTypeImpl)
                                            .toFunctionType(
                                                MockHandlePromise(),
                                                setOf(CommonAttribute.TraitMethod)
                                            )
                                    ), location = Location.NoProvided
                                ),
                                info = AbstractionSemanticInfo(
                                    listOf(
                                        Symbol(
                                            name = "a",
                                            type = TypeSemanticInfo(Either.Right(newParameterForTesting(1)))
                                        )
                                    ),
                                    returnType = TypeSemanticInfo(Either.Right(newParameterForTesting(2)))
                                ),
                                location = Location.NoProvided
                            ),
                            info = AbstractionSemanticInfo(
                                emptyList(),
                                returnType = TypeSemanticInfo(Either.Right(newParameterForTesting(0)))
                            ),
                            location = Location.NoProvided
                        )
                    )
            }
    }
    "Inference closure unit lambda" {
        """          
            doubleFn a = \-> a * 2
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeRight()
            .map {
                val longType = typeSystem["Long"]
                val longTypeImpl = ImplType(typeSystem["Num"].valueOrNull!!.cast(), longType.valueOrNull!!)
                it.abstractions
                    .apply {
                        size.shouldBe(1)
                    }
                    .first()
                    .shouldBe(
                        AbstractionNode(
                            attributes = setOf(CommonAttribute.Internal),
                            name = "doubleFn",
                            expression = AbstractionLambdaNode(
                                expression = ApplicationNode(
                                    functionName = ApplicationName(pck = null, name = "(*)"),
                                    arguments = listOf(
                                        VarNode(
                                            name = "a",
                                            info = Symbol(
                                                name = "a", type = TypeSemanticInfo(
                                                    type = Either.Right(
                                                        newParameterForTesting(0)
                                                    )
                                                )
                                            ),
                                            location = Location.NoProvided
                                        ),
                                        ConstantNode(
                                            value = 2.toLong(), info = TypeSemanticInfo(longType),
                                            location = Location.NoProvided
                                        )
                                    ),
                                    info = ApplicationSemanticInfo(
                                        function = listOf(longTypeImpl, longTypeImpl, longTypeImpl)
                                            .toFunctionType(
                                                MockHandlePromise(),
                                                setOf(CommonAttribute.TraitMethod)
                                            )
                                    ), location = Location.NoProvided
                                ),
                                info = AbstractionSemanticInfo(
                                    _parameters = emptyList(),
                                    returnType = TypeSemanticInfo(type = Either.Right(newParameterForTesting(2)))
                                ),
                                location = Location.NoProvided
                            ),
                            info = AbstractionSemanticInfo(
                                _parameters = listOf(
                                    Symbol(
                                        name = "a",
                                        type = TypeSemanticInfo(type = Either.Right(newParameterForTesting(0)))
                                    )
                                ),
                                returnType = TypeSemanticInfo(type = Either.Right(newParameterForTesting(1)))
                            ),
                            location = Location.NoProvided
                        )
                    )
            }
    }
    "Inference closure lambda with arguments" {
        """
            doubleFn b = \a -> (a + b) * 2
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeRight()
            .map {
                val longType = typeSystem["Long"]
                val numType = ImplType(
                    typeSystem["Num"].valueOrNull!!.cast(),
                    FixedTraitType(typeSystem["Num"].valueOrNull!!.cast())
                )
                val parametricNumType = numType.trait.toParametricType()
                it.abstractions
                    .apply {
                        size.shouldBe(1)
                    }
                    .first()
                    .shouldBe(
                        AbstractionNode(
                            attributes = setOf(CommonAttribute.Internal),
                            name = "doubleFn",
                            expression = AbstractionLambdaNode(
                                expression =
                                ApplicationNode(
                                    functionName = ApplicationName(pck = null, name = "(*)"),
                                    arguments = listOf(
                                        ApplicationNode(
                                            functionName = ApplicationName(pck = null, name = "(+)"),
                                            arguments = listOf(
                                                VarNode(
                                                    name = "a", info = Symbol(
                                                        name = "a", type = TypeSemanticInfo(
                                                            type = Either.Right(
                                                                newParameterForTesting(2)
                                                            )
                                                        )
                                                    ), location = Location.NoProvided
                                                ),
                                                VarNode(
                                                    name = "b",
                                                    info = Symbol(
                                                        name = "b", type = TypeSemanticInfo(
                                                            type = Either.Right(
                                                                newParameterForTesting(0)
                                                            )
                                                        )
                                                    ), location = Location.NoProvided
                                                )
                                            ),
                                            info = ApplicationSemanticInfo(
                                                function =
                                                listOf(parametricNumType, parametricNumType, parametricNumType)
                                                    .toFunctionType(
                                                        MockHandlePromise(),
                                                        setOf(CommonAttribute.TraitMethod)
                                                    )
                                            ),
                                            location = Location.NoProvided
                                        ),
                                        ConstantNode(
                                            value = 2.toLong(),
                                            info = TypeSemanticInfo(type = longType),
                                            location = Location.NoProvided
                                        )
                                    ),
                                    info = ApplicationSemanticInfo(
                                        function = listOf(numType, numType, numType)
                                            .toFunctionType(
                                                MockHandlePromise(),
                                                setOf(CommonAttribute.TraitMethod)
                                            )
                                    ),
                                    location = Location.NoProvided
                                ),
                                info = AbstractionSemanticInfo(
                                    _parameters = listOf(
                                        Symbol(
                                            name = "a", type = TypeSemanticInfo(
                                                type = Either.Right(
                                                    newParameterForTesting(2)
                                                )
                                            )
                                        )
                                    ), returnType = TypeSemanticInfo(type = Either.Right(newParameterForTesting(3)))
                                ),
                                location = Location.NoProvided
                            ),
                            info = AbstractionSemanticInfo(
                                _parameters = listOf(
                                    Symbol(
                                        name = "b", type = TypeSemanticInfo(
                                            type = Either.Right(
                                                newParameterForTesting(0)
                                            )
                                        )
                                    )
                                ), returnType = TypeSemanticInfo(type = Either.Right(newParameterForTesting(1)))
                            ),
                            location = Location.NoProvided
                        )
                    )
            }
    }
    "Inference pass lambda as high order function" {
        """
            highOrderFn f = f 10
            doubleFn = \a -> a * 2
            fn = highOrderFn doubleFn
        """.trimIndent()
            .toSemanticModuleInfo()
            .shouldBeRight()
            .map {
                val longType = typeSystem["Long"]
                val longTypeImpl = ImplType(typeSystem["Num"].valueOrNull!!.cast(), longType.valueOrNull!!)
                it.abstractions
                    .shouldBe(
                        listOf(
                            AbstractionNode(
                                attributes = setOf(CommonAttribute.Internal),
                                name = "highOrderFn",
                                expression = ApplicationNode(
                                    functionName = ApplicationName(pck = null, name = "f"),
                                    arguments = listOf(
                                        ConstantNode(
                                            value = 10.toLong(),
                                            info = TypeSemanticInfo(type = longType),
                                            location = Location.NoProvided
                                        )
                                    ),
                                    info = ApplicationSemanticInfo(
                                        function = listOf(longType.valueOrNull!!, newParameterForTesting(7))
                                            .toFunctionType(MockHandlePromise(), NoAttributes),
                                        functionSymbol = Symbol(
                                            name = "f", type = TypeSemanticInfo(
                                                type = Either.Right(
                                                    newParameterForTesting(0)
                                                )
                                            )
                                        )
                                    ), location = Location.NoProvided
                                ),
                                info = AbstractionSemanticInfo(
                                    _parameters = listOf(
                                        Symbol(
                                            name = "f", type = TypeSemanticInfo(
                                                type = Either.Right(
                                                    newParameterForTesting(0)
                                                )
                                            )
                                        )
                                    ), returnType = TypeSemanticInfo(type = Either.Right(newParameterForTesting(1)))
                                ),
                                location = Location.NoProvided
                            ),

                            AbstractionNode(
                                attributes = setOf(CommonAttribute.Internal),
                                name = "doubleFn",
                                expression = AbstractionLambdaNode(
                                    expression = ApplicationNode(
                                        functionName = ApplicationName(pck = null, name = "(*)"),
                                        arguments = listOf(
                                            VarNode(
                                                name = "a",
                                                info = Symbol(
                                                    name = "a", type = TypeSemanticInfo(
                                                        type = Either.Right(
                                                            newParameterForTesting(4)
                                                        )
                                                    )
                                                ), location = Location.NoProvided
                                            ),
                                            ConstantNode(
                                                value = 2.toLong(),
                                                info = TypeSemanticInfo(type = longType),
                                                location = Location.NoProvided
                                            )
                                        ),
                                        info = ApplicationSemanticInfo(
                                            function = listOf(longTypeImpl, longTypeImpl, longTypeImpl)
                                                .toFunctionType(
                                                    MockHandlePromise(),
                                                    setOf(CommonAttribute.TraitMethod)
                                                ), functionSymbol = null
                                        ),
                                        location = Location.NoProvided
                                    ),
                                    info = AbstractionSemanticInfo(
                                        _parameters = listOf(
                                            Symbol(
                                                name = "a", type = TypeSemanticInfo(
                                                    type = Either.Right(
                                                        newParameterForTesting(4)
                                                    )
                                                )
                                            )
                                        ),
                                        returnType = TypeSemanticInfo(type = Either.Right(newParameterForTesting(5)))
                                    ),
                                    location = Location.NoProvided
                                ),
                                info = AbstractionSemanticInfo(
                                    _parameters = emptyList(), returnType = TypeSemanticInfo(
                                        type = Either.Right(
                                            newParameterForTesting(2)
                                        )
                                    )
                                ), location = Location.NoProvided
                            ),
                            AbstractionNode(
                                attributes = setOf(CommonAttribute.Internal),
                                name = "fn",
                                expression = ApplicationNode(
                                    functionName = ApplicationName(pck = null, name = "highOrderFn"),
                                    arguments = listOf(
                                        VarNode(
                                            name = "doubleFn", info = TypeSemanticInfo(
                                                type = Either.Right(
                                                    newParameterForTesting(6)
                                                )
                                            ),
                                            location = Location.NoProvided
                                        )
                                    ),
                                    info = ApplicationSemanticInfo(
                                        function = listOf(
                                            listOf(longType.valueOrNull!!, newParameterForTesting(7))
                                                .toFunctionType(
                                                    MockHandlePromise(),
                                                    NoAttributes
                                                ),
                                            newParameterForTesting(7)
                                        ).toFunctionType(MockHandlePromise(), setOf(CommonAttribute.Internal)),
                                        functionSymbol = null
                                    ),
                                    location = Location.NoProvided
                                ), info = AbstractionSemanticInfo(
                                    _parameters = emptyList(),
                                    returnType = TypeSemanticInfo(type = Either.Right(newParameterForTesting(3)))
                                ), location = Location.NoProvided
                            )
                        )
                    )
            }
    }
}) {
    override suspend fun beforeAny(testCase: TestCase) {
        super.beforeAny(testCase)
        resetParameterCounterForTesting()
    }
}
