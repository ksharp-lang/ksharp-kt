@file:JvmName("KsLspMain")

package org.ksharp.lsp

import org.eclipse.lsp4j.launch.LSPLauncher
import org.ksharp.common.annotation.KoverIgnore

@KoverIgnore(reason = "Main function is not tested")
fun main() {
    val lsp = KSharpLanguageServer()
    val launcher = LSPLauncher.createServerLauncher(lsp, System.`in`, System.out)
    val client = launcher.remoteProxy
    lsp.connect(client)
    val listener = launcher.startListening()
    listener.get()
}
