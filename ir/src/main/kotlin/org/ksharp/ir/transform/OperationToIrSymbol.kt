package org.ksharp.ir.transform

import org.ksharp.common.Location
import org.ksharp.ir.IrExpression
import org.ksharp.module.prelude.types.NumericType
import org.ksharp.typesystem.attributes.Attribute

typealias BinaryOperationFactory = (attributes: Set<Attribute>, left: IrExpression, right: IrExpression, location: Location) -> IrExpression

fun binaryOperationFactory(factory: BinaryOperationFactory): CustomApplicationIrNode = { state ->
    val (attributes, symbols) = arguments.toIrSymbols(state)
    factory(
        attributes,
        symbols.first(),
        symbols.last(),
        location
    )
}

fun relationalOperationFactory(
    numericFactory: BinaryOperationFactory,
    objectFactory: BinaryOperationFactory
): CustomApplicationIrNode = { state ->
    val (attributes, symbols) = arguments.toIrSymbols(state)
    val first = symbols.first()
    val last = symbols.last()
    val firstType = arguments.first().inferredType
    val secondType = arguments.last().inferredType
    if (firstType == secondType && firstType is NumericType && secondType is NumericType) {
        numericFactory(
            attributes,
            first,
            last,
            location
        )
    } else {
        objectFactory(
            attributes,
            first,
            last,
            location
        )
    }
}
