package org.ksharp.module.prelude.unification

import org.ksharp.typesystem.types.Type
import org.ksharp.typesystem.unification.TypeUnification
import org.ksharp.typesystem.unification.UnificationAlgo

enum class TypeUnifications(override val algo: UnificationAlgo<out Type>) : TypeUnification {
    Numeric(NumericUnification()),
}