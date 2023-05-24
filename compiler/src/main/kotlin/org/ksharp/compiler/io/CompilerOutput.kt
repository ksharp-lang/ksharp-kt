package org.ksharp.compiler.io

interface CompilerOutput {

    fun write(path: String, content: String)
}