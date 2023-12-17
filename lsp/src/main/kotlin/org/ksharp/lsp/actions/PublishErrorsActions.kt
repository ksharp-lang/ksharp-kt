package org.ksharp.lsp.actions

import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.ksharp.common.Error
import org.ksharp.lsp.client.withClient
import org.ksharp.lsp.model.lspRange

val PublishSemanticErrorsAction = ActionId<Boolean>("PublishSemanticErrorsAction")

fun ActionCatalog.publishSemanticErrorsAction(uri: String) =
    action<List<Error>, Boolean>(PublishSemanticErrorsAction, false) {
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
