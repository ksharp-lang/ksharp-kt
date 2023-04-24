package org.ksharp.typesystem.unification

import org.ksharp.common.*
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.types.ParametricType
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.types.TypeVariable

class ParametricUnification : CompoundUnification<ParametricType>() {

    override val Type.isSameTypeClass: Boolean
        get() = this is ParametricType

    override fun compoundUnify(
        location: Location,
        typeSystem: TypeSystem,
        type1: ParametricType,
        type2: ParametricType
    ): ErrorOrType =
        if (type1.params.size != type2.params.size) incompatibleType(location, type1, type2)
        else {
            val type = if (type1.type == type2.type) {
                Either.Right(type1.type)
            } else incompatibleType(location, type1, type2)
            type.flatMap {
                val type1Params = type1.params.iterator()
                val type2Params = type2.params.iterator()
                val params = listBuilder<Type>()
                var result: ErrorOrType? = null
                while (type1Params.hasNext() && type2Params.hasNext()) {
                    val item1 = type1Params.next()
                    val item2 = type2Params.next()
                    val unifyItem = typeSystem.unify(location, item1, item2)
                    if (unifyItem.isLeft) {
                        result = incompatibleType(location, type1, type2)
                        break
                    }
                    params.add(unifyItem.cast<Either.Right<Type>>().value)
                }
                result ?: Either.Right(
                    ParametricType(
                        it as TypeVariable,
                        params.build()
                    )
                )
            }
        }
}