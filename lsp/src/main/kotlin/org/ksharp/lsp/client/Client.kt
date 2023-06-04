package org.ksharp.lsp.client

import org.eclipse.lsp4j.services.LanguageClient

object Client {

    private lateinit var clientInstance: LanguageClient

    var initialized = false
        private set

    fun initialize(client: LanguageClient) {
        if (!initialized) {
            initialized = true
            this.clientInstance = client
        }
    }

    fun run(action: (client: LanguageClient) -> Unit) {
        if (initialized) {
            action(clientInstance)
        }
    }

}

fun withClient(action: (client: LanguageClient) -> Unit) {
    Client.run(action)
}
