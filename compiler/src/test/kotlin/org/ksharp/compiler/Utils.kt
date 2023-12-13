package org.ksharp.compiler

import org.ksharp.common.Either
import org.ksharp.common.Error
import org.ksharp.module.CodeModule
import org.ksharp.module.ModuleInfo
import org.ksharp.parser.ksharp.parseModule
import org.ksharp.semantics.nodes.toCodeModule
import java.io.Reader
import java.io.StringReader

fun String.moduleInfo(context: String, preludeModule: ModuleInfo): Either<List<Error>, CodeModule> =
    StringReader(this).moduleInfo(context, preludeModule)

fun Reader.moduleInfo(context: String, preludeModule: ModuleInfo): Either<List<Error>, CodeModule> =
    this.parseModule(context, true)
        .mapLeft {
            listOf(it.error)
        }.flatMap {
            it.toCodeModule(preludeModule) { _, _ -> null }.let { codeModule ->
                if (codeModule.errors.isEmpty())
                    Either.Right(codeModule)
                else Either.Left(codeModule.errors)
            }
        }
