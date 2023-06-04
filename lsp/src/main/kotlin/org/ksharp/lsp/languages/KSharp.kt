package org.ksharp.lsp.languages

import org.eclipse.lsp4j.DocumentFilter

val kSharpLanguageId = "ksharp"

val kSharpDocumentSelector = DocumentFilter().apply {
    language = kSharpLanguageId
}
