package org.ksharp.lsp

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.eclipse.lsp4j.*
import org.ksharp.lsp.model.DocumentStorage

class KSharpDocumentServiceTest : StringSpec({
    "Document lifecycle test" {
        val storage = DocumentStorage()
        val server = KSharpLanguageServer(storage)
        val service = server.textDocumentService
        service.didOpen(DidOpenTextDocumentParams().apply {
            this.textDocument = TextDocumentItem().apply {
                this.languageId = "ksharp"
                this.uri = "testDoc"
                this.text = ""
            }
        })
        service.didChange(DidChangeTextDocumentParams().apply {
            this.textDocument = VersionedTextDocumentIdentifier().apply {
                this.uri = "testDoc"
            }
            this.contentChanges = listOf(
                TextDocumentContentChangeEvent().apply {
                    this.text = "t"
                    this.range = Range().apply {
                        this.start = Position().apply {
                            this.line = 0
                            this.character = 0
                        }
                        this.end = Position().apply {
                            this.line = 0
                            this.character = 0
                        }
                    }
                }
            )
        })
        storage.content("testDoc").shouldBe("t")
        service.didClose(DidCloseTextDocumentParams().apply {
            this.textDocument = TextDocumentIdentifier().apply {
                uri = "testDoc"
            }
        })
        storage.content("testDoc").shouldBeNull()
    }
    "Test semantic tokens function" {
        val storage = DocumentStorage()
        val server = KSharpLanguageServer(storage)
        val service = server.textDocumentService
        service.didOpen(DidOpenTextDocumentParams().apply {
            this.textDocument = TextDocumentItem().apply {
                this.languageId = "ksharp"
                this.uri = "testDoc"
                this.text = "import math as m"
            }
        })
        service.semanticTokensFull(SemanticTokensParams().apply {
            this.textDocument = TextDocumentIdentifier().apply {
                this.uri = "testDoc"
            }
        }).get().shouldBe(SemanticTokens().apply {
            this.data = listOf(0, 0, 6, 6, 0, 0, 7, 4, 7, 0, 0, 5, 2, 6, 0, 0, 3, 1, 7, 0)
        })
    }
})
