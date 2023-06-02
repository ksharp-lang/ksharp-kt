package org.ksharp.lsp.languages

import org.eclipse.lsp4j.DocumentFilter

val kSharpDocumentSelector = DocumentFilter().apply {
    language = "ksharp"
}
