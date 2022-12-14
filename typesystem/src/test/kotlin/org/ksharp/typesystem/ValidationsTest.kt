package org.ksharp.typesystem

import io.kotest.core.spec.style.StringSpec
import ksharp.test.shouldBeLeft
import ksharp.test.shouldBeRight
import org.ksharp.common.new

class ValidationsTest : StringSpec({
    "Test valid type names" {
        validateTypeName("Int").shouldBeRight("Int")
        validateTypeName("Int10").shouldBeRight("Int10")
        validateTypeName("Int_10").shouldBeRight("Int_10")
    }

    "Type name shouldn't start lowercase" {
        validateTypeName("int")
            .shouldBeLeft(TypeSystemErrorCode.TypeNameShouldStartWithUpperCase.new("name" to "int"))
    }

    "Type name shouldn't use invalid characters" {
        validateTypeName("Int-10")
            .shouldBeLeft(TypeSystemErrorCode.InvalidName.new("name" to "Int-10"))
    }

    "Test valid type param names" {
        validateTypeParamName("int").shouldBeRight("int")
        validateTypeParamName("int10").shouldBeRight("int10")
        validateTypeParamName("int_10").shouldBeRight("int_10")
    }

    "Type param name shouldn't start uppercase" {
        validateTypeParamName("Int")
            .shouldBeLeft(TypeSystemErrorCode.TypeParamNameShouldStartWithLowerCase.new("name" to "Int"))
    }

    "Type param name shouldn't use invalid characters" {
        validateTypeParamName("int-10")
            .shouldBeLeft(TypeSystemErrorCode.InvalidName.new("name" to "int-10"))
    }

    "Function names shouldn't contains spaces" {
        validateFunctionName("++ sum")
            .shouldBeLeft(TypeSystemErrorCode.FunctionNameShouldntHaveSpaces.new("name" to "++ sum"))
    }

    "Valid function names" {
        sequenceOf("++", "sum", "(+)")
            .forEach {
                validateFunctionName(it)
                    .shouldBeRight(it)
            }
    }

})