package ksharp.nodes.prelude.types

import org.ksharp.typesystem.TypeSystemBuilder
import org.ksharp.typesystem.types.alias
import org.ksharp.typesystem.types.parametricType
import java.math.BigDecimal
import java.math.BigInteger

fun TypeSystemBuilder.interpretedTypeSystem() {
    parametricType("Num") {
        parameter("n")
    }
    alias("Byte") {
        parametricType("Num") {
            interpreted(Byte::class)
        }
    }
    alias("Short") {
        parametricType("Num") {
            interpreted(Short::class)
        }
    }
    alias("Int") {
        parametricType("Num") {
            interpreted(Int::class)
        }
    }
    alias("Long") {
        parametricType("Num") {
            interpreted(Long::class)
        }
    }
    alias("Float") {
        parametricType("Num") {
            interpreted(Float::class)
        }
    }
    alias("Double") {
        parametricType("Num") {
            interpreted(Double::class)
        }
    }
    alias("BigInt") {
        parametricType("Num") {
            interpreted(BigInteger::class)
        }
    }
    alias("BigDec") {
        parametricType("Num") {
            interpreted(BigDecimal::class)
        }
    }

    alias("String") { interpreted(String::class) }
    alias("Char") { interpreted(Char::class) }
}