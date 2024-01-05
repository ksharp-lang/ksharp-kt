package org.ksharp.lsp.capabilities.actions

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.MarkupKind
import org.eclipse.lsp4j.Position
import org.ksharp.lsp.actions.AbstractionsHoverAction
import org.ksharp.lsp.actions.ActionExecutionState
import org.ksharp.lsp.actions.ParseAction
import org.ksharp.lsp.actions.documentActions
import org.ksharp.lsp.client.Client
import org.ksharp.lsp.mocks.LanguageClientMock

class AbstractionHoverActionTest : StringSpec({
    "Calculate hover for abstractions" {
        Client.reset()
        val client = LanguageClientMock()
        Client.initialize(client)
        val actions = documentActions("doc")
        val state = ActionExecutionState()
        actions(state, AbstractionsHoverAction, Position().apply {
            line = 0
            character = 1
        })
        actions(state, ParseAction, "sum a b = a + b")
        state[AbstractionsHoverAction].get().shouldBe(
            Hover().apply {
                setContents(
                    MarkupContent(
                        MarkupKind.PLAINTEXT,
                        "sum/2 :: ((Num a) -> (Num a) -> (Num a))"
                    )
                )
            }
        )
    }
})
