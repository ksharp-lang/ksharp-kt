rootProject.name = "ksharp-kt"

pluginManagement {
    plugins {
        kotlin("jvm") version "1.8.20" apply false
        kotlin("kapt") version "1.8.20" apply false
        kotlin("plugin.serialization") version "1.8.20" apply false
        id("org.sonarqube") version "4.0.0.2929"
        jacoco
    }
}

include("typesystem")
include("common")
include("test")
include("parser")
include("nodes")
include("semantics")
include("module")
include("compiler")
include("lsp")
