package org.ksharp.lsp.capabilities.semantic_tokens

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.eclipse.lsp4j.*
import org.ksharp.lsp.actions.ActionExecutionState
import org.ksharp.lsp.actions.ParseAction
import org.ksharp.lsp.actions.PublishSemanticErrorsAction
import org.ksharp.lsp.actions.documentActions
import org.ksharp.lsp.client.Client
import org.ksharp.lsp.mocks.LanguageClientMock

class DiagnosticsTest : StringSpec({
    "Diagnostic messages" {
        Client.reset()
        val client = LanguageClientMock()
        Client.initialize(client)
        val actions = documentActions("doc")
        val state = ActionExecutionState()
        actions(state, ParseAction, "a + 10")
        state[PublishSemanticErrorsAction].get().shouldBe(true)
        client.diagnostics.shouldBe(PublishDiagnosticsParams().apply {
            this.uri = "doc"
            this.diagnostics = listOf(
                Diagnostic().apply {
                    this.range = Range().apply {
                        start = Position().apply {
                            line = 0
                            character = 1
                        }
                        end = Position().apply {
                            line = 0
                            character = 2
                        }
                    }
                    this.severity = DiagnosticSeverity.Error
                    this.source = "k# semantic analysis"
                    this.message =
                        "ExpectingToken: Expecting token <LowerCaseWord, Operator different internal, type> was Operator10:+"
                }
            )
        })
    }
})
