package org.ksharp.typesystem.types

import org.ksharp.typesystem.TypeFactoryBuilder
import org.ksharp.typesystem.TypeSystemBuilder
import org.ksharp.typesystem.annotations.Annotation

fun TypeSystemBuilder.alias(name: String, annotations: List<Annotation> = listOf(), factory: TypeFactoryBuilder) =
    item(name, annotations) {
        factory()
    }
