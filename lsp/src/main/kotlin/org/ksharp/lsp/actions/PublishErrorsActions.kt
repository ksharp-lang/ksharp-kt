package org.ksharp.lsp.actions

import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.Range
import org.ksharp.common.Error
import org.ksharp.common.Location
import org.ksharp.lsp.client.withClient

const val PublishErrorsAction = "PublishErrorsAction"

private val org.ksharp.common.Position.lspPosition: org.eclipse.lsp4j.Position
    get() =
        org.eclipse.lsp4j.Position().apply {
            this.line = (first.value - 1).coerceAtLeast(0)
            this.character = (second.value - 1).coerceAtLeast(0)
        }

private val Location.lspRange: Range
    get() =
        Range().apply {
            this.start = this@lspRange.start.lspPosition
            this.end = this@lspRange.end.lspPosition
        }

fun publishSemanticErrorsAction(uri: String) = action<List<Error>, Boolean>(PublishErrorsAction, false) {
    execution { _, errors ->
        var result = false
        withClient { client ->
            client.publishDiagnostics(PublishDiagnosticsParams().apply {
                this.uri = uri
                this.diagnostics = errors.map {
                    Diagnostic().apply {
                        this.range = it.location!!.lspRange
                        this.severity = DiagnosticSeverity.Error
                        this.source = "k# semantic analysis"
                        this.message = it.toString()
                    }
                }
            })
            result = true
        }
        result
    }
}
