package org.ksharp.ir

import org.ksharp.common.Either
import org.ksharp.module.prelude.preludeModule
import org.ksharp.parser.ksharp.parseModule
import org.ksharp.semantics.nodes.toSemanticModuleInfo
import org.ksharp.test.shouldBeRight

fun String.toSemanticModuleInfo() =
    this.parseModule("irTest.ks", true)
        .flatMap {
            val moduleInfo = it.toSemanticModuleInfo(preludeModule)
            if (moduleInfo.errors.isNotEmpty()) {
                Either.Left(moduleInfo.errors)
            } else Either.Right(moduleInfo)
        }.shouldBeRight()
        .value
