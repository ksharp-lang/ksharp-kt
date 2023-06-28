package org.ksharp.ir.transform

import org.ksharp.common.Location
import org.ksharp.ir.IrExpression
import org.ksharp.typesystem.attributes.Attribute

typealias BinaryOperationFactory = (attributes: Set<Attribute>, left: IrExpression, right: IrExpression, location: Location) -> IrExpression

fun binaryOperationFactory(factory: BinaryOperationFactory): CustomApplicationIrNode = {
    val (attributes, symbols) = arguments.toIrSymbols(it)
    factory(
        attributes,
        symbols.first(),
        symbols.last(),
        location
    )
}