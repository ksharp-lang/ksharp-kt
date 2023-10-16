package org.ksharp.typesystem.unification

import org.ksharp.common.Location
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.incompatibleType
import org.ksharp.typesystem.types.TupleType
import org.ksharp.typesystem.types.Type

class TupleUnification : CompoundUnification<TupleType>() {

    override val Type.isSameTypeClass: Boolean
        get() = this is TupleType

    override fun compoundUnify(
        location: Location,
        type1: TupleType,
        type2: TupleType
    ): ErrorOrType =
        if (type1.elements.size != type2.elements.size) incompatibleType(location, type1, type2)
        else {
            unifyListOfTypes(location, type1, type2, type1.elements, type2.elements).map { params ->
                TupleType(type1.typeSystem, type1.attributes, params)
            }
        }
}
