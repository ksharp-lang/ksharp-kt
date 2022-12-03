package org.ksharp.typesystem.types

import org.ksharp.typesystem.TypeFactoryBuilder
import org.ksharp.typesystem.TypeSystemBuilder

fun TypeSystemBuilder.alias(name: String, factory: TypeFactoryBuilder) =
    item(name) {
        factory()
    }
