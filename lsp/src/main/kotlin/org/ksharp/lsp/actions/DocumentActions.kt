package org.ksharp.lsp.actions

fun documentActions(uri: String): Actions =
    actions {
        val publishErrorsAction = publishSemanticErrorsAction(uri)
        val semanticTokensAction = semanticTokensAction()
        val codeModuleErrorsAction = codeModuleErrorsAction {
            trigger {
                +publishErrorsAction
            }
        }
        val codeModule = codeModuleAction(uri) {
            trigger {
                +codeModuleErrorsAction
            }
        }
        parseAction {
            trigger {
                +semanticTokensAction
                +codeModule
            }
        }
        abstractionsHoverAction {
            dependsOn {
                +codeModule
            }
        }
    }
