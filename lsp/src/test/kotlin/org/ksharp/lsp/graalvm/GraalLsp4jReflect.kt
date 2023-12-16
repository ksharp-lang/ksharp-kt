package org.ksharp.lsp.graalvm

import io.kotest.core.spec.style.StringSpec
import org.reflections.Reflections
import org.reflections.scanners.Scanners

private fun reflectEntries(
    packageName: String
) =
    Reflections(packageName)
        .getAll(Scanners.SubTypes)
        .asSequence()
        .filter { !it.contains("$") }
        .filter { it.startsWith("$packageName.") }
        .joinToString(",\n") {
            """
                {
                    "name": "$it",
                    "allPublicMethods": true,
                    "allDeclaredConstructors": true,
                    "allDeclaredFields": true,
                    "allDeclaredMethods": true
                }
            """.trimIndent()
        }

class GraalLsp4jReflect : StringSpec({
    "Create reflect config for lsp4j" {
        val result = """
          [{
            "name": "com.google.gson.JsonObject",
            "allPublicMethods": true
          },
          {
            "name": "com.google.gson.JsonPrimitive",
            "allPublicMethods": true
          },
          {
            "name": "java.lang.Class",
            "allPublicMethods": true
          },
          {
            "name": "java.lang.Object",
            "allDeclaredFields": true,
            "allDeclaredMethods": true,
            "allPublicMethods": true,
            "allDeclaredConstructors": true
          },
          {
            "name": "java.lang.String",
            "allPublicMethods": true
          },
          {
            "name": "java.util.ArrayList",
            "allPublicConstructors": true
          },
          {
            "name": "java.util.Properties",
            "allPublicMethods": true
          },
          ${reflectEntries("org.eclipse.lsp4j")}]
        """.trimIndent()
        println(result)
    }
})
