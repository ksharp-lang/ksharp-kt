package org.ksharp.semantics

import org.ksharp.common.Either
import org.ksharp.common.Error
import org.ksharp.module.prelude.preludeModule
import org.ksharp.parser.ksharp.parseModule
import org.ksharp.semantics.nodes.SemanticModuleInfo
import org.ksharp.semantics.nodes.toSemanticModuleInfo

fun String.toSemanticModuleInfo(): Either<List<Error>, SemanticModuleInfo> =
    this.parseModule("irTest.ks", false)
        .flatMap {
            val moduleInfo = it.toSemanticModuleInfo(preludeModule)
            if (moduleInfo.errors.isNotEmpty()) {
                Either.Left(moduleInfo.errors)
            } else Either.Right(moduleInfo)
        }
