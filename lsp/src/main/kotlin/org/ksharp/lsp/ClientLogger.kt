package org.ksharp.lsp

import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.MessageType
import org.eclipse.lsp4j.services.LanguageClient

object ClientLogger {
    private lateinit var client: LanguageClient

    var initialized = false
        private set


    fun initialize(client: LanguageClient) {
        if (!initialized) {
            initialized = true
            this.client = client
        }
    }

    fun info(message: String) {
        if (initialized) {
            client.logMessage(MessageParams(MessageType.Info, message))
        }
    }
}
