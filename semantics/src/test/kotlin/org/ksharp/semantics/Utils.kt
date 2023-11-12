package org.ksharp.semantics

import org.ksharp.common.Either
import org.ksharp.common.Error
import org.ksharp.common.ErrorOrValue
import org.ksharp.module.prelude.preludeModule
import org.ksharp.parser.ksharp.parseModule
import org.ksharp.semantics.nodes.SemanticModuleInfo
import org.ksharp.semantics.nodes.toSemanticModuleInfo
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.solver.solve

fun String.toSemanticModuleInfo(): Either<List<Error>, SemanticModuleInfo> =
    this.parseModule("irTest.ks", false)
        .flatMap {
            if (it.errors.isNotEmpty()) {
                return@flatMap Either.Left(it.errors)
            }
            val moduleInfo = it.toSemanticModuleInfo(preludeModule)
            if (moduleInfo.errors.isNotEmpty()) {
                Either.Left(moduleInfo.errors)
            } else Either.Right(moduleInfo)
        }

fun String.getSemanticModuleInfo(): ErrorOrValue<SemanticModuleInfo> =
    this.parseModule("irTest.ks", false)
        .flatMap {
            Either.Right(it.toSemanticModuleInfo(preludeModule))
        }


fun TypeSystem.solve(name: String) =
    this.solve(this[name].valueOrNull!!).valueOrNull!!
