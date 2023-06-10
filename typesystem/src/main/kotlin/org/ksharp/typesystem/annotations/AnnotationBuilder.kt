package org.ksharp.typesystem.annotations

import org.ksharp.common.MapBuilder
import org.ksharp.common.annotation.Mutable
import org.ksharp.common.mapBuilder
import org.ksharp.common.put

@Mutable
class AnnotationBuilder(private var attrs: MapBuilder<String, Any>) {
    operator fun set(key: String, value: String) {
        attrs.put(key, value)
    }

    operator fun set(key: String, value: Annotation) {
        attrs.put(key, value)
    }

    operator fun set(key: String, value: Boolean) {
        attrs.put(key, value)
    }

    operator fun <T> set(key: String, value: List<T>) {
        attrs.put(key, value)
    }
}

fun annotation(name: String, block: AnnotationBuilder.() -> Unit = {}): Annotation {
    val builder = mapBuilder<String, Any>()
    AnnotationBuilder(builder).block()
    return Annotation(name, builder.build())
}
