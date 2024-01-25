package org.ksharp.ir

import org.ksharp.module.prelude.preludeModule
import org.ksharp.typesystem.types.Type
import java.math.BigDecimal
import java.math.BigInteger

val byteType = preludeModule.typeSystem["Int"].valueOrNull!!
val shortType = preludeModule.typeSystem["Short"].valueOrNull!!
val intType = preludeModule.typeSystem["Int"].valueOrNull!!
val longType = preludeModule.typeSystem["Long"].valueOrNull!!
val bigIntType = preludeModule.typeSystem["BigInt"].valueOrNull!!
val floatType = preludeModule.typeSystem["Float"].valueOrNull!!
val doubleType = preludeModule.typeSystem["Double"].valueOrNull!!
val bigDecimalType = preludeModule.typeSystem["BigDecimal"].valueOrNull!!
val charType = preludeModule.typeSystem["Char"].valueOrNull!!
val stringType = preludeModule.typeSystem["String"].valueOrNull!!
val boolType = preludeModule.typeSystem["Bool"].valueOrNull!!

data class KValue(
    val type: Type,
    val value: Any
) {
    companion object {
        @JvmStatic
        fun value(value: Any): Any =
            if (value is KValue) {
                value.value
            } else {
                value
            }

        @JvmStatic
        fun type(value: Any): Type? =
            when (value) {
                is KValue -> value.type
                is Byte -> byteType
                is Short -> shortType
                is Int -> intType
                is Long -> longType
                is BigInteger -> bigIntType
                is Float -> floatType
                is Double -> doubleType
                is BigDecimal -> bigDecimalType
                is Char -> charType
                is String -> stringType
                is Boolean -> boolType
                else -> null
            }

        @JvmStatic
        fun wrap(value: Any, type: Type): KValue =
            if (value is KValue) KValue(type, value.value)
            else KValue(type, value)
    }
}
