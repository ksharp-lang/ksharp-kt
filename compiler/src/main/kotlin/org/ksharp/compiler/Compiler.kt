package org.ksharp.compiler

import org.ksharp.common.Either
import org.ksharp.common.Error
import org.ksharp.parser.ksharp.parseModule
import org.ksharp.semantics.nodes.ModuleSemanticInfo
import org.ksharp.semantics.nodes.toModuleSemanticInfo
import java.io.File
import java.io.Reader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

fun Reader.moduleInfo(context: String): Either<List<Error>, ModuleSemanticInfo> =
    this.parseModule(context, true)
        .mapLeft {
            listOf(it.error)
        }.flatMap {
            val moduleInfo = it.toModuleSemanticInfo()
            if (moduleInfo.errors.isNotEmpty()) {
                Either.Left(moduleInfo.errors)
            } else Either.Right(moduleInfo)
        }

fun String.moduleInfo(context: String) =
    this.reader().moduleInfo(context)

fun Path.moduleInfo() =
    Files.newBufferedReader(this, StandardCharsets.UTF_8)
        .moduleInfo(fileName.toString())

fun File.moduleInfo() =
    reader(StandardCharsets.UTF_8).moduleInfo(name)