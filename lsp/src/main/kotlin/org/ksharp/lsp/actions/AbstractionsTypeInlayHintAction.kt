package org.ksharp.lsp.actions

import org.eclipse.lsp4j.InlayHint
import org.ksharp.module.CodeModule

val AbstractionsTypeInlayHintAction = ActionId<List<InlayHint>>("AbstractionsTypeInlayHintAction")

fun ActionCatalog.abstractionsTypeInlayHintAction() =
    action<CodeModule, List<InlayHint>>(AbstractionsTypeInlayHintAction, emptyList()) {
    }
