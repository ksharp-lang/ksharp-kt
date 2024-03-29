package org.ksharp.lsp

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.eclipse.lsp4j.*
import org.ksharp.lsp.client.Client
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
                                semanticTokensProvider = SemanticTokensWithRegistrationOptions().apply {
                                    legend = SemanticTokensLegend().apply {
                                        tokenTypes = listOf(
                                            SemanticTokenTypes.Type,
                                            SemanticTokenTypes.String,
                                            SemanticTokenTypes.Function,
                                            SemanticTokenTypes.Variable,
                                            SemanticTokenTypes.Operator,
                                            SemanticTokenTypes.Number,
                                            SemanticTokenTypes.Keyword,
                                            SemanticTokenTypes.Namespace,
                                            SemanticTokenTypes.Method,
                                            SemanticTokenTypes.TypeParameter,
                                            SemanticTokenTypes.Parameter,
                                            SemanticTokenTypes.Comment,
                                            SemanticTokenTypes.Decorator
                                        )
                                        tokenModifiers = listOf(
                                            SemanticTokenModifiers.Declaration
                                        )
                                    }
                                    setFull(true)
                                    setRange(false)
                                }
                                setHoverProvider(HoverOptions())
                            }
                        })
                shutdown()
                    .get()
                    .shouldBe(true)
            }
    }
    "Client logging" {
        Client.reset()
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
    "Trace method" {
        Client.reset()
        val client = LanguageClientMock()
        val server = KSharpLanguageServer()
        server.connect(client)
        server.setTrace(SetTraceParams(TraceValue.Off))
        client.events.shouldBe(
            listOf(
                MessageParams(MessageType.Info, "Set trace SetTraceParams [\n  value = \"off\"\n].")
            )
        )
    }
})
