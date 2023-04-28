package org.ksharp.typesystem.substitution

import org.ksharp.common.*
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.TypeSystemErrorCode
import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.unification.unify

class SubstitutionContext(
    val typeSystem: TypeSystem
) {
    internal val mappings = mapBuilder<String, Type>()
    internal val errors = mapBuilder<String, Error>()
    fun addMapping(location: Location, paramName: String, type: Type): ErrorOrValue<Boolean> =
        mappings.get(paramName)?.let {
            val typeUnification = typeSystem.unify(location, it, type)
            if (typeUnification.isLeft) typeSystem.unify(location, type, it)
            else typeUnification
        }?.map {
            mappings.put(paramName, it)
            true
        }?.mapLeft {
            errors.put(paramName, it)
            it
        } ?: run {
            mappings.put(paramName, type)
            Either.Right(true)
        }

    fun getMapping(location: Location, paramName: String, type: Type): ErrorOrType =
        errors.get(paramName)
            ?.let {
                Either.Left(it)
            }
            ?: mappings.get(paramName)?.let {
                Either.Right(it)
            }
            ?: TypeSystemErrorCode.SubstitutionNotFound.new(
                location,
                "param" to paramName,
                "type" to type.representation
            ).let { Either.Left(it) }
}