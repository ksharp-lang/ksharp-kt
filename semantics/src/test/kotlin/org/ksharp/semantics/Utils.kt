package org.ksharp.semantics

import org.ksharp.common.Either
import org.ksharp.common.Error
import org.ksharp.common.ErrorOrValue
import org.ksharp.module.prelude.preludeModule
import org.ksharp.parser.ksharp.parseModule
import org.ksharp.semantics.nodes.ModuleInfoLoader
import org.ksharp.semantics.nodes.SemanticModuleInfo
import org.ksharp.semantics.nodes.toSemanticModuleInfo
import org.ksharp.semantics.nodes.toSemanticModuleInterface
import org.ksharp.typesystem.TypeSystem
import org.ksharp.typesystem.solver.solve

fun String.toSemanticModuleInfo(loader: ModuleInfoLoader = ModuleInfoLoader { _, _ -> null }): Either<List<Error>, SemanticModuleInfo> =
    this.parseModule("irTest", false)
        .flatMap {
            if (it.errors.isNotEmpty()) {
                return@flatMap Either.Left(it.errors)
            }
            val moduleInfo = it.toSemanticModuleInterface(preludeModule, loader).toSemanticModuleInfo(emptyList())
            if (moduleInfo.errors.isNotEmpty()) {
                Either.Left(moduleInfo.errors)
            } else Either.Right(moduleInfo)
        }

fun String.getSemanticModuleInfo(loader: ModuleInfoLoader = ModuleInfoLoader { _, _ -> null }): ErrorOrValue<SemanticModuleInfo> =
    this.parseModule("irTest.ks", false)
        .flatMap {
            Either.Right(it.toSemanticModuleInterface(preludeModule, loader).toSemanticModuleInfo(emptyList()))
        }


fun TypeSystem.solve(name: String) =
    this[name].valueOrNull!!.solve().valueOrNull!!
