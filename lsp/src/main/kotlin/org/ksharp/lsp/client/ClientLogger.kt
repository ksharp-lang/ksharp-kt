package org.ksharp.lsp.client

import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.MessageType

object ClientLogger {
    fun info(message: String) {
        withClient {
            it.logMessage(MessageParams(MessageType.Info, message))
        }
    }
}
