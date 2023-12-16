package org.ksharp.lsp.mocks

import org.eclipse.lsp4j.MessageActionItem
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.ShowMessageRequestParams
import org.eclipse.lsp4j.services.LanguageClient
import java.util.concurrent.CompletableFuture

class LanguageClientMock(
    val events: MutableList<Any> = mutableListOf(),
) : LanguageClient {

    var diagnostics: PublishDiagnosticsParams? = null
        private set

    override fun telemetryEvent(`object`: Any?) {
        TODO("Not yet implemented")
    }

    override fun publishDiagnostics(diagnostics: PublishDiagnosticsParams?) {
        this.diagnostics = diagnostics
    }

    override fun showMessage(messageParams: MessageParams?) {
        TODO("Not yet implemented")
    }

    override fun showMessageRequest(requestParams: ShowMessageRequestParams?): CompletableFuture<MessageActionItem> {
        TODO("Not yet implemented")
    }

    override fun logMessage(message: MessageParams) {
        events.add(message)
    }
}
