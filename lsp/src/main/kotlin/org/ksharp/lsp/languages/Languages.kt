package org.ksharp.lsp.languages

import org.eclipse.lsp4j.DocumentFilter

val supportedLanguages = listOf<DocumentFilter>(
    kSharpDocumentSelector
)
