package org.ksharp.ir

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ksharp.common.Line
import org.ksharp.common.Location
import org.ksharp.common.Offset
import org.ksharp.typesystem.attributes.CommonAttribute

class ModuleTest : StringSpec({
    "IrModule " {
        "ten = 10"
            .toSemanticModuleInfo()
            .apply {
                abstractions
                    .toIrModule()
                    .first
                    .shouldBe(
                        IrModule(
                            listOf(
                                IrFunction(
                                    setOf(CommonAttribute.Internal, CommonAttribute.Constant),
                                    "ten/0",
                                    listOf(),
                                    0,
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
    }
})
