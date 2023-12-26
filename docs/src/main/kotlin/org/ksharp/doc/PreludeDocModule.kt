package org.ksharp.doc

import org.ksharp.common.io.bufferView

val preludeDocModule: DocModule
    get() =
        String.Companion::class.java.getResourceAsStream("/org/ksharp/module/prelude.ksd")!!
            .use { input ->
                input.bufferView {
                    it.readDocModule()
                }
            }
