package org.ksharp.semantics.expressions

import org.ksharp.common.cast
import org.ksharp.nodes.AnnotationNode
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.attributes.CommonAttribute

private fun List<AnnotationNode>?.nameAttribute(): Attribute? =
    if (this != null) asSequence().filter {
        it.name == "name"
    }.map {
        val name: String = it.attrs["default"]?.cast() ?: return@map null
        val targetLanguages: Set<String> = it.attrs["for"]?.let { targetLanguage ->
            when (targetLanguage) {
                is List<*> -> {
                    targetLanguage.asSequence().filterIsInstance<String>().toSet()
                }

                is String -> setOf(targetLanguage)
                else -> setOf()
            }
        } ?: return@map null
        if (targetLanguages.isEmpty()) null
        else targetLanguages.map { targetLanguage ->
            targetLanguage to name
        }
    }.filterNotNull()
        .flatten()
        .associate { it }
        .let {
            if (it.isEmpty()) null
            else org.ksharp.typesystem.attributes.nameAttribute(it)
        }
    else null

private fun List<AnnotationNode>?.targetLanguageAttribute(): Attribute? =
    if (this != null) asSequence().filter {
        it.name == "if"
    }.map {
        val targetLanguages: Set<String> = it.attrs["default"]?.let { targetLanguage ->
            when (targetLanguage) {
                is List<*> -> {
                    targetLanguage.asSequence().filterIsInstance<String>().toSet()
                }

                is String -> setOf(targetLanguage)
                else -> setOf()
            }
        } ?: return@map null
        targetLanguages.ifEmpty { null }
    }.filterNotNull()
        .flatten()
        .toSet()
        .let {
            if (it.isEmpty()) null
            else org.ksharp.typesystem.attributes.targetLanguageAttribute(it)
        }
    else null

private fun AnnotationNode.toAttribute() =
    when (name) {
        "sideEffect" -> CommonAttribute.Impure
        "impure" -> CommonAttribute.Impure
        "pure" -> CommonAttribute.Pure
        "constant" -> CommonAttribute.Constant
        else -> null
    }

fun List<AnnotationNode>?.toAttributes(extraAttributes: Set<Attribute>): Set<Attribute> =
    if (this != null) {
        val result = mutableSetOf<Attribute>()
        result.addAll(extraAttributes)
        asSequence()
            .map { it.toAttribute() }
            .filterNotNull()
            .forEach {
                result.add(it)
            }
        nameAttribute()?.let { result.add(it) }
        targetLanguageAttribute()?.let { result.add(it) }
        result
    } else extraAttributes
