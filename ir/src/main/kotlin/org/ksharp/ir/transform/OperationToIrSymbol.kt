package org.ksharp.ir.transform

import org.ksharp.common.Location
import org.ksharp.common.cast
import org.ksharp.ir.IrComparable
import org.ksharp.ir.IrExpression
import org.ksharp.ir.IrModuleCall
import org.ksharp.module.Impl
import org.ksharp.module.prelude.types.NumericType
import org.ksharp.typesystem.attributes.Attribute
import org.ksharp.typesystem.types.ImplType
import org.ksharp.typesystem.types.toFunctionType

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
    vararg expected: String
): CustomApplicationIrNode = { state ->
    val (attributes, symbols) = arguments.toIrSymbols(state)
    val first = symbols.first()
    val last = symbols.last()
    val firstType = arguments.first().inferredType.cast<ImplType>()
    val secondType = arguments.last().inferredType.cast<ImplType>()
    if (firstType == secondType && firstType.impl is NumericType && secondType.impl is NumericType) {
        numericFactory(
            attributes,
            first,
            last,
            location
        )
    } else {
        val (argsAttributes, arguments) = arguments.toIrSymbols(state)
        val impl = state.module.impls.first { it == Impl("", firstType.trait.name, firstType.impl) }
        val module = impl.module
        val returnType = info.getInferredType(location).valueOrNull!!
        val functionType = listOf(firstType, secondType, returnType).toFunctionType(state.module.typeSystem, attributes)
        val moduleCall = IrModuleCall(
            argsAttributes,
            module,
            functionName,
            arguments,
            functionType,
            location
        )
        IrComparable(
            moduleCall,
            listOf(*expected)
        )
    }
}

fun equalsOperationFactory(
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
