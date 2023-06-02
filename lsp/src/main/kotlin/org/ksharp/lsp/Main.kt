@file:JvmName("KsLspMain")

package org.ksharp.lsp

import org.eclipse.lsp4j.launch.LSPLauncher


fun main(args: Array<String>) {
    val lsp = KSharpLanguageServer()
    val launcher = LSPLauncher.createServerLauncher(lsp, System.`in`, System.out)
    val client = launcher.remoteProxy
    lsp.connect(client)
    val listener = launcher.startListening()
    listener.get()
}
