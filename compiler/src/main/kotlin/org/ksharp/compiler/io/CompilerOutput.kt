package org.ksharp.compiler.io

fun interface CompilerOutput {

    fun write(path: String, content: String)
}
