package org.ksharp.typesystem.substitution

import org.ksharp.common.ErrorOrValue
import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.typesystem.ErrorOrType
import org.ksharp.typesystem.types.Type

interface SubstitutionAlgo<T : Type> {
    fun extract(context: SubstitutionContext, location: Location, type1: T, type2: Type): ErrorOrValue<Boolean>

    fun substitute(context: SubstitutionContext, location: Location, type: T, typeContext: Type): ErrorOrType
}

interface Substitution {
    val algo: SubstitutionAlgo<out Type>
}

enum class Substitutions(override val algo: SubstitutionAlgo<out Type>) : Substitution {
    Alias(AliasSubstitution()),
    Annotated(AnnotatedSubstitution()),
    Identity(IdentitySubstitution()),
    Labeled(LabeledSubstitution()),
    Parameter(ParameterSubstitution()),
    NoDefined(object : SubstitutionAlgo<Type> {
        override fun extract(
            context: SubstitutionContext,
            location: Location,
            type1: Type,
            type2: Type
        ): ErrorOrValue<Boolean> {
            TODO("Not yet implemented")
        }

        override fun substitute(
            context: SubstitutionContext,
            location: Location,
            type: Type,
            typeContext: Type
        ): ErrorOrType {
            TODO("Not yet implemented")
        }
    })
}

fun SubstitutionContext.extract(location: Location, type1: Type, type2: Type): ErrorOrValue<Boolean> =
    type1.substitution.algo
        .cast<SubstitutionAlgo<Type>>()
        .extract(this, location, type1, type2)

fun SubstitutionContext.substitute(
    location: Location,
    type: Type,
    typeContext: Type
): ErrorOrType =
    type.substitution.algo
        .cast<SubstitutionAlgo<Type>>()
        .substitute(this, location, type, typeContext)