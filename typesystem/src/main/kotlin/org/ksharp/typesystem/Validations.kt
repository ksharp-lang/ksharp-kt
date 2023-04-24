package org.ksharp.typesystem

import org.ksharp.common.*

enum class TypeSystemErrorCode(override val description: String) : ErrorCode {
    InvalidName("Name should contains [a-zA-Z0-9_]: {name}"),
    NoParametrizedType("Type '{type}' is not parametrized"),
    ParametricTypeWithoutParameters("Parametric Type '{type}' without parameters"),
    InvalidNumberOfParameters("Type '{type}' requires {number} parameters: '{configuredType}'"),
    TypeNotFound("Type '{type}' not found"),
    TypeAlreadyRegistered("Type '{type}' already registered"),
    TypeNameShouldStartWithUpperCase("Type name should start with a Uppercase letter: '{name}'"),
    TypeParamNameShouldStartWithLowerCase("Type param should start with a lowercase letter: '{name}'"),
    FunctionNameShouldntHaveSpaces("Function names shouldn't have spaces: '{name}'"),
    InvalidFunctionType("Functions should have at least one argument and a return type"),
    IntersectionTypeShouldBeTraits("Intersection type should be Traits: '{name}'"),
    IncompatibleTypes("Type {type1} is not compatible with {type2}")
}

private fun validateRestName(name: String): ErrorOrValue<String> {
    val right = Either.Right(name)
    return name.asSequence()
        .drop(1)
        .map { c ->
            right.takeIf { (c.isLetterOrDigit() || c == '_') } ?: Either.Left(
                TypeSystemErrorCode.InvalidName.new(
                    "name" to name
                )
            )
        }
        .dropWhile { it.isRight }
        .firstOrNull() ?: right
}

fun validateTypeName(name: String): ErrorOrValue<String> {
    if (!name.first()
            .isUpperCase()
    ) return Either.Left(TypeSystemErrorCode.TypeNameShouldStartWithUpperCase.new("name" to name))
    return validateRestName(name)
}

fun validateTypeParamName(name: String): ErrorOrValue<String> {
    if (!name.first()
            .isLowerCase()
    ) return Either.Left(TypeSystemErrorCode.TypeParamNameShouldStartWithLowerCase.new("name" to name))
    return validateRestName(name)
}

fun validateFunctionName(name: String): ErrorOrValue<String> {
    val right = Either.Right(name)
    return name.asSequence()
        .map { c ->
            right.takeIf { !c.isWhitespace() } ?: Either.Left(
                TypeSystemErrorCode.FunctionNameShouldntHaveSpaces.new(
                    "name" to name
                )
            )
        }
        .dropWhile { it.isRight }
        .firstOrNull() ?: right
}