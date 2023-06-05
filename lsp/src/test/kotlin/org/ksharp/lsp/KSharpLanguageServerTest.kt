package org.ksharp.lsp

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.eclipse.lsp4j.*
import org.ksharp.lsp.capabilities.semantic_tokens.kSharpSemanticTokensProvider
import org.ksharp.lsp.client.ClientLogger
import org.ksharp.lsp.mocks.LanguageClientMock

class KSharpLanguageServerTest : StringSpec({
    "Default initialize -  shutdown" {
        KSharpLanguageServer()
            .apply {
                initialize(InitializeParams())
                    .get().shouldBe(
                        InitializeResult().apply {
                            capabilities = ServerCapabilities().apply {
                                positionEncoding = "utf-16"
                                setTextDocumentSync(TextDocumentSyncOptions().apply {
                                    openClose = true
                                    change = TextDocumentSyncKind.Incremental
                                })
                                semanticTokensProvider = kSharpSemanticTokensProvider
                            }
                        })
                shutdown()
                    .get()
                    .shouldBe(true)
            }
    }
    "Client logging" {
        val client = LanguageClientMock()
        KSharpLanguageServer()
            .connect(client)
        ClientLogger.info("Hello World")
        client.events.shouldBe(
            listOf(
                MessageParams(MessageType.Info, "Hello World")
            )
        )
    }
})
