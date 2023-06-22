package org.ksharp.ir.transform

import org.ksharp.ir.Expression
import org.ksharp.typesystem.attributes.Attribute

private fun shouldAddAttribute(expression: Expression, attributes: Set<Attribute>, attribute: Attribute) =
    expression.attributes.contains(attribute) && !attributes.contains(attribute)

fun Expression.addExpressionAttributes(
    currentAttributes: Set<Attribute>,
    vararg attributes: Attribute
): Set<Attribute> =
    attributes
        .asSequence()
        .filter { shouldAddAttribute(this, currentAttributes, it) }
        .toSet()
        .let {
            if (it.isNotEmpty()) currentAttributes + it
            else currentAttributes
        }
