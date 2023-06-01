package org.ksharp.lsp

import org.eclipse.lsp4j.launch.LSPLauncher

fun main() {
    val lsp = KSharpLanguageServer()
    val launcher = LSPLauncher.createServerLauncher(lsp, System.`in`, System.out)
    val listener = launcher.startListening()
    listener.get()
}
