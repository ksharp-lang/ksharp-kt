package org.ksharp.ir

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.Line
import org.ksharp.common.Location
import org.ksharp.common.Offset
import org.ksharp.module.prelude.preludeModule
import org.ksharp.typesystem.attributes.CommonAttribute
import org.ksharp.typesystem.attributes.NoAttributes
import org.ksharp.typesystem.types.toFunctionType

class ModuleTest : StringSpec({
    val longType = preludeModule.typeSystem["Long"].valueOrNull!!
    val unitType = preludeModule.typeSystem["Unit"].valueOrNull!!
    "IrModule " {
        "ten = 10"
            .toSemanticModuleInfo()
            .toIrModule()
            .first
            .shouldBe(
                IrModule(
                    listOf(),
                    listOf(
                        IrFunction(
                            setOf(CommonAttribute.Internal, CommonAttribute.Constant),
                            "ten",
                            listOf(),
                            0,
                            listOf(unitType, longType).toFunctionType(NoAttributes),
                            IrInteger(
                                10,
                                Location(Line(1) to Offset(6), Line(1) to Offset(8))
                            ),
                            Location(Line(1) to Offset(0), Line(1) to Offset(3))
                        )
                    )
                )
            )
    }
})
