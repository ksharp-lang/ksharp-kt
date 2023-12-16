package org.ksharp.lsp.actions

fun documentActions(uri: String): Actions =
    actions {
        val publishErrorsAction = publishSemanticErrorsAction(uri)
        val abstractionsTypeInlayHint = abstractionsTypeInlayHintAction()
        val semanticTokensActions = semanticTokenAction()
        val codeModuleErrors = codeModuleErrorsAction {
            trigger {
                +publishErrorsAction
            }
        }
        val codeModule = codeModuleAction(uri) {
            trigger {
                +codeModuleErrors
                +abstractionsTypeInlayHint
            }
        }
        parseAction {
            trigger {
                +semanticTokensActions
                +codeModule
            }
        }
    }
